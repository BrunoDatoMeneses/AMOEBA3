package agents.context;

import java.io.Serializable;
import java.util.Comparator;

import agents.percept.Percept;

public class CustomComparator implements Comparator<Context>, Serializable {
	

	private Percept percept;
	private String range;
	
	
	public CustomComparator(Percept percept, String range){
		this.percept = percept;
		this.range = range;
	}
	
    @Override
    public int compare(Context c1, Context c2) {
    	
    	Double r1 = new Double(c1.getRanges().get(percept).getRange(range));
    	Double r2 = new Double(c2.getRanges().get(percept).getRange(range));
    	
        return r1.compareTo(r2);
    }
}