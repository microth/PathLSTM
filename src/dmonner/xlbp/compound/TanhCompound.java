package dmonner.xlbp.compound;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.layer.TanhLayer;

public class TanhCompound extends FunctionCompound
{
	private static final long serialVersionUID = 1L;

	public TanhCompound(final FunctionCompound that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public TanhCompound(final String name, final int size)
	{
		this(name, size, true);
	}

	public TanhCompound(final String name, final int size, final boolean biases)
	{
		super(name, new TanhLayer(name + "Tanh", size), biases);
	}

}
