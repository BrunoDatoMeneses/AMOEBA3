package experiments.UnityLauncher;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import agents.context.Context;
import experiments.FILE;
import experiments.nDimensionsLaunchers.F_N_Manager;
import experiments.nDimensionsLaunchers.PARAMS;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.AmoebaMultiUIWindow;
import gui.AmoebaWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import kernel.World;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import kernel.backup.SaveHelperImpl;
import utils.TRACE_LEVEL;

public class Main implements Runnable {

	AMOEBA amoeba;
	StudiedSystem studiedSystem;
	Sender sender;
	
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

		try {
			launch();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}




	}





	public void launch() throws IOException{
		// Set AMAK configuration before creating an AMOEBA
		Configuration.commandLineMode = true;
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.waitForGUI = true;
		Configuration.plotMilliSecondsUpdate = 20000;

		AMOEBA amoeba = new AMOEBA(null,  null);
		StudiedSystem studiedSystem = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
		amoeba.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		File file = new File("resources/"+PARAMS.configFile);
		backupSystem.load(file);


		amoeba.allowGraphicalScheduler(false);
		amoeba.setRenderUpdate(false);
		amoeba.data.learningSpeed = PARAMS.learningSpeed;
		amoeba.data.numberOfPointsForRegression = PARAMS.regressionPoints;
		amoeba.data.isActiveLearning = PARAMS.setActiveLearning;
		amoeba.data.isSelfLearning = PARAMS.setSelfLearning;
		amoeba.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
		amoeba.data.isConflictDetection = PARAMS.setConflictDetection;
		amoeba.data.isConcurrenceDetection = PARAMS.setConcurrenceDetection;
		amoeba.data.isVoidDetection = PARAMS.setVoidDetection;
		amoeba.data.isConflictResolution = PARAMS.setConflictResolution;
		amoeba.data.isConcurrenceResolution = PARAMS.setConcurrenceResolution;
		amoeba.data.isVoidDetection2 = PARAMS.setVoidDetection2;
		amoeba.data.isFrontierRequest = PARAMS.setFrontierRequest;
		amoeba.data.initRegressionPerformance = PARAMS.setRegressionPerformance;

		amoeba.setRenderUpdate(false);

		World.minLevel = TRACE_LEVEL.ERROR;


		sender = new Sender(server, amoeba);


		for (int i = 0; i < PARAMS.nbCycle; ++i) {
			amoeba.cycle();

			updateContextsOnUnity(amoeba, sender);
		}



	}




	private void updateContextsOnUnity(AMOEBA amoeba, Sender sender) {
		ArrayList<Context> spatiallyAlteredContexts = amoeba.getSpatiallyAlteredContextForUnityUI();
		ArrayList<Context> toKillContexts = amoeba.getToKillContextsForUnityUI();

		if(spatiallyAlteredContexts.size()>0) {

			sender.sendContexts(spatiallyAlteredContexts);

			while (!sender.acq("CTXTS", amoeba.getCycle())) {
				try
				{
					Thread.sleep(10);
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
					Thread.sleep(10);
				}
				catch(InterruptedException ex)
				{
					Thread.currentThread().interrupt();
				}
			}
		}
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

