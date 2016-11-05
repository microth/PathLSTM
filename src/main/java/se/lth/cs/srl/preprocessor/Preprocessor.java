package se.lth.cs.srl.preprocessor;

import se.lth.cs.srl.corpus.StringInText;
import se.lth.cs.srl.preprocessor.tokenization.Tokenizer;
import is2.data.SentenceData09;

public abstract class Preprocessor {
	protected Tokenizer tokenizer;
	public long tokenizeTime = 0;
	public long lemmatizeTime = 0;
	public long dpTime = 0;

	public abstract boolean hasParser();

	public abstract StringBuilder getStatus();

	protected abstract SentenceData09 preprocess(SentenceData09 sentence);

	public SentenceData09 preprocess(String[] forms) {
		SentenceData09 instance = new SentenceData09();
		instance.init(forms);
		return preprocess(instance);
	}

	public String[] tokenize(String sentence) {
		synchronized (tokenizer) {
			long start = System.currentTimeMillis();
			String[] words = tokenizer.tokenize(sentence);
			tokenizeTime += (System.currentTimeMillis() - start);
			return words;
		}
	}

	public StringInText[] tokenizeplus(String sentence) {
		synchronized (tokenizer) {
			long start = System.currentTimeMillis();
			StringInText[] words = tokenizer.tokenizeplus(sentence);
			tokenizeTime += (System.currentTimeMillis() - start);
			return words;
		}
	}
}
