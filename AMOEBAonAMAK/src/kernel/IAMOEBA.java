package kernel;

import java.util.HashMap;

import agents.context.localModel.TypeLocalModel;

public interface IAMOEBA {
	public void setLocalModel(TypeLocalModel localModel);
	public void setDataForErrorMargin(double errorAllowed, double augmentationFactorError, double diminutionFactorError, double minErrorAllowed, int nConflictBeforeAugmentation, int nSuccessBeforeDiminution);
	public void setDataForInexactMargin(double inexactAllowed, double augmentationInexactError, double diminutionInexactError, double minInexactAllowed, int nConflictBeforeInexactAugmentation, int nSuccessBeforeInexactDiminution);
	public void learn(HashMap<String, Double> perceptionsActionState);
	public double request(HashMap<String, Double> perceptionsActionState);
}
