package spamclassificatie;

import java.util.*;

public class AdaBoost 
{
	// the data set as a list of Mail-objects
	public List<Mail> dataset;
	// the hypothesis set
	private BaseLearner[] hypotheses;
	// the weights on the data points
	private double[] weights;
	// the weights on the hypotheses
	private double[] alphas;
	
	private DataPair[][] sortedData;
	
	// constructor for an AdaBoost algorithm, consisting of m hypotheses which train on
	// the dataset
	public AdaBoost(int numberOfHypotheses, List<Mail> dataset)
	{
		/*
		 * Initialize data set, hypothesis set, weights on data points and weights on hypotheses (alphas). 
		 */
		this.dataset = dataset;
		hypotheses = new BaseLearner[numberOfHypotheses];
		weights = new double[dataset.size()];
		// initialize the weights on data points with value 1/N
		for (int i = 0; i < weights.length; i++)
			weights[i] = 1.0/dataset.size();
		alphas = new double[numberOfHypotheses];
		sortDatasetInVariables(dataset);
		
		/*
		 * Train m hypotheses according to the scaled AdaBoost algorithm.
		 */
		for (int hypothesis_index = 0; hypothesis_index < numberOfHypotheses; hypothesis_index++)
		{
			// choose a random feature <- {0,56}
			int feature = (int) (Math.random() * 57.0);
			// initialize hypothesis
			hypotheses[hypothesis_index] = new BaseLearner(sortedData, weights, feature, this);
			// calculate the error (cumulative weight of all misclassified points/ total weight)
			double error = calculateError(hypothesis_index);
			// calculate the alpha value for this hypothesis (alpha <- 1/2 ln((1 - err)/err)
			alphas[hypothesis_index] = calculateAlpha(error);
			// update the weights and scale them to a cumulative 1
			updateAndScaleWeights(hypothesis_index);
		}
	}

	private void sortDatasetInVariables(List<Mail> allData)
	{
		// TODO Comment
		sortedData = new DataPair[allData.get(0).x.length][];
		
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
	}


	private double calculateError(int m) 
	{
		double cumulativeError  = 0.0;
		// for every data point:
		for (int i = 0; i < dataset.size(); i++)
		{
			// if the hypothesis misclassifies the data point
			if (hypotheses[m].classify(dataset.get(i)) != dataset.get(i).y)
				// add the weight of the data point to the cumulative error
				cumulativeError += weights[i];
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
		
		// update all the weights: w_i <- w_i * e ^ (-y_i * g_m(x_i) * alpha_m)
		for (int i = 0; i < weights.length; i++)
		{
			// update w_i
			weights[i] *= Math.exp
					(
							-dataset.get(i).y *
							hypothesis.classify(dataset.get(i)) *
							alphas[m]
					);
			// add w_i to cumulative weights
			cumulativeWeights += weights[i];
		}
		// scale the weights in such a way that their sum is 1
		for (int i = 0; i < weights.length; i++)
			weights[i] /= cumulativeWeights;
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
