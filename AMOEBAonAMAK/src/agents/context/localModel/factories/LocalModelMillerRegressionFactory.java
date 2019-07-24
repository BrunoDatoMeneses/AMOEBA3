package agents.context.localModel.factories;

import agents.context.Context;
import agents.context.localModel.LocalModel;
import agents.context.localModel.LocalModelMillerRegression;

/**
 * A factory for creating {@link LocalModelMillerRegression}. Take a {@link Context} as param.
 * @author Hugo
 *
 */
public class LocalModelMillerRegressionFactory implements LocalModelFactory {

	@Override
	public LocalModel buildLocalModel(Object... params) {
		if(params.length != 1) {
			throw new IllegalArgumentException("Expected one "+Context.class+", got "+params.length+" arguments");
		}
		if(!(params[0] instanceof Context)) {
			throw new IllegalArgumentException("Expected "+Context.class+", got "+params[0].getClass());
		}
		Context c = (Context) params[0];
		return new LocalModelMillerRegression(c);
	}

}
