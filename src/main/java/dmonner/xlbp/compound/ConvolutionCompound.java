package dmonner.xlbp.compound;

import java.util.ArrayList;
import java.util.List;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.UpstreamComponent;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.layer.DownstreamLayer;
import dmonner.xlbp.layer.FanOutLayer;
import dmonner.xlbp.layer.OffsetLayer;
import dmonner.xlbp.layer.PiLayer;
import dmonner.xlbp.layer.ScaleLayer;
import dmonner.xlbp.layer.SigmaLayer;
import dmonner.xlbp.util.MatrixTools;

public class ConvolutionCompound extends AbstractInternalCompound implements InternalCompound
{
	private static final long serialVersionUID = 1L;

	private final int size;
	private final ScaleLayer scale;
	private final FanOutLayer af, bf;
	private final OffsetLayer[] ao, bo;
	private final PiLayer[] pi;
	private final SigmaLayer sigma;
	private FanOutLayer fanout;

	public ConvolutionCompound(final ConvolutionCompound that, final NetworkCopier copier)
	{
		super(that, copier);
		this.size = that.size;

		fanout = copier.getCopyOf(that.fanout);
		scale = copier.getCopyOf(that.scale);
		sigma = copier.getCopyOf(that.sigma);

		pi = new PiLayer[size];
		for(int i = 0; i < size; i++)
			pi[i] = copier.getCopyOf(that.pi[i]);

		af = copier.getCopyOf(that.af);
		ao = new OffsetLayer[size];
		for(int i = 0; i < size; i++)
			ao[i] = copier.getCopyOf(that.ao[i]);

		bf = copier.getCopyOf(that.bf);
		bo = new OffsetLayer[size];
		for(int i = 0; i < size; i++)
			bo[i] = copier.getCopyOf(that.bo[i]);

		// setup for superclass
		in = copier.getCopyOf(that.in);
		out = copier.getCopyOf(that.out);
	}

	public ConvolutionCompound(final String name, final int size)
	{
		super(name);
		this.size = size;

		fanout = new FanOutLayer(name + "FanOut", size);
		scale = new ScaleLayer(name + "Scale", size, 1F / size);
		sigma = new SigmaLayer(name + "Sigma", size);
		fanout.addUpstream(scale);
		scale.addUpstream(sigma);

		pi = new PiLayer[size];
		for(int i = 0; i < size; i++)
		{
			pi[i] = new PiLayer(name + "Pi" + i, size);
			sigma.addUpstream(pi[i]);
		}

		af = new FanOutLayer(name + "FanOutA", size);
		ao = new OffsetLayer[size];
		for(int i = 0; i < size; i++)
		{
			final int offset = i;
			ao[i] = new OffsetLayer(name + "OffsetA" + offset, size, offset);
			ao[i].addUpstream(af);
			pi[i].addUpstream(ao[i]);
		}

		bf = new FanOutLayer(name + "FanOutB", size);
		bo = new OffsetLayer[size];
		for(int i = 0; i < size; i++)
		{
			final int offset = size - i;
			bo[i] = new OffsetLayer(name + "OffsetB" + offset, size, offset);
			bo[i].addUpstream(bf);
			pi[i].addUpstream(bo[i]);
		}

		in = af;
		out = fanout;
	}

	@Override
	public void activateTest()
	{
		for(int i = 0; i < size; i++)
		{
			ao[i].activateTest();
			bo[i].activateTest();
			pi[i].activateTest();
		}

		sigma.activateTest();
		scale.activateTest();
	}

	@Override
	public void activateTrain()
	{
		for(int i = 0; i < size; i++)
		{
			ao[i].activateTrain();
			bo[i].activateTrain();
			pi[i].activateTrain();
		}

		sigma.activateTrain();
		scale.activateTrain();
	}

	@Override
	public void addUpstream(final UpstreamComponent upstream)
	{
		if(af.nUpstream() == 0)
			af.addUpstream(upstream);
		else if(bf.nUpstream() == 0)
			bf.addUpstream(upstream);
		else
			throw new IllegalStateException("ConvolutionCompound already has two inputs.");
	}

	@Override
	public void build()
	{
		if(!built)
		{
			af.build();
			bf.build();

			for(int i = 0; i < size; i++)
			{
				ao[i].build();
				bo[i].build();
				pi[i].build();
			}

			sigma.build();
			scale.build();

			if(fanout != null)
				fanout.build();

			built = true;
		}
	}

	@Override
	public void clearActivations()
	{
		af.clearActivations();
		bf.clearActivations();

		for(int i = 0; i < size; i++)
		{
			ao[i].clearActivations();
			bo[i].clearActivations();
			pi[i].clearActivations();
		}

		sigma.clearActivations();
		scale.clearActivations();
	}

	@Override
	public void clearEligibilities()
	{
		// Nothing to do; no incoming weights!
	}

	@Override
	public void clearResponsibilities()
	{
		af.clearResponsibilities();
		bf.clearResponsibilities();

		for(int i = 0; i < size; i++)
		{
			ao[i].clearResponsibilities();
			bo[i].clearResponsibilities();
			pi[i].clearResponsibilities();
		}

		sigma.clearResponsibilities();
		scale.clearResponsibilities();
	}

	@Override
	public ConvolutionCompound copy(final NetworkCopier copier)
	{
		return new ConvolutionCompound(this, copier);
	}

	@Override
	public ConvolutionCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final ConvolutionCompound copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public Component[] getComponents()
	{
		final List<Component> list = new ArrayList<Component>();
		list.add(af);
		list.add(bf);
		for(int i = 0; i < size; i++)
		{
			list.add(ao[i]);
			list.add(bo[i]);
			list.add(pi[i]);
		}
		list.add(sigma);
		list.add(scale);
		if(fanout != null)
			list.add(fanout);
		return list.toArray(new Component[list.size()]);
	}

	@Override
	public DownstreamLayer getInput()
	{
		return af;
	}

	@Override
	public DownstreamLayer getInput(final int index)
	{
		if(index == 0)
			return af;
		else if(index == 1)
			return bf;
		else
			throw new IllegalArgumentException("Index too large.");
	}

	@Override
	public int nInputs()
	{
		return 2;
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

		if(af.nUpstream() == 0 || bf.nUpstream() == 0)
			throw new IllegalArgumentException("Do not have the two required inputs.");

		af.optimize();
		bf.optimize();

		for(int i = 0; i < size; i++)
		{
			ao[i].optimize();
			bo[i].optimize();
			pi[i].optimize();
		}

		sigma.optimize();
		scale.optimize();

		if(fanout != null && !fanout.optimize())
		{
			fanout = null;
			out = sigma;
		}

		return true;
	}

	@Override
	public void processBatch()
	{
		// Nothing to do.
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
			if(fanout != null)
				fanout.toString(sb);
			scale.toString(sb);
			sigma.toString(sb);
			for(int i = 0; i < size; i++)
				pi[i].toString(sb);
			for(int i = 0; i < size; i++)
				bo[i].toString(sb);
			for(int i = 0; i < size; i++)
				ao[i].toString(sb);
			bf.toString(sb);
			af.toString(sb);
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
				sb.appendln(MatrixTools.toString(sigma.getActivations()));
				sb.popIndent();
			}

			if(sb.showResponsibilities())
			{
				sb.appendln("Responsibilities B:");
				sb.pushIndent();
				sb.appendln(bf.getResponsibilities().toString());
				sb.popIndent();

				sb.appendln("Responsibilities A:");
				sb.pushIndent();
				sb.appendln(af.getResponsibilities().toString());
				sb.popIndent();
			}
		}
	}

	@Override
	public void updateEligibilities()
	{
		if(fanout != null)
			fanout.updateEligibilities();
		scale.updateEligibilities();
		sigma.updateEligibilities();

		for(int i = 0; i < size; i++)
		{
			pi[i].updateEligibilities();
			bo[i].updateEligibilities();
			ao[i].updateEligibilities();
		}

		af.updateEligibilities();
		bf.updateEligibilities();
	}

	@Override
	public void updateResponsibilities()
	{
		if(fanout != null)
			fanout.updateResponsibilities();
		scale.updateResponsibilities();
		sigma.updateResponsibilities();

		for(int i = 0; i < size; i++)
		{
			pi[i].updateResponsibilities();
			bo[i].updateResponsibilities();
			ao[i].updateResponsibilities();
		}

		af.updateResponsibilities();
		bf.updateResponsibilities();
	}

	@Override
	public void updateWeights()
	{
		// Nothing to do
	}
}
