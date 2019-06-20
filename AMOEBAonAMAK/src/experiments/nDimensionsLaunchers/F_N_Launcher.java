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


	public static final boolean viewer = true;
	public static final double oracleNoiseRange = 0.0;
	public static final double learningSpeed = 0.01;
	public static final int regressionPoints = 100;
	public static final int dimension = 2	;
	public static final double spaceSize = 50.0	;
	public static final int nbOfModels = 5	;

	
	public static void main(String[] args) throws IOException {
		// Instantiating the MainWindow before usage.
		// It also allows you to change some of its behavior before creating an AMOEBA.
		// If you use Configuration.commandLineMode = True , then you should skip it. 
		AmoebaWindow.instance();
		launch(viewer);
	}
	


	public static void launch(boolean viewer) throws IOException{
		
		
		// Set AMAK configuration before creating an AMOEBA
		Configuration.commandLineMode = false;
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.waitForGUI = true;
		
		AMOEBA amoeba = new AMOEBA();
		StudiedSystem studiedSystem = new F_N_Manager(spaceSize, dimension, nbOfModels);
		amoeba.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		File file = new File("resources/twoDimensionsLauncher.xml");
		backupSystem.load(file);
		amoeba.saver = new SaveHelper(amoeba);
		amoeba.allowGraphicalScheduler(true);
		amoeba.setRenderUpdate(true);

		studiedSystem.playOneStep();
		amoeba.learn(studiedSystem.getOutput());
		
		
			
		HashMap<String,Double> amoebaSelfRequest = null;
		boolean activeLearning = false;
		
		
		
		amoeba.getHeadAgent().learningSpeed = learningSpeed;
		amoeba.getHeadAgent().numberOfPointsForRegression = regressionPoints;
		

//		for (int i = 0; i < 1000; ++i) {
//			studiedSystem.playOneStep();
//			amoeba.learn(studiedSystem.getOutput());
//		}
//		
//		for(Context ctxt : amoeba.getContexts()) {
//			System.out.println(ctxt.getName() + " " + ctxt.getLocalModel().getMaxProposition(ctxt) +  ctxt.getLocalModel().getMinProposition(ctxt) ) ;
//
//		}
		
		
		

		
	
	}
}
