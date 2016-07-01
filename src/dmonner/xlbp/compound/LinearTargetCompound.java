package dmonner.xlbp.compound;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.layer.LinearLayer;
import dmonner.xlbp.layer.SumOfSquaresTargetLayer;

public class LinearTargetCompound extends TargetCompound
{
	private static final long serialVersionUID = 1L;

	public LinearTargetCompound(final LinearTargetCompound that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public LinearTargetCompound(final String name, final int size)
	{
		super(name, new LinearLayer(name + "Output", size), new SumOfSquaresTargetLayer(
				name + "Target", size));
	}

	@Override
	public LinearTargetCompound copy(final NetworkCopier copier)
	{
		return new LinearTargetCompound(this, copier);
	}

	@Override
	public LinearTargetCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final LinearTargetCompound copy = copy(copier);
		copier.build();
		return copy;
	}
}
