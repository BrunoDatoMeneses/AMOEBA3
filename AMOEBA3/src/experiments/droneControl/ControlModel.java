package experiments.droneControl;

import java.awt.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

public class ControlModel {

	private double[][] data;
	private String action;
	private float threshold;
	private double[] sensitivity;
	private double[] normalizedSensitivity;
	private static double sensitivityTotal;
	private HashMap<String, Double> currentState;

	public ControlModel(String act, float thd) {

		data = new double[27][4];
		sensitivity = new double[3];
		normalizedSensitivity = new double[3];

		for(int i=0; i<sensitivity.length; i++) {
			sensitivity[i] = 0.0d;
			normalizedSensitivity[i] = 0.0d;
		}
		
		for(int i=0; i<data.length; i++) {
			for(int j=0; j<data[i].length; j++) {
				data[i][j] = 0.0d;
			}
		}
		
		sensitivityTotal = 0.0d;

		// Vx
		for(int i=0; i<27; i++) {
			if(i<9) data[i][0] = -1.0f;
			else if(i<18) data[i][0] = 0.0f;
			else data[i][0] = 1.0f;
		}

		// Vy
		for(int i=0; i<27; i++) {
			if((i<3) || ((i>=9)&&(i<12)) || ((i>=18)&&(i<21))) data[i][1] = -1.0f;
			else if((i<6) || ((i>=12)&&(i<15)) || ((i>=21)&&(i<24))) data[i][1] = 0.0f;
			else data[i][1] = 1.0f;
		}

		// Vz
		for(int i=0; i<27; i++) {

			if(i%3 == 0) data[i][2] = -1.0f;
			if(i%3 == 1) data[i][2] = 0.0f;
			if(i%3 == 2) data[i][2] = 1.0f;

		}

		action = act;
		threshold = thd;

	}

	public void learn(HashMap<String, Double> endogenousFeedback){
		double Vx_un, Vy_un, Vz_un;
		int index;
		
		currentState = new HashMap<String, Double>(endogenousFeedback);
		updateSensitivity();

		/*if ((Math.abs(endogenousFeedback.get("Vx"))>0.1) && (Math.abs(endogenousFeedback.get("Vz"))>0.1) && (Math.abs(endogenousFeedback.get(action))>0.1) ){
			Vx = endogenousFeedback.get("Vx");
			Vz = endogenousFeedback.get("Vz");

			Vx_un = Vx/Math.abs(Vx);
			Vz_un = Vz/Math.abs(Vz);

			index = findDirectionCase(Vx_un, Vz_un);

			data[index][2] = (endogenousFeedback.get(action) + data[index][2]) / 2;
		}
		else*/ 

		Vx_un = 0.0d;
		Vy_un = 0.0d;
		Vz_un = 0.0d;


		// Getting a Set of Key-value pairs
		Set entrySet = endogenousFeedback.entrySet();

		// Obtaining an iterator for the entry set
		Iterator it = entrySet.iterator();

		// Iterate through HashMap entries(Key-Value pairs)
		System.out.println("HashMap Key-Value Pairs : ");
		while(it.hasNext()){
			Map.Entry me = (Map.Entry)it.next();
			System.out.println(me.getKey() + " & " +me.getValue());
		}


		if ((Math.abs(endogenousFeedback.get("Vx"))>threshold) && (Math.abs(endogenousFeedback.get("Vy"))<(threshold/5)) && (Math.abs(endogenousFeedback.get("Vz"))<(threshold/5)) && (Math.abs(endogenousFeedback.get(action))>threshold) ){
			Vx_un = endogenousFeedback.get("Vx")/Math.abs(endogenousFeedback.get("Vx"));
			index = findDirectionCase(Vx_un, Vy_un, Vz_un);
			data[index][3] = (endogenousFeedback.get(action) + data[index][3]) / 2;

		}
		else if ((Math.abs(endogenousFeedback.get("Vy"))>threshold) && (Math.abs(endogenousFeedback.get("Vx"))<(threshold/5)) && (Math.abs(endogenousFeedback.get("Vz"))<(threshold/5)) && (Math.abs(endogenousFeedback.get(action))>threshold) ){
			Vy_un = endogenousFeedback.get("Vy")/Math.abs(endogenousFeedback.get("Vy"));
			index = findDirectionCase(Vx_un, Vy_un, Vz_un);
			data[index][3] = (endogenousFeedback.get(action) + data[index][3]) / 2;
		}
		else if ((Math.abs(endogenousFeedback.get("Vz"))>threshold) && (Math.abs(endogenousFeedback.get("Vy"))<(threshold/5)) && (Math.abs(endogenousFeedback.get("Vx"))<(threshold/5)) && (Math.abs(endogenousFeedback.get(action))>threshold) ){
			Vz_un = endogenousFeedback.get("Vz")/Math.abs(endogenousFeedback.get("Vz"));
			index = findDirectionCase(Vx_un, Vy_un, Vz_un);
			data[index][3] = (endogenousFeedback.get(action) + data[index][3]) / 2;
		}
		else System.out.println("ERROR : Too smal variations");


	}


	private int findDirectionCase(double Vx, double Vy, double Vz){
		int index = -1;

		for(int i=0; i<data.length; i++) {
			if ((data[i][0] == Vx) && (data[i][1] == Vy) && (data[i][2] == Vz))
				return i;
		}

		return index;
	}

	// Trouve les action différentes de 0.0f
	private ArrayList<Integer> findAvailableActions(){
		ArrayList<Integer> indexActions = new ArrayList<Integer>();

		for(int i=0; i<data.length; i++) {
			if (Math.abs(data[i][3]) > 0.0f)
				indexActions.add(i);
		}

		return indexActions;
	}

	private double findUsefullActions(HashMap<String, Double> endogenousFeedback){
		ArrayList<Integer> indexActions = findAvailableActions();
		double Vx_un, Vy_un, Vz_un;



		Vx_un = endogenousFeedback.get("Vx")/Math.abs(endogenousFeedback.get("Vx"));
		Vy_un = endogenousFeedback.get("Vy")/Math.abs(endogenousFeedback.get("Vy"));
		Vz_un = endogenousFeedback.get("Vz")/Math.abs(endogenousFeedback.get("Vz"));

		for(int i = 0; i < indexActions.size(); i++)
		{
			if ((Vx_un == data[indexActions.get(i)][0]) || (Vy_un == data[indexActions.get(i)][1]) || (Vz_un == data[indexActions.get(i)][2])){
				return data[indexActions.get(i)][3];
			}
		}

		return 0.0d;
	}



	public double request(HashMap<String, Double> endogenousFeedback){
		ArrayList<Integer> indexActions = findAvailableActions();

		double Vx_un, Vy_un, Vz_un;
		Vx_un = 0.0d;
		Vy_un = 0.0d;
		Vz_un = 0.0d;

		if(Math.abs(endogenousFeedback.get("Vx"))> (5*threshold)){
			Vx_un = endogenousFeedback.get("Vx")/Math.abs(endogenousFeedback.get("Vx"));
		}
		if(Math.abs(endogenousFeedback.get("Vy"))> (5*threshold)){
			Vy_un = endogenousFeedback.get("Vy")/Math.abs(endogenousFeedback.get("Vy"));
		}
		if(Math.abs(endogenousFeedback.get("Vz"))> (5*threshold)){
			Vz_un = endogenousFeedback.get("Vz")/Math.abs(endogenousFeedback.get("Vz"));
		}

		// Getting a Set of Key-value pairs
		Set entrySet = endogenousFeedback.entrySet();

		// Obtaining an iterator for the entry set
		Iterator it = entrySet.iterator();

		// Iterate through HashMap entries(Key-Value pairs)
		System.out.println("HashMap Key-Value Pairs : ");
		while(it.hasNext()){
			Map.Entry me = (Map.Entry)it.next();
			System.out.println(me.getKey() + " & " +me.getValue());
		}

		System.out.println(Vx_un);
		System.out.println(Vy_un);
		System.out.println(Vz_un);


		for(int i = 0; i < indexActions.size(); i++)
		{
			if ((Double.compare(Vx_un, data[indexActions.get(i)][0])==0) && (Double.compare(Vx_un, 0.0d)!=0)){ // il y a un problème ici, les conditions marhent pas tout le tps
				System.out.println(data[indexActions.get(i)][3]);
				return data[indexActions.get(i)][3];
			}
			else if  ((Double.compare(Vy_un, data[indexActions.get(i)][1])==0) && (Double.compare(Vy_un, 0.0d)!=0)){ 
				System.out.println(data[indexActions.get(i)][3]);
				return data[indexActions.get(i)][3];
			}
			else if  ((Double.compare(Vz_un, data[indexActions.get(i)][2])==0) && (Double.compare(Vz_un, 0.0d)!=0)){ 
				System.out.println(data[indexActions.get(i)][3]);
				return data[indexActions.get(i)][3];
			}
			else{

			}
		}

		System.out.println(0.0d);
		return 0.0d;
	}

	public void displayModel(){


		System.out.print("Vx\tVy\tVz\t"+action+"\n");
		for(int i=0; i<data.length; i++) {
			for(int j=0; j<data[i].length; j++) {
				System.out.print(data[i][j] + "\t");
			}
			System.out.print("\n");
		}
		System.out.print("\n");
	}
	
	private void updateSensitivity(){
		sensitivityTotal += Math.abs(currentState.get(action)) * (Math.abs(currentState.get("Vx")) + Math.abs(currentState.get("Vy")) + Math.abs(currentState.get("Vz")));
		
		
		sensitivity[0] += Math.abs(currentState.get(action)) * Math.abs(currentState.get("Vx"));
		sensitivity[1] += Math.abs(currentState.get(action)) * Math.abs(currentState.get("Vy"));
		sensitivity[2] += Math.abs(currentState.get(action)) * Math.abs(currentState.get("Vz"));
		
		normalizedSensitivity[0] = sensitivity[0]/sensitivityTotal;
		normalizedSensitivity[1] = sensitivity[1]/sensitivityTotal;
		normalizedSensitivity[2] = sensitivity[2]/sensitivityTotal;
		
		System.out.print("Sensitivity ["+action+"] \t");
		for(int i=0; i<normalizedSensitivity.length; i++) {
			System.out.print(normalizedSensitivity[i] + "\t"); 
		}
		System.out.print("\n");
	}


	/*public static void main(String[] args) {

		ControlModel model = new ControlModel("test", 0.1f);

		model.displayModel();




	}*/
	
}


