package se.lth.cs.srl.pipeline;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipOutputStream;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.languages.German;
import se.lth.cs.srl.languages.Language;
import se.lth.cs.srl.languages.Language.L;
import uk.ac.ed.inf.srl.features.Feature;
import uk.ac.ed.inf.srl.features.FeatureSet;
import uk.ac.ed.inf.srl.ml.LearningProblem;
import uk.ac.ed.inf.srl.ml.Model;

public class PredicateIdentifier extends AbstractStep {

	private static final String FILEPREFIX = "pi_";

	public PredicateIdentifier(FeatureSet fs) {
		super(fs);
	}

	public void parse(Sentence s) {
		boolean containspreds = false;
		for (int i = 1, size = s.size(); i < size; ++i) {
			Integer label = classifyInstance(s, i);

			if (label.equals(POSITIVE)
					|| (Language.getLanguage() instanceof German && s.get(i)
							.getPOS().startsWith("VV"))) {
				s.makePredicate(i);
				containspreds = true;
			}
		}

		if ((Language.getLanguage() instanceof German)) {
			// Set<Word> heads = s.get(0).getChildren();
			// OUTER: for(Word w : heads) {
			// if(w.getLemma().equals("sein")) {
			// for(Word c : w.getChildren()) {
			for (int i = 1, size = s.size(); i < size; ++i) {
				if(s.get(i) instanceof Predicate) continue;
				
				Word c = s.get(i);
				if (c.getDeprel().equals("PD") || c.getDeprel().equals("pred")) {
					s.makePredicate(c.getIdx());
					// break OUTER;
				}
				// }
				// }
			}
		}
	}

	private Integer classifyInstance(Sentence s, int i) {
		String POSPrefix = null;
		String POS = s.get(i).getPOS();
		for (String prefix : featureSet.POSPrefixes) {
			if (POS.startsWith(prefix)) {
				POSPrefix = prefix;
				break;
			}
		}
		if (POSPrefix == null)
			return NEGATIVE;
		Model m = models.get(POSPrefix);
		Collection<Integer> indices = new TreeSet<>();
		Map<Integer, Double> nonbinFeats = new TreeMap<>();
		Integer offset = 0;
		for (Feature f : featureSet.get(POSPrefix)) {
			f.addFeatures(s, indices, nonbinFeats, i, -1, offset, true);
			offset += f.size(true);
		}
		return m.classify(indices, nonbinFeats);
	}

	@Override
	protected String getModelFileName() {
		return FILEPREFIX + ".models";
	}


	@Override
	public void done() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void train() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeModels(ZipOutputStream zos) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prepareLearning() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prepareLearning(int i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void extractInstances(Sentence s) {
		// TODO Auto-generated method stub
		
	}

}
