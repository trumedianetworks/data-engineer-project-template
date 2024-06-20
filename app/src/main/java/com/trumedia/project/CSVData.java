package com.trumedia.project;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class CSVData
{
	public enum SortType { STRING, NUMBER }

	private final Header m_header;
	private final List<Line> m_lines;
	private final Index[] m_indexes;

	public CSVData(String[] headerCols)
	{
		m_header = new Header(headerCols);
		m_lines = new ArrayList<Line>();
		m_indexes = new Index[headerCols.length];
	}

	public CSVData(Collection<String> headerCols)
	{
		this(headerCols.toArray(new String[headerCols.size()]));
	}

	public CSVData(Header header)
	{
		this(header.m_cols);
	}

	public CSVData(Header header, Line line)
	{
		this(header);
		addLine(line);
	}

	public CSVData(Header header, Collection<Line> lines)
	{
		this(header);
		addLines(lines);
	}

	public Line addLine(String[] cols)
	{
		Line line = new Line(cols);
		m_lines.add(line);
		for (Index index : m_indexes)
		{
			if (index != null)
			{
				index.addLine(line);
			}
		}
		return line;
	}

	public Line addLine(Map<String,String> cols)
	{
		Line line = addEmptyLine();
		for (Map.Entry<String,String> entry : cols.entrySet())
		{
			line.setValue(entry.getKey(), entry.getValue());
		}
		for (Index index : m_indexes)
		{
			if (index != null)
			{
				index.addLine(line);
			}
		}
		return line;
	}

	public Line addLine(Line line)
	{
		return addLine(line.m_cols);
	}

	public void addLines(Collection<Line> lines)
	{
		if (lines != null)
		{
			for (Line l : lines)
			{
				addLine(l);
			}
		}
	}

	public Line addEmptyLine()
	{
		Line line = new Line();
		m_lines.add(line);
		return line;
	}

	public boolean removeLine(Line line)
	{
		if (line != null)
		{
			if (m_lines.remove(line))
			{
				for (Index index : m_indexes)
				{
					if (index != null)
					{
						index.removeLine(line);
					}
				}
				return true;
			}
		}
		return false;
	}

	public Header getHeader()
	{
		return m_header;
	}

	public List<Line> getLines()
	{
		return Collections.unmodifiableList(m_lines);
	}

	public int getColCount()
	{
		return m_header.getColCount();
	}

	public int getLineCount()
	{
		return m_lines.size();
	}

	public boolean isEmpty()
	{
		return m_lines.isEmpty();
	}

	public void sort(SortCriteria sortCriteria)
	{
		Collections.sort(m_lines, new LineComparator(sortCriteria));
	}

	public void addIndex(String header)
	{
		int headerIndex = m_header.getColIndex(header);
		if (headerIndex >= 0 && m_indexes[headerIndex] == null)
		{
			m_indexes[headerIndex] = new Index(headerIndex);
		}
	}

	public void reindex()
	{
		for (int i = 0; i < m_indexes.length; i++)
		{
			if (m_indexes[i] != null)
			{
				m_indexes[i] = new Index(i);
			}
		}
	}

	public boolean hasMatch(String header, String value)
	{
		return (findFirstMatch(header, value) != null);
	}

	public Line findFirstMatch(String header, String value)
	{
		List<Line> lines = findMatchingLines(header, value, 0, 1);
		return (lines != null && !lines.isEmpty()) ? lines.get(0) : null;
	}

	public String findFirstMatchJoinColumnValue(String header, String value, String joinColumn)
	{
		Line match = findFirstMatch(header, value);
		return (match != null) ? match.getColValue(joinColumn) : null;
	}

	public int findFirstMatchJoinIntColumnValue(String header, String value, String joinColumn, int defValue)
	{
		Line match = findFirstMatch(header, value);
		return (match != null) ? match.getIntColValue(joinColumn, defValue) : defValue;
	}

	public List<Line> findMatchingLines(String header, String value)
	{
		return findMatchingLines(header, value, 0, 0);
	}

	public List<Line> findMatchingLines(String header, String value, int start, int num)
	{
		if (value != null)
		{
			if (start < 0)
				start = 0;
			int headerIndex = m_header.getColIndex(header);
			if (headerIndex >= 0)
			{
				// check if we have an index in place
				if (m_indexes[headerIndex] != null)
				{
					List<Line> lines = m_indexes[headerIndex].findMatches(value);
					if (start <= 0 && num <= 0)
						return new ArrayList<Line>(lines);
					if (start < lines.size())
					{
						return new ArrayList<Line>((num <= 0) ? lines.subList(start, lines.size()) : lines.subList(start, Math.min(lines.size(), start + num)));
					}
				}
				else
				{
					List<Line> lines = new ArrayList<Line>();
					for (Line line : m_lines)
					{
						if (value.equalsIgnoreCase(line.getColValue(headerIndex)))
						{
							lines.add(line);
							if (num > 0 && lines.size() >= start + num)
								break;
						}
					}
					return new ArrayList<Line>((start <= 0) ? lines : lines.subList(start, lines.size()));
				}
			}
		}
		return new LinkedList<Line>();
	}

	public boolean hasMatch(Pair<String,String> ... criteria)
	{
		return (findFirstMatch(criteria) != null);
	}

	public Line findFirstMatch(Pair<String,String> ... criteria)
	{
		List<Line> lines = findMatchingLines(criteria, 0, 1);
		return (lines != null && !lines.isEmpty()) ? lines.get(0) : null;
	}

	public int countMatchingLines(Pair<String,String> ... criteria)
	{
		return findMatchingLines(criteria, 0, 0).size();
	}

	public List<Line> findMatchingLines(Pair<String,String> ... criteria)
	{
		return findMatchingLines(criteria, 0, 0);
	}

	public List<Line> findMatchingLines(Pair<String,String>[] criteria, int start, int num)
	{
		// try the easy way out
		if (criteria == null || criteria.length == 0)
			throw new IllegalArgumentException("Filter criteria required");
		if (criteria.length == 1)
			return findMatchingLines(criteria[0].getA(), criteria[0].getB(), start, num);

		// get all results for the initial criteria
		if (start < 0)
			start = 0;
		List<Line> lines = null;
		for (Pair<String,String> crit : criteria)
		{
			if (lines == null)
			{
				lines = findMatchingLines(crit.getA(), crit.getB());
			}
			else
			{
				int headerIndex = m_header.getColIndex(crit.getA());
				if (headerIndex >= 0)
				{
					List<Line> tempLines = new ArrayList<Line>(lines.size());
					for (Line line : lines)
					{
						if (crit.getB().equalsIgnoreCase(line.getColValue(headerIndex)))
						{
							tempLines.add(line);
						}
					}
					lines = tempLines;
				}
			}
		}
		if (lines == null)
			return new LinkedList<Line>();
		if (start <= 0)
			return (num > 0 && lines.size() > num) ? lines.subList(0, num) : lines;
		if (num <= 0 || lines.size() < start + num)
			return lines.subList(start, lines.size());
		return lines.subList(start, start + num);
	}

	public int removeMatchingLines(Pair<String,String> ... criteria)
	{
		int count = 0;
		List<Line> lines = findMatchingLines(criteria);
		if (lines.size() > 1)
		{
			Collections.reverse(lines);
		}
		for (Line line : lines)
		{
			if (removeLine(line))
				count++;
		}
		return count;
	}

	public List<Line> findLowestLines(String header)
	{
		int headerIndex = m_header.getColIndex(header);
		if (headerIndex >= 0)
		{
			// check if we have an index in place
			if (m_indexes[headerIndex] != null)
			{
				return m_indexes[headerIndex].findFirst();
			}
			else
			{
				String lowestValue = null;
				List<Line> list = new LinkedList<Line>();
				for (Line line : m_lines)
				{
					String value = line.getColValue(headerIndex);
					if (!isEmpty(value))
					{
						if (lowestValue == null)
						{
							lowestValue = value;
							list.add(line);
						}
						else
						{
							int compValue = lowestValue.compareToIgnoreCase(value);
							if (compValue == 0)
							{
								list.add(line);
							}
							else if (compValue > 0)
							{
								lowestValue = value;
								list.clear();
								list.add(line);
							}
						}
					}
				}
				return list;
			}
		}
		return null;
	}

	public List<Line> findHighestLines(String header)
	{
		int headerIndex = m_header.getColIndex(header);
		if (headerIndex >= 0)
		{
			// check if we have an index in place
			if (m_indexes[headerIndex] != null)
			{
				return m_indexes[headerIndex].findLast();
			}
			else
			{
				String highestValue = null;
				List<Line> list = new LinkedList<Line>();
				for (Line line : m_lines)
				{
					String value = line.getColValue(headerIndex);
					if (!isEmpty(value))
					{
						if (highestValue == null)
						{
							highestValue = value;
							list.add(line);
						}
						else
						{
							int compValue = highestValue.compareToIgnoreCase(value);
							if (compValue == 0)
							{
								list.add(line);
							}
							else if (compValue < 0)
							{
								highestValue = value;
								list.clear();
								list.add(line);
							}
						}
					}
				}
				return list;
			}
		}
		return null;
	}

	public void appendData(CSVData data)
	{
		for (Line externalLine : data.getLines())
		{
			Line newLine = addEmptyLine();
			for (int i = 0; i < getColCount(); i++)
			{
				newLine.setValue(i, externalLine.getColValue(getHeader().getColName(i)));
			}
		}
	}

	public byte[] toBytes()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new CSVWriter(baos).write(this, true);
		return baos.toByteArray();
	}

	private static boolean isEmpty(String str)
	{
		return (str == null || str.length() == 0 || str.trim().length() == 0);
	}

	private static boolean parseBoolean(String str, boolean defValue)
	{
		if (isEmpty(str))
			return defValue;
		if ("1".equals(str) || "T".equalsIgnoreCase(str) || "Y".equalsIgnoreCase(str) || "TRUE".equalsIgnoreCase(str) || "YES".equalsIgnoreCase(str) || "1.0".equalsIgnoreCase(str))
			return true;
		if ("0".equals(str) || "F".equalsIgnoreCase(str) || "N".equalsIgnoreCase(str) || "FALSE".equalsIgnoreCase(str) || "NO".equalsIgnoreCase(str) || "NULL".equalsIgnoreCase(str) || "0.0".equalsIgnoreCase(str))
			return false;
		return defValue;
	}

	public class Header
	{
		private final String[] m_cols;
		private final Map<String,Integer> m_indexLookup;

		private Header(String[] cols)
		{
			m_cols = Arrays.copyOf(cols, cols.length);
			m_indexLookup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			for (int i = 0; i < m_cols.length; i++)
			{
				if (m_cols[i] == null)
					m_cols[i] = "";
				m_cols[i] = m_cols[i].trim();
				if (!isEmpty(m_cols[i]) && !m_indexLookup.containsKey(m_cols[i]))
					m_indexLookup.put(m_cols[i], i);
			}
		}

		private Header(List<String> cols)
		{
			this(cols.toArray(new String[cols.size()]));
		}

		public String getColName(int index)
		{
			return m_cols[index];
		}

		public int getColIndex(String key)
		{
			Integer val = m_indexLookup.get(key);
			return (val != null) ? val.intValue() : -1;
		}

		public boolean containsCol(String key)
		{
			return m_indexLookup.containsKey(key);
		}

		public boolean containsAllColumns(String ... keys)
		{
			if (keys != null)
			{
				for (String key : keys)
				{
					if (!containsCol(key))
						return false;
				}
			}
			return true;
		}

		public boolean containsAllColumns(List<String> keys)
		{
			if (keys != null)
			{
				for (String key : keys)
				{
					if (!containsCol(key))
						return false;
				}
			}
			return true;
		}

		public List<String> getNames()
		{
			return Arrays.asList(m_cols);
		}

		public String[] getNameArr()
		{
			return m_cols;
		}

		public int getColCount()
		{
			return m_cols.length;
		}

		@Override
		public String toString()
		{
			StringBuilder str = new StringBuilder();
			for (int i = 0; i < m_cols.length; i++)
			{
				if (i > 0)
					str.append(",");
				str.append("\"" + m_cols[i] + "\"");
			}
			return str.toString();
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			for (String str : m_cols)
			{
				if (str != null)
					result = prime * result + str.toLowerCase().hashCode();
			}
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Header other = (Header)obj;
			if (m_cols.length != other.m_cols.length)
				return false;
			for (int i = 0; i < m_cols.length; i++)
			{
				if ((m_cols[i] == null && other.m_cols[i] != null) || (m_cols[i] != null && other.m_cols[i] == null))
					return false;
				if (m_cols[i] != null && !m_cols[i].equalsIgnoreCase(other.m_cols[i]))
					return false;
			}
			return true;
		}
	}

	public class Line
	{
		private final String[] m_cols;

		private Line()
		{
			this(new String[] {});
		}

		private Line(String[] cols)
		{
			m_cols = Arrays.copyOf(cols, m_header.getColCount());
			for (int i = 0; i < m_cols.length; i++)
			{
				if (m_cols[i] == null)
					m_cols[i] = "";
			}
		}

		public CSVData getCSV()
		{
			return (CSVData.this);
		}

		public Line createCopy()
		{
			return new Line(m_cols);
		}

		public String[] getColumns()
		{
			return m_cols;
		}

		public String getColValue(int index)
		{
			return (m_cols[index] != null) ? m_cols[index].trim() : "";
		}

		public String getColValue(String key)
		{
			int index = m_header.getColIndex(key);
			return (index >= 0) ? getColValue(index) : null;
		}

		public String getColValue(String key, String defValue)
		{
			String val = getColValue(key);
			return (CSVData.isEmpty(val)) ? defValue : val;
		}

		public int getIntColValue(String key) throws NumberFormatException
		{
			try
			{
				return Integer.parseInt(getColValue(key));
			}
			catch (Exception e)
			{
				throw new NumberFormatException("Key='" + key + "' - " + e.getMessage());
			}
		}

		public int getIntColValue(String key, int defValue)
		{
			try
			{
				return getIntColValue(key);
			}
			catch (Exception e)
			{
				return defValue;
			}
		}

		public float getFloatColValue(String key) throws NumberFormatException
		{
			try
			{
				return Float.parseFloat(getColValue(key));
			}
			catch (Exception e)
			{
				throw new NumberFormatException("Key='" + key + "' - " + e.getMessage());
			}
		}

		public float getFloatColValue(String key, float defValue)
		{
			try
			{
				return getFloatColValue(key);
			}
			catch (Exception e)
			{
				return defValue;
			}
		}

		public long getLongColValue(String key) throws NumberFormatException
		{
			try
			{
				return Long.parseLong(getColValue(key));
			}
			catch (Exception e)
			{
				throw new NumberFormatException("Key='" + key + "' - " + e.getMessage());
			}
		}

		public long getLongColValue(String key, long defValue)
		{
			try
			{
				return getLongColValue(key);
			}
			catch (Exception e)
			{
				return defValue;
			}
		}

		public double getDoubleColValue(String key) throws NumberFormatException
		{
			try
			{
				return Double.parseDouble(getColValue(key));
			}
			catch (Exception e)
			{
				throw new NumberFormatException("Key='" + key + "' - " + e.getMessage());
			}
		}

		public double getDoubleColValue(String key, double defValue)
		{
			try
			{
				return getDoubleColValue(key);
			}
			catch (Exception e)
			{
				return defValue;
			}
		}

		public double getDoubleNonZeroColValue(String key, double defValue)
		{
			double val = getDoubleColValue(key, defValue);
			if (Double.isNaN(val) || val == 0.0)
				return defValue;
			return val;
		}

		public boolean getBooleanColValue(String key)
		{
			return getBooleanColValue(key, false);
		}

		public boolean getBooleanColValue(String key, boolean defValue)
		{
			String val = getColValue(key);
			return (CSVData.isEmpty(val)) ? defValue : parseBoolean(val, defValue);
		}

		public int getColCount()
		{
			return m_header.getColCount();
		}

		public boolean isEmpty()
		{
			for (String cell : m_cols)
			{
				if (!CSVData.isEmpty(cell))
					return false;
			}
			return true;
		}

		public String getColName(int index)
		{
			return m_header.getColName(index);
		}

		public Header getHeader()
		{
			return m_header;
		}

		public List<String> getValues()
		{
			return Arrays.asList(m_cols);
		}

		public void setValue(int index, String value)
		{
			m_cols[index] = (value != null) ? value : "";
			Index indexObj = m_indexes[index];
			if (indexObj != null)
			{
				indexObj.removeLine(this);
				indexObj.addLine(this);
			}
		}

		public boolean setValue(String key, String value)
		{
			int index = m_header.getColIndex(key);
			if (index >= 0)
			{
				setValue(index, value);
				return true;
			}
			return false;
		}

		@Override
		public String toString()
		{
			StringBuilder str = new StringBuilder();
			for (int i = 0; i < m_cols.length; i++)
			{
				if (i > 0)
					str.append(",");
				str.append(m_header.getColName(i) + "=\"" + m_cols[i] + "\"");
			}
			return str.toString();
		}
	}

	public static class SortCriteria
	{
		private final String m_colName;
		private final SortType m_type;
		private final boolean m_asc;
		private final Object m_args;
		private final SortCriteria m_nextCriteria;
		private int m_colIndex;

		public SortCriteria(String colName, SortType type, boolean asc)
		{
			this(colName, type, asc, null);
		}

		public SortCriteria(String colName, SortType type, boolean asc, SortCriteria nextCriteria)
		{
			this(colName, type, asc, null, nextCriteria);
		}

		public SortCriteria(String colName, SortType type, boolean asc, Object args, SortCriteria nextCriteria)
		{
			m_colName = colName;
			m_type = type;
			m_asc = asc;
			m_args = args;
			m_nextCriteria = nextCriteria;
			m_colIndex = -1;
		}

		public SortCriteria(Header header, int colIdx, SortType type, boolean asc)
		{
			this(header.getColName(colIdx), type, asc, null);
		}

		public SortCriteria(Header header, int colIdx, SortType type, boolean asc, SortCriteria nextCriteria)
		{
			this(header.getColName(colIdx), type, asc, null, nextCriteria);
		}

		public SortCriteria(Header header, int colIdx, SortType type, boolean asc, Object args, SortCriteria nextCriteria)
		{
			this(header.getColName(colIdx), type, asc, args, nextCriteria);
		}

		public SortCriteria append(SortCriteria lastCriteria)
		{
			if (lastCriteria == null)
				return this;
			if (m_nextCriteria == null)
				return new SortCriteria(m_colName, m_type, m_asc, m_args, lastCriteria);
			return new SortCriteria(m_colName, m_type, m_asc, m_args, m_nextCriteria.append(lastCriteria));
		}

		public int compare(Line la, Line lb)
		{
			// init col index
			if (m_colIndex < 0)
				m_colIndex = la.getHeader().getColIndex(m_colName);

			// compare strings
			String a = la.getColValue(m_colIndex);
			String b = lb.getColValue(m_colIndex);
			if (isEmpty(a) && isEmpty(b))
				return 0;
			if (isEmpty(a))
				return m_asc ? 1 : -1;
			if (isEmpty(b))
				return m_asc ? -1 : 1;
			switch (m_type)
			{
				case STRING:
				{
					if (a.compareToIgnoreCase(b) == 0)
						break;
					return m_asc ? a.compareToIgnoreCase(b) : b.compareToIgnoreCase(a);
				}
				case NUMBER:
				{
					double an;
					try { an = Double.parseDouble(a); } catch (Exception e) { an = 0.0; }
					double bn;
					try { bn = Double.parseDouble(b); } catch (Exception e) { bn = 0.0; }
					if (an < bn)
						return m_asc ? -1 : 1;
					if (an > bn)
						return m_asc ? 1 : -1;
					break;
				}
			}
			if (m_nextCriteria != null)
			{
				return m_nextCriteria.compare(la, lb);
			}
			return 0;
		}
	}

	private class Index
	{
		private final int m_headerIndex;
		private final NavigableMap<String,List<Line>> m_values;

		public Index(int headerIndex)
		{
			m_headerIndex = headerIndex;
			m_values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			for (Line line : m_lines)
			{
				String value = line.getColValue(headerIndex);
				List<Line> matches = m_values.get(value);
				if (matches == null)
				{
					matches = new LinkedList<Line>();
					m_values.put(value, matches);
				}
				matches.add(line);
			}
		}

		public List<Line> findMatches(String value)
		{
			List<Line> matches = m_values.get(value);
			return (matches != null) ? matches : new LinkedList<Line>();
		}

		public void addLine(Line line)
		{
			String value = line.getColValue(m_headerIndex);
			List<Line> matches = m_values.get(value);
			if (matches == null)
			{
				matches = new LinkedList<Line>();
				m_values.put(value, matches);
			}
			matches.add(line);
		}

		public void removeLine(Line line)
		{
			String value = line.getColValue(m_headerIndex);
			List<Line> matches = m_values.get(value);
			if (matches != null)
			{
				matches.remove(line);
			}
		}

		public List<Line> findFirst()
		{
			return !m_values.isEmpty() ? m_values.firstEntry().getValue() : null;
		}

		public List<Line> findLast()
		{
			return !m_values.isEmpty() ? m_values.lastEntry().getValue() : null;
		}
	}

	private static class LineComparator implements Comparator<Line>
	{
		private final SortCriteria m_criteria;

		public LineComparator(SortCriteria criteria)
		{
			m_criteria = criteria;
		}

		public int compare(Line o1, Line o2)
		{
			return m_criteria.compare(o1, o2);
		}
	}
}
