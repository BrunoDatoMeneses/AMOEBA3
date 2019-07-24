package agents.context.localModel.factories;

import agents.context.localModel.LocalModel;

public interface LocalModelFactory {
	public LocalModel buildLocalModel(Object ...params);
}
