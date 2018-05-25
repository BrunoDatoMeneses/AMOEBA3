package mas.agents.head;

import java.io.Serializable;
import java.util.ArrayList;

import mas.kernel.World;
import mas.agents.Agent;
import mas.agents.SystemAgent;
import mas.agents.messages.Message;

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
