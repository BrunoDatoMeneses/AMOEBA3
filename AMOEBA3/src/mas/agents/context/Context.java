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



import mas.kernel.Config;
import mas.kernel.NCSMemory;
import mas.kernel.World;
import mas.ncs.NCS;
import mas.Pair;
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

	
	
	public HashMap<Percept , HashMap<String, Context>> nearestNeighbours;
	
	public HashMap<Context , HashMap<Percept, Pair<Double,Integer>>> otherContextsDistancesByPercept;
	
	public HashMap<Percept , HashMap<String, ArrayList<Context>>> sortedPossibleNeighbours = new HashMap<Percept , HashMap<String, ArrayList<Context>>>();
	
	public ArrayList<Context> possibleNeighbours = new  ArrayList<Context>();
	public ArrayList<Context> neighbours = new ArrayList<Context>();
	
	private double mappingCriticality = 0.0;
	
	

	/**
	 * The main constructor, used by AMOEBA to build new context agent.
	 * @param world : the world where the agent must live.
	 * @param head : the head agent associated with the next context agent
	 */
	public Context(World world, Head head) {
		super(world);
		buildContext(head);
		world.trace(new ArrayList<String>(Arrays.asList("CTXT CREATION", this.getName())));
	}
	
	public Context(World world, Head head, Context bestNearestContext) {
		super(world);
		buildContext(head, bestNearestContext);
		world.trace(new ArrayList<String>(Arrays.asList("CTXT CREATION WITH GODFATHER", this.getName())));
		NCSDetection_Uselessness();
		
		//////System.out.println("=======================================================================" +this.getName() + " <-- " + bestNearestContext.getName());
		//////System.out.println(this.toStringFull());
		//////System.out.println(bestNearestContext.toStringFull());
	}
	
	public Context(World world, Head head, Context fatherContext, HashMap<Percept,Pair<Double,Double>> contextDimensions) {
		super(world);
		buildContext(head, fatherContext, contextDimensions);
		world.trace(new ArrayList<String>(Arrays.asList("CTXT CREATION WITH GODFATHER AND DIM", this.getName())));
		//////System.out.println("=======================================================================" +this.getName() + " <-- " + bestNearestContext.getName());
		//////System.out.println(this.toStringFull());
		//////System.out.println(bestNearestContext.toStringFull());
	}
	
	
	private void buildContext (Head headAgent, Context fatherContext, HashMap<Percept,Pair<Double,Double>> contextDimensions) {
		
		
		this.tickCreation = world.getScheduler().getTick();
		
		ArrayList<Percept> var = world.getAllPercept();
		Experiment firstPoint = new Experiment(this);
		this.headAgent = headAgent;
		
		action = this.headAgent.getOracleValue();
		maxActivationsRequired = var.size();
		
		for(Context ctxt : world.getScheduler().getContextsAsContext()) {
			
			ctxt.addContext(this);
		}
		
		
		
		for (Percept pct : var) {
			Range r;
			double center = contextDimensions.get(pct).getA();
			double length = contextDimensions.get(pct).getB();
			r = new Range(this, center - length/2, center + length/2, 0, true, true, pct, world);
					
			ranges.put(pct, r);
			ranges.get(pct).setValue(center);
			sendExpressMessage(null, MessageType.REGISTER, pct);
			
			pct.addContextProjection(this);
			pct.addContextSortedRanges(this);
		}

		//expand();
		
		this.confidence = fatherContext.confidence;	
		if (fatherContext.world.getLocalModel() == TypeLocalModel.MILLER_REGRESSION) {
			
			this.localModel = new LocalModelMillerRegression(world,this);
			//this.formulaLocalModel = ((LocalModelMillerRegression) bestNearestContext.localModel).getFormula(bestNearestContext);
			double[] coef = ((LocalModelMillerRegression) fatherContext.localModel).getCoef();
			((LocalModelMillerRegression) this.localModel).setCoef(coef);
			this.actionProposition = ((LocalModelMillerRegression) fatherContext.localModel).getProposition(fatherContext);
			
		} else if (fatherContext.world.getLocalModel() == TypeLocalModel.FIRST_EXPERIMENT) {
			
			this.localModel = new LocalModelFirstExp(fatherContext.world,this);
			//this.formulaLocalModel = ((LocalModelFirstExp) bestNearestContext.localModel).getFormula(bestNearestContext);
			this.actionProposition = ((LocalModelFirstExp) fatherContext.localModel).getProposition(fatherContext);
			
		} else if (fatherContext.world.getLocalModel() == TypeLocalModel.AVERAGE) {
			
			this.localModel = new LocalModelAverage(fatherContext.world,this);
			//this.formulaLocalModel = ((LocalModelAverage) bestNearestContext.localModel).getFormula(bestNearestContext);
			this.actionProposition = ((LocalModelAverage) fatherContext.localModel).getProposition(fatherContext);
			
		}

		this.experiments = new ArrayList<Experiment>();
		experiments.addAll(fatherContext.getExperiments());

		
		
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
		
		//world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "EXPS")));
		for(Experiment exp : experiments) {
			//System.out.println(exp.toString());
		}
	}
	
	
	private void buildContext (Head headAgent, Context bestNearestContext) {
		
		
		this.tickCreation = world.getScheduler().getTick();
		
		ArrayList<Percept> var = world.getAllPercept();
		Experiment firstPoint = new Experiment(this);
		this.headAgent = headAgent;
		
		action = this.headAgent.getOracleValue();
		maxActivationsRequired = var.size();
		
		for(Context ctxt : world.getScheduler().getContextsAsContext()) {
			
			ctxt.addContext(this);
		}
		
		
		
		for (Percept v : var) {
			Range r;
			
			
			//////System.out.println("MAX RADIUS FOR CONTEXT CREATION AFTER TEST"  + v.getName() + " " + radius + " / " + (radius/v.getRadiusContextForCreation()));

			Pair<Double, Double> radiuses = world.getScheduler().getHeadAgent().getMaxRadiusesForContextCreation(v);
			
			////System.out.println("MAX RADIUS FOR CONTEXT CREATION "  + v.getName() + " < " + radiuses.getA() + " , "  + radiuses.getB() + " > / < " + (radiuses.getA()/v.getRadiusContextForCreation()) + " , " + (radiuses.getB()/v.getRadiusContextForCreation()) + " >");
			
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
			
			this.localModel = new LocalModelMillerRegression(world,this);
			//this.formulaLocalModel = ((LocalModelMillerRegression) bestNearestContext.localModel).getFormula(bestNearestContext);
			double[] coef = ((LocalModelMillerRegression) bestNearestContext.localModel).getCoef();
			((LocalModelMillerRegression) this.localModel).setCoef(coef);
			this.actionProposition = ((LocalModelMillerRegression) bestNearestContext.localModel).getProposition(bestNearestContext);
			
		} else if (bestNearestContext.world.getLocalModel() == TypeLocalModel.FIRST_EXPERIMENT) {
			
			this.localModel = new LocalModelFirstExp(bestNearestContext.world,this);
			//this.formulaLocalModel = ((LocalModelFirstExp) bestNearestContext.localModel).getFormula(bestNearestContext);
			this.actionProposition = ((LocalModelFirstExp) bestNearestContext.localModel).getProposition(bestNearestContext);
			
		} else if (bestNearestContext.world.getLocalModel() == TypeLocalModel.AVERAGE) {
			
			this.localModel = new LocalModelAverage(bestNearestContext.world,this);
			//this.formulaLocalModel = ((LocalModelAverage) bestNearestContext.localModel).getFormula(bestNearestContext);
			this.actionProposition = ((LocalModelAverage) bestNearestContext.localModel).getProposition(bestNearestContext);
			
		}

		this.experiments = new ArrayList<Experiment>();
		experiments.addAll(bestNearestContext.getExperiments());
//		Experiment newPoint = new Experiment(this);
//		
//		for(Percept pct : ranges.keySet()) {
//			newPoint.addDimension(pct, pct.getValue());
//		}
//		newPoint.setOracleProposition(this.headAgent.getOracleValue());
//		experiments.add(newPoint);
//		localModel.updateModel(this);
		
		localModel.updateModel(this.getCurrentExperiment(),world.getScheduler().getHeadAgent().learningSpeed,world.getScheduler().getHeadAgent().numberOfPointsForRegression);
		
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
		
		//world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "EXPS")));
		for(Experiment exp : experiments) {
			//System.out.println(exp.toString());
		}
	}
	
	
	
	Double getMaxRadiusForContextCreation(Percept pct) {
		Double maxRadius = world.getScheduler().getHeadAgent().getMaxRadiusForContextCreation(pct);
		//////System.out.println("MAX RADIUS FOR CONTEXT CREATION "  + pct.getName() + " " + maxRadius + " / " + (maxRadius/pct.getRadiusContextForCreation()));
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
		Experiment firstPoint = new Experiment(this);
		this.headAgent = headAgent;
		
		action = this.headAgent.getOracleValue();
		maxActivationsRequired = var.size();
		
		for(Context ctxt : world.getScheduler().getContextsAsContext()) {
			
			ctxt.addContext(this);
		}
		


		
		
		
		for (Percept v : var) {
			Range r;
			
			Pair<Double, Double> radiuses = world.getScheduler().getHeadAgent().getMaxRadiusesForContextCreation(v); 
			
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
		firstPoint.setOracleProposition(this.headAgent.getOracleValue());
		//world.trace(new ArrayList<String>(Arrays.asList(this.getName(),"NEW EXP", firstPoint.toString())));
		experiments.add(firstPoint);
		
		localModel.updateModel(this.getCurrentExperiment(),world.getScheduler().getHeadAgent().learningSpeed,world.getScheduler().getHeadAgent().numberOfPointsForRegression);
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
		
		////System.out.println("NEW CONTEXT " + this.getName());
		displayRanges();
		
		//world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "EXPS")));
		for(Experiment exp : experiments) {
			//System.out.println(exp.toString());
		}
	}
	
	public void displayRanges() {
		for(Percept pct : ranges.keySet()) {
			////System.out.println(pct.getName() + " [ " + ranges.get(pct).getStart() + " , " + ranges.get(pct).getEnd() + " ]");
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
					////System.out.println("DISTANCE " + ctxtNeigbor.getName()+ " " + pct.getName()+ " " +  expandingContext.distance(pct, ctxtNeigbor.getRanges().get(pct)) + " " + ( expandingContext.distance(pct, ctxtNeigbor.getRanges().get(pct))<0)) ;
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
	
			for(Percept pctDirectionForExpanding: ranges.keySet()) {
		
				if(pctDirectionForExpanding != fixedPct) {
								
					neighborsOnOneDirection = getContextsOnAPerceptDirectionFromContextsNeighbors(world.getScheduler().getHeadAgent().getActivatedNeighborsContexts(), pctDirectionForExpanding, alternativeContexts.get(fixedPct));
						
					Pair<Double,Double> expandingRadiuses = getMaxExpansionsForContextExpansionAfterCreation(neighborsOnOneDirection, pctDirectionForExpanding);
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
			////System.out.println("EXPANSION "  + pct.getName() +" < " + startExpansion + " , "  + endExpansion + " > / < " +pct.getMin() + " , " +pct.getMax() + " >");
			
			ranges.get(pct).setStart(biggerContextForCreation.getStart(pct));
			ranges.get(pct).setEnd(biggerContextForCreation.getEnd(pct));
			
		}
		
	}
	
	public Pair<Double,Double> getMaxExpansionsForContextExpansionAfterCreation(ArrayList<Context> contextNeighborsInOneDirection, Percept pct) {
		
		double startRadiusFromCreation = Math.abs(pct.getValue() - this.getRanges().get(pct).getStart());
		double endRadiusFromCreation = Math.abs(pct.getValue() - this.getRanges().get(pct).getEnd());
		Pair<Double,Double> maxExpansions = new Pair<Double,Double>(
				Math.min(pct.getRadiusContextForCreation() - startRadiusFromCreation, 
						Math.abs(pct.getMin()- ranges.get(pct).getStart())),
				Math.min(pct.getRadiusContextForCreation() - endRadiusFromCreation, 
						Math.abs(pct.getMax()-ranges.get(pct).getEnd())));
		
		double currentStartExpansion;
		double currentEndExpansion;
		
			
		//for(Context ctxt:partialNeighborContexts.get(pct)) {
		for(Context ctxt: contextNeighborsInOneDirection) {			
			
			////System.out.println("DISTANCE " + pct.getName() + " " + ctxt.getRanges().get(pct).centerDistance(pct.getValue()));
			if(ctxt.getRanges().get(pct).centerDistance(pct.getValue()) < 0) {
				// End radius
				
				currentEndExpansion = ctxt.getRanges().get(pct).distance(ranges.get(pct));
				////System.out.println("DISTANCE 2 " + pct.getName() + " " + ctxt.getRanges().get(pct).distance(ranges.get(pct)));
				//////System.out.println(ctxt.getName() + " " + pct.getName() + " " + currentRadius + " " + maxRadius);
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
				//////System.out.println(ctxt.getName() + " " + pct.getName() + " " + currentRadius + " " + maxRadius);
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
			this.localModel = new LocalModelMillerRegression(c.world,this);
			//this.formulaLocalModel = ((LocalModelMillerRegression) c.localModel).getFormula(c);
			double[] coef = ((LocalModelMillerRegression) c.localModel).getCoef();
			((LocalModelMillerRegression) this.localModel).setCoef(coef);
			this.actionProposition = ((LocalModelMillerRegression) c.localModel).getProposition(c);
		} else if (c.world.getLocalModel() == TypeLocalModel.FIRST_EXPERIMENT) {
			this.localModel = new LocalModelFirstExp(c.world,this);
			//this.formulaLocalModel = ((LocalModelFirstExp) c.localModel).getFormula(c);
			this.actionProposition = ((LocalModelFirstExp) c.localModel).getProposition(c);
		} else if (c.world.getLocalModel() == TypeLocalModel.AVERAGE) {
			this.localModel = new LocalModelAverage(c.world,this);
			//this.formulaLocalModel = ((LocalModelAverage) c.localModel).getFormula(c);
			this.actionProposition = ((LocalModelAverage) c.localModel).getProposition(c);
		}

		this.experiments = new ArrayList<Experiment>();
		for(Experiment obj: c.experiments) {
			Experiment exp = new Experiment(this);
			exp.setOracleProposition(obj.getOracleProposition());
			LinkedHashMap<Percept, Double> values = new LinkedHashMap<Percept, Double>();
			for(Entry<Percept, Double> entry : obj.getValuesAsLinkedHashMap().entrySet()) {
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
					////////System.out.println("Activation lower than 0 : exit");
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
		
		
		//world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "NON VALID PERCEPTS")));
//		for(Percept pct : nonValidPercepts) {
//			//System.out.println("--> " + pct.getName());
//		}
		
		if(nonValidPercepts.size() == 0) {

			sendMessage(getActionProposal(), MessageType.PROPOSAL, headAgent);
			Config.print("Message envoyé", 4);
			//System.out.println(world.getScheduler().getTick() + " " + this.name + " VALID");
			
			for(Percept pct : world.getScheduler().getPercepts()) {
				world.getScheduler().getHeadAgent().addPartiallyActivatedContextInNeighbors(pct, this);
			}
			
		}
		else if(nonValidPercepts.size() == 1){
			world.getScheduler().getHeadAgent().addPartiallyActivatedContext(nonValidPercepts.get(0), this);
		}
		
		
		if(nonValidNeightborPercepts.size() == 0) {
			
			//////System.out.println("VALID NEIGHBOR : " + this.getName());

			////System.out.println(world.getScheduler().getTick() + " " + this.getName() + " " + "VALID");
			world.getScheduler().getHeadAgent().addRequestNeighbor(this);
		}
		else if(nonValidNeightborPercepts.size() == 1){
			world.getScheduler().getHeadAgent().addPartialRequestNeighborContext(nonValidNeightborPercepts.get(0),this);
			////System.out.println(world.getScheduler().getTick() + " " + this.getName() + " " + "PARTIALLY VALID" + " " + nonValidNeightborPercepts.get(0).getName());
		}
		
		if ( (nonValidNeightborPercepts.size() == 0) &&  (nonValidPercepts.size() == 1) ) {
			
			world.getScheduler().getHeadAgent().addPartiallyActivatedContextInNeighbors(nonValidPercepts.get(0), this);
			
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
//			//////System.out.println(world.getScheduler().getTick() +" " + this.getName()+ " " + "solveNCS_Uselessness");
//			world.raiseNCS(NCS.CONTEXT_USELESSNESS);
//			this.die();
//		}
		
		
	}
	
	public void updateRequestNeighborState(){ //faire le update dans le head attention partial et full
		
		if(nonValidNeightborPercepts.size() == 0) {
			
			//////System.out.println("VALID NEIGHBOR : " + this.getName());

			
			world.getScheduler().getHeadAgent().addRequestNeighbor(this);
		}
		else {
			world.getScheduler().getHeadAgent().removeRequestNeighbor(this);
		}
		
	}
	
	public void updateActivatedContexts(){ //faire le update dans le head attention partial et full
		
		if(nonValidPercepts.size() == 0) {
			
			//////System.out.println("VALID NEIGHBOR : " + this.getName());

			
			world.getScheduler().getHeadAgent().addActivatedContext(this);
		}
		else {
			world.getScheduler().getHeadAgent().removeActivatedContext(this);
		}
		
	}
	
	public void updateActivatedContextsCopyForUpdate(){ //faire le update dans le head attention partial et full
		
		if(nonValidPercepts.size() == 0) {
			
			//////System.out.println("VALID NEIGHBOR : " + this.getName());

			
			world.getScheduler().getHeadAgent().addActivatedContextCopy(this);
		}
		else {
			world.getScheduler().getHeadAgent().removeActivatedContextCopy(this);
		}
		
	}

	public void clearNonValidPerceptNeighbors() {
		nonValidNeightborPercepts.clear();
	}
	
	public void clearNonValidPercepts() {
		nonValidPercepts.clear();
	}


	
	public void displayOtherContextsDistances() {
		////////System.out.println("Other Context Distances : " + this.getName());
		for(Context ctxt :otherContextsDistancesByPercept.keySet()) {
			////System.out.print(ctxt.getName() + " ");
			for(Percept pct : otherContextsDistancesByPercept.get(ctxt).keySet()) {
				////System.out.print(pct.getName() + " " + otherContextsDistancesByPercept.get(ctxt).get(pct).getFirst() + " " + otherContextsDistancesByPercept.get(ctxt).get(pct).getSecond() + " ");
			}
			////////System.out.println(" ");
		}
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
		world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "*********************************************************************************************************** SOLVE NCS INCOMPETENT HEAD")));
		//////System.out.println(world.getScheduler().getTick() +" " + this.getName()+ " " +"solveNCS_IncompetentHead");
		world.raiseNCS(NCS.HEAD_INCOMPETENT);
		growRanges();
		world.getScheduler().getHeadAgent().setBadCurrentCriticalityMapping();
	}
	
	/**
	 * Solve NC S concurrence.
	 *
	 * @param head the head
	 */
	public void solveNCS_Concurrence(Head head) {
		world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "*********************************************************************************************************** SOLVE NCS CONCURENCE")));
		//////System.out.println(world.getScheduler().getTick() +" " + this.getName()+ " " + "solveNCS_Concurrence");
		world.raiseNCS(NCS.CONTEXT_CONCURRENCE);
		this.shrinkRangesToJoinBorders( head.getBestContext());
		
		world.getScheduler().getHeadAgent().setBadCurrentCriticalityMapping();
	}
	
	/**
	 * Solve NC S uselessness.
	 *
	 * @param head the head
	 */
	public void solveNCS_Uselessness() {
		if(!isDying) {
			world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "*********************************************************************************************************** SOLVE NCS USELESSNESS")));
			world.raiseNCS(NCS.CONTEXT_USELESSNESS);
			this.die();
			world.getScheduler().getHeadAgent().setBadCurrentCriticalityMapping();
		}

	}
	
	/**
	 * Solve NC S conflict inexact.
	 *
	 * @param head the head
	 */
	private void solveNCS_ConflictInexact(Head head) {
		//////System.out.println(world.getScheduler().getTick() +" " + this.getName()+ " " + "solveNCS_ConflictInexact");
		world.raiseNCS(NCS.CONTEXT_CONFLICT_INEXACT);
		if(true) {
			confidence--;
		}
		//confidence = confidence * 0.5;
		updateExperiments();
	}
	
	
	
	private void setModelFromBetterContext(Context betterContext) {
		localModel = new LocalModelMillerRegression(world,this);
		
		this.confidence = betterContext.getConfidence();	
		
		double[] coef = ((LocalModelMillerRegression) betterContext.getLocalModel()).getCoef();

		((LocalModelMillerRegression) this.localModel).setCoef(coef);
		
		this.actionProposition = ((LocalModelMillerRegression) betterContext.getLocalModel()).getProposition(betterContext);
		
		this.experiments = new ArrayList<Experiment>();
		experiments.addAll(betterContext.getExperiments());
	}
	
	public void analyzeResults2(Head head) {

		//addNewExperiment();
		
		System.out.println(localModel.distance(getCurrentExperiment()) + "******************************************************************DISTANCE TO MODEL : " );
		
		if (head.getCriticity(this) > head.getErrorAllowed()) {
			
			
			Context betterContext = null;//head.getBetterContext(this,head.getActivatedNeighborsContexts(), getErrorOnAllExperiments());
			
			if(betterContext != null) {
				System.out.println(this.getName() + "<---" + betterContext.getName());
				this.setModelFromBetterContext(betterContext);				
				world.getScheduler().getHeadAgent().setBadCurrentCriticalityPrediction();
			}
			else {
				System.out.println("OLD COEFS " + localModel.coefsToString());
				LocalModelAgent newBetterModel = tryNewExperiment();
				
				if(newBetterModel!=null) {
					localModel = newBetterModel;
					System.out.println("NEW COEFS " + localModel.coefsToString());
					world.getScheduler().getHeadAgent().setBadCurrentCriticalityPrediction();
					
				}
				else {
					solveNCS_BadPrediction(head);
					this.world.getScheduler().addAlteredContext(this);
				}
			}
			
		}else {
			System.out.println("OLD COEFS " + localModel.coefsToString());
			//localModel.updateModelWithExperimentAndWeight(getCurrentExperiment(),0.5);
			//addCurrentExperimentTo(experiments);
			System.out.println("NEW COEFS " + localModel.coefsToString());
			confidence++;
			
		}
		
//		mappingCriticality = 0.0;
//		if(head.getActivatedNeighborsContexts().size()>0) {
//			for(Context ctxt : head.getActivatedNeighborsContexts()) {
//				mappingCriticality += this.distance(ctxt);
//			}
//			mappingCriticality /= head.getActivatedNeighborsContexts().size();
//		}

		
		//NCSDetection_OverMapping();	
		
	}
	
	
	public void analyzeResults3(Head head, Context closestContextToOracle) {

		
		if (head.getCriticity(this) < head.getErrorAllowed()) {
			
			confidence++;	
			
		}else {
			if(this != closestContextToOracle) {
				this.solveNCS_BadPrediction(head);
			}
			
		}
		
//		if (head.getCriticity(this) > head.getErrorAllowed()) {
//			
//			
//			Context betterContext = null;//head.getBetterContext(this,head.getActivatedNeighborsContexts(), getErrorOnAllExperiments());
//			
//			if(betterContext != null) {
//				System.out.println(this.getName() + "<---" + betterContext.getName());
//				this.setModelFromBetterContext(betterContext);				
//				world.getScheduler().getHeadAgent().setBadCurrentCriticalityPrediction();
//			}
//			else {
//				System.out.println("OLD COEFS " + localModel.coefsToString());
//				LocalModelAgent newBetterModel = tryNewExperiment();
//				
//				if(tryNewExperiment2()) {
//					System.out.println("NEW COEFS " + localModel.coefsToString());
//					world.getScheduler().getHeadAgent().setBadCurrentCriticalityPrediction();
//				}
//				else {
//					solveNCS_Conflict(head);
//					this.world.getScheduler().addAlteredContext(this);
//				}
//			}
//			
//		}else {
//			System.out.println("OLD COEFS " + localModel.coefsToString());
//			localModel.updateModelWithExperimentAndWeight(getCurrentExperiment(),0.5);
//			System.out.println("NEW COEFS " + localModel.coefsToString());
//			confidence++;
//			
//		}
		
//		mappingCriticality = 0.0;
//		if(head.getActivatedNeighborsContexts().size()>0) {
//			for(Context ctxt : head.getActivatedNeighborsContexts()) {
//				mappingCriticality += this.distance(ctxt);
//			}
//			mappingCriticality /= head.getActivatedNeighborsContexts().size();
//		}

		
		//NCSDetection_OverMapping();	
		
	}
	
//	public double distance(Context ctxt) {
//		double totalDistance = 1.0;
//		double currentDistance = 0.0;
//		int overlaps = 0;
//		int voids = 0;
//		
//		for(Percept pct : world.getScheduler().getPercepts()) {
//			currentDistance = this.distance(ctxt, pct);
//			
//			overlaps += ((currentDistance < 0) ? 1 : 0);
//			voids += ((currentDistance > 0) ? 1 : 0);
//			
//			totalDistance *= currentDistance;
//		}
//		
//		if(overlaps == world.getScheduler().getPercepts().size()) {
//			return - Math.abs(totalDistance);
//		}
//		else if((voids == 1) && (overlaps == world.getScheduler().getPercepts().size()-1)) {
//			
//		}
//		else {
//			return 0;
//		}
//	}
	
	
	
	public Pair<Double, Percept> distance(Context ctxt) {
		double minDistance = Double.POSITIVE_INFINITY;
		double maxDistance = Double.NEGATIVE_INFINITY;
		double currentDistance = 0.0;
		
		int overlapCounts = 0;
		Percept voidPercept = null;
		double voidDistance = 0.0 ;
		
		for(Percept pct : world.getScheduler().getPercepts()) {
			currentDistance = this.distance(ctxt, pct);
			overlapCounts = (currentDistance<0) ? overlapCounts+1 : overlapCounts;
			
			if(currentDistance>0) {
				voidPercept = pct;
				voidDistance = currentDistance/pct.getMinMaxDistance();
			}
			
			currentDistance = Math.abs(currentDistance);
			
			minDistance = Math.min(minDistance, currentDistance/pct.getMinMaxDistance());
			maxDistance = Math.max(maxDistance, currentDistance/pct.getMinMaxDistance());
			
					
			
		}
		
		if(overlapCounts == world.getScheduler().getPercepts().size()) {
			return new Pair<Double, Percept>(-minDistance, null);
		}
		else if(overlapCounts == (world.getScheduler().getPercepts().size() -1)){
			return new Pair<Double, Percept>(voidDistance, voidPercept);
		}
		else {
			return new Pair<Double, Percept>(maxDistance, null);
		}
				

	}
	
	public double distanceAsVolume(Context ctxt) {
		double totalDistanceAsVolume = 1.0;
		
		for(Percept pct : world.getScheduler().getPercepts()) {
			double currentDistance = this.distanceForVolume(ctxt, pct);
			totalDistanceAsVolume *= currentDistance;
			//System.out.println(pct.getName() + " " + currentDistance);					
			
		}
		
		return Math.abs(totalDistanceAsVolume);
	}
	
	public double maxDistance(Context ctxt) {
		double maxDistance = Double.NEGATIVE_INFINITY;
		
		for(Percept pct : world.getScheduler().getPercepts()) {
			double currentDistance = this.distanceForMaxOrMin(ctxt, pct)/pct.getMinMaxDistance();
			if(currentDistance>maxDistance) {
				maxDistance = currentDistance;
			}
			//System.out.println(pct.getName() + " " + currentDistance);					
			
		}
		
		return maxDistance;
	}
	
	public double minDistance(Context ctxt) {
		double minDistance = Double.POSITIVE_INFINITY;
		
		for(Percept pct : world.getScheduler().getPercepts()) {
			minDistance = Math.min(minDistance, this.distanceForMaxOrMin(ctxt, pct)/pct.getMinMaxDistance());
			//System.out.println(pct.getName() + " " + currentDistance);					
		}
		return minDistance;
	}
	
	public void NCSDetection_BetterNeighbor() {
		
		Context closestContextToOracle = this;
		double minDistanceToOraclePrediction = getLocalModel().distance(this.getCurrentExperiment());
		double currentDistanceToOraclePrediction = 0.0;
		
		for(Context ctxt : world.getScheduler().getHeadAgent().getActivatedNeighborsContexts()) {
			
			if(ctxt!=this) {
				currentDistanceToOraclePrediction = ctxt.getLocalModel().distance(this.getCurrentExperiment());
				if(currentDistanceToOraclePrediction<minDistanceToOraclePrediction) {
					minDistanceToOraclePrediction = currentDistanceToOraclePrediction;
					closestContextToOracle = ctxt;
				}
			}
			
		}
		
		if(closestContextToOracle!=this) {
			solveNCS_BetterNeighbor(closestContextToOracle);
		}
		
	}
	
	public void solveNCS_BetterNeighbor(Context betterContext) {
		world.trace(new ArrayList<String>(Arrays.asList(this.getName(), betterContext.getName(), "*********************************************************************************************************** SOLVE NCS BETTER NEIGHBOR")));
		localModel = new LocalModelMillerRegression(world, this, betterContext.getLocalModel().getCoef(), betterContext.getLocalModel().getFirstExperiments()); 
	}
	
	public void NCSDetection_OverMapping() {
		
		boolean fusionAcomplished = false;
		
		
		for(Context ctxt : world.getScheduler().getHeadAgent().getActivatedNeighborsContexts()) {
			
			
			if(ctxt != this && !ctxt.isDying()) {
				
				fusionAcomplished = false;
	
				if(this.sameModelAs(ctxt, world.getScheduler().getHeadAgent().getErrorAllowed()/10) ) {
					
					for(Percept pct : ranges.keySet()) {
						
						boolean fusionTest = true;
						
						world.trace(new ArrayList<String>(Arrays.asList(this.getName(),ctxt.getName(),pct.getName(), ""+Math.abs(this.distance(ctxt, pct)), "DISTANCE", "" + world.getMappingErrorAllowed())));
						if(Math.abs(this.distance(ctxt, pct)) < pct.getMappingErrorAllowed()){		
														
							for(Percept otherPct : ranges.keySet()) {
								
								if(otherPct != pct) {
																		
									double lengthDifference = Math.abs(ranges.get(otherPct).getLenght() - ctxt.getRanges().get(otherPct).getLenght());
									double centerDifference = Math.abs(ranges.get(otherPct).getCenter() - ctxt.getRanges().get(otherPct).getCenter());
									world.trace(new ArrayList<String>(Arrays.asList(this.getName(),ctxt.getName(),otherPct.getName(), ""+lengthDifference,""+centerDifference, "LENGTH & CENTER DIFF", ""  + world.getMappingErrorAllowed())));
									fusionTest = fusionTest && (lengthDifference < otherPct.getMappingErrorAllowed()) && (centerDifference< otherPct.getMappingErrorAllowed());
								}
							}
							
							if(fusionTest) {
								solveNCS_OverMapping(ctxt,pct);
								fusionAcomplished = true;
							}
							
						}
					}
					
				}
								
			}
		}
		
	}
	
	private void solveNCS_OverMapping(Context fusionContext, Percept perceptFusion) {
		world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "*********************************************************************************************************** SOLVE NCS OVERMAPPING")));
		world.raiseNCS(NCS.CONTEXT_OVERMAPPING);	
		
		
		if(this.getRanges().get(perceptFusion).getCenter()<fusionContext.getRanges().get(perceptFusion).getCenter()) {
			this.getRanges().get(perceptFusion).setEnd(fusionContext.getRanges().get(perceptFusion).getEnd());
		}else {
			this.getRanges().get(perceptFusion).setStart(fusionContext.getRanges().get(perceptFusion).getStart());
		}
		
		this.setConfidence(Math.max(this.getConfidence(), fusionContext.getConfidence()));
		
		fusionContext.die();
		world.getScheduler().getHeadAgent().setBadCurrentCriticalityMapping();
	}
	
	private boolean sameModelAs(Context ctxt, double errorAllowed) {
		
		if(this.getLocalModel().getCoef().length != ctxt.getLocalModel().getCoef().length) {
			return false;
		}
		else {
			double modelsDifference = 0.0;
			
			for(int i=0;i<this.getLocalModel().getCoef().length;i++) {
				modelsDifference += Math.abs(this.getLocalModel().getCoef()[i] - ctxt.getLocalModel().getCoef()[i]);
			}
			
			//world.trace(new ArrayList<String>(Arrays.asList(this.getName(),ctxt.getName(), ""+modelsDifference, "MODELS DIFFERENCE", ""+ errorAllowed)));
			if(modelsDifference<errorAllowed) {
				return true;
			}else {
				return false;
			}
		}
		
		
		
		
	}
	
	
	
	private void addCurrentExperiment() {
		ArrayList<Percept> percepts = world.getAllPercept();
		maxActivationsRequired = percepts.size();
		Experiment exp = new Experiment(this);
		for (Percept pct : percepts) {
			exp.addDimension(pct, pct.getValue());
		}
		exp.setOracleProposition(headAgent.getOracleValue());
		
		experiments.add(exp);
	}
	
	private void addCurrentExperimentTo(ArrayList<Experiment> experimentsList) {
		ArrayList<Percept> percepts = world.getAllPercept();
		maxActivationsRequired = percepts.size();
		Experiment exp = new Experiment(this);
		for (Percept pct : percepts) {
			exp.addDimension(pct, pct.getValue());
		}
		exp.setOracleProposition(headAgent.getOracleValue());
		
		experimentsList.add(exp);
	}
	
	public Experiment getCurrentExperiment() {
		ArrayList<Percept> percepts = world.getAllPercept();
		Experiment exp = new Experiment(this);
		for (Percept pct : percepts) {
			exp.addDimension(pct, pct.getValue());
		}
		exp.setOracleProposition(headAgent.getOracleValue());
		
		return exp;
	}
	
	private LocalModelAgent tryNewExperiment() {
		
		LocalModelAgent possibleNewlocalModel = new LocalModelMillerRegression(world,this);
		
		
		
		
		
		ArrayList<Experiment> newExperimentsList = new ArrayList<Experiment>();
		newExperimentsList.addAll(experiments);
		addCurrentExperimentTo(newExperimentsList);
		possibleNewlocalModel.updateModelWithExperiments(newExperimentsList);
		boolean betterModelTest = true;

		for(Experiment exp : experiments) {
			double oldModelError = Math.abs(localModel.getProposition(experiments, exp) - exp.getOracleProposition());
			double newModelError = Math.abs(possibleNewlocalModel.getProposition(newExperimentsList, exp) - exp.getOracleProposition());
			//world.trace(new ArrayList<String>(Arrays.asList(this.getName(),"OLD MODEL", oldModelError+"", "NEW MODEL", "" + newModelError)));
			betterModelTest = betterModelTest && (newModelError <=0.00001 + oldModelError);
			
		}
		
		if(betterModelTest || (experiments.size()< (world.getScheduler().getPercepts().size() +  1))) { //size
			experiments = newExperimentsList;
			return possibleNewlocalModel;
		}
		else return null;
	}
	
	private boolean tryNewExperiment2() {
		
		if(localModel.distance(getCurrentExperiment())<10.0) {
			localModel.updateModelWithExperimentAndWeight(getCurrentExperiment(),0.5,100);
			return true;
		}
		 return false;
	}
	
	public double sumOfRangesLengths() {
		double sum = 0;
		
		for(Percept pct : world.getScheduler().getPercepts()) {
			sum += this.getRanges().get(pct).getLenght();
		}
		
		return sum;
	}
	
	public double rangeLengthRatio(Percept pct) {
		return this.getRanges().get(pct).getLenght()/sumOfRangesLengths();
	}
	
	public Pair<Boolean, Double> tryAlternativeModel(LocalModelAgent alternativeModel) {
		
		boolean betterModelTest = true;
		double sumError = 0.0;

		for(Experiment exp : experiments) {
			double modelError = Math.abs(localModel.getProposition(experiments, exp) - exp.getOracleProposition());
			double alternativeModelError = Math.abs(alternativeModel.getProposition(experiments, exp) - exp.getOracleProposition());
			betterModelTest = betterModelTest && (alternativeModelError <=0.00001 + modelError);
			sumError += alternativeModelError;
		}
		
	
		
		return new Pair<Boolean, Double>(betterModelTest,sumError);
	}
	
	public Double getErrorOnAllExperiments() {
		
		double sumError = 0.0;

		for(Experiment exp : experiments) {
			double modelError = Math.abs(localModel.getProposition(experiments, exp) - exp.getOracleProposition());
			sumError += modelError;
		}
		
		return sumError;
	}
	
	
	
	public double getPropositionOnExperiment(Experiment exp) {
		return localModel.getProposition(experiments, exp);
	}
	
	private void addNewExperiment() {
		
		addCurrentExperiment();
		localModel.updateModel(this);
		
		
	}
	
	public void solveNCS_BadPrediction(Head head) {

		world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "*********************************************************************************************************** SOLVE NCS CONFLICT")));
		world.raiseNCS(NCS.CONTEXT_CONFLICT_FALSE);		
		
		if (head.getNewContext() == this) {
			head.setNewContext(null);
		};
		
		confidence -= 2;
		world.getScheduler().getHeadAgent().setBadCurrentCriticalityConfidence();

		ArrayList<Percept> percepts = new ArrayList<Percept>();
		percepts.addAll(ranges.keySet());

		Pair<Percept, Context> perceptForAdapatationAndOverlapingContext = getPerceptForAdaptationWithOverlapingContext(percepts);			
		Percept p = perceptForAdapatationAndOverlapingContext.getA();
		
//		if(perceptForAdapatationAndOverlapingContext.getB()!=null) {
//			world.trace(new ArrayList<String>(Arrays.asList(this.getName(),p.getName(),perceptForAdapatationAndOverlapingContext.getB().getName(), "*********************************************************************************************************** CONFLICT OVERLAP")));
//			Range overlapingRange = perceptForAdapatationAndOverlapingContext.getB().getRanges().get(p);
//			
//			
//			if (Math.abs(overlapingRange.getStart() - this.getRanges().get(p).getEnd()) >= Math.abs(overlapingRange.getEnd() - this.getRanges().get(p).getStart())) {
//						
//				this.getRanges().get(p).adaptOnOverlap(overlapingRange, overlapingRange.getEnd());
//			} else {
//				this.getRanges().get(p).adaptOnOverlap(overlapingRange, overlapingRange.getStart());
//			}
//			
//			//ranges.get(p).adapt(p.getValue(), Math.abs(this.distance(overlapingContext, p))); 
//			
//		}else {
//			ranges.get(p).adapt(p.getValue()); 
//		}
		
		ranges.get(p).adapt(p.getValue());
		
		
//		if(perceptForAdapatationAndOverlapingContext.getB()!=null) {
//			if(testIfOtherContextShouldFinalyShrink(perceptForAdapatationAndOverlapingContext.getB(), perceptForAdapatationAndOverlapingContext.getA())){
//				perceptForAdapatationAndOverlapingContext.getB().getRanges().get(p).adapt(p.getValue());
//			}
//		}
		
		
		
//		if (head.isContextFromPropositionWasSelected() && head.getCriticity() <= head.getErrorAllowed()){
//			
//			//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
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

//		for (Percept v : ranges.keySet()) {
//			if (ranges.get(v).isTooSmall()){
//				solveNCS_Uselessness();
//				break;
//			}
//		}
		world.getScheduler().getHeadAgent().setBadCurrentCriticalityMapping();
	}
	
	private boolean testIfOtherContextShouldFinalyShrink(Context otherContext, Percept shrinkingPercept) {
		boolean test = true;
		
		for(Percept pct : ranges.keySet()) {
			if(pct != shrinkingPercept) {
				test = test && (getRanges().get(pct).getLenght()>otherContext.getRanges().get(pct).getLenght());
			}
		}
		
		return test;
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
		
		//////System.out.println("PerceptWithLesserImpactOnVolumeNotIncludedIn ...");
		for (Percept percept : containingRanges) {
			

			if (!ranges.get(percept).isPerceptEnum()) {
				Range otherRanges = otherContext.getRanges().get(percept);
				
				if (!(otherRanges.getStart() <= ranges.get(percept).getStart() &&   ranges.get(percept).getEnd() <= otherRanges.getEnd())) {
					
					//////System.out.println(percept.getName());
					
					
					if (ranges.get(percept).getNearestLimit(percept.getValue()) == false) {
						//////System.out.println("end simu : " + ranges.get(percept).simulateNegativeAVTFeedbackEnd(percept.getValue()) + " start : " + ranges.get(percept).getStart());
						vol = ranges.get(percept).simulateNegativeAVTFeedbackEnd(percept.getValue()) - ranges.get(percept).getStart();
					} else {
						//////System.out.println("end : " + ranges.get(percept).getEnd() + " start simu : " + ranges.get(percept).simulateNegativeAVTFeedbackStart(percept.getValue()));
						vol = ranges.get(percept).getEnd() - ranges.get(percept).simulateNegativeAVTFeedbackStart(percept.getValue());
					}
					
					
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
					
					
					//////System.out.println("Vol lost : " + volumeLost + "percept " + p.getName());
					
				}
				
				
			}
			
			
		}
		
		
		return p;
	}
	
	private Percept getPerceptWithLesserImpactOnVolumeNotIncludedIn3(ArrayList<Percept> containingRanges, Context otherContext) {
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol = 1.0;
		
		////////System.out.println("LESSER ...");
		for (Percept percept : containingRanges) {
			

			if (!ranges.get(percept).isPerceptEnum()) {
				Range otherRanges = otherContext.getRanges().get(percept);
				
				if (!(otherRanges.getStart() <= ranges.get(percept).getStart() &&   ranges.get(percept).getEnd() <= otherRanges.getEnd())) {
					
					////////System.out.println(percept.getName());
					
					
					vol = Math.abs( Math.abs(otherRanges.getCenter() - ranges.get(percept).getCenter()) - otherRanges.getRadius() - ranges.get(percept).getRadius());
//					if (ranges.get(percept).getNearestLimit(percept.getValue()) == false) {
//						////////System.out.println("end simu : " + ranges.get(percept).simulateNegativeAVTFeedbackMax(percept.getValue()) + " start : " + ranges.get(percept).getStart());
//						vol = percept.getValue() - ranges.get(percept).getStart();
//					} else {
//						////////System.out.println("end : " + ranges.get(percept).getEnd() + " start simu : " + ranges.get(percept).simulateNegativeAVTFeedbackMin(percept.getValue()));
//						vol = ranges.get(percept).getEnd() - percept.getValue();
//					}
					
					
					////////System.out.println("Vol1 : " + vol);
					
					for (Percept p2 : ranges.keySet()) {
						if (!ranges.get(p2).isPerceptEnum() && p2 != percept) {
							////////System.out.println(p2.getName());
							vol *= ranges.get(p2).getLenght();
							////////System.out.println(p2.getName() + " " + ranges.get(p2).getLenght() + " " + getName());
						}
					}
					////////System.out.println("Vol2 : " + vol);
					
					
					if (vol < volumeLost) {
						volumeLost = vol;
						p = percept;
					}
					
					
					////////System.out.println("Vol lost : " + volumeLost);
					
				}
				
				
			}
			
			
		}
		
		
		return p;
	}
	
	private Percept getPerceptWithBiggerImpactOnOverlap(ArrayList<Percept> percepts, Context bestContext) {
		Percept perceptWithBiggerImpact = null;
		double volumeLost = Double.MAX_VALUE;
		double vol = 1.0;
		
		for (Percept percept : percepts) {
			

			if (!ranges.get(percept).isPerceptEnum()) {
				Range bestContextRanges = bestContext.getRanges().get(percept);
				
				if (!(bestContextRanges.getStart() <= ranges.get(percept).getStart() &&   ranges.get(percept).getEnd() <= bestContextRanges.getEnd())) {
					
					
					if(percept.contextOrder(this, bestContext)) {
						world.trace(new ArrayList<String>(Arrays.asList("ORDER :",percept.getName() ,this.getName(), bestContext.getName())));
						vol = Math.abs(percept.getEndRangeProjection(this) - percept.getStartRangeProjection(bestContext));
						if(vol<volumeLost) {
							volumeLost = vol;
							perceptWithBiggerImpact = percept;
						}
					}
					else if(percept.contextOrder(bestContext, this)) {
						world.trace(new ArrayList<String>(Arrays.asList("ORDER :",percept.getName() ,bestContext.getName(), this.getName())));
						vol = Math.abs(percept.getEndRangeProjection(bestContext) - percept.getStartRangeProjection(this));
						if(vol<volumeLost) {
							volumeLost = vol;
							perceptWithBiggerImpact = percept;
						}
					}
					else if(percept.contextIncludedIn(bestContext,this)) {
						world.trace(new ArrayList<String>(Arrays.asList("INCLUSION :",percept.getName() ,bestContext.getName(), this.getName())));
						vol = Math.abs(percept.getEndRangeProjection(bestContext) - percept.getStartRangeProjection(bestContext));
						if(vol<volumeLost) {
							volumeLost = vol;
							perceptWithBiggerImpact = percept;
						}
					}
					
				}
								
			}
						
		}

		
		
		return perceptWithBiggerImpact;
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
	////////System.out.println("percept " + p.getName());
	return p;
}


public double getOverlappingVolume(Context overlappingCtxt) {
	double volume = 1.0;
	for(Percept pct : ranges.keySet()) {
		volume *= this.getRanges().get(pct).overlapDistance(overlappingCtxt.getRanges().get(pct));
	}
	return volume;
}

private Pair<Percept, Context> getPerceptForAdaptationWithOverlapingContext(ArrayList<Percept> percepts) {
	Percept perceptForAdapation = null;
	Context overlapingContext = null;
	double minDistanceToFrontier = Double.MAX_VALUE;
	double distanceToFrontier;
	double maxOverlappingVolume = Double.NEGATIVE_INFINITY;
	double overlappingVolume;
	


	if(world.getScheduler().getHeadAgent().getActivatedContexts().size()>1) {
		
		for(Context ctxt : world.getScheduler().getHeadAgent().getActivatedContexts()) {
			
			
			
			if(ctxt != this) {
				if(this.containedBy(ctxt)) {
					this.die();
				}
				else {
					overlappingVolume = this.getOverlappingVolume(ctxt);
					if(overlappingVolume>maxOverlappingVolume) {
						perceptForAdapation = getPerceptWithBiggerImpactOnOverlap(percepts, ctxt);
						overlapingContext = ctxt;
					}
					
				}
				
			}
		
		}
	
	}
	if(perceptForAdapation == null) {
		for (Percept pct : percepts) {
			if (!ranges.get(pct).isPerceptEnum()) {

				distanceToFrontier = Math.min(ranges.get(pct).startDistance(pct.getValue()), ranges.get(pct).endDistance(pct.getValue()));
				
				if (distanceToFrontier < minDistanceToFrontier) {
					minDistanceToFrontier = distanceToFrontier;
					perceptForAdapation = pct;
				}
			
		}
	}

	
	

}

	return new Pair<Percept, Context>(perceptForAdapation,overlapingContext);
}


	public boolean containedBy(Context ctxt) {
		boolean contained = true;
		
		for(Percept pct : ranges.keySet()) {
			
			contained = contained && ranges.get(pct).containedBy(ctxt.getRanges().get(pct));
		}
		
		return contained;
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
		for(Experiment exp : experiments) {
			s+=exp.toString();
		}
		
		
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
		for(Experiment exp : experiments) {
			s+=exp.toString();
		}
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
			//////////System.out.println("INFLUTEST " + getInfluenceByPerceptSituation(pct, situation.get(pct)));
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
			//////////System.out.println("INFLUTEST " + getInfluenceByPerceptSituation(pct, situation.get(pct)));
			influence *= getInfluenceByPerceptSituation(pct, situation.get(pct));
		}
		
		return influence;
	}
	
	public double getWorstInfluenceWithConfidence(HashMap<Percept,Double> situation) {
		Double worstInfluence = Double.POSITIVE_INFINITY;
		Double currentInfluence = 0.0;
		
		for(Percept pct : situation.keySet()) {
			//////////System.out.println("INFLUTEST " + getInfluenceByPerceptSituation(pct, situation.get(pct)));
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
			//////////System.out.println("INFLUTEST " + getInfluenceByPerceptSituation(pct, situation.get(pct)));
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
	//	////////System.out.println("Update experiments");
		ArrayList<Percept> percepts = world.getAllPercept();
		maxActivationsRequired = percepts.size();
		Experiment exp = new Experiment(this);
		for (Percept pct : percepts) {
			exp.addDimension(pct, pct.getValue());
		}
		exp.setOracleProposition(headAgent.getOracleValue());
		
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
			solveNCS_BadPrediction(head);
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
	
	
	
	/**
	 * Grow every ranges allowing to includes current situation.
	 *
	 * @param head the head
	 */
	public void growRanges() {
		////////System.out.println("Grow " + this.getName() );
		ArrayList<Percept> allPercepts = world.getAllPercept();
		for (Percept pct : allPercepts) {
			boolean contain = ranges.get(pct).contains(pct.getValue()) == 0 ;
			////////System.out.println(pct.getName() + " " + contain);
			if (!contain) {
				ranges.get(pct).adapt(pct.getValue());
				//ranges.get(pct).extend(pct.getValue(), pct);
				//world.getScheduler().getHeadAgent().NCSMemories.add(new NCSMemory(world, new ArrayList<Context>(),"Grow Range "+ pct.getName()));
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

		
		Percept perceptWithBiggerImpactOnOverlap = getPerceptWithBiggerImpactOnOverlap(world.getScheduler().getPercepts(),bestContext);
		
			
		
		
		//if(perceptWithLesserImpact!=null) world.trace(new ArrayList<String>(Arrays.asList(this.getName(),perceptWithLesserImpact.getName(), "PERCEPT BIGGER IMPACT")));
		

		if (perceptWithBiggerImpactOnOverlap == null) {

			this.die();
		}else {
			
			//ranges.get(perceptWithBiggerImpactOnOverlap).matchBorderWithBestContext(bestContext);
			//ranges.get(perceptWithBiggerImpactOnOverlap).adaptTowardsBorder(bestContext);
			
			ranges.get(perceptWithBiggerImpactOnOverlap).adapt(perceptWithBiggerImpactOnOverlap.getValue());
			
			
//			if(testIfOtherContextShouldFinalyShrink(bestContext, perceptWithLesserImpact)){
//				bestContext.getRanges().get(perceptWithLesserImpact).adaptTowardsBorder(this);
//			}
//			else {
//				ranges.get(perceptWithLesserImpact).adaptTowardsBorder(bestContext);
//			}
			
			
		}
	}
	
	private ArrayList<Percept> getOverlapingPercepts(Context bestContext){
		ArrayList<Percept> overlapingPercepts = new ArrayList<Percept>();
		
		for(Percept pct : ranges.keySet()) {
			if(distance(bestContext, pct)<0) {
				overlapingPercepts.add(pct);
			}
		}
		
		return overlapingPercepts;
	}
	
	private double distance(Context ctxt, Percept pct) {
		return this.getRanges().get(pct).distance(ctxt.getRanges().get(pct));
	}
	
	private double distanceForVolume(Context ctxt, Percept pct) {
		return this.getRanges().get(pct).distanceForVolume(ctxt.getRanges().get(pct));
	}
	
	private double distanceForMaxOrMin(Context ctxt, Percept pct) {
		return Math.abs(this.getRanges().get(pct).distanceForMaxOrMin(ctxt.getRanges().get(pct)));
	}
	
	

	
	/* (non-Javadoc)
	 * @see agents.context.AbstractContext#die()
	 */
	public void die () {
		world.trace(new ArrayList<String>(Arrays.asList("-----------------------------------------",this.getName(), "DIE")));
		for(Context ctxt : world.getScheduler().getContextsAsContext()) {
			ctxt.removeContext(this);
		}
		
		//////////System.out.println("DIED : " + this.getName());
		if( localModel!=null) {
			localModel.die();
		}
		
		super.die();
		
		for(Percept percept : world.getScheduler().getPercepts()) {
			percept.deleteContextProjection(this);
		}
		
		if(!world.getScheduler().getToKillContext().contains(this)) {
			world.getScheduler().addToKillContext(this);
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
			//////////System.out.println(percept.getName()+"--->"+perceptValidities.get(percept));
			test = test && perceptValidities.get(percept);
		}
		return test;
	}
	
	

	
	public Boolean computeNeighborhoodValidityByPercepts() {
		Boolean test = true;
		for(Percept percept : perceptNeighborhoodValidities.keySet()) {
			//////////System.out.println(percept.getName()+"--->"+perceptNeighborhoodValidities.get(percept));
			test = test && perceptNeighborhoodValidities.get(percept);
		}
		return test;
	}
	
	
	public void addNonValidPercept(Percept pct) {
		//world.trace(new ArrayList<String>(Arrays.asList(this.getName(),pct.getName(), "NON VALID")));
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



	









	public Context clone() throws CloneNotSupportedException{
		return (Context)super.clone();
	}


	public LocalModelAgent getLocalModel() {
		return localModel;
	}
	
	public void setLocalModel(LocalModelAgent model) {
		 localModel = model;
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
				return otherContextsDistancesByPercept.get(ctxt).get(pct).getB();
			}
			
		}
		return null;
	}

	
	public double distance(Percept pct, double value) {
		return this.ranges.get(pct).distance(value);
	}


	public void NCSDetection_Uselessness() {
		for (Percept v : ranges.keySet()) {
			if (ranges.get(v).isTooSmall()){
				solveNCS_Uselessness();
				break;
			}
		}
//		if(!isDying) {
//			for(Context ctxt : world.getScheduler().getHeadAgent().getActivatedNeighborsContexts()) {
//				if(ctxt != this) {
//					if(this.getConfidence()<=ctxt.getConfidence()) {
//						if(this.containedBy(ctxt)) {
//							solveNCS_Uselessness();
//							break;
//						}
//					}
//				}
//			}
//		}
	}
	
	public String getColor() {
		String colors = "";
		Double r = 0.0;
		Double g = 0.0;
		Double b = 0.0;
		double[] coefs = this.getLocalModel().getCoef();

		if(coefs.length>0) {
			if(coefs.length==1) {
				
				b = normalizePositiveValues(255, 5, Math.abs(coefs[0]));
				if(b.isNaN()) {
					b = 0.0;
				}
			}
			else if(coefs.length==2) {

				g =  normalizePositiveValues(255, 5, Math.abs(coefs[0]));
				b =  normalizePositiveValues(255, 5, Math.abs(coefs[1]));
				if(g.isNaN()) {
					g = 0.0;
				}
				if(b.isNaN()) {
					b = 0.0;
				}
			}
			else if(coefs.length>=3) {

				r =  normalizePositiveValues(255, 5,  Math.abs(coefs[0]));
				g =  normalizePositiveValues(255, 5,  Math.abs(coefs[1]));
				b =  normalizePositiveValues(255, 5,  Math.abs(coefs[2]));
				if(r.isNaN()) {
					r = 0.0;
				}
				if(g.isNaN()) {
					g = 0.0;
				}
				if(b.isNaN()) {
					b = 0.0;
				}
			}
			else {
				r = 255.0;
				g = 255.0;
				b = 255.0;
			}
		}
		else {
			r = 255.0;
			g = 255.0;
			b = 255.0;
		}
		
		colors += r.intValue() + "," + g.intValue() + "," + b.intValue() + ",100";
		return colors;
	}
	
	public double normalizePositiveValues(double upperBound, double dispersion, double value) {
		return upperBound*2*(- 0.5 + 1/(1+Math.exp(-value/dispersion)));
	}
	
	public double getMappingCriticality() {
		return mappingCriticality;
	}
	
	

}
