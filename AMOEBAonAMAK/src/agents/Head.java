package agents;

import fr.irit.smac.amak.CommunicatingAgent;
import kernel.AMOEBA;
import kernel.World;

public class Head extends CommunicatingAgent<AMOEBA, World> {

	public Head(AMOEBA amas) {
		super(amas);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected int computeExecutionOrderLayer() {
		return 2;
	}

}
