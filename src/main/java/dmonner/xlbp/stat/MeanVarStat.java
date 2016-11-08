package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.Map;

import dmonner.xlbp.util.CSVWriter;

public class MeanVarStat extends AbstractStat
{
	private final String name;
	private float sum;
	private float sumsq;
	private float n;
	private float mean;
	private float var;

	public MeanVarStat(final MeanVarStat that)
	{
		this.name = that.name;
		this.sum = that.sum;
		this.sumsq = that.sumsq;
		this.n = that.n;
		this.mean = that.mean;
		this.var = that.var;
	}

	public MeanVarStat(final String name)
	{
		this.name = name;
	}

	public void add(final float obs)
	{
		sum += obs;
		sumsq += obs * obs;
		n++;
	}

	public void add(final MeanVarStat that)
	{
		sum += that.sum;
		sumsq += that.sumsq;
		n += that.n;
	}

	@Override
	public void add(final Stat that)
	{
		if(that instanceof MeanVarStat)
			add((MeanVarStat) that);
		else
			throw new IllegalArgumentException("Can only add in other MeanVarStats.");
	}

	@Override
	public void addTo(final String prefix, final Map<String, Object> map)
	{
		map.put(prefix + name + "Mean", mean);
		map.put(prefix + name + "Var", var);
	}

	@Override
	public void analyze()
	{
		mean = sum / n;
		var = (sumsq - 2 * sum * mean) / n + mean * mean;
	}

	@Override
	public void clear()
	{
		sum = 0;
		sumsq = 0;
		n = 0;
		mean = 0;
		var = 0;
	}

	public float getMean()
	{
		return mean;
	}

	public float getVar()
	{
		return var;
	}

	@Override
	public void saveData(final CSVWriter out) throws IOException
	{
		out.appendField(mean);
		out.appendField(var);
	}

	@Override
	public void saveHeader(final String prefix, final CSVWriter out) throws IOException
	{
		out.appendHeader(prefix + name + "Mean");
		out.appendHeader(prefix + name + "Var");
	}

	@Override
	public String toString(final String prefix)
	{
		final StringBuilder sb = new StringBuilder();

		final String prename = prefix + name;

		sb.append(prename);
		sb.append("Mean = ");
		sb.append(mean);
		sb.append("\n");

		sb.append(prename);
		sb.append("Variance = ");
		sb.append(var);
		sb.append("\n");

		return sb.toString();
	}

}
