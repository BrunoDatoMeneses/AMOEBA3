package agents.context;

import agents.AmoebaAgent;
import kernel.AMOEBA;

public class Context extends AmoebaAgent {

	public Context(AMOEBA amas) {
		super(amas);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected int computeExecutionOrderLayer() {
		return 1;
	}

}
