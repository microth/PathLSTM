package se.lth.cs.srl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StandOffAnnotation {

	List<String> sentences;
	List<Integer> sentEndIndices;
	SentenceAnnotation[] annos;
	Map<String, Integer> id2sent;

	public SentenceAnnotation get(int i) {
		return annos[i];
	}

	public StandOffAnnotation(File standoffFile) {
		if (standoffFile.getPath().endsWith((".txt"))) {
			if (standoffFile.getPath().contains("_vk")
					|| standoffFile.getPath().contains("_cs"))
				sentences = readSentences(new File(standoffFile.getPath()
						.replaceAll("_.*", ".txt")));
			else
				sentences = readSentences(standoffFile);

			standoffFile = new File(standoffFile.getPath().replaceAll(".txt",
					".ann"));
			annos = new SentenceAnnotation[sentEndIndices.size()];
			for (int i = 0; i < annos.length; i++)
				annos[i] = new SentenceAnnotation(i == 0 ? 0
						: sentEndIndices.get(i - 1) + 1, sentEndIndices.get(i));

			id2sent = new HashMap<String, Integer>();
			readAnnotations(standoffFile);
		} else {
			Object[] objects = readSentencesAndFrames(standoffFile);
			sentences = (List<String>) objects[0];
			List<SentenceAnnotation> anno = (List<SentenceAnnotation>) objects[1];

			annos = new SentenceAnnotation[sentences.size()];
			for (int i = 0; i < annos.length; i++)
				annos[i] = anno.get(i);
		}
	}

	private void readAnnotations(File inputCorpus) {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(inputCorpus));
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] num_anno_word = line.split("\t");
				int currentsentence = -1;

				// word annotation
				if (num_anno_word.length == 3) {
					int startCharacter = Integer.parseInt(num_anno_word[1]
							.split(" ")[1]);
					int endCharacter = Integer.parseInt(num_anno_word[1]
							.split(" ")[2]);
					currentsentence = getSentence(endCharacter);

					int offset = 0;
					if (currentsentence > 0)
						offset = sentEndIndices.get(currentsentence - 1);
					String sanitycheck = sentences.get(currentsentence)
							.substring(startCharacter - offset,
									endCharacter - offset);
					if (!sanitycheck.toLowerCase().equals(
							num_anno_word[2].toLowerCase())) {
						System.err.println("WARNING: Spelling mismatch "
								+ sanitycheck + " vs. " + num_anno_word[2]);
						// System.err.println("Sanity check went wrong. Errorneous annotation file?");
						// System.exit(1);
					}
				}
				// word-word annotation
				else {
					String label = num_anno_word[1].split("Arg2:")[1];
					currentsentence = id2sent.get(label);
				}

				id2sent.put(num_anno_word[0], currentsentence);
				annos[currentsentence].addAnnotation(num_anno_word);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	private int getSentence(int charpos) {
		for (int i = 0; i < sentEndIndices.size(); i++)
			if (charpos < sentEndIndices.get(i))
				return i;
		return -1;
	}

	private Object[] readSentencesAndFrames(File inputCorpus) {
		List<String> retval = new LinkedList<String>();
		List<SentenceAnnotation> anno = new LinkedList<SentenceAnnotation>();

		// int offset = 0;
		BufferedReader br = null;
		String currentSen = "";
		SentenceAnnotation currentAnno = null;
		String currentFrame = "";
		int currentFrameId = 0;
		int currentFEId = 0;

		boolean fix_dointonight = inputCorpus.getName().equals(
				"LUCorpus-v0.3__SNO-525.xml");

		try {
			br = new BufferedReader(new FileReader(inputCorpus));
			String line = "";
			String layer = "";
			while ((line = br.readLine()) != null) {
				if (line.contains("<layer ")) {
					layer = line.replaceAll(".*name=\"", "").replaceAll("\".*",
							"");
				} else if (line.contains("</layer>")) {
					layer = "";
				} else if (line.contains("<text>")) {
					if (currentAnno != null) {
						anno.add(currentAnno);
					}
					currentSen = line.replaceAll(".*<text>", "").replaceAll(
							" ?</text>.*", "");
					if (fix_dointonight && currentSen.contains("doin'tonight"))
						currentSen = currentSen.replaceAll("doin'tonight",
								"doin tonight");
					retval.add(currentSen);
					// System.out.println(currentSen);
					currentAnno = new SentenceAnnotation(0, currentSen.length());
				} else if (line.matches(".*<annotationSet .*frameName=.*")) {
					currentFrameId++;
					currentFrame = line.replaceAll(".* frameName=\"", "")
							.replaceAll("\".*", "").replaceAll(" ", "_");
				} else if (line.matches(".*<label .*")
						&& line.matches(".* start=.*")
						&& line.matches(".* end=.*")) {

					int begin = Integer.parseInt(line.replaceAll(".* start=\"",
							"").replaceAll("\".*", ""));
					int end = Integer.parseInt(line.replaceAll(".* end=\"", "")
							.replaceAll("\".*", ""));

					if (layer.equals("Target")) {
						/**
						 * TODO: Sometimes, the target comprises two words...
						 * what to do with them?
						 **/
						String[] num_anno_word = new String[] {
								"F" + currentFrameId,
								(currentFrame + " " + begin + " " + end),
								currentSen.substring(begin, end) };
						currentAnno.addAnnotation(num_anno_word);
					}
					if (layer.equals("FE")) {
						currentFEId++;
						String FE = line.replaceAll(".*name=\"", "")
								.replaceAll("\".*", "").replaceAll(" ", "_");

						// add FE as a concept
						String[] num_anno_word = new String[] {
								"E" + currentFEId,
								(FE + " " + begin + " " + end),
								currentSen.substring(begin, end) };
						currentAnno.addAnnotation(num_anno_word);

						// add Relation between Frame and FE
						num_anno_word = new String[] {
								"Rx",
								currentFrame + ":" + FE + " " + ":F"
										+ currentFrameId + " " + ":E"
										+ currentFEId };
						currentAnno.addAnnotation(num_anno_word);

					}
				}
			}
			anno.add(currentAnno);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		return new Object[] { retval, anno };
	}

	private List<String> readSentences(File inputCorpus) {
		List<String> retval = new LinkedList<String>();
		sentEndIndices = new LinkedList<Integer>();
		int offset = 0;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputCorpus));
			String line = "";
			while ((line = br.readLine()) != null) {
				retval.add(line);
				offset++;
				sentEndIndices.add(offset + line.length());
				offset += line.length();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			try {
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		return retval;
	}
}
