package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.Map;

import dmonner.xlbp.util.CSVWriter;

public class SetStat extends AbstractStat
{
	private final String name;
	private final TargetSetStat targets;
	private final FractionStat stepsCorrect;
	private final FractionStat trialsCorrect;

	public SetStat()
	{
		this("");
	}

	public SetStat(final String name)
	{
		this.name = name;
		this.targets = new TargetSetStat();
		this.stepsCorrect = new FractionStat("Steps");
		this.trialsCorrect = new FractionStat("Trials");
	}

	public SetStat(final String name, final Iterable<TrialStat> evaluations)
	{
		this(name);

		for(final TrialStat eval : evaluations)
			add(eval);

		analyze();
	}

	public SetStat(final String name, final TrialStat[] evaluations)
	{
		this(name);

		for(final TrialStat eval : evaluations)
			add(eval);

		analyze();
	}

	public void add(final SetStat that)
	{
		targets.add(that.targets);
		stepsCorrect.add(that.stepsCorrect);
		trialsCorrect.add(that.trialsCorrect);
	}

	@Override
	public void add(final Stat that)
	{
		if(that instanceof SetStat)
			add((SetStat) that);
		else if(that instanceof TrialStat)
			add((TrialStat) that);
		else
			throw new IllegalArgumentException("Can only add in SetStats or TrialStats.");
	}

	public void add(final TrialStat that)
	{
		targets.add(that.getTargets());
		stepsCorrect.add(that.getStepsCorrect());
		trialsCorrect.add(that.getTrialCorrect());
	}

	@Override
	public void addTo(final String prefix, final Map<String, Object> map)
	{
		map.put(prefix + "SetName", name);
		trialsCorrect.addTo(prefix, map);
		stepsCorrect.addTo(prefix, map);
		targets.addTo(prefix, map);
	}

	@Override
	public void analyze()
	{
		targets.analyze();
		stepsCorrect.analyze();
		trialsCorrect.analyze();
	}

	@Override
	public void clear()
	{
		targets.clear();
		stepsCorrect.clear();
		trialsCorrect.clear();
	}

	public FractionStat getStepStats()
	{
		return stepsCorrect;
	}

	public TargetSetStat getTargetStats()
	{
		return targets;
	}

	public FractionStat getTrialStats()
	{
		return trialsCorrect;
	}

	@Override
	public void saveData(final CSVWriter out) throws IOException
	{
		out.beginRecord();
		out.appendField(name);
		trialsCorrect.saveData(out);
		stepsCorrect.saveData(out);
		targets.saveData(out);
	}

	@Override
	public void saveHeader(final String prefix, final CSVWriter out) throws IOException
	{
		out.beginRecord();
		out.appendHeader(prefix + "name");
		trialsCorrect.saveHeader(prefix, out);
		stepsCorrect.saveHeader(prefix, out);
		targets.saveHeader(prefix, out);
	}

	@Override
	public String toString(final String prefix)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("SetName = ");
		sb.append(prefix);
		sb.append(name);
		sb.append("\n");

		sb.append(trialsCorrect.toString(prefix));
		sb.append(stepsCorrect.toString(prefix));
		sb.append(targets.toString(prefix));

		return sb.toString();
	}
}
