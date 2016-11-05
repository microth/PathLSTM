package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.TargetComponent;

public interface TargetLayer extends DownstreamLayer, TargetComponent
{
	@Override
	public TargetLayer copy(NetworkCopier copier);

	@Override
	public TargetLayer copy(String nameSuffix);
}
