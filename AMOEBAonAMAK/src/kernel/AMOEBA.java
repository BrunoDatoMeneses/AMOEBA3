package kernel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import agents.head.Head;
import agents.AmoebaAgent;
import agents.context.Context;
import agents.context.localModel.TypeLocalModel;
import agents.percept.Percept;
import fr.irit.smac.amak.Amas;
import fr.irit.smac.amak.Scheduling;
import mas.agents.context.Range;

public class AMOEBA extends Amas<World> {
	
	private Head head;
	
	private TypeLocalModel localModel = TypeLocalModel.MILLER_REGRESSION;
	
	private HashMap<String,Double> perceptionsAndActionState = new HashMap<String,Double>();
	private ArrayList<Context> lastModifiedContext = new ArrayList<Context>();

	private StudiedSystem studiedSystem;
	
	private boolean running = false;
	private boolean playOneStep = false;
	private boolean controlMode = false;
	private boolean useOracle = true;
	
	// Imported from World -----------
	private HashMap<String,Integer> numberOfAgents = new HashMap<String,Integer>();
	private boolean creationOfNewContext;
	private boolean loadPresetContext;
	//--------------------------------
	
	private File ressourceFile;
	

	/**
	 * Instantiates a new amoeba.
	 *
	 * @param studiedSystem the studied system
	 */
	/* Create an AMOEBA coupled with a studied system */
	public AMOEBA(StudiedSystem studiedSystem, World environment, File ressourceFile) {
		super(environment, Scheduling.DEFAULT);
		this.studiedSystem = studiedSystem;
		this.ressourceFile = ressourceFile;
	}
	
	@Override
	protected void onInitialAgentsCreation() {
		readRessourceFile(ressourceFile);
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
	 * Learn.
	 *
	 * @param actions the actions
	 */
	public void learn(HashMap<String, Double> perceptionsActionState) {
		setPerceptionsAndActionState(perceptionsActionState);
		scheduler.run(); 
	}
	
	/**
	 * Request.
	 *
	 * @param actions the actions
	 * @return the double
	 */
	public double request(HashMap<String, Double> perceptionsActionState) {
		if(isUseOracle()) head.changeOracleConection();
		setPerceptionsAndActionState(perceptionsActionState);
		scheduler.run();
		head.changeOracleConection();
		return getAction();
	}
	
	//Part scheduler TODO => see utility
	//getScheduler
	//setScheduler

	//isRunning used by visualization, removed (for now)

	//setRunning used by visualization, removed (for now)
	
	//playOneStep used by visualization, removed (for now)

	//changeControl used by visualization, removed (for now)
	
	//TODO check usefulness in amak
	public StudiedSystem getStudiedSystem() {
		return studiedSystem;
	}

	//setStudiedSystem never used -> removed
	
	public double getAveragePredictionCriticity() {
		return getHeadAgent().getAveragePredictionCriticity();
	}
	
	public double getAction() {
		return head.getAction();
	}
	
	public Head getHeadAgent() {
		return head;
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
	 * Read resource file and generate the AMOEBA described.
	 *
	 * @param systemFile the file XML file describing the AMOEBA.
	 */
	private void readRessourceFile(File systemFile) {
	      SAXBuilder sxb = new SAXBuilder();
	      Document document;
		try {
			document = sxb.build(systemFile);
		    Element racine = document.getRootElement();
		    System.out.println(racine.getName());
		    
		    //learning = Boolean.parseBoolean(racine.getChild("Configuration").getChild("Learning").getAttributeValue("allowed")); never used -> removed
		    creationOfNewContext = Boolean.parseBoolean(racine.getChild("Configuration").getChild("Learning").getAttributeValue("creationOfNewContext"));
		    loadPresetContext = Boolean.parseBoolean(racine.getChild("Configuration").getChild("Learning").getAttributeValue("loadPresetContext"));
		    
		    
		    
		    // Initialize the sensor agents
		    for (Element element : racine.getChild("StartingAgents").getChildren("Sensor")){
		    	Percept s = new Percept(this);
		    	s.setName(element.getAttributeValue("Name"));	   
		    }

		    
		    
		    //Initialize the controller agents
		    for (Element element : racine.getChild("StartingAgents").getChildren("Controller")){
		    	Head a = new Head(this);
		    	a.setName(element.getAttributeValue("Name"));
		    	System.out.print("CREATION OF CONTEXT : " + this.creationOfNewContext);
		    	a.setNoCreation(!creationOfNewContext);
		    	this.head = a;
		    }

		    
		    /*Load preset context if no learning required*/
			if (loadPresetContext) {
				
			    for (Element element : racine.getChild("PresetContexts").getChildren("Context")){
			    	
			    	double[] start, end;
			    	int[] n;
			    	String[] percepts;
			    			
			    	double action;
			    	start = new double[element.getChildren("Range").size()];
			    	end = new double[element.getChildren("Range").size()];
			    	n = new int[element.getChildren("Range").size()];
			    	percepts = new String[element.getChildren("Range").size()];
	
			    	
			    	int i = 0;
				    for (Element elem : element.getChildren("Range")){
				    	start[i] = Double.parseDouble(elem.getAttributeValue("start"));
				    	end  [i] = Double.parseDouble(elem.getAttributeValue("end"));
				    	n    [i] = Integer.parseInt(elem.getAttributeValue("n"));
				    	percepts[i] = elem.getAttributeValue("Name");
				    	i++;
				    }
				    action = Double.parseDouble(element.getAttributeValue("Action"));
			    	
				   Head c = ((Head) agents.get(element.getAttributeValue("Controller")));
				    
				   createPresetContext(start,end,n,new int[0],0,action,c,percepts);
			    }
				
			}
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private void createPresetContext(double[] start, double[] end, int[] n, int[] pos, int iteration, double action, Head controller, String[] percepts) {
		for (int i = 0 ; i < pos.length ; i++) {
			System.out.print("  " + pos[i]) ;
		}		
		
		int[] newpos = new int[pos.length+1];
		
		for (int i = 0 ; i < pos.length ; i++) {
			newpos[i] = pos[i];
		}
		newpos[pos.length] = 0;
		
		for (int i = 0 ; i < n[iteration] ; i++) {
			if (iteration < n.length - 1) {
				createPresetContext(start, end,n, newpos, iteration+1,action,controller, percepts);
				newpos[pos.length]++;
			}
			else
			{
				newpos[pos.length]++;
				HashMap<Percept,Range> ranges = new HashMap<Percept,Range>();
				
				
				for(int j = 0 ; j < start.length ; j++) {
					double pas = (end[j]-start[j])/n[j];
					Range r = new Range(null, start[j] + (pas * newpos[j]), start[j] + (pas * (newpos[j] + 1)), 0, true, true, (Percept) agents.get(percepts[j]));
					ranges.put((Percept) agents.get(percepts[j]), r);
					
				}
			}
		}
	}

}
