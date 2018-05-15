package experiments.droneControl;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import MAS.agents.Agent;
import fr.irit.smac.lxplot.LxPlot;
import fr.irit.smac.lxplot.commons.ChartType;

public class Main implements Runnable {

	private SocketServer server;
	
	/* GUI or not */
	public static final boolean viewer = false;
	private String request;
	private String message = "";
	private String previousMessage = "";
	private String previousHeadMessage = "";
	private double result;
	private int cylceCounter;
	private int errorCylceCounter;
	private HashMap<String,TABLIST> phaseData = new HashMap<String,TABLIST>();
	
	private Boolean shutDown;
	
	public AMOEBA_UI amoeba_x, amoeba_y, amoeba_z;

	public AMALOM amalom;
	
	public FILE[][] phaseFile;
	public FILE[][] dronePositionFile;
	public FILE[][] droneSpeedFile;
	public FILE[][] droneCommandsFile;
	
	public String folder = "19122017";
	
	public Main(ServerSocket ss, Socket s) {
		server = new SocketServer(ss, s);
		amoeba_x = new AMOEBA_UI(viewer, "position");
		amoeba_y = new AMOEBA_UI(viewer, "position");
		amoeba_z = new AMOEBA_UI(viewer, "position");
		
		
		phaseFile =  new FILE[3][3];
		dronePositionFile =  new FILE[3][3];
		droneSpeedFile =  new FILE[3][3];
		droneCommandsFile =  new FILE[3][3];
		
		for(int j=0; j<3;j++){
			for(int i=0; i<3;i++){
				phaseFile[i][j] = new FILE(folder, "phase_" + i + "_comportement_" + j);
				dronePositionFile[i][j] = new FILE(folder, "position_" + i + "_comportement_" + j);
				droneSpeedFile[i][j] = new FILE(folder, "vitesse_" + i + "_comportement_" + j);
				droneCommandsFile[i][j] = new FILE(folder, "commandes_" + i + "_comportement_" + j);
			}
		}
		
		//LxPlot.getChart("Phase "+i).add(behavior, dronePosition[i],  droneVelocity[i]);
		//LxPlot.getChart("Drone position "+ i).add(behavior, errorCylceCounter,  dronePosition[i]);
		
		
		amalom = new AMALOM(3, 3, 0.05d, 500, 0.05d, 0.3, ModelConstruction.HIGHEST_SENSIBILITY); 
		
//		LxPlot.getChart("PID - Drone Stats", ChartType.LINE);
//		LxPlot.getChart("PID - Drone Velocities", ChartType.LINE);
//		LxPlot.getChart("PID - Drone Commands", ChartType.LINE);
		
//		LxPlot.getChart("AMOEBA - Drone Stats", ChartType.LINE);
//		LxPlot.getChart("AMOEBA - Drone Velocities", ChartType.LINE);
//		LxPlot.getChart("AMOEBA - Drone Commands", ChartType.LINE);
		
//		LxPlot.getChart("PID Phase", ChartType.PLOT);
//		LxPlot.getChart("Phase", ChartType.PLOT);
		
		
		
		
		for(int i=0; i<3; i++){
			phaseData.put("PID - P"+i,new TABLIST());
			phaseData.put("AMOEBA - P"+i,new TABLIST());
			phaseData.put("AMALOM - P"+i,new TABLIST());
		}
		
		
//		for(int i=0; i<3; i++) {
//			LxPlot.getChart("Phase "+i, ChartType.PLOT);
//		}
		
		shutDown = false;
		
		cylceCounter = 0;
		errorCylceCounter = 0;
	}
	
	public void run() {

		amoeba_x.init();
		amoeba_y.init();
		amoeba_z.init();		 

		while (shutDown != true) {
			

			
			message = server.readMessage();
			System.out.println(message);
			//System.out.println(previousMessage);
			
			String delimsTag = "[~]+";
			String[] tokens = message.split(delimsTag);
			message = tokens[1];
			if (tokens[0].contentEquals("QUIT")){				
				quit();
			}
			else if (tokens[0].contentEquals("LRN") && !previousMessage.contentEquals("")){				
				learn();
				previousHeadMessage = "LRN";
			}
			else if(tokens[0].contentEquals("CTRL")){
				amoebaControl();
				previousHeadMessage = "CTRL";
			}
			else if(tokens[0].contentEquals("ENDCTRL")){
				amoebaAndAmalomControl();
				previousHeadMessage = "ENDCTRL";
			}
			else if(tokens[0].contentEquals("ATRCK")){
				controlAttraction();
				previousHeadMessage = "ATRCK";
			}
			else {
				ack();
				if(cylceCounter>2){
					amalom.diplayVariations(getOutputAmalom(message, "variation"));
				}
				previousHeadMessage = "PCT";
			}
			
			previousMessage = message;
			errorCylceCounter++;
			cylceCounter++;
		}

		
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private void quit(){
		
		server.close();
		for(int j=0; j<3;j++){
			for(int i=0; i<3;i++){
				phaseFile[i][j].close();
				dronePositionFile[i][j].close();
				droneSpeedFile[i][j].close();
				droneCommandsFile[i][j].close();
			}
		}
		
		
		amalom.stopWriting();
		
		shutDown = true;
	}
	
	private void ack(){
		//System.out.println("ACK...");
		//message = readMessage();
		//System.out.println(message);
		if(!message.contentEquals("")){
				server.sendMessage("ACK_" + Integer.toString(server.getMessageCounter())); 
				//System.out.println("ACK");
		}
		else{
			server.sendMessage("ERR_"+Integer.toString(server.getMessageCounter()));
		}
	}
	
	private void learn(){
		//System.out.println("LEARNING...");
		//message = readMessage();
		if(!message.contentEquals("")){
			amoeba_x.learn(new HashMap<String, Double>(getOutput(message,"position",0)));
			amoeba_z.learn(new HashMap<String, Double>(getOutput(message,"position",1)));
			amoeba_y.learn(new HashMap<String, Double>(getOutput(message,"position",2)));
			
			
			//sendMessage("LRN_" + Integer.toString(counter));
			
			server.sendMessage("VZ_"+ amoeba_x.getBestContextMessage() + "_" + amoeba_z.getBestContextMessage());
			
			
			//displayPlots(message, "PID");
			amalom.learn(getOutputAmalom(message, "variation"), getOutputAmalom(message, "command"));
			
			//displayPlots(message, "PID", validatePrevisousState("PCT"));
			
		}
		else{
			server.sendMessage("ERR_"+Integer.toString(server.getMessageCounter()));
		}
	}
	
	private boolean validatePrevisousState(String previousState){
		if(previousHeadMessage.equals(previousState)){
			return true;
		}
		else return false;
	}
	
	private void amoebaControl(){
		

		
		//System.out.println("CONTROLING...");
		//message = readMessage();
		if(!message.contentEquals("")){
			
			
			amalom.learn(getOutputAmalom(message, "variation"), getOutputAmalom(message, "command"));
			
			double[] Camoeba = new double[3];
						
			Camoeba[0] = amoeba_x.request(new HashMap<String, Double>(getOutput(message,"position",0))) ;
			Camoeba[1] = amoeba_z.request(new HashMap<String, Double>(getOutput(message,"position",1))) ;
			Camoeba[2] = amoeba_y.request(new HashMap<String, Double>(getOutput(message,"position",2))) ;
			
			
			
			
			double C0 = Camoeba[0];
			double C1 = Camoeba[1];
			double C2 = Camoeba[2];
			
			AMALOM.displayTab(Camoeba, "AMOEBA COMMAND");

			
			request = "CTRL_" + C0 +" "+ C1 +" "+ C2;
			request = request + "_" + "100"+amoeba_x.getBestContextId() + "_" + "200"+amoeba_z.getBestContextId();
			request = request + "_" + amoeba_x.getBestContextMessage() + "_" + amoeba_z.getBestContextMessage();
			
			server.sendMessage(request);
			
			//displayPlots(message, "AMOEBA");
			//amalom.learnSensibility(getOutputAmalom(message, "variation"), getOutputAmalom(message, "command"));
			//displayPlots(message, "AMOEBA", validatePrevisousState("PCT"));
		}
		else {
			server.sendMessage("ERR_"+Integer.toString(server.getMessageCounter()));
		}
		
	}
	
private void amoebaAndAmalomControl(){
		
		double weight = 1.0d;
		
		//System.out.println("CONTROLING...");
		//message = readMessage();
		if(!message.contentEquals("")){
			
			amalom.learn(getOutputAmalom(message, "variation"), getOutputAmalom(message, "command"));
			

			
			double[] CXamalom = amalom.request(getOutputAmalom(message, "position"), amoeba_x.getBestContextPerceptCenter());
			double[] CYamalom = amalom.request(getOutputAmalom(message, "position"), amoeba_y.getBestContextPerceptCenter());
			double[] CZamalom = amalom.request(getOutputAmalom(message, "position"), amoeba_z.getBestContextPerceptCenter());
			
			double[] CamalomMean = new double[3];
			
			CamalomMean[0] = (CXamalom[0] + CYamalom[0] + CZamalom[0])/3;
			CamalomMean[1] = (CXamalom[1] + CYamalom[1] + CZamalom[1])/3;
			CamalomMean[2] = (CXamalom[2] + CYamalom[2] + CZamalom[2])/3;	
			
			double[] Camoeba = new double[3];
						
			Camoeba[0] = amoeba_x.request(new HashMap<String, Double>(getOutput(message,"position",0))) ;
			Camoeba[1] = amoeba_z.request(new HashMap<String, Double>(getOutput(message,"position",1))) ;
			Camoeba[2] = amoeba_y.request(new HashMap<String, Double>(getOutput(message,"position",2))) ;
			
			
			// Single
			//double[] CEndo = VECTOR.difference(CXamalom, VECTOR.projection(CXamalom, Camoeba));
			// Mean
			double[] CEndo = VECTOR.difference(CamalomMean, VECTOR.projection(CamalomMean, Camoeba));
			
			double[] commandSum = VECTOR.sum(VECTOR.product(weight, CEndo), Camoeba);
			
			double C0 = commandSum[0];
			double C1 = commandSum[1];
			double C2 = commandSum[2];
			
			AMALOM.displayTab(CXamalom, "AMALOM X CTXT COMMAND");
			AMALOM.displayTab(Camoeba, "AMOEBA COMMAND");
			AMALOM.displayTab(CEndo, "ENDO X CTXT COMMAND");
			AMALOM.displayTab(commandSum, "SUM COMMAND");
			
			request = "CTRL_" + C0 +" "+ C1 +" "+ C2;
			request = request + "_" + "100"+amoeba_x.getBestContextId() + "_" + "200"+amoeba_z.getBestContextId();
			request = request + "_" + amoeba_x.getBestContextMessage() + "_" + amoeba_z.getBestContextMessage();
			
			server.sendMessage(request);
			
			//displayPlots(message, "AMOEBA");
			//amalom.learnSensibility(getOutputAmalom(message, "variation"), getOutputAmalom(message, "command"));
			//displayPlots(message, "AMALOM", validatePrevisousState("PCT"));
		}
		else {
			server.sendMessage("ERR_"+Integer.toString(server.getMessageCounter()));
		}
		
	}
	
	private void controlAttraction(){
		
		//System.out.println("CONTROLING...");
		//message = readMessage();
		
		if(!message.contentEquals("")){
			//request = amoebaVX.request(new HashMap<String, Double>(getOutputGoal(message, 0))) +" "+amoebaVZ.request(new HashMap<String, Double>(getOutputGoal(message, 1)));
			//request = controlModelVc.request(new HashMap<String, Double>(getOutputGoalEF(message, "Vc"))) +" "+controlModelHc.request(new HashMap<String, Double>(getOutputGoalEF(message, "Hc"))) +" "+controlModelUc.request(new HashMap<String, Double>(getOutputGoalEF(message, "Uc")));
			
			//request = request + "_" + "100"+amoeba_x.getBestContextId() + "_" + "200"+amoeba_z.getBestContextId();
			double[] commands = amalom.request(getOutputAmalom(message, "position"), getOutputAmalom(message, "goal"));
			request = commands[0] + " " + commands[1] + " " + commands[2] ; 
			System.out.println("COMMAND\t" + request);
			server.sendMessage("ATRCK_" + request);
		}
		else {
			server.sendMessage("ERR_"+Integer.toString(server.getMessageCounter()));
		}
		
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

	
	public HashMap<String, Double> getOutputAmalom(String message, String arg) {
		HashMap<String, Double> out = new HashMap<String, Double>();

		String position, rotation, velocity, angularVelocity, commands, time;
		
		double[] dronePosition = new double[3];
		double[] droneRotation = new double[4];
		double[] droneVelocity = new double[3];
		double[] droneAngularVelocity = new double[3];
		double[] droneCommands = new double[3];
		double timeStamp;
		
		

		

		String delimsTags = "[_]+";
		String[] tokens = message.split(delimsTags);
		
		position = tokens[0];
		rotation = tokens[1];
		velocity = tokens[2];
		angularVelocity = tokens[3];
		commands = tokens[4];
		time = tokens[5];
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
			droneCommands[i] = Double.parseDouble(tokensCommands[i]);
		}
		for (int i = 0; i <= 3; i++) {
			droneRotation[i] = Double.parseDouble(tokensRotation[i]);
		}
		
		timeStamp = Double.parseDouble(time);
		
		String previousPosition, previousRotation, previousVelocity, previousAngularVelocity, previousCommands, previousTime;
		
		double[] previousDronePosition = new double[3];
		double[] previousDroneRotation = new double[4];
		double[] previousDroneVelocity = new double[3];
		double[] previousDroneAngularVelocity = new double[3];
		double[] previousDroneCommands = new double[3];
		double previousTimeStamp = 0.0d;
		
		if(previousMessage!=null){
			String[] previousTokens = previousMessage.split(delimsTags);
			
			previousPosition = previousTokens[0];
			previousRotation = previousTokens[1];
			previousVelocity = previousTokens[2];
			previousAngularVelocity = previousTokens[3];
			previousCommands = previousTokens[4];
			previousTime = previousTokens[5];

			String[] previousTokensPosition = previousPosition.split(delimsValues);
			String[] previousTokensRotation = previousRotation.split(delimsValues);
			String[] previousTokensVelocity = previousVelocity.split(delimsValues);
			String[] previousTokensAngularVelocity = previousAngularVelocity.split(delimsValues);
			String[] previousTokensCommands = previousCommands.split(delimsValues);
			for (int i = 0; i <= 2; i++) {
				previousDronePosition[i] = Double.parseDouble(previousTokensPosition[i]);
				previousDroneVelocity[i] = Double.parseDouble(previousTokensVelocity[i]);
				previousDroneAngularVelocity[i] = Double.parseDouble(previousTokensAngularVelocity[i]);
				previousDroneCommands[i] = Double.parseDouble(previousTokensCommands[i]);
			}
			for (int i = 0; i <= 3; i++) {
				previousDroneRotation[i] = Double.parseDouble(previousTokensRotation[i]);
			}
			previousTimeStamp = Double.parseDouble(previousTime);
		}


		if (arg.contentEquals("speed")){
			for(int i =0; i<droneVelocity.length;i++){
				out.put("P"+i, droneVelocity[i]);
			}
		} else if (arg.contentEquals("variation") && (previousMessage!=null)){
			for(int i =0; i<droneCommands.length;i++){
				out.put("P"+i, (dronePosition[i] - previousDronePosition[i]));
			}
			out.put("DT", timeStamp - previousTimeStamp);
		}else if (arg.contentEquals("variation")){
			for(int i =0; i<droneCommands.length;i++){
				out.put("P"+i, dronePosition[i] );
			}
		}else if (arg.contentEquals("command")){
			for(int i =0; i<droneCommands.length;i++){
				out.put("C"+i, previousDroneCommands[i]);
			}
		}else if (arg.contentEquals("position")){
			for(int i =0; i<dronePosition.length;i++){
				out.put("P"+i, dronePosition[i]);
			}
		}else if (arg.contentEquals("goal")){
			String goal = tokens[6];
			System.out.println(goal);
			String[] tokensgoal = goal.split(delimsValues);
			System.out.println(tokensgoal.length);
			double[] goals = new double[3];
			for (int i = 0; i < goals.length; i++) {
				goals[i] = Double.parseDouble(tokensgoal[i]);
			}
			
			for(int i =0; i<droneCommands.length;i++){
				out.put("P"+i, goals[i]);
			}
		} else {
			out = null;
		}
	
	
	return out;
}
	
	private void displayPlots(String message, String behavior, boolean resetCycle){
		String position, rotation, velocity, angularVelocity, commands;
		double[] dronePosition = new double[3];
		double[] droneRotation = new double[4];
		double[] droneVelocity = new double[3];
		double[] droneAngularVelocity = new double[3];
		double[] droneCommands = new double[3];

		

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
			droneCommands[i] = Double.parseDouble(tokensCommands[i]);
		}
		for (int i = 0; i <= 3; i++) {
			droneRotation[i] = Double.parseDouble(tokensRotation[i]);
		}
		
//		for(int i=0; i<dronePosition.length; i++) {
//			LxPlot.getChart(behavior +" - Drone Stats").add("P"+i, cylceCounter,  dronePosition[i]);
//		}
//		
//		for(int i=0; i<droneVelocity.length; i++) {
//			LxPlot.getChart(behavior +" - Drone Stats").add("V"+i, cylceCounter,  droneVelocity[i]);
//		}
//		
//		for(int i=0; i<droneCommands.length; i++) {
//			LxPlot.getChart(behavior +" - Drone Stats").add("C"+i, cylceCounter,  droneCommands[i]);
//		}
//		
//		for(int i=0; i<dronePosition.length; i++) {
//			LxPlot.getChart(behavior +" Phase").add("P"+i, dronePosition[i],  droneVelocity[i]);
//		}
		if(resetCycle) errorCylceCounter = 0;
		
		Double[] phaseValue = new Double[2];
		double[] amoebaMeanPositionError, amoebaMeanSpeedError, amalomMeanPositionError, amalomMeanSpeedError;
		double[] amoebaVarPositionError, amoebaVarSpeedError, amalomVarPositionError, amalomVarSpeedError;
		
		amoebaMeanPositionError = new double[3];
		amoebaMeanSpeedError = new double[3];
		amalomMeanPositionError = new double[3];
		amalomMeanSpeedError = new double[3];
		amoebaVarPositionError = new double[3];
		amoebaVarSpeedError = new double[3];
		amalomVarPositionError = new double[3];
		amalomVarSpeedError = new double[3];
		
		
		
		for(int i=0; i<dronePosition.length; i++) {
			
			
			if(behavior.contentEquals("PID")){
				phaseFile[i][0].write(dronePosition[i], droneVelocity[i]);
				dronePositionFile[i][0].write((double)errorCylceCounter, dronePosition[i]);
				droneSpeedFile[i][0].write((double)errorCylceCounter, droneVelocity[i]);
				droneCommandsFile[i][0].write((double)errorCylceCounter, droneCommands[i]);
			}else if(behavior.contentEquals("AMOEBA")){
				phaseFile[i][1].write(dronePosition[i], droneVelocity[i]);
				dronePositionFile[i][1].write((double)errorCylceCounter, dronePosition[i]);
				droneSpeedFile[i][1].write((double)errorCylceCounter, droneVelocity[i]);
				droneCommandsFile[i][1].write((double)errorCylceCounter, droneCommands[i]);
			}else if(behavior.contentEquals("AMALOM")){
				phaseFile[i][2].write(dronePosition[i], droneVelocity[i]);
				dronePositionFile[i][2].write((double)errorCylceCounter, dronePosition[i]);
				droneSpeedFile[i][2].write((double)errorCylceCounter, droneVelocity[i]);
				droneCommandsFile[i][2].write((double)errorCylceCounter, droneCommands[i]);
			}
			
			
			
			//LxPlot.getChart("Phase "+i).add(behavior, dronePosition[i],  droneVelocity[i]);
			//LxPlot.getChart("Drone position "+ i).add(behavior, errorCylceCounter,  dronePosition[i]);
			//LxPlot.getChart("Drone velocity "+ i).add(behavior, errorCylceCounter,  droneVelocity[i]);
			//LxPlot.getChart("Drone commands "+ i).add(behavior, errorCylceCounter,  droneCommands[i]);
			
			phaseValue[0]=dronePosition[i];
			phaseValue[1]=droneVelocity[i];
			
			phaseData.get(behavior+" - P"+i).add(phaseValue);
			

			
			AMALOM.displayTab(phaseData.get("PID - P"+i).getMean(), "MEAN PID - P"+i);
			AMALOM.displayTab(phaseData.get("AMOEBA - P"+i).getMean(), "MEAN AMOEBA - P"+i);
			AMALOM.displayTab(phaseData.get("AMALOM - P"+i).getMean(), "MEAN AMALOM - P"+i);
			
			AMALOM.displayTab(phaseData.get("PID - P"+i).getVariance(), "VAR PID - P"+i);
			AMALOM.displayTab(phaseData.get("AMOEBA - P"+i).getVariance(), "VAR AMOEBA - P"+i);
			AMALOM.displayTab(phaseData.get("AMALOM - P"+i).getVariance(), "VAR AMALOM - P"+i);
			
			if((phaseData.get("AMALOM - P"+i).size() > 0) && (phaseData.get("AMOEBA - P"+i).size() > 0)){
				amoebaMeanPositionError[i] = phaseData.get("PID - P"+i).getMean()[0] - phaseData.get("AMOEBA - P"+i).getMean()[0];
				amoebaMeanSpeedError[i] = phaseData.get("PID - P"+i).getMean()[1] - phaseData.get("AMOEBA - P"+i).getMean()[1];
				amalomMeanPositionError[i] = phaseData.get("PID - P"+i).getMean()[0] - phaseData.get("AMALOM - P"+i).getMean()[0];
				amalomMeanSpeedError[i] = phaseData.get("PID - P"+i).getMean()[1] - phaseData.get("AMALOM - P"+i).getMean()[1];
				
				amoebaVarPositionError[i] = phaseData.get("PID - P"+i).getVariance()[0] - phaseData.get("AMOEBA - P"+i).getVariance()[0];
				amoebaVarSpeedError[i] = phaseData.get("PID - P"+i).getVariance()[1] - phaseData.get("AMOEBA - P"+i).getVariance()[1];
				amalomVarPositionError[i] = phaseData.get("PID - P"+i).getVariance()[0] - phaseData.get("AMALOM - P"+i).getVariance()[0];
				amalomVarSpeedError[i] = phaseData.get("PID - P"+i).getVariance()[1] - phaseData.get("AMALOM - P"+i).getVariance()[1];
			}
			
			
		}
		
		if(phaseData.get("AMALOM - P0").size() > 0){

			System.out.println("AMOEBA MEAN POSITION ERROR\t "+ VECTOR.norm(amoebaMeanPositionError));
			System.out.println("AMALOM MEAN POSITION ERROR\t "+ VECTOR.norm(amalomMeanPositionError));
			System.out.println("AMOEBA MEAN SPEED ERROR\t "+ VECTOR.norm(amoebaMeanSpeedError));
			System.out.println("AMALOM MEAN SPEED ERROR\t "+ VECTOR.norm(amalomMeanSpeedError));
			
			System.out.println("AMOEBA VAR POSITION ERROR\t "+ VECTOR.norm(amoebaVarPositionError));
			System.out.println("AMALOM VAR POSITION ERROR\t "+ VECTOR.norm(amalomVarPositionError));
			System.out.println("AMOEBA VAR SPEED ERROR\t "+ VECTOR.norm(amoebaVarSpeedError));
			System.out.println("AMALOM VAR SPEED ERROR\t "+ VECTOR.norm(amalomVarSpeedError));
		}
		

	}
}

