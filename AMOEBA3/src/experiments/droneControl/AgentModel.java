package experiments.droneControl;

import java.util.HashMap;

import fr.irit.smac.lxplot.LxPlot;
import fr.irit.smac.lxplot.commons.ChartType;

public class AgentModel {
	
	private String name;
	public ModelConstruction modelConstruction;
	
	private double criticity;
	private FIFO criticityMemory;
	private Double[] commandModel;
	private Double[] commandModelSave;
	private Double[] meanCommandModel;
	
	private int cycles;
	
	private double threshold;
	private double increment;
	
	
	private HashMap<String, FIFO> modelMeans;
	private HashMap<String, Double> localSensibilities;
	private String[] sortedSensibilities;
	
	private FILE modelFile;
	private FILE meanModelFile;
	private FILE criticityFile;
	
	public AgentModel(int nbrOfPerceptions, String Id, double precision, double incrmt, ModelConstruction typeOfModelConstruction){
		name = Id;
		modelFile = new FILE("19122017", "Modele_" + name);
		meanModelFile = new FILE("19122017", "ModeleMoyen_" + name);
		criticityFile = new FILE("19122017", "Criticite_" + name);
		
		commandModel = new Double[nbrOfPerceptions];
		commandModelSave = new Double[nbrOfPerceptions];
		meanCommandModel = new Double[nbrOfPerceptions];
		sortedSensibilities = new String[nbrOfPerceptions];
		
		cycles = 0;
		threshold = precision;
		increment = incrmt;
		
		modelConstruction = typeOfModelConstruction;
		
		for(int i=0; i<commandModel.length; i++){
			//commandModel[i]=(double) (1/nbrOfPerceptions);
			//commandModel[i]=-5 + Math.random()*10;
			
			commandModel[i]=0.0d;
			commandModelSave[i] = 0.0d;
			meanCommandModel[i]=0.0d;
		}
		
		
		modelMeans = new HashMap<String, FIFO>();
		for(int i=0; i<nbrOfPerceptions; i++){
			modelMeans.put("W"+name.charAt(1)+i,new FIFO(200));
		}
		
		criticityMemory = new FIFO(200);
	}
	
	public AgentModel(int nbrOfPerceptions, String Id, double precision, double incrmt, ModelConstruction typeOfModelConstruction, int initIndice, double initValue){
		commandModel = new Double[nbrOfPerceptions];
		commandModelSave = new Double[nbrOfPerceptions];
		meanCommandModel = new Double[nbrOfPerceptions];
		sortedSensibilities = new String[nbrOfPerceptions];
		name = Id;
		cycles = 0;
		threshold = precision;
		increment = incrmt;
		
		modelConstruction = typeOfModelConstruction;
		
		for(int i=0; i<commandModel.length; i++){
			//commandModel[i]=(double) (1/nbrOfPerceptions);
			//commandModel[i]=-5 + Math.random()*10;
			
			commandModel[i]=0.0d;
			commandModelSave[i] = 0.0d;
			meanCommandModel[i]=0.0d;
		}
		commandModel[initIndice]=initValue;
		
		modelMeans = new HashMap<String, FIFO>();
		for(int i=0; i<nbrOfPerceptions; i++){
			modelMeans.put("W"+name.charAt(1)+i,new FIFO(200));
		}
		
		criticityMemory = new FIFO(200);
	}
	
	
	
	public void updateModel(HashMap<String, Double> perceptions, Double command, String[] sortedSensib, HashMap<String, Double> sensibilities){
		double modelResult = 0.0d;
		
		sortedSensibilities = AMALOM.copyTab(sortedSensib);
		localSensibilities = new HashMap<String, Double>(sensibilities);
		
		for(int i =0; i<commandModel.length; i++){
			modelResult += commandModel[i]*perceptions.get("P"+i);
		}
		
		criticity = Math.abs(command - modelResult);
		criticityMemory.add(criticity);
		
		//LxPlot.getChart("Model criticities", ChartType.LINE).add(name, cycles, criticity);
		//LxPlot.getChart("Model mean criticities", ChartType.LINE).add(name, cycles, criticityMemory.getMean());
		
		criticityFile.write((double)cycles, criticity, criticityMemory.getMean());
		

		
		
		if(modelConstruction.equals(ModelConstruction.FULL)){
			for(int i=0;i<commandModel.length; i++){
				if(criticity > threshold){
					adaptLocalModel(i, command, perceptions.get(sortedSensibilities[i]));
				}
			}
		}else if(modelConstruction.equals(ModelConstruction.HIGHEST_SENSIBILITY)){
			if(criticity > threshold){
				adaptLocalModel(0, command, perceptions.get(sortedSensibilities[0]));
				
				for(int i=0;i<commandModel.length; i++){
					if(!sortedSensibilities[0].contentEquals("P"+i)){ // Not hightest sensibility coefficients
						if(Math.abs(commandModel[i])>increment){
							commandModelSave[i] = commandModel[i];
						}
						commandModel[i] = 0.0d;
					}
				}
			}
		}
		
		
		
		cycles ++;
	}
	
	
	public void adaptLocalModel(int modelIndice, int sensibilityIndice, double incrementSign, String[] sortedSensib, double criticity){
		
		commandModel[modelIndice] += incrementSign*increment*localSensibilities.get("P"+sensibilityIndice);
		
		for(int i=0; i<commandModel.length; i++){
			//LxPlot.getChart("Local Model " + name, ChartType.LINE).add("W"+name.charAt(1)+i, cycles, commandModel[i] );
			
			
			
			modelMeans.get("W"+name.charAt(1)+i).add(commandModel[i]);
			//LxPlot.getChart("Local Model Mean" + name, ChartType.LINE).add("W"+name.charAt(1)+i, cycles, modelMeans.get("W"+name.charAt(1)+i).getMean() );
			meanCommandModel[i]= modelMeans.get("W"+name.charAt(1)+i).getMean(); 
		}		
		
		modelFile.write((double)cycles, commandModel[0], commandModel[1], commandModel[2]);
		meanModelFile.write((double)cycles, modelMeans.get("W"+name.charAt(1)+0).getMean(), modelMeans.get("W"+name.charAt(1)+1).getMean(), modelMeans.get("W"+name.charAt(1)+2).getMean());
		
		cycles++;
	}
	
	
	private void adaptLocalModel(int sensibilityCoefficientIndicator, Double command, Double perception){
				
		
		int modelCoefficientIndicator = Character.getNumericValue(sortedSensibilities[sensibilityCoefficientIndicator].charAt(1)); // highest sensibility indice
		double incrementSign = (double) getIncrementSign(perception, command, modelCoefficientIndicator);
		
		if((Math.abs(commandModel[modelCoefficientIndicator])<increment) && (Math.abs(commandModelSave[modelCoefficientIndicator])>increment)){
			commandModel[modelCoefficientIndicator] = commandModelSave[modelCoefficientIndicator]; //use old value instead of learning again everything
		}
		commandModel[modelCoefficientIndicator] += incrementSign*increment;//*localSensibilities.get("P"+sensibilityCoefficientIndicator);//(sensibilityCoefficientIndicator+1);
		
//		System.out.println("SENSIBILITY COEF IND \t" + sensibilityCoefficientIndicator);
//		System.out.println("MODEL COEF IND \t" + modelCoefficientIndicator);
//		System.out.println("MODEL COEF  \t" + commandModel[modelCoefficientIndicator]);
//		System.out.println("COMMAND\t" + command);
//		System.out.println("PERCEPTION  \t" + perception);
//		System.out.println("INCRMT  \t" + incrementSign);
		
		
		for(int i=0; i<commandModel.length; i++){
			//LxPlot.getChart("Local Model " + name, ChartType.LINE).add("W"+name.charAt(1)+i, cycles, commandModel[i] );
			
			modelMeans.get("W"+name.charAt(1)+i).add(commandModel[i]);
			//LxPlot.getChart("Local Model Mean" + name, ChartType.LINE).add("W"+name.charAt(1)+i, cycles, modelMeans.get("W"+name.charAt(1)+i).getMean() );
			meanCommandModel[i]= modelMeans.get("W"+name.charAt(1)+i).getMean();
		}
		
		//modelFile.write((double)cycles, commandModel[0], commandModel[1], commandModel[2]); ***********************************************************************************************************
		//meanModelFile.write((double)cycles, modelMeans.get("W"+name.charAt(1)+0).getMean(), modelMeans.get("W"+name.charAt(1)+1).getMean(), modelMeans.get("W"+name.charAt(1)+2).getMean());
		
	}
	
	private int getIncrementSign(Double perception, Double command, int modelCoefficientIndicator){
		
		if(command > (perception*commandModel[modelCoefficientIndicator])){
			if(command>0){
				if((commandModel[modelCoefficientIndicator]>=0) && (perception>0)){
					return 1;
				}else if((commandModel[modelCoefficientIndicator]<0) && (perception<0)){
					return -1;
				}else if((commandModel[modelCoefficientIndicator]<0) && (perception>0)){
					return 1;
				}else if((commandModel[modelCoefficientIndicator]>0) && (perception<0)){
					return -1;
				}
				
			}else if(command<0){
				if((commandModel[modelCoefficientIndicator]<0) && (perception>0)){
					return 1;
				}else if((commandModel[modelCoefficientIndicator]>0) && (perception<0)){
					return -1;
				}
			}
			
		}else if(command < (perception*commandModel[modelCoefficientIndicator])){
			if(command>0){
				if((commandModel[modelCoefficientIndicator]>0) && (perception>0)){
					return -1;
				}else if((commandModel[modelCoefficientIndicator]<0) && (perception<0)){
					return 1;
				}
			}else if(command<0){
				if((commandModel[modelCoefficientIndicator]>0) && (perception>0)){
					return -1;
				}else if((commandModel[modelCoefficientIndicator]<0) && (perception<0)){
					return 1;
				}else if((commandModel[modelCoefficientIndicator]<=0) && (perception>0)){
					return -1;
				}else if((commandModel[modelCoefficientIndicator]>=0) && (perception<0)){
					return 1;
				}
			}
		}
		return 0;
	}
	
	public Double[] getLocalModel(){
		return commandModel;
	}
	
	public Double[] getMeanLocalModel(){
		return meanCommandModel;
	}

	public void stopWriting(){
		modelFile.close();
		meanCommandModel.clone();
		criticityFile.close();
	}
}
