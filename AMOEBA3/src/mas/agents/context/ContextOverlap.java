package mas.agents.context;

import java.io.Serializable;
import java.util.HashMap;

import mas.agents.head.Head;
import mas.agents.percept.Percept;
import mas.kernel.World;
import mas.ncs.NCS;

public class ContextOverlap implements Serializable{

	World world;
	
	Context context1;
	Context context2;
	
	String name;
	
	HashMap<Percept,HashMap<String,Double>> ranges;
	HashMap<String,HashMap<String,Double>> rangesByString = new HashMap<String,HashMap<String,Double>>();
	
	public ContextOverlap(World world, Context context1, Context context2, HashMap<Percept,HashMap<String,Double>> ranges) {
		
		this.world = world;
		this.context1 = context1;
		this.context2 = context2;
		this.ranges = ranges;
		this.name = context1.getName() + context2.getName();
		
		
		for(Percept percept : ranges.keySet()) {
			rangesByString.put(percept.getName(), new HashMap<String,Double>());
			double start = ranges.get(percept).get("start");
			double end = ranges.get(percept).get("end");
			rangesByString.get(percept.getName()).put("start", start);
			rangesByString.get(percept.getName()).put("end", end);
		}
		
	}
	
	
	public HashMap<String,Double> getRanges(Object perceptString) {
		return rangesByString.get(perceptString);
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		String s = "";
		
		s += "Name : " + name + "\n";
		s += "Context 1 : " + context1.getName() + "\n";
		s += "Context 2 : " + context2.getName() + "\n";
		for(Percept percept : ranges.keySet()) {
			s += percept.getName() + "\n";
			s += "Start " +ranges.get(percept).get("start") + "\n";
			s += "End " +ranges.get(percept).get("end") + "\n";
		}
		
		return s;
		
	}
	
	public double getMiddleValue(Percept percept) {
		return (ranges.get(percept).get("end") - ranges.get(percept).get("start"))/2;
	}
	
	public void solveNCS_Overlap(double conflictThreshold) {
		double context1OverlapProposal = context1.getOverlapActionProposal(this) ;
		double context2OverlapProposal = context2.getOverlapActionProposal(this) ;
		
		System.out.println("Proposal difference : " + Math.abs(context2OverlapProposal-context1OverlapProposal));
		
		if(Math.abs(context2OverlapProposal-context1OverlapProposal) > conflictThreshold) {
			solveNCS_OverlapConflict();
		}
		else {
			solveNCS_OverlapConcurrence();
		}
	}
	
	private void solveNCS_OverlapConflict() {
		if(context1.getConfidence()>context2.getConfidence()) {
			context2.shrinkRangesToJoinBordersOnOverlap(context1, this);
			System.out.println(context2.getName() + " shrinked");
		}
		else {
			context1.shrinkRangesToJoinBordersOnOverlap(context2, this);
			System.out.println(context1.getName() + " shrinked");
		}
		
		this.die();
		
	}
	
	private void solveNCS_OverlapConcurrence() {
		if(context1.getConfidence()>context2.getConfidence()) {
			context2.shrinkRangesToJoinBordersOnOverlap(context1, this);
			System.out.println(context2.getName() + " shrinked");
		}
		else {
			context1.shrinkRangesToJoinBordersOnOverlap(context2, this);
			System.out.println(context1.getName() + " shrinked");
		}
		
		this.die();
		
	}
	
	public double getLenghtByPercept(Percept percept) {
		return Math.abs(ranges.get(percept).get("start") - ranges.get(percept).get("end"));
	}
	
	private void die() {
		context1.deleteOverlap(this);
		context2.deleteOverlap(this);
		world.getScheduler().removeContextOverlap(this);
	}
	
	public boolean overlapComputedBy(Context context) {
		if( context.equals(context1) || context.equals(context2)) {
			return true;
		}
		else {
			return false;
		}
	}
	
}
