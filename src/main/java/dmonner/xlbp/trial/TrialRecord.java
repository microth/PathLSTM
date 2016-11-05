package dmonner.xlbp.trial;

import java.util.ArrayList;
import java.util.List;

public class TrialRecord
{
	private final Trial trial;
	private final List<StepRecord> recordings;

	public TrialRecord(final Trial trial)
	{
		this.trial = trial;
		this.recordings = new ArrayList<StepRecord>(trial.size());

		for(final Step step : trial.getSteps())
			recordings.add(step.getLastRecording());
	}

	public List<StepRecord> getRecordings()
	{
		return recordings;
	}

	public Trial getTrial()
	{
		return trial;
	}
}
