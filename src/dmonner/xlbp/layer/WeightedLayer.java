package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;

public interface WeightedLayer extends UpstreamLayer
{
	@Override
	public void addDownstreamCopyLayer(final CopySourceLayer copySource);

	@Override
	public WeightedLayer copy(NetworkCopier copier);

	@Override
	public WeightedLayer copy(String nameSuffix);
}
