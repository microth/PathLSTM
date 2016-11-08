package dmonner.xlbp;

import java.util.Random;

public class SetWeightInitializer implements WeightInitializer
{
	private static final long serialVersionUID = 1L;

	private final float[][] w;
	private final boolean full;

	public SetWeightInitializer(final float[][] w)
	{
		this.w = w;
		this.full = checkFull();
	}

	public SetWeightInitializer(final float[][] w, final boolean showFull)
	{
		this.w = w;
		this.full = showFull;
	}

	public SetWeightInitializer(int x, int y, int copy, float[][] matrix) {
		
		this.w = new float[x][y];
		//System.err.println(x + "\t" + y);
		//System.err.println(matrix.length + "\t" + matrix[0].length);
		
		for(int i=0; i<x; i++) {
			for(int j=0; j<y-copy; j++) {
				w[i][j] = new Random().nextFloat() * 2F - 1F; 
			}
			for(int j=1; j<=copy; j++) {
				w[i][y-j] = matrix[i][matrix[0].length-j];
			}
		}
		this.full = checkFull();

	}

	private boolean checkFull()
	{
		for(int j = 0; j < w.length; j++)
			for(int i = 0; i < w[j].length; i++)
				if(!newWeight(j, i))
					return false;

		return true;
	}

	@Override
	public boolean fullConnectivity()
	{
		return full;
	}

	@Override
	public boolean newWeight(final int j, final int i)
	{
		final float v = w[j][i];
		return !Float.isNaN(v) && !Float.isInfinite(v) && v != 0F;
	}

	@Override
	public float randomWeight(final int j, final int i)
	{
		return w[j][i];
	}

}
