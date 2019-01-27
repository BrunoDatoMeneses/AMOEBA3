package agents;

import fr.irit.smac.amak.CommunicatingAgent;
import kernel.AMOEBA;
import kernel.World;

public class Percept extends CommunicatingAgent<AMOEBA, World> {

	public Percept(AMOEBA amas) {
		super(amas);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected int computeExecutionOrderLayer() {
		return 0;
	}

}
