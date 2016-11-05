package se.lth.cs.srl.preprocessor.tokenization;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import se.lth.cs.srl.corpus.StringInText;

public class WhiteSpaceTokenizer implements Tokenizer {

	@Override
	public String[] tokenize(String sentence) {
		StringTokenizer tokenizer = new StringTokenizer(sentence);
		String[] tokens = new String[tokenizer.countTokens() + 1];
		int r = 0;
		tokens[r++] = is2.io.CONLLReader09.ROOT;
		while (tokenizer.hasMoreTokens())
			tokens[r++] = tokenizer.nextToken();
		return tokens;
	}

	public static void main(String[] args) {
		String t1 = "En gul bil körde hundratusen mil.";
		String t2 = "Leonardos fullständiga namn var Leonardo di ser Piero da Vinci.";
		String t3 = "Genom skattereformen införs individuell beskattning (särbeskattning) av arbetsinkomster.";
		String t4 = "\"Oh, no,\" she's saying, \"our $400 blender can't handle something this hard!\"";
		String[] tests = { t1, t2, t3, t4 };
		for (String test : tests) {
			WhiteSpaceTokenizer tokenizer = new WhiteSpaceTokenizer();
			String[] tokens = tokenizer.tokenize(test);
			for (String token : tokens)
				System.out.println(token);
			System.out.println();
		}
	}

	@Override
	public StringInText[] tokenizeplus(String sentence) {
		int offset = 0;
		StringTokenizer tokenizer = new StringTokenizer(sentence);
		List<StringInText> l = new ArrayList<>();

		while (tokenizer.hasMoreTokens()) {
			String s = tokenizer.nextToken();
			l.add(new StringInText(s, offset, offset + s.length()));
			offset += (1 + s.length());
		}
		StringInText[] tok = new StringInText[l.size() + 1];
		tok[0] = new StringInText(is2.io.CONLLReader09.ROOT, 0, 0);
		int i = 1;
		for (StringInText s : l)
			tok[i++] = s;

		return tok;
	}

}
