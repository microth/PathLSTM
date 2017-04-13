package se.lth.cs.srl.features;

import java.util.Set;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public class QDoubleChildSetFeature extends SetFeature implements
		QuadraticFeature {
	private static final long serialVersionUID = 1L;

	private ChildSetFeature f1, f2;

	protected QDoubleChildSetFeature(ChildSetFeature f1, ChildSetFeature f2,
			boolean usedForPredicateIdentification, String POSPrefix) {
		super(f1.name, f1.includeArgs && f2.includeArgs,
				usedForPredicateIdentification, POSPrefix); // The boolean
															// should always
															// evaluate to
															// false, seeing as
															// ChildSetFeatures
															// are always
															// focused on the
															// pred
		this.f1 = f1;
		this.f2 = f2;
	}

	@Override
	public String[] getFeatureStrings(Sentence s, int predIndex, int argIndex) {
		return makeFeatureStrings(s.get(predIndex).getChildren());
	}

	@Override
	public String[] getFeatureStrings(Predicate pred, Word arg) {
		return makeFeatureStrings(pred.getChildren());
	}

	private String[] makeFeatureStrings(Set<Word> children) {
		String[] ret = new String[children.size()];
		int i = 0;
		for (Word child : children)
			ret[i++] = child.getAttr(f1.attr) + VALUE_SEPARATOR
					+ child.getAttr(f2.attr);
		return ret;
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
		if (includeArgs) {
			throw new Error("You are wrong here.");
		} else {
			if (allWords) {
				for (int i = 1, size = s.size(); i < size; ++i) {
					if (doExtractFeatures(s.get(i)))
						for (String v : getFeatureStrings(s, i, -1))
							addMap(v);
				}
			} else {
				for (Predicate pred : s.getPredicates()) {
					if (doExtractFeatures(pred))
						for (String v : getFeatureStrings(pred, null))
							addMap(v);
				}
			}
		}
	}

	public String getName() {
		return FeatureGenerator.getCanonicalName(f1.name, f2.name);
	}
}
