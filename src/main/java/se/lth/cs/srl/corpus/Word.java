package se.lth.cs.srl.corpus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import se.lth.cs.srl.languages.Language;

public class Word implements Serializable {
	private static final long serialVersionUID = 12;

	public static enum WordData {
		Form, Lemma, POS, Deprel, Pred, OntPred, HMM, ClosedClassForm/*
																	 * ,
																	 * UniversalPOS
																	 */, Voice
	};

	final Sentence mySentence;
	String[] args;

	String Form;
	String Lemma = "_";
	String POS = "_";
	String universalPOS = "_";
	String Deprel = "_";
	String Feats = "_";
	int corefid = -1;
	Double[] rep;
	int headID;
	Word head;
	int begin, end = -1;
	Set<Word> children;
	Set<Word> span; // children + recursive grandchildren
	boolean isBOS;

	Word potentialArgument; // This is basically an attribute of Predicate
							// rather than Word,
							// but this way things work smoother with the
							// features.

	final int idx;

	public void freeMemory() {
		// POS = null;
		// Deprel = null;
		// mySentence=null;
		universalPOS = null;
		Feats = null;
		span = null;
	}

	// BOS constructor.
	public Word(Sentence s) {
		idx = 0;
		isBOS = true;
		children = new HashSet<Word>();
		this.Form = "<ROOT-FORM>";
		this.Lemma = "<ROOT-LEMMA>";
		this.POS = "<ROOT-POS>";
		this.Feats = "<ROOT-FEATS>";
		this.Deprel = "<ROOT-DEPREL>";
		this.headID = -1;

		this.Form = "";
		this.Lemma = "";
		this.POS = "";
		this.Feats = "";
		this.Deprel = "";
		this.mySentence = s;
		this.rep = new Double[] { 0.0 };
	}

	public Word(String form, String lemma, String POS, String feats,
			Sentence mySentence, int idx) {
		this.idx = idx;
		this.Form = form;
		this.Lemma = lemma == null ? "_" : lemma;
		this.POS = POS == null ? "_" : POS;
		// this.universalPOS=universal(this.POS);
		this.Feats = feats == null ? "_" : feats;
		this.mySentence = mySentence;
		this.rep = new Double[] { 0.0 };

		// children=new HashSet<Word>();
		children = new HashSet<Word>();
		
	}

	/**
	 * Map POS to universal POS tagset ** private String universal(String s) {
	 * if(s.equals("DT") || s.equals("EX") || s.equals("PDT") ||
	 * s.equals("WDT")) return "DET"; else if(s.equals("FW") || s.equals("LS")
	 * || s.equals("RN") || s.equals("SYM") || s.equals("UH") || s.equals("WH"))
	 * return "X"; else if(s.startsWith("J")) return "ADJ"; else
	 * if(s.equals("MD") || s.startsWith("V")) return "VERB"; else
	 * if(s.startsWith("N")) return "NOUN"; else if(s.equals("POS") ||
	 * s.equals("RP") || s.equals("PRT") || s.equals("TO")) return "PRT"; else
	 * if(s.startsWith("P") || s.startsWith("WP")) return "PRON"; else
	 * if(s.startsWith("RB") || s.equals("WRB")) return "ABV"; else return ".";
	 * }
	 **/

	public Word(Word w) {
		this(w, null);
	}

	/**
	 * Used to replace an old word with a new (updates dependencies). Used to
	 * make a predicate from a word during predicate identification.
	 * 
	 * @param w
	 *            The Word
	 */
	public Word(Word w, Sentence s) {
		this.idx = w.idx;
		this.Form = w.Form;
		this.Lemma = w.Lemma;
		this.POS = w.POS;
		// this.universalPOS=w.universalPOS;
		this.Feats = w.Feats;
		this.Deprel = w.Deprel;
		this.head = w.head;
		this.headID = w.headID;
		this.children = w.children;
		this.corefid = w.corefid;

		if (s == null) {
			this.mySentence = w.mySentence;
		} else {
			this.mySentence = s;
			if (s.size() > this.idx) {
				s.remove(this.idx);
				s.add(this.idx, this);
			}
		}
		this.isBOS = w.isBOS;
		this.rep = new Double[w.rep.length];
		for (int i = 0; i < w.rep.length; i++)
			this.rep[i] = w.rep[i];
		this.begin = w.begin;
		this.end = w.end;
		// if(s==null) {
		// Then we have to update our children to make them point to this head
		// rather than the old
		if (head != null) {
			for (Word child : children)
				child.head = this;
			// And update our head's children to forget the old and add this one
			head.children.remove(w);
			head.children.add(this);
		}
		/*
		 * } else { if(Learn.learnOptions!=null &&
		 * Learn.learnOptions.deterministicPipeline){ children=new
		 * TreeSet<Word>(mySentence.wordComparator); } else { children=new
		 * HashSet<Word>(); } }
		 */
	}

	public Word(String[] CoNLL2009Columns, Sentence s, int idx) {
		this(CoNLL2009Columns[1], CoNLL2009Columns[3], CoNLL2009Columns[5],
				CoNLL2009Columns[7], s, idx);
		// this.Form=CoNLL2009Columns[1];
		// this.Lemma=CoNLL2009Columns[3];
		// this.POS=CoNLL2009Columns[5];
		// this.Feats=CoNLL2009Columns[7];
		// this.mySentence=s;
		// children=new TreeSet<Word>(s.wordComparator);
		this.headID = Integer.parseInt(CoNLL2009Columns[9]);
		this.Deprel = CoNLL2009Columns[11];

		int offset = 0;
		if (CoNLL2009Columns.length > 12
				&& !CoNLL2009Columns[12].startsWith("_")
				&& !CoNLL2009Columns[12].startsWith("Y")) {
			offset++;
			String[] dims = CoNLL2009Columns[12].split(",");
			this.rep = new Double[dims.length];
			// if(dims.length==1)
			// this.rep[0] = 0.0;
			// else
			for (int i = 0; i < dims.length; i++)
				this.rep[i] = Double.parseDouble(dims[i]);

			// System.err.println(Double.parseDouble(dims[i]));
		}
		if (CoNLL2009Columns.length >= 14 + offset) {
			args = new String[CoNLL2009Columns.length - (14 + offset)];
			for (int i = 0; i < args.length; ++i) {
				args[i] = CoNLL2009Columns[14 + i + offset];
			}
		}
	}

	/*
	 * Getters
	 */
	public String getAttr(WordData attr) {
		switch (attr) {
		case Form:
			return Form;
		case Lemma:
			return Lemma;
		case POS:
			return POS;
		case Deprel:
			return Deprel;
		case ClosedClassForm:
			if (POS.startsWith("N") || POS.startsWith("V")
					|| POS.startsWith("J") || POS.startsWith("R")
					|| POS.equals("CD")) {
				return null;
			} else {
				return Form;
			}
		case HMM:
			return new Integer(rep[0].intValue()).toString();
		case Voice:
			return ((se.lth.cs.srl.languages.English) Language.getLanguage())
					.isPassiveVoice(this) ? "Y" : "N";
			// case UniversalPOS: return universalPOS;
		default:
			throw new Error("You are wrong here."); // We shouldn't enter here
		}
	}

	public Double getRep(int dim) {
		// return 0.0;
		if (dim < this.rep.length)
			return this.rep[dim];
		else
			return 0.0;
	}

	public void setRep(Double[] d) {
		this.rep = d;
	}

	public String getForm() {
		return Form;
	}

	public String getLemma() {
		return Lemma;
	}

	public void setLemma(String Lemma) {
		this.Lemma = Lemma;
	}

	public String getPOS() {
		return POS;
	}

	public void setPOS(String POS) {
		this.POS = POS;
	}

	public void setFeats(String Feats) {
		this.Feats = Feats;
	}

	public String getFeats() {
		return Feats;
	}

	public Word getHead() {
		return head;
	}

	public int getHeadId() {
		return headID;
	}

	public void setHeadId(int headID) {
		this.headID = headID;
	}

	public String getDeprel() {
		return Deprel;
	}

	public Word getPotentialArgument() {
		return potentialArgument;
	}

	public Set<Word> getChildren() {
		return children;
	}

	public Sentence getMySentence() {
		return mySentence;
	}

	public String getArg(int i) {
		try {
			return args[i];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err
					.println("Corpus contains errors, missing semantic arguments, Word: "
							+ this);
			return "_";
		}
	}

	/*
	 * Setters
	 */
	public void setHead(Word h) {
		head = h;
		headID = mySentence.indexOf(h);
		h.children.add(this);
	}

	public void setDeprel(String deprel) {
		this.Deprel = deprel;
	}

	public void setPotentialArgument(Word potentialArgument) {
		this.potentialArgument = potentialArgument;
	}

	protected void setChildren(HashSet<Word> children) {
		this.children = children;
	}

	void clearArgArray() {
		args = null;
	}

	public boolean isBOS() {
		return isBOS;
	}

	public boolean isPassiveVoiceEng() {
		if (!getPOS().equals("VBN"))
			return false;
		if (!head.isBOS && head.Form.matches("(be|am|are|is|was|were|been)"))
			return true;

		return false;
	}

	/*
	 * Getters for siblings and dependents
	 */
	public Word getLeftSibling() {
		for (int i = (mySentence.indexOf(this) - 1); i > 0; --i) {
			if (head.children.contains(mySentence.get(i)))
				return mySentence.get(i);
		}
		return null;
	}

	public Word getRightSibling() {
		for (int i = (mySentence.indexOf(this) + 1); i < mySentence.size(); ++i) {
			if (head.children.contains(mySentence.get(i)))
				return mySentence.get(i);
		}
		return null;
	}

	public Word getRightmostDep() {
		if (children.isEmpty())
			return null;
		Word ret = null;
		/**
		 * TODO: for some reason, not every word/predicate is part of
		 * mySentence?!
		 **/
		// System.err.println(idx + "\t" + mySentence.indexOf(this));
		for (int i = idx; i < mySentence.size(); ++i) {
			if (children.contains(mySentence.get(i)))
				ret = mySentence.get(i);
		}
		return ret;
	}

	public Word getLeftmostDep() {
		if (children.isEmpty())
			return null;
		Word ret = null;
		for (int i = mySentence.indexOf(this); i > 0; --i) {
			if (children.contains(mySentence.get(i)))
				ret = mySentence.get(i);
		}
		return ret;
	}

	public Word getSubj() {
		if (children.isEmpty())
			return null;
		Word ret = null;
		for (Word w : children) {
			if (w.getDeprel().equals("NSUBJ") || w.getDeprel().equals("SUBJ")
					|| w.getDeprel().equals("SBJ")
					|| w.getDeprel().equals("SUB"))
				ret = w;
		}
		return ret;
	}

	public static List<Word> findPath(Word pred, Word arg) {

		List<Word> predPath = pathToRoot(pred, new LinkedList<Word>());
		List<Word> argPath = pathToRoot(arg, new LinkedList<Word>());

		if (argPath == null || predPath == null)
			return null;

		List<Word> ret = new ArrayList<Word>();

		int commonIndex = 0;
		int min = (predPath.size() < argPath.size() ? predPath.size() : argPath
				.size());
		for (int i = 0; i < min; ++i) {
			// System.err.println( (predPath.get(i)==argPath.get(i)?"1":"0") +
			// " " + predPath.get(i).idx + " " + argPath.get(i).idx);
			/**
			 * if(predPath.get(i)==argPath.get(i)){ //Always true at root (ie
			 * first index)
			 **/

			if (predPath.get(i).idx == argPath.get(i).idx) { // sometimes, two
																// different
																// objects exist
																// that
																// represent the
																// same word
																// (because of
																// new
																// Pred(Word)
																// constructor?)
				commonIndex = i;
			}
		}
		for (int j = predPath.size() - 1; j >= commonIndex; --j) {
			ret.add(predPath.get(j));
		}
		for (int j = commonIndex + 1; j < argPath.size(); ++j) {
			ret.add(argPath.get(j));
		}
		return ret;
	}

	public static List<Word> pathToRoot(Word w, List<Word> visited) {
		// if(visited.contains(w))
		// return null;

		List<Word> path;

		if (w.isBOS) {
			path = new ArrayList<Word>();
			path.add(w);
			return path;
		}
		visited.add(w);

		path = pathToRoot(w.head, visited);
		// if(path==null)
		// return null;

		path.add(w);
		return path;
	}

	/**
	 * Converts this Word object one line following the CoNLL 2009 format.
	 * However, it does not include all columns, since this is part of a
	 * sentence. For proper CoNLL 2009 format output, use the
	 * Sentence.toString() method
	 */
	public String toString() {
		return Form + "\t" + Lemma + "\t" + Lemma + "\t" + POS + "\t" + POS
				+ "\t_\t" + Feats + "\t" + headID + "\t" + headID + "\t"
				+ Deprel + "\t" + Deprel;// + (rep[0]!=0.0?"\t"+rep[0]:"");
		// return Form+"\t"+Lemma+"\t"+POS+"\t"+headID+"\t"+Deprel;
	}

	public String toSpecialString() {
		return Form + "\t" + Lemma + "\t" + Lemma + "\t" + POS + "\t" + POS
				+ "\t_\t" + Feats + "\t" + headID + "\t" + headID + "\t"
				+ Deprel + "\t" + Deprel + (rep[0] != 0.0 ? "\t" + rep[0] : "");
		// return Form+"\t"+Lemma+"\t"+POS+"\t"+headID+"\t"+Deprel;
	}

	/**
	 * Recursive function that returns all nodes (words) dominated by the nodes,
	 * 
	 * @param words
	 *            The nodes to descend from
	 * @return
	 */
	private static Collection<Word> getDominated(Collection<Word> words) {
		Collection<Word> ret = new HashSet<Word>(words);
		for (Word c : words)
			ret.addAll(getDominated(c.getChildren()));
		return ret;
	}

	/**
	 * ) Returns the yield of this word, ie the complete phrase that defines the
	 * argument, with respect to the predicate. It follows algorithm 5.3 in
	 * Richard Johansson (2008), page 88
	 * 
	 * @param pred
	 *            The predicate of the proposition, required to deduce the yield
	 * @return the Yield
	 */
	public Yield getYield(Predicate pred, String argLabel, Set<Word> argSet) {
		Yield ret = new Yield(pred, mySentence, argLabel);
		ret.add(this);
		if (pred.idx == this.idx) // If the predicate is the argument, we don't
									// consider the yield
			return ret;
		Set<Integer> args = new TreeSet<Integer>();
		for (Word w : argSet)
			args.add(w.getIdx());
		for (Word child : children) {
			if (!args.contains(child.idx)) { // We don't branch down this child
												// if
				Collection<Word> subtree = getDominated(Arrays.asList(child));
				boolean containspred = false;
				for (Word w : subtree)
					if (w.idx == pred.idx)
						containspred = true;
				if (!containspred)
					// if(!subtree.contains(mySentence.get(pred.idx)));
					ret.addAll(subtree);
			}
		}
		// ret.addAll(getDominated(children));
		return ret;
	}

	public int getIdx() {
		return idx;
	}

	public static final Comparator<Word> WORD_LINEAR_ORDER_COMPARATOR = new Comparator<Word>() {
		@Override
		public int compare(Word arg0, Word arg1) {
			return arg0.idx - arg1.idx;
		}
	};

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	public void setCorefId(int corefid) {
		this.corefid = corefid;
	}

	public int getCorefId() {
		return corefid;
	}

	public Set<Word> getSpan() {
		if (span != null)
			return span;

		span = new TreeSet<Word>(new Comparator<Word>() {
			@Override
			public int compare(Word o1, Word o2) {
				return o1.getIdx() < o2.getIdx() ? -1
						: o1.getIdx() > o2.getIdx() ? 1 : 0;
			}
		});
		span.add(this);

		List<Word> children = new LinkedList<Word>();
		children.addAll(this.getChildren());
		while (!children.isEmpty()) {
			Word c = children.remove(0);
			if (span.contains(c))
				continue;

			span.add(c);
			children.addAll(c.getChildren());
		}

		return span;
	}

}
