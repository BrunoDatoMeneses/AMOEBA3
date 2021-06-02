package agents.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import agents.percept.Percept;
import kernel.ELLSA;
import kernel.World;
import ncs.NCS;
import utils.Pair;
import utils.TRACE_LEVEL;

/**
 * The Class Range.
 */
public class Range implements Serializable, Comparable, Cloneable {

	private double start = 0;
	private double end = 0;

	private boolean start_inclu;
	private boolean end_inclu;

	private double value;
	private double oldValue;

	private double alphaFactor;

	private int lastStartTickModification = 0;
	private int lastEndTickModification = 0;

	private int lastStartDirection = 0;
	private int lastEndDirection = 0;

	private int startCriticality = 0;
	private int endCriticality = 0;

	private double startIncrement;
	private double endIncrement;

	/** The Constant startLenghtRatio. */
	/*
	 * The weight in an interpolation : the impact on action for a +1 change in this
	 * range value
	 */
	private final static double minLenghtRatio = 0;

	private World world;
	private Context context;
	private Percept percept;

	public static int maxid = 0; // TODO for debug purposes
	public int id;
	public static final double mininimalRange = 1;
	private static final boolean useAVT = true;

	/*---------------AVT---------------*/
	private double AVT_deltaStart = 0.5;
	private double AVT_deltaEnd = 0.5;

	private int AVT_lastFeedbackStart = 1;
	private int AVT_lastFeedbackEnd = 1;

	private double AVT_acceleration;
	private double AVT_deceleration;

	private double AVT_minRatio;
	/*---------------------------------*/

	/*------------Percent--------------*/
	// Only used if useAVT == false
	static public double percent_up = 0.2;
	static public double percent_down = 0.1;
	/*---------------------------------*/

	public double increment_up = 0.05;
	public double increment_down = 0.05;

	/**
	 * Instantiates a new range.
	 *
	 * @param context                 the context
	 * @param start                   the start
	 * @param end                     the end
	 * @param extendedrangeatcreation the extendedrangeatcreation
	 * @param start_inclu             the start inclu
	 * @param end_inclu               the end inclu
	 * @param p                       the p
	 */
	public Range(Context context, double start, double end, double extendedrangeatcreation, boolean start_inclu,
			boolean end_inclu, Percept p) {
		super();

		world = context.getAmas().getEnvironment();

		AVT_deceleration = world.getAVT_deceleration();
		AVT_acceleration = world.getAVT_acceleration();
		AVT_minRatio = world.getAVT_percentAtStart();

		this.percept = p;
		if (isPerceptEnum()) {
			this.setStart_inclu(start_inclu);
			this.setEnd_inclu(end_inclu);
			this.setStart(Math.round(start));
			this.setEnd(Math.round(end));
		} else {
			this.setStart_inclu(start_inclu);
			this.setEnd_inclu(end_inclu);
			this.setStart(start - Math.abs(extendedrangeatcreation * start));
			this.setEnd(end + Math.abs(extendedrangeatcreation * end));
		}
		this.context = context;
		id = maxid;
		maxid++;

		/* Initialization of AVT : a better way to do that should be developped */
		this.AVT_deltaStart = getLenght() * 0.2 + 0.0001;
		this.AVT_deltaEnd = getLenght() * 0.2 + 0.0001;


		startIncrement = 0.25 * world.getMappingErrorAllowed() * percept.getMinMaxDistance();
		endIncrement = startIncrement;
		world
		.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList(context.getName(), p.getName(), "Init start increment " + startIncrement)));
		world
		.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList(context.getName(), p.getName(), "Init end increment " + endIncrement)));
	}

	// FOR TEST ONLY
	public Range(ELLSA ellsa, double start, double end, double extendedrangeatcreation, boolean start_inclu,
				 boolean end_inclu, Percept p) {
		super();

		world = ellsa.getEnvironment();

		AVT_deceleration = world.getAVT_deceleration();
		AVT_acceleration = world.getAVT_acceleration();
		AVT_minRatio = world.getAVT_percentAtStart();

		this.percept = p;
		if (isPerceptEnum()) {
			this.setStart_inclu(start_inclu);
			this.setEnd_inclu(end_inclu);
			this.setStart(Math.round(start));
			this.setEnd(Math.round(end));
		} else {
			this.setStart_inclu(start_inclu);
			this.setEnd_inclu(end_inclu);
			this.setStart(start - Math.abs(extendedrangeatcreation * start));
			this.setEnd(end + Math.abs(extendedrangeatcreation * end));
		}

		id = maxid;
		maxid++;

		/* Initialization of AVT : a better way to do that should be developped */
		this.AVT_deltaStart = getLenght() * 0.2 + 0.0001;
		this.AVT_deltaEnd = getLenght() * 0.2 + 0.0001;

		startIncrement = 0.25 * world.getMappingErrorAllowed() * percept.getMinMaxDistance();
		endIncrement = startIncrement;

	}
	
	public Range(Context context, double start, double end, double extendedrangeatcreation, boolean start_inclu,
			boolean end_inclu, Percept p, double startIncr, double endIncr) {
		super();

		world = context.getAmas().getEnvironment();

		AVT_deceleration = world.getAVT_deceleration();
		AVT_acceleration = world.getAVT_acceleration();
		AVT_minRatio = world.getAVT_percentAtStart();

		this.percept = p;
		if (isPerceptEnum()) {
			this.setStart_inclu(start_inclu);
			this.setEnd_inclu(end_inclu);
			this.setStart(Math.round(start));
			this.setEnd(Math.round(end));
		} else {
			this.setStart_inclu(start_inclu);
			this.setEnd_inclu(end_inclu);
			this.setStart(start - Math.abs(extendedrangeatcreation * start));
			this.setEnd(end + Math.abs(extendedrangeatcreation * end));
		}
		this.context = context;
		id = maxid;
		maxid++;

		/* Initialization of AVT : a better way to do that should be developped */
		this.AVT_deltaStart = getLenght() * 0.2 + 0.0001;
		this.AVT_deltaEnd = getLenght() * 0.2 + 0.0001;

		startIncrement =startIncr;
		endIncrement = endIncr;
	}



	public void adapt(Double oracleValue, boolean isOverlap, Context bestContext) {
		if (!isPerceptEnum()) {

			staticAdapt(oracleValue, getIncrement(), isOverlap, bestContext);

		}
	}



	private void staticAdapt(double oracleValue, double increment, boolean isOverlap, Context bestContext) {
		if (Math.abs(end - oracleValue) < Math.abs(oracleValue - start)) {
			adaptEnd(oracleValue, increment, isOverlap, bestContext);
		} else {
			adaptStart(oracleValue, increment, isOverlap, bestContext);
		}
	}



	private void adaptEnd(double oracleValue, double increment, boolean isOverlap, Context bestContext) {
		world.trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("INCREMENT ON END ADAPT", context.getName(), percept.getName(), "" + increment )));

		classicEndAdapt(oracleValue, increment, isOverlap, bestContext);

	}

	private void classicEndAdapt(double oracleValue, double increment, boolean isOverlap, Context bestContext) {
		if (!(contains(oracleValue) == 0.0)) { // value not contained --> end range will grow (growing direction = 1)

			if (lastEndDirection == -1) { // opposite direction -> negative feedback
				endCriticality = 1;
			} else if (lastEndDirection == 1) { // same direction -> positive feedback
				endCriticality = 0;
			}
			lastEndDirection = 1; // growing direction

			if (endCriticality == 1) { // negative feedback -> increment decreases
				endIncrement /= 3;
			} else if (endCriticality == 0) { // positive feedback -> increment increases
				endIncrement = Math.min(percept.getRadiusContextForCreation(), endIncrement * 2);
				// endIncrement *=2;
			}

			
			this.setEnd(end + endIncrement);
			
			
		} else { // value contained --> end range will shrink (shrinking direction = -1)

			if (lastEndDirection == 1) { // opposite direction -> negative feedback
				endCriticality = 1;
			} else if (lastEndDirection == -1) { // same direction -> positive feedback
				endCriticality = 0;
			}
			lastEndDirection = -1; // shrinking direction

			if (endCriticality == 1) { // negative feedback -> increment decreases
				endIncrement /= 3;
			} else if (endCriticality == 0) { // positive feedback -> increment increases
				endIncrement = Math.min(percept.getRadiusContextForCreation(), endIncrement * 2);
				// endIncrement *=2;
			}

			if(isOverlap) {
				this.setEnd(bestContext.getRanges().get(this.percept).getStart());
			}else {
				this.setEnd(end - endIncrement);
			}
			

		}

	}




	private void adaptStart(double oracleValue, double increment, boolean isOverlap, Context bestContext) {
		world.trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("INCREMENT ON START ADAPT", context.getName(), percept.getName(), "" + increment )));

		classicStartAdapt(oracleValue, increment, isOverlap, bestContext);

	}

	private void classicStartAdapt(double oracleValue, double increment, boolean isOverlap, Context bestContext) {
		if (!(contains(oracleValue) == 0.0)) {

			if (lastStartDirection == -1) {
				startCriticality = 1;
			} else if (lastStartDirection == 1) {
				startCriticality = 0;
			}
			lastStartDirection = 1;

			if (startCriticality == 1) {
				startIncrement /= 3;
			} else if (startCriticality == 0) {

				startIncrement = Math.min(percept.getRadiusContextForCreation(), startIncrement * 2);
			}


			this.setStart(start - startIncrement);


		} else {
			if (lastStartDirection == 1) {
				startCriticality = 1;
			} else if (lastStartDirection == -1) {
				startCriticality = 0;
			}
			lastStartDirection = -1;

			if (startCriticality == 1) {
				startIncrement /= 3;
			} else if (startCriticality == 0) {
				startIncrement = Math.min(percept.getRadiusContextForCreation(), startIncrement * 2);
				// startIncrement *=2;
			}


			if(isOverlap) {
				this.setStart(bestContext.getRanges().get(this.percept).getEnd());
			}else {
				this.setStart(start + startIncrement);
			}
		}

	}



	public double getIncrement() {
		double increment = 0.25 * world.getMappingErrorAllowed() * percept.getMinMaxDistance();
		// world.trace(new
		// ArrayList<String>(Arrays.asList(this.getContext().getName(),percept.getName(),
		// "INCREMENT", ""+increment)));
		return increment;
	}



	/**
	 * Check if the ranges is too small according to strategy.
	 * 
	 * @return boolean representing if the range is too small.
	 */
	public boolean isTooSmall() {
		
		if(Math.abs(end - start) < percept.getMappingErrorAllowedMin()) {
			world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.context.getName(), this.percept.getName(), "TOO SMALL DISTANCE", "" + Math.abs(end - start))));
		}

		return (Math.abs(end - start) < percept.getMappingErrorAllowedMin()) && !this.isPerceptEnum();
	}

	public boolean isAnomaly() {

		if(start > end) {
			world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.context.getName(), this.percept.getName(), "Anomaly", "Start : " + start + ">" + " End" + end)));
		}

		return start > end;
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


	public String toString() {
		return ((start_inclu ? "[" : "]") + start + "," + end
				+ (!end_inclu ? "[" : "]") + "  Current value : " + percept.getValue()
				+ "\n lastStartDirection : " + lastStartDirection
				+ "\n lastEndDirection : " + lastEndDirection 
				+ "\n startCriticality : " + startCriticality
				+ "\n endCriticality : " + endCriticality
				+ "\n startIncrement : " + startIncrement
				+ "\n endIncrement : " + endIncrement

				);
	}

	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * Sets the context.
	 *
	 * @param context the new context
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * Gets the AV twill to reduce.
	 *
	 * @param End the End
	 * @return the AV twill to reduce
	 */
	public double getAVTwillToReduce(boolean End) {
		if (End) {
			return this.AVT_lastFeedbackEnd * this.AVT_deltaEnd;
		} else {
			return this.AVT_lastFeedbackStart * this.AVT_deltaStart;
		}
	}



	public boolean containedBy(Range range) {
		return range.getStart() <= this.getStart() && this.getEnd() <= range.getEnd();
	}




	/**
	 * Contains.
	 *
	 * @param d : the value to test
	 * @return -1 if lower, +1 if higher, 0 if contained
	 */
	public int contains(Double d) {
		if ((d > start || (d >= start && start_inclu)) && (d < end || (d <= end && end_inclu))) {
			return 0;
		} else if (d <= start) {
			return -1;
		} else {
			return 1;
		}
	}



	/**
	 * Compare to.
	 *
	 * @param i the i
	 * @return the int
	 */
	public int compareTo(Range i) {
		// TODO

		if (i.getStart() > this.getStart())
			return -1;
		if (i.getStart() < this.getStart())
			return 1;
		if (i.getStart() == this.getStart()) {
			if (i.isStart_inclu() == this.isStart_inclu()) {
				return 0;
			} else if (i.isStart_inclu()) {
				return 1;
			} else {
				return -1;
			}
		}
		return 0;

	}

	/**
	 * Gets the alpha factor.
	 *
	 * @return the alpha factor
	 */
	public double getAlphaFactor() {
		return alphaFactor;
	}

	/**
	 * Sets the alpha factor.
	 *
	 * @param alphaFactor the new alpha factor
	 */
	public void setAlphaFactor(double alphaFactor) {
		this.alphaFactor = alphaFactor;
	}

	/**
	 * Checks if is end inclu.
	 *
	 * @return true, if is end inclu
	 */
	public boolean isEnd_inclu() {
		return end_inclu;
	}

	/**
	 * Sets the end inclu.
	 *
	 * @param end_inclu the new end inclu
	 */
	public void setEnd_inclu(boolean end_inclu) {
		this.end_inclu = end_inclu;
	}

	/**
	 * Checks if is start inclu.
	 *
	 * @return true, if is start inclu
	 */
	public boolean isStart_inclu() {
		return start_inclu;
	}

	/**
	 * Sets the start inclu.
	 *
	 * @param start_inclu the new start inclu
	 */
	public void setStart_inclu(boolean start_inclu) {
		this.start_inclu = start_inclu;
	}

	/**
	 * Gets the lenght.
	 *
	 * @return length of the range.
	 */
	public double getLenght() {
		return end - start;
	}

	/**
	 * Sets the minimum size.
	 *
	 * @param v the new minimum size
	 */
	public void setMinimumSize(Percept v) {
		if (getLenght() < v.getMinMaxDistance() * Range.minLenghtRatio) {
			double distanceToAdd = (v.getMinMaxDistance() * Range.minLenghtRatio) - getLenght();
			this.setStart(start - (distanceToAdd / 2.0));
			this.setEnd(end + (distanceToAdd / 2.0));
		}
	}

	/**
	 * Add a margin based on a percentage of the lenght of the range.
	 *
	 * @param percent : percentage of the length
	 */
	private void addMargin(double percent) {
		this.setStart(start - (this.getLenght() * percent));
		this.setEnd(end + (this.getLenght() * percent));
	}

	/**
	 * Gets the nearest limit.
	 *
	 * @param d the d
	 * @return End is true, start is false
	 */
	public boolean getNearestLimit(double d) {
		return (Math.abs(d - start) < Math.abs(end - d)) ? false : true;
	}

	/**
	 * Gets the start.
	 *
	 * @return the start
	 */
	public double getStart() {
		return start;
	}

	/**
	 * Gets the end.
	 *
	 * @return the end
	 */
	public double getEnd() {
		return end;
	}

	public double getRange(String rangeType) {
		if (rangeType.equals("start")) {
			return start;
		} else if (rangeType.equals("end")) {
			return end;
		} else {
			return 0d;
		}
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
		this.oldValue = this.value;
		this.value = value;
	}

	/**
	 * Checks if is percept enum.
	 *
	 * @return true, if is percept enum
	 */
	public boolean isPerceptEnum() { // TODO delete
		return percept.isEnum();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		return this.compareTo(o);
	}

	public void setStart(double newStartValue) {


		if(((Double) this.start) != null && this.context!= null && this.percept!= null){
			world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
					Arrays.asList(this.context.getName(), this.percept.getName(), "OLD START", "" + this.start)));
		}


		if (newStartValue < percept.getMin()) {
			this.start = percept.getMin();

		} else {
			this.start = newStartValue;
		}
		

		
		

		if (this.context != null) {
			
			world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
					Arrays.asList(this.context.getName(), this.percept.getName(), "SET START", "" + newStartValue)));
			
			lastStartTickModification = this.context.getAmas().getCycle();
			this.percept.updateContextProjectionStart(this.context);
			context.getAmas().addLastmodifiedContext(context);

			if (!this.context.isDying() && !context.getAmas().getSpatiallyAlteredContextForUnityUI().contains(this.context)) {
				context.getAmas().addSpatiallyAlteredContextForUnityUI(this.context);
			}
		}



	}

	public void setEnd(double newEndValue) {

		if(((Double) this.end) != null && this.context!= null && this.percept!= null){
			world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
					Arrays.asList(this.context.getName(), this.percept.getName(), "OLD END", "" + this.end)));
		}


		if (newEndValue > percept.getMax()) {
			this.end = percept.getMax();
		} else {
			this.end = newEndValue;
		}

		

		

		if (this.context != null) {
			world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
					Arrays.asList(this.context.getName(), this.percept.getName(), "SET END", "" + newEndValue)));
			
			lastEndTickModification = context.getAmas().getCycle();
			this.percept.updateContextProjectionEnd(this.context);
			context.getAmas().addLastmodifiedContext(context);

			if (!this.context.isDying() && !context.getAmas().getSpatiallyAlteredContextForUnityUI().contains(this.context)) {
				context.getAmas().addSpatiallyAlteredContextForUnityUI(this.context);
			}
		}


	}


	public boolean contains(Double value, Double neighborhood) {
		//// System.out.println(context.getName() +" "+ percept.getName() + " " + value
		//// + " " + (start - neighborhood) + " " + start + " " + end + " " + (end +
		//// neighborhood));
		return Math.abs(value - getCenter()) < (getRadius() + neighborhood);
	}

	public boolean contains2(Double value) {
		return Math.abs(value - getCenter()) < getRadius();
	}
	//return start < d && d < end;

	public Range clone() throws CloneNotSupportedException {
		return (Range) super.clone();
	}

	public double getCenter() {
		return (end + start) / 2;
	}

	public double getRandom() {
		return start + getLenght()*Math.random();
	}

	public double getRadius() {
		return (end - start) / 2;
	}

	public int getLastStartTickModification() {
		return lastStartTickModification;
	}

	public int getLastEndTickModification() {
		return lastEndTickModification;
	}

	public double distance(Range otherRange) {
		double centerDistance = Math.abs(this.getCenter() - otherRange.getCenter());

		if (centerDistance + otherRange.getRadius() < this.getRadius()) {//inclusions
			return -otherRange.getRadius() * 2;
		} else if (centerDistance + this.getRadius() < otherRange.getRadius()) {//inclusions
			return -this.getRadius() * 2;
		} else {
			return centerDistance - this.getRadius() - otherRange.getRadius();
		}
		//return Math.abs(this.getCenter() - otherRange.getCenter()) - this.getRadius() - otherRange.getRadius();
	}

	public double centerDistance(Range otherRange) {
		double centerDistance = Math.abs(this.getCenter() - otherRange.getCenter());

		return centerDistance;
	}



	public double startDistance(Range otherRange) {
		return Math.abs(this.getStart() - otherRange.getEnd());
	}

	public double endDistance(Range otherRange) {
		return Math.abs(this.getEnd() - otherRange.getStart());
	}

	public double startDistance(double value) {
		return Math.abs(this.getStart() - value);
	}

	public double endDistance(double value) {
		return Math.abs(this.getEnd() - value);
	}

	public double distance(double value) {
		return Math.abs(this.getCenter() - value) - this.getRadius();
	}

	public double centerDistance(double value) {
		return value - this.getCenter();
	}

	public double overlapDistance(Range otherRange) {

		double distanceBetweenRanges = distance(otherRange);
		if (distanceBetweenRanges < 0) {
			return Math.abs(distanceBetweenRanges);
		} else {
			return 0.0;
		}
	}

	private double nonOverlapDistance(Range otherRange) {
		return this.getLenght() - overlapDistance(otherRange);
	}

	private double voidDistance(Range otherRange) {

		double distanceBetweenRanges = distance(otherRange);
		if (distanceBetweenRanges > 0) {
			return distanceBetweenRanges;
		} else {
			return 0.0;
		}
	}

	public boolean inNeighborhood() {
		return this.contains(percept.getValue(), context.getEnvironment().getContextNeighborhoodRadius(context, percept))
				|| this.contains(percept.getValue(), context.getEnvironment().getContextNeighborhoodRadius(context, percept));
	}
	
	public double getStartIncrement() {
		return startIncrement;
	}
	
	public double getEndIncrement() {
		return endIncrement;
	}

}
