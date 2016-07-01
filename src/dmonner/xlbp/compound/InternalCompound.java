package dmonner.xlbp.compound;

import dmonner.xlbp.DownstreamComponent;
import dmonner.xlbp.InternalComponent;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.layer.DownstreamLayer;

public interface InternalCompound extends Compound, DownstreamComponent, InternalComponent
{
	@Override
	public InternalCompound copy(NetworkCopier copier);

	@Override
	public InternalCompound copy(String nameSuffix);

	public DownstreamLayer getInput();

	public DownstreamLayer getInput(int index);

	public int nInputs();
}
