package dmonner.xlbp;

import java.util.Arrays;

import dmonner.xlbp.connection.Connection;

public class NadamBatchWeightUpdater implements WeightUpdater
{
	public static final long serialVersionUID = 1L;

	private final float eps;
	private final float v;
	private final float alpha;
	private final Connection parent;
	private float[][] wc;
	private double[][] moments;
	private double[][] norms;
	private int to;
	private int from;
	
	private float t;
	private float mu;
	private float running_mu;
	
	public NadamBatchWeightUpdater(final Connection parent, final float alpha)
	{
		this.parent = parent;
		this.alpha = alpha;
		this.mu = 0.99F; // momentum
		this.running_mu = 1F;
		this.t = 1F;
		this.eps = 0.00000001F;
		this.v = 0.999F; // beta
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
		moments = new double[to][from];
		norms = new double[to][from];
	}

	@Override
	public void processBatch()
	{	
		// momentum update schedule
		double mu_t   = mu * (1- (Math.pow(0.96,t/2500)/2));
		double mu_tp1 = mu * (1- (Math.pow(0.96,(t+1)/2500)/2));
		running_mu *= mu_t;
		
		for(int j = 0; j < to; j++)
		{
			final float[] wcj = wc[j];
			final double[] momentj = moments[j];
			final double[] momentj_cap = new double[momentj.length];
			final double[] normj = norms[j];
			final double[] normj_cap = new double[normj.length];
			for(int i = 0; i < from; i++) {		
				if(wcj[i]==0.0) continue;
				
				momentj[i] = mu*momentj[i] + (1-mu)*wcj[i]; // correct?				
				momentj_cap[i] = momentj[i] / (1-(running_mu * mu_tp1)); // correct?
				
				normj[i] = v * normj[i] + (1-v) * (wcj[i]*wcj[i]);
				normj_cap[i] = normj[i] / (1-Math.pow(v, t));

				
				wcj[i] /= (1-running_mu);
				double update = (1-mu_t) * wcj[i] + mu_tp1 * momentj_cap[i];
				
				wcj[i] = (float)(alpha * update / (Math.sqrt(normj_cap[i]) + eps));
				
				//if(i==0 && j==0) System.err.println(parent.getName() + " update: " + wcj[i]);
			}
		}
		t++;

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
