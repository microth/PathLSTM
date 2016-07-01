package dmonner.xlbp.layer;

import java.util.Arrays;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.util.MatrixTools;

public class PiLayer extends AbstractFanInLayer
{
	private static final long serialVersionUID = 1L;

	protected float[][] prod;
	protected float[][] buf;

	public PiLayer(final PiLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public PiLayer(final String name, final int size)
	{
		super(name, size);
	}

	@Override
	public void activateTest()
	{
		System.arraycopy(upstream[0].getActivations(), 0, y, 0, size);

		for(int k = 1; k < nUpstream; k++)
			MatrixTools.multiply(upstream[k].getActivations(), y, size);
	}

	@Override
	public void activateTrain()
	{
		activateTest();
		updateProd();
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			y = new float[size];
			d = new Responsibilities(size);
			buf = new float[nUpstream][size];
			prod = new float[nUpstream][size];

			built = true;
		}
	}

	@Override
	public PiLayer copy(final NetworkCopier copier)
	{
		return new PiLayer(this, copier);
	}

	@Override
	public PiLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		super.copyConnectivityFrom(comp, copier);

		if(comp instanceof PiLayer)
		{
			final PiLayer that = (PiLayer) comp;
			if(that.prod != null && that.buf != null)
			{
				if(copier.copyState() && nUpstream == that.nUpstream)
				{
					this.prod = MatrixTools.copy(that.prod);
					this.buf = MatrixTools.copy(that.buf);
				}
				else
				{
					this.prod = new float[nUpstream][size];
					this.buf = new float[nUpstream][size];
				}
			}
		}
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		super.toString(sb);
		sb.pushIndent();

		if(sb.showExtra())
		{
			sb.appendln("Prod:");
			sb.pushIndent();
			sb.appendln(MatrixTools.toString(prod));
			sb.popIndent();
		}

		sb.popIndent();
	}

	@Override
	public void updateEligibilities()
	{
		if(downstreamCopyLayer != null) {
			downstream.updateUpstreamResponsibilities(myIndexInDownstream);
		}
	}

	protected void updateProd()
	{
		/*
		 * The goal is to have, in prod[i], the product of the activations of all the upstream y's,
		 * except for y[i] itself. Here we use a clever algorithm similar to
		 * VectorTools.multiplyAllButI(), which I in turn learned from a Google interview :)
		 */

		final int n = nUpstream;

		Arrays.fill(buf[0], 1F);

		for(int k = 1; k < n; k++)
		{
			final float[] ykm1 = upstream[k - 1].getActivations();
			final float[] bk = buf[k];
			final float[] bkm1 = buf[k - 1];

			for(int j = 0; j < size; j++)
				bk[j] = bkm1[j] * ykm1[j];
		}

		Arrays.fill(prod[n - 1], 1F);

		for(int k = n - 2; k >= 0; k--)
		{
			final float[] ykp1 = upstream[k + 1].getActivations();
			final float[] pk = prod[k];
			final float[] pkp1 = prod[k + 1];

			for(int j = 0; j < size; j++)
				pk[j] = pkp1[j] * ykp1[j];
		}

		MatrixTools.multiplyElementwise(buf, prod, n, size);
	}

	@Override
	public void updateResponsibilities()
	{
		if(downstreamCopyLayer == null) {
			downstream.updateUpstreamResponsibilities(myIndexInDownstream);
		}			
	}

	@Override
	public void updateUpstreamResponsibilities(final int index)
	{		
		upstream[index].getResponsibilities(myIndexInUpstream[index]).copyMul(d, prod[index]);
	}
}
