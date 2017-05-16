package uk.ac.ed.inf.srl.features;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipFile;

import dmonner.xlbp.compound.AbstractWeightedCompound;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word.WordData;
import uk.ac.ed.inf.srl.features.AnySetFeature;
import uk.ac.ed.inf.srl.features.ArgDependentAttrFeature;
import uk.ac.ed.inf.srl.features.ArgDependentBrown;
import uk.ac.ed.inf.srl.features.ArgDependentEmbedding;
import uk.ac.ed.inf.srl.features.ArgDependentFeatsFeature;
import uk.ac.ed.inf.srl.features.BrownPathFeature;
import uk.ac.ed.inf.srl.features.ChildSetFeature;
import uk.ac.ed.inf.srl.features.ContinuousFeature;
import uk.ac.ed.inf.srl.features.DepSubCatFeature;
import uk.ac.ed.inf.srl.features.DependencyCPathEmbedding;
import uk.ac.ed.inf.srl.features.DependencyIPathEmbedding;
import uk.ac.ed.inf.srl.features.DistanceFeature;
import uk.ac.ed.inf.srl.features.EmbeddingPath;
import uk.ac.ed.inf.srl.features.Feature;
import uk.ac.ed.inf.srl.features.FeatureFile;
import uk.ac.ed.inf.srl.features.FeatureName;
import uk.ac.ed.inf.srl.features.FeatureSet;
import uk.ac.ed.inf.srl.features.PBLabelFeature;
import uk.ac.ed.inf.srl.features.PathFeature;
import uk.ac.ed.inf.srl.features.PathItemSetFeature;
import uk.ac.ed.inf.srl.features.PathLengthFeature;
import uk.ac.ed.inf.srl.features.PositionFeature;
import uk.ac.ed.inf.srl.features.PredDependentAttrFeature;
import uk.ac.ed.inf.srl.features.PredDependentBrown;
import uk.ac.ed.inf.srl.features.PredDependentEmbedding;
import uk.ac.ed.inf.srl.features.PredDependentFeatsFeature;
import uk.ac.ed.inf.srl.features.QContinuousSetFeature;
import uk.ac.ed.inf.srl.features.QContinuousSingleFeature;
import uk.ac.ed.inf.srl.features.QDoubleChildSetFeature;
import uk.ac.ed.inf.srl.features.QSetSetFeature;
import uk.ac.ed.inf.srl.features.QSingleSetFeature;
import uk.ac.ed.inf.srl.features.QSingleSingleFeature;
import uk.ac.ed.inf.srl.features.SameSubTreeFeature;
import uk.ac.ed.inf.srl.features.SetFeature;
import uk.ac.ed.inf.srl.features.SingleFeature;
import uk.ac.ed.inf.srl.features.SpanLengthFeature;
import uk.ac.ed.inf.srl.features.SubCatSizeFeature;
import uk.ac.ed.inf.srl.features.TargetWord;
import se.lth.cs.srl.pipeline.Step;
import se.lth.cs.srl.util.BrownCluster;
import se.lth.cs.srl.util.BrownCluster.ClusterVal;
import se.lth.cs.srl.util.WordEmbedding;
import uk.ac.ed.inf.srl.lstm.DataConverter;
import uk.ac.ed.inf.srl.lstm.EmbeddingNetwork;

public class FeatureGenerator implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<FeatureName, Feature> cache;
	private Map<String, Feature> qcache;

	private Map<String, EmbeddingNetwork> nets;
	
	private DataConverter dc;
	
	private BufferedReader br;
	
	public FeatureGenerator() {
		br = null;
		cache = new HashMap<>();
		qcache = new HashMap<>();
	}

	public Map<Step, FeatureSet> readFeatureFiles(Map<Step, File> files,
			BrownCluster bc, WordEmbedding we) throws IOException {
		Map<Step, FeatureSet> featureSets = new HashMap<>();
		Map<String, List<String>> piNames = FeatureFile.readFile(files
				.get(Step.pi));
		featureSets.put(Step.pi, createFeatureSet(piNames, true, bc, we));
		for (Step s : new Step[] { Step.pd, Step.ai, Step.ac /* ,Step.po,Step.ao */}) {
			Map<String, List<String>> names = FeatureFile
					.readFile(files.get(s));
			featureSets.put(s, createFeatureSet(names, false, bc, we));
		}
		return featureSets;
	}

	private FeatureSet createFeatureSet(Map<String, List<String>> names,
			boolean includeAllWords, BrownCluster bc, WordEmbedding we) {
		Map<String, List<Feature>> fs = new HashMap<>();
		for (String POSPrefix : names.keySet()) {
			List<Feature> list = new ArrayList<>();
			fs.put(POSPrefix, list);
			for (String featureNameStr : names.get(POSPrefix)) {
				if (featureNameStr.contains("+")
						&& featureNameStr.contains("Embedding")) {
					String[] n = featureNameStr.split("\\+");
					FeatureName fn2 = FeatureName.valueOf(n[1]);
					for (int i = 0; i < WordEmbedding.DEF_DIMENSIONALITY; i++) {
						FeatureName fn1 = FeatureName.valueOf(n[0]
								+ String.format("%03d", i));
						list.add(getQFeature(fn1, fn2, includeAllWords,
								POSPrefix, bc, we));
					}
				} else if (featureNameStr.contains("+")) {
					String[] n = featureNameStr.split("\\+");
					FeatureName fn1 = FeatureName.valueOf(n[0]);
					FeatureName fn2 = FeatureName.valueOf(n[1]);
					list.add(getQFeature(fn1, fn2, includeAllWords, POSPrefix,
							bc, we));
				} else if (featureNameStr.contains("Embedding")) {
					if(featureNameStr.startsWith("PathEmbedding")) {
						String step = featureNameStr.substring(13/*,16*/);
						if(nets==null) {
							System.err.println("Creating DataConverter...");
							nets = new TreeMap<>();
						}
						if(!nets.containsKey(step)) {
							System.err.println("Loading network " + step);
							try {
								//EmbeddingNetwork net = (EmbeddingNetwork)new ObjectInputStream(new FileInputStream(step/*+"_network"*/)).readObject();
								ZipFile z = new ZipFile(step);
								EmbeddingNetwork net = (EmbeddingNetwork)new ObjectInputStream(z.getInputStream(z.getEntry("network.o"))).readObject();
								nets.put(step,  net);
							} catch (Exception e) {
								e.printStackTrace();
								System.exit(1);
							}	
						}
						if(dc==null) {
							System.err.println(featureNameStr +"\t" + featureNameStr.contains("ONT5"));
							dc = new DataConverter(nets.get(step), featureNameStr.contains("ONT5"));
						}
						EmbeddingNetwork net = nets.get(step);
						int hid1 = 0;
						if(net.getComponentByName("Hidden")!=null)
							hid1 = ((AbstractWeightedCompound)net.getComponentByName("Hidden")).getOutput().getActivations().length;
						int hid2 = 0;
						if(net.getComponentByName("ExtraHidden")!=null)
							hid2 = ((AbstractWeightedCompound)net.getComponentByName("ExtraHidden")).getOutput().getActivations().length;						
						//for(int dim=0; dim<(hid1+hid2); dim++) {
							Feature f;
							if(step.startsWith("ac"))
								 f = new DependencyCPathEmbedding(FeatureName.valueOf(featureNameStr), null, POSPrefix, true, nets.get(step), dc, hid1+hid2); 
							else f = new DependencyIPathEmbedding(FeatureName.valueOf(featureNameStr), null, POSPrefix, true, nets.get(step), dc, hid1+hid2);
							list.add(f);
							cache.put(FeatureName.valueOf(featureNameStr), f);
						//} 
					} else {
						for (int i = 0; i < WordEmbedding.DEF_DIMENSIONALITY; i++) {
							FeatureName fn = FeatureName.valueOf(featureNameStr
									+ String.format("%03d", i));
							list.add(getFeature(fn, includeAllWords, POSPrefix, bc, we));
						}
					}
				} else {
					FeatureName fn = FeatureName.valueOf(featureNameStr);
					list.add(getFeature(fn, includeAllWords, POSPrefix, bc, we));
				}
			}
		}
		return new FeatureSet(fs);
	}

	public Feature getFeature(String featureNameString,
			boolean includeAllWords, String POSPrefix, BrownCluster bc,
			WordEmbedding we) {
		if (featureNameString.contains("+")) {
			String[] s = featureNameString.split("\\+");
			FeatureName fn1 = FeatureName.valueOf(s[0]);
			FeatureName fn2 = FeatureName.valueOf(s[1]);
			return getQFeature(fn1, fn2, includeAllWords, POSPrefix, bc, we);
		} else {
			FeatureName fn = FeatureName.valueOf(featureNameString);
			return getFeature(fn, includeAllWords, POSPrefix, bc, we);
		}
	}

	public Feature getFeature(FeatureName fn, boolean includeAllWords,
			String POSPrefix, BrownCluster bc, WordEmbedding we) {
		Feature ret;
		if (cache.containsKey(fn)) {
			ret = cache.get(fn);
			ret.addPOSPrefix(POSPrefix);
			return ret;
		} else {

			switch (fn) {
			case PredWord:
				ret = new PredDependentAttrFeature(fn, WordData.Form,
						TargetWord.Pred, includeAllWords, POSPrefix);
				break;
			case PredLemma:
				ret = new PredDependentAttrFeature(fn, WordData.Lemma,
						TargetWord.Pred, includeAllWords, POSPrefix);
				break;
			case PredPOS:
				ret = new PredDependentAttrFeature(fn, WordData.POS,
						TargetWord.Pred, includeAllWords, POSPrefix);
				break;
			case PredDeprel:
				ret = new PredDependentAttrFeature(fn, WordData.Deprel,
						TargetWord.Pred, includeAllWords, POSPrefix);
				break;
			case PredLemmaSense:
				ret = new PredDependentAttrFeature(fn, WordData.Pred,
						TargetWord.Pred, false, POSPrefix);
				break;
			case OntPredLemmaSense:
				ret = new PredDependentAttrFeature(fn, WordData.OntPred,
						TargetWord.Pred, false, POSPrefix);
				break;
			case PredFeats:
				ret = new PredDependentFeatsFeature(fn, TargetWord.Pred,
						includeAllWords, POSPrefix);
				break;
			case PredVoice:
				ret = new PredDependentAttrFeature(fn, WordData.Voice,
						TargetWord.Pred, includeAllWords, POSPrefix);
				break;

			case PredParentWord:
				ret = new PredDependentAttrFeature(fn, WordData.Form,
						TargetWord.PredParent, includeAllWords, POSPrefix);
				break;
			case PredParentPOS:
				ret = new PredDependentAttrFeature(fn, WordData.POS,
						TargetWord.PredParent, includeAllWords, POSPrefix);
				break;
			case PredParentFeats:
				ret = new PredDependentFeatsFeature(fn, TargetWord.PredParent,
						includeAllWords, POSPrefix);
				break;

			case PredSubjWord:
				ret = new PredDependentAttrFeature(fn, WordData.Form,
						TargetWord.PredSubj, includeAllWords, POSPrefix);
				break;
			case PredSubjPOS:
				ret = new PredDependentAttrFeature(fn, WordData.POS,
						TargetWord.PredSubj, includeAllWords, POSPrefix);
				break;

			case DepSubCat:
				ret = new DepSubCatFeature(includeAllWords, POSPrefix);
				break;
			case ChildDepSet:
				ret = new ChildSetFeature(fn, WordData.Deprel, includeAllWords,
						POSPrefix);
				break;
			case ChildWordSet:
				ret = new ChildSetFeature(fn, WordData.Form, includeAllWords,
						POSPrefix);
				break;
			case ChildPOSSet:
				ret = new ChildSetFeature(fn, WordData.POS, includeAllWords,
						POSPrefix);
				break;

			case ArgWord:
				ret = new ArgDependentAttrFeature(fn, WordData.Form,
						TargetWord.Arg, POSPrefix);
				break;
			case ArgLemma:
				ret = new ArgDependentAttrFeature(fn, WordData.Lemma,
						TargetWord.Arg, POSPrefix);
				break;
			case ArgPOS:
				ret = new ArgDependentAttrFeature(fn, WordData.POS,
						TargetWord.Arg, POSPrefix);
				break;
			case ArgFeats:
				ret = new ArgDependentFeatsFeature(fn, TargetWord.Arg,
						POSPrefix);
				break;
			case ArgDeprel:
				ret = new ArgDependentAttrFeature(fn, WordData.Deprel,
						TargetWord.Arg, POSPrefix);
				break;
			case ArgVoice:
				ret = new ArgDependentAttrFeature(fn, WordData.Voice,
						TargetWord.Arg, POSPrefix);
				break;

			case FirstWord:
				ret = new ArgDependentAttrFeature(fn, WordData.Form,
						TargetWord.FirstWord, POSPrefix);
				break;
			case FirstLemma:
				ret = new ArgDependentAttrFeature(fn, WordData.Lemma,
						TargetWord.FirstWord, POSPrefix);
				break;
			case FirstPOS:
				ret = new ArgDependentAttrFeature(fn, WordData.POS,
						TargetWord.FirstWord, POSPrefix);
				break;
			case FirstDeprel:
				ret = new ArgDependentAttrFeature(fn, WordData.Deprel,
						TargetWord.FirstWord, POSPrefix);
				break;

			case SecondWord:
				ret = new ArgDependentAttrFeature(fn, WordData.Form,
						TargetWord.SecondWord, POSPrefix);
				break;
			case SecondLemma:
				ret = new ArgDependentAttrFeature(fn, WordData.Lemma,
						TargetWord.SecondWord, POSPrefix);
				break;
			case SecondPOS:
				ret = new ArgDependentAttrFeature(fn, WordData.POS,
						TargetWord.SecondWord, POSPrefix);
				break;
			case SecondDeprel:
				ret = new ArgDependentAttrFeature(fn, WordData.Deprel,
						TargetWord.SecondWord, POSPrefix);
				break;

			case LastLemma:
				ret = new ArgDependentAttrFeature(fn, WordData.Lemma,
						TargetWord.LastWord, POSPrefix);
				break;
			case LastPOS:
				ret = new ArgDependentAttrFeature(fn, WordData.POS,
						TargetWord.LastWord, POSPrefix);
				break;
			case LastDeprel:
				ret = new ArgDependentAttrFeature(fn, WordData.Deprel,
						TargetWord.LastWord, POSPrefix);
				break;

			case FirstCCWord:
				ret = new ArgDependentAttrFeature(fn, WordData.ClosedClassForm,
						TargetWord.FirstWord, POSPrefix);
				break;
			case SecondCCWord:
				ret = new ArgDependentAttrFeature(fn, WordData.ClosedClassForm,
						TargetWord.SecondWord, POSPrefix);
				break;
			case LastCCWord:
				ret = new ArgDependentAttrFeature(fn, WordData.ClosedClassForm,
						TargetWord.LastWord, POSPrefix);
				break;

			case AnyLemma:
				ret = new AnySetFeature(fn, WordData.Lemma, false, POSPrefix);
				break;
			case AnyPOS:
				ret = new AnySetFeature(fn, WordData.POS, false, POSPrefix);
				break;

			case LeftWord:
				ret = new ArgDependentAttrFeature(fn, WordData.Form,
						TargetWord.LeftDep, POSPrefix);
				break;
			case LeftPOS:
				ret = new ArgDependentAttrFeature(fn, WordData.POS,
						TargetWord.LeftDep, POSPrefix);
				break;
			case LeftFeats:
				ret = new ArgDependentFeatsFeature(fn, TargetWord.LeftDep,
						POSPrefix);
				break;

			case RightWord:
				ret = new ArgDependentAttrFeature(fn, WordData.Form,
						TargetWord.RightDep, POSPrefix);
				break;
			case RightPOS:
				ret = new ArgDependentAttrFeature(fn, WordData.POS,
						TargetWord.RightDep, POSPrefix);
				break;
			case RightFeats:
				ret = new ArgDependentFeatsFeature(fn, TargetWord.RightDep,
						POSPrefix);
				break;

			case LeftSiblingWord:
				ret = new ArgDependentAttrFeature(fn, WordData.Form,
						TargetWord.LeftSibling, POSPrefix);
				break;
			case LeftSiblingPOS:
				ret = new ArgDependentAttrFeature(fn, WordData.POS,
						TargetWord.LeftSibling, POSPrefix);
				break;
			case LeftSiblingFeats:
				ret = new ArgDependentFeatsFeature(fn, TargetWord.LeftSibling,
						POSPrefix);
				break;

			case RightSiblingWord:
				ret = new ArgDependentAttrFeature(fn, WordData.Form,
						TargetWord.RightSibling, POSPrefix);
				break;
			case RightSiblingPOS:
				ret = new ArgDependentAttrFeature(fn, WordData.POS,
						TargetWord.RightSibling, POSPrefix);
				break;
			case RightSiblingFeats:
				ret = new ArgDependentFeatsFeature(fn, TargetWord.RightSibling,
						POSPrefix);
				break;

			case POSPath:
				ret = new PathFeature(fn, WordData.POS, false, POSPrefix);
				break;
			case POSDepPath:
				ret = new PathFeature(fn, WordData.POS, true, POSPrefix);
				break;
			// case POSPath: ret=new PathFeature(fn,WordData.POS,POSPrefix);
			// break;
			case DeprelPath:
				ret = new PathFeature(fn, WordData.Deprel, true, POSPrefix);
				break;
			case Position:
				ret = new PositionFeature(POSPrefix);
				break;
			case Distance:
				ret = new DistanceFeature(fn, null, true, POSPrefix);
				break;
			case AssignedPBLabel:
				ret = new PBLabelFeature(fn, TargetWord.Arg, includeAllWords,
						POSPrefix);
				break;
			case PathItemSet:
				ret = new PathItemSetFeature(fn, WordData.Deprel, 1, false,
						POSPrefix);
				break;
			case Path2GramSet:
				ret = new PathItemSetFeature(fn, WordData.Deprel, 2, false,
						POSPrefix);
				break;
			case Path3GramSet:
				ret = new PathItemSetFeature(fn, WordData.Deprel, 3, false,
						POSPrefix);
				break;

			case PathLemmaSet:
				ret = new PathItemSetFeature(fn, WordData.Lemma, 1, false,
						POSPrefix);
				break;
			case Path2LemmaSet:
				ret = new PathItemSetFeature(fn, WordData.Lemma, 2, false,
						POSPrefix);
				break;
			case Path3LemmaSet:
				ret = new PathItemSetFeature(fn, WordData.Lemma, 3, false,
						POSPrefix);
				break;

			case SameSubTree:
				ret = new SameSubTreeFeature(fn, null, false, POSPrefix);
				break;
			case SameParentSubTree:
				ret = new SameSubTreeFeature(fn, null, true, POSPrefix);
				break;
			case DeprelDistance:
				ret = new PathLengthFeature(fn, WordData.Deprel, true,
						POSPrefix);
				break;
			case SubCatSize:
				ret = new SubCatSizeFeature(false, POSPrefix);
				break;
			case SpanSize:
				ret = new SpanLengthFeature(fn, WordData.Deprel, false,
						POSPrefix);
				break;		
			default:
				if (fn.toString().startsWith("WordEmbedding")
						|| fn.toString().startsWith("WordTokenEmbedding")) {
					if (we == null) {
						throw new RuntimeException(
								"Cannot use embedding features unless embeddings are provided on the cmd line");
					}
					int dim = Integer.parseInt(fn.toString().substring(
							fn.toString().length() - 3));

					if (fn.toString().contains("Path")) {
						if (fn.toString().contains("DepPath"))
							ret = new EmbeddingPath(fn, null, POSPrefix, true,
									we, dim);
						else
							ret = new EmbeddingPath(fn, null, POSPrefix, false,
									we, dim);
					} else if (fn.toString().contains("Pred")) {
						if (fn.toString().contains("Token"))
							ret = new PredDependentEmbedding(fn,
									TargetWord.valueOf("Pred"),
									includeAllWords, POSPrefix, we, dim, true);
						else
							ret = new PredDependentEmbedding(fn,
									TargetWord.valueOf("Pred"),
									includeAllWords, POSPrefix, we, dim, false);
					} else if (fn.toString().contains("Subj")) {
						if (fn.toString().contains("Token"))
							ret = new PredDependentEmbedding(fn,
									TargetWord.PredSubj, includeAllWords,
									POSPrefix, we, dim, true);
						else
							ret = new PredDependentEmbedding(fn,
									TargetWord.PredSubj, includeAllWords,
									POSPrefix, we, dim, false);
					} else {
						if (fn.toString().contains("Comp")) {
							if (fn.toString().contains("Token")) {
								TargetWord tw = TargetWord.valueOf(fn
										.toString().substring(22,
												fn.toString().length() - 3));
								ret = new ArgDependentEmbedding(fn, tw,
										POSPrefix, true, false, we, dim, true);
							} else {
								TargetWord tw = TargetWord.valueOf(fn
										.toString().substring(17,
												fn.toString().length() - 3));
								ret = new ArgDependentEmbedding(fn, tw,
										POSPrefix, true, false, we, dim, false);
							}
						} else if (fn.toString().contains("Avg")) {
							if (fn.toString().contains("Token")) {
								TargetWord tw = TargetWord.valueOf(fn
										.toString().substring(21,
												fn.toString().length() - 3));
								ret = new ArgDependentEmbedding(fn, tw,
										POSPrefix, false, true, we, dim, true);
							} else {
								TargetWord tw = TargetWord.valueOf(fn
										.toString().substring(16,
												fn.toString().length() - 3));
								ret = new ArgDependentEmbedding(fn, tw,
										POSPrefix, false, true, we, dim, false);
							}
						} else if (fn.toString().contains("Token")) {
							TargetWord tw = TargetWord.valueOf(fn.toString()
									.substring(18, fn.toString().length() - 3));
							ret = new ArgDependentEmbedding(fn, tw, POSPrefix,
									false, false, we, dim, true);
						} else {
							TargetWord tw = TargetWord.valueOf(fn.toString()
									.substring(13, fn.toString().length() - 3));
							ret = new ArgDependentEmbedding(fn, tw, POSPrefix,
									false, false, we, dim, false);
						}
					}

				} else if (fn.toString().startsWith("Brown")) {
					if (bc == null) {
						throw new RuntimeException(
								"Cannot use brown cluster features unless a cluster is provided on the cmd line");
					}
					ClusterVal cv = fn.toString().contains("Short") ? ClusterVal.SHORT
							: ClusterVal.LONG;

					if (fn.toString().contains("Path")) {
						if (fn.toString().contains("DepPath")) {
							ret = new BrownPathFeature(fn, true, bc, cv,
									POSPrefix);
						} else {
							ret = new BrownPathFeature(fn, false, bc, cv,
									POSPrefix);
						}
					} else {
						TargetWord tw = TargetWord.valueOf(fn.toString()
								.substring(
										"Brown".length()
												+ cv.toString().length()));
						if (fn.toString().contains("Pred")) {
							ret = new PredDependentBrown(fn, tw,
									includeAllWords, POSPrefix, bc, cv);
						} else {
							ret = new ArgDependentBrown(fn, tw, POSPrefix, bc,
									cv);
						}
					}
				} else {
					System.err
							.println("Unknown feature name: " + fn.toString());
					throw new Error(
							"You are wrong here. Check your implementation.");
				}
			}
			cache.put(fn, ret);
			return ret;
		}
	}

	public Feature getQFeature(FeatureName fn1, FeatureName fn2,
			boolean includeAllWords, String POSPrefix, BrownCluster bc,
			WordEmbedding we) {
		Feature ret;
		// String fnameStr=fn1.name()+"+"+fn2.name();
		String fnameStr = getCanonicalQFeatureName(fn1, fn2);
		if (qcache.containsKey(fnameStr)) {
			ret = qcache.get(fnameStr);
			ret.addPOSPrefix(POSPrefix);
			return ret;
		}
		Feature f1 = getFeature(fn1, includeAllWords, null, bc, we);
		Feature f2 = getFeature(fn2, includeAllWords, null, bc, we);
		if (f1 instanceof SingleFeature) {
			if (f2 instanceof SingleFeature) {
				ret = new QSingleSingleFeature((SingleFeature) f1,
						(SingleFeature) f2, includeAllWords, POSPrefix);
			} else {
				ret = new QSingleSetFeature((SingleFeature) f1,
						(SetFeature) f2, includeAllWords, POSPrefix);
			}
		} else if (f1 instanceof ContinuousFeature) {
			if (f2 instanceof SingleFeature) {
				ret = new QContinuousSingleFeature((ContinuousFeature) f1,
						(SingleFeature) f2, includeAllWords, POSPrefix);
			} else if (f2 instanceof SetFeature) {
				ret = new QContinuousSetFeature((ContinuousFeature) f1,
						(SetFeature) f2, includeAllWords, POSPrefix);
			} else {
				throw new IllegalArgumentException("Features " + f1.getName()
						+ " and " + f2.getName()
						+ " cannot be combined. Change your feature file");
			}
		} else {
			if (f2 instanceof SingleFeature) {
				ret = new QSingleSetFeature((SingleFeature) f2,
						(SetFeature) f1, includeAllWords, POSPrefix);
			} else { // otherwise both features are set features. These can only
						// be combined if theyre both childset features. else
						// its an error.
				if (f1 instanceof ChildSetFeature
						&& f2 instanceof ChildSetFeature) {
					ret = new QDoubleChildSetFeature((ChildSetFeature) f1,
							(ChildSetFeature) f2, includeAllWords, POSPrefix);
				} else {
					/*
					 * System.err.println("WARNING: Features "+f1.getName()+" and "
					 * +
					 * f2.getName()+" can not be combined. Skipping combination"
					 * ); return null;
					 */
					ret = new QSetSetFeature((SetFeature) f1, (SetFeature) f2,
							includeAllWords, POSPrefix);
				}
			}
		}
		qcache.put(getCanonicalQFeatureName(fn1, fn2), ret);
		return ret;
	}

	private static String getCanonicalQFeatureName(String featureNameString) {
		String[] s = featureNameString.split("\\+");
		FeatureName fn1 = FeatureName.valueOf(s[0]);
		FeatureName fn2 = FeatureName.valueOf(s[1]);
		return getCanonicalQFeatureName(fn1, fn2);
	}

	private static String getCanonicalQFeatureName(FeatureName f1,
			FeatureName f2) {
		if (f1.compareTo(f2) > 0) {
			return f1.toString() + "+" + f2.toString();
		} else {
			// System.err.println("Features are in wrong order?!");
			return f2.toString() + "+" + f1.toString();
		}
	}

	public static String getCanonicalName(FeatureName fn1, FeatureName fn2) {
		if (fn2 == null)
			return fn1.toString();
		else
			return getCanonicalQFeatureName(fn1, fn2);
	}

	public Feature getCachedFeature(String featureNameString) {
		Feature ret;
		if (featureNameString.contains("+")) {
			ret = qcache.get(getCanonicalQFeatureName(featureNameString));
		} else {
			ret = cache.get(FeatureName.valueOf(featureNameString));
		}
		if(ret==null)
			throw new Error(
				"Trying to read a cached feature that doesn't exist. Did you do something nasty with your model? Otherwise the implementation is wrong.");
		return ret;
	}

	public void buildFeatureMaps(Iterable<Sentence> sentences) {
		System.out.println("Extracting features (first pass)...");
		buildFeatureMaps(sentences, false);
		for (Feature f : cache.values())
			f.setDoneWithPredFeatureExtraction();
		for (Feature f : qcache.values())
			f.setDoneWithPredFeatureExtraction();
		System.out.println("Extracting features (second pass)...");
		buildFeatureMaps(sentences, true);
		for (Feature f : cache.values())
			System.out.println(f);
		for (Feature f : qcache.values())
			System.out.println(f);
	}

	private void buildFeatureMaps(Iterable<Sentence> sentences,
			boolean includeAllWords) {
		// Start by cleaning out all simple features that are in the cache but
		// without POS prefix. These have been created only for the QFeatures.
		Iterator<FeatureName> it = cache.keySet().iterator();
		while (it.hasNext()) {
			if (cache.get(it.next()).POSPrefix == null)
				it.remove();
		}
		// Then extract features.
		for (Sentence s : sentences) {
			for (Feature f : cache.values())
				f.extractFeatures(s, includeAllWords);
			for (Feature f : qcache.values())
				f.extractFeatures(s, includeAllWords);
		}
	}

}
