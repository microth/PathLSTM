package dmonner.xlbp.compound;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.connection.DiagonalConnection;
import dmonner.xlbp.connection.LayerConnection;
import dmonner.xlbp.layer.DownstreamLayer;
import dmonner.xlbp.layer.UpstreamLayer;

public class DiagonalWeightBank extends WeightBank
{
	private static final long serialVersionUID = 1L;
	private boolean fullOnly;

	public DiagonalWeightBank(final DiagonalWeightBank that, final NetworkCopier copier)
	{
		super(that, copier);
		this.fullOnly = true;
	}

	public DiagonalWeightBank(final String name, final UpstreamLayer upstream,
			final DownstreamLayer downstream, final WeightInitializer win, final WeightUpdaterType wut)
	{
		super(name, upstream, downstream, win, wut);
		this.fullOnly = true;
	}

	@Override
	public DiagonalWeightBank copy(final NetworkCopier copier)
	{
		return new DiagonalWeightBank(this, copier);
	}

	@Override
	public DiagonalWeightBank copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final DiagonalWeightBank copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public DiagonalConnection getConnection()
	{
		return (DiagonalConnection) super.getConnection();
	}

	@Override
	protected LayerConnection makeConnection()
	{
		final DiagonalConnection conn = new DiagonalConnection(getName(), getWeightInput(),
				getWeightOutput());
		conn.setFullOnly(fullOnly);
		return conn;
	}

	public void setFullOnly(final boolean fullOnly)
	{
		this.fullOnly = fullOnly;
		getConnection().setFullOnly(fullOnly);
	}
}
