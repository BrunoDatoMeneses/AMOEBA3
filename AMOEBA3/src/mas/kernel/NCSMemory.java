package mas.kernel;

import java.util.ArrayList;
import java.util.HashMap;

import mas.agents.context.Context;
import mas.agents.head.Head;
import mas.agents.percept.Percept;

public class NCSMemory {

	private ArrayList<Context> contexts = new ArrayList<Context>();
	private ArrayList<Context> NCSContexts = new ArrayList<Context>();
	private ArrayList<Context> partiallyActivatedContexts = new ArrayList<Context>();
	private ArrayList<Context> otherContexts = new ArrayList<Context>();
	private ArrayList<Percept> percepts = new ArrayList<Percept>();
	private Head head;
	private int tick;
	private World world;
	private String type;
	private Context bestContextExo;
	
	
	
	public NCSMemory(World wrld, ArrayList<Context> concernContexts, String NCSType) {
		world = wrld;
		type = NCSType;
		if(world.getScheduler().getHeadAgent().getBestContext() != null) {
			bestContextExo = new Context(world.getScheduler().getHeadAgent().getBestContext());
		}
		else {
			bestContextExo = null;
		}
		try {
			for(Percept pct : world.getScheduler().getPercepts()) {
				percepts.add( new Percept(pct));
			}
			for(Context ctxt : concernContexts) {
				NCSContexts.add(new Context(ctxt));
			}
			
			for(Percept pct : world.getScheduler().getPercepts()) {
				for(Context ctxt : world.getScheduler().getHeadAgent().getPartiallyActivatedContexts(pct)) {
					partiallyActivatedContexts.add(new Context(ctxt));
				}
			}
			
			
			
			for(Context ctx : world.getScheduler().getContextsAsContext()) {
				if((!concernContexts.contains(ctx)) && (!partiallyActivatedContexts.contains(ctx))) {
					otherContexts.add(new Context(ctx));
				}
			}
			
			for(Context ctx : world.getScheduler().getContextsAsContext()) {
				contexts.add(new Context(ctx));

			}
			
			head = (Head)world.getScheduler().getHeadAgent().clone();
			tick = world.getScheduler().getTick();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		

	}
	
	public String toString() {
		String string = "";
		
		string += type + " NCS " + tick + "\n";
		
		return string;
	}
	
	
	public String toStringDetailled() {
		String string = "";
		
		string += "Tick :" + tick + "\n\n";
		
		HashMap<Percept,Double> situation = new HashMap<Percept,Double>();
		for(Percept prct : percepts) {
			string += prct.getName() + " : " + prct.getValue() + "\n";
			situation.put(prct, prct.getValue());
		}
		
		string += "\n";
		
		
		
		
		for(Context ctxt : NCSContexts) {
			if(bestContextExo != null) {
				if(ctxt.getName().equals(bestContextExo.getName())) {
					string += "BEST CONTEXT EXO\n";
				}
			}
			string += ctxt.toStringReducted(situation);
		}
		
		string += "Predictions -> EXO :" + head.getPrediction()  + "\n";
		
		if(head.getEndogenousPredictionActivatedContextsOverlaps() != null) {
			string += "Predictions -> ENDO FULL DIM + CONF :" + head.getEndogenousPredictionActivatedContextsOverlaps() + "\n";
		}
		else {
			string += "Predictions -> ENDO FULL DIM + CONf : - \n";
		}
		
		if(head.getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluence() != null) {
			string += "Predictions -> ENDO WORST DIM + CONf :" + head.getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluence() + "\n";
		}
		else {
			string += "Predictions -> ENDO WORST DIM + CONf : - \n";
		}
		
		if(head.getEndogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence() != null) {
			string += "Predictions -> ENDO FULL DIM :" + head.getEndogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence() + "\n";
		}
		else {
			string += "Predictions -> ENDO FULL DIM : - \n";
		}
		
		if(head.getendogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence() != null) {
			string += "Predictions -> ENDO WORST DIM :" + head.getendogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence() + "\n";
		}
		else {
			string += "Predictions -> ENDO WORST DIM : - \n";
		}
		
		if(head.getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume() != null) {
			string += "Predictions -> ENDO WORST DIM + VOL :" + head.getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume() + "\n";
		}
		else {
			string += "Predictions -> ENDO WORST DIM + VOL : - \n";
		}
		
		if(head.getEndogenousPredictionActivatedContextsSharedIncompetence() != null) {
			string += "Predictions -> EndogenousPredictionActivatedContextsSharedIncompetence :" + head.getEndogenousPredictionActivatedContextsSharedIncompetence() + "\n";
		}
		else {
			string += "Predictions -> EndogenousPredictionActivatedContextsSharedIncompetence : -  \n";
		}
		
		string += "Predictions -> ORACLE :" + head.getOracleValue() + "\n\n";

		double exoError = Math.abs(head.getPrediction() - head.getOracleValue()) / Math.abs(head.getOracleValue());
		string += "Error -> EXO :" + exoError  + "\n";
		
		if(head.getEndogenousPredictionActivatedContextsOverlaps() != null) {
			double endoError2Ctxt = Math.abs(head.getEndogenousPredictionActivatedContextsOverlaps() - head.getOracleValue()) / (Math.abs(head.getOracleValue()) +1 );
			string += "Error -> EndogenousPredictionActivatedContextsOverlaps :" + endoError2Ctxt  + "\n";
		}
		
		if(head.getEndogenousPredictionActivatedContextsSharedIncompetence() != null) {
			double endoErrorNCtxt = Math.abs(head.getEndogenousPredictionActivatedContextsSharedIncompetence() - head.getOracleValue()) / Math.abs(head.getOracleValue());
			string += "Error -> getEndogenousPredictionActivatedContextsSharedIncompetence :" + endoErrorNCtxt  + "\n";
		}
		
		
		
		
		
		return string;
	}
	
	public int getTick() {
		return tick;
	}
	
	public Head getHead() {
		return head;
	}
	
	
	public ArrayList<Context> getContexts(){
		return contexts;
	}
	
	public ArrayList<Context> getNCSContexts(){
		return NCSContexts;
	}
	
	public ArrayList<Context> getOtherContexts(){
		return otherContexts;
	}
	
	public ArrayList<Context> getPartiallyActivatedContexts(){
		return partiallyActivatedContexts;
	}
	
	public ArrayList<Percept> getPercepts(){
		return percepts;
	}
	
	public Percept getPerceptByName(String name) {
		for(Percept pct : percepts) {
			if(pct.getName().equals(name)) {
				return pct;
			}
		}
		
		return null;
	}
	
	public Double getErrorLevelEndo2Ctxt() {
		double exoError = Math.abs(head.getPrediction() - head.getOracleValue()) / Math.abs(head.getOracleValue());
		
		if(head.getEndogenousPredictionActivatedContextsOverlaps() != null) {
			double endoError2Ctxt = Math.abs(head.getEndogenousPredictionActivatedContextsOverlaps() - head.getOracleValue()) / (Math.abs(head.getOracleValue() ) );
			
			return exoError - endoError2Ctxt;
		}
		else if(head.getEndogenousPredictionActivatedContextsSharedIncompetence() != null) {
			double endoError2Ctxt = Math.abs(head.getEndogenousPredictionActivatedContextsSharedIncompetence() - head.getOracleValue()) / (Math.abs(head.getOracleValue() ) );
			
			return exoError - endoError2Ctxt;
		}
		else {
			return null;
		}
	}
	
	public World getWorld() {
		return world;
	}
}
