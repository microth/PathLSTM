package se.lth.cs.srl.options;

import java.io.File;
import java.io.PrintStream;

import se.lth.cs.srl.languages.Language;
import se.lth.cs.srl.languages.Language.L;

public abstract class FullPipelineOptions {

	public L l;
	public File tokenizer;
	public File lemmatizer;
	public File tagger;
	public File morph;
	public File parser;
	public File srl;

	public boolean reranker = false;
	public int aiBeam = 4;
	public int acBeam = 4;
	public double alfa = 1.0;

	public static int cores = Runtime.getRuntime().availableProcessors();

	public boolean skipPI = false;
	public boolean skipAI = false;

	public boolean loadPreprocessorWithTokenizer = true;

	public boolean printRDF = false;
	public boolean printANN = false;
	public boolean hybrid = false;
	public boolean external = false;
	
	public boolean externalNNs = false;
	public boolean globalFeats = false;
	
	// links to external tools/servers/resources
	public String glovedir = null;
	public String mstserver = null;
	public String uiucparser = null;
	public String semaforserver = null;
	public String framenetdir = null;
	public boolean stanford = false;
	
	public static String NULL_LANGUAGE_NAME = "Unk";

	public ParseOptions getParseOptions() {
		ParseOptions parseOptions = new ParseOptions();
		parseOptions.modelFile = srl;
		parseOptions.useReranker = reranker;
		parseOptions.global_aiBeam = aiBeam;
		parseOptions.global_acBeam = acBeam;
		parseOptions.global_alfa = alfa;
		parseOptions.skipPI = skipPI;
		parseOptions.skipAI = skipAI;
		parseOptions.printRDF = printRDF;
		parseOptions.printANN = printANN;
		parseOptions.externalNNs = externalNNs;
		parseOptions.globalFeats = globalFeats;
		parseOptions.framenetdir = framenetdir;
		return parseOptions;
	}

	public void parseCmdLineArgs(String[] args) {
		int ai = 0;
		if (args.length < 1) {
			System.err.println("Not enough arguments. Aborting.");
			printUsage(System.err);
			System.exit(1);
		}
		try {
			l = L.valueOf(args[ai]);
		} catch (Exception e) {
			System.err.println("Unknown language: " + args[ai] + ", aborting.");
			System.err.println();
			printUsage(System.err);
			System.exit(1);
		}
		Language.setLanguage(l);
		ai++;
		while (ai < args.length) {
			int newAi = tryParseArg(args, ai);
			if (ai == newAi)
				newAi = trySubParseArg(args, ai);
			if (ai == newAi) {
				System.err.println("Unknown option: " + args[ai]);
				System.exit(1);
			}
			ai = newAi;
		}
	}

	protected abstract int trySubParseArg(String[] args, int ai);

	/**
	 * Tries to parse one argument off the cmdline.
	 * 
	 * @param args
	 *            the args for the main method
	 * @param ai
	 *            the current index
	 * @return the new ai
	 */
	public int tryParseArg(String[] args, int ai) {
		if (args[ai].equals("-h") || args[ai].equals("-help")
				|| args[ai].equals("--help")) {
			printUsage(System.err);
			System.exit(1);
		} else if (args[ai].equals("-hybrid")) {
			ai++;
			hybrid = true;
		} else if (args[ai].equals("-external")) {
			ai++;
			external = true;
		} else if (args[ai].equals("-token")) {
			ai++;
			tokenizer = new File(args[ai]);
			ai++;
		} else if (args[ai].equals("-lemma")) {
			ai++;
			lemmatizer = new File(args[ai]);
			ai++;
		} else if (args[ai].equals("-tagger")) {
			ai++;
			tagger = new File(args[ai]);
			ai++;
		} else if (args[ai].equals("-morph")) {
			ai++;
			morph = new File(args[ai]);
			ai++;
		} else if(args[ai].equals("-stanford")) {
			ai++;
			stanford = true;
		} else if (args[ai].equals("-parser")) {
			ai++;
			parser = new File(args[ai]);
			ai++;
		} else if (args[ai].equals("-srl")) {
			ai++;
			srl = new File(args[ai]);
			ai++;
		} else if (args[ai].equals("-reranker")) {
			ai++;
			reranker = true;
		} else if (args[ai].equals("-printRDF")) {
			ai++;
			printRDF = true;
		} else if (args[ai].equals("-printANN")) {
			ai++;
			printANN = true;
		} else if (args[ai].equals("-aiBeam")) {
			ai++;
			aiBeam = Integer.valueOf(args[ai]);
			ai++;
		} else if (args[ai].equals("-acBeam")) {
			ai++;
			acBeam = Integer.valueOf(args[ai]);
			ai++;
		} else if (args[ai].equals("-alfa")) {
			ai++;
			alfa = Double.parseDouble(args[ai]);
			ai++;
		} else if (args[ai].equals("-cores")) {
			ai++;
			cores = Integer.parseInt(args[ai++]);
		} else if (args[ai].equals("-glove")) {
			ai++;
			glovedir = args[ai++];
		} else if (args[ai].equals("-mst")) {
			ai++;
			mstserver = args[ai++];
		} else if (args[ai].equals("-semafor")) {
			ai++;
			semaforserver = args[ai++];
		} else if (args[ai].equals("-uiucparser")) {
			ai++;
			uiucparser = args[ai++];
		} else if (args[ai].equals("-framenet")) {
			ai++;
			framenetdir = args[ai++];
		} else if (args[ai].equals("-externalNNs")) {
			ai++;
			externalNNs=true;
		} else if (args[ai].equals("-globalFeats")) {
			ai++;
			globalFeats=true;
		} 
		return ai;
	}

	protected abstract Class<?> getIntendedEntryClass();

	protected abstract String getSubUsageOptions();

	public void printUsage(PrintStream out) {
		out.println("Usage:");
		out.println("java -cp ... " + getIntendedEntryClass().getName()
				+ " <lang> <options>");
		out.println();
		out.println("Where <lang> is one of: " + Language.getLsString());
		out.println();
		out.println("And <options> correnspond to one of the following:");
		out.println(USAGE_OPTIONS);
		out.println(getSubUsageOptions());
		out.println();
		out.println("The model files neccessary vary between languages. E.g. German uses a morphological tagger,\n"
				+ "whereas Chinese and English doesn't. The parser and srl models are always required though.\n"
				+ "For Chinese, the tokenizer model should point to the data directory of the Stanford Chinese\n"
				+ "Segmenter, as provided in the 2008-05-21 distribution.\n"
				+ "\n"
				+ "For further information check the website:\n"
				+ "http://code.google.com/p/mate-tools/\n");

	}

	private static final String USAGE_OPTIONS = "-token  <file>    path to the tokenizer model file\n"
			+ "-lemma  <file>    path to the lemmatizer model file\n"
			+ "-tagger <file>    path to the pos tagger model file\n"
			+ "-morph  <file>    path to the morphological tagger model file\n"
			+ "-parser <file>    path to the parser model file\n"
			+ "-srl    <file>    path to the srl model file\n"
			+ "-reranker         use the reranker for the srl-system (default is not)\n"
			+ "-aibeam <int>     set the beam width of the ai component (default 4)\n"
			+ "-acbeam <int>     set the beam width of the ac component (default 4)\n"
			+ "-alfa   <double>  set the alfa for the reranker (default 1.0)";

}
