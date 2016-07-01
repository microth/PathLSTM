package dmonner.xlbp.compound;

import dmonner.xlbp.Component;
import dmonner.xlbp.DownstreamComponent;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.layer.UpstreamLayer;

public abstract class AbstractCompound implements Compound
{
	private static final long serialVersionUID = 1L;

	protected final String name;
	protected UpstreamLayer out;
	protected boolean built;

	public AbstractCompound(final AbstractCompound that, final NetworkCopier copier)
	{
		this.name = copier.getCopyNameFrom(that);
	}

	public AbstractCompound(final String name)
	{
		this.name = name;
	}

	@Override
	public void addDownstream(final DownstreamComponent downstream)
	{
		out.addDownstream(downstream);
	}

	@Override
	public UpstreamLayer asUpstreamLayer()
	{
		return out;
	}

	@Override
	public void build()
	{
		if(!built)
		{
			built = true;
		}
	}

	@Override
	public void clear()
	{
		clearActivations();
		clearEligibilities();
		clearResponsibilities();
	}

	@Override
	public int compareTo(final Component that)
	{
		return name.compareTo(that.getName());
	}

	@Override
	public boolean connectedDownstream(final DownstreamComponent downstream)
	{
		return out.connectedDownstream(downstream);
	}

	@Override
	public abstract AbstractCompound copy(final NetworkCopier copier);

	@Override
	public AbstractCompound copy(final String nameSuffix)
	{
		final NetworkCopier copier = new NetworkCopier(nameSuffix);
		final AbstractCompound copy = copy(copier);
		copier.build();
		return copy;
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		if(comp instanceof AbstractCompound)
		{
			final AbstractCompound that = (AbstractCompound) comp;
			this.out.copyConnectivityFrom(that.out, copier);
		}
	}

	@Override
	public DownstreamComponent getDownstream()
	{
		return out.getDownstream();
	}

	@Override
	public DownstreamComponent getDownstream(final int index)
	{
		return out.getDownstream(index);
	}

	@Override
	public int getIndexInDownstream()
	{
		return out.getIndexInDownstream();
	}

	@Override
	public int getIndexInDownstream(final int index)
	{
		return out.getIndexInDownstream(index);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public UpstreamLayer getOutput()
	{
		return out;
	}

	@Override
	public UpstreamLayer getOutput(final int index)
	{
		if(index > 0)
			throw new IllegalArgumentException("Index too large.");

		return out;
	}

	@Override
	public int indexOfDownstream(final DownstreamComponent downstream)
	{
		return out.indexOfDownstream(downstream);
	}

	@Override
	public boolean isBuilt()
	{
		return built;
	}

	@Override
	public int nDownstream()
	{
		return out.nDownstream();
	}

	@Override
	public int nOutputs()
	{
		return 1;
	}

	@Override
	public boolean optimize()
	{
		if(out == null)
			throw new IllegalStateException("Missing output layer.");

		return true;
	}

	@Override
	public void removeDownstream(final DownstreamComponent downstream)
	{
		out.removeDownstream(downstream);
	}

	@Override
	public void removeDownstream(final int index)
	{
		out.removeDownstream(index);
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
			sb.append(" : ");
			sb.append(this.getClass().getSimpleName());
			sb.appendln();
		}
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
}
