package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;

public class DirectResponsibilityLayer extends AbstractTargetLayer
{
	private static final long serialVersionUID = 1L;

	public DirectResponsibilityLayer(final DirectResponsibilityLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public DirectResponsibilityLayer(final String name, final int size)
	{
		super(name, size);
	}

	@Override
	public DirectResponsibilityLayer copy(final NetworkCopier copier)
	{
		return new DirectResponsibilityLayer(this, copier);
	}

	@Override
	public DirectResponsibilityLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void updateResponsibilities()
	{
		if(t == null)
			d.clear();
		else
			d.set(t);

		super.updateResponsibilities();
	}
}
