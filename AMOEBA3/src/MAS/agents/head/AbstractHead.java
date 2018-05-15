package MAS.agents.head;

import java.io.Serializable;
import java.util.ArrayList;

import MAS.kernel.World;
import MAS.agents.Agent;
import MAS.agents.SystemAgent;
import MAS.agents.messages.Message;

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
