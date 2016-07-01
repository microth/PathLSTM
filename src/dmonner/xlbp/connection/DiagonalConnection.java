package dmonner.xlbp.connection;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.UniformWeightInitializer;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdater;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.layer.WeightReceiverLayer;
import dmonner.xlbp.layer.WeightSenderLayer;
import dmonner.xlbp.util.MatrixTools;

public class DiagonalConnection extends LayerConnection
{
	private static final long serialVersionUID = 1L;

	private WeightInitializer win;
	private WeightUpdaterType wut;
	private WeightUpdater updater;
	private float[] w;
	private float[] e;
	private float[] in;
	private boolean cleared;
	private boolean overwrite;
	private boolean fullOnly;

	public DiagonalConnection(final DiagonalConnection that, final NetworkCopier copier)
	{
		super(that, copier);

		this.win = that.win;
		this.wut = that.wut;
		this.updater = that.updater != null ? wut.make(this) : null;
		this.cleared = copier.copyState() ? that.cleared : true;
		this.overwrite = copier.copyState() ? that.overwrite : true;
		this.fullOnly = copier.copyState() ? that.fullOnly : true;

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
			}
			else
			{
				initializeAlphas(updater);
				initializeWeights(win);
			}
		}
	}

	public DiagonalConnection(final String name, final WeightReceiverLayer to,
			final WeightSenderLayer from)
	{
		super(name, to, from);

		if(from.size() != to.size())
			throw new IllegalArgumentException(
					"Sending and receiving layers of a DiagonalConnection must be the same size: "
							+ from.size() + " != " + to.size());

		this.win = new UniformWeightInitializer();
		cleared = true;
		overwrite = true;
		fullOnly = true;
	}

	public DiagonalConnection(final WeightReceiverLayer to, final WeightSenderLayer from)
	{
		this(from.getName() + "DiagonalTo" + to.getName(), to, from);
		cleared = true;
		fullOnly = true;
	}

	@Override
	public void activateTest()
	{
		final int toSize = to.size();
		final float[] y = to.getActivations();
		final float[] x = from.getActivations();

		for(int j = 0; j < toSize; j++)
			y[j] = w[j] * x[j];
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
		cleared = true;
		overwrite = true;
	}

	@Override
	public DiagonalConnection copy(final NetworkCopier copier)
	{
		return new DiagonalConnection(this, copier);
	}

	public float[] get()
	{
		return w;
	}

	@Override
	public float[] getCachedInput()
	{
		return in;
	}

	@Override
	public float getWeight(final int j, final int i)
	{
		return w[j];
	}

	@Override
	public void initializeAlphas(final WeightUpdater lrs)
	{
		lrs.initialize(to.size());
	}

	@Override
	public void initializeWeights(final WeightInitializer wi)
	{
		final int toSize = to.size();
		w = new float[toSize];
		e = new float[toSize];

		for(int j = 0; j < toSize; j++)
			w[j] = wi.randomWeight(j, j);
	}

	@Override
	public int nWeights()
	{
		return to.size();
	}

	@Override
	public void processBatch()
	{
		updater.processBatch();
	}

	public void set(final float[] w)
	{
		if(w.length != to.size())
			throw new IllegalArgumentException("Incompatible number of weights: " + to.size() + " != "
					+ w.length);

		System.arraycopy(w, 0, this.w, 0, w.length);
	}

	public void setFullOnly(final boolean fullOnly)
	{
		this.fullOnly = fullOnly;
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		if(fullOnly && !win.fullConnectivity())
			System.out.println("WARNING: Cannot use a DiagonalConnection with anything less than full "
					+ "connectivity; ignoring new WeightInitializer.");
		else
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
		final float[][] m = new float[to.size()][to.size()];
		if(!cleared)
			for(int i = 0; i < to.size(); i++)
				m[i][i] = to.getDownstreamCopyLayer() != null ? e[i] : in[i];
		return m;
	}

	@Override
	public float[][] toMatrix()
	{
		final float[][] m = new float[to.size()][to.size()];
		for(int i = 0; i < to.size(); i++)
			m[i][i] = w[i];
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
				e[j] = d[j] * in[j];

			overwrite = false;
		}
		else
		{
			final float[] p = prev.get();

			for(int j = 0; j < toSize; j++)
				e[j] = e[j] * p[j] + d[j] * in[j];
		}
	}

	@Override
	public void updateResponsibilities()
	{
		final float[] toD = getToLayerResponsibilities();

		// if the to layer has empty responsibility, nothing to do.
		if(toD == null)
			return;

		final float[] fromD = getFromLayerResponsibilities();

		final int toSize = to.size();

		for(int j = 0; j < toSize; j++)
			fromD[j] = w[j] * toD[j];
	}

	@Override
	public void updateWeights(final float[][] dw)
	{
		final int toSize = to.size();
		final float[] dwj = dw[0];

		for(int i = 0; i < toSize; i++)
			w[i] += dwj[i];
	}

	@Override
	public void updateWeightsFromEligibilities(final Responsibilities copyresp)
	{
		final int toSize = to.size();
		final float[] d = copyresp.get();

		updater.updateFromVector(e, d);

		for(int j = 0; j < toSize; j++)
			w[j] += updater.getUpdate(j, e[j] * d[j]);
	}

	@Override
	public void updateWeightsFromInputs(final Responsibilities resp)
	{
		final int toSize = to.size();
		final float[] d = resp.get();

		updater.updateFromVector(in, d);

		for(int j = 0; j < toSize; j++)
			w[j] += updater.getUpdate(j, in[j] * d[j]);
	}
}
