package uk.ac.ed.inf.srl.features;

import java.util.Collection;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public abstract class ContinuousSetFeature extends Feature {
	private static final long serialVersionUID = 1L;

	protected ContinuousSetFeature(FeatureName name, boolean includeArgs,
			boolean usedForPredicateIdentification, String POSPrefix) {
		super(name, includeArgs, usedForPredicateIdentification, POSPrefix);
	}

	/**
	 * This method works with features that have includeArgs==false. It extracts
	 * either from all words (if boolean allWords is true), or from the
	 * predicates only (if false)
	 */
	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		for(String x : getFeatureString(s, 0, -1))
			addMap(x);
	}

	public abstract String[] getFeatureString(Sentence s, int predIndex,
			int argIndex);

	public abstract String[] getFeatureString(Predicate pred, Word arg);

	public abstract float[] getFeatureValue(Sentence s, int predIndex,
			int argIndex);

	public abstract float[] getFeatureValue(Predicate pred, Word arg);
}
