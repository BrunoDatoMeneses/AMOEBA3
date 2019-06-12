package experiments.nDimensionsLaunchers;

import java.io.Serializable;
import java.util.HashMap;

import experiments.XmlConfigGenerator;
import mas.agents.localModel.TypeLocalModel;
import mas.agents.percept.Percept;
import mas.init.amoeba.AMOEBAFactory;
import mas.kernel.AMOEBA;

// TODO: Auto-generated Javadoc
/**
 * The Class BadContextLauncherEasy.
 */
public class F_N_Launcher implements Serializable {


	public static final boolean viewer = true;
	public static final double oracleNoiseRange = 0.0;
	public static final double learningSpeed = 0.01;
	public static final int regressionPoints = 100;
	public static final int dimension = 2	;
	public static final double spaceSize = 50.0	;

	public static void main(String[] args) {
		launch(viewer);
	}

	public static void launch(boolean viewer) {
		
		String XMLConfigFile = "nDimensionLauncher.xml";
		
		
		HashMap<String,Double> amoebaSelfRequest = null;
		boolean activeLearning = false;
		
		
		/*Here we create AMOEBA.*/
		AMOEBA amoeba = AMOEBAFactory.createAMOEBA(viewer,XMLConfigFile);
		
		/* These method calls allow to setup AMOEBA*/
		amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		
		/* Error parameter */
		amoeba.setDataForErrorMargin(1, 0.5, 0.5, 1, 20000, 20000);
		//amoeba.setDataForErrorMargin(1000, 0.5, 0.5, 1, 20, 20);
		
		/* Other parameters */
		amoeba.setRememberState(false);
		amoeba.setGenerateCSV(false);
		

		F_N_Manager f_N_Manager = new F_N_Manager(spaceSize, dimension);
		
		
		amoeba.getScheduler().getHeadAgent().learningSpeed = learningSpeed;
		amoeba.getScheduler().getHeadAgent().numberOfPointsForRegression = regressionPoints;
		
		
		
	

		
		amoeba.setRunning(false);
		
		int i = 0;
		while(i<10000) {
		//while((amoeba.getScheduler().getHeadAgent().getCriticity()>0.001 || i==0) && i!=1000) {
			
			//System.out.println("Running :" + amoeba.isRunning());
			try        
			{
			    Thread.sleep(amoeba.temporisation);
			} 
			catch(InterruptedException ex) 
			{
			    Thread.currentThread().interrupt();
			}
			
			
			
			
			if(amoeba.getScheduler().requestAsked()) {
				amoeba.manual = true;
				System.out.println("                                                                                                     MANUAL REQUEST");
				amoebaSelfRequest = amoeba.learn(new HashMap<String, Double>(f_N_Manager.getOutputRequest2D(amoeba.getScheduler().getManualRequest())));
				amoeba.manual = false;
				
			}else if(amoeba.isRunning()) {
				
				if(activeLearning) {
					activeLearning = false;
					amoebaSelfRequest = amoeba.learn(new HashMap<String, Double>(f_N_Manager.getOutputWithAmoebaRequest(amoebaSelfRequest, oracleNoiseRange)));
				}
				else {
					/*Random samples on the unique context */
					f_N_Manager.playOneStep(0);
					
					/*This is a learning step of AMOEBA*/
					amoebaSelfRequest = amoeba.learn(new HashMap<String, Double>(f_N_Manager.getOutputWithNoise(oracleNoiseRange)));
				}
				
				
				
				i++;
			}
			else if(amoeba.getPlayOneStep()) {
				
				System.out.println("                                                                                                     ONE STEP");
				System.out.println("ACTIVE LEARNING : " + activeLearning);
				
				
				amoeba.setPlayOneStep(false);
				
				
				if(activeLearning) {
					activeLearning = false;
					amoebaSelfRequest = amoeba.learn(new HashMap<String, Double>(f_N_Manager.getOutputWithAmoebaRequest(amoebaSelfRequest, oracleNoiseRange)));
				}else {
					/*Random samples on the unique context */
					f_N_Manager.playOneStep(0);
					
					/*This is a learning step of AMOEBA*/
					amoebaSelfRequest = amoeba.learn(new HashMap<String, Double>(f_N_Manager.getOutputWithNoise(oracleNoiseRange)));
				}
				
				System.out.println(amoebaSelfRequest);
				
				i++;
				
				
			}
			
			if(amoebaSelfRequest != null) {
				
				
				activeLearning = true;
			}
			
			if(amoeba.getScheduler().getHeadAgent().getCriticity()>1000000.0) {
				i=1000;
			}
 
			
		}
		
	
	}
}
