package dmonner.xlbp.trial;

import dmonner.xlbp.Network;

public abstract class AbstractTrialStream implements TrialStream
{
	protected static String makeSplitString(final int train, final int test, final int valid)
	{
		return train + "/" + valid + "/" + test;
	}

	private final String name;
	private final Network meta;
	private final int ntrain, ntest, nvalid, nfolds;

	public AbstractTrialStream(final String name, final Network net)
	{
		// By default, we only train, with no test or validation.
		this(name, net, 1, 0, 0);
	}

	public AbstractTrialStream(final String name, final Network net, final int train, final int test,
			final int valid)
	{
		this(name, net, makeSplitString(train, test, valid));
	}

	public AbstractTrialStream(final String name, final Network net, final String split)
	{
		final String[] fields = split.split("/");

		if(fields.length < 1 || fields.length > 3)
			throw new IllegalArgumentException("Need 1-3 fields in split string; found " + fields.length
					+ " in: " + split);

		this.name = name;
		this.meta = net;

		try
		{
			this.ntrain = Integer.parseInt(fields[0]);
			this.nvalid = fields.length > 1 ? Integer.parseInt(fields[1]) : 0;
			this.ntest = fields.length > 2 ? Integer.parseInt(fields[2]) : 0;
		}
		catch(final NumberFormatException ex)
		{
			throw new IllegalArgumentException("Malformed split string; all entries must be numbers: "
					+ split, ex);
		}

		this.nfolds = ntrain + ntest + nvalid;

		/*
		 * Valid uses of this class include:
		 * 
		 * --Training only (i.e. 1/0/0)
		 * 
		 * --Testing only (i.e. 0/0/1)
		 * 
		 * --Training & Testing without validation (e.g. 9/0/1)
		 * 
		 * --Training & Testing with validation (e.g. 8/1/1)
		 * 
		 * Other uses are disallowed by the exceptions below.
		 */

		if(ntrain < 0 || ntest < 0 || nvalid < 0)
			throw new IllegalArgumentException("Negative folds not allowed.");

		if(ntrain == 0 && ntest == 0)
			throw new IllegalArgumentException("Need at least 1 training or testing fold.");

		if(ntest == 0 && nvalid > 0)
			throw new IllegalArgumentException("Cannot use validation set without testing.");

		if(ntrain == 0 && nvalid > 0)
			throw new IllegalArgumentException("Cannot use validation set without training.");

		if(ntrain == 0 && ntest != 1)
			throw new IllegalArgumentException("Testing alone must be done with a single fold.");

		if(ntest == 0 && ntrain != 1)
			throw new IllegalArgumentException("Training alone must be done with a single fold.");
	}

	@Override
	public Network getMetaNetwork()
	{
		return meta;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public int nFolds()
	{
		return nfolds;
	}

	@Override
	public int nTestFolds()
	{
		return ntest;
	}

	@Override
	public int nTrainFolds()
	{
		return ntrain;
	}

	@Override
	public int nValidationFolds()
	{
		return nvalid;
	}
}
