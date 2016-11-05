package dmonner.xlbp;

import java.io.Serializable;

public interface Component extends Serializable, Comparable<Component>
{
	public void activateTest();

	public void activateTrain();

	public void build();

	public void clear();

	public void clearActivations();

	public void clearEligibilities();

	public void clearResponsibilities();

	public Component copy(NetworkCopier copier);

	public Component copy(String nameSuffix);

	public void copyConnectivityFrom(Component comp, NetworkCopier copier);

	public String getName();

	public boolean isBuilt();

	public int nWeights();

	/**
	 * To be called once after network is set up. For individual layers, checks that they have all the
	 * necessary inputs and outputs. For fan-in or fan-out layers, checks to see if they only have one
	 * input/output in the direction that should have multiples; if so, bows itself out and connects
	 * input to output directly. For higher level components, keeps track of this process and reworks
	 * internal pointers.
	 * 
	 * @return false iff this component has removed itself from the network; true otherwise.
	 */
	public boolean optimize();

	public void processBatch();

	public void setWeightInitializer(WeightInitializer win);

	public void setWeightUpdaterType(WeightUpdaterType wut);

	/**
	 * @return The name of this component.
	 */
	@Override
	public String toString();

	public void toString(NetworkStringBuilder sb);

	public String toString(String show);

	public void unbuild();

	public void updateEligibilities();

	public void updateResponsibilities();

	public void updateWeights();
}
