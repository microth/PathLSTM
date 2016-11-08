package dmonner.xlbp;

import java.util.Map;

import dmonner.xlbp.layer.InputLayer;

public class Input
{
	private final InputLayer layer;
	//private final float[] value;
	//private final int[] binValue;
	
	private final Map<Integer, Float> inputs;

	public Input(final InputLayer layer, Map<Integer, Float> values)  //final float[] value, final int[] binValue)
	{
		this.layer = layer;
		//this.value = value;
		//this.binValue = binValue;
		inputs = values;

		//if(value!=null) {
		//	if(value.length != layer.size())
		//		throw new IllegalArgumentException("Incorrect Input Size; expected " + layer.size() + " for "
		//				+ layer.getName() + ", got " + value.length);
		//} else {
		for(int i : values.keySet()) {
			if(i>layer.size())
				throw new IllegalArgumentException("Incorrect Input Size; expected " + layer.size() + " for "
						+ layer.getName() + ", got " + i);
		}
		//}
	}

	public void apply()
	{
		layer.setInput(inputs);
	}

	@Override
	public boolean equals(final Object other)
	{
		if(other instanceof Input)
		{
			final Input that = (Input) other;
			return that.layer == this.layer;
		}

		return false;
	}

	public InputLayer getLayer()
	{
		return layer;
	}

	/*public float[] getValue()
	{
		return value;
	}
	
	public int[] getBinValue()
	{
		return binValue;
	}*/
	
	public Map<Integer, Float> getValue() {
		return inputs;
	}

	@Override
	public int hashCode()
	{
		return layer.hashCode();
	}

	@Override
	public String toString()
	{
		return layer.getName() + ": " + inputs.toString(); // MatrixTools.toString(value);
	}
}
