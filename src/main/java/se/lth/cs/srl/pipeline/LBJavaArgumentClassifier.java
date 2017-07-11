package se.lth.cs.srl.pipeline;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.depparse.DepAnnotator;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.nlp.lemmatizer.IllinoisLemmatizer;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import is2.data.SentenceData09;
import se.lth.cs.srl.Parse;
import se.lth.cs.srl.corpus.ArgMap;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.options.ParseOptions;

import java.io.PrintStream;
import java.util.*;

/**
 * A wrapper of {@link ArgumentClassifier} into CogComp's LBJava {@link Learner}
 */
public class LBJavaArgumentClassifier extends Learner {
    private Reranker srl;
    private final String POS;
	
	private String prv_annoid;
	private Sentence prv_parse;
    
    public static void main(String[] args) {
    	// put a tokenized test sentence here
    	String test = "Michael went to Chicago by train .";
    	String[] forms = test.split(" ");

		Annotator pos = new POSAnnotator();
		Annotator lemma = new IllinoisLemmatizer();
		Annotator chunk = new ChunkerAnnotator();
		Annotator parser = new DepAnnotator();
   		
   		List<String[]> tokens = new LinkedList<>();
   		tokens.add(forms);
		TextAnnotation annotation = BasicTextAnnotationBuilder.createTextAnnotationFromTokens("", "", tokens);
		try {
			annotation.addView(pos);
			annotation.addView(lemma);
			annotation.addView(chunk);
			annotation.addView(parser);
		} catch (AnnotatorException e) {
			e.printStackTrace();
		} 		
		
		LBJavaArgumentClassifier lbj = new LBJavaArgumentClassifier(args);
		Constituent con = annotation.getView(ViewNames.TOKENS).getConstituents().get(0);
		Constituent pred = annotation.getView(ViewNames.TOKENS).getConstituents().get(1);
		new Relation("O", pred, con, 1.0);
		System.out.println(lbj.scores(con));
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
			System.arraycopy(args, 0, newargs, 0, x);
			System.arraycopy(args, x + 2, newargs, x + 2 - 2, args.length - (x + 2));
    		args = newargs;
    	}
    	/* END */

    	// load actual SRL pipeline + reranker 
    	Parse.parseOptions = new ParseOptions(args);
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

		// reuse previous parse, if possible
		if(annotation.getId().equals(prv_annoid)) {
			parse = prv_parse;
		} else {
			// assume the dependency parse already exists (part of preprocessing)
			// add parsing information to internal sentence representation
			instance.pheads = new int[instance.forms.length];
			instance.plabels = new String[instance.forms.length];
			instance.pfeats = new String[instance.forms.length];
			instance.pheads[0] = -1;
			// XXX: temporary fix (Jan 11): instance has no dummy token in anna version 3.5 
			// -> use struct index i-1
			for (Constituent node : annotation.getView(ViewNames.DEPENDENCY).getConstituents()) {
				int position = node.getStartSpan();
				int head = (node.getIncomingRelations().size() > 0) ?
						node.getIncomingRelations().get(0).getSource().getStartSpan() : -1;
				instance.pheads[position] = head + 1;
				instance.plabels[position] = node.getLabel();
			}
			
			// finalize internal representation
			parse = new Sentence(instance, false);
			prv_parse = parse;
		}
		
		// perform actual role labeling of given predicate
		// (if predicate has not been assigned a PAS before)
		int predIndex = 1+constituent.getIncomingRelations().get(0).getSource().getSpan().getFirst();
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
    		String label = "O";
    		for(Word w : candidate.keySet()) {
    			if( (predIndex==argIndex && argIndex==w.getIdx()) 
    					|| (predIndex!=argIndex && w.getYield(p, candidate.get(w), candidate.keySet()).contains(a))) {
    				label = candidate.get(w); 
    			}
    		}
    		
    		// sum up score of all candidates that assign specific label to constituent
    		if(!label2unnormalized_score.containsKey(label))
    			label2unnormalized_score.put(label, candidate.getProb());
    		else
    			label2unnormalized_score.put(label, candidate.getProb() + label2unnormalized_score.get(label) );
    			//if(candidate.getProb() > label2unnormalized_score.get(label))
        		//	label2unnormalized_score.put(label, candidate.getProb());

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
