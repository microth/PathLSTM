package se.lth.cs.srl.features;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public class QSingleSingleFeature extends SingleFeature implements
		QuadraticFeature {
	private static final long serialVersionUID = 1L;

	private SingleFeature f1, f2;

	protected QSingleSingleFeature(SingleFeature f1, SingleFeature f2,
			boolean usedForPredicateIdentification, String POSPrefix) {
		super(f1.name, f1.includeArgs || f2.includeArgs,
				usedForPredicateIdentification, POSPrefix);
		this.f1 = f1;
		this.f2 = f2;
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		if (includeArgs) {
			for (Predicate pred : s.getPredicates()) {
				if (doExtractFeatures(pred))
					for (Word arg : pred.getArgMap().keySet()) {
						addMap(getFeatureString(pred, arg));
					}
			}
		} else {
			super.performFeatureExtraction(s, allWords);
		}
	}

	@Override
	public String getFeatureString(Sentence s, int predIndex, int argIndex) {
		return f1.getFeatureString(s, predIndex, argIndex) + VALUE_SEPARATOR
				+ f2.getFeatureString(s, predIndex, argIndex);
	}

	@Override
	public String getFeatureString(Predicate pred, Word arg) {
		return f1.getFeatureString(pred, arg) + VALUE_SEPARATOR
				+ f2.getFeatureString(pred, arg);
	}

	public String getName() {
		return FeatureGenerator.getCanonicalName(f1.name, f2.name);
	}

}
