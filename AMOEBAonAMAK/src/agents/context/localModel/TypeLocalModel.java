package agents.context.localModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import agents.context.localModel.factories.LocalModelCoopFactory;
import agents.context.localModel.factories.LocalModelFactory;
import agents.context.localModel.factories.LocalModelMillerRegressionFactory;

/**
 * Defines the different implemented local model. Each local model is associated
 * to a class.
 *
 */
public enum TypeLocalModel implements Serializable {
	/** The miller regression. */
	MILLER_REGRESSION(new LocalModelMillerRegressionFactory()),
	
	COOP_MILLER_REGRESSION(new LocalModelCoopFactory(MILLER_REGRESSION.factory));
	
	public final LocalModelFactory factory;
	private static final Map<LocalModelFactory, TypeLocalModel> BY_FACTORY = new HashMap<>();
	
	static {
		for (TypeLocalModel t : values()) {
			BY_FACTORY.put(t.factory, t);
		}
	}
	
	private TypeLocalModel(LocalModelFactory factory) {
		this.factory = factory;
	}
	
	public static TypeLocalModel valueOf(LocalModelCoopFactory factory) {
		return BY_FACTORY.get(factory);
	}
}
