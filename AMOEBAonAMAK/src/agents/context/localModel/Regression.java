package agents.context.localModel;

import org.apache.commons.math3.stat.regression.MillerUpdatingRegression;
import org.apache.commons.math3.stat.regression.ModelSpecificationException;


// TODO: Auto-generated Javadoc
/**
 * Class needed for serialization.
 */
public class Regression extends MillerUpdatingRegression {

	/**
	 * Instantiates a new regression.
	 *
	 * @param numberOfVariables the number of variables
	 * @param includeConstant the include constant
	 * @throws ModelSpecificationException the model specification exception
	 */
	public Regression(int numberOfVariables, boolean includeConstant)
			throws ModelSpecificationException {
		super(numberOfVariables, includeConstant);
	}

}
