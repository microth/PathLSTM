package se.lth.cs.srl.preprocessor;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.depparse.DepAnnotator;
import edu.illinois.cs.cogcomp.nlp.lemmatizer.IllinoisLemmatizer;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import is2.data.SentenceData09;
import se.lth.cs.srl.preprocessor.tokenization.StanfordPTBTokenizer;
import se.lth.cs.srl.preprocessor.tokenization.Tokenizer;
import se.lth.cs.srl.util.Util;

import java.util.ArrayList;
import java.util.List;

public class IllinoisPreprocessor extends Preprocessor {

	protected final Annotator pos, lemma, chunk, parser;
	
	public IllinoisPreprocessor(Tokenizer tokenizer, String modelfile) {
		this.tokenizer = tokenizer;
		parser = new DepAnnotator();
		pos = new POSAnnotator();
		lemma = new IllinoisLemmatizer();
		chunk = new ChunkerAnnotator();
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
		
		// lemmatization and POS tagging of all tokens except <root> (index 0)
		List<String[]> tokens = new ArrayList<String[]>();
		String[] tmp = new String[instance.forms.length-1];
		for(int i=0; i<tmp.length; i++)
			tmp[i] = instance.forms[i+1];
		tokens.add(tmp);		
		TextAnnotation annotation = BasicTextAnnotationBuilder.createTextAnnotationFromTokens("", "", tokens);
		try {
			annotation.addView(pos);
			annotation.addView(lemma);
			annotation.addView(chunk);
			annotation.addView(parser);
		} catch (AnnotatorException e) {
			e.printStackTrace();
		}
		// add preprocessing information to return value object
		TokenLabelView POSView = (TokenLabelView) annotation.getView(ViewNames.POS);
		TokenLabelView LemmaView = (TokenLabelView) annotation.getView(ViewNames.LEMMA);
		for (int i = 1; i < instance.ppos.length; i++) {
			instance.ppos[i] = POSView.getLabel(i-1);
			instance.plemmas[i] = LemmaView.getLabel(i-1);
		}
		
		// dependency parse preprocessed text
		// add parsing information to return value object
		instance.pheads = new int[instance.forms.length];
		instance.plabels = new String[instance.forms.length];
		instance.pfeats = new String[instance.forms.length];
		instance.pheads[0] = -1;
		for (Constituent node : annotation.getView(ViewNames.DEPENDENCY).getConstituents()) {
			int position = node.getStartSpan();
			int head = (node.getIncomingRelations().size() > 0) ?
					node.getIncomingRelations().get(0).getSource().getStartSpan() : -1;
			instance.pheads[position] = head + 1;
			instance.plabels[position] = node.getLabel();
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
		return true;
	}
}
