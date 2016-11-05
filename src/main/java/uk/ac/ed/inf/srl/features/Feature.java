package uk.ac.ed.inf.srl.features;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public abstract class Feature implements Serializable {
	private static final long serialVersionUID = 1L;

	protected Map<String, Integer> indices = new HashMap<>();
	protected Map<String, Integer> nonbinFeats = new HashMap<>();

	protected int indexcounter = 1;
	protected int predMaxIndex;
	protected FeatureName name;

	protected final boolean includeArgs;
	private final boolean usedForPredicateIdentification;

	protected String POSPrefix;

	protected abstract void performFeatureExtraction(Sentence s,
			boolean allWords);

	public abstract void addFeatures(Sentence s, Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, int predIndex, int argIndex,
			Integer offset, boolean allWords); // This way the collection can be
												// both a set or a list (ie one
												// that can allow multiple
												// identical values, or not,
												// depending on choice from
												// above)

	public abstract void addFeatures(Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, Predicate pred, Word arg,
			Integer offset, boolean allWords);

	protected Feature(FeatureName name, boolean includeArgs,
			boolean usedForPredicateIdentification, String POSPrefix) {
		this.name = name;
		this.includeArgs = includeArgs;
		this.usedForPredicateIdentification = usedForPredicateIdentification;
		this.POSPrefix = POSPrefix;
	}

	protected void addMap(String val) {
		if (val != null && !val.equals("") && !val.equals("_")
				&& !indices.containsKey(val)) {
			indices.put(val, indexcounter);
			indexcounter++;
		}
	}

	public int size(boolean includeAllWords) {
		if (includeAllWords)
			return indices.size() + nonbinFeats.size();
		else
			return predMaxIndex;
	}

	// TODO this method should return null rather than -1 if its missing. I
	// assume its faster to test if its null than all the autoboxing that goes
	// on when you compare an Integer to -1
	public Integer indexOf(String s) {
		Integer i;
		if ((i = indices.get(s)) != null)
			return i;
		else
			return Integer.valueOf(-1);
	}

	public String toString() {
		StringBuilder ret = new StringBuilder(getName() + ", "
				+ this.getClass().toString());
		if (indices != null)
			ret.append(", size: " + size(true));
		ret.append(", POSPrefix: " + POSPrefix);
		return ret.toString();
	}

	public String getName() {
		return name.toString();
	}

	public void addPOSPrefix(String prefix) {
		// Prefix is null then do nothing (this is used for QFeatures)
		// or the current prefix is a prefix of the added one
		if (prefix == null)
			return;
		if (POSPrefix == null) {
			POSPrefix = prefix;
			return;
		}
		if (prefix.startsWith(POSPrefix))
			return;
		if (POSPrefix.startsWith(prefix)) {
			POSPrefix = prefix;
		} else { // We need to find the longest common prefix of the two prefix
					// strings
			int len = 0;
			for (int max = Math.min(prefix.length(), POSPrefix.length()); len < max; ++len) {
				if (prefix.charAt(len) != POSPrefix.charAt(len))
					break;
			}
			POSPrefix = prefix.substring(0, len);
		}
	}

	protected boolean doExtractFeatures(Word pred) {
		return pred.getPOS().startsWith(POSPrefix);
	}

	public void extractFeatures(Sentence s, boolean allWords) {
		if (allWords) {
			if (!usedForPredicateIdentification)
				return;
			performFeatureExtraction(s, allWords);
		} else {
			performFeatureExtraction(s, allWords);
		}

	}

	public void setDoneWithPredFeatureExtraction() {
		if (predMaxIndex != 0)
			throw new Error(
					"Multiple calls to setDoneWithPredFeatureExtraction() in Feature.java. You are wrong here. Check your implementation.");
		predMaxIndex = (indices.size() + nonbinFeats.size());
		// predMaxIndex=indices.size()-1;
	}

	public Map<String, Integer> getMap() {
		return indices;
	}
}
