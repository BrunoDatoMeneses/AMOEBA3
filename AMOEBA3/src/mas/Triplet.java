package mas;

public class Triplet<A,B,C> {

	public  A a;
    public  B b;
    public  C c;

    public Triplet(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
    
    public A getA() {
    	return a;
    }
    
    public B getB() {
    	return b;
    }
    
    public C getC() {
    	return c;
    }
    
    public void setA(A a) {
    	 this.a = a;
    }
    
    public void setB(B b) {
   	 this.b = b;
   }
    
    public void setC(C c) {
      	 this.c = c;
      }
}


