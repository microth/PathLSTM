package se.lth.cs.srl.util;

import is2.lemmatizer.Lemmatizer;
import is2.parser.Parser;
import is2.tag.Tagger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import se.lth.cs.srl.options.FullPipelineOptions;
import se.lth.cs.srl.options.Options;

public class BohnetHelper {

	public static Lemmatizer getLemmatizer(File modelFile)
			throws FileNotFoundException, IOException {
		String[] argsL = { "-model", modelFile.toString() };
		return new Lemmatizer(modelFile.toString());

		// new is2.lemmatizer.Options(argsL));
	}

	public static Tagger getTagger(File modelFile) {
		String[] argsT = { "-model", modelFile.toString() };
		return new Tagger(modelFile.toString());
		// new is2.tag.Options(argsT));
	}

	public static is2.mtag.Tagger getMTagger(File modelFile) throws IOException {
		String[] argsMT = { "-model", modelFile.toString() };
		return new is2.mtag.Tagger(modelFile.toString());
		// new is2.mtag.Options(argsMT));
	}

	public static Parser getParser(File modelFile) {
		String[] argsDP = {
				"-model",
				modelFile.toString(),
				"-cores",
				Integer.toString(Math.min(Options.cores,
						FullPipelineOptions.cores)) };
		return new Parser(modelFile.toString());
		// new is2.parser.Options(argsDP));
	}

}
