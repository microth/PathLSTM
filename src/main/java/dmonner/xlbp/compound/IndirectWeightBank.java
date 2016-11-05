package dmonner.xlbp.compound;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.connection.IndirectConnection;
import dmonner.xlbp.connection.LayerConnection;
import dmonner.xlbp.layer.DownstreamLayer;
import dmonner.xlbp.layer.UpstreamLayer;

public class IndirectWeightBank extends WeightBank
{
	private static final long serialVersionUID = 1L;

	public IndirectWeightBank(final String name, final UpstreamLayer upstream,
			final DownstreamLayer downstream, final WeightInitializer win, final WeightUpdaterType wut)
	{
		super(name, upstream, downstream, win, wut);
	}

	public IndirectWeightBank(final WeightBank that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	@Override
	public IndirectWeightBank copy(final NetworkCopier copier)
	{
		return new IndirectWeightBank(this, copier);
	}

	@Override
	public IndirectWeightBank copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final IndirectWeightBank copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public IndirectConnection getConnection()
	{
		return (IndirectConnection) super.getConnection();
	}

	@Override
	protected LayerConnection makeConnection()
	{
		return new IndirectConnection(getWeightInput(), getWeightOutput());
	}

}
