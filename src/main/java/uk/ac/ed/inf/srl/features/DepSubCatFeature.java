package se.lth.cs.srl.features;

import java.util.Arrays;
import java.util.Set;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public class DepSubCatFeature extends SingleFeature {
	private static final long serialVersionUID = 1L;

	private static final String SEPARATOR = " ";

	DepSubCatFeature(boolean usedForPredicateIdentification, String POSPrefix) {
		super(FeatureName.DepSubCat, false, usedForPredicateIdentification,
				POSPrefix);
	}

	@Override
	public String getFeatureString(Sentence s, int predIndex, int argIndex) {
		return makeFeatureString(s, s.get(predIndex).getChildren());
	}

	@Override
	public String getFeatureString(Predicate pred, Word arg) {
		return makeFeatureString(pred.getMySentence(), pred.getChildren());
	}

	private String makeFeatureString(Sentence s, Set<Word> children) {
		switch (children.size()) {
		case 0:
			return " "; // This is the String corresponding to 0 children. Yet
						// this should not be ignored as a feature which it
						// would by the addMap() method if it would return the
						// empty string or null. Not really sure if this is
						// optimal, or this feature should be ignored.
		case 1:
			return children.iterator().next().getDeprel();
		default:
			Word[] sortedChildren = children.toArray(new Word[0]);
			Arrays.sort(sortedChildren, s.wordComparator);
			StringBuilder ret = new StringBuilder(sortedChildren[0].getDeprel());
			for (int i = 1, size = sortedChildren.length; i < size; ++i)
				ret.append(SEPARATOR).append(sortedChildren[i].getDeprel());
			return ret.toString();
		}
	}
}
