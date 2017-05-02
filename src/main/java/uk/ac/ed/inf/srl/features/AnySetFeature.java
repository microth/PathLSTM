package se.lth.cs.srl.features;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.corpus.Word.WordData;

public class AnySetFeature extends SetFeature {
	private static final long serialVersionUID = 1L;

	WordData attr;

	protected AnySetFeature(FeatureName name, WordData attr,
			boolean usedForPredicateIdentification, String POSPrefix) {
		super(name, true, false, POSPrefix);
		this.attr = attr;
	}

	@Override
	public String[] getFeatureStrings(Sentence s, int predIndex, int argIndex) {
		return makeFeatureStrings(s.get(argIndex));
	}

	@Override
	public String[] getFeatureStrings(Predicate pred, Word arg) {
		return makeFeatureStrings(arg);
	}

	private String[] makeFeatureStrings(Word w) {
		String[] ret = new String[w.getSpan().size()];
		int i = 0;
		for (Word child : w.getChildren())
			ret[i++] = child.getAttr(attr);
		return ret;
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		for (Word child : s)
			addMap(child.getAttr(attr));
	}
}
