package se.lth.cs.srl.features;

import java.util.List;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.corpus.Word.WordData;

public class PathLengthFeature extends SingleFeature {
	private static final long serialVersionUID = 1L;

	protected PathLengthFeature(FeatureName name, WordData attr,
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
		List<Word> path = Word.findPath(pred, arg);
		return "PathLength" + NumFeature.bin(path.size());
	}

}
