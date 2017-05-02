package se.lth.cs.srl.features;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.util.BrownCluster;
import se.lth.cs.srl.util.BrownCluster.ClusterVal;

public class PredDependentBrown extends PredDependentAttrFeature {
	private static final long serialVersionUID = 1L;

	private BrownCluster bc;
	private ClusterVal cv;

	protected PredDependentBrown(FeatureName name, TargetWord tw,
			boolean includeAllWords, String POSPrefix, BrownCluster bc,
			ClusterVal cv) {
		super(name, null, tw, includeAllWords, POSPrefix);
		this.bc = bc;
		this.cv = cv;
	}

	@Override
	public String getFeatureString(Sentence s, int predIndex, int argIndex) {
		Word w = wordExtractor.getWord(s, predIndex, argIndex);
		if (w.isBOS())
			return "ROOT";
		return bc.getValue(w.getForm(), cv);
	}

	@Override
	public String getFeatureString(Predicate pred, Word arg) {
		Word w = wordExtractor.getWord(pred, arg);
		if (w.isBOS())
			return "ROOT";
		return bc.getValue(w.getForm(), cv);
	}
}
