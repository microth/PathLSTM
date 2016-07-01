package dmonner.xlbp.trial;

import java.io.IOException;

import dmonner.xlbp.Network;
import dmonner.xlbp.stat.SetStat;
import dmonner.xlbp.stat.TestStat;
import dmonner.xlbp.stat.TrialStat;
import dmonner.xlbp.util.CSVWriter;

public class Trainer
{
	private final Network net;
	private final TrialStream stream;
	private TrialStat[][] evals;
	private TrialStat[][] bestEvals;
	private TrialRecord[][] records;
	private TrialRecord[][] bestRecords;
	private boolean keepEvaluations;
	private boolean keepRecords;
	private CSVWriter trainlog, testlog, validlog;
	private TrainingBreaker breaker;

	public Trainer(final Network net, final TrialStream stream)
	{
		this.net = net;
		this.stream = stream;
		this.keepEvaluations = false;
		this.keepRecords = false;
		this.breaker = new NeverBreaker();
	}

	private SetStat evaluateTest(final int fold, final int ep)
	{
		if(keepEvaluations)
			evals[fold] = new TrialStat[stream.nTestTrials()];

		final String time = "F" + fold + "-" + ep;
		final SetStat summary = new SetStat("Test" + time);

		for(int i = 0; i < stream.nTestTrials(); i++)
		{
			final Trial trial = stream.nextTestTrial();

			// Run the network on each test trial, while evaluating
			trial.setEvaluate(true);
			trial.setRecord(true);
			trial.run();

			summary.add(trial.getLastEvaluation());
			if(keepEvaluations)
				evals[fold][i] = trial.getLastEvaluation();

			postTestTrial(trial, trial.getLastEvaluation());
		}

		summary.analyze();
		return summary;
	}

	private SetStat evaluateTrain(final int fold, final int ep, final boolean train)
	{
		if(keepEvaluations)
			evals[fold] = new TrialStat[stream.nTrainTrials()];

		final String time = "F" + fold + "-" + ep;
		final SetStat summary = new SetStat("Train" + time);

		for(int i = 0; i < stream.nTrainTrials(); i++)
		{
			final Trial trial = stream.nextTrainTrial();

			// Run the network on each training trial, while evaluating
			trial.setEvaluate(true);
			trial.setRecord(true);
			trial.run(train);

			summary.add(trial.getLastEvaluation());
			if(keepEvaluations)
				evals[fold][i] = trial.getLastEvaluation();

			postTrainTrial(trial, trial.getLastEvaluation());
		}

		net.processBatch();

		summary.analyze();
		return summary;
	}

	private SetStat evaluateValid(final int fold, final int ep)
	{
		if(keepEvaluations)
			evals[fold] = new TrialStat[stream.nValidationTrials()];

		final String time = "F" + fold + "-" + ep;
		final SetStat summary = new SetStat("Valid" + time);

		for(int i = 0; i < stream.nValidationTrials(); i++)
		{
			final Trial trial = stream.nextValidationTrial();

			// Run the network on each training trial, while evaluating
			trial.setEvaluate(true);
			trial.setRecord(true);
			trial.run();

			summary.add(trial.getLastEvaluation());
			if(keepEvaluations)
				evals[fold][i] = trial.getLastEvaluation();

			postValidationTrial(trial, trial.getLastEvaluation());
		}

		summary.analyze();
		return summary;
	}

	public TrialStat[] getEvaluations()
	{
		// Calculate the total number of evaluationss
		int n = 0;
		for(int i = 0; i < bestEvals.length; i++)
			n += bestEvals[i].length;

		// Move them into a single temporary array
		final TrialStat[] all = new TrialStat[n];
		int t = 0;
		for(int i = 0; i < bestEvals.length; i++)
		{
			final TrialStat[] fold = bestEvals[i];
			for(int j = 0; j < fold.length; j++)
				all[t++] = fold[j];
		}

		return all;
	}

	public Network getNetwork()
	{
		return net;
	}

	public TrialRecord[] getRecords()
	{
		// Calculate the total number of records
		int n = 0;
		for(int i = 0; i < bestRecords.length; i++)
			n += bestRecords[i].length;

		// Move them into a single temporary array
		final TrialRecord[] all = new TrialRecord[n];
		int t = 0;
		for(int i = 0; i < bestRecords.length; i++)
		{
			final TrialRecord[] fold = bestRecords[i];
			for(int j = 0; j < fold.length; j++)
				all[t++] = fold[j];
		}

		return all;
	}

	private void log(final CSVWriter log, final SetStat summary, final int ep, final int fold)
	{
		if(log != null)
		{
			try
			{
				if(ep == 0 && fold == 0)
					summary.saveHeader(log);
				summary.saveData(log);
			}
			catch(final IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public void postEpoch(final int ep, final TestStat stat)
	{
		// Empty hook method.
	}

	public void postFold(final int fold, final TestStat stat)
	{
		// Empty hook method.
	}

	public void postTestTrial(final Trial trial, final TrialStat stat)
	{
		// Empty hook method.
	}

	public void postTrainTrial(final Trial trial, final TrialStat stat)
	{
		// Empty hook method.
	}

	public void postValidationTrial(final Trial trial, final TrialStat stat)
	{
		// Empty hook method.
	}

	public void preEpoch(final int ep)
	{
		// Empty hook method.
	}

	public void preFold(final int fold)
	{
		// Empty hook method.
	}

	public void preTest(final int fold)
	{
		// Empty hook method.
	}

	public void preTrain(final int fold)
	{
		// Empty hook method.
	}

	public TestStat run(final int maxEpochs)
	{
		final TestStat total = new TestStat();

		for(int f = 0; f < stream.nFolds(); f++)
		{
			// reinitialize the Network that is about to be trained
			net.rebuild();

			// run the training procedure for this fold and add up the results
			total.add(runFold(f, maxEpochs));
		}

		total.analyze();

		return total;
	}

	public TestStat runFold(final int fold, final int maxEpochs)
	{
		stream.setFold(fold);
		breaker.reset();
		preFold(fold);

		final TestStat stat = new TestStat();

		// Pre-training test
		log(trainlog, evaluateTrain(fold, 0, false), 0, fold);

		if(stream.nValidationFolds() > 0)
			log(validlog, evaluateValid(fold, 0), 0, fold);

		if(stream.nTestFolds() > 0)
			log(testlog, evaluateTest(fold, 0), 0, fold);

		// For each epoch
		for(int ep = 1; ep <= maxEpochs; ep++)
		{
			SetStat trainStat = null, validStat = null, testStat = null;

			preEpoch(ep);
			preTrain(fold);

			// Train the network on the training set, evaluating on the way.
			if(stream.nTrainFolds() > 0)
			{
				trainStat = evaluateTrain(fold, ep, true);
				log(trainlog, trainStat, ep, fold);
			}

			preTest(fold);

			// If we're using a validation set, evaluate on it.
			if(stream.nValidationFolds() > 0)
			{
				validStat = evaluateValid(fold, ep);
				log(validlog, validStat, ep, fold);
			}

			// If we're using a testing set, evaluate on it.
			if(stream.nTestFolds() > 0)
			{
				testStat = evaluateTest(fold, ep);
				log(testlog, testStat, ep, fold);
			}

			final boolean newBest = stat.add(trainStat, validStat, testStat);

			if(newBest)
				updateBest(fold);

			postEpoch(ep, stat);

			// Quit training if out performance tells us to.
			if(breaker.isBreakTime(stat))
				break;
		}

		postFold(fold, stat);

		return stat;
	}

	public void setBreaker(final TrainingBreaker breaker)
	{
		this.breaker = breaker;
	}

	public void setKeepEvaluations(final boolean keepEvaluations)
	{
		this.keepEvaluations = keepEvaluations;

		if(keepEvaluations)
		{
			this.evals = new TrialStat[stream.nFolds()][];
			this.bestEvals = new TrialStat[stream.nFolds()][];
		}
		else
		{
			this.evals = null;
			this.bestEvals = null;
		}
	}

	public void setKeepRecords(final boolean keepRecords)
	{
		this.keepRecords = keepRecords;

		if(keepRecords)
		{
			this.records = new TrialRecord[stream.nFolds()][];
			this.bestRecords = new TrialRecord[stream.nFolds()][];
		}
		else
		{
			this.records = null;
			this.bestRecords = null;
		}
	}

	public void setTestLog(final CSVWriter log)
	{
		testlog = log;
	}

	public void setTrainLog(final CSVWriter log)
	{
		trainlog = log;
	}

	public void setValidationLog(final CSVWriter log)
	{
		validlog = log;
	}

	private void updateBest(final int fold)
	{
		// Save the current test set evaluations; at the end of training we will have a
		// complete picture with a test-set result for each trial
		if(keepEvaluations)
			bestEvals[fold] = evals[fold];

		if(keepRecords)
			bestRecords[fold] = records[fold];
	}
}
