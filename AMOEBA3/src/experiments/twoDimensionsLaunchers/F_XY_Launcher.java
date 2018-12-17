package experiments.twoDimensionsLaunchers;

import java.io.Serializable;
import java.util.HashMap;
import mas.agents.localModel.TypeLocalModel;
import mas.init.amoeba.AMOEBAFactory;
import mas.kernel.AMOEBA;

// TODO: Auto-generated Javadoc
/**
 * The Class BadContextLauncherEasy.
 */
public class F_XY_Launcher implements Serializable {


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
		amoeba.setDataForErrorMargin(1000, 5, 0.4, 0.1, 40, 80);
		amoeba.setDataForInexactMargin(500, 2.5, 0.2, 0.05, 40, 80);
		
		/* Other parameters */
		amoeba.setRememberState(false);
		amoeba.setGenerateCSV(false);
		

		F_XY_Manager f_XY_Manager = new F_XY_Manager(50.0);

		
		for (int i = 0 ; i < 1000 ; i++) {

			/*Random samples of the studied system */
			f_XY_Manager.playOneStep(0);
			
			/*This is a learning step of AMOEBA*/
			amoeba.learn(new HashMap<String, Double>(f_XY_Manager.getOutput()));
						
		}		
	}
}
