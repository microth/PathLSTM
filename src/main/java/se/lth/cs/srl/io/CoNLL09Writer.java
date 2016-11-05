package se.lth.cs.srl.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import se.lth.cs.srl.corpus.Sentence;

public class CoNLL09Writer implements SentenceWriter {

	private BufferedWriter out;

	public CoNLL09Writer(File filename) {
		System.out.println("Writing corpus to " + filename + "...");
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), Charset.forName("UTF-8")));
			// out = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e) {
			System.out.println("Failed while opening writer...\n"
					+ e.toString());
			System.exit(1);
		}
	}

	public void write(Sentence s) {
		try {
			out.write(s.toString() + "\n\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to write sentance.");
			System.exit(1);
		}
	}

	@Override
	public void specialwrite(Sentence s) {
		try {
			out.write(s.toSpecialString() + "\n\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to write sentance.");
			System.exit(1);
		}
	}

	public void close() {
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to close writer.");
			System.exit(1);
		}
	}

}
