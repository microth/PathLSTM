package dmonner.xlbp.compound;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.layer.LogisticLayer;

public class LogisticCompound extends FunctionCompound
{
	private static final long serialVersionUID = 1L;

	public LogisticCompound(final FunctionCompound that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public LogisticCompound(final String name, final int size)
	{
		this(name, size, true);
	}

	public LogisticCompound(final String name, final int size, final boolean biases)
	{
		super(name, new LogisticLayer(name + "Log", size), biases);
	}
}
