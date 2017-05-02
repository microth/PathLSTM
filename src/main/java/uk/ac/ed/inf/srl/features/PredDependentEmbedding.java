package se.lth.cs.srl.features;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.util.WordEmbedding;

public class PredDependentEmbedding extends ContinuousFeature {
	private static final long serialVersionUID = 1L;

	private WordExtractor wordExtractor;
	private WordEmbedding bc;
	private int dim;
	private boolean tokenembedding;

	protected PredDependentEmbedding(FeatureName name, TargetWord tw,
			boolean includeAllWords, String POSPrefix, WordEmbedding bc,
			int dim, boolean token) {
		// super(name, null, tw, includeAllWords, POSPrefix);
		super(name, false, true, POSPrefix);
		indices.put("WEPRED" + dim, 1);

		this.wordExtractor = WordExtractor.getExtractor(tw);
		this.bc = bc;
		this.dim = dim;
		this.tokenembedding = token;
	}

	@Override
	public Double getFeatureValue(Sentence s, int predIndex, int argIndex) {
		Word w = wordExtractor.getWord(s, predIndex, argIndex);
		if (w == null)
			return 0.0;
		else
			return this.getValue(w, dim);
		// return w.getRep(dim);
	}

	@Override
	public String getFeatureString(Sentence s, int predIndex, int argIndex) {
		Word w = wordExtractor.getWord(s, predIndex, argIndex);
		return "WEPRED" + dim;
	}

	@Override
	public Double getFeatureValue(Predicate pred, Word arg) {
		Word w = wordExtractor.getWord(pred, arg);
		if (w == null)
			return 0.0;
		else {
			// return w.getRep(dim);
			return this.getValue(w, dim);
		}
	}

	@Override
	public String getFeatureString(Predicate pred, Word arg) {
		return "WEPRED" + dim;
	}

	private Double getValue(Word w, int dim) {
		if (tokenembedding)
			return w.getRep(dim);
		else
			return bc.getValue(w.getForm(), dim);
	}
}
