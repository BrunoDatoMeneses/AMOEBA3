package agents.context.localModel;

import java.util.ArrayList;

import agents.context.Context;
import agents.context.Experiment;
import agents.percept.Percept;
import kernel.AMOEBA;

/**
 * The Class LocalModelMillerRegression.
 */
public class LocalModelMillerRegression extends LocalModel {

	/** The n parameters. */
	private int nParameters;

	/** The regression. */
	transient private Regression regression;

	/** The coef. */
	private double[] coef;

	/**
	 * Instantiates a new local model miller regression.
	 *
	 * @param world the world
	 */
	public LocalModelMillerRegression(AMOEBA amoeba) {
		ArrayList<Percept> percepts = amoeba.getPercepts();
		this.nParameters = percepts.size();
		regression = new Regression(nParameters, true);
	}

	/**
	 * Gets the coef.
	 *
	 * @return the coef
	 */
	public double[] getCoef() {
		return coef;
	}

	public double getProposition(Context context) {
		ArrayList<Percept> percepts = context.getAmas().getPercepts();

		if (context.getExperiments().size() == 1) {
			return context.getExperiments().get(0).getProposition();
		}

		double result = coef[0];
		if (coef[0] == Double.NaN)
			System.exit(0);
		for (int i = 1; i < coef.length; i++) {
			if (Double.isNaN(coef[i]))
				coef[i] = 0;
			result += coef[i] * percepts.get(i - 1).getValue();
		}

		return result;
	}

	public double getProposition(Context context, Percept p1, Percept p2, double v1, double v2) {

		if (context.getExperiments().size() == 1) {
			return context.getExperiments().get(0).getProposition();
		}

		regression = new Regression(nParameters, true);
		for (Experiment exp : context.getExperiments()) {
			regression.addObservation(exp.getValuesAsArray(), exp.getProposition());

			while (regression.getN() < context.getExperiments().get(0).getValues().size() + 2) { // TODO : to improve
				regression.addObservation(context.getExperiments().get(0).getValuesAsArray(),
						context.getExperiments().get(0).getProposition());
			}
		}

		double[] coef = regression.regress().getParameterEstimates();

		double[] tabv = { v1, v2 };

		double result = coef[0];
		if (coef[0] == Double.NaN)
			System.exit(0);
		for (int i = 1; i < coef.length; i++) {
			if (Double.isNaN(coef[i]))
				coef[i] = 0;
			result += coef[i] * tabv[i - 1];
		}
		return result;

	}

	public String getFormula(Context context) {
		String s = "";
		if (context.getExperiments().size() == 1) {
			return "" + context.getExperiments().get(0).getProposition();
		} else {
			if (regression == null) {
				updateModel(context);
			}
			double[] coef = regression.regress().getParameterEstimates();

			ArrayList<Percept> percepts = context.getAmas().getPercepts();

			if (coef[0] == Double.NaN)
				System.exit(0);
			for (int i = 1; i < coef.length; i++) {
				if (Double.isNaN(coef[i]))
					coef[i] = 0;
				s += coef[i] + "*" + percepts.get(i - 1).getName();

				if (i < coef.length - 1)
					s += " + ";
			}

			s += "\n with ";

			for (int i = 1; i < coef.length; i++) {
				if (Double.isNaN(coef[i]))
					coef[i] = 0;
				s += percepts.get(i - 1).getName() + " = " + percepts.get(i - 1).getValue();
				s += ", ";
			}

			s += "\n with ";
			s += context.getExperiments().size() + " experimentations";

			s += "\n with ";
			s += getProposition(context) + " as result";

			return s;
		}

	}

	public String getCoefsFormula() {
		String result = "" + coef[0];
		if (coef[0] == Double.NaN)
			System.exit(0);

		for (int i = 1; i < coef.length; i++) {
			if (Double.isNaN(coef[i]))
				coef[i] = 0;

			result += "\t" + coef[i];
		}
		return result;
	}

	@Override
	public void updateModel(Context context) {
		regression = new Regression(nParameters, true);
		for (Experiment exp : context.getExperiments()) {
			regression.addObservation(exp.getValuesAsArray(), exp.getProposition());
		}

		while (regression.getN() < context.getExperiments().get(0).getValues().size() + 2) { // TODO : to improve
			regression.addObservation(context.getExperiments().get(0).getValuesAsArray(),
					context.getExperiments().get(0).getProposition());
		}

		coef = regression.regress().getParameterEstimates();
	}

}
