package agents.head;

import java.util.HashMap;

import org.junit.platform.commons.util.ToStringBuilder;

public class EndogenousRequest {
	
	
	private Integer priority;
	private HashMap<String, Double> request;
	
	public EndogenousRequest(HashMap<String, Double> rqst, int prty) {
	
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
		String m="" + priority + " ";
		m+= request;
		return m;
	} 

}
