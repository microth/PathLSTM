package se.lth.cs.srl.preprocessor.tokenization;

import se.lth.cs.srl.corpus.StringInText;

public interface Tokenizer {

	/**
	 * Tokenize a sentence. The returned array contains a root-token
	 * 
	 * @param sentence
	 *            The sentence to tokenize
	 * @return a root token, followed by the forms
	 */
	public abstract String[] tokenize(String sentence);

	public abstract StringInText[] tokenizeplus(String sentence);

}
