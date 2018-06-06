package mas.kernel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import visualization.view.system.MainPanel;
import visualization.view.system.ScheduledItem;
import mas.agents.Agent;
import mas.agents.percept.Percept;
import mas.agents.context.Context;
import mas.agents.head.Head;
import mas.blackbox.BBFunction;
import mas.blackbox.Input;
import mas.blackbox.Output;

// TODO: Auto-generated Javadoc
/**
 * The Class Scheduler.
 */
public class Scheduler implements Serializable{

	transient private MainPanel view;
	private World world;
	
	private ArrayList<Agent> agents = new ArrayList<Agent>();
	private ArrayList<Agent> waitList = new ArrayList<Agent>();
	private ArrayList<Agent> killList = new ArrayList<Agent>();
	transient private ArrayList<ScheduledItem> scheduled = new ArrayList<ScheduledItem>();
	
	/*List of agents according to their roles*/
	private ArrayList<Percept> percepts = new ArrayList<Percept>();
	
	private ArrayList<Agent> contexts = new ArrayList<Agent>();
	private ArrayList<Agent> validContexts = new ArrayList<Agent>();
	private ArrayList<Agent> heads = new ArrayList<Agent>();
	private ArrayList<Agent> inputs = new ArrayList<Agent>();
	private ArrayList<Agent> functions = new ArrayList<Agent>();
	private ArrayList<Agent> outputs = new ArrayList<Agent>();
	private ArrayList<Context> alteredContexts = new ArrayList<Context>();

	private boolean running = false;
	private boolean waitForGUIUpdate = false;
	private boolean playOneStep = false;
	private boolean useOracle = true;
	
	private int tick;
	private int nextID = 0;
	private int moduloMeasure = 1000;
	
	private long time;
		
	
	/**
	 * Instantiates a new scheduler.
	 */
	public Scheduler() {
		agents = new ArrayList<Agent>();
		
		
	}
	
	/**
	 * Register agent.
	 *
	 * @param a the a
	 */
	public void registerAgent(Agent a) {
		waitList.add(a);
		addNewAgents();
	//	if (graphSystemPanel != null) graphSystemPanel.newAgent(a);
	}
	
	/**
	 * Start.
	 *
	 * @param running the running
	 */
	public void start(boolean running) {
		setRunning(running);
	}
	
	/**
	 * Adds the new agents.
	 */
	private void addNewAgents() {
		for (Agent a : waitList) {
			a.setID(nextID);
			nextID++;
			addToRightList(a);
		}
		waitList.clear();
	}
	
	/**
	 * Adds the to right list.
	 *
	 * @param a the a
	 */
	private void addToRightList(Agent a) {
		
		if (a instanceof Context) {
			if (((Context) a).isValid()) {
				validContexts.add(a);
			}
			else {
				contexts.add(a);
			}
		} 
		else if (a instanceof Percept) {
			percepts.add((Percept) a);
		} 
		else if (a instanceof Head) {
			heads.add(a);
		}
		else if (a instanceof Input) {
			inputs.add(a);
		}
		else if (a instanceof BBFunction) {
			functions.add(a);
		} 
		else if (a instanceof Output) {
			outputs.add(a);
		} 
		// System.out.println(a.getClass().toString());
	}
	
	/**
	 * Removes the from list.
	 *
	 * @param a the a
	 */
	private void removeFromList(Agent a) {
		
		if (a instanceof Context) {
			if (((Context) a).isValid()) {
				validContexts.remove(a);
			}
			else {
				contexts.remove(a);
			}
		} 
		else if (a instanceof Percept) {
			percepts.remove((Percept) a);
		} 
		else if (a instanceof Head) {
			heads.remove(a);
		}
		else if (a instanceof Input) {
			inputs.remove(a);
		}
		else if (a instanceof BBFunction) {
			functions.remove(a);
		} 
		else if (a instanceof Output) {
			outputs.remove(a);
		} 
		// System.out.println(a.getClass().toString());
	}
	
	
	/**
	 * Removes the valid context from list.
	 *
	 * @param agent the agent
	 */
	public void removeValidContextFromList(Context agent) {
		validContexts.remove(agent);
	}
	
	/**
	 * Learn.
	 */
	public void run() { 

		if (running) {//old launcher
			
			addNewAgents();//scheduler acces to new agents
			killAgents();
			playAllAgents();
			scheduledItemsAndView();		
			ticksUpdate();
			
			
			/*if (world.isStartSerialization()) {
				serialize();
				world.setStartSerialization(false, null);
				Config.print("End of the serialization", -100);
			}*/

			if (playOneStep) {
				playOneStep = false;
				running = false;
			}
			
			alteredContexts.clear();
		}
		
	//	System.out.println("Step : " + tick + " with " + world.getNumberOfAgents() + " agents.");

	}
	
	private void ticksUpdate() {
		tick++;
		if (tick%moduloMeasure == 0) {
			Config.print("Tick : " + tick + "  " + (System.nanoTime() - time) +"ns", -7);
		}
	}
	
	private void scheduledItemsAndView() {
		//Scheduled item
		if (scheduled == null) scheduled = new ArrayList<ScheduledItem>();
		
		ArrayList <ScheduledItem> tempList = new ArrayList<ScheduledItem>(scheduled);
		for (ScheduledItem item : tempList) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						item.update();
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
			}

		if (view != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						view.update();
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void playAllAgents() {
		//BB agents
		for (Agent agent : inputs) {
			agent.readMessage();
			agent.play();
		}
		
		for (Agent agent : functions) {
			agent.readMessage();
			agent.play();
		}
		
		for (Agent agent : outputs) {
			agent.readMessage();
			agent.play();
		}
		//
		

		for (Agent agent : percepts) {
			agent.readMessage();
			agent.play();
		}
		
		
		for (Agent agent : contexts) {
			agent.readMessage();
			agent.play();
			
		}

		
		for (Agent agent : heads) {
			agent.readMessage();
			agent.play();
		}



	}
	
	private void killAgents() {
		//kill agents to kill
		for (Agent agent : killList) {
			removeFromList(agent);
			if (agent instanceof Context) {
				removeValidContextFromList((Context) agent);
			}
		}
		killList.clear();
	}
	
	/**
	 * Kill agent.
	 *
	 * @param a the a
	 */
	public void killAgent(Agent a) {
		killList.add(a);
		//agents.remove(a);
	}

	/**
	 * Gets the agents.
	 *
	 * @return the agents
	 */
	public ArrayList<Agent> getAgents() {
		return agents;
	}

	/**
	 * Sets the agents.
	 *
	 * @param agents the new agents
	 */
	public void setAgents(ArrayList<Agent> agents) {
		this.agents = agents;
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
	}

	/**
	 * Gets the tick.
	 *
	 * @return the tick
	 */
	public int getTick() {
		return tick;
	}

	/**
	 * Sets the tick.
	 *
	 * @param tick the new tick
	 */
	public void setTick(int tick) {
		this.tick = tick;
	}

	/**
	 * Gets the next ID.
	 *
	 * @return the next ID
	 */
	public int getNextID() {
		return nextID;
	}

	/**
	 * Sets the next ID.
	 *
	 * @param nextID the new next ID
	 */
	public void setNextID(int nextID) {
		this.nextID = nextID;
	}

	/**
	 * Gets the view.
	 *
	 * @return the view
	 */
	public MainPanel getView() {
		return view;
	}

	/**
	 * Sets the view.
	 *
	 * @param view the new view
	 */
	public void setView(MainPanel view) {
		this.view = view;
	}

	/**
	 * Checks if is wait for GUI update.
	 *
	 * @return true, if is wait for GUI update
	 */
	public boolean isWaitForGUIUpdate() {
		return waitForGUIUpdate;
	}

	/**
	 * Sets the wait for GUI update.
	 *
	 * @param waitForGUIUpdate the new wait for GUI update
	 */
	public void setWaitForGUIUpdate(boolean waitForGUIUpdate) {
		this.waitForGUIUpdate = waitForGUIUpdate;
	}

	

	/**
	 * Gets the world.
	 *
	 * @return the world
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Sets the world.
	 *
	 * @param world the new world
	 */
	public void setWorld(World world) {
		this.world = world;
	}
	
	/**
	 * Adds the scheduled item.
	 *
	 * @param item the item
	 */
	public void addScheduledItem(ScheduledItem item) {
		if (scheduled == null) {
			scheduled = new ArrayList<ScheduledItem>();
		}
		scheduled.add(item);
	}

	/**
	 * Adds the altered context.
	 *
	 * @param context the context
	 */
	public void addAlteredContext(Context context) {
		alteredContexts.add(context);
	}

	/**
	 * Gets the altered contexts.
	 *
	 * @return the altered contexts
	 */
	public ArrayList<Context> getAlteredContexts() {
		return alteredContexts;
	}

	/**
	 * Sets the altered contexts.
	 *
	 * @param alteredContexts the new altered contexts
	 */
	public void setAlteredContexts(ArrayList<Context> alteredContexts) {
		this.alteredContexts = alteredContexts;
	}

	/**
	 * Change oracle conection.
	 */
	public void changeOracleConection() {
		useOracle = !useOracle ;
		for (Agent agent : heads) {
			((Head) agent).changeOracleConnection();
		}		
	}
	
	/**
	 * Serialize.
	 */
	public void serialize() {
		FileOutputStream fos;
		try {
			
			fos = new FileOutputStream(world.getFileToSerialize());
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			Object[] obj = new Object[3];
			// We store 3 objects in the file, world, observation list & boolean rememberState
			obj[0] = world;
			if (world.getAmoeba().getMainPanel() != null) {
				obj[1] = world.getAmoeba().getMainPanel().getRememberState();
				obj[2] = world.getAmoeba().getMainPanel().getObservationList();
			} else {
				obj[1] = false;
				obj[2] = null;
			}
	        oos.writeObject(obj);
	 
	        fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Gets the outputs.
	 *
	 * @return the outputs
	 */
	public ArrayList<Agent> getOutputs() {
		return outputs;
	}

	/**
	 * Sets the outputs.
	 *
	 * @param outputs the new outputs
	 */
	public void setOutputs(ArrayList<Agent> outputs) {
		this.outputs = outputs;
	}

	/**
	 * Checks if is use oracle.
	 *
	 * @return true, if is use oracle
	 */
	public boolean isUseOracle() {
		return useOracle;
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
	public Percept getPerceptByName(String name) {
		for (Agent a : percepts) {
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
	public ArrayList<Agent> getContexts() {
		return contexts;
	}
	
	/**
	 * Gets the contexts as context.
	 *
	 * @return the contexts as context
	 */
	public ArrayList<Context> getContextsAsContext() {
		ArrayList<Context>  c = new ArrayList<Context>();
		for (Agent a : contexts) {
			c.add((Context)a);
		}
		return c;
	}

	/**
	 * Sets the contexts.
	 *
	 * @param contexts the new contexts
	 */
	public void setContexts(ArrayList<Agent> contexts) {
		this.contexts = contexts;
	}
	
	public Context getContextByName(String name) {
		for(Agent agt: contexts) {
			if(agt.getName().equals(name)) {
				return (Context)agt;
			}
		}
		return null;
	}
	
	
}
