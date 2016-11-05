package se.lth.cs.srl.preprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import is2.data.SentenceData09;
import is2.lemmatizer.Lemmatizer;
import is2.parser.Parser;
import is2.tag.Tagger;
import is2.tools.Tool;

import se.lth.cs.srl.corpus.StringInText;
import se.lth.cs.srl.preprocessor.tokenization.StanfordPTBTokenizer;
import se.lth.cs.srl.preprocessor.tokenization.Tokenizer;
import se.lth.cs.srl.util.BohnetHelper;
import se.lth.cs.srl.util.Util;

public class PipelinedPreprocessor extends Preprocessor {

	protected final Tool lemmatizer;
	protected final Tagger tagger;
	protected final is2.mtag.Tagger mtagger;
	protected final Parser parser;

	public PipelinedPreprocessor(Tokenizer tokenizer, Lemmatizer lemmatizer,
			Tagger tagger, is2.mtag.Tagger mtagger, Parser parser) {
		this.tokenizer = tokenizer;
		this.lemmatizer = (Tool) lemmatizer;
		this.tagger = tagger;
		this.mtagger = mtagger;
		this.parser = parser;
	}

	public long tagTime = 0;
	public long mtagTime = 0;

	/**
	 * Executes all loaded components on this SentenceData09 object. It is
	 * assumed to have the forms filled out only
	 * 
	 * @param instance
	 *            the instance to process
	 * @return the same object as it was passed, but with some arrays filled out
	 */
	protected SentenceData09 preprocess(SentenceData09 instance) {
		if (lemmatizer != null) {
			long start = System.currentTimeMillis();
			lemmatizer.apply(instance);
			lemmatizeTime += System.currentTimeMillis() - start;
		}

		// parantheses fix
		for (int i = 0; i < instance.forms.length; i++) {
			if (instance.forms[i].equals("-LRB-"))
				instance.forms[i] = "(";
			if (instance.forms[i].equals("-RRB-"))
				instance.forms[i] = ")";
		}

		if (tagger != null) {
			long start = System.currentTimeMillis();
			tagger.apply(instance);
			tagTime += System.currentTimeMillis() - start;
		}
		if (mtagger != null) {
			long start = System.currentTimeMillis();
			mtagger.apply(instance);
			// XXX
			// Need to split the feats and put them in the right place for the
			// parser.
			for (int i = 1; i < instance.pfeats.length; ++i) {
				if (instance.pfeats[i] != null
						&& !instance.pfeats[i].equals("_"))
					instance.feats[i] = instance.pfeats[i].split("\\|");
			}
			mtagTime += System.currentTimeMillis() - start;
		} else {
			instance.pfeats = new String[instance.forms.length];
			Arrays.fill(instance.pfeats, "_");
		}
		if (parser != null) {
			synchronized (parser) {
				long start = System.currentTimeMillis();
				instance = parser.apply(instance);
				dpTime += System.currentTimeMillis() - start;
			}
		} else { // If there is no parser, we have to recreate the sentence
					// object so the root dummy gets thrown out (as in the
					// parser)
			instance = new SentenceData09(instance);
		}
		return instance;
	}

	public StringBuilder getStatus() {
		StringBuilder sb = new StringBuilder();
		if (tokenizer != null)
			sb.append("Tokenizer: " + tokenizer.getClass().getSimpleName())
					.append('\n');
		sb.append("Tokenizer time:  " + Util.insertCommas(tokenizeTime))
				.append('\n');
		sb.append("Lemmatizer time: " + Util.insertCommas(lemmatizeTime))
				.append('\n');
		sb.append("Tagger time:     " + Util.insertCommas(tagTime))
				.append('\n');
		sb.append("MTagger time:    " + Util.insertCommas(mtagTime)).append(
				'\n');
		sb.append("Parser time:     " + Util.insertCommas(dpTime)).append('\n');
		return sb;
	}

	/*
	 * public static void main(String[] args) throws Exception{
	 * 
	 * File desegmentedInput=new File("chi-desegmented.out"); Tokenizer
	 * tokenizer=new StanfordChineseSegmenterWrapper(new
	 * File("/home/anders/Download/stanford-chinese-segmenter-2008-05-21/data"
	 * )); Lemmatizer lemmatizer=new SimpleChineseLemmatizer(); Tagger
	 * tagger=BohnetHelper.getTagger(new File("models/chi/tag-chn.model"));
	 * Preprocessor pp=new Preprocessor(tokenizer,lemmatizer,tagger,null,null);
	 * BufferedReader reader=new BufferedReader(new InputStreamReader(new
	 * FileInputStream(desegmentedInput),"UTF-8")); String line;
	 * while((line=reader.readLine())!=null){ String[] tokens=pp.tokenize(line);
	 * SentenceData09 s=pp.preprocess(tokens); System.out.println(s); }
	 * reader.close(); }
	 */

	public void resetStartPosition() {
		((StanfordPTBTokenizer) tokenizer).resetStartPosition();
	}

	public boolean hasParser() {
		return parser != null;
	}
}
