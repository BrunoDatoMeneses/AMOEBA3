package mas.agents.head;

import mas.agents.context.Context;
import mas.agents.percept.Percept;

public class Pair<Context1,Context2> {

	private Context l;
    private Context r;
    public Pair(Context l, Context r){
        this.l = l;
        this.r = r;
    }
    public Context getL(){ return l; }
    public Context getR(){ return r; }
    public void setL(Context l){ this.l = l; }
    public void setR(Context r){ this.r = r; }
	
    
    public void print(Percept p) {
    	if(l!=null && r!=null) {
    		System.out.println("< " + l.getRanges().get(p).getEnd() + " ; " + r.getRanges().get(p).getStart() + " >");
    	}
    	else if(l==null && r==null) {
    		System.out.println("< " + "-" + " ; " + "-" + " >");
    	}
    	else if(l==null) {
    		System.out.println("< " + "-" + " ; " + r.getRanges().get(p).getStart() + " >");
    	}
    	else if(r==null) {
    		System.out.println("< " + l.getRanges().get(p).getEnd() + " ; " + "-" + " >");
    	}
    	else {
    		System.out.println("ERROR PAIR");
    	}
    	
    }
    
    public void clear() {
    	this.l = null;
        this.r = null;
    }
    
    public boolean contains(Context ctxt) {
    	return ctxt == this.l || ctxt == this.r;
    }
}
