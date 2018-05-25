package mas.blackbox;

import java.io.Serializable;

import mas.agents.Agent;
import mas.agents.messages.Message;
import mas.agents.messages.MessageType;

// TODO: Auto-generated Javadoc
/**
 * The Class Input.
 */
public class Input extends BlackBoxAgent implements Serializable {

	/** The func. */
	private BBFunction func;
	
	/** The value. */
	private double value;
	
	/** The next value. */
	private double nextValue;

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
		value = nextValue;
		for (Agent target : targets) {
			sendMessage(new Message(value, MessageType.VALUE, this), target);
		}
	}

	/**
	 * Gets the func.
	 *
	 * @return the func
	 */
	public BBFunction getFunc() {
		return func;
	}

	/**
	 * Sets the func.
	 *
	 * @param func the new func
	 */
	public void setFunc(BBFunction func) {
		this.func = func;
	}

	/* (non-Javadoc)
	 * @see blackbox.BlackBoxAgent#getValue()
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(double value) {
		this.value = value;
		this.nextValue = value;
	}

	/* (non-Javadoc)
	 * @see blackbox.BlackBoxAgent#computeAMessage(agents.messages.Message)
	 */
	@Override
	public void computeAMessage(Message m) {
		if (m.getType() == MessageType.VALUE) {
			value = (double) m.getContent();
		}

	}

	/**
	 * Mult value.
	 *
	 * @param factor the factor
	 */
	public void multValue(double factor) {
		value *= factor;
	}

	/**
	 * Gets the next value.
	 *
	 * @return the next value
	 */
	public double getNextValue() {
		return nextValue;
	}

	/**
	 * Sets the next value.
	 *
	 * @param nextValue the new next value
	 */
	public void setNextValue(double nextValue) {
		this.nextValue = nextValue;
	}

}
