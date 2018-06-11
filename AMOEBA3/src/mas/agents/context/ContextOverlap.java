package mas.agents.context;

import java.util.HashMap;

import mas.agents.percept.Percept;

public class ContextOverlap {

	Context context1;
	Context context2;
	
	String name;
	
	HashMap<Percept,HashMap<String,Double>> ranges;
	HashMap<String,HashMap<String,Double>> rangesByString = new HashMap<String,HashMap<String,Double>>();
	
	public ContextOverlap(Context context1, Context context2, HashMap<Percept,HashMap<String,Double>> ranges) {
		
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
	
	
}
