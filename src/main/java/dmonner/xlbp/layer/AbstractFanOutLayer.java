package dmonner.xlbp.layer;

import java.util.Arrays;

import dmonner.xlbp.Component;
import dmonner.xlbp.DownstreamComponent;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.UpstreamComponent;

public abstract class AbstractFanOutLayer extends AbstractDownstreamLayer implements InternalLayer
{
	private static final long serialVersionUID = 1L;

	protected DownstreamLayer[] downstream;
	protected int nDownstream;
	protected int[] myIndexInDownstream;
	protected Responsibilities[] dDownstream;
	protected CopySourceLayer downstreamCopyLayer;

	public AbstractFanOutLayer(final AbstractFanOutLayer that, final NetworkCopier copier)
	{
		super(that, copier);

		this.downstream = new DownstreamLayer[0];
		this.myIndexInDownstream = new int[0];
		this.dDownstream = new Responsibilities[0];
		this.nDownstream = 0;
	}

	public AbstractFanOutLayer(final String name, final int size)
	{
		super(name, size);

		this.downstream = new DownstreamLayer[0];
		this.myIndexInDownstream = new int[0];
		this.dDownstream = new Responsibilities[0];
		this.nDownstream = 0;
	}

	@Override
	public void addDownstream(final DownstreamComponent downstream)
	{
		final int index = nDownstream;
		nDownstream++;
		setDownstreamCapacity(nDownstream);
		this.downstream[index] = downstream.asDownstreamLayer();
		dDownstream[index] = new Responsibilities(size);
		connectDownstream(index);
		this.myIndexInDownstream[index] = downstream.indexOfUpstream(this);
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
	public void aliasResponsibilities(final int index, final Responsibilities resp)
	{
		dDownstream[index] = resp;
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
			super.build();

			// find my index in the downstream layers
			myIndexInDownstream = new int[nDownstream];

			for(int i = 0; i < nDownstream; i++)
			{
				final DownstreamLayer downstream = this.downstream[i];
				final int index = downstream.indexOfUpstream(this);

				if(index < 0)
					throw new IllegalStateException(name + " is not in " + downstream.getName()
							+ " layer's list of connected upstream layers.");

				if(downstream.size() != size())
					throw new IllegalStateException("Incompatible sizes between " + name + " (" + size()
							+ ") and " + downstream.getName() + " (" + downstream.size() + ").");

				myIndexInDownstream[i] = index;
			}

			built = true;
		}
	}

	@Override
	public void clearResponsibilities()
	{
		super.clearResponsibilities();

		for(int i = 0; i < nDownstream; i++)
			dDownstream[i].clear();
	}

	protected void connectDownstream(final int index)
	{
		final DownstreamComponent downstream = this.downstream[index];

		if(!downstream.connectedUpstream(this))
			downstream.addUpstream(this);
	}

	@Override
	public boolean connectedDownstream(final DownstreamComponent downstream)
	{
		return indexOfDownstream(downstream) >= 0;
	}

	@Override
	public abstract AbstractFanOutLayer copy(final NetworkCopier copier);

	@Override
	public AbstractFanOutLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		super.copyConnectivityFrom(comp, copier);

		if(comp instanceof AbstractFanOutLayer)
		{
			final AbstractFanOutLayer that = (AbstractFanOutLayer) comp;
			this.downstream = new DownstreamLayer[that.downstream.length];
			this.myIndexInDownstream = new int[that.myIndexInDownstream.length];
			this.dDownstream = new Responsibilities[that.dDownstream.length];
			this.nDownstream = that.nDownstream;

			for(int i = 0; i < that.downstream.length; i++)
			{
				this.downstream[i] = copier.getCopyIfExists(that.downstream[i]);
				this.myIndexInDownstream[i] = that.myIndexInDownstream[i];
				this.dDownstream[i] = new Responsibilities(size);
			}
		}
	}

	protected void disconnectDownstream(final DownstreamComponent downstream)
	{
		final int index = downstream.indexOfUpstream(this);

		if(index >= 0)
			downstream.removeUpstream(index);
	}

	@Override
	public DownstreamComponent getDownstream()
	{
		return getDownstream(0);
	}

	@Override
	public DownstreamComponent getDownstream(final int index)
	{
		return downstream[index];
	}

	@Override
	public CopySourceLayer getDownstreamCopyLayer()
	{
		return downstreamCopyLayer;
	}

	@Override
	public int getIndexInDownstream()
	{
		return getIndexInDownstream(0);
	}

	@Override
	public int getIndexInDownstream(final int index)
	{
		return myIndexInDownstream[index];
	}

	@Override
	public Responsibilities getResponsibilities(final int index)
	{
		return dDownstream[index];
	}

	@Override
	public int indexOfDownstream(final DownstreamComponent downstream)
	{
		for(int i = 0; i < nDownstream; i++)
			if(this.downstream[i] == downstream)
				return i;

		return -1;
	}

	@Override
	public int nDownstream()
	{
		return nDownstream;
	}

	@Override
	public boolean optimize()
	{
		if(!super.optimize())
			return false;

		// check that there is a upstream connection
		if(upstream == null)
			throw new IllegalStateException(name + " is missing an upstream layer.");

		// check that there is an downstream connection
		if(nDownstream == 0)
			throw new IllegalStateException(name + " is missing a downstream layer.");

		// if only one downstream connection to a fan-in layer, the layer is redundant
		if(nDownstream == 1)
		{
			final UpstreamComponent upstream = this.upstream;
			final DownstreamLayer downstream = this.downstream[0];

			// connect upstream layer directly to single downstream layer
			upstream.removeDownstream(this);
			downstream.removeUpstream(this);
			upstream.addDownstream(downstream);

			// tell caller to remove this layer from network
			return false;
		}

		// otherwise we have two or more downstream layers -- this layer is necessary

		// tell caller to keep this layer
		return true;
	}

	@Override
	public void removeDownstream(final DownstreamComponent downstream)
	{
		removeDownstream(indexOfDownstream(downstream));
	}

	@Override
	public void removeDownstream(final int index)
	{
		final DownstreamComponent old = downstream[index];
		// condense the array, leaving the last element "empty"
		for(int i = index + 1; i < nDownstream; i++)
			downstream[i - 1] = downstream[i];

		// decrease the size
		nDownstream--;

		// decrease the array length to fit
		setDownstreamCapacity(nDownstream);

		// tell the about-to-be-removed layer that it should disconnect from us
		disconnectDownstream(old);
	}

	protected void setDownstreamCapacity(final int capacity)
	{
		downstream = Arrays.copyOf(downstream, capacity);
		myIndexInDownstream = Arrays.copyOf(myIndexInDownstream, capacity);
		dDownstream = Arrays.copyOf(dDownstream, capacity);
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		super.toString(sb);
		sb.pushIndent();

		if(sb.showConnectivity())
		{
			sb.indent();
			sb.append("Downstream: [");
			sb.append(downstream[0].getName());

			for(int i = 1; i < nDownstream; i++)
			{
				sb.append(", ");
				sb.append(downstream[i].getName());
			}

			sb.append("]");
			sb.appendln();
		}

		if(sb.showExtra() && downstreamCopyLayer != null)
			sb.appendln("DownstreamCopyLayer: " + downstreamCopyLayer.getName());

		sb.popIndent();
	}
}
