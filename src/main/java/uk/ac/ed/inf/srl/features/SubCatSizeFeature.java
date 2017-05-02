package se.lth.cs.srl.features;

import java.util.Set;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public class SubCatSizeFeature extends SingleFeature {
	private static final long serialVersionUID = 1L;

	SubCatSizeFeature(boolean usedForPredicateIdentification, String POSPrefix) {
		super(FeatureName.SubCatSize, false, usedForPredicateIdentification,
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
		return "SUBCAT" + NumFeature.bin(children.size());
	}
}
