package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.Map;

import dmonner.xlbp.util.CSVWriter;

public class ErrorStat extends AbstractStat
{
	private final String name;
	private float sse;
	private float mse;
	private float rmse;
	private int n;

	public ErrorStat()
	{
		this("");
	}

	public ErrorStat(final ErrorStat that)
	{
		this.name = that.name;
		this.sse = that.sse;
		this.n = that.n;
		this.mse = that.mse;
		this.rmse = that.rmse;
	}

	public ErrorStat(final String name)
	{
		this.name = name;
	}

	public void add(final ErrorStat that)
	{
		this.sse += that.sse;
		this.n += that.n;
	}

	@Override
	public void add(final Stat that)
	{
		if(that instanceof ErrorStat)
			add((ErrorStat) that);
		else
			throw new IllegalArgumentException("Can only add in other ErrorStats.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dmonner.xlbp.util.Stat#addTo(java.util.Map)
	 */
	@Override
	public void addTo(final String prefix, final Map<String, Object> map)
	{
		map.put(prefix + name + "N", n);
		map.put(prefix + name + "SSE", sse);
		map.put(prefix + name + "MSE", mse);
		map.put(prefix + name + "RMSE", rmse);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dmonner.xlbp.util.Stat#analyze()
	 */
	@Override
	public void analyze()
	{
		mse = sse / n;
		rmse = (float) Math.sqrt(mse);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dmonner.xlbp.util.Stat#clear()
	 */
	@Override
	public void clear()
	{
		sse = 0F;
		mse = 0F;
		rmse = 0F;
		n = 0;
	}

	public void compare(final float[] target, final float[] output)
	{
		for(int i = 0; i < target.length; i++)
		{
			final float diff = target[i] - output[i];
			sse += diff * diff;
		}

		n += target.length;
	}

	public float getMSE()
	{
		return mse;
	}

	public int getN()
	{
		return n;
	}

	public float getRMSE()
	{
		return rmse;
	}

	public float getSSE()
	{
		return sse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dmonner.xlbp.util.Stat#saveData(dmonner.xlbp.util.CSVWriter)
	 */
	@Override
	public void saveData(final CSVWriter out) throws IOException
	{
		out.appendField(n);
		out.appendField(sse);
		out.appendField(mse);
		out.appendField(rmse);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dmonner.xlbp.util.Stat#saveHeader(dmonner.xlbp.util.CSVWriter)
	 */
	@Override
	public void saveHeader(final String prefix, final CSVWriter out) throws IOException
	{
		out.appendHeader(prefix + name + "N");
		out.appendHeader(prefix + name + "SSE");
		out.appendHeader(prefix + name + "MSE");
		out.appendHeader(prefix + name + "RMSE");
	}

	@Override
	public String toString(final String prefix)
	{
		final StringBuilder sb = new StringBuilder();

		final String prename = prefix + name;

		sb.append(prename);
		sb.append("N = ");
		sb.append(n);
		sb.append("\n");

		sb.append(prename);
		sb.append("SSE = ");
		sb.append(sse);
		sb.append("\n");

		sb.append(prename);
		sb.append("MSE = ");
		sb.append(mse);
		sb.append("\n");

		sb.append(prename);
		sb.append("RMSE = ");
		sb.append(rmse);
		sb.append("\n");

		return sb.toString();
	}

}
