package se.lth.cs.srl.features;

import java.util.List;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.corpus.Word.WordData;

public class SameSubTreeFeature extends SingleFeature {
	private static final long serialVersionUID = 1L;

	boolean parent;

	protected SameSubTreeFeature(FeatureName name, WordData attr,
			boolean consider_parent, String POSPrefix) {
		super(name, true, false, POSPrefix);
		this.parent = consider_parent;
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
		List<Word> path = Word.findPath(parent ? pred.getHead() : pred, arg);
		boolean inTree = true;
		for (int i = 0; i < (path.size() - 1); ++i) {
			Word w = path.get(i);
			// path goes up instead of down
			if (w.getHead() == path.get(i + 1)) {
				inTree = false;
				break;
			}
		}

		return (parent ? "Parent" : "") + "SubTree" + (inTree ? 1 : 0);
	}

}
