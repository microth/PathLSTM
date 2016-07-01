package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;

public class SumOfSquaresTargetLayer extends AbstractTargetLayer
{
	private static final long serialVersionUID = 1L;

	public SumOfSquaresTargetLayer(final String name, final int size)
	{
		super(name, size);
	}

	public SumOfSquaresTargetLayer(final SumOfSquaresTargetLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	@Override
	public SumOfSquaresTargetLayer copy(final NetworkCopier copier)
	{
		return new SumOfSquaresTargetLayer(this, copier);
	}

	@Override
	public SumOfSquaresTargetLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void updateResponsibilities()
	{
		if(t == null)
			d.clear();
		else
			d.target(t, y, w);

		super.updateResponsibilities();
	}
}
