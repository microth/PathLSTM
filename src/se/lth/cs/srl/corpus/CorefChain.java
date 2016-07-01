package se.lth.cs.srl.corpus;

import java.util.LinkedList;

public class CorefChain extends LinkedList<String> {

	int id;

	public CorefChain(int id) {
		this.id = id;
	}

	public String getId() {
		return new Integer(id).toString();
	}

}
