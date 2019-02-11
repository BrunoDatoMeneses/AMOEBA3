package agents.percept;

import agents.context.Context;

public class ContextProjection {
	
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
	
	public void updateStart() {
		this.start = context.getRanges().get(this.percept).getStart();
	}
	
	public void updateEnd() {
		this.end = context.getRanges().get(this.percept).getEnd();
	}
	
	public boolean contains(Double value) {
		return ((value > start)  && (value < end));
	}
	
	public Context getContext() {
		return this.context;
	}	
}
