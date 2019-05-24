package experiments.Regression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import experiments.FILE;
import mas.agents.AbstractPair;
import mas.agents.localModel.TypeLocalModel;
import mas.agents.percept.Percept;
import mas.init.amoeba.AMOEBAFactory;
import mas.kernel.AMOEBA;

public class XP {
	
	public static final double errorForEndingXPStep = 0.001;
	public static final int numberOfTestsForMean = 50;
	
	public static void main(String[] args) {
		launch();
	}

	public static void launch() {
	
		
		FILE file = new FILE("Regression","22052019_Equally_1");
		
		ArrayList<String> cycles = new ArrayList<String>();
		
		ArrayList<Double> learningSpeeds = new ArrayList<Double>(Arrays.asList(0.1,0.25,0.5,0.75,0.9));
		ArrayList<Integer> numbersOfRegressionPoints = new ArrayList<Integer>(Arrays.asList(20,40,60,80,90,100,200));
		

		AbstractPair<Integer, Integer> XPResults = new AbstractPair<Integer, Integer>(null,null);
		
		System.out.println("LEARNING SPEEDS");
		for(Double learningSpeed : learningSpeeds) {
			
			System.out.println(learningSpeed);
			
			for(Integer regressionPoints : numbersOfRegressionPoints) {
				
				
				for(int i=0;i<numberOfTestsForMean;i++) {
					
					XPResults = XPStep(file,regressionPoints,learningSpeed);
					cycles.add(""+XPResults.getA());
					
				}
				
				file.write(new ArrayList<String>(Arrays.asList(
						"Prediction error for ending",""+ errorForEndingXPStep,
						"Learning Speed",""+learningSpeed,
						"Number Of Regression Points",""+XPResults.getB(),
						"Number Of Test",""+numberOfTestsForMean)),
						cycles);
				
				cycles.clear();
				
			}
			
		}
		
		
		
		
		file.close();
	}
	
	
	public static AbstractPair<Integer, Integer> XPStep(FILE file, int numberOfPointsForRegression, double learningSpeed) {
		
		/*Here we create AMOEBA.*/
		AMOEBA amoeba = AMOEBAFactory.createAMOEBA(false,"BadContext_solver.xml");
		
		/* These method calls allow to setup AMOEBA*/
		amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		
		/* Error parameter */
		amoeba.setDataForErrorMargin(1, 0.5, 0.5, 1, 20000, 20000);
		
		/* Other parameters */
		amoeba.setRememberState(false);
		amoeba.setGenerateCSV(false);
		

		F_XY_Manager f_XY_Manager = new F_XY_Manager(50.0);
		
		for(Percept pct : amoeba.getScheduler().getPercepts()) {
			pct.setMin(-100.0);
			pct.setMax(100.0);
		}
		
		amoeba.getScheduler().getHeadAgent().learningSpeed = learningSpeed;
		amoeba.getScheduler().getHeadAgent().numberOfPointsForRegression = numberOfPointsForRegression;
		amoeba.setRunning(true);
		amoeba.learn(new HashMap<String, Double>(f_XY_Manager.getOriginOutput()));

		double[] constrains = new double[2];
		constrains[0] = amoeba.getScheduler().getContextsAsContext().get(0).getRanges().get(amoeba.getScheduler().getPercepts().get(0)).getStart();
		constrains[1] = amoeba.getScheduler().getContextsAsContext().get(0).getRanges().get(amoeba.getScheduler().getPercepts().get(0)).getEnd();

		int i = 0;
		while((amoeba.getScheduler().getHeadAgent().getCriticity()>errorForEndingXPStep || i==0) && i!=1000) {		

				/*Random samples on the unique context */
				f_XY_Manager.playOneStepConstrained(constrains);
				
				/*This is a learning step of AMOEBA*/
				amoeba.learn(new HashMap<String, Double>(f_XY_Manager.getOutput()));
				
				i++;
				
				if(amoeba.getScheduler().getHeadAgent().getCriticity()>1000000.0) {
					i=1000;
				}
			
		}	
		return new AbstractPair<Integer, Integer>(i-1, amoeba.getScheduler().getWorld().regressionPoints);
	}
}
