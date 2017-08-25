package se.lth.cs.srl.preprocessor;

import java.util.Properties;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations.DependencyAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CoNLLDepAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.trees.Dependency;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import is2.data.SentenceData09;
import se.lth.cs.srl.preprocessor.tokenization.StanfordPTBTokenizer;
import se.lth.cs.srl.preprocessor.tokenization.Tokenizer;

public class StanfordPreprocessor extends Preprocessor {
	StanfordCoreNLP stanfordpipeline;
	
	
	public StanfordPreprocessor(Tokenizer tokenizer) {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
		props.put("tokenize.whitespace", "true");
		props.put("ssplit.eolonly", "true");
		props.put("parse.keepPunct", "true"); // not working?
		
		stanfordpipeline = new StanfordCoreNLP(props);
		
		this.tokenizer = tokenizer;
	}
	
	@Override
	public boolean hasParser() {
		return true;
	}

	@Override
	public StringBuilder getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SentenceData09 preprocess(SentenceData09 sen) {
		StringBuilder text = new StringBuilder();		
		for(int i=1; i<sen.forms.length; i++) {
			if(i>0) text.append(" ");
			text.append(sen.forms[i]);
		}
		
		Annotation document = new Annotation(text.toString());
		stanfordpipeline.annotate(document);
		
		// not set up yet?
		sen.pfeats = new String[sen.forms.length];
		sen.plabels = new String[sen.forms.length];
		sen.pheads = new int[sen.forms.length];		
		sen.pheads[0] = -1;
		
		CoreMap sentence = document.get(SentencesAnnotation.class).get(0);
		int i=1;
		try {
		for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			//if(!sen.forms[i].equals("donotincreasethecounter")) i++;
			sen.forms[i] = token.word();
			sen.ppos[i] = token.tag();
			sen.plemmas[i] = token.lemma();
			sen.pfeats[i] = "_";
			i++;
		}} catch(Exception e) {
			System.err.println("ERROR: sentence length mismatches token number in Stanford annotation!");
			i=1;
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				System.err.print(token.word() + "\t");
				System.err.println(sen.forms[i++]);
			}
			System.exit(1);
		}

		SemanticGraph tree = sentence.get(BasicDependenciesAnnotation.class);
		for(TypedDependency dep : tree.typedDependencies()) {
			sen.plabels[dep.dep().index()] = dep.reln().toString().toUpperCase();
			sen.pheads[dep.dep().index()] = dep.gov().index();			
		}
		
		for(i=1; i<sen.pheads.length; i++) {
			if(sen.plabels[i]!=null) continue;
			sen.plabels[i] = "PUNCT";
			sen.pheads[i] = 0;
		}
			
		return sen;
	}

}
