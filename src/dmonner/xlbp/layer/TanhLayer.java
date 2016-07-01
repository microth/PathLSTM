package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;

public class TanhLayer extends AbstractFunctionLayer
{
	private static final long serialVersionUID = 1L;

	public TanhLayer(final String name, final int size)
	{
		super(name, size);
	}

	public TanhLayer(final TanhLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	@Override
	public TanhLayer copy(final NetworkCopier copier)
	{
		return new TanhLayer(this, copier);
	}

	@Override
	public TanhLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public float f(final int j)
	{
		return (float) Math.tanh(x[j]);
	}

	@Override
	public float fprime(final int j)
	{
		return 1F - (y[j] * y[j]);
	}
}
