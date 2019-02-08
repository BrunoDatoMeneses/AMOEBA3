package mas.agents;

public class AbstractPair<A,B> {

	public final A a;
    public final B b;

    public AbstractPair(A a, B b) {
        this.a = a;
        this.b = b;
    }
    
    public A getA() {
    	return a;
    }
    
    public B getB() {
    	return b;
    }
}


