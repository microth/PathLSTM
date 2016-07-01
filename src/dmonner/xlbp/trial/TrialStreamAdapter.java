package dmonner.xlbp.trial;

import dmonner.xlbp.Network;

public class TrialStreamAdapter extends AbstractTrialStream
{
	public TrialStreamAdapter(final String name, final Network net)
	{
		super(name, net);
	}

	public TrialStreamAdapter(final String name, final Network net, final int train, final int test,
			final int valid)
	{
		super(name, net, train, test, valid);
	}

	public TrialStreamAdapter(final String name, final Network net, final String split)
	{
		super(name, net, split);
	}

	@Override
	public Trial nextTestTrial()
	{
		return null;
	}

	@Override
	public Trial nextTrainTrial()
	{
		return null;
	}

	@Override
	public Trial nextValidationTrial()
	{
		return null;
	}

	@Override
	public int nTestTrials()
	{
		return 0;
	}

	@Override
	public int nTrainTrials()
	{
		return 0;
	}

	@Override
	public int nValidationTrials()
	{
		return 0;
	}

	@Override
	public void setFold(final int fold)
	{
	}
}
