package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.Map;

import dmonner.xlbp.connection.Connection;
import dmonner.xlbp.util.CSVWriter;

public class ConnectionStat extends AbstractStat
{
	private final MeanVarStat weights;
	private final FractionStat connections;

	public ConnectionStat()
	{
		weights = new MeanVarStat("Weights");
		connections = new FractionStat("Connections");
	}

	public ConnectionStat(final Connection conn)
	{
		this();

		final float[][] w = conn.toMatrix();
		for(final float[] row : w)
			for(final float wt : row)
				weights.add(Math.abs(wt));

		connections.add(conn.nWeights(), conn.nWeightsPossible());

		analyze();
	}

	public void add(final ConnectionStat that)
	{
		weights.add(that.weights);
		connections.add(that.connections);
	}

	@Override
	public void add(final Stat that)
	{
		if(that instanceof ConnectionStat)
			add((ConnectionStat) that);
		else
			throw new IllegalArgumentException("Can only add in ConnectionStats.");
	}

	@Override
	public void addTo(final String prefix, final Map<String, Object> map)
	{
		weights.addTo(prefix, map);
		connections.addTo(prefix, map);
	}

	@Override
	public void analyze()
	{
		weights.analyze();
		connections.analyze();
	}

	@Override
	public void clear()
	{
		weights.clear();
		connections.clear();
	}

	public FractionStat getConnections()
	{
		return connections;
	}

	public MeanVarStat getWeights()
	{
		return weights;
	}

	@Override
	public void saveData(final CSVWriter out) throws IOException
	{
		weights.saveData(out);
		connections.saveData(out);
	}

	@Override
	public void saveHeader(final String prefix, final CSVWriter out) throws IOException
	{
		weights.saveHeader(prefix, out);
		connections.saveHeader(prefix, out);
	}

	@Override
	public String toString(final String prefix)
	{
		final StringBuffer sb = new StringBuffer();

		sb.append(weights.toString(prefix));
		sb.append(connections.toString(prefix));

		return sb.toString();
	}

}
