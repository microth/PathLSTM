package se.lth.cs.srl.features;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.corpus.Word.WordData;

public class ChildSetFeature extends SetFeature {
	private static final long serialVersionUID = 1L;

	WordData attr;

	protected ChildSetFeature(FeatureName name, WordData attr,
			boolean usedForPredicateIdentification, String POSPrefix) {
		super(name, false, usedForPredicateIdentification, POSPrefix);
		this.attr = attr;
	}

	@Override
	public String[] getFeatureStrings(Sentence s, int predIndex, int argIndex) {
		return makeFeatureStrings(s.get(predIndex));
	}

	@Override
	public String[] getFeatureStrings(Predicate pred, Word arg) {
		return makeFeatureStrings(pred);
	}

	private String[] makeFeatureStrings(Word pred) {
		String[] ret = new String[pred.getChildren().size()];
		int i = 0;
		for (Word child : pred.getChildren())
			ret[i++] = child.getAttr(attr);
		return ret;
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		if (allWords) {
			for (int i = 1, size = s.size(); i < size; ++i) {
				if (doExtractFeatures(s.get(i)))
					for (Word child : s.get(i).getChildren()) {
						addMap(child.getAttr(attr));
					}
			}
		} else {
			for (Predicate pred : s.getPredicates()) {
				if (doExtractFeatures(pred))
					for (Word child : pred.getChildren())
						addMap(child.getAttr(attr));
			}
		}
	}
}
