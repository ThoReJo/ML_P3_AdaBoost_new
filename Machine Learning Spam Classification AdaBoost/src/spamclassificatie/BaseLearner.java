package spamclassificatie;

import java.util.List;

public class BaseLearner {

	// the modifier a <- {-1,1}
	private int a;
	// the index of the variable this base learner is training on i <- {0,1,...,56}
	private int var_index;
	// the threshhold b <- R
	private double b;
	// the sorted dataSet
	private DataPair[][] sortedData;
	
	// the adaBoost algorithm this Base Learner is part of
	AdaBoost parent;
	//
	private double[] weights;
	
	public BaseLearner(DataPair[][] sortedData, double[] weights, int variable, AdaBoost adaBoost) {
		a = (int) (b = 0);
		var_index = variable;
		this.sortedData = sortedData;
		this.weights = weights;
		parent = adaBoost;
		// TODO Auto-generated constructor stub
		train();
		
	}
	// TODO

	private void train()
	{
		// the cumulative weight of all spam
		double w_plus_all = 0;
		// the cumulative weight of all normal e-mail
		double w_minus_all = 0;
		
		//
		DataPair[] current_feature = sortedData[var_index];
		// calculate w_- and w_+
		for (int i = 0; i < current_feature.length; i++)
		{
			if (parent.dataset.get(current_feature[i].x_index).y < 0)
				w_minus_all += weights[current_feature[i].x_index];
			else
				w_plus_all += weights[current_feature[i].x_index];
		}
		
		// TODO
		double w_minus_A = 0;
		double w_plus_A = 0;
		// TODO
		double classification = 0;
		double e = 0;
		
		// the variables to store the best e yet
		double e_min = Double.MAX_VALUE;
		int x_min = 0;
		double classification_min = 0;
		
		
		// 
		for (int i = 0; i < current_feature.length; i++)
		{
			if (parent.dataset.get(current_feature[i].x_index).y < 0)
				w_minus_A += weights[current_feature[i].x_index];
			else
				w_plus_A += weights[current_feature[i].x_index];
			// add the weighted classification to the cumulative classification
			classification += weights[current_feature[i].x_index] * parent.dataset.get(current_feature[i].x_index).y;
			
			double w_minus_B = w_minus_all - w_minus_A;
			double w_plus_B = w_plus_all - w_plus_A;
			double p_B = (w_minus_B + w_plus_B) == 0 ? 0 : w_plus_B / (w_minus_B + w_plus_B);
			double e_B = 2*p_B * (1 - p_B);
			double p_A = (w_minus_A + w_plus_A) == 0 ? 0 : w_plus_A / (w_minus_A + w_plus_A);
			double e_A = 2*p_A * (1 - p_A);
			
			e = ((w_plus_B + w_minus_B) * e_B) + ((w_plus_A + w_minus_A) * e_A);
			
			if (e < e_min)
			{
				e_min = e;
				// TODO checken!
				// TODO hier kan ws. performance gewonnen worden, kijken of we de hele lijst 
				// door moeten
				
				x_min = i;
				classification_min = classification;
				//System.out.println("i = "+ i + ", e = " + e + ", x_min = " + x_min + ", min = " + current_feature[0].value + ", max = " + current_feature[current_feature.length-1].value);
			}
		}
		//
		a = classification_min < 0 ? -1 : 1;
		b = current_feature[x_min].value;
	}

	/**
	 * Classifies the mail as spam or not-spam
	 * @param mail
	 * @return +1 (spam) or -1 (not-spam)
	 */
	public int classify(Mail mail) {
		// h(x) = a * sign(x_i - b)
		return a * (mail.x[var_index] - b <= 0 ? -1 : 1);
	}
}
