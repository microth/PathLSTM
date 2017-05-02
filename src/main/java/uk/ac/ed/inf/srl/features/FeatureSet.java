package se.lth.cs.srl.features;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A feature set is basically a Map<String,List<Feature>>, where String is a
 * prefix of a POS-tag, and List<Feature> is the features used by the classifier
 * for that prefix.
 * 
 * @author anders bjorkelund
 *
 */

public class FeatureSet extends HashMap<String, List<Feature>> {
	private static final long serialVersionUID = 1L;
	/**
	 * The prefixes of this Map, sorted in reverse order, i.e. longer prefixes
	 * go before shorter. And the empty string comes last.
	 */
	public final String[] POSPrefixes;

	/**
	 * Constructs a featureset based on the argument. The POSPrefixes are
	 * extracted are sorted in reverse order on construction.
	 * 
	 * @param featureSet
	 *            the featureset
	 */
	public FeatureSet(Map<String, List<Feature>> featureSet) {
		super(featureSet);
		POSPrefixes = this.keySet().toArray(new String[0]);
		// Arrays.sort(POSPrefixes,Collections.reverseOrder());
		Arrays.sort(POSPrefixes);
	}
}
