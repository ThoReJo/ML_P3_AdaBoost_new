package spamclassificatie;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortedDataSet
{

	private int feature_index;
	private List<Mail> orderedList;
	
	/**
	 * Constructor for a sorted dataset, sorted by a feature.
	 * @param originalDataSet the dataset that will be sorted
	 * @param feature_index the index of the feature in x of the original dataset
	 */
	public SortedDataSet(List<Mail> originalDataSet, int feature_index)
	{
		this.feature_index = feature_index;
		// sort the original dataset based on the selected feature
		orderedList = sort(originalDataSet, feature_index);
	}

	/**
	 * Sorts a list of mails based on 1 of their features.
	 * @param originalList the original dataset
	 * @param feature_index the index of the feature in x
	 * @return a sorted list of mails based on the feature.
	 */
	public static List<Mail> sort(List<Mail> originalList, int feature_index)
	{
		// copy the original dataset
		List<Mail> sorted = new ArrayList<Mail>(originalList);
		// sort the set based on the specified feature
		sorted.sort(new Comparator<Mail>() {
			@Override
			public int compare(Mail o1, Mail o2)
			{
				// compare the mails based on their value for this feature
				if (o1.x[feature_index] == o2.x[feature_index]) return 0;
				else return o1.x[feature_index] < o2.x[feature_index] ? -1 : 1;
			}
		});
		// return the sorted list
		return sorted;
	}
	
	/**
	 * @param index_of_mail the index of the mail in this ordered dataset
	 * @return the mail at the index
	 */
	public Mail get(int index_of_mail)
	{
		return orderedList.get(index_of_mail);
	}
	
	/**
	 * Method to get the feature this list is sorted on
	 * @return the index of the feature in x
	 */
	public int getFeatureIndex()
	{
		return feature_index;
	}
	
	/**
	 * @param index_of_mail the index of the mail in this ordered list
	 * @return the value of this emails' feature 
	 */
	public double getFeatureValue(int index_of_mail)
	{
		return get(index_of_mail).x[feature_index];
	}
	
	/**
	 * @param index_of_mail the index of the mail in this ordered list
	 * @return the value of y for this mail
	 */
	public int y(int index_of_mail)
	{
		return get(index_of_mail).y;
	}

	/**
	 * @return the size of the ordered list
	 */
	public int size()
	{
		return orderedList.size();
	}
}
