package MAS.agents;

import java.io.Serializable;

import MAS.agents.context.Context;
import MAS.kernel.World;

public class ContextProjection implements Serializable{
	
	
	private Percept percept;
	private Context context;
	
	
	private double start;
	private double end;
	
	
	
	
	public ContextProjection(Percept percept, Context context) {
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
	
	public void update() {
		this.start = context.getRanges().get(this.percept).getStart();
		this.end = context.getRanges().get(this.percept).getEnd();
	}

	public String getRanges() {
		return "{" + start + " , " + end + "}";
	}
	
	public boolean contains(Double value) {
		return ((value > start)  && (value < end));
	}
	
	public Context getContex() {
		return this.context;
	}
}
