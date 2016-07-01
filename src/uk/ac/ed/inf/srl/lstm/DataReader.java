package uk.ac.ed.inf.srl.lstm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sun.org.apache.bcel.internal.generic.NEW;

import uk.ac.ed.inf.srl.lstm.DataReader.DataStream;
import dmonner.xlbp.Input;
import dmonner.xlbp.compound.InputCompound;
import dmonner.xlbp.layer.InputLayer;
import dmonner.xlbp.Network;
import dmonner.xlbp.trial.Step;
import dmonner.xlbp.trial.Trial;

public class DataReader {
	final EmbeddingNetwork net;
	private int negoffset;
	
	public DataReader(EmbeddingNetwork net) {
		this.net = net;
	}
	
	public DataReader(EmbeddingNetwork net, int negoffset) {
		this.net = net;
		this.negoffset = negoffset;
	}
	
	public Trial[] readDataAsTrials(String datafile, int count) {
		Trial[] trials = new Trial[count];
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(datafile)));
			for(int i=0; i<count; i++) {
				String line = br.readLine();
				trials[i] = createTrial(line, false);				
			}			
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			try {
				br.close();
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
			
		return trials;
	}
	
	private Trial createTrial(String line, boolean supplement) {
		final Trial trial = new Trial(net);
		
		trial.setClear(true);
		//trial.log(); // record predicted outputs
		
		//System.err.println(line);
		String[] inputs = line.split(" ");
		
		if(supplement) {
			final Step step = trial.nextStep();
			Map<Integer, Float> inputmap = new TreeMap<Integer, Float>();
			for(int i=1; i<(inputs.length-negoffset); i++)
				inputmap.put(Integer.valueOf(inputs[i]), 1.0F);
			step.addInput(new Input(((InputCompound)net.getComponentByName("ExtraInput")).getInputLayer(), inputmap));
			
			//System.err.println(inputmap.keySet());
			return trial;
		}
		//System.err.println(line);
		
		/** reverse input: predicate first, then go from there **
		 * -- preferable, since the predicate dictates what the
		 *    dependency relations should express w.r.t. roles
		 *    (at least in PropBank)
		 */
		
		int layers = net.getNumberOfInputs();
		int[] begin = net.getLayersBegin();
		int[] layersizes = new int[layers];
		Map<Integer, Float[]>[] embeddings = new Map[layers];				
		for(int i=0; i<layers; i++) {
			embeddings[i] = net.getEmbeddings(i);
			layersizes[i] = net.getLayersEnd()[i]-begin[i];
		}
				
		Map inputmap = new TreeMap<Integer, Float>();
		for(int i=inputs.length-1; i>0; i--) {
			// position
			if(i==inputs.length-1) {
				//inputmap.put(begin[layers-1] + new Integer(inputs[i]), 1.0F);
			// words
			} else if(embeddings[(i-1)%(layers-1)] != null) {		
				if(embeddings[(i-1)%(layers-1)].containsKey(new Integer(inputs[i]))) {
					for(int j=0; j<layersizes[(i-1)%(layers-1)]; j++) {
						inputmap.put(j+begin[(i-1)%(layers-1)], embeddings[(i-1)%(layers-1)].get(new Integer(inputs[i]))[j]);
					}
				} else {
					for(int j=0; j<layersizes[(i-1)%(layers-1)]; j++) {
						inputmap.put(j+begin[(i-1)%(layers-1)], embeddings[(i-1)%(layers-1)].get(0)[j]);
					}
				}
				//step.addInput(new int[]{0});
			}
			/* else if(string2vector != null) {
					
				if(string2vector.containsKey(inputs[i])) {
					float[] retval = new float[net.getInputLayer().size()];
					for(int j=0; j<retval.length; j++)
						retval[j] = string2vector.get(inputs[i])[j];
					step.addInput(retval);
				} else {
					float[] retval = new float[net.getInputLayer().size()];
					for(int j=0; j<retval.length; j++)
						retval[j] = string2vector.get("=unk=")[j];
					step.addInput(retval);
				}
				
			} else {
				int index = vocab.indexOf(inputs[i]);
				step.addInput(new int[]{++index});
			}
			}*/ 
			else {
				// ignore POS 
				//if(net.layers.indexOf("rels") == (i-1)%(layers-1) || net.layers.indexOf("words") == (i-1)%(layers-1)) {
					inputmap.put(begin[(i-1)%(layers-1)] + new Integer(inputs[i]), 1.0F);				
				//}
			}
							
			//if((i-1)%layers==0) {
			if(!inputmap.isEmpty()) {
			//if(i==1) {			
				if(net.getComponentByName("Input")==null && i>1)
					continue;
				
				Step step = trial.nextStep();
				if(net.getComponentByName("Input")!=null)
					step.addInput(new Input(((InputCompound)net.getComponentByName("Input")).getInputLayer(), inputmap));
				if(i>1)
					inputmap = new TreeMap<Integer, Float>();
				if(i==1)					
					step.addTarget(buildVector(Integer.toString(new Integer(inputs[0])), false));
			}
		}
			
		//if(true)
		//	System.exit(1);
		
		return trial;
	}

	private float[] buildVector(String string, boolean input) {
		// initialize vector	
		float[] retval = new float[input?net.getInputLayerSize():net.getTargetLayer().size()];
		Arrays.fill(retval, 0);
		retval[Integer.parseInt(string)] = 1;
		return retval;
	}

	public DataStream createDataStream(String string) {
		return new DataStream(string);
	}
	
	public DataStream createDataStream(String string, boolean supplement) {
		return new DataStream(string, supplement);
	}
	
	public class DataStream {
		BufferedReader br;
		String next;
		final boolean supplement;
	
		public DataStream(String datafile, boolean supplement) {
			this.supplement = supplement;

			try {
				br = new BufferedReader(new FileReader(new File(datafile)));
				next = br.readLine();
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		public DataStream(String datafile) {
			supplement = false;

			try {
				br = new BufferedReader(new FileReader(new File(datafile)));
				next = br.readLine();
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		public boolean hasNext() {
			return next!=null;
		}

		public Trial next() {
			//System.out.println(next);
			Trial t = createTrial(next, supplement);
			try {
				next = br.readLine();
			}  catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			return t;
		}

		public void close() {
			try {
				br.close();
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
	}

}
