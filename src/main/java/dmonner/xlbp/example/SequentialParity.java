package dmonner.xlbp.example;

import dmonner.xlbp.Network;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.compound.InputCompound;
import dmonner.xlbp.compound.MemoryCellCompound;
import dmonner.xlbp.compound.XEntropyTargetCompound;
import dmonner.xlbp.stat.TestStat;
import dmonner.xlbp.trial.Step;
import dmonner.xlbp.trial.Trainer;
import dmonner.xlbp.trial.Trial;
import dmonner.xlbp.trial.TrialStreamAdapter;

/**
 * A SequentialParity task is a time-extended version of the classic XOR task for neural networks.
 * The network receives a single bit (0 or 1) as input at each time step, and must keep a running
 * tally of the parity bits it receives, which is the output at each step. Put another way, the
 * network must add the bits it is given and after each one, tell us whether the total received so
 * far is odd (output=1) or even (output=0).
 * 
 * @author dmonner
 */
public class SequentialParity extends TrialStreamAdapter
{
	public static void main(final String[] args)
	{
		// The number of steps in a trial. Default = 5.
		final int trialLength = 5;

		// The number of trials in an epoch; i.e. size of group to measure. Default = 100.
		final int trialsPerEpoch = 100;

		// The number of epochs to allow the network to train. Default = 500.
		final int epochs = 500;

		// The number of memory cells in the middle layer. Default = 5.
		final int memSize = 5;

		// Describes the memory cell type. Default has input and output gates only = "IO".
		final String memType = "IOFU";

		// Create a new Network to learn the task.
		final Network net = new Network("SeqParityNet");

		// Set the learning rate to 0.1.
		net.setWeightUpdaterType(WeightUpdaterType.basic(0.1F));

		// Instantiate a SequentialParity task.
		final SequentialParity task = new SequentialParity(net, trialsPerEpoch, trialLength);

		// Define three layers -- Input, Memory Cells, Output (Logistic).
		final InputCompound bit = new InputCompound("Bit", 1);
		final MemoryCellCompound mem = new MemoryCellCompound("Mem", memSize, memType);
		final XEntropyTargetCompound ans = new XEntropyTargetCompound("Ans", 1);

		// Add weight matrices connecting Bit=>Mem and Mem=>Ans.
		ans.addUpstreamWeights(mem);
		mem.addUpstreamWeights(bit);

		// Add the layers to the Network in the order in which they will be activated.
		net.add(bit);
		net.add(mem);
		net.add(ans);
		
		net.build();
		
		NetworkStringBuilder sb = new NetworkStringBuilder("-CNWXI");
		net.toString(sb);
		System.err.println(sb.toString());
		
		/**
		// Create a new Trainer to train the Network on the task.
		final Trainer trainer = new Trainer(net, task)
		{
			// Override the Trainer's hook method postEpoch to print the per-step accuracy.
			@Override
			public void postEpoch(final int ep, final TestStat stat)
			{
				System.out.println(ep + ":\t" + stat.getLastTrain().getStepStats().getFraction());
			}
		};

		System.out.println("Epoch\tAccuracy");

		// Train the Network on the task for the specified number of trials.
		final TestStat result = trainer.run(epochs);

		System.out.println("Final Results:");
		System.out.println(result);**/
	}

	private final int trialsPerEpoch;
	private final int trialLength;

	/**
	 * @param trialsPerEpoch
	 *          The number of trials between evaluations.
	 * @param trialLength
	 *          The number of steps (input/output pairs) in a trial.
	 */
	public SequentialParity(final Network net, final int trialsPerEpoch, final int trialLength)
	{
		super("SequentialParity" + trialLength, net);
		this.trialsPerEpoch = trialsPerEpoch;
		this.trialLength = trialLength;
	}

	@Override
	public Trial nextTrainTrial()
	{
		final Trial trial = new Trial(getMetaNetwork());

		// Default output = 0
		boolean odd = false;

		for(int i = 0; i < trialLength; i++)
		{
			// Generate a random boolean input.
			final boolean input = Math.random() < 0.5;

			// The new output is the previous output XOR'd with the new input.
			odd = odd ^ input;

			// Add the input and output to a new Step in the Trial
			final Step step = trial.nextStep();
			///////step.addInput(new float[] { input ? 1F : 0F });
			step.addTarget(new float[] { odd ? 1F : 0F });
		}

		return trial;
	}

	@Override
	public int nTrainTrials()
	{
		return trialsPerEpoch;
	}
}
