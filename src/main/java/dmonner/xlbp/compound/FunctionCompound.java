package dmonner.xlbp.compound;

import java.util.ArrayList;
import java.util.List;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.layer.BiasLayer;
import dmonner.xlbp.layer.FanOutLayer;
import dmonner.xlbp.layer.FunctionLayer;
import dmonner.xlbp.layer.RepulsionLayer;
import dmonner.xlbp.layer.SigmaLayer;
import dmonner.xlbp.layer.UpstreamLayer;
import dmonner.xlbp.util.MatrixTools;

public class FunctionCompound extends AbstractWeightedCompound
{
	private static final long serialVersionUID = 1L;

	private final int size;
	private BiasLayer bias;
	private SigmaLayer net;
	private final FunctionLayer act;
	private RepulsionLayer repel;
	private FanOutLayer fan;
	private Component[] activate;

	public FunctionCompound(final FunctionCompound that, final NetworkCopier copier)
	{
		super(that, copier);
		this.size = that.size;
		this.bias = copier.getCopyOf(that.bias);
		this.net = copier.getCopyOf(that.net);
		this.act = copier.getCopyOf(that.act);
		this.repel = copier.getCopyOf(that.repel);
		this.fan = copier.getCopyOf(that.fan);

		this.activate = new Component[that.activate.length];
		for(int i = 0; i < that.activate.length; i++)
			this.activate[i] = copier.getCopyOf(that.activate[i]);

		// setup for superclass
		in = copier.getCopyOf(that.in);
		out = copier.getCopyOf(that.out);
	}

	public FunctionCompound(final String name, final FunctionLayer act)
	{
		this(name, act, true);
	}

	public FunctionCompound(final String name, final FunctionLayer act, final boolean biases)
	{
		super(name);
		this.size = act.size();
		this.bias = biases ? new BiasLayer(name + "Biases", size) : null;
		this.net = new SigmaLayer(name + "Net", size);
		this.act = act;
		this.fan = new FanOutLayer(name + "Fanout", size);

		fan.addUpstream(act);
		act.addUpstream(net);
		if(bias != null)
			net.addUpstream(bias);

		if(bias != null)
			activate = new Component[] { bias, net, act, fan };
		else
			activate = new Component[] { net, act, fan };

		// setup for superclass
		in = net;
		out = fan;
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

	public void addRepulsion(final float retain, final float amount)
	{
		repel = new RepulsionLayer(name + "Repel", size, retain, amount);
		fan.removeUpstream(0);
		// act.removeDownstream(0);
		fan.addUpstream(repel);
		repel.addUpstream(act);

		if(bias != null)
			activate = new Component[] { bias, net, act, repel, fan };
		else
			activate = new Component[] { net, act, repel, fan };
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
	public FunctionCompound copy(final NetworkCopier copier)
	{
		return new FunctionCompound(this, copier);
	}

	@Override
	public FunctionCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final FunctionCompound copy = copy(copier);
		copier.build();
		return copy;
	}

	public FunctionLayer getActLayer()
	{
		return act;
	}

	public BiasLayer getBiasInput()
	{
		return bias;
	}

	@Override
	public Component[] getComponents()
	{
		return activate.clone();
	}

	public SigmaLayer getNetLayer()
	{
		return net;
	}

	@Override
	public int nWeights()
	{
		int n = super.nWeights();
		if(bias != null)
			n += bias.nWeights();
		return n;
	}

	@Override
	public boolean optimize()
	{
		if(!super.optimize())
			return false;

		final List<Component> activate = new ArrayList<>(5);

		if(bias != null)
			if(bias.optimize())
				activate.add(bias);
			else
				bias = null;

		if(net != null)
			if(net.optimize())
				activate.add(net);
			else
				net = null;

		if(act != null && act.optimize())
			activate.add(act);
		else
			throw new IllegalStateException("Optimized out the activation FunctionLayer in " + name);

		if(repel != null)
			if(repel.optimize())
				activate.add(repel);
			else
				repel = null;

		if(fan != null)
			if(fan.optimize())
				activate.add(fan);
			else
				fan = null;

		this.activate = activate.toArray(new Component[activate.size()]);
		this.in = net == null ? act : net;
		this.out = (UpstreamLayer) this.activate[this.activate.length - 1];

		return true;
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		super.setWeightInitializer(win);

		if(bias != null)
			bias.setWeightInitializer(win);
	}

	@Override
	public void setWeightUpdaterType(final WeightUpdaterType wut)
	{
		super.setWeightUpdaterType(wut);

		if(bias != null)
			bias.setWeightUpdaterType(wut);
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

			if(sb.showActivations())
			{
				sb.appendln("Activations:");
				sb.pushIndent();
				sb.appendln(MatrixTools.toString(act.getActivations()));
				sb.popIndent();
			}

			if(sb.showStates() && net != null)
			{
				sb.appendln("States:");
				sb.pushIndent();
				sb.appendln(MatrixTools.toString(net.getActivations()));
				sb.popIndent();
			}

			if(sb.showResponsibilities())
			{
				sb.appendln("Responsibilities:");
				sb.pushIndent();
				sb.appendln(net.getResponsibilities().toString());
				sb.popIndent();
			}

			if(bias != null)
				bias.getConnection().toString(sb);

			for(final WeightBank bank : conn)
				bank.getConnection().toString(sb);

			sb.popIndent();
		}
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
	
	@Override
	public void processBatch()
	{
		for(int i = activate.length - 1; i >= 0; i--)
			activate[i].processBatch();
		super.processBatch();
	}
}
