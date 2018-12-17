package experiments.droneControl;

public class VECTOR {

	
	public static double[] sum(double[] tab1, double[] tab2){
		double[] sumTab = new double[tab1.length];
		
		for(int i=0; i<tab1.length; i++){
			sumTab[i] = tab1[i] + tab2[i];
		}
		
		return sumTab;
	}
	
	public static double[] difference(double[] tab1, double[] tab2){
		double[] difTab = new double[tab1.length];
		
		for(int i=0; i<tab1.length; i++){
			difTab[i] = tab1[i] - tab2[i];
		}
		
		return difTab;
	}
	
	public static double[] termProduct(double[] tab1, double[] tab2){
		double[] productTab = new double[tab1.length];
		
		for(int i=0; i<tab1.length; i++){
			productTab[i] = tab1[i] * tab2[i];
		}
		
		return productTab;
	}
	
	public static double norm(double[] tab1){
		double norm = 0.0d;
		
		for(int i=0; i<tab1.length; i++){
			norm += Math.pow(tab1[i], 2);
		}
		norm = Math.sqrt(norm);
		
		return norm;
	}
	
	public static double scalarProduct(double[] tab1, double[] tab2){
		double product = 0.0d;
		
		for(int i=0; i<tab1.length; i++){
			product += tab1[i] * tab2[i];
		}
		
		return product;
	}
	
	public static double[] product(double coef, double[] tab){
		double[] productTab = new double[tab.length];
		
		for(int i=0; i<tab.length; i++){
			productTab[i] = coef*tab[i] ;
		}
		
		return productTab;
	}
	
	
	/**
	 * Projection of tab1 on tab2
	 *
	 */
	public static double[] projection(double[] tab1, double[] tab2){ 
		double[] projectionTab = new double[tab1.length];
		
		projectionTab = product((scalarProduct(tab1, tab2)/Math.pow(norm(tab2), 2)), tab2) ; //projection formula
		
		return projectionTab;
	}
	
	public static double[] identity(int size){ 
		double[] identity = new double[size];
		
		for(int i=0; i<identity.length; i++){
			identity[i] = 1.0d ;
		}
		
		return identity;
	}
}
