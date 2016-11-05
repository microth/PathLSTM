package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import dmonner.xlbp.Component;
import dmonner.xlbp.Network;
import dmonner.xlbp.compound.Compound;
import dmonner.xlbp.compound.WeightBank;
import dmonner.xlbp.compound.WeightedCompound;
import dmonner.xlbp.connection.Connection;
import dmonner.xlbp.layer.BiasLayer;
import dmonner.xlbp.layer.Layer;
import dmonner.xlbp.util.CSVWriter;

public class NetworkStat extends AbstractStat
{
	private final ConnectionStat all;

	public NetworkStat(final Network net)
	{
		all = new ConnectionStat();

		// Do a deep dive into net to find all unique Connections
		final Set<Connection> cs = new HashSet<Connection>();
		final Queue<Component> q = new LinkedList<Component>();

		q.add(net);

		while(!q.isEmpty())
		{
			final Component comp = q.poll();

			if(comp instanceof WeightedCompound)
			{
				final WeightedCompound wcomp = (WeightedCompound) comp;
				for(int i = 0; i < wcomp.nUpstreamWeights(); i++)
				{
					final WeightBank bank = wcomp.getUpstreamWeights(i);
					q.add(bank);
				}
			}

			if(comp instanceof WeightBank)
				cs.add(((WeightBank) comp).getConnection());
			else if(comp instanceof BiasLayer)
				cs.add(((BiasLayer) comp).getConnection());
			else if(comp instanceof Compound)
				for(final Component sub : ((Compound) comp).getComponents())
					q.add(sub);
			else if(comp instanceof Network)
				for(final Component sub : ((Network) comp).getComponents())
					q.add(sub);
			else if(comp instanceof Layer)
				; // nothing to do
			else
				throw new IllegalArgumentException("Unhandled subtype of Component: " + comp);
		}

		// Aggregate all the stats for the Connections
		for(final Connection c : cs)
			all.add(new ConnectionStat(c));

		analyze();
	}

	public void add(final NetworkStat that)
	{
		all.add(that.all);
	}

	@Override
	public void add(final Stat that)
	{
		if(that instanceof NetworkStat)
			add((NetworkStat) that);
		else
			throw new IllegalArgumentException("Can only add in NetworkStats.");
	}

	@Override
	public void addTo(final String prefix, final Map<String, Object> map)
	{
		all.addTo(prefix, map);
	}

	@Override
	public void analyze()
	{
		all.analyze();
	}

	@Override
	public void clear()
	{
		all.clear();
	}

	public ConnectionStat getConnectionStat()
	{
		return all;
	}

	@Override
	public void saveData(final CSVWriter out) throws IOException
	{
		all.saveData(out);
	}

	@Override
	public void saveHeader(final String prefix, final CSVWriter out) throws IOException
	{
		all.saveHeader(prefix, out);
	}

	@Override
	public String toString(final String prefix)
	{
		// final StringBuffer sb = new StringBuffer();

		return all.toString(prefix);

		// return sb.toString();
	}
}
