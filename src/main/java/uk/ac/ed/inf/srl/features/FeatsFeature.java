package se.lth.cs.srl.features;

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
