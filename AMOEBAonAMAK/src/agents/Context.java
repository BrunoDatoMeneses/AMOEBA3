package agents;

import fr.irit.smac.amak.CommunicatingAgent;
import kernel.AMOEBA;
import kernel.World;

public class Context extends CommunicatingAgent<AMOEBA, World> {

	public Context(AMOEBA amas) {
		super(amas);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected int computeExecutionOrderLayer() {
		return 1;
	}

}
