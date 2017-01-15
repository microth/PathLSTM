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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * A wrapper of {@link ArgumentClassifier} into CogComp's LBJava {@link Learner}
 */
public class LBJavaArgumentClassifier extends Learner {
    private Reranker srl;
	protected final SLModel parser;
    private final String POS;
	
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
		Sentence parse = new Sentence(instance, false);
		//System.err.println(parse);
		
		// perform actual role labeling step 
    	srl.parse(parse);

    	// number of SRL candidate outputs is #candidates(pred_1) * ... * #candidates(pred_n)
    	int size = 1;
    	List<Predicate> predicates = new LinkedList<>();
    	// only consider predicates that match -pos option (if specified)
    	for(Predicate p : parse.getPredicates()) {
    		// note that this is exactly how the noun/verb models are separated
    		if(POS.equals("") || p.getPOS().startsWith(POS))
    			predicates.add(p);
    	}
    	int preds = predicates.size();
    	int[] offsets = new int[preds];
		for(int i=0; i<preds; i++) {
			List<ArgMap> candidateStructures = predicates.get(i).getCandidates();
			offsets[i] = size;
			size *= candidateStructures.size();		
		}
        double[] scores = new double[size];
        Arrays.fill(scores, 1.0);
        
        String[][] valuePerWord = new String[size][parse.size()];

        // iterate through all combinations i of candidate predicate--argument structures 
        for(int i=0; i<size; i++) {
        	int rest = i;
        	for(int j=preds-1; j>=0; j--) {
        		Predicate pred = predicates.get(j);        		
        		ArgMap candidate = pred.getCandidates().get(rest/offsets[j]);
        		
        		// compute score of candidate SRL output as average over selected PAS  
        		scores[i] *= candidate.getProb()/(double)preds;
        		
        		for(Word w : candidate.keySet()) {
        			int k = w.getIdx();

        			// one word can be part of multiple predicate--argument structures 
        			if(valuePerWord[i][k]!=null)
        				valuePerWord[i][k] += ",";
        			else
        				valuePerWord[i][k] = "";
        			
        			// each label assignment is a combination of the predicate lemma
        			// and the argument label for a word as predicated in the candidate structure
        			valuePerWord[i][k] += predicates.get(j).getLemma()+"--"+candidate.get(w);
        		}
        		
        		rest = rest%offsets[j];
        	}
    	}

        // for each combination i of candidates, 
        //     output label assignments over all words j in the sentence 
    	String[] values = new String[size];
    	for(int i=0; i<values.length; i++) {
    		StringBuffer sb = new StringBuffer();
    		// start at j=1 to skip root token
    		for(int j=1; j<valuePerWord[i].length; j++) {
    			if(j>0) sb.append(" ");
    			sb.append(j);
    			sb.append(":");
    			
    			// output "0" in case no role was assigned to word j 
    			sb.append(valuePerWord[i][j]==null?"0":valuePerWord[i][j]);
    		}
    		values[i] = sb.toString();
    	}
        return new ScoreSet(values, scores);
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
