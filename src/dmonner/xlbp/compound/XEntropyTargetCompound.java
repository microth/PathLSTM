package dmonner.xlbp.compound;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.layer.XEntropyLogisticLayer;
import dmonner.xlbp.layer.XEntropyTargetLayer;

public class XEntropyTargetCompound extends TargetCompound
{
	private static final long serialVersionUID = 1L;

	public XEntropyTargetCompound(final String name, final int size)
	{
		this(name, size, true);
	}

	public XEntropyTargetCompound(final String name, final int size, final boolean biases)
	{
		super(name, new XEntropyLogisticLayer(name + "Output", size), new XEntropyTargetLayer(name
				+ "Target", size), biases);
	}

	public XEntropyTargetCompound(final XEntropyTargetCompound that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	@Override
	public XEntropyTargetCompound copy(final NetworkCopier copier)
	{
		return new XEntropyTargetCompound(this, copier);
	}

	@Override
	public XEntropyTargetCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final XEntropyTargetCompound copy = copy(copier);
		copier.build();
		return copy;
	}

}
