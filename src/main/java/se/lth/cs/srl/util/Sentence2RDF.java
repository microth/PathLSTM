package se.lth.cs.srl.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeSet;

import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

/**
 *
 * @author pierre
 */
public class Sentence2RDF {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public Sentence sentence;
	public int inx;

	//
	// public static void main(String[] args) throws IOException {
	// // To load all the triples in Sesame, set the Tomcat memory to 2g:
	// //
	// /* A query
	// PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	// PREFIX gstruct:<http://cs.lth.se/ontologies/gstruct.owl#>
	//
	// SELECT ?sent ?s ?n ?id ?f {
	// ?sent rdf:type gstruct:Sentence.
	// ?sent gstruct:predicates ?s.
	// ?s gstruct:predsense "play.02".
	// ?s gstruct:args ?n.
	// ?n gstruct:argtype "AM-TMP".
	// ?n gstruct:id ?id.
	// ?sent gstruct:words ?n2.
	// ?n2 gstruct:id ?id.
	// ?n2 gstruct:form ?f
	// }
	// */
	// String file =
	// "/Users/pierre/Projets/robotcad/Rosetta/Rosetta/semparser/corpora/train.50p.txt";
	// //String file =
	// "/Users/pierre/Projets/robotcad/Rosetta/Rosetta/semparser/corpora/one_sentence.txt";
	// List<Sentence> sentences = CorpusReader.getCorpusCoNLL2009(new
	// File(file));
	// Sentence2RDF sentence2rdf = new Sentence2RDF();
	// String prefix;
	// String grammar;
	// String semantics;
	// prefix = sentence2rdf.makePrefix();
	// System.out.println(prefix);
	//
	// for (int i = 0; i < sentences.size(); i++) {
	// sentence2rdf = new Sentence2RDF(sentences.get(i), i);
	// grammar = sentence2rdf.makeGramTriples();
	// semantics = sentence2rdf.makeSemTriples();
	// if (sentence2rdf.sentence.getPredicates().isEmpty()) {
	// System.out.print(grammar);
	// } else {
	// System.out.print(grammar + ";\n" + semantics);
	// }
	// System.out.println(".");
	// }
	// }

	// public Sentence2RDF() {
	// }

	// Parameters: The sentence and its index in the document
	public Sentence2RDF(Sentence sentence, int inx) {
		this.sentence = sentence;
		this.inx = inx;
	}

	// public void printSentence() {
	// System.out.println(sentence);
	// }

	// public String makeGramTriples() {
	// String grammar = "gstruct:sentence_" + inx + "\t";
	// grammar += makeGramTriplesWithBlankNode();
	// return grammar;
	// }

	// public String makeGramTriplesWithBlankNode() {
	// String grammar = "rdf:type\t" + "gstruct:Sentence ;\n";
	// grammar += "\tgstruct:inx\t" + inx + " ;\n";
	// grammar += "\tgstruct:words\n";
	// for (int i = 1; i < sentence.size(); i++) {
	// grammar += "\t\t[";
	// grammar += "gstruct:id " + i + " ; ";
	// grammar += "gstruct:form \"" + sentence.get(i).getForm() + "\"" + " ; ";
	// grammar += "gstruct:lemma \"" + sentence.get(i).getLemma() + "\"" +
	// " ; ";
	// grammar += "gstruct:pos \"" + sentence.get(i).getPOS() + "\"" + " ; ";
	// grammar += "gstruct:head \"" + sentence.get(i).getHeadId() + "\"" +
	// " ; ";
	// grammar += "gstruct:deprel \"" + sentence.get(i).getDeprel() + "\"";
	// grammar += "]";
	// if (i != (sentence.size() - 1)) {
	// grammar += ",\n";
	// }
	// }
	// return grammar;
	// }

	// public String makeSemTriples() {
	// String semantics = "\tgstruct:predicates\n";
	// for (int i = 0; i < sentence.getPredicates().size(); i++) {
	// semantics += "\t\t[";
	// semantics += "gstruct:id " + sentence.getPredicates().get(i).getInx() +
	// " ; ";
	// semantics += "gstruct:predsense \"" +
	// sentence.getPredicates().get(i).getSense() + "\"" + " ; ";
	// semantics += "gstruct:args\n";
	// semantics += makeArgString(sentence.getPredicates().get(i).getArgMap());
	// semantics += "]";
	// if (i != (sentence.getPredicates().size() - 1)) {
	// semantics += ",\n";
	// }
	// }
	// return semantics;
	// }

	public String makeArgString(Map<Word, String> argMap) {
		if (argMap.isEmpty()) {
			return "[]";
		}
		TreeSet<Word> ts = new TreeSet<Word>(Word.WORD_LINEAR_ORDER_COMPARATOR);
		ts.addAll(argMap.keySet());
		String tmp = "";
		for (Word word : ts) {
			tmp += "\t\t\t[gstruct:id " + word.getIdx() + " ; ";
			tmp += " gstruct:argtype \"" + argMap.get(word) + "\"],\n";
		}
		tmp = tmp.replaceAll(",\n$", "");
		return tmp;
	}

	public void printRDF(PrintStream out) {
		out.println("gstruct:sentence_" + inx + "\trdf:type\t"
				+ "gstruct:Sentence ;");
		out.println("\tgstruct:inx\t" + inx + " ;");
		out.println("\tgstruct:words");
		String tmp;
		for (int i = 1; i < sentence.size(); i++) {
			String lemma = sentence.get(i).getLemma();
			String pos = sentence.get(i).getPOS();
			tmp = "\t\t[";
			tmp += "gstruct:id " + i + " ; ";
			tmp += "gstruct:form \"" + sentence.get(i).getForm() + "\"" + " ; ";
			if (lemma != null && !lemma.equals("_"))
				tmp += "gstruct:lemma \"" + lemma + "\"" + " ; ";
			tmp += "gstruct:pos \"" + pos + "\"" + " ; ";
			if (pos != null && !pos.equals("_"))
				tmp += "gstruct:head \"" + sentence.get(i).getHeadId() + "\""
						+ " ; ";
			tmp += "gstruct:deprel \"" + sentence.get(i).getDeprel() + "\"";
			tmp += "]";
			if (i != (sentence.size() - 1)) {
				tmp += ",\n";
			}
			out.print(tmp);
		}
		if (sentence.getPredicates().isEmpty()) {
			out.println(".");
			return;
		} else {
			out.println(";");
		}
		out.println("\tgstruct:predicates");
		for (int i = 0; i < sentence.getPredicates().size(); i++) {
			tmp = "\t\t[";
			tmp += "gstruct:id " + sentence.getPredicates().get(i).getIdx()
					+ " ; ";
			tmp += "gstruct:predsense \""
					+ sentence.getPredicates().get(i).getSense() + "\"" + " ; ";
			tmp += "gstruct:args\n";
			tmp += makeArgString(sentence.getPredicates().get(i).getArgMap());
			tmp += "]";
			if (i != (sentence.getPredicates().size() - 1)) {
				tmp += ",\n";
			}
			out.print(tmp);
		}
		out.println(".");
	}

	// public String makePrefix() {
	// // gstruct means grammatical structure
	// String prefix =
	// "@prefix gstruct: <http://cs.lth.se/ontologies/gstruct.owl#> .\n";
	// prefix +=
	// "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n";
	// return prefix;
	// }

	public void printPrefix(PrintStream out) {
		// gstruct means grammatical structure
		out.println("@prefix gstruct: <http://cs.lth.se/ontologies/gstruct.owl#> .");
		out.println("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");

	}

	public static String sentence2RDF(Sentence s)
			throws UnsupportedEncodingException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream p = new PrintStream(baos, true, "UTF8");
		Sentence2RDF s2r = new Sentence2RDF(s, 1);
		s2r.printPrefix(p);
		p.println();
		s2r.printRDF(p);
		p.close();
		return baos.toString("UTF8");
	}
}