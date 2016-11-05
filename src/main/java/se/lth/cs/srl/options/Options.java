package se.lth.cs.srl.options;

import java.io.File;
import java.io.PrintStream;

import se.lth.cs.srl.languages.Language;
import se.lth.cs.srl.languages.Language.L;

public abstract class Options {

	public File inputCorpus;
	public File modelFile;

	public int threads = 1;

	public int global_aiBeam = 4;
	public int global_acBeam = 4;

	public static int cores = Runtime.getRuntime().availableProcessors();

	protected void superParseCmdLine(String[] args) {
		if (args.length < 3) {
			System.err.println("Not enough arguments, aborting.");
			usage();
			System.exit(1);
		}
		int ai = 0;
		L lang = null;
		try {
			lang = L.valueOf(args[ai]);
		} catch (Exception e) {
			System.err.println("Unknown language " + args[ai] + ", aborting.");
			System.exit(1);
		}
		ai++;
		Language.setLanguage(lang);
		inputCorpus = new File(args[ai]);
		ai++;
		modelFile = new File(args[ai]);
		ai++;
		while (ai < args.length) { // First we try to parse the common options.
									// If none are found, then we try the
									// specialized methods for Learn and Parse
									// (ie the abstract parseCmdLine method)
			if (args[ai].equals("-threads")) {
				ai++;
				threads = Integer.parseInt(args[ai]);
				ai++;
			} else if (args[ai].equals("-aibeam")) {
				ai++;
				global_aiBeam = Integer.parseInt(args[ai]);
				ai++;
			} else if (args[ai].equals("-acbeam")) {
				ai++;
				global_acBeam = Integer.parseInt(args[ai]);
				ai++;
			} else if (args[ai].equals("-cores")) {
				ai++;
				cores = Integer.parseInt(args[ai++]);
				System.out.println("using cores: " + cores);
			} else if (args[ai].equals("-help") || args[ai].equals("--help")) {
				usage();
				System.exit(0);
			} else {
				int newAi = parseCmdLine(args, ai);
				if (newAi == ai) {
					System.err.println("Unknown argument: " + args[ai]
							+ ", aborting.");
					usage();
					System.exit(1);
				} else {
					ai = newAi;
				}
			}
		}
		// Ok, were done parsing the command line, now lets verify that the
		// arguments make sense.
		if (!inputCorpus.exists() || !inputCorpus.canRead()) {
			System.err.println("Input corpus" + inputCorpus
					+ " does not exist or can not be read, aborting.");
			System.exit(1);
		}
		verifyArguments();
	}

	abstract int parseCmdLine(String[] args, int ai);

	abstract boolean verifyArguments();

	abstract void usage();

	protected void printUsageOptions(PrintStream out) {
		out.println("Options:");
		// out.println(" -threads <int>    the number of threads used (not implemented now, for future use)");
		out.println(" -aibeam <int>     the size of the ai-beam for the reranker");
		out.println(" -acbeam <int>     the size of the ac-beam for the reranker");
		out.println(" -help             prints this message");
	}

	protected void printUsageLanguages(PrintStream out) {
		out.println("<lang> corresponds to the language and is one of");
		out.println(" chi, eng, ger");
	}

}
