package dmonner.xlbp;

import java.io.Serializable;
import java.util.Map;

import dmonner.xlbp.connection.Connection;

public class WeightUpdaterType implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static WeightUpdaterType basic()
	{
		final String name = "dmonner.xlbp.BasicWeightUpdater";
		final Class<?>[] classes = new Class<?>[] { Connection.class };
		final Object[] params = new Object[] { null };

		return new WeightUpdaterType(name, classes, params);
	}

	public static WeightUpdaterType basic(final float a)
	{
		final String name = "dmonner.xlbp.BasicWeightUpdater";
		final Class<?>[] classes = new Class<?>[] { Connection.class, float.class };
		final Object[] params = new Object[] { null, a };

		return new WeightUpdaterType(name, classes, params);
	}

	public static WeightUpdaterType batch()
	{
		final String name = "dmonner.xlbp.BatchWeightUpdater";
		final Class<?>[] classes = new Class<?>[] { Connection.class };
		final Object[] params = new Object[] { null };

		return new WeightUpdaterType(name, classes, params);
	}

	public static WeightUpdaterType batch(final float a)
	{
		final String name = "dmonner.xlbp.BatchWeightUpdater";
		final Class<?>[] classes = new Class<?>[] { Connection.class, float.class };
		final Object[] params = new Object[] { null, a };

		return new WeightUpdaterType(name, classes, params);
	}
	
	public static WeightUpdaterType nadam(final float a)
	{
		final String name = "dmonner.xlbp.NadamBatchWeightUpdater";
		final Class<?>[] classes = new Class<?>[] { Connection.class, float.class };
		final Object[] params = new Object[] { null, a };

		return new WeightUpdaterType(name, classes, params);
	}

	public static WeightUpdaterType adagradbatch(final float a) {
		final String name = "dmonner.xlbp.AdagradBatchWeightUpdater";
		final Class<?>[] classes = new Class<?>[] { Connection.class, float.class };
		final Object[] params = new Object[] { null, a };

		return new WeightUpdaterType(name, classes, params);	
	}
	

	public static WeightUpdaterType adadelta(final float beta, final float e, int batchsize) {
		if(batchsize==1) {
			final String name = "dmonner.xlbp.AdadeltaBasicWeightUpdater";
			final Class<?>[] classes = new Class<?>[] { Connection.class, float.class, float.class };
			final Object[] params = new Object[] { null, beta , e};
			return new WeightUpdaterType(name, classes, params);
		} else {
			final String name = "dmonner.xlbp.AdadeltaBatchWeightUpdater";
			final Class<?>[] classes = new Class<?>[] { Connection.class, float.class, float.class, int.class };
			final Object[] params = new Object[] { null, beta , e, batchsize};
			return new WeightUpdaterType(name, classes, params);
		}
	}
	
	public static WeightUpdaterType adam(float f, float g, float h, float d, int i) {
		final String name = "dmonner.xlbp.AdamBasicWeightUpdater";
		final Class<?>[] classes = new Class<?>[] { Connection.class, float.class, float.class, float.class, float.class };
		final Object[] params = new Object[] { null, f, g, h, d};
		return new WeightUpdaterType(name, classes, params);
	}

	
	public static WeightUpdaterType decay(final float a, final float b)
	{
		final String name = "dmonner.xlbp.DecayWeightUpdater";
		final Class<?>[] classes = new Class<?>[] { Connection.class, float.class, float.class };
		final Object[] params = new Object[] { null, a, b };

		return new WeightUpdaterType(name, classes, params);
	}

	private static float f(final Map<String, Object> p, final String key)
	{
		return Float.parseFloat(p.get(key).toString());
	}

	public static WeightUpdaterType fromString(final String type, final Map<String, Object> p)
	{
		if(type.equalsIgnoreCase("momentum"))
			return WeightUpdaterType.momentum(f(p, "learningRate"), f(p, "momentum"));
		else if(type.equalsIgnoreCase("resilient"))
			return WeightUpdaterType.resilient();
		else if(type.equalsIgnoreCase("relig"))
			return WeightUpdaterType.relig();
		else if(type.equalsIgnoreCase("basic"))
			return WeightUpdaterType.basic(f(p, "learningRate"));
		else if(type.equalsIgnoreCase("batch"))
			return WeightUpdaterType.batch(f(p, "learningRate"));
		else if(type.equalsIgnoreCase("decay"))
			return WeightUpdaterType.decay(f(p, "learningRate"), f(p, "decayRate"));
		else
			throw new IllegalArgumentException("Unhandled WeightUpdaterType: " + type);
	}

	public static WeightUpdaterType momentum()
	{
		final String name = "dmonner.xlbp.MomentumWeightUpdater";
		final Class<?>[] classes = new Class<?>[] { Connection.class };
		final Object[] params = new Object[] { null };

		return new WeightUpdaterType(name, classes, params);
	}

	public static WeightUpdaterType momentum(final float a, final float m)
	{
		final String name = "dmonner.xlbp.MomentumWeightUpdater";
		final Class<?>[] classes = new Class<?>[] { Connection.class, float.class, float.class };
		final Object[] params = new Object[] { null, a, m };

		return new WeightUpdaterType(name, classes, params);
	}

	public static WeightUpdaterType relig()
	{
		final String name = "dmonner.xlbp.ResilientEligibilitiesWeightUpdater";
		final Class<?>[] classes = new Class<?>[] { Connection.class };
		final Object[] params = new Object[] { null };

		return new WeightUpdaterType(name, classes, params);
	}

	public static WeightUpdaterType resilient()
	{
		final String name = "dmonner.xlbp.ResilientWeightUpdater";
		final Class<?>[] classes = new Class<?>[] { Connection.class };
		final Object[] params = new Object[] { null };

		return new WeightUpdaterType(name, classes, params);
	}

	private final String name;
	private final Class<?>[] classes;
	private final Object[] params;

	private WeightUpdaterType(final String name, final Class<?>[] classes, final Object[] params)
	{
		this.name = name;
		this.classes = classes;
		this.params = params;
	}

	public WeightUpdater make(final Connection conn)
	{
		params[0] = conn;

		try
		{
			return (WeightUpdater) Class.forName(name).getConstructor(classes).newInstance(params);
		}
		catch(final Exception ex)
		{
			ex.printStackTrace();
		}

		return null;
	}

}
