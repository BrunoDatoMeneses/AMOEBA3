package agents.head;

import agents.AmoebaAgent;
import kernel.AMOEBA;

public class Head extends AmoebaAgent {

	public Head(AMOEBA amas) {
		super(amas);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected int computeExecutionOrderLayer() {
		return 2;
	}

}
