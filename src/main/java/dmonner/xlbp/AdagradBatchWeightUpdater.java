package dmonner.xlbp;

import java.util.Arrays;

import dmonner.xlbp.connection.Connection;

public class AdagradBatchWeightUpdater implements WeightUpdater
{
	public static final long serialVersionUID = 1L;

	private final float a;
	private final Connection parent;
	private float[][] wc;
	private float[][] sumsqr_prv_wc;
	private int to;
	private int from;

	public AdagradBatchWeightUpdater(final Connection parent, final float a)
	{
		this.parent = parent;
		this.a = a;
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
		return 0F;
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
		sumsqr_prv_wc = new float[to][from];
	}

	@Override
	public void processBatch()
	{	
		for(int j = 0; j < to; j++)
		{
			final float[] wcj = wc[j];
			final float[] sumsqr_prv_wcj = sumsqr_prv_wc[j];
			for(int i = 0; i < from; i++) {
				wcj[i] /= 250F; // divide by batch size
				if(sumsqr_prv_wcj[i]>0.0) // scale by previous updates, if able
					wcj[i] = wcj[i] / (float)Math.sqrt(sumsqr_prv_wcj[i]);

				sumsqr_prv_wcj[i] += (wcj[i]*wcj[i]); // update sum of squared previous updates 
				wcj[i] *= a; // scale by the learning rate
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
			sb.appendln("Learning Rate:" + a);
	}

	@Override
	public void updateFromBiases(final float[] d)
	{
		final float[] wcj = wc[0];
		for(int i = 0; i < from; i++)
			wcj[i] += d[i];
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
			for(int i = 0; i < from; i++)
				wcj[i] += in[i] * dj;
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
