package se.lth.cs.srl.preprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.depparse.DepInst;
import edu.illinois.cs.cogcomp.depparse.DepStruct;
import edu.illinois.cs.cogcomp.depparse.io.CONLLReader;
import edu.illinois.cs.cogcomp.nlp.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPipelineFactory;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer.Tokenization;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import is2.data.SentenceData09;
import is2.lemmatizer.Lemmatizer;
import is2.parser.Parser;
import is2.tag.Tagger;
import is2.tools.Tool;

import se.lth.cs.srl.preprocessor.tokenization.StanfordPTBTokenizer;
import se.lth.cs.srl.preprocessor.tokenization.Tokenizer;
import se.lth.cs.srl.util.Util;

public class IllinoisPreprocessor extends Preprocessor {

	protected final AnnotatorService as;
	protected final SLModel parser;
	
	public IllinoisPreprocessor(String modelfile) {
		AnnotatorService temp1 = null;
		SLModel temp2 = null;
		try {
			Properties defaultProps = new Properties();
			defaultProps.put(PipelineConfigurator.USE_POS.key, Configurator.TRUE);
			defaultProps.put(PipelineConfigurator.USE_LEMMA.key, Configurator.TRUE);
			defaultProps.put(PipelineConfigurator.USE_SHALLOW_PARSE.key, Configurator.TRUE);
			defaultProps.put(PipelineConfigurator.USE_NER_CONLL.key, Configurator.FALSE);
			defaultProps.put(PipelineConfigurator.USE_NER_ONTONOTES.key, Configurator.FALSE);
			defaultProps.put(PipelineConfigurator.USE_STANFORD_DEP.key, Configurator.FALSE);
			defaultProps.put(PipelineConfigurator.USE_STANFORD_PARSE.key, Configurator.FALSE);
			defaultProps.put(PipelineConfigurator.USE_SRL_VERB.key, Configurator.FALSE);
			defaultProps.put(PipelineConfigurator.USE_SRL_NOM.key, Configurator.FALSE);
	   		ResourceManager rm = new ResourceManager(defaultProps);
	        
			temp1 = IllinoisPipelineFactory.buildPipeline(rm);
			temp2 = SLModel.loadModel(modelfile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AnnotatorException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		as = temp1;
		CONLLReader.as = as;
		parser = temp2;
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
			as.addView(annotation, ViewNames.LEMMA);
			as.addView(annotation, ViewNames.POS);
			//as.addView(annotation, ViewNames.SHALLOW_PARSE);
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
		DepInst sent = new DepInst(annotation);
		DepStruct struct = null;
		try {
			struct = (DepStruct) parser.infSolver.getBestStructure(parser.wv, sent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// add parsing information to return value object
		instance.pheads = new int[instance.forms.length];
		instance.plabels = new String[instance.forms.length];
		instance.pfeats = new String[instance.forms.length];
		instance.pheads[0] = -1;
		for (int i = 1; i < sent.forms.length; i++) {
			instance.pheads[i] = struct.heads[i];
			instance.plabels[i] = struct.deprels[i];
			instance.pfeats[i] = "_";
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
