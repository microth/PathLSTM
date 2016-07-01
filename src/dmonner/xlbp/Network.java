package dmonner.xlbp;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import dmonner.xlbp.compound.Compound;
import dmonner.xlbp.compound.InputCompound;
import dmonner.xlbp.compound.MemoryCellCompound;
import dmonner.xlbp.compound.TargetCompound;
import dmonner.xlbp.compound.WeightBank;
import dmonner.xlbp.compound.WeightedCompound;
import dmonner.xlbp.compound.XEntropyTargetCompound;
import dmonner.xlbp.layer.InputLayer;
import dmonner.xlbp.layer.Layer;
import dmonner.xlbp.layer.TargetLayer;

public class Network implements Component
{
	private static final long serialVersionUID = 1L;

	private final String name;
	private Component[] all;
	private Component[] activate;
	private Component[] train;
	private DownstreamComponent[] directEntry;
	private UpstreamComponent[] directExit;
	private WeightedCompound[] weightedEntry;
	private InputLayer[] input;
	private TargetLayer[] target;
	private Network[] subnet;
	private int nAll;
	private int nActivate;
	private int nTrain;
	private int nInput;
	private int nTarget;
	private int nSubnet;
	private WeightInitializer win;
	private WeightUpdaterType wut;
	private boolean built;

	public Network(final Network that, final NetworkCopier copier)
	{
		this.name = copier.getCopyNameFrom(that);
		this.nAll = that.nAll;
		this.nActivate = that.nActivate;
		this.nTrain = that.nTrain;
		this.nInput = that.nInput;
		this.nTarget = that.nTarget;
		this.nSubnet = that.nSubnet;

		this.win = that.win;
		this.wut = that.wut;

		this.all = new Component[that.all.length];
		this.activate = new Component[that.activate.length];
		this.train = new Component[that.train.length];
		this.input = new InputLayer[that.input.length];
		this.target = new TargetLayer[that.target.length];
		this.subnet = new Network[that.subnet.length];
		this.directEntry = new DownstreamComponent[that.directEntry.length];
		this.directExit = new UpstreamComponent[that.directExit.length];
		this.weightedEntry = new WeightedCompound[that.weightedEntry.length];

		for(int i = 0; i < that.all.length; i++)
			this.all[i] = copier.getCopyOf(that.all[i]);

		for(int i = 0; i < that.activate.length; i++)
			this.activate[i] = copier.getCopyOf(that.activate[i]);

		for(int i = 0; i < that.train.length; i++)
			this.train[i] = copier.getCopyOf(that.train[i]);

		for(int i = 0; i < that.input.length; i++)
			this.input[i] = copier.getCopyOf(that.input[i]);

		for(int i = 0; i < that.target.length; i++)
			this.target[i] = copier.getCopyOf(that.target[i]);

		for(int i = 0; i < that.subnet.length; i++)
			this.subnet[i] = copier.getCopyOf(that.subnet[i]);

		for(int i = 0; i < that.directEntry.length; i++)
			this.directEntry[i] = copier.getCopyOf(that.directEntry[i]);

		for(int i = 0; i < that.directExit.length; i++)
			this.directExit[i] = copier.getCopyOf(that.directExit[i]);

		for(int i = 0; i < that.weightedEntry.length; i++)
			this.weightedEntry[i] = copier.getCopyOf(that.weightedEntry[i]);
	}

	public Network(final String name)
	{
		this(name, 0, 0, 0, 0, 0, 0);
	}

	public Network(final String name, final int nAll, final int nActivate, final int nTrain,
			final int nInputs, final int nTargets, final int nSubnet)
	{
		this.name = name;
		all = new Component[nAll];
		activate = new Component[nActivate];
		train = new Component[nTrain];
		input = new InputLayer[nInputs];
		target = new TargetLayer[nTargets];
		subnet = new Network[nSubnet];
		directEntry = new DownstreamComponent[0];
		directExit = new UpstreamComponent[0];
		weightedEntry = new WeightedCompound[0];
		win = new UniformWeightInitializer();
		wut = WeightUpdaterType.basic();
	}

	@Override
	public void activateTest()
	{
		for(int i = 0; i < nActivate; i++)
			activate[i].activateTest();
	}

	@Override
	public void activateTrain()
	{
		for(int i = 0; i < nActivate; i++)
			activate[i].clearResponsibilities();

		for(int i = 0; i < nActivate; i++) {
			activate[i].activateTrain();
		}
	}

	public void add(final Component component)
	{
		add(component, true, true, false, false);
	}

	public void add(final Component component, final boolean activate, final boolean train,
			final boolean entry, final boolean exit)
	{
		// set weight initializer and updater
		component.setWeightInitializer(win);
		component.setWeightUpdaterType(wut);

		// pull out subnetworks and keep them separately
		if(component instanceof Network)
			addSubnet((Network) component);

		// pull input layer out of compound to keep it separately
		if(component instanceof InputCompound)
			addInput(((InputCompound) component).getInputLayer());

		if(component instanceof InputLayer)
			addInput((InputLayer) component);

		// pull target layer out of compound to keep it separately
		if(component instanceof TargetCompound)
			addTarget(((TargetCompound) component).getTargetLayer());

		if(component instanceof TargetLayer)
			addTarget((TargetLayer) component);

		// compound still needs to be activated regardless of above
		if(activate)
			addActivate(component);

		if(train)
			addTrain(component);

		if(entry)
			addEntry(component);

		if(exit)
			addExit(component);

		addAll(component);
	}

	private void addActivate(final Component component)
	{
		ensureActivateCapacity(nActivate + 1);
		activate[nActivate++] = component;
	}

	public void addActivateOnly(final Component component)
	{
		add(component, true, false, false, false);
	}

	private void addAll(final Component component)
	{
		ensureAllCapacity(nAll + 1);
		all[nAll++] = component;
	}

	private void addEntry(final Component component)
	{
		if(component instanceof WeightedCompound)
		{
			ensureWeightedEntryCapacity(weightedEntry.length + 1);
			weightedEntry[weightedEntry.length - 1] = (WeightedCompound) component;
		}

		if(component instanceof DownstreamComponent)
		{
			ensureDirectEntryCapacity(directEntry.length + 1);
			directEntry[directEntry.length - 1] = (DownstreamComponent) component;
		}
	}

	private void addExit(final Component component)
	{
		if(component instanceof UpstreamComponent)
		{
			ensureDirectExitCapacity(directExit.length + 1);
			directExit[directExit.length - 1] = (UpstreamComponent) component;
		}
	}

	private void addInput(final InputLayer inLayer)
	{
		ensureInputCapacity(nInput + 1);
		input[nInput++] = inLayer;
	}

	private void addSubnet(final Network sub)
	{
		ensureSubnetCapacity(nSubnet + 1);
		subnet[nSubnet++] = sub;

		for(final InputLayer in : sub.input)
			addInput(in);

		for(final TargetLayer tgt : sub.target)
			addTarget(tgt);
	}

	private void addTarget(final TargetLayer tgtLayer)
	{
		ensureTargetCapacity(nTarget + 1);
		target[nTarget++] = tgtLayer;
	}

	private void addTrain(final Component component)
	{
		ensureTrainCapacity(nTrain + 1);
		train[nTrain++] = component;
	}

	public void addTrainOnly(final Component component)
	{
		add(component, false, true, false, false);
	}

	public void addUpstream(final UpstreamComponent upstream)
	{
		for(final DownstreamComponent entry : directEntry)
			entry.addUpstream(upstream);
	}

	public void addUpstream(final UpstreamComponent upstream, final boolean weighted)
	{
		if(weighted)
			addUpstreamWeights(upstream);
		else
			addUpstream(upstream);
	}

	public void addUpstreamWeights(final UpstreamComponent upstream)
	{
		for(final WeightedCompound entry : weightedEntry)
			entry.addUpstreamWeights(upstream);
	}

	@Override
	public void build()
	{
		if(!built)
		{
			for(int i = 0; i < nAll; i++)
				all[i].build();
		}
		built = true;
	}

	@Override
	public void clear()
	{
		clearActivations();
		clearEligibilities();
		clearResponsibilities();
	}

	@Override
	public void clearActivations()
	{
		for(int i = 0; i < nAll; i++)
			all[i].clearActivations();
	}

	@Override
	public void clearEligibilities()
	{
		for(int i = 0; i < nAll; i++)
			all[i].clearEligibilities();
	}

	public void clearInputs()
	{
		for(int i = 0; i < nInput; i++)
			input[i].clear();

		for(final Network sub : subnet)
			sub.clearInputs();
	}

	@Override
	public void clearResponsibilities()
	{
		for(int i = 0; i < nAll; i++)
			all[i].clearResponsibilities();
	}

	@Override
	public int compareTo(final Component that)
	{
		return name.compareTo(that.getName());
	}

	@Override
	public Network copy(final NetworkCopier copier)
	{
		return new Network(this, copier);
	}

	@Override
	public Network copy(final String suffix)
	{
		return copy(suffix, false, false);
	}

	public Network copy(final String suffix, final boolean copyState, final boolean copyWeights)
	{
		return copy("", suffix, copyState, copyWeights);
	}

	public Network copy(final String prefix, final String suffix, final boolean copyState,
			final boolean copyWeights)
	{
		final NetworkCopier copier = new NetworkCopier(prefix, suffix, copyState, copyWeights);
		final Network copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		// Nothing to do.
	}

	public void ensureActivateCapacity(final int cActivate)
	{
		if(cActivate >= activate.length)
			activate = Arrays.copyOf(activate, cActivate);
	}

	public void ensureAllCapacity(final int cAll)
	{
		if(cAll >= all.length)
			all = Arrays.copyOf(all, cAll);
	}

	public void ensureDirectEntryCapacity(final int cEntry)
	{
		if(cEntry >= directEntry.length)
			directEntry = Arrays.copyOf(directEntry, cEntry);
	}

	public void ensureDirectExitCapacity(final int cExit)
	{
		if(cExit >= directExit.length)
			directExit = Arrays.copyOf(directExit, cExit);
	}

	public void ensureInputCapacity(final int cInputs)
	{
		if(cInputs >= input.length)
			input = Arrays.copyOf(input, cInputs);
	}

	public void ensureSubnetCapacity(final int cSubnets)
	{
		if(cSubnets >= subnet.length)
			subnet = Arrays.copyOf(subnet, cSubnets);
	}

	public void ensureTargetCapacity(final int cTargets)
	{
		if(cTargets >= target.length)
			target = Arrays.copyOf(target, cTargets);
	}

	public void ensureTrainCapacity(final int cTrain)
	{
		if(cTrain >= train.length)
			train = Arrays.copyOf(train, cTrain);
	}

	public void ensureWeightedEntryCapacity(final int cEntry)
	{
		if(cEntry >= weightedEntry.length)
			weightedEntry = Arrays.copyOf(weightedEntry, cEntry);
	}

	public Component getActivate(final int index)
	{
		return activate[index];
	}

	public int getActivateSize()
	{
		return activate.length;
	}

	public Component getComponent(final int index)
	{
		return all[index];
	}

	public Component getComponentByName(final String name)
	{
		for(final Component component : all)
			if(component.getName().equals(name))
				return component;

		return null;
	}

	public Component[] getComponents()
	{
		return all;
	}

	public UpstreamComponent getExitPoint()
	{
		return getExitPoint(0);
	}

	public UpstreamComponent getExitPoint(final int i)
	{
		return directExit[i];
	}

	public InputLayer getInputLayer()
	{
		return getInputLayer(0);
	}

	public InputLayer getInputLayer(final int index)
	{
		return input[index];
	}

	public InputLayer[] getInputLayers()
	{
		return input.clone();
	}

	@Override
	public String getName()
	{
		return name;
	}

	public int getNExitPoints()
	{
		return directExit.length;
	}

	public TargetLayer getTargetLayer()
	{
		return getTargetLayer(0);
	}

	public TargetLayer getTargetLayer(final int index)
	{
		return target[index];
	}

	public TargetLayer[] getTargetLayers()
	{
		return target.clone();
	}

	public Component getTrain(final int index)
	{
		return train[index];
	}

	public int getTrainSize()
	{
		return train.length;
	}

	@Override
	public boolean isBuilt()
	{
		return built;
	}

	public int nInput()
	{
		return nInput;
	}

	public int nTarget()
	{
		return nTarget;
	}

	@Override
	public int nWeights()
	{
		int sum = 0;

		for(int i = 0; i < nAll; i++)
			sum += all[i].nWeights();

		return sum;
	}

	public int nWeightsDeep()
	{
		final Map<Layer, Integer> map = new HashMap<Layer, Integer>();
		final Queue<Component> q = new LinkedList<Component>();

		q.add(this);

		while(!q.isEmpty())
		{
			final Component comp = q.poll();

			if(comp instanceof WeightedCompound)
			{
				final WeightedCompound wcomp = (WeightedCompound) comp;
				for(int i = 0; i < wcomp.nUpstreamWeights(); i++)
				{
					final WeightBank bank = wcomp.getUpstreamWeights(i);
					q.add(bank);
				}
			}

			if(comp instanceof Compound)
				for(final Component sub : ((Compound) comp).getComponents())
					q.add(sub);
			else if(comp instanceof Network)
				for(final Component sub : ((Network) comp).getComponents())
					q.add(sub);
			else if(comp instanceof WeightBank)
				map.put(((WeightBank) comp).getWeightInput(), comp.nWeights());
			else if(comp instanceof Layer)
				map.put(((Layer) comp), comp.nWeights());
			else
				throw new IllegalArgumentException("Unhandled subtype of Component: " + comp);
		}

		int sum = 0;

		for(final int i : map.values())
			sum += i;

		return sum;
	}

	@Override
	public boolean optimize()
	{
		// move all the layers to a list
		final List<Component> allList = Arrays.asList(all);

		// optimize all these components and remove those that become obsolete
		final Iterator<Component> allIt = allList.iterator();
		while(allIt.hasNext())
			if(!allIt.next().optimize())
				allIt.remove();

		// set the layers to be the cleaned list
		all = allList.toArray(new Component[allList.size()]);
		nAll = all.length;

		// check for anything removed from activate
		final List<Component> activateList = Arrays.asList(activate);
		final Iterator<Component> activateIt = activateList.iterator();
		while(activateIt.hasNext())
			if(!allList.contains(activateIt.next()))
				activateIt.remove();

		activate = activateList.toArray(new Component[activateList.size()]);
		nActivate = activate.length;

		// check for anything removed from train
		final List<Component> trainList = Arrays.asList(train);
		final Iterator<Component> trainIt = trainList.iterator();
		while(trainIt.hasNext())
			if(!allList.contains(trainIt.next()))
				trainIt.remove();

		train = trainList.toArray(new Component[trainList.size()]);
		nTrain = train.length;

		return true;
	}

	@Override
	public void processBatch()
	{
		for(final Component comp : train)
			comp.processBatch();
	}

	public void rebuild()
	{
		unbuild();
		build();
	}

	public void setActivateSize(final int nActivate)
	{
		this.nActivate = nActivate;
	}

	public void setAllSize(final int nAll)
	{
		this.nAll = nAll;
	}

	/*public void setInput(final float[] input)
	{
		setInput(0, input);
	}
	
	public void setInput(final int[] in)
	{
		Arrays.fill(input[0].getActivations(), 0);
		for(int i : in)
			input[0].getActivations()[i] = 1;
	}

	public void setInput(final int index, final float[] activations)
	{
		input[index].setInput(activations);
	}*/
	
	
	

	public void setInputSize(final int nInputs)
	{
		this.nInput = nInputs;
	}

	public void setTarget(final float[] target)
	{
		setTarget(0, target);
	}

	public void setTarget(final int index, final float[] activations)
	{
		target[index].setTarget(activations);
	}

	public void setTargetSize(final int nTargets)
	{
		this.nTarget = nTargets;
	}

	public void setTrainSize(final int nTrain)
	{
		this.nTrain = nTrain;
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		this.win = win;
		for(int i = 0; i < nAll; i++)
			all[i].setWeightInitializer(win);
	}

	@Override
	public void setWeightUpdaterType(final WeightUpdaterType wut)
	{
		this.wut = wut;
		for(int i = 0; i < nAll; i++)
			all[i].setWeightUpdaterType(wut);
	}

	public int size()
	{
		return all.length;
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		sb.appendln(name + ": ");
		for(int i = nAll - 1; i >= 0; i--) {
			all[i].toString(sb);
		}
	}

	@Override
	public String toString(final String show)
	{
		final NetworkStringBuilder sb = new NetworkStringBuilder(show);
		toString(sb);
		return sb.toString();
	}

	@Override
	public void unbuild()
	{
		built = false;
		for(int i = 0; i < nAll; i++)
			all[i].unbuild();
	}

	@Override
	public void updateEligibilities()
	{
		for(int i = nActivate - 1; i >= 0; i--)
			activate[i].updateEligibilities();
	}

	@Override
	public void updateResponsibilities()
	{
		for(int i = nTrain - 1; i >= 0; i--)
			train[i].updateResponsibilities();
	}

	@Override
	public void updateWeights()
	{
		for(int i = nTrain - 1; i >= 0; i--)
			train[i].updateWeights();
	}
}
