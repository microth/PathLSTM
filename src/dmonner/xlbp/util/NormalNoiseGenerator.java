package dmonner.xlbp.util;

import java.util.Random;

public class NormalNoiseGenerator implements NoiseGenerator
{
	private final Random random;
	private final float mean;
	private final float sd;
	private float cached;

	public NormalNoiseGenerator(final Random random, final float mean, final float sd)
	{
		this.random = random;
		this.mean = mean;
		this.sd = sd;
	}

	@Override
	public float next()
	{
		// Return the cached variable if there is one.
		if(!Float.isNaN(cached))
		{
			final float rv = mean + cached * sd;
			cached = Float.NaN;
			return rv;
		}

		// Otherwise, generate two new normal variates.
		float v1, v2, s;
		do
		{
			v1 = 2 * random.nextFloat() - 1;
			v2 = 2 * random.nextFloat() - 1;
			s = v1 * v1 + v2 * v2;
		}
		while(s >= 1);

		// This method generates two normal random variates. Saved one for next
		// time, and return the other.
		final float factor = (float) Math.sqrt(-2 * Math.log(s) / s);
		cached = factor * v1;
		return mean + factor * v2 * sd;
	}
}
