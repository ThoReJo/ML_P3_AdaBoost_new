package spamclassificatie;
import java.util.*;
import java.io.*;

/**
 * @author Tom Steensma 3121674
 */
public class SpamClassificatie {

    public static void main(String[] args) 
    {
        SpamClassificatie main = new SpamClassificatie();
        List<Mail> allData = new ArrayList<>();
        
        main.LoadFile("spambase.txt", allData);

        // number of datasets, used for crossvalidation
		int numberOfDataSets = 100;
		
		// value expressing the number of hypotheses learned by the AdaBoost algorithm
		// -- startvalue
		int valueOfM = 10;
		// -- exclusive limit
		int valueOfM_limit = 1000;
		// -- incrementing with
		int valueOfM_incrementer = 10;
        
		// split the dataset into numberOfDataSets randomized independent datasets with
		// size = |dataset| / numberOfDataSets
        List<List<Mail>> datasets = splitDataSet(allData, numberOfDataSets);
        
        // start the timer
        long startTime = System.currentTimeMillis();
        
        /**
         * Class to store a combination of dataset and validationset
         */
        class DataSet 
    	{
    		private List<Mail> dataSet, validationSet;
    		
    		public DataSet(List<Mail> dataSet, List<Mail> validationSet)
    		{
    			this.dataSet = dataSet;
    			this.validationSet = validationSet;
    		}
    	}
        
        // generate the datasets (training + validation set)
        List<DataSet> new_datasets = new ArrayList<DataSet>(numberOfDataSets);
		for (int dataset_index = 0; dataset_index < numberOfDataSets; dataset_index++) 
		{
			List<Mail> currentDataSet = new ArrayList<Mail>();
			List<Mail> validationSet = datasets.get(dataset_index);
			// concatenate all datasets except the validation set into one training set
			for (int a = 0; a < numberOfDataSets; a++)
				if (a!= dataset_index) currentDataSet.addAll(datasets.get(a));
			// add the combination to new_datasets
			new_datasets.add(new DataSet(currentDataSet, validationSet));
		}
		
		// sort the data based on every value of x, a DataPair[] is a sorted list of e-mails
		// using one of its features as comparator, where sortedData[0] corresponds with the
		// the dataset sorted on the value of feature x[0].
		DataPair[][] sortedData = sortDatasetInVariables(allData);
		
		// generate all AdaBoosters (1 per dataset)
		AdaBoost[] boosters = new AdaBoost[numberOfDataSets];
		for (int i = 0; i < numberOfDataSets; i++)
		{
			List<Mail> currentDataSet = new_datasets.get(i).dataSet;
			boosters[i] = new AdaBoost(valueOfM, currentDataSet, sortedData);
		}
		
		// print the current parameters
		System.out.printf("Startvalue m = %d, incrementing with %d, stop when m >= %d\n"
				+ "Using %d-Fold Crossvalidation\n\n",
				valueOfM,valueOfM_incrementer,valueOfM_limit,numberOfDataSets);
		
		// increment the number of hypotheses learned by the AdaBoost algorithm and calculate
		// the validation-error at each m by taking the average error of the boosters.
		for (; valueOfM < valueOfM_limit; valueOfM += valueOfM_incrementer)
        {
			// average error of the AdaBoosters with this number of hypotheses
        	double error_total = 0;
        	
        	// iterate through all AdaBoosters
        	for (int i = 0; i < numberOfDataSets; i++)		
    		{
    			AdaBoost adaBoost = boosters[i];
    			List<Mail> validationSet = new_datasets.get(i).validationSet;
    			// the error of this AdaBoost instance for this number of hypotheses
    			double error = 0;
    			// calculate the validation error by calculating the classification error 
    			// on the validation set
    			for (int a = 0; a < validationSet.size(); a++)
    				error += adaBoost.classify(validationSet.get(a)) == validationSet.get(a).y ? 0 : 1;
    			// calculate the average error
    			error /= validationSet.size();
    			// add the average error to the total
    			error_total += error;
    			// let the algorithm train valueOfM_incrementer new hypotheses (for the next round)
    			adaBoost.train(valueOfM_incrementer);
    		}
        	
        	// time every cycle
        	long currentTime_seconds = (System.currentTimeMillis() - startTime) / 1000;
        	// print results to console
        	System.out.printf("%ds:\tm = %d\terror = %f\n",
        			currentTime_seconds,valueOfM,error_total/(numberOfDataSets));
        }
    }
    
    /**
     * Splits a Dataset into parts of size allData.size()/numberOfDataSets.
     * @param allData original dataset as a List<Mail>
     * @param numberOfDataSets number of datasets to split into
     * @return List<List<Mail>> with numberOfDataSets elements
     */
    private static List<List<Mail>> splitDataSet(List<Mail> allData, int numberOfDataSets)
	{
    	int mailsPerSet = allData.size()/numberOfDataSets;
    	List<List<Mail>> datasets = new ArrayList<List<Mail>>();
    	for (int dataset_index = 0; dataset_index < numberOfDataSets; dataset_index++)
    	{
    		List<Mail> dataset = new ArrayList<Mail>();
    		// repeat mailsPerSet times:
    		for (int i = 0; i < mailsPerSet; i++)
    		{
    			// pick a random mail from allData,
    			int randomMail_index = (int) Math.random() * allData.size();
    			// add it to the current dataset and
    			dataset.add(allData.get(randomMail_index));
    			// remove it from allData
    			allData.remove(randomMail_index);
    		}
    		// add this dataset to the set of datasets
    		datasets.add(dataset);
    	}
    	// if there are still mails left in allData (the number of mails modulo numberOfDataSets != 0)
    	if (!allData.isEmpty())
    		// add them to the first dataset in the set
    		datasets.get(0).addAll(allData);
     	// return the set of datasets
		return datasets;
	}
    
    /**
     * Sort the data based on every value of x, a DataPair[] is a sorted list of e-mails
	 * using one of its features as comparator, where sortedData[0] corresponds with the
	 * the dataset sorted on the value of feature x[0].
     * @param allData original dataset as a List<Mail>
     * @return DataPair[][] with all sorted instances of this dataset
     */
    private static DataPair[][] sortDatasetInVariables(List<Mail> allData)
	{
		// variable to store the sorted data
		DataPair[][] sortedData = new DataPair[allData.get(0).x.length][];
		// initialize array
		for (int feature = 0; feature < sortedData.length; feature++)
			sortedData[feature] = new DataPair[allData.size()];
		
		// create a DataPair for every mail + variable combination (57 combinations per mail)
		for (int mail = 0; mail < allData.size(); mail++)
			for (int feature = 0; feature < sortedData.length; feature++)
				sortedData[feature][mail] = new DataPair(mail, allData.get(mail).x[feature]);
		
		// sort every feature array by using Arrays.sort(), DataPair implements a compareTo
		// method to automatically sort on the right feature
		for (int feature = 0; feature < sortedData.length; feature++)
			Arrays.sort(sortedData[feature]);
		// return the sorted arrays
		return sortedData;
	}

    /** 
     * Method provided with assignment
     */
	public void PrintList(List<Mail> dataList)
    {
        for (Mail mail : dataList)
        {
	    String mailInfo = mail.toString();
            System.out.println(mailInfo);
        }        
    }
    
	/** 
     * Method provided with assignment
     */
    public void LoadFile(String filename, List<Mail> dataList)
    {
        File file = new File(filename);
        BufferedReader reader = null;
	
        try {
            reader = new BufferedReader(new FileReader(file));
            String text;
	    
            while ((text = reader.readLine()) != null) 
            {
            	Mail mail = new Mail();
            	mail.fromString(text);
                dataList.add(mail);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.out);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
    }
}
