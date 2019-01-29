package agents;

import fr.irit.smac.amak.messaging.IAmakMessage;

// TODO: Auto-generated Javadoc
/**
 * The Class Message.
 */
public class AmoebaMessage implements IAmakMessage {

	/** The content. */
	private Object content;
	
	/** The type. */
	private MessageType type;
	
	/** The sender. */
	private AmoebaAgent sender;

	/**
	 * Instantiates a new message.
	 *
	 * @param content the content
	 * @param type the type
	 * @param sender the sender
	 */
	public AmoebaMessage(Object content, MessageType type, AmoebaAgent sender) {
		this.content = content;
		this.type = type;
		this.sender = sender;
	}

	/**
	 * Gets the content.
	 *
	 * @return The content of the message.
	 */
	public Object getContent() {
		return content;
	}

	/**
	 * Sets the content.
	 *
	 * @param content the new content
	 */
	public void setContent(Object content) {
		this.content = content;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public MessageType getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(MessageType type) {
		this.type = type;
	}

	/**
	 * Gets the sender.
	 *
	 * @return the sender
	 */
	public AmoebaAgent getSender() {
		return sender;
	}

	/**
	 * Sets the sender.
	 *
	 * @param sender the new sender
	 */
	public void setSender(AmoebaAgent sender) {
		this.sender = sender;
	}

}
