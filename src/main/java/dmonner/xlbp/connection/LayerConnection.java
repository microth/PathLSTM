package dmonner.xlbp.connection;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.layer.WeightReceiverLayer;
import dmonner.xlbp.layer.WeightSenderLayer;

public abstract class LayerConnection implements Connection
{
	private static final long serialVersionUID = 1L;

	protected final String name;
	protected final WeightReceiverLayer to;
	protected final WeightSenderLayer from;
	protected Responsibilities dbuf;
	protected boolean built;

	public LayerConnection(final LayerConnection that, final NetworkCopier copier)
	{
		this.name = copier.getCopyNameFrom(that);
		this.to = copier.getCopyOf(that.to);
		this.from = copier.getCopyOf(that.from);
		this.built = that.built;

		if(that.built)
			if(copier.copyState())
				this.dbuf = that.dbuf.copy();
			else
				this.dbuf = new Responsibilities(that.dbuf.size());
	}

	public LayerConnection(final String name, final WeightReceiverLayer to,
			final WeightSenderLayer from)
	{
		this.name = name;
		this.to = to;
		this.from = from;
	}

	public LayerConnection(final WeightReceiverLayer to, final WeightSenderLayer from)
	{
		this(from.getName() + "To" + to.getName(), to, from);
	}

	@Override
	public void build()
	{
		if(!built)
		{
			dbuf = new Responsibilities(getToLayer().size());
			built = true;
		}
	}

	@Override
	public abstract LayerConnection copy(NetworkCopier copier);

	public abstract float[] getCachedInput();

	public WeightSenderLayer getFromLayer()
	{
		return from;
	}

	public float[] getFromLayerResponsibilities()
	{
		final WeightSenderLayer from = getFromLayer();
		final Responsibilities resp = from.getResponsibilities();
		resp.touch(); // in case array was empty, so we don't think it is after we modify it in caller.
		return resp.get();
	}

	@Override
	public String getName()
	{
		return name;
	}

	public WeightReceiverLayer getToLayer()
	{
		return to;
	}

	public float[] getToLayerResponsibilities()
	{
		final WeightReceiverLayer to = getToLayer();

		if(to.getDownstreamCopyLayer() != null)
		{
			// the deltas to pass back are the product of the copy layer's deltas and
			final Responsibilities copyresp = to.getDownstreamCopyLayer().getResponsibilities();
			// the local layer's pre-copy deltas
			final Responsibilities resp = to.getResponsibilities();
			// multiply them and use that as the delta to pass downstream
			dbuf.copyMul(copyresp, resp.get());			
			return dbuf.get();
		}
		// otherwise, we just use the local layer's deltas
		else
		{
			final Responsibilities resp = to.getResponsibilities();

			// if those responsibilities are empty, return
			if(resp.empty())
				return null;

			return resp.get();
		}
	}

	@Override
	public int nWeightsPossible()
	{
		return to.size() * from.size();
	}

	@Override
	public String toString(final String show)
	{
		final NetworkStringBuilder sb = new NetworkStringBuilder(show);
		toString(sb);
		return sb.toString();
	}

	@Override
	public void unbuild()
	{
		built = false;
	}

	public abstract void updateResponsibilities();

}
