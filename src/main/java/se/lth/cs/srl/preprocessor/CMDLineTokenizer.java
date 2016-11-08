package se.lth.cs.srl.preprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import se.lth.cs.srl.languages.Language;
import se.lth.cs.srl.languages.Language.L;
import se.lth.cs.srl.preprocessor.tokenization.Tokenizer;

public class CMDLineTokenizer {

	public static void main(String[] args) throws IOException {
		String l = args[0];
		File modelFile = args.length > 1 ? new File(args[1]) : null;
		L lang = null;
		try {
			lang = L.valueOf(l);
		} catch (Exception e) {
			System.err.println("Unknown language " + l + ", aborting.");
			System.exit(1);
		}
		Language.setLanguage(lang);
		Tokenizer tokenizer = Language.getLanguage().getTokenizer(modelFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in, "UTF8"));
		String line;
		int senCount = 0;
		while ((line = reader.readLine()) != null) {
			senCount++;
			String[] tokens = tokenizer.tokenize(line);
			for (int i = 1; i < tokens.length; ++i) {
				StringBuilder sb = new StringBuilder();
				sb.append(senCount).append('_').append(i).append('\t')
						.append(tokens[i]).append(COLUMNS);
				System.out.println(sb.toString());
			}
			System.out.println();
		}
	}

	static final String COLUMNS = "\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_";

	public static void usage() {
		System.err
				.println("Reads untokenized text on STDIN (one sentence per line), and writes it out in CoNLL09 format to STDOUT");
		System.err.println("Usage: java -cp ... "
				+ CMDLineTokenizer.class.getCanonicalName()
				+ " <eng|chi|swe|fre|...> [model-file]");
	}
}
