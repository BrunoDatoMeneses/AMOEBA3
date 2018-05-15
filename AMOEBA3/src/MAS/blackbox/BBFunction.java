package MAS.blackbox;

import java.io.Serializable;

import MAS.agents.Agent;
import MAS.agents.messages.Message;
import MAS.agents.messages.MessageType;
import MAS.blackbox.constraints.ConstraintFuncOneLinkOut;
import MAS.blackbox.constraints.ConstraintFuncTwoLinkIn;

// TODO: Auto-generated Javadoc
/**
 * The Class BBFunction.
 */
public class BBFunction extends BlackBoxAgent implements Serializable{

	/** The func. */
	private MathFunction func;
	
	/** The agent A. */
	private Agent agentA;
	
	/** The agent B. */
	private Agent agentB;
	
	/** The a. */
	private double a;
	
	/** The b. */
	private double b;
	
	/** The result. */
	private double result;

	/**
	 * Instantiates a new BB function.
	 */
	public BBFunction() {
		this.addConstraint(new ConstraintFuncTwoLinkIn(this));
		this.addConstraint(new ConstraintFuncOneLinkOut(this));
		//this.addConstraint(new ConstraintConnectedToOneInput(this));
	}

	/* (non-Javadoc)
	 * @see agents.Agent#play()
	 */
	public void play() {
		super.play();

	}

	/* (non-Javadoc)
	 * @see blackbox.BlackBoxAgent#fastPlay()
	 */
	public void fastPlay() {
		result = func.compute(a, b);
		for (Agent target : targets) {
			sendMessage(new Message(result, MessageType.VALUE, this), target);
		}
	}

	/**
	 * Gets the func.
	 *
	 * @return the func
	 */
	public MathFunction getFunc() {
		return func;
	}

	/**
	 * Sets the func.
	 *
	 * @param func the new func
	 */
	public void setFunc(MathFunction func) {
		this.func = func;
	}

	
	/* (non-Javadoc)
	 * @see blackbox.BlackBoxAgent#computeAMessage(agents.messages.Message)
	 */
	@Override
	public void computeAMessage(Message m) {
		if (m.getType() == MessageType.VALUE) {
			// Agent ag = (Agent) m.getContent();
			if (m.getSender() == agentA) {
				a = (double) m.getContent();
			} else if (m.getSender() == agentB) {
				b = (double) m.getContent();
			}
		}
	}

	/**
	 * Gets the agent A.
	 *
	 * @return the agent A
	 */
	public Agent getAgentA() {
		return agentA;
	}

	/**
	 * Sets the agent A.
	 *
	 * @param agentA the new agent A
	 */
	public void setAgentA(Agent agentA) {
		this.agentA = agentA;
	}

	/**
	 * Gets the agent B.
	 *
	 * @return the agent B
	 */
	public Agent getAgentB() {
		return agentB;
	}

	/**
	 * Sets the agent B.
	 *
	 * @param agentB the new agent B
	 */
	public void setAgentB(Agent agentB) {
		this.agentB = agentB;
	}

	/* (non-Javadoc)
	 * @see blackbox.BlackBoxAgent#getValue()
	 */
	public double getValue() {
		return result;
	}
	
	/**
	 * Adds the input agent.
	 *
	 * @param agent the agent
	 */
	public void addInputAgent (Agent agent){
		if (agentA == null) {
			agentA = agent;
		} else if (agentB == null) {
			agentB = agent;
		}
	}
	
	/**
	 * Count free input slot.
	 *
	 * @return the int
	 */
	public int countFreeInputSlot() {
		if (agentA == null) {
			if (agentB == null) {
				return 2;
			} else {
				return 1;
			}
		}
		else {
			if (agentB == null) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	/**
	 * Own specific input.
	 *
	 * @param a the a
	 * @return true, if successful
	 */
	public boolean ownSpecificInput(Agent a) {
		return agentA == a || agentB == a;
	}
	
	/**
	 * Checks if is looping itself.
	 *
	 * @return true, if is looping itself
	 */
	public boolean isLoopingItself() {
		return agentA == this || agentB == this;
	}
}
