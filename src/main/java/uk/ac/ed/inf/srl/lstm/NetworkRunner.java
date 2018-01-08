package uk.ac.ed.inf.srl.lstm;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import uk.ac.ed.inf.srl.lstm.DataReader.DataStream;
import dmonner.xlbp.AdamBasicWeightUpdater;
import dmonner.xlbp.BasicWeightUpdater;
import dmonner.xlbp.compound.AbstractWeightedCompound;
import dmonner.xlbp.compound.InputCompound;
import dmonner.xlbp.compound.FunctionCompound;
import dmonner.xlbp.trial.Step;
import dmonner.xlbp.trial.Trial;

public class NetworkRunner {
	final EmbeddingNetwork net;
	final String setup;
	
	final static int VECTORSIZE = 40;
	static int OUTPUTLENGTH = -1; /* 1 + number of labels */

	final int batchsize;
	
	String DATADIR = "/disk/scratch/mroth/mateplus/";
	String TRAIN = "_main.ssv";
	String SUPP = "_ultimate.ssv";
	
	String VAL = "_valdata_main.ssv";
	String VALSUPP = "_valdata_ultimate.ssv";
	
	int NEGOFFSET = 0;
	private NetworkOptions options; 	
	final static String TEST = "";
	
	public void addSupplementary() {
		net.addSupplementary();
	}
	
	public NetworkRunner(NetworkOptions options) {
		this.options = options;
		if(options.datatype.startsWith("ai"))
			OUTPUTLENGTH = 2;
		
		if(options.ablate!=null)
			SUPP = SUPP.replaceAll("ultimate", "ultimate-"+options.ablate);
		
		if(options.language.equals("ENG")) {
			if(options.datatype.startsWith("ac"))
				OUTPUTLENGTH = 54;
		} else {
			TRAIN = TRAIN.replaceAll("main", "traindata_main_"+options.language);
			SUPP = SUPP.replaceAll("ultimate", "traindata_supp_"+options.language);
			
			VAL = VAL.replaceAll("valdata_main", "devdata_main_"+options.language);
			VALSUPP = VALSUPP.replaceAll("valdata_ultimate", "devdata_supp_"+options.language);
			
			if(options.datatype.startsWith("ac")) {
				if(options.language.equals("GER")) {
					OUTPUTLENGTH = 10;
				}
				if(options.language.equals("SPA")) {
					OUTPUTLENGTH = 43;
				}
				if(options.language.equals("CHI")) {
					OUTPUTLENGTH = 36;
				}
				if(options.language.equals("ONT5")) {
					OUTPUTLENGTH = 66;
				}
				if(options.language.equals("FNET17")) {
					OUTPUTLENGTH = 732;				
				}
			}
			if(options.language.equals("FNET17")) {
				DATADIR = "/local/mroth/mateplus/";
				TRAIN = "_data_main_FNET17.ssv";
				SUPP  = "_data_supp_FNET17.ssv";
				VAL= "_data_main_FNET17.ssv";
				VALSUPP  = "_data_supp_FNET17.ssv";	
			}
		}
				
		// initialize network
		setup = options.gates;		
		net = new EmbeddingNetwork("Canonical LSTM", options);	
		this.batchsize = options.batchsize;
	}
	
	public NetworkRunner(File serializedFile, NetworkOptions options) throws IOException, ClassNotFoundException {
		this.options = options;
		if(options.datatype.startsWith("ac"))
			OUTPUTLENGTH = 54;
		if(options.datatype.startsWith("ai"))
			OUTPUTLENGTH = 2;
		
		batchsize = 1;
		setup = "";
		ZipFile z = new ZipFile(serializedFile);
		ObjectInputStream ois = new ObjectInputStream(
				z.getInputStream(z.getEntry("network.o")));
		//ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serializedFile));
		net = (EmbeddingNetwork)ois.readObject();		
	}

	public static void main(String[] args) throws Exception {
		NetworkOptions options = new NetworkOptions(args);
		/**/NetworkRunner network = new NetworkRunner(options);
		network.addSupplementary();

		network.addInput("words", 
				new File("/local/mroth/xlbp/lexicon"+(!options.language.equals("ENG")?"_"+options.language.toLowerCase():"")+"/words.txt"), 
				new File("/local/mroth/vectors/en-fr.en"));
				//new File("/disk/scratch/mroth/german_vectors/out_orig1_projected.txt.sparse+bin"));
				//null);
		network.addInput("pos", 
				new File("/local/mroth/xlbp/lexicon"+(!options.language.equals("ENG")?"_"+options.language.toLowerCase():"")+"/pos.txt"), 
				null);
		network.addInput("rels", 
				new File("/local/mroth/xlbp/lexicon"+(!options.language.equals("ENG")?"_"+options.language.toLowerCase():"")+"/rels.txt"), 
				null);
		network.addInput("position",
				new File("/local/mroth/xlbp/lexicon"+(!options.language.equals("ENG")?"_"+options.language.toLowerCase():"")+"/position.txt"),
				null);
		
		if(options.nonbinpath) {
			switch (options.datatype) {
				case "acV": network.NEGOFFSET=5; break;
				case "acN": network.NEGOFFSET=5; break;
				case "aiV": network.NEGOFFSET=4; break;
				case "aiN": network.NEGOFFSET=6; break;
				default: System.err.println("No such dataype: " + args[6]); System.exit(1);
			}
		}
		
		network.build();
		if(options.datatype.startsWith("ac"))
			network.runExperiment(50);
		else
			network.runExperiment(25);/**/
				
		//for(File f : new File(".").listFiles(new FilenameFilter() {			
		//	@Override
		//	public boolean accept(File dir, String name) {
		//		return 
		//				name.startsWith("aiN_SRLNetwork_wIO_1.0percTrain_99hid_208hid2_wSuppFeats_best") ||
		//				name.startsWith("aiN_SRLNetwork_wIOFUT_1.0percTrain_43hid_29hid2_wSuppFeats_best");
		//	}
		//})) {
		//	try {
		//		new NetworkRunner(f, options).runExperiment(0);
		//		System.out.println("\t" + f.toString());
		//	} catch(ZipException e) {
		//		// skip corrupted zip files
		//	}
		//}		
	}

	private void build() {
		net.build();
	}

	private void addInput(String string, File lexicon, File vectors) {
		net.addEmbeddingLayer(string, lexicon, vectors);
	}

	private void runExperiment(int epochs) throws IOException {
		double trainPercent = 1.0;
								
		if(epochs==-1) {
			BufferedWriter bw = new BufferedWriter(new FileWriter("interesting_cases.vec"));
			DataStream test = new DataReader(net).createDataStream(DATADIR+options.datatype+TEST);
			while(test.hasNext()) {
				net.clear();
				
				Trial t = test.next();
				for(Step s : t.getSteps()) {
					((InputCompound)net.getComponentByName("Input")).getInputLayer().setInput(s.getInput().getValue());
					net.activateTest();
				}
				
				if(!TEST.equals("")) {
					boolean first = true;
					//for(float f : ((MemoryCellCompound)net.getComponent(1)).getStateLayer().getActivations()) {
					for(float f : ((FunctionCompound)net.getComponent(1)).getOutput().getActivations()) {
						if(!first) bw.write(" ");
						else first = false;
						bw.write(Float.toString(f));							
					}
					bw.newLine();
				}
			}
			bw.close();
			test.close();			
		} else if(epochs==0) {
			{
				DataStream test = new DataReader(net, NEGOFFSET).createDataStream(DATADIR+options.datatype+VAL, false);
				DataStream supplement = 
						(net.getComponentByName("ExtraInput")!=null?new DataReader(net).createDataStream(DATADIR+options.datatype+VALSUPP, true):null);	
				
				int corr = 0;
				int incorr = 0;
				int tp = 0;
				int fp = 0;
				int fn = 0;
				
				//NetworkStringBuilder nw = new NetworkStringBuilder("-W");
				//((AbstractWeightedCompound)net.getComponentByName("ExtraHidden")).toString(nw);
				//System.err.println(nw);
				
				while(test.hasNext()) {
                    //if(tp+fp+fn>100000)// System.err.print(".");
                    //     break;
					
					net.clear();
					Trial supp = (supplement!=null?supplement.next():null);
					Trial t = test.next();
					int gold = -1;
					
					for(Step s : t.getSteps()) {	
						/*if(s.getInput().getBinValue()!=null)
							net.setInput(s.getInput().getBinValue());	
						else
							net.setInput(s.getInput().getValue());*/
						if(net.getInputLayers().length>(supp!=null?1:0) && s.getInput(supp!=null?1:0)!=null) {
							net.getInputLayer(supp!=null?1:0).setInput(s.getInput(supp!=null?1:0).getValue());
							/////System.err.println(s.getInput(supp!=null?1:0).getValue());
						}
					
						if(s.getTargets().size()>0) {
							if(supp!=null) {
								net.getInputLayer(0).setInput(supp.getSteps().get(0).getInput(0).getValue());
								/////System.err.println(supp.getSteps().get(0).getInput(0));
							}
							
							net.activateTest();
							
							/////float[] emb = ((AbstractWeightedCompound)net.getComponentByName("Hidden")).getOutput().getActivations();
							/////System.err.println("\n" + Arrays.toString(emb));
							
							/////emb = ((AbstractWeightedCompound)net.getComponentByName("ExtraHidden")).getOutput().getActivations();
							/////System.err.println("\n" + Arrays.toString(emb));
							
							//////if(corr+incorr>10)
							//////	System.exit(1);
							
							float[] gold_activ = s.getTarget().getValue();
							for(int i=0; i<gold_activ.length; i++) {
								if(gold_activ[i]>0) {
									gold = i;
									/////break;
								}
							}
						} else 
							net.activateFirstTest();

					}
								
					/////if(true)
					/////	System.exit(0);
					
					int best_class = -1;
					float best_activ = Float.MIN_VALUE;
					float[] output = net.getTargetLayer().getActivations();
					for(int i=0; i<output.length; i++) {
						if(output[i]>best_activ) {
							best_class = i;
							best_activ = output[i];
						}
					}
					
					if(OUTPUTLENGTH<5) {
						//System.out.println(gold + "\t" + best_class);
						if(gold==1 && best_class==gold) {
							tp++;
						} else {
							if(gold==1)
								fn++;
							else if(best_class==1)
								fp++;								
						}
					} else {
						if(best_class==gold) {
							corr++;
						} else {
							incorr++;
						}
					}
				}
			test.close();
			if(OUTPUTLENGTH<5) {
				//System.out.print(tp +"\t" + fp + "\t" + fn + "\t");
				float p = ((float)tp/(float)(tp+fp));
				float r = ((float)tp/(float)(tp+fn));
				System.out.print(/*"F1: %f\n", */(2F*p*r)/(p+r));
			} else
				System.out.print((float)corr/(float)(corr+incorr));
					
			}
		} else {
			float last_valacc = 0.0F;
			float seclast_valacc = 0.0F;
			float best_valacc = 0.0F;
			for(int e = 0; e < epochs; e++) {
				DataStream train = new DataReader(net).createDataStream(DATADIR+options.datatype+TRAIN, false);
				DataStream supplement = 
						(net.getComponentByName("ExtraInput")!=null?new DataReader(net, NEGOFFSET).createDataStream(DATADIR+options.datatype+SUPP, true):null);				
				DataStream test = new DataReader(net).createDataStream(DATADIR+options.datatype+VAL, false);
				DataStream testsupp = 
						(net.getComponentByName("ExtraInput")!=null?new DataReader(net, NEGOFFSET).createDataStream(DATADIR+options.datatype+VALSUPP, true):null);
				
				Random rand = new Random(66539L+e); 			
							
				System.out.print("Epoch " + (e>9?"":" ") + e + " ");
				// train
				int count = 0;
				while(train.hasNext()) {
					++count;
					if(count%100==0)
						AdamBasicWeightUpdater.t += 1;

					if(count%10000==0) {
						System.out.print(".");
					}
					
					if(count==options.trainsize)
						break;
					
					Trial t = null;
					try {
						t = train.next();
					} catch(Exception ex) {
						System.err.println("Error at instance " + count);
						ex.printStackTrace();
						System.exit(0);						
					}
					Trial supp = (supplement==null?null:supplement.next());
					if(rand.nextDouble()>trainPercent) continue;					
					
					net.clear();
					
					int stepnum = 0;
					for(Step s : t.getSteps()) {
						/*if(s.getInput().getBinValue()!=null) {
							net.setInput(s.getInput().getBinValue());
							//if(count==3) { System.err.println(Arrays.toString(s.getInput().getBinValue())); }							
						} else {
							net.setInput(s.getInput().getValue());
							//if(count==3) { System.err.println(Arrays.toString(s.getInput().getValue())); }
						}*/
						if(net.getInputLayers().length>(supp!=null?1:0) && s.getInput(supp!=null?1:0)!=null)
							net.getInputLayer(supp!=null?1:0).setInput(s.getInput(supp!=null?1:0).getValue());
						//System.out.println(++stepnum + " " + s.getInput().getValue());
						
						//System.out.println(Arrays.toString(s.getInput().getValue()));

	
						//System.err.println(Arrays.toString(((MemoryCellCompound)net.getComponent(1)).getOutput().getActivations()));
						
						if(s.getTargets().size()>0) {
							if(epochs>options.supplementEpoch && supp!=null) {
								net.getInputLayer(0).setInput(supp.getSteps().get(0).getInput(0).getValue());
							}
							
							net.activateTrain();
							net.updateEligibilities();
							
							net.setTarget(s.getTarget().getValue());
							net.updateResponsibilities();
							
							net.updateWeights(); // update weights now with BasicWeightUpdater
						} else {
							//net.activateTrain();
							//net.updateEligibilities();
							net.activateFirstTrain();							
						}
					}
					if(count%batchsize==0) {
						net.processBatch();
						//System.err.println(((XEntropyTargetCompound)net.getComponentByName("Output")).getUpstreamWeights().getConnection().getWeight(0, 0));
					}
					
				}
				train.close();
				if(supplement!=null)
					supplement.close();
				//if(true)
				//	continue;
				
				
				
				//if(e == epochs-1) {
				if(true) {
					int corr = 0;
					int incorr = 0;
					int tp = 0;
					int fp = 0;
					int fn = 0;
					count = 0;
					while(test.hasNext()) {
						net.clear();
						count++;
						
						Trial t = test.next();
						Trial supp = testsupp==null?null:testsupp.next();
						int gold = -1;
						
						for(Step s : t.getSteps()) {						
							/*if(s.getInput().getBinValue()!=null)
								net.setInput(s.getInput().getBinValue());	
							else
								net.setInput(s.getInput().getValue());*/
							
							if(net.getInputLayers().length>(supp!=null?1:0) && s.getInput(supp!=null?1:0)!=null)
								net.getInputLayer(supp!=null?1:0).setInput(s.getInput(supp!=null?1:0).getValue());
													
							if(s.getTargets().size()>0) {
								if(testsupp!=null)
									net.getInputLayer(0).setInput(supp.getSteps().get(0).getInput(0).getValue());
								
								net.activateTest();
								
								float[] gold_activ = s.getTarget().getValue();
								for(int i=0; i<gold_activ.length; i++) {
									if(gold_activ[i]>0) {
										gold = i;
										break;
									}
								}
							} else {
								net.activateFirstTest();
								//net.activateTest();
							}
	
						}
									
						int best_class = -1;
						float best_activ = Float.MIN_VALUE;
						float[] output = net.getTargetLayer().getActivations();
						//if(count==1) System.err.println(Arrays.toString(output));
						for(int i=0; i<output.length; i++) {
							if(output[i]>best_activ) {
								best_class = i;
								best_activ = output[i];
							}
						}
						
						if(OUTPUTLENGTH<5) {
							//if(best_class==1)
							//	System.out.println(gold + "\t" + best_class);
							if(gold==1 && best_class==gold) {
								tp++;
							} else {
								if(gold==1)
									fn++;
								else if(best_class==1)
									fp++;								
							}
						} else {
							if(best_class==gold) {
								corr++;
							} else {
								incorr++;
							}
						}
					}
				test.close();
				if(testsupp!=null)
					testsupp.close();
				//if(OUTPUTLENGTH<5) {
				//	System.out.print(tp +"\t" + fp + "\t" + fn + "\t");
				//	System.out.printf("P/R: %f/%f\n", ((float)tp/(float)(tp+fp)), ((float)tp/(float)(tp+fn)));
				//}
				//else {
				float curr_valacc = -1;
				if(OUTPUTLENGTH<5) {
					float prec = ((float)tp/(float)(tp+fp));
					float rec = ((float)tp/(float)(tp+fn));
					curr_valacc = (float)(2.0F*prec*rec)/(prec+rec);
				} else {
					curr_valacc = (float)corr/(float)(corr+incorr);
				}
					if(options.updatealpha && curr_valacc < last_valacc && curr_valacc < seclast_valacc) {
						BasicWeightUpdater.HALF *= 2.0F;
						//System.out.println(" (alpha*2)");
					} //else
						//System.out.println();
					
					if(curr_valacc>best_valacc) {
						best_valacc = curr_valacc;
						// TODO: also write out lexicon and other training properties (name of training file, etc.?) 
						ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
								new FileOutputStream(new File(options.datatype+"_SRLNetwork_" + 
										"w" + (setup.equals("")?"oAny":setup) + "_" +
//										trainPercent + "percTrain" +
										(net.getComponentByName("Hidden")==null?"":"_"+((AbstractWeightedCompound)net.getComponentByName("Hidden")).getOutput().getActivations().length + "hid") +
										(net.getComponentByName("ExtraHidden")==null?"":"_"+((AbstractWeightedCompound)net.getComponentByName("ExtraHidden")).getOutput().getActivations().length + "hid2") +
										(net.getInputLayers().length>1?"_wSuppFeats":"") +
										(options.language!=null?"_"+options.language:"")+
										"_seed"+net.getSeed()))));
										//"_best"))));
						zos.putNextEntry(new ZipEntry("network.o"));
						ObjectOutputStream oos = new ObjectOutputStream(zos);
						oos.writeObject(net);
						oos.flush();
						zos.closeEntry();			
						zos.close();
					}
					
//					if(e==epochs-1) {
						// take average over last three results to avoid overfitting of hyperparameters
//						float avg = (curr_valacc+seclast_valacc+last_valacc)/3.0F;
//						if(Float.isNaN(avg))
//							avg = 0.0F;
						System.out.println(curr_valacc); 
//					} else
//						System.out.println();
//					seclast_valacc = last_valacc;
//					last_valacc = curr_valacc;
				}
			}
		}
			
	}



}
