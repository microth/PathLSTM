package se.lth.cs.srl.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipOutputStream;

import se.lth.cs.srl.Parse;
import se.lth.cs.srl.corpus.ArgMap;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import uk.ac.ed.inf.srl.features.DependencyCPathEmbedding;
import uk.ac.ed.inf.srl.features.DependencyIPathEmbedding;
import uk.ac.ed.inf.srl.features.DependencyPathEmbedding;
import uk.ac.ed.inf.srl.features.Feature;
import uk.ac.ed.inf.srl.features.FeatureSet;
import uk.ac.ed.inf.srl.ml.Model;
import uk.ac.ed.inf.srl.ml.liblinear.Label;

public class ArgumentIdentifier extends ArgumentStep {

	private static final String FILEPREFIX = "ai_";

	public ArgumentIdentifier(FeatureSet fs) {
		super(fs);
	}


	@Override
	public void parse(Sentence s) {
		for (Predicate pred : s.getPredicates()) {
			// System.err.println("Looking for arguments of " + pred.getSense()
			// + " ...");
			for (int i = 1, size = s.size(); i < size; ++i) {
				Word arg = s.get(i);

				Integer label = super.classifyInstance(pred, arg);
				if (label.equals(POSITIVE)) {
					pred.addArgMap(arg, "ARG");
				}

			}
		}

	}

	@Override
	protected Integer getLabel(Predicate pred, Word arg) {
		return pred.getArgMap().containsKey(arg) ? POSITIVE : NEGATIVE;
	}

	@Override
	protected String getModelFileName() {
		return FILEPREFIX + ".models";
	}

	List<ArgMap> beamSearch(Predicate pred, int beamSize) {
		List<ArgMap> candidates = new ArrayList<>();
		candidates.add(new ArgMap());
		Sentence s = pred.getMySentence();
		SortedSet<ArgMap> newCandidates = new TreeSet<>(
                ArgMap.REVERSE_PROB_COMPARATOR);
		String POSPrefix = super.getPOSPrefix(pred.getPOS());
		if (POSPrefix == null)
			POSPrefix = super.featureSet.POSPrefixes[0]; // TODO fix me. or
															// discard examples
															// with wrong
															// POS-tags
		Model model = models.get(POSPrefix);

		for (int i = 1, size = s.size(); i < size; ++i) {
			newCandidates.clear();
			Word arg = s.get(i);
			Collection<Integer> indices = new TreeSet<>();
			Map<Integer, Double> nonbinFeats = new TreeMap<>();
			super.collectFeatures(pred, arg, POSPrefix, indices, nonbinFeats);

			List<Label> probs = null;
			int numoutputs = 0;
			float[] outputs = null; 
			if((Parse.parseOptions!=null && Parse.parseOptions.externalNNs)) {
				for(Feature f : featureSet.get(POSPrefix)) {				
					if(f instanceof DependencyPathEmbedding) {
						numoutputs++;
						if(outputs==null) outputs = ((DependencyPathEmbedding) f).getOutput();
						else {
							float[] tmp = ((DependencyPathEmbedding) f).getOutput();
							for(int j=0; j<outputs.length; j++)
								outputs[j] += tmp[j];
						}
					}				
				}
			}
			if(numoutputs>0) {
				probs = new ArrayList<>(outputs.length);
				for (int j = 0; j < outputs.length; ++j) {
					probs.add(new Label(j, (double)(outputs[j]/(double)numoutputs)));
				}
				Collections.sort(probs, Collections.reverseOrder());			
			} else
				probs = model.classifyProb(indices, nonbinFeats);			
			
			for (ArgMap argmap : candidates) {
				for (Label label : probs) {
					ArgMap branch = new ArgMap(argmap);
					if (label.getLabel().equals(POSITIVE)) {
						branch.put(arg, "ARG", label.getProb());
					} else {
						branch.multiplyProb(label.getProb());
					}
					newCandidates.add(branch);
				}
			}
			candidates.clear();
			Iterator<ArgMap> it = newCandidates.iterator();
			for (int j = 0; j < beamSize && it.hasNext(); j++)
				candidates.add(it.next());
		}
		for (ArgMap argmap : candidates) {
			argmap.setIdProb(argmap.getProb());
			argmap.resetProb();
		}
		return candidates;
	}


	@Override
	public void prepareLearning() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void done() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void train() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void writeModels(ZipOutputStream zos) throws IOException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void prepareLearning(int i) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void extractInstances(Sentence s) {
		// TODO Auto-generated method stub
		
	}
}
