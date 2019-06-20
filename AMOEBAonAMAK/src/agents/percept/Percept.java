package agents.percept;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.math3.exception.OutOfRangeException;

import agents.AmoebaAgent;
import agents.context.CenterRangeComparator;
import agents.context.Context;
import agents.context.CustomComparator;
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

	public HashMap<String, ArrayList<Context>> sortedRanges = new HashMap<String, ArrayList<Context>>();

	public ArrayList<Context> sortedContextbyStartRanges = new ArrayList<Context>();
	public ArrayList<Context> sortedContextbyEndRanges = new ArrayList<Context>();

	public HashMap<String, CustomComparator> customRangeComparators = new HashMap<String, CustomComparator>();

	private CustomComparator rangeStartComparator = new CustomComparator(this, "start");
	private CustomComparator rangeEndComparator = new CustomComparator(this, "end");
	private CenterRangeComparator centerComparator = new CenterRangeComparator(this);

	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;

	private double oldValue;
	private double value;
	private boolean isEnum = false;

	/**
	 * Instantiates a new percept.
	 *
	 * @param world the world
	 */
	public Percept(AMOEBA amoeba) {
		super(amoeba);

		sortedRanges.put("start", new ArrayList<Context>());
		sortedRanges.put("end", new ArrayList<Context>());

		customRangeComparators.put("start", new CustomComparator(this, "start"));
		customRangeComparators.put("end", new CustomComparator(this, "end"));
	}

	@Override
	public void onAct() {
		oldValue = value;
		value = getAmas().getPerceptions(this.name);
		ajustMinMax();
		computeContextProjectionValidity();

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

	/**
	 * Allow the percept to record the lower and higher value perceived.
	 */
	public void ajustMinMax() {
		if (value < min)
			min = value;
		if (value > max)
			max = value;
		
		System.out.println(this.getName() + " MIN : " + min + " MAX : " + max );

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
	 * Gets the old value.
	 *
	 * @return the old value
	 */
	public double getOldValue() {
		return oldValue;
	}

	/**
	 * Sets the old value.
	 *
	 * @param oldValue the new old value
	 */
	public void setOldValue(double oldValue) {
		this.oldValue = oldValue;
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
	 * ENDO
	 */

	public HashMap<String, ArrayList<Context>> getSortedRanges() {
		return sortedRanges;
	}

	public ArrayList<Context> getSortedRangesSubGroup(ArrayList<Context> subGroupOfContexts, String rangeString) {
		ArrayList<Context> sortedRangesSubGroup = new ArrayList<Context>();

		for (Context ctxt : sortedRanges.get(rangeString)) {
			if (subGroupOfContexts.contains(ctxt)) {
				sortedRangesSubGroup.add(ctxt);
			}
		}

		return sortedRangesSubGroup;
	}

	/*
	 * Sorted Ranges methods
	 */

	public void displaySortedRanges() {
		//////// System.out.println("########### SORTED RANGES DISPLAY " +
		//////// this.getName() +" ###########");
		//////// System.out.println("########### START ###########");
		for (Context cntxt : this.sortedRanges.get("start")) {
			//////// System.out.println(cntxt.getRanges().get(this).getStart());
		}

		//////// System.out.println("########### END ###########");
		for (Context cntxt : this.sortedRanges.get("end")) {
			//////// System.out.println(cntxt.getRanges().get(this).getEnd());
		}
	}

	public void displaySortedRangesTreeSet() {
		//////// System.out.println("########### SORTED RANGES DISPLAY TREE " +
		//////// this.getName() +" ###########");
		//////// System.out.println(sortedContextbyStartRanges.size()+ " " +
		//////// sortedContextbyEndRanges.size());
		//////// System.out.println("########### START ###########");

		for (Context ctxt : sortedContextbyStartRanges) {
			//////// System.out.println(ctxt.getRanges().get(this).getStart());
		}

		//////// System.out.println("########### END ###########");
		for (Context ctxt : sortedContextbyEndRanges) {
			//////// System.out.println(ctxt.getRanges().get(this).getEnd());
		}
	}

	public void sortOnStartRanges(ArrayList<Context> contextsSet) {

		Collections.sort(contextsSet, rangeStartComparator);

	}

	public void sortOnCenterOfRanges(ArrayList<Context> contextsSet) {

		Collections.sort(contextsSet, centerComparator);

	}

	public void sortOnEndRanges(ArrayList<Context> contextsSet) {

		Collections.sort(contextsSet, rangeEndComparator);

	}

	public void updateSortedRanges(Context context, String range) {
		int contextIndex = sortedRanges.get(range).indexOf(context);
		boolean rightPlace = false;

		Collections.sort(sortedContextbyEndRanges, rangeEndComparator);
		Collections.sort(sortedContextbyStartRanges, rangeStartComparator);
		Collections.sort(sortedRanges.get("end"), rangeEndComparator);
		Collections.sort(sortedRanges.get("start"), rangeStartComparator);

		/*
		 * if(contextIndex<sortedRanges.get(range).size()-1) {
		 * 
		 * if(getRangeProjection(sortedRanges.get(range).get(contextIndex), range) >
		 * getRangeProjection(sortedRanges.get(range).get(contextIndex +1), range)) {
		 * 
		 * while(contextIndex<sortedRanges.get(range).size()-1 && !rightPlace){
		 * if(getRangeProjection(sortedRanges.get(range).get(contextIndex), range) >
		 * getRangeProjection(sortedRanges.get(range).get(contextIndex +1), range)) {
		 * swapListElements(sortedRanges.get(range), contextIndex); contextIndex +=1;
		 * 
		 * if(contextIndex<sortedRanges.get(range).size()-1) {
		 * if(getRangeProjection(sortedRanges.get(range).get(contextIndex), range) <
		 * getRangeProjection(sortedRanges.get(range).get(contextIndex +1), range)) {
		 * rightPlace = true; } } else { rightPlace = true; }
		 * 
		 * 
		 * }
		 * 
		 * } }
		 * 
		 * 
		 * }
		 * 
		 * if(contextIndex>0) {
		 * 
		 * rightPlace = false;
		 * 
		 * if(getRangeProjection(sortedRanges.get(range).get(contextIndex), range) <
		 * getRangeProjection(sortedRanges.get(range).get(contextIndex -1), range)) {
		 * 
		 * while(contextIndex> 0 && !rightPlace){
		 * if(getRangeProjection(sortedRanges.get(range).get(contextIndex), range) <
		 * getRangeProjection(sortedRanges.get(range).get(contextIndex -1), range)) {
		 * swapListElements(sortedRanges.get(range), contextIndex -1); contextIndex -=1;
		 * 
		 * if(contextIndex> 0) {
		 * if(getRangeProjection(sortedRanges.get(range).get(contextIndex), range) >
		 * getRangeProjection(sortedRanges.get(range).get(contextIndex -1), range)) {
		 * rightPlace = true; } } else { rightPlace = true; }
		 * 
		 * 
		 * }
		 * 
		 * } } }
		 */

	}

	private void swapListElements(ArrayList<Context> list, int indexFirstElement) {
		try {
			list.add(indexFirstElement, list.get(indexFirstElement + 1));
			////////// System.out.println(list);
			list.remove(indexFirstElement + 2);
		} catch (OutOfRangeException e) {
			// TODO: handle exception
		}
	}

	public void addContextSortedRanges(Context context) {

		sortedRanges.get("start").add(context);
		sortedRanges.get("end").add(context);

		sortedContextbyStartRanges.add(context);
		sortedContextbyEndRanges.add(context);

		Collections.sort(sortedRanges.get("end"), rangeEndComparator);
		Collections.sort(sortedRanges.get("start"), rangeStartComparator);
		Collections.sort(sortedContextbyEndRanges, rangeEndComparator);
		Collections.sort(sortedContextbyStartRanges, rangeStartComparator);

		// displaySortedRanges();
		// displaySortedRangesTreeSet();

		////////// System.out.println("----------------------AUTO PRINT");
		////////// System.out.println(sortedEndRanges.size()+ " " + sortedStartRanges);
	}

	private void insertContextInSortedRanges(Context context, String range) {

		int i = 0;
		boolean inserted = false;
		while (i < sortedRanges.get(range).size() && !inserted) {
			if (getRangeProjection(context, range) < getRangeProjection(sortedRanges.get(range).get(i), range)) {
				sortedRanges.get(range).add(i, context);
				inserted = true;
			}
			i += 1;
		}
		if (i == sortedRanges.get(range).size() && !inserted) {
			sortedRanges.get(range).add(context);
		}

	}

	public void deleteContextRanges(Context context) {
		sortedRanges.get("start").remove(context);
		sortedRanges.get("end").remove(context);

		sortedContextbyStartRanges.remove(context);
		sortedContextbyStartRanges.remove(context);
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

	// -----------------------
	// AMOEBAonAMAK code ----
	// -----------------------

	@Override
	public String toString() {
		return getName();
	}
}
