package se.lth.cs.srl.features;

import java.util.Collection;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public abstract class SetFeature extends Feature {
	private static final long serialVersionUID = 1L;

	protected SetFeature(FeatureName name, boolean includeArgs,
			boolean usedForPredicateIdentification, String POSPrefix) {
		super(name, includeArgs, usedForPredicateIdentification, POSPrefix);
	}

	public abstract String[] getFeatureStrings(Sentence s, int predIndex,
			int argIndex);

	public abstract String[] getFeatureStrings(Predicate pred, Word arg);

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		if (includeArgs) {
			for (Predicate pred : s.getPredicates()) {
				if (doExtractFeatures(pred))
					for (Word arg : pred.getArgMap().keySet()) {
						for (String v : getFeatureStrings(pred, arg))
							addMap(v);
					}
			}
		} else {
			if (allWords) {
				for (int i = 1, size = s.size(); i < size; ++i) {
					if (doExtractFeatures(s.get(i)))
						for (String v : getFeatureStrings(s, i, -1))
							addMap(v);
				}
			} else {
				for (Predicate pred : s.getPredicates()) {
					if (doExtractFeatures(pred))
						for (String v : getFeatureStrings(pred, null))
							addMap(v);
				}
			}
		}
	}

	@Override
	public void addFeatures(Sentence s, Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, int predIndex, int argIndex,
			Integer offset, boolean allWords) {
		for (String v : getFeatureStrings(s, predIndex, argIndex)) {
			Integer i = indexOf(v);
			if (i != -1 && (allWords || i < predMaxIndex))
				indices.add(i + offset);
		}
	}

	@Override
	public void addFeatures(Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, Predicate pred, Word arg,
			Integer offset, boolean allWords) {
		for (String v : getFeatureStrings(pred, arg)) {
			Integer i = indexOf(v);
			if (i != -1 && (allWords || i < predMaxIndex))
				indices.add(i + offset);
		}
	}
}
