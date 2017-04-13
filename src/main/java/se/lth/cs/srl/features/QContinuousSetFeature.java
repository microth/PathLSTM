package se.lth.cs.srl.features;

import java.util.Collection;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public class QContinuousSetFeature extends SetFeature implements
		QuadraticFeature {
	private static final long serialVersionUID = 1L;

	private ContinuousFeature f1;
	private SetFeature f2;

	protected QContinuousSetFeature(ContinuousFeature f1, SetFeature f2,
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
		for (String v : getFeatureStrings(s, predIndex, argIndex)) {
			Integer i = indexOf(v);
			Double d = f1.getFeatureValue(s, predIndex, argIndex);
			if (i != 1 && (allWords || i < predMaxIndex))
				nonbinFeats.put(i + offset, d);
		}
	}

	@Override
	public void addFeatures(Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, Predicate pred, Word arg,
			Integer offset, boolean allWords) {
		for (String v : getFeatureStrings(pred, arg)) {
			Integer i = indexOf(v);
			Double d = f1.getFeatureValue(pred, arg);
			if (i != -1 && (allWords || i < predMaxIndex))
				nonbinFeats.put(i + offset, d);
		}
	}

	@Override
	public String[] getFeatureStrings(Sentence s, int predIndex, int argIndex) {
		String f1val = f1.getFeatureString(s, predIndex, argIndex);
		String[] f2vals = f2.getFeatureStrings(s, predIndex, argIndex);
		makeFeatureStrings(f1val, f2vals);
		return f2vals;
	}

	@Override
	public String[] getFeatureStrings(Predicate pred, Word arg) {
		String f1val = f1.getFeatureString(pred, arg);
		String[] f2vals = f2.getFeatureStrings(pred, arg);
		if (f2vals != null) {
			makeFeatureStrings(f1val, f2vals);
			return f2vals;
		} else {
			return new String[] { "" };
			// return new String[0];
		}
	}

	private void makeFeatureStrings(String f1val, String[] f2vals) {
		for (int i = 0, length = f2vals.length; i < length; ++i)
			f2vals[i] += VALUE_SEPARATOR + f1val;
	}

	public String getName() {
		return FeatureGenerator.getCanonicalName(f1.name, f2.name);
	}

}
