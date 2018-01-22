package agents.head;

import java.io.Serializable;
import java.util.ArrayList;

import kernel.World;
import agents.Agent;
import agents.SystemAgent;
import agents.messages.Message;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractHead.
 */
public class AbstractHead extends SystemAgent implements Serializable {
	

	public AbstractHead(World world) {
		super(world);
	}


	public ArrayList<? extends Agent> getTargets() {
		return new ArrayList<>();
	}


	@Override
	public void computeAMessage(Message m) {		
	}

	
	
}
