package se.lth.cs.srl.pipeline;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import se.lth.cs.srl.Parse;
import se.lth.cs.srl.corpus.ArgMap;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.languages.Language;
import se.lth.cs.srl.options.CompletePipelineCMDLineOptions;
import se.lth.cs.srl.preprocessor.Preprocessor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * A wrapper of {@link ArgumentClassifier} into CogComp's LBJava {@link Learner}
 */
public class LBJavaArgumentClassifier extends Learner {
    private Reranker srl;
    private Preprocessor pp;
    
    public LBJavaArgumentClassifier(String[] args) {
    	CompletePipelineCMDLineOptions options = new CompletePipelineCMDLineOptions();
    	options.parseCmdLineArgs(args);
    	try {
			pp = Language.getLanguage().getPreprocessor(options);
		} catch (IOException e) {
			// throw exception if model files for preprocessing cannot be read
			e.printStackTrace();
			System.exit(1);
		}
    	Parse.parseOptions = options.getParseOptions();    	
        srl = new Reranker(Parse.parseOptions);
    }

    @Override
    public ScoreSet scores(Object example) {
		Constituent constituent = (Constituent) example;
    	TextAnnotation anno = constituent.getTextAnnotation();
    	
    	Sentence s = anno.sentences().get(0);
    	se.lth.cs.srl.corpus.Sentence parse = new se.lth.cs.srl.corpus.Sentence(pp.preprocess(s.getTokens()), false);  
    	srl.parse(parse);

    	// number of SRL candidate outputs is #candidates(pred_1) * ... * #candidates(pred_n)
    	int size = 1;
    	List<Predicate> predicates = parse.getPredicates();
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
