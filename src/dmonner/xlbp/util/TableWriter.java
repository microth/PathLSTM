package dmonner.xlbp.util;

import java.io.IOException;

public class TableWriter extends CSVWriter
{
	public TableWriter(final String filename) throws IOException
	{
		this(filename, false);
	}

	public TableWriter(final String filename, final boolean append) throws IOException
	{
		super(filename, append, "\t", "\n", "");
	}
}
