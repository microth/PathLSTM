package se.lth.cs.srl.features;

import java.util.Collection;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public class PositionFeature extends SingleFeature {
	private static final long serialVersionUID = 1L;
	/*
	 * Position is the position of the argument wrt the predicate. I.e. if the
	 * predicate is at position 2, and the argument at position 4, their
	 * relation is AFTER
	 */
	public static final String BEFORE = "B";
	public static final String ON = "O";
	public static final String AFTER = "A";

	protected PositionFeature(String POSPrefix) {
		super(FeatureName.Position, true, false, POSPrefix);
		indices.put(BEFORE, Integer.valueOf(1));
		indices.put(ON, Integer.valueOf(2));
		indices.put(AFTER, Integer.valueOf(3));
		indexcounter = 4;
	}

	@Override
	public void addFeatures(Sentence s, Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, int predIndex, int argIndex,
			Integer offset, boolean allWords) {
		indices.add(indexOf(getFeatureString(s, predIndex, argIndex)) + offset);
	}

	@Override
	public void addFeatures(Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, Predicate pred, Word arg,
			Integer offset, boolean allWords) {
		indices.add(indexOf(getFeatureString(pred, arg)) + offset);
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		// Do nothing, the map is constructed in the constructor.
	}

	@Override
	public String getFeatureString(Sentence s, int predIndex, int argIndex) {
		if (predIndex == argIndex)
			return ON;
		else if (predIndex < argIndex)
			return AFTER;
		else
			return BEFORE;
	}

	@Override
	public String getFeatureString(Predicate pred, Word arg) {
		// int cmp=pred.compareTo(arg);
		int cmp = pred.getMySentence().wordComparator.compare(pred, arg);
		if (cmp < 0) {
			return AFTER;
		} else if (cmp == 0) {
			return ON;
		} else {
			return BEFORE;
		}
	}

}
