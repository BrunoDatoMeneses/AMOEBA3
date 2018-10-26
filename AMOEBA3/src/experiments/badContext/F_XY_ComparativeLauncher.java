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
public class F_XY_ComparativeLauncher implements Serializable {


	public static final boolean viewer = false;
	public static final boolean verboseriticity = true;

	public static void main(String[] args) {
		launch(viewer);
	}

	public static void launch(boolean viewer) {
	
		/*Here we create AMOEBA.*/
		AMOEBA amoeba1 = AMOEBAFactory.createAMOEBA(viewer,"BadContext_solver.xml");
		AMOEBA amoeba2 = AMOEBAFactory.createAMOEBA(true,"BadContext_solver.xml");
		AMOEBA amoeba3 = AMOEBAFactory.createAMOEBA(true,"BadContext_solver.xml");
		AMOEBA amoeba4 = AMOEBAFactory.createAMOEBA(viewer,"BadContext_solver.xml");
		
		FILE criticitiesFile = new FILE("C:/Users/dato/Documents/THESE/XP/","23102018", "Test_Errors_Cycles1000_FIX200_100_50_25NB40_80_80_160");
		FILE agentsContextesFile = new FILE("C:/Users/dato/Documents/THESE/XP/","23102018", "Test_NB_CTXT_Cycles1000_FIX200_100_50_25NB40_80_80_160");
		
		
		
		
		/* These method calls allow to setup AMOEBA*/
		amoeba1.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoeba2.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoeba3.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoeba4.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		
		//Errors margins
		amoeba1.setDataForErrorMargin(200, 5, 0.4, 0.1, 50000, 10000);
		amoeba1.setDataForInexactMargin(100.05, 2.5, 0.2, 0.05, 5000, 10000);
		amoeba2.setDataForErrorMargin(50, 5, 0.4, 0.1, 10000, 20000);
		amoeba2.setDataForInexactMargin(25, 2.5, 0.2, 0.05, 10000, 20000);
		amoeba3.setDataForErrorMargin(0.1, 5, 0.4, 0.1, 40, 80);
		amoeba3.setDataForInexactMargin(0.05, 2.5, 0.2, 0.05, 40, 80);
		amoeba4.setDataForErrorMargin(0.1, 5, 0.4, 0.1, 80, 160);
		amoeba4.setDataForInexactMargin(0.05, 2.5, 0.2, 0.05, 80, 160);
		
		
		criticitiesFile.write("ErrorMargin(200, 5, 0.4, 0.1, 50000, 10000) InexactMargin(100.05, 2.5, 0.2, 0.05, 5000, 10000)",
								"ErrorMargin(50, 5, 0.4, 0.1, 10000, 20000) InexactMargin(25, 2.5, 0.2, 0.05, 10000, 20000)", 
								"ErrorMargin(0.1, 5, 0.4, 0.1, 40, 80) InexactMargin(0.05, 2.5, 0.2, 0.05, 40, 80)", 
								"ErrorMargin(0.1, 5, 0.4, 0.1, 80, 160) InexactMargin(0.05, 2.5, 0.2, 0.05, 80, 160)");
		agentsContextesFile.write("ErrorMargin(200, 5, 0.4, 0.1, 50000, 10000) InexactMargin(100.05, 2.5, 0.2, 0.05, 5000, 10000)",
								"ErrorMargin(50, 5, 0.4, 0.1, 10000, 20000) InexactMargin(25, 2.5, 0.2, 0.05, 10000, 20000)", 
								"ErrorMargin(0.1, 5, 0.4, 0.1, 40, 80) InexactMargin(0.05, 2.5, 0.2, 0.05, 40, 80)", 
								"ErrorMargin(0.1, 5, 0.4, 0.1, 80, 160) InexactMargin(0.05, 2.5, 0.2, 0.05, 80, 160)");
		
		
		amoeba1.setRememberState(false);
		amoeba1.setGenerateCSV(false);
		amoeba2.setRememberState(false);
		amoeba2.setGenerateCSV(false);
		amoeba3.setRememberState(false);
		amoeba3.setGenerateCSV(false);
		amoeba4.setRememberState(false);
		amoeba4.setGenerateCSV(false);
		
		/* This is the initialization of the studied system. It's only for the sake of example, not a part of AMOEBA initialization*/
		F_XY_Manager bcm = new F_XY_Manager(50.0);
		

		double averageCriticity1 = 0.0;
		double averageCriticity2 = 0.0;
		double averageCriticity3 = 0.0;
		double averageCriticity4 = 0.0;
		
		
		for (int i = 0 ; i < 100 ; i++) {

			/* This is the studied system part. Feel free to use any data source.*/
			bcm.playOneStep(0);
			
			/*This is a learning step of AMOEBA*/
			amoeba1.learn(new HashMap<String, Double>(bcm.getOutput()));
			averageCriticity1 = amoeba1.getAveragePredictionCriticity();
			
			amoeba2.learn(new HashMap<String, Double>(bcm.getOutput()));
			averageCriticity2 = amoeba2.getAveragePredictionCriticity();
			
			amoeba3.learn(new HashMap<String, Double>(bcm.getOutput()));
			averageCriticity3 = amoeba3.getAveragePredictionCriticity();
			
			amoeba4.learn(new HashMap<String, Double>(bcm.getOutput()));
			averageCriticity4 = amoeba4.getAveragePredictionCriticity();
			
			criticitiesFile.write(averageCriticity1, averageCriticity2, averageCriticity3, averageCriticity4);
			agentsContextesFile.write(amoeba1.getNumberOfContextAgents(), amoeba2.getNumberOfContextAgents(), amoeba3.getNumberOfContextAgents(), amoeba4.getNumberOfContextAgents());
			
			
		}
		
		/*for (int i = 0 ; i < 1000 ; i++) {
			bcm.playOneStep(0);
			HashMap<String, Double> data = new HashMap<String, Double>(bcm.getOutput());
		//	data.remove("test");
			System.out.println("Test request : " + amoeba.request(data));
		}*/
		

		
		
		
		

		criticitiesFile.close();
		agentsContextesFile.close();
		

	}

}
