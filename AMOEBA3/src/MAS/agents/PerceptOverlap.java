package MAS.agents;

import java.util.ArrayList;

import MAS.agents.context.Context;

public class PerceptOverlap {

	private double start;
	private double end;
	private Context context1;
	private Context context2;
	private String name;
	
	
	public PerceptOverlap(Context context1, Context context2, double start, double end, String name) {
		this.start = start;
		this.end = end;
		this.context1 = context1;
		this.context2 = context2;
		this.name = name;
	}
	
	public ArrayList<Context> getContexts(){
		ArrayList<Context> contexts = new ArrayList<Context>();
		contexts.add(context1);
		contexts.add(context2);
		return contexts;
	}
}
