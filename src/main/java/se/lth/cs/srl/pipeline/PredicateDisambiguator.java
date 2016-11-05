package se.lth.cs.srl.pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import se.lth.cs.srl.Parse;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.PredicateReference;
import se.lth.cs.srl.corpus.Sentence;
import uk.ac.ed.inf.srl.features.Feature;
import uk.ac.ed.inf.srl.features.FeatureSet;
import uk.ac.ed.inf.srl.ml.LearningProblem;
import uk.ac.ed.inf.srl.ml.Model;
import uk.ac.ed.inf.srl.ml.liblinear.LibLinearLearningProblem;

public class PredicateDisambiguator implements PipelineStep {

	public static final String FILE_PREFIX = "pd_";

	private FeatureSet featureSet;
	private PredicateReference predicateReference;

	// This is a map filename -> model
	protected Map<String, Model> models;

	private Map<String, List<String>> lexicon;
	private Map<String, List<Predicate>> instances;

	public PredicateDisambiguator(FeatureSet featureSet,
			PredicateReference predicateReference) {
		this.featureSet = featureSet;
		this.predicateReference = predicateReference;

		if (Parse.parseOptions != null && Parse.parseOptions.framenetdir != null)
			lexicon = createLexicon(Parse.parseOptions.framenetdir+"/frame/");
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
				while ((line = br.readLine()) != null) {
					if (!line.contains("<lexeme "))
						continue;
					String lexeme = line.replaceAll(".*name=\"", "")
							.replaceAll("\".*", "");
					if (!retval.containsKey(lexeme))
						retval.put(lexeme, new LinkedList<String>());
					retval.get(lexeme).add(framename);
				}
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

	public void parse(Sentence s) {
		for (Predicate pred : s.getPredicates()) {
			String POSPrefix = getPOSPrefix(pred);
			String lemma = pred.getLemma();
			String sense = "-1";
			if (lexicon != null && lexicon.containsKey("lemma")
					&& lexicon.get(lemma).size() == 1) {
				pred.setSense(lexicon.get(lemma).get(0));
				continue;
			}

			if (POSPrefix == null
					&& (Parse.parseOptions != null && Parse.parseOptions.framenetdir != null))
				POSPrefix = featureSet.POSPrefixes[0];

			if (POSPrefix == null) {
				sense = predicateReference.getSimpleSense(pred, null);
			} else {
				String filename = predicateReference.getFileName(lemma,
						POSPrefix);
				if (filename == null) {
					sense = predicateReference.getSimpleSense(pred, POSPrefix);
				} else {
					Model m = getModel(filename);
					Collection<Integer> indices = new TreeSet<Integer>();
					Map<Integer, Double> nonbinFeats = new TreeMap<Integer, Double>();
					Integer offset = 0;
					for (Feature f : featureSet.get(POSPrefix)) {
						f.addFeatures(indices, nonbinFeats, pred, null, offset,
								false);
						offset += f.size(false);
					}

					// no framenet
					if ((Parse.parseOptions != null && Parse.parseOptions.framenetdir == null)) {
						Integer label = m.classify(indices, nonbinFeats);
						sense = predicateReference.getSense(lemma, POSPrefix,
								label);
						// framenet
					} else {
						boolean foundone = false;

						/** with lexicon! **/
						List<uk.ac.ed.inf.srl.ml.liblinear.Label> labels = m
								.classifyProb(indices, nonbinFeats);
						if (!lexicon.containsKey(lemma))
							sense = predicateReference.getSense(lemma,
									POSPrefix, labels.get(0).getLabel());
						// + "," +
						// predicateReference.getSense(lemma,POSPrefix,labels.get(1).getLabel());
						else {
							for (uk.ac.ed.inf.srl.ml.liblinear.Label l : labels) {
								if (lexicon.get(lemma).contains(
										predicateReference.getSense(lemma,
												POSPrefix, l.getLabel()))) {
									sense = predicateReference.getSense(lemma,
											POSPrefix, l.getLabel());
									// pred.addPotentialSense(sense,
									// l.getProb());
									pred.setSense(sense);
									foundone = true;
									/**/break;/**/
									// }
								}
							}

							if (sense.equals("-1")) {
								sense = predicateReference.getSense(lemma,
										POSPrefix, labels.get(0).getLabel());
							}
						}
					}

				}
			}

			pred.setSense(sense);
		}
	}

	private Model getModel(String filename) {
		return models.get(filename);
	}

	private String getPOSPrefix(Predicate pred) {
		for (String prefix : featureSet.POSPrefixes) {
			if (pred.getPOS().startsWith(prefix))
				return prefix;
		}
		return null;
	}

	@Override
	public void readModels(ZipFile zipFile) throws IOException,
			ClassNotFoundException {
		models = new HashMap<String, Model>();
		AbstractStep.readModels(zipFile, models, getModelFileName());
	}

	private String getModelFileName() {
		return FILE_PREFIX + ".models";
	}

	@Override
	public void prepareLearning(int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public void prepareLearning() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void extractInstances(Sentence s) {
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

}
