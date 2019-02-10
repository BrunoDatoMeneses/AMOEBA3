package agents;

import fr.irit.smac.amak.CommunicatingAgent;
import kernel.AMOEBA;
import kernel.World;

/**
 * The base class for all AMOEBA agents
 *
 */
public abstract class AmoebaAgent extends CommunicatingAgent<AMOEBA, World> {

	protected String name;
	
	public AmoebaAgent(AMOEBA amas) {
		super(amas);
	}
	
	@Override
	protected void onDecide() {
		for(AmoebaMessage m : getReceivedMessagesGivenType(AmoebaMessage.class) ) {
			computeAMessage(m);
		}
	}
	
	public abstract void computeAMessage(AmoebaMessage m);
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Deprecated
	public void sendExpressMessage(Object object, MessageType type, AmoebaAgent a) {
		a.receiveExpressMessage(new AmoebaMessage(object,type,this));
	}

	@Deprecated
	private void receiveExpressMessage(AmoebaMessage message) {
		computeAMessage(message);
	}
}
