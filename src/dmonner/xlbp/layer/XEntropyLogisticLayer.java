package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.Responsibilities;

public class XEntropyLogisticLayer extends LogisticLayer
{
	private static final long serialVersionUID = 1L;

	public XEntropyLogisticLayer(final String name, final int size)
	{
		super(name, size);
	}

	public XEntropyLogisticLayer(final XEntropyLogisticLayer that, final NetworkCopier copier)
	{
		super(that, copier);
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
			upstream.aliasResponsibilities(myIndexInUpstream, d);

			built = true;
		}
	}

	@Override
	public XEntropyLogisticLayer copy(final NetworkCopier copier)
	{
		return new XEntropyLogisticLayer(this, copier);
	}

	@Override
	public XEntropyLogisticLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void updateUpstreamResponsibilities(final int upstreamIndex)
	{
		// Nothing to do -- upstream ds are already aliased to this layer's d.
	}
}
