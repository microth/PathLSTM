package dmonner.xlbp.layer;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.connection.BiasConnection;

public class BiasLayer extends AbstractUpstreamLayer implements WeightedLayer
{
	private static final long serialVersionUID = 1L;

	private BiasConnection biases;

	public BiasLayer(final BiasLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public BiasLayer(final String name, final int size)
	{
		super(name, size);
		biases = new BiasConnection(this);
	}

	@Override
	public void activateTest()
	{
		biases.activateTest();
	}

	@Override
	public void activateTrain()
	{
		biases.activateTrain();
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();
			biases.build();

			// alias activations to bias values
			y = biases.get();
			d = new Responsibilities(size);

			built = true;
		}
	}

	@Override
	public void clearActivations()
	{
		// Nothing to do; activations aliased to bias weights.
	}

	@Override
	public void clearEligibilities()
	{
		biases.clear();
	}

	@Override
	public BiasLayer copy(final NetworkCopier copier)
	{
		return new BiasLayer(this, copier);
	}

	@Override
	public BiasLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		super.copyConnectivityFrom(comp, copier);

		if(comp instanceof BiasLayer)
		{
			final BiasLayer that = (BiasLayer) comp;
			if(copier.copyWeights())
				this.biases = that.biases.copy(copier);
			else
				this.biases = new BiasConnection(this);
		}
	}

	public BiasConnection getConnection()
	{
		return biases;
	}

	@Override
	public int nWeights()
	{
		return biases.nWeights();
	}

	@Override
	public void processBatch()
	{
		biases.processBatch();
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		biases.setWeightInitializer(win);
	}

	@Override
	public void setWeightUpdaterType(final WeightUpdaterType wut)
	{
		biases.setWeightUpdater(wut);
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		super.toString(sb);
		sb.pushIndent();
		biases.toString(sb);
		sb.popIndent();
	}

	@Override
	public void unbuild()
	{
		super.unbuild();
		biases.unbuild();
	}

	@Override
	public void updateEligibilities()
	{
		if(downstreamCopyLayer != null)
		{
			downstream.updateUpstreamResponsibilities(myIndexInDownstream);
			biases.updateEligibilities(d, downstreamCopyLayer.getPreviousResponsibilities());
		}
	}

	@Override
	public void updateResponsibilities()
	{
		if(downstreamCopyLayer == null)
			downstream.updateUpstreamResponsibilities(myIndexInDownstream);
	}

	@Override
	public void updateWeights()
	{
		if(downstreamCopyLayer == null)
			biases.updateWeightsFromInputs(d);
		else
			biases.updateWeightsFromEligibilities(downstreamCopyLayer.getResponsibilities());
	}
}
