package mas.agents.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.util.Pair;

import mas.kernel.Config;
import mas.kernel.World;
import mas.ncs.NCS;
import mas.agents.AbstractPair;
import mas.agents.Agent;
import mas.agents.percept.Percept;
import mas.agents.head.Head;
import mas.agents.localModel.LocalModelAgent;
import mas.agents.localModel.LocalModelAverage;
import mas.agents.localModel.LocalModelFirstExp;
import mas.agents.localModel.LocalModelMillerRegression;
import mas.agents.localModel.TypeLocalModel;
import mas.agents.messages.Message;
import mas.agents.messages.MessageType;


// TODO: Auto-generated Javadoc
/**
 * The core agent of AMOEBA.
 * 
 * 
 */
public class Context extends AbstractContext implements Serializable,Cloneable{

	ArrayList<Percept> perceptSenders = new ArrayList<Percept>();
	
	private Head headAgent;
	private HashMap<Percept, Range> ranges = new HashMap<Percept, Range>();
	private ArrayList<Experiment> experiments = new ArrayList<Experiment>(); /*If memory is a concern, their is room for improvements here*/
	
	private LocalModelAgent localModel;
	
	private Double actionProposition = null;
	private String formulaLocalModel = null;
	
	private double action = -1.0;
	private double confidence = 0;
	
	private int nSelection = 0;
	private int maxActivationsRequired = 0;
	private int activations = 0;
	private int tickCreation;

	private boolean bestContext = false;
	private boolean valid = false;
	private boolean firstTimePeriod = true;
	
	private HashMap<Percept, Boolean> perceptValidities = new HashMap<Percept, Boolean>();
	private HashMap<Percept, Boolean> perceptNeighborhoodValidities = new HashMap<Percept, Boolean>();
	
	private ArrayList<Percept> nonValidPercepts = new ArrayList<Percept>();
	private ArrayList<Percept> nonValidNeightborPercepts = new ArrayList<Percept>();
	
	
	public HashMap<Context, HashMap<Percept, Boolean>> contextOverlapsByPercept = new HashMap<Context, HashMap<Percept, Boolean>>();
	public HashMap<Context, HashMap<Percept, Boolean>> contextOverlapsByPerceptSave = new HashMap<Context, HashMap<Percept, Boolean>>();
	public HashMap<Context,String> overlaps = new HashMap<Context,String>();
	public ArrayList<ContextOverlap> contextOverlaps = new ArrayList<ContextOverlap>();
	public ArrayList<ContextVoid> contextVoids = new ArrayList<ContextVoid>();
	
	
	public HashMap<Percept , HashMap<String, Context>> nearestNeighbours;
	
	public HashMap<Context , HashMap<Percept, Pair<Double,Integer>>> otherContextsDistancesByPercept;
	
	public HashMap<Percept , HashMap<String, ArrayList<Context>>> sortedPossibleNeighbours = new HashMap<Percept , HashMap<String, ArrayList<Context>>>();
	
	public ArrayList<Context> possibleNeighbours = new  ArrayList<Context>();
	public ArrayList<Context> neighbours = new ArrayList<Context>();
	
	

	/**
	 * The main constructor, used by AMOEBA to build new context agent.
	 * @param world : the world where the agent must live.
	 * @param head : the head agent associated with the next context agent
	 */
	public Context(World world, Head head) {
		super(world);
		buildContext(head);
	}
	
	public Context(World world, Head head, Context bestNearestContext) {
		super(world);
		buildContext(head, bestNearestContext);
		////System.out.println("=======================================================================" +this.getName() + " <-- " + bestNearestContext.getName());
		////System.out.println(this.toStringFull());
		////System.out.println(bestNearestContext.toStringFull());
	}
	
	private void buildContext (Head headAgent, Context bestNearestContext) {
		
		
		this.tickCreation = world.getScheduler().getTick();
		
		ArrayList<Percept> var = world.getAllPercept();
		Experiment firstPoint = new Experiment();
		this.headAgent = headAgent;
		
		action = this.headAgent.getOracleValue();
		maxActivationsRequired = var.size();
		
		for(Context ctxt : world.getScheduler().getContextsAsContext()) {
			
			ctxt.addContext(this);
		}
		
		
		
		for (Percept v : var) {
			Range r;
			
			
			////System.out.println("MAX RADIUS FOR CONTEXT CREATION AFTER TEST"  + v.getName() + " " + radius + " / " + (radius/v.getRadiusContextForCreation()));

			AbstractPair<Double, Double> radiuses = world.getScheduler().getHeadAgent().getMaxRadiusesForContextCreation(v);
			
			//System.out.println("MAX RADIUS FOR CONTEXT CREATION "  + v.getName() + " < " + radiuses.getA() + " , "  + radiuses.getB() + " > / < " + (radiuses.getA()/v.getRadiusContextForCreation()) + " , " + (radiuses.getB()/v.getRadiusContextForCreation()) + " >");
			
			r = new Range(this, v.getValue() - radiuses.getA(), v.getValue() + radiuses.getB(), 0, true, true, v, world);
					
			ranges.put(v, r);
			ranges.get(v).setValue(v.getValue());
			sendExpressMessage(null, MessageType.REGISTER, v);
			firstPoint.addDimension(v, v.getValue());
			
			v.addContextProjection(this);
			v.addContextSortedRanges(this);
		}

		expand();
		
		this.confidence = bestNearestContext.confidence;	
		if (bestNearestContext.world.getLocalModel() == TypeLocalModel.MILLER_REGRESSION) {
			
			this.localModel = new LocalModelMillerRegression(world);
			//this.formulaLocalModel = ((LocalModelMillerRegression) bestNearestContext.localModel).getFormula(bestNearestContext);
			double[] coef = ((LocalModelMillerRegression) bestNearestContext.localModel).getCoef();
			((LocalModelMillerRegression) this.localModel).setCoef(coef);
			this.actionProposition = ((LocalModelMillerRegression) bestNearestContext.localModel).getProposition(bestNearestContext);
			
		} else if (bestNearestContext.world.getLocalModel() == TypeLocalModel.FIRST_EXPERIMENT) {
			
			this.localModel = new LocalModelFirstExp(bestNearestContext.world);
			//this.formulaLocalModel = ((LocalModelFirstExp) bestNearestContext.localModel).getFormula(bestNearestContext);
			this.actionProposition = ((LocalModelFirstExp) bestNearestContext.localModel).getProposition(bestNearestContext);
			
		} else if (bestNearestContext.world.getLocalModel() == TypeLocalModel.AVERAGE) {
			
			this.localModel = new LocalModelAverage(bestNearestContext.world);
			//this.formulaLocalModel = ((LocalModelAverage) bestNearestContext.localModel).getFormula(bestNearestContext);
			this.actionProposition = ((LocalModelAverage) bestNearestContext.localModel).getProposition(bestNearestContext);
			
		}

		this.experiments = new ArrayList<Experiment>();
		for(Experiment obj: bestNearestContext.experiments) {
			Experiment exp = new Experiment();
			exp.setProposition(obj.getProposition());
			LinkedHashMap<Percept, Double> values = new LinkedHashMap<Percept, Double>();
			for(Entry<Percept, Double> entry : obj.getValues().entrySet()) {
				Percept percept = new Percept(entry.getKey());
				Double value = new Double(entry.getValue());
				values.put(percept, value);
			}
			exp.setValues(values);
			this.experiments.add(exp);
		}
		
		this.world.getScheduler().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));
		this.world.startAgent(this);
		
		perceptValidities = new HashMap<Percept, Boolean>();
		for(Percept percept : var) {
			perceptValidities.put(percept, false);
			perceptNeighborhoodValidities.put(percept, false);
		}
		
		contextOverlapsByPercept = new HashMap<Context, HashMap<Percept, Boolean>>();
		nearestNeighbours = new HashMap<Percept , HashMap<String, Context>>();
		otherContextsDistancesByPercept = new HashMap<Context , HashMap<Percept, Pair<Double,Integer>>>();
		
		for(Percept p : ranges.keySet()) {
			nearestNeighbours.put(p, new HashMap<String, Context>());
			
			sortedPossibleNeighbours.put(p, new HashMap<String, ArrayList<Context>>());
			
			nearestNeighbours.get(p).put("start", null);
			nearestNeighbours.get(p).put("end", null);
			
			sortedPossibleNeighbours.get(p).put("start", new ArrayList<Context>() );
			sortedPossibleNeighbours.get(p).put("end", new ArrayList<Context>() );
			
			
		}
		
		overlaps =  new HashMap<Context,String>();
	}
	
	
	
	Double getMaxRadiusForContextCreation(Percept pct) {
		Double maxRadius = world.getScheduler().getHeadAgent().getMaxRadiusForContextCreation(pct);
		////System.out.println("MAX RADIUS FOR CONTEXT CREATION "  + pct.getName() + " " + maxRadius + " / " + (maxRadius/pct.getRadiusContextForCreation()));
		return maxRadius;
		
		//return Math.abs(pct.getMinMaxDistance()) * world.contextCreationPercentage;
	}
	
	/**
	 * Builds the context.
	 *
	 * @param world the world
	 * @param headAgent the headAgent
	 */
	private void buildContext (Head headAgent) {
		
		
		this.tickCreation = world.getScheduler().getTick();
		
		ArrayList<Percept> var = world.getAllPercept();
		Experiment firstPoint = new Experiment();
		this.headAgent = headAgent;
		
		action = this.headAgent.getOracleValue();
		maxActivationsRequired = var.size();
		
		for(Context ctxt : world.getScheduler().getContextsAsContext()) {
			
			ctxt.addContext(this);
		}
		
		HashMap<Percept,Double> maxRadiusesForContextCreation = new HashMap<Percept,Double>();
		AbstractPair<Percept, Double> betterRadius = new AbstractPair<Percept, Double>(null,Double.NEGATIVE_INFINITY);
		AbstractPair<Percept, Double> worstRadius = new AbstractPair<Percept, Double>(null,Double.POSITIVE_INFINITY);
		
		for(Percept pct : var) {
			double maxRadius = getMaxRadiusForContextCreation(pct);
			double maxRadiusRatio = maxRadius / pct.getRadiusContextForCreation();
			maxRadiusesForContextCreation.put(pct, maxRadiusRatio);
			
			if(maxRadiusRatio > betterRadius.getB()) {
				betterRadius.setA(pct);
				betterRadius.setB(maxRadiusRatio);
			}
			if(maxRadiusRatio < worstRadius.getB()) {
				worstRadius.setA(pct);
				worstRadius.setB(maxRadiusRatio);
			}
		}
		
		
		
		for (Percept v : var) {
			Range r;
			Double radius;
			
			
			if(world.getScheduler().getHeadAgent().getActivatedNeighborsContexts().size() == 1) {
				if(betterRadius.getB() < 1.0 ) {
					if(betterRadius.getA() == v){
						radius = maxRadiusesForContextCreation.get(v)*v.getRadiusContextForCreation();
					}
					else {
						radius = v.getRadiusContextForCreation();
					}
				}
				else {
					radius = maxRadiusesForContextCreation.get(v)*v.getRadiusContextForCreation();
				}
			}
			else {
				if(worstRadius.getA() == v){
					radius = maxRadiusesForContextCreation.get(v)*v.getRadiusContextForCreation();
				}
				else {
					radius = v.getRadiusContextForCreation();
				}
			}


			////System.out.println("MAX RADIUS FOR CONTEXT CREATION AFTER TEST"  + v.getName() + " " + radius + " / " + (radius/v.getRadiusContextForCreation()));

			AbstractPair<Double, Double> radiuses = world.getScheduler().getHeadAgent().getMaxRadiusesForContextCreation(v);
			
			//System.out.println("MAX RADIUS FOR CONTEXT CREATION "  + v.getName() + " < " + radiuses.getA() + " , "  + radiuses.getB() + " > / < " + (radiuses.getA()/v.getRadiusContextForCreation()) + " , " + (radiuses.getB()/v.getRadiusContextForCreation()) + " >");
			
			r = new Range(this, v.getValue() - radiuses.getA(), v.getValue() + radiuses.getB(), 0, true, true, v, world);
			
			
			//r = new Range(this, v.getValue() - radius, v.getValue() + radius, 0, true, true, v, world);
			ranges.put(v, r);
			ranges.get(v).setValue(v.getValue());
			sendExpressMessage(null, MessageType.REGISTER, v);
			firstPoint.addDimension(v, v.getValue());
			
			v.addContextProjection(this);
			v.addContextSortedRanges(this);
		}
		
		displayRanges();
		expand();
		
		localModel = this.world.buildLocalModel(this);
		firstPoint.setProposition(this.headAgent.getOracleValue());
		experiments.add(firstPoint);
		localModel.updateModel(this);
		this.world.getScheduler().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));
		this.world.startAgent(this);
		
		perceptValidities = new HashMap<Percept, Boolean>();
		for(Percept percept : var) {
			perceptValidities.put(percept, false);
			perceptNeighborhoodValidities.put(percept, false);
		}
		
		contextOverlapsByPercept = new HashMap<Context, HashMap<Percept, Boolean>>();
		nearestNeighbours = new HashMap<Percept , HashMap<String, Context>>();
		otherContextsDistancesByPercept = new HashMap<Context , HashMap<Percept, Pair<Double,Integer>>>();
		
		for(Percept p : ranges.keySet()) {
			nearestNeighbours.put(p, new HashMap<String, Context>());
			
			sortedPossibleNeighbours.put(p, new HashMap<String, ArrayList<Context>>());
			
			nearestNeighbours.get(p).put("start", null);
			nearestNeighbours.get(p).put("end", null);
			
			sortedPossibleNeighbours.get(p).put("start", new ArrayList<Context>() );
			sortedPossibleNeighbours.get(p).put("end", new ArrayList<Context>() );
			
			
		}
		
		overlaps =  new HashMap<Context,String>();
		
		//System.out.println("NEW CONTEXT " + this.getName());
		displayRanges();
		
	}
	
	public void displayRanges() {
		for(Percept pct : ranges.keySet()) {
			//System.out.println(pct.getName() + " [ " + ranges.get(pct).getStart() + " , " + ranges.get(pct).getEnd() + " ]");
		}
	}
	
	public ArrayList<Context> getContextsOnAPerceptDirectionFromContextsNeighbors(ArrayList<Context> contextNeighbors, Percept pctDirection){
		ArrayList<Context> contexts = new ArrayList<Context>();
		
		boolean test = true;
		for(Context ctxtNeigbor: contextNeighbors) {
			for(Percept pct : ranges.keySet()) {
				if(pct!=pctDirection) {
					test = test && ( this.ranges.get(pct).distance(ctxtNeigbor.getRanges().get(pct))<0);
				}
				
				
			}
			if(test) {
				contexts.add(ctxtNeigbor);
			}
		}
		
		
		return contexts;
	}
	
	public ArrayList<Context> getContextsOnAPerceptDirectionFromContextsNeighbors(ArrayList<Context> contextNeighbors, Percept pctDirection, SpatialContext expandingContext){
		ArrayList<Context> contexts = new ArrayList<Context>();
		
		boolean test = true;
		for(Context ctxtNeigbor: contextNeighbors) {
			for(Percept pct : ranges.keySet()) {
				if(pct!=pctDirection) {
					//System.out.println("DISTANCE " + ctxtNeigbor.getName()+ " " + pct.getName()+ " " +  expandingContext.distance(pct, ctxtNeigbor.getRanges().get(pct)) + " " + ( expandingContext.distance(pct, ctxtNeigbor.getRanges().get(pct))<0)) ;
					test = test && ( expandingContext.distance(pct, ctxtNeigbor.getRanges().get(pct))<-0.0001);
				}
				
				
			}
			if(test) {
				contexts.add(ctxtNeigbor);
			}
			
			test = true;
		}
		
		
		return contexts;
	}
	
	public void expand() {
		
		ArrayList<Context> neighborsOnOneDirection;
		HashMap<Percept,SpatialContext>  alternativeContexts = new HashMap<Percept,SpatialContext>();
		double maxVolume = this.getVolume();
		double currentVolume;
		SpatialContext maxVolumeSpatialContext = null;

		for(Percept fixedPct: ranges.keySet()) {
			alternativeContexts.put(fixedPct, new SpatialContext(this));

			//System.out.println("FIXED PERCEPT :"  + fixedPct.getName());
			
			for(Percept pctDirectionForExpanding: ranges.keySet()) {

				
				if(pctDirectionForExpanding != fixedPct) {
					
					//System.out.println("DIRECTION PERCEPT :"  + pctDirectionForExpanding.getName());
					
					//System.out.println("ALL NEIGHBORS");
					for(Context ct : world.getScheduler().getHeadAgent().getActivatedNeighborsContexts()) {
						//System.out.println(ct.getName());
					}
					
					neighborsOnOneDirection = getContextsOnAPerceptDirectionFromContextsNeighbors(world.getScheduler().getHeadAgent().getActivatedNeighborsContexts(), pctDirectionForExpanding, alternativeContexts.get(fixedPct));
					
					
					//System.out.println("NEIGHBORS THROUGH " + pctDirectionForExpanding.getName());
					for(Context ct : neighborsOnOneDirection) {
						//System.out.println(ct.getName());
					}
					
					
					AbstractPair<Double,Double> expandingRadiuses = getMaxExpansionsForContextExpansionAfterCreation(neighborsOnOneDirection, pctDirectionForExpanding);
					//System.out.println("START : " + expandingRadiuses.getA() + " END " + expandingRadiuses.getB());
					alternativeContexts.get(fixedPct).expandEnd(pctDirectionForExpanding, expandingRadiuses.getB());
					alternativeContexts.get(fixedPct).expandStart(pctDirectionForExpanding, expandingRadiuses.getA());
				}
			}
			
			currentVolume = alternativeContexts.get(fixedPct).getVolume();
			if(currentVolume>maxVolume) {
				maxVolume = currentVolume;
				maxVolumeSpatialContext = alternativeContexts.get(fixedPct);
			}
			
		}
		if(maxVolumeSpatialContext != null) {
			matchSpatialContextRanges(maxVolumeSpatialContext);
		}
		
		
	}
	
	public void matchSpatialContextRanges(SpatialContext biggerContextForCreation) {
		
		for(Percept pct : ranges.keySet()) {
			
			double startExpansion = Math.abs(ranges.get(pct).getStart()-biggerContextForCreation.getStart(pct));
			double endExpansion = Math.abs(ranges.get(pct).getEnd()-biggerContextForCreation.getEnd(pct));
			//System.out.println("EXPANSION "  + pct.getName() +" < " + startExpansion + " , "  + endExpansion + " > / < " +pct.getMin() + " , " +pct.getMax() + " >");
			
			ranges.get(pct).setStart(biggerContextForCreation.getStart(pct));
			ranges.get(pct).setEnd(biggerContextForCreation.getEnd(pct));
			
		}
		
	}
	
	public AbstractPair<Double,Double> getMaxExpansionsForContextExpansionAfterCreation(ArrayList<Context> contextNeighborsInOneDirection, Percept pct) {
		AbstractPair<Double,Double> maxExpansions = new AbstractPair<Double,Double>(
				Math.min(pct.getRadiusContextForCreation(), 
						Math.abs(pct.getMin()- ranges.get(pct).getStart())),
				Math.min(pct.getRadiusContextForCreation(), 
						Math.abs(pct.getMax()-ranges.get(pct).getEnd())));
		//AbstractPair<Double,Double> maxExpansions = new AbstractPair<Double,Double>(pct.getRadiusContextForCreation(),pct.getRadiusContextForCreation());
		//AbstractPair<Double,Double> maxExpansions = new AbstractPair<Double,Double>(Math.abs(pct.getMin()- ranges.get(pct).getStart()),Math.abs(pct.getMax()-ranges.get(pct).getEnd()));

		////System.out.println("EXPANSION MIN MAX "  + pct.getName() +" " + pct.getValue() + " < " + pct.getMin() + " , "  + pct.getMax() + " > / < " + Math.abs(pct.getMin()- pct.getValue()) + " , " + Math.abs(pct.getMax()-pct.getValue()) + " >");
		
		double currentStartExpansion;
		double currentEndExpansion;
		
			
		//for(Context ctxt:partialNeighborContexts.get(pct)) {
		for(Context ctxt: contextNeighborsInOneDirection) {			
			
			//System.out.println("DISTANCE " + pct.getName() + " " + ctxt.getRanges().get(pct).centerDistance(pct.getValue()));
			if(ctxt.getRanges().get(pct).centerDistance(pct.getValue()) < 0) {
				// End radius
				
				currentEndExpansion = ctxt.getRanges().get(pct).distance(ranges.get(pct));
				//System.out.println("DISTANCE 2 " + pct.getName() + " " + ctxt.getRanges().get(pct).distance(ranges.get(pct)));
				////System.out.println(ctxt.getName() + " " + pct.getName() + " " + currentRadius + " " + maxRadius);
				if(currentEndExpansion < maxExpansions.getB() && currentEndExpansion >= -0.00001 ) {
					if(Math.abs(currentEndExpansion)<0.0001) {
						currentEndExpansion = 0.0;
					}
					maxExpansions.setB(currentEndExpansion); 
				}
			}
			
			if(ctxt.getRanges().get(pct).centerDistance(pct.getValue()) > 0) {
				// Start radius
				currentStartExpansion = ctxt.getRanges().get(pct).distance(ranges.get(pct));
				////System.out.println(ctxt.getName() + " " + pct.getName() + " " + currentRadius + " " + maxRadius);
				if(currentStartExpansion < maxExpansions.getA() && currentStartExpansion >= -0.00001 ) {
					if(Math.abs(currentStartExpansion)<0.0001) {
						currentEndExpansion = 0.0;
					}
					maxExpansions.setA(currentStartExpansion); 
				}
			}

			
			
		}
		
		return maxExpansions;
		
		
	}
	
	/**
	 * Instantiates a new context.
	 *
	 * @param c the c
	 */
	public Context(Context c) {
		super(c.world);
		this.tickCreation = c.tickCreation;
		this.ID = c.ID;
		this.name = c.name;
		this.messages = c.messages;
		this.messagesBin = c.messagesBin;
		this.isDying = c.isDying;
		
		this.ranges = new HashMap<Percept, Range>();
		for(Percept pct : c.ranges.keySet()) {
		   Percept percept = new Percept(pct);
		   Range range = new Range(c.ranges.get(pct));
		   this.ranges.put(percept, range);
		}
		this.headAgent = c.headAgent;
		this.action = c.action;
		this.nSelection = c.nSelection;
		this.bestContext = c.bestContext;
		this.valid = c.valid;
		this.firstTimePeriod = c.firstTimePeriod;
		this.perceptSenders = new ArrayList<Percept>();
		for(Percept obj: c.perceptSenders) {
			this.perceptSenders.add(new Percept(obj));
		}
		this.maxActivationsRequired = c.maxActivationsRequired;
		this.activations = c.activations;
		this.confidence = c.confidence;	
		if (c.world.getLocalModel() == TypeLocalModel.MILLER_REGRESSION) {
			this.localModel = new LocalModelMillerRegression(c.world);
			//this.formulaLocalModel = ((LocalModelMillerRegression) c.localModel).getFormula(c);
			double[] coef = ((LocalModelMillerRegression) c.localModel).getCoef();
			((LocalModelMillerRegression) this.localModel).setCoef(coef);
			this.actionProposition = ((LocalModelMillerRegression) c.localModel).getProposition(c);
		} else if (c.world.getLocalModel() == TypeLocalModel.FIRST_EXPERIMENT) {
			this.localModel = new LocalModelFirstExp(c.world);
			//this.formulaLocalModel = ((LocalModelFirstExp) c.localModel).getFormula(c);
			this.actionProposition = ((LocalModelFirstExp) c.localModel).getProposition(c);
		} else if (c.world.getLocalModel() == TypeLocalModel.AVERAGE) {
			this.localModel = new LocalModelAverage(c.world);
			//this.formulaLocalModel = ((LocalModelAverage) c.localModel).getFormula(c);
			this.actionProposition = ((LocalModelAverage) c.localModel).getProposition(c);
		}

		this.experiments = new ArrayList<Experiment>();
		for(Experiment obj: c.experiments) {
			Experiment exp = new Experiment();
			exp.setProposition(obj.getProposition());
			LinkedHashMap<Percept, Double> values = new LinkedHashMap<Percept, Double>();
			for(Entry<Percept, Double> entry : obj.getValues().entrySet()) {
				Percept percept = new Percept(entry.getKey());
				Double value = new Double(entry.getValue());
				values.put(percept, value);
			}
			exp.setValues(values);
			this.experiments.add(exp);
		}
		
	}
	
	/**
	 * Gets the value action proposition.
	 *
	 * @return the value action proposition
	 */
	public Double getValueActionProposition() {
		return this.actionProposition;
	}

	/* (non-Javadoc)
	 * @see agents.context.AbstractContext#computeAMessage(agents.messages.Message)
	 */
	@Override
	public void computeAMessage(Message m) {

		if (m.getType() == MessageType.VALIDATE) {
			
			computeAMessageTypeValidate(m);
			
		} else if (m.getType() == MessageType.SELECTION) { // ++ number of selection only

			computeAMessageTypeSelection(m);
		}
	}
	
	
	private void computeAMessageTypeValidate(Message m) {
		
		if (m.getSender() instanceof Percept) {
			boolean activate = (boolean) m.getContent();
			if (activate) {
				activations++;	
			}
			else {
				activations--;

				if (activations < 0)  {
					//////System.out.println("Activation lower than 0 : exit");
					System.exit(-2);
				}
				if (valid) {
					world.getScheduler().removeValidContextFromList(this);
					valid = false;
				}
			}
				
			if (activations == maxActivationsRequired) {
				valid = true;
				world.getScheduler().registerAgent(this);
			}
		}
	}
	
	private void computeAMessageTypeSelection(Message m) {
		nSelection++;
	}


	/* (non-Javadoc)
	 * @see agents.SystemAgent#play()
	 */
	public void play() {
		

		
		
		
		super.play();
		
		
		world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "NON VALID PERCEPTS")));
		for(Percept pct : nonValidPercepts) {
			System.out.println("--> " + pct.getName());
		}
		
		if(nonValidPercepts.size() == 0) {

			sendMessage(getActionProposal(), MessageType.PROPOSAL, headAgent);
			Config.print("Message envoyé", 4);
			System.out.println(world.getScheduler().getTick() + " " + this.name + " VALID");
		}
		else if(nonValidPercepts.size() == 1){
			world.getScheduler().getHeadAgent().addPartiallyActivatedContext(nonValidPercepts.get(0), this);
		}
		
		
		if(nonValidNeightborPercepts.size() == 0) {
			
			////System.out.println("VALID NEIGHBOR : " + this.getName());

			//System.out.println(world.getScheduler().getTick() + " " + this.getName() + " " + "VALID");
			world.getScheduler().getHeadAgent().addRequestNeighbor(this);
		}
		else if(nonValidNeightborPercepts.size() == 1){
			world.getScheduler().getHeadAgent().addPartialRequestNeighborContext(nonValidNeightborPercepts.get(0),this);
			//System.out.println(world.getScheduler().getTick() + " " + this.getName() + " " + "PARTIALLY VALID" + " " + nonValidNeightborPercepts.get(0).getName());
		}
		
		

		

		

		//assert computeValidityByPercepts() == (nonValidPercepts.size() == 0) : "Erreur Valid Non Valid";
		
		
		this.activations = 0;
		this.valid = false;

		
		
		
		// Reset percepts validities
		for(Percept percept : perceptValidities.keySet()) {
			perceptValidities.put(percept, false);
			perceptNeighborhoodValidities.put(percept, false);
		}
		
		
		
//		nonValidPercepts.clear();
		//nonValidNeightborPercepts.clear();
		
		//ENDO
//		for (Percept v : ranges.keySet()) {
//			if (ranges.get(v).isTooSmall()){
//				solveNCS_Uselessness(headAgent);
//				break;
//			}
//		}
		/*NCSDetections();
		
		for(Context ctxt : contextOverlapsByPercept.keySet()) {
			contextOverlapsByPerceptSave.put(ctxt, new HashMap<Percept,Boolean>());
			for(Percept p : ranges.keySet()) {
				contextOverlapsByPerceptSave.get(ctxt).put(p, contextOverlapsByPercept.get(ctxt).get(p));
			}
		}

		contextOverlapsByPercept.clear();*/
		
		
		Random rand = new Random();
		
//		if( this.getConfidence()  <= 0 && tickCreation + 125 < world.getScheduler().getTick() ) {
//			////System.out.println(world.getScheduler().getTick() +" " + this.getName()+ " " + "solveNCS_Uselessness");
//			world.raiseNCS(NCS.CONTEXT_USELESSNESS);
//			this.die();
//		}
		
		
	}
	
	public void updateRequestNeighborState(){ //faire le update dans le head attention partial et full
		
		if(nonValidNeightborPercepts.size() == 0) {
			
			////System.out.println("VALID NEIGHBOR : " + this.getName());

			
			world.getScheduler().getHeadAgent().addRequestNeighbor(this);
		}
		else {
			world.getScheduler().getHeadAgent().removeRequestNeighbor(this);
		}
		
	}
	
	public void updateActivatedContexts(){ //faire le update dans le head attention partial et full
		
		if(nonValidPercepts.size() == 0) {
			
			////System.out.println("VALID NEIGHBOR : " + this.getName());

			
			world.getScheduler().getHeadAgent().addActivatedContext(this);
		}
		else {
			world.getScheduler().getHeadAgent().removeActivatedContext(this);
		}
		
	}

	public void clearNonValidPerceptNeighbors() {
		nonValidNeightborPercepts.clear();
	}
	
	public void clearNonValidPercepts() {
		nonValidPercepts.clear();
	}

	private void NCSDetections() {
		
		NCSDetection_Overlap();

		
	}
	
	public void displayOtherContextsDistances() {
		//////System.out.println("Other Context Distances : " + this.getName());
		for(Context ctxt :otherContextsDistancesByPercept.keySet()) {
			//system.out.print(ctxt.getName() + " ");
			for(Percept pct : otherContextsDistancesByPercept.get(ctxt).keySet()) {
				//system.out.print(pct.getName() + " " + otherContextsDistancesByPercept.get(ctxt).get(pct).getFirst() + " " + otherContextsDistancesByPercept.get(ctxt).get(pct).getSecond() + " ");
			}
			//////System.out.println(" ");
		}
	}
	
	private void NCSDetection_Overlap() {
		
		computeOverlapsByPercepts();
		getNearestNeighbours();
		
		
//		for(Context ctxt: overlaps.keySet()) {
//			ctxt.getOverlapType(this);
//		}
		
	}


	private void getOverlapType(Context context) {
		
		
		
	}



//--------------------------------NCS Resolutions-----------------------------------------
	
	/**
	 * Solve NC S incompetent head.
	 *
	 * @param head the head
	 */
	public void solveNCS_IncompetentHead(Head head) {
		////System.out.println(world.getScheduler().getTick() +" " + this.getName()+ " " +"solveNCS_IncompetentHead");
		world.raiseNCS(NCS.HEAD_INCOMPETENT);
		growRanges();
	}
	
	/**
	 * Solve NC S concurrence.
	 *
	 * @param head the head
	 */
	public void solveNCS_Concurrence(Head head) {
		////System.out.println(world.getScheduler().getTick() +" " + this.getName()+ " " + "solveNCS_Concurrence");
		world.raiseNCS(NCS.CONTEXT_CONCURRENCE);
		this.shrinkRangesToJoinBorders( head.getBestContext());
	}
	
	/**
	 * Solve NC S uselessness.
	 *
	 * @param head the head
	 */
	private void solveNCS_Uselessness(Head head) {
		////System.out.println(world.getScheduler().getTick() +" " + this.getName()+ " " + "solveNCS_Uselessness");
		world.raiseNCS(NCS.CONTEXT_USELESSNESS);
		this.die();
	}
	
	/**
	 * Solve NC S conflict inexact.
	 *
	 * @param head the head
	 */
	private void solveNCS_ConflictInexact(Head head) {
		////System.out.println(world.getScheduler().getTick() +" " + this.getName()+ " " + "solveNCS_ConflictInexact");
		world.raiseNCS(NCS.CONTEXT_CONFLICT_INEXACT);
		if(true) {
			confidence--;
		}
		//confidence = confidence * 0.5;
		updateExperiments();
	}
	
	
	
	private void solveNCS_Conflict(Head head) {


		world.raiseNCS(NCS.CONTEXT_CONFLICT_FALSE);		
		
		if (head.getNewContext() == this) {
			head.setNewContext(null);
		};
		
		confidence -= 2;

		ArrayList<Percept> percepts = new ArrayList();
		percepts.addAll(ranges.keySet());
		Percept p;
		p = this.getPerceptWithLesserImpactOnVolume2(percepts);
		ranges.get(p).adapt(p.getValue()); 
		
//		if (head.isContextFromPropositionWasSelected() && head.getCriticity() <= head.getErrorAllowed()){
//			
//			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
//			
//				p = this.getPerceptWithLesserImpactOnVolumeNotIncludedIn2(percepts, head.getBestContext());
//
//			if (p == null) {
//				this.die();
//			}else {	
//				ranges.get(p).adaptTowardsBorder(head.getBestContext());
//			}
//		} else {
//			p = this.getPerceptWithLesserImpactOnVolume2(percepts);
//			ranges.get(p).adapt(p.getValue()); 
//		}

		for (Percept v : ranges.keySet()) {
			if (ranges.get(v).isTooSmall()){
				solveNCS_Uselessness(head);
				break;
			}
		}
	}
	
	public void updateAVT() {
		for(Percept p : ranges.keySet()) {
			if(ranges.get(p).getLastEndTickModification()!=world.getScheduler().getTick()) {
				ranges.get(p).endogenousAdaptEndUsingAVT();
			}
			if(ranges.get(p).getLastStartTickModification()!=world.getScheduler().getTick()) {
				ranges.get(p).endogenousAdaptStartUsingAVT();
			}
			
		}
	}
	
	//-----------------------------------------------------------------------------------------------
	
	/**
	 * Gets the percept with lesser impact on volume not included in.
	 *
	 * @param containingRanges the containing ranges
	 * @param c the c
	 * @return the percept with lesser impact on volume not included in
	 */
	private Percept getPerceptWithLesserImpactOnVolumeNotIncludedIn(ArrayList<Percept> containingRanges, Context otherContext) { //Conflict or concurence
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol = 1.0;
		
		////System.out.println("PerceptWithLesserImpactOnVolumeNotIncludedIn ...");
		for (Percept percept : containingRanges) {
			

			if (!ranges.get(percept).isPerceptEnum()) {
				Range otherRanges = otherContext.getRanges().get(percept);
				
				if (!(otherRanges.getStart() <= ranges.get(percept).getStart() &&   ranges.get(percept).getEnd() <= otherRanges.getEnd())) {
					
					////System.out.println(percept.getName());
					
					
					if (ranges.get(percept).getNearestLimit(percept.getValue()) == false) {
						////System.out.println("end simu : " + ranges.get(percept).simulateNegativeAVTFeedbackEnd(percept.getValue()) + " start : " + ranges.get(percept).getStart());
						vol = ranges.get(percept).simulateNegativeAVTFeedbackEnd(percept.getValue()) - ranges.get(percept).getStart();
					} else {
						////System.out.println("end : " + ranges.get(percept).getEnd() + " start simu : " + ranges.get(percept).simulateNegativeAVTFeedbackStart(percept.getValue()));
						vol = ranges.get(percept).getEnd() - ranges.get(percept).simulateNegativeAVTFeedbackStart(percept.getValue());
					}
					
					
					////System.out.println("Vol1 : " + vol);
					
					for (Percept p2 : ranges.keySet()) {
						if (!ranges.get(p2).isPerceptEnum() && p2 != percept) {
							////System.out.println(p2.getName());
							vol *= ranges.get(p2).getLenght();
							////System.out.println(p2.getName() + " " + ranges.get(p2).getLenght() + " " + getName());
						}
					}
					////System.out.println("Vol2 : " + vol);
					
					
					if (vol < volumeLost) {
						volumeLost = vol;
						p = percept;
					}
					
					
					////System.out.println("Vol lost : " + volumeLost + "percept " + p.getName());
					
				}
				
				
			}
			
			
		}
		
		
		return p;
	}
	
	private Percept getPerceptWithLesserImpactOnVolumeNotIncludedIn3(ArrayList<Percept> containingRanges, Context otherContext) {
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol = 1.0;
		
		//////System.out.println("LESSER ...");
		for (Percept percept : containingRanges) {
			

			if (!ranges.get(percept).isPerceptEnum()) {
				Range otherRanges = otherContext.getRanges().get(percept);
				
				if (!(otherRanges.getStart() <= ranges.get(percept).getStart() &&   ranges.get(percept).getEnd() <= otherRanges.getEnd())) {
					
					//////System.out.println(percept.getName());
					
					
					vol = Math.abs( Math.abs(otherRanges.getCenter() - ranges.get(percept).getCenter()) - otherRanges.getRadius() - ranges.get(percept).getRadius());
//					if (ranges.get(percept).getNearestLimit(percept.getValue()) == false) {
//						//////System.out.println("end simu : " + ranges.get(percept).simulateNegativeAVTFeedbackMax(percept.getValue()) + " start : " + ranges.get(percept).getStart());
//						vol = percept.getValue() - ranges.get(percept).getStart();
//					} else {
//						//////System.out.println("end : " + ranges.get(percept).getEnd() + " start simu : " + ranges.get(percept).simulateNegativeAVTFeedbackMin(percept.getValue()));
//						vol = ranges.get(percept).getEnd() - percept.getValue();
//					}
					
					
					//////System.out.println("Vol1 : " + vol);
					
					for (Percept p2 : ranges.keySet()) {
						if (!ranges.get(p2).isPerceptEnum() && p2 != percept) {
							//////System.out.println(p2.getName());
							vol *= ranges.get(p2).getLenght();
							//////System.out.println(p2.getName() + " " + ranges.get(p2).getLenght() + " " + getName());
						}
					}
					//////System.out.println("Vol2 : " + vol);
					
					
					if (vol < volumeLost) {
						volumeLost = vol;
						p = percept;
					}
					
					
					//////System.out.println("Vol lost : " + volumeLost);
					
				}
				
				
			}
			
			
		}
		
		
		return p;
	}
	
	private Percept getPerceptWithLesserImpactOnVolumeNotIncludedIn2(ArrayList<Percept> containingRanges, Context otherContext) {
		Percept p3 = null;
		double volumeLost3 = Double.MAX_VALUE;
		double vol3 = 1.0;
		
		////System.out.println("LESSER ...");
		for (Percept percept : containingRanges) {
			

			if (!ranges.get(percept).isPerceptEnum()) {
				Range otherRanges = otherContext.getRanges().get(percept);
				
				if (!(otherRanges.getStart() <= ranges.get(percept).getStart() &&   ranges.get(percept).getEnd() <= otherRanges.getEnd())) {
					
					////System.out.println(percept.getName());
					
					
					if(percept.contextOrder(this, otherContext)) {
						vol3 = Math.abs(percept.getEndRangeProjection(this) - percept.getStartRangeProjection(otherContext));
					}
					else if(percept.contextOrder(otherContext, this)) {
						vol3 = Math.abs(percept.getEndRangeProjection(otherContext) - percept.getStartRangeProjection(this));
					}
					////System.out.println("vol3 : " + vol3);
					
					
					for (Percept p2 : ranges.keySet()) {
						if (!ranges.get(p2).isPerceptEnum() && p2 != percept) {
							//////System.out.println(p2.getName());
							vol3 *= ranges.get(p2).getLenght();
							//////System.out.println(p2.getName() + " " + ranges.get(p2).getLenght() + " " + getName());
						}
					}
					//////System.out.println("vol3 : " + vol3);
					
					
					if (vol3 < volumeLost3) {
						volumeLost3 = vol3;
						p3 = percept;
					}
					
					//////System.out.println("Vol3 lost : " + volumeLost3);
					
				}
				
				
			}
			
			
		}
		if(p3 != null) {////System.out.println("p3 : " + p3.getName());

		}
		
		
		return p3;
	}
	
	private Percept getPerceptWithLesserImpactOnVolumeOnOverlap(ArrayList<Percept> containingRanges, Context otherContext) {
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol;
		
		for (Percept percept : containingRanges) {
			
			if (!ranges.get(percept).isPerceptEnum()) {
				Range otherRanges = otherContext.getRanges().get(percept);
				
				if (!(otherRanges.getStart() <= ranges.get(percept).getStart() &&   ranges.get(percept).getEnd() <= otherRanges.getEnd())) {
					
					if (ranges.get(percept).getNearestLimit(percept.getValue()) == false) {
						vol = ranges.get(percept).simulateNegativeAVTFeedbackEnd(percept.getValue()) - ranges.get(percept).getStart();
					} else {
						vol = ranges.get(percept).getEnd() - ranges.get(percept).simulateNegativeAVTFeedbackStart(percept.getValue());
					}

				for (Percept p2 : ranges.keySet()) {
					if (!ranges.get(p2).isPerceptEnum() && p2 != percept) {
						vol *= ranges.get(p2).getLenght();
					}
				}
				if (vol < volumeLost) {
					volumeLost = vol;
					p = percept;
				}
			}
		}
		}
		return p;
	}
	
	
	
	
	private Percept getPerceptWithLesserImpactOnVolume(Context consideredContext, ContextOverlap contextOverlap) {
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol;
		
		for (Percept percept : ranges.keySet()) {
			
			vol = 1.0;
			
			if (!ranges.get(percept).isPerceptEnum()) {
				
				vol *= contextOverlap.getLenghtByPercept(percept);

				for (Percept p2 : ranges.keySet()) {
					if (!ranges.get(p2).isPerceptEnum() && p2 != percept) {
						
						vol *= ranges.get(p2).getLenght();
					}
				}
				
				if (vol < volumeLost) {
					volumeLost = vol;
					p = percept;
				}
			}
		}
		//////System.out.println("percept " + p.getName());
		return p;
	}
	
	

/**
 * Gets the percept with lesser impact on volume.
 *
 * @param containingRanges the containing ranges
 * @return the percept with lesser impact on volume
 */
private Percept getPerceptWithLesserImpactOnVolume(ArrayList<Percept> containingRanges) {
	Percept p = null;
	double volumeLost = Double.MAX_VALUE;
	double vol;
	
	for (Percept v : containingRanges) {
		if (!ranges.get(v).isPerceptEnum()) {

				if (ranges.get(v).getNearestLimit(v.getValue()) == false) {
					vol = ranges.get(v).simulateNegativeAVTFeedbackEnd(v.getValue()) - ranges.get(v).getStart();
				} else {
					vol = ranges.get(v).getEnd() - ranges.get(v).simulateNegativeAVTFeedbackStart(v.getValue());
				}

			for (Percept v2 : ranges.keySet()) {
				if (!ranges.get(v).isPerceptEnum() && v2 != v) {
					vol *= ranges.get(v2).getLenght();
				}
			}
			if (vol < volumeLost) {
				volumeLost = vol;
				p = v;
			}
		
	}

}
	//////System.out.println("percept " + p.getName());
	return p;
}

private Percept getPerceptWithLesserImpactOnVolume2(ArrayList<Percept> containingRanges) {
	Percept p = null;
	double maxLength = Double.MIN_VALUE;
	double length;
	
	for (Percept v : containingRanges) {
		if (!ranges.get(v).isPerceptEnum()) {

			length = this.getRanges().get(v).getLenght();
			
			if (length > maxLength) {
				maxLength = length;
				p = v;
			}
		
	}

}
	//////System.out.println("percept " + p.getName());
	return p;
}



	
	/**
	 * Gets the percept with lesser impact on AVT.
	 *
	 * @param percepts the percepts
	 * @return the percept with lesser impact on AVT
	 */
	private Percept getPerceptWithLesserImpactOnAVT(ArrayList<Percept> percepts) {
		Percept p = null;
		double impact = Double.MAX_VALUE;
		double tempImpact;
		
		for (Percept v : percepts) {
			if (!ranges.get(v).isPerceptEnum()) {
				tempImpact = ranges.get(v).getAVTwillToReduce(ranges.get(v).getNearestLimit(v.getValue()));

				if (tempImpact < impact) {
					impact = tempImpact;
					p = v;
				}
			}
		}
		return p;
	}
	
	/**
	 * Gets the percept with larger impact on AVT.
	 *
	 * @param percepts the percepts
	 * @return the percept with larger impact on AVT
	 */
	private Percept getPerceptWithLargerImpactOnAVT(ArrayList<Percept> percepts) {
		Percept p = null;
		double impact = Double.NEGATIVE_INFINITY;
		double tempImpact;
		
		for (Percept v : percepts) {
			if (!ranges.get(v).isPerceptEnum()) {
				tempImpact = (-1) * Math.abs(ranges.get(v).getAVTwillToReduce(ranges.get(v).getNearestLimit(v.getValue())));

				if (tempImpact > impact) {
					impact = tempImpact;
					p = v;
				}
			}
		}
		return p;
	}

	
	/**
	 * Gets the action proposal.
	 *
	 * @return the action proposal
	 */
	public double getActionProposal() {
		return localModel.getProposition(this);
	}
	
	public double getOverlapActionProposal(ContextOverlap contextOverlap) {
		return localModel.getProposition(this, contextOverlap);
	}


	/**
	 * Compute validity.
	 *
	 * @return true, if successful
	 */
	private boolean computeValidity() {
		boolean b = true;
		for (Percept p : ranges.keySet()) {
			if (ranges.get(p).contains(p.getValue()) != 0) {
				b = false;
				break;
			}
		}
		return b;
	}

	/**
	 * Gets the ranges.
	 *
	 * @return the ranges
	 */
	public HashMap<Percept, Range> getRanges() {
		return ranges;
	}
	
	public Range getRangeByPerceptName(String percetName) {
		for(Percept prct : ranges.keySet()) {
			if(prct.getName().equals(percetName)) {
				return ranges.get(prct);
			}
		}
		return null;
	}

	/**
	 * Sets the ranges.
	 *
	 * @param ranges the ranges
	 */
	public void setRanges(HashMap<Percept, Range> ranges) {
		this.ranges = ranges;
	}

	/**
	 * Gets the controler.
	 *
	 * @return the controler
	 */
	public Head getControler() {
		return headAgent;
	}

	/**
	 * Sets the controler.
	 *
	 * @param controler the new controler
	 */
	public void setControler(Head controler) {
		this.headAgent = controler;
	}

	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	public double getAction() {
		return action;
	}

	/**
	 * Sets the action.
	 *
	 * @param action the new action
	 */
	public void setAction(double action) {
		this.action = action;
	}

	/* (non-Javadoc)
	 * @see agents.context.AbstractContext#getTargets()
	 */
	@Override
	public ArrayList<? extends Agent> getTargets() {
		ArrayList<Agent> arrayList = new ArrayList<Agent>();
		arrayList.add(headAgent);
		return arrayList;
	}

	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	
	public String toString() {
		return "Context :" + this.getName();
	}
	public String toStringFull() {
		String s = "";
		s += "Context : " + getName() + "\n";
		s += "\n";
		
		s += "Model : ";
		s += this.localModel.getCoefsFormula() + "\n";
		//double[] coefs = ((LocalModelMillerRegression) this.localModel).getCoef();
		//for (int i = 1 ; i < coefs.length ; i++) {
			/*if (Double.isNaN(coefs[i])) {
				s += "0.0" + "\t";	
			}
			else {
				s += coefs[i] + "\t";				
			}*/
			//s += coefs[i] + "\t";
		//}
		//s += "\n";
		s += "\n";
		
		for (Percept v : ranges.keySet()) {
			s += v.getName() + " : " + ranges.get(v).toString() + "\n";
			
			s += "\n";
			s += "Neighbours : \n";
			
			if(nearestNeighbours.get(v).get("start") != null) {
				s+= "START :" + nearestNeighbours.get(v).get("start").getName() + "\n";
			}
			else {
				s+= "START : \n";
			}
			s += "Sorted start possible neighbours :\n";
			if(sortedPossibleNeighbours.get(v).get("start").size()>0) {
				for(Context ctxt : sortedPossibleNeighbours.get(v).get("start")) {
					
					if(ctxt.equals(this)) {
						s += "# " + ctxt.getName() + " --> " + ctxt.getRanges().get(v).getStart() + "\n";
					}
					else {
						s += ctxt.getName() + " ---> " + ctxt.getRanges().get(v).getStart() + "\n";
					}
						
					
				}
			}
			s += "Sorted end possible neighbours :\n";
			if(sortedPossibleNeighbours.get(v).get("end").size()>0) {
				for(Context ctxt : sortedPossibleNeighbours.get(v).get("start")) {
					
					if(ctxt.equals(this)) {
						s += "# " +ctxt.getName()+ " --> " + ctxt.getRanges().get(v).getEnd() + "\n";
					}
					else {
						s += ctxt.getName() + " ---> " + ctxt.getRanges().get(v).getEnd() + "\n";
					}
					
				}
			}
			
			if(nearestNeighbours.get(v).get("end") != null) {
				s+= "END :" + nearestNeighbours.get(v).get("end").getName() + "\n";
			}
			else {
				s+= "END : \n";
			}
			
			

		}
		s += "\n";
		s += "Number of activations : " + activations + "\n";
		if (actionProposition != null) {
			s += "Action proposed : " + this.actionProposition + "\n";
		} else {
			s += "Action proposed : " + this.getActionProposal() + "\n";
		}
		s += "Number of experiments : " + experiments.size() + "\n";
		s += "Confidence : " + confidence + "\n";
//		if (formulaLocalModel != null) {
//			s += "Local model : " + this.formulaLocalModel + "\n";
//		} else {
//			s += "Local model : " + localModel.getFormula(this) + "\n";
//		}
		
		s += "\n";
		s += "Possible neighbours : \n";
		for(Context ctxt : possibleNeighbours) {
			s += ctxt.getName() + "\n";
		}

		
		
		return s;
	}
	
	public String toStringReducted(HashMap<Percept,Double> situation) {
		String s = "";
		s += "Context : " + getName() + "\n";
		s += "Model : ";
		s += this.localModel.getCoefsFormula() + "\n";
;
		
		for (Percept v : ranges.keySet()) {
			s += v.getName() + " : " + ranges.get(v).toString() + "\n";
			
		}

		s += "Number of activations : " + activations + "\n";
		if (actionProposition != null) {
			s += "Action proposed : " + this.actionProposition + "\n";
		} else {
			s += "Action proposed : " + this.getActionProposal() + "\n";
		}
		s += "Number of experiments : " + experiments.size() + "\n";
		s += "Confidence : " + confidence + "\n";
		s += "Normalized confidence : " + getNormalizedConfidence() + "\n";
		s += "Influence :" + getInfluence(situation) + "\n";
		s += "Worst Influence :" + getWorstInfluence(situation) + "\n";
		s += "Worst Influence + Conf :" + getWorstInfluenceWithConfidence(situation) + "\n";
		s += "Worst Influence + Vol :" + getWorstInfluenceWithVolume(situation) + "\n";
		for (Percept pct : situation.keySet()) {
			s += "Influence " +pct.getName() +  " : " + getInfluenceByPerceptSituation(pct, situation.get(pct)) + "\n";
			
		}
		s += "Global Influence * Confidence :" + getInfluenceWithConfidence(situation) + "\n";
		for (Percept pct : situation.keySet()) {
			s += "Influence * Confidence " +pct.getName() + " : " + getInfluenceByPerceptSituationWithConfidence(pct, situation.get(pct)) + "\n";
			
		}
		
		
		if (formulaLocalModel != null) {
			s += "Local model : " + this.formulaLocalModel + "\n";
		} else {
			//s += "Local model : " + localModel.getFormula(this) + "\n"; // Provoque erreur pas assez de données (0 lignes) pour 3 prédicteurs
		}
		
		s += "\n";

		
		
		return s;
	}

	/**
	 * Gets the n selection.
	 *
	 * @return the n selection
	 */
	public int getNSelection() {
		return activations;
	}

	/**
	 * Sets the n selection.
	 *
	 * @param nSelection the new n selection
	 */
	public void setnSelection(int nSelection) {
		this.nSelection = nSelection;
	}


	/**
	 * Checks if is valid.
	 *
	 * @return true, if is valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Sets the valid.
	 *
	 * @param valid the new valid
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * Checks if is first time period.
	 *
	 * @return true, if is first time period
	 */
	public boolean isFirstTimePeriod() {
		return firstTimePeriod;
	}

	/**
	 * Sets the first time period.
	 *
	 * @param firstTimePeriod the new first time period
	 */
	public void setFirstTimePeriod(boolean firstTimePeriod) {
		this.firstTimePeriod = firstTimePeriod;
	}


	/**
	 * Checks if is best context.
	 *
	 * @return true, if is best context
	 */
	public boolean isBestContext() {
		return bestContext;
	}


	/**
	 * Sets the best context.
	 *
	 * @param bestContext the new best context
	 */
	public void setBestContext(boolean bestContext) {
		this.bestContext = bestContext;
	}

	/**
	 * Gets the function.
	 *
	 * @return the function
	 */
	public LocalModelAgent getFunction() {
		return localModel;
	}


	/**
	 * Sets the function.
	 *
	 * @param function the new function
	 */
	public void setFunction(LocalModelAgent function) {
		this.localModel = function;
	}

	/**
	 * Gets the experiments.
	 *
	 * @return the experiments
	 */
	public ArrayList<Experiment> getExperiments() {
		return experiments;
	}

	/**
	 * Sets the experiments.
	 *
	 * @param experiments the new experiments
	 */
	public void setExperiments(ArrayList<Experiment> experiments) {
		this.experiments = experiments;
	}

	/**
	 * Gets the confidence.
	 *
	 * @return the confidence
	 */
	public double getConfidence() {
		//This is a test to use confidence as directly linked to size of context.
	/*	double d = 1.0;
		ArrayList<Percept> percepts = world.getAllPercept();

		for (Percept p : percepts) {
			d *= ranges.get(p).getLenght();
		}
		
		return 1/d;*/
		return confidence;

	}
	
	public double getNormalizedConfidence() {
		return 1/(1+Math.exp(-confidence));
		//return getParametrizedNormalizedConfidence(20.0);
	}
	
	public double getParametrizedNormalizedConfidence(double dispersion) {
		return 1/(1+Math.exp(-confidence/dispersion));
	}
	
	public double getInfluenceWithConfidence(HashMap<Percept,Double> situation) {
		Double influence = 1.0;
		
		for(Percept pct : situation.keySet()) {
			////////System.out.println("INFLUTEST " + getInfluenceByPerceptSituation(pct, situation.get(pct)));
			influence *= getInfluenceByPerceptSituationWithConfidence(pct, situation.get(pct));
		}
		
		return influence;
	}
	
	public double getInfluenceWithConfidenceAndVolume(HashMap<Percept,Double> situation) {	
		return getVolume()*getInfluenceWithConfidence(situation);
	}
	
	public double getInfluence(HashMap<Percept,Double> situation) {
		Double influence = 1.0;
		
		for(Percept pct : situation.keySet()) {
			////////System.out.println("INFLUTEST " + getInfluenceByPerceptSituation(pct, situation.get(pct)));
			influence *= getInfluenceByPerceptSituation(pct, situation.get(pct));
		}
		
		return influence;
	}
	
	public double getWorstInfluenceWithConfidence(HashMap<Percept,Double> situation) {
		Double worstInfluence = Double.POSITIVE_INFINITY;
		Double currentInfluence = 0.0;
		
		for(Percept pct : situation.keySet()) {
			////////System.out.println("INFLUTEST " + getInfluenceByPerceptSituation(pct, situation.get(pct)));
			currentInfluence = getInfluenceByPerceptSituationWithConfidence(pct, situation.get(pct));
			if(currentInfluence < worstInfluence) {
				worstInfluence = currentInfluence;
			}
		}
		
		return worstInfluence;
	}
	
	public double getWorstInfluence(HashMap<Percept,Double> situation) {
		Double worstInfluence = Double.POSITIVE_INFINITY;
		Double currentInfluence = 0.0;
		
		for(Percept pct : situation.keySet()) {
			////////System.out.println("INFLUTEST " + getInfluenceByPerceptSituation(pct, situation.get(pct)));
			currentInfluence = getInfluenceByPerceptSituation(pct, situation.get(pct));
			if(currentInfluence < worstInfluence) {
				worstInfluence = currentInfluence;
			}
		}
		
		return worstInfluence;
	}
	
	public double getWorstInfluenceWithVolume(HashMap<Percept,Double> situation) {
		
		return getVolume()*getWorstInfluence(situation);
	}
	
	public double getWorstInfluenceWithWorstRange(HashMap<Percept,Double> situation) {
		
		return getWorstRange()*getWorstInfluence(situation);
	}
	
	public double getInfluenceByPerceptSituation(Percept pct, double situation) {
		double center = getCenterByPercept(pct);
		double radius = getRadiusByPercept(pct);
				
		return  Math.exp(- Math.pow(situation-center, 2)/(2*Math.pow(radius, 2)));
	}
	
	public double getInfluenceByPerceptSituationWithConfidence(Percept pct, double situation) {
				
		return  getNormalizedConfidence()* getInfluenceByPerceptSituation(pct, situation);
	}
	
	public double getCenterByPercept(Percept pct) {
		return (this.getRangeByPerceptName(pct.getName()).getEnd() + this.getRangeByPerceptName(pct.getName()).getStart()) /2;
	}
	
	public double getRadiusByPercept(Percept pct) {
		return (this.getRangeByPerceptName(pct.getName()).getEnd() - this.getRangeByPerceptName(pct.getName()).getStart()) /2;
	}
	
	public double getVolume() {
		double volume = 1.0;
		
		for(Percept pct: getRanges().keySet()) {
			volume *= 2*getRadiusByPercept(pct);
		}
		return volume;
	}
	

	
	public double getWorstRange() {
		Double volume = Double.POSITIVE_INFINITY;
		
		for(Percept pct: getRanges().keySet()) {
			//volume *= 2*getRadiusByPercept(pct);
			if(getRadiusByPercept(pct)<volume) {
				volume = getRadiusByPercept(pct);
			}
		}
		return volume;
	}

	/**
	 * Sets the confidence.
	 *
	 * @param confidence the new confidence
	 */
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}


	/**
	 * Update experiments.
	 */
	private void updateExperiments() {
	//	//////System.out.println("Update experiments");
		ArrayList<Percept> var = world.getAllPercept();
		maxActivationsRequired = var.size();
		Experiment exp = new Experiment();
		for (Percept v : var) {
			exp.addDimension(v, v.getValue());
		}
		exp.setProposition(headAgent.getOracleValue());
		
		experiments.add(exp);
		this.world.getScheduler().addAlteredContext(this);
		localModel.updateModel(this);
	}
	




	
	
	/**
	 * Analyze results.
	 *
	 * @param ctrl the ctrl
	 */
	public void analyzeResults(Head head) {


		
		if (head.getCriticity(this) > head.getErrorAllowed()) {
			solveNCS_Conflict(head);
			this.world.getScheduler().addAlteredContext(this);
		}
		else {		
			if (head.getCriticity(this) > head.getInexactAllowed()) {
				solveNCS_ConflictInexact(head);
			}
			else {
				confidence++;
				//confidence = confidence * 2;
			}
		}

	}
	
	public void analyzeResults2(Head head) {

		
		if (head.getCriticity(this) <  head.getInexactAllowed()) {
			confidence++;
		}
		else {
			
			if (head.getCriticity(this) > head.getErrorAllowed()) {
				solveNCS_Conflict(head);
				this.world.getScheduler().addAlteredContext(this);
			}
			else if(head.getCriticity(this) > head.getInexactAllowed()) {
				solveNCS_ConflictInexact(head);
			}
			
			
			
		}
		

	}
	
	/**
	 * Grow every ranges allowing to includes current situation.
	 *
	 * @param head the head
	 */
	public void growRanges() {
		//////System.out.println("Grow " + this.getName() );
		ArrayList<Percept> allPercepts = world.getAllPercept();
		for (Percept pct : allPercepts) {
			boolean contain = ranges.get(pct).contains(pct.getValue()) == 0 ;
			//////System.out.println(pct.getName() + " " + contain);
			if (!contain) {
				ranges.get(pct).adapt(pct.getValue());
				//ranges.get(pct).extend(pct.getValue(), pct);
			}
		}
	}
	
	
	
	/**
	 * Shrink ranges to join borders.
	 *
	 * @param head the head
	 * @param c the c
	 */
	public void shrinkRangesToJoinBorders(Context bestContext) {
		Set<Percept> percetList = ranges.keySet();
		ArrayList<Percept> containingRanges = new ArrayList<Percept>();
		
		for (Percept percept : percetList) {
			boolean contain = ranges.get(percept).contains(percept.getValue()) == 0 ? true : false;
			if (contain) {
				containingRanges.add(percept);
			}
		}
		
		Percept perceptWithLesserImpact = getPerceptWithLesserImpactOnVolumeNotIncludedIn2(containingRanges,bestContext);
		//Percept perceptWithLesserImpact = getPerceptWithLesserImpactOnVolumeNotIncludedIn2(containingRanges,bestContext);
		if (perceptWithLesserImpact == null) {
			////System.out.println(world.getScheduler().getTick() + "shrinkRangesToJoinBordersAndDie");
			this.die();
		}else {
			//ranges.get(perceptWithLesserImpact).matchBorderWithBestContext(bestContext);
			ranges.get(perceptWithLesserImpact).adaptTowardsBorder(bestContext);
		}
	}
	
	public void shrinkRangesToJoinBordersOnOverlap(Context consideredContext, ContextOverlap contextOverlap) {
		ArrayList<Percept> percepts = new ArrayList<Percept>();
		percepts.addAll(ranges.keySet());
		
		Percept perceptWithLesserImpact = getPerceptWithLesserImpactOnVolume(consideredContext, contextOverlap);
		if (perceptWithLesserImpact == null) {
			this.die();
		}else {
			ranges.get(perceptWithLesserImpact).matchBorderWith(consideredContext);
		}
		
		//perceptWithLesserImpact.overlapDeletion(contextOverlap);
	}

	
	/* (non-Javadoc)
	 * @see agents.context.AbstractContext#die()
	 */
	public void die () {
		for(Context ctxt : world.getScheduler().getContextsAsContext()) {
			ctxt.removeContext(this);
		}
		
		////////System.out.println("DIED : " + this.getName());
		localModel.die();
		super.die();
		
		for(Percept percept : world.getScheduler().getPercepts()) {
			percept.deleteContextProjection(this);
		}
	}
	
	
	public void setPerceptValidity(Percept percept) {
		perceptValidities.put(percept, true);
	}
	
	public void setNeighborhoodPerceptValidity(Percept percept) {
		perceptNeighborhoodValidities.put(percept, true);
	}
	
	
	
	public void setPerceptOverlap(Percept percept, Context context) {
		if(!contextOverlapsByPercept.keySet().contains(context)) {
			contextOverlapsByPercept.put(context, new HashMap<Percept,Boolean>());
			
			for(Percept p : ranges.keySet()) {
				contextOverlapsByPercept.get(context).put(p, false);
			}
		}
		
		contextOverlapsByPercept.get(context).put(percept, true);
	}
	
	
	
	public Boolean computeValidityByPercepts() {
		Boolean test = true;
		for(Percept percept : perceptValidities.keySet()) {
			////////System.out.println(percept.getName()+"--->"+perceptValidities.get(percept));
			test = test && perceptValidities.get(percept);
		}
		return test;
	}
	
	

	
	public Boolean computeNeighborhoodValidityByPercepts() {
		Boolean test = true;
		for(Percept percept : perceptNeighborhoodValidities.keySet()) {
			////////System.out.println(percept.getName()+"--->"+perceptNeighborhoodValidities.get(percept));
			test = test && perceptNeighborhoodValidities.get(percept);
		}
		return test;
	}
	
	
	public void addNonValidPercept(Percept pct) {
		world.trace(new ArrayList<String>(Arrays.asList(this.getName(),pct.getName(), "NON VALID")));
		nonValidPercepts.add(pct);
	}
	
	public ArrayList<Percept> getNonValidPercepts() {
		return nonValidPercepts;
	}
	
	public void removeNonValidPercept(Percept pct) {
		nonValidPercepts.remove(pct);
	}
	
	
	
	public void addNonValidNeighborPercept(Percept pct) {
		
		nonValidNeightborPercepts.add(pct);
		
	}
	
	public ArrayList<Percept> getNonValidNeighborPercepts() {
		
		return nonValidNeightborPercepts;
	}

	public void removeNonValidNeighborPercept(Percept pct) {
		nonValidNeightborPercepts.remove(pct);
	}
	
	
	public Boolean computeOverlapsByPercepts() {
		Boolean test = true;
		
		overlaps.clear();
		contextOverlaps.clear();
		//this.world.getScheduler().clearContextOverlaps();
		
		for(Context context : contextOverlapsByPercept.keySet()) {
			test = true;
			for(Percept percept : ranges.keySet()) {
				test = test && contextOverlapsByPercept.get(context).get(percept);
			}
			
			if(test && !context.overlapComputed(this)) {
							
				
				overlaps.put(context, "Overlap");
				
				HashMap<Percept,HashMap<String,Double>> overlapRanges = new HashMap<Percept,HashMap<String,Double>>();
				for(Percept percept : ranges.keySet()) {
					overlapRanges.put(percept, new HashMap<String,Double>());
					//////System.out.println("CONTEXT 1" + context.getName() + " CONTEXT2" + this.getName());
					double startRange = percept.getOverlapRangesBetweenContexts(this, context).get("start");
					double endRange = percept.getOverlapRangesBetweenContexts(this, context).get("end");
					overlapRanges.get(percept).put("start", startRange);
					overlapRanges.get(percept).put("end", endRange);
				}
				ContextOverlap overlap = new ContextOverlap(world, this, context, overlapRanges);
				contextOverlaps.add(overlap);	
				this.world.getScheduler().addContextOverlap(overlap);
				
				
			}
		}
		
		
		return test;
	}
	
	public Boolean computeOverlapsBySelectedPercepts(ArrayList<Percept> selectedPercepts, Context context) {
		Boolean test = true;
		
		test = true;
		for(Percept percept : selectedPercepts) {
			test = test && contextOverlapsByPercept.get(context).get(percept);
		}
		if(test) {
			//neigbours.put(context, "Overlap");
		}
		
		
		return test;
	}
	
	public HashMap<String , ArrayList<Context>> getSortedPossibleNeigbours(Percept percept) {
		
		ArrayList<Percept> otherPercetps = new ArrayList<Percept>();; 
		ArrayList<Context> contextOverlapedInOtherPercepts = new ArrayList<Context>();
		boolean contextOverlapedInOtherPerceptsTest = true;
		
		for(Percept p : ranges.keySet()) {
			if(p != percept) {
				otherPercetps.add(p);
			}
		}
		
		
		for(Context ctxt : contextOverlapsByPercept.keySet()) {
			contextOverlapedInOtherPerceptsTest = true;
			for(Percept otherPctpt: otherPercetps) {
				contextOverlapedInOtherPerceptsTest = contextOverlapedInOtherPerceptsTest && contextOverlapsByPercept.get(ctxt).get(otherPctpt);
			}
			if(contextOverlapedInOtherPerceptsTest) {
				contextOverlapedInOtherPercepts.add(ctxt);
				if(!possibleNeighbours.contains(ctxt)) {
					possibleNeighbours.add(ctxt);
				}
				
			}
		}
		
		 
		 HashMap<String , ArrayList<Context>> sortedRangesSubGroup = new HashMap<String , ArrayList<Context>>();
		 sortedRangesSubGroup.put("start", percept.getSortedRangesSubGroup(contextOverlapedInOtherPercepts, "start"));
		 sortedRangesSubGroup.put("end", percept.getSortedRangesSubGroup(contextOverlapedInOtherPercepts, "end"));
		
		 return sortedRangesSubGroup;
	}
	
	public void getNearestNeighbours(){
		
		HashMap<Percept,  HashMap<String , ArrayList<Context>>> localSortedPossibleNeigbours = new HashMap<Percept,  HashMap<String , ArrayList<Context>>>();
		
		for(Percept p : ranges.keySet()) {
			
			sortedPossibleNeighbours.get(p).clear();
			nearestNeighbours.get(p).clear();
			neighbours.clear();
			
		}
		
		for(Percept p : ranges.keySet()) {
			
			localSortedPossibleNeigbours.put(p,getSortedPossibleNeigbours(p));
			localSortedPossibleNeigbours.get(p).get("start").add(this);
			localSortedPossibleNeigbours.get(p).get("end").add(this);
			
			
			
			Collections.sort(localSortedPossibleNeigbours.get(p).get("start"), p.customRangeComparators.get("start"));
			Collections.sort(localSortedPossibleNeigbours.get(p).get("end"), p.customRangeComparators.get("end"));
			
			sortedPossibleNeighbours.get(p).put("start", localSortedPossibleNeigbours.get(p).get("start"));
			sortedPossibleNeighbours.get(p).put("end", localSortedPossibleNeigbours.get(p).get("end"));
			
			
			
		}
		
		for(Percept p : ranges.keySet()) {
			
			
			Context startNeighbour = getNearestContextBySortedPerceptAndRange(localSortedPossibleNeigbours.get(p), p, "start");
			Context endNeighbour = getNearestContextBySortedPerceptAndRange(localSortedPossibleNeigbours.get(p), p, "end");
			
			
			nearestNeighbours.get(p).put("end", startNeighbour);
			nearestNeighbours.get(p).put("start", endNeighbour);
			
			neighbours.add(startNeighbour);
			neighbours.add(endNeighbour);
		}
		
		
	}
	
	public void computeNearestNeighbour() {
		
		////////System.out.println("VOISINS : " + neighbours.size());
		for(Context neighbourContext : neighbours) {
			
			
			if(neighbourContext != null){
				ContextVoid computedVoid = neighbourContext.voidComputed(this);
				if(computedVoid != null) {
					contextVoids.add(computedVoid);
				}
				else {
					voidDetection(neighbourContext);
				}
			}
			
		}
		
		
	}
	
	
	//Context void creation between this the current context on the one in arg
	public void voidDetection(Context context) {
		boolean noVoid = false;
		HashMap<Percept,Double> voidPosition = new HashMap<Percept,Double>();
		HashMap<Percept,Double> voidWidth = new HashMap<Percept,Double>();
		
		
		for(Percept percept : ranges.keySet()) {	
			
			double thisStart = this.getRanges().get(percept).getStart();
			double thisEnd = this.getRanges().get(percept).getEnd();
			double ctxtStart = context.getRanges().get(percept).getStart();
			double ctxtEnd = context.getRanges().get(percept).getEnd();
			
			double perceptPosition = 0d; 
			double perceptWidth = 0d;
			
			////////System.out.println(context.getName() + "\n" +contextOverlapsByPerceptSave);
			if(contextOverlapsByPerceptSave.get(context).get(percept)) {
	
				
				
				
				
				if( percept.contextIncludedIn(this, context) ) {
					perceptPosition = (thisStart + thisEnd) / 2 ;
					perceptWidth = thisEnd - thisStart;
				}
				else if( percept.contextIncludedIn(context, this) ) {
					perceptPosition = (ctxtStart + ctxtEnd) / 2 ;
					perceptWidth = ctxtEnd - ctxtStart;
				}
				else if( percept.contextOrder(this, context) ) {
					perceptPosition = (ctxtStart + thisEnd) / 2 ;
					perceptWidth = thisEnd - ctxtStart;
				}
				else if( percept.contextOrder(context, this) ) {
					perceptPosition = (thisStart + ctxtEnd) / 2 ;
					perceptWidth = ctxtEnd - thisStart;
				}
				else {
					//////System.out.println("PROBLEM !!!!!!!!!!!!!!!!! Void detection" );
				}
				

				voidPosition.put(percept, perceptPosition);
				voidWidth.put(percept, perceptWidth);
				
			}
			else {
				
				if(ctxtEnd + 1.0 < thisStart) {
					perceptPosition = (ctxtEnd +  thisStart)/2 ;
					perceptWidth = thisStart - ctxtEnd;
					
					voidPosition.put(percept, perceptPosition);
					voidWidth.put(percept, perceptWidth);
				}
				else if(thisEnd + 1.0 < ctxtStart) {
					perceptPosition = (thisEnd +  ctxtStart)/2 ;
					perceptWidth = ctxtStart - thisEnd;
					
					voidPosition.put(percept, perceptPosition);
					voidWidth.put(percept, perceptWidth);
				}
				else {
					//////System.out.println("NO VOID !");
					noVoid = true;
				}
			}
			
		}
		
		if(!noVoid) {
			ContextVoid currentVoid = new ContextVoid(world, this, context, voidPosition, voidWidth);
			contextVoids.add(currentVoid);
			getWorld().getScheduler().contextVoids.add(currentVoid);
		}
	}
	
	public Context getNearestContextBySortedPerceptAndRange(HashMap<String , ArrayList<Context>> sortedPossibleNeigbours, Percept percept, String range) {
		
		
		int indexOfCurrentContext = sortedPossibleNeigbours.get(range).indexOf(this);
		
		if(sortedPossibleNeigbours.get(range).size()>1) {
			
			if((indexOfCurrentContext > 0) && ( indexOfCurrentContext < sortedPossibleNeigbours.get(range).size()-1)) {
				if(range.equals("start")) {
					return sortedPossibleNeigbours.get(range).get(indexOfCurrentContext+1);
				}
				else if(range.equals("end")) {
					return sortedPossibleNeigbours.get(range).get(indexOfCurrentContext-1);
				}
				else {
					return null;
				}
			}
			
			else if(indexOfCurrentContext == 0 ) {
				if(range.equals("start")) {
					return sortedPossibleNeigbours.get(range).get(indexOfCurrentContext+1);
				}
				else {
					return null;
				}
			}
			
			else if( indexOfCurrentContext == sortedPossibleNeigbours.get(range).size()-1)  {
				if(range.equals("end")) {
					return sortedPossibleNeigbours.get(range).get(indexOfCurrentContext-1);
				}
				else {
					return null;
				}
			}
			
			else {
				return null;
			}
			
		}
		else {
			return null;
		}
	

	
			
		
		
	}



	public boolean overlapComputed(Context context) {
		for(ContextOverlap contextOverlap : contextOverlaps) {
			if(contextOverlap.overlapComputedBy(context)) {
				return true;
			}
		}
		return false;
	}



	public void deleteOverlap(ContextOverlap contextOverlap) {
		contextOverlaps.remove(contextOverlap);
	}

	public ContextVoid voidComputed(Context context) {
		for(ContextVoid contextVoid : contextVoids) {
			if(contextVoid.voidComputedBy(context)) {
				return contextVoid;
			}
		}
		return null;
	}




	public Context clone() throws CloneNotSupportedException{
		return (Context)super.clone();
	}


	public LocalModelAgent getLocalModel() {
		return localModel;
	}

	
	public void addContext(Context ctxt) {
		if(ctxt != this) {
			otherContextsDistancesByPercept.put(ctxt, new HashMap<Percept,Pair<Double,Integer>>());
		}
		for(Percept pct : world.getScheduler().getPercepts()) {
			otherContextsDistancesByPercept.get(ctxt).put(pct, new Pair<>(null,world.getScheduler().getTick()));
		}
		
	}
	
	public void addContextDistance(Context ctxt, Percept percept, double distance) {
		

		if(ctxt != this) {
			
			if(otherContextsDistancesByPercept.get(ctxt) == null) {
				addContext(ctxt);
			}
			otherContextsDistancesByPercept.get(ctxt).put(percept, new Pair<>(distance,world.getScheduler().getTick()));
		}

	}

	public void removeContext(Context ctxt) {
		otherContextsDistancesByPercept.remove(ctxt);
	}

	public Integer getContextDistanceUpdateTick(Context ctxt, Percept pct) {
		if(otherContextsDistancesByPercept.get(ctxt) != null) {
			if(otherContextsDistancesByPercept.get(ctxt).get(pct) != null) {
				return otherContextsDistancesByPercept.get(ctxt).get(pct).getSecond();
			}
			
		}
		return null;
	}

	
	public double distance(Percept pct, double value) {
		return this.ranges.get(pct).distance(value);
	}


	 

}
