package experiments.droneControl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ControlLinearModel {

	private double[][] w_ij;
	private double[] crit;
	
	private double increment;
	private double threshold;
	
	
	private HashMap<String, Double> currentState;
	
	public ControlLinearModel() {

		w_ij = new double[3][3];

		for(int i=0; i<w_ij.length; i++) {
			for(int j=0; j<w_ij[i].length; j++) {
				w_ij[i][j] = 0.0f;
			}
		}
		
		increment = 0.1d;
		threshold = 5.0d;
		
		crit = new double[3];
	}

	public void learn(HashMap<String, Double> endogenousFeedback){
		
		double[] C = new double[3];
		
		currentState = new HashMap<String, Double>(endogenousFeedback);
		
		crit[0] = Math.abs(currentState.get("Vx"));
		crit[1] = Math.abs(currentState.get("Vy"));
		crit[2] = Math.abs(currentState.get("Vz"));

		
		


	}
	
	private int max(double[] tab){
		int maxTabIndex=-1;
		double maxTab = -1.0d;

		for(int i=0; i<tab.length;i++){
			if(tab[i]>maxTab) maxTabIndex = i;
		}
			
		return maxTabIndex;
	}
	
	private void incrementW_ij(int i, int j, int max){
		if (Math.abs(w_ij[i][j])<1){
			w_ij[i][j] += increment * crit[i]/Math.abs(crit[i]);
		}
		
	}
}
