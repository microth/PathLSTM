package se.lth.cs.srl.pipeline;

import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import se.lth.cs.srl.Parse;
import se.lth.cs.srl.corpus.ArgMap;
import se.lth.cs.srl.corpus.Predicate;

import java.io.PrintStream;
import java.util.List;

/**
 * A wrapper of {@link ArgumentClassifier} into CogComp's LBJava {@link Learner}
 */
public class LBJavaArgumentClassifier extends Learner {
    private Reranker srl;

    public LBJavaArgumentClassifier() {
        srl = new Reranker(Parse.parseOptions);
    }

    @Override
    public ScoreSet scores(Object example) {
        //TODO Convert example to predicate-induced sentence
        Predicate pred = null;
        List<ArgMap> candidateStructures = srl.getArgMaps(pred);
        int size = candidateStructures.size();
        String[] values = new String[size];
        double[] scores = new double[size];
        for (int i = 0; i < size; i++) {
            values[i] = candidateStructures.get(i).toString();
            scores[i] = candidateStructures.get(i).getRerankProb();
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
