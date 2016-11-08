package dmonner.xlbp.layer;

import java.util.concurrent.ThreadLocalRandom;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.util.MatrixTools;

public class DropoutPiLayer extends PiLayer
{
	private static final long serialVersionUID = 1L;
	private final float dropoutrate;
	
	private boolean[] dropoutmask;
	
	
	public DropoutPiLayer(final DropoutPiLayer that, final NetworkCopier copier)
	{
		super(that, copier);
		this.dropoutrate = that.dropoutrate;
		dropoutmask = new boolean[that.size];
	}

	public DropoutPiLayer(final String name, final int size, float dropoutrate)
	{
		super(name, size);
		this.dropoutrate = dropoutrate;
		dropoutmask = new boolean[size];
	}
	
	@Override
	public void clearActivations()
	{
		super.clearActivations();	
		for(int j=0; j<size; j++)
			dropoutmask[j] = (ThreadLocalRandom.current().nextFloat()>dropoutrate);
	}

	@Override
	public void activateTest()
	{
		System.arraycopy(upstream[0].getActivations(), 0, y, 0, size);

		for(int k = 1; k < nUpstream; k++)
			MatrixTools.multiply(upstream[k].getActivations(), y, size);
		
		for(int j=0; j<y.length; j++)
			y[j] *= (1-dropoutrate);
	}

	@Override
	public void activateTrain()
	{
		System.arraycopy(upstream[0].getActivations(), 0, y, 0, size);	
		for(int k = 1; k < nUpstream; k++)
			MatrixTools.multiply(upstream[k].getActivations(), y, size);
		
		for(int j=0; j<y.length; j++) {
			y[j] *= dropoutmask[j]?1.0F:0.0F;
		}
		
		super.updateProd();	
	}

	@Override
	public DropoutPiLayer copy(final NetworkCopier copier)
	{
		return new DropoutPiLayer(this, copier);
	}

	@Override
	public DropoutPiLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}
}
