package se.lth.cs.srl.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.util.DasFilter;

public class FrameNetXMLWriter implements SentenceWriter {

	private BufferedWriter out;
	private int count = 0;

	final boolean reconstructSpan = true;

	public FrameNetXMLWriter(File filename) {
		System.out.println("Writing corpus to " + filename + "...");
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), Charset.forName("UTF-8")));
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
			out.write("<corpus ID=\"100\" XMLCreated=\"" + new Date()
					+ "\" name=\"ONE\">\n");
			out.write("  <documents>\n");
			out.write("    <document ID=\"1\" description=\"TWO\">\n");
			out.write("      <paragraphs>\n");
			out.write("        <paragraph ID=\"2\" documentOrder=\"1\">\n");
			out.write("          <sentences>\n");

		} catch (IOException e) {
			System.out.println("Failed while opening writer...\n"
					+ e.toString());
			System.exit(1);
		}
	}

	public void write(Sentence s) {
		StringBuffer text = new StringBuffer();
		/** hard-coded fix for test set starting with "Simply" **/
		if (s.get(1).getForm().equals("Simply")) {
			//System.err.println("XXX");
			text.append("  ");
		}
		for (int i = 1; i < s.size(); i++) {
			if (i > 1)
				text.append(' ');

			s.get(i).setBegin(text.length());
			text.append(s.get(i).getForm());
			s.get(i).setEnd(text.length());
		}

		int annID = count * 100;

		try {
			out.write("          <sentence ID=\"" + count + "\">\n");
			out.write("            <text>" + text + "</text>\n");
			out.write("            <annotationSets>\n");

			for (int i = 1; i < s.size(); i++) {
				if (s.get(i) instanceof Predicate) {
					Predicate p = (Predicate) s.get(i);
					int layerID = annID * 100 + 1;
					int labelID = layerID * 100 + 1;
					out.write("              <annotationSet ID=\"" + annID
							+ "\" frameName=\"" + p.getSense() + "\">\n");
					out.write("                <layers>\n");
					out.write("                  <layer ID=\"" + layerID
							+ "\" name=\"Target\">\n");
					out.write("                    <labels>\n");
					out.write("                      <label ID=\"" + labelID
							+ "\" end=\"" + (p.getEnd() - 1)
							+ "\" name=\"Target\" start=\"" + p.getBegin()
							+ "\"/>\n");
					out.write("                    </labels>\n");
					out.write("                  </layer>\n");

					layerID++;
					labelID = layerID * 100 + 1;

					out.write("                  <layer ID=\"" + layerID
							+ "\" name=\"FE\">\n");
					out.write("                    <labels>\n");

					if (!reconstructSpan)
						for (Word a : p.getArgMap().keySet()) {
							out.write("                      <label ID=\""
									+ labelID + "\" end=\"" + (a.getEnd() - 1)
									+ "\" name=\"" + p.getArgMap().get(a)
									+ "\" start=\"" + a.getBegin() + "\"/>\n");
							labelID++;
						}
					else {
						Map<String, List<Word[]>> label2spans = new HashMap<String, List<Word[]>>();
						for (Word a : p.getArgMap().keySet()) {
							Word[] begin_end = DasFilter.pass(p, a);
							if (begin_end == null)
								continue;

							String label = p.getArgMap().get(a);
							if (!label2spans.containsKey(label))
								label2spans
										.put(label, new LinkedList<Word[]>());
							label2spans.get(label).add(begin_end);

						}

						Map<String, List<Word[]>> newlabel2spans = new HashMap<String, List<Word[]>>();
						for (String label : label2spans.keySet()) {
							List<Word[]> begin_ends = DasFilter
									.merge(label2spans.get(label));
							newlabel2spans.put(label, begin_ends);
						}

						DasFilter.resolveConflictingSpans(newlabel2spans);
						for (String label : newlabel2spans.keySet()) {
							List<Word[]> begin_ends = newlabel2spans.get(label);
							for (Word[] begin_end : begin_ends) {
								out.write("                      <label ID=\""
										+ labelID + "\" end=\""
										+ (begin_end[1].getEnd() - 1)
										+ "\" name=\"" + label + "\" start=\""
										+ (begin_end[0].getBegin()) + "\"/>\n");
								labelID++;
							}
						}

					}
					out.write("                    </labels>\n");
					out.write("                  </layer>\n");
					out.write("                </layers>\n");
					out.write("              </annotationSet>\n");
				}
			}

			out.write("            </annotationSets>\n");
			out.write("          </sentence>\n");
			count++;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to write sentance.");
			System.exit(1);
		}
	}

	@Override
	public void specialwrite(Sentence s) {
		this.write(s);
	}

	public void close() {
		try {
			out.write("	          </sentences>\n");
			out.write("	       </paragraph>\n");
			out.write("	     </paragraphs>\n");
			out.write("	   </document>\n");
			out.write("  </documents>\n");
			out.write("</corpus>\n");

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to close writer.");
			System.exit(1);
		}
	}

}
