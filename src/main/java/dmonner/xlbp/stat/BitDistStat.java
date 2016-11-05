package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.Map;

import dmonner.xlbp.util.CSVWriter;

public class BitDistStat extends AbstractStat
{
	public static float OFF = 0F;
	public static float ON = 1F;
	public static float MID = 0.5F;
	public static boolean WTA = false;

	private final float on;
	private final float mid;
	private final boolean wta;

	private final String name;
	private final int n;
	private final int[] output;
	private final int[] target;
	private final float[] fracOutput;
	private final float[] fracTarget;
	private int totOutput;
	private int totTarget;

	public BitDistStat(final BitDistStat that)
	{
		this.name = that.name;
		this.on = that.on;
		this.mid = that.mid;
		this.wta = that.wta;
		this.n = that.n;
		this.output = that.output.clone();
		this.target = that.target.clone();
		this.fracOutput = that.fracOutput.clone();
		this.fracTarget = that.fracTarget.clone();
		this.totOutput = that.totOutput;
		this.totTarget = that.totTarget;
	}

	public BitDistStat(final int n)
	{
		this("Dist", n);
	}

	public BitDistStat(final String name, final int n)
	{
		this.name = name;
		this.on = ON;
		this.mid = MID;
		this.wta = WTA;
		this.n = n;
		this.output = new int[n];
		this.target = new int[n];
		this.fracOutput = new float[n];
		this.fracTarget = new float[n];
	}

	public void add(final BitDistStat that)
	{
		if(n != that.n)
			throw new IllegalArgumentException("Can only add in BitDistStats with same n.");

		for(int i = 0; i < n; i++)
		{
			output[i] += that.output[i];
			target[i] += that.target[i];
		}
	}

	@Override
	public void add(final Stat that)
	{
		if(that instanceof BitDistStat)
			add((BitDistStat) that);
		else
			throw new IllegalArgumentException("Can only add in other BitDistStats.");
	}

	@Override
	public void addTo(final String prefix, final Map<String, Object> map)
	{
		for(int i = 0; i < n; i++)
			map.put(prefix + name + "Output" + i, output[i]);

		for(int i = 0; i < n; i++)
			map.put(prefix + name + "Target" + i, target[i]);

		for(int i = 0; i < n; i++)
			map.put(prefix + name + "FracOutput" + i, fracOutput[i]);

		for(int i = 0; i < n; i++)
			map.put(prefix + name + "FracTarget" + i, fracTarget[i]);
	}

	@Override
	public void analyze()
	{
		totOutput = 0;
		totTarget = 0;

		for(int i = 0; i < n; i++)
		{
			totOutput += output[i];
			totTarget += target[i];
		}

		for(int i = 0; i < n; i++)
		{
			fracOutput[i] = ((float) output[i]) / totOutput;
			fracTarget[i] = ((float) target[i]) / totTarget;
		}
	}

	private void append(final StringBuilder sb, final String field, final float val)
	{
		sb.append(name);
		sb.append(field);
		sb.append(" = ");
		sb.append(val);
		sb.append("\n");
	}

	private void append(final StringBuilder sb, final String field, final int val)
	{
		sb.append(name);
		sb.append(field);
		sb.append(" = ");
		sb.append(val);
		sb.append("\n");
	}

	@Override
	public void clear()
	{
		totOutput = 0;
		totTarget = 0;

		for(int i = 0; i < n; i++)
		{
			output[i] = 0;
			target[i] = 0;
		}

		for(int i = 0; i < n; i++)
		{
			fracOutput[i] = 0F;
			fracTarget[i] = 0F;
		}

	}

	public void compare(final float[] target, final float[] output)
	{
		if(WTA)
			compareWTA(target, output);
		else
			compareExact(target, output);
	}

	public void compareExact(final float[] target, final float[] output)
	{
		for(int i = 0; i < target.length; i++)
		{
			if(Float.isInfinite(target[i]) || Float.isNaN(target[i]))
				throw new IllegalArgumentException("Infinite/NaN Target!");

			if(Float.isInfinite(output[i]) || Float.isNaN(output[i]))
				throw new IllegalArgumentException("Infinite/NaN Output!");

			if(target[i] >= MID)
				this.target[i]++;

			if(output[i] >= MID)
				this.output[i]++;
		}
	}

	public void compareWTA(final float[] target, final float[] output)
	{
		int targetIdx = -1;
		int outputIdx = -1;
		float outputMax = Float.NEGATIVE_INFINITY;

		for(int i = 0; i < target.length; i++)
		{
			if(Float.isInfinite(target[i]) || Float.isNaN(target[i]))
				throw new IllegalArgumentException("Infinite/NaN Target!");

			if(Float.isInfinite(output[i]) || Float.isNaN(output[i]))
				throw new IllegalArgumentException("Infinite/NaN Output!");

			// Find the target bit that's ON
			if(target[i] == ON)
				if(targetIdx >= 0)
					throw new IllegalArgumentException("Multiple target bits set! Not suitable for WTA.");
				else
					targetIdx = i;

			// Find the index of the maximum output
			if(output[i] > outputMax)
			{
				outputIdx = i;
				outputMax = output[i];
			}
		}

		this.target[targetIdx]++;
		this.output[outputIdx]++;
	}

	@Override
	public void saveData(final CSVWriter out) throws IOException
	{
		for(int i = 0; i < n; i++)
			out.appendField(output[i]);

		for(int i = 0; i < n; i++)
			out.appendField(target[i]);

		for(int i = 0; i < n; i++)
			out.appendField(fracOutput[i]);

		for(int i = 0; i < n; i++)
			out.appendField(fracTarget[i]);
	}

	@Override
	public void saveHeader(final String prefix, final CSVWriter out) throws IOException
	{
		for(int i = 0; i < n; i++)
			out.appendHeader(prefix + name + "Output" + i);

		for(int i = 0; i < n; i++)
			out.appendHeader(prefix + name + "Target" + i);

		for(int i = 0; i < n; i++)
			out.appendHeader(prefix + name + "FracOutput" + i);

		for(int i = 0; i < n; i++)
			out.appendHeader(prefix + name + "FracTarget" + i);
	}

	@Override
	public String toString(final String prefix)
	{
		final StringBuilder sb = new StringBuilder();

		for(int i = 0; i < n; i++)
			append(sb, prefix + name + "Output" + i, output[i]);

		for(int i = 0; i < n; i++)
			append(sb, prefix + name + "Target" + i, target[i]);

		for(int i = 0; i < n; i++)
			append(sb, prefix + name + "FracOutput" + i, fracOutput[i]);

		for(int i = 0; i < n; i++)
			append(sb, prefix + name + "FracTarget" + i, fracTarget[i]);

		return sb.toString();
	}
}
