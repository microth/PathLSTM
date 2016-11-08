package dmonner.xlbp;

import dmonner.xlbp.layer.DownstreamLayer;

public interface DownstreamComponent extends Component
{
	public void addUpstream(final UpstreamComponent upstream);

	public DownstreamLayer asDownstreamLayer();

	public boolean connectedUpstream(final UpstreamComponent upstream);

	@Override
	public DownstreamComponent copy(NetworkCopier copier);

	@Override
	public DownstreamComponent copy(String nameSuffix);

	public int getIndexInUpstream();

	public int getIndexInUpstream(final int index);

	public UpstreamComponent getUpstream();

	public UpstreamComponent getUpstream(final int index);

	public int indexOfUpstream(final UpstreamComponent upstream);

	public int nUpstream();

	public void removeUpstream(final int index);

	public void removeUpstream(final UpstreamComponent upstream);
}
