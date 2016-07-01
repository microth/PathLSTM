package se.lth.cs.srl.preprocessor.tokenization;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import se.lth.cs.srl.corpus.StringInText;

public class OpenNLPToolsTokenizerWrapper implements Tokenizer {
	private int startpos = 0;

	opennlp.tools.tokenize.Tokenizer tokenizer;

	public OpenNLPToolsTokenizerWrapper(
			opennlp.tools.tokenize.Tokenizer tokenizerImplementation) {
		this.tokenizer = tokenizerImplementation;
	}

	@Override
	public String[] tokenize(String sentence) {
		String[] tokens = tokenizer.tokenize(sentence);
		String[] withRoot = new String[tokens.length + 1];
		// withRoot[0]="<root>";
		withRoot[0] = is2.io.CONLLReader09.ROOT;
		System.arraycopy(tokens, 0, withRoot, 1, tokens.length);
		return withRoot;
	}

	public static OpenNLPToolsTokenizerWrapper loadOpenNLPTokenizer(
			File modelFile) throws IOException {
		BufferedInputStream modelIn = new BufferedInputStream(
				new FileInputStream(modelFile.toString()));
		opennlp.tools.tokenize.Tokenizer tokenizer = new TokenizerME(
				new TokenizerModel(modelIn));
		return new OpenNLPToolsTokenizerWrapper(tokenizer);
	}

	@Override
	public StringInText[] tokenizeplus(String sentence) {
		Reader r = new StringReader(sentence);
		List<StringInText> l = new ArrayList<StringInText>();
		for (String s : tokenize(sentence)) {
			Word w = new Word(s);
			l.add(new StringInText(w.word(), w.beginPosition() + startpos, w
					.endPosition() + startpos));
		}
		StringInText[] tok = new StringInText[l.size()];
		// tok[0]=new StringInText(is2.io.CONLLReader09.ROOT,0,0);
		int i = 0;
		for (StringInText s : l)
			tok[i++] = s;

		startpos += (1 + sentence.length());

		return tok;
	}
}
