package uk.ac.ed.inf.srl.ml.liblinear;

public class Label implements Comparable<Label> {
	private Integer cl;
	private double prob;

	public Label(Integer cl) {
		this.cl = cl;
		prob = 0;
	}

	public Label(Integer cl, double prob) {
		this.cl = cl;
		this.prob = prob;
	}

	public boolean equals(Label otherLabel) {
		return cl == otherLabel.cl;
	}

	public Integer getLabel() {
		return cl;
	}

	public double getProb() {
		return prob;
	}

	public void setProb(double prob) {
		this.prob = prob;
	}

	public int compareTo(Label o) {
		if (prob < o.prob) {
			return -1;
		} else if (prob > o.prob) {
			return 1;
		}
		return 0;
	}
	
	public String toString() {
		return (cl + ":" + prob);
	}
}
