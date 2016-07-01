package dmonner.xlbp.layer;

import java.util.Stack;

import dmonner.xlbp.Component;
import dmonner.xlbp.DownstreamComponent;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.UpstreamComponent;

public class CopySourceLayer extends AbstractInternalLayer
{
	private static final long serialVersionUID = 1L;

	private CopyDestinationLayer destination;

	public CopySourceLayer(final CopySourceLayer that, final NetworkCopier copier)
	{
		super(that, copier);
		this.destination = copier.getCopyOf(that.destination);
	}

	public CopySourceLayer(final String name, final int size)
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
	public void build()
	{
		if(!built)
		{
			super.build();

			if(destination == null)
				throw new IllegalStateException("CopySourceLayer " + name + " has no destination!");

			upstream.build();
			y = upstream.getActivations();
			d = new Responsibilities(size);

			// checkForUpstreamFanOutLayers();
			checkSourceDownstream();
			notifyUpstreamLayers();

			built = true;
		}
	}

	private void checkSourceDownstream()
	{
		// Check that destination comes back to source
		UpstreamComponent current = destination;
		boolean foundSource = false;

		while(current.getDownstream() instanceof InternalLayer)
		{
			current = (UpstreamComponent) current.getDownstream();

			if(current == this)
			{
				foundSource = true;
				break;
			}
		}

		if(!foundSource)
			throw new IllegalStateException("The CopyDestinationLayer " + destination.name
					+ " was not found upstream of this new CopySourceLayer" + name + ".");
	}

	@Override
	public void clear()
	{
		// Nothing to do -- activations are aliased from upstream layer.
		// Nothing to do for deltas -- they will get scrubbed by the upstream layer.
	}

	@Override
	public CopySourceLayer copy(final NetworkCopier copier)
	{
		return new CopySourceLayer(this, copier);
	}

	@Override
	public CopySourceLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	@Override
	public void copyConnectivityFrom(final Component comp, final NetworkCopier copier)
	{
		super.copyConnectivityFrom(comp, copier);

		if(comp instanceof CopySourceLayer)
			this.destination = copier.getCopyIfExists(((CopySourceLayer) comp).destination);
	}

	public CopyDestinationLayer getCopyDestination()
	{
		return destination;
	}

	public Responsibilities getPreviousResponsibilities()
	{
		return destination.getResponsibilities();
	}

	private void notifyUpstreamLayers()
	{
		// Set up pointers to this layer throughout the rest of the network
		final Stack<UpstreamComponent> up = new Stack<UpstreamComponent>();
		up.push(upstream);

		while(!up.empty())
		{
			final UpstreamComponent current = up.pop();

			// This will error out if there is already a CopySourceLayer downstream from current
			current.asUpstreamLayer().addDownstreamCopyLayer(this);

			if(current instanceof DownstreamLayer)
			{
				final DownstreamComponent cDownstream = (DownstreamComponent) current;
				for(int i = 0; i < cDownstream.nUpstream(); i++)
					up.push(cDownstream.getUpstream(i));
			}
		}
	}

	public void setCopyDestination(final CopyDestinationLayer destination)
	{
		this.destination = destination;
	}

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		super.toString(sb);
		sb.pushIndent();

		if(sb.showExtra())
			sb.appendln("CopyDestination: " + destination);

		sb.popIndent();
	}

	@Override
	public void updateEligibilities()
	{
		// Nothing to do.
	}

	@Override
	public void updateResponsibilities()
	{
		downstream.updateUpstreamResponsibilities(myIndexInDownstream);
	}

	@Override
	public void updateUpstreamResponsibilities(final int index)
	{
		// set upstream d to 1s; leaving the "delta" side, starting the "eligibility" side
		upstream.getResponsibilities(myIndexInUpstream).setOnes();
	}
}
