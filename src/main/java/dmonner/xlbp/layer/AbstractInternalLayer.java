package dmonner.xlbp.layer;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.UpstreamComponent;

public abstract class AbstractInternalLayer extends AbstractUpstreamLayer implements InternalLayer
{
	private static final long serialVersionUID = 1L;

	protected UpstreamLayer upstream;
	protected int nUpstream;
	protected int myIndexInUpstream;

	public AbstractInternalLayer(final AbstractInternalLayer that, final NetworkCopier copier)
	{
		super(that, copier);

		this.upstream = null;
		this.nUpstream = 0;
		this.myIndexInUpstream = -1;
	}

	public AbstractInternalLayer(final String name, final int size)
	{
		super(name, size);

		this.upstream = null;
		this.nUpstream = 0;
		this.myIndexInUpstream = -1;
	}

	@Override
	public void addUpstream(final UpstreamComponent upstream)
	{
		if(this.upstream != null)
			throw new IllegalStateException("Upstream layer for " + name + " already set!");

		this.nUpstream = 1;
		this.upstream = upstream.asUpstreamLayer();
		connectUpstream();
		myIndexInUpstream = upstream.indexOfDownstream(this);
	}

	@Override
	public DownstreamLayer asDownstreamLayer()
	{
		return this;
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			// find my index in the upstream layer
			myIndexInUpstream = upstream.indexOfDownstream(this);

			built = true;
		}
	}

	@Override
	public boolean connectedUpstream(final UpstreamComponent upstream)
	{
		return this.upstream == upstream;
	}

	protected void connectUpstream()
	{
		if(!upstream.connectedDownstream(this))
			upstream.addDownstream(this);
	}

	@Override
	public abstract AbstractInternalLayer copy(final NetworkCopier copier);

	@Override
	public AbstractInternalLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		super.copyConnectivityFrom(comp, copier);

		if(comp instanceof AbstractInternalLayer)
		{
			final AbstractInternalLayer that = (AbstractInternalLayer) comp;
			this.upstream = copier.getCopyIfExists(that.upstream);
			this.nUpstream = that.nUpstream;
			this.myIndexInUpstream = that.myIndexInUpstream;
		}
	}

	protected void disconnectUpstream()
	{
		if(upstream != null)
		{
			final int index = upstream.indexOfDownstream(this);

			if(index >= 0)
				upstream.removeDownstream(index);
		}
	}

	@Override
	public int getIndexInUpstream()
	{
		return myIndexInUpstream;
	}

	@Override
	public int getIndexInUpstream(final int index)
	{
		if(index != 0)
			throw new IndexOutOfBoundsException("Only " + nUpstream + " upstream layers available from "
					+ name + "; cannot get layer " + index);

		return myIndexInUpstream;
	}

	@Override
	public UpstreamComponent getUpstream()
	{
		return upstream;
	}

	@Override
	public UpstreamComponent getUpstream(final int index)
	{
		if(index != 0)
			throw new IndexOutOfBoundsException("Only " + nUpstream + " upstream layers available from "
					+ name + "; cannot get layer " + index);

		return upstream;
	}

	@Override
	public int indexOfUpstream(final UpstreamComponent upstream)
	{
		return this.upstream == upstream ? 0 : -1;
	}

	@Override
	public int nUpstream()
	{
		return nUpstream;
	}

	@Override
	public boolean optimize()
	{
		if(!super.optimize())
			return false;

		if(upstream == null)
			throw new IllegalStateException("No upstream layer set for " + name);

		return true;
	}

	@Override
	public void removeUpstream(final int index)
	{
		if(index >= nUpstream)
			throw new IndexOutOfBoundsException("Only " + nUpstream + " upstream layers available from "
					+ name + "; cannot remove layer " + index);

		disconnectUpstream();
		this.upstream = null;
		this.nUpstream = 0;
	}

	@Override
	public void removeUpstream(final UpstreamComponent upstream)
	{
		if(this.upstream != upstream)
			throw new IllegalArgumentException(upstream.getName()
					+ " is not present in upstream layers of " + name);

		disconnectUpstream();
		this.upstream = null;
		this.nUpstream = 0;
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		super.toString(sb);
		sb.pushIndent();

		if(sb.showConnectivity())
			sb.appendln("Upstream: " + upstream.getName());

		sb.popIndent();
	}
}
