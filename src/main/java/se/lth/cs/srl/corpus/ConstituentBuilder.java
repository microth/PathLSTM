package se.lth.cs.srl.corpus;

import java.util.TreeSet;

public class ConstituentBuilder {

	Sentence sen;
	Word head;

	public ConstituentBuilder(Sentence s, Word w) {
		sen = s;
		head = w;
	}

	public String toString() {
		TreeSet<Integer> children = new TreeSet<Integer>();
		children.add(head.getIdx());
		TreeSet<Integer> processed = new TreeSet<Integer>();
		while (!children.isEmpty()) {
			Word c = sen.get(children.pollFirst());
			if (!processed.contains(c.getIdx())) {
				processed.add(c.getIdx());
				for (Word cc : c.getChildren()) {
					children.add(cc.getIdx());
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		String[] wordformArray = sen.getFormArray();
		for (int i = processed.first(); i <= processed.last(); i++) {
			sb.append(wordformArray[i]);
			sb.append(" ");
		}
		return sb.toString().trim();
	}
}