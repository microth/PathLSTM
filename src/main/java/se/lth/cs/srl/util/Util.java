package se.lth.cs.srl.util;

public class Util {

	public static String insertCommas(long l) {
		StringBuilder ret = new StringBuilder(Long.toString(l));
		ret.reverse();
		for (int i = 3; i < ret.length(); i += 4) {
			if (i + 1 <= ret.length())
				ret.insert(i, ",");
		}
		return ret.reverse().toString();
	}

}
