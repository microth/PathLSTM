package dmonner.xlbp.util;

import java.io.FileWriter;
import java.io.IOException;

public class CSVWriter extends FileWriter
{
	private final String fieldSep;
	private final String recordSep;
	private final String quote;
	private final String escape;
	private boolean beginningOfRecord;

	public CSVWriter(final String filename) throws IOException
	{
		this(filename, false, ",", "\n", "\"");
	}

	public CSVWriter(final String filename, final boolean append) throws IOException
	{
		this(filename, append, ",", "\n", "\"");
	}

	public CSVWriter(final String filename, final boolean append, final String fieldSep,
		final String recordSep, final String quote) throws IOException
	{
		super(filename, append);
		this.fieldSep = fieldSep;
		this.recordSep = recordSep;
		this.quote = quote;
		this.escape = "\\\\";
		this.beginningOfRecord = true;
	}

	public void appendField(final boolean field) throws IOException
	{
		sep();
		append(String.valueOf(field));
	}

	public void appendField(final float field) throws IOException
	{
		sep();
		append(String.valueOf(field));
	}

	public void appendField(final int field) throws IOException
	{
		sep();
		append(String.valueOf(field));
	}

	public void appendField(final Object field) throws IOException
	{
		sep();
		if(field == null)
		{
			append("null");
		}
		else
		{
			append(quote);
			append(clean(field.toString()));
			append(quote);
		}
	}

	public void appendFields(final float[] fields) throws IOException
	{
		if(fields.length > 0)
		{
			sep();
			append(String.valueOf(fields[0]));
		}

		for(int i = 1; i < fields.length; i++)
		{
			append(fieldSep);
			append(String.valueOf(fields[i]));
		}
	}

	public void appendFields(final Object[] fields) throws IOException
	{
		if(fields.length > 0)
		{
			sep();

			if(fields[0] instanceof String)
			{
				append(quote);
				append(clean((String)fields[0]));
				append(quote);
			}
			else
			{
				append(String.valueOf(fields[0]));
			}
		}

		for(int i = 1; i < fields.length; i++)
		{
			if(fields[i] instanceof String)
			{
				append(fieldSep);
				append(quote);
				append(clean((String)fields[i]));
				append(quote);
			}
			else
			{
				append(fieldSep);
				append(String.valueOf(fields[i]));
			}
		}
	}

	public void appendHeader(final String header) throws IOException
	{
		sep();
		append(quote);
		append(clean(header));
		append(quote);
	}

	public void appendHeaders(final String[] headers) throws IOException
	{
		if(headers.length > 0)
		{
			sep();
			append(quote);
			append(clean(headers[0]));
			append(quote);
		}

		for(int i = 1; i < headers.length; i++)
		{
			append(fieldSep);
			append(quote);
			append(clean(headers[i]));
			append(quote);
		}
	}

	public void beginRecord() throws IOException
	{
		endRecord();
	}

	private String clean(final String s)
	{
		if(quote.isEmpty())
			return s.replaceAll(escape, escape + escape);
		return s.replaceAll(escape, escape + escape).replaceAll(quote, escape + quote);
	}

	@Override
	public void close() throws IOException
	{
		endRecord();
		super.close();
	}

	public void endRecord() throws IOException
	{
		if(!beginningOfRecord)
		{
			append(recordSep);
			flush();
			beginningOfRecord = true;
		}
	}

	private void sep() throws IOException
	{
		if(beginningOfRecord)
			beginningOfRecord = false;
		else
			append(fieldSep);
	}
}
