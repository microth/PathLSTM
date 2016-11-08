package se.lth.cs.srl.languages;

import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.options.FullPipelineOptions;

/**
 * this class is just so we can run the pipeline without the srl stuff i.e., to
 * run the http interface of the anna pipeline without srl.
 * 
 * @author anders
 *
 */
public abstract class AbstractDummyLanguage extends Language {

	@Override
	public String getDefaultSense(Predicate pred) {
		throw new Error("!");
	}

	@Override
	public String getCoreArgumentLabelSequence(Predicate pred,
			Map<Word, String> proposition) {
		throw new Error("!");
	}

	@Override
	public String getLexiconURL(Predicate pred) {
		throw new Error("!");
	}

	@Override
	public String verifyLanguageSpecificModelFiles(FullPipelineOptions options) {
		return null;
	}

}
