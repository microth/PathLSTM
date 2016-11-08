package dmonner.xlbp;

import dmonner.xlbp.compound.FunctionCompound;
import dmonner.xlbp.compound.LinearCompound;
import dmonner.xlbp.compound.LinearTargetCompound;
import dmonner.xlbp.compound.LogisticCompound;
import dmonner.xlbp.compound.SimpleCompound;
import dmonner.xlbp.compound.SumOfSquaresTargetCompound;
import dmonner.xlbp.compound.TanhCompound;
import dmonner.xlbp.compound.TanhTargetCompound;
import dmonner.xlbp.compound.TargetCompound;
import dmonner.xlbp.compound.WeightedCompound;
import dmonner.xlbp.compound.XEntropyTargetCompound;
import dmonner.xlbp.layer.FunctionLayer;
import dmonner.xlbp.layer.LinearLayer;
import dmonner.xlbp.layer.LogisticLayer;
import dmonner.xlbp.layer.TanhLayer;

public enum Function
{
	LOGISTIC, LINEAR, TANH, NONE;

	public static WeightedCompound compound(final String fcn, final String name, final int size)
	{
		if(fcn.endsWith("-ub"))
			return compound(fcn.substring(0, fcn.length() - 3), name, size, false);
		else
			return compound(fcn, name, size, true);
	}

	public static WeightedCompound compound(final String fcn, final String name, final int size,
			final boolean biases)
	{
		final String ufcn = fcn.toUpperCase();

		if(NONE.toString().startsWith(ufcn))
			return new SimpleCompound(name, size);
		else if(LOGISTIC.toString().startsWith(ufcn))
			return new LogisticCompound(name, size, biases);
		else if(LINEAR.toString().startsWith(ufcn))
			return new LinearCompound(name, size, biases);
		else if(TANH.toString().startsWith(ufcn))
			return new TanhCompound(name, size, biases);
		else
			throw new IllegalArgumentException("Unrecognized function type: " + fcn);
	}

	public static FunctionCompound fcompound(final String fcn, final String name, final int size)
	{
		if(fcn.endsWith("-ub"))
			return fcompound(fcn.substring(0, fcn.length() - 3), name, size, false);
		else
			return fcompound(fcn, name, size, true);
	}

	public static FunctionCompound fcompound(final String fcn, final String name, final int size,
			final boolean biases)
	{
		final String ufcn = fcn.toUpperCase();

		if(NONE.toString().startsWith(ufcn))
			return null;
		else if(LOGISTIC.toString().startsWith(ufcn))
			return new LogisticCompound(name, size, biases);
		else if(LINEAR.toString().startsWith(ufcn))
			return new LinearCompound(name, size, biases);
		else if(TANH.toString().startsWith(ufcn))
			return new TanhCompound(name, size, biases);
		else
			throw new IllegalArgumentException("Unrecognized function type: " + fcn);
	}

	public static FunctionLayer layer(final String fcn, final String name, final int size)
	{
		final String ufcn = fcn.toUpperCase();

		if(NONE.toString().startsWith(ufcn))
			return null;
		else if(LOGISTIC.toString().startsWith(ufcn))
			return new LogisticLayer(name, size);
		else if(LINEAR.toString().startsWith(ufcn))
			return new LinearLayer(name, size);
		else if(TANH.toString().startsWith(ufcn))
			return new TanhLayer(name, size);
		else
			throw new IllegalArgumentException("Unrecognized function type: " + fcn);
	}

	public static float max(final String fcn)
	{
		final String ufcn = fcn.toUpperCase();

		if(NONE.toString().startsWith(ufcn))
			return 0F;
		else if(LOGISTIC.toString().startsWith(ufcn))
			return 1F;
		else if(LINEAR.toString().startsWith(ufcn))
			return Float.POSITIVE_INFINITY;
		else if(TANH.toString().startsWith(ufcn))
			return 1F;
		else
			throw new IllegalArgumentException("Unrecognized function type: " + fcn);
	}

	public static float mid(final String fcn)
	{
		final String ufcn = fcn.toUpperCase();

		if(NONE.toString().startsWith(ufcn))
			return 0F;
		else if(LOGISTIC.toString().startsWith(ufcn))
			return 0.5F;
		else if(LINEAR.toString().startsWith(ufcn))
			return 0F;
		else if(TANH.toString().startsWith(ufcn))
			return 0F;
		else
			throw new IllegalArgumentException("Unrecognized function type: " + fcn);
	}

	public static float min(final String fcn)
	{
		final String ufcn = fcn.toUpperCase();

		if(NONE.toString().startsWith(ufcn))
			return 0F;
		else if(LOGISTIC.toString().startsWith(ufcn))
			return 0F;
		else if(LINEAR.toString().startsWith(ufcn))
			return Float.NEGATIVE_INFINITY;
		else if(TANH.toString().startsWith(ufcn))
			return -1F;
		else
			throw new IllegalArgumentException("Unrecognized function type: " + fcn);
	}

	public static TargetCompound target(final String fcn, final String name, final int size,
			final boolean xe)
	{
		final String ufcn = fcn.toUpperCase();

		if(NONE.toString().startsWith(ufcn))
			return null;
		else if(LOGISTIC.toString().startsWith(ufcn))
			if(xe)
				return new XEntropyTargetCompound(name, size);
			else
				return new SumOfSquaresTargetCompound(name, size);
		else if(LINEAR.toString().startsWith(ufcn))
			return new LinearTargetCompound(name, size);
		else if(TANH.toString().startsWith(ufcn))
			return new TanhTargetCompound(name, size);
		else
			throw new IllegalArgumentException("Unrecognized function type: " + fcn);
	}
}
