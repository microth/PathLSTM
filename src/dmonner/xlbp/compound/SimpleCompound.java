package dmonner.xlbp.compound;

import java.util.ArrayList;
import java.util.List;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.layer.FanOutLayer;
import dmonner.xlbp.layer.SigmaLayer;
import dmonner.xlbp.layer.UpstreamLayer;
import dmonner.xlbp.util.MatrixTools;

public class SimpleCompound extends AbstractWeightedCompound
{
	private static final long serialVersionUID = 1L;

	private final int size;
	private SigmaLayer net;
	private FanOutLayer fan;
	private Component[] activate;

	public SimpleCompound(final SimpleCompound that, final NetworkCopier copier)
	{
		super(that, copier);
		this.size = that.size;
		this.net = copier.getCopyOf(that.net);
		this.fan = copier.getCopyOf(that.fan);

		this.activate = new Component[that.activate.length];
		for(int i = 0; i < that.activate.length; i++)
			this.activate[i] = copier.getCopyOf(that.activate[i]);

		// setup for superclass
		in = copier.getCopyOf(that.in);
		out = copier.getCopyOf(that.out);
	}

	public SimpleCompound(final String name, final int size)
	{
		super(name);
		this.size = size;
		this.net = new SigmaLayer(name + "Net", size);
		this.fan = new FanOutLayer(name + "Fanout", size);

		fan.addUpstream(net);

		activate = new Component[] { net, fan };

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
	public SimpleCompound copy(final NetworkCopier copier)
	{
		return new SimpleCompound(this, copier);
	}

	@Override
	public SimpleCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final SimpleCompound copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public Component[] getComponents()
	{
		return activate.clone();
	}

	@Override
	public boolean optimize()
	{
		if(!super.optimize())
			return false;

		final List<Component> activate = new ArrayList<Component>(2);

		if(net != null)
			if(net.optimize())
				activate.add(net);
			else
				net = null;

		if(fan != null)
			if(fan.optimize())
				activate.add(fan);
			else
				fan = null;

		this.activate = activate.toArray(new Component[activate.size()]);
		this.in = net == null ? fan : net;
		this.out = this.activate.length == 0 ? null
				: (UpstreamLayer) this.activate[this.activate.length - 1];

		return true;
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
