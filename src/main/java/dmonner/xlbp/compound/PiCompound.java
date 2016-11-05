package dmonner.xlbp.compound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.UpstreamComponent;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.layer.FanOutLayer;
import dmonner.xlbp.layer.PiLayer;
import dmonner.xlbp.util.MatrixTools;

public class PiCompound extends AbstractWeightedCompound
{
	private static final long serialVersionUID = 1L;

	private final int size;
	private final PiLayer pi;
	private FanOutLayer fan;
	private FunctionCompound[] ins;

	public PiCompound(final PiCompound that, final NetworkCopier copier)
	{
		super(that, copier);
		this.size = that.size;
		this.pi = copier.getCopyOf(that.pi);
		this.fan = copier.getCopyOf(that.fan);

		this.ins = new FunctionCompound[that.ins.length];
		for(int i = 0; i < that.ins.length; i++)
			this.ins[i] = copier.getCopyOf(that.ins[i]);

		// setup for superclass
		in = copier.getCopyOf(that.in);
		out = copier.getCopyOf(that.out);
	}

	public PiCompound(final String name, final int size)
	{
		super(name);
		this.size = size;
		this.pi = new PiLayer(name + "Pi", size);
		this.fan = new FanOutLayer(name + "Fan", size);
		this.ins = new FunctionCompound[0];

		fan.addUpstream(pi);

		in = pi;
		out = fan;
	}

	@Override
	public void activateTest()
	{
		super.activateTest();
		for(final FunctionCompound in : ins)
			in.activateTest();
		pi.activateTest();
	}

	@Override
	public void activateTrain()
	{
		super.activateTrain();
		for(final FunctionCompound in : ins)
			in.activateTrain();
		pi.activateTrain();
	}

	/**
	 * Add a WeightBank from upstream to a new LogisticCompound that precedes the central PiLayer in
	 * this Compound.
	 */
	@Override
	public void addUpstreamWeights(final UpstreamComponent upstream)
	{
		ins = Arrays.copyOf(ins, ins.length + 1);
		final LogisticCompound log = new LogisticCompound(upstream.getName() + "Log", size);
		ins[ins.length - 1] = log;
		pi.addUpstream(log);
		log.addUpstreamWeights(upstream);
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			for(final FunctionCompound in : ins)
				in.build();

			pi.build();

			if(fan != null)
				fan.build();

			built = true;
		}
	}

	@Override
	public void clearActivations()
	{
		super.clearActivations();
		for(final FunctionCompound in : ins)
			in.clearActivations();
		pi.clearActivations();
	}

	@Override
	public void clearEligibilities()
	{
		super.clearEligibilities();
		for(final FunctionCompound in : ins)
			in.clearEligibilities();
	}

	@Override
	public void clearResponsibilities()
	{
		super.clearResponsibilities();
		for(final FunctionCompound in : ins)
			in.clearResponsibilities();
		pi.clearResponsibilities();
		if(fan != null)
			fan.clearResponsibilities();
	}

	@Override
	public PiCompound copy(final NetworkCopier copier)
	{
		return new PiCompound(this, copier);
	}

	@Override
	public PiCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final PiCompound copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public Component[] getComponents()
	{
		final List<Component> list = new ArrayList<Component>();
		for(final FunctionCompound in : ins)
			list.add(in);
		list.add(pi);
		if(fan != null)
			list.add(fan);
		return list.toArray(new Component[list.size()]);
	}

	@Override
	public int nWeights()
	{
		int sum = super.nWeights();
		for(final FunctionCompound in : ins)
			sum += in.nWeights();
		return sum;
	}

	@Override
	public boolean optimize()
	{
		if(!super.optimize())
			return false;

		final List<FunctionCompound> list = new ArrayList<FunctionCompound>(ins.length);
		for(final FunctionCompound in : ins)
			list.add(in);

		final Iterator<FunctionCompound> it = list.iterator();
		while(it.hasNext())
			if(!it.next().optimize())
				it.remove();

		ins = list.toArray(new FunctionCompound[list.size()]);

		if(!pi.optimize())
			return false;

		if(fan != null && !fan.optimize())
		{
			fan = null;
			out = pi;
		}

		return true;
	}

	@Override
	public void processBatch()
	{
		super.processBatch();
		for(final FunctionCompound in : ins)
			in.processBatch();
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		super.setWeightInitializer(win);
		for(final FunctionCompound in : ins)
			in.setWeightInitializer(win);
	}

	@Override
	public void setWeightUpdaterType(final WeightUpdaterType wut)
	{
		super.setWeightUpdaterType(wut);
		for(final FunctionCompound in : ins)
			in.setWeightUpdaterType(wut);
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
			pi.toString(sb);
			for(final FunctionCompound in : ins)
				in.toString(sb);
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
				sb.appendln(MatrixTools.toString(pi.getActivations()));
				sb.popIndent();
			}

			if(sb.showResponsibilities())
			{
				sb.appendln("Responsibilities:");
				sb.pushIndent();
				sb.appendln(pi.getResponsibilities().toString());
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
		for(final FunctionCompound in : ins)
			in.unbuild();
		pi.unbuild();
		if(fan != null)
			fan.unbuild();
	}

	@Override
	public void updateEligibilities()
	{
		if(fan != null)
			fan.updateEligibilities();
		pi.updateEligibilities();
		for(final FunctionCompound in : ins)
			in.updateEligibilities();
		super.updateEligibilities();
	}

	@Override
	public void updateResponsibilities()
	{
		if(fan != null)
			fan.updateResponsibilities();
		pi.updateResponsibilities();
		for(final FunctionCompound in : ins)
			in.updateResponsibilities();
		super.updateResponsibilities();
	}

	@Override
	public void updateWeights()
	{
		pi.updateWeights();
		for(final FunctionCompound in : ins)
			in.updateWeights();
		super.updateWeights();
	}
}
