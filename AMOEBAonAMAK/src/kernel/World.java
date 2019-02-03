package kernel;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import fr.irit.smac.amak.Environment;
import fr.irit.smac.amak.Scheduling;
import agents.head.Head;
import agents.percept.Percept;
import ncs.NCS;

public class World extends Environment {

	//--------
	//TODO see if we keep them in World
	private HashMap<String,Integer> numberOfAgents = new HashMap<String,Integer>();
	private boolean creationOfNewContext;
	private boolean loadPresetContext;
	//--------
	
	private HashMap<NCS,Integer> numberOfNCS = new HashMap<NCS,Integer>();
	private HashMap<NCS,Integer> allNCS = new HashMap<NCS,Integer>();
	private HashMap<NCS,Integer> thisLoopNCS = new HashMap<NCS,Integer>();
	
	/**
	 * Instantiates a new world.
	 *
	 * @param systemFile the system file
	 */
	public World (File systemFile) {
		super(Scheduling.DEFAULT);
		System.out.println("---Initialize the world---");
		
		readRessourceFile(systemFile);
		
		numberOfAgents.put("Context", 0);
		numberOfAgents.put("Variable", 0);
		numberOfAgents.put("Controller", 0);
		numberOfAgents.put("Criterion", 0);
		
		for (NCS ncs : NCS.values()) {
			numberOfNCS.put(ncs, 0);			
		}

		
		for (NCS ncs : NCS.values()) {
			allNCS.put(ncs, 0);
			thisLoopNCS.put(ncs, 0);
		}
		
		System.out.println("---End initialize the world---");
	}
	
	/**
	 * Read ressource file.
	 *
	 * @param systemFile the system file
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
		    	amas.registerAgent(s);	   
		    	agents.put(s.getName(), s);
		    	//s.setSensor(blackbox.getBlackBoxAgents().get(element.getAttributeValue("Source")));
		    }

		    
		    
		    //Initialize the controller agents
		    for (Element element : racine.getChild("StartingAgents").getChildren("Controller")){
		    	Head a = new Head(this);
		    	a.setName(element.getAttributeValue("Name"));
		    	//a.setOracle( blackbox.getBlackBoxAgents().get(element.getAttributeValue("Oracle")));
		    	System.out.print("CREATION OF CONTEXT : " + this.creationOfNewContext);
		    	a.setNoCreation(!creationOfNewContext);
		    	
		    	scheduler.registerAgent(a);	   
		    	agents.put(a.getName(), a);

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
			//	    	System.out.println(start[i] + " " + end[i] + " " + n[i]);
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

}

