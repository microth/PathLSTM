package se.lth.cs.srl.corpus;

public class StringInText {
	protected String s;
	protected int beginPos;
	protected int endPos;

	public StringInText(String s, int beginPos, int endPos) {
		this.s = s;
		this.beginPos = beginPos;
		this.endPos = endPos;
	}

	public String word() {
		return s;
	}

	public int begin() {
		return beginPos;
	}

	public int end() {
		return endPos;
	}
}
