package uk.ac.ed.inf.srl.features;

import se.lth.cs.srl.corpus.Word.WordData;
import uk.ac.ed.inf.srl.features.FeatureName;
import uk.ac.ed.inf.srl.features.SingleFeature;
import uk.ac.ed.inf.srl.features.TargetWord;
import uk.ac.ed.inf.srl.features.WordExtractor;

public abstract class AttrFeature extends SingleFeature {
	private static final long serialVersionUID = 1;

	protected WordData attr;
	protected WordExtractor wordExtractor;

	protected AttrFeature(FeatureName name, WordData attr, TargetWord tw,
			boolean includeArgs, boolean usedForPredicateIdentification,
			String POSPrefix) {
		super(name, includeArgs, usedForPredicateIdentification, POSPrefix);
		this.attr = attr;
		this.wordExtractor = WordExtractor.getExtractor(tw);
	}

}
