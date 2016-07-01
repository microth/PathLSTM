package dmonner.xlbp;

import java.util.Arrays;

import dmonner.xlbp.connection.Connection;

public class AdadeltaBasicWeightUpdater implements WeightUpdater
{
	public static final long serialVersionUID = 1L;

	private final float beta;
	private final float eps;
	private final Connection parent;
	private double[][] squared_gradients;
	private double[][] squared_deltas;

	public AdadeltaBasicWeightUpdater(final Connection parent, final float beta, final float eps)
	{
		this.parent = parent;
		this.beta = beta;
		this.eps = eps;
	}

	@Override
	public Connection getConnection()
	{
		return parent;
	}

	@Override
	public float getUpdate(final int i, final float dw)
	{
		squared_gradients[i][0] = beta * squared_gradients[i][0] + (1-beta) * (dw*dw);
		float retval = (float)(Math.sqrt(squared_deltas[i][0] + eps) / Math.sqrt(squared_gradients[i][0] + eps) * dw);
		squared_deltas[i][0] = beta * squared_deltas[i][0] + (1-beta) * (retval*retval);
		return retval;	
	}

	@Override
	public float getUpdate(final int j, final int i, final float dw)
	{			
		squared_gradients[i][j] = beta * squared_gradients[i][j] + (1-beta) * (dw*dw);
		float retval = (float)(Math.sqrt(squared_deltas[i][j] + eps) / Math.sqrt(squared_gradients[i][j] + eps) * dw);
		squared_deltas[i][j] = beta * squared_deltas[i][j] + (1-beta) * (retval*retval);
		return retval;
	}

	@Override
	public void initialize(final int size)
	{
		// put 1D array in the 2nd dimension, for array access efficiency
		initialize(1, size);
	}

	@Override
	public void initialize(final int to, final int from)
	{
		squared_gradients = new double[from][to];
		squared_deltas = new double[from][to];
	}

	@Override
	public void processBatch()
	{	
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		if(sb.showLearningRates())
			sb.appendln("TODO");
	}

	@Override
	public void updateFromBiases(final float[] d)
	{
	}

	@Override
	public void updateFromEligibilities(final float[][] e, final float[] d)
	{
	}

	@Override
	public void updateFromInputs(final float[] in, final float[] d)
	{
	}

	@Override
	public void updateFromVector(final float[] v, final float[] d)
	{
	}
}
