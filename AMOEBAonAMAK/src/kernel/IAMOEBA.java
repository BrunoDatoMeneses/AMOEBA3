package kernel;

import java.util.HashMap;
import java.util.List;

import fr.irit.smac.amak.Agent;
import fr.irit.smac.amak.Amas;

import agents.context.localModel.TypeLocalModel;

public interface IAMOEBA {
	public void setLocalModel(TypeLocalModel localModel);
	public void setDataForErrorMargin(double errorAllowed, double augmentationFactorError, double diminutionFactorError, double minErrorAllowed, int nConflictBeforeAugmentation, int nSuccessBeforeDiminution);
	public void setDataForInexactMargin(double inexactAllowed, double augmentationInexactError, double diminutionInexactError, double minInexactAllowed, int nConflictBeforeInexactAugmentation, int nSuccessBeforeInexactDiminution);
	public void learn(HashMap<String, Double> perceptionsActionState);
	public double request(HashMap<String, Double> perceptionsActionState);
	public List<Agent<? extends Amas<World>, World>> getAgents();
	public boolean isCreationOfNewContext();
	public boolean isLoadPresetContext();
}
