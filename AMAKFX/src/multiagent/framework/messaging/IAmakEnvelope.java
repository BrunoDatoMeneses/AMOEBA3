package multiagent.framework.messaging;

import multiagent.framework.aid.AddressableAID;

public interface IAmakEnvelope extends IAmakMessageMetaData {

	IAmakMessage getMessage();

	IAmakMessageMetaData getMetadata();

	AddressableAID getMessageSenderAID();

}
