package agents.context.localModel;

import java.util.*;

import agents.context.Context;
import agents.context.Experiment;
import agents.percept.Percept;
import utils.TRACE_LEVEL;

/**
 * The Class LocalModelMillerRegression.
 */
public class LocalModelMillerRegression extends LocalModel{
	
	/** The n parameters. */
	private int nParameters;
	
	/** The regression. */
	transient private Regression regression;

	private Context context;
	
	/** The coef. */
	private Double[] coefs;
	
	private ArrayList<Experiment> firstExperiments;
	
	public boolean isReinforcement = false;

	public int experiemntNb = 0;

	/**
	 * Instantiates a new local model miller regression.
	 *
	 * @param world the world
	 */
	public LocalModelMillerRegression(Context associatedContext) {
		this.context = associatedContext;
		ArrayList<Percept> var = associatedContext.getAmas().getPercepts();
		this.nParameters = var.size();
		regression = new Regression(nParameters,true);
		firstExperiments = new ArrayList<Experiment>();
		
		isReinforcement = associatedContext.getAmas().isReinforcement();
		initCoefs();
	}
	
	public LocalModelMillerRegression(Context associatedContext, Double[] coefsCopy, List<Experiment> fstExperiments) {
		this.context = associatedContext;
		ArrayList<Percept> var = associatedContext.getAmas().getPercepts();
		this.nParameters = var.size();
		regression = new Regression(nParameters,true);
		coefs = coefsCopy;
		firstExperiments = new ArrayList<Experiment>(fstExperiments);
		initCoefs();
	}

	private void initCoefs() {
		coefs = new Double[context.getAmas().getPercepts().size()+1];
		for(int j = 0; j < coefs.length; j++) {
			coefs[j] = 0.0;

		}
	}

	@Override
	public Context getContext() {
		return context;
	}
	
	@Override
	public void setContext(Context context) {
		this.context = context;
	}
	
	@Override
	public void setCoef(Double[] coef) {
		this.coefs = coef.clone();
	}
	
	@Override
	public Double[] getCoef() {
		return coefs;
	}

	@Override
	public double getProposition() {
			
		ArrayList<Percept> percepts = context.getAmas().getPercepts();
		

			
		double result = coefs[0];

		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			result += coefs[i] * percepts.get(i-1).getValue();

		}
	
		return result;
	}

	public double getPropositionWithouAllPercepts(ArrayList<Percept> allPercepts, ArrayList<Percept> subPercepts) {


		double result = coefs[0];

		if (coefs[0] == Double.NaN) System.exit(0);

		for (int i = 1 ; i < coefs.length ; i++) {

			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;

			if(subPercepts.contains(allPercepts.get(i-1))){
				result += coefs[i] * allPercepts.get(i-1).getValue();
			}else{
				result += coefs[i] * context.getRanges().get(allPercepts.get(i-1)).getCenter();
			}


		}

		return result;
	}
	
	
	
	private double getProposition(Context context, double[] situation) {
		
		double result = coefs[0];

		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			result += coefs[i] * situation[i-1];

		}
	
		return result;
	}

	public double getPropositionFrom2DPerceptions(double[] situation) {

		double result = coefs[0];

		if (coefs[0] == Double.NaN) System.exit(0);

		for (int i = 1 ; i < coefs.length ; i++) {

			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			result += coefs[i] * situation[i-1];

		}

		return result;
	}
	
	@Override
	public double getMaxProposition() {
		
		ArrayList<Percept> percepts = context.getAmas().getPercepts();			
		double result = coefs[0];

		if (coefs[0] == Double.NaN)
			throw new ArithmeticException("First coeficient of model cannot be NaN");
		
		for (int i = 1 ; i < coefs.length ; i++) {
			double coef = coefs[i];
			if (Double.isNaN(coef)) coef = 0.0;
			if(coef>0) {
				result += coef * context.getRanges().get(percepts.get(i-1)).getEnd();
			}
			else {
				result += coef * context.getRanges().get(percepts.get(i-1)).getStart();
			}
		}
	
		return result;
	}
	
	@Override
	public HashMap<String, Double> getMaxWithConstraint(HashMap<String, Double> fixedPercepts){
		ArrayList<Percept> percepts = context.getAmas().getPercepts();
		
		HashMap<String, Double> result = new HashMap<String, Double>();
		result.put("oracle", coefs[0]);

		if (coefs[0] == Double.NaN)
			throw new ArithmeticException("First coeficient of model cannot be NaN");
		
		for (int i = 1 ; i < coefs.length ; i++) {
			double coef = coefs[i];
			if (Double.isNaN(coef)) coef = 0.0;
			double pos;
			Percept p = percepts.get(i-1);
			
			if(fixedPercepts.containsKey(p.getName())) {
				pos = fixedPercepts.get(p.getName());
			} else {
				if(coef>0) {
					pos = context.getRanges().get(p).getEnd();
				}
				else {
					pos = context.getRanges().get(p).getStart();
				}
			}
			
			double value = coef * pos;
			result.put("oracle", result.get("oracle") + value);
			result.put(p.getName(), pos);
		}
		
		return result;
	}
	
	@Override
	public double getMinProposition() {
		
		ArrayList<Percept> percepts = context.getAmas().getPercepts();			
		double result = coefs[0];

		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			if(coefs[i]<0) {
				result += coefs[i] * context.getRanges().get(percepts.get(i-1)).getEnd();
			}
			else {
				result += coefs[i] * context.getRanges().get(percepts.get(i-1)).getStart();
			}
		}
	
		return result;
	}
	
	public double getProposition(Experiment experiment) {
		
		if (coefs[0] == Double.NaN) System.exit(0);
		
		double result = coefs[0];
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			result += coefs[i] * experiment.getValuesAsArray()[i-1];

		}
	
		return result;
			
	}

	
	@Override
	public void updateModel(Experiment newExperiment, double weight) {


		if(context.currentPerceptionsFarEnoughOfCenter()){
			experiemntNb +=1;
			context.getAmas().getEnvironment().trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList(context.getName(),"NEW POINT REGRESSION", "FIRST POINTS :", ""+firstExperiments.size(), "OLD MODEL :", coefsToString())));

			if(isReinforcement) {
				updateModelReinforcement(newExperiment, weight);
			}
			else if(!finishedFirstExperiments()) {
					firstExperiments.add(newExperiment);
					updateModel();

			}
			else {
				//updateModelWithExperimentAndWeight(newExperiment, weight, context.getAmas().data.PARAM_numberOfPointsForRegression_ASUPPRIMER);
				updateModel(new ArrayList<>(Arrays.asList(newExperiment)), weight);


				/*ArrayList<Experiment> experiments = new ArrayList<>();
				experiments.add(newExperiment);
				experiments.add(getSymetricalExperiment(newExperiment));
				updateModel(experiments, weight);*/
			}

			context.getAmas().addSpatiallyAlteredContextForUnityUI(context);
			context.getAmas().getEnvironment().trace(TRACE_LEVEL.INFORM,new ArrayList<String>(Arrays.asList(context.getName(),"NEW POINT REGRESSION", "FIRST POINTS :", ""+firstExperiments.size(), "MODEL :", coefsToString())));

		}else{
			context.getAmas().getEnvironment().trace(TRACE_LEVEL.INFORM,new ArrayList<String>(Arrays.asList(context.getName(),"NEW POINT TO CLOSE TO CONTEXT CENTER", "" + newExperiment)));
		}

	}



	public void updateModel(ArrayList<Experiment> newExperiments, double weight) {



		experiemntNb +=newExperiments.size();
		//context.getAmas().getEnvironment().trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList(context.getName(),"NEW POINT REGRESSION", "FIRST POINTS :", ""+firstExperiments.size(), "OLD MODEL :", coefsToString())));




		context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),"EXPERIMENTS WITH WEIGHT")));

		regression = new Regression(nParameters,true);


		int numberOfArtificialPoints = (int)(newExperiments.size()*(1-weight)/weight) ;
		int factor = 1;
		while(numberOfArtificialPoints + newExperiments.size() < nParameters + 2){
			numberOfArtificialPoints+=numberOfArtificialPoints;
			factor++;
		}

		//ArrayList<Experiment> artificialExperiments = getRandomlyDistributedArtificialExperimentsAsArray(numberOfArtificialPoints);
		ArrayList<Experiment> artificialExperiments = getNormalyDistributedArtificialExperimentsAsArray(numberOfArtificialPoints);


		context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),"ARTIFICIAL" )));

		for (Experiment artificialExperiment : artificialExperiments) {

			context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),""+artificialExperiment.getValuesAsArray(), artificialExperiment.getProposition()+"" )));
			regression.addObservation(artificialExperiment.getValuesAsArray(), artificialExperiment.getProposition());
		}



		context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),"XP")));
		int repeatIndice = 0;
		while(repeatIndice<factor){
			for (Experiment newExperiment : newExperiments) {

				context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(), ""+newExperiment.getValuesAsArray(), newExperiment.getProposition()+"" )));
				regression.addObservation(newExperiment.getValuesAsArray(), newExperiment.getProposition());

			}
			repeatIndice++;
		}


		while (regression.getN() < nParameters + 2) {

			regression.addObservation(artificialExperiments.get(0).getValuesAsArray(), artificialExperiments.get(0).getProposition());

			System.err.println("ADING Observations with neighbors" + regression.getN()); //Should not happen

		}


		updateRegressionAndCoefs();

		//context.getAmas().getEnvironment().regressionPoints = newExperiments.size() + artificialExperiments.size();


		context.getAmas().addSpatiallyAlteredContextForUnityUI(context);



	}

	private void updateRegressionAndCoefs() {
		double[] coef = regression.regress().getParameterEstimates();
		coefs = new Double[coef.length];

		Double maxCoef = Double.NEGATIVE_INFINITY;
		Double minCoef = Double.POSITIVE_INFINITY;
		for (int i = 0; i < coef.length; i++) {
			coefs[i] = coef[i];
			if(Double.isNaN(coefs[i])){
				maxCoef = Math.max(maxCoef,0.0);
				minCoef = Math.min(minCoef,0.0);
			}else{
				maxCoef = Math.max(maxCoef,coefs[i]);
				minCoef = Math.min(minCoef,coefs[i]);
			}


		}
		getContext().getAmas().updateMinAndMaxRegressionCoefs(minCoef,maxCoef, getContext().getNormalizedConfidenceWithParams(0.99,0.001));
	}


	public void updateModelReinforcement(Experiment newExperiment, double weight) {
		
		context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),"REINFORCEMENT")));
		
		
		double weightedNewProposition;
		
		if(coefs != null) {
			weightedNewProposition = (newExperiment.getProposition() * weight) + ((1-weight) * this.getProposition());
			context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),weight+ " " + newExperiment.getProposition(),(1-weight)+  " " + this.getProposition())));
		}
		else {
			weightedNewProposition = newExperiment.getProposition();
			context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(), "NEW CTXT " + newExperiment.getProposition())));
		}
		
		
		regression = new Regression(nParameters,true);
		
		int i = 0;
		while (regression.getN() < nParameters + 2) { // TODO TODO TODO pas comme ça
			
			//context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),i+"", ""+firstExperiments.get(i%firstExperiments.size()).getValuesAsArray(), firstExperiments.get(i%firstExperiments.size()).getOracleProposition()+"" )));
			regression.addObservation(newExperiment.getValuesAsArray(), weightedNewProposition);
			i++;
		}


		updateRegressionAndCoefs();

	}
	
	
	
	
	public void updateModel() {
		
		context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),"FIRST EXPERIMENTS")));
		regression = new Regression(nParameters,true);


		while (regression.getN() < nParameters + 2){
			for (Experiment exp : firstExperiments) {

				regression.addObservation(exp.getValuesAsArray(), exp.getProposition());

			}
		}

		updateRegressionAndCoefs();

	}



	private ArrayList<Experiment> getNormalyDistributedArtificialExperimentsAsArray(int amount){

		ArrayList<Experiment> artificialExperiments = new ArrayList<>();

		for (int i = 0; i < amount;i ++) {

			Experiment exp = new Experiment(context);
			for(Percept pct : context.getAmas().getPercepts()) {

				double rangeCenter = this.context.getRanges().get(pct).getCenter();
				double rangeRadius = this.context.getRanges().get(pct).getRadius();
				java.util.Random r = new java.util.Random();
				double ramdomGaussianPosition = (r.nextGaussian() * Math.pow((getContext().getAmas().data.PARAM_perceptionsGenerationCoefficient *rangeRadius/(getContext().getAmas().data.PARAM_quantileForGenerationOfArtificialPerceptions)),1)) + rangeCenter;
				exp.addDimension(pct,ramdomGaussianPosition);
			}
			exp.setProposition(this.getProposition(exp));
			artificialExperiments.add(exp);
		}

		return artificialExperiments;
	}
	
	@Override
	public double distance(Experiment experiment) {
		
		if (coefs[0] == Double.NaN) System.exit(0);
		double distanceToTheModel = -coefs[0];
		double normOfOthogonalVectorToThePlane = 0.0;
		
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			distanceToTheModel += -coefs[i] * experiment.getValuesAsArray()[i-1];
			normOfOthogonalVectorToThePlane += coefs[i]*coefs[i];

		}
		distanceToTheModel += experiment.getProposition();
		normOfOthogonalVectorToThePlane += 1;
		
		distanceToTheModel = Math.abs(distanceToTheModel);
		normOfOthogonalVectorToThePlane = Math.sqrt(normOfOthogonalVectorToThePlane);
	
		distanceToTheModel /= normOfOthogonalVectorToThePlane;
		
		return distanceToTheModel;
			
	}
	
	@Override
	public ArrayList<Experiment> getFirstExperiments() {
		return firstExperiments;
	}
	
	@Override
	public void setFirstExperiments( ArrayList<Experiment> frstExp) {
		firstExperiments = frstExp;
	}
	
	@Override
	public String coefsToString() {
		String coefsString = "";
		if(coefs != null) {
			for(int i=0; i<coefs.length; i ++) {
				coefsString += coefs[i]  + "\t";
			}
		}
		return coefsString;
	}
	
	@Override
	public boolean finishedFirstExperiments() {
		return firstExperiments.size()>= (nParameters + 3);
	}

	@Override
	public TypeLocalModel getType() {
		return TypeLocalModel.MILLER_REGRESSION;
	}

	@Override
	public void setType(TypeLocalModel type) {
	}

	@Override
	public double getModelDifference(LocalModel otherModel) { //TODO différente max ?
		Double difference = Double.NEGATIVE_INFINITY;
		double currentDifference;

		for(int i=0;i<coefs.length;i++){
			//getContext().getEnvironment().print(TRACE_LEVEL.DEBUG,context.getName(),coefs[i], otherModel.getContext().getName(),otherModel.getCoef()[i]);
			/*double coef1 = (Math.abs(coefs[i]) < 0.0001) ? 0.0001 : coefs[i];
			double coef2 = (Math.abs(otherModel.getCoef()[i]) < 0.0001) ? 0.0001 : otherModel.getCoef()[i];*/
			double coef1 =  coefs[i];
			double coef2 =  otherModel.getCoef()[i];

			currentDifference = Math.abs(coef1 - coef2)/getContext().getAmas().data.maxModelCoef;
			//difference += currentDifference;
			difference = Math.max(currentDifference,difference);


			getContext().getEnvironment().print(TRACE_LEVEL.DEBUG,context.getName(),coef1, otherModel.getContext().getName(),coef2, "Diff",currentDifference, "maxCoef",getContext().getAmas().data.maxModelCoef);
		}

		return difference;
		//return difference/coefs.length;
	}
}
