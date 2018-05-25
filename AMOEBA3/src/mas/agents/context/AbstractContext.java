package mas.agents.context;

import java.io.Serializable;
import java.util.ArrayList;

import mas.kernel.World;
import mas.agents.Agent;
import mas.agents.percept.Percept;
import mas.agents.SystemAgent;
import mas.agents.messages.Message;
import mas.agents.messages.MessageType;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractContext.
 */
public class AbstractContext extends SystemAgent implements Serializable{


	public AbstractContext(World world) {
		super(world);
	}


	@Override
	public ArrayList<? extends Agent> getTargets() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}


	@Override
	public void computeAMessage(Message m) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Remove all the references in other agents and remove the agent from the
	 * scheduler.
	 */
	public void die() {
		super.die();
		ArrayList<Percept> var = (ArrayList<Percept>) world.getAllPercept();
		for (Percept v : var) {
			sendExpressMessage(null, MessageType.UNREGISTER, v);
		}

		world.kill(this);
	}
}
