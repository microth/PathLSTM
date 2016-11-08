package dmonner.xlbp.compound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dmonner.xlbp.Component;
import dmonner.xlbp.InputComponent;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.layer.FanOutLayer;
import dmonner.xlbp.layer.InputLayer;
import dmonner.xlbp.util.NoiseGenerator;

public class InputCompound extends AbstractCompound implements InputComponent
{
	private static final long serialVersionUID = 1L;

	private final InputLayer input;
	private FanOutLayer fan;
	private boolean responsible;

	public InputCompound(final InputCompound that, final NetworkCopier copier)
	{
		super(that, copier);
		this.input = copier.getCopyOf(that.input);
		this.fan = copier.getCopyOf(that.fan);

		// setup for superclass
		out = copier.getCopyOf(that.out);
	}

	public InputCompound(final String name, final int size)
	{
		this(name, size, null);
	}

	public InputCompound(final String name, final int size, final NoiseGenerator noise)
	{
		super(name);

		if(noise == null)
			input = new InputLayer(name + "Input", size);
		else {
			input = null;
			try {				
				throw new Exception("NoiseGenerator currently not supported");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		fan = new FanOutLayer(name + "Fanout", size);

		fan.addUpstream(input);

		out = fan;
	}

	@Override
	public void activateTest()
	{
		// Nothing to do.
	}

	@Override
	public void activateTrain()
	{
		// Nothing to do.
	}

	@Override
	public void build()
	{
		if(!built)
		{
			input.build();
			if(fan != null)
				fan.build();

			built = true;
		}
	}

	@Override
	public void clearActivations()
	{
		input.clearActivations();
	}

	@Override
	public void clearEligibilities()
	{
		// Nothing to do; no incoming weights!
	}

	@Override
	public void clearResponsibilities()
	{
		// Generally, do nothing; unless we're set to responsible, then clear
		if(responsible && fan != null)
			fan.clearResponsibilities();
	}

	@Override
	public InputCompound copy(final NetworkCopier copier)
	{
		return new InputCompound(this, copier);
	}

	@Override
	public InputCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final InputCompound copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public Component[] getComponents()
	{
		final List<Component> list = new ArrayList<>();
		list.add(input);
		if(fan != null)
			list.add(fan);
		return list.toArray(new Component[list.size()]);
	}

	public InputLayer getInputLayer()
	{
		return input;
	}

	@Override
	public int nWeights()
	{
		return 0;
	}

	@Override
	public boolean optimize()
	{
		if(!super.optimize())
			return false;

		// this will be fine
		input.optimize();

		// this could go away; if so, set the "out" layer to be the input itself
		if(fan != null && !fan.optimize())
		{
			out = input;
			fan = null;
		}

		return true;
	}

	@Override
	public void processBatch()
	{
		// Nothing to do.
	}

	public void setResponsible(final boolean responsible)
	{
		this.responsible = responsible;
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		// Nothing to do.
	}

	@Override
	public void setWeightUpdaterType(final WeightUpdaterType wut)
	{
		// Nothing to do.
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
			input.toString(sb);
			sb.popIndent();
		}
		else
		{
			input.toString(sb);
		}
	}

	@Override
	public void unbuild()
	{
		super.unbuild();
		input.unbuild();
		if(fan != null)
			fan.unbuild();
	}

	@Override
	public void updateEligibilities()
	{
		// Nothing to do.
	}

	@Override
	public void updateResponsibilities()
	{
		// Generally, do nothing; unless we're set to responsible, then propagate to input nodes.
		if(responsible && fan != null)
			fan.updateResponsibilities();
	}

	@Override
	public void updateWeights()
	{
		// Nothing to do.
	}

	@Override
	public void setInput(Map<Integer, Float> activations) {
		input.setInput(activations);
	}
}
