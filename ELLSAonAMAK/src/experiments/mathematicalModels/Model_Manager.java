package experiments.mathematicalModels;

import java.util.HashMap;


import agents.percept.Percept;
import kernel.ELLSA;
import kernel.StudiedSystem;
import utils.RAND_REPEATABLE;


public class Model_Manager implements StudiedSystem{


	Double[] x ;
	Double[] xEploration ;

	public int cycle;
	public String model;
	

	double result = 0;
	
	int[] modelCoefs1;
	int[] modelCoefs2;
	
	int[][] modelCoefs;
	double[][] modelCenterZones;
	

	boolean randomExploration;
	boolean spaceLimited;
	public double spaceSize;
	
	public int dimension;
	int numberOfModels;
	int normType;
	
	double[] explorationVector;
	
	HashMap<String,Double> selfRequest;
	public boolean activeLearning = false;
	public boolean selfLearning = false;
	
	double noiseRange;
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


	/* Gaussian model setting */
	private static final double gaussianCoef = 10000;
	private static final double gaussianVariance = 15;
	
	
	public Model_Manager(double size, int dim, int nbOfModels, int nrmType, boolean rndExploration, double explIncrement, double explnVariation, boolean limiteToSpace, double noise) {
		this.spaceSize= size;
		dimension = dim;
		numberOfModels = nbOfModels;
		normType = nrmType;
		x = new Double[dimension];
		xEploration = new Double[dimension];
		
		noiseRange = noise;
		spaceLimited = limiteToSpace;

		
		modelCoefs = new int[nbOfModels][dim+1];
		modelCenterZones = new double[nbOfModels][dim];
		
		
		for(int nb = 0; nb<nbOfModels; nb++) {
			for(int i = 0; i<dimension; i++) {
				x[i] = 0.0;
				xEploration[i] = 0.0;
				
				modelCoefs[nb][i] = (int) (RAND_REPEATABLE.random() * 500 - 255);
				modelCenterZones[nb][i] = 75;
			}
			modelCoefs[nb][dimension] = (int) (RAND_REPEATABLE.random() * 500 - 255);


		}
		
		modelCoefs1 = new int[dim+1];
		modelCoefs2 = new int[dim+1];
		for(int i = 0; i<dimension; i++) {
			x[i] = 0.0;
			xEploration[i] = 0.0;
			
			modelCoefs1[i] = (int) (RAND_REPEATABLE.random() * 500 - 255);
			modelCoefs2[i] = (int) (RAND_REPEATABLE.random() * 500 - 255);
		}
		modelCoefs1[dimension] = (int) (RAND_REPEATABLE.random() * 500 - 255);
		modelCoefs2[dimension] = (int) (RAND_REPEATABLE.random() * 500 - 255);

		randomExploration= rndExploration;
		
		explorationVector = new double[dimension];	
		for(int i = 0 ; i < dimension ; i++) {
			explorationVector[i] = RAND_REPEATABLE.random() - 0.5;
		}
		double vectorNorm = normeP(explorationVector, 2);
		for(int i = 0 ; i < dimension ; i++) {
			explorationVector[i] /= vectorNorm;
		}
		
		
		explorationIncrement = explIncrement;
		explorationMaxVariation = explnVariation;
	}



	@Override
	public HashMap<String, Double> playOneStep() {


		if(selfLearning) {

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
		else if(!randomExploration) {

			nonRandomExplorationStep();

		}

		else {

			
			for(int i = 0 ; i < dimension ; i++) {
				x[i] = (RAND_REPEATABLE.random() - 0.5) * spaceSize * 4;
			}
			randomRequestCounts++;


		}

		
		return null;
	}
	
	
	private void nonRandomExplorationStep() {
		
		
		for(int i = 0 ; i < dimension ; i++) {
			explorationVector[i] += RAND_REPEATABLE.random()*explorationMaxVariation - (explorationMaxVariation/2);
		}
		double vectorNorm = normeP(explorationVector, 2);
		for(int i = 0 ; i < dimension ; i++) {
			explorationVector[i] /= vectorNorm;
		}
		
		for(int i = 0 ; i < dimension ; i++) {
			xEploration[i] += explorationIncrement*explorationVector[i];
			if(spaceLimited) {
				if(xEploration[i]>2*spaceSize) xEploration[i]= -2*spaceSize;
				if(xEploration[i]<-2*spaceSize) xEploration[i]= 2*spaceSize;
			}
			x[i] = xEploration[i];
			
		}

	}



	
	public double model(Double[] situation) {

		if(PARAMS.noiseRange >0.0){
			return getModelOutput(situation) + addGaussianNoise();
		}else{
			return getModelOutput(situation);
		}

	}

	public double addGaussianNoise() {
		return ((RAND_REPEATABLE.randomGauss() * Math.pow(PARAMS.noiseRange/2, 1)));
	}

	public double modelWithoutNoise(Double[] situation) {
			return getModelOutput(situation);
	}

	private double getModelOutput(Double[] situation) {
		Double[] xRequest = new Double[dimension];

		if(situation == null) {
			xRequest = x;
		}else {
			for( int i = 0; i<situation.length;i++){
				if(situation[i] == null) xRequest[i] = 0.0;
				else xRequest[i] = situation[i];
			}
			//xRequest = situation;
		}

		double[] center =  new double[dimension];
		center[0]=0.0;
		center[1]=0.0;


		int subzone = subzone2D(xRequest);

		model= PARAMS.model;
		/* Multi */
		if(PARAMS.model.equals("multi")){
			return multiModelFixed(xRequest, subzone);
		}

		/* LINEAR */

		/* Losange */
		if(PARAMS.model.equals("los")){
			return distanceToCenterbyNormFixedModel(xRequest,1);
		}

		/* Disc */
		if(PARAMS.model.equals("disc")){
			return circleFixedModel(xRequest);
		}

		/* Square */
		if(PARAMS.model.equals("square")){
			return squareModel(xRequest);
		}

		/* Square artcile JFSMA 2020*/
		if(PARAMS.model.equals("squareFixed")){
			return squareFixedJFSMA(xRequest);

		}

		/* Triangle */
		if(PARAMS.model.equals("triangle")){
			return diagFixedModel(xRequest);
		}

		/* Split */
		if(PARAMS.model.equals("split")){
			return splitModel(xRequest);
		}


		/* Disk */
		if(PARAMS.model.equals("disk")){
			return circleFixedModel(xRequest);
		}

		/* NON LINEAR */
		/* Gaussian */
		if(PARAMS.model.equals("gaussian")){
			return gaussianModel(xRequest, center, 500, 60);
		}

		/* Poly 2 */
		if(PARAMS.model.equals("polynomial")){
			return polynomialModel(xRequest, 2, 50);
		}

		/* Goutte */
		if(PARAMS.model.equals("gaussianCos2")){
			return goutteModel(xRequest, center);
		}

		/* Cos */
		if(PARAMS.model.equals("cosX")){
			return cosX(xRequest, 0.045) + 3;
		}

		/* CosSin */
		if(PARAMS.model.equals("cosSinX")){
			return cosSinX(xRequest, 0.045) + 3;
		}

		/* Rosenbrock */
		if(PARAMS.model.equals("rosenbrock")){
			return rosenbrock2DModel(xRequest, 1, 100, -50, 0.15, 0.0001);
		}

		/* DYNAMICAL */

		if(PARAMS.model.equals("squareSplitTriangle")){
			return squareSplitDiagModel(xRequest, PARAMS.transferCyclesRatio);
		}

		if(PARAMS.model.equals("squareDiagCircle")){
			return squareDiagCirclModel(xRequest, PARAMS.transferCyclesRatio);
		}

		if(PARAMS.model.equals("squareDiscLos")){
			return squareDisclLosModel(xRequest, PARAMS.transferCyclesRatio);
		}


		if(PARAMS.model.equals("squareDiag")){
			return squareDiagModel(xRequest, PARAMS.transferCyclesRatio);
		}

		if(PARAMS.model.equals("squareDisc")){
			return squareDiscModel(xRequest, PARAMS.transferCyclesRatio);
		}


		if(PARAMS.model.equals("squareSplitFixed")){
			return jfsmaDynamicalModel(xRequest, PARAMS.transferCyclesRatio);
		}

		return model1(xRequest[0],xRequest[1]);
	}

	private double squareFixedJFSMA(Double[] xRequest) {
		return (xRequest[0] > -spaceSize && xRequest[0] < spaceSize && xRequest[1] < spaceSize && xRequest[1] > -spaceSize) ? model1JFSMA2020(xRequest[0], xRequest[1]) : model2JFSMA2020(xRequest[0], xRequest[1]);
	}

	private double jfsmaDynamicalModel(Double[] xRequest, double cycleChange) {
		if(cycle<(PARAMS.nbLearningCycle-(2*PARAMS.nbLearningCycle*cycleChange))) {
			return squareFixedJFSMA(xRequest);
		}
		else return (xRequest[0] > xRequest[1]) ? model1JFSMA2020(xRequest[0],xRequest[1]) : model2JFSMA2020(xRequest[0],xRequest[1]);
	}

	private double squareSplitDiagModel(Double[] xRequest, double cycleChange) {
		if(cycle<(PARAMS.nbLearningCycle-(2*PARAMS.nbLearningCycle*cycleChange))) return squareModel(xRequest);
		else if(cycle<(PARAMS.nbLearningCycle-(PARAMS.nbLearningCycle*cycleChange))) return splitModel(xRequest);
		else return diagModel(xRequest);
	}

	private double squareDiagCirclModel(Double[] xRequest, double cycleChange) {
		if(cycle<(PARAMS.nbLearningCycle-(2*PARAMS.nbLearningCycle*cycleChange))) return squareFixedJFSMA(xRequest);
		else if(cycle<(PARAMS.nbLearningCycle-(PARAMS.nbLearningCycle*cycleChange))) return diagFixedModel(xRequest);
		else return circleFixedModel(xRequest);
	}

	private double squareDisclLosModel(Double[] xRequest, double cycleChange) {
		//System.out.println(cycle + " " + (PARAMS.nbLearningCycle) + " " + (cycleChange) + " " + (PARAMS.nbLearningCycle-(2*PARAMS.nbLearningCycle*cycleChange)) +" "+ (PARAMS.nbLearningCycle-(PARAMS.nbLearningCycle*cycleChange)));
		if(cycle<(PARAMS.nbLearningCycle-(2*PARAMS.nbLearningCycle*cycleChange))) return squareFixedJFSMA(xRequest);
		else if(cycle<(PARAMS.nbLearningCycle-(PARAMS.nbLearningCycle*cycleChange))) return circleFixedModel(xRequest);
		else return distanceToCenterbyNormFixedModel(xRequest,1);
	}

	private double squareDiagModel(Double[] xRequest, double cycleChange) {
		if(cycle<(PARAMS.nbLearningCycle-(PARAMS.nbLearningCycle*cycleChange))) return squareFixedJFSMA(xRequest);
		else return diagFixedModel(xRequest);
	}

	private double squareDiscModel(Double[] xRequest, double cycleChange) {
		if(cycle<(PARAMS.nbLearningCycle-(PARAMS.nbLearningCycle*cycleChange))) return squareFixedJFSMA(xRequest);
		else return circleFixedModel(xRequest);
	}

	private double splitModel(Double[] xRequest) {
		return ( xRequest[0] <= 0 ) ? model1(xRequest[0],xRequest[1]) : model2(xRequest[0],xRequest[1]);
	}

	private double circleFixedModel(Double[] xRequest) {
		return ( distanceToCenter(xRequest) < spaceSize*1.25 ) ? model1JFSMA2020(xRequest[0],xRequest[1]) : model2JFSMA2020(xRequest[0],xRequest[1]);
	}

	private double distanceToCenterbyNormFixedModel(Double[] xRequest, int normeCycle) {
		return ( distanceToCenterByNorm(xRequest,normeCycle) < spaceSize*1.25 ) ? model1JFSMA2020(xRequest[0],xRequest[1]) : model2JFSMA2020(xRequest[0],xRequest[1]);
	}


	private double diagModel(Double[] xRequest) {
		return (xRequest[0] > xRequest[1]) ? model1(xRequest[0],xRequest[1]) : model2(xRequest[0],xRequest[1]);
	}

	private double diagFixedModel(Double[] xRequest) {
		return (xRequest[0] > xRequest[1]) ? model1JFSMA2020(xRequest[0],xRequest[1]) : model2JFSMA2020(xRequest[0],xRequest[1]);
	}

	private double squareModel(Double[] xRequest) {
		return (xRequest[0] > -spaceSize && xRequest[0] < spaceSize && xRequest[1] < spaceSize && xRequest[1] > -spaceSize) ? model1(xRequest[0],xRequest[1]) : model2(xRequest[0],xRequest[1]);
	}

	private double multiModelFixed(Double[] xRequest, int subzone) {
		if(subzone == 1) {
			/* Disques */
			return 20000 + modelNFixed(xRequest) ;
		}else if (subzone == 2) {
			/* Gaussian model */
			return 40000 +gaussianModel(xRequest, subZoneCenter3D(2), gaussianCoef, gaussianVariance);

		}else if (subzone == 3) {
			/* Square */
			return 20000 +square2DModelFixed(xRequest, subZoneCenter3D(3));

		}else if (subzone == 4) {
			/* Exp */
			return 20000 +gaussianMapping2DFixed(xRequest);
		}

		return 20000 +model1(xRequest[0],xRequest[1]);
	}
	
	
	private int subzone2D(Double[] situation) {
		
		Double[] xRequest;
		
		if(situation == null) {
			xRequest = x;
		}else {
			xRequest = situation;
		}	
		if(xRequest[0] >= 0 && xRequest[1] >= 0) {
			return 1;
		}else if(xRequest[0] < 0 && xRequest[1] < 0){
			return 2;
		}else if(xRequest[0] < 0 && xRequest[1] >= 0){
			return 3;
		}else if(xRequest[0] >= 0 && xRequest[1] < 0){
			return 4;
		}else {
			return 0;
		}
			
		
		
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

	private double gaussianMapping2DFixed(Double[] xRequest) {
		return (xRequest[1] > 30*Math.exp(-(Math.pow((xRequest[0]-spaceSize)/7, 2))/2) -50) ? model1JFSMA2020(xRequest[0],xRequest[1]) : model2JFSMA2020(xRequest[0],xRequest[1]);
	}



	private double square2DModelFixed(Double[] xRequest, double[] center) {
		return ((center[0]-spaceSize/2)  < xRequest[0]  &&
				xRequest[0] < (center[0]+spaceSize/2) &&
				(center[1]-spaceSize/2)  < xRequest[1]  &&
				xRequest[1] < (center[1]+spaceSize/2)) ? model2JFSMA2020(xRequest[0],xRequest[1]) : model1JFSMA2020(xRequest[0],xRequest[1]) ;
	}

	
	private double gaussianModel(Double[] xRequest, double[] center, double factor, double variance) {
		double result = 1.0;
		for(int i=0;i<dimension;i++) {
			result *= Math.exp(-(Math.pow((xRequest[i] - center[i])/variance, 2))/2);
		}
		return factor*result;
	}

	private double polynomialModel(Double[] xRequest, double power, double division) {
		double result = 0.0;
		for(int i=0;i<dimension;i++) {
			result += Math.pow(xRequest[i],power);
		}
		return result/division;
	}

	private double cosCommeCarreModel(Double[] xRequest, double param) {
		double result = 0.0;
		for(int i=0;i<dimension;i++) {
			result += Math.pow(xRequest[i],2);
		}
		return Math.cos(param*result);
	}

	private double cosX(Double[] xRequest, double param) {
		return Math.cos(xRequest[0]*param);
	}

	private double cosSinX(Double[] xRequest, double param) {
		return xRequest[0]>-12.5 ?   Math.cos(xRequest[0]*param) : Math.sin(xRequest[0]*param);
	}

	private double goutteModel(Double[] xRequest, double[] center) {
		return  gaussianModel(xRequest, center, 200, 50)*cosCommeCarreModel(xRequest, 0.0004) + 200;
	}

	private double rosenbrock2DModel(Double[] xRequest, double a, double b, double yOffset, double xFactor, double scale) {
		double part1 = Math.pow(a-xRequest[0]*xFactor,2);
		double part2 = b* Math.pow(xRequest[1]-yOffset -  Math.pow(xRequest[0]*xFactor,2),2);

		return  scale*(part1 + part2) + 200;
	}


	private double modelNFixed(Double[] xRequest) {

		for(int nb = 0; nb<numberOfModels-1; nb++) {

			if(distance(xRequest,modelCenterZones[nb]) < spaceSize*0.75) {
				return model1JFSMA2020(xRequest[0],xRequest[1]);
			}

		}
		return model2JFSMA2020(xRequest[0],xRequest[1]);

	}
	
	private double distance(Double[] x1, double[] x2) {
		return normeP(x1,x2,normType);
	}

	private double distanceToCenter(Double[] x1) {
		return normePToCenter(x1,normType);
	}


	private double distanceToCenterByNorm(Double[] x1, int normIndice) {
		return normePToCenter(x1,normIndice);
	}

	
	private double normeP(Double[] x1, double[] x2, int p) {
		double distance = 0;
		for(int i = 0; i < x1.length; i ++) {
			distance += Math.pow(Math.abs(x2[i] - x1[i]), p) ;
		}
		return Math.pow(distance, 1.0/p);
	}

	private double normePToCenter(Double[] x1, int p) {
		double distance = 0;
		for(int i = 0; i < x1.length; i ++) {
			distance += Math.pow(Math.abs(x1[i]), p) ;
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
		result += 20000;
		return result;
	}

	public double model2JFSMA2020(double x0, double x1) {
		double result = 0.0;
		result += x0*150;
		result += x1*0;
		result += 20000;
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

		if(PARAMS.noiseRange >0.0){
			for(int i = 0 ; i < dimension ; i++) {

				if(!selfLearning){
					x[i] = x[i] + addGaussianNoise();
				}

			}
		}
		
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

		result = model(null) - noiseRange/2 + RAND_REPEATABLE.random()*noiseRange ;
		
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
		
		result = model(null) - noiseRange/2 + RAND_REPEATABLE.random()*noiseRange ;
		
		for(int i = 0; i<dimension; i++) {
			
			out.put("px" + i,x[i]);
			
		}
		out.put("oracle",result);
		return out;
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
	public void setControlModels(HashMap<String, ELLSA> controlModels) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setControl(boolean value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getErrorOnRequest(ELLSA ellsa){


		for(int i = 0 ; i < dimension ; i++) {
			x[i] = (RAND_REPEATABLE.random() - 0.5) * spaceSize * 4;
		}
		HashMap<String, Double> out = new HashMap<String, Double>();
		double oracleValue = modelWithoutNoise(null);
		for(int i = 0; i<dimension; i++) {

			out.put("px" + i,x[i]);

		}
		out.put("oracle",null);

		double prediction = ellsa.request(out);
		double error = Math.abs(oracleValue-prediction)/Math.abs(ellsa.data.maxPrediction- ellsa.data.minPrediction);


		return error;

	}



}