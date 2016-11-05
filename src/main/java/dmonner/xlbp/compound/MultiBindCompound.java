package dmonner.xlbp.compound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import dmonner.xlbp.Component;
import dmonner.xlbp.DownstreamComponent;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.UpstreamComponent;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.layer.CopyDestinationLayer;
import dmonner.xlbp.layer.CopySourceLayer;
import dmonner.xlbp.layer.FanOutLayer;
import dmonner.xlbp.layer.PiLayer;
import dmonner.xlbp.layer.SigmaLayer;

public class MultiBindCompound extends AbstractInternalCompound
{
	private static final long serialVersionUID = 1L;

	private final int size;
	private Component[] activate;

	private FunctionCompound[] inbind;
	private FunctionCompound[] membind;
	private FunctionCompound[] outbind;
	private final FunctionCompound squash;
	private final CopySourceLayer memsrc;
	private final CopyDestinationLayer memdst;
	private final SigmaLayer state;
	private final PiLayer inpi;
	private final PiLayer mempi;
	private final PiLayer outpi;
	private final FanOutLayer fan;

	public MultiBindCompound(final MultiBindCompound that, final NetworkCopier copier)
	{
		super(that, copier);

		this.size = that.size;

		this.inbind = new FunctionCompound[that.inbind.length];
		for(int i = 0; i < that.inbind.length; i++)
			this.inbind[i] = copier.getCopyOf(that.inbind[i]);

		this.membind = new FunctionCompound[that.membind.length];
		for(int i = 0; i < that.membind.length; i++)
			this.membind[i] = copier.getCopyOf(that.membind[i]);

		this.outbind = new FunctionCompound[that.outbind.length];
		for(int i = 0; i < that.outbind.length; i++)
			this.outbind[i] = copier.getCopyOf(that.outbind[i]);

		this.squash = copier.getCopyOf(that.squash);
		this.memsrc = copier.getCopyOf(that.memsrc);
		this.memdst = copier.getCopyOf(that.memdst);
		this.state = copier.getCopyOf(that.state);
		this.inpi = copier.getCopyOf(that.inpi);
		this.mempi = copier.getCopyOf(that.mempi);
		this.outpi = copier.getCopyOf(that.outpi);
		this.fan = copier.getCopyOf(that.fan);

		// copy the activate array;
		this.activate = new Component[that.activate.length];
		for(int i = 0; i < that.activate.length; i++)
			this.activate[i] = copier.getCopyOf(that.activate[i]);

		// setup for superclass
		in = copier.getCopyOf(that.in);
		out = copier.getCopyOf(that.out);
	}

	public MultiBindCompound(final String name, final int size, final String binds)
	{
		this(name, size, binds, null);
	}

	public MultiBindCompound(final String name, final int size, final String binds, final String fcns)
	{
		super(name);
		this.size = size;

		// init the basic network structure
		inbind = new FunctionCompound[0];
		membind = new FunctionCompound[0];
		outbind = new FunctionCompound[0];
		squash = new LogisticCompound(name + "Squash", size);
		memsrc = new CopySourceLayer(name + "MemSrc", size);
		memdst = new CopyDestinationLayer(name + "MemDst", memsrc);
		state = new SigmaLayer(name + "State", size);
		inpi = new PiLayer(name + "InPi", size);
		mempi = new PiLayer(name + "MemPi", size);
		outpi = new PiLayer(name + "OutPi", size);
		fan = new FanOutLayer(name + "FanOut", size);

		// connect the base structure
		fan.addUpstream(outpi);
		outpi.addUpstream(squash);
		squash.addUpstream(memsrc);
		memsrc.addUpstream(state);
		state.addUpstream(mempi);
		state.addUpstream(inpi);
		mempi.addUpstream(memdst);

		// add binds according to input string
		addBinds(binds, fcns);

		in = inpi;
		out = fan;
	}

	@Override
	public void activateTest()
	{
		for(int i = 0; i < activate.length; i++)
			activate[i].activateTest();
	}

	@Override
	public void activateTrain()
	{
		for(int i = 0; i < activate.length; i++)
			activate[i].activateTrain();
	}

	public void addBind(final char bind, final char fcn)
	{
		switch(bind)
		{
			case 'i':
			case 'I':
				addInputBind(fcn);
				break;
			case 'm':
			case 'M':
			case 'f':
			case 'F':
				addMemoryBind(fcn);
				break;
			case 'o':
			case 'O':
				addOutputBind(fcn);
				break;
			default:
				throw new IllegalArgumentException("Unhandled binding type: " + fcn);
		}
	}

	public void addBinds(final String binds)
	{
		addBinds(binds, null);
	}

	public void addBinds(final String binds, final String fcns)
	{
		int fcnspos = 0;
		for(int i = 0; i < binds.length(); i++)
		{
			final char bind = binds.charAt(i);

			if(bind == 'i' || bind == 'I' || bind == 'm' || bind == 'M' || bind == 'o' || bind == 'O')
				addBind(bind, fcns == null ? 'L' : fcns.charAt(fcnspos++));
			else
				throw new IllegalArgumentException("Unhandled binding type: " + bind);
		}
	}

	public void addInputBind(final char fcn)
	{
		addInputBind(translateFcn(fcn, "InBind" + (inbind.length + 1)));
	}

	public void addInputBind(final FunctionCompound fc)
	{
		inbind = Arrays.copyOf(inbind, inbind.length + 1);
		inbind[inbind.length - 1] = fc;
		inpi.addUpstream(fc);
	}

	public void addMemoryBind(final char fcn)
	{
		addMemoryBind(translateFcn(fcn, "MemBind" + (membind.length + 1)));
	}

	public void addMemoryBind(final FunctionCompound fc)
	{
		membind = Arrays.copyOf(membind, membind.length + 1);
		membind[membind.length - 1] = fc;
		mempi.addUpstream(fc);
	}

	public void addOutputBind(final char fcn)
	{
		addOutputBind(translateFcn(fcn, "OutBind" + (outbind.length + 1)));
	}

	public void addOutputBind(final FunctionCompound fc)
	{
		outbind = Arrays.copyOf(outbind, outbind.length + 1);
		outbind[outbind.length - 1] = fc;
		outpi.addUpstream(fc);
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			for(final Component component : activate)
				component.build();

			built = true;
		}
	}

	@Override
	public void clearActivations()
	{
		for(int i = 0; i < activate.length; i++)
			activate[i].clearActivations();
	}

	@Override
	public void clearEligibilities()
	{
		for(int i = 0; i < activate.length; i++)
			activate[i].clearEligibilities();
	}

	@Override
	public void clearResponsibilities()
	{
		// The weight layers save their previous eligibilities and capture their current inputs
		for(int i = 0; i < activate.length; i++)
			activate[i].clearResponsibilities();
	}

	@Override
	public MultiBindCompound copy(final NetworkCopier copier)
	{
		return new MultiBindCompound(this, copier);
	}

	@Override
	public MultiBindCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final MultiBindCompound copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		super.copyConnectivityFrom(comp, copier);

		if(comp instanceof MultiBindCompound)
		{
			final MultiBindCompound that = (MultiBindCompound) comp;

			squash.copyConnectivityFrom(that.squash, copier);

			for(int i = 0; i < inbind.length; i++)
				inbind[i].copyConnectivityFrom(that.inbind[i], copier);

			for(int i = 0; i < membind.length; i++)
				membind[i].copyConnectivityFrom(that.membind[i], copier);

			for(int i = 0; i < outbind.length; i++)
				outbind[i].copyConnectivityFrom(that.outbind[i], copier);
		}
	}

	@Override
	public Component[] getComponents()
	{
		if(activate != null)
			return activate.clone();

		final List<Component> comps = new ArrayList<Component>();

		for(final FunctionCompound bind : inbind)
			comps.add(bind);

		comps.add(inpi);

		for(final FunctionCompound bind : membind)
			comps.add(bind);

		comps.add(memdst);
		comps.add(mempi);
		comps.add(state);
		comps.add(memsrc);
		comps.add(squash);

		for(final FunctionCompound bind : outbind)
			comps.add(bind);

		comps.add(outpi);
		comps.add(fan);

		return comps.toArray(new Component[comps.size()]);
	}

	public FunctionCompound getInputBind(final int i)
	{
		return inbind[i];
	}

	public FunctionCompound getMemoryBind(final int i)
	{
		return membind[i];
	}

	public FunctionCompound getOutputBind(final int i)
	{
		return outbind[i];
	}

	public int nInputBind()
	{
		return inbind.length;
	}

	public int nMemoryBind()
	{
		return membind.length;
	}

	public int nOutputBind()
	{
		return outbind.length;
	}

	@Override
	public int nWeights()
	{
		int sum = 0;

		sum += squash.nWeights();

		for(final FunctionCompound bind : inbind)
			sum += bind.nWeights();

		for(final FunctionCompound bind : membind)
			sum += bind.nWeights();

		for(final FunctionCompound bind : outbind)
			sum += bind.nWeights();

		return sum;
	}

	@Override
	public boolean optimize()
	{
		if(!super.optimize())
			return false;

		final List<Component> act = new ArrayList<Component>();

		for(final FunctionCompound bind : inbind)
			act.add(bind);

		act.add(inpi);

		for(final FunctionCompound bind : membind)
			act.add(bind);

		act.add(memdst);
		act.add(mempi);
		act.add(state);
		act.add(memsrc);
		act.add(squash);

		for(final FunctionCompound bind : outbind)
			act.add(bind);

		act.add(outpi);
		act.add(fan);

		final Iterator<Component> it = act.iterator();
		while(it.hasNext())
			if(!it.next().optimize())
				it.remove();

		activate = act.toArray(new Component[act.size()]);
		in = ((DownstreamComponent) activate[0]).asDownstreamLayer();
		out = ((UpstreamComponent) activate[activate.length - 1]).asUpstreamLayer();

		return true;
	}

	@Override
	public void processBatch()
	{
		squash.processBatch();

		for(final FunctionCompound bind : inbind)
			bind.processBatch();

		for(final FunctionCompound bind : membind)
			bind.processBatch();

		for(final FunctionCompound bind : outbind)
			bind.processBatch();
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		squash.setWeightInitializer(win);

		for(final FunctionCompound bind : inbind)
			bind.setWeightInitializer(win);

		for(final FunctionCompound bind : membind)
			bind.setWeightInitializer(win);

		for(final FunctionCompound bind : outbind)
			bind.setWeightInitializer(win);
	}

	@Override
	public void setWeightUpdaterType(final WeightUpdaterType wut)
	{
		squash.setWeightUpdaterType(wut);

		for(final FunctionCompound bind : inbind)
			bind.setWeightUpdaterType(wut);

		for(final FunctionCompound bind : membind)
			bind.setWeightUpdaterType(wut);

		for(final FunctionCompound bind : outbind)
			bind.setWeightUpdaterType(wut);
	}

	public int size()
	{
		return size;
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		super.toString(sb);

		sb.pushIndent();

		for(int i = activate.length - 1; i >= 0; i--)
			activate[i].toString(sb);

		sb.popIndent();
	}

	private FunctionCompound translateFcn(final char fcn, final String nameSuffix)
	{
		switch(fcn)
		{
			case 'l':
			case 'L':
			case 's':
			case 'S':
				return new LogisticCompound(name + nameSuffix, size);
			case '/':
				return new LinearCompound(name + nameSuffix, size);
			case 't':
			case 'T':
				return new TanhCompound(name + nameSuffix, size);
			default:
				throw new IllegalArgumentException("Unhandled function type: " + fcn);
		}
	}

	public void truncate(final boolean truncate)
	{
		squash.truncate(truncate);

		for(final FunctionCompound bind : inbind)
			bind.truncate(truncate);

		for(final FunctionCompound bind : membind)
			bind.truncate(truncate);

		for(final FunctionCompound bind : outbind)
			bind.truncate(truncate);
	}

	@Override
	public void unbuild()
	{
		super.unbuild();

		for(final Component component : activate)
			component.unbuild();
	}

	@Override
	public void updateEligibilities()
	{
		for(int i = activate.length - 1; i >= 0; i--)
			activate[i].updateEligibilities();
	}

	@Override
	public void updateResponsibilities()
	{
		for(int i = activate.length - 1; i >= 0; i--)
			activate[i].updateResponsibilities();
	}

	@Override
	public void updateWeights()
	{
		for(int i = activate.length - 1; i >= 0; i--)
			activate[i].updateWeights();
	}
}
