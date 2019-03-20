package kernel;

import java.util.HashMap;
import java.util.List;

import fr.irit.smac.amak.Agent;
import fr.irit.smac.amak.Amas;

import agents.context.localModel.TypeLocalModel;
import agents.head.Head;

public interface IAMOEBA {
	public void clearAgents();
	public void learn(HashMap<String, Double> perceptionsActionState);
	public double request(HashMap<String, Double> perceptionsActionState);
	
	public void setCreationOfNewContext(boolean creationOfNewContext);
	public void setDataForErrorMargin(double errorAllowed, double augmentationFactorError, double diminutionFactorError, double minErrorAllowed, int nConflictBeforeAugmentation, int nSuccessBeforeDiminution);
	public void setDataForInexactMargin(double inexactAllowed, double augmentationInexactError, double diminutionInexactError, double minInexactAllowed, int nConflictBeforeInexactAugmentation, int nSuccessBeforeInexactDiminution);
	public void setHead(Head head);
	public void setLoadPresetContext(boolean loadPresetContext);
	public void setLocalModel(TypeLocalModel localModel);
	
	public List<Agent<? extends Amas<World>, World>> getAgents();
	public boolean isCreationOfNewContext();
	public boolean isLoadPresetContext();
}
