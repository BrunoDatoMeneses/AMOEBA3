package agents.head;

import java.util.ArrayList;

import agents.AmoebaAgent;
import agents.AmoebaMessage;
import kernel.AMOEBA;

/**
 * The Class AbstractHead.
 */
public abstract class AbstractHead extends AmoebaAgent {
	
	public AbstractHead(AMOEBA amas) {
		super(amas);
	}

	public ArrayList<? extends AmoebaAgent> getTargets() {
		return new ArrayList<>();
	}
	
	public void computeAMessage(AmoebaMessage m) {
	}
	
	public AbstractHead clone() throws CloneNotSupportedException{
		return (AbstractHead)super.clone();
	}
}
