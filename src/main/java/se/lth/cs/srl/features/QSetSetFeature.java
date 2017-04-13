package se.lth.cs.srl.features;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public class QSetSetFeature extends SetFeature implements QuadraticFeature {
	private static final long serialVersionUID = 1L;

	private SetFeature f1;
	private SetFeature f2;

	protected QSetSetFeature(SetFeature f1, SetFeature f2,
			boolean usedForPredicateIdentification, String POSPrefix) {
		super(f1.name, f1.includeArgs || f2.includeArgs,
				usedForPredicateIdentification, POSPrefix);
		this.f1 = f1;
		this.f2 = f2;
	}

	@Override
	public String[] getFeatureStrings(Sentence s, int predIndex, int argIndex) {
		String[] f1vals = f1.getFeatureStrings(s, predIndex, argIndex);
		String[] f2vals = f2.getFeatureStrings(s, predIndex, argIndex);
		makeFeatureStrings(f1vals, f2vals);
		return f2vals;
	}

	@Override
	public String[] getFeatureStrings(Predicate pred, Word arg) {
		String[] f1vals = f1.getFeatureStrings(pred, arg);
		String[] f2vals = f2.getFeatureStrings(pred, arg);
		if (f2vals != null) {
			makeFeatureStrings(f1vals, f2vals);
			return f2vals;
		} else {
			return new String[] { "" };
			// return new String[0];
		}
	}

	private void makeFeatureStrings(String[] f1vals, String[] f2vals) {

		if (f1vals.length != f2vals.length) {
			System.err
					.println("CHECK YOUR IMPLEMENTATION! Trying to combine two set features of different lengths");
			System.exit(1);
		}

		for (int i = 0, length = f2vals.length; i < length; ++i)
			f2vals[i] += VALUE_SEPARATOR + f1vals[i];
	}

	public String getName() {
		return FeatureGenerator.getCanonicalName(f1.name, f2.name);
	}

}
