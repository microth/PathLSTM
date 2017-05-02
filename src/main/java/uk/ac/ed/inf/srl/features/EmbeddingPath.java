package se.lth.cs.srl.features;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.util.WordEmbedding;

public class EmbeddingPath extends ContinuousFeature {
	private static final long serialVersionUID = 1L;

	private WordEmbedding bc;
	private int dim;
	private boolean syntax;

	protected EmbeddingPath(FeatureName name, TargetWord tw, String POSPrefix,
			boolean syntax, WordEmbedding bc, int dim) {
		super(name, true, false, POSPrefix);
		indices.put("WEPATH" + dim, 1);
		indexcounter = 1;
		this.bc = bc;
		this.dim = dim;
		this.syntax = syntax;

	}

	@Override
	public void addFeatures(Sentence s, Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, int predIndex, int argIndex,
			Integer offset, boolean allWords) {
		nonbinFeats.put(indexOf(getFeatureString(s, predIndex, argIndex))
				+ offset, getFeatureValue(s, predIndex, argIndex));
	}

	@Override
	public void addFeatures(Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, Predicate pred, Word arg,
			Integer offset, boolean allWords) {
		nonbinFeats.put(indexOf(getFeatureString(pred, arg)) + offset,
				getFeatureValue(pred, arg));
	}

	@Override
	protected void performFeatureExtraction(Sentence s, boolean allWords) {
	}

	@Override
	public Double getFeatureValue(Sentence s, int predIndex, int argIndex) {
		if (syntax == true)
			return getDepBasedFeatureValue(s, predIndex, argIndex);

		double ret = 0.0;
		double size = Math.abs(predIndex - argIndex);
		if (size == 1.0)
			return ret;

		boolean up = true;
		if (predIndex < argIndex)
			up = false;

		for (int i = up ? argIndex : predIndex; i < (up ? predIndex : argIndex); i++) {
			ret += bc.getValue(s.get(i).getForm(), dim);
		}
		return (ret / (size - 1.0));
	}

	public Double getDepBasedFeatureValue(Sentence s, int predIndex,
			int argIndex) {
		double ret = 0.0;
		List<Word> path = Word.findPath(s.get(predIndex), s.get(argIndex));
		if (path == null)
			return ret;

		double size = (double) path.size();
		if (size == 1.0)
			return ret;

		for (int i = 0; i < (path.size() - 1); ++i)
			ret += bc.getValue(path.get(i).getForm(), dim);

		return (ret / (size - 1.0));
	}

	public String getFeatureString(Sentence s, int predIndex, int argIndex) {
		return "WEPATH" + dim;
	}

	@Override
	public Double getFeatureValue(Predicate pred, Word arg) {
		return getFeatureValue(pred.getMySentence(), pred.getIdx(),
				arg.getIdx());
	}

	public String getFeatureString(Predicate pred, Word arg) {
		return "WEPATH" + dim;
	}
}
