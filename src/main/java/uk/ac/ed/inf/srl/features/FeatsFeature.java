package uk.ac.ed.inf.srl.features;

import uk.ac.ed.inf.srl.features.FeatureName;
import uk.ac.ed.inf.srl.features.SetFeature;
import uk.ac.ed.inf.srl.features.TargetWord;
import uk.ac.ed.inf.srl.features.WordExtractor;

public abstract class FeatsFeature extends SetFeature {
	private static final long serialVersionUID = 1L;

	protected WordExtractor wordExtractor;

	protected FeatsFeature(FeatureName name, TargetWord tw,
			boolean includeArgs, boolean usedForPredicateIdentification,
			String POSPrefix) {
		super(name, includeArgs, usedForPredicateIdentification, POSPrefix);
		wordExtractor = WordExtractor.getExtractor(tw);
	}
}
