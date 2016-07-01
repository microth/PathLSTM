package dmonner.xlbp.connection;

import java.io.Serializable;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdater;
import dmonner.xlbp.WeightUpdaterType;

public interface Connection extends Serializable
{
	public void activateTest();

	public void activateTrain();

	public void build();

	public void clear();

	public Connection copy(NetworkCopier copy);

	public String getName();

	public float getWeight(int j, int i);

	public void initializeAlphas(WeightUpdater lrs);

	public void initializeWeights(WeightInitializer win);

	public int nWeights();

	public int nWeightsPossible();

	public void processBatch();

	public void setWeightInitializer(WeightInitializer win);

	public void setWeightUpdater(WeightUpdaterType wut);

	public float[][] toEligibilitiesMatrix();

	public float[][] toMatrix();

	public void toString(NetworkStringBuilder sb);

	public String toString(String show);

	public void unbuild();

	public void updateEligibilities(final Responsibilities resp, final Responsibilities prev);

	public void updateWeights(float[][] dw);

	public void updateWeightsFromEligibilities(final Responsibilities copyresp);

	public void updateWeightsFromInputs(final Responsibilities resp);
}
