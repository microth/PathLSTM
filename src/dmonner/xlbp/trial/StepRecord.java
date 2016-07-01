package dmonner.xlbp.trial;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import dmonner.xlbp.layer.Layer;

public class StepRecord
{
	private final Step step;
	private final Map<Layer, float[]> recordings;

	public StepRecord(final Step step)
	{
		this.step = step;
		this.recordings = new HashMap<Layer, float[]>();

		for(final Layer layer : step.getRecordLayers())
			recordings.put(layer, layer.getActivations().clone());
	}

	public float[] getRecording(final Layer layer)
	{
		return recordings.get(layer);
	}

	public Set<Entry<Layer, float[]>> getRecordings()
	{
		return recordings.entrySet();
	}

	public Step getStep()
	{
		return step;
	}
}
