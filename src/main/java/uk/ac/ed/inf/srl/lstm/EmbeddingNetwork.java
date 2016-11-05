package uk.ac.ed.inf.srl.lstm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import dmonner.xlbp.Network;
import dmonner.xlbp.UniformWeightInitializer;
import dmonner.xlbp.WeightUpdaterType;
import dmonner.xlbp.compound.InputCompound;
import dmonner.xlbp.compound.MemoryCellCompound;
import dmonner.xlbp.compound.RectifiedLinearCompound;
import dmonner.xlbp.compound.XEntropyTargetCompound;

public class EmbeddingNetwork extends Network {

	private static final long serialVersionUID = 6988305909477053307L;
	
	public List<String> layers;
	List<Map<Integer, Float[]>> embeddings;
	List<Integer> layersBegin;
	List<Integer> layersEnd;
	int size;
	boolean supplementary;

	private String gates;
	private int hidden1;
	private int hidden2;
	private float dropout;
	
	private long seed;
	
	MemoryCellCompound mc;
	//MemoryCellCompound mc2;
	InputCompound in;
	
	private NetworkOptions options;
	
	public EmbeddingNetwork(String name, NetworkOptions options) {
		super(name);
		this.options = options;
		
		this.seed = options.seed;
		this.hidden1 = options.hidden1;
		this.hidden2 = options.hidden2;
		this.gates = options.gates;
		this.dropout = options.dropout;
		
		size = 0;
		layers = new ArrayList<>();
		embeddings = new ArrayList<>();
		layersBegin = new ArrayList<>();
		layersEnd = new ArrayList<>();
		supplementary = false;
	}
	
	public int getNumberOfInputs() {
		return layers.size();
	}
	
	public int[] getLayersBegin() {
		int[] retval = new int[layers.size()];
		for(int i=0; i<retval.length; i++)
			retval[i] = layersBegin.get(i);
		return retval;
	}
	
	public int[] getLayersEnd() {
		int[] retval = new int[layers.size()];
		for(int i=0; i<retval.length; i++)
			retval[i] = layersEnd.get(i);
		return retval;	}
	
	public Map<Integer, Float[]> getEmbeddings(int i) {
		return embeddings.get(i);
	}

	public int getInputLayerSize() {
		return size;
	}
	
	public void addEmbeddingLayer(String inputName, File lexicon, File vectors) {
		int inputsize = -1;
		layersBegin.add(layers.size()==0?0:1+size);		
		layers.add(inputName);		
		if(vectors!=null) {		
			Map<Integer, Float[]> map = readVectors(vectors , lexicon);
			embeddings.add(map);
			inputsize = ((Object[])map.values().iterator().next()).length;
		} else {
			embeddings.add(null);
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(lexicon));
				String line = null;
				while((line = br.readLine()) != null) {
					String[] parts = line.split(" ");
					int tmp = Integer.parseInt(parts[0]);
					if(tmp>inputsize) inputsize = tmp;
				}
			} catch(Exception e) {
				e.printStackTrace();
				System.exit(1);
			} finally {
				try {
					br.close();					
				} catch(Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			inputsize+=2; // +1 for unknown types
		}
		size += inputsize;
		layersEnd.add(size);
			
	}
	
	@Override
	public void build() {
		in = new InputCompound("Input", size); /* size of vocab */
		final XEntropyTargetCompound out = new XEntropyTargetCompound("Output", NetworkRunner.OUTPUTLENGTH); /* size of label set */
		mc = null;
		if(hidden1>0) {
			mc = new MemoryCellCompound("Hidden", hidden1, gates);		
			mc.addUpstreamWeights(in);
			//mc2 = new MemoryCellCompound("Hidden2", hidden1, gates);
			//mc2.addUpstreamWeights(mc);
			out.addUpstreamWeights(mc);
		}
		
		if(supplementary) {
			if(hidden2>0) {
				final InputCompound supp = new InputCompound("ExtraInput", 450000);
				this.add(supp);			
				final RectifiedLinearCompound supphid = new RectifiedLinearCompound("ExtraHidden", hidden2, dropout);
				supphid.addUpstreamWeights(supp);
				if(hidden1>0) supphid.addUpstreamWeights(mc);
				out.addUpstreamWeights(supphid);			
				this.add(supphid);
			}			
			/**out.addUpstreamWeights(supp);**/
		}
		
		if(hidden1>0) {
			this.add(in);
			this.add(mc); // one hidden layer
			//this.add(mc2);
		}
		this.add(out); // softmax output layer
		
		this.setWeightUpdaterType(WeightUpdaterType.basic(options.alpha));
		this.setWeightInitializer(new UniformWeightInitializer(new Random(seed), 1.0F, -options.max, options.max));
		
		//// experimental:
		//this.setWeightUpdaterType(WeightUpdaterType.adam(0.9F, 0.999F, 0.00000001F, options.alpha, 1));
		//this.setWeightUpdaterType(WeightUpdaterType.adam(0.9F, 0.999F, 0.000001F, 0.01F, 1));
		//mc.setWeightUpdaterType(WeightUpdaterType.basic(0.1F));
		
		this.optimize();
		super.build();		
	}
	
	private Map<Integer, Float[]> readVectors(File vectors, File lexicon) {
		BufferedReader br = null;
		Map<String, Integer> tmp = new HashMap<>();
		Map<Integer, Float[]> retval = new TreeMap<>();

		tmp.put("=unk=", 0);
//		tmp.put("unknown", 0);
		try {
			br = new BufferedReader(new FileReader(lexicon));
			String line = "";
			while((line = br.readLine())!=null) {			
				String[] id_label = line.split(" "); 
                tmp.put(id_label[1], Integer.parseInt(id_label[0]));
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
		
		try {
			br = new BufferedReader(new FileReader(vectors));
			String line = "";
			while((line = br.readLine())!=null) {
				String[] parts = line.split(" ");
				if(parts.length < 5 ) continue;				
				
				if(tmp.containsKey(parts[0])) {
					int i = tmp.get(parts[0]);
					Float[] val = new Float[parts.length-1];
					for(int j=1; j < parts.length; j++) {
						val[j-1] = Float.valueOf(parts[j]);
					}
					retval.put(i, val);
				}
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
		return retval; 
	}

	public void addSupplementary() {
		supplementary = true;
	}

	public void activateFirstTest() {
		in.activateTest();
		if(mc!=null) mc.activateTest();
		//if(mc2!=null) mc2.activateTest();
	}
	
	public void activateFirstTrain() {
		in.activateTrain();		
		if(mc!=null) mc.activateTrain();
		//if(mc2!=null) mc2.activateTrain();
		in.updateEligibilities();
		if(mc!=null) mc.updateEligibilities();
		//if(mc2!=null) mc2.updateEligibilities();
	}

	public long getSeed() {
		return seed;
	}


	
}
