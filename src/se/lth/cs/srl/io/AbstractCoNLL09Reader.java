package se.lth.cs.srl.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

//import se.lth.cs.srl.corpus.Corpus;
import se.lth.cs.srl.corpus.Sentence;

public abstract class AbstractCoNLL09Reader implements SentenceReader {

	protected static final Pattern NEWLINE_PATTERN = Pattern.compile("\n");

	protected BufferedReader in;
	protected Sentence nextSen;
	// protected Corpus c;
	private File file;

	public AbstractCoNLL09Reader(File file) {
		this.file = file;
		open();
	}

	private void restart() {
		try {
			in.close();
			open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void open() {
		System.err.println("Opening reader for " + file + "...");
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), Charset.forName("UTF-8")));
			// in = new BufferedReader(new FileReader(file));
			readNextSentence();
		} catch (IOException e) {
			System.out.println("Failed: " + e.toString());
			System.exit(1);
		}
	}

	protected abstract void readNextSentence() throws IOException;

	private Sentence getSentence() {
		Sentence ret = nextSen;
		try {
			readNextSentence();
		} catch (IOException e) {
			System.out.println("Failed to read from corpus file... exiting.");
			System.exit(1);
		}
		return ret;
	}

	@Override
	public List<Sentence> readAll() {
		ArrayList<Sentence> ret = new ArrayList<Sentence>();
		for (Sentence s : this)
			ret.add(s);
		ret.trimToSize();
		return ret;
	}

	@Override
	public Iterator<Sentence> iterator() {
		if (nextSen == null)
			restart();
		return new SentenceIterator();
	}

	@Override
	public void close() {
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class SentenceIterator implements Iterator<Sentence> {
		@Override
		public boolean hasNext() {
			return nextSen != null;
		}

		@Override
		public Sentence next() {
			return getSentence();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not implemented");
		}

	}

}