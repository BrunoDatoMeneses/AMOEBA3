package utils;

public class Quadruplet<A,B,C,D> {

	public  A a;
    public  B b;
    public  C c;
    public  D d;

    public Quadruplet(A a, B b, C c, D d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
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
    
    public D getD() {
    	return d;
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
    
    public void setD(D d) {
     	 this.d = d;
     }
}


