package se.lth.cs.srl.util;

public class Relation {
	public int head;
	public int dependent;
	public String label;

	public Relation(int head, int dependent, String label) {
		this.head = head;
		this.dependent = dependent;
		this.label = label;
	}
}
