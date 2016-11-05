package se.lth.cs.srl.preprocessor.tokenization;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import se.lth.cs.srl.corpus.StringInText;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;

public class StanfordPTBTokenizer implements Tokenizer {
	private int startpos = 0;

	@Override
	public String[] tokenize(String sentence) {
		Reader r = new StringReader(sentence);
		PTBTokenizer<Word> tokenizer = PTBTokenizer.newPTBTokenizer(r);
		List<String> l = new ArrayList<String>();
		while (tokenizer.hasNext()) {
			Word w = tokenizer.next();
			l.add(w.word());
		}
		String[] tok = new String[l.size() + 1];
		tok[0] = is2.io.CONLLReader09.ROOT;
		int i = 1;
		for (String s : l)
			tok[i++] = s;
		return tok;
	}

	public StringInText[] tokenizeplus(String sentence) {
		Reader r = new StringReader(sentence);
		PTBTokenizer<Word> tokenizer = PTBTokenizer.newPTBTokenizer(r);
		List<StringInText> l = new ArrayList<StringInText>();
		while (tokenizer.hasNext()) {
			Word w = tokenizer.next();
			l.add(new StringInText(w.word(), w.beginPosition() + startpos, w
					.endPosition() + startpos));
		}
		StringInText[] tok = new StringInText[l.size() + 1];
		tok[0] = new StringInText(is2.io.CONLLReader09.ROOT, 0, 0);
		int i = 1;
		for (StringInText s : l)
			tok[i++] = s;

		startpos += (1 + sentence.length());

		return tok;
	}

	public static void main(String[] args) {
		// String
		// t="\"Oh, no,\" she's saying, \"our $400 blender can't handle something this hard!\"";
		String t2 = "Another ex-Golden Stater, Paul Stankowski from Oxnard, is contending\n"
				+ "for a berth on the U.S. Ryder Cup team after winning his first PGA Tour\n"
				+ "event last year and staying within three strokes of the lead through\n"
				+ "three rounds of last month's U.S. Open. H.J. Heinz Company said it\n"
				+ "completed the sale of its Ore-Ida frozen-food business catering to the\n"
				+ "service industry to McCain Foods Ltd. for about $500 million.\n"
				+ "It's the first group action of its kind in Britain and one of\n"
				+ "only a handful of lawsuits against tobacco companies outside the\n"
				+ "U.S. A Paris lawyer last year sued France's Seita SA on behalf of\n"
				+ "two cancer-stricken smokers. Japan Tobacco Inc. faces a suit from\n"
				+ "five smokers who accuse the government-owned company of hooking\n"
				+ "them on an addictive product.";
		String[] tokens = new StanfordPTBTokenizer().tokenize(t2);
		for (String token : tokens)
			System.out.println(token);
	}

	public void resetStartPosition() {
		startpos = 0;
	}
}
