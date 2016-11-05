package dmonner.xlbp.compound;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.layer.LogisticLayer;
import dmonner.xlbp.layer.SumOfSquaresTargetLayer;

public class SumOfSquaresTargetCompound extends TargetCompound
{
	private static final long serialVersionUID = 1L;

	public SumOfSquaresTargetCompound(final String name, final int size)
	{
		super(name, new LogisticLayer(name + "Output", size), new SumOfSquaresTargetLayer(name
				+ "Target", size));
	}

	public SumOfSquaresTargetCompound(final SumOfSquaresTargetCompound that,
			final NetworkCopier copier)
	{
		super(that, copier);
	}

	@Override
	public SumOfSquaresTargetCompound copy(final NetworkCopier copier)
	{
		return new SumOfSquaresTargetCompound(this, copier);
	}

	@Override
	public SumOfSquaresTargetCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final SumOfSquaresTargetCompound copy = copy(copier);
		copier.build();
		return copy;
	}
}
