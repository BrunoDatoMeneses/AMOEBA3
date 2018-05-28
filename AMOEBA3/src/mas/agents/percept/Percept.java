package mas.agents.percept;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import mas.kernel.Config;
import mas.kernel.World;
import mas.agents.Agent;
import mas.agents.SystemAgent;
import mas.agents.context.Context;
import mas.agents.context.Range;
import mas.agents.messages.Message;
import mas.agents.messages.MessageType;
import mas.blackbox.BlackBoxAgent;

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
	public ArrayList<Context> validContextProjection = new ArrayList<Context>();
	public HashMap<String, PerceptOverlap> perceptOverlaps = new HashMap<String, PerceptOverlap>();
	public HashMap<String, ArrayList<Context>> sortedRanges = new HashMap<String, ArrayList<Context>>();
	//public ArrayList<Context> sortedStartRanges = new ArrayList<Context>();
	//public ArrayList<Context> sortedEndRanges = new ArrayList<Context>();
	
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
			a.setID(obj.getID());
			a.setDying(obj.isDying());
			a.setName(obj.getName());
			a.setMessages(obj.getMessages());
			a.setMessagesBin(obj.getMessagesBin());
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
			a.setID(obj.getID());
			a.setDying(obj.isDying());
			a.setName(obj.getName());
			a.setMessages(obj.getMessages());
			a.setMessagesBin(obj.getMessagesBin());
			this.activatedContext.add(obj);
		}

		this.min = p.min;
		this.max = p.max;
		this.isEnum = p.isEnum;
		
		this.contextProjections = new HashMap<Context, ContextProjection>();
		this.validContextProjection = new ArrayList<Context>();
		this.perceptOverlaps = new HashMap<String, PerceptOverlap>();
		
		this.sortedRanges.put("start", new ArrayList<Context>());
		this.sortedRanges.put("end", new ArrayList<Context>());

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
		validContextProjection = new ArrayList<Context>();
		for(ContextProjection contextProjection : contextProjections.values()) {
			if(contextProjection.contains(this.value)) {
				validContextProjection.add(contextProjection.getContex());
				System.out.println("Percept "+this.name+ " Context "+contextProjection.getContex().getName());
			}
		}
		
		for(Context context : validContextProjection) {
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
	
	/*
	 * ENDO
	 */
	
	public HashMap<String, ArrayList<Context>> getSortedRanges(){
		return sortedRanges;
	}
	
	public ArrayList<Context> getSortedRangesSubGroup(ArrayList<Context> subGroupOfContexts, String rangeString){
		ArrayList<Context> sortedRangesSubGroup = new ArrayList<Context>();
		
		for(Context ctxt : sortedRanges.get(rangeString)) {
			if(subGroupOfContexts.contains(ctxt)) {
				sortedRangesSubGroup.add(ctxt);
			}
		}	
		
		return sortedRangesSubGroup;
	}
	
	/*
	 * Sorted Ranges methods
	 */
	
	public void addContextSortedRanges(Context context) {
		
		if(sortedRanges.isEmpty()) {
			sortedRanges.put("start", new ArrayList<Context>());
			sortedRanges.put("end", new ArrayList<Context>());
		}
		if(sortedRanges.get("start").size()==0) {
			sortedRanges.get("start").add(context);
		}
		else {
			insertContextInSortedRanges(context, "start");
		}
		
		if(sortedRanges.get("end").size()==0) {
			sortedRanges.get("end").add(context);
		}
		else {
			insertContextInSortedRanges(context, "end");
		}
		
	}
	
	private void insertContextInSortedRanges(Context context, String range) {
		
		int i = 0;
		boolean inserted = false;
		while(i<sortedRanges.get(range).size() && !inserted) {
			if(getRangeProjection(context, range) < getRangeProjection(sortedRanges.get(range).get(i), range)) {
				sortedRanges.get(range).add(i, context);
				inserted = true;
			}
			i+=1;		
		}
		if(i==sortedRanges.get(range).size() && !inserted) {
			sortedRanges.get(range).add(context);
		}
		
	}
	
	public void deleteContextRanges(Context context) {
		sortedRanges.get("start").remove(context);
		sortedRanges.get("end").remove(context);
	}
	

	
	/*
	 * Context projection methods
	 */
	
	
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
	
	public void overlapNotification() {
		for(PerceptOverlap perceptOverlap : perceptOverlaps.values()) {
			ArrayList<Context> contexts = perceptOverlap.getContexts();
			contexts.get(0).setPerceptOverlap(this, contexts.get(1));
			contexts.get(1).setPerceptOverlap(this, contexts.get(0));
		}
	}

	public void overlapsDetection() {
		
		ArrayList<Context> computedContexts = new ArrayList<Context>(); 
		for(Context selectedContext : contextProjections.keySet()) {
			
			for(Context testedContext : contextProjections.keySet()) {
				
				if((testedContext != selectedContext) && (!computedContexts.contains(testedContext))){
					if(overlapBetweenContexts(selectedContext, testedContext)) {
						
						String overlapName = selectedContext.getName() + testedContext.getName();						
						HashMap<String, Double> overlapRanges = getOverlapRangesBetweenContexts(selectedContext, testedContext);
						
						PerceptOverlap overlap = new PerceptOverlap(selectedContext, testedContext, overlapRanges.get("start"), overlapRanges.get("end"), overlapName);
						perceptOverlaps.put(overlapName, overlap);
					}
				}
			}
			
			computedContexts.add(selectedContext);
		}
	}
	
	public boolean overlapBetweenContexts(Context context1, Context context2) {
		
		double contextStart1 = getStartRangeProjection(context1);
		double contextStart2 = getStartRangeProjection(context2);
		double contextEnd1 = getEndRangeProjection(context1);
		double contextEnd2 = getEndRangeProjection(context2);
		
		return ( (contextStart1< contextStart2 && contextStart2 <contextEnd1) || ((contextStart1< contextEnd2 && contextEnd2 <contextEnd1)) ) ;
		
	}
	
	public double getRangeProjection(Context context, String range) {
		if(range.equals("start")) {
			return context.getRanges().get(this).getStart();
		}
		else if(range.equals("end")) {
			return context.getRanges().get(this).getEnd();
		}
		else {
			return 0;
		}
		
	}
	
	public double getEndRangeProjection(Context context) {
		return context.getRanges().get(this).getEnd();
	}
	
	public double getStartRangeProjection(Context context) {
		return context.getRanges().get(this).getStart();
	}
	
	public HashMap<String, Double> getOverlapRangesBetweenContexts(Context context1, Context context2) {
		
		HashMap<String, Double> overlapRanges = new HashMap<String, Double>();	
		
		if( contextIncludedIn(context1, context2) ) {
			overlapRanges.put("start", getStartRangeProjection(context1));
			overlapRanges.put("end", getEndRangeProjection(context1));
		}
		else if(contextIncludedIn(context2, context1) ) {
			overlapRanges.put("start", getStartRangeProjection(context2));
			overlapRanges.put("end", getEndRangeProjection(context2));
		}
		else if(contextOrder(context1, context2)) {
			overlapRanges.put("start", getStartRangeProjection(context2));
			overlapRanges.put("end", getEndRangeProjection(context1));
		}
		else if(contextOrder(context2, context1)) {
			overlapRanges.put("start", getStartRangeProjection(context1));
			overlapRanges.put("end", getEndRangeProjection(context2));
		}
		
		
		return overlapRanges;
	}
	
	public boolean contextIncludedIn(Context includedContext, Context includingContext) {
		
		double includedContextStart = getStartRangeProjection(includedContext);
		double includingContextStart = getStartRangeProjection(includingContext);
		double includedContextEnd = getEndRangeProjection(includedContext);
		double includingContextEnd = getEndRangeProjection(includingContext);
		
		return ( (includingContextStart< includedContextStart && includedContextStart <includingContextEnd) && ((includingContextStart< includedContextEnd && includedContextEnd <includingContextEnd)) );
	}
	
	public boolean contextOrder(Context context1, Context context2) {
		
	double contextStart1 = getStartRangeProjection(context1);
	double contextStart2 = getStartRangeProjection(context2);
	double contextEnd1 = getEndRangeProjection(context1);
		
		return  (contextStart1< contextStart2 && contextStart2 <contextEnd1)  ;
	}
}
