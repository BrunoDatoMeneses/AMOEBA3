package experiments.reinforcement.WithStudiedSystem;

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
import kernel.World;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import kernel.backup.SaveHelperImpl;
import utils.TRACE_LEVEL;


/**
 * The Class BadContextLauncherEasy.
 */
public class ReinforcementLauncher2D extends Application implements Serializable {


	public static final double oracleNoiseRange = 0.5;
	public static final double learningSpeed = 0.5;
	public static final int regressionPoints = 100;
	public static final int dimension = 2;
	public static final double spaceSize = 10.0	;
	public static final int nbOfModels = 3	;
	public static final int normType = 2	;
	public static final boolean randomExploration = false;
	public static final boolean limitedToSpaceZone = true;
	//public static final double mappingErrorAllowed = 0.07; // BIG SQUARE
	public static double mappingErrorAllowed = 0.02; // MULTI
	public static final double explorationIncrement = 1.0	;
	public static final double explorationWidht = 0.5	;
	
	public static final int nbCycle = 10000;
	
	ELLSA ellsaSpatialReward;
	StudiedSystem studiedSystem;
	VUIMulti amoebaSpatialRewardVUI;
	EllsaMultiUIWindow amoebaSpatialRewardUI;
	

	
	
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
		
		amoebaSpatialRewardVUI = new VUIMulti("2D");
		amoebaSpatialRewardUI = new EllsaMultiUIWindow("ELLSA", amoebaSpatialRewardVUI, null);
		
		
		// Exemple for adding a tool in the toolbar
		Slider slider = new Slider(0.01, 0.1, mappingErrorAllowed);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println("new Value "+newValue);
				mappingErrorAllowed = (double)newValue;
				ellsaSpatialReward.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
			}
		});
		amoebaSpatialRewardUI.addToolbar(slider);
		
		
		
		
		
		startTask(100, 1);


		
		
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
                	ellsaSpatialReward = new ELLSA(amoebaSpatialRewardUI,  amoebaSpatialRewardVUI);
            		studiedSystem = new ReinforcementManager2D(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
            		ellsaSpatialReward.setStudiedSystem(studiedSystem);
            		IBackupSystem backupSystem = new BackupSystem(ellsaSpatialReward);
            		File file = new File("resources/twoDimensionsLauncher.xml");
            		backupSystem.load(file);
            		
            		ellsaSpatialReward.saver = new SaveHelperImpl(ellsaSpatialReward, amoebaSpatialRewardUI);
            		ellsaSpatialReward.allowGraphicalScheduler(true);
            		ellsaSpatialReward.setRenderUpdate(true);
            		ellsaSpatialReward.data.learningSpeed = learningSpeed;
            		ellsaSpatialReward.data.numberOfPointsForRegression = regressionPoints;
            		ellsaSpatialReward.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
            		ellsaSpatialReward.setReinforcement(true);
					ellsaSpatialReward.getEnvironment().minLevel = TRACE_LEVEL.DEBUG;
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
                    	ellsaSpatialReward.learn(studiedSystem.getOutput());
                    	if(ellsaSpatialReward.getHeadAgent().isActiveLearning()) {
                    		studiedSystem.setActiveLearning(true);
                    		studiedSystem.setSelfRequest(ellsaSpatialReward.getHeadAgent().getSelfRequest()); //TODO self active ...
    						 
    					}
                    	//System.out.println(status);
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