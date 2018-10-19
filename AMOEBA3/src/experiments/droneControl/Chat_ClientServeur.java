package experiments.droneControl;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;



import mas.agents.Agent;
import mas.agents.percept.Percept;
import mas.agents.context.Range;
import mas.agents.localModel.TypeLocalModel;
import mas.init.amoeba.AMOEBAFactory;
import mas.kernel.AMOEBA;

public class Chat_ClientServeur implements Runnable {


	private Socket socket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private ArrayList<Agent> agentsContextes;

	private double result;
	


	/* GUI or not */
	public static final boolean viewer = false;
	public static final boolean verboseriticity = true;
	public static final boolean linksOn = false;

	private String request;
	
	private int counter = 0;
	private String message = "";

	public AMOEBA amoebaX = AMOEBAFactory.createAMOEBA(viewer,"/experiments/droneControl/DroneControl_solver.xml");
	public AMOEBA amoebaY = AMOEBAFactory.createAMOEBA(viewer,"/experiments/droneControl/DroneControl_solver.xml");
	public AMOEBA amoebaZ = AMOEBAFactory.createAMOEBA(viewer,"/experiments/droneControl/DroneControl_solver.xml");
	
	public AMOEBA amoebaVX = AMOEBAFactory.createAMOEBA(viewer,"/experiments/droneControl/DroneControlVariations_solver.xml");
	public AMOEBA amoebaVZ = AMOEBAFactory.createAMOEBA(viewer,"/experiments/droneControl/DroneControlVariations_solver.xml");
	

	public ControlModel controlModelVc = new ControlModel("Vc",0.5f);
	public ControlModel controlModelHc = new ControlModel("Hc",0.5f);
	public ControlModel controlModelUc = new ControlModel("Uc",0.5f);
	
	
	public Chat_ClientServeur(ServerSocket ss, Socket s) {
		socket = s;
		
	}
	

	public void run() {



		amoebaX.getScheduler().getWorld().setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoebaX.getScheduler().getHeadAgent().setDataForErrorMargin(0.1, 1, 1, 0.1, 10, 100);
		amoebaX.getScheduler().getHeadAgent().setDataForInexactMargin(0.1, 1, 1, 0.1, 10, 100);
		amoebaX.setAVT_acceleration(1.1f);
		amoebaX.setAVT_deceleration(0.1);
		amoebaX.setAVT_percentAtStart(0.1f);
		
		amoebaY.getScheduler().getWorld().setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoebaY.getScheduler().getHeadAgent().setDataForErrorMargin(0.1, 1, 1, 0.1, 10, 100);
		amoebaY.getScheduler().getHeadAgent().setDataForInexactMargin(0.1, 1, 1, 0.1, 10, 100);
		amoebaY.setAVT_acceleration(1.1f);
		amoebaY.setAVT_deceleration(0.1);
		amoebaY.setAVT_percentAtStart(0.1f);
		
		amoebaZ.getScheduler().getWorld().setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoebaZ.getScheduler().getHeadAgent().setDataForErrorMargin(0.1, 1, 1, 0.1, 10, 100);
		amoebaZ.getScheduler().getHeadAgent().setDataForInexactMargin(0.1, 1, 1, 0.1, 10, 100);
		amoebaZ.setAVT_acceleration(1.1f);
		amoebaZ.setAVT_deceleration(0.1);
		amoebaZ.setAVT_percentAtStart(0.1f);
		
		amoebaVX.getScheduler().getWorld().setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoebaVX.getScheduler().getHeadAgent().setDataForErrorMargin(0.1, 1, 1, 0.1, 10, 100);
		amoebaVX.getScheduler().getHeadAgent().setDataForInexactMargin(0.05, 1, 1, 0.05, 10, 100);
		amoebaVX.setAVT_acceleration(1.1f);
		amoebaVX.setAVT_deceleration(0.1);
		amoebaVX.setAVT_percentAtStart(0.1f);
		
		amoebaVZ.getScheduler().getWorld().setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoebaVZ.getScheduler().getHeadAgent().setDataForErrorMargin(0.1, 1, 1, 0.1, 10, 100);
		amoebaVZ.getScheduler().getHeadAgent().setDataForInexactMargin(0.05, 1, 1, 0.05, 10, 100);
		amoebaVZ.setAVT_acceleration(1.1f);
		amoebaVZ.setAVT_deceleration(0.1);
		amoebaVZ.setAVT_percentAtStart(0.1f);
		
		
		 

		while (true) {
			
//			controlModelVc.displayModel();
//			controlModelHc.displayModel();
//			controlModelUc.displayModel();
			
			message = readMessage();
			System.out.println(message);
			
			String delimsTag = "[~]+";
			String[] tokens = message.split(delimsTag);
			message = tokens[1];
			if (tokens[0].contentEquals("LRN")){				
				learn();
			}
			else if(tokens[0].contentEquals("CTRL")){
				control();
			}
			else if(tokens[0].contentEquals("ATRCK")){
				controlAttraction();
			}
			else ack();
			
			agentsContextes = amoebaX.getScheduler().getContexts();
		}

	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////

	private void contextAgentsInterface(){
		agentsContextes = amoebaX.getScheduler().getContexts();
		//System.out.println("INTERFACE...");
		message = readMessage();
		//System.out.println(message);
		sendMessage("CTXT_" + agentsContextes.size());
	}
	
	private void ack(){
		//System.out.println("ACK...");
		//message = readMessage();
		//System.out.println(message);
		if(!message.contentEquals("")){
				sendMessage("ACK_" + Integer.toString(counter)); 
				//System.out.println("ACK");
		}
		else{
			sendMessage("ERR_"+Integer.toString(counter));
		}
	}
	
	private void learn(){
		//System.out.println("LEARNING...");
		//message = readMessage();
		if(!message.contentEquals("")){
			amoebaX.learn(new HashMap<String, Double>(getOutput(message,"position",0)));
			amoebaZ.learn(new HashMap<String, Double>(getOutput(message,"position",1)));
			amoebaY.learn(new HashMap<String, Double>(getOutput(message,"position",2)));
			
			amoebaVX.learn(new HashMap<String, Double>(getOutput(message,"velocity",0)));
			amoebaVZ.learn(new HashMap<String, Double>(getOutput(message,"velocity",1)));
			
			controlModelVc.learn(new HashMap<String, Double>(getOutputEndogenous(message,"Vc")));
			controlModelHc.learn(new HashMap<String, Double>(getOutputEndogenous(message,"Hc")));
			controlModelUc.learn(new HashMap<String, Double>(getOutputEndogenous(message,"Uc")));
			//sendMessage("LRN_" + Integer.toString(counter));
			
			ArrayList<Percept> percepts1= amoebaX.getScheduler().getWorld().getAllPercept();
			HashMap<Percept, Range> contextRanges1 = new HashMap<Percept, Range>();
			contextRanges1 = amoebaX.getScheduler().getHeadAgent().getBestContext().getRanges();
			
			ArrayList<Percept> percepts2= amoebaZ.getScheduler().getWorld().getAllPercept();
			HashMap<Percept, Range> contextRanges2 = new HashMap<Percept, Range>();
			contextRanges2 = amoebaZ.getScheduler().getHeadAgent().getBestContext().getRanges();

			
			
			//System.out.println(contextRanges1.get(percepts1.get(0)).getContext());
			//System.out.println(contextRanges1);
			
			//System.out.println(contextRanges2.get(percepts2.get(0)).getContext());
			//System.out.println(contextRanges2);
			
			createValidityZones((float)(contextRanges1.get(percepts1.get(0)).getStart() + contextRanges1.get(percepts1.get(0)).getEnd())/2, 
					(float)(contextRanges1.get(percepts1.get(1)).getStart() + contextRanges1.get(percepts1.get(1)).getEnd())/2, 
					(float)(contextRanges1.get(percepts1.get(2)).getStart() + contextRanges1.get(percepts1.get(2)).getEnd())/2, 
					(float)(contextRanges1.get(percepts1.get(0)).getLenght()), 
					(float)(contextRanges1.get(percepts1.get(1)).getLenght()),	
					(float)(contextRanges1.get(percepts1.get(2)).getLenght()),
					(float)(amoebaX.getScheduler().getHeadAgent().getBestContext().getActionProposal()),
					Integer.toString(amoebaX.getScheduler().getHeadAgent().getBestContext().getID()),
					
					(float)(contextRanges2.get(percepts2.get(0)).getStart() + contextRanges2.get(percepts2.get(0)).getEnd())/2, 
					10, 
					(float)(contextRanges2.get(percepts2.get(2)).getStart() + contextRanges2.get(percepts2.get(2)).getEnd())/2, 
					(float)(contextRanges2.get(percepts2.get(0)).getLenght()), 
					2,	
					(float)(contextRanges2.get(percepts2.get(2)).getLenght()),
					(float)(amoebaZ.getScheduler().getHeadAgent().getBestContext().getActionProposal()),
					Integer.toString(amoebaZ.getScheduler().getHeadAgent().getBestContext().getID()));
		}
		else{
			sendMessage("ERR_"+Integer.toString(counter));
		}
	}
	
	private void control(){
		
		//System.out.println("CONTROLING...");
		//message = readMessage();
		if(!message.contentEquals("")){
			
			controlModelVc.learn(new HashMap<String, Double>(getOutputEndogenous(message,"Vc")));
			controlModelHc.learn(new HashMap<String, Double>(getOutputEndogenous(message,"Hc")));
			controlModelUc.learn(new HashMap<String, Double>(getOutputEndogenous(message,"Uc")));
			
			ArrayList<Percept> percepts1= amoebaX.getScheduler().getWorld().getAllPercept();
			HashMap<Percept, Range> contextRanges1 = new HashMap<Percept, Range>();
			contextRanges1 = amoebaX.getScheduler().getHeadAgent().getBestContext().getRanges();
			
			ArrayList<Percept> percepts2= amoebaZ.getScheduler().getWorld().getAllPercept();
			HashMap<Percept, Range> contextRanges2 = new HashMap<Percept, Range>();
			contextRanges2 = amoebaZ.getScheduler().getHeadAgent().getBestContext().getRanges();
			
			
			/*request = "CTRL";
			
			if(amoebaX.getScheduler().getHeadAgent().getNoBestContext()){
				System.out.println("NO BEST 1...");
				request = request + "_" + amoebaVX.request(new HashMap<String, Double>(getOutputGoal(message, 0, (float)(contextRanges1.get(percepts1.get(0)).getStart() + contextRanges1.get(percepts1.get(0)).getEnd())/2, (float)(contextRanges1.get(percepts1.get(1)).getStart() + contextRanges1.get(percepts1.get(1)).getEnd())/2)));
				//amoebaX.learn(new HashMap<String, Double>(getOutputEndogenous(message,0, amoebaVX.request(new HashMap<String, Double>(getOutputGoal(message, 0, (float)(contextRanges1.get(percepts1.get(0)).getStart() + contextRanges1.get(percepts1.get(0)).getEnd())/2, (float)(contextRanges1.get(percepts1.get(1)).getStart() + contextRanges1.get(percepts1.get(1)).getEnd())/2))))));
				//contextRanges1 = amoebaX.getScheduler().getHeadAgent().getBestContext().getRanges();
				
			} else {
				request = request + "_" + amoebaX.request(new HashMap<String, Double>(getOutput(message,"position",0)));
			}
			
			if(amoebaZ.getScheduler().getHeadAgent().getNoBestContext()){
				System.out.println("NO BEST 2...");
				request = request + " " + amoebaVZ.request(new HashMap<String, Double>(getOutputGoal(message, 0, (float)(contextRanges2.get(percepts2.get(0)).getStart() + contextRanges2.get(percepts2.get(0)).getEnd())/2, (float)(contextRanges2.get(percepts2.get(1)).getStart() + contextRanges2.get(percepts2.get(1)).getEnd())/2)));
				//amoebaZ.learn(new HashMap<String, Double>(getOutputEndogenous(message,0, amoebaVZ.request(new HashMap<String, Double>(getOutputGoal(message, 0, (float)(contextRanges2.get(percepts2.get(0)).getStart() + contextRanges2.get(percepts2.get(0)).getEnd())/2, (float)(contextRanges2.get(percepts2.get(1)).getStart() + contextRanges2.get(percepts2.get(1)).getEnd())/2))))));
				//contextRanges2 = amoebaZ.getScheduler().getHeadAgent().getBestContext().getRanges();
				
			} else {
				request = request + " " + amoebaZ.request(new HashMap<String, Double>(getOutput(message,"position",0)));
			}*/
			
			
			request = "CTRL_" + amoebaX.request(new HashMap<String, Double>(getOutput(message,"position",0))) +" "+amoebaZ.request(new HashMap<String, Double>(getOutput(message,"position",1))) +" "+amoebaY.request(new HashMap<String, Double>(getOutput(message,"position",2)));
			request = request + "_" + "100"+Integer.toString(amoebaX.getScheduler().getHeadAgent().getBestContext().getID()) + "_" + "200"+Integer.toString(amoebaZ.getScheduler().getHeadAgent().getBestContext().getID());
			
			

			
			
			//System.out.println(contextRanges1.get(percepts1.get(0)).getContext());
			//System.out.println(contextRanges1);
			
			//System.out.println(contextRanges2.get(percepts2.get(0)).getContext());
			//System.out.println(contextRanges2);
			
			createValidityZones(request, (float)(contextRanges1.get(percepts1.get(0)).getStart() + contextRanges1.get(percepts1.get(0)).getEnd())/2, 
					(float)(contextRanges1.get(percepts1.get(1)).getStart() + contextRanges1.get(percepts1.get(1)).getEnd())/2, 
					(float)(contextRanges1.get(percepts1.get(2)).getStart() + contextRanges1.get(percepts1.get(2)).getEnd())/2, 
					(float)(contextRanges1.get(percepts1.get(0)).getLenght()), 
					(float)(contextRanges1.get(percepts1.get(1)).getLenght()),	
					(float)(contextRanges1.get(percepts1.get(2)).getLenght()),
					(float)(amoebaX.getScheduler().getHeadAgent().getBestContext().getActionProposal()),
					Integer.toString(amoebaX.getScheduler().getHeadAgent().getBestContext().getID()),
					
					(float)(contextRanges2.get(percepts2.get(0)).getStart() + contextRanges2.get(percepts2.get(0)).getEnd())/2, 
					10, 
					(float)(contextRanges2.get(percepts2.get(2)).getStart() + contextRanges2.get(percepts2.get(2)).getEnd())/2, 
					(float)(contextRanges2.get(percepts2.get(0)).getLenght()), 
					2,	
					(float)(contextRanges2.get(percepts2.get(2)).getLenght()),
					(float)(amoebaZ.getScheduler().getHeadAgent().getBestContext().getActionProposal()),
					Integer.toString(amoebaZ.getScheduler().getHeadAgent().getBestContext().getID()));
			
			
			//sendMessage("CTRL_" + request);
		}
		else {
			sendMessage("ERR_"+Integer.toString(counter));
		}
		
	}
	
	private void controlAttraction(){
		
		//System.out.println("CONTROLING...");
		//message = readMessage();
		
		if(!message.contentEquals("")){
			//request = amoebaVX.request(new HashMap<String, Double>(getOutputGoal(message, 0))) +" "+amoebaVZ.request(new HashMap<String, Double>(getOutputGoal(message, 1)));
			request = controlModelVc.request(new HashMap<String, Double>(getOutputGoalEF(message, "Vc"))) +" "+controlModelHc.request(new HashMap<String, Double>(getOutputGoalEF(message, "Hc"))) +" "+controlModelUc.request(new HashMap<String, Double>(getOutputGoalEF(message, "Uc")));
			request = request + "_" + "100"+Integer.toString(amoebaX.getScheduler().getHeadAgent().getBestContext().getID()) + "_" + "200"+Integer.toString(amoebaZ.getScheduler().getHeadAgent().getBestContext().getID());
			sendMessage("ATRCK_" + request);
		}
		else {
			sendMessage("ERR_"+Integer.toString(counter));
		}
		
	}

	private void sendMessage(String message) {
		try {
			// socket = socketserver.accept(); // Un client se connecte on
			// l'accepte
			out = new PrintWriter(socket.getOutputStream());
			out.println(message);
			out.flush();
			// out.close();

		} catch (IOException e) {
			System.err.println("Déconnection ");
		}

	}

	private void createValidityZones(float positionX1, float positionY1, float positionZ1, float scaleX1, float scaleY1,
			float scaleZ1, float action1, String id1, float positionX2, float positionY2, float positionZ2, float scaleX2, float scaleY2,
			float scaleZ2, float action2, String id2) {
		String message = "VZ";
		message = message + "_" + Float.toString(positionX1) + " " + Float.toString(positionY1) + " "
				+ Float.toString(positionZ1);
		message = message + "_" + Float.toString(scaleX1) + " " + Float.toString(scaleY1) + " " + Float.toString(scaleZ1);
		message = message + "_" + Float.toString(action1);
		message = message + "_" + "100" + id1  ;
		
		message = message + "_" + Float.toString(positionX2) + " " + Float.toString(positionY2) + " "
				+ Float.toString(positionZ2);
		message = message + "_" + Float.toString(scaleX2) + " " + Float.toString(scaleY2) + " " + Float.toString(scaleZ2);
		message = message + "_" + Float.toString(action2);
		message = message + "_" + "200" + id2  ;
		sendMessage(message);
	}
	
	private void createValidityZones(String message, float positionX1, float positionY1, float positionZ1, float scaleX1, float scaleY1,
			float scaleZ1, float action1, String id1, float positionX2, float positionY2, float positionZ2, float scaleX2, float scaleY2,
			float scaleZ2, float action2, String id2) {
		message = message + "_" + Float.toString(positionX1) + " " + Float.toString(positionY1) + " "
				+ Float.toString(positionZ1);
		message = message + "_" + Float.toString(scaleX1) + " " + Float.toString(scaleY1) + " " + Float.toString(scaleZ1);
		message = message + "_" + Float.toString(action1);
		message = message + "_" + "100" + id1  ;
		
		message = message + "_" + Float.toString(positionX2) + " " + Float.toString(positionY2) + " "
				+ Float.toString(positionZ2);
		message = message + "_" + Float.toString(scaleX2) + " " + Float.toString(scaleY2) + " " + Float.toString(scaleZ2);
		message = message + "_" + Float.toString(action2);
		message = message + "_" + "200" + id2  ;
		sendMessage(message);
	}
	
	private String readMessage() {
		String message = "";
		try {
			// socket = socketserver.accept(); // Un client se connecte on
			// l'accepte
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			message = in.readLine();
			//System.out.println(message);
		} catch (IOException e) {
			System.err.println("Error getOutPut");
			e.printStackTrace();

		}
		
		counter ++;

		String delimsTags = "[ _~]+";
		String[] tokens = message.split(delimsTags);
		if ((tokens.length != 18) && (tokens.length != 21)) return "";
		
		return message;
	}

	public HashMap<String, Double> getOutput(String message, String arg, int chanel) {
			HashMap<String, Double> out = new HashMap<String, Double>();


			String position, rotation, velocity, angularVelocity, commands;
			double[] dronePosition = new double[3];
			double[] droneRotation = new double[4];
			double[] droneVelocity = new double[3];
			double[] droneAngularVelocity = new double[3];

			

			String delimsTags = "[_]+";
			String[] tokens = message.split(delimsTags);
			position = tokens[0];
			rotation = tokens[1];
			velocity = tokens[2];
			angularVelocity = tokens[3];
			commands = tokens[4];
			String delimsValues = "[ ]+";
			String[] tokensPosition = position.split(delimsValues);
			String[] tokensRotation = rotation.split(delimsValues);
			String[] tokensVelocity = velocity.split(delimsValues);
			String[] tokensAngularVelocity = angularVelocity.split(delimsValues);
			String[] tokensCommands = commands.split(delimsValues);
			for (int i = 0; i <= 2; i++) {
				dronePosition[i] = Double.parseDouble(tokensPosition[i]);
				droneVelocity[i] = Double.parseDouble(tokensVelocity[i]);
				droneAngularVelocity[i] = Double.parseDouble(tokensAngularVelocity[i]);
			}
			for (int i = 0; i <= 3; i++) {
				droneRotation[i] = Double.parseDouble(tokensRotation[i]);
			}

			result = Double.parseDouble(tokensCommands[chanel]);

			if (arg.contentEquals("position")){
				out.put("x", dronePosition[0]);
				out.put("y", dronePosition[1]);
				out.put("z", dronePosition[2]);
			} else if (arg.contentEquals("velocity")){
				out.put("x", droneVelocity[0]);
				out.put("y", droneVelocity[1]);
				out.put("z", droneVelocity[2]);
			}
			
			//out.put("xaxis", Double.parseDouble(tokensAxis[0]));
			//out.put("yaxis", Double.parseDouble(tokensAxis[1]));
			//out.put("sx", droneVelocity[0]);
			//out.put("sy", droneVelocity[1]);
			//out.put("sz", droneVelocity[2]);
			out.put("axis", result);
			// System.out.println( position + axis);
		
		return out;
	}
	
	public HashMap<String, Double> getOutputGoal(String message, int chanel) {
		HashMap<String, Double> out = new HashMap<String, Double>();


		String position, rotation, velocity, angularVelocity, commands, XYGoal;
		double[] dronePosition = new double[3];
		double[] droneRotation = new double[4];
		double[] droneVelocity = new double[3];
		double[] droneAngularVelocity = new double[3];

		

		String delimsTags = "[_]+";
		String[] tokens = message.split(delimsTags);
		position = tokens[0];
		rotation = tokens[1];
		velocity = tokens[2];
		angularVelocity = tokens[3];
		commands = tokens[4];
		XYGoal = tokens[5];
		
		String delimsValues = "[ ]+";
		String[] tokensPosition = position.split(delimsValues);
		String[] tokensRotation = rotation.split(delimsValues);
		String[] tokensVelocity = velocity.split(delimsValues);
		String[] tokensAngularVelocity = angularVelocity.split(delimsValues);
		String[] tokensCommands = commands.split(delimsValues);
		String[] tokensXYZGoal = XYGoal.split(delimsValues);
		for (int i = 0; i <= 2; i++) {
			dronePosition[i] = Double.parseDouble(tokensPosition[i]);
			droneVelocity[i] = Double.parseDouble(tokensVelocity[i]);
			droneAngularVelocity[i] = Double.parseDouble(tokensAngularVelocity[i]);
		}
		for (int i = 0; i <= 3; i++) {
			droneRotation[i] = Double.parseDouble(tokensRotation[i]);
		}

		result = Double.parseDouble(tokensCommands[chanel]);

		
		out.put("x", Double.parseDouble(tokensXYZGoal[0]) - dronePosition[0]);
		out.put("y", Double.parseDouble(tokensXYZGoal[1]) - dronePosition[1]);
		out.put("z", Double.parseDouble(tokensXYZGoal[2]) - dronePosition[2]);
		
		
		//out.put("xaxis", Double.parseDouble(tokensAxis[0]));
		//out.put("yaxis", Double.parseDouble(tokensAxis[1]));
		//out.put("sx", droneVelocity[0]);
		//out.put("sy", droneVelocity[1]);
		//out.put("sz", droneVelocity[2]);
		out.put("axis", result);
		// System.out.println( position + axis);
	
		return out;
	}
	
	public HashMap<String, Double> getOutputGoalEF(String message, String action) {
		HashMap<String, Double> out = new HashMap<String, Double>();


		String position, rotation, velocity, angularVelocity, commands, XYGoal;
		double[] dronePosition = new double[3];
		double[] droneRotation = new double[4];
		double[] droneVelocity = new double[3];
		double[] droneAngularVelocity = new double[3];

		

		String delimsTags = "[_]+";
		String[] tokens = message.split(delimsTags);
		position = tokens[0];
		rotation = tokens[1];
		velocity = tokens[2];
		angularVelocity = tokens[3];
		commands = tokens[4];
		XYGoal = tokens[5];
		
		String delimsValues = "[ ]+";
		String[] tokensPosition = position.split(delimsValues);
		String[] tokensRotation = rotation.split(delimsValues);
		String[] tokensVelocity = velocity.split(delimsValues);
		String[] tokensAngularVelocity = angularVelocity.split(delimsValues);
		String[] tokensCommands = commands.split(delimsValues);
		String[] tokensXYZGoal = XYGoal.split(delimsValues);
		for (int i = 0; i <= 2; i++) {
			dronePosition[i] = Double.parseDouble(tokensPosition[i]);
			droneVelocity[i] = Double.parseDouble(tokensVelocity[i]);
			droneAngularVelocity[i] = Double.parseDouble(tokensAngularVelocity[i]);
		}
		for (int i = 0; i <= 3; i++) {
			droneRotation[i] = Double.parseDouble(tokensRotation[i]);
		}
		
		out.put("Vx", Double.parseDouble(tokensXYZGoal[0]) - dronePosition[0]);
		out.put("Vy", Double.parseDouble(tokensXYZGoal[1]) - dronePosition[1]);
		out.put("Vz", Double.parseDouble(tokensXYZGoal[2]) - dronePosition[2]);
		
		
		if(action.contentEquals("Vc")){
			out.put("Vc", Double.parseDouble(tokensCommands[0]));
		}
		if(action.contentEquals("Hc")){
			out.put("Hc", Double.parseDouble(tokensCommands[1]));
		}
		if(action.contentEquals("Uc")){
			out.put("Uc", Double.parseDouble(tokensCommands[2]));
		}
	
		return out;
	}
	
	public HashMap<String, Double> getOutputEndogenous(String message, String action) {
		HashMap<String, Double> out = new HashMap<String, Double>();


		String position, rotation, velocity, angularVelocity, commands;
		double[] dronePosition = new double[3];
		double[] droneRotation = new double[4];
		double[] droneVelocity = new double[3];
		double[] droneAngularVelocity = new double[3];

		

		String delimsTags = "[_]+";
		String[] tokens = message.split(delimsTags);
		position = tokens[0];
		rotation = tokens[1];
		velocity = tokens[2];
		angularVelocity = tokens[3];
		commands = tokens[4];
		String delimsValues = "[ ]+";
		String[] tokensPosition = position.split(delimsValues);
		String[] tokensRotation = rotation.split(delimsValues);
		String[] tokensVelocity = velocity.split(delimsValues);
		String[] tokensAngularVelocity = angularVelocity.split(delimsValues);
		String[] tokensCommands = commands.split(delimsValues);
		for (int i = 0; i <= 2; i++) {
			dronePosition[i] = Double.parseDouble(tokensPosition[i]);
			droneVelocity[i] = Double.parseDouble(tokensVelocity[i]);
			droneAngularVelocity[i] = Double.parseDouble(tokensAngularVelocity[i]);
		}
		for (int i = 0; i <= 3; i++) {
			droneRotation[i] = Double.parseDouble(tokensRotation[i]);
		}


		
		out.put("Vx", droneVelocity[0]);
		out.put("Vy", droneVelocity[1]);
		out.put("Vz", droneVelocity[2]);
		
		if(action.contentEquals("Vc")){
			out.put("Vc", Double.parseDouble(tokensCommands[0]));
		}
		if(action.contentEquals("Uc")){
			out.put("Uc", Double.parseDouble(tokensCommands[2]));
		}
		if(action.contentEquals("Hc")){
			out.put("Hc", Double.parseDouble(tokensCommands[1]));
		}
		
	
	return out;
}

}
