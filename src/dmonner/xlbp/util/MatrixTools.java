package dmonner.xlbp.util;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

public class MatrixTools
{
	public static DecimalFormat floatFormat = new DecimalFormat("0.0000");
	public static String floatWidth = "%8s";

	public static int argmin(final int[] a)
	{
		int arg = 0;
		for(int i = 1; i < a.length; i++)
			if(a[i] < a[arg])
				arg = i;
		return arg;
	}

	public static float[] copy(final float[] source)
	{
		if(source == null)
			return null;
		return Arrays.copyOf(source, source.length);
	}

	public static float[][] copy(final float[][] source)
	{
		if(source == null)
			return null;
		final float[][] dest = new float[source.length][];
		for(int i = 0; i < dest.length; i++)
			dest[i] = copy(source[i]);
		return dest;
	}

	public static int[] copy(final int[] source)
	{
		if(source == null)
			return null;
		return Arrays.copyOf(source, source.length);
	}

	public static int[][] copy(final int[][] source)
	{
		if(source == null)
			return null;
		final int[][] dest = new int[source.length][];
		for(int i = 0; i < dest.length; i++)
			dest[i] = copy(source[i]);
		return dest;
	}

	public static float distance(final float[] a, final float[] b)
	{
		return (float) Math.sqrt(distanceSq(a, b));
	}

	public static float distanceSq(final float[] a, final float[] b)
	{
		if(a.length != b.length)
			throw new IllegalArgumentException("Vectors must be of the same length: " + a.length + " != "
					+ b.length);

		float dist = 0f;

		for(int i = 0; i < a.length; i++)
			dist += (b[i] - a[i]) * (b[i] - a[i]);

		return dist;
	}

	public static float[] empty(final float[] source)
	{
		if(source == null)
			return null;
		return new float[source.length];
	}

	public static float[][] empty(final float[][] source)
	{
		if(source == null)
			return null;
		final float[][] dest = new float[source.length][];
		for(int i = 0; i < dest.length; i++)
			dest[i] = empty(source[i]);
		return dest;
	}

	public static int[] empty(final int[] source)
	{
		if(source == null)
			return null;
		return new int[source.length];
	}

	public static int[][] empty(final int[][] source)
	{
		if(source == null)
			return null;
		final int[][] dest = new int[source.length][];
		for(int i = 0; i < dest.length; i++)
			dest[i] = empty(source[i]);
		return dest;
	}

	public static float[][] identity(final int n)
	{
		final float[][] f = new float[n][n];
		for(int i = 0; i < n; i++)
			f[i][i] = 1F;
		return f;
	}

	public static void multiply(final float[] source, final float[] dest, final int len)
	{
		for(int k = 0; k < len; k++)
			dest[k] *= source[k];
	}

	public static void multiplyElementwise(final float[][] source, final float[][] dest, final int m,
			final int n)
	{
		for(int i = 0; i < m; i++)
		{
			final float[] di = dest[i];
			final float[] si = source[i];
			for(int j = 0; j < n; j++)
				di[j] *= si[j];
		}
	}

	public static void randomize(final int[] t, final Random r)
	{
		for(int i = 0; i < t.length - 1; i++)
		{
			final int j = r.nextInt(t.length - i) + i;
			final int tmp = t[i];
			t[i] = t[j];
			t[j] = tmp;
		}
	}

	public static <T> void randomize(final T[] t, final Random r)
	{
		for(int i = 0; i < t.length - 1; i++)
		{
			final int j = r.nextInt(t.length - i) + i;
			final T tmp = t[i];
			t[i] = t[j];
			t[j] = tmp;
		}
	}

	public static int[] range(final int end)
	{
		final int[] r = new int[end];
		for(int i = 0; i < end; i++)
			r[i] = i;
		return r;
	}

	public static void rotateLeft(final float[] dest, final float[] src, int offset)
	{
		final int x = src.length;
		offset = offset % x;
		if(offset < 0)
			offset += x;
		for(int i = offset, j = 0; i < x; i++, j++)
			dest[j] = src[i];
		for(int i = 0, j = x - offset; i < offset; i++, j++)
			dest[j] = src[i];
	}

	public static void rotateRight(final float[] dest, final float[] src, int offset)
	{
		final int x = src.length;
		offset = offset % x;
		if(offset < 0)
			offset += x;
		for(int i = 0, j = offset; j < x; i++, j++)
			dest[j] = src[i];
		for(int i = x - offset, j = 0; j < offset; i++, j++)
			dest[j] = src[i];
	}

	public static String toString(final float[] a)
	{
		return toString(a, a.length, floatFormat, floatWidth);
	}

	public static String toString(final float[] a, final DecimalFormat fmt)
	{
		final StringBuffer sb = new StringBuffer();
		sb.append("[");
		for(int i = 0; i < a.length; i++)
		{
			sb.append(fmt.format(a[i]));
			if(i < a.length - 1)
				sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	public static String toString(final float[] a, final DecimalFormat fmt, final String widthfmt)
	{
		return toString(a, a.length, fmt, widthfmt);
	}

	public static String toString(final float[] a, final int size)
	{
		return toString(a, size, floatFormat, floatWidth);
	}

	public static String toString(final float[] a, final int size, final DecimalFormat fmt,
			final String widthfmt)
	{
		final StringBuffer sb = new StringBuffer();
		sb.append("[");
		for(int i = 0; i < size; i++)
		{
			sb.append(String.format(widthfmt, fmt.format(a[i])));
			if(i < a.length - 1)
				sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	public static String toString(final float[][] a)
	{
		return toString(a, a.length, a.length > 0 ? a[0].length : 0, floatFormat, floatWidth);
	}

	public static String toString(final float[][] a, final int rowSize, final int colSize)
	{
		return toString(a, rowSize, colSize, floatFormat, floatWidth);
	}

	public static String toString(final float[][] a, final int rowSize, final int colSize,
			final DecimalFormat fmt, final String widthfmt)
	{
		final StringBuffer sb = new StringBuffer();
		sb.append("[");
		for(int i = 0; i < rowSize; i++)
		{
			if(i > 0)
				sb.append(" ");
			sb.append(MatrixTools.toString(a[i], colSize, fmt, widthfmt));
			if(i < a.length - 1)
				sb.append(",\n");
		}
		sb.append("]");
		return sb.toString();
	}

    public static float[][] matrixMultiply(float[][] A, float[][] B) {
        int mA = A.length;
        int nA = A[0].length;
        int mB = B.length;
        int nB = B[0].length;
        if (nA != mB) throw new RuntimeException("Illegal matrix dimensions: " + mA + "x" + nA + " * " + mB + "x" + nB);
        float[][] C = new float[mA][nB];
        for (int i = 0; i < mA; i++)
            for (int j = 0; j < nB; j++)
                for (int k = 0; k < nA; k++)
                    C[i][j] += A[i][k] * B[k][j];
        return C;
    }

}
