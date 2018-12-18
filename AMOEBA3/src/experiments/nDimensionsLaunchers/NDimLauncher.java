package experiments.nDimensionsLaunchers;

import java.io.Serializable;
import java.util.HashMap;
import mas.agents.localModel.TypeLocalModel;
import mas.init.amoeba.AMOEBAFactory;
import mas.kernel.AMOEBA;

// TODO: Auto-generated Javadoc
/**
 * The Class BadContextLauncherEasy.
 */
public class NDimLauncher implements Serializable {


	public static final boolean viewer = true;
	public static final boolean verboseriticity = true;
	
	static HashMap<String,Double> output;

	public static void main(String[] args) {
		launch(viewer);
	}

	public static void launch(boolean viewer) {
	
		/*Here we create AMOEBA.*/
		AMOEBA amoeba = AMOEBAFactory.createAMOEBA(viewer,"threeDimensionsLauncher.xml");
		
		/* These method calls allow to setup AMOEBA*/
		amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		
		/* Error parameter */
		amoeba.setDataForErrorMargin(1000, 5, 0.4, 0.1, 40, 80);
		amoeba.setDataForInexactMargin(500, 2.5, 0.2, 0.05, 40, 80);
		
		/* Other parameters */
		amoeba.setRememberState(false);
		amoeba.setGenerateCSV(false);
		
		// Dimension 3
		NDimManager f_XY_Manager = new NDimManager(50.0, 3);

		
		for (int i = 0 ; i < 500 ; i++) {

			/*Random samples of the studied system */
			f_XY_Manager.playOneStep(0);
			
			output = f_XY_Manager.getOutput();
			
			/*This is a learning step of AMOEBA*/
			amoeba.learn(output);
						
		}		
	}
}
