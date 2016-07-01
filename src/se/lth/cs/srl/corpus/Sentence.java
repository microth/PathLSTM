package se.lth.cs.srl.corpus;

import is2.data.SentenceData09;
import is2.io.CONLLReader09;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import se.lth.cs.srl.CompletePipeline;
import se.lth.cs.srl.options.CompletePipelineCMDLineOptions;
import se.lth.cs.srl.options.FullPipelineOptions;

public class Sentence extends ArrayList<Word> {

	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

	private static final long serialVersionUID = 10;

	protected List<Predicate> predicates;

	private Sentence() {
		Word BOS = new Word(this);
		super.add(BOS); // Add the root token
		predicates = new ArrayList<Predicate>();
	}

	// generates copy of s -- but without SRL annotation
	public Sentence(Sentence s) {
		this();
		for (int i = 1; i < s.size(); i++) {
			Word w = s.get(i);
			super.add(new Word(w, this));
			super.get(i).setCorefId(w.getCorefId());
		}

		for (Predicate p : s.predicates) {
			this.makePredicate(p.idx);
			Predicate new_p = (Predicate) super.get(p.idx);
			new_p.setSense(p.getSense());
		}

		for (Predicate p : s.predicates) {
			for (Word w : p.getArgMap().keySet()) {
				((Predicate) super.get(p.idx)).addArgMap(super.get(w.idx), p
						.getArgMap().get(w));
			}
		}

		for (int i = 1; i < s.size(); i++) {
			Word w = s.get(i); // skip ROOT
			Word curWord = super.get(w.idx);
			curWord.setHead(super.get(w.headID));
			curWord.setDeprel(w.getDeprel());
		}

		this.buildDependencyTree();
	}

	public Sentence(SentenceData09 data, boolean skipTree) {
		this();
		int offset = 0;
		if (data.pheads[0] == -1)
			offset++;
		for (int i = 0 + offset; i < data.forms.length; ++i) {
			Word nextWord = new Word(data.forms[i], data.plemmas[i],
					data.ppos[i], data.pfeats[i], this, i + 1 - offset);
			super.add(nextWord);
		}
		if (skipTree)
			return;
		for (int i = 0; i < data.forms.length - offset; ++i) {
			Word curWord = super.get(i + 1);
			curWord.setHead(super.get(data.pheads[i + offset]));
			curWord.setDeprel(data.plabels[i + offset]);
		}
		this.buildDependencyTree();
	}

	public Sentence(String[] words, String[] lemmas, String[] tags,
			String[] morphs) {
		this();
		for (int i = 1; i < words.length; ++i) { // Skip root-tokens.
			Word nextWord = new Word(words[i], lemmas[i], tags[i], morphs[i],
					this, i);
			super.add(nextWord);
		}
	}

	private void addPredicate(Predicate pred) {
		predicates.add(pred);
	}

	public void removePredicate(Predicate pred) {
		super.set(pred.idx, new Word(pred));
		predicates.remove(pred);
	}

	public List<Predicate> getPredicates() {
		return predicates;
	}

	public void buildDependencyTree() {
		for (int i = 1; i < size(); ++i) {
			Word curWord = get(i);
			curWord.setHead(get(curWord.getHeadId()));
		}
	}

	public void buildSemanticTree() {
		int offset = 0;
		if (get(1).args.length > predicates.size())
			offset++;

		for (int i = 0; i < predicates.size(); ++i) {
			Predicate pred = predicates.get(i);
			for (int j = 1; j < super.size(); ++j) {
				Word curWord = get(j);
				String arg = curWord.getArg(i + offset);
				if (!arg.equals("_"))
					pred.addArgMap(curWord, arg);
			}
		}
		// for(Word w:this) //Free this memory as we no longer need this string
		// array
		// w.clearArgArray();
	}

	public String toString() {
		String tag;
		StringBuilder ret = new StringBuilder();
		for (int i = 1; i < super.size(); ++i) {
			Word w = super.get(i);
			ret.append(i).append("\t").append(w.toString());
			if (!(w instanceof Predicate)) // If its not a predicate add the
											// FILLPRED and PRED cols
				ret.append("\t_\t_");
			for (int j = 0; j < predicates.size(); ++j) {
				ret.append("\t");
				Predicate pred = predicates.get(j);
				ret.append((tag = pred.getArgumentTag(w)) != null ? tag : "_");
			}
			ret.append("\n");
		}
		return ret.toString().trim();
	}

	public String toSpecialString() {
		String tag;
		StringBuilder ret = new StringBuilder();
		for (int i = 1; i < super.size(); ++i) {
			Word w = super.get(i);
			ret.append(i).append("\t").append(w.toString());
			if (!(w instanceof Predicate)) // If its not a predicate add the
											// FILLPRED and PRED cols
				ret.append("\t_\t_");

			ret.append("\t");
			ret.append(w.corefid < 0 ? "_" : w.corefid);

			for (int j = 0; j < predicates.size(); ++j) {
				ret.append("\t");
				Predicate pred = predicates.get(j);
				ret.append((tag = pred.getArgumentTag(w)) != null ? tag : "_");
			}
			ret.append("\n");
		}
		return ret.toString().trim();
	}

	public void makePredicate(int wordIndex) {
		Predicate p = new Predicate(super.get(wordIndex), this);
		super.set(wordIndex, p);
		addPredicate(p);
	}

	/*
	 * Functions used when interfacing with Bohnets parser These need to be
	 * fixed. Or rather the Sentence object should go altogether.
	 */
	public String[] getFormArray() {
		String[] ret = new String[this.size()];
		// ret[0]="<root>";
		for (int i = 0; i < this.size(); ++i)
			ret[i] = this.get(i).Form;
		return ret;
	}

	public String[] getPOSArray() {
		String[] ret = new String[this.size()];
		// ret[0]="<root-POS>";
		for (int i = 0; i < this.size(); ++i)
			ret[i] = this.get(i).POS;
		return ret;
	}

	public String[] getFeats() {
		String[] ret = new String[this.size()];
		ret[0] = CONLLReader09.NO_TYPE;
		for (int i = 1; i < this.size(); ++i)
			ret[i] = this.get(i).getFeats();
		return ret;
	}

	public void setHeadsAndDeprels(int[] heads, String[] deprels) {
		for (int i = 0; i < heads.length; ++i) {
			Word w = this.get(i + 1);
			w.setHead(this.get(heads[i]));
			w.setDeprel(deprels[i]);
		}
	}

	public static Sentence newDepsOnlySentence(String[] lines) {
		Sentence ret = new Sentence();
		Word nextWord;
		int ix = 1;
		for (String line : lines) {
			String[] cols = WHITESPACE_PATTERN.split(line, 13);
			nextWord = new Word(cols, ret, ix++);
			ret.add(nextWord);
		}
		ret.buildDependencyTree();
		return ret;

	}

	public static Sentence newSentence(String[] lines) {
		Sentence ret = new Sentence();
		Word nextWord;
		int ix = 1;
		for (String line : lines) {
			String[] cols = WHITESPACE_PATTERN.split(line);
			if (cols[0].contains("_")) {
				cols = Arrays.copyOfRange(cols, 1, cols.length);
			}

			if (cols.length < 13) {
				System.err.println(line);
			}

			if (cols[12].equals("Y") || cols[13].equals("Y")) {
				Predicate pred = new Predicate(cols, ret, ix++);
				ret.addPredicate(pred);
				pred.setSense(cols[12].equals("Y") ? cols[13] : cols[14]);
				nextWord = pred;
			} else {
				nextWord = new Word(cols, ret, ix++);
			}

			if (cols.length > 14 && cols[14].matches("^[0-9].*"))
				nextWord.setCorefId(Integer.parseInt(cols[14]));
			if (cols.length > 15 && cols[15].matches("^[0-9].*"))
				nextWord.setCorefId(Integer.parseInt(cols[15]));

			ret.add(nextWord);
		}
		ret.buildDependencyTree();
		ret.buildSemanticTree();
		return ret;
	}

	public static Sentence newSRLOnlySentence(String[] lines) {
		Sentence ret = new Sentence();
		Word nextWord;
		int ix = 1;
		for (String line : lines) {
			String[] cols;
			String corefid = null;
			int offset = 0;
			if (!line.matches("^[0-9].*")) {
				cols = WHITESPACE_PATTERN.split(line);// ,17);
				if (!cols[13].equals("_") && !cols[13].equals("Y"))
					offset++;

				corefid = cols[15 + offset].equals("_") ? null
						: cols[15 + offset];
				line = line.replaceFirst("[^\t]\t", "");
			}

			cols = WHITESPACE_PATTERN.split(line, 14);

			if (offset == 0 && !cols[12].equals("_") && !cols[12].equals("Y"))
				offset++;

			if (cols[12 + offset].charAt(0) == 'Y') {
				Predicate pred = new Predicate(cols, ret, ix++);
				ret.addPredicate(pred);
				if (WHITESPACE_PATTERN.split(line).length > (14 + offset))
					pred.setSense(WHITESPACE_PATTERN.split(line)[13 + offset]);
				nextWord = pred;
			} else {
				nextWord = new Word(cols, ret, ix++);
			}

			if (corefid != null)
				nextWord.setCorefId(Integer.parseInt(corefid));

			ret.add(nextWord);
		}
		ix = 1;
		for (String line : lines) {
			String[] cols = WHITESPACE_PATTERN.split(line);
			for (int i = 14; i < cols.length; i++) {
				if (cols[i].charAt(0) == 'Y') {
					// ret.getPredicates().get(i-14).addArgMap(ret.get(ix),
					// "ARG");
				}
			}
			ix++;
		}

		ret.buildDependencyTree();
		return ret;
	}

	public final Comparator<Word> wordComparator = new Comparator<Word>() {
		@Override
		public int compare(Word arg0, Word arg1) {
			return arg0.idx - arg1.idx;
		}
	};
}
