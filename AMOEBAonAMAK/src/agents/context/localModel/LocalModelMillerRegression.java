package agents.context.localModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import agents.context.Context;
import agents.context.Experiment;
import agents.percept.Percept;
import org.apache.commons.math3.analysis.function.Exp;
import utils.Pair;
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
				updateModelWithExperimentAndWeight(newExperiment, weight, context.getAmas().data.numberOfPointsForRegression);

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

	private Experiment getSymetricalExperiment(Experiment experiment) {
		Experiment symetricalExperiment = new Experiment(context);
		Experiment centerExperiment = context.getCenterExperiment();
		for(Percept pct : context.getAmas().getPercepts()){
			double rangeCenter = context.getRanges().get(pct).getCenter();
			symetricalExperiment.addDimension(pct, rangeCenter-Math.abs(rangeCenter-experiment.getValuesAsHashMap().get(pct)));
		}
		double centerProposition = getProposition(centerExperiment);
		symetricalExperiment.setProposition(centerProposition- Math.abs(centerProposition - experiment.getProposition()));
		return symetricalExperiment;
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



		double[] coef = regression.regress().getParameterEstimates();
		coefs = new Double[coef.length];
		for(int i = 0; i < coef.length; i++) {
			coefs[i] = coef[i];
		}

		context.getAmas().getEnvironment().regressionPoints = newExperiments.size() + artificialExperiments.size();


		context.getAmas().addSpatiallyAlteredContextForUnityUI(context);



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
		

		double[] coef = regression.regress().getParameterEstimates();
		coefs = new Double[coef.length];
		for(int j = 0; j < coef.length; j++) {
			coefs[j] = coef[j];
		}
		
	}
	
	
	
	
	public void updateModel() {
		
		context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),"FIRST EXPERIMENTS")));
		regression = new Regression(nParameters,true);


		while (regression.getN() < nParameters + 2){
			for (Experiment exp : firstExperiments) {

				regression.addObservation(exp.getValuesAsArray(), exp.getProposition());

			}
		}


		
		/*int i = 0;
		while (regression.getN() < nParameters + 2) { 
			
			//context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),i+"", ""+firstExperiments.get(i%firstExperiments.size()).getValuesAsArray(), firstExperiments.get(i%firstExperiments.size()).getOracleProposition()+"" )));
			regression.addObservation(firstExperiments.get(i%firstExperiments.size()).getValuesAsArray(), firstExperiments.get(i%firstExperiments.size()).getOracleProposition());
			i++;
		}*/
		

		double[] coef = regression.regress().getParameterEstimates();
		coefs = new Double[coef.length];
		for(int j = 0; j < coef.length; j++) {
			coefs[j] = coef[j];
		}
		
	}
	
	
	public void updateModelWithExperimentAndWeight(Experiment newExperiment, double weight, int numberOfPoints) {
		
		context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),"EXPERIMENTS WITH WEIGHT")));
		
		regression = new Regression(nParameters,true);

		
		int numberOfPointsForRegression = numberOfPoints;
		if(numberOfPointsForRegression < (nParameters+2)) {
			numberOfPointsForRegression += numberOfPointsForRegression*((int)((nParameters+2)/numberOfPointsForRegression));
		}

		//ArrayList<Experiment> artificialExperiments = getRandomlyDistributedArtificialExperimentsAsArray((int)(numberOfPointsForRegression - (numberOfPointsForRegression*weight)));
		ArrayList<Experiment> artificialExperiments = getNormalyDistributedArtificialExperimentsAsArray((int)(numberOfPointsForRegression - (numberOfPointsForRegression*weight)));
		//Pair<double[][], double[]> artificialSituations = getEquallyDistributedArtificialExperiments((int)(numberOfPointsForRegression - (numberOfPointsForRegression*weight)));
		
		context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),"ARTIFICIAL" )));

		int numberOfArtificialPoints = artificialExperiments.size();
		for (Experiment artificialExperiment : artificialExperiments) {
			
			context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(), ""+artificialExperiment )));
			regression.addObservation(artificialExperiment.getValuesAsArray(), artificialExperiment.getProposition());
		}
		

		int numberOfXPPoints;
		if(numberOfArtificialPoints != (int)(numberOfPointsForRegression - (numberOfPointsForRegression*weight))) {
			numberOfXPPoints = (int)(weight*numberOfArtificialPoints/(1-weight));
		}
		else {
			numberOfXPPoints = (int)(numberOfPointsForRegression*weight);
		}
		
		context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),"XP")));
		for (int i =0;i<numberOfXPPoints;i++) {
			
			context.getAmas().getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(),i+"", ""+newExperiment.getValuesAsArray(), newExperiment.getProposition()+"" )));
			regression.addObservation(newExperiment.getValuesAsArray(), newExperiment.getProposition());
			
			
	
		}
		
		while (regression.getN() < newExperiment.getValuesAsLinkedHashMap().size() + 2) { 
			
			regression.addObservation(newExperiment.getValuesAsArray(), newExperiment.getProposition());
			
			System.out.println("ADING Observations " + regression.getN()); //Should not happen
			
		}
		

		
		double[] coef = regression.regress().getParameterEstimates();
		coefs = new Double[coef.length];
		for(int i = 0; i < coef.length; i++) {
			coefs[i] = coef[i];
		}
		
		context.getAmas().getEnvironment().regressionPoints = numberOfXPPoints + numberOfArtificialPoints;
		
		
	}
	
	private Pair<double[][], double[]> getEquallyDistributedArtificialExperiments(int amount){
		
		
		int nbPercept = context.getAmas().getPercepts().size();
		
		int[] nbPointsByPercept = new int[nbPercept];
		
		ArrayList<Double> pointsByPercept[] = new ArrayList[nbPercept];
		
		int totalNumberOfPoints = 1;
		
		for(int i = 0 ; i < nbPercept ; i++) {
			
			double startRange = this.context.getRanges().get(context.getAmas().getPercepts().get(i)).getStart();
			double endRange = this.context.getRanges().get(context.getAmas().getPercepts().get(i)).getEnd();			
			
			nbPointsByPercept[i] = (int)(amount*this.context.rangeLengthRatio(context.getAmas().getPercepts().get(i)));
			pointsByPercept[i] = new ArrayList<Double>();

			totalNumberOfPoints *= nbPointsByPercept[i];
			
			
			for(int j=0;j<=nbPointsByPercept[i]-1;j++) {
				pointsByPercept[i].add(startRange + ((endRange-startRange)/(nbPointsByPercept[i]-1))*j );
			}
			
		}
		
		nbPercept = context.getAmas().getPercepts().size();
		int[] perceptIndices = new int[nbPercept];
		for(int i = 0 ; i < nbPercept ; i++) {
			perceptIndices[i] = 0;
		}

		
		int i = 0;
		boolean test = true;
		double[][] artificalExperiments = new double[totalNumberOfPoints][nParameters];
		double[] artificalResults = new double[totalNumberOfPoints];

		while(test) {
			
			for(int j = 0;j<nParameters;j++) {
				
				artificalExperiments[i][j] = pointsByPercept[j].get(perceptIndices[j]);
				
			}
			artificalResults[i] = this.getProposition(context, artificalExperiments[i]);
			
			test = nextMultiDimCounter(perceptIndices, nbPointsByPercept);
			
			i++;
			
		}

		
		
		
		return new Pair<double[][], double[]>(artificalExperiments, artificalResults);
	}
	
	private boolean nextMultiDimCounter(int[] indices, int[] bounds){
		
		
		
		for(int i = 0; i<indices.length;i++) {
			
			if(indices[i]==bounds[i]-1) {
				if(i==indices.length-1) {
					indices[i]=0;
					return false;
				}
				else {
					indices[i]=0;
				}				
			}
			else {
				indices[i] += 1;
				return true;
			}
			
		}
		
		return false;

		
	}
	
	private Pair<double[][], double[]> getRandomlyDistributedArtificialExperiments(int amount){
		
		double[][] artificalExperiments = new double[amount][nParameters];
		double[] artificalResults = new double[amount];
		

		
		for (int i = 0; i < amount;i ++) {
			
			for(int j = 0;j<nParameters;j++) {
				
				double startRange = this.context.getRanges().get(context.getAmas().getPercepts().get(j)).getStart();
				double endRange = this.context.getRanges().get(context.getAmas().getPercepts().get(j)).getEnd();
				
				artificalExperiments[i][j] = startRange + (Math.random()*(endRange - startRange));
			}
			artificalResults[i] = this.getProposition(context, artificalExperiments[i]);
			
		}
		
		return new Pair<double[][], double[]>(artificalExperiments, artificalResults);
	}

	private ArrayList<Experiment> getRandomlyDistributedArtificialExperimentsAsArray(int amount){

		ArrayList<Experiment> artificialExperiments = new ArrayList<>();

		for (int i = 0; i < amount;i ++) {

			Experiment exp = new Experiment(context);
			for(Percept pct : context.getAmas().getPercepts()) {

				double rangeLength = this.context.getRanges().get(pct).getLenght();
				double startRange = this.context.getRanges().get(pct).getStart() + (rangeLength/4);
				double endRange = this.context.getRanges().get(pct).getEnd() - (rangeLength/4);
				exp.addDimension(pct,startRange + (Math.random()*(endRange - startRange)));
			}
			exp.setProposition(this.getProposition(exp));
			artificialExperiments.add(exp);
		}

		return artificialExperiments;
	}

	private ArrayList<Experiment> getNormalyDistributedArtificialExperimentsAsArray(int amount){

		ArrayList<Experiment> artificialExperiments = new ArrayList<>();

		for (int i = 0; i < amount;i ++) {

			Experiment exp = new Experiment(context);
			for(Percept pct : context.getAmas().getPercepts()) {

				double rangeCenter = this.context.getRanges().get(pct).getCenter();
				double rangeLength = this.context.getRanges().get(pct).getLenght();
				java.util.Random r = new java.util.Random();
				double ramdomGaussianPosition = (r.nextGaussian() * Math.sqrt(rangeLength/10)) + rangeCenter;
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
		double difference = 0;

		for(int i=0;i<coefs.length;i++){
			difference += Math.abs(coefs[i] - otherModel.getCoef()[i]);
		}

		return difference;
	}
}
