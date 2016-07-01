package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.Responsibilities;

public class SigmaLayer extends AbstractFanInLayer
{
	private static final long serialVersionUID = 1L;

	public SigmaLayer(final SigmaLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public SigmaLayer(final String name, final int size)
	{
		super(name, size);
	}

	@Override
	public void activateTest()
	{
		System.arraycopy(upstream[0].getActivations(), 0, y, 0, size);

		for(int k = 1; k < nUpstream; k++)
		{
			final float[] yk = upstream[k].getActivations();
			for(int j = 0; j < size; j++)
				y[j] += yk[j];
		}
	}

	@Override
	public void activateTrain()
	{
		activateTest();
	}

	@Override
	public void aliasResponsibilities(final int index, final Responsibilities resp)
	{
		super.aliasResponsibilities(index, resp);
		for(int i = 0; i < nUpstream; i++)
			upstream[i].aliasResponsibilities(myIndexInUpstream[i], resp);
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			y = new float[size];
			d = new Responsibilities(size);

			for(int i = 0; i < nUpstream; i++)
			{
				upstream[i].build();
				upstream[i].aliasResponsibilities(myIndexInUpstream[i], d);
			}

			built = true;
		}
	}

	@Override
	public SigmaLayer copy(final NetworkCopier copier)
	{
		return new SigmaLayer(this, copier);
	}

	@Override
	public SigmaLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void updateEligibilities()
	{
		if(downstreamCopyLayer != null)
			downstream.updateUpstreamResponsibilities(myIndexInDownstream);
	}

	@Override
	public void updateResponsibilities()
	{
		if(downstreamCopyLayer == null)
			downstream.updateUpstreamResponsibilities(myIndexInDownstream);
	}

	@Override
	public void updateUpstreamResponsibilities(final int index)
	{
		// Nothing to do -- upstream ds are already aliased to this layer's d.
	}

}
