package se.lth.cs.srl.features;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.corpus.Word.WordData;

public class PathItemSetFeature extends SetFeature {
	private static final long serialVersionUID = 1L;

	WordData attr;
	int ngramlength;

	protected PathItemSetFeature(FeatureName name, WordData attr,
			int ngramlength, boolean usedForPredicateIdentification,
			String POSPrefix) {
		super(name, false, usedForPredicateIdentification, POSPrefix);
		this.attr = attr;
		this.ngramlength = ngramlength;
	}

	@Override
	public String[] getFeatureStrings(Sentence s, int predIndex, int argIndex) {
		List<Word> path = Word.findPath(s.get(predIndex), s.get(argIndex));
		if (path.size() == 0)
			return new String[] {};

		List<String> values = computeNGrams(path);

		String[] retval = new String[values.size()];
		retval = values.toArray(retval);
		return retval;
	}

	@Override
	public String[] getFeatureStrings(Predicate pred, Word arg) {
		if (arg == null) {
			List<String> allvalues = new LinkedList<>();
			for (int i = 1, size = pred.getMySentence().size(); i < size; ++i) {
				for (String s : getFeatureStrings(pred.getMySentence(),
						pred.getIdx(), i))
					allvalues.add(s);
			}
			String[] retval = new String[allvalues.size()];
			retval = allvalues.toArray(retval);
			return retval;
		} else
			return getFeatureStrings(pred.getMySentence(), pred.getIdx(),
					arg.getIdx());
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		for (Predicate pred : s.getPredicates()) {
			if (doExtractFeatures(pred)) {
				for (int i = 1, size = s.size(); i < size; ++i) {
					Word arg = s.get(i);
					boolean up = true;
					List<Word> path = Word.findPath(pred, arg);
					if (path.size() == 0)
						continue;

					List<String> ngrams = computeNGrams(path);
					for (String f : ngrams)
						addMap(f);
				}
			}
		}
	}

	List<String> computeNGrams(List<Word> path) {
		List<String> retval = new LinkedList<>();

		List<String> steps = new ArrayList<>();
		for (int j = 1; j < ngramlength; j++) {
			steps.add("TAB");
		}

		boolean up = true;
		for (int j = 0; j < (path.size() - 1); ++j) {
			Word w = path.get(j);
			if (up) {
				if (w.getHead() == path.get(j + 1)) { // Arrow up
					steps.add(w.getAttr(attr) + "UP");
				} else { // Arrow down
					steps.add(w.getAttr(attr) + "DOWN");
					up = false;
				}
			} else {
				steps.add(w.getAttr(attr) + "DOWN");
			}
		}

		for (int j = 1; j < ngramlength; j++) {
			steps.add("TAB");
		}

		int size = steps.size() + 1 - ngramlength; // stay within array
													// boundaries
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < size; j++) {
			for (int i = 0; i < ngramlength; i++) {
				sb.append(steps.get(j + i)); // simply concatenate n steps
			}
			retval.add(sb.toString());

			sb = new StringBuffer();
		}

		return retval;

	}
}
