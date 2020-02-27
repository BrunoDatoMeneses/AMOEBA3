package experiments.nDimensionsLaunchers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import experiments.FILE;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.examples.randomantsMultiUi.AntHillExampleMultiUI;
import fr.irit.smac.amak.examples.randomantsMultiUi.WorldExampleMultiUI;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.VUI;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.AmoebaMultiUIWindow;
import gui.AmoebaWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import kernel.backup.SaveHelperImpl;
import utils.TRACE_LEVEL;


/**
 * The Class BadContextLauncherEasy.
 */
public class F_N_LauncherMultiUI extends Application implements Serializable {


	public static final double oracleNoiseRange = 0.5;
	public static final double learningSpeed = 0.01;
	public static final int regressionPoints = 100;
	public static final int dimension = 2;
	public static final double spaceSize = 50.0	;
	public static final int nbOfModels = 3	;
	public static final int normType = 2	;
	public static final boolean randomExploration = true;
	public static final boolean limitedToSpaceZone = true;
	//public static final double mappingErrorAllowed = 0.07; // BIG SQUARE
	public static double mappingErrorAllowed = 0.03; // MULTI
	public static final double explorationIncrement = 1.0	;
	public static final double explorationWidht = 0.5	;
	
	public static final int nbCycle = 1000;
	
	AMOEBA amoeba;
	StudiedSystem studiedSystem;
	VUIMulti amoebaVUI;
	AmoebaMultiUIWindow amoebaUI;
	
	AMOEBA amoeba2;
	StudiedSystem studiedSystem2;
	VUIMulti amoebaVUI2;
	AmoebaMultiUIWindow amoebaUI2;
	
	public static void main(String[] args) throws IOException {
		
		
		Application.launch(args);


	}
	
	@Override
	public void start(Stage arg0) throws Exception, IOException {

		Configuration.multiUI=true;
		Configuration.commandLineMode = false;
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.waitForGUI = true;
		Configuration.plotMilliSecondsUpdate = 20000;
		
		amoebaVUI = new VUIMulti("2D");
		amoebaUI = new AmoebaMultiUIWindow("ELLSA", amoebaVUI, null);
		
		
		// Exemple for adding a tool in the toolbar
		Slider slider = new Slider(0.01, 0.1, mappingErrorAllowed);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println("new Value "+newValue);
				mappingErrorAllowed = (double)newValue;
				amoeba.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
			}
		});
		amoebaUI.addToolbar(slider);
		
		
		amoebaVUI2 = new VUIMulti("2D");
		amoebaUI2 = new AmoebaMultiUIWindow("ELLSA", amoebaVUI2, null);
		
		
		// Exemple for adding a tool in the toolbar
		Slider slider2 = new Slider(0.01, 0.1, mappingErrorAllowed);
		slider2.setShowTickLabels(true);
		slider2.setShowTickMarks(true);
		
		slider2.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println("new Value "+newValue);
				mappingErrorAllowed = (double)newValue;
				amoeba2.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
			}
		});
		amoebaUI2.addToolbar(slider2);
		
		
		
		startTask(100, 1000);
		startTask2(500, 100);


		
//		VUIMulti amoebaVUI2 = VUIMulti.get("2D");
//		AmoebaMultiUIWindow amoebaUI2 = new AmoebaMultiUIWindow("ELLSA", amoebaVUI2);
//		AMOEBA amoeba2 = new AMOEBA(amoebaUI2,  amoebaVUI2);
//		
//		StudiedSystem studiedSystem2 = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
//		amoeba2.setStudiedSystem(studiedSystem2);
//		IBackupSystem backupSystem2 = new BackupSystem(amoeba2);
//		File file2 = new File("resources/twoDimensionsLauncher.xml");
//		backupSystem2.load(file2);
//		
//		amoeba2.saver = new SaveHelperImpl(amoeba2);
//		amoeba2.allowGraphicalScheduler(true);
//		amoeba2.setRenderUpdate(true);		
//		amoeba2.data.learningSpeed = learningSpeed;
//		amoeba2.data.numberOfPointsForRegression = regressionPoints;
//		amoeba2.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
//		
//		// Exemple for adding a tool in the toolbar
//		Slider slider2 = new Slider(0.01, 0.1, mappingErrorAllowed);
//		slider2.setShowTickLabels(true);
//		slider2.setShowTickMarks(true);
//		
//		slider2.valueProperty().addListener(new ChangeListener<Number>() {
//			@Override
//			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//				System.out.println("new Value "+newValue);
//				mappingErrorAllowed = (double)newValue;
//				amoeba2.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
//			}
//		});
//		amoebaUI2.addToolbar(slider2);
//		
//		studiedSystem2.playOneStep();
//		amoeba2.learn(studiedSystem2.getOutput());
		
//		try {
//			   Thread.sleep(2000) ;
//			}  catch (InterruptedException e) {
//			    // gestion de l'erreur
//			}
//		
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < nbCycle; ++i) {
//			studiedSystem.playOneStep();
//			amoeba.learn(studiedSystem.getOutput());
//		}
//		long end = System.currentTimeMillis();
//		System.out.println("Done in : " + (end - start) );
		

		
	}
	
	public void startTask(long wait, int cycles) 
    {
        // Create a Runnable
        Runnable task = new Runnable()
        {
            public void run()
            {
                runTask(wait, cycles);
            }
        };
 
        // Run the task in a background thread
        Thread backgroundThread = new Thread(task);
        // Terminate the running thread if the application exits
        backgroundThread.setDaemon(true);
        // Start the thread
        backgroundThread.start();
        
     
    }
	
	public void startTask2(long wait, int cycles) 
    {
        // Create a Runnable
        Runnable task = new Runnable()
        {
            public void run()
            {
                runTask2(wait, cycles);
            }
        };
 
        // Run the task in a background thread
        Thread backgroundThread = new Thread(task);
        // Terminate the running thread if the application exits
        backgroundThread.setDaemon(true);
        // Start the thread
        backgroundThread.start();
        
     
    }
	
	public void runTask(long wait, int cycles) 
    {
		
		try
        {
             
            // Update the Label on the JavaFx Application Thread        
            Platform.runLater(new Runnable() 
            {
                @Override
                public void run() 
                {
                	amoeba = new AMOEBA(amoebaUI,  amoebaVUI);
            		studiedSystem = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
            		amoeba.setStudiedSystem(studiedSystem);
            		IBackupSystem backupSystem = new BackupSystem(amoeba);
            		File file = new File("resources/twoDimensionsLauncher.xml");
            		backupSystem.load(file);
            		
            		amoeba.saver = new SaveHelperImpl(amoeba);
            		amoeba.allowGraphicalScheduler(true);
            		amoeba.setRenderUpdate(true);		
            		amoeba.data.learningSpeed = learningSpeed;
            		amoeba.data.numberOfPointsForRegression = regressionPoints;
            		amoeba.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
            		
                }
            });
     
            Thread.sleep(wait);
        }
        catch (InterruptedException e) 
        {
            e.printStackTrace();
        }
		
		
		
        for(int i = 0; i < cycles; i++) 
        {
            try
            {
                // Get the Status
                final String status = "Processing " + i + " of " + cycles;
                 
                // Update the Label on the JavaFx Application Thread        
                Platform.runLater(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                    	studiedSystem.playOneStep();
                    	amoeba.learn(studiedSystem.getOutput());
                    	if(amoeba.getHeadAgent().isActiveLearning()) {
                    		studiedSystem.setActiveLearning(true);
                    		studiedSystem.setSelfRequest(amoeba.getHeadAgent().getSelfRequest()); //TODO self active ...
    						 
    					}
                    	System.out.println(status);
                    }
                });
         
                Thread.sleep(wait);
            }
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
    }   
	
	public void runTask2(long wait, int cycles) 
    {
		
		try
        {
             
            // Update the Label on the JavaFx Application Thread        
            Platform.runLater(new Runnable() 
            {
                @Override
                public void run() 
                {
                	amoeba2 = new AMOEBA(amoebaUI2,  amoebaVUI2);
            		studiedSystem2 = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
            		amoeba2.setStudiedSystem(studiedSystem2);
            		IBackupSystem backupSystem2 = new BackupSystem(amoeba2);
            		File file2 = new File("resources/twoDimensionsLauncher.xml");
            		backupSystem2.load(file2);
            		
            		amoeba2.saver = new SaveHelperImpl(amoeba2);
            		amoeba2.allowGraphicalScheduler(true);
            		amoeba2.setRenderUpdate(true);		
            		amoeba2.data.learningSpeed = learningSpeed;
            		amoeba2.data.numberOfPointsForRegression = regressionPoints;
            		amoeba2.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
            		
                }
            });
     
            Thread.sleep(wait);
        }
        catch (InterruptedException e) 
        {
            e.printStackTrace();
        }
		
		
		
        for(int i = 0; i < cycles; i++) 
        {
            try
            {
                // Get the Status
                final String status = "Processing " + i + " of " + cycles;
                 
                // Update the Label on the JavaFx Application Thread        
                Platform.runLater(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                    	studiedSystem2.playOneStep();
                    	amoeba2.learn(studiedSystem2.getOutput());
                    	if(amoeba2.getHeadAgent().isActiveLearning()) {
                    		studiedSystem2.setActiveLearning(true);
                    		studiedSystem2.setSelfRequest(amoeba2.getHeadAgent().getSelfRequest()); //TODO self active ...
    						 
    					}
                    	System.out.println(status);
                    }
                });
         
                Thread.sleep(wait);
            }
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
    }   
	
	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
	}

	public static void launch() throws IOException{
		
		
	
		
		
		

		
		/* AUTOMATIC */
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < nbCycle; ++i) {
//			studiedSystem.playOneStep();
//			amoeba.learn(studiedSystem.getOutput());
//		}
//		long end = System.currentTimeMillis();
//		System.out.println("Done in : " + (end - start) );
//		
//		start = System.currentTimeMillis();
//		for (int i = 0; i < nbCycle; ++i) {
//			studiedSystem.playOneStep();
//			amoeba.request(studiedSystem.getOutput());
//		}
//		end = System.currentTimeMillis();
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



	
}
