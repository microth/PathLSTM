package se.lth.cs.srl.features;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipFile;

public class DumpFeature {

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		ZipFile zipFile = new ZipFile("/home/anders/slask/ger-pl.model");
		ObjectInputStream ois = new ObjectInputStream(
				zipFile.getInputStream(zipFile.getEntry("objects")));
		FeatureGenerator fg = (FeatureGenerator) ois.readObject();
		Feature f = fg.getQFeature(FeatureName.LeftPOS, FeatureName.ArgFeats,
				true, "V", null, null);
		ArrayList<String> values = new ArrayList<>();
		values.addAll(f.indices.keySet());
		Collections.sort(values);
		for (String value : values) {
			System.out.println(value + " - " + f.indexOf(value));
		}
	}
}
