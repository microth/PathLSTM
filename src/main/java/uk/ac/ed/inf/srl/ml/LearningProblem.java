package uk.ac.ed.inf.srl.ml;

import java.util.Collection;
import java.util.Map;

public interface LearningProblem {

	public void addInstance(int label, Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats);

	public void done();

	public Model train();

}