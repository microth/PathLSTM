package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.Map;

import dmonner.xlbp.layer.Layer;
import dmonner.xlbp.util.CSVWriter;

public class TargetStat extends AbstractStat
{
	private final String name;
	private final Layer layer;
	private final BitStat bits;
	private final BitDistStat dist;
	private final ErrorStat error;
	private final FractionStat correct;

	public TargetStat(final Layer layer)
	{
		this(layer.getName(), layer);
	}

	public TargetStat(final String name, final Layer layer)
	{
		this.name = name;
		this.layer = layer;
		this.bits = new BitStat(name);
		this.dist = new BitDistStat(name, layer.size());
		this.error = new ErrorStat(name);
		this.correct = new FractionStat(name);
	}

	public TargetStat(final TargetStat that)
	{
		this.name = that.name;
		this.layer = that.layer;
		this.bits = new BitStat(that.bits);
		this.dist = new BitDistStat(that.dist);
		this.error = new ErrorStat(that.error);
		this.correct = new FractionStat(that.correct);
	}

	@Override
	public void add(final Stat that)
	{
		if(that instanceof TargetStat)
			add((TargetStat) that);
		else
			throw new IllegalArgumentException("Can only add in other TargetStats.");
	}

	public void add(final TargetStat that)
	{
		if(this.layer != that.layer)
			throw new IllegalArgumentException("Can only add in TargetStats that share a Layer.");

		bits.add(that.bits);
		dist.add(that.dist);
		error.add(that.error);
		correct.add(that.correct);
	}

	@Override
	public void addTo(final String prefix, final Map<String, Object> map)
	{
		correct.addTo(prefix, map);
		bits.addTo(prefix, map);
		dist.addTo(prefix, map);
		error.addTo(prefix, map);
	}

	@Override
	public void analyze()
	{
		bits.analyze();
		dist.analyze();
		error.analyze();
		correct.analyze();
	}

	@Override
	public void clear()
	{
		bits.clear();
		dist.clear();
		error.clear();
		correct.clear();
	}

	public void compare(final float[] target)
	{
		final float[] output = layer.getActivations();
		bits.compare(target, output);
		dist.compare(target, output);
		error.compare(target, output);
		bits.analyze();

		final int possible = 1;
		correct.add(bits.getIncorrect() == 0 ? possible : 0, possible);
	}

	public BitStat getBits()
	{
		return bits;
	}

	public FractionStat getCorrect()
	{
		return correct;
	}

	public BitDistStat getDist()
	{
		return dist;
	}

	public ErrorStat getError()
	{
		return error;
	}

	public Layer getLayer()
	{
		return layer;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public void saveData(final CSVWriter out) throws IOException
	{
		correct.saveData(out);
		bits.saveData(out);
		dist.saveData(out);
		error.saveData(out);
	}

	@Override
	public void saveHeader(final String prefix, final CSVWriter out) throws IOException
	{
		correct.saveHeader(prefix, out);
		bits.saveHeader(prefix, out);
		dist.saveHeader(prefix, out);
		error.saveHeader(prefix, out);
	}

	@Override
	public String toString(final String prefix)
	{
		final StringBuffer sb = new StringBuffer();

		sb.append(correct.toString(prefix));
		sb.append(bits.toString(prefix));
		sb.append(dist.toString(prefix));
		sb.append(error.toString(prefix));

		return sb.toString();
	}
}
