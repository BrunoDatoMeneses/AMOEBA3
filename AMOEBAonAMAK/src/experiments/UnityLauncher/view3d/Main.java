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
import kernel.World;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;

public class Main implements Runnable {

	ELLSA amoeba;
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

		ELLSA amoeba = new ELLSA(null,  null);
		StudiedSystem studiedSystem = new F_N_Manager(PARAMS_UNITY.spaceSize, PARAMS_UNITY.dimension, PARAMS_UNITY.nbOfModels, PARAMS_UNITY.normType, PARAMS_UNITY.randomExploration, PARAMS_UNITY.explorationIncrement,PARAMS_UNITY.explorationWidht,PARAMS_UNITY.limitedToSpaceZone, PARAMS_UNITY.oracleNoiseRange);
		amoeba.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		File file = new File("resources/"+PARAMS_UNITY.configFile);
		backupSystem.load(file);


		//amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

		amoeba.allowGraphicalScheduler(true);
		amoeba.setRenderUpdate(false);
		amoeba.data.learningSpeed = PARAMS_UNITY.learningSpeed;
		amoeba.data.numberOfPointsForRegression = PARAMS_UNITY.regressionPoints;
		amoeba.data.isActiveLearning = PARAMS_UNITY.setActiveLearning;
		amoeba.data.isSelfLearning = PARAMS_UNITY.setSelfLearning;
		amoeba.data.isConflictDetection = PARAMS_UNITY.setConflictDetection;
		amoeba.data.isConcurrenceDetection = PARAMS_UNITY.setConcurrenceDetection;
		amoeba.data.isVoidDetection2 = PARAMS_UNITY.setVoidDetection2;
		amoeba.data.isConflictResolution = PARAMS_UNITY.setConflictResolution;
		amoeba.data.isConcurrenceResolution = PARAMS_UNITY.setConcurrenceResolution;
		amoeba.data.isFrontierRequest = PARAMS_UNITY.setFrontierRequest;
		amoeba.data.isSelfModelRequest = PARAMS_UNITY.setSelfModelRequest;
		amoeba.data.isCoopLearningWithoutOracle = PARAMS_UNITY.setCoopLearning;

		amoeba.data.isLearnFromNeighbors = PARAMS_UNITY.setLearnFromNeighbors;
		amoeba.data.nbOfNeighborForLearningFromNeighbors = PARAMS_UNITY.nbOfNeighborForLearningFromNeighbors;
		amoeba.data.isDream = PARAMS_UNITY.setDream;
		amoeba.data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS_UNITY.nbOfNeighborForVoidDetectionInSelfLearning;
		amoeba.data.nbOfNeighborForContexCreationWithouOracle = PARAMS_UNITY.nbOfNeighborForContexCreationWithouOracle;

		amoeba.getEnvironment().setMappingErrorAllowed(PARAMS_UNITY.mappingErrorAllowed);
		amoeba.data.initRegressionPerformance = PARAMS_UNITY.setRegressionPerformance;
		World.minLevel = PARAMS_UNITY.traceLevel;


		sender = new Sender(server, amoeba);


		for (int i = 0; i < PARAMS_UNITY.nbCycle; ++i) {
			amoeba.cycle();

			updateContextsOnUnity(amoeba, sender);
		}



	}




	private void updateContextsOnUnity(ELLSA amoeba, Sender sender) {
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

