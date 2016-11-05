package se.lth.cs.srl.pipeline;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

import se.lth.cs.srl.corpus.Sentence;
import uk.ac.ed.inf.srl.features.FeatureSet;
import uk.ac.ed.inf.srl.ml.Model;

public abstract class AbstractStep implements PipelineStep {

	public static final Integer POSITIVE = 1;
	public static final Integer NEGATIVE = 0;

	protected FeatureSet featureSet;
	protected Map<String, Model> models;

	public AbstractStep(FeatureSet fs) {
		this.featureSet = fs;
	}

	public abstract void extractInstances(Sentence s);

	public abstract void parse(Sentence s);

	protected abstract String getModelFileName();

	@Override
	public void readModels(ZipFile zipFile) throws IOException,
			ClassNotFoundException {
		models = new HashMap<>();
		readModels(zipFile, models, getModelFileName());
	}


	static void readModels(ZipFile zipFile, Map<String, Model> models,
			String filename) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(
				zipFile.getInputStream(zipFile.getEntry(filename)));
		int numberOfModels = ois.readInt();
		for (int i = 0; i < numberOfModels; ++i) {
			String POSPrefix = (String) ois.readObject();
			Model m = (Model) ois.readObject();
			models.put(POSPrefix, m);
		}
	}

}
