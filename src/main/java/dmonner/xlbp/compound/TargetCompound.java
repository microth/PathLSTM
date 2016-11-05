package dmonner.xlbp.compound;

import java.util.Arrays;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.TargetComponent;
import dmonner.xlbp.layer.FunctionLayer;
import dmonner.xlbp.layer.TargetLayer;

public class TargetCompound extends FunctionCompound implements TargetComponent
{
	private static final long serialVersionUID = 1L;

	private final TargetLayer target;

	public TargetCompound(final String name, final FunctionLayer act, final TargetLayer target)
	{
		this(name, act, target, true);
	}

	public TargetCompound(final String name, final FunctionLayer act, final TargetLayer target,
			final boolean biases)
	{
		super(name, act, biases);
		this.target = target;

		target.addUpstream(super.getOutput());
	}

	public TargetCompound(final TargetCompound that, final NetworkCopier copier)
	{
		super(that, copier);
		this.target = copier.getCopyOf(that.target);
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			target.build();

			built = true;
		}
	}

	@Override
	public void clearActivations()
	{
		super.clearActivations();
		target.clearActivations();
	}

	@Override
	public void clearResponsibilities()
	{
		super.clearResponsibilities();
		target.clearResponsibilities();
	}

	@Override
	public TargetCompound copy(final NetworkCopier copier)
	{
		return new TargetCompound(this, copier);
	}

	@Override
	public TargetCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final TargetCompound copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public Component[] getComponents()
	{
		Component[] comps = super.getComponents();
		comps = Arrays.copyOf(comps, comps.length + 1);
		comps[comps.length - 1] = target;
		return comps;
	}

	public TargetLayer getTargetLayer()
	{
		return target;
	}

	@Override
	public boolean optimize()
	{
		if(!super.optimize())
			return false;

		// this will be fine
		target.optimize();

		return true;
	}

	@Override
	public void setTarget(final float[] activations)
	{
		target.setTarget(activations);
	}

	@Override
	public void setTarget(final float[] activations, final float weight)
	{
		target.setTarget(activations, weight);
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		target.toString(sb);
		super.toString(sb);
	}

	@Override
	public void unbuild()
	{
		super.unbuild();
		target.unbuild();
	}

	@Override
	public void updateResponsibilities()
	{
		target.updateResponsibilities();
		super.updateResponsibilities();
	}
}
