package se.lth.cs.srl.features;

import java.util.Collection;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public class QContinuousSingleFeature extends Feature implements
		QuadraticFeature {
	private static final long serialVersionUID = 1L;

	private ContinuousFeature f1;
	private SingleFeature f2;

	protected QContinuousSingleFeature(ContinuousFeature f1, SingleFeature f2,
			boolean usedForPredicateIdentification, String POSPrefix) {
		super(f1.name, f1.includeArgs || f2.includeArgs,
				usedForPredicateIdentification, POSPrefix);
		this.f1 = f1;
		this.f2 = f2;
	}

	@Override
	public void addFeatures(Sentence s, Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, int predIndex, int argIndex,
			Integer offset, boolean allWords) {
		Integer i = indexOf(getFeatureString(s, predIndex, argIndex));
		Double d = f1.getFeatureValue(s, predIndex, argIndex);
		if (i != -1 && (allWords || i < predMaxIndex))
			nonbinFeats.put(i + offset, d);
	}

	@Override
	public void addFeatures(Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, Predicate pred, Word arg,
			Integer offset, boolean allWords) {
		Integer i = indexOf(getFeatureString(pred, arg));
		Double d = f1.getFeatureValue(pred, arg);
		if (i != -1 && (allWords || i < predMaxIndex))
			nonbinFeats.put(i + offset, d);
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		if (includeArgs) {
			for (Predicate pred : s.getPredicates()) {
				if (doExtractFeatures(pred))
					for (Word arg : pred.getArgMap().keySet())
						addMap(getFeatureString(pred, arg));
			}
		} else {
			if (allWords) {
				for (int i = 1, size = s.size(); i < size; ++i)
					if (doExtractFeatures(s.get(i)))
						addMap(getFeatureString(s, i, -1));
			} else {
				for (Predicate pred : s.getPredicates())
					if (doExtractFeatures(pred))
						addMap(getFeatureString(pred, null));
			}
		}
		/*
		 * if(includeArgs){ for(Predicate pred:s.getPredicates()){
		 * if(doExtractFeatures(pred)) for(Word arg:pred.getArgMap().keySet()){
		 * addMap(getFeatureString(pred,arg)); } } } else {
		 * super.performFeatureExtraction(s, allWords); }
		 */
	}

	// @Override
	public String getFeatureString(Sentence s, int predIndex, int argIndex) {
		String ret = f1.getFeatureString(s, predIndex, argIndex)
				+ VALUE_SEPARATOR + f2.getFeatureString(s, predIndex, argIndex);
		// System.err.println(ret);
		return ret;
	}

	// @Override
	public String getFeatureString(Predicate pred, Word arg) {
		String ret = f1.getFeatureString(pred, arg) + VALUE_SEPARATOR
				+ f2.getFeatureString(pred, arg);
		// System.err.println(ret);
		return ret;
	}

	public String getName() {
		return FeatureGenerator.getCanonicalName(f1.name, f2.name);
		// return f1.name+"+"+f2.name;
	}

}
