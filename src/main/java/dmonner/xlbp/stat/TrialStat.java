package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dmonner.xlbp.trial.Step;
import dmonner.xlbp.trial.Trial;
import dmonner.xlbp.util.CSVWriter;

public class TrialStat extends AbstractStat
{
	private final Trial trial;
	private final List<StepStat> steps;
	private final TargetSetStat targets;
	private final FractionStat stepsCorrect;
	private final FractionStat trialCorrect;

	public TrialStat(final Trial trial)
	{
		this.trial = trial;
		this.steps = new ArrayList<>(trial.size());
		this.targets = new TargetSetStat();
		this.stepsCorrect = new FractionStat("Steps");
		this.trialCorrect = new FractionStat("Trial");

		for(final Step step : trial.getSteps())
			add(step.getLastEvaluation());

		analyze();
	}

	@Override
	public void add(final Stat that)
	{
		if(that instanceof StepStat)
			add((StepStat) that);
		else
			throw new IllegalArgumentException("Can only add in StepStats.");
	}

	public void add(final StepStat step)
	{
		targets.add(step.getTargets());
		stepsCorrect.add(step.getCorrect());
	}

	@Override
	public void addTo(final String prefix, final Map<String, Object> map)
	{
		targets.addTo(prefix, map);
		stepsCorrect.addTo(prefix, map);
	}

	@Override
	public void analyze()
	{
		targets.analyze();
		stepsCorrect.analyze();
		final int correct = stepsCorrect.getFraction() == 1F ? 1 : 0;
		final int possible = stepsCorrect.getPossible() > 0 ? 1 : 0;
		trialCorrect.add(correct, possible);
		trialCorrect.analyze();
	}

	@Override
	public void clear()
	{
		targets.clear();
		stepsCorrect.clear();
		trialCorrect.clear();
	}

	public List<StepStat> getSteps()
	{
		return steps;
	}

	public FractionStat getStepsCorrect()
	{
		return stepsCorrect;
	}

	public TargetSetStat getTargets()
	{
		return targets;
	}

	public Trial getTrial()
	{
		return trial;
	}

	public FractionStat getTrialCorrect()
	{
		return trialCorrect;
	}

	@Override
	public void saveData(final CSVWriter out) throws IOException
	{
		trialCorrect.saveData(out);
		stepsCorrect.saveData(out);
		targets.saveData(out);
	}

	@Override
	public void saveHeader(final String prefix, final CSVWriter out) throws IOException
	{
		trialCorrect.saveHeader(prefix, out);
		stepsCorrect.saveHeader(prefix, out);
		targets.saveHeader(prefix, out);
	}

	protected void saveMetaData(final CSVWriter out) throws IOException
	{
		out.beginRecord();
		trialCorrect.saveData(out);
		stepsCorrect.saveData(out);
		targets.saveData(out);
	}

	protected void saveMetaHeader(final CSVWriter out) throws IOException
	{
		out.beginRecord();
		trialCorrect.saveHeader(out);
		stepsCorrect.saveHeader(out);
		targets.saveHeader(out);
	}

	public int size()
	{
		return steps.size();
	}

	@Override
	public String toString(final String prefix)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append(trialCorrect.toString(prefix));
		sb.append(stepsCorrect.toString(prefix));
		sb.append(targets.toString(prefix));

		return sb.toString();
	}
}
