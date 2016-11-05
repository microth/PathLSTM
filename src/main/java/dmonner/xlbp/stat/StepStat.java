package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.Map;

import dmonner.xlbp.Target;
import dmonner.xlbp.trial.Step;
import dmonner.xlbp.util.CSVWriter;

public class StepStat extends AbstractStat
{
	private final Step step;
	private final TargetSetStat targets;
	private final FractionStat correct;

	public StepStat(final Step step)
	{
		this.step = step;
		this.targets = new TargetSetStat();
		this.correct = new FractionStat("Step");

		analyze();
	}

	@Override
	public void add(final Stat that)
	{
		throw new IllegalArgumentException("Can only get data directly from a Step.");
	}

	@Override
	public void addTo(final String prefix, final Map<String, Object> map)
	{
		targets.addTo(prefix, map);
		correct.addTo(prefix, map);
	}

	@Override
	public void analyze()
	{
		for(final Target target : step.getTargets())
		{
			final TargetStat stat = new TargetStat(target.getLayer());
			stat.compare(target.getValue());
			stat.analyze();
			targets.add(stat);
		}

		targets.analyze();
		final int possible = targets.size() > 0 ? 1 : 0;
		final int actual = targets.getCorrect().getFraction() == 1F ? possible : 0;
		correct.add(actual, possible);
		correct.analyze();
	}

	@Override
	public void clear()
	{
		targets.clear();
		correct.clear();
	}

	public FractionStat getCorrect()
	{
		return correct;
	}

	public Step getStep()
	{
		return step;
	}

	public TargetSetStat getTargets()
	{
		return targets;
	}

	@Override
	public void saveData(final CSVWriter out) throws IOException
	{
		correct.saveData(out);
		targets.saveData(out);
	}

	@Override
	public void saveHeader(final String prefix, final CSVWriter out) throws IOException
	{
		correct.saveHeader(prefix, out);
		targets.saveHeader(prefix, out);
	}

	@Override
	public String toString(final String prefix)
	{
		final StringBuffer sb = new StringBuffer();

		sb.append(correct.toString(prefix));
		sb.append(targets.toString(prefix));

		return sb.toString();
	}

}
