package dmonner.xlbp.trial;

import dmonner.xlbp.stat.TestStat;

public class PerfectBreaker implements TrainingBreaker
{
	@Override
	public boolean isBreakTime(final TestStat stat)
	{
		return stat.getLastTrain().getTrialStats().getFraction() == 1F;
	}

	@Override
	public void reset()
	{
	}
}
