package agents.head;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.platform.commons.util.ToStringBuilder;

import agents.context.Context;
import agents.percept.Percept;
import utils.Pair;

public class EndogenousRequest {
	
	
	private Integer priority;
	private HashMap<Percept, Double> request;
	private HashMap<Percept, Pair<Double, Double>> bounds;
	private ArrayList<Context> askingContexts;
	private String name;
	private REQUEST requestType;
	
	public EndogenousRequest(HashMap<Percept, Double> rqst, HashMap<Percept, Pair<Double, Double>> bnds, int prty, ArrayList<Context> contexts, REQUEST type) {
	
		request = rqst;
		bounds = bnds;
		priority = prty;
		askingContexts = contexts;
		
		name = "";

		for(Context ctxt: askingContexts) {
			name += ctxt.getName();
		}
		
		requestType = type;
	}
	
	public EndogenousRequest(HashMap<Percept, Double> rqst, Integer prty) {
		
		request = rqst;
		priority = prty;
	}
	
	public Integer getPriority() {
		return priority;
	}
	
	public HashMap<Percept, Double> getRequest(){
		return request;
	}
	
	public String toString() {
		String m="";
		for(Context ctxt : askingContexts) {
			m+= ctxt.getName() + " ";
		}
		m+=priority + " ";
		m+= request + " ";
		m+= requestType;
		return m;
	} 
	
	public ArrayList<Context> getAskingContexts(){
		return askingContexts;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean testIfContextsAlreadyAsked(ArrayList<Context> contexts) {
		boolean test = true;
		for(Context ctxt : contexts) {
			test = test && askingContexts.contains(ctxt);
		}
		return test;
	}
	
	public boolean requestInBounds(HashMap<Percept, Double> rqt) {
		boolean test = true;
		
		
		Iterator<Percept> it = rqt.keySet().iterator();
		
		while(test && it.hasNext()) {
			Percept pct = (Percept) it.next();
			test = test && ( (bounds.get(pct).getA() < rqt.get(pct))  &&  (rqt.get(pct) < bounds.get(pct).getB() ));
		}
		
		
		return test;
	}
	
	public REQUEST getType() {
		return requestType;
	}
	
	
	
}
