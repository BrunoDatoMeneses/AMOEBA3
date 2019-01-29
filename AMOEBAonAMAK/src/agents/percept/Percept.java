package agents.percept;

import agents.AmoebaAgent;
import kernel.AMOEBA;

public class Percept extends AmoebaAgent {

	public Percept(AMOEBA amas) {
		super(amas);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected int computeExecutionOrderLayer() {
		return 0;
	}

}
