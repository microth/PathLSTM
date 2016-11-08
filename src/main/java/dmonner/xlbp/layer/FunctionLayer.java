package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;

public interface FunctionLayer extends InternalLayer
{
	@Override
	public FunctionLayer copy(NetworkCopier copier);

	@Override
	public FunctionLayer copy(String nameSuffix);

	public float f(final int j);

	public float fprime(final int j);
}
