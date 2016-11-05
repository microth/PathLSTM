package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.Responsibilities;

/**
 * Implements Thuan Huynh's E_2 term, which acts as a force that repels the weight vectors from each
 * other. The force is applied by adding an additional error responsibility which is the difference
 * between the average unit activations and the current activations. Seems to be useful for tightly
 * clustering hidden layer representations, making them more amenable to, for example, principal
 * component analysis. Empirical results suggest that adding this force during training does not
 * significantly impact performance.
 * 
 * @author dmonner
 */
public class RepulsionLayer extends AbstractInternalLayer
{
	private static final long serialVersionUID = 1L;

	private float[] mu, buf;
	private boolean mu_init;
	private final float adjust, retain, amount;

	public RepulsionLayer(final RepulsionLayer that, final NetworkCopier copier)
	{
		super(that, copier);

		this.mu = that.mu == null ? null : that.mu.clone();
		this.buf = that.buf == null ? null : that.mu.clone();
		this.adjust = that.adjust;
		this.retain = that.retain;
		this.amount = that.amount;
		this.mu_init = that.mu_init;
	}

	public RepulsionLayer(final String name, final int size)
	{
		this(name, size, 0.95F, 0.10F);
	}

	public RepulsionLayer(final String name, final int size, final float retain, final float amount)
	{
		super(name, size);

		if(0F >= retain || retain >= 1F)
			throw new IllegalArgumentException("Retain value must be in interval (0, 1).");

		this.adjust = 1F - retain;
		this.retain = retain;
		this.amount = amount;
	}

	@Override
	public void activateTest()
	{
		// Nothing to do; activations aliased to upstream layer
	}

	@Override
	public void activateTrain()
	{
		// Nothing to do; activations aliased to upstream layer

		// general case: update exponential average activation
		if(mu_init)
		{
			for(int j = 0; j < size; j++)
				mu[j] = y[j] * adjust + mu[j] * retain;
		}
		// first run: initialize the exponential average with the current activation
		else
		{
			System.arraycopy(y, 0, mu, 0, size);
			mu_init = true;
		}
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			upstream.build();
			y = upstream.getActivations();
			mu = new float[size];
			buf = new float[size];
			d = new Responsibilities(size);

			built = true;
		}
	}

	@Override
	public void clearActivations()
	{
		// Nothing to do; activations aliased to upstream layer
	}

	@Override
	public RepulsionLayer copy(final NetworkCopier copier)
	{
		return new RepulsionLayer(this, copier);
	}

	@Override
	public RepulsionLayer copy(final String nameSuffix)
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
		upstream.getResponsibilities(myIndexInUpstream).copyPlusScaledDiff(d, mu, y, amount);
	}
}
