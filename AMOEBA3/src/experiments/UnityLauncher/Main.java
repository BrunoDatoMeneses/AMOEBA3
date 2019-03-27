package experiments.UnityLauncher;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import mas.agents.context.Context;
import mas.agents.localModel.TypeLocalModel;
import mas.init.amoeba.AMOEBAFactory;
import mas.kernel.AMOEBA;

public class Main implements Runnable {

	private SocketServer server;
	
	/* GUI or not */
	public static final boolean viewer = false;
	private String message = "";

	
	private Boolean shutDown;
	


	
	public Main(ServerSocket ss, Socket s) {
		server = new SocketServer(ss, s);

		
		
		shutDown = false;
		

	}
	
	public void run() {

		/*Here we create AMOEBA.*/
		AMOEBA amoeba = AMOEBAFactory.createAMOEBA(viewer,"threeDimensionsLauncher.xml");
		
		/* These method calls allow to setup AMOEBA*/
		amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		
		/* Error parameter */
		amoeba.setDataForErrorMargin(500, 0.5, 0.5, 1, 20, 20);
		
		/* Other parameters */
		amoeba.setRememberState(false);
		amoeba.setGenerateCSV(false);
		

		Manager f_XY_Manager = new Manager(50.0);
		
		Sender sender = new Sender(server, amoeba);
		
		amoeba.setRunning(true);

		int i = 0;

		while (shutDown != true)  {
			
			//System.out.println("Running :" + amoeba.isRunning());
			try        
			{
			    Thread.sleep(amoeba.temporisation);
			} 
			catch(InterruptedException ex) 
			{
			    Thread.currentThread().interrupt();
			}
			
			
			if(amoeba.getScheduler().requestAsked()) {
				amoeba.manual = true;
				System.out.println("                                                                                                     MANUAL REQUEST");
				amoeba.learn(new HashMap<String, Double>(f_XY_Manager.getOutputRequest(amoeba.getScheduler().getManualRequest())));
				amoeba.manual = false;
				
			}else if(amoeba.isRunning()) {
				
				/*Random samples of the studied system */
				f_XY_Manager.playOneStep(0);
				
				/*This is a learning step of AMOEBA*/
				amoeba.learn(new HashMap<String, Double>(f_XY_Manager.getOutput()));
				
				i++;
			}
			else if(amoeba.getPlayOneStep()) {
				
				amoeba.setPlayOneStep(false);
				/*Random samples of the studied system */
				f_XY_Manager.playOneStep(0);
				
				/*This is a learning step of AMOEBA*/
				amoeba.learn(new HashMap<String, Double>(f_XY_Manager.getOutput()));
				
				i++;
				
				
			}
 
			ArrayList<Context> spatiallyAlteredContexts = amoeba.getScheduler().getSpatiallyAlteredContext();
			ArrayList<Context> toKillContexts = amoeba.getScheduler().getToKillContext();
			if(spatiallyAlteredContexts.size()>0) {
				sender.sendContexts(spatiallyAlteredContexts);
				while (!sender.acq("CTXTS", amoeba.getScheduler().getTick())) {
					try        
					{
					    Thread.sleep(100);
					} 
					catch(InterruptedException ex) 
					{
					    Thread.currentThread().interrupt();
					}
				}
			}
			
			if(toKillContexts.size()>0) {
				sender.sendContextsToKill(toKillContexts);
				while (!sender.acq("KILL", amoeba.getScheduler().getTick())) {
					try        
					{
					    Thread.sleep(100);
					} 
					catch(InterruptedException ex) 
					{
					    Thread.currentThread().interrupt();
					}
				}
			}
			
					
			
			// CTXTS_1~1489196812_PCT_2-px_65.94282672146022_13.84702351908335-py_9.267954533508991_33.35921345566747

			
		}

		
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private void quit(){
		
		server.close();		
		shutDown = true;
	}
	
	private void ack(){
		//System.out.println("ACK...");
		//message = readMessage();
		//System.out.println(message);
		if(!message.contentEquals("")){
				server.sendMessage("ACK_" + Integer.toString(server.getMessageCounter())); 
				//System.out.println("ACK");
		}
		else{
			server.sendMessage("ERR_"+Integer.toString(server.getMessageCounter()));
		}
	}
	
	
	

}

