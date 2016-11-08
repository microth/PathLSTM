package se.lth.cs.srl.options;

import java.io.File;

import se.lth.cs.srl.CompletePipeline;

public class CompletePipelineCMDLineOptions extends FullPipelineOptions {

	// TODO add some sort of toString() method to all option classes. And make
	// the system print these out when it is initialized.

	public File output = new File("out.txt");
	public File input;
	public boolean desegment = false;

	public CompletePipelineCMDLineOptions() {
		super.loadPreprocessorWithTokenizer = false; // We think default is
														// CoNLL09 corpus,
	}

	@Override
	protected String getSubUsageOptions() {
		return "-test   <file>    the input corpus. assumed to be tokenized like CoNLL 09 data\n"
				+ "-out    <file>    the file to write output to (default out.txt)\n"
				+ "-nopi             skips the predicate identification\n"
				+ "-tokenize         implies the input is unsegmented, with one sentence per line, i.e. _not_ CoNLL09 format";
	}

	@Override
	protected int trySubParseArg(String[] args, int ai) {
		if (args[ai].equals("-out")) {
			ai++;
			output = new File(args[ai]);
			ai++;
		} else if (args[ai].equals("-test")) {
			ai++;
			input = new File(args[ai]);
			ai++;
		} else if (args[ai].equals("-nopi")) {
			ai++;
			skipPI = true;
		} else if (args[ai].equals("-noai")) {
			ai++;
			skipAI = true;
		} else if (args[ai].equals("-desegment")) { // Not printed out in the
													// help
													// (getSubUsageOptions()),
													// don't think it needs to.
													// This is only
													// experimental.
			ai++;
			desegment = true;
			skipPI = false; // This won't be regarded anyway. It's not
							// applicable when the initial segmentation is lost.
		} else if (args[ai].equals("-tokenize")) {
			ai++;
			super.loadPreprocessorWithTokenizer = true;
			skipPI = false; // Same as above
			desegment = false;
		} else if (args[ai].equals("-hybrid")) {
			hybrid = true;
			ai++;
		} else if (args[ai].equals("-external")) {
			external = true;
			ai++;
		}
		return ai;
	}

	@Override
	protected Class<?> getIntendedEntryClass() {
		return CompletePipeline.class;
	}

}
