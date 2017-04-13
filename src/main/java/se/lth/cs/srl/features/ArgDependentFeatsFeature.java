package se.lth.cs.srl.features;

import java.util.Collection;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.languages.Language;

public class ArgDependentFeatsFeature extends FeatsFeature {
	private static final long serialVersionUID = 1L;

	protected ArgDependentFeatsFeature(FeatureName name, TargetWord tw,
			String POSPrefix) {
		super(name, tw, true, false, POSPrefix);
	}

	@Override
	public String[] getFeatureStrings(Sentence s, int predIndex, int argIndex) {
		Word w = wordExtractor.getWord(s, predIndex, argIndex);
		if (w == null)
			return null;
		else
			return Language.getLanguage().getFeatSplitPattern()
					.split(w.getFeats());
	}

	@Override
	public String[] getFeatureStrings(Predicate pred, Word arg) {
		Word w = wordExtractor.getWord(pred, arg);
		if (w == null)
			return null;
		else
			return Language.getLanguage().getFeatSplitPattern()
					.split(w.getFeats());
	}

	@Override
	public void addFeatures(Sentence s, Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, int predIndex, int argIndex,
			Integer offset, boolean allWords) {
		addFeatures(indices, getFeatureStrings(s, predIndex, argIndex), offset,
				allWords);

	}

	@Override
	public void addFeatures(Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, Predicate pred, Word arg,
			Integer offset, boolean allWords) {
		addFeatures(indices, getFeatureStrings(pred, arg), offset, allWords);
	}

	private void addFeatures(Collection<Integer> indices, String[] values,
			Integer offset, boolean allWords) {
		if (values == null)
			return;
		for (String v : values) {
			Integer i = indexOf(v);
			if (i != -1 && (allWords || i < predMaxIndex))
				indices.add(i + offset);
		}
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		for (Predicate p : s.getPredicates()) {
			if (doExtractFeatures(p))
				for (Word arg : p.getArgMap().keySet()) {
					Word w = wordExtractor.getWord(null, arg);
					if (w == null)
						continue;
					for (String v : Language.getLanguage()
							.getFeatSplitPattern().split(w.getFeats()))
						addMap(v);
				}
		}

	}
}
