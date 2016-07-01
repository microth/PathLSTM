package dmonner.xlbp.compound;

import java.util.ArrayList;
import java.util.List;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.layer.FanOutLayer;
import dmonner.xlbp.layer.InternalLayer;
import dmonner.xlbp.util.MatrixTools;

public class SingletonCompound extends AbstractWeightedCompound
{
	private static final long serialVersionUID = 1L;

	private InternalLayer layer;
	private FanOutLayer fan;

	public SingletonCompound(final SingletonCompound that, final NetworkCopier copier)
	{
		super(that, copier);
		this.layer = copier.getCopyOf(that.layer);
		this.fan = copier.getCopyOf(that.fan);

		// setup for superclass
		in = copier.getCopyOf(that.in);
		out = copier.getCopyOf(that.out);
	}

	public SingletonCompound(final String name, final InternalLayer layer)
	{
		super(name);
		this.layer = layer;
		this.fan = new FanOutLayer(name + "FanOut", layer.size());
		this.fan.addUpstream(layer);
		in = layer;
		out = fan;
	}

	@Override
	public void activateTest()
	{
		super.activateTest();
		if(layer != null)
			layer.activateTest();
	}

	@Override
	public void activateTrain()
	{
		super.activateTrain();
		if(layer != null)
			layer.activateTrain();
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			if(layer != null)
				layer.build();
			if(fan != null)
				fan.build();

			built = true;
		}
	}

	@Override
	public void clearActivations()
	{
		super.clearActivations();
		if(layer != null)
			layer.clearActivations();
	}

	@Override
	public void clearResponsibilities()
	{
		super.clearResponsibilities();
		if(layer != null)
			layer.clearResponsibilities();
		if(fan != null)
			fan.clearResponsibilities();
	}

	@Override
	public SingletonCompound copy(final NetworkCopier copier)
	{
		return new SingletonCompound(this, copier);
	}

	@Override
	public SingletonCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final SingletonCompound copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public Component[] getComponents()
	{
		final List<Component> list = new ArrayList<Component>();
		if(layer != null)
			list.add(layer);
		if(fan != null)
			list.add(fan);
		return list.toArray(new Component[list.size()]);
	}

	public InternalLayer getLayer()
	{
		return layer;
	}

	@Override
	public boolean optimize()
	{
		boolean rv = false;

		if(layer != null && !layer.optimize())
		{
			layer = null;
			in = fan;
		}
		else
		{
			rv = true;
		}

		if(fan != null && !fan.optimize())
		{
			fan = null;
			out = layer;
		}
		else
		{
			rv = true;
		}

		return rv;
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		if(sb.showIntermediate())
		{
			super.toString(sb);

			sb.pushIndent();
			if(fan != null)
				fan.toString(sb);
			if(layer != null)
				layer.toString(sb);
			for(final WeightBank bank : conn)
				bank.toString(sb);
			sb.popIndent();
		}
		else
		{
			super.toString(sb);

			sb.pushIndent();

			if(layer != null)
			{
				if(sb.showActivations())
				{
					sb.appendln("Activations:");
					sb.pushIndent();
					sb.appendln(MatrixTools.toString(layer.getActivations()));
					sb.popIndent();
				}

				if(sb.showResponsibilities())
				{
					sb.appendln("Responsibilities:");
					sb.pushIndent();
					sb.appendln(layer.getResponsibilities().toString());
					sb.popIndent();
				}
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
		if(layer != null)
			layer.unbuild();
		if(fan != null)
			fan.unbuild();
	}

	@Override
	public void updateEligibilities()
	{
		if(fan != null)
			fan.updateEligibilities();
		if(layer != null)
			layer.updateEligibilities();
		super.updateEligibilities();
	}

	@Override
	public void updateResponsibilities()
	{
		if(fan != null)
			fan.updateResponsibilities();
		if(layer != null)
			layer.updateResponsibilities();
		super.updateResponsibilities();
	}

	@Override
	public void updateWeights()
	{
		if(layer != null)
			layer.updateWeights();
		super.updateWeights();
	}
}
