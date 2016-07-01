package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;

public class XEntropyTargetLayer extends AbstractTargetLayer
{
	private static final long serialVersionUID = 1L;

	public XEntropyTargetLayer(final String name, final int size)
	{
		super(name, size);
	}

	public XEntropyTargetLayer(final XEntropyTargetLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	@Override
	public XEntropyTargetLayer copy(final NetworkCopier copier)
	{
		return new XEntropyTargetLayer(this, copier);
	}

	@Override
	public XEntropyTargetLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void updateResponsibilities()
	{
		if(t == null)
			d.clear();
		else
			d.target(t, y, w);

		super.updateResponsibilities();
	}
	
	public void weightResult(float[] weight) {
		for(int i=0; i<t.length; i++) {
			t[i] *= weight[i];
			y[i] *= weight[i];
		}		
		
	}
}
