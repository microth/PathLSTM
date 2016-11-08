package dmonner.xlbp.compound;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.UpstreamComponent;
import dmonner.xlbp.layer.UpstreamLayer;

public interface Compound extends UpstreamComponent
{
	@Override
	public Compound copy(NetworkCopier copier);

	@Override
	public Compound copy(String nameSuffix);

	public Component[] getComponents();

	public UpstreamLayer getOutput();

	public UpstreamLayer getOutput(int index);

	public int nOutputs();
}
