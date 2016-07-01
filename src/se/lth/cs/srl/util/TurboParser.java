package se.lth.cs.srl.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public class TurboParser {

	BufferedReader br;

	public TurboParser(String file) {
		try {
			br = new BufferedReader(new FileReader(file));

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void overwriteParse(Sentence s) {
		try {
			// skip ROOT (i==0);
			for (int i = 1; i < s.size(); i++) {
				Word w = s.get(i);
				String line = br.readLine();
				// if current line is blank (end of last sentence), read next
				// line
				if (line.equals(""))
					line = br.readLine();

				String[] parts = line.split("\t");
				// sanity check
				if (!parts[1].toLowerCase().equals(w.getForm().toLowerCase())) {
					System.err
							.println("WARNING: different normalization applied? ("
									+ parts[1] + " vs. " + w.getForm() + ")");
					w.setLemma(w.getForm().replaceAll("[0-9]", "D"));
				}

				// CoNLL-X
				/**/w.setPOS(parts[3]);
				w.setHeadId(Integer.parseInt(parts[6]));
				w.setDeprel(parts[7]);/**/

				// CoNLL-09
				/**
				 * w.setPOS(parts[4]); w.setHeadId(Integer.parseInt(parts[8]));
				 * w.setDeprel(parts[10]);/
				 **/

			}
			s.buildDependencyTree();

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
