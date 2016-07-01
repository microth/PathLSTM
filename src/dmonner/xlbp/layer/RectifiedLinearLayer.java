package dmonner.xlbp.layer;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import dmonner.xlbp.NetworkCopier;

public class RectifiedLinearLayer extends AbstractFunctionLayer
{
	private static final long serialVersionUID = 1L;
	final private float dropoutrate;

	public RectifiedLinearLayer(final RectifiedLinearLayer that, final NetworkCopier copier)
	{
		super(that, copier);
		this.dropoutrate = that.dropoutrate;
	}

	public RectifiedLinearLayer(final String name, final int size, float dropoutrate)
	{
		super(name, size);
		this.dropoutrate = dropoutrate;
	}

	@Override
	public RectifiedLinearLayer copy(final NetworkCopier copier)
	{
		return new RectifiedLinearLayer(this, copier);
	}

	@Override
	public RectifiedLinearLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public float f(final int j)
	{
		//System.out.print(x[i][j]>0.0?x[i][j]:0.0);
		//if(j==x[0].length-1) System.out.println(); else System.out.print("\t");
		return x[j]>0.0?x[j]:0.0F;
	}

	@Override
	public float fprime(final int j)
	{
		return y[j]>0.0?1.0F:0.0F;
	}
	
	@Override
	public void activateTest()
	{		
		for(int j = 0; j < size; j++)
			y[j] = (1-dropoutrate) * f(j);
	}

	@Override
	public void activateTrain()
	{
		for(int j = 0; j < size; j++) {
			if(ThreadLocalRandom.current().nextFloat()>dropoutrate) {
				y[j] = f(j);
				fprime[j] = fprime(j);
			} else {
				y[j] = 0.0F;
				fprime[j] = 0.0F;
			}
		}
	}
}
