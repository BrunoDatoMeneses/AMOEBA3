package experiments.nDimensionsLaunchers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import agents.percept.Percept;
import kernel.AMOEBA;
import kernel.StudiedSystem;


/**
 * The Class BadContextManager.
 */
public class F_N_Manager implements StudiedSystem{

	/** The x. */
	Double[] x ;
	
	
	/** The result. */
	double result = 0;
	
	int[] modelCoefs1;
	int[] modelCoefs2;
	
	int[][] modelCoefs;
	double[][] modelCenterZones;
	
	/** The first step. */
	boolean firstStep = true;
	boolean randomExploration = false;
	boolean spaceLimited = true;
	double spaceSize;
	
	int dimension;
	int numberOfModels;
	int normType;
	
	double[] explorationVector;
	
	HashMap<String,Double> selfRequest;
	boolean activeLearning = false;
	boolean selfLearning = false;
	
	double noiseRange;
	
	/** The world. */
	Random generator;
	
	double explorationIncrement;
	double explorationMaxVariation;
	
	private Double activeRequestCounts = 0.0;
	private Double selfRequestCounts = 0.0;
	private Double randomRequestCounts = 0.0;
	
	public Double getActiveRequestCounts() {
		return activeRequestCounts;
	}




	public Double getSelfRequestCounts() {
		return selfRequestCounts;
	}




	public Double getRandomRequestCounts() {
		return randomRequestCounts;
	}




	/* Parameters */
	private static final double gaussianCoef = 1000;
	private static final double gaussianVariance = 10;
	
	
	public F_N_Manager(double size, int dim, int nbOfModels, int nrmType, boolean rndExploration, double explIncrement, double explnVariation, boolean limiteToSpace, double noise) {
		this.spaceSize= size;
		dimension = dim;
		numberOfModels = nbOfModels;
		normType = nrmType;
		x = new Double[dimension];
		
		noiseRange = noise;
		spaceLimited = limiteToSpace;
		
		//gaussianCoef = Math.random()*2000;
		
		modelCoefs = new int[nbOfModels][dim+1];
		modelCenterZones = new double[nbOfModels][dim];
		
		
		for(int nb = 0; nb<nbOfModels; nb++) {
			for(int i = 0; i<dimension; i++) {
				x[i] = 0.0;
				
				modelCoefs[nb][i] = (int) (Math.random() * 500 - 255);
				modelCenterZones[nb][i] = spaceSize +  (Math.random() - 0.5) * spaceSize * 2;
			}
			modelCoefs[nb][dimension] = (int) (Math.random() * 500 - 255);


		}
		
		modelCoefs1 = new int[dim+1];
		modelCoefs2 = new int[dim+1];
		for(int i = 0; i<dimension; i++) {
			x[i] = 0.0;
			
			modelCoefs1[i] = (int) (Math.random() * 500 - 255);
			modelCoefs2[i] = (int) (Math.random() * 500 - 255);
		}
		modelCoefs1[dimension] = (int) (Math.random() * 500 - 255);
		modelCoefs2[dimension] = (int) (Math.random() * 500 - 255);
		
		
		//printModels(nbOfModels);
		
		
		
		randomExploration= rndExploration;
		
		explorationVector = new double[dimension];	
		for(int i = 0 ; i < dimension ; i++) {
			explorationVector[i] = Math.random() - 0.5;
		}
		double vectorNorm = normeP(explorationVector, 2);
		for(int i = 0 ; i < dimension ; i++) {
			explorationVector[i] /= vectorNorm;
		}
		
		
		explorationIncrement = explIncrement;
		explorationMaxVariation = explnVariation;
	}


	private void printModels(int nbOfModels) {
		System.out.println("ZONE 1 DISKS");
		for(int nb = 0; nb<nbOfModels; nb++) {
			System.out.print(modelCoefs[nb][dimension] + "\t");
			for(int i =0;i<dimension;i++) {
				System.out.print(modelCoefs[nb][i] + "\t");
			}
			System.out.println("");
		}
		System.out.println("");
		
		System.out.println("ZONE 2 MULTIDIM GAUSSIAN");
		System.out.println(gaussianCoef + " * exp( - ( x +" + spaceSize + ")² / 2 *" + gaussianVariance + "² )");
		System.out.println("");
		
		System.out.println("ZONE 3 SQUARE");
		System.out.print(modelCoefs1[dimension] + "\t");
		for(int i =0;i<dimension;i++) {
			System.out.print(modelCoefs1[i] + "\t");
		}
		System.out.println("");
		System.out.print(modelCoefs2[dimension] + "\t");
		for(int i =0;i<dimension;i++) {
			System.out.print(modelCoefs2[i] + "\t");
		}
		System.out.println("");
		System.out.println("");
	}
	
	
	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#playOneStep(double)
	 */
	@Override
	public HashMap<String, Double> playOneStep() {
		

		if(!randomExploration) {
			
			nonRandomExplorationStep();
			
		}
		else if(selfLearning) {
					
			for(int i = 0 ; i < dimension ; i++) {
				x[i] = selfRequest.get("px" + i);
			}
			selfRequestCounts++;
		}
		else if(activeLearning) {
			
			for(int i = 0 ; i < dimension ; i++) {
				x[i] = selfRequest.get("px" + i);
			}
			activeRequestCounts ++;
		}

		else {
			if (generator == null)	generator = new Random(29);
			
			for(int i = 0 ; i < dimension ; i++) {
				x[i] = (generator.nextDouble() - 0.5) * spaceSize * 4;
			}
			randomRequestCounts++;
		}
		
		//System.out.println("[PLAY ONE STEP] " + "selfLearning " + selfLearning + " activeLearning " + activeLearning);
		
		return null;
	}
	
	
	private void nonRandomExplorationStep() {
		
		
		for(int i = 0 ; i < dimension ; i++) {
			explorationVector[i] += Math.random()*explorationMaxVariation - (explorationMaxVariation/2);
		}
		double vectorNorm = normeP(explorationVector, 2);
		for(int i = 0 ; i < dimension ; i++) {
			explorationVector[i] /= vectorNorm;
		}
		
		for(int i = 0 ; i < dimension ; i++) {
			x[i] += explorationIncrement*explorationVector[i];
			if(spaceLimited) {
				if(x[i]>2*spaceSize) x[i]= -2*spaceSize;
				if(x[i]<-2*spaceSize) x[i]= 2*spaceSize;
			}
			
		}

		
	}
	
	public void playOneStepConstrained(double[][] constrains) {		
		
		for(int i = 0 ; i < dimension ; i++) {
			x[i] = constrains[i][0] + (Math.random()*(constrains[i][1] - constrains[i][0]));
		}
	}
	
	public void playOneStepConstrainedWithNoise(double[][] constrains, double noiseRange) {
		
		
		for(int i = 0 ; i < dimension ; i++) {
			x[i] = constrains[i][0] + (Math.random()*(constrains[i][1] - constrains[i][0])) - noiseRange/2 + Math.random()*noiseRange;
		}

	}

	
//	public double model() {
//		
//		
//		
//		/* Disc */
//		//return (x[1]*x[1] + x[0]*x[0] < spaceSize*spaceSize ) ? model1() :  model2();
//		
//		/* Square */
//		//return (x[1] > -spaceSize && x[1] < spaceSize && x[0] < spaceSize && x[0] > -spaceSize) ? model1() : model2() ;
//		//return model1();
//		
//		/* Triangle */
//		//return (x[1] > x[0]) ? model1() : model2();
//		
//		/* Split */
//		//return ( x <= 0 ) ? 2*x + y : 5*x - 8*y;
//		
//		/* Exp */
//		//return (x[1] > 100*Math.exp(-(Math.pow(x[0]/25, 2))/2) -50) ? model1() : model2();
//		
//		
//		/* Cercle */
////		double rho = Math.sqrt(x[1]*x[1] + x[0]*x[0]);
////		double start = 50.0;
////		double width = 25.0;
////		return ( (start  < rho) && (rho < start + width)) ? model1() : model2();
//		
//		
//		/* Disques */
//		return modelN();
//		
//		
//		/* Gaussian model */
//		//return gaussianModel();
//	}


	
	

	
	public double model(Double[] situation) {
		
		Double[] xRequest;
		
		if(situation == null) {
			xRequest = x;
		}else {
			xRequest = situation;
		}
		
		double[] center =  new double[2];
		center[0]=0.0;
		center[1]=0.0;
		//return gaussianModel(xRequest, center,gaussianCoef, gaussianVariance);
		//return squareSimpleModel(xRequest);
		
		int subzone = subzone2D(xRequest);
		
		/* Multi */
//		return multiModel(xRequest, subzone);
		
		
		/* Disc */
//		return (xRequest[0]*xRequest[0] + xRequest[1]*xRequest[1] < spaceSize*spaceSize ) ? model1(xRequest[0],xRequest[1]) : model2(xRequest[0],xRequest[1]);
		
		/* Square */
//		return (xRequest[0] > -spaceSize && xRequest[0] < spaceSize && xRequest[1] < spaceSize && xRequest[1] > -spaceSize) ? model1(xRequest[0],xRequest[1]) : model2(xRequest[0],xRequest[1]) ;

		/* Square artcile JFSMA 2020*/
		return (xRequest[0] > -spaceSize && xRequest[0] < spaceSize && xRequest[1] < spaceSize && xRequest[1] > -spaceSize) ? model1JFSMA2020(xRequest[0],xRequest[1]) : model2JFSMA2020(xRequest[0],xRequest[1]) ;
		
		/* Triangle */
		//return (xRequest[0] > xRequest[1]) ? model1(xRequest[0],xRequest[1]) : model2(xRequest[0],xRequest[1]);
		
		/* Split */
		//return ( xRequest[0] <= 0 ) ? model1(xRequest[0],xRequest[1]) : model2(xRequest[0],xRequest[1]);
		
		
		
		/* Cercle */
//		double rho = Math.sqrt(x1*x1 + x0*x0);
//		double start = 50.0;
//		double width = 25.0;
//		return ( (start  < rho) && (rho < start + width)) ? model1() : model2();
		
		
		
		
		
	}


	private double multiModel(Double[] xRequest, int subzone) {
		if(subzone == 1) {
			/* Disques */
			return modelN(xRequest) ;
		}else if (subzone == 2) {
			/* Gaussian model */
			return gaussianModel(xRequest, subZoneCenter3D(2), gaussianCoef, gaussianVariance);
			
		}else if (subzone == 3) {
			/* Square */
			return square2DModel(xRequest, subZoneCenter3D(3));
			
		}else if (subzone == 4) {
			/* Exp */
			return gaussianMapping2D(xRequest);
		}
		
		return model1();
	}
	
	
	private int subzone2D(Double[] situation) {
		
		Double[] xRequest;
		
		if(situation == null) {
			xRequest = x;
		}else {
			xRequest = situation;
		}	
		if(xRequest[0] > 0 && xRequest[1] > 0) {
			return 1;
		}else if(xRequest[0] < 0 && xRequest[1] < 0){
			return 2;
		}else if(xRequest[0] < 0 && xRequest[1] > 0){
			return 3;
		}else if(xRequest[0] > 0 && xRequest[1] < 0){
			return 4;
		}else {
			return 0;
		}
			
		
		
	}
	
	private double[] subZoneCenter2D(int nb) {
		
		double[] center =  new double[2];
		
		if(nb == 1) {
			center[0] = spaceSize;
			center[1] = spaceSize;
		}else if(nb == 2) {
			center[0] = -spaceSize;
			center[1] = -spaceSize;
		}
		else if(nb == 3) {
			center[0] = -spaceSize;
			center[1] = spaceSize;
		}
		else if(nb == 4) {
			center[0] = spaceSize;
			center[1] = -spaceSize;
		}
		
		return center;
		
	}
	
private double[] subZoneCenter3D(int nb) {
		
		double[] center =  new double[3];
		
		if(nb == 1) {
			center[0] = spaceSize;
			center[1] = spaceSize;
			center[2] = 0.0;
		}else if(nb == 2) {
			center[0] = -spaceSize;
			center[1] = -spaceSize;
			center[2] = 0.0;
		}
		else if(nb == 3) {
			center[0] = -spaceSize;
			center[1] = spaceSize;
			center[2] = 0.0;
		}
		else if(nb == 4) {
			center[0] = spaceSize;
			center[1] = -spaceSize;
			center[2] = 0.0;
		}
		
		return center;
		
	}
	
	private double gaussianMapping2D(Double[] xRequest) {
		return (xRequest[1] > 30*Math.exp(-(Math.pow((xRequest[0]-spaceSize)/5, 2))/2) -50) ? model1() : model2();
	}
	
	private double square2DModel(Double[] xRequest, double[] center) {
		return ((center[0]-spaceSize/2)  < xRequest[0]  && 
				xRequest[0] < (center[0]+spaceSize/2) &&
				(center[1]-spaceSize/2)  < xRequest[1]  && 
				xRequest[1] < (center[1]+spaceSize/2)) ? model1(xRequest[0],xRequest[1]) : model2(xRequest[0],xRequest[1]) ;
	}
	
	private double squareSimpleModel(Double[] xRequest) {
		return ((-spaceSize)  < xRequest[0]  && 
				xRequest[0] < (spaceSize) &&
				(-spaceSize)  < xRequest[1]  && 
				xRequest[1] < (+spaceSize)) ? model1(xRequest[0],xRequest[1]) : model2(xRequest[0],xRequest[1]) ;
	}
	
	private double gaussianModel() {
		double result = 1.0;
		for(int i=0;i<dimension;i++) {
			result *= Math.exp(-(Math.pow(x[i]/gaussianVariance, 2))/2);
		}
		return gaussianCoef*result;
	}
	
	private double gaussianModel(Double[] xRequest, double[] center, double factor, double variance) {
		double result = 1.0;
		for(int i=0;i<dimension;i++) {
			result *= Math.exp(-(Math.pow((xRequest[i] - center[i])/variance, 2))/2);
		}
		return factor*result;
	}
	
	private double modelN() {
		
		for(int nb = 0; nb<numberOfModels-1; nb++) {
			
			if(distance(x,modelCenterZones[nb]) < spaceSize) {
				return modeli(nb);
			}
			
		}
		return modeli(numberOfModels-1);
		
	}
	
	private double modelN(Double[] xRequest) {
		
		for(int nb = 0; nb<numberOfModels-1; nb++) {
			
			if(distance(xRequest,modelCenterZones[nb]) < spaceSize*0.75) {
				return modeli(nb, xRequest);
			}
			
		}
		return modeli(numberOfModels-1, xRequest);
		
	}
	
	private double distance(Double[] x1, double[] x2) {
		return normeP(x1,x2,normType);
	}
	
	private double norme1(double[] x1, double[] x2) {
		double distance = 0;
		for(int i = 0; i < x1.length; i ++) {
			distance += Math.abs(x2[i] - x1[i]) ;
		}
		return distance;
	}
	
	
	
	private double normeP(Double[] x1, double[] x2, int p) {
		double distance = 0;
		for(int i = 0; i < x1.length; i ++) {
			distance += Math.pow(Math.abs(x2[i] - x1[i]), p) ;
		}
		return Math.pow(distance, 1.0/p);
	}
	
	private double normeP(double[] x1, int p) {
		double distance = 0;
		for(int i = 0; i < x1.length; i ++) {
			distance += Math.pow(Math.abs(x1[i]), p) ;
		}
		return Math.pow(distance, 1.0/p);
	}
	
	private double norme2(double[] x1, double[] x2) {
		double distance = 0;
		for(int i = 0; i < x1.length; i ++) {
			distance += Math.pow(x2[i] - x1[i], 2) ;
		}
		return Math.sqrt(distance);
	}
	
	private double modeli(int modelNb) {
		double result = 0.0;
		for(int i = 0; i<dimension;i++) {
			result += x[i]*modelCoefs[modelNb][i];
		}
		result += modelCoefs[modelNb][dimension];
		return result;		
	}
	
	private double modeli(int modelNb, Double[] xRequest) {
		double result = 0.0;
		for(int i = 0; i<dimension;i++) {
			result += xRequest[i]*modelCoefs[modelNb][i];
		}
		result += modelCoefs[modelNb][dimension];
		return result;		
	}
	
	public double model1() {
		double result = 0.0;
		for(int i = 0; i<dimension;i++) {
			result += x[i]*modelCoefs1[i];
		}
		result += modelCoefs1[dimension];
		return result;		
	}
	
	public double model2() {
		double result = 0.0;
		for(int i = 0; i<dimension;i++) {
			result += x[i]*modelCoefs2[i];
		}
		result += modelCoefs2[dimension];
		return result;		
	}
	
	public double model1(double x0, double x1) {
		double result = 0.0;
		result += x0*modelCoefs1[0];
		result += x1*modelCoefs1[1];
		result += modelCoefs1[dimension];
		return result;		
	}

	public double model1JFSMA2020(double x0, double x1) {
		double result = 0.0;
		result += x0*0;
		result += x1*150;
		result += 0;
		return result;
	}

	public double model2JFSMA2020(double x0, double x1) {
		double result = 0.0;
		result += x0*150;
		result += x1*0;
		result += 0;
		return result;
	}
	
	public double model2(double x0, double x1) {
		double result = 0.0;
		result += x0*modelCoefs2[0];
		result += x1*modelCoefs2[1];
		result += modelCoefs2[dimension];
		return result;		
	}
	
	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#getOutput()
	 */

	public HashMap<String, Double> getOutput() {
		HashMap<String, Double> out = new HashMap<String, Double>();

		result = model(null);
		
		for(int i = 0; i<dimension; i++) {
			
			out.put("px" + i,x[i]);
			
		}
		if(selfLearning) {
			selfLearning = false;
			out.put("oracle",null);
		}else {
			out.put("oracle",result);
		}
		if(activeLearning) {
			activeLearning=false;
		}
		//out.put("oracle",result);
		//System.out.println("[GET OUTPUT] " +out);
		
		return out;
	}
	
	public HashMap<String, Double> getIntput() {
		HashMap<String, Double> in = new HashMap<String, Double>();

		
		for(int i = 0; i<dimension; i++) {
			
			in.put("px" + i,x[i]);
			
		}
		return in;
	}
	
	public HashMap<String, Double> getOutputWithNoise(double noiseRange) {
		HashMap<String, Double> out = new HashMap<String, Double>();

		result = model(null) - noiseRange/2 + Math.random()*noiseRange ;
		
		for(int i = 0; i<dimension; i++) {
			
			out.put("px" + i,x[i]);
			
		}
		out.put("oracle",result);
		return out;
	}
	
	public HashMap<String, Double> getOutputWithAmoebaRequest(HashMap<String, Double> amoebaRequest,  double noiseRange) {
		HashMap<String, Double> out = new HashMap<String, Double>();

		for(int i = 0; i<dimension; i++) {
			
			x[i] = amoebaRequest.get("px" + i);
			
		}
		
		result = model(null) - noiseRange/2 + Math.random()*noiseRange ;
		
		for(int i = 0; i<dimension; i++) {
			
			out.put("px" + i,x[i]);
			
		}
		out.put("oracle",result);
		return out;
	}
	
	public HashMap<String, Double> getOriginOutput() {
		HashMap<String, Double> out = new HashMap<String, Double>();

		for(int i = 0; i<dimension; i++) {
			x[i] = 0.0;
			
		}
		
		result = model(null);
		
		for(int i = 0; i<dimension; i++) {
			
			out.put("px" + i,x[i]);
			
		}
		out.put("oracle",result);
		return out;
	}
	
	
	
	
	public HashMap<String, Double> getOutputRequest2D(HashMap<String, Double> values) {
		HashMap<String, Double> out = new HashMap<String, Double>();

		x[0] = values.get("px0");
		x[1] = values.get("px1");
		
		
		result =  model(null);
		
		out.put("px0",x[0]);
		out.put("px1",x[1]);
		out.put("oracle",result);
		return out;
	}

	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#switchControlMode()
	 */

	public void switchControlMode() {
		
	}
	

	public double getSpaceSize() {
		return spaceSize;
	}


	
	


	@Override
	public double requestOracle(HashMap<String, Double> request) {
		
		Double[] xRequest = new Double[request.size()];
		
		for(int i = 0; i<dimension; i++) {
			
			xRequest[i] = request.get("px" + i);
			
		}
		
		return model(xRequest);
	}
	
	@Override
	public void setActiveLearning(boolean value) {
		activeLearning = value;
	}
	
	@Override
	public void setSelfLearning(boolean value) {
		selfLearning = value;
	}
	
	@Override
	public void setSelfRequest(HashMap<Percept, Double> request){
		HashMap<String,Double> newRequest = new HashMap<String,Double>();
		
		//System.out.println("[SET SELF REQUEST] " +request);
		
		for(Percept pct : request.keySet()) {
			newRequest.put(pct.getName(), request.get(pct));
		}
		
		selfRequest = newRequest;
	}


	@Override
	public HashMap<String, Double> playOneStepWithControlModel() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setControlModels(HashMap<String, AMOEBA> controlModels) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setControl(boolean value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getErrorOnRequest(AMOEBA amoeba){

		if (generator == null)	generator = new Random(29);
		for(int i = 0 ; i < dimension ; i++) {
			x[i] = (generator.nextDouble() - 0.5) * spaceSize * 4;
		}
		HashMap<String, Double> out = new HashMap<String, Double>();
		double oracleValue = model(null);
		for(int i = 0; i<dimension; i++) {

			out.put("px" + i,x[i]);

		}
		out.put("oracle",null);

		double prediction = amoeba.request(out);
		double error = Math.abs(oracleValue-prediction)/Math.abs(oracleValue);
		//System.out.println(oracleValue + " " + prediction + " " + error);

		return error;

	}



}