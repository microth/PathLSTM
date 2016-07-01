package dmonner.xlbp.layer;

import java.util.Arrays;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.WeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.util.MatrixTools;

public abstract class AbstractLayer implements Layer
{
	private static final long serialVersionUID = 1L;

	protected final String name;
	protected final int size;
	protected float[] y;
	protected Responsibilities d;
	protected boolean built;

	public AbstractLayer(final AbstractLayer that, final NetworkCopier copier)
	{
		this.name = copier.getCopyNameFrom(that);
		this.size = that.size;

		if(that.y != null)
			this.y = copier.copyState() ? MatrixTools.copy(that.y) : MatrixTools.empty(that.y);

		if(that.d != null)
			this.d = copier.copyState() ? that.d.copy() : new Responsibilities(that.d.size());
	}

	public AbstractLayer(final String name, final int size)
	{
		this.name = name;
		this.size = size;
	}

	@Override
	public void aliasResponsibilities(final int index, final Responsibilities resp)
	{
		d = resp;
	}

	@Override
	public void clear()
	{
		clearActivations();
		clearEligibilities();
		clearResponsibilities();
	}

	@Override
	public void clearActivations()
	{
		Arrays.fill(y, 0F);
	}

	@Override
	public void clearEligibilities()
	{
		// Nothing to do in most cases.
	}

	@Override
	public void clearResponsibilities()
	{
		d.clear();
	}

	@Override
	public int compareTo(final Component that)
	{
		return name.compareTo(that.getName());
	}

	@Override
	public abstract AbstractLayer copy(final NetworkCopier copier);

	@Override
	public AbstractLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void copyConnectivityFrom(final Component that, final NetworkCopier copier)
	{
		// Nothing to do by default.
	}

	@Override
	public float[] getActivations()
	{
		return y;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Responsibilities getResponsibilities()
	{
		return d;
	}

	@Override
	public Responsibilities getResponsibilities(final int index)
	{
		return d;
	}

	@Override
	public boolean isBuilt()
	{
		return built;
	}

	@Override
	public int nWeights()
	{
		return 0;
	}

	@Override
	public void processBatch()
	{
		// Nothing to do for most layers, since they have no weights.
	}

	@Override
	public void setWeightInitializer(final WeightInitializer win)
	{
		// Do nothing; most Layers will not have any connections to update.
	}

	@Override
	public void setWeightUpdaterType(final WeightUpdaterType wut)
	{
		// Do nothing; most Layers will not have any connections to update.
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		if(sb.showName())
		{
			sb.indent();
			sb.append(name);
			sb.append(" (");
			sb.append(String.valueOf(size()));
			sb.append(")");
			sb.append(" : ");
			sb.append(this.getClass().getSimpleName());
			sb.appendln();
		}

		sb.pushIndent();

		if(sb.showActivations())
		{
			sb.appendln("Activations:");
			sb.pushIndent();
			sb.appendln(MatrixTools.toString(y));
			sb.popIndent();
		}

		if(sb.showResponsibilities())
		{
			sb.appendln("Responsibilities:");
			sb.pushIndent();
			sb.appendln(d.toString());
			sb.popIndent();
		}

		sb.popIndent();
	}

	@Override
	public String toString(final String show)
	{
		final NetworkStringBuilder sb = new NetworkStringBuilder(show);
		toString(sb);
		return sb.toString();
	}

	@Override
	public void unbuild()
	{
		built = false;
	}

	@Override
	public void updateWeights()
	{
		// Do nothing; most Layers will not have any weights to update.
	}
}
