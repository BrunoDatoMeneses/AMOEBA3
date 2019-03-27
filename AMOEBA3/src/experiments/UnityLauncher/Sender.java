package experiments.UnityLauncher;

import java.util.ArrayList;

import mas.agents.context.Context;
import mas.agents.percept.Percept;
import mas.kernel.AMOEBA;

public class Sender {

	AMOEBA amoeba;
	SocketServer server;
	
	public Sender(SocketServer serverInstance,AMOEBA amoebaInstance) {
		amoeba = amoebaInstance;
		server = serverInstance;
	}
	
	public void sendContexts(ArrayList<Context> contexts) {
		ArrayList<Percept> percepts = amoeba.getScheduler().getPercepts();
		String message = initializeMessage("CTXTS");
		
		for(Context ctxt : contexts) {
			message += "~";
			message += ctxt.getName() + "_";
			message += ctxt.getColor();
			
			for(Percept pct : percepts) {
				message += "_";
				message += pct.getName() + "#";
				message += ctxt.getRanges().get(pct).getCenter() + "#";
				message += ctxt.getRanges().get(pct).getLenght();
			}
			
			
		}
		System.out.println(message);
		server.sendMessage(message);
	}
	
	public void sendContextsToKill(ArrayList<Context> contexts) {
		String message = initializeMessage("KILL");
		
		for(Context ctxt : contexts) {
			message += "~" + ctxt.getName();
			
			
		}
		System.out.println(message);
		server.sendMessage(message);
	}
	
	private String initializeMessage(String prefix) {
		return prefix + "~" + amoeba.getScheduler().getTick();
	}
	
	public boolean acq(String type, int cycle) {
		String message = server.readMessage();
		String[] tokens = message.split("_");
		return type.equals(tokens[1]) && Integer.parseInt(tokens[2])==cycle;
	}
	
}



