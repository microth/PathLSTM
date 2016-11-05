package dmonner.xlbp.layer;

import dmonner.xlbp.InternalComponent;
import dmonner.xlbp.NetworkCopier;

public interface InternalLayer extends Layer, InternalComponent, UpstreamLayer, DownstreamLayer
{
	@Override
	public InternalLayer copy(String nameSuffix);

	@Override
	public InternalLayer copy(NetworkCopier copier);
}
