package dmonner.xlbp.layer;

import dmonner.xlbp.Component;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;

public class CopyDestinationLayer extends AbstractUpstreamLayer
{
	private static final long serialVersionUID = 1L;

	private CopySourceLayer source;

	public CopyDestinationLayer(final CopyDestinationLayer that, final NetworkCopier copier)
	{
		super(that, copier);
	}

	public CopyDestinationLayer(final String name, final CopySourceLayer source)
	{
		super(name, source.size());
		this.source = source;
		this.source.setCopyDestination(this);
	}

	@Override
	public void activateTest()
	{
		System.arraycopy(source.y, 0, y, 0, size);
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
			if(source == null)
				throw new IllegalStateException("CopyDestinationLayer " + name + " has no source!");

			super.build();

			y = new float[size];
			d = new Responsibilities(size);

			built = true;
		}
	}

	@Override
	public CopyDestinationLayer copy(final NetworkCopier copier)
	{
		return new CopyDestinationLayer(this, copier);
	}

	@Override
	public CopyDestinationLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		super.copyConnectivityFrom(comp, copier);

		if(comp instanceof CopyDestinationLayer)
			this.source = copier.getCopyIfExists(((CopyDestinationLayer) comp).source);
	}

	public CopySourceLayer getCopySource()
	{
		return source;
	}

	public void setCopySource(final CopySourceLayer source)
	{
		this.source = source;
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		super.toString(sb);
		sb.pushIndent();

		if(sb.showExtra())
			sb.appendln("CopySource: " + source);

		sb.popIndent();
	}

	@Override
	public void updateEligibilities()
	{
		// No "if" because there is always an upstream copy source
		downstream.updateUpstreamResponsibilities(myIndexInDownstream);
	}

	@Override
	public void updateResponsibilities()
	{
		// Nothing to do; CopySource is always downstream, so this is done in activateTrain()
	}

}
