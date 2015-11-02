package spamclassificatie;

/**
 * Class used to sort the data.
 * @author Tom Steensma 3121674
 */
public class DataPair implements Comparable<DataPair>
{
	// index of the mail in the original dataset
	public int x_index;
	// the value of the feature, used in the compare-method
	public double value;
	
	/**
	 * Constructor for a DataPair
	 * @param original_index the index of this mail in the original dataset
	 * @param d the value of the feature on which to sort
	 */
	public DataPair(int original_index, double d)
	{
		x_index = original_index;
		value = d;
	}

	@Override
	public int compareTo(DataPair o)
	{
		// sort based on the value of the feature
		if (value == o.value) return 0;
		else return value < o.value ? -1 : 1;
	}
}
