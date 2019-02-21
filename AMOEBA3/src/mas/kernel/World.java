package mas.kernel;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import mas.ncs.NCS;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import mas.agents.Agent;
import mas.agents.percept.Percept;
import mas.agents.SystemAgent;
import mas.agents.context.Context;
import mas.agents.context.Range;
import mas.agents.head.Head;
import mas.agents.localModel.LocalModelAgent;
import mas.agents.localModel.LocalModelAverage;
import mas.agents.localModel.LocalModelFirstExp;
import mas.agents.localModel.LocalModelMillerRegression;
import mas.agents.localModel.TypeLocalModel;
//import mas.blackbox.BlackBox;

// TODO: Auto-generated Javadoc
/**
 * The Class World.
 */
public class World implements Serializable {


	Scheduler scheduler;
	//BlackBox blackbox;
	private transient AMOEBA amoeba;
	private StudiedSystem studiedSystem;
	private TypeLocalModel localModel = TypeLocalModel.MILLER_REGRESSION;
	
	private File fileToSerialize;
	
	private HashMap<String,SystemAgent> agents = new HashMap<String,SystemAgent>();
	private HashMap<NCS,Integer> allNCS = new HashMap<NCS,Integer>();
	private HashMap<NCS,Integer> thisLoopNCS = new HashMap<NCS,Integer>();
	private HashMap<String,Integer> numberOfAgents = new HashMap<String,Integer>();
	private HashMap<NCS,Integer> numberOfNCS = new HashMap<NCS,Integer>();
	
	private boolean learning; /*False -> Notify the World to load a preset of Context and to prevent creation of new context*/
	private boolean creationOfNewContext;
	private boolean loadPresetContext;
	private boolean startSerialization = false;
	
	public int testValue = 0;
	
	private double AVT_acceleration = 2;
	private double AVT_deceleration = 1./3.0;
	private double AVT_percentAtStart = 0.2;
	
	private double growingPercent = 0.2;
	
//	private int xGraphSize = 2500;
//	private int yGraphSize = 1500;
	
	
	//BUREAU
//	private int xGraphSize = 1600;
//	private int yGraphSize = 800;
	
	//REUNION
	private int xGraphSize = 1200;
	private int yGraphSize = 600;
	
	public double increment_up = 0.05;
	public double increment_down = 0.05;
	
	public int tickThreshol = 10000;
	
	private double contextCreationPercentage = 0.2;
	
	public World() {
		
	}
	
	public double getContextCreationPercentage() {
		return contextCreationPercentage;
	}
	
	public void trace(ArrayList<String> infos) {
		String message = "" +this.getScheduler().getTick();
		for(String info : infos) {
			message += " " + info;
		}
		System.out.println(message);
	}
	
	/**
	 * Instantiates a new world.
	 *
	 * @param scheduler the scheduler
	 * @param systemFile the system file
	 * @param blackbox the blackbox
	 */
	public World (Scheduler scheduler, File systemFile) {
		System.out.println("---Initialize the world---");
		
		this.scheduler = scheduler;
		//this.blackbox = blackbox;
		
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
		
		setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		
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
		    
		    learning = Boolean.parseBoolean(racine.getChild("Configuration").getChild("Learning").getAttributeValue("allowed"));
		    creationOfNewContext = Boolean.parseBoolean(racine.getChild("Configuration").getChild("Learning").getAttributeValue("creationOfNewContext"));
		    loadPresetContext = Boolean.parseBoolean(racine.getChild("Configuration").getChild("Learning").getAttributeValue("loadPresetContext"));
		    
		    
		    
		    // Initialize the sensor agents
		    for (Element element : racine.getChild("StartingAgents").getChildren("Sensor")){
		    	Percept s = new Percept(this);
		    	s.setName(element.getAttributeValue("Name"));
		    	scheduler.registerAgent(s);	   
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
			   
			   for (String s : percepts) {
	//				  ((Percept) (agents.get(s))).tree.top.resetMax();
		//			  ((Percept) (agents.get(s))).tree.top.allComputeMax();
			   }
		    	
		  /*  	NodexteController a = new NodexteController(this);
		    	a.setName(element.getAttributeValue("Name"));
		    	a.setBlackBoxInput((Input) blackbox.getBlackBoxAgents().get(element.getAttributeValue("InputTarget")));

		    	scheduler.registerAgent(a);	   
		    	agents.put(a.getName(), a);*/
		    }
			
		}
		
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
		

	}

	
	/**
	 * Creates the preset context.
	 *
	 * @param start the start
	 * @param end the end
	 * @param n the n
	 * @param pos the pos
	 * @param iteration the iteration
	 * @param action the action
	 * @param controller the controller
	 * @param percepts the percepts
	 */
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
					Range r = new Range(null, start[j] + (pas * newpos[j]), start[j] + (pas * (newpos[j] + 1)), 0, true, true, (Percept) agents.get(percepts[j]), this);
					ranges.put((Percept) agents.get(percepts[j]), r);
					
				}

				testValue++;
				//TODO : broken by criterion
				//Context c = new Context(this, true, ranges, action, controller, null, "_"+testValue+"_");

				//agents.put(c.getName(), c);

			}
		}
	}
	
	
	
	
	
	//----Get/Set----
	
	/**
	 * Gets the scheduler.
	 *
	 * @return the scheduler
	 */
	public Scheduler getScheduler() {
		return scheduler;
	}

	/**
	 * Sets the scheduler.
	 *
	 * @param scheduler the new scheduler
	 */
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	/**
	 * Sets the black box.
	 *
	 * @param blackbox the new black box
	 */
//	public void setBlackBox(BlackBox blackbox) {
//		this.blackbox = blackbox;
//	}

	/**
	 * Gets the blackbox.
	 *
	 * @return the blackbox
	 */
//	public BlackBox getBlackbox() {
//		return blackbox;
//	}

	/**
	 * Gets the agents.
	 *
	 * @return the agents
	 */
	public HashMap<String, SystemAgent> getAgents() {
		return agents;
	}

	/**
	 * Sets the agents.
	 *
	 * @param agents the agents
	 */
	public void setAgents(HashMap<String, SystemAgent> agents) {
		this.agents = agents;
	}

	/**
	 * Sets the blackbox.
	 *
	 * @param blackbox the new blackbox
	 */
//	public void setBlackbox(BlackBox blackbox) {
//		this.blackbox = blackbox;
//	}
	
	/**
	 * Gets the all agent instance of.
	 *
	 * @param cl the cl
	 * @return the all agent instance of
	 */
	public ArrayList<? extends Agent> getAllAgentInstanceOf(Class<? extends Agent> cl) {
		ArrayList<Agent> agentsList = new ArrayList<Agent>();
		for(String key : agents.keySet()) {
			Agent a = agents.get(key);
			if (a.getClass().equals(cl)) {
				agentsList.add(a);
			}
		}
		return agentsList;
	}

	/**
	 * Start agent.
	 *
	 * @param a the a
	 */
	public void startAgent(SystemAgent a) {
		scheduler.registerAgent(a);	   
    	agents.put(a.getName(), a);
	}

	/**
	 * Gets the all NCS.
	 *
	 * @return the all NCS
	 */
	public HashMap<NCS, Integer> getAllNCS() {
		return allNCS;
	}

	/**
	 * Sets the all NCS.
	 *
	 * @param allNCS the all NCS
	 */
	public void setAllNCS(HashMap<NCS, Integer> allNCS) {
		this.allNCS = allNCS;
	}

	/**
	 * Gets the this loop NCS.
	 *
	 * @return the this loop NCS
	 */
	public HashMap<NCS, Integer> getThisLoopNCS() {
		return thisLoopNCS;
	}

	/**
	 * Sets the this loop NCS.
	 *
	 * @param thidLoopNCS the thid loop NCS
	 */
	public void setThisLoopNCS(HashMap<NCS, Integer> thidLoopNCS) {
		this.thisLoopNCS = thidLoopNCS;
	}

	/**
	 * Raise NCS.
	 *
	 * @param ncs the ncs
	 */
	public void raiseNCS(NCS ncs) {
		thisLoopNCS.put(ncs, thisLoopNCS.get(ncs) + 1);
		
		if (ncs.equals(NCS.CONTEXT_CONFLICT_FALSE) || ncs.equals(NCS.HEAD_INCOMPETENT)) {
			NCS.a = true;
		}
			}

	/**
	 * Destroy.
	 *
	 * @param cl the cl
	 */
	public void destroy(Class<Context> cl) {
		for(String key : agents.keySet()) {
			Agent a = agents.get(key);
			if (a.getClass().equals(cl)) {
				scheduler.killAgent(a);
			}
		}		
	}
	
	
	
	/**
	 * Gets the number of agents.
	 *
	 * @return the number of agents
	 */
	public HashMap<String, Integer> getNumberOfAgents() {
		return numberOfAgents;
	}

	/**
	 * Sets the number of agents.
	 *
	 * @param numberOfAgents the number of agents
	 */
	public void setNumberOfAgents(HashMap<String, Integer> numberOfAgents) {
		this.numberOfAgents = numberOfAgents;
	}

	/**
	 * Change agent number.
	 *
	 * @param x the x
	 * @param cl the cl
	 */
	public void changeAgentNumber(int x, String cl) {
		if (numberOfAgents.containsKey(cl)) {
			numberOfAgents.put(cl, numberOfAgents.get(cl) + x);
		} else {
			numberOfAgents.put(cl, x);
		}
	}
	
	/**
	 * Change NCS number.
	 *
	 * @param x the x
	 * @param ncs the ncs
	 */
	public void changeNCSNumber(int x, NCS ncs) {
		if (numberOfNCS.containsKey(ncs)) {
			numberOfNCS.put(ncs, numberOfNCS.get(ncs) + x);
		} else {
			numberOfNCS.put(ncs, x);
		}
		

	}

	/**
	 * Gets the number of NCS.
	 *
	 * @return the number of NCS
	 */
	public HashMap<NCS,Integer> getNumberOfNCS() {
		return numberOfNCS;
	}

	/**
	 * Sets the number of NCS.
	 *
	 * @param numberOfNCS the number of NCS
	 */
	public void setNumberOfNCS(HashMap<NCS,Integer> numberOfNCS) {
		this.numberOfNCS = numberOfNCS;
	}

	/**
	 * Kill.
	 *
	 * @param agent the agent
	 */
	public void kill(Agent agent) {
		agents.remove(agent.getName());
		changeAgentNumber(-1,agent.getClass().getSimpleName());
		scheduler.killAgent(agent);
	}

	/**
	 * Export as picture.
	 *
	 * @param pasX the pas X
	 * @param startX the start X
	 * @param endX the end X
	 * @param pX the p X
	 * @param pasY the pas Y
	 * @param startY the start Y
	 * @param endY the end Y
	 * @param pY the p Y
	 * @param minTol the min tol
	 * @param maxTol the max tol
	 */
	//TODO : old function, but interesting to re-implement
	public void exportAsPicture(int pasX, double startX, double endX, Percept pX, int pasY, double startY, double endY, Percept pY, double minTol, double maxTol) {
		BufferedImage img = new BufferedImage(pasX, pasY, BufferedImage.TYPE_INT_ARGB);
		//img.setRGB(x, y,Color.red.getRGB());
		double incrX = (endX-startX)/pasX;
		double incrY = (endY-startY)/pasY;
		ArrayList<Range> rx = new ArrayList<Range>(), ry = new ArrayList<Range>();
		ArrayList<Context> cx, cy;
		
		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		double actions[][] = new double[pasX][pasY];

		for (int x = 0 ; x < pasX ; x++) {
			for (int y = 0 ; y < pasY ; y++) {
				rx.clear();
				ry.clear();
				
	//			pX.getTree().search(pX.getTree().top, startX + (x*incrX), rx);
	//			pY.getTree().search(pY.getTree().top, startY + (y*incrY), ry);
				
			//	System.out.println("px : " + rx);
			//	System.out.println("py : " + ry);
				
				cx = new ArrayList<Context>();
				cy = new ArrayList<Context>();
				
				for (Range r : rx) {
					cx.add(r.getContext());
				}
				for (Range r : ry) {
					cy.add(r.getContext());
				}
				
				for (Context c : cx) {
					if (cy.contains(c)) {
	//					double action = c.getActionProposal2D(pX, pY, startX + (x*incrX), startY + (y*incrY));
				//		if (action > 1) action = 1;
				//		if (action < -1) action = -1;
					//	double action = c.getAction();
	//					if (action > max && maxTol != Double.NaN) max = action;
	//					if (action < min && minTol != Double.NaN ) min = action;
	//					actions[x][y] = action;
					break;
					
					}
				}
				

				
	//			System.out.println(x + " : " + y);
			}
			

		}
		
		if (maxTol != Double.NaN) max = maxTol;
		if (minTol != Double.NaN ) min = minTol;
		
		for (int i = 0 ; i < pasX ; i++) {
			for (int j = 0 ; j < pasY ; j++) {
	//			System.out.println(actions[i][j] + "  " + max + "   " + min);
	//			System.out.println((int) (((actions[i][j] - min)/(max - min))*255));
				if (maxTol != Double.NaN && actions[i][j] > maxTol) {
					img.setRGB(i, j, Color.red.getRGB());
				} else if (minTol != Double.NaN && actions[i][j] < minTol){
					img.setRGB(i, j, Color.LIGHT_GRAY.getRGB());
				} else {
					img.setRGB(i, j, new Color(0,0, (int) (((actions[i][j] - min)/(max - min))*255)).getRGB());
				}
			}
		}
		
		System.out.println("Min : " + min + "    " + "Max : " + max);
		/*Write the output file*/
	    File outputfile = new File("model_map.png");
	    try {
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Change oracle connection.
	 */
	public void changeOracleConnection() {
		scheduler.changeOracleConection();
		
	}

	/**
	 * Gets the selectable context.
	 *
	 * @return the selectable context
	 */
	public ArrayList<Agent> getSelectableContext() {
		ArrayList<Percept> percepts = getAllPercept();
		ArrayList<ArrayList<Agent>> listoflist = new ArrayList<ArrayList<Agent>>();
		for (Percept p : percepts) {
			listoflist.add(p.getActivatedContext());
		}
		
		return getIntersection(listoflist);
	}
	
	/**
	 * Gets the intersection.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @return the intersection
	 */
	static public <T> ArrayList<T> getIntersection(ArrayList<ArrayList<T>> list) {
		ArrayList<T> newList = new ArrayList<T>();
		Boolean intersect;
		for (T elem : list.get(0)) {
			intersect = true;
			for (int i = 1 ; i < list.size() ; i++) {
				if (!list.get(i).contains(elem)) {
					intersect = false;
				}
			}
			if (intersect) {
				newList.add(elem);
			}
		}
		return newList;	
	}

	/**
	 * Checks if is start serialization.
	 *
	 * @return true, if is start serialization
	 */
	public boolean isStartSerialization() {
		return startSerialization;
	}

	/**
	 * Sets the start serialization.
	 *
	 * @param startSerialization the start serialization
	 * @param file the file
	 */
	public void setStartSerialization(boolean startSerialization, File file) {
		fileToSerialize = file;
		this.startSerialization = startSerialization;
	}

	/**
	 * Gets the amoeba.
	 *
	 * @return the amoeba
	 */
	public AMOEBA getAmoeba() {
		return amoeba;
	}

	/**
	 * Sets the amoeba.
	 *
	 * @param amoeba the new amoeba
	 */
	public void setAmoeba(AMOEBA amoeba) {
		this.amoeba = amoeba;
		this.studiedSystem = amoeba.getStudiedSystem();
	}

	/**
	 * Builds the local model.
	 *
	 * @param context the context
	 * @return the local model agent
	 */
	public LocalModelAgent buildLocalModel(Context context) {
		
		if (localModel == TypeLocalModel.MILLER_REGRESSION) {
			return new LocalModelMillerRegression(this);
		}
		if (localModel == TypeLocalModel.FIRST_EXPERIMENT) {
			return new LocalModelFirstExp(this);
		}
		if (localModel == TypeLocalModel.AVERAGE) {
			return new LocalModelAverage(this);
		}
		return null;
	}

	/**
	 * Gets the file to serialize.
	 *
	 * @return the file to serialize
	 */
	public File getFileToSerialize() {
		return fileToSerialize;
	}

	/**
	 * Gets the local model.
	 *
	 * @return the local model
	 */
	public TypeLocalModel getLocalModel() {
		return localModel;
	}

	/**
	 * Define the kind of local model used by newly created Context Agent.
	 * Ancient name of local model was proposition function.
	 *
	 * @param localModel the new local model
	 */
	public void setLocalModel(TypeLocalModel localModel) {
		this.localModel = localModel;
	}
	
	/**
	 * Gets the all percept.
	 *
	 * @return the all percept
	 */
	public ArrayList<Percept> getAllPercept() {
		return scheduler.getPercepts();
	}

	/**
	 * Gets the studied system.
	 *
	 * @return the studied system
	 */
	public StudiedSystem getStudiedSystem() {
		return studiedSystem;
	}
	
	/**
	 * Input stream to file.
	 *
	 * @param input the input
	 * @param file the file
	 */
	private void inputStreamToFile(InputStream input, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte buf[] = new byte[1024];
			int len;
			while((len = input.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Gets the AV T acceleration.
	 *
	 * @return the AV T acceleration
	 */
	public double getAVT_acceleration() {
		return AVT_acceleration;
	}


	/**
	 * Sets the AV T acceleration.
	 *
	 * @param aVT_acceleration the new AV T acceleration
	 */
	public void setAVT_acceleration(double aVT_acceleration) {
		AVT_acceleration = aVT_acceleration;
	}


	/**
	 * Gets the AV T deceleration.
	 *
	 * @return the AV T deceleration
	 */
	public double getAVT_deceleration() {
		return AVT_deceleration;
	}


	/**
	 * Sets the AV T deceleration.
	 *
	 * @param aVT_deceleration the new AV T deceleration
	 */
	public void setAVT_deceleration(double aVT_deceleration) {
		AVT_deceleration = aVT_deceleration;
	}


	/**
	 * Gets the AV T percent at start.
	 *
	 * @return the AV T percent at start
	 */
	public double getAVT_percentAtStart() {
		return AVT_percentAtStart;
	}


	/**
	 * Sets the AV T percent at start.
	 *
	 * @param aVT_percentAtStart the new AV T percent at start
	 */
	public void setAVT_percentAtStart(double aVT_percentAtStart) {
		AVT_percentAtStart = aVT_percentAtStart;
	}

	public double getContextGrowingPercent() {
		return growingPercent;
	}
	
	public int getXgraphSize() {
		return xGraphSize;
	}
	
	public int getYgraphSize() {
		return yGraphSize;
	}
	
	public void setIncrements(double value) {
		increment_up = value;
		increment_down = value;
	}
	
	public double getIncrements() {
		return increment_up ;
	}
	
	public double getNeighborhood(Context ctxt, Percept pct) {
		//return 2*ctxt.getRanges().get(pct).getRadius();
		return pct.getRadiusContextForCreation();
	}
	
}
