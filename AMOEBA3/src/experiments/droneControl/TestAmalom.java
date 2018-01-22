package experiments.droneControl;

import java.util.HashMap;

import fr.irit.smac.lxplot.LxPlot;
import fr.irit.smac.lxplot.commons.ChartType;

public class TestAmalom {

	private static int cycles=0;
	
	public static void main(String[] args) {

		AMALOM amalom;
		double commandSize = 1.0d;
		
		/*amalom = new AMALOM(2, 2, 0.05d, 500, 0.05d, 0.3, ModelConstruction.HIGHEST_SENSIBILITY);
		double[] C = new double[2];
		
		for(int i =0; i<500;i++){
					
			C[0] = -commandSize + Math.random()*2.0d*commandSize; 
			C[1] = -commandSize + Math.random()*2.0d*commandSize;
			
			amalom.learn(getOutputAmalom(C, "variation"), getOutputAmalom(C, "command"));
			
			cycles = i;
		}*/
		
		/*amalom = new AMALOM(3, 3, 0.05d, 500, 0.05d, 0.3, ModelConstruction.HIGHEST_SENSIBILITY);
		double[] C = new double[3];
		
		for(int i =0; i<500;i++){
					
			C[0] = -commandSize + Math.random()*2.0d*commandSize; 
			C[1] = -commandSize + Math.random()*2.0d*commandSize;
			C[2] = -commandSize + Math.random()*2.0d*commandSize;
			
			amalom.learn(getOutputAmalom(C, "variation"), getOutputAmalom(C, "command"));
			
			cycles = i;
		}*/
		
		/*amalom = new AMALOM(5, 5, 0.05d, 500, 0.05d, 0.3, ModelConstruction.HIGHEST_SENSIBILITY);
		double[] C = new double[5];
		
		for(int i =0; i<500;i++){
					
			C[0] = -commandSize + Math.random()*2.0d*commandSize; 
			C[1] = -commandSize + Math.random()*2.0d*commandSize;
			C[2] = -commandSize + Math.random()*2.0d*commandSize;
			C[3] = -commandSize + Math.random()*2.0d*commandSize;
			C[4] = -commandSize + Math.random()*2.0d*commandSize;
			
			amalom.learn(getOutputAmalom(C, "variation"), getOutputAmalom(C, "command"));
			
			cycles = i;
		}*/
		
		amalom = new AMALOM(10, 10, 0.05d, 500, 0.05d, 0.3, ModelConstruction.HIGHEST_SENSIBILITY);
		double[] C = new double[10];
		
		for(int i =0; i<500;i++){
					
			C[0] = -commandSize + Math.random()*2.0d*commandSize; 
			C[1] = -commandSize + Math.random()*2.0d*commandSize;
			C[2] = -commandSize + Math.random()*2.0d*commandSize;
			C[3] = -commandSize + Math.random()*2.0d*commandSize;
			C[4] = -commandSize + Math.random()*2.0d*commandSize;
			C[5] = -commandSize + Math.random()*2.0d*commandSize; 
			C[6] = -commandSize + Math.random()*2.0d*commandSize;
			C[7] = -commandSize + Math.random()*2.0d*commandSize;
			C[8] = -commandSize + Math.random()*2.0d*commandSize;
			C[9] = -commandSize + Math.random()*2.0d*commandSize;
			
			amalom.learn(getOutputAmalom(C, "variation"), getOutputAmalom(C, "command"));
			
			cycles = i;
		}
		
	}
	
	public static double[] hiddenModel2x2(double[] C){
		double[] P = new double[C.length];
		
		P[0] = 0.1* C[0] +  0.0* C[1];
		P[1] = 0.0* C[0] + 0.1* C[1];
		
		LxPlot.getChart("TEST AMALOM", ChartType.LINE).add("C0", cycles,  C[0]);
		LxPlot.getChart("TEST AMALOM", ChartType.LINE).add("C1", cycles,  C[1]);
		LxPlot.getChart("TEST AMALOM", ChartType.LINE).add("P0", cycles,  P[0]);
		LxPlot.getChart("TEST AMALOM", ChartType.LINE).add("P1", cycles,  P[1]);
		
		return P;
		
		
	}
	
	public static double[] hiddenModel3x3(double[] C){
		double[] P = new double[C.length];
		
		P[0] = 0.1* C[0] +  0.0* C[1] + 0.0*C[2];
		P[1] = 0.0* C[0] + 0.1* C[1] + 0.0*C[2];
		P[2] = 0.0* C[0] + 0.0* C[1] + 0.1*C[2];
		
		LxPlot.getChart("TEST AMALOM", ChartType.LINE).add("C0", cycles,  C[0]);
		LxPlot.getChart("TEST AMALOM", ChartType.LINE).add("C1", cycles,  C[1]);
		LxPlot.getChart("TEST AMALOM", ChartType.LINE).add("C2", cycles,  C[2]);
		LxPlot.getChart("TEST AMALOM", ChartType.LINE).add("P0", cycles,  P[0]);
		LxPlot.getChart("TEST AMALOM", ChartType.LINE).add("P1", cycles,  P[1]);
		LxPlot.getChart("TEST AMALOM", ChartType.LINE).add("P2", cycles,  P[2]);
		
		return P;
		
		
	}
	
	public static double[] hiddenModel5x5(double[] C){
		double[] P = new double[C.length];
		
		P[0] = 0.1* C[0] +  0.0* C[1] + 0.0*C[2] + 0.0*C[3] + 0.0*C[4] ;
		P[1] = 0.0* C[0] +  0.1* C[1] + 0.0*C[2] + 0.0*C[3] + 0.0*C[4] ;
		P[2] = 0.0* C[0] +  0.0* C[1] + 0.1*C[2] + 0.0*C[3] + 0.0*C[4] ;
		P[3] = 0.0* C[0] +  0.0* C[1] + 0.0*C[2] + 0.1*C[3] + 0.0*C[4] ;
		P[4] = 0.0* C[0] +  0.0* C[1] + 0.0*C[2] + 0.0*C[3] + 0.1*C[4] ;
		

		
		return P;
		
		
	}
	
	public static double[] hiddenModel10x10(double[] C){
		double[] P = new double[C.length];
		

		
		P[0] = 0.1* C[0] +  0.0* C[1] + 0.0*C[2] + 0.0*C[3] + 0.0*C[4] + 0.0* C[5] +  0.0* C[6] + 0.0*C[7] + 0.0*C[8] + 0.0*C[9]  ;
		P[1] = 0.0* C[0] +  0.1* C[1] + 0.0*C[2] + 0.0*C[3] + 0.0*C[4] + 0.0* C[5] +  0.0* C[6] + 0.0*C[7] + 0.0*C[8] + 0.0*C[9]  ;
		P[2] = 0.0* C[0] +  0.0* C[1] + 0.1*C[2] + 0.0*C[3] + 0.0*C[4] + 0.0* C[5] +  0.0* C[6] + 0.0*C[7] + 0.0*C[8] + 0.0*C[9]  ;
		P[3] = 0.0* C[0] +  0.0* C[1] + 0.0*C[2] + 0.1*C[3] + 0.0*C[4] + 0.0* C[5] +  0.0* C[6] + 0.0*C[7] + 0.0*C[8] + 0.0*C[9]  ;
		P[4] = 0.0* C[0] +  0.0* C[1] + 0.0*C[2] + 0.0*C[3] + 0.1*C[4] + 0.0* C[5] +  0.0* C[6] + 0.0*C[7] + 0.0*C[8] + 0.0*C[9]  ;
		P[5] = 0.0* C[0] +  0.0* C[1] + 0.0*C[2] + 0.0*C[3] + 0.0*C[4] + 0.1* C[5] +  0.0* C[6] + 0.0*C[7] + 0.0*C[8] + 0.0*C[9]  ;
		P[6] = 0.0* C[0] +  0.0* C[1] + 0.0*C[2] + 0.0*C[3] + 0.0*C[4] + 0.0* C[5] +  0.1* C[6] + 0.0*C[7] + 0.0*C[8] + 0.0*C[9]  ;
		P[7] = 0.0* C[0] +  0.0* C[1] + 0.0*C[2] + 0.0*C[3] + 0.0*C[4] + 0.0* C[5] +  0.0* C[6] + 0.1*C[7] + 0.0*C[8] + 0.0*C[9]  ;
		P[8] = 0.0* C[0] +  0.0* C[1] + 0.0*C[2] + 0.0*C[3] + 0.0*C[4] + 0.0* C[5] +  0.0* C[6] + 0.0*C[7] + 0.1*C[8] + 0.0*C[9]  ;
		P[9] = 0.0* C[0] +  0.0* C[1] + 0.0*C[2] + 0.0*C[3] + 0.0*C[4] + 0.0* C[5] +  0.0* C[6] + 0.0*C[7] + 0.0*C[8] + 0.1*C[9]  ;
		

		
		return P;
		
		
	}
	
	
	
	public static HashMap<String, Double> getOutputAmalom(double[] C, String arg) {
		HashMap<String, Double> out = new HashMap<String, Double>();
		
		//double[] P = TestAmalom.hiddenModel2x2(C);
		//double[] P = TestAmalom.hiddenModel3x3(C);
		double[] P = TestAmalom.hiddenModel10x10(C);

		if (arg.contentEquals("speed")){
			for(int i =0; i<P.length;i++){
				out.put("P"+i,P[i]/0.2d);
			}
		} else if (arg.contentEquals("variation")){
			for(int i =0; i<P.length;i++){
				out.put("P"+i,P[i]);
			}
			out.put("D", 0.0d);
		}else if (arg.contentEquals("command")){
			for(int i =0; i<C.length;i++){
				out.put("C"+i,C[i]);
			}
		} else {
			out = null;
		}
	
	
	return out;
}

}
