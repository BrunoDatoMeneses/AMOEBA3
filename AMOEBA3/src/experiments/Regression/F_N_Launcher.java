package experiments.Regression;

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
	public static final double oracleNoiseRange = 10;
	public static final double learningSpeed = 0.001;
	public static final int regressionPoints = 1000;
	public static final int dimension = 2	;

	public static void main(String[] args) {
		launch(viewer);
	}

	public static void launch(boolean viewer) {
		
		String XMLConfigFile = "nDimensionLauncher.xml";
		
		//XmlConfigGenerator.makeXML(XMLConfigFile, dimension);
	
		
		
		/*Here we create AMOEBA.*/
		AMOEBA amoeba = AMOEBAFactory.createAMOEBA(viewer,XMLConfigFile);
		
		/* These method calls allow to setup AMOEBA*/
		amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		
		/* Error parameter */
		amoeba.setDataForErrorMargin(1, 0.5, 0.5, 1, 20000, 20000);
		
		/* Other parameters */
		amoeba.setRememberState(false);
		amoeba.setGenerateCSV(false);
		

		F_N_Manager f_N_Manager = new F_N_Manager(50.0, dimension);
		
		for(Percept pct : amoeba.getScheduler().getPercepts()) {
			pct.setMin(-100.0);
			pct.setMax(100.0);
		}
		
		amoeba.getScheduler().getHeadAgent().learningSpeed = learningSpeed;
		amoeba.getScheduler().getHeadAgent().numberOfPointsForRegression = regressionPoints;
		
		
		
		amoeba.setRunning(true);
		
		amoeba.learn(new HashMap<String, Double>(f_N_Manager.getOriginOutput()));

		
		double[][] constrains = new double[dimension][2];
		for(int i = 0 ; i < dimension ; i++) {
			constrains[i][0] = amoeba.getScheduler().getContextsAsContext().get(0).getRanges().get(amoeba.getScheduler().getPercepts().get(0)).getStart();
			constrains[i][1] = amoeba.getScheduler().getContextsAsContext().get(0).getRanges().get(amoeba.getScheduler().getPercepts().get(0)).getEnd();
		}
		

		
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
				//System.out.println("                                                                                                     MANUAL REQUEST");
				amoeba.learn(new HashMap<String, Double>(f_N_Manager.getOutputRequest(amoeba.getScheduler().getManualRequest())));
				amoeba.manual = false;
				
			}else if(amoeba.isRunning()) {
				
				/*Random samples on the unique context */
				f_N_Manager.playOneStepConstrained(constrains);
				
				/*This is a learning step of AMOEBA*/
				amoeba.learn(new HashMap<String, Double>(f_N_Manager.getOutputWithNoise(oracleNoiseRange)));
				
				i++;
			}
			else if(amoeba.getPlayOneStep()) {
				
				amoeba.setPlayOneStep(false);
				/*Random samples on the unique context */
				f_N_Manager.playOneStepConstrained(constrains);
				
				/*This is a learning step of AMOEBA*/
				amoeba.learn(new HashMap<String, Double>(f_N_Manager.getOutputWithNoise(oracleNoiseRange)));
				
				i++;
				
				
			}
			
			if(amoeba.getScheduler().getHeadAgent().getCriticity()>1000000.0) {
				i=1000;
			}
 
			
		}
		
	
	}
}
