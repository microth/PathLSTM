package dmonner.xlbp.trial;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dmonner.xlbp.Input;
import dmonner.xlbp.Network;
import dmonner.xlbp.Target;
import dmonner.xlbp.layer.InputLayer;
import dmonner.xlbp.layer.Layer;
import dmonner.xlbp.layer.TargetLayer;
import dmonner.xlbp.stat.StepStat;

public class Step
{
	private final Trial trial;
	private Network net;
	private final Map<InputLayer, Input> inputs;
	private final Map<TargetLayer, Target> targets;
	private final Map<Layer, LayerCheck> checks;
	private final Set<Layer> recordLayers;
	private StepStat evaluation;
	private StepRecord recording;
	private boolean evaluate;
	private boolean record;

	public Step(final Trial trial)
	{
		this.trial = trial;
		this.net = trial.getMetaNetwork();
		this.inputs = new HashMap<InputLayer, Input>();
		this.targets = new HashMap<TargetLayer, Target>();
		this.checks = new HashMap<Layer, LayerCheck>();
		this.recordLayers = new HashSet<Layer>();
		this.evaluate = true;
		this.record = true;
	}

	public void addCheck(final Layer layer, final float[] check)
	{
		addCheck(new LayerCheck(layer, check));
	}

	public void addCheck(final LayerCheck check)
	{
		checks.put(check.getLayer(), check);
	}
	
	public void addInput(final Input input)
	{
		inputs.put(input.getLayer(), input);
	}
	
	/*public void addInput(final float[] input)
	{
		addInput(net.getInputLayer(), input);
	}
	
	public void addInput(final InputLayer layer, final int[] input)
	{
		addInput(new Input(layer, null, input));
	}
	
	/*public void addInput(final int[] input)
	{
		addInput(net.getInputLayer(), input);
	}

	public void addInput(final InputLayer layer, final float[] input)
	{
		addInput(new Input(layer, input, null));
	}
	
	public void addInput(final InputLayer layer, final int[] input)
	{
		addInput(new Input(layer, null, input));
	}

	public void addInput(final int inputIndex, final float[] input)
	{
		addInput(net.getInputLayer(inputIndex), input);
	}*/

	public void addRecordLayer(final Layer record)
	{
		recordLayers.add(record);
	}

	public void addTarget(final float[] target)
	{
		addTarget(net.getTargetLayer(), target);
	}

	public void addTarget(final int targetIndex, final float[] target)
	{
		addTarget(net.getTargetLayer(targetIndex), target);
	}

	public void addTarget(final Target target)
	{
		targets.put(target.getLayer(), target);
	}

	public void addTarget(final TargetLayer layer, final float[] target)
	{
		addTarget(new Target(layer, target));
	}

	public void clear()
	{
		evaluation = null;
		recording = null;
	}

	@Override
	public boolean equals(final Object other)
	{
		if(super.equals(other))
			return true;

		if(other instanceof Step)
		{
			final Step that = (Step) other;
			// TODO: this is not yet correct because float[]s are compared by reference
			return inputs.equals(that.inputs) && targets.equals(that.targets);
		}

		return false;
	}

	public StepStat evaluate()
	{
		evaluation = makeEvaluation();
		return evaluation;
	}

	public Collection<LayerCheck> getChecks()
	{
		return checks.values();
	}

	public Input getInput()
	{
		return getInput(net.getInputLayer());
	}

	public Input getInput(final InputLayer layer)
	{
		return inputs.get(layer);
	}

	public Input getInput(final int inputIndex)
	{
		return getInput(net.getInputLayer(inputIndex));
	}

	public Collection<Input> getInputs()
	{
		return inputs.values();
	}

	public StepStat getLastEvaluation()
	{
		return evaluation;
	}

	public StepRecord getLastRecording()
	{
		return recording;
	}

	public Network getNetwork()
	{
		return net;
	}

	public Set<Layer> getRecordLayers()
	{
		return recordLayers;
	}

	public Target getTarget()
	{
		return getTarget(net.getTargetLayer());
	}

	public Target getTarget(final int targetIndex)
	{
		return getTarget(net.getTargetLayer(targetIndex));
	}

	public Target getTarget(final TargetLayer layer)
	{
		return targets.get(layer);
	}

	public Collection<Target> getTargets()
	{
		return targets.values();
	}

	public Trial getTrial()
	{
		return trial;
	}

	@Override
	public int hashCode()
	{
		return inputs.hashCode() + targets.hashCode();
	}

	public void initialize()
	{
		inputs.clear();
		targets.clear();
		checks.clear();
		recordLayers.clear();
		evaluation = null;
		recording = null;
	}

	protected StepStat makeEvaluation()
	{
		return new StepStat(this);
	}

	public StepRecord makeRecord()
	{
		return new StepRecord(this);
	}

	public int nEvals()
	{
		return checks.size();
	}

	public int nInputs()
	{
		return inputs.size();
	}

	public int nOutputs()
	{
		return net.nTarget();
	}

	public int nRecordLayers()
	{
		return recordLayers.size();
	}

	public int nTargets()
	{
		return targets.size();
	}

	public StepRecord record()
	{
		recording = makeRecord();
		return recording;
	}

	public void run()
	{
		run(false);
	}

	public void run(final boolean train)
	{
		// apply inputs
		for(final Input input : getInputs())
			input.apply();

		if(train)
		{
			// activate layers
			net.activateTrain();

			// update eligibilities for layers upstream of a copy source
			net.updateEligibilities();

			// apply targets
			for(final Target target : getTargets())
				target.apply();

			if(nTargets() > 0)
			{
				// update responsibilities
				net.updateResponsibilities();

				// update weights
				net.updateWeights();
			}
		}
		else
		{
			// activate layers
			net.activateTest();
		}

		if(evaluate)
			evaluate();

		if(record)
			record();
	}

	public void setEvaluate(final boolean evaluate)
	{
		this.evaluate = evaluate;
	}

	public void setNetwork(final Network net)
	{
		this.net = net;
	}

	public void setRecord(final boolean record)
	{
		this.record = record;
	}

	public void test()
	{
		run(false);
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer();

		for(final Input input : getInputs())
		{
			sb.append(input);
			sb.append("\n");
		}

		for(final Target target : getTargets())
		{
			sb.append(target);
			sb.append("\n");
		}

		return sb.toString();
	}

	public void train()
	{
		run(true);
	}
}
