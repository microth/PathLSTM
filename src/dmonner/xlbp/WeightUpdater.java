package dmonner.xlbp;

import java.io.Serializable;

import dmonner.xlbp.connection.Connection;

public interface WeightUpdater extends Serializable
{
	public Connection getConnection();

	public float getUpdate(int i, float dw);

	public float getUpdate(int j, int i, float dw);

	public void initialize(int size);

	public void initialize(int to, int from);

	public void processBatch();

	public void toString(NetworkStringBuilder sb);

	public void updateFromBiases(float[] d);

	public void updateFromEligibilities(float[][] e, float[] d);

	public void updateFromInputs(float[] in, float[] d);

	public void updateFromVector(float[] v, float[] d);
}
