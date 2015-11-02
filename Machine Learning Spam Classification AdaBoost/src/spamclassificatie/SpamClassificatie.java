package spamclassificatie;
import java.util.*;
import java.lang.*;
import java.io.*;

public class SpamClassificatie {

    public static void main(String[] args) 
    {
    	// TODO Some comments
        SpamClassificatie main = new SpamClassificatie();
        List<Mail> allData = new ArrayList<>();
        
        main.LoadFile("spambase.txt", allData);

        // number of datasets, used for crossvalidation
		int numberOfDataSets = 10;
		
		// value expressing the number of hypotheses learned by the AdaBoost algorithm
		// startvalue
		int valueOfM = 180;
		// exclusive limit
		int valueOfM_limit = 280;
		// incrementing with
		int valueOfM_incrementer = 10;
        
		// split the dataset into numberOfDataSets randomized independent datasets with
		// size = |dataset| / numberOfDataSets
        List<List<Mail>> datasets = splitDataSet(allData, numberOfDataSets);
        
        // start the timer
        long startTime = System.currentTimeMillis();
        
        class DataSet 
    	{
    		private List<Mail> dataSet, validationSet;
    		
    		public DataSet(List<Mail> dataSet, List<Mail> validationSet)
    		{
    			this.dataSet = dataSet;
    			this.validationSet = validationSet;
    		}
    	}
    	
        /**
         * TODO Het is echt veel sneller om de AdaBoost algoritmes te trainen op m data, dan
         * valideren, dan x nieuwe hypotheses erbij leren, dan weer valideren en dan de errors
         * naar een lijst schrijven die je uiteindelijk deelt door max - min / increment
         */
        
        // generate and cross-validate
        List<DataSet> new_datasets = new ArrayList<DataSet>(numberOfDataSets);
		for (int i = 0;i < numberOfDataSets;i++) 
		{
			List<Mail> currentDataSet = new ArrayList<Mail>();
			List<Mail> validationSet = datasets.get(i);
			for (int a = 0; a < numberOfDataSets; a++)
				if (a!= i) currentDataSet.addAll(datasets.get(a));
			new_datasets.add(new DataSet(currentDataSet, validationSet));
		}
		
		DataPair[][] sortedData = sortDatasetInVariables(allData);
		
		for (; valueOfM < valueOfM_limit; valueOfM += valueOfM_incrementer)
        {
        	double error_total = 0;
        	int max_iterations = 5;
        	for (int iterations = 0; iterations < max_iterations; iterations++)
        	{
        		for (int i = 0; i < numberOfDataSets; i++)
        		
        		{
        			List<Mail> currentDataSet = new_datasets.get(i).dataSet;
        			List<Mail> validationSet = new_datasets.get(i).validationSet;
        			AdaBoost adaBoost = new AdaBoost(valueOfM,currentDataSet, sortedData);
        			double error = 0;
        			for (int a = 0; a < validationSet.size(); a++)
        			{
        				error += adaBoost.classify(validationSet.get(a)) == validationSet.get(a).y ? 0 : 1;
        			}
        			error /= validationSet.size();
        			error_total += error;
        		}
        	}
        	long currentTime_seconds = (System.currentTimeMillis() - startTime) / 1000;
        	System.out.println(currentTime_seconds + "s:\tm = " + valueOfM +
        			"\t| error: " + error_total/(numberOfDataSets*max_iterations));
        }
        //main.PrintList(allData);  
    }
    
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
    
    private static DataPair[][] sortDatasetInVariables(List<Mail> allData)
	{
		// TODO Comment
		DataPair[][] sortedData = new DataPair[allData.get(0).x.length][];
		
		for (int i = 0; i < sortedData.length; i++)
			sortedData[i] = new DataPair[allData.size()];
		
		for (int dataPoint = 0; dataPoint < allData.size(); dataPoint++)
		{
			for (int var = 0; var < sortedData.length; var++)
			{
				sortedData[var][dataPoint] = new DataPair(dataPoint, allData.get(dataPoint).x[var]);
			}
		}
		// sort every variable array
		for (int variable_index = 0; variable_index < sortedData.length; variable_index++)
			Arrays.sort(sortedData[variable_index]);
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
