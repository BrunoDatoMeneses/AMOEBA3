package experiments.droneControl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jfree.chart.plot.XYPlot;

import fr.irit.smac.lxplot.LxPlot;
import fr.irit.smac.lxplot.commons.ChartType;
import fr.irit.smac.lxplot.server.LxPlotChart;

public class AgentSensibility {
	

	
	private String commandName;
	private int perceptionsNbr;
	
	private HashMap<String, Double> sensib;
	private HashMap<String, Double> normalisedSensib;
	private HashMap<String, Double> normalisedSensibility10;
	private HashMap<String, Double> normalisedSensibility100;
	private HashMap<String, Double> normalisedSensibility200;
	private HashMap<String, Double> normalisedSensibility500;
	private HashMap<String, Double> normalisedSensibility1000;
	private HashMap<String, FIFO> HMSensibility10;
	private HashMap<String, FIFO> HMSensibility100;
	private HashMap<String, FIFO> HMSensibility200;
	private HashMap<String, FIFO> HMSensibility500;
	private HashMap<String, FIFO> HMSensibility1000;
	
	private List<String> sortedSensibilityList;
	private String[] sortedSensibilityTab;
	
	private double confidence;
	private double globalConfidence;
	
	private int cycles;
	
	private FIFO sensibility10;
	private FIFO sensibility100;
	private FIFO sensibility200;
	private FIFO sensibility500;
	private FIFO sensibility1000;
	
	private FIFO betaMoy1000;
	
	private FILE sensibilityFile;
	
	private double[] sensibilityLocalNormalizingTerms;	
	
	public AgentSensibility(){}
	
	public AgentSensibility(int nbrOfPerceptions, String name, int memorySize) {
		
		sensibilityFile = new FILE("19122017", "Sensibilite_" + name);
		perceptionsNbr = nbrOfPerceptions;
		sensib = new HashMap<String, Double>();
		normalisedSensib = new HashMap<String, Double>();
		normalisedSensibility10 = new HashMap<String, Double>();
		normalisedSensibility100 = new HashMap<String, Double>();
		normalisedSensibility200 = new HashMap<String, Double>();
		normalisedSensibility500 = new HashMap<String, Double>();
		normalisedSensibility1000 = new HashMap<String, Double>();
		
		HMSensibility10 = new HashMap<String, FIFO>();
		HMSensibility100 = new HashMap<String, FIFO>();
		HMSensibility200 = new HashMap<String, FIFO>();
		HMSensibility500 = new HashMap<String, FIFO>();
		HMSensibility1000 = new HashMap<String, FIFO>();
		
		for(int i=0; i<nbrOfPerceptions; i++){
			HMSensibility10.put("P"+i,new FIFO(10));
			HMSensibility100.put("P"+i,new FIFO(100));
			HMSensibility200.put("P"+i,new FIFO(200));
			HMSensibility500.put("P"+i,new FIFO(500));
			HMSensibility1000.put("P"+i,new FIFO(1000));
		}
		
		sensibility10 = new FIFO(10);
		sensibility100 = new FIFO(100);
		sensibility200 = new FIFO(200);
		sensibility500 = new FIFO(500);
		sensibility1000 = new FIFO(1000);
		
		betaMoy1000 = new FIFO(1000);
		
		sensibilityLocalNormalizingTerms = new double[6];
		for(int i=0; i<sensibilityLocalNormalizingTerms.length; i++){
			sensibilityLocalNormalizingTerms[i] =0.0d;
		}
		
		sortedSensibilityList = new ArrayList<>();
		sortedSensibilityTab = new String[nbrOfPerceptions];
		commandName = name; 
		double sensibilityInitialization;
		Random rand = new Random();
		
		for(int i=0; i<nbrOfPerceptions; i++) {

			//sensibilityInitialization = rand.nextDouble()/3;
			sensibilityInitialization = 0.0d;
			sensib.put("P"+i, sensibilityInitialization);
			normalisedSensib.put("P"+i, sensibilityInitialization);
			normalisedSensibility10.put("P"+i, sensibilityInitialization);
			normalisedSensibility100.put("P"+i, sensibilityInitialization);
			normalisedSensibility200.put("P"+i, sensibilityInitialization);
			normalisedSensibility500.put("P"+i, sensibilityInitialization);
			normalisedSensibility1000.put("P"+i, sensibilityInitialization);
			
		}
		
//		LxPlot.getChart(commandName + " Sensibility", ChartType.LINE);
//		LxPlot.getChart(commandName + " Sensibility10", ChartType.LINE, 10);
//		LxPlot.getChart(commandName + " Sensibility100", ChartType.LINE, 100);
//		LxPlot.getChart(commandName + " Sensibility200", ChartType.LINE, 200);
//		LxPlot.getChart(commandName + " Sensibility500", ChartType.LINE, 500);
//		LxPlot.getChart(commandName + " Sensibility1000", ChartType.LINE, 1000);
		
//		LxPlot.getChart(commandName + " Sensibility10", ChartType.LINE, 500);
//		LxPlot.getChart(commandName + " Sensibility100", ChartType.LINE, 500);
//		LxPlot.getChart(commandName + " Sensibility200", ChartType.LINE, 500);
//		LxPlot.getChart(commandName + " Sensibility500", ChartType.LINE, 500);
//		LxPlot.getChart(commandName + " Sensibility1000", ChartType.LINE, 500);
		
		confidence = 0.0d;
		globalConfidence = 0.0d;
		
		cycles=0;
		
	}
	
	public void update(HashMap<String, Double> perceptVariations, double action, double beta, double[] normalizingTerm){
		
		
		if(normalizingTerm[0] > 0){
			for(int i=0; i<perceptVariations.size()-1; i++) {
				
				sensib.put("P"+i, sensib.get("P"+i) + Math.abs(beta*action*perceptVariations.get("P"+i)));
				
				HMSensibility10.get("P"+i).add((Math.abs(beta*action*perceptVariations.get("P"+i))));
				HMSensibility100.get("P"+i).add((Math.abs(beta*action*perceptVariations.get("P"+i))));
				HMSensibility200.get("P"+i).add((Math.abs(beta*action*perceptVariations.get("P"+i))));
				HMSensibility500.get("P"+i).add((Math.abs(beta*action*perceptVariations.get("P"+i))));
				HMSensibility1000.get("P"+i).add((Math.abs(beta*action*perceptVariations.get("P"+i))));
				
				normalisedSensib.put("P"+i, sensib.get("P"+i)/normalizingTerm[0]);
				normalisedSensibility10.put("P"+i, HMSensibility10.get("P"+i).getSum()/normalizingTerm[1]);
				normalisedSensibility100.put("P"+i, HMSensibility100.get("P"+i).getSum()/normalizingTerm[2]);
				normalisedSensibility200.put("P"+i, HMSensibility200.get("P"+i).getSum()/normalizingTerm[3]);
				normalisedSensibility500.put("P"+i, HMSensibility500.get("P"+i).getSum()/normalizingTerm[4]);
				normalisedSensibility1000.put("P"+i, HMSensibility1000.get("P"+i).getSum()/normalizingTerm[5]);
				
//				normalisedSensib.put("P"+i, sensib.get("P"+i)/normalizingTerm[0]);
//				normalisedSensibility10.put("P"+i, HMSensibility10.get("P"+i).getSum()/normalizingTerm[1]);
//				normalisedSensibility100.put("P"+i, HMSensibility100.get("P"+i).getSum()/normalizingTerm[2]);
//				normalisedSensibility200.put("P"+i, HMSensibility200.get("P"+i).getSum()/normalizingTerm[3]);
//				normalisedSensibility500.put("P"+i, HMSensibility500.get("P"+i).getSum()/normalizingTerm[4]);
//				normalisedSensibility1000.put("P"+i, HMSensibility1000.get("P"+i).getSum()/normalizingTerm[5]);
				
				
				
			}
		}
		
		sortedSensibilityTab = sortSensibility();
		
		betaMoy1000.add(beta);
		//System.out.println(commandName + "\tBETA moy\t" + (betaMoy1000.getMean()));
		
	}
	
	public void updateSensibilityLocalNormalizingTerms(HashMap<String, Double> perceptVariations, Double action){
		double sij = 0.0d;
		
		
			for(int j=0; j<perceptVariations.size(); j++) {
				//sensibilityNormalizingTerms[0] += Math.abs(beta[i]*actions.get("C"+i)*perceptVariations.get("P"+j));
				sij += Math.abs(action*perceptVariations.get("P"+j)); 
			}
		
		sensibility10.add(sij);
		sensibility100.add(sij);
		sensibility200.add(sij);
		sensibility500.add(sij);
		sensibility1000.add(sij);
		
		sensibilityLocalNormalizingTerms[0] += sij;
		sensibilityLocalNormalizingTerms[1] = sensibility10.getSum();
		sensibilityLocalNormalizingTerms[2] = sensibility100.getSum();
		sensibilityLocalNormalizingTerms[3] = sensibility200.getSum();
		sensibilityLocalNormalizingTerms[4] = sensibility500.getSum();
		sensibilityLocalNormalizingTerms[5] = sensibility1000.getSum();
		
		/*System.out.println("NORMALIZING TERMS");
		for(int i =0; i<6;i++){
			System.out.println(sensibilityNormalizingTerms[i]);
		}*/
		
		
	}
	
	public void plotSensibility(){
		 
		//System.out.println(normalizedSensibility);
		//System.out.println(normalisedSensib);
		//System.out.println(commandName + " "+ sortedSensibilityList);
		
		for(int i=0; i<normalisedSensib.size(); i++) {
			//LxPlot.getChart(commandName + " Sensibility", ChartType.LINE).add("P"+i, cycles,  normalisedSensib.get("P"+i));
//			LxPlot.getChart(commandName + " Sensibility10", ChartType.LINE, 10).add("P"+i, cycles,  normalisedSensibility10.get("P"+i));
//			LxPlot.getChart(commandName + " Sensibility100", ChartType.LINE, 100).add("P"+i, cycles,  normalisedSensibility100.get("P"+i));
//			LxPlot.getChart(commandName + " Sensibility200", ChartType.LINE, 200).add("P"+i, cycles,  normalisedSensibility200.get("P"+i));
//			LxPlot.getChart(commandName + " Sensibility500", ChartType.LINE, 500).add("P"+i, cycles,  normalisedSensibility500.get("P"+i));
			LxPlot.getChart(commandName + " Sensibility1000", ChartType.LINE, 1000).add("P"+i, cycles,  normalisedSensibility1000.get("P"+i));
			
//			LxPlot.getChart(commandName + " Sensibility10", ChartType.LINE, 500).add("P"+i, cycles,  normalisedSensibility10.get("P"+i));
//			LxPlot.getChart(commandName + " Sensibility100", ChartType.LINE, 500).add("P"+i, cycles,  normalisedSensibility100.get("P"+i));
//			LxPlot.getChart(commandName + " Sensibility200", ChartType.LINE, 500).add("P"+i, cycles,  normalisedSensibility200.get("P"+i));
//			LxPlot.getChart(commandName + " Sensibility500", ChartType.LINE, 500).add("P"+i, cycles,  normalisedSensibility500.get("P"+i));
//			LxPlot.getChart(commandName + " Sensibility1000", ChartType.LINE, 500).add("P"+i, cycles,  normalisedSensibility1000.get("P"+i));
		}
		
		//sensibilityFile.write((double)cycles, normalisedSensibility1000.get("P"+0), normalisedSensibility1000.get("P"+1), normalisedSensibility1000.get("P"+2)); ***************************************************

		
		cycles++;
	}
	
	public String[] sortSensibility(){
		String[] sortedSensibilityTab = new String[perceptionsNbr];
		SortedSet<Map.Entry<String, Double>> sortedSensibility = new TreeSet<>(new Comparator<Map.Entry<String, Double>>() {
		    @Override
		    public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
		        int res = e1.getValue().compareTo(e2.getValue());
		        if(res == 0)
		            return e1.getKey().compareTo(e2.getKey());
		        return res * -1;
		    }
		});
		
		sortedSensibility.addAll(normalisedSensibility1000.entrySet());
		List<String> list = new ArrayList<>();
		for(Map.Entry<String, Double> e: sortedSensibility)
		    list.add(e.getKey());
		
		sortedSensibilityList = list;
		//System.out.println(commandName + " "+sortedSensibility + " " + Math.abs(sortedSensibility.first().getValue()-normalisedSensib.get(sortedSensibilityList.get(1))));
		//System.out.println(sortedSensibility.first());
		
		// Get confidence
		confidence = cycles*Math.abs(normalisedSensibility1000.get(sortedSensibilityList.get(0))-normalisedSensibility1000.get(sortedSensibilityList.get(1)));
		globalConfidence = 0.0d;
		for(int i = 0;i<sortedSensibility.size()-1;i++){
			globalConfidence += Math.abs(normalisedSensibility1000.get(sortedSensibilityList.get(i))-normalisedSensibility1000.get(sortedSensibilityList.get(i+1)));
		}
		globalConfidence = cycles*globalConfidence / (sortedSensibility.size()-1);
		
		
		// Build simple tab
		for(int i=0;i<sortedSensibility.size();i++){
			sortedSensibilityTab[i] = sortedSensibilityList.get(i);
			//System.out.print(sortedSensibilityTab[i]+"\t");
		}
		//System.out.print("\n");
		
		return sortedSensibilityTab;
	}
	
	
	public double getConfidence(){
		return confidence;
	}
	
	public double getGlobalConfidence(){
		return globalConfidence;
	}
	
	public String[] getSensibility(){
		return sortedSensibilityTab;
	}
	
	
	
	public HashMap<String, Double> getSensibilityValues(){
		return normalisedSensibility1000;
	}

	public int getHighestSensibilityIndice(){
		return Character.getNumericValue(sortSensibility()[0].charAt(1));
	}
	
	public void stopWriting(){
		sensibilityFile.close();
	}
	
}
