package dmonner.xlbp.connection;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import dmonner.xlbp.AdadeltaBasicWeightUpdater;
import dmonner.xlbp.AdadeltaBatchWeightUpdater;
import dmonner.xlbp.BatchWeightUpdater;
import dmonner.xlbp.NadamBatchWeightUpdater;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdater;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.layer.WeightReceiverLayer;
import dmonner.xlbp.layer.WeightSenderLayer;
import dmonner.xlbp.util.MatrixTools;

public class AdjacencyMatrixConnection extends LayerConnection
{
	private static final long serialVersionUID = 1L;

	private WeightInitializer win;
	private WeightUpdaterType wut;
	private WeightUpdater updater;
	private float[][] w;
	private float[][] e;
	private float[] in;
	private int nw;
	private boolean cleared;
	private boolean overwrite;

	Set<Integer> responsibilities;
	
	public AdjacencyMatrixConnection(final AdjacencyMatrixConnection that, final NetworkCopier copier)
	{
		super(that, copier);

		this.win = that.win;
		this.wut = that.wut;
		this.updater = that.updater != null ? wut.make(this) : null;
		this.cleared = copier.copyState() ? that.cleared : true;
		this.overwrite = copier.copyState() ? that.overwrite : true;
		this.responsibilities = that.responsibilities;
		
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
				this.nw = that.nw;
			}
			else
			{
				initializeAlphas(updater);
				initializeWeights(win);
			}
		}
	}

	public AdjacencyMatrixConnection(final String name, final WeightReceiverLayer to,
			final WeightSenderLayer from)
	{
		super(name, to, from);
		responsibilities = new TreeSet<Integer>();

		cleared = true;
		overwrite = true;
	}

	public AdjacencyMatrixConnection(final WeightReceiverLayer to, final WeightSenderLayer from)
	{
		super(to, from);
		responsibilities = new TreeSet<Integer>();
		cleared = true;
		overwrite = true;
	}

	@Override
	public void activateTest()
	{
		final int toSize = to.size();
		final int fromSize = from.size();
		final float[] y = to.getActivations();
		final float[] x = from.getActivations();
		
		/* dense vector multiplication
		for(int j = 0; j < toSize; j++)
		{
			float sum = 0F;
			final float[] wj = w[j];

			for(int i = 0; i < fromSize; i++)
				sum += wj[i] * x[i];

			y[j] = sum;
		}*/
		
		/* new: sparse multiplication */
		for(int j = 0; j < toSize; j++)
			y[j] = 0F;
		
		for(int i = 0; i < fromSize; i++) {
			if(x[i]==0) continue;
			responsibilities.add(i);
			for(int j = 0; j < toSize; j++)
			{
				final float[] wj = w[j];
				y[j] += wj[i] * x[i];
				//if(i==0 && j==0) { System.err.println(name + "\t" + x[i] + " => " + y[i]); }
			}
		}
		
		//System.out.println(name + "\t" + Arrays.toString(x));
	}

	@Override
	public void activateTrain()
	{
		cleared = false;
		System.arraycopy(from.getActivations(), 0, in, 0, from.size());
		activateTest();
	}

	public void alias(final float[][] w)
	{
		if(w.length != to.size())
			throw new IllegalArgumentException("Incompatible number of to-weights: " + to.size() + " != "
					+ w.length);

		if(w[0].length != from.size())
			throw new IllegalArgumentException("Incompatible number of from-weights: " + from.size()
					+ " != " + w[0].length);

		this.w = w;
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			if(win == null)
				throw new IllegalStateException("Missing a WeightInitializer in " + name);

			if(!win.fullConnectivity())
				throw new IllegalStateException(
						"Cannot use an AdjacencyMatrixConnection with anything less than full connectivity.");

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
		//System.err.println();
		responsibilities.clear();
		cleared = true;
		overwrite = true;
	}

	@Override
	public AdjacencyMatrixConnection copy(final NetworkCopier copier)
	{
		return new AdjacencyMatrixConnection(this, copier);
	}

	@Override
	public float[] getCachedInput()
	{
		return in;
	}

	@Override
	public float getWeight(final int j, final int i)
	{
		return w[j][i];
	}

	@Override
	public void initializeAlphas(final WeightUpdater lrs)
	{
		lrs.initialize(to.size(), from.size());
	}

	@Override
	public void initializeWeights(final WeightInitializer wi)
	{
		final int toSize = to.size();
		final int fromSize = from.size();
		nw = toSize * fromSize;
		w = new float[toSize][fromSize];
		e = new float[toSize][fromSize];
		for(int j = 0; j < toSize; j++)
		{
			final float[] wj = w[j];

			for(int i = 0; i < fromSize; i++)
				wj[i] = wi.randomWeight(j, i);
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
			for(int j = 0; j < to.size(); j++)
				for(int i = 0; i < from.size(); i++)
					m[j][i] = to.getDownstreamCopyLayer() != null ? e[j][i] : in[i];
		return m;
	}

	@Override
	public float[][] toMatrix()
	{
		return MatrixTools.copy(w);
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
		final int fromSize = from.size();
		final float[] d = resp.get();

		if(overwrite)
		{
			for(int j = 0; j < toSize; j++)
			{
				final float[] ej = e[j];
				final float dj = d[j];
				for(int i = 0; i < fromSize; i++) {
					ej[i] = dj * in[i];
				}
			}

			overwrite = false;
		}
		else
		{
			final float[] p = prev.get();
			if(responsibilities!=null) {
				for(int i : responsibilities) {
					for(int j = 0; j < toSize; j++)
					{
						final float[] ej = e[j];
						final float dj = d[j];
						final float pj = p[j];
						//if(i==0 && j==0) System.err.println(name + "\t" + ej[i]);												
						ej[i] = ej[i] * pj + dj * in[i];
					}
				}
			} else {
				for(int j = 0; j < toSize; j++)
				{
					final float[] ej = e[j];
					final float dj = d[j];
					final float pj = p[j];
	
					for(int i = 0; i < fromSize; i++) {
						//if(i==0 && j==0) System.err.println(name + "\t" + ej[i]);												
						ej[i] = ej[i] * pj + dj * in[i];
					}
				}
			}
		}
	}

	@Override
	public void updateResponsibilities()
	{
		final float[] toD = getToLayerResponsibilities();
		//System.err.println(Arrays.toString(toD));

		// if the to layer has empty responsibility, nothing to do.
		if(toD == null)
			return;

		// zero out responsibilities before += below.
		final float[] fromD = getFromLayerResponsibilities();
		Arrays.fill(fromD, 0F);

		final int toSize = to.size();
		final int fromSize = from.size();

		if(responsibilities!=null) {
			for(int j : responsibilities) {
				for(int k = 0; k < toSize; k++) {
					final float[] wk = w[k];
					fromD[j] += wk[j] * toD[k];
				}
			}
		} else {
			for(int k = 0; k < toSize; k++)
			{
				final float[] wk = w[k];
				for(int j = 0; j < fromSize; j++)
					fromD[j] += wk[j] * toD[k];
			}
		}
	}

	@Override
	public void updateWeights(final float[][] dw)
	{
		final int toSize = to.size();
		final int fromSize = from.size();

		/*if(responsibilities!=null) {
			for(int i : responsibilities) {
				for(int j = 0; j < toSize; j++)
				{
					final float[] wj = w[j];
					final float[] dwj = dw[j];
					wj[i] += dwj[i];
				}
			}			
		} else {*/
			for(int j = 0; j < toSize; j++)
			{
				final float[] wj = w[j];
				final float[] dwj = dw[j];
				for(int i = 0; i < fromSize; i++) {
					wj[i] += dwj[i];
				}
			}
		//}
	}

	@Override
	public void updateWeightsFromEligibilities(final Responsibilities copyresp)
	{
		final int toSize = to.size();
		final int fromSize = from.size();
		final float[] d = copyresp.get();
		
		if(updater.getClass()==AdadeltaBatchWeightUpdater.class || 
		   updater.getClass()==BatchWeightUpdater.class ||
		   updater.getClass()==NadamBatchWeightUpdater.class) {		
			updater.updateFromEligibilities(e, d);
			return;
		}
		
		if(responsibilities!=null) {
			for(int i : responsibilities) {
				for(int j = 0; j < toSize; j++)
				{
					final float[] ej = e[j];
					final float dj = d[j];
					final float[] wj = w[j];
		
					//if(i==0 && j==0) System.err.println(name + "\t" + (dj));
					wj[i] += updater.getUpdate(j, i, ej[i] * dj);
				}
			}
		} else {
			for(int j = 0; j < toSize; j++)
			{
				final float[] ej = e[j];
				final float dj = d[j];
				final float[] wj = w[j];
	
				for(int i = 0; i < fromSize; i++) {
					//if(i==0 && j==0) System.err.println(name + "\t" + (dj));
					wj[i] += updater.getUpdate(j, i, ej[i] * dj);
				}
			}
		}
	}

	@Override
	public void updateWeightsFromInputs(final Responsibilities resp)
	{
		final int toSize = to.size();
		final int fromSize = from.size();
		final float[] d = resp.get();

		if(updater.getClass()==AdadeltaBatchWeightUpdater.class || 
		   updater.getClass()==BatchWeightUpdater.class ||
		   updater.getClass()==NadamBatchWeightUpdater.class) {
			updater.updateFromInputs(in, d);
			return;
		}

		if(responsibilities!=null) {
			for(int i : responsibilities) {
				for(int j = 0; j < toSize; j++)
				{			
					final float dj = d[j];
					final float[] wj = w[j];
					wj[i] += updater.getUpdate(j, i, in[i] * dj);
				}
			}
		} else {
			for(int j = 0; j < toSize; j++)
			{
				final float dj = d[j];
				final float[] wj = w[j];
	
				for(int i = 0; i < fromSize; i++) {
					//if(i==0 && j==0) System.err.println(name + "\t" + Arrays.toString(d));
					wj[i] += updater.getUpdate(j, i, in[i] * dj);
				}
			}
		}
	}
}
