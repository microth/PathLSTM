package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.Map;

import dmonner.xlbp.util.CSVWriter;

public class TestStat extends AbstractStat
{
	private SetStat bestTrain;
	private SetStat bestValid;
	private SetStat bestTest;
	private SetStat lastTrain;
	private SetStat lastValid;
	private SetStat lastTest;

	private boolean initialized;
	private final Optimizer optimizer;

	public TestStat()
	{
		this(Optimizer.defaultOptimizer);
	}

	public TestStat(final Optimizer optimizer)
	{
		this.optimizer = optimizer;
	}

	public boolean add(final SetStat train, final SetStat valid, final SetStat test)
	{
		initialized = true;

		lastTrain = train;
		lastValid = valid;
		lastTest = test;

		if(optimizer.betterThan(valid, bestValid))
		{
			bestTrain = train;
			bestValid = valid;
			bestTest = test;
			return true;
		}

		return false;
	}

	@Override
	public void add(final Stat that)
	{
		if(that instanceof TestStat)
			add((TestStat) that);
		else
			throw new IllegalArgumentException("Can only add in TestStats.");
	}

	public void add(final TestStat that)
	{
		// if we're not initialized, get ready to import whatever fields the other object has.
		if(!initialized)
		{
			if(that.bestTrain != null)
				bestTrain = new SetStat("bestTrain");
			if(that.bestValid != null)
				bestValid = new SetStat("bestValid");
			if(that.bestTest != null)
				bestTest = new SetStat("bestTest");
			if(that.lastTrain != null)
				lastTrain = new SetStat("lastTrain");
			if(that.lastValid != null)
				lastValid = new SetStat("lastValid");
			if(that.lastTest != null)
				lastTest = new SetStat("lastTest");

			initialized = true;
		}

		if(bestTrain != null)
			bestTrain.add(that.bestTrain);
		if(bestValid != null)
			bestValid.add(that.bestValid);
		if(bestTest != null)
			bestTest.add(that.bestTest);
		if(lastTrain != null)
			lastTrain.add(that.lastTrain);
		if(lastValid != null)
			lastValid.add(that.lastValid);
		if(lastTest != null)
			lastTest.add(that.lastTest);
	}

	@Override
	public void addTo(final String prefix, final Map<String, Object> map)
	{
		if(bestTest != null)
			bestTest.addTo("bestTest" + prefix, map);
		if(lastTest != null)
			lastTest.addTo("lastTest" + prefix, map);
		if(bestValid != null)
			bestValid.addTo("bestValid" + prefix, map);
		if(lastValid != null)
			lastValid.addTo("lastValid" + prefix, map);
		if(bestTrain != null)
			bestTrain.addTo("bestTrain" + prefix, map);
		if(lastTrain != null)
			lastTrain.addTo("lastTrain" + prefix, map);
	}

	@Override
	public void analyze()
	{
		if(bestTest != null)
			bestTest.analyze();
		if(lastTest != null)
			lastTest.analyze();
		if(bestValid != null)
			bestValid.analyze();
		if(lastValid != null)
			lastValid.analyze();
		if(bestTrain != null)
			bestTrain.analyze();
		if(lastTrain != null)
			lastTrain.analyze();
	}

	@Override
	public void clear()
	{
		if(bestTest != null)
			bestTest.clear();
		if(lastTest != null)
			lastTest.clear();
		if(bestValid != null)
			bestValid.clear();
		if(lastValid != null)
			lastValid.clear();
		if(bestTrain != null)
			bestTrain.clear();
		if(lastTrain != null)
			lastTrain.clear();
	}

	public SetStat getBestTest()
	{
		return bestTest;
	}

	public SetStat getBestTrain()
	{
		return bestTrain;
	}

	public SetStat getBestValid()
	{
		return bestValid;
	}

	public SetStat getLastTest()
	{
		return lastTest;
	}

	public SetStat getLastTrain()
	{
		return lastTrain;
	}

	public SetStat getLastValid()
	{
		return lastValid;
	}

	@Override
	public void saveData(final CSVWriter out) throws IOException
	{
		out.beginRecord();
		if(bestTest != null)
			bestTest.saveData(out);
		if(lastTest != null)
			lastTest.saveData(out);
		if(bestValid != null)
			bestValid.saveData(out);
		if(lastValid != null)
			lastValid.saveData(out);
		if(bestTrain != null)
			bestTrain.saveData(out);
		if(lastTrain != null)
			lastTrain.saveData(out);
	}

	@Override
	public void saveHeader(final String prefix, final CSVWriter out) throws IOException
	{
		out.beginRecord();
		if(bestTest != null)
			bestTest.saveHeader("bestTest" + prefix, out);
		if(lastTest != null)
			lastTest.saveHeader("lastTest" + prefix, out);
		if(bestValid != null)
			bestValid.saveHeader("bestValid" + prefix, out);
		if(lastValid != null)
			lastValid.saveHeader("lastValid" + prefix, out);
		if(bestTrain != null)
			bestTrain.saveHeader("bestTrain" + prefix, out);
		if(lastTrain != null)
			lastTrain.saveHeader("lastTrain" + prefix, out);
	}

	@Override
	public String toString(final String prefix)
	{
		final StringBuilder sb = new StringBuilder();

		if(bestTest != null)
		{
			sb.append("BestTest:\n");
			sb.append(bestTest.toString(prefix));
			sb.append("\n");
		}

		if(lastTest != null)
		{
			sb.append("LastTest:\n");
			sb.append(lastTest.toString(prefix));
			sb.append("\n");
		}

		if(bestValid != null)
		{
			sb.append("BestValid:\n");
			sb.append(bestValid.toString(prefix));
			sb.append("\n");
		}

		if(lastValid != null)
		{
			sb.append("LastValid:\n");
			sb.append(lastValid.toString(prefix));
			sb.append("\n");
		}

		if(bestTrain != null)
		{
			sb.append("BestTrain:\n");
			sb.append(bestTrain.toString(prefix));
			sb.append("\n");
		}

		if(lastTrain != null)
		{
			sb.append("LastTrain:\n");
			sb.append(lastTrain.toString(prefix));
			sb.append("\n");
		}

		return sb.toString();

	}

}
