package uk.ac.ed.inf.srl.features;

import java.util.Collection;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import uk.ac.ed.inf.srl.features.FeatureName;
import uk.ac.ed.inf.srl.features.SingleFeature;
import uk.ac.ed.inf.srl.features.TargetWord;

public class PBLabelFeature extends SingleFeature {
	private static final long serialVersionUID = 1L;

	public PBLabelFeature(FeatureName name, TargetWord tw,
			boolean usedForPredicateIdentification, String POSPrefix) {
		super(name, false, usedForPredicateIdentification, POSPrefix);
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		for (Predicate p : s.getPredicates()) {
			if (doExtractFeatures(p))
				for (Word arg : p.getArgMap().keySet()) {
					addMap(p.getArgMap().get(arg));
				}
		}
	}

	@Override
	public String getFeatureString(Sentence s, int predIndex, int argIndex) {
		return ((Predicate) s.get(predIndex)).getArgMap().get(/*
															 * wordExtractor.getWord
															 * (s, predIndex,
															 * argIndex)
															 */s.get(argIndex));
	}

	@Override
	public String getFeatureString(Predicate pred, Word arg) {
		return pred.getArgMap().get(/* wordExtractor.getWord(pred, */arg);
	}

	@Override
	public void addFeatures(Sentence s, Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, int predIndex, int argIndex,
			Integer offset, boolean allWords) {
		addFeatures(indices, getFeatureString(s, predIndex, argIndex), offset,
				allWords);
	}

	@Override
	public void addFeatures(Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, Predicate pred, Word arg,
			Integer offset, boolean allWords) {
		addFeatures(indices, getFeatureString(pred, arg), offset, allWords);
	}

	private void addFeatures(Collection<Integer> indices, String featureString,
			Integer offset, boolean allWords) {
		if (featureString == null) {
			return;
		}
		Integer i = indexOf(featureString);
		if (i != -1 && (allWords || i < predMaxIndex))
			indices.add(i + offset);
	}
}
