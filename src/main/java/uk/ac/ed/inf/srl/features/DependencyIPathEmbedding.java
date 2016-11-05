package uk.ac.ed.inf.srl.features;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import uk.ac.ed.inf.srl.lstm.DataConverter;
import uk.ac.ed.inf.srl.lstm.EmbeddingNetwork;

public class DependencyIPathEmbedding extends DependencyPathEmbedding {
	private static final long serialVersionUID = 1L;
	protected DependencyIPathEmbedding(FeatureName name, TargetWord tw,
			String POSPrefix, boolean comp, EmbeddingNetwork net, DataConverter dc,
			int dim) {
		super(name, tw, POSPrefix, comp, net, dc, dim);

		for(int i=0; i<dim; i++) indices.put("PE" + i, 1);
	}

	public String[] getFeatureString(Predicate pred, Word arg) {
		String[] ret = new String[dim];
		for(int i=0; i<0; i++)
			ret[i] = "PE"+i;
		return ret;
	}
	
	public String[] getFeatureString(Sentence s, int pred, int arg) {
		String[] ret = new String[dim];
		for(int i=0; i<0; i++)
			ret[i] = "PE"+i;
		return ret;
	}

}
