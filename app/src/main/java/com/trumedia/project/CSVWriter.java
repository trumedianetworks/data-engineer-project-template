package com.trumedia.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import com.trumedia.project.CSVData.Line;

public class CSVWriter
{
	private static final char DEFAULT_DELIM = ',';
	private static final char DEFAULT_QUOTE = '"';
	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	private static final String RAW_LINEFEED = "\n";
	private static final String RAW_CARRIAGE_RETURN = "\r";
	private static final String ESCAPED_LINEFEED = "\\n";
	private static final String ESCAPED_CARRIAGE_RETURN = "\\r";

	private final PrintWriter m_writer;
	private final String m_delimChar;
	private final String m_quoteChar;
	private final String m_doubleQuote;
	private boolean m_onStartOfLine;

	public CSVWriter(File file) throws FileNotFoundException
	{
		this(file, DEFAULT_CHARSET, DEFAULT_DELIM, DEFAULT_QUOTE);
	}

	public CSVWriter(File file, Charset charSet) throws FileNotFoundException
	{
		this(file, charSet, DEFAULT_DELIM, DEFAULT_QUOTE);
	}

	public CSVWriter(File file, char delimChar, char quoteChar) throws FileNotFoundException
	{
		this(file, DEFAULT_CHARSET, delimChar, quoteChar);
	}

	public CSVWriter(File file, Charset charSet, char delimChar, char quoteChar) throws FileNotFoundException
	{
		this(new FileOutputStream(file), charSet, delimChar, quoteChar);
	}

	public CSVWriter(OutputStream stream)
	{
		this(stream, DEFAULT_CHARSET, DEFAULT_DELIM, DEFAULT_QUOTE);
	}

	public CSVWriter(OutputStream stream, Charset charSet)
	{
		this(stream, charSet, DEFAULT_DELIM, DEFAULT_QUOTE);
	}

	public CSVWriter(OutputStream stream, char delimChar, char quoteChar)
	{
		this(stream, DEFAULT_CHARSET, delimChar, quoteChar);
	}

	public CSVWriter(OutputStream stream, Charset charSet, char delimChar, char quoteChar)
	{
		m_writer = new PrintWriter(new OutputStreamWriter(stream, charSet));
		m_delimChar = String.valueOf(delimChar);
		m_quoteChar = String.valueOf(quoteChar);
		m_doubleQuote = String.valueOf(m_quoteChar) + String.valueOf(m_quoteChar);
		m_onStartOfLine = true;
	}

	public void write(CSVData csv, boolean closeWriter)
	{
		write(csv, true, closeWriter);
	}

	public void write(CSVData csv, boolean includeHeader, boolean closeWriter)
	{
		// header
		if (includeHeader)
		{
			for (String headerName : csv.getHeader().getNames())
			{
				write(headerName);
			}
			endLine();
		}

		// values
		for (Line line : csv.getLines())
		{
			for (String value : line.getValues())
			{
				write(value);
			}
			endLine();
		}

		if (closeWriter)
		{
			close();
		}
		else
		{
			m_writer.flush();
		}
	}

	public void write(String value)
	{
		if (value == null)
		{
			writeRawEntry("");
		}
		else
		{
			if (value.contains(m_delimChar) || value.contains(m_quoteChar) || value.contains(RAW_CARRIAGE_RETURN) || value.contains(RAW_LINEFEED))
				writeRawEntry(m_quoteChar + value.replace(m_quoteChar, m_doubleQuote).replace(RAW_CARRIAGE_RETURN, ESCAPED_CARRIAGE_RETURN).replace(RAW_LINEFEED, ESCAPED_LINEFEED) + m_quoteChar);
			else
				writeRawEntry(value);
		}
	}

	public void write(int value)
	{
		writeRawEntry(String.valueOf(value));
	}

	public void write(long value)
	{
		writeRawEntry(String.valueOf(value));
	}

	public void write(float value)
	{
		if (Float.isNaN(value))
			writeRawEntry("NaN");
		else
			writeRawEntry(String.valueOf(value));
	}

	public void write(double value)
	{
		if (Double.isNaN(value))
			writeRawEntry("NaN");
		else
			writeRawEntry(String.valueOf(value));
	}

	public void write(boolean value)
	{
		writeRawEntry(String.valueOf(value));
	}

	public void endLine()
	{
		m_writer.println();
		m_onStartOfLine = true;
	}

	public void close()
	{
		m_writer.close();
	}

	protected void writeRawEntry(String raw)
	{
		if (!m_onStartOfLine)
			m_writer.write(m_delimChar);
		else
			m_onStartOfLine = false;
		m_writer.write(raw);
	}
}
