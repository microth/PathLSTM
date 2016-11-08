package se.lth.cs.srl.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.io.DepsOnlyCoNLL09Reader;
import se.lth.cs.srl.io.SentenceReader;

public class ChineseDesegmenter {

	public static String desegment(String[] forms) {
		StringBuilder ret = new StringBuilder();
		for (int i = 1; i < forms.length; ++i)
			ret.append(forms[i]);
		return ret.toString();
	}

	public static void main(String[] args) throws FileNotFoundException {

		String inputFile =
		// "/home/anders/corpora/conll09/CoNLL2009-ST-Chinese-train.txt";
		"/home/anders/corpora/conll09/chi/CoNLL2009-ST-evaluation-Chinese.txt";
		String outputFile = "chi-desegmented.out";
		boolean separateLines = true;
		if (args.length > 0)
			inputFile = args[0];
		if (args.length > 1)
			outputFile = args[1];
		if (args.length > 2)
			separateLines = Boolean.parseBoolean(args[2]); // Whether to print
															// newlines between
															// sentences.
		File input = new File(inputFile);
		File output = new File(outputFile);
		SentenceReader reader = new DepsOnlyCoNLL09Reader(input);
		PrintStream out = new PrintStream(new FileOutputStream(output));
		for (Sentence s : reader) {
			String desegmented = desegment(s.getFormArray());
			if (separateLines)
				out.println(desegmented);
			else
				out.print(desegmented);
		}
	}

}
