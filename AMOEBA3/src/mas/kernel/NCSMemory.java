package mas.kernel;

import java.util.ArrayList;

import mas.agents.context.Context;
import mas.agents.head.Head;
import mas.agents.percept.Percept;

public class NCSMemory {

	private ArrayList<Context> contexts = new ArrayList<Context>();
	private ArrayList<Percept> percepts = new ArrayList<Percept>();;
	private Head head;
	private int tick;
	
	
	public NCSMemory(World world, ArrayList<Context> concernContexts) {
		
		
		
		try {
			for(Percept pct : world.getScheduler().getPercepts()) {
				percepts.add((Percept)pct.clone());
			}
			for(Context ctxt : concernContexts) {
				contexts.add((Context)ctxt.clone());
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
		
		string += "Tick :" + tick + "\n";
		
		for(Percept prct : percepts) {
			string += prct.getName() + " : " + prct.getValue() + "\n";
		}
		
		for(Context ctxt : contexts) {
			string += ctxt.getName() + "\n";
		}
		
		string += "Predictions -> EXO :" + head.getPrediction() + " ENDO :" + head.getEndogenousPrediction() +  " ORACLE :" + head.getOracleValue() + "\n";

		
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
}
