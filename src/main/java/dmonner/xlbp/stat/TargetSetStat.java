package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import dmonner.xlbp.layer.Layer;
import dmonner.xlbp.util.CSVWriter;

public class TargetSetStat extends AbstractStat
{
	private final Map<Layer, TargetStat> targets;
	private final BitStat bits;
	private final ErrorStat error;
	private final FractionStat correct;
	private int size;

	public TargetSetStat()
	{
		this.targets = new TreeMap<Layer, TargetStat>();
		this.bits = new BitStat("TotalBits");
		this.error = new ErrorStat("TotalError");
		this.correct = new FractionStat("TotalCorrect");
	}

	@Override
	public void add(final Stat that)
	{
		if(that instanceof TargetStat)
			add((TargetStat) that);
		else if(that instanceof TargetSetStat)
			add((TargetSetStat) that);
		else
			throw new IllegalArgumentException("Can only add in other StepStats.");
	}

	public void add(final TargetSetStat that)
	{
		size += that.size;
		for(final TargetStat stat : that.targets.values())
			add(stat);
	}

	public void add(final TargetStat that)
	{
		final TargetStat existing = targets.get(that.getLayer());

		if(existing == null)
			targets.put(that.getLayer(), new TargetStat(that));
		else
			existing.add(that);

		size++;
	}

	@Override
	public void addTo(final String prefix, final Map<String, Object> map)
	{
		for(final TargetStat stat : targets.values())
			stat.addTo(prefix, map);
	}

	@Override
	public void analyze()
	{
		bits.clear();
		error.clear();
		correct.clear();

		for(final TargetStat stat : targets.values())
		{
			stat.analyze();
			bits.add(stat.getBits());
			error.add(stat.getError());
			correct.add(stat.getCorrect());
		}

		bits.analyze();
		error.analyze();
		correct.analyze();
	}

	@Override
	public void clear()
	{
		targets.clear();
		bits.clear();
		error.clear();
		correct.clear();
	}

	public BitStat getBits()
	{
		return bits;
	}

	public FractionStat getCorrect()
	{
		return correct;
	}

	public ErrorStat getError()
	{
		return error;
	}

	public TargetStat getTargetStat(final Layer layer)
	{
		return targets.get(layer);
	}

	public Collection<TargetStat> getTargetStats()
	{
		return targets.values();
	}

	@Override
	public void saveData(final CSVWriter out) throws IOException
	{
		correct.saveData(out);
		error.saveData(out);
		bits.saveData(out);
		for(final TargetStat stat : targets.values())
			stat.saveData(out);
	}

	@Override
	public void saveHeader(final String prefix, final CSVWriter out) throws IOException
	{
		correct.saveHeader(prefix, out);
		error.saveHeader(prefix, out);
		bits.saveHeader(prefix, out);
		for(final TargetStat stat : targets.values())
			stat.saveHeader(prefix, out);
	}

	public int size()
	{
		return size;
	}

	@Override
	public String toString(final String prefix)
	{
		final StringBuffer sb = new StringBuffer();

		sb.append(correct.toString(prefix));
		sb.append(error.toString(prefix));
		sb.append(bits.toString(prefix));
		for(final TargetStat stat : targets.values())
			sb.append(stat.toString(prefix));

		return sb.toString();
	}
}
