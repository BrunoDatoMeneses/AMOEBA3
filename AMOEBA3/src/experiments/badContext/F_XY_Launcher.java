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
		
		//Dynamic errors
//		amoeba.setDataForErrorMargin(1000, 5, 0.4, 0.1, 40, 80);
//		amoeba.setDataForInexactMargin(500, 2.5, 0.2, 0.05, 40, 80);
		
		//amoeba.setDataForErrorMargin(100, 5, 0.4, 0.1, 10000, 20000);
		//amoeba.setDataForInexactMargin(50, 2.5, 0.2, 0.05, 10000, 20000);
		
		//Static errors
		amoeba.setDataForErrorMargin(5, 5, 0.4, 200, 5000, 100000);
		amoeba.setDataForInexactMargin(2.5, 2.5, 0.2, 100, 5000, 100000);
		
		//amoeba.setDataForErrorMargin(200, 5, 0.4, 200, 5000, 100000);
		//amoeba.setDataForInexactMargin(100, 2.5, 0.2, 100, 5000, 100000);
		
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
		//F_XY_ManagerSphere bcm = new F_XY_ManagerSphere(50.0);
		F_XY_Manager bcm = new F_XY_Manager(50.0);
		ArrayList<Percept> percepts = new  ArrayList<Percept>();
		
		for (int i = 0 ; i < 10 ; i++) {

			/* This is the studied system part. Feel free to use any data source.*/
			bcm.playOneStep(0);
			
			/*This is a learning step of AMOEBA*/
			amoeba.learn(new HashMap<String, Double>(bcm.getOutput()));
			percepts = amoeba.getScheduler().getWorld().getAllPercept();
		}
		
		
		while(true) {
			
			try        
			{
			    Thread.sleep(1000);
			} 
			catch(InterruptedException ex) 
			{
			    Thread.currentThread().interrupt();
			}
			
			if(amoeba.getScheduler().requestAsked()) {
				System.out.println("MANUAL REQUEST");
				amoeba.request(amoeba.getScheduler().getManualRequest());
			}
			
		}
		
// 		for (int i = 0 ; i < 1000 ; i++) {
//	 		bcm.playOneStep(0);
//	 		HashMap<String, Double> data = new HashMap<String, Double>(bcm.getOutput());
//	 		//data.remove("test");
//	 		amoeba.request(data);
//	 		
//	 		try        
//			{
//			    Thread.sleep(1000);
//			} 
//			catch(InterruptedException ex) 
//			{
//			    Thread.currentThread().interrupt();
//			}
// 		}
		
		/*for (int i = 0 ; i < 1000 ; i++) {
			bcm.playOneStep(0);
			HashMap<String, Double> data = new HashMap<String, Double>(bcm.getOutput());
		//	data.remove("test");
			System.out.println("Test request : " + amoeba.request(data));
		}*/
		
		/*AMOEBA_UI.launchOverlapDetection(amoeba);
		
		ArrayList<Percept> P = AMOEBA_UI.getAllPercepts(amoeba);
		ArrayList<Agent> A = AMOEBA_UI.getContexts(amoeba);
		ArrayList<Context> C = AMOEBA_UI.getContextsAsContexts(amoeba);

		System.out.println(C);
		
		Percept P0 = P.get(0);
		Percept P1 = P.get(1);*/
		
		
		
		
		
		

		
		
		

	}

}
