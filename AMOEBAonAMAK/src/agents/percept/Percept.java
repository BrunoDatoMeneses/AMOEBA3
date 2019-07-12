package agents.percept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import agents.AmoebaAgent;
import agents.context.Context;
import kernel.AMOEBA;

/**
 * Percept agent is in charge of the communication with the environment. Each
 * Percept agent must be connected to one data source.
 *
 */
public class Percept extends AmoebaAgent {

	// private BlackBoxAgent sensor;
	protected ArrayList<AmoebaAgent> targets = new ArrayList<>();
	protected ArrayList<Context> activatedContext = new ArrayList<>();

	public HashMap<Context, ContextProjection> contextProjections = new HashMap<Context, ContextProjection>();
	private HashSet<Context> validContextProjection = new HashSet<Context>();
	private HashSet<Context> neighborContextProjection = new HashSet<Context>();

	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;


	private double value;
	private boolean isEnum = false;

	/**
	 * Instantiates a new percept.
	 *
	 * @param amoeba
	 */
	public Percept(AMOEBA amoeba) {
		super(amoeba);
		getAmas().addPercept(this);
	}
	
	/**
	 * Instanriates a new percept, not linked to any amoeba.
	 * USE FOR VISUALIZATION ONLY
	 */
	public Percept() {
		super(null);
	}

	@Override
	public void onAct() {
		value = getAmas().getPerceptions(this.name);
		ajustMinMax();
		computeContextProjectionValidityOptimized();
	}

	public void computeContextProjectionValidity() {

		for (ContextProjection contextProjection : contextProjections.values()) {

			// if(!contextProjection.contains(this.value, getRadiusContextForCreation())) {

			if (!contextProjection.inNeighborhood()) {
				contextProjection.getContext().addNonValidNeighborPercept(this);
				contextProjection.getContext().addNonValidPercept(this);
			} else if (!contextProjection.contains(this.value)) {
				contextProjection.getContext().addNonValidPercept(this);
			}
		}
	}
	
	public void computeContextNeighborsValidity(ArrayList<Context> contextNeighbors) {

		for (ContextProjection contextProjection : contextProjections.values()) {

			if(contextNeighbors.contains(contextProjection.getContext())) {
				if (!contextProjection.contains(this.value)) {
					contextProjection.getContext().addNonValidNeighborPercept(this);
				} 
			}
			
			
		}
	}
	
	public void computeContextProjectionValidityOptimized() {

		/* The algorithm used here :
		 * 
		 * Variables :
		 * global set allContexts
		 * global set validContexts
		 * local set myValidContexts
		 * 
		 * Algorithm : for each percept do :
		 * myValidContexts <- validContexts
		 * if myValidContexts = null then
		 * 		myValidContexts <- allContexts
		 * fi
		 * 
		 * for context in myValidContext do
		 * 		if not isValid(context) then
		 * 			myValidContexts.remove(context)
		 * 		fi
		 * done
		 * 
		 * validContexts <- intersect(validContexts, myValidContexts)
		 * #we use an intersect to allow multithreading, avoiding that a percept override the work of another
		 * 
		 */
		
		validContextProjection = new HashSet<Context>();
		neighborContextProjection = new HashSet<Context>();
		
		// To avoid unnecessary tests, we only compute validity on context
		// validated by percepts that have finished before us
		HashSet<Context> contexts = amas.getValidContexts();
		if(contexts == null) {
			// If we are one of the first percept to run, we compute validity on all contexts
			contexts = new HashSet<>(amas.getContexts());
		}
		
		for (Context c : contexts) {
			if (activateContext(c)) {
				validContextProjection.add(c);
			}
		} 
		amas.updateValidContexts(validContextProjection);
		
		HashSet<Context> neighborsContexts = amas.getNeighborContexts();
		if(neighborsContexts == null) {
			// If we are one of the first percept to run, we compute validity on all contexts
			neighborsContexts = new HashSet<>(amas.getContexts());
		}
		
		for (Context c : neighborsContexts) {
			if(inNeighborhood(c)) {
				neighborContextProjection.add(c);
			}
		} 
		amas.updateNeighborContexts(neighborContextProjection);
		
		logger().debug("CYCLE "+getAmas().getCycle(), "%s's valid contexts : %s", toString(), validContextProjection.toString());
	}
	
	/**
	 * Return true if the context is activated by this percept.
	 * @param context
	 * @return
	 */
	public boolean activateContext(Context context) {
		return contextProjections.get(context).contains(this.value);
	}
	
	/**
	 * Return true if the context is in the neighborhood of this percept.
	 * @param context
	 * @return
	 */
	public boolean inNeighborhood(Context context) {
		return contextProjections.get(context).inNeighborhood();
	}


	/**
	 * Allow the percept to record the lower and higher value perceived.
	 */
	public void ajustMinMax() {
		if (value < min)
			min = value;
		if (value > max)
			max = value;
		
		

		/*
		 * In order to avoid big gap in min-max value in order to adapt with the system
		 * dynamic It's also a warranty to avoid to flaw AVT with flawed value
		 */
		double dist = max - min;
		// TODO ?
		// min += 0.05*dist;
		// max -= 0.05*dist;
	}

	/**
	 * Gets the min max distance.
	 *
	 * @return the min max distance
	 */
	public double getMinMaxDistance() {
		if (min == Double.MAX_VALUE || max == Double.MIN_VALUE) 
			return 0;
		return Math.abs(max - min);
	}

	public double getMin() {
		if (min == Double.MAX_VALUE) {
			return 0.0;
		}
		return min;
	}

	public double getMax() {
		if (max == Double.MIN_VALUE) {
			return 0.0;
		}
		return max;
	}

	public void setMin(double value) {
		min = value;
	}

	public void setMax(double value) {
		max = value;
	}

	/**
	 * Gets the activated context.
	 *
	 * @return the activated context
	 */
	public ArrayList<Context> getActivatedContext() {
		return activatedContext;
	}

	/**
	 * Sets the activated context.
	 *
	 * @param activatedContext the new activated context
	 */
	public void setActivatedContext(ArrayList<Context> activatedContext) {
		this.activatedContext = activatedContext;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see agents.SystemAgent#getTargets()
	 */
	public ArrayList<? extends AmoebaAgent> getTargets() {
		return targets;
	}

	/**
	 * Sets the targets.
	 *
	 * @param targets the new targets
	 */
	public void setTargets(ArrayList<AmoebaAgent> targets) {
		this.targets = targets;
	}





	/**
	 * Checks if is enum.
	 *
	 * @return true, if is enum
	 */
	public boolean isEnum() {
		return isEnum;
	}

	/**
	 * Sets the enum.
	 *
	 * @param isEnum the new enum
	 */
	public void setEnum(boolean isEnum) {
		this.isEnum = isEnum;
	}

	/*
	 * Context projection methods
	 */

	public void addContextProjection(Context context) {
		ContextProjection newContextProjection = new ContextProjection(this, context);
		contextProjections.put(context, newContextProjection);

	}

	public void deleteContextProjection(Context context) {
		contextProjections.remove(context);
		//////// System.out.println("DELETION
		//////// ------------------------------------------------------------------------------------------------------"
		//////// + world.getScheduler().getTick());
		//////// System.out.println(context.getName());
		//////// System.out.println("----------------------------------------------------------------------------------------------------------------");
	}

	public void updateContextProjectionStart(Context context) {
		//////// System.out.println(context.getName());
		//////// System.out.println(contextProjections.get(context));
		//////// System.out.println(contextProjections.size() + " " +
		//////// world.getScheduler().getContextsAsContext().size());
		if (!context.isDying()) {
			contextProjections.get(context).updateStart();
		}

	}

	public void updateContextProjectionEnd(Context context) {
		if (!context.isDying()) {
			contextProjections.get(context).updateEnd();
		}

	}

	public boolean overlapBetweenContexts(Context context1, Context context2) {

		double contextStart1 = getStartRangeProjection(context1);
		double contextStart2 = getStartRangeProjection(context2);
		double contextEnd1 = getEndRangeProjection(context1);
		double contextEnd2 = getEndRangeProjection(context2);
		////////// System.out.println(context1.getName() + " " + contextStart1 + " " +
		////////// contextEnd1 + " " + context2.getName() + " " + contextStart2 + " " +
		////////// contextEnd2);
		return ((contextStart1 < contextStart2 && contextStart2 < contextEnd1)
				|| ((contextStart1 < contextEnd2 && contextEnd2 < contextEnd1)))
				|| ((contextStart2 < contextStart1 && contextStart1 < contextEnd2)
						|| ((contextStart2 < contextEnd1 && contextEnd1 < contextEnd2)));

	}

	public double getRangeProjection(Context context, String range) {
		if (range.equals("start")) {
			return context.getRanges().get(this).getStart();
		} else if (range.equals("end")) {
			return context.getRanges().get(this).getEnd();
		} else {
			return 0;
		}

	}

	public Double getEndRangeProjection(Context context) {
		return new Double(context.getRanges().get(this).getEnd());
	}

	public Double getStartRangeProjection(Context context) {
		return new Double(context.getRanges().get(this).getStart());
	}

	public HashMap<String, Double> getOverlapRangesBetweenContexts(Context context1, Context context2) {

		HashMap<String, Double> overlapRanges = new HashMap<String, Double>();

		if (contextIncludedIn(context1, context2)) {
			overlapRanges.put("start", getStartRangeProjection(context1));
			overlapRanges.put("end", getEndRangeProjection(context1));
		} else if (contextIncludedIn(context2, context1)) {
			overlapRanges.put("start", getStartRangeProjection(context2));
			overlapRanges.put("end", getEndRangeProjection(context2));
		} else if (contextOrder(context1, context2)) {
			overlapRanges.put("start", getStartRangeProjection(context2));
			overlapRanges.put("end", getEndRangeProjection(context1));
		} else if (contextOrder(context2, context1)) {
			overlapRanges.put("start", getStartRangeProjection(context1));
			overlapRanges.put("end", getEndRangeProjection(context2));
		} else {
			////////// System.out.println("PROBLEM !!!!!!!!!!!!!!!!! " + context1.getName()
			////////// + " " + getStartRangeProjection(context1) + " " +
			////////// getEndRangeProjection(context1) + " " + context2.getName() + " " +
			////////// getStartRangeProjection(context2) + " " +
			////////// getEndRangeProjection(context2));
			overlapRanges.put("start", -1.0);
			overlapRanges.put("end", 1.0);
			// return null;
		}

		return overlapRanges;
	}

	public boolean contextIncludedIn(Context includedContext, Context includingContext) {

		double includedContextStart = getStartRangeProjection(includedContext);
		double includingContextStart = getStartRangeProjection(includingContext);
		double includedContextEnd = getEndRangeProjection(includedContext);
		double includingContextEnd = getEndRangeProjection(includingContext);

		return ((includingContextStart < includedContextStart && includedContextStart < includingContextEnd)
				&& ((includingContextStart < includedContextEnd && includedContextEnd < includingContextEnd)));
	}

	public boolean contextOrder(Context context1, Context context2) {

		double contextStart1 = getStartRangeProjection(context1);
		double contextEnd1 = getEndRangeProjection(context1);

		double contextStart2 = getStartRangeProjection(context2);
		double contextEnd2 = getEndRangeProjection(context2);

		// world.trace(new
		// ArrayList<String>(Arrays.asList(""+contextStart1,""+contextStart2,
		// ""+contextEnd1, ""+(contextStart1 <= contextStart2 && contextStart2 <=
		// contextEnd1))));

		return (contextStart1 <= contextStart2 && contextStart2 <= contextEnd1 && contextEnd1 <= contextEnd2);
	}

	public Percept clone() throws CloneNotSupportedException {
		return (Percept) super.clone();
	}

	public double getRadiusContextForCreation() {
		// return 200*world.getContextCreationPercentage();
		return getMinMaxDistance() * getEnvironment().getMappingErrorAllowed();
	}

	public double getMappingErrorAllowed() {
		return getMinMaxDistance() * getEnvironment().getMappingErrorAllowed();
	}
	
	public double getMappingErrorAllowedMin() {
		return getMinMaxDistance() * getEnvironment().getMappingErrorAllowed() * 0.4;
	}
	
	public double getMappingErrorAllowedOverMapping() {
		return getMinMaxDistance() * getEnvironment().getMappingErrorAllowed() * 0.5;
	}
	
	public double getMappingErrorAllowedMax() {
		return getMinMaxDistance() * getEnvironment().getMappingErrorAllowed() * 1.5;
	}

	// -----------------------
	// AMOEBAonAMAK code ----
	// -----------------------

	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		getAmas().setPercepts();
	}
}
