package spamclassificatie;

public class DataPair implements Comparable<DataPair>
{

	public int x_index;
	public double value;
	
	public DataPair(int dataPoint, double d)
	{
		x_index = dataPoint;
		value = d;
		
	}

	@Override
	public int compareTo(DataPair o)
	{
		if (value == o.value) return 0;
		else return value < o.value ? -1 : 1;
	}

	public static void main(String ... args)
	{
		DataPair[] testCase = new DataPair[] {
			new DataPair(0,0.2),
			new DataPair(1,0.1),
			new DataPair(2,5.0),
			new DataPair(3,3.2)
		};
		java.util.Arrays.sort(testCase);
		for (DataPair data : testCase)
			System.out.println(data.x_index + " " + data.value);
	}
	
}
