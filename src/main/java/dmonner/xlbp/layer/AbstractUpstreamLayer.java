package dmonner.xlbp.layer;

import dmonner.xlbp.Component;
import dmonner.xlbp.DownstreamComponent;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;

public abstract class AbstractUpstreamLayer extends AbstractLayer implements UpstreamLayer
{
	private static final long serialVersionUID = 1L;

	protected DownstreamLayer downstream;
	protected int nDownstream;
	protected int myIndexInDownstream;
	protected CopySourceLayer downstreamCopyLayer;

	public AbstractUpstreamLayer(final AbstractUpstreamLayer that, final NetworkCopier copier)
	{
		super(that, copier);

		this.downstream = null;
		this.nDownstream = 0;
		this.myIndexInDownstream = -1;
	}

	public AbstractUpstreamLayer(final String name, final int size)
	{
		super(name, size);

		this.downstream = null;
		this.nDownstream = 0;
		this.myIndexInDownstream = -1;
	}

	@Override
	public void addDownstream(final DownstreamComponent downstream)
	{
		if(this.downstream != null)
			throw new IllegalStateException("Downstream layer for " + name + " already set!");

		this.nDownstream = 1;
		this.downstream = downstream.asDownstreamLayer();
		connectDownstream();
		this.myIndexInDownstream = downstream.indexOfUpstream(this);
	}

	@Override
	public void addDownstreamCopyLayer(final CopySourceLayer copySource)
	{
		if(downstreamCopyLayer != copySource)
		{
			if(downstreamCopyLayer != null)
				throw new IllegalStateException("Cannot add " + copySource
						+ " as the downstream copy layer for " + name + " since " + downstreamCopyLayer
						+ " already fills that role.");

			downstreamCopyLayer = copySource;
		}
	}

	@Override
	public UpstreamLayer asUpstreamLayer()
	{
		return this;
	}

	@Override
	public void build()
	{
		if(!built)
		{
			// find my index in the upstream layer
			myIndexInDownstream = downstream.indexOfUpstream(this);

			if(downstream.size() != size())
				throw new IllegalStateException("Incompatible sizes between " + name + " (" + size()
						+ ") and " + downstream.getName() + " (" + downstream.size() + ").");

			built = true;
		}
	}

	protected void connectDownstream()
	{
		if(!downstream.connectedUpstream(this))
			downstream.addUpstream(this);
	}

	@Override
	public boolean connectedDownstream(final DownstreamComponent downstream)
	{
		return this.downstream == downstream;
	}

	@Override
	public abstract AbstractUpstreamLayer copy(final NetworkCopier copier);

	@Override
	public AbstractUpstreamLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		super.copyConnectivityFrom(comp, copier);

		if(comp instanceof AbstractUpstreamLayer)
		{
			final AbstractUpstreamLayer that = (AbstractUpstreamLayer) comp;
			this.downstream = copier.getCopyIfExists(that.downstream);
			this.nDownstream = that.nDownstream;
			this.myIndexInDownstream = that.myIndexInDownstream;
		}
	}

	protected void disconnectDownstream(final DownstreamComponent downstream)
	{
		if(downstream != null)
		{
			final int index = downstream.indexOfUpstream(this);

			if(index >= 0)
				downstream.removeUpstream(index);
		}
	}

	@Override
	public DownstreamComponent getDownstream()
	{
		return downstream;
	}

	@Override
	public DownstreamComponent getDownstream(final int index)
	{
		if(index != 0)
			throw new IndexOutOfBoundsException("Only " + nDownstream
					+ " downstream layers available from " + name + "; cannot get layer " + index);

		return downstream;
	}

	@Override
	public CopySourceLayer getDownstreamCopyLayer()
	{
		return downstreamCopyLayer;
	}

	@Override
	public int getIndexInDownstream()
	{
		return myIndexInDownstream;
	}

	@Override
	public int getIndexInDownstream(final int index)
	{
		if(index != 0)
			throw new IndexOutOfBoundsException("Only " + nDownstream
					+ " downstream layers available from " + name + "; cannot get layer " + index);

		return myIndexInDownstream;
	}

	@Override
	public int indexOfDownstream(final DownstreamComponent downstream)
	{
		return this.downstream == downstream ? 0 : -1;
	}

	@Override
	public int nDownstream()
	{
		return nDownstream;
	}

	@Override
	public boolean optimize()
	{
		if(downstream == null)
			throw new IllegalStateException("No downstream layer set for " + name);

		return true;
	}

	@Override
	public void removeDownstream(final DownstreamComponent downstream)
	{
		if(this.downstream != downstream)
			throw new IllegalArgumentException(downstream.getName()
					+ " is not present in downstream layers of " + name);

		final DownstreamComponent old = this.downstream;

		this.downstream = null;
		this.nDownstream = 0;

		disconnectDownstream(old);
	}

	@Override
	public void removeDownstream(final int index)
	{
		if(index >= nDownstream)
			throw new IndexOutOfBoundsException("Only " + nDownstream
					+ " downstream layers available from " + name + "; cannot remove layer " + index);

		final DownstreamComponent old = this.downstream;

		this.downstream = null;
		this.nDownstream = 0;

		disconnectDownstream(old);
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		super.toString(sb);
		sb.pushIndent();

		if(sb.showConnectivity())
			sb.appendln("Downstream: " + downstream.getName());

		if(sb.showExtra() && downstreamCopyLayer != null)
			sb.appendln("DownstreamCopyLayer: " + downstreamCopyLayer.getName());

		sb.popIndent();
	}
}
