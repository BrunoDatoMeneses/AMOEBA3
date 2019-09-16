package ros;

import fr.irit.smac.amak.Configuration;
import kernel.AMOEBA;

public class Main {

	public static void main(String[] args) {
		if(args.length < 5){
			System.out.println(
					"Usage : RosbridgeWebsocketURI ConfigFilePath RosToAmoebaTopic AmoebaToRosTopic MsgType [--nogui]"
					+ "\nRosbridge websocket URI example:\n\tws://localhost:9090"
					+ "\nConfig file example : \n\tresources/MyAmoebaConfig.xml"
					+ "\nRos to Amoeba topic example : \n\t/amoeba_req"
					+ "\nAmoeba to Ros topic example : \n\t/amoeba_res"
					+ "\nROS Message Type example : \n\tyour_ros_package/MyAmoebaMessageType"
			);
			System.exit(0);
		}
		String uri = args[0];
		String config = args[1];
		String topicSub = args[2];
		String topicPub = args[3];
		String msgType = args[4];
		if(args.length > 5) {
			for(int i = 5; i < args.length; i++) {
				if("--nogui".equals(args[i])) {
					Configuration.commandLineMode = true;
				}
			}
		}
		
		AMOEBA amoeba = new AMOEBA(config, null);
		amoeba.allowGraphicalScheduler(false);

		RosBridge bridge = new RosBridge();
		bridge.connect(uri, true);
		bridge.waitForConnection();

		//How it work look a lot like a service
		
		Publisher pub = new Publisher(topicPub, msgType, bridge);
		
		bridge.subscribe(SubscriptionRequestMsg.generate(topicSub)
					.setType(msgType)
					.setThrottleRate(1)
					.setQueueLength(1),
				new RosListenDelegateForAmoeba(amoeba, pub)
		);
		
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
