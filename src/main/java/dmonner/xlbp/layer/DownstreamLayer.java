package dmonner.xlbp.layer;

import dmonner.xlbp.DownstreamComponent;
import dmonner.xlbp.NetworkCopier;

public interface DownstreamLayer extends Layer, DownstreamComponent
{
	@Override
	public DownstreamLayer copy(String nameSuffix);

	@Override
	public DownstreamLayer copy(NetworkCopier copier);

	public void updateUpstreamResponsibilities(final int index);
}
