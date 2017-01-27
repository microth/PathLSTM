package se.lth.cs.srl.pipeline;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.depparse.DepInst;
import edu.illinois.cs.cogcomp.depparse.DepStruct;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import is2.data.SentenceData09;
import se.lth.cs.srl.Parse;
import se.lth.cs.srl.corpus.ArgMap;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.options.ParseOptions;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A wrapper of {@link ArgumentClassifier} into CogComp's LBJava {@link Learner}
 */
public class LBJavaArgumentClassifier extends Learner {
    private Reranker srl;
	protected final SLModel parser;
    private final String POS;
	
	private String prv_annoid;
	private Sentence prv_parse;
    
    public static void main(String[] args) {
    	// put a test sentence here
    	String test = "";
    	String[] forms = test.split(" ");
    	
		Properties nonDefaultProps = new Properties();
		nonDefaultProps.put(PipelineConfigurator.USE_POS.key, Configurator.TRUE);
		nonDefaultProps.put(PipelineConfigurator.USE_LEMMA.key, Configurator.TRUE);
		nonDefaultProps.put(PipelineConfigurator.USE_SHALLOW_PARSE.key, Configurator.TRUE);
		nonDefaultProps.put(PipelineConfigurator.USE_NER_CONLL.key, Configurator.FALSE);
		nonDefaultProps.put(PipelineConfigurator.USE_NER_ONTONOTES.key, Configurator.FALSE);
		nonDefaultProps.put(PipelineConfigurator.USE_STANFORD_DEP.key, Configurator.FALSE);
		nonDefaultProps.put(PipelineConfigurator.USE_STANFORD_PARSE.key, Configurator.FALSE);
		nonDefaultProps.put(PipelineConfigurator.USE_SRL_VERB.key, Configurator.FALSE);
		nonDefaultProps.put(PipelineConfigurator.USE_SRL_NOM.key, Configurator.FALSE);
   		ResourceManager rm = Configurator.mergeProperties(new PipelineConfigurator().getDefaultConfig(),
				new ResourceManager(nonDefaultProps));
        
   		AnnotatorService as = null;
   		try {
			as = PipelineFactory.buildPipeline(rm);
		} catch (Exception e) {
			e.printStackTrace();			
		}
   		
   		List<String[]> tokens = new LinkedList<>();
   		tokens.add(forms);
		TextAnnotation annotation = BasicTextAnnotationBuilder.createTextAnnotationFromTokens("", "", tokens);
		try {
			as.addView(annotation, ViewNames.POS);
			as.addView(annotation, ViewNames.LEMMA);
			as.addView(annotation, ViewNames.SHALLOW_PARSE);
		} catch (AnnotatorException e) {
			e.printStackTrace();
		} 		
		
		LBJavaArgumentClassifier lbj = new LBJavaArgumentClassifier(args);
		Constituent con = new Constituent("", "", annotation, 0, test.length());
		lbj.scores(con);
    }
    
    public LBJavaArgumentClassifier(String[] args) {   	
    	/* BEGIN: Check for -pos parameter to do VerbSRL / NounSRL separately */
    	String temp1 = "";
    	int x = -1;
    	for(int i=0; i<args.length; i++) {
    		if(args[i].equals("-pos")) {
    			// commandline paramaters should contain "-pos N" or "-pos V" (or none of the two)
    			temp1 = args[i+1];
    			x = i;
    			break;
    		}
    	}
    	POS = temp1;
    	if(!POS.equals("")) {
    		String[] newargs = new String[args.length-2];
    		for(int i=0; i<x; i++)
    			newargs[i] = args[i];
    		for(int i=x+2; i<args.length; i++)
    			newargs[i-2] = args[i];
    		args = newargs;
    	}
    	/* END */
    	  	
    	
    	/* BEGIN: Check for -parser parameter to load dependency parser model */
    	SLModel temp2 = null;
    	x = -1;
    	for(int i=0; i<args.length; i++) {
    		if(args[i].equals("-parse")) {
    			temp1 = args[i+1];
    			x = i;
    			break;
    		}
    	}
    	try {
			temp2 = SLModel.loadModel(temp1);
		} catch (Exception e) {
			e.printStackTrace();
		}
   		String[] newargs = new String[args.length-2];
   		for(int i=0; i<x; i++)
   			newargs[i] = args[i];
   		for(int i=x+2; i<args.length; i++)
   			newargs[i-2] = args[i];
    	parser = temp2;
    	/* END */

    	// load actual SRL pipeline + reranker 
    	Parse.parseOptions = new ParseOptions(newargs);   	
        srl = new Reranker(Parse.parseOptions);
        // Required by LBJava
		name = "PathLSTMClassifier" + POS;
    }

    @Override
    public ScoreSet scores(Object example) {
		Constituent constituent = (Constituent) example;
    	TextAnnotation annotation = constituent.getTextAnnotation();

    	// initialize internal representation
    	String[] forms = annotation.getTokens();
		SentenceData09 instance = new SentenceData09();
		instance.init(forms);
		
		// re-use POS and lemma information from preprocessing		
		TokenLabelView POSView = (TokenLabelView) annotation.getView(ViewNames.POS);
		TokenLabelView LemmaView = (TokenLabelView) annotation.getView(ViewNames.LEMMA);
		
		// XXX: temporary fix (Jan 11): instance has no dummy token in anna version 3.5
		// -> start indexing at 0
		for (int i = 0; i < instance.ppos.length; i++) {
			instance.ppos[i] = POSView.getLabel(i);
			instance.plemmas[i] = LemmaView.getLabel(i);
		}
				
		Sentence parse = null;
		int predIndex = 1+constituent.getIncomingRelations().get(0).getSource().getSpan().getFirst();

		// reuse previous parse, if possible
		if(annotation.getId().equals(prv_annoid)) {
			parse = prv_parse;
		} else {
			// run dependency parser (not part of preprocessing?)
			DepInst sent = new DepInst(annotation);
			DepStruct struct = null;
			try {
				struct = (DepStruct) parser.infSolver.getBestStructure(parser.wv, sent);
			} catch (Exception e) {
				e.printStackTrace();
			}
				
			// add parsing information to internal sentenec representation
			instance.pheads = new int[instance.forms.length];
			instance.plabels = new String[instance.forms.length];
			instance.pfeats = new String[instance.forms.length];
			instance.pheads[0] = -1;
			// XXX: temporary fix (Jan 11): instance has no dummy token in anna version 3.5 
			// -> use struct index i-1
			for (int i = 1; i < sent.forms.length; i++) {
				instance.pheads[i-1] = struct.heads[i];
				instance.plabels[i-1] = struct.deprels[i];
				instance.pfeats[i-1] = "_";
			}
			
			// finalize internal representation
			parse = new Sentence(instance, false);
			prv_parse = parse;
		}
		
		// perform actual role labeling of given predicate
		// (if predicate has not been assigned a PAS before) 
		if(parse.get(predIndex).getClass() != Predicate.class) {
			parse.getPredicates().clear();
			parse.makePredicate(predIndex);
			srl.parse(parse);
		}
		
		// store for future reference
		prv_annoid = annotation.getId();

		int argIndex = 1+constituent.getStartSpan();
		// Predicate and argument objects after SRL
		Predicate p = (Predicate)parse.get(predIndex);
		Word  a = parse.get(argIndex);
   	
    	double total = 0.0;
    	// iterate over reranker's n-best output 
    	Map<String, Double> label2unnormalized_score = new HashMap<String, Double>();
    	for(ArgMap candidate : p.getCandidates()) {
    		// sum up total score of all candidate
    		total += candidate.getProb();
    		
    		// assume no label as default
    		String label = "";
    		for(Word w : candidate.keySet()) {
    			if( (predIndex==argIndex && argIndex==w.getIdx()) 
    					|| (predIndex!=argIndex && getDominated(Collections.singleton(w)).contains(a))) {
    				label = candidate.get(w); 
    			}
    		}
    		
    		// sum up score of all candidates that assign specific label to constituent
    		if(!label2unnormalized_score.containsKey(label))
    			label2unnormalized_score.put(label, candidate.getProb());
    		else
    			label2unnormalized_score.put(label, candidate.getProb() + label2unnormalized_score.get(label) );
		}
   
    	// normalize score for constituent label
    	String[] values = new String[label2unnormalized_score.size()];
    	double[] scores = new double[label2unnormalized_score.size()];    	
    	int i=0;
    	for(String label : label2unnormalized_score.keySet()) {
    		values[i] = label;
    		scores[i] = label2unnormalized_score.get(label)/total; 
    		i++;
    	}
    	
        return new ScoreSet(values, scores);
    }
    
    // compute set of all words governed by (singleton) set w
    private static Collection<Word> getDominated(Set<Word> w) {
		Collection<Word> ret = new HashSet<>(w);
		for (Word c : w)
			ret.addAll(getDominated(c.getChildren()));
		return ret;
    }

	@Override
	public String discreteValue(Object example) {
    	ScoreSet scoreSet = scores(example);
    	return scoreSet.highScoreValue();
	}

    @Override
    public FeatureVector classify(int[] exampleFeatures, double[] exampleValues) {
        throw new RuntimeException("Should not be called during runtime.");
    }

    @Override
    public ScoreSet scores(int[] exampleFeatures, double[] exampleValues) {
        throw new RuntimeException("Should not be called during runtime.");
    }

    @Override
    public void learn(int[] exampleFeatures, double[] exampleValues, int[] exampleLabels, double[] labelValues) {
        throw new RuntimeException("Cannot train ArgumentClassifier from here. " +
                "Please use the code in se.lth.cs.srl.pipeline.PipelineStep");
    }

    @Override
    public void write(PrintStream out) {
        throw new RuntimeException("Cannot write the ArgumentClassifier model from here. " +
                "Please use the code in se.lth.cs.srl.pipeline.PipelineStep");
    }
}
