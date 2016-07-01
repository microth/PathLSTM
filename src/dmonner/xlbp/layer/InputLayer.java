package dmonner.xlbp.layer;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import dmonner.xlbp.InputComponent;
import dmonner.xlbp.NetworkCopier;
import dmonner.xlbp.NetworkStringBuilder;
import dmonner.xlbp.Responsibilities;
import dmonner.xlbp.util.MatrixTools;

public class InputLayer extends AbstractUpstreamLayer implements InputComponent
{
	private static final long serialVersionUID = 1L;

	private boolean zeroed;

	public InputLayer(final InputLayer that, final NetworkCopier copier)
	{
		super(that, copier);
		this.zeroed = copier.copyState() ? that.zeroed : true;
	}

	public InputLayer(final String name, final int size)
	{
		super(name, size);
		zeroed = true;
	}

	@Override
	public void activateTest()
	{
		// Nothing to do;
	}

	@Override
	public void activateTrain()
	{
		// Nothing to do;
	}

	@Override
	public void build()
	{
		if(!built)
		{
			super.build();

			y = new float[size];
			d = new Responsibilities(size);

			built = true;
		}
	}

	@Override
	public void clearActivations()
	{
		if(!zeroed)
		{
			Arrays.fill(y, 0F);
			zeroed = true;
		}
	}

	@Override
	public InputLayer copy(final NetworkCopier copier)
	{
		return new InputLayer(this, copier);
	}

	@Override
	public InputLayer copy(final String nameSuffix)
	{
		return copy(new NetworkCopier(nameSuffix));
	}

	public void setFilled()
	{
		zeroed = false;
	}

	//@Override
	//public void setInput(final float[] activations)
	//{
	//	System.arraycopy(activations, 0, y, 0, size);
	//	zeroed = false;
	//}	
	

	@Override
	public void toString(final NetworkStringBuilder sb)
	{
		if(sb.showName())
		{
			sb.indent();
			sb.append(name);
			sb.append(" (");
			sb.append(String.valueOf(size()));
			sb.append(")");
			sb.append(" : ");
			sb.append(this.getClass().getSimpleName());
			sb.appendln();
		}

		sb.pushIndent();

		if(sb.showActivations())
		{
			sb.appendln("Inputs:");
			sb.pushIndent();
			sb.appendln(MatrixTools.toString(y));
			sb.popIndent();
		}

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
		// Nothing to do.
	}

	public void setInput(Map<Integer, Float> inputs) {
		for(int i : inputs.keySet())
			y[i] = inputs.get(i);
		zeroed = false;
	}
		
	public void setInput(int... inputs) {
		for(int i : inputs)
			y[i] = 1;
		zeroed = false;
	}
}
