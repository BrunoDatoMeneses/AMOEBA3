package experiments.UnityLauncher;

import java.util.ArrayList;

import agents.context.Context;
import agents.percept.Percept;
import kernel.AMOEBA;



public class Sender {

	AMOEBA amoeba;
	SocketServer server;
	
	public Sender(SocketServer serverInstance,AMOEBA amoebaInstance) {
		amoeba = amoebaInstance;
		server = serverInstance;
	}
	
	public void sendContexts(ArrayList<Context> contexts) {
		ArrayList<Percept> percepts = amoeba.getPercepts();
		String message = initializeMessage("CTXTS");
		
		for(Context ctxt : contexts) {
			message += "~";
			message += ctxt.getName() + "_";
			message += ctxt.getColorForUnity();
			
			for(Percept pct : percepts) {
				message += "_";
				message += pct.getName() + "#";
				message += ctxt.getRanges().get(pct).getCenter() + "#";
				message += ctxt.getRanges().get(pct).getLenght();
			}
			
			
		}
		//System.out.println(message);
		server.sendMessage(message);
	}
	
	public void sendContextsToKill(ArrayList<Context> contexts) {
		String message = initializeMessage("KILL");
		
		for(Context ctxt : contexts) {
			message += "~" + ctxt.getName();
			
			
		}
		//System.out.println(message);
		server.sendMessage(message);
	}
	
	private String initializeMessage(String prefix) {
		return prefix + "~" + amoeba.getCycle();
	}
	
	public boolean acq(String type, int cycle) {
		String message = server.readMessage();
		String[] tokens = message.split("_");
		return type.equals(tokens[1]) && Integer.parseInt(tokens[2])==cycle;
	}
	
}



