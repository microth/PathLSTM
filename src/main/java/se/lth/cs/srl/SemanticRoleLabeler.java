package se.lth.cs.srl;

import java.util.Date;
import java.util.List;

import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.StringInText;
import se.lth.cs.srl.util.Util;

public abstract class SemanticRoleLabeler {

	public void parseSentence(Sentence s) {
		long startTime = System.currentTimeMillis();
		parse(s);
		parsingTime += System.currentTimeMillis() - startTime;
		senCount++;
		predCount += s.getPredicates().size();
	}

	protected abstract void parse(Sentence s);

	public String getStatus() {
		StringBuilder ret = new StringBuilder(
				"Semantic role labeler started at " + startDate + "\n");
		ret.append("Time spent loading SRL models (ms)\t\t"
				+ Util.insertCommas(loadingTime) + "\n");
		ret.append("Time spent parsing semantic roles (ms)\t\t"
				+ Util.insertCommas(parsingTime) + "\n");
		ret.append("\n");
		ret.append("Number of sentences\t" + Util.insertCommas(senCount) + "\n");
		ret.append("Number of predicates\t" + Util.insertCommas(predCount)
				+ "\n");
		ret.append("SRL speed (ms/sen)\t" + ((double) parsingTime / senCount)
				+ "\n");
		ret.append(getSubStatus());
		return ret.toString();
	}

	protected abstract String getSubStatus();

	public long loadingTime = 0;
	public long parsingTime = 0;
	public int senCount = 0;
	public int predCount = 0;
	public final Date startDate = new Date();

}
