package se.lth.cs.srl.features;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.util.BrownCluster;
import se.lth.cs.srl.util.BrownCluster.ClusterVal;

public class ArgDependentBrown extends ArgDependentAttrFeature {
	private static final long serialVersionUID = 1L;

	private BrownCluster bc;
	private ClusterVal cv;

	protected ArgDependentBrown(FeatureName name, TargetWord tw,
			String POSPrefix, BrownCluster bc, ClusterVal cv) {
		super(name, null, tw, POSPrefix);
		this.bc = bc;
		this.cv = cv;
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		for (Predicate p : s.getPredicates()) {
			if (doExtractFeatures(p))
				for (Word arg : p.getArgMap().keySet()) {
					Word w = wordExtractor.getWord(null, arg);
					if (w != null)
						addMap(bc.getValue(w.getForm(), cv));
				}
		}
	}

	@Override
	public String getFeatureString(Sentence s, int predIndex, int argIndex) {
		Word w = wordExtractor.getWord(s, predIndex, argIndex);
		if (w == null)
			return null;
		else
			return bc.getValue(w.getForm(), cv);

	}

	@Override
	public String getFeatureString(Predicate pred, Word arg) {
		Word w = wordExtractor.getWord(pred, arg);
		if (w == null)
			return null;
		else
			return bc.getValue(w.getForm(), cv);
	}
}
