package se.lth.cs.srl.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public class ANNWriter implements SentenceWriter {

	private BufferedWriter out;
	int tnum;
	int rnum;
	Map<Word, String> word2id;

	public ANNWriter(File filename) {
		tnum = 1;
		rnum = 1;
		word2id = new HashMap<Word, String>();

		System.out.println("Writing corpus to " + filename + "...");
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), Charset.forName("UTF-8")));
		} catch (Exception e) {
			System.out.println("Failed while opening writer...\n"
					+ e.toString());
			System.exit(1);
		}
	}

	public void write(Sentence s) {
		try {
			for (Predicate p : s.getPredicates()) {
				if (p.getSense().equals("Action")
						|| p.getSense().equals("OPERATION")) {
					out.write(id(p) + "\t" + "Action" + " " + p.getBegin()
							+ " " + p.getEnd() + "\t" + p.getForm() + "\n");

					for (Word w : p.getArgMap().keySet()) {
						String label = p.getArgMap().get(w);
						if (label.equals("Theme"))
							label = "Object";

						if (!word2id.containsKey(w))
							out.write(id(w) + "\t" + label + " " + w.getBegin()
									+ " " + w.getEnd() + "\t" + w.getForm()
									+ "\n");

						out.write("R"
								+ (rnum++)
								+ "\t"
								+ (label.equals("Actor") ? ("IsActorOf Arg1:"
										+ id(w) + " Arg2:" + id(p))
										: (label.equals("Property") ? ("HasProperty Arg1:"
												+ id(p) + " Arg2:" + id(w))
												: ("ActsOn Arg1:" + id(p)
														+ " Arg2:" + id(w))))
								+ "\n");
					}
				}

				if (p.getSense().equals("Object")
						|| p.getSense().equals("CONCEPT")
						|| p.getSense().equals("Property")) {
					if (!word2id.containsKey(p))
						out.write(id(p) + "\t" + p.getSense() + " "
								+ p.getBegin() + " " + p.getEnd() + "\t"
								+ p.getForm() + "\n");

					for (Word w : p.getArgMap().keySet()) {
						String label = p.getArgMap().get(w);
						if (label.equals("Theme"))
							label = "Object";

						if (!word2id.containsKey(w))
							out.write(id(w) + "\t" + label + " " + w.getBegin()
									+ " " + w.getEnd() + "\t" + w.getForm()
									+ "\n");

						out.write("R" + (rnum++) + "\t" + "HasProperty Arg1:"
								+ id(p) + " Arg2:" + id(w) + "\n");
					}
				}
			}
			// out.write(s.toString()+"\n\n");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to write sentance.");
			System.exit(1);
		}
	}

	private String id(Word w) {
		if (!word2id.containsKey(w))
			word2id.put(w, ("T" + (tnum++)));
		return word2id.get(w);
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

	@Override
	public void specialwrite(Sentence s) {
		write(s);
	}

}
