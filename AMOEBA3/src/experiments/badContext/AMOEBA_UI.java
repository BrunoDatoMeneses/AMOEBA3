package experiments.badContext;

import java.util.ArrayList;
import java.util.HashMap;

import MAS.agents.Agent;
import MAS.agents.Percept;
import MAS.agents.context.Context;
import MAS.agents.context.Range;
import MAS.agents.localModel.TypeLocalModel;
import MAS.init.amoeba.AMOEBAFactory;
import MAS.kernel.AMOEBA;

public class AMOEBA_UI {

	
	private AMOEBA amoeba;
	
	
	
	public AMOEBA_UI(boolean viewer, String percepts){
		
		if(percepts.contentEquals("position")){
			amoeba = AMOEBAFactory.createAMOEBA(viewer, "src/experiments/droneControl/DroneControl.xml",
					"src/experiments/droneControl/DroneControl_solver.xml");
		}
		else if (percepts.contentEquals("speed")){
			amoeba = AMOEBAFactory.createAMOEBA(viewer, "src/experiments/droneControl/DroneControlVariations.xml",
					"src/experiments/droneControl/DroneControlVariations_solver.xml");
		}
	}
	
	
	void init(){
		amoeba.getScheduler().getWorld().setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoeba.getScheduler().getHeadAgent().setDataForErrorMargin(0.1, 1, 1, 0.1, 10, 100);
		amoeba.getScheduler().getHeadAgent().setDataForInexactMargin(0.1, 1, 1, 0.1, 10, 100);
		amoeba.setAVT_acceleration(1.1f);
		amoeba.setAVT_deceleration(0.1);
		amoeba.setAVT_percentAtStart(0.1f);
	}
	
	ArrayList<Agent> getContexts(){
		return amoeba.getScheduler().getContexts();
	}
	
	public static ArrayList<Agent> getContexts(AMOEBA a){
		return a.getScheduler().getContexts();
	}
	
	public static ArrayList<Context> getContextsAsContexts(AMOEBA a){
		return a.getScheduler().getContextsAsContext();
	}
	
	public void learn(HashMap<String, Double> actions){
		amoeba.learn(actions);
	}
	
	public double request(HashMap<String, Double> actions){
		return amoeba.request(actions);
	}
	
	public ArrayList<Percept> getAllPercepts(){
		return amoeba.getScheduler().getWorld().getAllPercept();
	}
	
	public static ArrayList<Percept> getAllPercepts(AMOEBA a){
		return a.getScheduler().getWorld().getAllPercept();
	}
	
	public HashMap<Percept, Range> getRanges(){
		return amoeba.getScheduler().getHeadAgent().getBestContext().getRanges();
	}
	
	public String getBestContextId(){
		return Integer.toString(amoeba.getScheduler().getHeadAgent().getBestContext().getID());
	}
	
	public HashMap<String, Double> getBestContextPerceptCenter(){
		HashMap<String, Double> goal = new HashMap<String, Double>();
		double[] perceptCenter = new double[getAllPercepts().size()];
		ArrayList<Percept> percepts= this.getAllPercepts();
		HashMap<Percept, Range> contextRanges = this.getRanges();
		
		for(int i=0; i<perceptCenter.length; i++){
			perceptCenter[i] = (contextRanges.get(percepts.get(i)).getStart() + contextRanges.get(percepts.get(i)).getEnd())/2;
			goal.put("P"+i, (contextRanges.get(percepts.get(i)).getStart() + contextRanges.get(percepts.get(i)).getEnd())/2);
		}
		
		return goal;
	}
	
	public String getBestContextMessage(){
		String message ="";	
		ArrayList<Percept> percepts= this.getAllPercepts();
		HashMap<Percept, Range> contextRanges = this.getRanges();
		String[] contextDescription = new String[8];
		
		contextDescription[0] = Double.toString((contextRanges.get(percepts.get(0)).getStart() + contextRanges.get(percepts.get(0)).getEnd())/2);
		contextDescription[1] = Double.toString((contextRanges.get(percepts.get(1)).getStart() + contextRanges.get(percepts.get(1)).getEnd())/2);
		contextDescription[2] = Double.toString((contextRanges.get(percepts.get(2)).getStart() + contextRanges.get(percepts.get(2)).getEnd())/2);
		contextDescription[3] = Double.toString((contextRanges.get(percepts.get(0)).getLenght()));
		contextDescription[4] = Double.toString((contextRanges.get(percepts.get(1)).getLenght()));
		contextDescription[5] = Double.toString((contextRanges.get(percepts.get(2)).getLenght()));
		contextDescription[6] = Double.toString((amoeba.getScheduler().getHeadAgent().getBestContext().getActionProposal()));
		contextDescription[7] = Integer.toString(amoeba.getScheduler().getHeadAgent().getBestContext().getID());
		
		message = message  + contextDescription[0] + " " + contextDescription[1] + " "	+ contextDescription[2];
		message = message + "_" + contextDescription[3] + " " + contextDescription[4] + " " + contextDescription[5];
		message = message + "_" + contextDescription[6];
		message = message + "_" + "100" + contextDescription[7]  ;
		
		return message;
	}
	
}
