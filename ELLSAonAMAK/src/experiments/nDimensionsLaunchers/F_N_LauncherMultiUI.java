package experiments.nDimensionsLaunchers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import experiments.FILE;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.EllsaMultiUIWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import kernel.backup.SaveHelperImpl;


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
	
	ELLSA ellsa;
	StudiedSystem studiedSystem;
	VUIMulti amoebaVUI;
	EllsaMultiUIWindow amoebaUI;
	
	ELLSA ellsa2;
	StudiedSystem studiedSystem2;
	VUIMulti amoebaVUI2;
	EllsaMultiUIWindow amoebaUI2;
	
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
		amoebaUI = new EllsaMultiUIWindow("ELLSA", amoebaVUI, null);
		
		
		// Exemple for adding a tool in the toolbar
		Slider slider = new Slider(0.01, 0.1, mappingErrorAllowed);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println("new Value "+newValue);
				mappingErrorAllowed = (double)newValue;
				ellsa.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
			}
		});
		amoebaUI.addToolbar(slider);
		
		
		amoebaVUI2 = new VUIMulti("2D");
		amoebaUI2 = new EllsaMultiUIWindow("ELLSA", amoebaVUI2, null);
		
		
		// Exemple for adding a tool in the toolbar
		Slider slider2 = new Slider(0.01, 0.1, mappingErrorAllowed);
		slider2.setShowTickLabels(true);
		slider2.setShowTickMarks(true);
		
		slider2.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println("new Value "+newValue);
				mappingErrorAllowed = (double)newValue;
				ellsa2.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
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
                	ellsa = new ELLSA(amoebaUI,  amoebaVUI);
            		studiedSystem = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
            		ellsa.setStudiedSystem(studiedSystem);
            		IBackupSystem backupSystem = new BackupSystem(ellsa);
            		File file = new File("resources/twoDimensionsLauncher.xml");
            		backupSystem.load(file);
            		
            		ellsa.saver = new SaveHelperImpl(ellsa);
            		ellsa.allowGraphicalScheduler(true);
            		ellsa.setRenderUpdate(true);
            		ellsa.data.PARAM_exogenousLearningWeight = learningSpeed;
            		ellsa.data.PARAM_numberOfPointsForRegression_ASUPPRIMER = regressionPoints;
            		ellsa.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
            		
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
                    	ellsa.learn(studiedSystem.getOutput());
                    	if(ellsa.getHeadAgent().isActiveLearning()) {
                    		studiedSystem.setActiveLearning(true);
                    		studiedSystem.setSelfRequest(ellsa.getHeadAgent().getSelfRequest()); //TODO self active ...
    						 
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
                	ellsa2 = new ELLSA(amoebaUI2,  amoebaVUI2);
            		studiedSystem2 = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
            		ellsa2.setStudiedSystem(studiedSystem2);
            		IBackupSystem backupSystem2 = new BackupSystem(ellsa2);
            		File file2 = new File("resources/twoDimensionsLauncher.xml");
            		backupSystem2.load(file2);
            		
            		ellsa2.saver = new SaveHelperImpl(ellsa2);
            		ellsa2.allowGraphicalScheduler(true);
            		ellsa2.setRenderUpdate(true);
            		ellsa2.data.PARAM_exogenousLearningWeight = learningSpeed;
            		ellsa2.data.PARAM_numberOfPointsForRegression_ASUPPRIMER = regressionPoints;
            		ellsa2.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
            		
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
                    	ellsa2.learn(studiedSystem2.getOutput());
                    	if(ellsa2.getHeadAgent().isActiveLearning()) {
                    		studiedSystem2.setActiveLearning(true);
                    		studiedSystem2.setSelfRequest(ellsa2.getHeadAgent().getSelfRequest()); //TODO self active ...
    						 
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
