package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.Map;

import dmonner.xlbp.util.CSVWriter;

public class BitStat extends AbstractStat
{
	public static float OFF = 0F;
	public static float ON = 1F;
	public static float MID = 0.5F;
	public static boolean WTA = false;

	private final float on;
	private final float mid;
	private final boolean wta;

	private final String name;
	private int tp;
	private int fn;
	private int fp;
	private int tn;
	private int tot;
	private int posact;
	private int posans;
	private int negact;
	private int negans;
	private int corr;
	private int incorr;
	private float acc;
	private float prec;
	private float rec;
	private float spec;
	private float f1;

	public BitStat()
	{
		this("");
	}

	public BitStat(final BitStat that)
	{
		this.name = that.name;
		this.on = that.on;
		this.mid = that.mid;
		this.wta = that.wta;
		this.tp = that.tp;
		this.fn = that.fn;
		this.fp = that.fp;
		this.tn = that.tn;
		this.tot = that.tot;
		this.posact = that.posact;
		this.posans = that.posans;
		this.negact = that.negact;
		this.negans = that.negans;
		this.corr = that.corr;
		this.incorr = that.incorr;
		this.acc = that.acc;
		this.prec = that.prec;
		this.rec = that.rec;
		this.spec = that.spec;
		this.f1 = that.f1;
	}

	public BitStat(final String name)
	{
		this.name = name;
		this.on = ON;
		this.mid = MID;
		this.wta = WTA;
	}

	public void add(final BitStat that)
	{
		this.tp += that.tp;
		this.fn += that.fn;
		this.fp += that.fp;
		this.tn += that.tn;
	}

	public void add(final int ht, final int ms, final int fp, final int cr)
	{
		this.tp += ht;
		this.fn += ms;
		this.fp += fp;
		this.tn += cr;
	}

	@Override
	public void add(final Stat that)
	{
		if(that instanceof BitStat)
			add((BitStat) that);
		else
			throw new IllegalArgumentException("Can only add in other BitStats.");
	}

	public void addFalseNegative()
	{
		fn++;
	}

	public void addFalsePositive()
	{
		fp++;
	}

	@Override
	public void addTo(final String prefix, final Map<String, Object> map)
	{
		map.put(prefix + name + "TP", tp);
		map.put(prefix + name + "FN", fn);
		map.put(prefix + name + "FP", fp);
		map.put(prefix + name + "TN", tn);
		map.put(prefix + name + "Correct", corr);
		map.put(prefix + name + "Incorrect", incorr);
		map.put(prefix + name + "Accuracy", acc);
		map.put(prefix + name + "Total", tot);
		map.put(prefix + name + "Precision", prec);
		map.put(prefix + name + "Recall", rec);
		map.put(prefix + name + "Specificity", spec);
		map.put(prefix + name + "F1Score", f1);
	}

	public void addTrueNegative()
	{
		tn++;
	}

	public void addTruePositive()
	{
		tp++;
	}

	@Override
	public void analyze()
	{
		corr = tp + tn;
		incorr = fp + fn;
		posact = tp + fn;
		negact = tn + fp;
		posans = tp + fp;
		negans = tn + fn;
		tot = corr + incorr;
		acc = tot == 0 ? 0F : ((float) corr) / tot;
		prec = posans == 0 ? 0F : ((float) tp) / posans;
		rec = posact == 0 ? 0F : ((float) tp) / posact;
		spec = negact == 0 ? 0F : ((float) tn) / negact;
		f1 = (prec + rec) == 0 ? 0F : 2 * prec * rec / (prec + rec);
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
		tp = 0;
		fn = 0;
		fp = 0;
		tn = 0;
		corr = 0;
		incorr = 0;
		tot = 0;
		acc = 0F;
		prec = 0F;
		rec = 0F;
		spec = 0F;
		f1 = 0F;
	}

	public void compare(final float[] target, final float[] output)
	{
		if(wta)
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

			if(target[i] >= mid)
			{
				if(output[i] >= mid)
					tp++;
				else
					fn++;
			}
			else
			// target[i] < mid
			{
				if(output[i] >= mid)
					fp++;
				else
					tn++;
			}
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
			if(target[i] == on)
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

		// If we get it right, we get one true positive and a bunch of true negatives
		if(targetIdx == outputIdx)
		{
			tp++;
			tn += target.length - 1;
		}
		// If we get it wrong, one false positive, one false negative, remaining true negatives
		else
		{
			fp++;
			fn++;
			tn += target.length - 2;
		}
	}

	public float getAccuracy()
	{
		return acc;
	}

	public int getActualNegatives()
	{
		return negact;
	}

	public int getActualPositives()
	{
		return posact;
	}

	public int getCorrect()
	{
		return corr;
	}

	public float getF1Score()
	{
		return f1;
	}

	public int getFalseNegatives()
	{
		return fn;
	}

	public int getFalsePositives()
	{
		return fp;
	}

	public int getIncorrect()
	{
		return incorr;
	}

	public int getNegativeAnswers()
	{
		return negans;
	}

	public int getPositiveAnswers()
	{
		return posans;
	}

	public float getPrecision()
	{
		return prec;
	}

	public float getRecall()
	{
		return rec;
	}

	public float getSensitivity()
	{
		// Sensitivity == Precision
		return prec;
	}

	public float getSpecificity()
	{
		return spec;
	}

	public int getTotal()
	{
		return tot;
	}

	public int getTrueNegatives()
	{
		return tn;
	}

	public int getTruePositives()
	{
		return tp;
	}

	@Override
	public void saveData(final CSVWriter out) throws IOException
	{
		out.appendField(tp);
		out.appendField(fn);
		out.appendField(fp);
		out.appendField(tn);
		out.appendField(corr);
		out.appendField(incorr);
		out.appendField(tot);
		out.appendField(acc);
		out.appendField(prec);
		out.appendField(rec);
		out.appendField(spec);
		out.appendField(f1);
	}

	@Override
	public void saveHeader(final CSVWriter out) throws IOException
	{
		saveHeader("", out);
	}

	@Override
	public void saveHeader(final String prefix, final CSVWriter out) throws IOException
	{
		out.appendHeader(prefix + name + "TP");
		out.appendHeader(prefix + name + "FN");
		out.appendHeader(prefix + name + "FP");
		out.appendHeader(prefix + name + "TN");
		out.appendHeader(prefix + name + "Correct");
		out.appendHeader(prefix + name + "Incorrect");
		out.appendHeader(prefix + name + "Total");
		out.appendHeader(prefix + name + "Accuracy");
		out.appendHeader(prefix + name + "Precision");
		out.appendHeader(prefix + name + "Recall");
		out.appendHeader(prefix + name + "Specificity");
		out.appendHeader(prefix + name + "F1Score");
	}

	@Override
	public String toString(final String prefix)
	{
		final StringBuilder sb = new StringBuilder();

		append(sb, prefix + "TruePositives", tp);
		append(sb, prefix + "FalseNegatives", fn);
		append(sb, prefix + "FalsePositives", fp);
		append(sb, prefix + "TrueNegatives", tn);
		append(sb, prefix + "BitsCorrect", corr);
		append(sb, prefix + "BitsIncorrect", incorr);
		append(sb, prefix + "BitsTotal", tot);
		append(sb, prefix + "BitsAccuracy", acc);
		append(sb, prefix + "Precision", prec);
		append(sb, prefix + "Recall", rec);
		append(sb, prefix + "Specificity", spec);
		append(sb, prefix + "F1Score", f1);

		return sb.toString();
	}
}
