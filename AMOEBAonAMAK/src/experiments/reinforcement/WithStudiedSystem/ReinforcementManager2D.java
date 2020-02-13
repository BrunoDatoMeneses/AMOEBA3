package experiments.reinforcement.WithStudiedSystem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import agents.percept.Percept;
import kernel.AMOEBA;
import kernel.StudiedSystem;


/**
 * The Class BadContextManager.
 */
public class ReinforcementManager2D implements StudiedSystem{

	/** The x. */
	Double[] x ;
	Double[] oldX ;
	
	double a1;
	double a2;
	
	public boolean AI = false;
	
	/** The result. */
	double result = 0;
	
	HashMap<String, AMOEBA> amoebas;

	
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
	
	double noiseRange;
	
	/** The world. */
	Random generator;
	
	double explorationIncrement;
	double explorationMaxVariation;
	
	
	/* Parameters */
	private static final double gaussianCoef = 1000;
	private static final double gaussianVariance = 10;
	
	private double lastReward;
	
	
	public ReinforcementManager2D(double size, int dim, int nbOfModels, int nrmType, boolean rndExploration, double explIncrement, double explnVariation, boolean limiteToSpace, double noise) {
		this.spaceSize= size;
		dimension = dim;
		numberOfModels = nbOfModels;
		normType = nrmType;
		x = new Double[dimension];
		oldX = new Double[dimension];
		
		noiseRange = noise;
		spaceLimited = limiteToSpace;
		
		//gaussianCoef = Math.random()*2000;
		

		
		
		
		generator = new Random();
		
		
		
		
		
		
		
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
		
		resetRandomExploration();
	}
	
	
	
	
	
	
	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#playOneStep(double)
	 */
	@Override
	public HashMap<String, Double> playOneStep() {
		
		HashMap<String, Double> state = new HashMap<String, Double>();

		if(!randomExploration) {
			
			pseudoRandomExplorationStep();
			
		}
		else if(activeLearning) {
			
			
			
			activeLearning = false;
			
			
			
			for(int i = 0 ; i < dimension ; i++) {
				x[i] = selfRequest.get("px" + i);
			}
		}

		else {
			
			resetRandomExploration();
		}
		
		if(oldX[0] != null) {
			for(int i = 1 ; i < dimension+1 ; i++) {
				state.put("p" + i + "Goal", x[i-1]);
				state.put("p" + i, oldX[i-1]);
			}
			state.put("a1" , a1);
			state.put("a2" , a2);
		}
		
		
		return state;
	}
	
	@Override
	public HashMap<String, Double> playOneStepWithControlModel() {
		
		HashMap<String, Double> state = new HashMap<String, Double>();

		if(!randomExploration) {
			
			pseudoRandomExplorationStep();
			
		}
		else if(activeLearning) {
			
			
			
			activeLearning = false;
			
			
			
			for(int i = 0 ; i < dimension ; i++) {
				x[i] = selfRequest.get("px" + i);
			}
		}

		else {
			
			resetRandomExploration();
		}
		
		if(oldX[0] != null) {
			for(int i = 1 ; i < dimension+1 ; i++) {
				state.put("p" + i + "Goal", x[i-1]);
				state.put("p" + i, oldX[i-1]);
			}
			state.put("a1" , a1);
			state.put("a2" , a2);
		}
		
		
		return state;
	}
	
	private void resetRandomExploration() {
		for(int i = 0 ; i < dimension ; i++) {
			x[i] = (double) (generator.nextInt((int) (spaceSize*2+5)) - (spaceSize+2));
		}
		oldX = new Double[dimension];
	}
	
	
	private void pseudoRandomExplorationStep() {
		
		if(x[0] != null) {
			for(int i = 0 ; i < dimension ; i++) {
				oldX[i] = x[i];
			}
		}
		
		
		if(lastReward == -1000.0 || lastReward == 1000.0) {
			resetRandomExploration();
		}else {	
			HashMap<String, Double> perceptions = new HashMap<String, Double>();
			perceptions.put("px0", x[0]);
			perceptions.put("px1", x[1]);
			perceptions.put("oracle", 0.0);
			HashMap<String, Double> bestFuturePosition = amoebas.get("spatialReward").reinforcementRequest(perceptions);
			System.out.println(bestFuturePosition);
			
			if(bestFuturePosition != null && AI) {
				HashMap<String, Double> a1Request = new HashMap<String, Double>();
				a1Request.put("p1", x[0]);
				a1Request.put("p2", x[1]);
				a1Request.put("p1Goal", bestFuturePosition.get("px0"));
				a1Request.put("oracle", 0.0);
				a1 = amoebas.get("a1").request(a1Request);
				HashMap<String, Double> a2Request = new HashMap<String, Double>();
				a2Request.put("p1", x[0]);
				a2Request.put("p2", x[1]);
				a2Request.put("p2Goal", bestFuturePosition.get("px1"));
				a2Request.put("oracle", 0.0);
				a2 = amoebas.get("a2").request(a2Request);
			}
			else {
				a1 = generator.nextInt(3) - 1;
				a2 = (a1 == 0.0) ? (generator.nextBoolean() ? -1 : 1) : (generator.nextInt(3) - 1);
			}
			
			x[0] += a1;
			x[1] += a2;
		}
 
		
		HashMap<String, Double> perceptionsActionState1 = new HashMap<String, Double>();
		HashMap<String, Double> perceptionsActionState2 = new HashMap<String, Double>();
		
		
		if(oldX[0] != null) {
			perceptionsActionState1.put("p1", oldX[0]);
			perceptionsActionState1.put("p2", oldX[1]);
			perceptionsActionState1.put("p1Goal", x[0]);
			perceptionsActionState1.put("oracle", a1);
			
			
			perceptionsActionState2.put("p1", oldX[0]);
			perceptionsActionState2.put("p2", oldX[1]);
			perceptionsActionState2.put("p2Goal", x[1]);
			perceptionsActionState2.put("oracle", a2);
			
			System.out.println(perceptionsActionState1);
			System.out.println(perceptionsActionState2);
			
			amoebas.get("a1").learn(perceptionsActionState1);
			amoebas.get("a2").learn(perceptionsActionState2);
		}
		
		
	}
	
	private double normeP(double[] x1, int p) {
		double distance = 0;
		for(int i = 0; i < x1.length; i ++) {
			distance += Math.pow(Math.abs(x1[i]), p) ;
		}
		return Math.pow(distance, 1.0/p);
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

	



	
	

	
	public double model(Double[] situation) {
		
		Double[] xRequest;
		
		if(situation == null) {
			xRequest = x;
		}else {
			xRequest = situation;
		}
		
		
		
		return reinforcementModel2D(xRequest);
		
		

		
		
	}
	
	
	public double reinforcementModel2D(Double[] position) {
		
		double reward;
		if(position[0] < -spaceSize || position[0] > spaceSize || position[1] < -spaceSize || position[1] > spaceSize) {
			reward = -1000.0;
		} else if(Math.abs(position[0]) < 1.5 && Math.abs(position[1]) < 1.5 ) {
			// win !
			reward = 1000.0;
		} else {
			reward = -1.0;
		}
		
		lastReward = reward;
		return reward;
		
		

		
		
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
		out.put("oracle",result);
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
	public void setSelfRequest(HashMap<Percept, Double> request){
		HashMap<String,Double> newRequest = new HashMap<String,Double>();
		
		for(Percept pct : request.keySet()) {
			newRequest.put(pct.getName(), request.get(pct));
		}
		
		selfRequest = newRequest;
	}

	@Override
	public void setControlModels(HashMap<String, AMOEBA> controlModels) {
		amoebas = controlModels;
		
	}






	@Override
	public void setControl(boolean value) {
		AI = value;
		
	}






	@Override
	public void setSelfLearning(boolean value) {
		// TODO Auto-generated method stub
		
	}






	@Override
	public Double getActiveRequestCounts() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public Double getSelfRequestCounts() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public Double getRandomRequestCounts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getErrorOnRequest(AMOEBA amoeba) {
		return 0;
	}


}