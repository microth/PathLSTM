package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.Responsibilities;

public class LinearLayer extends AbstractInternalLayer implements FunctionLayer
{
	private static final long serialVersionUID = 1L;

	public LinearLayer(final LinearLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public LinearLayer(final String name, final int size)
	{
		super(name, size);
	}

	@Override
	public void activateTest()
	{
		// Nothing to do; aliased y to upstream y.
	}

	@Override
	public void activateTrain()
	{
		// Nothing to do; aliased y to upstream y.
	}

	@Override
	public void aliasResponsibilities(final int index, final Responsibilities resp)
	{
		super.aliasResponsibilities(index, resp);
		upstream.aliasResponsibilities(myIndexInUpstream, resp);
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
	public void clearActivations()
	{
		// Nothing to do; aliased y to upstream y;
	}

	@Override
	public LinearLayer copy(final NetworkCopier copier)
	{
		return new LinearLayer(this, copier);
	}

	@Override
	public LinearLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public float f(final int j)
	{
		return y[j];
	}

	@Override
	public float fprime(final int j)
	{
		return 1F;
	}

	@Override
	public void updateEligibilities()
	{
		if(downstreamCopyLayer != null) {
			downstream.updateUpstreamResponsibilities(myIndexInDownstream);

		}
	}

	@Override
	public void updateResponsibilities()
	{
		if(downstreamCopyLayer == null) {
			downstream.updateUpstreamResponsibilities(myIndexInDownstream);
		}
	}

	@Override
	public void updateUpstreamResponsibilities(final int index)
	{
		// Nothing to do; upstream d is aliased to our d.
	}

}
