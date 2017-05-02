package uk.ac.ed.inf.srl.features;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.corpus.Word.WordData;
import se.lth.cs.srl.features.FeatureName;
import se.lth.cs.srl.features.NumFeature;
import se.lth.cs.srl.features.SingleFeature;

public class SpanLengthFeature extends SingleFeature {
	private static final long serialVersionUID = 1L;

	protected SpanLengthFeature(FeatureName name, WordData attr,
			boolean consider_deptree, String POSPrefix) {
		super(name, true, false, POSPrefix);
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		for (Predicate pred : s.getPredicates()) {
			if (doExtractFeatures(pred))
				for (Word arg : pred.getArgMap().keySet()) {
					addMap(getFeatureString(pred, arg));
				}
		}
	}

	@Override
	public String getFeatureString(Sentence s, int predIndex, int argIndex) {
		return makeFeatureString(s.get(predIndex), s.get(argIndex));
	}

	@Override
	public String getFeatureString(Predicate pred, Word arg) {
		return makeFeatureString(pred, arg);
	}

	public String makeFeatureString(Word pred, Word arg) {
		// if(arg.getSpan().size()>1) {
		// for(Word w : arg.getSpan())
		// System.err.print(w.getForm()+ " ");
		// System.err.println("-> " +NumFeature.bin(arg.getSpan().size()));
		// }
		return "SpanLength" + NumFeature.bin(arg.getSpan().size());
	}

}
