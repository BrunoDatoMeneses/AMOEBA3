package mas.agents.percept;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import mas.agents.context.Context;
import mas.kernel.World;

public class ContextProjection implements Serializable{
	
	
	World world;
	
	private Percept percept;
	private Context context;
	
	
	private double start;
	private double end;
	
	// For testing only //
	public ContextProjection(double start, double end) {
		this.percept = null;
		this.context = null;
		this.start = start;
		this.end = end;
	}
	
	
	public ContextProjection(World wrld, Percept percept, Context context) {
		world = wrld;
		this.percept = percept;
		this.context = context;
		this.start = context.getRanges().get(this.percept).getStart();
		this.end = context.getRanges().get(this.percept).getEnd();
	}
	
	public void setRanges(double start, double end) {
		this.start = start;
		this.end = end;
	}
	
	public void setRangeStart(double start) {
		this.start = start;
	}
	
	public void setRangeEnd(double end) {
		this.end = end;
	}
	
	public void updateStart() {
		this.start = context.getRanges().get(this.percept).getStart();
	}
	
	public void updateEnd() {
		this.end = context.getRanges().get(this.percept).getEnd();
	}

	public String getRanges() {
		return "{" + start + " , " + end + "}";
	}
	
	public double getRanges(String range) {
		if(range.equals("start")) {
			return this.start;
		}
		else if (range.equals("end")) {
			return this.end;
		}
		else {
			return 0;
		}
	}
	
	public boolean contains(Double value) {
		//world.trace(new ArrayList<String>(Arrays.asList(context.getName(),percept.getName(), ""+value, ""+(getCenter()-getRadius()), ""+ (getCenter() + getRadius()))));
		return Math.abs(value - getCenter()) <= getRadius() ;
	}
	
	public boolean contains(Double value, Double neighborhood) {
		//System.out.println(context.getName() +" "+ percept.getName() + " " + value + " " + (start - neighborhood) + " " + start + " " + end + " " + (end + neighborhood));
		return Math.abs(value - getCenter()) < (getRadius() + neighborhood );
	}
	
	public boolean inNeighborhoodOf(Double value) {
		return Math.abs(value - this.getCenter())< 2*getNeighboorhood();
	}
	
	public double distance(ContextProjection ctxtPrjct) {
		double contextCenter1 = this.getCenter();
		double contextCenter2 = ctxtPrjct.getCenter();
		double contextRadius1 = this.getRadius();
		double contextRadius2 = ctxtPrjct.getRadius();
		return Math.abs(contextCenter1 - contextCenter2) - contextRadius1 - contextRadius2;
	}
	
	public Context getContext() {
		return this.context;
	}
	
	public String toString() {
        return "{"+this.start+" , "+this.end+"}";
    }
	
	public double getRadius() {
		return (end - start)/2;
	}
	
	public double getCenter() {
		return (end + start)/2;
	}
	
	public double getNeighboorhood() {
		return getRadius();
	}
	
	public boolean inNeighborhood() {
		return this.contains(percept.getValue(), world.getContextCreationNeighborhood(context, percept)) ||
				this.contains(percept.getValue(), this.getRadius()/2);
	}
	
}
