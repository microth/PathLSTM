package uk.ac.ed.inf.srl.lstm;

import java.io.Serializable;

public class NetworkOptions implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5036939912497699587L;
	
	public int hidden1 = 0;
	public int hidden2 = 0; 
	public String gates = "IOUF"; 
	public float alpha = 0.03F;
	public int batchsize = 1; 
	public boolean adagrad = false;
	public String datatype = null;
	public boolean nonbinpath = false;	
	public float dropout = 0.0F;
	public float max = 0.1F;
	public int supplementEpoch = -1;
	public boolean updatealpha = false;
	public int trainsize = -1;
	public long seed = 1L;
	public String ablate = null;
	public String language = "ENG";

	
	
	public NetworkOptions(String[] args) throws Exception {
		if(args.length == 0) {
			System.err.println("Please specify at least one of arguments from the following code block:\n"
			 + "case \"-nobinpathfeats\": nonbinpath = true; break;\n"
			 + "case \"-hidden1\": hidden1 = Integer.valueOf(args[++i]); break;\n"
			 + "case \"-hidden2\": hidden2 = Integer.valueOf(args[++i]); break;\n"
			 + "case \"-data\": datatype = args[++i]; break;\n"
			 + "case \"-batchsize\": batchsize = Integer.valueOf(args[++i]); break;\n"
			 + "case \"-alpha\": alpha = Float.valueOf(args[++i]); break;\n"
			 + "case \"-gates\": gates = args[++i]; break;\n"
			 + "case \"-dropout\": dropout = Float.valueOf(args[++i]); break;\n"
			 + "case \"-weightinit\": max = Float.valueOf(args[++i]); break;\n"
			 + "case \"-delaysupp\": supplementEpoch = Integer.valueOf(args[++i]); break;\n"
			 + "case \"-trainsize\": trainsize = Integer.valueOf(args[++i]); break;\n"
			 + "case \"-updatealpha\": updatealpha = true; break;\n"
			 + "case \"-language\": language = ENG; break;\n"
			 + "case \"-ablate\": ablate = {lexsyn,misc,context}; break;\n"
			 + "case \"-seed\": seed = 1L; break;\n");
			System.exit(1);
		}
		
		for(int i=0; i<args.length; i++) {
			switch (args[i]) {
				case "-nobinpathfeats": { nonbinpath = true; break; }
				case "-hidden1": { hidden1 = Integer.valueOf(args[++i]); break; }
				case "-hidden2": { hidden2 = Integer.valueOf(args[++i]); break; }
				case "-data": { datatype = args[++i]; break; }
				case "-batchsize": batchsize = Integer.valueOf(args[++i]); break;
				case "-alpha": alpha = Float.valueOf(args[++i]); break;
				case "-gates": gates = args[++i]; break;
				case "-dropout": dropout = Float.valueOf(args[++i]); break;
				case "-weightinit": max = Float.valueOf(args[++i]); break;
				case "-delaysupp": supplementEpoch = Integer.valueOf(args[++i]); break;
				case "-trainsize": trainsize = Integer.valueOf(args[++i]); break;
				case "-updatealpha": updatealpha = true; break;
				case "-seed": seed = Long.valueOf(args[++i]); break;
				case "-language": language = args[++i].toUpperCase(); break;
				case "-ablate": ablate = args[++i]; break;
				default: throw new Exception("ERROR! No such argument: " + args[i]);
			}
		}
	}
}
