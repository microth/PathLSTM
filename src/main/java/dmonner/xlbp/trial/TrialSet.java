package dmonner.xlbp.trial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import dmonner.xlbp.Network;
import dmonner.xlbp.Target;
import dmonner.xlbp.layer.TargetLayer;
import dmonner.xlbp.stat.BitStat;
import dmonner.xlbp.util.ArrayQueue;
import dmonner.xlbp.util.MatrixTools;

public class TrialSet extends AbstractTrialStream
{
	private static int[] parseFoldSplit(final String foldSplit)
	{
		if(foldSplit.isEmpty())
			return new int[0];

		final String[] s = foldSplit.split("/");
		final int[] f = new int[s.length];

		try
		{
			for(int i = 0; i < s.length; i++)
				f[i] = Integer.parseInt(s[i].trim());
		}
		catch(final NumberFormatException ex)
		{
			throw new IllegalArgumentException(
					"Malformed foldSplit string; all entries must be numbers: " + foldSplit, ex);
		}

		return f;
	}

	private final Trial[][] folds;
	private Trial[] train, test, valid;
	private boolean balance;
	private final ArrayQueue<Trial> trainCache, testCache, validCache;
	private final Random rand;

	public TrialSet(final String name, final Network net, final Trial[] set, final int train,
			final int test, final int valid)
	{
		this(name, net, set, train, test, valid, new Random());
	}

	public TrialSet(final String name, final Network net, final Trial[] set, final int train,
			final int test, final int valid, final Random random)
	{
		this(name, net, set, makeSplitString(train, test, valid), random);
	}

	public TrialSet(final String name, final Network net, final Trial[] set, final int[] foldSizes,
			final int test, final int valid)
	{
		this(name, net, set, foldSizes, test, valid, new Random());
	}

	public TrialSet(final String name, final Network net, final Trial[] set, final int[] foldSizes,
			final int test, final int valid, final Random random)
	{
		this(name, net, set, foldSizes, makeSplitString(foldSizes.length - test - valid, test, valid),
				random);
	}

	public TrialSet(final String name, final Network net, final Trial[] set, final int[] foldSizes,
			final String split)
	{
		this(name, net, set, foldSizes, split, new Random());
	}

	public TrialSet(final String name, final Network net, final Trial[] set, final int[] foldSizes,
			final String split, final Random random)
	{
		super(name, net, split);

		rand = random;
		trainCache = new ArrayQueue<Trial>();
		testCache = new ArrayQueue<Trial>();
		validCache = new ArrayQueue<Trial>();

		// If we have specified sizes for the folds
		if(foldSizes.length > 0)
		{
			if(foldSizes.length != nFolds())
				throw new IllegalArgumentException("Number of foldSizes entries (" + foldSizes.length
						+ ") does not match number of folds (" + nFolds() + ").");

			// Split the trials into folds based on their indices, as specified by foldSizes
			folds = new Trial[nFolds()][];
			int prev = 0;
			for(int f = 0; f < nFolds(); f++)
			{
				final int n = foldSizes[f];

				folds[f] = new Trial[n];
				for(int i = 0; i < n; i++)
					folds[f][i] = set[prev + i];

				prev += n;
			}
			// No need to randomize the folds; this gets done in setFold().
		}
		// Otherwise we should split up the folds evenly as best we can.
		else
		{
			final double fraction = ((double) set.length) / nFolds();

			// Randomize the trials
			MatrixTools.randomize(set, rand);

			// Split the trials into folds
			folds = new Trial[nFolds()][];
			for(int f = 0; f < nFolds(); f++)
			{
				final int start = (int) (f * fraction);
				final int end = (int) ((f + 1) * fraction);
				final int n = end - start;

				folds[f] = new Trial[n];
				for(int i = 0; i < n; i++)
					folds[f][i] = set[start + i];
			}
		}
	}

	public TrialSet(final String name, final Network net, final Trial[] set, final String split)
	{
		this(name, net, set, "", split, new Random());
	}

	public TrialSet(final String name, final Network net, final Trial[] set, final String split,
			final Random random)
	{
		this(name, net, set, "", split, random);
	}

	public TrialSet(final String name, final Network net, final Trial[] set, final String foldSplit,
			final String split)
	{
		this(name, net, set, parseFoldSplit(foldSplit), split, new Random());
	}

	public TrialSet(final String name, final Network net, final Trial[] set, final String foldSplit,
			final String split, final Random random)
	{
		this(name, net, set, parseFoldSplit(foldSplit), split, random);
	}

	private void balanceTrainingSet()
	{
		final Map<TargetLayer, List<Target>> byLayer = groupTargetsByLayer(train);

		// For each TargetLayer, further sort the targets by active bit
		for(final Entry<TargetLayer, List<Target>> entry : byLayer.entrySet())
		{
			final TargetLayer layer = entry.getKey();
			final List<Target> targets = entry.getValue();
			final int n = layer.size() == 1 ? 2 : layer.size();

			final List<List<Target>> byBit = new ArrayList<List<Target>>(n);
			for(int i = 0; i < n; i++)
				byBit.add(new ArrayList<Target>());

			// Examine the active bit in each Target and put it in the appropriate bucket
			for(final Target target : targets)
				byBit.get(getSetBitIndex(target)).add(target);

			// Calculate the size of each bucket
			final int[] sizes = new int[n];
			for(int i = 0; i < n; i++)
				sizes[i] = byBit.get(i).size();

			// Find the bit with the minimum number of Targets assigned
			final int mindex = MatrixTools.argmin(sizes);

			// Devise weights for Targets with each active bit
			final float[] weights = new float[n];
			final float min = sizes[mindex];
			for(int i = 0; i < n; i++)
				weights[i] = min / sizes[i];

			// Set weights for Targets in each set
			for(int i = 0; i < n; i++)
				for(final Target target : byBit.get(i))
					target.setWeight(weights[i]);

			System.out.println(layer.getName() + ":");
			System.out.println(Arrays.toString(sizes));
			System.out.println(MatrixTools.toString(weights));
		}
	}

	private int getSetBitIndex(final Target target)
	{
		final float[] value = target.getValue();

		if(value.length == 1)
		{
			// In the binary case, we have two lists: off and on
			if(value[0] >= BitStat.MID)
				return 1;
			else
				return 0;
		}
		else
		{
			// Otherwise we have layer.size() lists, one for each bit
			int index = -1;
			for(int i = 0; i < value.length; i++)
			{
				if(value[i] >= BitStat.MID)
				{
					// Throw an exception if we find more than one bit set
					if(index >= 0)
						throw new IllegalStateException("Cannot balance with more than one bit per Target.");

					index = i;
				}
			}

			// Throw an exception if we find no bits set
			if(index < 0)
				throw new IllegalStateException("Cannot balance with no bits set in a Target.");

			return index;
		}
	}

	public Trial getTestTrial(final int index)
	{
		return test[index];
	}

	public Trial getTrainTrial(final int index)
	{
		return train[index];
	}

	public Trial getValidationTrial(final int index)
	{
		return valid[index];
	}

	private Map<TargetLayer, List<Target>> groupTargetsByLayer(final Trial[] set)
	{
		// Collect all Targets in the training set and sort them by TargetLayer
		final Map<TargetLayer, List<Target>> byLayer = new HashMap<TargetLayer, List<Target>>();
		for(final TargetLayer layer : getMetaNetwork().getTargetLayers())
			byLayer.put(layer, new ArrayList<Target>());

		for(final Trial trial : set)
			for(final Step step : trial.getSteps())
				for(final Target target : step.getTargets())
					byLayer.get(target.getLayer()).add(target);

		return byLayer;
	}

	@Override
	public Trial nextTestTrial()
	{
		if(testCache.isEmpty())
			testCache.fill(test);

		return testCache.pop();
	}

	@Override
	public Trial nextTrainTrial()
	{
		if(trainCache.isEmpty())
			trainCache.fill(train);

		return trainCache.pop();
	}

	@Override
	public Trial nextValidationTrial()
	{
		if(validCache.isEmpty())
			validCache.fill(valid);

		return validCache.pop();
	}

	@Override
	public int nTestTrials()
	{
		return test.length;
	}

	@Override
	public int nTrainTrials()
	{
		return train.length;
	}

	@Override
	public int nValidationTrials()
	{
		return valid.length;
	}

	private Trial[] select(final int start, final int num)
	{
		// Calculate how many trials are being selected
		int n = 0;
		for(int i = 0; i < num; i++)
			n += folds[(start + i) % folds.length].length;

		// Move them into a single temporary array
		final Trial[] tr = new Trial[n];
		int t = 0;
		for(int i = 0; i < num; i++)
		{
			final Trial[] fold = folds[(start + i) % folds.length];
			for(int j = 0; j < fold.length; j++)
				tr[t++] = fold[j];
		}

		return tr;
	}

	private Trial[] selectTest(final int fold)
	{
		final Trial[] trials = select((fold + nTrainFolds()) % folds.length, nTestFolds());
		for(final Trial trial : trials)
			trial.setKnown(false);
		return trials;
	}

	private Trial[] selectTrain(final int fold)
	{
		final Trial[] trials = select(fold, nTrainFolds());
		for(final Trial trial : trials)
			trial.setKnown(true);
		return trials;
	}

	private Trial[] selectValidation(final int fold)
	{
		final Trial[] trials = select((fold + nTrainFolds() + nTestFolds()) % folds.length,
				nValidationFolds());
		for(final Trial trial : trials)
			trial.setKnown(false);
		return trials;
	}

	public void setBalance(final boolean balance)
	{
		this.balance = balance;
	}

	@Override
	public void setFold(final int fold)
	{
		train = selectTrain(fold);
		test = selectTest(fold);
		valid = selectValidation(fold);

		if(balance)
			balanceTrainingSet();

		// Clear any temporary information in the Trials (like learned representations)
		for(final Trial[] array : folds)
			for(final Trial trial : array)
				trial.clear();

		MatrixTools.randomize(train, rand);
		trainCache.fill(train);
		testCache.fill(test);
		validCache.fill(valid);
	}
}
