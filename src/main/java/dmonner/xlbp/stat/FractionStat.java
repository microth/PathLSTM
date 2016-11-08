package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.Map;

import dmonner.xlbp.util.CSVWriter;

public class FractionStat extends AbstractStat
{
	private final String name;
	private int actual;
	private int possible;
	private float fraction;

	public FractionStat(final FractionStat that)
	{
		this.name = that.name;
		this.actual = that.actual;
		this.possible = that.possible;
		this.fraction = that.fraction;
	}

	public FractionStat(final String name)
	{
		this.name = name;
	}

	public void add(final FractionStat that)
	{
		this.actual += that.actual;
		this.possible += that.possible;
	}

	public void add(final int actual, final int possible)
	{
		this.actual += actual;
		this.possible += possible;
	}

	@Override
	public void add(final Stat that)
	{
		if(that instanceof FractionStat)
			add((FractionStat) that);
		else
			throw new IllegalArgumentException("Can only add in other FractionStats.");
	}

	@Override
	public void addTo(final String prefix, final Map<String, Object> map)
	{
		map.put(prefix + name + "Fraction", fraction);
		map.put(prefix + name + "Actual", actual);
		map.put(prefix + name + "Possible", possible);
	}

	@Override
	public void analyze()
	{
		fraction = ((float) actual) / possible;
	}

	@Override
	public void clear()
	{
		actual = 0;
		possible = 0;
		fraction = 0;
	}

	public int getActual()
	{
		return actual;
	}

	public float getFraction()
	{
		return fraction;
	}

	public int getPossible()
	{
		return possible;
	}

	@Override
	public void saveData(final CSVWriter out) throws IOException
	{
		out.appendField(fraction);
		out.appendField(actual);
		out.appendField(possible);
	}

	@Override
	public void saveHeader(final String prefix, final CSVWriter out) throws IOException
	{
		out.appendHeader(prefix + name + "Fraction");
		out.appendHeader(prefix + name + "Actual");
		out.appendHeader(prefix + name + "Possible");
	}

	@Override
	public String toString(final String prefix)
	{
		final StringBuilder sb = new StringBuilder();

		final String prename = prefix + name;

		sb.append(prename);
		sb.append("Fraction = ");
		sb.append(fraction);
		sb.append("\n");

		sb.append(prename);
		sb.append("Actual = ");
		sb.append(actual);
		sb.append("\n");

		sb.append(prename);
		sb.append("Possible = ");
		sb.append(possible);
		sb.append("\n");

		return sb.toString();
	}
}
