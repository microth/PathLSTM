package uk.ac.ed.inf.srl.features;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.features.ContinuousFeature;
import se.lth.cs.srl.features.FeatureName;
import se.lth.cs.srl.features.TargetWord;
import se.lth.cs.srl.features.WordExtractor;
import se.lth.cs.srl.util.WordEmbedding;

public class ArgDependentEmbedding extends ContinuousFeature {
	private static final long serialVersionUID = 1L;

	private WordExtractor wordExtractor;
	private String TW;

	private WordEmbedding bc;
	private int dim;
	private boolean comp;
	private boolean avg;
	private boolean tokenbased;

	protected ArgDependentEmbedding(FeatureName name, TargetWord tw,
			String POSPrefix, boolean comp, boolean avg, WordEmbedding bc,
			int dim, boolean tokenbased) {
		super(name, true, false, POSPrefix);
		indexcounter = 1;

		this.wordExtractor = WordExtractor.getExtractor(tw);
		this.bc = bc;
		this.dim = dim;
		this.comp = comp;
		this.avg = avg;
		this.tokenbased = tokenbased;
		this.TW = tw.toString().toUpperCase();

		indices.put(
				"WE" + (comp ? "COMP" : avg ? "AVG" : tokenbased ? "TOK" : TW)
						+ dim, 1);

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
		// Uhm... something happens for every argument of every predicate here
		// for(Predicate p:s.getPredicates()){
		// if(doExtractFeatures(p))
		// for(Word arg:p.getArgMap().keySet()){
		// Word w=wordExtractor.getWord(null, arg);
		// if(w!=null)
		// addMap(bc.getValue(w.getForm(), dim));
		// }
		// }
	}

	@Override
	public Double getFeatureValue(Sentence s, int predIndex, int argIndex) {
		Word w = wordExtractor.getWord(s, predIndex, argIndex);
		if (w == null)
			return 0.0;
		else if (avg)
			return getAvgFeatureValue(s.get(argIndex));
		else
			return comp ? getValue(w, dim) + getValue(s.get(predIndex), dim)
					: getValue(w, dim);
	}

	private Double getAvgFeatureValue(Word word) {
		List<Word> allChildren = new LinkedList<>();
		List<Word> processed = new LinkedList<>();

		double ret = 0.0;
		allChildren.add(word);
		while (!allChildren.isEmpty()) {
			Word w = allChildren.remove(0);
			ret += getValue(w, dim);
			processed.add(w);

			for (Word wc : w.getChildren())
				if (!processed.contains(wc))
					allChildren.add(wc);
		}

		return ret / (processed.size() * 1.0);
	}

	public String getFeatureString(Sentence s, int predIndex, int argIndex) {
		return "WE" + (comp ? "COMP" : avg ? "AVG" : tokenbased ? "TOK" : TW)
				+ dim;
	}

	@Override
	public Double getFeatureValue(Predicate pred, Word arg) {
		Word w = wordExtractor.getWord(pred, arg);

		if (w == null)
			return 0.0;
		else if (avg)
			return getAvgFeatureValue(arg);
		else
			return comp ? getValue(w, dim) + getValue(pred, dim) : getValue(w,
					dim);
	}

	public String getFeatureString(Predicate pred, Word arg) {
		return "WE" + (comp ? "COMP" : avg ? "AVG" : tokenbased ? "TOK" : TW)
				+ dim;
	}

	private double getValue(Word w, int dim) {
		if (tokenbased)
			return w.getRep(dim);
		else
			return bc.getValue(w.getForm(), dim);
	}
}
