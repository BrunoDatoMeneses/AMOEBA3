package experiments.UnityLauncher.view3d;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import agents.context.Context;
import experiments.FILE;
import experiments.UnityLauncher.Sender;
import experiments.UnityLauncher.SocketServer;
import experiments.nDimensionsLaunchers.F_N_Manager;
import fr.irit.smac.amak.Configuration;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;

public class Main implements Runnable {

	ELLSA ellsa;
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

		ELLSA ellsa = new ELLSA(null,  null);
		StudiedSystem studiedSystem = new F_N_Manager(PARAMS_UNITY.spaceSize, PARAMS_UNITY.dimension, PARAMS_UNITY.nbOfModels, PARAMS_UNITY.normType, PARAMS_UNITY.randomExploration, PARAMS_UNITY.explorationIncrement,PARAMS_UNITY.explorationWidht,PARAMS_UNITY.limitedToSpaceZone, PARAMS_UNITY.oracleNoiseRange);
		ellsa.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(ellsa);
		File file = new File("resources/"+PARAMS_UNITY.configFile);
		backupSystem.load(file);


		//amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

		ellsa.allowGraphicalScheduler(true);
		ellsa.setRenderUpdate(false);
		ellsa.data.PARAM_learningSpeed = PARAMS_UNITY.learningSpeed;
		ellsa.data.PARAM_numberOfPointsForRegression_ASUPPRIMER = PARAMS_UNITY.regressionPoints;
		ellsa.data.PARAM_isActiveLearning = PARAMS_UNITY.setActiveLearning;
		ellsa.data.PARAM_isSelfLearning = PARAMS_UNITY.setSelfLearning;
		ellsa.data.PARAM_NCS_isConflictDetection = PARAMS_UNITY.setConflictDetection;
		ellsa.data.PARAM_NCS_isConcurrenceDetection = PARAMS_UNITY.setConcurrenceDetection;
		ellsa.data.PARAM_NCS_isVoidDetection = PARAMS_UNITY.setVoidDetection2;
		ellsa.data.PARAM_NCS_isConflictResolution = PARAMS_UNITY.setConflictResolution;
		ellsa.data.PARAM_NCS_isConcurrenceResolution = PARAMS_UNITY.setConcurrenceResolution;
		ellsa.data.PARAM_NCS_isFrontierRequest = PARAMS_UNITY.setFrontierRequest;
		ellsa.data.PARAM_NCS_isSelfModelRequest = PARAMS_UNITY.setSelfModelRequest;
		ellsa.data.isCoopLearningWithoutOracle_ASUPPRIMER = PARAMS_UNITY.setCoopLearning;

		ellsa.data.PARAM_isLearnFromNeighbors = PARAMS_UNITY.setLearnFromNeighbors;
		ellsa.data.PARAM_nbOfNeighborForLearningFromNeighbors = PARAMS_UNITY.nbOfNeighborForLearningFromNeighbors;
		ellsa.data.PARAM_isDream = PARAMS_UNITY.setDream;
		ellsa.data.PARAM_nbOfNeighborForVoidDetectionInSelfLearning = PARAMS_UNITY.nbOfNeighborForVoidDetectionInSelfLearning;
		ellsa.data.PARAM_nbOfNeighborForContexCreationWithouOracle = PARAMS_UNITY.nbOfNeighborForContexCreationWithouOracle;

		ellsa.getEnvironment().setMappingErrorAllowed(PARAMS_UNITY.mappingErrorAllowed);
		ellsa.data.PARAM_initRegressionPerformance = PARAMS_UNITY.setRegressionPerformance;
		ellsa.getEnvironment().PARAM_minTraceLevel = PARAMS_UNITY.traceLevel;


		sender = new Sender(server, ellsa);


		for (int i = 0; i < PARAMS_UNITY.nbCycle; ++i) {
			ellsa.cycle();

			updateContextsOnUnity(ellsa, sender);
		}



	}




	private void updateContextsOnUnity(ELLSA ellsa, Sender sender) {
		ArrayList<Context> spatiallyAlteredContexts = ellsa.getSpatiallyAlteredContextForUnityUI();
		ArrayList<Context> toKillContexts = ellsa.getToKillContextsForUnityUI();

		if(spatiallyAlteredContexts.size()>0) {

			sender.sendContexts(spatiallyAlteredContexts);

			while (!sender.acq("CTXTS", ellsa.getCycle())) {
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

			while (!sender.acq("KILL", ellsa.getCycle())) {
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

