package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.connection.LayerConnection;

public class WeightSenderLayer extends AbstractDownstreamLayer
{
	private static final long serialVersionUID = 1L;

	private LayerConnection out;

	public WeightSenderLayer(final String name, final int size)
	{
		super(name, size);
	}

	public WeightSenderLayer(final WeightSenderLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	@Override
	public void activateTest()
	{
		// Nothing to do -- activations are aliased from upstream layer.
	}

	@Override
	public void activateTrain()
	{
		// Nothing to do -- activations are aliased from upstream layer.
	}

	@Override
	public void aliasResponsibilities(final int index, final Responsibilities resp)
	{
		super.aliasResponsibilities(index, resp);
		upstream.aliasResponsibilities(myIndexInUpstream, resp);
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			upstream.build();
			y = upstream.getActivations();
			d = new Responsibilities(size);
			upstream.aliasResponsibilities(myIndexInUpstream, d);

			built = true;
		}
	}

	@Override
	public void clear()
	{
		// Nothing to do.
	}

	@Override
	public void clearActivations()
	{
		// Nothing to do -- activations are aliased from upstream layer.
	}

	@Override
	public void clearEligibilities()
	{
		// Nothing to do -- these will get scrubbed by the receiving layer.
	}

	@Override
	public void clearResponsibilities()
	{
		// Nothing to do -- these will get scrubbed by the upstream layer.
	}

	@Override
	public WeightSenderLayer copy(final NetworkCopier copier)
	{
		return new WeightSenderLayer(this, copier);
	}

	@Override
	public WeightSenderLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	public LayerConnection getConnection()
	{
		return out;
	}

	public void removeConnection()
	{
		if(out != null)
		{
			final WeightReceiverLayer to = out.getToLayer();
			out = null;
			if(to.getConnection() != null)
				to.removeConnection();
		}
	}

	public void setConnection(final LayerConnection connection)
	{
		if(this.out != null)
			throw new IllegalStateException(name + " already has an output connection.");

		final WeightSenderLayer from = connection.getFromLayer();
		final WeightReceiverLayer to = connection.getToLayer();

		if(from != this)
			throw new IllegalStateException("Connection " + connection.getName() + " comes from " + from
					+ ", not " + name + ".");

		this.out = connection;

		if(to.getConnection() != connection)
			to.setConnection(connection);
	}

	@Override
	public void updateEligibilities()
	{
		// Nothing to do -- no possible downstream CopySource layer.
	}

	@Override
	public void updateResponsibilities()
	{
		out.updateResponsibilities();
	}

	@Override
	public void updateUpstreamResponsibilities(final int index)
	{
		// Nothing to do -- upstream ds are already aliased to this layer's d.
	}
}
