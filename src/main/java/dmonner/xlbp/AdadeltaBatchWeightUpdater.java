package dmonner.xlbp;

import java.util.Arrays;

import dmonner.xlbp.connection.Connection;

public class AdadeltaBatchWeightUpdater implements WeightUpdater
{
	public static final long serialVersionUID = 1L;

	private final float beta;
	private final float eps;
	private final Connection parent;
	private float[][] wc;
	private double[][] squared_gradients;
	private double[][] squared_deltas;
	private int to;
	private int from;
	private int batchsize;

	public AdadeltaBatchWeightUpdater(final Connection parent, final float beta, final float eps, final int batchsize)
	{
		this.parent = parent;
		this.beta = beta;
		this.eps = eps;
		this.batchsize = batchsize;
	}

	@Override
	public Connection getConnection()
	{
		return parent;
	}

	@Override
	public float getUpdate(final int i, final float dw)
	{
		
		return 0F;
	}

	@Override
	public float getUpdate(final int j, final int i, final float dw)
	{		
		/*final float[] wcj = wc[j];
		final double[] sumsqr_prv_gj = squared_gradients[j];
		final double[] sumsqr_prv_dj = squared_deltas[j];
		
		// update running sum of squared gradients
		sumsqr_prv_gj[i] = beta * sumsqr_prv_gj[i] + (1-beta) * (wcj[i]*wcj[i]);
		
		wcj[i] = (float)(Math.sqrt(sumsqr_prv_dj[i] + eps) / Math.sqrt(sumsqr_prv_gj[i] + eps) * wcj[i]);
		float retval = wcj[i];
		
		// update running sum of squared weight updates
		sumsqr_prv_dj[i] = beta * sumsqr_prv_dj[i] + (1-beta) * (wcj[i]*wcj[i]);*/
		
		float retval = 0F;
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
		this.to = to;
		this.from = from;
		wc = new float[to][from];
		squared_gradients = new double[to][from];
		squared_deltas = new double[to][from];
	}

	@Override
	public void processBatch()
	{	
		for(int j = 0; j < to; j++)
		{
			final float[] wcj = wc[j];
			final double[] sumsqr_prv_gj = squared_gradients[j];
			final double[] sumsqr_prv_dj = squared_deltas[j];
			for(int i = 0; i < from; i++) {
				//if(i==0 && j==0) { System.err.println( parent.getName() + "\t" + wcj[i]); }

				
				//if(i==0 && j==0) System.err.print(wcj[i]+"\t");
				wcj[i] /= (float)batchsize;
				sumsqr_prv_gj[i] = beta * sumsqr_prv_gj[i] + (1-beta) * (wcj[i]*wcj[i]);
				wcj[i] = (float)(Math.sqrt(sumsqr_prv_dj[i] + eps) / Math.sqrt(sumsqr_prv_gj[i] + eps) * wcj[i]);
				sumsqr_prv_dj[i] = beta * sumsqr_prv_dj[i] + (1-beta) * (wcj[i]*wcj[i]);
				
				//if(i==0 && j==0) System.err.println(wcj[i]);
			}
		}

		// update the weights in the parent connection
		parent.updateWeights(wc);

		// zero out the dw array to start the next batch
		for(int j = 0; j < wc.length; j++)
			Arrays.fill(wc[j], 0F);
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
		final float[] wcj = wc[0];
		for(int i = 0; i < from; i++) {
			wcj[i] += d[i];
		}
	}

	@Override
	public void updateFromEligibilities(final float[][] e, final float[] d)
	{
		for(int j = 0; j < to; j++)
		{
			final float[] ej = e[j];
			final float dj = d[j];
			final float[] wcj = wc[j];
			for(int i = 0; i < from; i++)
				wcj[i] += ej[i] * dj;
		}
	}

	@Override
	public void updateFromInputs(final float[] in, final float[] d)
	{
		for(int j = 0; j < to; j++)
		{
			final float dj = d[j];
			final float[] wcj = wc[j];
			for(int i = 0; i < from; i++) {
				wcj[i] += in[i] * dj;
			}
		}
	}

	@Override
	public void updateFromVector(final float[] v, final float[] d)
	{
		final float[] wcj = wc[0];
		for(int i = 0; i < from; i++)
			wcj[i] += v[i] * d[i];
	}
}
