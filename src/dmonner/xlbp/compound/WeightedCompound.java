package dmonner.xlbp.compound;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.UpstreamComponent;
import dmonner.xlbp.connection.ConnectionType;

public interface WeightedCompound extends InternalCompound
{
	public void addUpstream(final UpstreamComponent upstream, final ConnectionType type);

	public void addUpstreamWeights(final UpstreamComponent upstream);

	@Override
	public WeightedCompound copy(NetworkCopier copier);

	@Override
	public WeightedCompound copy(String nameSuffix);

	public WeightBank getUpstreamWeights();

	public WeightBank getUpstreamWeights(final int index);

	public int nUpstreamWeights();
}
