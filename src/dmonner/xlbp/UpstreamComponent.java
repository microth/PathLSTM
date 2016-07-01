package dmonner.xlbp;

import dmonner.xlbp.layer.UpstreamLayer;

public interface UpstreamComponent extends Component
{
	public void addDownstream(final DownstreamComponent downstream);

	public UpstreamLayer asUpstreamLayer();

	public boolean connectedDownstream(final DownstreamComponent downstream);

	@Override
	public UpstreamComponent copy(String nameSuffix);

	@Override
	public UpstreamComponent copy(NetworkCopier copier);

	public DownstreamComponent getDownstream();

	public DownstreamComponent getDownstream(final int index);

	public int getIndexInDownstream();

	public int getIndexInDownstream(final int index);

	public int indexOfDownstream(final DownstreamComponent downstream);

	public int nDownstream();

	public void removeDownstream(final DownstreamComponent downstream);

	public void removeDownstream(final int index);
}
