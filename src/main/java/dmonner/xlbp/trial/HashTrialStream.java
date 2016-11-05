package dmonner.xlbp.trial;

import java.util.Random;

import dmonner.xlbp.Network;
import dmonner.xlbp.util.ArrayQueue;
import dmonner.xlbp.util.MatrixTools;

public abstract class HashTrialStream extends AbstractTrialStream
{
	private final int perFold;
	private final ArrayQueue<Trial>[] permutation;
	private final ArrayQueue<Trial> trainCache, testCache, validCache;

	public HashTrialStream(final String name, final Network net, final int perFold,
			final int cacheSize, final int train, final int test, final int valid)
	{
		this(name, net, perFold, cacheSize, train, test, valid, new Random());
	}

	public HashTrialStream(final String name, final Network net, final int perFold,
			final int cacheSize, final int train, final int test, final int valid, final Random random)
	{
		this(name, net, perFold, cacheSize, makeSplitString(train, test, valid), random);
	}

	public HashTrialStream(final String name, final Network net, final int perFold,
			final int cacheSize, final String split)
	{
		this(name, net, perFold, cacheSize, split, new Random());
	}

	@SuppressWarnings("unchecked")
	public HashTrialStream(final String name, final Network net, final int perFold,
			final int cacheSize, final String split, final Random random)
	{
		super(name, net, split);

		this.perFold = perFold;
		this.trainCache = new ArrayQueue<Trial>(cacheSize);
		this.testCache = new ArrayQueue<Trial>(cacheSize);
		this.validCache = new ArrayQueue<Trial>(cacheSize);

		this.permutation = new ArrayQueue[nFolds()];

		int p = 0;
		for(int i = 0; i < nTrainFolds(); i++)
			permutation[p++] = trainCache;
		for(int i = 0; i < nTestFolds(); i++)
			permutation[p++] = testCache;
		for(int i = 0; i < nValidationFolds(); i++)
			permutation[p++] = validCache;

		MatrixTools.randomize(permutation, random);
	}

	private void consume()
	{
		final Trial t = nextTrial();
		final int mod = Math.abs(t.hashCode()) % nFolds();
		final ArrayQueue<Trial> q = permutation[mod];
		if(!q.isFull())
			q.push(t);
	}

	@Override
	public Trial nextTestTrial()
	{
		while(testCache.isEmpty())
			consume();

		final Trial next = testCache.pop();
		next.setKnown(false);
		return next;
	}

	@Override
	public Trial nextTrainTrial()
	{
		while(trainCache.isEmpty())
			consume();

		final Trial next = trainCache.pop();
		next.setKnown(true);
		return next;
	}

	public abstract Trial nextTrial();

	@Override
	public Trial nextValidationTrial()
	{
		if(nValidationFolds() == 0)
			return null;

		while(validCache.isEmpty())
			consume();

		final Trial next = validCache.pop();
		next.setKnown(false);
		return next;

	}

	@Override
	public int nTestTrials()
	{
		return nTestFolds() * perFold;
	}

	@Override
	public int nTrainTrials()
	{
		return nTrainFolds() * perFold;
	}

	@Override
	public int nValidationTrials()
	{
		return nValidationFolds() * perFold;
	}

	@Override
	public void setFold(final int fold)
	{
		// Nothing to do; folds have no meaning in a true stream.
	}
}
