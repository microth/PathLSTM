package dmonner.xlbp;

import java.io.Serializable;
import java.util.Arrays;

import dmonner.xlbp.util.MatrixTools;

public class Responsibilities implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final float[] d;
	private final int size;
	private boolean empty;

	public Responsibilities(final int size)
	{
		this.d = new float[size];
		this.size = size;
		this.empty = true;
	}

	public void add(final Responsibilities that)
	{
		if(!that.empty())
		{
			if(empty)
				Arrays.fill(d, 0F);
			//System.err.println(Arrays.toString(d));

			final float[] addend = that.d;

			for(int j = 0; j < size; j++)
				d[j] += addend[j];

			empty = false;
		}
		//System.err.println(Arrays.toString(d));
	}

	public void clear()
	{
		empty = true;
	}

	public Responsibilities copy()
	{
		final Responsibilities resp = new Responsibilities(size);
		resp.copy(this);
		return resp;
	}

	public void copy(final Responsibilities that)
	{
		if(that.empty())
		{
			empty = true;
		}
		else
		{
			System.arraycopy(that.d, 0, d, 0, size);
			empty = false;
		}
	}

	public void copyAdd(final Responsibilities that, final float[] add)
	{
		if(that.empty())
		{
			empty = true;
		}
		else
		{
			final float[] base = that.d;

			for(int j = 0; j < size; j++)
				d[j] = base[j] + add[j];

			empty = false;
		}
	}

	public void copyMul(final Responsibilities that, final float[] factor)
	{
		if(that.empty())
		{
			empty = true;
		}
		else
		{
			final float[] base = that.d;
			//System.err.println(Arrays.toString(d));

			for(int j = 0; j < size; j++)
				d[j] = base[j] * factor[j];

			empty = false;
		}

	}

	public void copyPlusScaledDiff(final Responsibilities that, final float[] add, final float[] sub,
			final float amount)
	{
		if(that.empty())
		{
			this.empty = true;
		}
		else
		{
			final float[] base = that.d;

			for(int j = 0; j < size; j++)
				d[j] = base[j] + (add[j] - sub[j]) * amount;

			empty = false;
		}

	}

	public boolean empty()
	{
		return empty;
	}

	public float[] get()
	{
		if(empty)
			Arrays.fill(d, 0F);

		return d;
	}

	public void scale(final float[] factor)
	{
		if(!empty)
		{
			if(empty)
				Arrays.fill(d, 0F);

			for(int j = 0; j < size; j++)
				d[j] *= factor[j];

			empty = false;
		}

	}

	public void set(final float[] resp)
	{
		System.arraycopy(resp, 0, d, 0, size);
		empty = false;
	}

	public void setOnes()
	{
		Arrays.fill(d, 1F);
		empty = false;
	}

	public int size()
	{
		return size;
	}

	public void target(final float[] targets, final float[] output)
	{
		for(int j = 0; j < size; j++)
			d[j] = targets[j] - output[j];

		empty = false;
	}

	public void target(final float[] targets, final float[] output, final float weight)
	{
		for(int j = 0; j < size; j++)
			d[j] = (targets[j] - output[j]) * weight;
		
		empty = false;
	}

	@Override
	public String toString()
	{
		if(empty)
			return "Empty";
		else
			return MatrixTools.toString(d);
	}

	public void touch()
	{
		empty = false;
	}
}
