package dmonner.xlbp.compound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.UniformWeightInitializer;
import dmonner.xlbp.UpstreamComponent;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.connection.ConnectionType;
import dmonner.xlbp.layer.UpstreamLayer;

public abstract class AbstractWeightedCompound extends AbstractInternalCompound implements
		WeightedCompound
{
	private static final long serialVersionUID = 1L;

	protected WeightInitializer win;
	protected WeightUpdaterType wut;
	protected WeightBank[] conn;
	private Boolean truncate;

	public AbstractWeightedCompound(final AbstractWeightedCompound that, final NetworkCopier copier)
	{
		super(that, copier);
		this.win = that.win;
		this.wut = that.wut;
		this.truncate = that.truncate;
		this.conn = new WeightBank[0];

		for(final WeightBank bank : that.conn)
			copier.addWeightBank(bank);
	}

	public AbstractWeightedCompound(final String name)
	{
		super(name);
		this.win = new UniformWeightInitializer();
		this.wut = WeightUpdaterType.basic();
		this.truncate = null;
		this.conn = new WeightBank[0];
	}

	@Override
	public void activateTest()
	{
		for(final WeightBank bank : conn) {
			bank.activateTest();
		}
	}

	@Override
	public void activateTrain()
	{
		for(final WeightBank bank : conn) {
			//if(name.equals("Hidden")) System.err.println(name + "\t" + bank.getWeightInput().getActivations()[0]);
			bank.activateTrain();
		}
	}

	public void addUpstream(final UpstreamComponent upstream, final ConnectionType type)
	{
		if(type == ConnectionType.WEIGHTED)
			addUpstreamWeights(upstream);
		else if(type == ConnectionType.DIRECT)
			addUpstream(upstream);
		else if(type == ConnectionType.INDIRECT)
			addUpstreamWeights(new IndirectWeightBank(upstream.getName() + "IndirectTo" + name,
					upstream.asUpstreamLayer(), in, win, wut));
		else if(type == ConnectionType.DIAGONAL)
			addUpstreamWeights(new DiagonalWeightBank(upstream.getName() + "DiagonalTo" + name,
					upstream.asUpstreamLayer(), in, win, wut));
		else
			throw new IllegalArgumentException("Unhandled ConnectionType: " + type);
	}

	private void addUpstreamWeights(final String incomingName, final UpstreamLayer upstream)
	{
		addUpstreamWeights(new WeightBank(incomingName + "To" + name, upstream, in, win, wut));
	}

	@Override
	public void addUpstreamWeights(final UpstreamComponent upstream)
	{
		addUpstreamWeights(upstream.getName(), upstream.asUpstreamLayer());
	}

	public void addUpstreamWeights(final WeightBank bank)
	{
		bank.setWeightUpdaterType(wut);
		bank.setWeightInitializer(win);

		if(truncate != null)
			bank.truncate(truncate);

		final int index = conn.length;
		conn = Arrays.copyOf(conn, conn.length + 1);
		conn[index] = bank;
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			for(final WeightBank bank : conn)
				bank.build();

			built = true;
		}
	}

	@Override
	public void clearActivations()
	{
		for(final WeightBank bank : conn)
			bank.clearActivations();
	}

	@Override
	public void clearEligibilities()
	{
		for(final WeightBank bank : conn)
			bank.clearEligibilities();
	}

	@Override
	public void clearResponsibilities()
	{
		for(final WeightBank bank : conn)
			bank.clearResponsibilities();
	}

	@Override
	public abstract AbstractWeightedCompound copy(NetworkCopier copier);

	@Override
	public AbstractWeightedCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final AbstractWeightedCompound copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		super.copyConnectivityFrom(comp, copier);

		if(comp instanceof AbstractWeightedCompound)
		{
			final AbstractWeightedCompound that = (AbstractWeightedCompound) comp;

			final List<WeightBank> list = new ArrayList<WeightBank>(that.conn.length);
			for(final WeightBank bank : that.conn)
				if(copier.copyExists(bank))
					list.add(copier.getCopyOf(bank));

			this.conn = list.toArray(new WeightBank[list.size()]);
		}
	}

	@Override
	public WeightBank getUpstreamWeights()
	{
		return getUpstreamWeights(0);
	}

	@Override
	public WeightBank getUpstreamWeights(final int index)
	{
		return conn[index];
	}

	@Override
	public int nUpstreamWeights()
	{
		return conn.length;
	}

	@Override
	public int nWeights()
	{
		int sum = 0;

		for(final WeightBank bank : conn)
			sum += bank.nWeights();

		return sum;
	}

	@Override
	public boolean optimize()
	{
		if(!super.optimize())
			return false;

		if(in == null)
			throw new IllegalStateException("Missing input layer.");

		// these will be fine
		for(final WeightBank bank : conn)
			bank.optimize();

		return true;
	}

	@Override
	public void processBatch()
	{
		for(final WeightBank bank : conn) {
			bank.processBatch();
		}
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		this.win = win;
		for(final WeightBank bank : conn)
			bank.setWeightInitializer(win);
	}

	@Override
	public void setWeightUpdaterType(final WeightUpdaterType wut)
	{
		this.wut = wut;
		for(final WeightBank bank : conn)
			bank.setWeightUpdaterType(wut);
	}

	public void truncate(final boolean truncate)
	{
		this.truncate = truncate;
		for(final WeightBank bank : conn)
			bank.truncate(truncate);
	}

	@Override
	public void unbuild()
	{
		super.unbuild();
		for(final WeightBank bank : conn)
			bank.unbuild();
	}

	@Override
	public void updateEligibilities()
	{
		for(final WeightBank bank : conn)
			bank.updateEligibilities();
	}

	@Override
	public void updateResponsibilities()
	{
		for(final WeightBank bank : conn)
			bank.updateResponsibilities();
	}

	@Override
	public void updateWeights()
	{
		for(final WeightBank bank : conn)
			bank.updateWeights();
	}
}
