package se.lth.cs.srl.features;

public class NumFeature {
	static public String bin(int i) {
		if (i <= -20)
			return "-20";
		if (i <= -10)
			return "-10";
		if (i <= -5)
			return "-5";

		if (i >= 20)
			return "20";
		if (i >= 10)
			return "10";
		if (i >= 5)
			return "5";

		return Integer.toString(i);
	}
}
