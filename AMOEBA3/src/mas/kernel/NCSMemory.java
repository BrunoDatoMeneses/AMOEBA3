package mas.kernel;

import java.util.ArrayList;

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
	
	
	public NCSMemory(World wrld, ArrayList<Context> concernContexts, String NCSType) {
		world = wrld;
		type = NCSType;
		
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
		
		string += "Tick :" + tick + "\n";
		
		for(Percept prct : percepts) {
			string += prct.getName() + " : " + prct.getValue() + "\n";
		}
		
		for(Context ctxt : contexts) {
			string += ctxt.toStringReducted();
		}
		
		string += "Predictions -> EXO :" + head.getPrediction()  + "\n";
		string += "Predictions -> ENDO :" + head.getEndogenousPrediction() + "\n";
		string += "Predictions -> ORACLE :" + head.getOracleValue() + "\n";

		double exoError = Math.abs(head.getPrediction() - head.getOracleValue()) / Math.abs(head.getOracleValue());
		double endoError = Math.abs(head.getEndogenousPrediction() - head.getOracleValue()) / Math.abs(head.getOracleValue());
		
		string += "Error -> EXO :" + exoError  + "\n";
		string += "Error -> ENDO :" + endoError  + "\n";
		
		
		
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
	
	public double getErrorLevel() {
		double exoError = Math.abs(head.getPrediction() - head.getOracleValue()) / Math.abs(head.getOracleValue());
		
		double endoError = Math.abs(head.getEndogenousPrediction() - head.getOracleValue()) / Math.abs(head.getOracleValue());
		
		return exoError - endoError;
	}
}
