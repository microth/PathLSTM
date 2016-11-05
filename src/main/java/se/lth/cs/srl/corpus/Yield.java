package se.lth.cs.srl.corpus;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

public class Yield extends TreeSet<Word> implements Comparable<Yield> {
	private static final long serialVersionUID = 1L;
	private Predicate pred;
	private Sentence sen;
	private String argLabel;

	public String getArgLabel() {
		return argLabel;
	}

	public Yield(Predicate pred, Sentence sen, String argLabel) {
		super(sen.wordComparator);
		this.pred = pred;
		this.sen = sen;
		this.argLabel = argLabel;
	}

	public Predicate getPred() {
		return pred;
	}

	/**
	 * Checks whether this yield is continuous, ie they contain all the words in
	 * the sentence between this.first() and this.last(). Yields with 1 word are
	 * always continuous
	 * 
	 * @return true if this yield is continuous, false otherwise
	 */
	public boolean isContinuous() {
		if (this.size() < 2)
			return true;
		int senIndex = this.first().idx;
		for (Word w : this) {
			if (w.idx != senIndex++)
				return false;
		}
		return true;
	}

	/**
	 * Compares yields. Yields are sorted according to their left-most token.
	 * I.e. with three yields with first element tokens with ID's 1, 7, and 5
	 * respectively, they will be sorted in the order 1, 5, 7, regardless of
	 * gaps and continuity
	 */
	@Override
	public int compareTo(Yield y) {
		return this.first().getMySentence().wordComparator.compare(
				this.first(), y.first());
	}

	/**
	 * Breaks this yield down to continuous yields if this yield is
	 * discontinuous, otherwise it returns itself in a list. Yields are labeled
	 * lab, C-lab, C-C-lab, etc in a sequential manner from left to right. It
	 * follows algorithm 5.3 in Richard Johansson (2008), page 88
	 * 
	 * @return a collection of continuous yields
	 */
	public Collection<Yield> explode() {
		if (isContinuous())
			return Arrays.asList(this);
		Collection<Yield> ret = new TreeSet<Yield>();
		String curArgLabel = argLabel;
		Yield subYield = new Yield(pred, sen, curArgLabel);
		for (int i = this.first().idx; i <= this.last().idx; ++i) {
			Word curWord = sen.get(i);
			if (this.contains(curWord)) { // If this yield contain the word, add
											// it, it's continuous.
				subYield.add(curWord);
			} else if (!subYield.isEmpty()) { // If this yield doesn't contain
												// the word, and we have an
												// unempty subyield, then the
												// subyield is completed
				ret.add(subYield);
				curArgLabel = "C-" + curArgLabel;
				subYield = new Yield(pred, sen, curArgLabel);
			}
		}
		if (!subYield.isEmpty()) // Add the last subyield
			ret.add(subYield);
		return ret;
	}
}
