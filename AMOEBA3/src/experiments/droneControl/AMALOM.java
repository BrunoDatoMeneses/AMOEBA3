package experiments.droneControl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


import fr.irit.smac.lxplot.LxPlot;
import fr.irit.smac.lxplot.commons.ChartType;

public class AMALOM {
	
	private Double[][] w;
	private double increment;
	private double[] sensibilityNormalizingTerms;
	private double beta[];
	private double[] previousInfinitesimalGoal;
	private double precisionthreshold;
	private AgentVariation[] variationAgents;
	private AgentSensibility[] sensibilityAgents;
	private AgentModel[] modelAgents;
	
	
	private double followingStrengh;
	private int perceptNbr;
	private int actionNbr;
	
	private FIFO sensibility10;
	private FIFO sensibility100;
	private FIFO sensibility200;
	private FIFO sensibility500;
	private FIFO sensibility1000;
	
	private FIFO[] PIDPhase;
	private FIFO[] AMOEBAPhase;
	
	HashMap<String, Double> previousPerceptVariations;
	
	private int cycles;
	
	private ModelConstruction modelConstruction ;

	
	public AMALOM(int nbrOfActions, int nbrOfPerceptions, double incrementValue, int memorySize, double precision, double followingCoefficient, ModelConstruction typeOfModelConstruction)
	{
		perceptNbr = nbrOfPerceptions;
		actionNbr = nbrOfActions;
		
		w = new Double[nbrOfActions][nbrOfPerceptions];
		beta = new double[nbrOfActions];
		previousInfinitesimalGoal = new double[nbrOfPerceptions];
		
		variationAgents = new AgentVariation[nbrOfPerceptions];
		sensibilityAgents = new AgentSensibility[nbrOfActions];
		modelAgents = new AgentModel[nbrOfActions];
		
		previousPerceptVariations = new HashMap<String, Double>();
		for(int i=0; i<nbrOfPerceptions; i++){
			previousPerceptVariations.put("P"+i, 0.0d);
			previousInfinitesimalGoal[i] = 0.0d;
		}
		
		for(int i=0; i<w.length; i++) {
			for(int j=0; j<w[i].length; j++) {
				w[i][j] = 0.0d;
			}
		}
		
		
		
		
		for(int i=0; i<variationAgents.length; i++){
			variationAgents[i] = new AgentVariation("P"+i);
		}
		
		for(int i=0; i<sensibilityAgents.length; i++){
			sensibilityAgents[i] = new AgentSensibility(perceptNbr, "C"+i,memorySize);
			modelAgents[i] = new AgentModel(perceptNbr, "C"+i, precision, incrementValue, typeOfModelConstruction); 
		}
//		modelAgents[0] = new AgentModel(perceptNbr, "C"+0, precision, incrementValue, typeOfModelConstruction, 2, 0.5d); 
//		modelAgents[1] = new AgentModel(perceptNbr, "C"+1, precision, incrementValue, typeOfModelConstruction, 0, 0.5d); 
//		modelAgents[2] = new AgentModel(perceptNbr, "C"+2, precision, incrementValue, typeOfModelConstruction, 1, 0.9d); 
		
		
		
		increment=incrementValue;
		sensibilityNormalizingTerms = new double[6];
		for(int i=0; i<sensibilityNormalizingTerms.length; i++){
			sensibilityNormalizingTerms[i] =0.0d;
		}
		
		sensibility10 = new FIFO(10);
		sensibility100 = new FIFO(100);
		sensibility200 = new FIFO(200);
		sensibility500 = new FIFO(500);
		sensibility1000 = new FIFO(1000);
		
		for(int i=0; i<sensibilityAgents.length; i++){
			sensibilityAgents[i].plotSensibility();
		}
		
		PIDPhase = new FIFO[2];
		AMOEBAPhase = new FIFO[2];
		
		for(int i=0; i<PIDPhase.length; i++){
			PIDPhase[i] = new FIFO(1000);
			AMOEBAPhase[i] = new FIFO(1000);
		}
		
		modelConstruction = typeOfModelConstruction;
		
		followingStrengh = followingCoefficient;
		precisionthreshold = precision;
		cycles = 0;
	}
	
	public void diplayVariations(HashMap<String, Double> perceptVariations){
		
		double[] variations = new double[perceptVariations.size()];
		
			System.out.println(perceptVariations);
			for(int i=0; i<variations.length-1; i++){
				variations[i] = Math.abs(perceptVariations.get("P"+i));	
			}
			
			AMALOM.displayTab(variations, "VARIATIONS");
			System.out.println("NORME : \t"+VECTOR.norm(variations));
			System.out.println("DELTA TIME : \t"+perceptVariations.get("DT"));
		
	}
	
	public void learn(HashMap<String, Double> perceptVariations, HashMap<String, Double> actions){
		
		updateSensibilityNormalizingTerms(perceptVariations, actions);
		
		for(int i=0; i<variationAgents.length; i++){
			//variationAgents[i].update(perceptVariations.get("P"+i));	
		}

		

		for(int i=0; i<sensibilityAgents.length; i++){
			
			sensibilityAgents[i].update(perceptVariations, actions.get("C"+i), beta[i], sensibilityNormalizingTerms);
			modelAgents[i].updateModel(perceptVariations, actions.get("C"+i), sensibilityAgents[i].sortSensibility(), sensibilityAgents[i].getSensibilityValues());
			
			w[i] = copyTab(modelAgents[i].getLocalModel());
			
			sensibilityAgents[i].plotSensibility();
			
		}
		
		
		//previousInfinitesimalGoal = convertPerceptHashMapToTab(HashMap<String, Double> map);
		
		displayModel();
		displaySensibilities();
		//adaptModel();
	}
	
	public void updateSensibility(HashMap<String, Double> perceptVariations, HashMap<String, Double> actions){
		
		updateSensibilityNormalizingTerms(perceptVariations, actions);
		
		for(int i=0; i<sensibilityAgents.length; i++){
			
			sensibilityAgents[i].update(perceptVariations, actions.get("C"+i), beta[i], sensibilityNormalizingTerms);
			sensibilityAgents[i].plotSensibility();
			
		}
		
		displayModel();
		displaySensibilities();
	}
	
	public AgentSensibility getAgentSensibility(int nbr){
		return sensibilityAgents[nbr];
	}
	
	
	public double[] request(HashMap<String, Double> perceptVariations, HashMap<String, Double> goal){
		double[] out =  new double[perceptVariations.size()];
		double[] goalPerceptions = new double[goal.size()];
		double[][] perceptions = new double[2][perceptVariations.size()];
		
		for(int i=0; i<goalPerceptions.length; i++){
			goalPerceptions[i]=goal.get("P"+i);
		}
		
		for(int i=0; i<perceptions[0].length; i++){
			perceptions[1][i]=perceptVariations.get("P"+i);
			perceptions[0][i]=previousPerceptVariations.get("P"+i);
			previousPerceptVariations.put("P"+i, perceptVariations.get("P"+i));
			
		}
		
		out = requestVariation(perceptions,  goalPerceptions);
		
		displayModel();
		AMALOM.displayTab(out, "COMMANDS");
		
		return out;
	}
	
	private double[] requestVariation(double[][] currentPerceptions, double[] goalPerceptions){
		 
		return requestInfinitesimalVariation(getNextPossiblePerceptionVariations(currentPerceptions, goalPerceptions));
	}
	
	private double[] getNextPossiblePerceptionVariations(double[][] currentPerceptions, double[] goalPerceptions){
		 
		double[] currentPerceptionVariations = new double[currentPerceptions[0].length];
		System.out.println("DISTANCE TO GOAL\t"+ getDistanceToGoal( currentPerceptions[1], goalPerceptions));
		/*if(getDistanceToGoal( currentPerceptions[1], goalPerceptions)<5){
			currentPerceptionVariations = VECTOR.product(0.3*getDistanceToGoal( currentPerceptions[1], goalPerceptions), VECTOR.identity(currentPerceptions[1].length));
		}else{
			currentPerceptionVariations = VECTOR.product(followingStrengh, VECTOR.identity(currentPerceptions[1].length));
		}*/
		
		currentPerceptionVariations = VECTOR.product(followingStrengh*getDistanceToGoal( currentPerceptions[1], goalPerceptions), VECTOR.identity(currentPerceptions[1].length));
		//currentPerceptionVariations = VECTOR.product(2, VECTOR.identity(currentPerceptions[1].length)); //Wanted variation (speed)
		double[] goalDirection = getGoalDirection(currentPerceptions[1],goalPerceptions);
		double[] nextPossiblePerceptionState = VECTOR.sum(currentPerceptions[1], VECTOR.termProduct(currentPerceptionVariations, goalDirection));
		double[] nextPossiblePerceptionVariation = VECTOR.difference(nextPossiblePerceptionState, currentPerceptions[1]);
		
		System.out.println("いいいいいいいいいいいいいいいいいいいいいいいいいいい");
		displayTab(currentPerceptionVariations, "CURRENT WANTED VARIATIONS");
		displayTab(currentPerceptions[0], "PREVIOUS PERCEPTION");
		displayTab(currentPerceptions[1], "CURRENT PERCEPTION");
		displayTab(nextPossiblePerceptionState, "NEXT POSSIBLE PERCEPTION");
		displayTab(nextPossiblePerceptionVariation, "NEXT POSSIBLE PERCEPTION VARIATION");
		System.out.println("VARIATION NORM \t" + VECTOR.norm(nextPossiblePerceptionVariation));
		
		displayTab(goalPerceptions, "GOAL");
		displayTab(goalDirection, "GOAL DIRECTION");
		displayTab(previousInfinitesimalGoal, "PREVIOUS GOAL");
		displayTab(currentPerceptions[1], "CURRENT PERCEPTION");
		System.out.println("いいいいいいいいいいいいいいいいいいいいいいいいいいい");
		
		//adaptModelByVariation(currentPerceptions[1], previousInfinitesimalGoal, goalDirection);
		previousInfinitesimalGoal = copyTab(nextPossiblePerceptionState);
		
		return nextPossiblePerceptionVariation;
	}
	
	private void adaptModelByVariation(double[] currentPerceptions, double[] wantedperceptions, double[] goalDirection){
		
		for(int i=0; i<variationAgents.length; i++){
			variationAgents[i].adapt(currentPerceptions[i], wantedperceptions[i], goalDirection[i]);	
		}
		
		if(modelConstruction.equals(ModelConstruction.HIGHEST_SENSIBILITY)){
			for(int i=0; i<modelAgents.length; i++){
				for(int j=0; j<variationAgents.length; j++){
					
					System.out.println("*******************************************************");
					System.out.println("CRIT\t" + variationAgents[j].getCriticity() + "\t" + 0.5);
					System.out.println("HIGEST SENSIB\t" + sensibilityAgents[i].getHighestSensibilityIndice() + "\t" + j);
					
					if((variationAgents[j].getCriticity() > 1) && (sensibilityAgents[i].getHighestSensibilityIndice() == j)){ // highest sensibility indice equals to j
						System.out.println("############################################################");
						System.out.println(variationAgents[j].getName());
						displayTab(sensibilityAgents[i].getSensibility(), "SORTED SENSIBILITY");
						System.out.println("CURRENT VARIATION\t" + j);
						System.out.println("CRITICITY\t" + variationAgents[j].getCriticity());
						modelAgents[i].adaptLocalModel(j, j, variationAgents[j].getState().get("IncrementalSign"), sensibilityAgents[i].getSensibility(), variationAgents[j].getState().get("Criticity"));
						System.out.println("############################################################");
					}
					
				}
				w[i] = copyTab(modelAgents[i].getLocalModel());
			}
		}
		
		
		
	}
	
	private int getHighestCriticityIndiceVairationAgents(){
		int highestCriticityIndice = -1;
		double highestCriticity = 0.0d;
		
		for(int j=0; j<variationAgents.length; j++){
			if(variationAgents[j].getCriticity()>highestCriticity){
				highestCriticity = variationAgents[j].getCriticity();
				highestCriticityIndice = j;
			}
		}
		
		return highestCriticityIndice;
	}
	
	
	
	private double[] getGoalDirection(double[] currentPerceptions, double[] goalPerceptions){
		double[] goalDirection = new double[goalPerceptions.length];
		double sum=0;
		
		for(int i =0; i<goalPerceptions.length; i++){
			sum += Math.abs(goalPerceptions[i]-currentPerceptions[i]);
		}
		
		for(int i =0; i<goalPerceptions.length; i++){
			goalDirection[i] = (goalPerceptions[i]-currentPerceptions[i])/sum;
		}
		
		return goalDirection;
	}
	
	
	
	private double[] getPerceptionVariations(double[][] currentPerceptions){
		double[] currentPerceptionVariations = new double[currentPerceptions[0].length];
		
		for(int i =0; i<currentPerceptions[0].length; i++){
			//currentPerceptionVariations[i] = currentPerceptions[1][i]-currentPerceptions[0][i];
			currentPerceptionVariations[i] = 5.0d;
		}
		
		return currentPerceptionVariations;
	}
	
	private double getDistanceToGoal(double[] currentPerceptions, double[] currentgoal){
		double distance = 0.0d;
		
		distance = VECTOR.norm(VECTOR.difference(currentgoal, currentPerceptions));
				
		return distance;
	}
	
	private double[] requestInfinitesimalVariation(double[] perceptInfinitesimalVariations){
		double[] actions = new double[actionNbr];
		
		for(int i=0; i<actions.length; i++){
			actions[i]=0.0d;
			for(int j=0; j<perceptInfinitesimalVariations.length; j++){
				actions[i] += w[i][j]*perceptInfinitesimalVariations[j];
			}
		}
		
		return actions;
	}
	
	public void updateSensibilityNormalizingTerms(HashMap<String, Double> perceptVariations, HashMap<String, Double> actions){
		double actionsSum = 0.0d;
		double sij = 0.0d;
		
		for(int i=0; i<actions.size(); i++) {
			actionsSum += Math.abs(actions.get("C"+i));
		}
		
		for(int i=0; i<actions.size(); i++) {
			if(actionsSum > 0){
				beta[i] = Math.abs(actions.get("C"+i))/actionsSum;
			}
			else 
			{
				beta[i] = 0;
			}
			for(int j=0; j<perceptVariations.size()-1; j++) {
				//sensibilityNormalizingTerms[0] += Math.abs(beta[i]*actions.get("C"+i)*perceptVariations.get("P"+j));
				sij += Math.abs(beta[i]*actions.get("C"+i)*perceptVariations.get("P"+j)); 
			}
		}
		
		sensibility10.add(sij);
		sensibility100.add(sij);
		sensibility200.add(sij);
		sensibility500.add(sij);
		sensibility1000.add(sij);
		
		sensibilityNormalizingTerms[0] += sij;
		sensibilityNormalizingTerms[1] = sensibility10.getSum();
		sensibilityNormalizingTerms[2] = sensibility100.getSum();
		sensibilityNormalizingTerms[3] = sensibility200.getSum();
		sensibilityNormalizingTerms[4] = sensibility500.getSum();
		sensibilityNormalizingTerms[5] = sensibility1000.getSum();
		
		/*System.out.println("NORMALIZING TERMS");
		for(int i =0; i<6;i++){
			System.out.println(sensibilityNormalizingTerms[i]);
		}*/
		
		
	}
	
	
	
	public void adaptModel(){
		Double[][] wTemp = new Double[actionNbr][perceptNbr];
		String[] criticitiesIndices = new String[perceptNbr];
		wTemp = copyMatrix(w);
		criticitiesIndices = sort(getCriticities());
		
		for(int i=0; i<wTemp.length; i++) {
			for(int j=0; j<w[i].length; j++) {
				wTemp[i][j] = wTemp[i][j] + variationAgents[j].getState().get("IncrementalSign")*increment;
			}
		}
		
		displayMatrix(wTemp, "WTEMP");
		
		for(int i=0; i<wTemp.length; i++) {
			displayTab(sort(wTemp[i]), "WTEMP_"+i);
			displayTab(sensibilityAgents[i].sortSensibility(), "SENSIBILITY_"+i);
			System.out.println("TEST = " + compare(sort(wTemp[i]), sensibilityAgents[i].sortSensibility()));
			
			if(compare(sort(wTemp[i]), sensibilityAgents[i].sortSensibility())){
				w[i] = copyTab(wTemp[i]);
			}
			else {
				int minimalCriticityIndice = sensibilityAgents.length-1;
				
				while(compare(sort(wTemp[i]), sensibilityAgents[i].sortSensibility()) && (minimalCriticityIndice>-1)){
					reverseUpdates(wTemp[i], Integer.parseInt(criticitiesIndices[minimalCriticityIndice]));
					minimalCriticityIndice--;
				}
				
				w[i] = copyTab(wTemp[i]);
			}
		}
		displayModel();
		
		
		plotModel();
		
		cycles++;
	}
	
	private void plotModel(){
		for(int i=0; i<w.length; i++) {
			for(int j=0; j<w[i].length; j++) {
				//LxPlot.getChart("C" + i + " Model", ChartType.LINE).add("W"+i+j, cycles,  w[i][j]);
			}

		}
		
		
	}
	
	private Double[] getCriticities(){
		Double[] criticities = new Double[variationAgents.length];
		for(int i =0; i< variationAgents.length;i++){
			criticities[i] = variationAgents[i].getState().get("Criticity");
		}
		return criticities;
	}
	
	private void reverseUpdates(Double[] wTemp, int perception){
		
			wTemp[perception] = wTemp[perception] - variationAgents[perception].getState().get("IncrementalSign")*increment;
	}
	
	private int getAgentWithMininumCriticity(AgentVariation[] variationAgents){
		int indice = 0;
		double criticity = variationAgents[0].getState().get("Criticity");
		
		for(int i = 1;i<variationAgents.length;i++){
			if(variationAgents[i].getState().get("Criticity")<criticity){
				criticity = variationAgents[i].getState().get("Criticity");
				indice = i;
			}
		}
		
		return indice;
	}
	
	private boolean compare(String[] tab1, String[] tab2){
		
		if(tab1.length != tab2.length){
			return false;
		}
		else{
			for(int j=0; j<tab1.length; j++) {
				if (!tab1[j].equals(tab2[j])){
					return false;
				}
			}
		}
		
		return true;
	}
	
	public void displayModel(){
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
		System.out.print("\nCONTROL MODEL\n");
		for(int i=0; i<w.length; i++) {
			for(int j=0; j<w[i].length; j++) {
				System.out.print(w[i][j]+"\t");
			}
			System.out.print("\n");
		}
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
	}
	
	public void displaySensibilities(){
		System.out.print("\nSENSIBILITIES\n");
		for(int i=0; i<sensibilityAgents.length; i++) {
			System.out.println(sensibilityAgents[i].getSensibilityValues());
		}
		for(int i=0; i<sensibilityAgents.length; i++) {
			displayTab(sensibilityAgents[i].getSensibility(), "C"+i+"\t["+sensibilityAgents[i].getConfidence()+"]");
		}
		
	}
	
	public String[] sort(Double[] tab){
		String[] sortedIndicesArray = new String[tab.length];
		ArrayIndexComparator comparator = new ArrayIndexComparator(tab);
		Integer[] indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);
		for (int i = 0; i < tab.length; i++)
        {
			sortedIndicesArray[i] = "P"+indexes[i].toString();
        }
		
		return sortedIndicesArray;		
	}
	
	private void displayTab(String[] tab, String info){
		System.out.println(info);
		for(int i = 0; i< tab.length;i++){
			System.out.print(tab[i]+"\t");
		}
		System.out.println(" ");
	}
	
	public static void displayTab(double[] tab, String info){
		System.out.println(info);
		if(tab!=null){
			
			for(int i = 0; i< tab.length;i++){
				System.out.print(tab[i]+"\t");
			}
			System.out.println(" ");
		}
		
	}
	
	private static Double[][] copyMatrix(Double[][] matrix){
		Double[][] copy = new Double[matrix.length][matrix[0].length];
		
		for(int i=0; i<matrix.length; i++) {
			for(int j=0; j<matrix[i].length; j++) {
				copy[i][j] = matrix[i][j];
			}
		}
		
		return copy;
	}
	
	private void displayMatrix(Double[][] matrix, String info){
		System.out.println("\n"+info);
		for(int i=0; i<matrix.length; i++) {
			for(int j=0; j<matrix[i].length; j++) {
				System.out.print(matrix[i][j]+"\t");
			}
			System.out.print("\n");
		}
		
	}
	
	private static Double[] copyTab(Double[] tab){
		Double[] copy = new Double[tab.length];
		
		for(int i=0; i<tab.length; i++) {
				copy[i] = tab[i];
		}
		
		return copy;
	}
	
	private static double[] copyTab(double[] tab){
		double[] copy = new double[tab.length];
		
		for(int i=0; i<tab.length; i++) {
				copy[i] = tab[i];
		}
		
		return copy;
	}
	
	public static String[] copyTab(String[] tab){
		String[] copy = new String[tab.length];
		
		for(int i=0; i<tab.length; i++) {
				copy[i] = tab[i];
		}
		
		return copy;
	}
	
	/*public static void main(String[] args) {
		
		Double[][] w = new Double[2][2];
		
		for(int i=0; i<w.length; i++) {
			for(int j=0; j<w[i].length; j++) {
				w[i][j] = 1.0d;
			}
		}
		
		displayMatrix(w,"1");
		
		for(int i=0; i<w.length; i++) {
			for(int j=0; j<w[i].length; j++) {
				w[i][j] = w[i][j] + 0.1;
			}
		}
		
		displayMatrix(w,"2");
		
		reverseUpdates(w, 1); 
		
		displayMatrix(w,"3");
		
	}*/
	
	public  AgentSensibility getSensibilityAgent(Object key){	
		for(int i =0; i<sensibilityAgents.length;i++){
			if(sensibilityAgents[i].getSensibility().equals(key)){
				return sensibilityAgents[i];
			}
		}	
		return null;
	}

	
	public double[] convertPerceptHashMapToTab(HashMap<String, Double> map){
		double[] tab = new double[map.size()];
		
		for(int i=0; i<map.size(); i++){
			tab[i]=map.get("P"+i);
		}
		
		return tab;
	}
	
	public void stopWriting(){
		for(int i=0; i<sensibilityAgents.length; i++){
			sensibilityAgents[i].stopWriting();
		}
		
		for(int i=0; i<modelAgents.length; i++){
			modelAgents[i].stopWriting();
		}
	}
}
