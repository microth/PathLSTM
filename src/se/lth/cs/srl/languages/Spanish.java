package se.lth.cs.srl.languages;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.options.FullPipelineOptions;

public class Spanish extends Language {

	private static final Pattern CALSPattern = Pattern
			.compile("((arg0|arg1|arg2|arg3|arg4)-[a-z]+)");

	private static final boolean useVoice = false; // We don't use voice...

	/**
	 * This is the code we used to deduce voice in Spanish (and Catalan) for
	 * CoNLL 2009, however we didn't actually use it in the final submission. I
	 * think it was because we never saw any real improvement. I'm not sure it's
	 * proper though, my Spanish skills are rather non-existant. I just put it
	 * here for future reference.
	 * 
	 * @param pred
	 *            the predicate
	 * @return true if the predicate (verb) is in passive tense, false otherwise
	 */
	private boolean isPassive(Predicate pred) {
		for (Word c : pred.getChildren())
			if ((c.getLemma().equals("estar") || c.getLemma().equals("ser"))
					&& c.getFeats().contains("auxiliary"))
				return true;
		return false;
	}

	@Override
	public String getCoreArgumentLabelSequence(Predicate pred,
			Map<Word, String> proposition) {
		Sentence s = pred.getMySentence();
		StringBuffer ret = new StringBuffer();
		for (int i = 1; i < s.size(); ++i) {
			Word w = s.get(i);
			if (pred == w) {
				ret.append(" ").append(pred.getSense());
				if (useVoice)
					ret.append(isPassive(pred) ? "/P" : "/A");
			}
			if (proposition.containsKey(w)) {
				Matcher m = CALSPattern.matcher(proposition.get(w));
				if (m.matches())
					ret.append(" ").append(m.group(1));
			}
		}
		return ret.toString();
	}

	@Override
	public String getDefaultSense(Predicate pred) {
		String PFeat = pred.getFeats();
		String label;
		if (PFeat.contains("postype=auxiliary")) {
			label = "c2";
		} else if (PFeat.contains("postype=common")) {
			label = "a2";
		} else if (PFeat.contains("postype=main")) {
			label = "a2";
		} else if (PFeat.contains("postype=qualificative")) {
			label = "b2";
		} else if (PFeat.contains("postype=semiauxiliary")) {
			label = "c2";
		} else {
			label = "a2";
		}
		return pred.getLemma() + "." + label;
	}

	@Override
	public L getL() {
		return L.spa;
	}

	@Override
	public String getLexiconURL(Predicate pred) {
		// dunno if this still works
		return "http://clic.ub.edu/mbertran/ancora/lexentry.php?file="
				+ pred.getLemma() + ".lex.xml&lexicon=AnCoraVerb_ES";
	}

	// @Override
	// public Preprocessor getPreprocessor(FullPipelineOptions options) throws
	// IOException {
	// Tokenizer tokenizer=(options.loadPreprocessorWithTokenizer ?
	// OpenNLPToolsTokenizerWrapper.loadOpenNLPTokenizer(options.tokenizer) :
	// null);
	// Lemmatizer
	// lemmatizer=options.lemmatizer==null?null:BohnetHelper.getLemmatizer(options.lemmatizer);
	// Tagger
	// tagger=options.tagger==null?null:BohnetHelper.getTagger(options.tagger);
	// is2.mtag.Tagger
	// mtagger=options.morph==null?null:BohnetHelper.getMTagger(options.morph);
	// Parser
	// parser=options.parser==null?null:BohnetHelper.getParser(options.parser);
	// Preprocessor pp=new Preprocessor(tokenizer, lemmatizer, tagger, mtagger,
	// parser);
	// return pp;
	// }

	@Override
	public String verifyLanguageSpecificModelFiles(FullPipelineOptions options) {
		// TODO Auto-generated method stub
		// cba
		return null;
	}

	@Override
	public String toLangNameString() {
		return "Spanish";
	}

}
