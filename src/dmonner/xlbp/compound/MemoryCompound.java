package dmonner.xlbp.compound;

import java.util.ArrayList;
import java.util.List;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.layer.BiasLayer;
import dmonner.xlbp.layer.CopyDestinationLayer;
import dmonner.xlbp.layer.CopySourceLayer;
import dmonner.xlbp.layer.FanOutLayer;
import dmonner.xlbp.layer.FunctionLayer;
import dmonner.xlbp.layer.LogisticLayer;
import dmonner.xlbp.layer.RepulsionLayer;
import dmonner.xlbp.layer.SigmaLayer;
import dmonner.xlbp.layer.UpstreamLayer;
import dmonner.xlbp.util.MatrixTools;

public class MemoryCompound extends AbstractWeightedCompound
{
	private static final long serialVersionUID = 1L;

	private final int size;
	private BiasLayer bias;
	private SigmaLayer net;
	private CopySourceLayer src;
	private CopyDestinationLayer dest;
	private final FunctionLayer act;
	private RepulsionLayer repel;
	private FanOutLayer fan;
	private Component[] activate;

	public MemoryCompound(final MemoryCompound that, final NetworkCopier copier)
	{
		super(that, copier);
		this.size = that.size;
		this.bias = copier.getCopyOf(that.bias);
		this.net = copier.getCopyOf(that.net);
		this.src = copier.getCopyOf(that.src);
		this.dest = copier.getCopyOf(that.dest);
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

	public MemoryCompound(final String name, final FunctionLayer act)
	{
		super(name);
		this.size = act.size();
		this.bias = new BiasLayer(name + "Biases", size);
		this.net = new SigmaLayer(name + "Net", size);
		this.src = new CopySourceLayer(name + "CopySrc", size);
		this.dest = new CopyDestinationLayer(name + "CopyDest", src);
		this.act = act;
		this.fan = new FanOutLayer(name + "Fanout", size);

		fan.addUpstream(act);
		act.addUpstream(src);
		src.addUpstream(net);
		net.addUpstream(bias);

		activate = new Component[] { bias, net, src, dest, act, fan };

		// setup for superclass
		in = net;
		out = fan;
	}

	public MemoryCompound(final String name, final int size)
	{
		this(name, new LogisticLayer(name + "Log", size));
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
			activate = new Component[] { bias, net, src, dest, act, repel, fan };
		else
			activate = new Component[] { net, src, dest, act, repel, fan };
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
	public void clear()
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
	public MemoryCompound copy(final NetworkCopier copier)
	{
		return new MemoryCompound(this, copier);
	}

	@Override
	public MemoryCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final MemoryCompound copy = copy(copier);
		copier.build();
		return copy;
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

	public CopyDestinationLayer getCopyDestination()
	{
		return dest;
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

		final List<Component> activate = new ArrayList<Component>(7);

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

		if(src != null)
			if(src.optimize())
				activate.add(src);
			else
				src = null;

		if(dest != null)
			if(dest.optimize())
				activate.add(dest);
			else
				dest = null;

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
	public void processBatch()
	{
		super.processBatch();

		if(bias != null)
			bias.processBatch();
	}

	public void removeBiases()
	{
		net.removeUpstream(bias);
		bias = null;

		if(repel != null)
			activate = new Component[] { net, src, dest, act, repel, fan };
		else
			activate = new Component[] { net, src, dest, act, fan };
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
}
