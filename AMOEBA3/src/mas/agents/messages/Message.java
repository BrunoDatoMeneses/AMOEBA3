package mas.agents.messages;

import java.io.Serializable;

import mas.agents.Agent;

// TODO: Auto-generated Javadoc
/**
 * The Class Message.
 */
public class Message implements Serializable {

	/** The content. */
	private Object content;
	
	/** The type. */
	private MessageType type;
	
	/** The sender. */
	private Agent sender;

	/**
	 * Instantiates a new message.
	 *
	 * @param content the content
	 * @param type the type
	 * @param sender the sender
	 */
	public Message(Object content, MessageType type, Agent sender) {
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
	public Agent getSender() {
		return sender;
	}

	/**
	 * Sets the sender.
	 *
	 * @param sender the new sender
	 */
	public void setSender(Agent sender) {
		this.sender = sender;
	}

}
