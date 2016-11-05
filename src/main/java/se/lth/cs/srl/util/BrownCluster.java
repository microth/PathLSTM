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

public class BrownCluster implements Serializable {
	private static final long serialVersionUID = 1L;
	static final int DEF_SHORT_LEN = 6;
	static final int DEF_CUT_THRESHOLD = 10;

	private final Map<String, ClusterEntry> map;

	public BrownCluster(DataInput input) throws IOException {
		map = new HashMap<String, ClusterEntry>();
		read(input);
	}

	public BrownCluster(File dataFile) throws IOException {
		this(dataFile, DEF_SHORT_LEN, DEF_CUT_THRESHOLD);
	}

	public BrownCluster(File dataFile, int shortLen, int threshold)
			throws IOException {
		map = new HashMap<String, ClusterEntry>();
		if (dataFile != null) {
			InputStream is = new FileInputStream(dataFile);
			if (dataFile.toString().endsWith(".gz"))
				is = new GZIPInputStream(is);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF8"));
			populateCluster(reader, shortLen, threshold);
			reader.close();
		}
	}

	private void populateCluster(BufferedReader reader, int shortLen,
			int threshold) throws IOException {
		int sInd = 1;
		int lInd = 1;
		Map<Integer, Integer> sM = new HashMap<Integer, Integer>();
		Map<Integer, Integer> lM = new HashMap<Integer, Integer>();
		String line;
		Pattern tab = Pattern.compile("\t");
		int lineCount = 0;
		int saveCount = 0;
		while ((line = reader.readLine()) != null) {
			lineCount++;
			String[] cols = tab.split(line);
			int count = Integer.parseInt(cols[2]);
			if (count < threshold)
				continue;
			saveCount++;
			// Ok, we store it. Now calculate the short and long bit strings as
			// short values:
			Integer _long = Integer.parseInt(cols[0], 2);
			Integer _short = Integer.parseInt(
					cols[0].length() > shortLen ? cols[0]
							.substring(0, shortLen) : cols[0], 2);

			Integer _s = sM.get(_short);
			if (_s == null) {
				_s = sInd++;
				sM.put(_short, _s);
			}
			Integer _l = lM.get(_long);
			if (_l == null) {
				_l = lInd++;
				lM.put(_long, _l);
			}
			map.put(cols[1], new ClusterEntry(_s, _l));
		}
		System.out.println("Initiated brown cluster. Read " + lineCount
				+ " lines, saved " + saveCount);
	}

	static final class ClusterEntry implements Serializable {
		private static final long serialVersionUID = 1L;
		final int s;
		final int l;

		public ClusterEntry(int s, int l) {
			this.s = s;
			this.l = l;
		}
	}

	// Helpers for the feature functions:
	public enum ClusterVal {
		SHORT, LONG
	}

	public String getValue(String s, ClusterVal cv) {
		ClusterEntry ce = map.get(s);
		if (ce == null)
			return null;
		switch (cv) {
		case SHORT:
			return Integer.toString(ce.s);
		case LONG:
			return Integer.toString(ce.l);
		default:
			throw new Error("not implemented");
		}
	}

	// For reading and writing manually (not really needed if serializable is
	// used)
	private static final String MAGIC_STRING = "CLUSTER-MAGIC-KEY";

	private void read(DataInput input) throws IOException {
		String foo = input.readUTF();
		if (!foo.equals(MAGIC_STRING))
			throw new Error(
					"Error reading brown cluster. Magic string not found.");
		int entries = input.readInt();
		for (int i = 0; i < entries; ++i) {
			String str = input.readUTF();
			int s = input.readInt();
			int l = input.readInt();
			map.put(str, new ClusterEntry(s, l));
		}
	}

	public void write(DataOutput output) throws IOException {
		output.writeUTF(MAGIC_STRING);
		output.writeInt(map.size());
		for (Entry<String, ClusterEntry> e : map.entrySet()) {
			ClusterEntry ce = e.getValue();
			output.writeUTF(e.getKey());
			output.writeInt(ce.s);
			output.writeInt(ce.l);
		}
	}

	// To test
	public static void main(String[] args) throws IOException {
		// File input=new
		// File("/home/users0/anders/d8/corpora/paths-eng-781k.txt");
		File input = new File(
				"/afs/inf.ed.ac.uk/user/m/mroth/mate++/conll-2009-ST-English-train.wordsonly-c32-p1.out/paths");
		BrownCluster c = new BrownCluster(input, 3, 3);

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
		BrownCluster c2 = new BrownCluster(dis);
		printExamples(examples, c2);
	}

	private static void printExamples(String[] examples, BrownCluster c) {
		System.out.printf("%12s | %12s | %12s\n", "Form", "Short", "Long");
		for (String e : examples) {
			ClusterEntry ce = c.map.get(e);
			Object[] o;
			if (ce == null)
				o = new Object[] { e, "null", "null" };
			else
				o = new Object[] { e, new Integer(ce.s), new Integer(ce.l) };
			System.out.printf("%-12s | %12s | %12s\n", o);
		}
		System.out.println();
	}

}
