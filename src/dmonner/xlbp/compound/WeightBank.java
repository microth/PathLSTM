package dmonner.xlbp.compound;

import java.util.LinkedList;
import java.util.Queue;

import dmonner.xlbp.Component;
import dmonner.xlbp.DownstreamComponent;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.UpstreamComponent;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.connection.AdjacencyListConnection;
import dmonner.xlbp.connection.AdjacencyMatrixConnection;
import dmonner.xlbp.connection.LayerConnection;
import dmonner.xlbp.layer.DownstreamLayer;
import dmonner.xlbp.layer.UpstreamLayer;
import dmonner.xlbp.layer.WeightReceiverLayer;
import dmonner.xlbp.layer.WeightSenderLayer;
import dmonner.xlbp.layer.WeightedLayer;

public class WeightBank implements Component
{
	private static final long serialVersionUID = 1L;

	private final String name;
	private final WeightSenderLayer wsend;
	private final WeightReceiverLayer wrecv;
	private WeightUpdaterType wut;
	private WeightInitializer win;
	private final UpstreamLayer upstream;
	private final DownstreamLayer downstream;
	private Boolean truncate;
	private boolean built;

	public WeightBank(final String name, final UpstreamLayer upstream,
			final DownstreamLayer downstream, final WeightInitializer win, final WeightUpdaterType wut)
	{
		this.name = name;
		this.win = win;
		this.wut = wut;
		this.upstream = upstream;
		this.downstream = downstream;

		// Create intervening weight layers
		this.wsend = new WeightSenderLayer(name + "WeightSender", upstream.size());
		this.wrecv = new WeightReceiverLayer(name + "WeightReceiver", downstream.size());

		// Connect everything up
		downstream.addUpstream(wrecv);
		wsend.addUpstream(upstream);

		// Create the connection
		installConnection();
	}

	public WeightBank(final WeightBank that, final NetworkCopier copier)
	{
		this.name = copier.getCopyNameFrom(that);
		this.win = that.win;
		this.wut = that.wut;
		this.truncate = that.truncate;
		this.built = that.built;

		this.upstream = copier.getCopyOf(that.upstream);
		this.downstream = copier.getCopyOf(that.downstream);
		this.wsend = copier.getCopyOf(that.wsend);
		this.wrecv = copier.getCopyOf(that.wrecv);
	}

	@Override
	public void activateTest()
	{
		wrecv.activateTest();
	}

	@Override
	public void activateTrain()
	{
		wrecv.activateTrain();
	}

	@Override
	public void build()
	{
		if(!built)
		{
			wsend.build();
			wrecv.build();

			checkForUpstreamWeights();

			built = true;
		}
	}

	private void checkForUpstreamWeights()
	{
		// if we haven't explicitly set a truncate value, check the network for clues
		if(truncate == null)
		{
			// set truncate to true (unless we find an upstream weighted layer below)
			truncate = true;

			// start just upstream of the weight sender
			final Queue<UpstreamComponent> q = new LinkedList<UpstreamComponent>();
			q.add(wsend.getUpstream());

			// while there are more upstream components to look at
			while(!q.isEmpty())
			{
				final UpstreamComponent current = q.remove();

				// if we find a set of weights, don't truncate; we can quit.
				if(current instanceof WeightedLayer)
				{
					truncate = false;
					break;
				}

				// otherwise, continue to look upstream
				if(current instanceof DownstreamLayer)
				{
					final DownstreamComponent currDown = (DownstreamComponent) current;
					for(int i = 0; i < currDown.nUpstream(); i++)
						q.add(currDown.getUpstream(i));
				}
			}
		}
	}

	@Override
	public void clear()
	{
		clearActivations();
		clearEligibilities();
		clearResponsibilities();
	}

	@Override
	public void clearActivations()
	{
		wrecv.clearActivations();
		// wsend.clearActivations();
	}

	@Override
	public void clearEligibilities()
	{
		wrecv.clearEligibilities();
	}

	@Override
	public void clearResponsibilities()
	{
		wrecv.clearResponsibilities();
		// wsend.clearResponsibilities();
	}

	@Override
	public int compareTo(final Component that)
	{
		return name.compareTo(that.getName());
	}

	@Override
	public WeightBank copy(final NetworkCopier copier)
	{
		return new WeightBank(this, copier);
	}

	@Override
	public WeightBank copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final WeightBank copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		// Nothing to do.
	}

	public LayerConnection getConnection()
	{
		return wrecv.getConnection();
	}

	public DownstreamLayer getDownstream()
	{
		return downstream;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public UpstreamLayer getUpstream()
	{
		return upstream;
	}

	public WeightReceiverLayer getWeightInput()
	{
		return wrecv;
	}

	public WeightSenderLayer getWeightOutput()
	{
		return wsend;
	}

	protected void installConnection()
	{
		final LayerConnection conn = makeConnection();
		conn.setWeightInitializer(win);
		conn.setWeightUpdater(wut);
		wrecv.removeConnection();
		wrecv.setConnection(conn);
	}

	@Override
	public boolean isBuilt()
	{
		return built;
	}

	protected LayerConnection makeConnection()
	{
		if(win.fullConnectivity())
			return new AdjacencyMatrixConnection(name, wrecv, wsend);
		else
			return new AdjacencyListConnection(name, wrecv, wsend);
	}

	@Override
	public int nWeights()
	{
		return wrecv.getConnection().nWeights();
	}

	@Override
	public boolean optimize()
	{
		wsend.optimize();
		wrecv.optimize();

		return true;
	}

	@Override
	public void processBatch()
	{
		wrecv.processBatch();
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		this.win = win;
		installConnection();
	}

	@Override
	public void setWeightUpdaterType(final WeightUpdaterType wut)
	{
		this.wut = wut;
		installConnection();
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		if(sb.showIntermediate())
		{
			if(sb.showName())
			{
				sb.indent();
				sb.append(name);
				sb.append(" : ");
				sb.append(this.getClass().getSimpleName());
				sb.appendln();
			}

			if(sb.showExtra())
			{
				sb.indent();
				sb.append("Truncated? ");
				sb.append(String.valueOf(truncate));
				sb.appendln();
			}

			sb.pushIndent();
			wrecv.toString(sb);
			wsend.toString(sb);
			sb.popIndent();
		}
		else
		{
			wrecv.getConnection().toString(sb);
		}
	}

	@Override
	public String toString(final String show)
	{
		final NetworkStringBuilder sb = new NetworkStringBuilder(show);
		toString(sb);
		return sb.toString();
	}

	public void truncate(final boolean truncate)
	{
		this.truncate = truncate;
	}

	@Override
	public void unbuild()
	{
		built = false;
		wsend.unbuild();
		wrecv.unbuild();
	}

	@Override
	public void updateEligibilities()
	{
		wrecv.updateEligibilities();
	}

	@Override
	public void updateResponsibilities()
	{
		wrecv.updateResponsibilities();
		if(!truncate)
			wsend.updateResponsibilities();
	}

	@Override
	public void updateWeights()
	{
		wrecv.updateWeights();
	}

}
