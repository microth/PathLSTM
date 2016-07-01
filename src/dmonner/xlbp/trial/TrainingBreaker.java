package dmonner.xlbp.trial;

import dmonner.xlbp.stat.TestStat;

public interface TrainingBreaker
{
	public boolean isBreakTime(final TestStat stat);

	public void reset();
}
