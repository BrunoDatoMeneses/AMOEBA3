package mas.agents.head;

import java.io.Serializable;
import java.util.ArrayList;

import mas.kernel.World;
import mas.agents.Agent;
import mas.agents.SystemAgent;
import mas.agents.messages.Message;
import mas.agents.percept.Percept;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractHead.
 */
public class AbstractHead extends SystemAgent implements Serializable,Cloneable {
	

	public AbstractHead(World world) {
		super(world);
	}


	public ArrayList<? extends Agent> getTargets() {
		return new ArrayList<>();
	}


	@Override
	public void computeAMessage(Message m) {		
	}

	
	public AbstractHead clone() throws CloneNotSupportedException{
		return (AbstractHead)super.clone();
	}
}
