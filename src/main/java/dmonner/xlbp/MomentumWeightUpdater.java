package dmonner.xlbp;

import dmonner.xlbp.connection.Connection;
import dmonner.xlbp.util.MatrixTools;

public class MomentumWeightUpdater implements WeightUpdater
{
	public static final long serialVersionUID = 1L;

	private final Connection parent;
	private float[][] trace;
	private final float a;
	private final float m;
	private int to;
	private int from;

	public MomentumWeightUpdater(final Connection parent)
	{
		this(parent, 0.1F, 0.9F);
	}

	public MomentumWeightUpdater(final Connection parent, final float a, final float m)
	{
		this.parent = parent;
		this.a = a;
		this.m = m;
	}

	@Override
	public Connection getConnection()
	{
		return parent;
	}

	@Override
	public float getUpdate(final int i, final float dw)
	{
		return a * trace[0][i];
	}

	@Override
	public float getUpdate(final int j, final int i, final float dw)
	{
		return a * trace[j][i];
	}

	@Override
	public void initialize(final int size)
	{
		this.to = 1;
		this.from = size;

		trace = new float[to][from];
	}

	@Override
	public void initialize(final int to, final int from)
	{
		this.to = to;
		this.from = from;

		trace = new float[to][from];
	}

	@Override
	public void processBatch()
	{
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		if(sb.showLearningRates())
			sb.appendln("Learning Rates: " + a);

		if(sb.showExtra())
		{
			sb.appendln("Traces:");
			sb.pushIndent();
			sb.appendln(MatrixTools.toString(trace));
			sb.popIndent();
		}
	}

	@Override
	public void updateFromBiases(final float[] d)
	{
		final float[] trj = trace[0];
		for(int i = 0; i < from; i++)
			updateSingle(trj, d[i], i);
	}

	@Override
	public void updateFromEligibilities(final float[][] e, final float[] d)
	{
		for(int j = 0; j < to; j++)
		{
			final float[] ej = e[j];
			final float dj = d[j];
			final float[] trj = trace[j];
			for(int i = 0; i < from; i++)
				updateSingle(trj, ej[i] * dj, i);
		}
	}

	@Override
	public void updateFromInputs(final float[] in, final float[] d)
	{
		for(int j = 0; j < to; j++)
		{
			final float dj = d[j];
			final float[] trj = trace[j];
			for(int i = 0; i < from; i++)
				updateSingle(trj, in[i] * dj, i);
		}
	}

	@Override
	public void updateFromVector(final float[] v, final float[] d)
	{
		final float[] trj = trace[0];
		for(int i = 0; i < from; i++)
			updateSingle(trj, v[i] * d[i], i);
	}

	private void updateSingle(final float[] trj, final float dwji, final int i)
	{
		trj[i] = trj[i] * m + dwji;
	}
}
