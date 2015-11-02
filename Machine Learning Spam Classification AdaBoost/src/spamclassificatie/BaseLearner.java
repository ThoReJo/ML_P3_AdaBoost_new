package spamclassificatie;

import java.util.List;

/**
 * @author Tom Steensma 3121674
 */
public class BaseLearner {

	// the modifier a <- {-1,1}
	private int a;
	// the index of the variable this base learner is training on i <- {0,1,...,56}
	private int feature;
	// the threshhold b <- R
	private double b;
	// the sorted dataSet
	private DataPair[][] sortedData;
	
	// the AdaBoost algorithm this BaseLearner is part of
	AdaBoost parent;
	// the weights associated with each e-mail
	private List<Double> weights;
	
	// the weight of this BaseLearner
	public double alpha;
	
	/**
	 * Constructor for a BaseLearner.
	 * @param sortedData a sorted version of the dataset
	 * @param weights the weights per e-mail
	 * @param feature the feature this BaseLearner is training on
	 * @param adaBoost the parent AdaBoost algoritm
	 */
	public BaseLearner(DataPair[][] sortedData, List<Double> weights, int feature, AdaBoost adaBoost) {
		// initialize fields
		a = (int) (b = 0);
		this.feature = feature;
		this.sortedData = sortedData;
		this.weights = weights;
		parent = adaBoost;
		alpha = 0;
		
		// train the BaseLearner by minimizing the Gini index.
		train();	
	}

	/**
	 * Train this BaseLearner using the Gini index
	 */
	private void train()
	{
		// the cumulative weight of all spam
		double w_plus_all = 0;
		// the cumulative weight of all normal e-mail
		double w_minus_all = 0;
		
		// for readability
		DataPair[] current_feature = sortedData[feature];
		// calculate w_- and w_+
		for (int i = 0; i < current_feature.length; i++)
		{
			// if non-spam
			if (parent.dataset.get(current_feature[i].x_index).y < 0)
				// add weight to non-spam total
				w_minus_all += weights.get(current_feature[i].x_index);
			else
				// else add weight to spam total
				w_plus_all += weights.get(current_feature[i].x_index);;
		}
		
		// the cumulative weights from the minimum to the current position.
		double w_minus_A = 0;
		double w_plus_A = 0;
		
		// this variable will be negative if the weighted classification of the mails up to the
		// current position are negative, positive otherwise, this gives a good prediction for
		// the value of a
		double classification = 0;
		
		// the variable to minimize
		double e = 0;
		
		// the variables to store the best e yet
		double e_min = Double.MAX_VALUE;
		int x_min = 0;
		double classification_min = 0;
		
		
		// iterate through all mails in the training set
		for (int current_mail = 0; current_mail < current_feature.length; current_mail++)
		{
			// if the mail is non-spam
			if (parent.dataset.get(current_feature[current_mail].x_index).y < 0)
				// add its weight to the cumulative weight of non-spam
				w_minus_A += weights.get(current_feature[current_mail].x_index);
			else
				// else add its weight to the cumulative weight of spam
				w_plus_A += weights.get(current_feature[current_mail].x_index);
			// add the weighted classification to the cumulative classification
			classification += weights.get(current_feature[current_mail].x_index) * parent.dataset.get(current_feature[current_mail].x_index).y;
			
			/* 
			 * Calculate the Gini index 
			 */
			// the weight of the opposite side is the total - the cumulative weight up to here
			double w_minus_B = w_minus_all - w_minus_A;
			double w_plus_B = w_plus_all - w_plus_A;
			// the formulas to calculate e
			double p_B = (w_minus_B + w_plus_B) == 0 ? 0 : w_plus_B / (w_minus_B + w_plus_B);
			double e_B = 2*p_B * (1 - p_B);
			double p_A = (w_minus_A + w_plus_A) == 0 ? 0 : w_plus_A / (w_minus_A + w_plus_A);
			double e_A = 2*p_A * (1 - p_A);
			
			// calculate e
			e = ((w_plus_B + w_minus_B) * e_B) + ((w_plus_A + w_minus_A) * e_A);
			
			// if e is the best e yet
			if (e < e_min)
			{
				// store the value of e in e_min
				e_min = e;
				
				// store the index of the e-mail
				x_min = current_mail;
				// store the weighted classification of part A
				classification_min = classification;
			}
		}
		
		// a can be approximated by using the weighted classification of part A
		a = classification_min < 0 ? -1 : 1;
		// b is the value of feature of the mail where e is the lowest
		b = current_feature[x_min].value;
	}
	

	/**
	 * Classifies the mail as spam or not-spam
	 * @param mail
	 * @return +1 (spam) or -1 (not-spam)
	 */
	public int classify(Mail mail) {
		// h(x) = a * sign(x_i - b)
		return a * (mail.x[feature] - b <= 0 ? -1 : 1);
	}
}
