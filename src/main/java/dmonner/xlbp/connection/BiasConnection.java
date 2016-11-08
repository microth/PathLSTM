package dmonner.xlbp.connection;

import dmonner.xlbp.AdadeltaBatchWeightUpdater;
import dmonner.xlbp.BatchWeightUpdater;
import dmonner.xlbp.NadamBatchWeightUpdater;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdater;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.layer.BiasLayer;
import dmonner.xlbp.util.MatrixTools;

public class BiasConnection implements Connection
{
	private static final long serialVersionUID = 1L;

	private final String name;
	private final BiasLayer to;
	private WeightInitializer win;
	private WeightUpdaterType wut;
	private WeightUpdater updater;
	private float[] w;
	private float[] e;
	private boolean cleared;
	private boolean overwrite;
	private boolean built;

	public BiasConnection(final BiasConnection that, final NetworkCopier copier)
	{
		this.name = copier.getCopyNameFrom(that);
		this.to = copier.getCopyOf(that.to);
		this.win = that.win;
		this.wut = that.wut;
		this.updater = that.updater != null ? wut.make(this) : null;
		this.built = that.built;

		this.cleared = copier.copyState() ? that.cleared : true;
		this.overwrite = copier.copyState() ? that.overwrite : true;
		this.w = copier.copyWeights() ? MatrixTools.copy(that.w) : MatrixTools.empty(that.w);
		this.e = copier.copyWeights() ? MatrixTools.copy(that.e) : MatrixTools.empty(that.e);
	}

	public BiasConnection(final BiasLayer to)
	{
		this(to.getName(), to);
	}

	public BiasConnection(final String name, final BiasLayer to)
	{
		this.name = name;
		this.to = to;
		this.cleared = true;
		this.overwrite = true;
	}

	@Override
	public void activateTest()
	{
		// Nothing to do; BiasLayer's activations are aliased to w.
	}

	@Override
	public void activateTrain()
	{
		cleared = false;
	}

	public void alias(final float[] w)
	{
		if(w.length != to.size())
			throw new IllegalArgumentException("Incompatible number of weights: " + to.size() + " != "
					+ w.length);

		this.w = w;
	}

	@Override
	public void build()
	{
		if(!built)
		{
			if(win == null)
				throw new IllegalStateException("Missing a WeightInitializer in " + name);

			if(wut == null)
				throw new IllegalStateException("Missing a WeightUpdaterType in " + name);

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
	public BiasConnection copy(final NetworkCopier copier)
	{
		return new BiasConnection(this, copier);
	}

	public float[] get()
	{
		return w;
	}

	@Override
	public String getName()
	{
		return name;
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
	public int nWeightsPossible()
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
		final float[][] m = new float[to.size()][to.size()];
		if(!cleared)
			for(int i = 0; i < to.size(); i++)
				m[i][i] = to.getDownstreamCopyLayer() != null ? e[i] : 1F;
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

		if(sb.showWeights())
		{
			sb.appendln("Biases:");
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
	public String toString(final String show)
	{
		final NetworkStringBuilder sb = new NetworkStringBuilder(show);
		toString(sb);
		return sb.toString();
	}

	@Override
	public void unbuild()
	{
		built = false;
	}

	@Override
	public void updateEligibilities(final Responsibilities resp, final Responsibilities prev)
	{
		final int toSize = to.size();
		final float[] d = resp.get();

		if(overwrite)
		{
			System.arraycopy(d, 0, e, 0, toSize);

			overwrite = false;
		}
		else
		{
			final float[] p = prev.get();

			for(int j = 0; j < toSize; j++)
				e[j] = e[j] * p[j] + d[j];
		}
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

		if(updater.getClass()==AdadeltaBatchWeightUpdater.class || 
		   updater.getClass()==BatchWeightUpdater.class ||
		   updater.getClass()==NadamBatchWeightUpdater.class) {
			updater.updateFromVector(e, d);		
			return;
		}
		
		for(int j = 0; j < toSize; j++) {
			w[j] += updater.getUpdate(j, e[j] * d[j]);
		}
	}

	@Override
	public void updateWeightsFromInputs(final Responsibilities resp)
	{
		final int toSize = to.size();
		final float[] d = resp.get();

		if(updater.getClass()==AdadeltaBatchWeightUpdater.class || 
		   updater.getClass()==BatchWeightUpdater.class ||
		   updater.getClass()==NadamBatchWeightUpdater.class) {
			updater.updateFromBiases(d);
			return;
		}

		for(int j = 0; j < toSize; j++) {
			
			w[j] += updater.getUpdate(j, d[j]);
		}
	}
}
