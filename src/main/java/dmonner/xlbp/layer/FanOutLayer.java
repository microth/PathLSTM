package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.Responsibilities;

public class FanOutLayer extends AbstractFanOutLayer
{
	private static final long serialVersionUID = 1L;

	public FanOutLayer(final FanOutLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public FanOutLayer(final String name, final int size)
	{
		super(name, size);
	}

	@Override
	public void activateTest()
	{
		// Nothing to do -- activations are aliased from upstream layer.
	}

	@Override
	public void activateTrain()
	{
		// Nothing to do -- activations are aliased from upstream layer.
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			upstream.build();
			y = upstream.getActivations();
			d = new Responsibilities(size);
			upstream.aliasResponsibilities(myIndexInUpstream, d);

			built = true;
		}
	}

	@Override
	public void clear()
	{
		// Nothing to do -- activations are aliased from upstream layer.
		// Nothing to do for deltas -- they will get scrubbed by the upstream layer.
	}

	@Override
	public FanOutLayer copy(final NetworkCopier copier)
	{
		return new FanOutLayer(this, copier);
	}

	@Override
	public FanOutLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void updateEligibilities()
	{
		if(downstreamCopyLayer != null)
		{
			// Each update call sets the corresponding values in dDownstream[k]

			// copy the first one into d
			downstream[0].updateUpstreamResponsibilities(myIndexInDownstream[0]);
			d.copy(dDownstream[0]);

			// add the subsequent ones into d
			for(int k = 1; k < nDownstream; k++)
			{
				downstream[k].updateUpstreamResponsibilities(myIndexInDownstream[k]);
				d.add(dDownstream[k]);
			}
		}
	}

	@Override
	public void updateResponsibilities()
	{
		if(downstreamCopyLayer == null)
		{
			// Each update call sets the corresponding values in dDownstream[k]

			// copy the first one into d
			downstream[0].updateUpstreamResponsibilities(myIndexInDownstream[0]);
			d.copy(dDownstream[0]);
			
			// add the subsequent ones into d
			for(int k = 1; k < nDownstream; k++)
			{
				downstream[k].updateUpstreamResponsibilities(myIndexInDownstream[k]);
				d.add(dDownstream[k]);
			}
		}
	}

	@Override
	public void updateUpstreamResponsibilities(final int index)
	{
		// Nothing to do -- upstream ds are already aliased to this layer's d.
	}
}
