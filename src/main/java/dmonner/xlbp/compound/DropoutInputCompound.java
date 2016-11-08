package dmonner.xlbp.compound;

import java.util.Random;

import dmonner.xlbp.NetworkCopier;

public class DropoutInputCompound extends InputCompound {

	private static final long serialVersionUID = 1574221823303987773L;
	final double dropout;  
	final Random r;
	
	public DropoutInputCompound(InputCompound that, NetworkCopier copier) {
		super(that, copier);
		dropout = 0.0;
		r = new Random();
	}

	public DropoutInputCompound(String string, double d, int inputlength) {
		super(string, inputlength);
		dropout = d;
		r = new Random();		
	}
	
	public void setInput(final float[] activations) {
		for(int i=0; i<activations.length; i++) {
			if(r.nextDouble()<dropout)
				activations[i] = 0;
		}
	}


}
