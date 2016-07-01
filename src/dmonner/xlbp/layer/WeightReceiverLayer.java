package dmonner.xlbp.layer;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.connection.LayerConnection;

public class WeightReceiverLayer extends AbstractUpstreamLayer implements WeightedLayer
{
	private static final long serialVersionUID = 1L;

	private LayerConnection in;

	public WeightReceiverLayer(final String name, final int size)
	{
		super(name, size);
	}

	public WeightReceiverLayer(final WeightReceiverLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	@Override
	public void activateTest()
	{
		in.activateTest();
	}

	@Override
	public void activateTrain()
	{
		in.activateTrain();
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			if(in == null)
				throw new IllegalStateException("WeightReceiverLayer " + name
						+ " has no incoming weight Connection.");

			y = new float[size];
			d = new Responsibilities(size);
			in.build();

			built = true;
		}
	}

	@Override
	public void clearEligibilities()
	{
		in.clear();
	}

	@Override
	public WeightReceiverLayer copy(final NetworkCopier copier)
	{
		return new WeightReceiverLayer(this, copier);
	}

	@Override
	public WeightReceiverLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		super.copyConnectivityFrom(comp, copier);

		if(comp instanceof WeightReceiverLayer)
		{
			final WeightReceiverLayer that = (WeightReceiverLayer) comp;
			if(that.in != null && copier.copyWeights())
				setConnection(that.in.copy(copier));
		}
	}

	public LayerConnection getConnection()
	{
		return in;
	}

	@Override
	public int nWeights()
	{
		return in.nWeights();
	}

	@Override
	public void processBatch()
	{
		if(in != null)
			in.processBatch();
	}

	public void removeConnection()
	{
		if(in != null)
		{
			final WeightSenderLayer from = in.getFromLayer();
			in = null;
			if(from.getConnection() != null)
				from.removeConnection();
		}
	}

	public void setConnection(final LayerConnection connection)
	{
		if(this.in != null)
			throw new IllegalStateException(name + " already has an input connection.");

		final WeightSenderLayer from = connection.getFromLayer();
		final WeightReceiverLayer to = connection.getToLayer();

		if(to != this)
			throw new IllegalStateException("Connection " + connection.getName() + " goes to " + to
					+ ", not " + name + ".");

		this.in = connection;

		if(from.getConnection() != connection)
			from.setConnection(connection);
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		if(in != null)
			in.setWeightInitializer(win);
	}

	@Override
	public void setWeightUpdaterType(final WeightUpdaterType wut)
	{
		if(in != null)
			in.setWeightUpdater(wut);
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		super.toString(sb);
		sb.pushIndent();
		if(in != null)
			in.toString(sb);
		sb.popIndent();
	}

	@Override
	public void unbuild()
	{
		super.unbuild();
		if(in != null)
			in.unbuild();
	}

	@Override
	public void updateEligibilities()
	{
		if(downstreamCopyLayer != null)
		{
			downstream.updateUpstreamResponsibilities(myIndexInDownstream);
			in.updateEligibilities(d, downstreamCopyLayer.getPreviousResponsibilities());
		}
	}

	@Override
	public void updateResponsibilities()
	{
		if(downstreamCopyLayer == null) {
			downstream.updateUpstreamResponsibilities(myIndexInDownstream);
		}
	}

	@Override
	public void updateWeights()
	{
		if(downstreamCopyLayer == null)
			in.updateWeightsFromInputs(d);
		else
			in.updateWeightsFromEligibilities(downstreamCopyLayer.getResponsibilities());
	}
}
