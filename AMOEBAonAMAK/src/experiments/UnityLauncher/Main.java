package experiments.UnityLauncher;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import agents.context.Context;
import experiments.FILE;
import experiments.nDimensionsLaunchers.F_N_Manager;
import fr.irit.smac.amak.Configuration;
import gui.AmoebaWindow;
import kernel.AMOEBA;
import kernel.BackupSystem;
import kernel.IBackupSystem;
import kernel.SaveHelper;
import kernel.StudiedSystem;

public class Main implements Runnable {

	
	
	public static final double oracleNoiseRange = 0.0;
	public static final double learningSpeed = 0.01;
	public static final int regressionPoints = 100;
	public static final int dimension = 3	;
	public static final double spaceSize = 50.0	;
	public static final int nbOfModels = 3	;
	public static final int normType = 2	;
	public static final boolean randomExploration = true;
	public static final boolean limitedToSpaceZone = true;
	public static final double mappingErrorAllowed = 0.03;
	public static final double explorationIncrement = 1.0	;
	public static final double explorationWidht = 0.5	;
	
	public static final int nbCycle = 1000;
	
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

		AmoebaWindow.instance();
		try {
			launch();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		
	}
	
	public void launch() throws IOException{
		
		
		
		
		// Set AMAK configuration before creating an AMOEBA
		Configuration.commandLineMode = false;
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.waitForGUI = true;
		Configuration.plotMilliSecondsUpdate = 10000;
		
		AMOEBA amoeba = new AMOEBA();
		StudiedSystem studiedSystem = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone);
		amoeba.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		File file = new File("resources/threeDimensionsLauncherUnity.xml");
		backupSystem.load(file);
		
		amoeba.saver = new SaveHelper(amoeba);
		amoeba.allowGraphicalScheduler(true);
		amoeba.setRenderUpdate(true);		
		amoeba.getHeadAgent().learningSpeed = learningSpeed;
		amoeba.getHeadAgent().numberOfPointsForRegression = regressionPoints;
		amoeba.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
		
		
		Sender sender = new Sender(server, amoeba);

		studiedSystem.playOneStep();
		amoeba.learn(studiedSystem.getOutput());
		
		
		
	

		for (int i = 0; i < nbCycle; ++i) {
			
			
			
			
			studiedSystem.playOneStep();
			amoeba.learn(studiedSystem.getOutput());
			if(amoeba.getHeadAgent().isActiveLearning()) {
			
				studiedSystem.setActiveLearning(true);
				studiedSystem.setSelfRequest(amoeba.getHeadAgent().getSelfRequest());
			 
			}
			
			
			ArrayList<Context> spatiallyAlteredContexts = amoeba.getSpatiallyAlteredContextForUnityUI();
			ArrayList<Context> toKillContexts = amoeba.getToKillContextsForUnityUI();
			
			if(spatiallyAlteredContexts.size()>0) {
				
				
				
				sender.sendContexts(spatiallyAlteredContexts);
				
				while (!sender.acq("CTXTS", amoeba.getCycle())) {
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
				
				while (!sender.acq("KILL", amoeba.getCycle())) {
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
		}
		
		
		
		
		
		
		
		/* AUTOMATIC */
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < nbCycle; ++i) {
//			studiedSystem.playOneStep();
//			amoeba.learn(studiedSystem.getOutput());
//		}
//		long end = System.currentTimeMillis();
//		System.out.println("Done in : " + (end - start) );
		
		
//		/* XP PIERRE */
//		
//		String fileName = fileName(new ArrayList<String>(Arrays.asList("GaussiennePierre")));
//		
//		FILE Pierrefile = new FILE("Pierre",fileName);
//		for (int i = 0; i < nbCycle; ++i) {
//			studiedSystem.playOneStep();
//			amoeba.learn(studiedSystem.getOutput());
//			if(amoeba.getHeadAgent().isActiveLearning()) {
//				studiedSystem.setActiveLearning(true);
//				studiedSystem.setSelfRequest(amoeba.getHeadAgent().getSelfRequest());
//				 
//			}
//		}
//		
//		for (int i = 0; i < 10; ++i) {
//			studiedSystem.playOneStep();
//			System.out.println(studiedSystem.getOutput());
//			System.out.println(amoeba.request(studiedSystem.getOutput()));
//			
//			
//		}
//		
//		Pierrefile.write(new ArrayList<String>(Arrays.asList("ID contexte","Coeff Cte","Coeff X0","Coeff X1","Min Value","Max Value")));
//		
//		for(Context ctxt : amoeba.getContexts()) {
//			
//			writeMessage(Pierrefile, ctxt.toStringArrayPierre());
//
//		}
//		
//		
//		Pierrefile.close();
		
	
	}
	
	public static String fileName(ArrayList<String> infos) {
		String fileName = "";
		
		for(String info : infos) {
			fileName += info + "_";
		}
		
		return fileName;
	}
	
	public static void writeMessage(FILE file, ArrayList<String> message) {
		
		file.initManualMessage();
		
		for(String m : message) {
			file.addManualMessage(m);
		}
		
		file.sendManualMessage();
		
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

