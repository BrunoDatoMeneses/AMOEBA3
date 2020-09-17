package multiagent.framework.messaging.reader;

import java.util.ArrayList;
import java.util.Collection;

import multiagent.framework.messaging.IAmakEnvelope;

public class MessagingReaderAllMsgsOfCycle extends AbstractMessagingReader {



	/**
	 * Get the received messages of the current cycle.
	 * 
	 * @return the received message. The return collection can be modify.
	 **/
	public Collection<IAmakEnvelope> getMessages() {
		return new ArrayList<>(messagesOfTheCycle);
	}

}
