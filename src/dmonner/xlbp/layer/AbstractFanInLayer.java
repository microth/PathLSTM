package dmonner.xlbp.layer;

import java.util.Arrays;

import dmonner.xlbp.Component;
import dmonner.xlbp.DownstreamComponent;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.UpstreamComponent;

public abstract class AbstractFanInLayer extends AbstractUpstreamLayer implements InternalLayer
{
	private static final long serialVersionUID = 1L;

	protected UpstreamLayer[] upstream;
	protected int nUpstream;
	protected int[] myIndexInUpstream;

	public AbstractFanInLayer(final AbstractFanInLayer that, final NetworkCopier copier)
	{
		super(that, copier);

		this.upstream = new UpstreamLayer[0];
		this.myIndexInUpstream = new int[0];
		this.nUpstream = 0;
	}

	public AbstractFanInLayer(final String name, final int size)
	{
		super(name, size);

		this.upstream = new UpstreamLayer[0];
		this.myIndexInUpstream = new int[0];
		this.nUpstream = 0;
	}

	@Override
	public void addUpstream(final UpstreamComponent upstream)
	{
		final int index = nUpstream;
		nUpstream++;
		setUpstreamCapacity(nUpstream);
		this.upstream[index] = upstream.asUpstreamLayer();
		connectUpstream(index);
		this.myIndexInUpstream[index] = upstream.indexOfDownstream(this);
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

			// find my index in the upstream layers
			myIndexInUpstream = new int[nUpstream];

			for(int i = 0; i < nUpstream; i++)
			{
				final UpstreamLayer upstream = this.upstream[i];
				final int index = upstream.indexOfDownstream(this);

				if(index < 0)
					throw new IllegalStateException(name + " is not in " + upstream.getName()
							+ " layer's list of connected downstream layers.");

				if(upstream.size() != size())
					throw new IllegalStateException("Incompatible sizes between " + name + " (" + size()
							+ ") and " + upstream.getName() + " (" + upstream.size() + ").");

				myIndexInUpstream[i] = index;
			}

			built = true;
		}
	}

	@Override
	public boolean connectedUpstream(final UpstreamComponent upstream)
	{
		return indexOfUpstream(upstream) >= 0;
	}

	protected void connectUpstream(final int index)
	{
		final UpstreamComponent upstream = this.upstream[index];

		if(!upstream.connectedDownstream(this))
			upstream.addDownstream(this);
	}

	@Override
	public abstract AbstractFanInLayer copy(final NetworkCopier copier);

	@Override
	public AbstractFanInLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		super.copyConnectivityFrom(comp, copier);

		if(comp instanceof AbstractFanInLayer)
		{
			final AbstractFanInLayer that = (AbstractFanInLayer) comp;
			this.upstream = new UpstreamLayer[that.upstream.length];
			this.myIndexInUpstream = new int[that.myIndexInUpstream.length];
			this.nUpstream = that.nUpstream;

			for(int i = 0; i < that.upstream.length; i++)
			{
				this.upstream[i] = copier.getCopyIfExists(that.upstream[i]);
				this.myIndexInUpstream[i] = that.myIndexInUpstream[i];
			}
		}
	}

	protected void disconnectUpstream(final UpstreamComponent upstream)
	{
		final int index = upstream.indexOfDownstream(this);

		if(index >= 0)
			upstream.removeDownstream(index);
	}

	@Override
	public int getIndexInUpstream()
	{
		return getIndexInUpstream(0);
	}

	@Override
	public int getIndexInUpstream(final int index)
	{
		return myIndexInUpstream[index];
	}

	@Override
	public UpstreamComponent getUpstream()
	{
		return getUpstream(0);
	}

	@Override
	public UpstreamComponent getUpstream(final int upstreamIndex)
	{
		return upstream[upstreamIndex];
	}

	@Override
	public int indexOfUpstream(final UpstreamComponent upstream)
	{
		for(int i = 0; i < nUpstream; i++)
			if(this.upstream[i] == upstream)
				return i;

		return -1;
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

		// check that there is a downstream connection
		if(downstream == null)
			throw new IllegalStateException(name + " is missing a downstream layer.");

		// check that there is an upstream connection
		if(nUpstream == 0)
			throw new IllegalStateException(name + " is missing an upstream layer.");

		// if only one upstream connection to a fan-in layer, the layer is redundant
		if(nUpstream == 1)
		{
			// necessary to keep down/upstream locally because remove...() is going to null them out...
			final DownstreamComponent downstream = this.downstream;
			final UpstreamLayer upstream = this.upstream[0];

			// connect downstream layer directly to single upstream layer
			downstream.removeUpstream(this);
			upstream.removeDownstream(this);
			downstream.addUpstream(upstream);

			// tell caller to remove this layer from network
			return false;
		}

		// otherwise we have two or more upstream layers -- this layer is necessary

		// tell caller to keep this layer
		return true;
	}

	@Override
	public void removeUpstream(final int index)
	{
		final UpstreamComponent old = upstream[index];

		// condense the array, leaving the last element "empty"
		for(int i = index + 1; i < nUpstream; i++)
			upstream[i - 1] = upstream[i];

		// decrease the size
		nUpstream--;

		// decrease the array length to fit
		setUpstreamCapacity(nUpstream);

		// tell the about-to-be-removed layer that it should disconnect from us
		disconnectUpstream(old);
	}

	@Override
	public void removeUpstream(final UpstreamComponent upstream)
	{
		removeUpstream(indexOfUpstream(upstream));
	}

	protected void setUpstreamCapacity(final int capacity)
	{
		this.upstream = Arrays.copyOf(this.upstream, capacity);
		this.myIndexInUpstream = Arrays.copyOf(this.myIndexInUpstream, capacity);
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		super.toString(sb);
		sb.pushIndent();

		if(sb.showConnectivity())
		{
			sb.indent();
			sb.append("Upstream: [");
			sb.append(upstream[0].getName());

			for(int i = 1; i < nUpstream; i++)
			{
				sb.append(", ");
				sb.append(upstream[i].getName());
			}

			sb.append("]");
			sb.appendln();
		}

		sb.popIndent();
	}
}
