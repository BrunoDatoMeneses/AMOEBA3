package agents.context.localModel;

import java.io.Serializable;

/**
 * Defines the different implemented local model. Each local model is associated
 * to a class.
 *
 */
public enum TypeLocalModel implements Serializable {
	/** The first experiment. */
	FIRST_EXPERIMENT,
	/** The average. */
	AVERAGE,
	/** The miller regression. */
	MILLER_REGRESSION
}
