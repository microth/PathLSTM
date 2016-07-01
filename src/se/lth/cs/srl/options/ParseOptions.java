package se.lth.cs.srl.options;

import java.io.File;

import se.lth.cs.srl.Parse;

public class ParseOptions extends Options {

	ParseOptions() {
	}

	public ParseOptions(String[] args) {
		superParseCmdLine(args);
	}

	public boolean skipPI = false; // Whether to skip predicate identification.
	public boolean skipAI = false; // " argument identification
	public boolean skipPD = false; // " predicate disambiguation
	public boolean useReranker = false;

	public String framenetdir = null;
	public boolean printRDF = false;
	public boolean printANN = false;
	public boolean printXML = false;

	public File output;

	public double global_alfa = 1.0;
	public boolean globalFeats = false;
	public boolean writeCoref = false;
	public boolean externalNNs = false;
	public boolean noPathEmbs = false;

	@Override
	int parseCmdLine(String[] args, int ai) {
		if (ai == args.length - 1) {
			output = new File(args[ai]);
			return ai + 1;
		}
		if (args[ai].equals("-writeCoref")) {
			ai++;
			writeCoref = true;
		}
		if (args[ai].equals("-globalFeats")) {
			ai++;
			globalFeats = true;
		} else if (args[ai].equals("-externalNNs")) {
			ai++;
			externalNNs = true;
		} else if (args[ai].equals("-noPathEmbs")) {
			ai++;
			noPathEmbs = true;
		}
		if (args[ai].equals("-framenet")) {
			ai++;
			framenetdir = args[ai++];
		}
		if (args[ai].equals("-nopi")) {
			ai++;
			skipPI = true;
		}
		if (args[ai].equals("-nopd")) {
			ai++;
			skipPD = true;
		}
		if (args[ai].equals("-noai")) {
			ai++;
			skipAI = true;
		}
		if (args[ai].equals("-printRDF")) {
			ai++;
			printRDF = true;
		}
		if (args[ai].equals("-printANN")) {
			ai++;
			printANN = true;
		}
		if (args[ai].equals("-printXML")) {
			ai++;
			printXML = true;
		}
		if (args[ai].equals("-reranker")) {
			ai++;
			useReranker = true;
		} else if (args[ai].equals("-alfa")) {
			ai++;
			global_alfa = Double.parseDouble(args[ai]);
			ai++;
		}
		return ai;
	}

	@Override
	void usage() {
		System.err.println("Usage:");
		System.err.println(" java -cp <classpath> " + Parse.class.getName()
				+ " <lang> <input-corpus> <model-file> [options] <output>");
		System.err.println();
		System.err.println("Example:");
		System.err
				.println(" java -cp srl.jar:lib/liblinear-1.51-with-deps.jar"
						+ Parse.class.getName()
						+ " eng ~/corpora/eng/CoNLL2009-ST-English-evaluation-SRLonly.txt eng-srl.mdl -reranker -nopi -alfa 1.0 eng-eval.out");
		System.err.println();
		System.err
				.println(" parses in the input corpus using the model eng-srl.mdl and saves it to eng-eval.out, using a reranker and skipping the predicate identification step");
		System.err.println();
		super.printUsageLanguages(System.err);
		System.err.println();
		super.printUsageOptions(System.err);
		System.err.println();
		System.err.println("Parsing-specific options:");
		System.err
				.println(" -nopi           skips the predicate identification. This is equivalent to the");
		System.err.println("                 setting in the CoNLL 2009 ST.");
		System.err
				.println(" -reranker       uses a reranker (assumed to be included in the model)");
		System.err
				.println(" -alfa <double>  the alfa used by the reranker. (default "
						+ global_alfa + ")");
	}

	@Override
	boolean verifyArguments() {
		if (!modelFile.exists() || !modelFile.canRead()) {
			System.err.println("Model file " + modelFile
					+ " does not exist or can not be read. Aborting.");
			System.exit(1);
		}
		return true;
	}

}
