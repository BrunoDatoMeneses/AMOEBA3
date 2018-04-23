package experiments.badContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import agents.Agent;
import agents.Percept;
import agents.context.Context;
import agents.localModel.TypeLocalModel;
import init.amoeba.AMOEBAFactory;
import kernel.AMOEBA;

// TODO: Auto-generated Javadoc
/**
 * The Class BadContextLauncherEasy.
 */
public class BadContextLauncherEasy implements Serializable {


	public static final boolean viewer = true;
	public static final boolean verboseriticity = true;

	public static void main(String[] args) {
		launch(viewer);
	}

	public static void launch(boolean viewer) {
	
		/*Here we create AMOEBA.*/
		AMOEBA amoeba = AMOEBAFactory.createAMOEBA(viewer, "src/experiments/badContext/BadContext.xml","src/experiments/badContext/BadContext_solver.xml");
		
		/* These method calls allow to setup AMOEBA*/
		amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoeba.setDataForErrorMargin(0.5, 1, 1, 0.4, 10, 100);
		amoeba.setDataForInexactMargin(0.5, 1, 1, 0.4, 10, 100);
		
		amoeba.setRememberState(false);
		amoeba.setGenerateCSV(false);
		
		/* This is the initialization of the studied system. It's only for the sake of example, not a part of AMOEBA initialization*/
		BadContextManager bcm = new BadContextManager();
		bcm.setWorld(amoeba.getScheduler().getWorld());
		
		for (int i = 0 ; i < 100 ; i++) {

			/* This is the studied system part. Feel free to use any data source.*/
			bcm.playOneStep(0);
			
			/*This is a learning step of AMOEBA*/
			amoeba.learn(new HashMap<String, Double>(bcm.getOutput()));
			
			try        
			{
			    Thread.sleep(1000);
			} 
			catch(InterruptedException ex) 
			{
			    Thread.currentThread().interrupt();
			}
		}
		
		/*for (int i = 0 ; i < 1000 ; i++) {
			bcm.playOneStep(0);
			HashMap<String, Double> data = new HashMap<String, Double>(bcm.getOutput());
		//	data.remove("test");
			System.out.println("Test request : " + amoeba.request(data));
		}*/
		
		ArrayList<Percept> P = AMOEBA_UI.getAllPercepts(amoeba);
		ArrayList<Agent> A = AMOEBA_UI.getContexts(amoeba);
		ArrayList<Context> C = AMOEBA_UI.getContextsAsContexts(amoeba);

		System.out.println(C);
		
		Percept P0 = P.get(0);
		Percept P1 = P.get(1);
		
		
		
		
		
		

		
		
		

	}

}
