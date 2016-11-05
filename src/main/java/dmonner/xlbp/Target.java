package dmonner.xlbp;

import java.text.DecimalFormat;

import dmonner.xlbp.layer.TargetLayer;
import dmonner.xlbp.util.MatrixTools;

public class Target
{
	private static final DecimalFormat df = new DecimalFormat("0.000");
	private final TargetLayer layer;
	private final float[] value;
	private float weight;

	public Target(final TargetLayer layer, final float[] value)
	{
		this(layer, value, 1F);
	}

	public Target(final TargetLayer layer, final float[] value, final float weight)
	{
		this.layer = layer;
		this.value = value;
		this.weight = weight;

		if(value.length != layer.size())
			throw new IllegalArgumentException("Incorrect Target Size; expected " + layer.size()
					+ " for " + layer.getName() + ", got " + value.length);
	}

	public void apply()
	{
		layer.setTarget(value, weight);
	}

	public TargetLayer getLayer()
	{
		return layer;
	}

	public float[] getValue()
	{
		return value;
	}

	public float getWeight()
	{
		return weight;
	}

	public void setWeight(final float weight)
	{
		this.weight = weight;
	}

	@Override
	public String toString()
	{
		return layer.getName() + "(" + df.format(weight) + "): " + MatrixTools.toString(value);
	}
}
