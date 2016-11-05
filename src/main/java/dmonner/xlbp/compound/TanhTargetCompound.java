package dmonner.xlbp.compound;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.layer.SumOfSquaresTargetLayer;
import dmonner.xlbp.layer.TanhLayer;

public class TanhTargetCompound extends TargetCompound
{
	private static final long serialVersionUID = 1L;

	public TanhTargetCompound(final String name, final int size)
	{
		this(name, size, true);
	}

	public TanhTargetCompound(final String name, final int size, final boolean biases)
	{
		super(name, new TanhLayer(name + "Output", size), new SumOfSquaresTargetLayer(name + "Target",
				size), biases);
	}

	public TanhTargetCompound(final TanhTargetCompound that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	@Override
	public TanhTargetCompound copy(final NetworkCopier copier)
	{
		return new TanhTargetCompound(this, copier);
	}

	@Override
	public TanhTargetCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final TanhTargetCompound copy = copy(copier);
		copier.build();
		return copy;
	}

}
