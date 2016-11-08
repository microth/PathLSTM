package dmonner.xlbp;

import dmonner.xlbp.connection.Connection;

public class AdamBasicWeightUpdater implements WeightUpdater
{
	public static final long serialVersionUID = 1L;

	private final float beta;
	private final float beta2;
	private final float alpha;
	private final float eps;
	private final Connection parent;
	private double[][] squared_deltas;
	private double[][] m;
	private double[][] v;
	public static int t;
	
	private double[] betas;
	private double[] betas2;
	
	public AdamBasicWeightUpdater(final Connection parent, final float beta, final float beta2, final float eps, final float alpha)
	{
		this.parent = parent;
		this.beta = beta;
		this.beta2 = beta2;
		this.alpha = alpha;
		this.eps = eps;
		t = 1;		
		
	}

	@Override
	public Connection getConnection()
	{
		return parent;
	}

	@Override
	public float getUpdate(final int i, final float dw)
	{
		m[i][0] = beta * m[i][0] + (1-beta) * dw;
		v[i][0] = beta2* v[i][0] + (1-beta2) * (dw*dw); 
		
		return (float)(alpha * (m[i][0]/(double)(1-Math.pow(beta,t))) / (Math.sqrt(v[i][0]/(double)(1-Math.pow(beta2,t)))+eps));	
	}

	@Override
	public float getUpdate(final int j, final int i, final float dw)
	{	
		//if(i==3 && j==3) System.err.println(parent.getName() + "\t" + dw);	
		m[i][j] = beta * m[i][j] + (1-beta) * dw;
		v[i][j] = beta2* v[i][j] + (1-beta2) * (dw*dw);		
		return (float)(alpha * (m[i][j]/(double)(1-Math.pow(beta,t))) / (Math.sqrt(v[i][j]/(double)(1-Math.pow(beta2,t)))+eps));	
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
		m = new double[from][to];
		v = new double[from][to];
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
