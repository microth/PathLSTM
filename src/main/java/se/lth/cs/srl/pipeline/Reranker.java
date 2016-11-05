package se.lth.cs.srl.pipeline;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import se.lth.cs.srl.Parse;
import se.lth.cs.srl.SemanticRoleLabeler;
import se.lth.cs.srl.corpus.ArgMap;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.io.AllCoNLL09Reader;
import se.lth.cs.srl.languages.Language;
import se.lth.cs.srl.languages.Language.L;
import se.lth.cs.srl.options.ParseOptions;
import se.lth.cs.srl.util.BrownCluster;
import se.lth.cs.srl.util.DasFilter;
import se.lth.cs.srl.util.WordEmbedding;
import uk.ac.ed.inf.srl.features.DependencyPathEmbedding;
import uk.ac.ed.inf.srl.features.Feature;
import uk.ac.ed.inf.srl.features.FeatureGenerator;
import uk.ac.ed.inf.srl.features.FeatureSet;
import uk.ac.ed.inf.srl.ml.LearningProblem;
import uk.ac.ed.inf.srl.ml.Model;
import uk.ac.ed.inf.srl.ml.liblinear.Label;
import uk.ac.ed.inf.srl.ml.liblinear.LibLinearLearningProblem;
import uk.ac.ed.inf.srl.ml.liblinear.LibLinearModel;

public class Reranker extends SemanticRoleLabeler {

	public static final String FILENAME = "global";

	private final double alfa;
	private final boolean noPI;
	private final int aiBeam;
	private final int acBeam;

	private Model model;

	private List<String> argLabels;

	private FeatureSet allAIfeatures;
	private FeatureSet allACfeatures;
	private int[] sizeAIFeatures;
	private int[] sizeACFeatures;
	private int sizePipelineFeatures = 1000000 + (Language.getLanguage().getL()==L.ont5?1000000:0);

	private Map<String, Integer> calsMap;
	private int calsCounter = 1;

	private Pipeline pipeline;
	private ArgumentIdentifier aiModule;
	private ArgumentClassifier acModule;

	private int[] rankCount;
	private int zeroArgMapCount = 0;

	@SuppressWarnings("unchecked")
	public Reranker(ParseOptions parseOptions) throws ZipException,
			IOException, ClassNotFoundException {
		this(parseOptions.global_alfa, parseOptions.skipPI,
				parseOptions.global_aiBeam, parseOptions.global_acBeam);
		ZipFile zipFile = new ZipFile(parseOptions.modelFile);
		pipeline = parseOptions.skipPD ? Pipeline.fromZipFile(zipFile,
				new Step[] { Step.ai, Step.ac }) : noPI ? Pipeline.fromZipFile(
				zipFile, new Step[] { Step.pd, Step.ai, Step.ac }) : Pipeline
				.fromZipFile(zipFile);
		System.out.println("Loading reranker from " + zipFile.getName());
		if (noPI)
			System.out
					.println("Skipping predicate identification. Input is assumed to have predicates identified.");
		argLabels = pipeline.getArgLabels();
		populateRerankerFeatureSets(pipeline.getFeatureSets(), pipeline.getFg());
		
		/** TODO: load model for each POSPrefix in union(step.ac, step.ai) **/
		ObjectInputStream ois = new ObjectInputStream(
				zipFile.getInputStream(zipFile.getEntry(FILENAME)));
		model = (Model) ois.readObject();
		////((LibLinearModel)model).printWeights();
		
		calsMap = (Map<String, Integer>) ois.readObject();
		ois.close();
		int i = parseOptions.skipPD ? 0 : noPI ? 1 : 2;
		aiModule = (ArgumentIdentifier) pipeline.steps.get(i);
		acModule = (ArgumentClassifier) pipeline.steps.get(i + 1);
		zipFile.close();
	}

	private Reranker(double alfa, boolean noPI, int aiBeam, int acBeam) {
		this.alfa = alfa;
		this.noPI = noPI;
		this.aiBeam = aiBeam;
		this.acBeam = acBeam;
		rankCount = new int[aiBeam * acBeam];
	}

	@Override
	protected void parse(Sentence sen) {
		if (!Parse.parseOptions.skipPD) {
			pipeline.steps.get(0).parse(sen);
			if (!noPI)
				pipeline.steps.get(1).parse(sen);
		}
		
		for (Predicate pred : sen.getPredicates()) {
		
			List<ArgMap> candidates = acModule.beamSearch(pred,
					aiModule.beamSearch(pred, aiBeam), acBeam);
			List<Map<Integer, Double>> candidate_representations = new LinkedList<>();
			for (ArgMap argMap : candidates) {
				ArrayList<Integer> indices = new ArrayList<>();
				Map<Integer, Double> nonbinFeats = new TreeMap<>();
				
				collectPipelineFeatureIndices(pred, argMap, indices,
						nonbinFeats);
				
				collectGlobalFeatures(pred, argMap, indices, nonbinFeats);
				
				candidate_representations.add(nonbinFeats);
				
				List<Label> labels = model.classifyProb(indices, nonbinFeats);
				for (Label label : labels) {
					if (label.getLabel().equals(AbstractStep.NEGATIVE))
						continue;								
					argMap.setRerankProb(label.getProb());
					argMap.resetProb();
				}
			}
			int bestCandidateIndex = softMax(candidates); // Returns the index
															// of the best
															// argmap		
			if(bestCandidateIndex==-1) {
				zeroArgMapCount++;
				continue;
			}	
			
			rankCount[bestCandidateIndex]++;
			ArgMap bestCandidate = candidates.get(bestCandidateIndex);
			if (bestCandidate.size() == 0)
				zeroArgMapCount++;
			pred.setArgMap(bestCandidate);
		}
	}

	private boolean equal(ArgMap newargs, Map<Word, String> oldargs) {
		for (Word w : newargs.keySet()) {
			if (!oldargs.containsKey(w)
					|| !oldargs.get(w).equals(newargs.get(w)))
				return false;
		}
		for (Word w : oldargs.keySet()) {
			if (newargs.containsKey(w))
				return false;
		}
		return true;
	}

	private int softMax(List<ArgMap> argmaps) {
		// To perform softmax on the reranking probabilities, uncomment the
		// sumRR lines.
		// double sumProbs=0;
		// double sumRR=0;
		for (ArgMap am : argmaps) {
			double prob = am.getIdProb();
			if (am.size() != 0) // Empty argmaps have P(Labeling)==1
				prob *= Math.pow(am.getLblProb(), 1.0 / am.size());
			am.setProb(prob);
			// sumProbs+=prob;
			// sumRR+=am.getRerankProb();
		}
		double bestScore = 0;
		int bestIndex = -1;
		for (int i = 0, size = argmaps.size(); i < size; ++i) {
			ArgMap am = argmaps.get(i);
			// double localProb=am.getProb()/sumProbs;
			double localProb = am.getProb();
			// am.setRerankProb(am.getRerankProb()/sumRR);
			double weightedRerankProb = Math.pow(am.getRerankProb(), alfa);
			
		
			double score = localProb * weightedRerankProb;
			if (score > bestScore) {
				bestIndex = i;
				bestScore = score;
			} else if (score == bestScore) {
				System.err.println("!same score..");
			}

		}
		return bestIndex;
	}

	protected int getPOSPrefix(String pos, FeatureSet featureSet) {
		for (int i = 0; i<featureSet.POSPrefixes.length; i++) {
			if (pos.startsWith(featureSet.POSPrefixes[i]))
				return i;
		}
		return 0;
	}
	
	private Collection<Integer> collectPipelineFeatureIndices(Predicate pred,
			ArgMap argMap, Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats) {
		
		int aiprefix = getPOSPrefix(pred.getPOS(), allAIfeatures);
		int acprefix = getPOSPrefix(pred.getPOS(), allACfeatures);
		List<Feature> aiFeatures = allAIfeatures.get(allAIfeatures.POSPrefixes[aiprefix]);
		List<Feature> acFeatures = allACfeatures.get(allACfeatures.POSPrefixes[acprefix]);
		
		List<String> processedargs = new LinkedList<>();
		for (Word arg : argMap.keySet()) {		
			boolean hybrid = false;
			
			Integer aiOffset = 0;
			HashSet<Integer> currentInstance = new HashSet<>();
			HashSet<Integer> currentBackup = new HashSet<>();
			
			Map<Integer, Double> currentNonbinary = new TreeMap<>();
			boolean clear = false;
			List<NNThread> nnfeats = new LinkedList<>();
			Integer acOffset = 0;
						
			clear = false;
			nnfeats = new LinkedList<>();
			//tmp = null;
			for (Feature f : acFeatures) {
				if(f instanceof DependencyPathEmbedding) {
					if(!clear) acOffset = 0;
					f.addFeatures(currentInstance, currentNonbinary, pred, arg, acOffset+(aiprefix*250000+500000+(processedargs.indexOf(argMap.get(arg))>-1?(Language.getLanguage().getL()==L.ont5?66:52):0 + argLabels.indexOf(argMap.get(arg)))*4000), false);
//					NNThread t = new NNThread(f, currentInstance, pred, arg, acOffset+(aiprefix*250000+500000+(processedargs.indexOf(argMap.get(arg))>-1?(Language.getLanguage().getL()==L.ont5?66:52):0 + argLabels.indexOf(argMap.get(arg)))*4000));
//					nnfeats.add(t);
//					t.start();					
					clear = true;
				} else {
					f.addFeatures(currentInstance, currentNonbinary, pred, arg, acOffset, false);
				}
				acOffset += f.size(false);				
			}
			
			if(clear) {
				for(NNThread t : nnfeats) {
					// wait until done (in order of starting)
					try {
						t.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(1);
					}
					int i = t.getFeats().keySet().iterator().next();
					////System.err.println(i + ":" + t.getFeats().get(i));
					// add hidden state components to list of non-binary features
					
					////nonbinFeats.putAll(t.getFeats());
					for(Entry<Integer, Double> e : t.getFeats().entrySet()) {
						if(e.getValue()!=0.0) currentNonbinary.put(e.getKey(), e.getValue());
					}
				}
				currentInstance.clear();
			}
								
			indices.addAll(currentInstance);
			if(hybrid) indices.addAll(currentBackup);
			nonbinFeats.putAll(currentNonbinary);

			processedargs.add(argMap.get(arg));
		}
		return indices;
	}
	
	private class NNThread extends Thread {
		Feature f;
		Collection<Integer> indices;
		Predicate p;
		Word a;
		int offset;
		Map<Integer, Double> feats;
		
		private NNThread(Feature f, Collection<Integer> indices, Predicate p, Word a, int offset) {
			this.f = f;
			this.indices = indices;
			this.p = p;
			this.a = a;
			this.offset = offset;
			this.feats = new TreeMap<>();
		}
		
		@Override
		public void run() {
			try {
				f.addFeatures(indices, feats, p, a, offset, false);
			} catch(Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		private Map<Integer, Double> getFeats() {
			return feats;
		}
	}

	/**
	 * This method extracts the one global feature (core argument label
	 * sequence). It also adds new features to the map. The method is meant to
	 * be run during training.
	 * 
	 * @param pred
	 *            the predicate
	 * @param argMap
	 *            the argmap
	 * @param indices
	 *            the container to add the index too
	 */
	private void addAndCollectGlobalFeatures(Predicate pred, ArgMap argMap,
			Collection<Integer> indices, Map<Integer, Double> nonbinFeats) {
		int offset = 0;
		
		if ((Parse.parseOptions != null && Parse.parseOptions.globalFeats)) {
			indices.add(sizePipelineFeatures + argMap.size());
			offset = 10;
		}

		String cals = Language.getLanguage().getCoreArgumentLabelSequence(pred,
				argMap);
		Integer index = calsMap.get(cals); // Need to build this beforehand --
											// can't be done, since we don't
											// know what faulty CALS we will
											// generate during training
		if (index == null) {
			calsMap.put(cals, calsCounter);
			index = calsCounter++;
		}

		indices.add(offset + sizePipelineFeatures + index);/**/
	}

	/**
	 * This method extract the one global feature (core argument label
	 * sequence). It is meant to be used during parsing and does not add new
	 * features.
	 * 
	 * @param pred
	 *            the predicate
	 * @param argMap
	 *            the argmap
	 * @param indices
	 *            the container to add the index too
	 */
	private void collectGlobalFeatures(Predicate pred, ArgMap argMap,
			Collection<Integer> indices, Map<Integer, Double> nonbinFeats) {
		int offset = 0;
		if ((Parse.parseOptions != null && Parse.parseOptions.globalFeats)) {
			indices.add(sizePipelineFeatures + argMap.size());
			offset = 10;
		}


		String cals = Language.getLanguage().getCoreArgumentLabelSequence(pred,
				argMap);
		Integer index = calsMap.get(cals);		
		if (index != null) {
			indices.add(offset + sizePipelineFeatures + index);/**/
		}
	}

	private void populateRerankerFeatureSets(Map<Step, FeatureSet> featureSets,
			FeatureGenerator fg) {
		/** TODO: all of this needs could be made POSPrefix specific (cf. TODO comments above) **/
		/*aiFeatures = new ArrayList<Feature>();
		acFeatures = new ArrayList<Feature>();
		for (Entry<String, List<Feature>> entry : featureSets.get(Step.ai)
				.entrySet())
			aiFeatures.addAll(entry.getValue());
		for (Entry<String, List<Feature>> entry : featureSets.get(Step.ac)
				.entrySet())
			acFeatures.addAll(entry.getValue());

		sizeAIFeatures = 0;
		sizeACFeatures = 0;
		for (Feature f : aiFeatures)
			sizeAIFeatures += f.size(false);
		for (Feature f : acFeatures)
			sizeACFeatures += f.size(false);
		sizePipelineFeatures = sizeAIFeatures + argLabels.size()
				* sizeACFeatures;*/
		
		allAIfeatures = featureSets.get(Step.ai);
		sizeAIFeatures = new int[allAIfeatures.POSPrefixes.length];		
		for(int i=0; i<sizeAIFeatures.length; i++)
			for (Feature f : allAIfeatures.get(allAIfeatures.POSPrefixes[i])) sizeAIFeatures[i] += f.size(false);
		
		allACfeatures = featureSets.get(Step.ac);
		sizeACFeatures = new int[allACfeatures.POSPrefixes.length];		
		for(int i=0; i<sizeACFeatures.length; i++)
			for (Feature f : allACfeatures.get(allACfeatures.POSPrefixes[i])) sizeACFeatures[i] += f.size(false);

	}

	private static double partitionBestArgMaps(List<ArgMap> candidates,
			Map<Word, String> goldStandard, Set<ArgMap> bestArgMaps) {
		double bestScore = 0;
		for (ArgMap candidate : candidates) {
			double curScore = candidate.computeScore(goldStandard);
			if (curScore > bestScore) {
				bestScore = curScore;
				bestArgMaps.clear();
				bestArgMaps.add(candidate);
			} else if (curScore == bestScore) {
				bestArgMaps.add(candidate);
			}
		}
		candidates.removeAll(bestArgMaps);
		return bestScore;
	}

	@Override
	protected String getSubStatus() {
		StringBuilder ret = new StringBuilder("Reranker status:\n");
		ret.append("AI beam:\t\t" + aiBeam + "\n");
		ret.append("AC beam:\t\t" + acBeam + "\n");
		ret.append("Alfa:\t\t\t" + alfa + "\n");
		ret.append("\n");
		ret.append("Reranker choices:\n");
		ret.append("Rank\tFrequency\n");
		for (int i = 0; i < rankCount.length; ++i) {
			ret.append((i + 1) + "\t" + rankCount[i] + "\n");
		}
		ret.append("\n");
		ret.append("Number of zero size argmaps:\t" + zeroArgMapCount + "\n");
		return ret.toString();
	}

}
