package dmonner.xlbp;

import java.io.Serializable;

public interface WeightInitializer extends Serializable
{
	public boolean fullConnectivity();

	public boolean newWeight(int j, int i);

	public float randomWeight(int j, int i);
}
