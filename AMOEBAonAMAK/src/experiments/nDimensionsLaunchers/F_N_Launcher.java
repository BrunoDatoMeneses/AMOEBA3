package experiments.nDimensionsLaunchers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import agents.context.Context;
import experiments.F_XY_System;
import experiments.XmlConfigGenerator;
import fr.irit.smac.amak.Configuration;
import gui.AmoebaWindow;
import kernel.AMOEBA;
import kernel.BackupSystem;
import kernel.IBackupSystem;
import kernel.SaveHelper;
import kernel.StudiedSystem;


// TODO: Auto-generated Javadoc
/**
 * The Class BadContextLauncherEasy.
 */
public class F_N_Launcher implements Serializable {


	public static final double oracleNoiseRange = 0.0;
	public static final double learningSpeed = 0.01;
	public static final int regressionPoints = 100;
	public static final int dimension = 2	;
	public static final double spaceSize = 50.0	;
	public static final int nbOfModels = 5	;
	public static final int normType = 2	;
	public static final boolean randomExploration = true;
	public static final boolean limitedToSpaceZone = false;
	public static final double mappingErrorAllowed = 0.05	;
	public static final double explorationIncrement = 1.0	;
	public static final double explorationWidht = 1	;
	
	public static final int nbCycle = 1000;
	

	
	public static void main(String[] args) throws IOException {
		// Instantiating the MainWindow before usage.
		// It also allows you to change some of its behavior before creating an AMOEBA.
		// If you use Configuration.commandLineMode = True , then you should skip it. 
		AmoebaWindow.instance();
		launch();
	}
	


	public static void launch() throws IOException{
		
		
		// Set AMAK configuration before creating an AMOEBA
		Configuration.commandLineMode = false;
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.waitForGUI = true;
		
		AMOEBA amoeba = new AMOEBA();
		StudiedSystem studiedSystem = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone);
		amoeba.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		File file = new File("resources/twoDimensionsLauncher.xml");
		backupSystem.load(file);
		
		amoeba.saver = new SaveHelper(amoeba);
		amoeba.allowGraphicalScheduler(true);
		amoeba.setRenderUpdate(true);		
		amoeba.data.learningSpeed = learningSpeed;
		amoeba.data.numberOfPointsForRegression = regressionPoints;
		amoeba.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
		

		studiedSystem.playOneStep();
		amoeba.learn(studiedSystem.getOutput());
		
		
		/* AUTOMATIC */
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < nbCycle; ++i) {
//			studiedSystem.playOneStep();
//			amoeba.learn(studiedSystem.getOutput());
//		}
//		long end = System.currentTimeMillis();
//		System.out.println("Done in : " + (end - start) );
		
		
		/* XP PIERRE */
//		for (int i = 0; i < 1000; ++i) {
//			studiedSystem.playOneStep();
//			amoeba.learn(studiedSystem.getOutput());
//		}
//		
//		for (int i = 0; i < 10; ++i) {
//			studiedSystem.playOneStep();
//			System.out.println(studiedSystem.getOutput());
//			System.out.println(amoeba.request(studiedSystem.getOutput()));
//			
//		}
//		
//		for(Context ctxt : amoeba.getContexts()) {
//			System.out.println(ctxt.toStringPierre());
//
//		}
		
		
		

		
	
	}
}
