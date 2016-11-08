package se.lth.cs.srl.languages;

import java.io.File;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.options.FullPipelineOptions;
import se.lth.cs.srl.util.FileExistenceVerifier;

public class German extends Language {

	@Override
	public String getDefaultSense(Predicate pred) {
		return pred.getLemma() + ".1";
	}

	@Override
	public String getCoreArgumentLabelSequence(Predicate pred,
			Map<Word, String> proposition) {
		Sentence sen = pred.getMySentence();
		StringBuilder ret = new StringBuilder();
		for (int i = 1, size = sen.size(); i < size; ++i) {
			Word word = sen.get(i);
			if (pred == word) {
				ret.append(" " + pred.getSense() + "/");
				ret.append(isPassiveVoice(pred) ? "P" : "A");
			}
			if (proposition.containsKey(word)) {
				ret.append(" " + proposition.get(word));
			}
		}

		return ret.toString();
	}

	private boolean isPassiveVoice(Predicate pred) {
		Word head = pred.getHead();
		return !head.isBOS() && pred.getPOS().equals("VVPP")
				&& head.getLemma().equals("werden");
	}

	@Override
	public L getL() {
		return L.ger;
	}

	@Override
	public String getLexiconURL(Predicate pred) {
		return null;
	}

	@Override
	public String verifyLanguageSpecificModelFiles(FullPipelineOptions options) { // TODO
																					// this
																					// could
																					// be
																					// done
																					// nicer...
																					// I
																					// guess
																					// the
																					// proper
																					// way
																					// would
																					// be
																					// to
																					// create
																					// an
																					// enum
																					// of
																					// all
																					// the
																					// modules
																					// in
																					// the
																					// complete
																					// pipeline,
																					// and
																					// let
																					// each
																					// language
																					// enumerate
																					// which
																					// modules
																					// it
																					// requires.
																					// Then
																					// the
																					// language
																					// class
																					// can
																					// handle
																					// the
																					// verification,
																					// and
																					// not
																					// every
																					// subclass.
		File[] files;
		// files=new File[2];
		// files[0]=options.lemmatizer;
		// files[1]=options.morph;
		files = new File[0];
		return FileExistenceVerifier.verifyFiles(files);

	}

	@Override
	public String toLangNameString() {
		return "German";
	}

}
