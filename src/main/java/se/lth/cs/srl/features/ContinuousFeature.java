package se.lth.cs.srl.features;

import java.util.Collection;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public abstract class ContinuousFeature extends Feature {
	private static final long serialVersionUID = 1L;

	protected ContinuousFeature(FeatureName name, boolean includeArgs,
			boolean usedForPredicateIdentification, String POSPrefix) {
		super(name, includeArgs, usedForPredicateIdentification, POSPrefix);
		indexcounter = 2;
	}

	@Override
	public void addFeatures(Sentence s, Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, int predIndex, int argIndex,
			Integer offset, boolean allWords) {
		// Integer i=indexOf(getFeatureString(s,predIndex,argIndex));
		Double d = getFeatureValue(s, predIndex, argIndex);

		// if(i!=-1 && (allWords || i<predMaxIndex))
		// indices.add(i+offset);
		nonbinFeats.put(1 + offset, d);
	}

	@Override
	public void addFeatures(Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, Predicate pred, Word arg,
			Integer offset, boolean allWords) {
		// Integer i=indexOf(getFeatureString(pred,arg));
		Double d = getFeatureValue(pred, arg);

		// if(i!=-1 && (allWords || i<predMaxIndex))
		// indices.add(i+offset);
		nonbinFeats.put(1 + offset, d);
	}

	/**
	 * This method works with features that have includeArgs==false. It extracts
	 * either from all words (if boolean allWords is true), or from the
	 * predicates only (if false)
	 */
	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		if (includeArgs) {
			throw new Error("You are wrong here, check your implementation.");
		} else {
			if (allWords) {
				for (int i = 1, size = s.size(); i < size; ++i)
					if (doExtractFeatures(s.get(i)))
						addMap(getFeatureString(s, i, -1));
			} else {
				for (Predicate pred : s.getPredicates())
					if (doExtractFeatures(pred))
						addMap(getFeatureString(pred, null));
			}
		}
	}

	public abstract String getFeatureString(Sentence s, int predIndex,
			int argIndex);

	public abstract String getFeatureString(Predicate pred, Word arg);

	public abstract Double getFeatureValue(Sentence s, int predIndex,
			int argIndex);

	public abstract Double getFeatureValue(Predicate pred, Word arg);
}
