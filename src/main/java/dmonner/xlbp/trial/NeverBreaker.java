package dmonner.xlbp.trial;

import dmonner.xlbp.stat.TestStat;

public class NeverBreaker implements TrainingBreaker
{
	@Override
	public boolean isBreakTime(final TestStat stat)
	{
		return false;
	}

	@Override
	public void reset()
	{
	}
}
