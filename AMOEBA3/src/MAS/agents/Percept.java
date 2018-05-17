package MAS.agents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import MAS.kernel.Config;
import MAS.kernel.World;
import MAS.agents.context.Context;
import MAS.agents.context.Range;
import MAS.agents.messages.Message;
import MAS.agents.messages.MessageType;
import MAS.blackbox.BlackBoxAgent;

// TODO: Auto-generated Javadoc
/**
 * Percept agent is in charge of the communication with the environment.
 * Each Percept agent must be connected to one data source.
 *
 */
public class Percept extends SystemAgent implements Serializable {

	
	private BlackBoxAgent sensor;
	protected ArrayList<Agent> targets = new ArrayList<Agent>();
	protected ArrayList<Agent> activatedContext = new ArrayList<Agent>();
	
	public HashMap<Context, ContextProjection> contextProjections = new HashMap<Context, ContextProjection>();
	public ArrayList<Context> validContexteProjection = new ArrayList<Context>();
	
	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;
	
	private double oldValue;
	private double value;
	private boolean isEnum = false;
	
	/**
	 * Instantiates a new percept.
	 *
	 * @param world the world
	 */
	public Percept(World world) {
		super(world);
	}
	
	/**
	 * Instantiates a new percept.
	 *
	 * @param p the p
	 */
	public Percept(Percept p) {
		super(p.world);
		this.ID = p.ID;
		this.name = p.name;
		this.isDying = p.isDying;
		this.messages = p.messages;
		this.messagesBin = p.messagesBin;
		this.world = p.world;
		
		this.oldValue = p.oldValue;
		this.value = p.value;
		this.sensor = p.sensor;
		
		this.targets = new ArrayList<Agent>();
		for(Agent obj: p.targets) {
			Agent a = new Agent() {
				
				@Override
				public void computeAMessage(Message m) {
					// TODO Auto-generated method stub
					obj.computeAMessage(m);
					
				}
			};
			a.setID(obj.ID);
			a.setDying(obj.isDying);
			a.setName(obj.name);
			a.setMessages(obj.messages);
			a.setMessagesBin(obj.messagesBin);
			this.targets.add(obj);
		}
		
		this.activatedContext = new ArrayList<Agent>();
		for(Agent obj: p.activatedContext) {
			Agent a = new Agent() {
				
				@Override
				public void computeAMessage(Message m) {
					// TODO Auto-generated method stub
					obj.computeAMessage(m);
					
				}
			};
			a.setID(obj.ID);
			a.setDying(obj.isDying);
			a.setName(obj.name);
			a.setMessages(obj.messages);
			a.setMessagesBin(obj.messagesBin);
			this.activatedContext.add(obj);
		}

		this.min = p.min;
		this.max = p.max;
		this.isEnum = p.isEnum;
		
		contextProjections = new HashMap<Context, ContextProjection>();
		validContexteProjection = new ArrayList<Context>();
	}


	/**This is the core behaviour of the percept agent.
	 * Most the other methods are remnant, used to use binary tree 
	 */
	public void play() {
		super.play();
				
		oldValue = value;
		value = sensor.getValue();
		ajustMinMax(); 
		computeContextProjectionValidity();
		

	}	
	
	public void computeContextProjectionValidity() {
		validContexteProjection = new ArrayList<Context>();
		for(ContextProjection contextProjection : contextProjections.values()) {
			if(contextProjection.contains(this.value)) {
				validContexteProjection.add(contextProjection.getContex());
				System.out.println("Percept "+this.name+ " Context "+contextProjection.getContex().getName());
			}
		}
		
		for(Context context : validContexteProjection) {
			context.setPerceptValidity(this);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see agents.Agent#computeAMessage(agents.messages.Message)
	 */
	@Override
	public void computeAMessage(Message m) {
		if (m.getType() == MessageType.REGISTER) {
			
			if (m.getSender() instanceof Context) {
				Context c = (Context) m.getSender();
	        	


		    	ArrayList<Range> list = new ArrayList<Range>();
			} else {
				targets.add(m.getSender());
			}
		} else if (m.getType() == MessageType.UNREGISTER) {
			targets.remove(m.getSender());
			if (m.getSender() instanceof Context) {
				Context c = (Context) m.getSender();



			}
		} else if (m.getType() == MessageType.REMOVE_FROM_TREE) {
			Context c = (Context) m.getSender();

			
		}else if (m.getType() == MessageType.ADD_TO_TREE) {
			Context c = (Context) m.getSender();


		}
		
	}
	
	/**
	 * Allow the percept to record the lower and higher value perceived.
	 */
	public void ajustMinMax() {
		if (value < min) min = value;
		if (value > max) max = value;
		
		/* In order to avoid big gap in min-max value in order to adapt with the system dynamic
		 * It's also a warranty to avoid to flaw AVT with flawed value */
		double dist = max - min;
		min += 0.05*dist;
		max -= 0.05*dist;
	}
	
	/**
	 * Gets the min max distance.
	 *
	 * @return the min max distance
	 */
	public double getMinMaxDistance() {
		if (min == Double.MAX_VALUE || max == Double.MIN_VALUE) return 0;
		return Math.abs(max - min);
	}
	
	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}

	/**
	 * Gets the activated context.
	 *
	 * @return the activated context
	 */
	public ArrayList<Agent> getActivatedContext() {
		return activatedContext;
	}

	/**
	 * Sets the activated context.
	 *
	 * @param activatedContext the new activated context
	 */
	public void setActivatedContext(ArrayList<Agent> activatedContext) {
		this.activatedContext = activatedContext;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * Gets the sensor.
	 *
	 * @return the sensor
	 */
	public BlackBoxAgent getSensor() {
		return sensor;
	}

	/**
	 * Sets the sensor.
	 *
	 * @param sensor the new sensor
	 */
	public void setSensor(BlackBoxAgent sensor) {
		this.sensor = sensor;
	}

	/* (non-Javadoc)
	 * @see agents.SystemAgent#getTargets()
	 */
	public ArrayList<? extends Agent> getTargets() {
		return targets;
	}

	/**
	 * Sets the targets.
	 *
	 * @param targets the new targets
	 */
	public void setTargets(ArrayList<Agent> targets) {
		this.targets = targets;
	}

	

	/**
	 * Gets the old value.
	 *
	 * @return the old value
	 */
	public double getOldValue() {
		return oldValue;
	}

	/**
	 * Sets the old value.
	 *
	 * @param oldValue the new old value
	 */
	public void setOldValue(double oldValue) {
		this.oldValue = oldValue;
	}




	/**
	 * Checks if is enum.
	 *
	 * @return true, if is enum
	 */
	public boolean isEnum() {
		return isEnum;
	}

	/**
	 * Sets the enum.
	 *
	 * @param isEnum the new enum
	 */
	public void setEnum(boolean isEnum) {
		this.isEnum = isEnum;
	}
	
	
	public void addContextProjection(Context context) {
		ContextProjection newContextProjection = new ContextProjection(this, context);
		contextProjections.put(context, newContextProjection);
	}
	
	public void deleteContextProjection(Context context) {
		contextProjections.remove(context);
	}
	
	public void updateContextProjection(Context context) {
		contextProjections.get(context).update();
	}

	
}
