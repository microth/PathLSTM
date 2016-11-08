package dmonner.xlbp.trial;

import dmonner.xlbp.Network;

public interface TrialStream
{
	public Network getMetaNetwork();

	public String getName();

	public Trial nextTestTrial();

	public Trial nextTrainTrial();

	public Trial nextValidationTrial();

	public int nFolds();

	public int nTestFolds();

	public int nTestTrials();

	public int nTrainFolds();

	public int nTrainTrials();

	public int nValidationFolds();

	public int nValidationTrials();

	public void setFold(int fold);
}
