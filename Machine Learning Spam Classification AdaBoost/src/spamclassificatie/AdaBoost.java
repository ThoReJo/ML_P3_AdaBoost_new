package spamclassificatie;

import java.util.*;

/**
 * @author Tom Steensma 3121674
 */
public class AdaBoost 
{
	// the data set as a list of Mail-objects
	public List<Mail> dataset;
	// the hypothesis set
	private List<BaseLearner> hypotheses;
	// the weights on the data points
	private List<Double> weights;
	// the weights on the hypotheses
	//private double[] alphas;
	
	private DataPair[][] sortedData;
	
	// constructor for an AdaBoost algorithm, consisting of m hypotheses which train on
	// the dataset
	public AdaBoost(int numberOfHypotheses, List<Mail> dataset, DataPair[][] sortedData)
	{
		/*
		 * Initialize data set, hypothesis set, the weights of the mails and the sorted data set.
		 */
		this.dataset = dataset;
		hypotheses = new ArrayList<BaseLearner>(numberOfHypotheses);
		weights = new ArrayList<Double>(dataset.size());
		// initialize the weights on data points with value 1/N
		for (int i = 0; i < dataset.size(); i++)
			weights.add(1.0/dataset.size());
		this.sortedData = sortedData;
		
		/*
		 * Train m hypotheses according to the scaled AdaBoost algorithm.
		 */
		train(numberOfHypotheses);
	}
	
	/**
	 * Expands this AdaBoost algorithm with numberOfNewHypotheses hypotheses.
	 * @param numberOfNewHypotheses
	 */
	public void train(int numberOfNewHypotheses)
	{
		// this is needed, because the algorithm can be trained after initialization
		int limit = hypotheses.size() + numberOfNewHypotheses;
		// add new hypotheses
		for (int hypothesis_index = hypotheses.size(); hypothesis_index < limit; hypothesis_index++)
		{
			// choose a random feature <- {0,56}
			int feature = (int) (Math.random() * 57.0);
			// initialize hypothesis
			hypotheses.add(new BaseLearner(sortedData, weights, feature, this));
			// calculate the error (cumulative weight of all misclassified points/ total weight)
			double error = calculateError(hypothesis_index);
			// calculate the alpha value for this hypothesis (alpha <- 1/2 ln((1 - err)/err)
			hypotheses.get(hypothesis_index).alpha = calculateAlpha(error);
			// update the weights and scale them to a cumulative 1
			updateAndScaleWeights(hypothesis_index);
		}
	}

	/**
	 * Calculates the classification error of this BaseLearner.
	 * @param m the BaseLearner
	 * @return the classification error
	 */
	private double calculateError(int m) 
	{
		double cumulativeError  = 0.0;
		// for every data point:
		for (int i = 0; i < dataset.size(); i++)
		{
			// if the hypothesis misclassifies the data point
			if (hypotheses.get(m).classify(dataset.get(i)) != dataset.get(i).y)
				// add the weight of the data point to the cumulative error
				cumulativeError += weights.get(i);
		}
		// return err_m <- total weight of the errors / total weight (total weight is 1)
		return cumulativeError;
	}

	/**
	 * Calculates the relative importance of a BaseLearner based on its classification error.
	 * @param error classification error of the BaseLearner
	 * @return the alpha for this BaseLearner
	 */
	private double calculateAlpha(double error) 
	{
		// return 1/2 ln((1 - err)/err)
		return 0.5 * Math.log((1 - error)/error);
	}

	/**
	 * Updates and scales the weights in respect to the learned hypothesis <b>g_m</b>. The
	 * weights are updated using the scaled AdaBoost algorithm, where we focus our next
	 * hypothesis on this hypothesis' worst-predicted points, i.e. we change our weights in such
	 * a way that <b>g_m+1</b> will focus on that part of the input space where <b>g_m</b> 
	 * doesn't perform well.<br />
	 * Furthermore, based on how well <b>g_m</b> performs on the data, a number <b>alpha</b> is
	 * calculated. Lastly, the new weights are scaled in such a way that their cumulative value
	 * is 1.
	 * @param m index of the hypothesis in the hypothesis set
	 */
	private void updateAndScaleWeights(int m) 
	{
		// variable used to scale the weights back to a cumulative 1 after the update
		double cumulativeWeights = 0.0;
		// call the current hypothesis g:
		BaseLearner hypothesis = hypotheses.get(m);
		
		// update all the weights: w_i <- w_i * e ^ (-y_i * g_m(x_i) * alpha_m)
		for (int i = 0; i < weights.size(); i++)
		{
			// update w_i
			weights.set(i, weights.get(i) * Math.exp
					(
							-dataset.get(i).y *
							hypothesis.classify(dataset.get(i)) *
							hypotheses.get(m).alpha
					));
			// add w_i to cumulative weights
			cumulativeWeights += weights.get(i);
		}
		// scale the weights in such a way that their sum is 1
		for (int i = 0; i < weights.size(); i++)
			weights.set(i, weights.get(i) / cumulativeWeights);
	}
	
	/**
	 * Classifies an e-mail as <b>spam</b> or <b>not spam</b> by calculating the weighted 
	 * classification by this AdaBoosts' BaseLearners.
	 * @param mail the e-mail to classify
	 * @return +1 (spam) or -1 (not-spam)
	 */
	public int classify(Mail mail)
	{
		double cumulativeValue = 0.0;
		for (int index = 0; index < hypotheses.size(); index++)
		{
			cumulativeValue += (hypotheses.get(index).alpha * hypotheses.get(index).classify(mail));
		}
		// return -1 if the weighted classification is smaller than 0, and 1 otherwise
		return cumulativeValue < 0 ? -1 : 1;
	}
}
