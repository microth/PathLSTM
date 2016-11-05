package uk.ac.ed.inf.srl.features;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureFile {

	/*
	 * These files have the following format: POSPrefix feat feat feat ...
	 * 
	 * POSPrefix feat feat ...
	 */

	public static Map<String, List<String>> readFile(File file)
			throws IOException {
		if (!file.exists())
			return null;
		BufferedReader in = new BufferedReader(new FileReader(file));
		return readFile(in);

	}

	public static Map<String, List<String>> readFile(BufferedReader in)
			throws IOException {
		Map<String, List<String>> ret = new HashMap<String, List<String>>();
		List<String> array = null;
		String line, prefix = null;
		while ((line = in.readLine()) != null) {
			if (line.startsWith("#")) {
				continue;
			} else if (prefix == null) { // Prefix was null, the line contains a
											// POS-tag
				prefix = line;
				array = new ArrayList<String>();
			} else if (line.equals("")) { // Empty line, this marks the end of a
											// feature set
				ret.put(prefix, array);
				prefix = null; // Set prefix to null, next line should contain a
								// POS-tag
				array = null;
			} else { // Else this is a feature to be added to the current array
				String name;
				if (line.contains(" ")) { // Only consider the first word on the
											// line
					name = line.substring(0, line.indexOf(" "));
				} else {
					name = line;
				}
				array.add(name);
			}
		}
		if (prefix != null && array != null && array.size() != 0)
			ret.put(prefix, array);
		in.close();
		return ret;
	}

	public static void writeToOutput(FeatureSet fs, PrintWriter out)
			throws IOException {
		boolean first = true;
		for (String prefix : fs.POSPrefixes) {
			if (first) {
				first = false;
			} else {
				out.println();
			}
			out.println(prefix);
			for (Feature f : fs.get(prefix)) {
				out.println(f.getName() + " - size: " + f.size(false) + "/"
						+ f.size(true));
			}
		}
		out.flush();
	}

	public static void writeToFile(List<Feature> features, String POSPrefix,
			List<String> comments, File file) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(new BufferedOutputStream(
				new FileOutputStream(file)));
		out.println(POSPrefix);
		for (int i = 0; i < features.size(); ++i) {
			Feature f = features.get(i);
			out.print(f.getName() + " - size: " + f.size(false) + "/"
					+ f.size(true));
			if (i < comments.size())
				out.print(" # " + comments.get(i));
			out.println();
		}
		out.close();
	}
}
