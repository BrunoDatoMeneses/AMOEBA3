package agents.context;

import java.io.Serializable;
import java.util.Comparator;

import agents.percept.Percept;

public class CenterRangeComparator implements Comparator<Context>, Serializable{
	
	
	private Percept percept;
	private String range;
	
	
	public CenterRangeComparator(Percept percept){
		this.percept = percept;
	}
	
    @Override
    public int compare(Context c1, Context c2) {
    	
    	Double r1 = new Double(c1.getRanges().get(percept).getCenter());
    	Double r2 = new Double(c2.getRanges().get(percept).getCenter());
    	
        return r1.compareTo(r2);

    } 
    
}
