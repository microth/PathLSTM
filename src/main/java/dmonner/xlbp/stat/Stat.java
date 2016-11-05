package dmonner.xlbp.stat;

import java.io.IOException;
import java.util.Map;

import dmonner.xlbp.util.CSVWriter;

public interface Stat
{
	public abstract void add(final Stat that);

	public abstract void addTo(final Map<String, Object> map);

	public abstract void addTo(final String prefix, final Map<String, Object> map);

	public abstract void analyze();

	public abstract void clear();

	public abstract void saveData(final CSVWriter out) throws IOException;

	public abstract void saveHeader(final CSVWriter out) throws IOException;

	public abstract void saveHeader(final String prefix, final CSVWriter out) throws IOException;

	public abstract String toString(final String prefix);
}
