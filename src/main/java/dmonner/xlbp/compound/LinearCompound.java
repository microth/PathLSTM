package dmonner.xlbp.compound;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.layer.LinearLayer;

public class LinearCompound extends FunctionCompound
{
	private static final long serialVersionUID = 1L;

	public LinearCompound(final FunctionCompound that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public LinearCompound(final String name, final int size)
	{
		this(name, size, true);
	}

	public LinearCompound(final String name, final int size, final boolean biases)
	{
		super(name, new LinearLayer(name + "Lin", size), biases);
	}
}
