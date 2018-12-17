package mas.kernel;

import java.util.ArrayList;
import java.util.HashMap;

import mas.agents.context.Context;
import mas.agents.head.Head;
import mas.agents.percept.Percept;

public class NCSMemory {

	private ArrayList<Context> contexts = new ArrayList<Context>();
	private ArrayList<Context> otherContexts = new ArrayList<Context>();
	private ArrayList<Percept> percepts = new ArrayList<Percept>();;
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
				contexts.add(new Context(ctxt));
			}
			for(Context ctx : world.getScheduler().getContextsAsContext()) {
				if(!concernContexts.contains(ctx)) {
					otherContexts.add(new Context(ctx));
				}
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
		
		
		
		
		for(Context ctxt : contexts) {
			if(bestContextExo != null) {
				if(ctxt.getName().equals(bestContextExo.getName())) {
					string += "BEST CONTEXT EXO\n";
				}
			}
			string += ctxt.toStringReducted(situation);
		}
		
		string += "Predictions -> EXO :" + head.getPrediction()  + "\n";
		if(head.getEndogenousPrediction2Contexts() != null) {
			string += "Predictions -> ENDO 2 CTXT :" + head.getEndogenousPrediction2Contexts() + "\n";
		}
		else {
			string += "Predictions -> ENDO 2 CTXT : - \n";
		}
		
		if(head.getEndogenousPredictionNContexts() != null) {
			string += "Predictions -> ENDO N CTXT :" + head.getEndogenousPredictionNContexts() + "\n";
		}
		else {
			string += "Predictions -> ENDO N CTXT : - \n";
		}
		
		string += "Predictions -> ORACLE :" + head.getOracleValue() + "\n\n";

		double exoError = Math.abs(head.getPrediction() - head.getOracleValue()) / Math.abs(head.getOracleValue());
		string += "Error -> EXO :" + exoError  + "\n";
		
		if(head.getEndogenousPrediction2Contexts() != null) {
			double endoError2Ctxt = Math.abs(head.getEndogenousPrediction2Contexts() - head.getOracleValue()) / Math.abs(head.getOracleValue());
			string += "Error -> ENDO 2 CTXT :" + endoError2Ctxt  + "\n";
		}
		
		if(head.getEndogenousPredictionNContexts() != null) {
			double endoErrorNCtxt = Math.abs(head.getEndogenousPredictionNContexts() - head.getOracleValue()) / Math.abs(head.getOracleValue());
			string += "Error -> ENDO N CTXT :" + endoErrorNCtxt  + "\n";
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
	
	public ArrayList<Context> getOtherContexts(){
		return otherContexts;
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
		
		if(head.getEndogenousPrediction2Contexts() != null) {
			double endoError2Ctxt = Math.abs(head.getEndogenousPrediction2Contexts() - head.getOracleValue()) / Math.abs(head.getOracleValue());
			
			return exoError - endoError2Ctxt;
		}
		else {
			return null;
		}
	}
}
