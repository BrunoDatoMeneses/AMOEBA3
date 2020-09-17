package multiagent.framework.messaging;

import multiagent.framework.aid.AddressableAID;

public interface IAmakReceivedEnvelope<T extends IAmakMessage, M extends IAmakMessageMetaData, I extends AddressableAID> {

	T getMessage();

	M getMetadata();

	I getMessageSenderAID();

}
