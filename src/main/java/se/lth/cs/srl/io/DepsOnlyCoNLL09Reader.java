package se.lth.cs.srl.io;

import java.io.File;
import java.io.IOException;

import se.lth.cs.srl.corpus.Sentence;

public class DepsOnlyCoNLL09Reader extends AbstractCoNLL09Reader {

	public DepsOnlyCoNLL09Reader(File file) {
		super(file);
	}

	@Override
	protected void readNextSentence() throws IOException {
		String str;
		Sentence sen = null;
		StringBuilder senBuffer = new StringBuilder();
		while ((str = in.readLine()) != null) {
			if (!str.trim().equals("")) {
				senBuffer.append(str).append("\n");
			} else {
				sen = Sentence.newDepsOnlySentence(NEWLINE_PATTERN
						.split(senBuffer.toString()));
				// System.err.println("Processing sentence ...");
				// System.err.println(sen);
				break;
			}
		}
		if (sen == null) {
			nextSen = null;
			in.close();
		} else {
			nextSen = sen;
		}
	}

}
