package experiments.UnityLauncher;


import java.io.Serializable;
import java.util.HashMap;
import mas.agents.localModel.TypeLocalModel;
import mas.init.amoeba.AMOEBAFactory;
import mas.kernel.AMOEBA;

// TODO: Auto-generated Javadoc
/**
 * The Class BadContextLauncherEasy.
 */
public class Launcher implements Serializable {


	public static final boolean viewer = true;
	public static final boolean verboseriticity = true;

	public static void main(String[] args) {
		launch(viewer);
	}

	public static void launch(boolean viewer) {
	
		/*Here we create AMOEBA.*/
		AMOEBA amoeba = AMOEBAFactory.createAMOEBA(viewer,"BadContext_solver.xml");
		
		/* These method calls allow to setup AMOEBA*/
		amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		
		/* Error parameter */
		amoeba.setDataForErrorMargin(10, 5, 0.4, 10, 8000, 8000);
		
		/* Other parameters */
		amoeba.setRememberState(false);
		amoeba.setGenerateCSV(false);
		

		Manager f_XY_Manager = new Manager(50.0);
		

		int i = 0;
		while(i<10000) {
			
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
				amoeba.learn(new HashMap<String, Double>(f_XY_Manager.getOutputRequest(amoeba.getScheduler().getManualRequest())));
				amoeba.manual = false;
				
			}else if(amoeba.isRunning()) {
				
				/*Random samples of the studied system */
				f_XY_Manager.playOneStep(0);
				
				/*This is a learning step of AMOEBA*/
				amoeba.learn(new HashMap<String, Double>(f_XY_Manager.getOutput()));
				
				i++;
			}
			else if(amoeba.getPlayOneStep()) {
				
				amoeba.setPlayOneStep(false);
				/*Random samples of the studied system */
				f_XY_Manager.playOneStep(0);
				
				/*This is a learning step of AMOEBA*/
				amoeba.learn(new HashMap<String, Double>(f_XY_Manager.getOutput()));
				
				i++;
				
				
			}
 
			
		}
		
	
	}
}
