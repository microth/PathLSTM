package dmonner.xlbp.layer;

import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.util.MatrixTools;

public class OffsetLayer extends AbstractInternalLayer
{
	private static final long serialVersionUID = 1L;

	private final int offset;
	private float[] x;

	public OffsetLayer(final OffsetLayer that, final NetworkCopier copier)
	{
		super(that, copier);
		this.offset = that.offset;
		if(that.x != null)
			this.x = copier.copyState() ? MatrixTools.copy(that.x) : MatrixTools.empty(that.x);
	}

	public OffsetLayer(final String name, final int size, final int offset)
	{
		super(name, size);
		this.offset = offset;
	}

	@Override
	public void activateTest()
	{
		MatrixTools.rotateRight(y, x, offset);
	}

	@Override
	public void activateTrain()
	{
		activateTest();
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
			d = new Responsibilities(size);

			built = true;
		}
	}

	@Override
	public OffsetLayer copy(final NetworkCopier copier)
	{
		return new OffsetLayer(this, copier);
	}

	@Override
	public OffsetLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		super.toString(sb);
		sb.pushIndent();

		if(sb.showExtra())
			sb.appendln("Offset: " + offset);

		sb.popIndent();
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
		final Responsibilities dup = upstream.getResponsibilities(myIndexInUpstream);
		MatrixTools.rotateLeft(dup.get(), d.get(), offset);
		dup.touch();
	}

}
