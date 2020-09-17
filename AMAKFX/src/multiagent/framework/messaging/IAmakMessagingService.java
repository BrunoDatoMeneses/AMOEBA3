package multiagent.framework.messaging;

import multiagent.framework.aid.AddressableAID;

public interface IAmakMessagingService {

	IAmakMessageBox buildNewAmakMessageBox(AddressableAID aid);

	IAmakAddress getOrCreateAmakAddress(String randomUUID);

	void disposeAll();

	void dispose(AddressableAID aid);
}
