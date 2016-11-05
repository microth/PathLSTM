package dmonner.xlbp.trial;

import dmonner.xlbp.layer.Layer;

public class LayerCheck
{
	private final String name;
	private final Layer layer;
	private final float[] eval;

	public LayerCheck(final Layer layer, final float[] eval)
	{
		this(layer.getName(), layer, eval);
	}

	public LayerCheck(final String name, final Layer layer, final float[] eval)
	{
		this.name = name;
		this.layer = layer;
		this.eval = eval;
	}

	public float[] getEval()
	{
		return eval;
	}

	public Layer getLayer()
	{
		return layer;
	}

	public String getName()
	{
		return name;
	}
}
