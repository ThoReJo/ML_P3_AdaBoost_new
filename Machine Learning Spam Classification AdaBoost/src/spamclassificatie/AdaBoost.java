package spamclassificatie;

import java.util.*;

public class AdaBoost 
{
	// the data set as a list of Mail-objects TODO Change comments
	public List<SortedDataSet> datasets;
	// the hypothesis set
	private BaseLearner[] hypotheses;
	// the weights on the hypotheses
	private double[] alphas;
	
	// constructor for an AdaBoost algorithm, consisting of m hypotheses which train on
	// the dataset
	public AdaBoost(int numberOfHypotheses, List<SortedDataSet> datasets)
	{
		/*
		 * Initialize data set, hypothesis set, weights on data points and weights on hypotheses (alphas). 
		 */
		this.datasets = datasets;
		hypotheses = new BaseLearner[numberOfHypotheses];
		resetWeights();
		alphas = new double[numberOfHypotheses];
		
		/*
		 * Train m hypotheses according to the scaled AdaBoost algorithm.
		 */
		for (int hypothesis_index = 0; hypothesis_index < numberOfHypotheses; hypothesis_index++)
		{
			// choose a random feature <- {0,56}
			int feature = (int) (Math.random() * 57.0);
			// initialize hypothesis
			hypotheses[hypothesis_index] = new BaseLearner(datasets.get(feature), feature, this);
			// calculate the error (cumulative weight of all misclassified points/ total weight)
			double error = calculateError(hypothesis_index);
			// calculate the alpha value for this hypothesis (alpha <- 1/2 ln((1 - err)/err)
			alphas[hypothesis_index] = calculateAlpha(error);
			// update the weights and scale them to a cumulative 1
			updateAndScaleWeights(hypothesis_index);
		}
	}

	/**
	 * Method to reset all the weights associated with the mails in the dataset.
	 */
	private void resetWeights()
	{
		// initialize the weights of every data point with value 1/N
		for (int i = 0; i < datasets.get(0).size(); i++)
			datasets.get(0).get(i).weight = 1.0/datasets.get(0).size();
	}

	private double calculateError(int m) 
	{
		double cumulativeError  = 0.0;
		// for every data point:
		for (int i = 0; i < datasets.get(0).size(); i++)
		{
			// if the hypothesis misclassifies the data point
			if (hypotheses[m].classify(datasets.get(0).get(i)) != datasets.get(0).y(i))
				// add the weight of the data point to the cumulative error
				cumulativeError += datasets.get(0).get(i).weight;
		}
		// return err_m <- total weight of the errors / total weight (total weight is 1)
		return cumulativeError;
	}

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
		BaseLearner hypothesis = hypotheses[m];
		// for readability, for this method the order doesn't matter, so we'll use the 1st list
		SortedDataSet dataset = datasets.get(0);
		
		// update all the weights: w_i <- w_i * e ^ (-y_i * g_m(x_i) * alpha_m)
		for (int i = 0; i < dataset.size(); i++)
		{
			// update w_i
			dataset.get(i).weight *= Math.exp
					(
							-dataset.y(i) *
							hypothesis.classify(dataset.get(i)) *
							alphas[m]
					);
			// add w_i to cumulative weights
			cumulativeWeights += dataset.get(i).weight;
		}
		// scale the weights in such a way that their sum is 1
		for (int i = 0; i < dataset.size(); i++)
			dataset.get(i).weight /= cumulativeWeights;
	}
	
	
	/**
	 * Classifies the e-mail as <b>spam</b> (+1) or <b>not spam</b> (-1) by 
	 * @param mail
	 * @return
	 */
	public int classify(Mail mail)
	{
		double cumulativeValue = 0.0;
		for (int index = 0; index < hypotheses.length; index++)
		{
			cumulativeValue += (alphas[index] * hypotheses[index].classify(mail));
		}
		// return -1 if the weighted classification is smaller than 0, and 1 otherwise
		return cumulativeValue < 0 ? -1 : 1;
	}
}
