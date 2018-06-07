package experiments.badContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SimpleTimeZone;

import mas.agents.Agent;
import mas.agents.percept.ContextProjection;
import mas.agents.percept.Percept;
import mas.agents.context.Context;
import mas.agents.localModel.TypeLocalModel;
import mas.init.amoeba.AMOEBAFactory;
import mas.kernel.AMOEBA;

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
//		AMOEBA amoeba = AMOEBAFactory.createAMOEBA(viewer, "/experiments/badContext/BadContext.xml","/experiments/badContext/BadContext_solver.xml");
		AMOEBA amoeba = AMOEBAFactory.createAMOEBA(viewer, "BadContext.xml","BadContext_solver.xml");
		
		/* These method calls allow to setup AMOEBA*/
		amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoeba.setDataForErrorMargin(0.5, 1, 1, 0.4, 10, 100);
		amoeba.setDataForInexactMargin(0.5, 1, 1, 0.4, 10, 100);
		
		/* Default */
//		amoeba.setAVT_acceleration(2);
//		amoeba.setAVT_deceleration(1./3.);
//		amoeba.setAVT_percentAtStart(0.2);
		
		/* Custum */
//		amoeba.setAVT_acceleration(0.5);
//		amoeba.setAVT_deceleration(0.5);
//		amoeba.setAVT_percentAtStart(0.001);
		
		amoeba.setRememberState(false);
		amoeba.setGenerateCSV(false);
		
		/* This is the initialization of the studied system. It's only for the sake of example, not a part of AMOEBA initialization*/
		BadContextManager bcm = new BadContextManager(50.0);
		bcm.setWorld(amoeba.getScheduler().getWorld());
		ArrayList<Percept> percepts = new  ArrayList<Percept>();
		
		for (int i = 0 ; i < 20 ; i++) {

			/* This is the studied system part. Feel free to use any data source.*/
			bcm.playOneStep(0);
			
			/*This is a learning step of AMOEBA*/
			amoeba.learn(new HashMap<String, Double>(bcm.getOutput()));
			percepts = amoeba.getScheduler().getWorld().getAllPercept();
			try        
			{
			    Thread.sleep(500);
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
		
		AMOEBA_UI.launchOverlapDetection(amoeba);
		
		ArrayList<Percept> P = AMOEBA_UI.getAllPercepts(amoeba);
		ArrayList<Agent> A = AMOEBA_UI.getContexts(amoeba);
		ArrayList<Context> C = AMOEBA_UI.getContextsAsContexts(amoeba);

		//System.out.println(C);
		
		Percept P0 = P.get(0);
		Percept P1 = P.get(1);
		
		
		
		
		
		

		
		
		

	}

}
