package dmonner.xlbp;

import java.util.Arrays;

import dmonner.xlbp.connection.Connection;
import dmonner.xlbp.util.MatrixTools;

public class ResilientEligibilitiesWeightUpdater implements WeightUpdater
{
	public static final long serialVersionUID = 1L;

	private final float eta_plus = 1.2F;
	private final float eta_minus = 0.5F;
	private final float a_max = 1e2F;
	private final float a_min = 1e-10F;
	private final float a_init = 1e-3F;

	private final Connection parent;
	private float[][] pdw;
	private float[][] a;
	private float[][] dw;
	private float[][] ew;
	private float[][] wc;
	private int to;
	private int from;

	public ResilientEligibilitiesWeightUpdater(final Connection parent)
	{
		this.parent = parent;
	}

	public float[][] get()
	{
		return a;
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

		pdw = new float[to][from];
		dw = new float[to][from];
		ew = new float[to][from];
		a = new float[to][from];
		wc = new float[to][from];

		for(int j = 0; j < to; j++)
			Arrays.fill(a[j], a_init);
	}

	@Override
	public void processBatch()
	{
		// calculate the weight changes for this step
		for(int j = 0; j < to; j++)
		{
			final float[] dwj = dw[j];
			final float[] ewj = ew[j];
			final float[] pdwj = pdw[j];
			final float[] aj = a[j];
			final float[] wcj = wc[j];

			for(int i = 0; i < from; i++)
			{
				final float aji = aj[i];
				final float dwji = dwj[i];
				final float ewji = Math.abs(ewj[i]);
				final float prod = pdwj[i] * dwji;

				if(prod > 0F)
				{
					aj[i] = Math.min(aj[i] * eta_plus, a_max);
					pdwj[i] = dwji;
					wcj[i] = aji * sign(dwji) * ewji;
				}
				else if(prod < 0F)
				{
					aj[i] = Math.max(aj[i] * eta_minus, a_min);
					pdwj[i] = 0F;
					wcj[i] = 0F;
				}
				else
				{
					pdwj[i] = dwji;
					wcj[i] = aji * sign(dwji) * ewji;
				}
			}
		}

		// update the weights in the parent connection
		parent.updateWeights(wc);

		// zero out the dw array to start the next batch; wc array will be overwritten
		for(int j = 0; j < dw.length; j++)
			Arrays.fill(dw[j], 0F);
	}

	private float sign(final float x)
	{
		if(x > 0)
			return 1F;
		else if(x < 0)
			return -1F;
		else
			return 0F;
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		if(sb.showLearningRates())
		{
			sb.appendln("Learning Rates:");
			sb.pushIndent();
			sb.appendln(MatrixTools.toString(a));
			sb.popIndent();
		}

		if(sb.showExtra())
		{
			sb.appendln("Previous Weight Deltas:");
			sb.pushIndent();
			sb.appendln(MatrixTools.toString(pdw));
			sb.popIndent();

			sb.appendln("Weight Deltas:");
			sb.pushIndent();
			sb.appendln(MatrixTools.toString(dw));
			sb.popIndent();
		}
	}

	@Override
	public void updateFromBiases(final float[] d)
	{
		final float[] dwj = dw[0];
		final float[] ewj = ew[0];
		for(int i = 0; i < from; i++)
		{
			dwj[i] += d[i];
			ewj[i] += 1F;
		}
	}

	@Override
	public void updateFromEligibilities(final float[][] e, final float[] d)
	{
		for(int j = 0; j < to; j++)
		{
			final float[] ej = e[j];
			final float dj = d[j];
			final float[] dwj = dw[j];
			final float[] ewj = ew[j];
			for(int i = 0; i < from; i++)
			{
				dwj[i] += ej[i] * dj;
				ewj[i] += ej[i];
			}
		}
	}

	@Override
	public void updateFromInputs(final float[] in, final float[] d)
	{
		for(int j = 0; j < to; j++)
		{
			final float dj = d[j];
			final float[] dwj = dw[j];
			final float[] ewj = ew[j];
			for(int i = 0; i < from; i++)
			{
				dwj[i] += in[i] * dj;
				ewj[i] += in[i];
			}
		}
	}

	@Override
	public void updateFromVector(final float[] v, final float[] d)
	{
		final float[] dwj = dw[0];
		final float[] ewj = ew[0];
		for(int i = 0; i < from; i++)
		{
			dwj[i] += v[i] * d[i];
			ewj[i] += v[i];
		}
	}
}
