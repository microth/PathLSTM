package dmonner.xlbp.connection;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdater;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.layer.WeightReceiverLayer;
import dmonner.xlbp.layer.WeightSenderLayer;

public class IndirectConnection extends LayerConnection
{
	private static final long serialVersionUID = 1L;

	public IndirectConnection(final IndirectConnection that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public IndirectConnection(final String name, final WeightReceiverLayer to,
			final WeightSenderLayer from)
	{
		super(name, to, from);

		if(to.size() != from.size())
			throw new IllegalArgumentException("To and From layers must be equal sizes.");
	}

	public IndirectConnection(final WeightReceiverLayer to, final WeightSenderLayer from)
	{
		this(from.getName() + "IndirectTo" + to.getName(), to, from);
	}

	@Override
	public void activateTest()
	{
		System.arraycopy(from.getActivations(), 0, to.getActivations(), 0, to.size());
	}

	@Override
	public void activateTrain()
	{
		System.arraycopy(from.getActivations(), 0, to.getActivations(), 0, to.size());
	}

	@Override
	public void clear()
	{
		// Nothing to do.
	}

	@Override
	public IndirectConnection copy(final NetworkCopier copier)
	{
		return new IndirectConnection(this, copier);
	}

	@Override
	public float[] getCachedInput()
	{
		return from.getActivations();
	}

	@Override
	public float getWeight(final int j, final int i)
	{
		return 0;
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
		return 0;
	}

	@Override
	public void processBatch()
	{
		// Nothing to do.
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
		return new float[0][0];
	}

	@Override
	public float[][] toMatrix()
	{
		return new float[0][0];
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
	}

	@Override
	public void updateEligibilities(final Responsibilities resp, final Responsibilities prev)
	{
		// Nothing to do.
	}

	@Override
	public void updateResponsibilities()
	{
		final float[] td = getToLayerResponsibilities();
		final float[] fd = getFromLayerResponsibilities();
		System.arraycopy(td, 0, fd, 0, fd.length);
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
