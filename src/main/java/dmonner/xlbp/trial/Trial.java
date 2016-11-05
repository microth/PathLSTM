package dmonner.xlbp.trial;

import java.util.ArrayList;
import java.util.List;

import dmonner.xlbp.Network;
import dmonner.xlbp.stat.TrialStat;

public class Trial
{
	private final Network meta;
	private final List<Step> steps;
	private TrialStat evaluation;
	private TrialRecord recording;
	private boolean clear;
	private boolean evaluate;
	private boolean record;
	private boolean clearInputs;
	private boolean known;

	public Trial(final Network meta)
	{
		this.meta = meta;
		this.steps = new ArrayList<Step>();
		this.clear = true;
		this.clearInputs = true;
		this.evaluate = true;
		this.record = true;
		this.known = false;
	}

	public void clear()
	{
		evaluation = null;
		recording = null;
		for(final Step step : steps)
			step.clear();
	}

	public Step currentStep()
	{
		return steps.get(steps.size() - 1);
	}

	@Override
	public boolean equals(final Object other)
	{
		if(super.equals(other))
			return true;

		if(other instanceof Trial)
		{
			final Trial that = (Trial) other;

			if(steps.size() != that.steps.size())
				return false;

			for(int i = 0; i < steps.size(); i++)
				if(!steps.get(i).equals(that.steps.get(i)))
					return false;

			return true;
		}

		return false;
	}

	public TrialStat evaluate()
	{
		evaluation = makeEvaluation();
		return evaluation;
	}

	public boolean getClear()
	{
		return clear;
	}

	public boolean getClearInputs()
	{
		return clearInputs;
	}

	public boolean getEvaluate()
	{
		return evaluate;
	}

	public boolean getKnown()
	{
		return known;
	}

	public TrialStat getLastEvaluation()
	{
		return evaluation;
	}

	public TrialRecord getLastRecording()
	{
		return recording;
	}

	public Network getMetaNetwork()
	{
		return meta;
	}

	public boolean getRecord()
	{
		return record;
	}

	public List<Step> getSteps()
	{
		return steps;
	}

	@Override
	public int hashCode()
	{
		int code = 0;
		for(final Step step : steps)
			code += step.hashCode();
		return code;
	}

	public void initialize()
	{
		evaluation = null;
		recording = null;
		steps.clear();
	}

	public boolean isTrainTrial()
	{
		return known;
	}

	public void log()
	{
		for(final Step step : steps)
			for(int t = 0; t < step.getNetwork().nTarget(); t++)
				step.addRecordLayer(step.getNetwork().getTargetLayer(t));
	}

	protected TrialStat makeEvaluation()
	{
		return new TrialStat(this);
	}

	protected TrialRecord makeRecord()
	{
		return new TrialRecord(this);
	}

	protected Step makeStep()
	{
		return new Step(this);
	}

	public Step nextStep()
	{
		final Step step = makeStep();
		steps.add(step);
		return step;
	}

	public TrialRecord record()
	{
		recording = makeRecord();
		return recording;
	}

	public void run()
	{
		run(false);
	}

	public void run(final boolean train)
	{
		if(clear)
			meta.clear();

		for(final Step step : steps)
		{
			if(clearInputs)
				meta.clearInputs();

			if(train)
				step.train();
			else
				step.run();
		}

		if(evaluate)
			evaluate();

		if(record)
			record();
	}

	public void setClear(final boolean clear)
	{
		this.clear = clear;
	}

	public void setClearInputs(final boolean clearInputs)
	{
		this.clearInputs = clearInputs;
	}

	public void setEvaluate(final boolean evaluate)
	{
		this.evaluate = evaluate;
		for(final Step step : steps)
			step.setEvaluate(evaluate);
	}

	public void setKnown(final boolean known)
	{
		this.known = known;
	}

	public void setRecord(final boolean record)
	{
		this.record = record;
		for(final Step step : steps)
			step.setRecord(record);
	}

	public int size()
	{
		return steps.size();
	}

	public void test()
	{
		run(false);
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer();

		return sb.toString();
	}

	public void train()
	{
		run(true);
	}
}
