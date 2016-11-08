package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.util.MatrixTools;

public abstract class AbstractFunctionLayer extends AbstractInternalLayer implements FunctionLayer
{
	private static final long serialVersionUID = 1L;

	protected float[] x;
	protected float[] fprime;

	public AbstractFunctionLayer(final AbstractFunctionLayer that, final NetworkCopier copier)
	{
		super(that, copier);

		if(that.x != null)
			this.x = copier.copyState() ? MatrixTools.copy(that.x) : MatrixTools.empty(that.x);

		if(that.fprime != null)
			this.fprime = copier.copyState() ? MatrixTools.copy(that.fprime) : MatrixTools
					.empty(that.fprime);
	}

	public AbstractFunctionLayer(final String name, final int size)
	{
		super(name, size);
	}

	@Override
	public void activateTest()
	{
		for(int j = 0; j < size; j++)
			y[j] = f(j);
	}

	@Override
	public void activateTrain()
	{
		activateTest();

		for(int j = 0; j < size; j++)
			fprime[j] = fprime(j);
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			upstream.build();
			x = upstream.getActivations();
			y = new float[size];
			fprime = new float[size];
			d = new Responsibilities(size);

			built = true;
		}
	}

	@Override
	public abstract AbstractFunctionLayer copy(NetworkCopier copier);

	@Override
	public AbstractFunctionLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		super.toString(sb);
		sb.pushIndent();

		if(sb.showExtra())
		{
			sb.appendln("FPrime:");
			sb.pushIndent();
			sb.appendln(MatrixTools.toString(fprime));
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
		//System.err.println(name + "\t" + java.util.Arrays.toString(fprime));
		upstream.getResponsibilities(myIndexInUpstream).copyMul(d, fprime);
	}

}
