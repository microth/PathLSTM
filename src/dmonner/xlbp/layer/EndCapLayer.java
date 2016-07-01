package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;

public class EndCapLayer extends AbstractDownstreamLayer
{
	private static final long serialVersionUID = 1L;

	public EndCapLayer(final EndCapLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public EndCapLayer(final String name, final int size)
	{
		super(name, size);
	}

	@Override
	public void activateTest()
	{
		// Nothing to do.
	}

	@Override
	public void activateTrain()
	{
		// Nothing to do.
	}

	@Override
	public EndCapLayer copy(final NetworkCopier copier)
	{
		return new EndCapLayer(this, copier);
	}

	@Override
	public void updateEligibilities()
	{
		// Nothing to do.
	}

	@Override
	public void updateResponsibilities()
	{
		// Nothing to do.
	}

	@Override
	public void updateUpstreamResponsibilities(final int index)
	{
		// Nothing to do.
	}
}
