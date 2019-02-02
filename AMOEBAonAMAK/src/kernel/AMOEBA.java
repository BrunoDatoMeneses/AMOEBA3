package kernel;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;

import agents.head.Head;
import agents.AmoebaAgent;
import agents.context.Context;
import agents.localModel.TypeLocalModel;
import agents.percept.Percept;
import fr.irit.smac.amak.Amas;
import fr.irit.smac.amak.Scheduling;

public class AMOEBA extends Amas<World> {
	private ArrayList<AmoebaAgent> agents = new ArrayList<AmoebaAgent>();
	private ArrayList<AmoebaAgent> heads = new ArrayList<AmoebaAgent>();
	private ArrayList<AmoebaAgent> contexts = new ArrayList<AmoebaAgent>();
	private ArrayList<Percept> percepts = new ArrayList<Percept>();
	
	private TypeLocalModel localModel = TypeLocalModel.MILLER_REGRESSION;
	
	private HashMap<String,Double> perceptionsAndActionState = new HashMap<String,Double>();
	private ArrayList<Context> lastModifiedContext = new ArrayList<Context>();

	private StudiedSystem studiedSystem;
	
	private boolean running = false;
	private boolean playOneStep = false;
	private boolean controlMode = false;
	private boolean useOracle = true;
	
	
	

	/**
	 * Instantiates a new amoeba.
	 *
	 * @param studiedSystem the studied system
	 */
	/* Create an AMOEBA coupled with a studied system */
	public AMOEBA(StudiedSystem studiedSystem, World environment) {
		super(environment, Scheduling.DEFAULT);
		this.studiedSystem = studiedSystem;
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
		getHeadAgent().setDataForErrorMargin(errorAllowed,augmentationFactorError,diminutionFactorError,minErrorAllowed,nConflictBeforeAugmentation,nSuccessBeforeDiminution);
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
		getHeadAgent().setDataForInexactMargin(inexactAllowed, augmentationInexactError, diminutionInexactError, minInexactAllowed, nConflictBeforeInexactAugmentation, nSuccessBeforeInexactDiminution);
	}
	
	/**
	 * Sets the local model.
	 *
	 * @param model the new local model
	 */
	public void setLocalModel(TypeLocalModel model) {
		localModel=model;
	}
		
	/**
	 * Start.
	 *
	 * @param running the running
	 */
	public void start(boolean running) {
		//scheduler.start(running);
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

				//scheduler.run();//TODO
				if (studiedSystem != null) {
					if (isUseOracle()) {
						studiedSystem.playOneStep(Double.NaN);
					} else {
				//		System.out.println(scheduler.getAction());
						studiedSystem.playOneStep(getAction());
					}
				}
				
				if (playOneStep) {
					setRunning(false);
					playOneStep = false;
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
		setPerceptionsAndActionState(perceptionsActionState);
		//updateOutputAgentsValues(perceptionsActionState);//TODO
		//scheduler.run(); 
	}
	
	
	
	/**
	 * Request.
	 *
	 * @param actions the actions
	 * @return the double
	 */
	public double request(HashMap<String, Double> perceptionsActionState) {
		if(isUseOracle()) changeOracleConection();
		//updateOutputAgentsValues(actions);//TODO
		setPerceptionsAndActionState(perceptionsActionState);
		//scheduler.run();//TODO Maybe use to play agent => see in amak
		changeOracleConection();
		return getAction();
	}
	
	public double request2(HashMap<String, Double> perceptionsActionState) {
		if(isUseOracle()) changeOracleConection();
		//updateOutputAgentsValues(actions);//TODO
		setPerceptionsAndActionState(perceptionsActionState);
		//scheduler.run();//TODO//Maybe use to play agent => see in amak
		changeOracleConection();
		return getAction();
	}
	
	//Part scheduler TODO => see utility
	
	
	/**
	 * Gets the scheduler.
	 *
	 * @return the scheduler
	 */
	/*public Scheduler getScheduler() {
		return scheduler;
	}*/
	
	

	/**
	 * Sets the scheduler.
	 *
	 * @param scheduler the new scheduler
	 */
	//TODO
	/*public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
//			HashMap<String, BlackBoxAgent> outputList = scheduler.getWorld().getBlackbox().getBlackBoxAgents();
//			for (String s : outputList.keySet()) {
//				perceptionsAndActionState.put(outputList.get(s).getName(), (Output) outputList.get(s));
//				System.out.println("Name : " + outputList.get(s).getName());
//			}
		
	}*/
	
	

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
		//scheduler.setRunning(running);//TODO see in amak
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
	 *///TODO world into function => see in amak
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
	 *///TODO same as before
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
	
	
	//TODO see world interaction
	/**
	 * Sets the AV T acceleration.
	 *
	 * @param aVT_acceleration the new AV T acceleration
	 */
	/*public void setAVT_acceleration(double aVT_acceleration) {
		scheduler.getWorld().setAVT_acceleration(aVT_acceleration);
	}*/

	/**
	 * Sets the AV T deceleration.
	 *
	 * @param aVT_deceleration the new AV T deceleration
	 */
	/*public void setAVT_deceleration(double aVT_deceleration) {
		scheduler.getWorld().setAVT_deceleration(aVT_deceleration);
	}*/

	/**
	 * Sets the AV T percent at start.
	 *
	 * @param aVT_percentAtStart the new AV T percent at start
	 */
	/*
	public void setAVT_percentAtStart(double aVT_percentAtStart) {
		scheduler.getWorld().setAVT_percentAtStart(aVT_percentAtStart);
	}*/
	
	//TODO add functions in head
	public double getAveragePredictionCriticity() {
		return getHeadAgent().getAveragePredictionCriticity();
	}
	
	public double getNumberOfContextAgents() {
		return getContexts().size();
	}
	
	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	//TODO Manage with many controller
	public double getAction() {
		return ((Head) heads.get(0)).getAction();
	}
	
	/**
	 * Gets the head agent.
	 *
	 * @return the head agent
	 */
	public Head getHeadAgent() {
		return ((Head) heads.get(0));
	}

	/**
	 * Gets the percept by name.
	 *
	 * @param name the name
	 * @return the percept by name
	 */
	//TODO same for percept
	public Percept getPerceptByName(String name) {
		for (AmoebaAgent a : percepts) {
			if (a.getName().equals(name)) return (Percept) a;
		}
		return null;
	}

	/**
	 * Gets the variables.
	 *
	 * @return the variables
	 */
	public ArrayList<Percept> getPercepts() {
		return percepts;
	}

	/**
	 * Sets the variables.
	 *
	 * @param variables the new variables
	 */
	public void setPercepts(ArrayList<Percept> percepts) {
		this.percepts = percepts;
	}

	/**
	 * Gets the contexts.
	 *
	 * @return the contexts
	 */
	public ArrayList<AmoebaAgent> getContexts() {
		return contexts;
	}
	
	/**
	 * Gets the contexts as context.
	 *
	 * @return the contexts as context
	 */
	//TODO same for context
	public ArrayList<Context> getContextsAsContext() {
		ArrayList<Context>  c = new ArrayList<Context>();
		for (AmoebaAgent a : contexts) {
			c.add((Context)a);
		}
		return c;
	}

	/**
	 * Sets the contexts.
	 *
	 * @param contexts the new contexts
	 */
	public void setContexts(ArrayList<AmoebaAgent> contexts) {
		this.contexts = contexts;
	}
	
	public Context getContextByName(String name) {
		for(AmoebaAgent agt: contexts) {
			if(agt.getName().equals(name)) {
				return (Context)agt;
			}
		}
		return null;
	}
	
	public boolean isUseOracle() {
		return useOracle;
	}
	
	public void setPerceptionsAndActionState(HashMap<String,Double> perceptionsAndActions) {
		this.perceptionsAndActionState = perceptionsAndActions;
	}
	
	public Double getPerceptionsOrAction(String key) {
		return this.perceptionsAndActionState.get(key);	
	}
	
	/**
	 * Change oracle conection.
	 */
	public void changeOracleConection() {
		useOracle = !useOracle ;
		for (AmoebaAgent agent : heads) {
			((Head) agent).changeOracleConnection();
		}		
	}

}
