package com.trumedia.project;

import java.util.Comparator;

public class Pair<AT,BT>
{
	public static Comparator<Pair<String,?>> A_STRING_ORDER = new AStringSort();
	public static Comparator<Pair<String,?>> A_STRING_ORDER_DESC = new AStringSortDesc();
	public static Comparator<Pair<Integer,?>> A_INT_ORDER = new AIntSort();
	public static Comparator<Pair<Integer,?>> A_INT_ORDER_DESC = new AIntSortDesc();
	public static Comparator<Pair<Double,?>> A_DOUBLE_ORDER = new ADoubleSort();
	public static Comparator<Pair<Double,?>> A_DOUBLE_ORDER_DESC = new ADoubleSortDesc();
	public static Comparator<Pair<?,String>> B_STRING_ORDER = new BStringSort();
	public static Comparator<Pair<?,String>> B_STRING_ORDER_DESC = new BStringSortDesc();
	public static Comparator<Pair<?,Integer>> B_INT_ORDER = new BIntSort();
	public static Comparator<Pair<?,Integer>> B_INT_ORDER_DESC = new BIntSortDesc();
	public static Comparator<Pair<?,Double>> B_DOUBLE_ORDER = new BDoubleSort();
	public static Comparator<Pair<?,Double>> B_DOUBLE_ORDER_DESC = new BDoubleSortDesc();

	private AT m_a;
	private BT m_b;

	public Pair(AT a, BT b)
	{
		m_a = a;
		m_b = b;
	}

	public AT getA()			{ return m_a; }
	public void setA(AT val)	{ m_a = val; }
	public BT getB()			{ return m_b; }
	public void setB(BT val)	{ m_b = val; }

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof Pair))
			return false;
		Pair<?,?> op = (Pair<?,?>)o;
		if ((m_a == null && op.m_a != null) || (m_a != null && op.m_a == null))
			return false;
		if (m_a != null && op.m_a != null && !m_a.equals(op.m_a))
			return false;
		if ((m_b == null && op.m_b != null) || (m_b != null && op.m_b == null))
			return false;
		if (m_b != null && op.m_b != null && !m_b.equals(op.m_b))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		String a = m_a != null ? String.valueOf(m_a.hashCode()) : "null";
		String b = m_b != null ? String.valueOf(m_b.hashCode()) : "null";
		return (a + "||" + b).hashCode();
	}

	@Override
	public String toString()
	{
		return "[" + String.valueOf(m_a) + "," + String.valueOf(m_b) + "]";
	}

	private static class AStringSort implements Comparator<Pair<String,?>>
	{
		public int compare(Pair<String,?> x, Pair<String,?> y)
		{
			return String.CASE_INSENSITIVE_ORDER.compare(x.getA(), y.getA());
		}
	}

	private static class AStringSortDesc implements Comparator<Pair<String,?>>
	{
		public int compare(Pair<String,?> x, Pair<String,?> y)
		{
			return String.CASE_INSENSITIVE_ORDER.compare(y.getA(), x.getA());
		}
	}

	private static class AIntSort implements Comparator<Pair<Integer,?>>
	{
		public int compare(Pair<Integer,?> x, Pair<Integer,?> y)
		{
			return x.getA() - y.getA();
		}
	}

	private static class AIntSortDesc implements Comparator<Pair<Integer,?>>
	{
		public int compare(Pair<Integer,?> x, Pair<Integer,?> y)
		{
			return y.getA() - x.getA();
		}
	}

	private static class ADoubleSort implements Comparator<Pair<Double,?>>
	{
		public int compare(Pair<Double,?> x, Pair<Double,?> y)
		{
			return Double.compare(x.getA(), y.getA());
		}
	}

	private static class ADoubleSortDesc implements Comparator<Pair<Double,?>>
	{
		public int compare(Pair<Double,?> x, Pair<Double,?> y)
		{
			return Double.compare(y.getA(), x.getA());
		}
	}

	private static class BStringSort implements Comparator<Pair<?,String>>
	{
		public int compare(Pair<?,String> x, Pair<?,String> y)
		{
			return String.CASE_INSENSITIVE_ORDER.compare(x.getB(), y.getB());
		}
	}

	private static class BStringSortDesc implements Comparator<Pair<?,String>>
	{
		public int compare(Pair<?,String> x, Pair<?,String> y)
		{
			return String.CASE_INSENSITIVE_ORDER.compare(y.getB(), x.getB());
		}
	}

	private static class BIntSort implements Comparator<Pair<?,Integer>>
	{
		public int compare(Pair<?,Integer> x, Pair<?,Integer> y)
		{
			return x.getB() - y.getB();
		}
	}

	private static class BIntSortDesc implements Comparator<Pair<?,Integer>>
	{
		public int compare(Pair<?,Integer> x, Pair<?,Integer> y)
		{
			return y.getB() - x.getB();
		}
	}

	private static class BDoubleSort implements Comparator<Pair<?,Double>>
	{
		public int compare(Pair<?,Double> x, Pair<?,Double> y)
		{
			return Double.compare(x.getB(), y.getB());
		}
	}

	private static class BDoubleSortDesc implements Comparator<Pair<?,Double>>
	{
		public int compare(Pair<?,Double> x, Pair<?,Double> y)
		{
			return Double.compare(y.getB(), x.getB());
		}
	}
}
