package dmonner.xlbp.stat;

public abstract class Optimizer
{
	public static enum Type
	{
		BIT_ACCURACY, TARGET_ACCURACY, TRIAL_ACCURACY, STEP_ACCURACY, SSE
	}

	public static Optimizer defaultOptimizer = get(Type.SSE);

	public static Optimizer get(final Type type)
	{
		if(type == Type.BIT_ACCURACY)
			return new Optimizer()
			{
				@Override
				public boolean betterThan(final SetStat newest, final SetStat best)
				{
					if(newest == null)
						return false;
					if(best == null)
						return true;
					return newest.getTargetStats().getBits().getAccuracy() >= best.getTargetStats().getBits()
							.getAccuracy();
				}
			};
		else if(type == Type.TARGET_ACCURACY)
			return new Optimizer()
			{
				@Override
				public boolean betterThan(final SetStat newest, final SetStat best)
				{
					if(newest == null)
						return false;
					if(best == null)
						return true;
					return newest.getTargetStats().getCorrect().getFraction() >= best.getTargetStats()
							.getCorrect().getFraction();
				}
			};
		else if(type == Type.TRIAL_ACCURACY)
			return new Optimizer()
			{
				@Override
				public boolean betterThan(final SetStat newest, final SetStat best)
				{
					if(newest == null)
						return false;
					if(best == null)
						return true;
					return newest.getTrialStats().getFraction() >= best.getTrialStats().getFraction();
				}
			};
		else if(type == Type.STEP_ACCURACY)
			return new Optimizer()
			{
				@Override
				public boolean betterThan(final SetStat newest, final SetStat best)
				{
					if(newest == null)
						return false;
					if(best == null)
						return true;
					return newest.getStepStats().getFraction() >= best.getStepStats().getFraction();
				}
			};
		else if(type == Type.SSE)
			return new Optimizer()
			{
				@Override
				public boolean betterThan(final SetStat newest, final SetStat best)
				{
					if(newest == null)
						return false;
					if(best == null)
						return true;
					return newest.getTargetStats().getError().getSSE() <= best.getTargetStats().getError()
							.getSSE();
				}
			};
		else
			throw new IllegalStateException("Unhandled Type: " + type);
	}

	public abstract boolean betterThan(SetStat newest, SetStat best);
}
