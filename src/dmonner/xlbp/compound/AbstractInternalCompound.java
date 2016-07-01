package dmonner.xlbp.compound;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.UpstreamComponent;
import dmonner.xlbp.layer.DownstreamLayer;

public abstract class AbstractInternalCompound extends AbstractCompound implements InternalCompound
{
	private static final long serialVersionUID = 1L;

	protected DownstreamLayer in;

	public AbstractInternalCompound(final AbstractCompound that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public AbstractInternalCompound(final String name)
	{
		super(name);
	}

	@Override
	public void addUpstream(final UpstreamComponent upstream)
	{
		in.addUpstream(upstream);
	}

	@Override
	public DownstreamLayer asDownstreamLayer()
	{
		return in;
	}

	@Override
	public boolean connectedUpstream(final UpstreamComponent upstream)
	{
		return in.connectedUpstream(upstream);
	}

	@Override
	public abstract AbstractInternalCompound copy(NetworkCopier copier);

	@Override
	public AbstractInternalCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final AbstractInternalCompound copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		super.copyConnectivityFrom(comp, copier);

		if(comp instanceof AbstractInternalCompound)
		{
			final AbstractInternalCompound that = (AbstractInternalCompound) comp;
			this.in.copyConnectivityFrom(that.in, copier);
		}
	}

	@Override
	public int getIndexInUpstream()
	{
		return in.getIndexInUpstream();
	}

	@Override
	public int getIndexInUpstream(final int index)
	{
		return in.getIndexInUpstream(index);
	}

	@Override
	public DownstreamLayer getInput()
	{
		return in;
	}

	@Override
	public DownstreamLayer getInput(final int index)
	{
		if(index > 0)
			throw new IllegalArgumentException("Index too large");

		return in;
	}

	@Override
	public UpstreamComponent getUpstream()
	{
		return in.getUpstream();
	}

	@Override
	public UpstreamComponent getUpstream(final int index)
	{
		return in.getUpstream(index);
	}

	@Override
	public int indexOfUpstream(final UpstreamComponent upstream)
	{
		return in.indexOfUpstream(upstream);
	}

	@Override
	public int nInputs()
	{
		return 1;
	}

	@Override
	public int nUpstream()
	{
		return in.nUpstream();
	}

	@Override
	public void removeUpstream(final int index)
	{
		in.removeUpstream(index);
	}

	@Override
	public void removeUpstream(final UpstreamComponent upstream)
	{
		in.removeUpstream(upstream);
	}

}
