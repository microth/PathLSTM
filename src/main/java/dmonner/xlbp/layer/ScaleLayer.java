package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;

public class ScaleLayer extends AbstractFunctionLayer
{
	private static final long serialVersionUID = 1L;

	private final float factor;

	public ScaleLayer(final ScaleLayer that, final NetworkCopier copier)
	{
		super(that, copier);
		this.factor = that.factor;
	}

	public ScaleLayer(final String name, final int size, final float factor)
	{
		super(name, size);
		this.factor = factor;
	}

	@Override
	public ScaleLayer copy(final NetworkCopier copier)
	{
		return new ScaleLayer(this, copier);
	}

	@Override
	public ScaleLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public float f(final int j)
	{
		return x[j] * factor;
	}

	@Override
	public float fprime(final int j)
	{
		return factor;
	}
}
