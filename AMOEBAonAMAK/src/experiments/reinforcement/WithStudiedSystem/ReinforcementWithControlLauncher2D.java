package experiments.reinforcement.WithStudiedSystem;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import agents.context.localModel.TypeLocalModel;
import experiments.FILE;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.examples.randomantsMultiUi.AntHillExampleMultiUI;
import fr.irit.smac.amak.examples.randomantsMultiUi.WorldExampleMultiUI;
import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.VUI;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.AmoebaMultiUIWindow;
import gui.AmoebaWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import kernel.World;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import kernel.backup.SaveHelperDummy;
import kernel.backup.SaveHelperImpl;
import utils.Pair;
import utils.TRACE_LEVEL;
import utils.XmlConfigGenerator;


/**
 * The Class BadContextLauncherEasy.
 */
public class ReinforcementWithControlLauncher2D extends Application implements Serializable {


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
	
	AMOEBA amoebaSpatialReward;
	StudiedSystem studiedSystem;
	VUIMulti amoebaSpatialRewardVUI;
	AmoebaMultiUIWindow amoebaSpatialRewardUI;
	
	AMOEBA amoebaActionModel1;
	VUIMulti amoebaActionModelVUI1;
	AmoebaMultiUIWindow amoebaActionModelUI1;
	
	AMOEBA amoebaActionModel2;
	VUIMulti amoebaActionModelVUI2;
	AmoebaMultiUIWindow amoebaActionModelUI2;
	
	
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
		
		amoebaSpatialRewardVUI = new VUIMulti("2D REWARD");
		amoebaSpatialRewardUI = new AmoebaMultiUIWindow("SPATIAL REWARD", amoebaSpatialRewardVUI, null);
		
		amoebaActionModelVUI1 = new VUIMulti("2D");
		amoebaActionModelUI1 = new AmoebaMultiUIWindow("ACTION 1 MODEL", amoebaActionModelVUI1, null);
		
		amoebaActionModelVUI2 = new VUIMulti("2D");
		amoebaActionModelUI2 = new AmoebaMultiUIWindow("ACTION 2 MODEL", amoebaActionModelVUI2, null);
		

		
		
		// Exemple for adding a tool in the toolbar
		Slider slider = new Slider(0.01, 0.1, mappingErrorAllowed);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println("new Value "+newValue);
				mappingErrorAllowed = (double)newValue;
				amoebaSpatialReward.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
			}
		});
		amoebaSpatialRewardUI.addToolbar(slider);
		
		Button btn = new Button();
		btn.setText("AI");
		btn.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				studiedSystem.setControl(true);
				
			}
		});
		
		amoebaSpatialRewardUI.addToolbar(btn);
		
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
                	amoebaSpatialReward = setupSpatialReward();
                	amoebaActionModel1 = setupControlModel("1", amoebaActionModelUI1, amoebaActionModelVUI1);
                	amoebaActionModel2 = setupControlModel("2", amoebaActionModelUI2, amoebaActionModelVUI2);
                	
                	HashMap<String, AMOEBA> amoebas = new HashMap<String, AMOEBA>();
                	amoebas.put("a1", amoebaActionModel1);
                	amoebas.put("a2", amoebaActionModel2);
                	amoebas.put("spatialReward", amoebaSpatialReward);
                	studiedSystem.setControlModels(amoebas);
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
                    	studiedSystem.playOneStepWithControlModel();
                    	
                    	
                    	
                    	amoebaSpatialReward.learn(studiedSystem.getOutput());
                    	if(amoebaSpatialReward.getHeadAgent().isActiveLearning()) {
                    		studiedSystem.setActiveLearning(true);
                    		studiedSystem.setSelfRequest(amoebaSpatialReward.getHeadAgent().getSelfRequest()); //TODO self active ...
    						 
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
	
	
	
	private AMOEBA setupSpatialReward() {
		AMOEBA amoeba = new AMOEBA(amoebaSpatialRewardUI,  amoebaSpatialRewardVUI);
		studiedSystem = new ReinforcementManager2D(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
		amoeba.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		File file = new File("resources/twoDimensionsLauncher.xml");
		backupSystem.load(file);
		
		amoeba.saver = new SaveHelperImpl(amoeba, amoebaSpatialRewardUI);
		amoeba.allowGraphicalScheduler(true);
		amoeba.setRenderUpdate(true);		
		amoeba.data.learningSpeed = learningSpeed;
		amoeba.data.numberOfPointsForRegression = regressionPoints;
		amoeba.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
		amoeba.setReinforcement(true);
		World.minLevel = TRACE_LEVEL.DEBUG;
		
		
		return amoeba;
	}
	

	private AMOEBA setupControlModel(String action, AmoebaMultiUIWindow window, VUIMulti VUI) {
		ArrayList<Pair<String, Boolean>> sensors = new ArrayList<>();
		sensors.add(new Pair<String, Boolean>("p1", false));
		sensors.add(new Pair<String, Boolean>("p2", false));
		sensors.add(new Pair<String, Boolean>("p"+action+"Goal", false));
		File config;
		try {
			config = File.createTempFile("configControlModel", "xml");
			XmlConfigGenerator.makeXML(config, sensors);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
			return null; // now compilator know config is initialized
		}
		//File config = new File("resources/simpleReinManualTrained.xml");
		
		Log.defaultMinLevel = Log.Level.INFORM;
		World.minLevel = TRACE_LEVEL.ERROR;
		AMOEBA amoeba = new AMOEBA(window, VUI, config.getAbsolutePath(), null);
		amoeba.saver = new SaveHelperDummy();
		
		
		
		
		amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoeba.getEnvironment().setMappingErrorAllowed(0.025);
		
		return amoeba;
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
