package dmonner.xlbp.compound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dmonner.xlbp.Component;
import dmonner.xlbp.DownstreamComponent;
import dmonner.xlbp.Function;
import dmonner.xlbp.InternalComponent;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.UpstreamComponent;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.layer.CopyDestinationLayer;
import dmonner.xlbp.layer.CopySourceLayer;
import dmonner.xlbp.layer.DropoutPiLayer;
import dmonner.xlbp.layer.FanOutLayer;
import dmonner.xlbp.layer.FunctionLayer;
import dmonner.xlbp.layer.Layer;
import dmonner.xlbp.layer.LogisticLayer;
import dmonner.xlbp.layer.PiLayer;
import dmonner.xlbp.layer.SigmaLayer;

public class MemoryCellCompound extends AbstractWeightedCompound
{
	private static final long serialVersionUID = 1L;

	private final int size;
	private Component[] activate;
	private Boolean truncateGates;

	private final SingletonCompound ug;
	private final WeightedCompound is;
	private final FunctionCompound ig;
	private final FunctionCompound fg;
	private final FunctionCompound og;
	private final FunctionCompound mc;

	private final InternalComponent mc_in_gated;
	private final InternalComponent mc_state;
	private final InternalComponent mc_state_squashed;
	private final InternalComponent mc_state_gated;

	public MemoryCellCompound(final MemoryCellCompound that, final NetworkCopier copier)
	{
		super(that, copier);

		this.size = that.size;
		this.truncateGates = that.truncateGates;

		this.ug = copier.getCopyOf(that.ug);
		this.is = copier.getCopyOf(that.is);
		this.ig = copier.getCopyOf(that.ig);
		this.fg = copier.getCopyOf(that.fg);
		this.og = copier.getCopyOf(that.og);
		this.mc = copier.getCopyOf(that.mc);

		this.mc_in_gated = copier.getCopyOf(that.mc_in_gated);
		this.mc_state = copier.getCopyOf(that.mc_state);
		this.mc_state_squashed = copier.getCopyOf(that.mc_state_squashed);
		this.mc_state_gated = copier.getCopyOf(that.mc_state_gated);

		// copy the activate array;
		this.activate = new Component[that.activate.length];
		for(int i = 0; i < that.activate.length; i++)
			this.activate[i] = copier.getCopyOf(that.activate[i]);

		// setup for superclass
		in = copier.getCopyOf(that.in);
		out = copier.getCopyOf(that.out);
	}

	public MemoryCellCompound(final String name, final int size, final boolean memory,
			final boolean inputGates, final boolean forgetGates, final boolean outputGates,
			final boolean squashState, final boolean squashInput)
	{
		this(name, size, memory, //
				squashInput ? new LogisticLayer(name + "InAct", size) : null, //
				inputGates ? new LogisticLayer(name + "IGAct", size) : null, //
				forgetGates ? new LogisticLayer(name + "FGAct", size) : null, //
				squashState ? new LogisticLayer(name + "MCAct", size) : null, //
				outputGates ? new LogisticLayer(name + "OGAct", size) : null);
	}

	public MemoryCellCompound(final String name, final int size, final boolean memory,
			final FunctionCompound inputSquash, final FunctionCompound inputGates,
			final FunctionCompound forgetGates, final FunctionCompound stateSquash,
			final FunctionCompound outputGates)
	{
		super(name);
		this.size = size;

		final List<Component> act = new ArrayList<>();

		if(inputSquash != null)
		{
			is = inputSquash;
			act.add(is);
		}
		else
		{
			// make it unbiased
			is = new LinearCompound(name + "In", size, false);
			act.add(is);
		}

		if(inputGates != null)
		{
			ig = inputGates;
			final PiLayer pi = new PiLayer(name + "MCInGated", size);
			pi.addUpstream(is);
			pi.addUpstream(ig);
			mc_in_gated = pi;

			act.add(ig);
			act.add(pi);
		}
		else
		{
			ig = null;
			mc_in_gated = is;
		}

		if(memory)
		{
			final UpstreamComponent mc_prev_gated;
			final CopySourceLayer mc_state_copy_src = new CopySourceLayer(name + "MCStateCopier", size);
			final CopyDestinationLayer mc_state_copy_dest = new CopyDestinationLayer(name + "MCPrev",
					mc_state_copy_src);
			final SingletonCompound st = new SingletonCompound(name + "MCState", new SigmaLayer(name
					+ "MCStateLayer", size));

			if(forgetGates != null)
			{
				fg = forgetGates;
				final PiLayer pi = new PiLayer(name + "MCPrevGated", size);
				pi.addUpstream(mc_state_copy_dest);
				pi.addUpstream(fg);
				mc_prev_gated = pi;

				act.add(fg);
				act.add(mc_state_copy_dest);
				act.add(pi);
			}
			else
			{
				fg = null;
				mc_prev_gated = mc_state_copy_dest;

				act.add(mc_state_copy_dest);
			}

			st.addUpstream(mc_in_gated);
			st.addUpstream(mc_prev_gated);
			mc_state_copy_src.addUpstream(st);

			ug = st;
			mc_state = mc_state_copy_src;

			act.add(ug);
			act.add(mc_state);
		}
		else
		{
			final SingletonCompound st = new SingletonCompound(name + "MCState", new SigmaLayer(name
					+ "MCStateLayer", size));
			st.addUpstream(mc_in_gated);
			fg = null;
			ug = st;
			mc_state = st;

			act.add(mc_state);
		}

		if(stateSquash != null)
		{
			mc = stateSquash;
			mc.addUpstream(mc_state);
			mc_state_squashed = mc;

			act.add(mc);
		}
		else
		{
			mc = null;
			final FanOutLayer fan = new FanOutLayer(name + "MCFan", size);
			fan.addUpstream(mc_state);
			mc_state_squashed = fan;

			act.add(fan);
		}

		if(outputGates != null)
		{
			og = outputGates;
			//final PiLayer pi = new DropoutPiLayer(name + "MCGated", size, 0.5F);
			final PiLayer pi = new PiLayer(name + "MCGated", size);
			final FanOutLayer fan = new FanOutLayer(name + "MCGatedFan", size);
			pi.addUpstream(mc_state_squashed);
			pi.addUpstream(og);
			fan.addUpstream(pi);
			mc_state_gated = fan;

			act.add(og);
			act.add(pi);
			act.add(fan);
		}
		else
		{
			og = null;
			mc_state_gated = mc_state_squashed;
		}

		activate = act.toArray(new Component[act.size()]);
		in = ((DownstreamComponent) activate[0]).asDownstreamLayer();
		out = ((UpstreamComponent) activate[activate.length - 1]).asUpstreamLayer();
	}

	public MemoryCellCompound(final String name, final int size, final boolean memory,
			final FunctionLayer inputSquash, final FunctionLayer inputGates,
			final FunctionLayer forgetGates, final FunctionLayer stateSquash,
			final FunctionLayer outputGates)
	{
		this(name, size, memory, //
				inputSquash == null ? null : new FunctionCompound(name + "In", inputSquash), //
				inputGates == null ? null : new FunctionCompound(name + "IG", inputGates), //
				forgetGates == null ? null : new FunctionCompound(name + "FG", forgetGates), //
				stateSquash == null ? null : new FunctionCompound(name + "MC", stateSquash), //
				outputGates == null ? null : new FunctionCompound(name + "OG", outputGates) //
		);
	}

	public MemoryCellCompound(final String name, final int size, final boolean memory,
			final String inFcn, final String igFcn, final String fgFcn, final String mcFcn,
			final String ogFcn)
	{
		this(name, size, memory, //
				Function.fcompound(inFcn, name + "In", size), //
				Function.fcompound(igFcn, name + "IG", size), //
				Function.fcompound(fgFcn, name + "FG", size), //
				Function.fcompound(mcFcn, name + "MC", size), //
				Function.fcompound(ogFcn, name + "OG", size) //
		);
	}

	public MemoryCellCompound(final String name, final int size, final boolean memory,
			final String[] fcns)
	{
		this(name, size, memory, //
				Function.layer(fcns[0], name + "InAct", size), //
				Function.layer(fcns[1], name + "IGAct", size), //
				Function.layer(fcns[2], name + "FGAct", size), //
				Function.layer(fcns[3], name + "MCAct", size), //
				Function.layer(fcns[4], name + "OGAct", size) //
		);

		if(fcns.length != 5)
			throw new IllegalArgumentException("Too many functions specified: " + fcns.length);
	}

	public MemoryCellCompound(final String name, final int size, final String type)
	{
		this(name, size, type, "logistic");
	}

	public MemoryCellCompound(final String name, final int size, final String type, final String fcn)
	{
		this(name, size, !type.contains("N"), new String[] { //
				type.contains("S") ? fcn : "none", //
						type.contains("I") ? fcn : "none", //
						type.contains("F") ? fcn : "none", //
						fcn, //
						type.contains("O") ? fcn : "none", //
				});

		if(type.contains("P"))
			addPeepholeConnections();

		if(type.contains("L") || type.contains("G"))
			addGatedLateralConnections();

		if(type.contains("U"))
			addUngatedLateralConnections();

		if(type.contains("T"))
			truncateGates(true);
	}

	@Override
	public void activateTest()
	{
		super.activateTest();
		for(int i = 0; i < activate.length; i++)
			activate[i].activateTest();
	}

	@Override
	public void activateTrain()
	{
		super.activateTrain();
		for(int i = 0; i < activate.length; i++)
			activate[i].activateTrain();
	}

	public void addGatedLateralConnections()
	{
		if(ig != null)
			ig.addUpstreamWeights(this);

		if(fg != null)
			fg.addUpstreamWeights(this);

		if(og != null)
			og.addUpstreamWeights(this);
	}

	public void addPeepholeConnections()
	{
		if(ig != null)
			ig.addUpstreamWeights(new DiagonalWeightBank(name + "IGPeephole", mc_state_squashed
					.asUpstreamLayer(), ig.getInput(), win, wut));

		if(fg != null)
			fg.addUpstreamWeights(new DiagonalWeightBank(name + "FGPeephole", mc_state_squashed
					.asUpstreamLayer(), fg.getInput(), win, wut));

		if(og != null)
			og.addUpstreamWeights(new DiagonalWeightBank(name + "OGPeephole", mc_state_squashed
					.asUpstreamLayer(), og.getInput(), win, wut));
	}

	public void addUngatedLateralConnections()
	{
		if(ig != null)
			ig.addUpstreamWeights(mc_state_squashed);

		if(fg != null)
			fg.addUpstreamWeights(mc_state_squashed);

		if(og != null)
			og.addUpstreamWeights(mc_state_squashed);
	}

	@Override
	public void addUpstream(final UpstreamComponent upstream)
	{
		is.addUpstream(upstream);

		if(ig != null)
			ig.addUpstream(upstream);

		if(fg != null)
			fg.addUpstream(upstream);

		if(og != null)
			og.addUpstream(upstream);
	}

	public void addUpstreamGatedLateralConnections()
	{
		if(ig != null)
			ig.addUpstreamWeights(this);

		if(fg != null)
			fg.addUpstreamWeights(this);
	}

	@Override
	public void addUpstreamWeights(final UpstreamComponent upstream)
	{
		super.addUpstreamWeights(upstream);

		if(ig != null)
			ig.addUpstreamWeights(upstream);

		if(fg != null)
			fg.addUpstreamWeights(upstream);

		if(og != null)
			og.addUpstreamWeights(upstream);
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
		super.clearActivations();
		for(int i = 0; i < activate.length; i++)
			activate[i].clearActivations();
	}

	@Override
	public void clearEligibilities()
	{
		super.clearEligibilities();
		for(int i = 0; i < activate.length; i++)
			activate[i].clearEligibilities();
	}

	@Override
	public void clearResponsibilities()
	{
		super.clearResponsibilities();
		for(int i = 0; i < activate.length; i++)
			activate[i].clearResponsibilities();
	}

	@Override
	public MemoryCellCompound copy(final NetworkCopier copier)
	{
		return new MemoryCellCompound(this, copier);
	}

	@Override
	public MemoryCellCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final MemoryCellCompound copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		super.copyConnectivityFrom(comp, copier);

		if(comp instanceof MemoryCellCompound)
		{
			final MemoryCellCompound that = (MemoryCellCompound) comp;

			if(this.ig != null && that.ig != null)
				this.ig.copyConnectivityFrom(that.ig, copier);

			if(this.fg != null && that.fg != null)
				this.fg.copyConnectivityFrom(that.fg, copier);

			if(this.mc != null && that.mc != null)
				this.mc.copyConnectivityFrom(that.mc, copier);

			if(this.og != null && that.og != null)
				this.og.copyConnectivityFrom(that.og, copier);
		}
	}

	@Override
	public Component[] getComponents()
	{
		return activate.clone();
	}

	public FunctionCompound getForgetGates()
	{
		return fg;
	}

	public Layer getGatedInput()
	{
		return mc_in_gated.asUpstreamLayer();
	}

	public FunctionCompound getInputGates()
	{
		return ig;
	}

	public FunctionCompound getMemoryCells()
	{
		return mc;
	}

	public Layer getNetInputLayer()
	{
		if(is instanceof FunctionCompound)
			return ((FunctionCompound) is).getActLayer();
		else
			return ((SingletonCompound) is).getLayer();
	}

	public FunctionCompound getOutputGates()
	{
		return og;
	}

	public Layer getStateLayer()
	{
		if(mc_state instanceof CopySourceLayer)
			return (CopySourceLayer) mc_state;
		else
			return ((SingletonCompound) mc_state).getLayer();
	}

	public SingletonCompound getUngatedInput()
	{
		return ug;
	}

	public InternalComponent getUngatedOutput()
	{
		return mc_state_squashed;
	}

	@Override
	public int nWeights()
	{
		int sum = super.nWeights();

		sum += is.nWeights();

		if(mc != null)
			sum += mc.nWeights();

		if(ug != null)
			sum += ug.nWeights();

		if(ig != null)
			sum += ig.nWeights();

		if(fg != null)
			sum += fg.nWeights();

		if(og != null)
			sum += og.nWeights();

		return sum;
	}

	@Override
	public boolean optimize()
	{
		if(!super.optimize())
			return false;

		final List<Component> act = new ArrayList<>();
		for(final Component comp : activate)
			act.add(comp);

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
		super.processBatch();

		if(is != null)
			is.processBatch();

		if(ig != null)
			ig.processBatch();

		if(fg != null)
			fg.processBatch();

		if(mc != null)
			mc.processBatch();

		if(ug != null)
			ug.processBatch();

		if(og != null)
			og.processBatch();
	}

	public void setPeepholeFullOnly(final boolean fullOnly)
	{
		if(ig != null)
		{
			for(int i = 0; i < ig.nUpstreamWeights(); i++)
			{
				final WeightBank bank = ig.getUpstreamWeights(i);
				if(bank instanceof DiagonalWeightBank && bank.getName().equals(name + "IGPeephole"))
					((DiagonalWeightBank) bank).setFullOnly(fullOnly);
			}
		}

		if(fg != null)
		{
			for(int i = 0; i < fg.nUpstreamWeights(); i++)
			{
				final WeightBank bank = fg.getUpstreamWeights(i);
				if(bank instanceof DiagonalWeightBank && bank.getName().equals(name + "FGPeephole"))
					((DiagonalWeightBank) bank).setFullOnly(fullOnly);
			}
		}

		if(og != null)
		{
			for(int i = 0; i < og.nUpstreamWeights(); i++)
			{
				final WeightBank bank = og.getUpstreamWeights(i);
				if(bank instanceof DiagonalWeightBank && bank.getName().equals(name + "OGPeephole"))
					((DiagonalWeightBank) bank).setFullOnly(fullOnly);
			}
		}
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		super.setWeightInitializer(win);

		if(is != null)
			is.setWeightInitializer(win);

		if(ig != null)
			ig.setWeightInitializer(win);

		if(fg != null)
			fg.setWeightInitializer(win);

		if(ug != null)
			ug.setWeightInitializer(win);

		if(mc != null)
			mc.setWeightInitializer(win);

		if(og != null)
			og.setWeightInitializer(win);
	}

	@Override
	public void setWeightUpdaterType(final WeightUpdaterType wut)
	{
		super.setWeightUpdaterType(wut);

		if(is != null)
			is.setWeightUpdaterType(wut);

		if(ig != null)
			ig.setWeightUpdaterType(wut);

		if(fg != null)
			fg.setWeightUpdaterType(wut);

		if(mc != null)
			mc.setWeightUpdaterType(wut);

		if(ug != null)
			ug.setWeightUpdaterType(wut);

		if(og != null)
			og.setWeightUpdaterType(wut);
	}

	public int size()
	{
		return size;
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		if(sb.showIntermediate())
		{
			super.toString(sb);

			sb.pushIndent();

			for(int i = activate.length - 1; i >= 0; i--)
				activate[i].toString(sb);

			for(final WeightBank bank : conn)
				bank.toString(sb);

			sb.popIndent();
		}
		else
		{
			super.toString(sb);

			sb.pushIndent();

			if(og != null)
				og.toString(sb);
				
			if(mc != null) {
				mc.toString(sb);
			}
			
			if(mc_state != null)  {
				mc_state.toString(sb);
			}
			
			for(final WeightBank bank : conn) {
				bank.getConnection().toString(sb);
			}

			if(fg != null)
				fg.toString(sb);

			if(ig != null)
				ig.toString(sb);

			sb.popIndent();
		}
	}

	@Override
	public void truncate(final boolean truncate)
	{
		super.truncate(truncate);
		if(ug != null)
			ug.truncate(truncate);
		if(mc != null)
			mc.truncate(truncate);
		truncateGates(truncate);
	}

	public void truncateGates(final boolean truncate)
	{
		this.truncateGates = truncate;

		if(ig != null)
			ig.truncate(truncateGates);

		if(fg != null)
			fg.truncate(truncateGates);

		if(og != null)
			og.truncate(truncateGates);
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
		super.updateEligibilities();
	}

	@Override
	public void updateResponsibilities()
	{
		for(int i = activate.length - 1; i >= 0; i--)
			activate[i].updateResponsibilities();
		super.updateResponsibilities();
	}

	@Override
	public void updateWeights()
	{
		for(int i = activate.length - 1; i >= 0; i--)
			activate[i].updateWeights();
		super.updateWeights();
	}
}
