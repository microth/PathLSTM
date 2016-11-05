package dmonner.xlbp.compound;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.layer.LogisticLayer;
import dmonner.xlbp.layer.RectifiedLinearLayer;

public class RectifiedLinearCompound extends FunctionCompound
{
	private static final long serialVersionUID = 1L;

	public RectifiedLinearCompound(final FunctionCompound that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public RectifiedLinearCompound(final String name, final int size, final float dropoutrate)
	{
		this(name, size, dropoutrate, true);
	}

	public RectifiedLinearCompound(final String name, final int size, final float dropoutrate, final boolean biases)
	{
		super(name, new RectifiedLinearLayer(name + "Log", size, dropoutrate), biases);
	}
}
