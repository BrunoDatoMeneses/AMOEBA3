package agents.context;

import java.util.HashMap;

import utils.Pair;
import agents.percept.Percept;

public class SpatialContext {

	private HashMap<Percept, Pair<Double, Double>> ranges = new HashMap<Percept, Pair<Double, Double>>();
	
	
	
	public SpatialContext(Context ctxt) { 
		
		for(Percept pct : ctxt.getRanges().keySet()) {
			ranges.put(pct, new  Pair<Double, Double>(ctxt.getRanges().get(pct).getStart(), ctxt.getRanges().get(pct).getEnd()));
		}
		
	}
	
	public void setRange(Percept pct, Pair<Double, Double> range) {
		ranges.put(pct, range);
	}
	
	public Pair<Double, Double> getRange(Percept pct){
		return ranges.get(pct);
	}
	
	
	public double getVolume() {
		double volume = 1.0;
		for(Percept pct : ranges.keySet()) {
			volume *= 2*getRadius(pct);
		}
		return volume;
	}
	
	public double getRadius(Percept pct) {
		return Math.abs(getEnd(pct) - getStart(pct))/2;
	}
	
	public double getCenter(Percept pct) {
		return (getEnd(pct) + getStart(pct))/2;
	}
	
	public double getEnd(Percept pct) {
		return ranges.get(pct).getB();
	}
	
	public void setEnd(Percept pct, double value) {
		ranges.get(pct).setB(value);
	}
	
	public void expandEnd(Percept pct, double value) {
		ranges.get(pct).setB(getEnd(pct) + value);
	}
	
	public double getStart(Percept pct) {
		return ranges.get(pct).getA();
	}
	
	public void setStart(Percept pct, double value) {
		ranges.get(pct).setA(value);
	}
	
	public void expandStart(Percept pct, double value) {
		ranges.get(pct).setA(getStart(pct) - value);
	}
	
	public double distance(Percept pct, Range otherRange) {
		
		return Math.abs(this.getCenter(pct) - otherRange.getCenter()) - this.getRadius(pct) - otherRange.getRadius();
	}
}


