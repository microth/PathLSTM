package dmonner.xlbp;

public class ForceAdjacencyListWeightInitializer extends UniformWeightInitializer
{
	public static final long serialVersionUID = 1L;

	public ForceAdjacencyListWeightInitializer()
	{
		super();
	}

	public ForceAdjacencyListWeightInitializer(final float p)
	{
		super(p);
	}

	public ForceAdjacencyListWeightInitializer(final float p, final float min, final float max)
	{
		super(p, min, max);
	}

	@Override
	public boolean fullConnectivity()
	{
		return false;
	}
}
