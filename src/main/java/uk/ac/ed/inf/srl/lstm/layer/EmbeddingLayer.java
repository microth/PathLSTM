package uk.ac.ed.inf.srl.lstm.layer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EmbeddingLayer {
	List<String> internalLayers;
	List<Map<Integer, Float[]>> internalEmbeddings;
	List<Integer> internalLayersBegin;
	List<Integer> internalLayersEnd;
	int size;
	
	public EmbeddingLayer() {
		size = 0;
		internalLayers = new ArrayList<String>();
		internalEmbeddings = new ArrayList<Map<Integer, Float[]>>();
		internalLayersBegin = new ArrayList<Integer>();
		internalLayersEnd = new ArrayList<Integer>();
	}

	public int getNumberOfInputs() {
		return internalLayers.size();
	}
	
	public int[] getLayersBegin() {
		int[] retval = new int[internalLayers.size()];
		for(int i=0; i<retval.length; i++)
			retval[i] = internalLayersBegin.get(i);
		return retval;
	}
	
	public int[] getLayersEnd() {
		int[] retval = new int[internalLayers.size()];
		for(int i=0; i<retval.length; i++)
			retval[i] = internalLayersEnd.get(i);
		return retval;	}
	
	public Map<Integer, Float[]> getEmbeddings(int i) {
		return internalEmbeddings.get(i);
	}
	
	public int getInputLayerSize() {
		return size;
	}
	
	public void addEmbeddingLayer(String inputName, File lexicon, File vectors) {
		int inputsize = -1;
		internalLayersBegin.add(internalLayers.size()==0?0:1+size);		
		internalLayers.add(inputName);		
		if(vectors!=null) {		
			Map<Integer, Float[]> map = readVectors(vectors , lexicon);
			internalEmbeddings.add(map);
			inputsize = ((Object[])map.values().iterator().next()).length;
		} else {
			internalEmbeddings.add(null);
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
		internalLayersEnd.add(size);
			
	}
	
	private Map<Integer, Float[]> readVectors(File vectors, File lexicon) {
		BufferedReader br = null;
		Map<String, Integer> tmp = new HashMap<String, Integer>();
		Map<Integer, Float[]> retval = new TreeMap<Integer, Float[]>();		

		tmp.put("=unk=", 0);
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
	
}
