package agents.context;

import java.io.Serializable;
import java.util.ArrayList;

import kernel.World;
import agents.Agent;
import agents.Percept;
import agents.SystemAgent;
import agents.messages.Message;
import agents.messages.MessageType;

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
