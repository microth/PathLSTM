package dmonner.xlbp.connection;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdater;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.layer.WeightReceiverLayer;
import dmonner.xlbp.layer.WeightSenderLayer;

public class ImmutableDiagonalConnection extends LayerConnection
{
	private static final long serialVersionUID = 1L;

	private float w;

	public ImmutableDiagonalConnection(final ImmutableDiagonalConnection that,
			final NetworkCopier copier)
	{
		super(that, copier);

		this.w = that.w;
	}

	public ImmutableDiagonalConnection(final String name, final WeightReceiverLayer to,
			final WeightSenderLayer from)
	{
		super(name, to, from);

		if(from.size() != to.size())
			throw new IllegalArgumentException(
					"Sending and receiving layers of a DiagonalConnection must be the same size: "
							+ from.size() + " != " + to.size());
	}

	public ImmutableDiagonalConnection(final WeightReceiverLayer to, final WeightSenderLayer from)
	{
		this(from.getName() + "DiagonalTo" + to.getName(), to, from);
	}

	@Override
	public void activateTest()
	{
		final int toSize = to.size();
		final float[] y = to.getActivations();
		final float[] x = from.getActivations();

		for(int j = 0; j < toSize; j++)
			y[j] = w * x[j];
	}

	@Override
	public void activateTrain()
	{
		activateTest();
	}

	@Override
	public void clear()
	{
		// Nothing to do.
	}

	@Override
	public ImmutableDiagonalConnection copy(final NetworkCopier copier)
	{
		return new ImmutableDiagonalConnection(this, copier);
	}

	@Override
	public float[] getCachedInput()
	{
		return null;
	}

	@Override
	public float getWeight(final int j, final int i)
	{
		return w;
	}

	@Override
	public void initializeAlphas(final WeightUpdater lrs)
	{
		// Nothing to do.
	}

	@Override
	public void initializeWeights(final WeightInitializer win)
	{
		// Nothing to do.
	}

	@Override
	public int nWeights()
	{
		return to.size();
	}

	@Override
	public void processBatch()
	{
		// Nothing to do.
	}

	public void set(final float w)
	{
		this.w = w;
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		// Nothing to do.
	}

	@Override
	public void setWeightUpdater(final WeightUpdaterType wut)
	{
		// Nothing to do.
	}

	@Override
	public float[][] toEligibilitiesMatrix()
	{
		final float[][] m = new float[to.size()][to.size()];
		return m;
	}

	@Override
	public float[][] toMatrix()
	{
		final float[][] m = new float[to.size()][to.size()];
		for(int i = 0; i < to.size(); i++)
			m[i][i] = w;
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
			sb.appendln("Untrainable Weights:");
			sb.pushIndent();
			sb.appendln(w + " (x" + to.size() + ")");
			sb.popIndent();
		}

		sb.popIndent();
	}

	@Override
	public void updateEligibilities(final Responsibilities resp, final Responsibilities prev)
	{
		// Nothing to do.
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
			fromD[j] = w * toD[j];
	}

	@Override
	public void updateWeights(final float[][] dw)
	{
		// Nothing to do.
	}

	@Override
	public void updateWeightsFromEligibilities(final Responsibilities copyresp)
	{
		// Nothing to do.
	}

	@Override
	public void updateWeightsFromInputs(final Responsibilities resp)
	{
		// Nothing to do.
	}
}
