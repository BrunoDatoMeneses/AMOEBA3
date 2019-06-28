package agents.head;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.platform.commons.util.ToStringBuilder;

import agents.context.Context;

public class EndogenousRequest {
	
	
	private Integer priority;
	private HashMap<String, Double> request;
	private ArrayList<Context> askingContexts;
	private String name;
	
	public EndogenousRequest(HashMap<String, Double> rqst, int prty, ArrayList<Context> contexts) {
	
		request = rqst;
		priority = prty;
		askingContexts = contexts;
		
		name = "";

		for(Context ctxt: askingContexts) {
			name += ctxt.getName();
		}
	}
	
	public EndogenousRequest(HashMap<String, Double> rqst, Integer prty) {
		
		request = rqst;
		priority = prty;
	}
	
	public Integer getPriority() {
		return priority;
	}
	
	public HashMap<String, Double> getRequest(){
		return request;
	}
	
	public String toString() {
		String m="";
		for(Context ctxt : askingContexts) {
			m+= ctxt.getName() + " ";
		}
		m+=priority + " ";
		m+= request;
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
	
	
	
}
