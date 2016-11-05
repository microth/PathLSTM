package se.lth.cs.srl.pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeMap;
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

public class ArgumentClassifier extends ArgumentStep {

	private static final String FILEPREFIX = "ac_";

	private List<String> argLabels;

	private Map<String, List<String>> roles;

	public ArgumentClassifier(FeatureSet fs, List<String> argLabels) {
		super(fs);
		this.argLabels = argLabels;
		if (Parse.parseOptions != null && Parse.parseOptions.framenetdir != null)
			roles = createLexicon(Parse.parseOptions.framenetdir + "/frame/");
	}

	private Map<String, List<String>> createLexicon(String lexicondir) {
		Map<String, List<String>> retval = new HashMap<String, List<String>>();
		File[] files = new File(lexicondir).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});

		BufferedReader br = null;
		for (File f : files) {
			try {
				br = new BufferedReader(new FileReader(f));
				String framename = f.getName().replaceAll("\\..*", "");
				String line = "";
				List<String> FEs = new LinkedList<String>();
				while ((line = br.readLine()) != null) {
					if (!line.contains("<FE "))
						continue;
					String FE = line.replaceAll(".*name=\"", "").replaceAll(
							"\".*", "");
					FEs.add(FE);
				}
				retval.put(framename, FEs);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}

		return retval;
	}

	private void collectRestrictedFeatures(Predicate pred, Word arg,
			String POSPrefix, Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, String[] strings) {

		if (POSPrefix != null) {
			String label = pred.getArgMap().get(arg);
			if (label != null/* && label.matches("A[0-5]") */) {
				if (arg.getPOS().equals("IN") || arg.getPOS().equals("TO"))
					System.err.print(label
							+ " "
							+ arg.getForm()
							+ (arg.getChildren().size() > 0 ? "."
									+ arg.getChildren().iterator().next()
											.getForm() : ""));
				else
					System.err.print(label + " " + arg.getForm());

				Integer offset = 0;
				for (String s : strings) {
					indices = new TreeSet<Integer>();
					for (Feature f : featureSet.get(POSPrefix)) {
						if (f.getName().equals(s)) {
							f.addFeatures(indices, nonbinFeats, pred, arg,
									offset, false);
							offset += f.size(false);
						}
					}
				}
			}
		}

		return;
	}

	@Override
	public void parse(Sentence s) {
		for (Predicate pred : s.getPredicates()) {
			Map<Word, String> argMap = pred.getArgMap();
			/** if(pred.getCandSenses()<2) { **/
			for (Word arg : argMap.keySet()) {
				if ((Parse.parseOptions != null && Parse.parseOptions.framenetdir ==null)) {
						Integer label = super.classifyInstance(pred, arg);
					argMap.put(arg, argLabels.get(label));
				} else {
					// modified
					String POSPrefix = getPOSPrefix(pred.getPOS());
					if (POSPrefix == null) {
						POSPrefix = featureSet.POSPrefixes[0];
					}
					Model m = models.get(POSPrefix);
					Collection<Integer> indices = new TreeSet<Integer>();
					Map<Integer, Double> nonbinFeats = new TreeMap<Integer, Double>();
					collectFeatures(pred, arg, POSPrefix, indices,
							nonbinFeats);
					List<Label> labels = m.classifyProb(indices, nonbinFeats);

					for (Label l : labels) {
						String tmp = argLabels.get(l.getLabel());
						if (!roles.containsKey(pred.getSense())) {
							argMap.put(arg, tmp);
							System.err.println("Frame not found: "
									+ pred.getSense());
							break;
						}
						if (roles.get(pred.getSense()).contains(tmp)) {
							argMap.put(arg, tmp);
							// scores[i] += l.getProb();
							break;
						}
					}
				}

			}
		}
	}

	@Override
	protected Integer getLabel(Predicate pred, Word arg) {
		return argLabels.indexOf(pred.getArgMap().get(arg));
	}


	@Override
	protected String getModelFileName() {
		return FILEPREFIX + ".models";
	}

	List<ArgMap> beamSearch(Predicate pred, List<ArgMap> candidates,
			int beamSize) {
		ArrayList<ArgMap> ret = new ArrayList<ArgMap>();
		String POSPrefix = super.getPOSPrefix(pred.getPOS());
		if (POSPrefix == null)
			POSPrefix = super.featureSet.POSPrefixes[0]; // TODO fix me. or
															// discard examples
															// with wrong
															// POS-tags
		Model model = models.get(POSPrefix);
		for (ArgMap argMap : candidates) { // Candidates from AI module
			ArrayList<ArgMap> branches = new ArrayList<ArgMap>();
			branches.add(argMap);
			SortedSet<ArgMap> newBranches = new TreeSet<ArgMap>(
					ArgMap.REVERSE_PROB_COMPARATOR);
			for (Word arg : argMap.keySet()) { // TODO we can optimize this
												// severely by not computing the
												// labels for the same arg more
												// than once.
				Collection<Integer> indices = new TreeSet<Integer>();
				Map<Integer, Double> nonbinFeats = new HashMap<Integer, Double>();
				super.collectFeatures(pred, arg, POSPrefix, indices,
						nonbinFeats);
				
				List<Label> probs = null;
				int numoutputs = 0;
				float[] outputs = null; 
				if(	(Parse.parseOptions!=null && Parse.parseOptions.externalNNs)) {
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
					probs = new ArrayList<Label>(outputs.length);
					for (int j = 0; j < outputs.length; ++j) {
						probs.add(new Label(j, (double)(outputs[j]/(double)numoutputs)));
					}
					Collections.sort(probs, Collections.reverseOrder());
				} else
					probs = model.classifyProb(indices, nonbinFeats);
				
				for (ArgMap branch : branches) { // Study this branch
					for (int i = 0; i < beamSize && i < probs.size(); ++i) { // and
																				// create
																				// k
																				// new
																				// branches
																				// with
																				// current
																				// arg
						Label label = probs.get(i);
						ArgMap newBranch = new ArgMap(branch);
						
						// HACK to prevent index out of bound?!
						if(label.getLabel()>=argLabels.size())
							continue;
						
						newBranch.put(arg, argLabels.get(label.getLabel()),
								label.getProb());
						newBranches.add(newBranch);
					}
				}
				branches.clear();
				Iterator<ArgMap> it = newBranches.iterator();
				for (int i = 0; i < beamSize && it.hasNext(); ++i) {
					ArgMap cur = it.next();
					branches.add(cur);
				}
				newBranches.clear();
			}
			// When this loop finishes, we have the best 4 in branches
			for (int i = 0, size = branches.size(); i < beamSize && i < size; ++i) {
				ArgMap cur = branches.get(i);			
				
				cur.setLblProb(cur.getProb());
				cur.resetProb();
				ret.add(cur);
			}
		}
		return ret;
	}
	
	public int indexOfLabel(String label) {
		return argLabels.indexOf(label);
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
