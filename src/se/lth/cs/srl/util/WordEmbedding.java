package se.lth.cs.srl.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class WordEmbedding implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final int DEF_DIMENSIONALITY = 50;

	private final Map<String, EmbeddingEntry> map;

	public WordEmbedding(DataInput input) throws IOException {
		map = new HashMap<String, EmbeddingEntry>();
		read(input);
	}

	public WordEmbedding(File dataFile) throws IOException {
		this(dataFile, DEF_DIMENSIONALITY);
	}

	public WordEmbedding(File dataFile, int dim) throws IOException {
		map = new HashMap<String, EmbeddingEntry>();
		if (dataFile != null) {
			InputStream is = new FileInputStream(dataFile);
			if (dataFile.toString().endsWith(".gz"))
				is = new GZIPInputStream(is);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF8"));
			populateEmbedding(reader, dim);
			reader.close();
		}
	}

	private void populateEmbedding(BufferedReader reader, int dim)
			throws IOException {
		// int sInd=1;
		// int lInd=1;
		// Map<Integer,Integer> sM=new HashMap<Integer,Integer>();
		// Map<Integer,Integer> lM=new HashMap<Integer,Integer>();
		String line;
		Pattern tab = Pattern.compile(" ");
		int lineCount = 0;
		int saveCount = 0;
		while ((line = reader.readLine()) != null) {
			lineCount++;
			String[] cols = tab.split(line);
			// int count=Integer.parseInt(cols[2]);
			// if(count<threshold)
			// continue;
			saveCount++;
			// Ok, we store it. Now calculate the short and long bit strings as
			// short values:

			double[] entry = new double[cols.length - 1];
			for (int i = 0; i < entry.length; i++)
				entry[i] = Double.parseDouble(cols[i + 1]);

			// Integer _long=Integer.parseInt(cols[0], 2);
			// Integer
			// _short=Integer.parseInt(cols[0].length()>shortLen?cols[0].substring(0,
			// shortLen):cols[0], 2);
			// Integer _s=sM.get(_short);
			// if(_s==null){
			// _s=sInd++;
			// sM.put(_short, _s);
			// }
			// Integer _l=lM.get(_long);
			// if(_l==null){
			// _l=lInd++;
			// lM.put(_long, _l);
			// }

			map.put(cols[0], new EmbeddingEntry(entry));
		}
		System.out.println("Initiated word embedding. Read " + lineCount
				+ " lines, saved " + saveCount);
	}

	static final class EmbeddingEntry implements Serializable {
		private static final long serialVersionUID = 1L;
		final double[] entry;

		public EmbeddingEntry(double[] entry) {
			this.entry = new double[entry.length];
			for (int i = 0; i < entry.length; i++)
				this.entry[i] = entry[i];
		}
	}

	// Helpers for the feature functions:
	public Double getValue(String s, int dim) {
		EmbeddingEntry ee = map.get(s);
		if (ee == null)
			return 0.0;
		if (ee.entry.length <= dim)
			return 0.0;
		return ee.entry[dim];
	}

	// For reading and writing manually (not really needed if serializable is
	// used)
	private static final String MAGIC_STRING = "EMBEDDING-MAGIC-KEY";

	private void read(DataInput input) throws IOException {
		String foo = input.readUTF();
		if (!foo.equals(MAGIC_STRING))
			throw new Error(
					"Error reading word embedding. Magic string not found.");
		int entries = input.readInt();
		for (int i = 0; i < entries; ++i) {
			String str = input.readUTF();
			double[] entry = new double[DEF_DIMENSIONALITY];
			for (int j = 0; j < entry.length; j++)
				entry[j] = input.readDouble();
			map.put(str, new EmbeddingEntry(entry));
		}
	}

	public void write(DataOutput output) throws IOException {
		output.writeUTF(MAGIC_STRING);
		output.writeInt(map.size());
		for (Entry<String, EmbeddingEntry> e : map.entrySet()) {
			EmbeddingEntry ce = e.getValue();
			output.writeUTF(e.getKey());
			for (int i = 0; i < ce.entry.length; i++)
				output.writeDouble(ce.entry[i]);
		}
	}

	// To test
	public static void main(String[] args) throws IOException {
		File input = new File(
				"/afs/inf.ed.ac.uk/user/m/mroth/s-case/mate/embeddings/CW_embeddings_by_turian_50dims_scaled.txt");
		WordEmbedding c = new WordEmbedding(input);

		String[] examples = { "believe", "hello", "hi", "bye", "banana",
				"apple", "pepsi", "beer", "wine", "water", "asasfasfaf",
				"drink", "eat", "ate", "drank", "drunk", "eaten", "devour" };

		// Write out some stuff
		printExamples(examples, c);

		// Save the cluster
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream("foobar")));
		c.write(dos);
		dos.close();
		// Reread it and write out the same stuff
		DataInputStream dis = new DataInputStream(new BufferedInputStream(
				new FileInputStream("foobar")));
		WordEmbedding c2 = new WordEmbedding(dis);
		printExamples(examples, c2);
	}

	private static void printExamples(String[] examples, WordEmbedding c) {
		System.out.printf("%12s | %12s | %12s\n", "Form", "Dim 0", "Dim 1");
		for (String e : examples) {
			EmbeddingEntry ce = c.map.get(e);
			Object[] o;
			if (ce == null)
				o = new Object[] { e, "null", "null" };
			else
				o = new Object[] { e, new Double(ce.entry[0]),
						new Double(ce.entry[1]) };
			System.out.printf("%-12s | %12s | %12s\n", o);
		}
		System.out.println();
	}

}
