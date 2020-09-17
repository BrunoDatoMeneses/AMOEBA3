package multiagent.framework.messaging.reader;

import java.util.Collection;

import multiagent.framework.messaging.IAmakEnvelope;
import multiagent.framework.messaging.IAmakReadableMessageBox;

/**
 * Interface of messaging reader strategies.</br>
 * Each of strategy offer different ways to get the received messages but have
 * the same methods.
 */
public interface IMessagingReader {

	/**
	 * Retrieve the new received messages.
	 */
	void readMsgbox();

	/**
	 * Get the messages according to the strategy implementation.
	 */
	Collection<IAmakEnvelope> getMessages();

	void setReadableMessageBox(IAmakReadableMessageBox messageBox);

}
