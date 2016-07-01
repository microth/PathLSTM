package dmonner.xlbp.connection;

import java.util.Arrays;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdater;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.layer.WeightReceiverLayer;
import dmonner.xlbp.layer.WeightSenderLayer;
import dmonner.xlbp.util.MatrixTools;

public class AdjacencyListConnection extends LayerConnection
{
	private static final long serialVersionUID = 1L;

	private WeightInitializer win;
	private WeightUpdaterType wut;
	private WeightUpdater updater;
	private float[][] w;
	private float[][] e;
	private float[] in;
	private int[][] c;
	private int[][] r;
	private int[] n;
	private int nw;
	private boolean cleared;
	private boolean overwrite;

	public AdjacencyListConnection(final AdjacencyListConnection that, final NetworkCopier copier)
	{
		super(that, copier);

		this.win = that.win;
		this.wut = that.wut;
		this.updater = that.updater != null ? wut.make(this) : null;
		this.cleared = copier.copyState() ? that.cleared : true;
		this.overwrite = copier.copyState() ? that.overwrite : true;

		if(that.built)
		{
			if(copier.copyState())
				this.in = MatrixTools.copy(that.in);
			else
				this.in = MatrixTools.empty(that.in);

			if(copier.copyWeights())
			{
				this.w = MatrixTools.copy(that.w);
				this.e = MatrixTools.copy(that.e);
				this.c = MatrixTools.copy(that.c);
				this.n = MatrixTools.copy(that.n);
				this.nw = that.nw;
			}
			else
			{
				initializeAlphas(updater);
				initializeWeights(win);
			}
		}
	}

	public AdjacencyListConnection(final String name, final WeightReceiverLayer to,
			final WeightSenderLayer from)
	{
		super(name, to, from);
		cleared = true;
		overwrite = true;
	}

	public AdjacencyListConnection(final WeightReceiverLayer to, final WeightSenderLayer from)
	{
		super(to, from);
		cleared = true;
		overwrite = true;
	}

	@Override
	public void activateTest()
	{
		final int toSize = to.size();
		final float[] y = to.getActivations();
		final float[] x = from.getActivations();

		for(int j = 0; j < toSize; j++)
		{
			float sum = 0F;
			final float[] wj = w[j];
			final int[] cj = c[j];
			final int nj = n[j];

			for(int i = 0; i < nj; i++)
			{
				final float input = x[cj[i]];
				sum += wj[i] * input;
			}

			y[j] = sum;
		}
	}

	@Override
	public void activateTrain()
	{
		cleared = false;
		System.arraycopy(from.getActivations(), 0, in, 0, from.size());
		activateTest();
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			if(win == null)
				throw new IllegalStateException("Missing a WeightInitializer in " + name);

			if(wut == null)
				throw new IllegalStateException("Missing a WeightUpdaterType in " + name);

			in = new float[from.size()];
			updater = wut.make(this);
			initializeWeights(win);
			initializeAlphas(updater);

			built = true;
		}
	}

	@Override
	public void clear()
	{
		// Arrays.fill(in, 0F);
		cleared = true;
		overwrite = true;
	}

	@Override
	public AdjacencyListConnection copy(final NetworkCopier copier)
	{
		return new AdjacencyListConnection(this, copier);
	}

	@Override
	public float[] getCachedInput()
	{
		return in;
	}

	@Override
	public float getWeight(final int j, final int i)
	{
		// TODO: test
		return w[j][r[j][i]];
	}

	@Override
	public void initializeAlphas(final WeightUpdater wu)
	{
		wu.initialize(to.size(), from.size());
	}

	@Override
	public void initializeWeights(final WeightInitializer wi)
	{
		nw = 0;
		final int toSize = to.size();
		final int fromSize = from.size();
		w = new float[toSize][fromSize];
		e = new float[toSize][fromSize];
		c = new int[toSize][fromSize];
		r = new int[toSize][fromSize];
		n = new int[toSize];

		for(int j = 0; j < toSize; j++)
		{
			int nj = 0;
			final int[] cj = c[j];
			final int[] rj = r[j];
			final float[] wj = w[j];

			for(int i = 0; i < fromSize; i++)
			{
				if(wi.newWeight(j, i))
				{
					cj[nj] = i;
					rj[i] = nj;
					wj[nj] = wi.randomWeight(j, i);
					nj++;
				}
			}

			n[j] = nj;
			nw += nj;
		}
	}

	@Override
	public int nWeights()
	{
		return nw;
	}

	@Override
	public void processBatch()
	{
		updater.processBatch();
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		this.win = win;
	}

	@Override
	public void setWeightUpdater(final WeightUpdaterType wut)
	{
		this.wut = wut;
	}

	@Override
	public float[][] toEligibilitiesMatrix()
	{
		final float[][] m = new float[to.size()][from.size()];
		if(!cleared)
		{
			for(int j = 0; j < to.size(); j++)
			{
				for(int k = 0; k < n[j]; k++)
				{
					final int i = c[j][k];
					m[j][i] = to.getDownstreamCopyLayer() != null ? e[j][k] : in[i];
				}
			}
		}
		return m;
	}

	@Override
	public float[][] toMatrix()
	{
		final float[][] m = new float[to.size()][from.size()];
		for(int i = 0; i < to.size(); i++)
		{
			for(int k = 0; k < n[i]; k++)
			{
				final int j = c[i][k];
				m[i][j] = w[i][k];
			}
		}
		return m;
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		if(sb.showName())
		{
			sb.indent();
			sb.append(name);
			sb.append(" : ");
			sb.append(this.getClass().getSimpleName());
			sb.appendln();
		}

		sb.pushIndent();

		if(sb.showExtra())
		{
			sb.appendln("Cached Inputs:");
			sb.pushIndent();
			if(cleared)
				sb.appendln("Empty");
			else
				sb.appendln(MatrixTools.toString(in));
			sb.popIndent();
		}

		if(sb.showWeights())
		{
			sb.appendln("Weights:");
			sb.pushIndent();
			sb.appendln(MatrixTools.toString(w));
			sb.popIndent();
		}

		if(sb.showEligibilities())
		{
			sb.appendln("Eligibilities:");
			sb.pushIndent();
			if(overwrite)
				sb.appendln("Empty");
			else
				sb.appendln(MatrixTools.toString(e));
			sb.popIndent();
		}

		updater.toString(sb);

		sb.popIndent();
	}

	@Override
	public void updateEligibilities(final Responsibilities resp, final Responsibilities prev)
	{
		final int toSize = to.size();
		final float[] d = resp.get();

		if(overwrite)
		{
			for(int j = 0; j < toSize; j++)
			{
				final float[] ej = e[j];
				final float dj = d[j];
				final int nj = n[j];
				final int[] cj = c[j];

				for(int i = 0; i < nj; i++)
					ej[i] = dj * in[cj[i]];
			}

			overwrite = false;
		}
		else
		{
			final float[] p = prev.get();

			for(int j = 0; j < toSize; j++)
			{
				final float[] ej = e[j];
				final float dj = d[j];
				final float pj = p[j];
				final int nj = n[j];
				final int[] cj = c[j];

				for(int i = 0; i < nj; i++)
					ej[i] = ej[i] * pj + dj * in[cj[i]];
			}
		}
	}

	@Override
	public void updateResponsibilities()
	{
		final float[] toD = getToLayerResponsibilities();

		// if the to layer has empty responsibility, nothing to do.
		if(toD == null)
			return;

		// zero out responsibilities before += below.
		final float[] fromD = getFromLayerResponsibilities();
		Arrays.fill(fromD, 0F);

		final int toSize = to.size();

		for(int k = 0; k < toSize; k++)
		{
			final float[] wk = w[k];
			final int[] ck = c[k];
			final int nk = n[k];

			for(int j = 0; j < nk; j++)
				fromD[ck[j]] += wk[j] * toD[k];
		}
	}

	@Override
	public void updateWeights(final float[][] dw)
	{
		final int toSize = to.size();

		for(int j = 0; j < toSize; j++)
		{
			final float[] wj = w[j];
			final float[] dwj = dw[j];
			final int nj = n[j];

			for(int i = 0; i < nj; i++)
				wj[i] += dwj[i];
		}
	}

	@Override
	public void updateWeightsFromEligibilities(final Responsibilities copyresp)
	{
		final int toSize = to.size();
		final float[] d = copyresp.get();

		updater.updateFromEligibilities(e, d);

		for(int j = 0; j < toSize; j++)
		{
			final float[] ej = e[j];
			final float dj = d[j];
			final float[] wj = w[j];
			final int nj = n[j];

			for(int i = 0; i < nj; i++)
				wj[i] += updater.getUpdate(j, i, ej[i] * dj);
		}
	}

	@Override
	public void updateWeightsFromInputs(final Responsibilities resp)
	{
		final int toSize = to.size();
		final float[] d = resp.get();

		updater.updateFromInputs(in, d);

		for(int j = 0; j < toSize; j++)
		{
			final float dj = d[j];
			final float[] wj = w[j];
			final int nj = n[j];
			final int[] cj = c[j];

			for(int i = 0; i < nj; i++)
				wj[i] += updater.getUpdate(j, i, in[cj[i]] * dj);
		}
	}
}
