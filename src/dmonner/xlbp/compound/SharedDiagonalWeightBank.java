package dmonner.xlbp.compound;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.connection.ImmutableDiagonalConnection;
import dmonner.xlbp.connection.LayerConnection;
import dmonner.xlbp.layer.DownstreamLayer;
import dmonner.xlbp.layer.UpstreamLayer;

public class SharedDiagonalWeightBank extends WeightBank
{
	private static final long serialVersionUID = 1L;

	public SharedDiagonalWeightBank(final SharedDiagonalWeightBank that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public SharedDiagonalWeightBank(final String name, final UpstreamLayer upstream,
			final DownstreamLayer downstream, final WeightInitializer win, final WeightUpdaterType wut)
	{
		super(name, upstream, downstream, win, wut);
	}

	@Override
	public SharedDiagonalWeightBank copy(final NetworkCopier copier)
	{
		return new SharedDiagonalWeightBank(this, copier);
	}

	@Override
	public SharedDiagonalWeightBank copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final SharedDiagonalWeightBank copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public ImmutableDiagonalConnection getConnection()
	{
		return (ImmutableDiagonalConnection) super.getConnection();
	}

	@Override
	protected LayerConnection makeConnection()
	{
		return new ImmutableDiagonalConnection(getName(), getWeightInput(), getWeightOutput());
	}

}
