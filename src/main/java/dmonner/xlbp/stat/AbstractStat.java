package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.Map;

import dmonner.xlbp.util.CSVWriter;

public abstract class AbstractStat implements Stat
{
	@Override
	public void addTo(final Map<String, Object> map)
	{
		addTo("", map);
	}

	@Override
	public void saveHeader(final CSVWriter out) throws IOException
	{
		saveHeader("", out);
	}

	@Override
	public String toString()
	{
		return toString("");
	}
}
