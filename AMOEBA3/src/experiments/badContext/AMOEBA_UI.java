package experiments.badContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import mas.agents.Agent;
import mas.agents.percept.ContextProjection;
import mas.agents.percept.Percept;
import mas.agents.context.Context;
import mas.agents.context.ContextOverlap;
import mas.agents.context.ContextVoid;
import mas.agents.context.Range;
import mas.agents.localModel.TypeLocalModel;
import mas.init.amoeba.AMOEBAFactory;
import mas.kernel.AMOEBA;

public class AMOEBA_UI {

	
	private AMOEBA amoeba;
	
	
	
	public AMOEBA_UI(boolean viewer, String percepts){
		
		if(percepts.contentEquals("position")){
			amoeba = AMOEBAFactory.createAMOEBA(viewer, "experiments/droneControl/DroneControl.xml",
					"experiments/droneControl/DroneControl_solver.xml");
		}
		else if (percepts.contentEquals("speed")){
			amoeba = AMOEBAFactory.createAMOEBA(viewer, "experiments/droneControl/DroneControlVariations.xml",
					"experiments/droneControl/DroneControlVariations_solver.xml");
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
	
	
	public static void launchOverlapDetection(AMOEBA usedAmoeba) {
		
//		for(Percept percept : getAllPercepts(usedAmoeba)) {
//			percept.overlapsDetection();
//			percept.overlapNotification();
//		}
//		
//		displayPerceptInfo(usedAmoeba);
		
		for(Context context : getContextsAsContexts(usedAmoeba)) {
			
//			context.computeOverlapsByPercepts();
//			context.getNearestNeighbours();
//			
//			for(ContextOverlap contextOverlap : context.contextOverlaps) {
//				
//				usedAmoeba.getScheduler().getView().getTabbedPanel().getPanelTwoDimStandard().drawOverlap(contextOverlap);
//				
//			}
			
			context.computeNearestNeighbour();
			for(ContextVoid contextVoid : context.contextVoids) {
			usedAmoeba.getScheduler().getView().getTabbedPanel().getPanelTwoDimStandard().drawVoid(contextVoid);
			
		}
			
		}
		
		//displayContextInfo(usedAmoeba);
		
	}
	
	public static void displayPerceptInfo(AMOEBA usedAmoeba) {
		for(Percept percept : getAllPercepts(usedAmoeba)) {
			
			System.out.println("********************************** PERCEPT **********************************");
			
			System.out.println("Nbr of contexts "+percept.contextProjections.size());
			Collection<ContextProjection> contextProjections = percept.contextProjections.values();
			for(ContextProjection contextProjection : contextProjections) {
				System.out.println(contextProjection.getRanges());
			}
			
			System.out.println(percept.getName()+" overlaps "+percept.perceptOverlaps.size());
			for(String key : percept.perceptOverlaps.keySet()) {
				System.out.println(percept.perceptOverlaps.get(key));
			}
			
			//percept.displaySortedRanges();
			//percept.displaySortedRangesTreeSet();

		}
	}

	public static void displayContextInfo(AMOEBA usedAmoeba) {
		
		for(Context context : getContextsAsContexts(usedAmoeba)) {
			
			System.out.println("********************************** CONTEXT **********************************");
			System.out.println(context.getName()+" neighbours : "+context.overlaps.size());
			
			
			
			System.out.println("*************************** OVERLAPS ***************************");
			for(Context ctxt : context.contextOverlapsByPercept.keySet()) {
				System.out.print(ctxt.getName() + " --> ");
				for(Percept percept : getAllPercepts(usedAmoeba)) {
					System.out.print(percept.getName() + "(" + context.contextOverlapsByPercept.get(ctxt).get(percept) + ") ");
					
				}
				System.out.println("");
			}
			
			
			
			
			
			System.out.println("*************************** NEAREST NEIGHBOURS ***************************");
			for(Percept percept : getAllPercepts(usedAmoeba)) {
				System.out.print(percept.getName());
				if(context.nearestNeighbours.get(percept).get("start") != null) {
					System.out.println(" " + "Start : " + context.nearestNeighbours.get(percept).get("start").getName());
				}
				else {
					System.out.println(" " + "Start : null" );
				}
				
				System.out.print(percept.getName());
				if(context.nearestNeighbours.get(percept).get("end") != null) {
					System.out.println(" " + "End : " + context.nearestNeighbours.get(percept).get("end").getName());
				}
				else {
					System.out.println(" " + "End : null" );
				}
			}
			
			for(Context c: context.overlaps.keySet()){
				
				System.out.println(c.getName()+ " ---> " + context.overlaps.get(c));
				
				
			}
			
			System.out.println("************************** SORTED NEIGHBOURS **************************");
			
			for(Percept percept : getAllPercepts(usedAmoeba)) {
				System.out.println("**************************" + percept.getName() + "**************************");
				
				HashMap<String, ArrayList<Context>> neighbourscontextSortedContexts = context.getSortedPossibleNeigbours(percept);
				System.out.println("************************** START **************************");
				for(Context cntxt : neighbourscontextSortedContexts.get("start")) {
					System.out.println(cntxt.getName() + "--->" + cntxt.getRanges().get(percept).getStart());
				}
				System.out.println("************************** END **************************");
				for(Context cntxt : neighbourscontextSortedContexts.get("end")) {
					System.out.println(cntxt.getName() + "--->" + cntxt.getRanges().get(percept).getEnd());
				}
				
			}
			
			

		}
		
	}
}
