package se.lth.cs.srl.io;

import java.util.List;

import se.lth.cs.srl.corpus.Sentence;

public interface SentenceReader extends Iterable<Sentence> {

	List<Sentence> readAll();

	public void close();

}
