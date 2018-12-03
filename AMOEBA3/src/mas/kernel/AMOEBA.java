package mas.kernel;

import java.util.ArrayList;
import java.util.HashMap;


import javax.swing.JFrame;

import visualization.view.JMainFrame;
import visualization.view.system.MainPanel;
import visualization.graphView.TemporalGraph;
import mas.init.LearningProvider;
import visualization.log.LogFile;
import visualization.log.LogMessageType;
import mas.agents.percept.Percept;

import mas.agents.context.Context;

import mas.agents.localModel.TypeLocalModel;
//import mas.blackbox.BlackBox;
//import mas.blackbox.BlackBoxAgent;
//import mas.blackbox.Output;
import visualization.csv.CsvFileWriter;


/**
 * The Class AMOEBA. 
 *
 * @author nigon
 */
public class AMOEBA extends Thread {
	
	private Scheduler scheduler;
	private MainPanel mainPanel = null;
	private LogFile logFile = new LogFile();
	private CsvFileWriter csvFile = null;
	private TemporalGraph temporalGraph = new TemporalGraph();
	private LearningProvider learningProvider = new LearningProvider();
	
	private StudiedSystem studiedSystem;
	
	private boolean running = false;
	private boolean playOneStep = false;
	private boolean controlMode = false;
	private boolean viewer = true;
	private boolean csv = true;
	
	//private HashMap<String,Output> perceptionsAndActionState = new HashMap<String,Output>();
	

	/**
	 * Instantiates a new amoeba.
	 *
	 * @param studiedSystem the studied system
	 */
	/* Create an AMOEBA coupled with a studied system */
	public AMOEBA(StudiedSystem studiedSystem) {
		this.studiedSystem = studiedSystem;
	}
	
	/**
	 * Instantiates a new amoeba.
	 *
	 * @param viewer the viewer
	 * @param scheduler the scheduler
	 * @param world the world
	 * @param blackBox the black box
	 */
	/* Create an AMOEBA from scheduler, world and blackbox */
	public AMOEBA(Boolean viewer, Scheduler scheduler, World world) {
		
		JFrame frame = null;
		
		printStartInfo();
		this.viewer = viewer;
		
		world.setAmoeba(this);
		scheduler.getHeadAgent().setDataForErrorMargin(1, 1, 1, 1, 1, 100);
		scheduler.getHeadAgent().setDataForInexactMargin(0.5, 1, 1, 0.5, 25, 100);
		
		
		if (this.viewer) {
			frame = new JMainFrame();
			mainPanel = new MainPanel();
			
			frame.setContentPane(mainPanel);			
			frame.setVisible(true);
		
			///world.setBlackBox(blackBox);
			///world.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
			///world.setAmoeba(this);
			
			///scheduler.getHeadAgent().setDataForErrorMargin(1, 1, 1, 1, 1, 100);
			///scheduler.getHeadAgent().setDataForInexactMargin(0.5, 1, 1, 0.5, 25, 100);

			mainPanel.setAMOEBA(this);
			mainPanel.setWorld(world);
			//mainPanel.setBlackBox(blackBox);
			
			frame.pack();
			
			scheduler.setView(mainPanel);	
			scheduler.setWorld(world);
			this.setScheduler(scheduler);
			this.start(false);
			scheduler.setRunning(true);
			
			mainPanel.setAMOEBA(this);
			mainPanel.setTemporalGraph(temporalGraph);
		
		} else {
			
			///world.setBlackBox(blackBox);
			///world.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
			///world.setAmoeba(this);
			
			///scheduler.getHeadAgent().setDataForErrorMargin(1, 1, 1, 1, 1, 100);
			///scheduler.getHeadAgent().setDataForInexactMargin(0.5, 1, 1, 0.5, 25, 100);
			
			scheduler.setView(mainPanel);	
			scheduler.setWorld(world);
			this.setScheduler(scheduler);
			this.start(false);
			scheduler.setRunning(true);
			
		}
		
		learningProvider.setAMOEBA(this);
		
		

	
		
		
	}
	
	/**
	 * Sets the data for error margin.
	 *
	 * @param errorAllowed the error allowed
	 * @param augmentationFactorError the augmentation factor error
	 * @param diminutionFactorError the diminution factor error
	 * @param minErrorAllowed the min error allowed
	 * @param nConflictBeforeAugmentation the n conflict before augmentation
	 * @param nSuccessBeforeDiminution the n success before diminution
	 */
	public void setDataForErrorMargin(double errorAllowed, double augmentationFactorError, double diminutionFactorError, double minErrorAllowed, int nConflictBeforeAugmentation, int nSuccessBeforeDiminution) {
		scheduler.getHeadAgent().setDataForErrorMargin(errorAllowed,augmentationFactorError,diminutionFactorError,minErrorAllowed,nConflictBeforeAugmentation,nSuccessBeforeDiminution);
	}
	
	/**
	 * Sets the data for inexact margin.
	 *
	 * @param inexactAllowed the inexact allowed
	 * @param augmentationInexactError the augmentation inexact error
	 * @param diminutionInexactError the diminution inexact error
	 * @param minInexactAllowed the min inexact allowed
	 * @param nConflictBeforeInexactAugmentation the n conflict before inexact augmentation
	 * @param nSuccessBeforeInexactDiminution the n success before inexact diminution
	 */
	public void setDataForInexactMargin(double inexactAllowed, double augmentationInexactError, double diminutionInexactError, double minInexactAllowed, int nConflictBeforeInexactAugmentation, int nSuccessBeforeInexactDiminution) {
		scheduler.getHeadAgent().setDataForInexactMargin(inexactAllowed, augmentationInexactError, diminutionInexactError, minInexactAllowed, nConflictBeforeInexactAugmentation, nSuccessBeforeInexactDiminution);
	}
	
	/**
	 * Sets the local model.
	 *
	 * @param model the new local model
	 */
	public void setLocalModel(TypeLocalModel model) {
		scheduler.getWorld().setLocalModel(model);
	}
		
	/**
	 * Start.
	 *
	 * @param running the running
	 */
	public void start(boolean running) {
		scheduler.start(running);
		this.running = running;
		super.start();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		System.out.println("starting run");
		while(true){
			
			if (!running) {
			    try {
			        Thread.sleep(0);
			    } catch (InterruptedException ignore) {
			    }
			}
			
			while (running) {

				if (studiedSystem != null) {
					readInput();
				}
				scheduler.run();
				if (studiedSystem != null) {
					if (scheduler.isUseOracle()) {
						studiedSystem.playOneStep(Double.NaN);
					} else {
				//		System.out.println(scheduler.getAction());
						studiedSystem.playOneStep(scheduler.getAction());
					}
				}
				
				if (playOneStep) {
					setRunning(false);
					playOneStep = false;
					getLogFile().messageToDebug("Play one step", 5, new LogMessageType[] {LogMessageType.INFORMATION});
				}
				
			}
			
		}
	}
	
	/**
	 * Learn.
	 *
	 * @param actions the actions
	 */
	public void learn(HashMap<String, Double> perceptionsActionState) {
		scheduler.setPerceptionsAndActionState(perceptionsActionState);
		//updateOutputAgentsValues(perceptionsActionState);
		scheduler.run();
	}
	
	
	
	/**
	 * Request.
	 *
	 * @param actions the actions
	 * @return the double
	 */
	public double request(HashMap<String, Double> perceptionsActionState) {
		if(scheduler.isUseOracle()) scheduler.changeOracleConection();
		//updateOutputAgentsValues(actions);
		scheduler.setPerceptionsAndActionState(perceptionsActionState);
		scheduler.run();
		scheduler.changeOracleConection();
		return scheduler.getAction();
	}
	
	public double request2(HashMap<String, Double> perceptionsActionState) {
		if(scheduler.isUseOracle()) scheduler.changeOracleConection();
		//updateOutputAgentsValues(actions);
		scheduler.setPerceptionsAndActionState(perceptionsActionState);
		scheduler.run();
		scheduler.changeOracleConection();
		return scheduler.getAction();
	}
	
	
	
	
	private void updateOutputAgentsValues(HashMap<String, Double> perceptionsAndAction) {
		for (String s : perceptionsAndAction.keySet()) {
			//perceptionsAndActionState.get(s).setValue(perceptionsAndAction.get(s));
		}
	}
	

	/**
	 * Gets the scheduler.
	 *
	 * @return the scheduler
	 */
	public Scheduler getScheduler() {
		return scheduler;
	}
	
	/**
	 * Read input.
	 */
	public void readInput() {
		HashMap<String, Double> actions = studiedSystem.getOutput();
		for (String s : actions.keySet()) {
			//perceptionsAndActionState.get(s).setValue(actions.get(s));
		}
	}

	/**
	 * Sets the scheduler.
	 *
	 * @param scheduler the new scheduler
	 */
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
//			HashMap<String, BlackBoxAgent> outputList = scheduler.getWorld().getBlackbox().getBlackBoxAgents();
//			for (String s : outputList.keySet()) {
//				perceptionsAndActionState.put(outputList.get(s).getName(), (Output) outputList.get(s));
//				System.out.println("Name : " + outputList.get(s).getName());
//			}
		
	}

	/**
	 * Checks if is running.
	 *
	 * @return true, if is running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Sets the running.
	 *
	 * @param running the new running
	 */
	public void setRunning(boolean running) {
		this.running = running;
		scheduler.setRunning(running);
	}
	
	/**
	 * Play one step.
	 */
	public void playOneStep() {
		setRunning(true);
		playOneStep = true;
	}

	/**
	 * Change control.
	 */
	public void changeControl() {
		System.out.println("switch control mode to " + !controlMode);
		controlMode = !controlMode;
		studiedSystem.switchControlMode();
	}
	
	
	
	/**
	 * Gets the all valid context but.
	 *
	 * @param values the values
	 * @param s the s
	 * @return the all valid context but
	 */
	/*public ArrayList<Context> getAllValidContextBut(HashMap<String, Double> values, String s) {
		ArrayList<ArrayList<Context>> contextsList = new ArrayList<ArrayList<Context>>();
		ArrayList<Percept> percepts = scheduler.getVariables();
		for (Percept p : percepts) {
			if (!p.getSensor().getName().equals(s)) {
				contextsList.add(p.getContextIncluding(values.get(p.getSensor().getName())));
			}
		}
		return World.getIntersection(contextsList);
	}*/
	
	/**
	 * Gets the studied system.
	 *
	 * @return the studied system
	 */
	/*public ArrayList<Context> getAllValidContextBut(HashMap<String, Double> values) {
		ArrayList<ArrayList<Context>> contextsList = new ArrayList<ArrayList<Context>>();
		ArrayList<Percept> percepts = scheduler.getVariables();
		for (Percept p : percepts) {
			contextsList.add(p.getContextIncluding(values.get(p.getSensor().getName())));
		}
		return World.getIntersection(contextsList);
	}*/
	
	public StudiedSystem getStudiedSystem() {
		return studiedSystem;
	}

	/**
	 * Sets the studied system.
	 *
	 * @param studiedSystem the new studied system
	 */
	public void setStudiedSystem(StudiedSystem studiedSystem) {
		this.studiedSystem = studiedSystem;
	}
	
	/**
	 * Gets the main panel.
	 *
	 * @return the main panel
	 */
	public MainPanel getMainPanel() {
		return mainPanel;
	}
	
	/**
	 * Gets the learning provider.
	 *
	 * @return the learning provider
	 */
	public LearningProvider getLearningProvider() {
		return learningProvider;
	}
	
	/**
	 * Gets the log file.
	 *
	 * @return the log file
	 */
	public LogFile getLogFile() {
		return logFile;
	}
	
	/**
	 * Gets the temporal graph.
	 *
	 * @return the temporal graph
	 */
	public TemporalGraph getTemporalGraph() {
		return temporalGraph;
	}

	/**
	 * Sets the remember state.
	 *
	 * @param rememberState the new remember state
	 */
	public void setRememberState(boolean rememberState) {
		if (viewer) {
			getMainPanel().disableCheckBoxRememberState(rememberState);
		}
	}
	
	/**
	 * Sets the generate CSV.
	 *
	 * @param csv the new generate CSV
	 */
	public void setGenerateCSV(boolean csv) {
		this.csv = csv;
		if (csv) {
			csvFile = new CsvFileWriter();
			csvFile.setWorld(scheduler.getWorld());
			csvFile.initHeaderCSV();
			scheduler.addScheduledItem(csvFile);
		}
		if (viewer) {
			getMainPanel().disableCheckBoxGenerateCsv(csv);
		}
	}
	
	/**
	 * Gets the csv.
	 *
	 * @return the csv
	 */
	public boolean getCSV() {
		return csv;
	}
	
	/**
	 * Gets the CSV file.
	 *
	 * @return the CSV file
	 */
	public CsvFileWriter getCSVFile() {
		return csvFile;
	}
	
	/**
	 * Prints the start info.
	 */
	private static void printStartInfo() {
		System.out.println(printWithDash("",30));
		System.out.println(printWithDash(Config.getProjectname(),30));
		System.out.println(printWithDash(Config.getVersionname(),30));
		System.out.println(printWithDash(Config.getVersionnumber(),30));
		System.out.println(printWithDash("Test with square",30));
		System.out.println(printWithDash("",30));
	}
	
	/**
	 * Prints the with dash.
	 *
	 * @param str the str
	 * @param l the l
	 * @return the string
	 */
	private static String printWithDash(String str, int l) {
		String newStr = str;
		int nDash = (l - str.length())/2;
		for (int i = 0 ; i < nDash; i++) {
			newStr = "-" + newStr + "-";
		}
		return newStr;
		
	}
	
	/**
	 * Sets the AV T acceleration.
	 *
	 * @param aVT_acceleration the new AV T acceleration
	 */
	public void setAVT_acceleration(double aVT_acceleration) {
		scheduler.getWorld().setAVT_acceleration(aVT_acceleration);
	}

	/**
	 * Sets the AV T deceleration.
	 *
	 * @param aVT_deceleration the new AV T deceleration
	 */
	public void setAVT_deceleration(double aVT_deceleration) {
		scheduler.getWorld().setAVT_deceleration(aVT_deceleration);
	}

	/**
	 * Sets the AV T percent at start.
	 *
	 * @param aVT_percentAtStart the new AV T percent at start
	 */
	public void setAVT_percentAtStart(double aVT_percentAtStart) {
		scheduler.getWorld().setAVT_percentAtStart(aVT_percentAtStart);
	}
	
	public double getAveragePredictionCriticity() {
		return scheduler.getHeadAgent().getAveragePredictionCriticity();
	}
	
	public double getNumberOfContextAgents() {
		return scheduler.getContexts().size();
	}

}