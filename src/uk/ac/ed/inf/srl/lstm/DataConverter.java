package uk.ac.ed.inf.srl.lstm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.ParseConversionEvent;
import javax.xml.crypto.Data;

import se.lth.cs.srl.languages.Language;
import se.lth.cs.srl.languages.Language.L;

public class DataConverter implements Serializable {

	private static final long serialVersionUID = 1L;
	
	final EmbeddingNetwork net;
	int layers;
	int[] begin;
	int[] layersizes;
	Map<Integer, Float[]>[] embeddings;
	Map<String, Integer>[] indices;
	
	public DataConverter(EmbeddingNetwork net, boolean ont5) {
		this.net = net;
		layers = net.getNumberOfInputs();
		begin = net.getLayersBegin();
		layersizes = new int[layers];
		embeddings = new Map[layers];
		indices = new Map[layers];
		for(int i=0; i<layers; i++) {
			layersizes[i] = net.getLayersEnd()[i]-begin[i];
			//if(net.layers.get(i).equals("words")) {
			//	/** going off-duty here! **/
			////	System.err.println("Going off-duty!");
			//	createIndicesAndEmbeddingsFromFile(i, "/disk/scratch/mroth/vectors/en-fr.en");
			//} else {
				embeddings[i] = net.getEmbeddings(i);
			if(ont5)
				indices[i] = readIndices(new File("/disk/scratch/mroth/xlbp/lexicon_ont5/" + net.layers.get(i) + ".txt"));
			else if(Language.getLanguage().getL()==L.eng)
				indices[i] = readIndices(new File("/disk/scratch/mroth/xlbp/lexicon/" + net.layers.get(i) + ".txt"));
			else
				indices[i] = readIndices(new File("/disk/scratch/mroth/xlbp/lexicon_"+ Language.getLanguage().getL().toString() +"/" + net.layers.get(i) + ".txt"));					
		}
	}
	
	public DataConverter(EmbeddingNetwork embeddingNetwork) {
		this(embeddingNetwork, false);
	}

	private void createIndicesAndEmbeddingsFromFile(int x, String filename) {
		Map<Integer, Float[]> emb = new TreeMap<Integer, Float[]>();
		Map<String, Integer> index = new HashMap<String, Integer>();
		
		BufferedReader br = null;
		int i = 0;
		try {
			br = new BufferedReader(new FileReader(new File(filename)));
			String line = "";
			while((line = br.readLine())!=null) {
				String[] parts = line.split(" ");
				String s = parts[0];
				Float[] e = new Float[parts.length-1];
				for(int j=0; j<e.length; j++)
					e[j] = Float.valueOf(parts[1+j]);
				
				if(s.equals("=unk=")) emb.put(0, e);
				else emb.put(++i, e);
				
				index.put(s, i);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			try {
				br.close();								
			}  catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			} 
		}
		
		embeddings[x] = emb;
		indices[x] = index;
	}
	
	private Map<String, Integer> readIndices(File file) {
		Map<String, Integer> retval = new HashMap<String, Integer>();
		BufferedReader br = null;
		try {
			System.err.println("\tReading file " + file.toString());
			br = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = br.readLine())!=null) {
				String[] parts = line.split(" ");
				retval.put(parts[1], Integer.valueOf(parts[0]));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);							
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			try {
				br.close();								
			}  catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			} 
		}
		return retval;
	}

	public TreeMap<Integer, Float> createTrial(String input, String layername) {
		int layer = net.layers.indexOf(layername);
		int index = indices[layer].containsKey(input)?indices[layer].get(input):0;
		//System.err.print(input + "(" + index + ") ");
		
		TreeMap<Integer, Float> retval = new TreeMap<Integer, Float>();
		if(embeddings[layer] != null) {
			if(embeddings[layer].containsKey(index)) {
				for(int j=0; j<layersizes[layer]; j++)
					retval.put(j+begin[layer], embeddings[layer].get(index)[j]);
			} else {
				//System.err.println("Word not found: " + input);
				for(int j=0; j<layersizes[layer]; j++)
					retval.put(j+begin[layer], embeddings[layer].get(0)[j]);				
			}
		} else {
			retval.put(begin[layer] + index, 1.0F);
		}
		//System.err.println(retval);
		return retval;
	}

}
