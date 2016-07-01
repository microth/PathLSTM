package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.UpstreamComponent;

public interface UpstreamLayer extends Layer, UpstreamComponent
{
	public void addDownstreamCopyLayer(final CopySourceLayer copySource);

	@Override
	public UpstreamLayer copy(String nameSuffix);

	@Override
	public UpstreamLayer copy(NetworkCopier copier);

	public CopySourceLayer getDownstreamCopyLayer();
}
