package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.util.MatrixTools;

public abstract class AbstractTargetLayer extends AbstractDownstreamLayer implements TargetLayer
{
	private static final long serialVersionUID = 1L;

	protected float[] t;
	protected float w;

	public AbstractTargetLayer(final AbstractTargetLayer that, final NetworkCopier copier)
	{
		super(that, copier);

		if(copier.copyState())
			this.t = that.t;
	}

	public AbstractTargetLayer(final String name, final int size)
	{
		super(name, size);
	}

	@Override
	public void activateTest()
	{
		// Nothing to do -- activations are aliased from upstream layer.
	}

	@Override
	public void activateTrain()
	{
		// Nothing to do -- activations are aliased from upstream layer.
	}

	@Override
	public void aliasResponsibilities(final int index, final Responsibilities resp)
	{
		super.aliasResponsibilities(index, resp);
		upstream.aliasResponsibilities(myIndexInUpstream, resp);
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			upstream.build();
			y = upstream.getActivations();
			d = new Responsibilities(size);
			upstream.aliasResponsibilities(myIndexInUpstream, d);

			built = true;
		}
	}

	@Override
	public void clear()
	{
		// Nothing to do for activations -- they are aliased from upstream layer.
		// Nothing to do for deltas -- they will get scrubbed by the upstream layer.
		t = null;
		w = 1F;
	}

	@Override
	public abstract AbstractTargetLayer copy(NetworkCopier copier);

	@Override
	public AbstractTargetLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void setTarget(final float[] targets)
	{
		t = targets;
		w = 1F;
	}

	@Override
	public void setTarget(final float[] targets, final float weight)
	{
		t = targets;
		w = weight;
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		super.toString(sb);

		if(sb.showExtra())
		{
			sb.pushIndent();

			if(sb.showExtra())
			{
				if(t == null)
				{
					sb.appendln("Targets: null");
				}
				else
				{
					sb.appendln("Targets:");
					sb.pushIndent();
					sb.appendln(MatrixTools.toString(t));
					sb.popIndent();
				}
			}

			sb.popIndent();
		}
	}

	@Override
	public void updateEligibilities()
	{
		// Nothing to do.
	}

	@Override
	public void updateResponsibilities()
	{
		t = null;
	}

	@Override
	public void updateUpstreamResponsibilities(final int index)
	{
		// Nothing to do -- upstream ds are already aliased to this layer's d.
	}

}
