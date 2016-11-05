package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;

public class LogisticLayer extends AbstractFunctionLayer
{
	private static final long serialVersionUID = 1L;

	public LogisticLayer(final LogisticLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public LogisticLayer(final String name, final int size)
	{
		super(name, size);
	}

	@Override
	public LogisticLayer copy(final NetworkCopier copier)
	{
		return new LogisticLayer(this, copier);
	}

	@Override
	public LogisticLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public float f(final int j)
	{
		return 1F / (1F + (float) Math.exp(-x[j]));
	}

	@Override
	public float fprime(final int j)
	{
		return y[j] * (1 - y[j]);
	}
}
