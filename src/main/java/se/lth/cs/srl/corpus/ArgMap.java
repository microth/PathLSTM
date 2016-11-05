package se.lth.cs.srl.corpus;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ArgMap extends HashMap<Word, String> {
	private static final long serialVersionUID = 1L;

	private double prob;
	private double idProb;
	private double lblProb;
	private double rerankProb;

	public ArgMap() {
		super();
		prob = 1;
	}

	public ArgMap(ArgMap argmap) {
		super(argmap);
		this.prob = argmap.prob;
		this.idProb = argmap.idProb;
		this.lblProb = argmap.lblProb;
		this.rerankProb = argmap.rerankProb;
	}

	public ArgMap(Map<Word, String> argMap) {
		super(argMap);
	}

	public String put(Word arg, String argLabel, double prob) {
		multiplyProb(prob);
		return super.put(arg, argLabel);
	}

	public void multiplyProb(double prob) {
		this.prob *= prob;
	}

	public double computeScore(Map<Word, String> goldStandard) {
		if (this.size() == 0 && goldStandard.size() == 0) // If both are empty
															// its 1
			return 1;
		int tp = 0, fp = 0, fn = 0;
		for (Word w : this.keySet()) {
			if (goldStandard.containsKey(w)) {
				String goldLabel = goldStandard.get(w);
				String propLabel = this.get(w);
				if (goldLabel.equals(propLabel)) { // Match
					++tp;
				} else {
					++fn; // Correct Label non-NONE, guessed Label non-NONE but
							// incorrect
					++fp; // Guessed label non-NONE, correct Label different
							// non-NONE
				}
			} else { // Guessed label non-NONE, correct Label NONE
				++fp;
			}
		}

		for (Word w : goldStandard.keySet())
			if (!this.containsKey(w))
				++fn; // Correct Label non-NONE, guessed Label NONE

		double p = (double) tp / (tp + fp);
		double r = (double) tp / (tp + fn);
		if (p + r > 0)
			return 2 * p * r / (p + r);
		else
			return 0;
	}

	public static final Comparator<ArgMap> PROB_COMPARATOR = new Comparator<ArgMap>() {
		@Override
		public int compare(ArgMap arg0, ArgMap arg1) {
			if (arg0.prob < arg1.prob) {
				return -1;
			} else if (arg0.prob == arg1.prob) {
				return 0;
			} else {
				return 1;
			}
		}
	};
	public static final Comparator<ArgMap> REVERSE_PROB_COMPARATOR = Collections
			.reverseOrder(PROB_COMPARATOR);

	public String toString() {
		return "Prob: " + prob + ", IDProb: " + idProb + ", LblProb: "
				+ lblProb + ", RerankProb: " + rerankProb + ". "
				+ super.toString();
	}

	public double getProb() {
		return prob;
	}

	public void setProb(double prob) {
		this.prob = prob;
	}

	public void resetProb() {
		this.prob = 1;
	}

	public double getIdProb() {
		return idProb;
	}

	public void setIdProb(double idProb) {
		this.idProb = idProb;
	}

	public double getLblProb() {
		return lblProb;
	}

	public void setLblProb(double lblProb) {
		this.lblProb = lblProb;
	}

	public double getRerankProb() {
		return rerankProb;
	}

	public void setRerankProb(double rerankProb) {
		this.rerankProb = rerankProb;
	}
}
