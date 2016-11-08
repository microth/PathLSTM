package dmonner.xlbp.trial;

import dmonner.xlbp.stat.Optimizer;
import dmonner.xlbp.stat.SetStat;
import dmonner.xlbp.stat.TestStat;

public class ValidationBreaker implements TrainingBreaker
{
	private final int maxEpochsWithoutImprovement;
	private final Optimizer optimizer;

	private int epochsWithoutImprovement;
	private SetStat bestValid;

	public ValidationBreaker()
	{
		this(10);
	}

	public ValidationBreaker(final int maxEpochsWithoutImprovement)
	{
		this(maxEpochsWithoutImprovement, Optimizer.defaultOptimizer);
	}

	public ValidationBreaker(final int maxEpochsWithoutImprovement, final Optimizer optimizer)
	{
		this.maxEpochsWithoutImprovement = maxEpochsWithoutImprovement;
		this.optimizer = optimizer;
	}

	@Override
	public boolean isBreakTime(final TestStat stat)
	{
		final SetStat newest = stat.getBestValid();

		if(optimizer.betterThan(newest, bestValid))
		{
			bestValid = newest;
			epochsWithoutImprovement = 0;
		}
		else
		{
			epochsWithoutImprovement++;
		}

		return epochsWithoutImprovement >= maxEpochsWithoutImprovement;
	}

	@Override
	public void reset()
	{
		bestValid = null;
		epochsWithoutImprovement = 0;
	}
}
