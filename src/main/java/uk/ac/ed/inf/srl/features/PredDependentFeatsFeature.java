package se.lth.cs.srl.features;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.languages.Language;

public class PredDependentFeatsFeature extends FeatsFeature {
	private static final long serialVersionUID = 1L;

	protected PredDependentFeatsFeature(FeatureName name, TargetWord tw,
			boolean usedForPredicateIdentification, String POSPrefix) {
		super(name, tw, false, usedForPredicateIdentification, POSPrefix);
	}

	@Override
	public String[] getFeatureStrings(Sentence s, int predIndex, int argIndex) {
		Word w = wordExtractor.getWord(s, predIndex, argIndex);
		return Language.getLanguage().getFeatSplitPattern().split(w.getFeats());
	}

	@Override
	public String[] getFeatureStrings(Predicate pred, Word arg) {
		Word w = wordExtractor.getWord(pred, arg);
		return Language.getLanguage().getFeatSplitPattern().split(w.getFeats());
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
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
