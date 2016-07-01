package dmonner.xlbp;

import dmonner.xlbp.connection.Connection;

public class DecayWeightUpdater implements WeightUpdater
{
	public static final long serialVersionUID = 1L;

	private final Connection parent;
	private final float a;
	private final float b;

	public DecayWeightUpdater(final Connection parent)
	{
		this(parent, 0.1F, 0.001F);
	}

	public DecayWeightUpdater(final Connection parent, final float a, final float b)
	{
		this.parent = parent;
		this.a = a;
		this.b = b;
	}

	@Override
	public Connection getConnection()
	{
		return parent;
	}

	@Override
	public float getUpdate(final int j, final float dw)
	{
		return a * (dw - b * parent.getWeight(j, 0));
	}

	@Override
	public float getUpdate(final int j, final int i, final float dw)
	{
		return a * (dw - b * parent.getWeight(j, i));
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
		{
			sb.appendln("Learning Rate:" + a);
			sb.appendln("Weight Decay Rate:" + b);
		}
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
