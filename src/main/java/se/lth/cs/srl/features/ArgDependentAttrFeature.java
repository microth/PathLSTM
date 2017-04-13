package se.lth.cs.srl.features;

import java.util.Collection;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.corpus.Word.WordData;

public class ArgDependentAttrFeature extends AttrFeature {
	private static final long serialVersionUID = 1L;

	protected ArgDependentAttrFeature(FeatureName name, WordData attr,
			TargetWord tw, String POSPrefix) {
		super(name, attr, tw, true, false, POSPrefix);
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		for (Predicate p : s.getPredicates()) {
			if (doExtractFeatures(p))
				for (Word arg : p.getArgMap().keySet()) {
					for (Word w : arg.getSpan())
						addMap(w.getAttr(attr));
				}
		}
	}

	@Override
	public String getFeatureString(Sentence s, int predIndex, int argIndex) {
		Word w = wordExtractor.getWord(s, predIndex, argIndex);
		if (w == null)
			return null;
		else
			return w.getAttr(attr);
	}

	@Override
	public String getFeatureString(Predicate pred, Word arg) {
		Word w = wordExtractor.getWord(pred, arg);
		if (w == null)
			return null;
		else
			return w.getAttr(attr);
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
		if (featureString == null)
			return;
		Integer i = indexOf(featureString);
		if (i != -1 && (allWords || i < predMaxIndex))
			indices.add(i + offset);
	}
}
