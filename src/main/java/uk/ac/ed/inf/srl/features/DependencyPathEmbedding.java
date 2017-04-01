package uk.ac.ed.inf.srl.features;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmonner.xlbp.compound.AbstractWeightedCompound;
import se.lth.cs.srl.Parse;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import uk.ac.ed.inf.srl.lstm.DataConverter;
import uk.ac.ed.inf.srl.lstm.EmbeddingNetwork;

public abstract class DependencyPathEmbedding extends ContinuousSetFeature {
	private static final long serialVersionUID = 1L;

	protected EmbeddingNetwork net;
	protected DataConverter dc;
	protected int dim;

	protected DependencyPathEmbedding(FeatureName name, TargetWord tw,
			String POSPrefix, boolean comp, EmbeddingNetwork net, DataConverter dc,
			int dim) {
		super(name, true, false, POSPrefix);
		
		indexcounter = dim;

		this.net = net;
		this.dc = dc;
		this.dim = dim;
	}

	@Override
	public void addFeatures(Sentence s, Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, int predIndex, int argIndex,
			Integer offset, boolean allWords) {
		
		float[] emb = getFeatureValue(indices, s, predIndex, argIndex);
		for(int i=0; i<emb.length; i++) {
			nonbinFeats.put(i + offset, (double)emb[i]);
		}
	}

	@Override
	public void addFeatures(Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats, Predicate pred, Word arg,
			Integer offset, boolean allWords) {

		float[] emb = getFeatureValue(indices, pred, arg);
		for(int i=0; i<emb.length; i++) {
			nonbinFeats.put(i + offset, (double)emb[i]);
		}
	}

	public float[] getFeatureValue(Collection<Integer> indices, Sentence s, int predIndex, int argIndex) {
		return getFeatureValue(indices, (Predicate)s.get(predIndex), s.get(argIndex));
	}

	@Override
	public float[] getFeatureValue(Sentence s, int predIndex, int argIndex) {
		System.err.println("ERROR");
		return null;
	}
	
	@Override
	public float[] getFeatureValue(Predicate pred, Word arg) {
		System.err.println("ERROR");
		return null;
	}
		
	public float[] getFeatureValue(Collection<Integer> indices, Predicate pred, Word arg) {
		float[] emb = pred.getPathEmbedding(name.toString(), arg);
		if(emb==null) {
			
			net.clear();
			
			List<Word> path = Word.findPath(pred, arg);
			int length = path.size();
			
			boolean up = true;
			
			StringBuffer path2string = new StringBuffer();
			for(int i=0; i<length; i++) {
				Word w = path.get(i);
				
				
				if( (Parse.parseOptions!=null && Parse.parseOptions.noPathEmbs)) {
					HashMap<Integer, Float> ix = new HashMap<>();
					for(int x : indices)
						ix.put(x, 1.0F);		
					net.getInputLayer(0).setInput(ix);
					net.activateTest();				
					break;
				}							
				
				if(net.getInputLayers().length>0) {
					if(i>0) path2string.append(":");
					net.getInputLayer(1).setInput( dc.createTrial(w.getPOS(), "pos")  );
					path2string.append(w.getPOS());
					path2string.append(":");
					path2string.append(w.getForm().toLowerCase());
					net.activateFirstTest();
					net.getInputLayer(1).setInput( dc.createTrial(w.getForm().toLowerCase(), "words")  );
				}						
				
				if(i==length-1) {
					HashMap<Integer, Float> in = new HashMap<>();
					for(int x : indices)
						in.put(x, 1.0F);
			
					net.getInputLayer(0).setInput(in);
					net.activateTest();					
					
				} else if(net.getInputLayers().length>1) { 
					net.activateFirstTest();
					String dep = null;
					if (up) {
						if (w.getHead() == path.get(i + 1)) { // Arrow up
							dep = path.get(i).getDeprel() + "v";
						} else { // Arrow down
							dep = path.get(i+1).getDeprel()  + "^";
							up = false;
						}
					} else {
						dep = path.get(i+1).getDeprel() + "^";
					}
					net.getInputLayer(1).setInput( dc.createTrial(dep, "rels")  );
					path2string.append(":");
					path2string.append(dep);
					net.activateFirstTest();
				}
			}
	
			int hid1 = 0;
			if(net.getComponentByName("Hidden")!=null)
				hid1 = ((AbstractWeightedCompound)net.getComponentByName("Hidden")).getOutput().getActivations().length;
			int hid2 = 0;
			if(net.getComponentByName("ExtraHidden")!=null)
				hid2 = ((AbstractWeightedCompound)net.getComponentByName("ExtraHidden")).getOutput().getActivations().length;
			
			emb = new float[hid1+hid2];
			Arrays.fill(emb, 0.0F);
			
			if(hid1>0 && !(Parse.parseOptions!=null && Parse.parseOptions.noPathEmbs))
				System.arraycopy(((AbstractWeightedCompound)net.getComponentByName("Hidden")).getOutput().getActivations(), 0, emb, 0, hid1);
			if(hid2>0)
				System.arraycopy(((AbstractWeightedCompound)net.getComponentByName("ExtraHidden")).getOutput().getActivations(), 0, emb, hid1, hid2);
					
			pred.putPathEmbedding(name.toString(), arg, emb);
			pred.putPathPrediction(name.toString(), arg, net.getTargetLayer().getActivations().clone());
		}
	
		return emb;
	}

	public int classify() {
		int ret = -1;
		float max = Float.MIN_VALUE;

		float[] output = net.getTargetLayer().getActivations();
		for(int i=0; i<output.length; i++) {
			if(output[i]>max) {
				ret = i;
				max = output[i];
			}
		}
		//System.err.println("Classified by NN: " + ret);
		return ret;
	}

	public float[] getOutput() {
		return net.getTargetLayer().getActivations();
	}
	
	/*public List<Label> classifyProb() {
		float[] output = net.getTargetLayer().getActivations();
		ArrayList<Label> ret = new ArrayList<Label>(output.length);
		for (int i = 0; i < output.length; ++i) {
			ret.add(new Label(i, (double)output[i]));
		}
		Collections.sort(ret, Collections.reverseOrder());
		return ret;
	}*/

	public float[] getEmbedding() {
		return ((AbstractWeightedCompound)net.getComponentByName("Hidden")).getOutput().getActivations();
	}

	public String getPath(Predicate pred, Word arg) {
		List<Word> path = Word.findPath(pred, arg);
		StringBuffer path2string = new StringBuffer();
		int length = path.size();

		boolean up = true;
		for(int i=0; i<length; i++) {
			Word w = path.get(i);
			
			if(net.getInputLayers().length>0) {
				if(i>0) path2string.append(":");
				net.getInputLayer(1).setInput( dc.createTrial(w.getPOS(), "pos")  );
				path2string.append(w.getPOS());
				path2string.append(":");
				path2string.append(w.getForm().toLowerCase());
				net.getInputLayer(1).setInput( dc.createTrial(w.getForm().toLowerCase(), "words")  );
			}						
			
			if(i==length-1) {
				
			} else if(net.getInputLayers().length>1) { 
				String dep = null;
				if (up) {
					if (w.getHead() == path.get(i + 1)) { // Arrow up
						dep = path.get(i).getDeprel() + "v";
					} else { // Arrow down
						dep = path.get(i+1).getDeprel()  + "^";
						up = false;
					}
				} else {
					dep = path.get(i+1).getDeprel() + "^";
				}
				net.getInputLayer(1).setInput( dc.createTrial(dep, "rels")  );
				path2string.append(":");
				path2string.append(dep);
			}
		}
		return path2string.toString();
	}

}
