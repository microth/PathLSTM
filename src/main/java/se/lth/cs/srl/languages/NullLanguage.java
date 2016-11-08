package se.lth.cs.srl.languages;

import se.lth.cs.srl.options.FullPipelineOptions;
import se.lth.cs.srl.preprocessor.tokenization.StanfordPTBTokenizer;
import se.lth.cs.srl.preprocessor.tokenization.Tokenizer;

//import se.lth.cs.srl.preprocessor.tokenization.WhiteSpaceTokenizer;

public class NullLanguage extends AbstractDummyLanguage {

	@Override
	public String toLangNameString() {
		return FullPipelineOptions.NULL_LANGUAGE_NAME;
	}

	@Override
	public L getL() {
		return L.nul;
	}
	
	// Not sure what is the better tokenizer... I'll leave it with Stanford for
	// now.
	Tokenizer getDefaultTokenizer() {
		// return new WhiteSpaceTokenizer();
		return new StanfordPTBTokenizer();
	}

}
