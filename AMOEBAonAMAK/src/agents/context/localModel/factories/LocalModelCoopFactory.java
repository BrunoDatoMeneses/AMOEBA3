package agents.context.localModel.factories;

import agents.context.localModel.LocalModel;
import agents.context.localModel.LocalModelCoopModifier;
import agents.context.localModel.TypeLocalModel;

/**
 * A factory for creating {@link LocalModelCoopModifier}. Take a {@link LocalModel} as param, 
 * or a {@link LocalModelFactory} with all param to build a LocalModel.
 * @author Hugo
 *
 */
public class LocalModelCoopFactory implements LocalModelFactory {
	private LocalModelFactory factory;
	
	public LocalModelCoopFactory(LocalModelFactory factory) {
		this.factory = factory;
	}
	
	public LocalModelCoopFactory() {
		this.factory = null;
	}
	
	@Override
	public LocalModel buildLocalModel(Object... params) {
		if(factory != null) {
			return new LocalModelCoopModifier(factory.buildLocalModel(params), TypeLocalModel.valueOf(this));
		} else {
			if(params.length != 1) {
				throw new IllegalArgumentException("Expected one "+LocalModel.class+", got "+params.length+" arguments");
			}
			if(!(params[0] instanceof LocalModel)) {
				throw new IllegalArgumentException("Expected "+LocalModel.class+", got "+params[0].getClass());
			}
			
			LocalModel lm = (LocalModel) params[0];
			return new LocalModelCoopModifier(lm, TypeLocalModel.valueOf(this));
		}
	}
	
}
