package dmonner.xlbp;

import dmonner.xlbp.connection.Connection;

public class BasicWeightUpdater implements WeightUpdater
{ 
	public static final long serialVersionUID = 1L;

	public static float HALF = 1.0F;
	
	private final Connection parent;
	private final float a;

	public BasicWeightUpdater(final Connection parent)
	{
		this(parent, 0.1F);
	}

	public BasicWeightUpdater(final Connection parent, final float a)
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
		return (a/HALF) * dw;
	}

	@Override
	public float getUpdate(final int j, final int i, final float dw)
	{
		return (a/HALF) * dw;
	}

	@Override
	public void initialize(final int size)
	{
	}

	@Override
	public void initialize(final int to, final int from)
	{
	}

	@Override
	public void processBatch()
	{
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
