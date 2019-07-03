package agents.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import agents.percept.Percept;
import kernel.World;
import ncs.NCS;
import utils.Pair;
import utils.TRACE_LEVEL;

// TODO: Auto-generated Javadoc
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
			this.setStart(Math.round(p.getValue()));
			this.setEnd(Math.round(p.getValue()));
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
//		this.AVT_deltaStart = (end - start) * AVT_minRatio + 0.0001;
//		this.AVT_deltaEnd = (end - start) * AVT_minRatio + 0.0001;
		this.AVT_deltaStart = getLenght() * 0.2 + 0.0001;
		this.AVT_deltaEnd = getLenght() * 0.2 + 0.0001;
		////// System.out.println(world.getScheduler().getTick() + "\t" +
		////// context.getName() + "\t" + percept.getName()+ "\t" + "Creation" + "\t" +
		////// "START" + "\t" + AVT_deltaStart);
		////// System.out.println(world.getScheduler().getTick() + "\t" +
		////// context.getName() + "\t" + percept.getName()+ "\t" + "Creation" + "\t" +
		////// "END" + "\t" + AVT_deltaEnd);

		startIncrement = 0.25 * world.getMappingErrorAllowed() * percept.getMinMaxDistance();
		endIncrement = startIncrement;
	}

	/**
	 * Extends the range to the specified target value.
	 *
	 * @param target : value to be included
	 * @param p      the p
	 * @return true if the context was extended, false else.
	 */
	public boolean extend(double target, Percept p) {
		int c = contains(target);
		if (c == -1) {
//			AVT_deltaStart = Math.abs(target-getStart());
//			AVT_lastFeedbackStart = 1;
			this.setStart(target);
			return true;
		} else if (c == 1) {
//			AVT_deltaEnd = Math.abs(target-getEnd());
//			AVT_lastFeedbackStart = 1;
			this.setEnd(target);
			return true;
		} else {
			return false;
		}

	}

	public void shrink(double target, Percept p) {

		if (Math.abs(getStart() - target) < Math.abs(getEnd() - target)) {
			this.setStart(target + this.getRadius() * 0.02);
		} else {
			this.setEnd(target - this.getRadius() * 0.02);
		}
	}

	/**
	 * Method called by the context agent. Allow the range to adapt itself.
	 *
	 * @param c           : the associated Context.
	 * @param oracleValue the oracle value
	 * @param p           the p
	 */
	public void adapt(Double oracleValue, double increment) {
		if (!isPerceptEnum()) {

			double minIncrement = Math.min(increment, getIncrement());

			staticAdapt(oracleValue, minIncrement);

			// adaptUsingAVT(c, oracleValue);
			// adaptWithoutAVT(c, oracleValue);

//			if (Range.useAVT) {
//				adaptUsingAVT(c, oracleValue);
//			} else {
//				adaptWithoutAVT(c, oracleValue);
//			}
		}
	}

	public void adapt(Double oracleValue) {
		if (!isPerceptEnum()) {

			staticAdapt(oracleValue, getIncrement());

			// adaptUsingAVT(c, oracleValue);
			// adaptWithoutAVT(c, oracleValue);

//			if (Range.useAVT) {
//				adaptUsingAVT(c, oracleValue);
//			} else {
//				adaptWithoutAVT(c, oracleValue);
//			}
		}
	}

	/**
	 * Adapt without AVT.
	 *
	 * @param c           the c
	 * @param oracleValue the oracle value
	 */
	private void adaptWithoutAVT(Context c, double oracleValue) {
		if (Math.abs(end - oracleValue) < Math.abs(oracleValue - start)) {
			adaptEndWithoutAVT(c, oracleValue);
		} else {
			adaptStartWithoutAVT(c, oracleValue);
		}
	}

	/**
	 * Adapt End without AVT.
	 *
	 * @param c           the c
	 * @param oracleValue the oracle value
	 */
	private void adaptEndWithoutAVT(Context c, double oracleValue) {
		if (contains(oracleValue) == 0.0) {
			this.setEnd(end - ((end - start) * percent_down));
		} else {
			this.setEnd(end + ((end - start) * percent_up));
		}
	}

	/**
	 * Adapt start without AVT.
	 *
	 * @param c           the c
	 * @param oracleValue the oracle value
	 */
	private void adaptStartWithoutAVT(Context c, double oracleValue) {
		if (contains(oracleValue) == 0.0) {

			this.setStart(start + ((end - start) * percent_up));
		} else {
			this.setStart(start - ((end - start) * percent_down));
		}
	}

	/**
	 * Adapt using AVT.
	 *
	 * @param c           the c
	 * @param oracleValue the oracle value
	 */
	private void adaptUsingAVT(Context c, Double oracleValue) {

		if (Math.abs(end - oracleValue) < Math.abs(oracleValue - start)) {
			adaptEndUsingAVT(oracleValue);
		} else {
			adaptStartUsingAVT(oracleValue);
		}

	}

	private void staticAdapt(double oracleValue, double increment) {
		if (Math.abs(end - oracleValue) < Math.abs(oracleValue - start)) {
			adaptEnd(oracleValue, increment);
		} else {
			adaptStart(oracleValue, increment);
		}
	}

	/**
	 * Adapt End using AVT.
	 *
	 * @param c           the c
	 * @param oracleValue the oracle value
	 */
	private void adaptEndUsingAVT(double oracleValue) {

		//// System.out.print(world.getScheduler().getTick() + "\t" + context.getName()
		//// + "\t" + percept.getName()+ "\t" + " AdaptEndUsingAVT");

		if (contains(oracleValue) == 0.0) { // If value is contained, it's a negative feedback for AVT (ie : we must
											// exclude the value)

			//// System.out.print( "\tContained : True" );

			if (AVT_lastFeedbackEnd == 1) {
				AVT_deltaEnd *= AVT_deceleration;
				//// System.out.print(" AVT_deceleration AVT_deltaEnd : " + "\t" +
				//// AVT_deltaEnd);
			} else {
				AVT_deltaEnd *= AVT_acceleration;
				//// System.out.print(" AVT_acceleration AVT_deltaEnd : " + "\t" +
				//// AVT_deltaEnd);
			}
			this.setEnd(end - AVT_deltaEnd);

			//// System.out.print("\tAVT_lastFeedbackEn\t" + AVT_lastFeedbackEnd);
			AVT_lastFeedbackEnd = -1;
			//// System.out.print("\t" + AVT_lastFeedbackEnd + "\n");

		} else {
			//// System.out.print( "\tContained : False" );
			if (AVT_lastFeedbackEnd == 1) {
				AVT_deltaEnd *= AVT_acceleration;
				//// System.out.print(" AVT_acceleration AVT_deltaEnd : " + "\t" +
				//// AVT_deltaEnd);
			} else {
				AVT_deltaEnd *= AVT_deceleration;
				//// System.out.print(" AVT_deceleration AVT_deltaEnd : " + "\t" +
				//// AVT_deltaEnd);
			}
			this.setEnd(end + AVT_deltaEnd);

			//// System.out.print("\tAVT_lastFeedbackEn\t" + AVT_lastFeedbackEnd);
			AVT_lastFeedbackEnd = 1;
			//// System.out.print("\t" + AVT_lastFeedbackEnd + "\n");
		}

	}

	public void endogenousAdaptEndUsingAVT() {

		//// System.out.print(world.getScheduler().getTick() + "\t" + context.getName()
		//// + "\t" + percept.getName()+ "\t" + " AdaptEndUsingAVT");

		AVT_deltaEnd *= AVT_deceleration;
		//// System.out.print(" AVT_deceleration AVT_deltaEnd : " + "\t" +
		//// AVT_deltaEnd);

		//// System.out.print("\tAVT_lastFeedbackEn\t" + AVT_lastFeedbackEnd);
		AVT_lastFeedbackEnd = 0;
		//// System.out.print("\t" + AVT_lastFeedbackEnd + "\n");

	}

	public void endogenousAdaptStartUsingAVT() {

		//// System.out.print(world.getScheduler().getTick() + "\t" + context.getName()
		//// + "\t" + percept.getName()+ "\t" + " AdaptEndUsingAVT");

		AVT_deltaStart *= AVT_deceleration;
		//// System.out.print(" AVT_deceleration AVT_deltaStart : " + "\t" +
		//// AVT_deltaStart);

		//// System.out.print("\tAVT_lastFeedbackStart\t" + AVT_lastFeedbackStart);
		AVT_lastFeedbackStart = 0;
		//// System.out.print("\t" + AVT_lastFeedbackStart + "\n");

	}

	private void adaptEnd(double oracleValue, double increment) {
		world.trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("INCREMENT ON END ADAPT", context.getName(), percept.getName(), "" + increment )));

		classicEndAdapt(oracleValue, increment);
		// adaptEndWithSplitting(oracleValue, increment);

	}

	private void classicEndAdapt(double oracleValue, double increment) {
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
				endIncrement /= 2;
			} else if (endCriticality == 0) { // positive feedback -> increment increases
				endIncrement = Math.min(percept.getRadiusContextForCreation(), endIncrement * 2);
				// endIncrement *=2;
			}

			this.setEnd(end - endIncrement);

		}

	}

	private void adaptEndWithSplitting(double oracleValue, double increment) {
		world.trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("" + increment, "INCREMENT")));

		ArrayList<Context> bordererContexts = new ArrayList<Context>();

		if (!(contains(oracleValue) == 0.0)) {

			for (Context ctxt : context.getAmas().getHeadAgent().getActivatedNeighborsContexts()) {

				world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
						Arrays.asList(context.getName(), "" + this.distance(ctxt.getRanges().get(this.percept)),
								"DISTANCE SPLIT", percept.getName(), ctxt.getName())));

				if (Math.abs(this.distance(ctxt.getRanges().get(this.percept))) <= increment
						&& ctxt.getRanges().get(this.percept).getCenter() > this.getCenter()) {

					boolean bordererContextTest = true;
					for (Percept pct : context.getAmas().getPercepts()) {

						if (pct != this.percept) {

							Range contextRangeOnOtherPercept = this.context.getRanges().get(pct);
							Range otherContextRangeOnOtherPercept = ctxt.getRanges().get(pct);

							bordererContextTest = bordererContextTest
									&& (contextRangeOnOtherPercept.distance(otherContextRangeOnOtherPercept) < 0);

						}
					}

					if (bordererContextTest) {
						world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(context.getName(), "END BORDER CONTEXT",
								percept.getName(), ctxt.getName())));
						bordererContexts.add(ctxt);
					}

				}

			}
			if (bordererContexts.size() > 0) {
				world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("END BORDER CONTEXT", percept.getName())));
				double minAlternativeIncrement = increment;
				double alternativeIncrement;
				for (Context ctxt : bordererContexts) {
					alternativeIncrement = Math.abs(this.distance(ctxt.getRanges().get(this.percept)));
					if (alternativeIncrement < minAlternativeIncrement) {
						minAlternativeIncrement = alternativeIncrement;
					}
				}
				this.setEnd(end + minAlternativeIncrement);
				growWithBorderContext();
			} else {
				this.setEnd(end + increment);
			}

			// this.setEnd(end + increment);
			// this.setEnd(end + getIncrementDependingOnNeighboorDistances("end"));
			// this.setEnd(end + getMaxIncrement("end"));
		} else {

			this.setEnd(end - increment);
			// this.setEnd(end - getIncrementDependingOnNeighboorDistances("end"));
		}

	}

	private void growWithBorderContext() {
//		Context newContext = new Context(world, world.getScheduler().getHeadAgent(), this.context);
//		world.getScheduler().getHeadAgent().addActivatedContext(newContext);
//		world.getScheduler().getHeadAgent().addRequestNeighbor(newContext);
	}

	/**
	 * Adapt start using AVT.
	 *
	 * @param c           the c
	 * @param oracleValue the oracle value
	 */
	private void adaptStartUsingAVT(double oracleValue) {

		//// System.out.print(world.getScheduler().getTick() + "\t" + context.getName()
		//// + "\t" + percept.getName()+ "\t" + " AdaptStartUsingAVT");

		if (contains(oracleValue) == 0.0) { // If value is contained, it's a negative feedback for AVT (ie : we must
											// exclude the value)

			//// System.out.print( "\tContained : True" );
			if (AVT_lastFeedbackStart == 1) {
				AVT_deltaStart *= AVT_deceleration;
				//// System.out.print(" AVT_deceleration AVT_deltaStart : " + "\t" +
				//// AVT_deltaStart);
			} else {
				AVT_deltaStart *= AVT_acceleration;
				//// System.out.print(" AVT_acceleration AVT_deltaStart : " + "\t" +
				//// AVT_deltaStart);
			}
			this.setStart(start + AVT_deltaStart);

			//// System.out.print("\tAVT_lastFeedbackStart\t" + AVT_lastFeedbackStart);
			AVT_lastFeedbackStart = -1;
			//// System.out.print("\t" + AVT_lastFeedbackStart + "\n");

		} else {

			//// System.out.print( "\tContained : False" );
			if (AVT_lastFeedbackStart == 1) {
				AVT_deltaStart *= AVT_acceleration;
				//// System.out.print(" AVT_acceleration AVT_deltaStart : " + "\t" +
				//// AVT_deltaStart);
			} else {
				AVT_deltaStart *= AVT_deceleration;
				//// System.out.print(" AVT_deceleration AVT_deltaStart : " + "\t" +
				//// AVT_deltaStart);
			}
			this.setStart(start - AVT_deltaStart);

			//// System.out.print("\tAVT_lastFeedbackStart\t" + AVT_lastFeedbackStart);
			AVT_lastFeedbackStart = 1;
			//// System.out.print("\t" + AVT_lastFeedbackStart + "\n");
		}

	}

	private void adaptStart(double oracleValue, double increment) {
		world.trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("INCREMENT ON END ADAPT", context.getName(), percept.getName(), "" + increment )));


		classicStartAdapt(oracleValue, increment);
		// adaptStartWithSplitting(oracleValue, increment);

	}

	private void classicStartAdapt(double oracleValue, double increment) {
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
				// startIncrement *=2;
			}

//			System.out.println(world.getScheduler().getTick() + " " +
//							this.context.getName() + " " +
//							this.percept.getName()+ " " +
//							lastStartDirection + " " +
//							" ++ " +
//							startCriticality + " " +
//							startIncrement 
//							);

			this.setStart(start - startIncrement);

			// this.setStart(start - getIncrementDependingOnNeighboorDistances("start"));
			// this.setStart(start - getMaxIncrement("start"));

		} else {
			if (lastStartDirection == 1) {
				startCriticality = 1;
			} else if (lastStartDirection == -1) {
				startCriticality = 0;
			}
			lastStartDirection = -1;

			if (startCriticality == 1) {
				startIncrement /= 2;
			} else if (startCriticality == 0) {
				startIncrement = Math.min(percept.getRadiusContextForCreation(), startIncrement * 2);
				// startIncrement *=2;
			}

//			System.out.println(world.getScheduler().getTick() + " " +
//					this.context.getName() + " " +
//					this.percept.getName()+ " " +
//					lastStartDirection + " " +
//					" -- " +
//					startCriticality + " " +
//					startIncrement 
//					);

			this.setStart(start + startIncrement);
		}

		// this.setStart(start + getIncrementDependingOnNeighboorDistances("start"));

		// this.adaptStartUsingAVT(oracleValue);
	}

	private void adaptStartWithSplitting(double oracleValue, double increment) {
		world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("" + increment, "INCREMENT")));
		ArrayList<Context> bordererContexts = new ArrayList<Context>();

		if (!(contains(oracleValue) == 0.0)) {

			for (Context ctxt : context.getAmas().getHeadAgent().getActivatedNeighborsContexts()) {

				if (Math.abs(this.distance(ctxt.getRanges().get(this.percept))) <= increment
						&& ctxt.getRanges().get(this.percept).getCenter() < this.getCenter()) {

					boolean bordererContextTest = true;
					for (Percept pct : context.getAmas().getPercepts()) {

						if (pct != this.percept) {

							Range contextRangeOnOtherPercept = this.context.getRanges().get(pct);
							Range otherContextRangeOnOtherPercept = ctxt.getRanges().get(pct);

							bordererContextTest = bordererContextTest
									&& (contextRangeOnOtherPercept.distance(otherContextRangeOnOtherPercept) < 0);

						}
					}

					if (bordererContextTest) {
						world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
								Arrays.asList("START BORDER CONTEXT", percept.getName(), ctxt.getName())));
						bordererContexts.add(ctxt);
					}

				}

			}
			if (bordererContexts.size() > 0) {
				world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("START BORDER CONTEXT", percept.getName())));
				double minAlternativeIncrement = increment;
				double alternativeIncrement;
				for (Context ctxt : bordererContexts) {
					alternativeIncrement = Math.abs(this.distance(ctxt.getRanges().get(this.percept)));
					if (alternativeIncrement < minAlternativeIncrement) {
						minAlternativeIncrement = alternativeIncrement;
					}
				}
				this.setStart(start - minAlternativeIncrement);
				growWithBorderContext();
			} else {
				this.setStart(start - increment);
			}

			// this.setStart(start - getIncrementDependingOnNeighboorDistances("start"));
			// this.setStart(start - getMaxIncrement("start"));

		} else {
			this.setStart(start + increment);
		}

		// this.setStart(start + getIncrementDependingOnNeighboorDistances("start"));

		// this.adaptStartUsingAVT(oracleValue);
	}

	public double getIncrement() {
		double increment = 0.25 * world.getMappingErrorAllowed() * percept.getMinMaxDistance();
		// double increment = 10*world.getIncrements()*this.getRadius();
		// world.trace(new
		// ArrayList<String>(Arrays.asList(this.getContext().getName(),percept.getName(),
		// "INCREMENT", ""+increment)));
		return increment;
	}

	private double getIncrementDependingOnNeighboorDistances(String rangeSide) {
		double increment = this.getRadius() / 2;
		double incrementSum = 0.0;
		ArrayList<Context> neighbors = new ArrayList<Context>();

		for (Context ctxt : context.getAmas().getHeadAgent().getActivatedNeighborsContexts()) {

			if (ctxt != this.context) {

				if (rangeSide.equals("start")) {
					double startDistance = startDistance(ctxt.getRanges().get(percept));
					if (startDistance < this.getRadius() / 2) {
						if (startDistance > incrementSum) {
							incrementSum = startDistance;
						}

					}
				} else if (rangeSide.equals("end")) {
					double endDistance = endDistance(ctxt.getRanges().get(percept));
					if (endDistance < this.getRadius() / 2) {
						if (endDistance > incrementSum) {
							incrementSum = endDistance;
						}
					}
				}
			}
		}
		if (incrementSum != 0.0) {
			increment = incrementSum;
		}

		return increment;
	}

	private double getMaxIncrement(String rangeSide) {
		double increment = world.getIncrements() * percept.getMinMaxDistance();
		double possibleIncrement;

		// System.out.println("INI INCREMENT " + increment);
		// System.out.println("NEIGHBORS " +
		// world.getScheduler().getHeadAgent().getActivatedNeighborsContexts().size());
		for (Context ctxt : context.getAmas().getHeadAgent().getActivatedNeighborsContexts()) {

			if (ctxt != this.context) {
//				possibleIncrement = this.distance(ctxt.getRanges().get(this.percept));
//				//System.out.println("POS INCREMENT " + possibleIncrement);
//				if(possibleIncrement<increment) {
//					increment = possibleIncrement;
//				}
				if (rangeSide.equals("start")) {
					if (this.getStart() > ctxt.getRanges().get(this.percept).getEnd()) {
						possibleIncrement = this.distance(ctxt.getRanges().get(this.percept));
						if (possibleIncrement < increment) {
							increment = possibleIncrement;
						}
					}
				} else if (rangeSide.equals("end")) {
					if (this.getEnd() < ctxt.getRanges().get(this.percept).getStart()) {
						possibleIncrement = this.distance(ctxt.getRanges().get(this.percept));
						if (possibleIncrement < increment) {
							increment = possibleIncrement;
						}
					}
				}
			}

		}

		// world.trace(new ArrayList<String>(Arrays.asList(this.context.getName(),
		// this.percept.getName(), rangeSide +" INCREMENT ", ""+increment)));
		return increment;
	}

	/**
	 * Simulate negative AVT feedback start.
	 *
	 * @param oracleValue the oracle value
	 * @return the double
	 */
	public double simulateNegativeAVTFeedbackStart(double oracleValue) {

		//// System.out.print(world.getScheduler().getTick() + "\t" + context.getName()
		//// + "\t" + percept.getName()+ "\t" );
		if (AVT_lastFeedbackStart == 1) {
			////// System.out.println("simulateNegativeAVTFeedbackStart :" +
			////// Math.abs(AVT_deltaStart * AVT_deceleration) + "\t" + "AVT_deltaStart : "
			////// + "\t" + AVT_deltaStart);
			return start + (AVT_deltaStart * AVT_deceleration);
		} else {
			////// System.out.println("simulateNegativeAVTFeedbackStart :" +
			////// Math.abs(AVT_deltaStart * AVT_acceleration) + "\t" + "AVT_deltaStart : "
			////// + "\t" + AVT_deltaStart);
			return start + (AVT_deltaStart * AVT_acceleration);
		}

	}

	/**
	 * Simulate negative AVT feedback End.
	 *
	 * @param oracleValue the oracle value
	 * @return the double
	 */
	public double simulateNegativeAVTFeedbackEnd(double oracleValue) {

		//// System.out.print(world.getScheduler().getTick() + "\t" + context.getName()
		//// + "\t" + percept.getName()+ "\t" );
		if (AVT_lastFeedbackEnd == 1) {
			////// System.out.println("simulateNegativeAVTFeedbackEnd :" +
			////// Math.abs(AVT_deltaEnd * AVT_deceleration) + "\t" + "AVT_deltaEnd : " +
			////// "\t" + AVT_deltaEnd);
			return end - (AVT_deltaEnd * AVT_deceleration);
		} else {
			////// System.out.println("simulateNegativeAVTFeedbackEnd :" +
			////// Math.abs(AVT_deltaEnd * AVT_acceleration) + "\t" + "AVT_deltaEnd : " +
			////// "\t" + AVT_deltaEnd);
			return end - (AVT_deltaEnd * AVT_acceleration);
		}

	}

	/**
	 * Check if the ranges is too small according to strategy.
	 * 
	 * @return boolean representing if the range is too small.
	 */
	public boolean isTooSmall() {
		

		return ((end - start) < (percept.getMappingErrorAllowed()*0.1)) && !this.isPerceptEnum();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
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

	/**
	 * Match border with.
	 *
	 * @param c the c
	 */
	public void matchBorderWith(Context c) {
		Range r = c.getRanges().get(percept);
		if (r.getStart() <= this.start && r.getEnd() >= this.end) {
			this.context.destroy();
		} else {
			if (Math.abs(r.getStart() - this.getEnd()) > Math.abs(r.getEnd() - this.getStart())) {
				// Change start
//				AVT_deltaStart = Math.abs(r.getEnd()-getStart());
//				AVT_lastFeedbackStart = 1;
				this.setStart(r.getEnd());
				this.setStart_inclu(!r.isEnd_inclu());

			} else {
				// Change End
//				AVT_deltaEnd = Math.abs(r.getStart()-getEnd());
//				AVT_lastFeedbackEnd = 1;
				this.setEnd(r.getStart());
				this.setEnd_inclu(!r.isStart_inclu());
			}
		}

	}

	public void matchBorderWithBestContext(Context bestContext) {
		////// System.out.println("Match border " + percept.getName());

		Range bestContextRanges = bestContext.getRanges().get(percept);

		if (bestContextRanges.getStart() <= this.start && this.end <= bestContextRanges.getEnd()) {

			////// System.out.println(context.getName() + " DIES");
			this.context.destroy();

		} else {

			if (Math.abs(bestContextRanges.getStart() - this.getEnd()) >= Math
					.abs(bestContextRanges.getEnd() - this.getStart())) {

				matchBorderOnOverlap(bestContextRanges, bestContextRanges.getEnd());
			} else {
				matchBorderOnOverlap(bestContextRanges, bestContextRanges.getStart());
			}
		}

	}

	public void adaptTowardsBorder(Context bestContext) {

		Range bestContextRanges = bestContext.getRanges().get(percept);

		if (bestContextRanges.getStart() <= this.start && this.end <= bestContextRanges.getEnd()) {

			this.context.destroy();

		} else {

			if (Math.abs(bestContextRanges.getStart() - this.getEnd()) >= Math
					.abs(bestContextRanges.getEnd() - this.getStart())) {

				setOnConcurentOverlap(bestContextRanges, bestContextRanges.getEnd());
			} else {
				setOnConcurentOverlap(bestContextRanges, bestContextRanges.getStart());
			}
		}

	}

	public void adaptOnOverlap(Range overlappingContextRanges, double border) {

		world.trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList(this.context.getName(), percept.getName(),
				"*********************************************************************************************************** ADAPT ON OVERLAP")));
		world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
				Arrays.asList(this.context.getName(), overlappingContextRanges.getContext().getName())));
		double increment = Math.min(Math.abs(this.distance(overlappingContextRanges)), getIncrement());

		HashMap<Percept, Pair<Double, Double>> newContextDimensions = new HashMap<Percept, Pair<Double, Double>>();
		HashMap<Percept, Pair<Double, Double>> newContextDimensionsBis = new HashMap<Percept, Pair<Double, Double>>();
		Double center;
		double length = increment;

		if (overlapDistance(overlappingContextRanges) > nonOverlapDistance(overlappingContextRanges)) {

			if (Math.abs(end - border) > Math.abs(border - start)) {
				center = (end - increment + end) / 2;
			} else {
				center = (start + increment + start) / 2;
			}

		} else {
			if (Math.abs(end - border) < Math.abs(border - start)) {
				center = (end - increment + end) / 2;
			} else {
				center = (start + increment + start) / 2;

			}
		}

		if (overlapDistance(overlappingContextRanges) > nonOverlapDistance(overlappingContextRanges)) {

			if (Math.abs(end - border) > Math.abs(border - start)) {
				adaptEnd(border, increment);
			} else {
				adaptStart(border, increment);
			}

		} else {
			if (Math.abs(end - border) < Math.abs(border - start)) {
				adaptEnd(border, increment);
			} else {
				adaptStart(border, increment);
			}
		}

		// if(this.context.getLocalModel().getCoef()[world.getScheduler().getPercepts().size()]
		// != 0.0) { // if model
		newContextDimensions.put(this.percept, new Pair<Double, Double>(center, length));
		newContextDimensionsBis.put(this.percept, new Pair<Double, Double>(center, length));
		ArrayList<Pair<Double, Double>> centersAndLengths = new ArrayList<Pair<Double, Double>>();
		boolean newContext = true;

		for (Percept pct : context.getAmas().getPercepts()) {
			if (pct != percept) {
				Context overlappingContext = overlappingContextRanges.getContext();
				newContext = newContext
						&& !context.getRanges().get(pct).containedBy(overlappingContext.getRanges().get(pct));

				if (newContext) {

					world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
							Arrays.asList(this.context.getName(), "" + context.getRanges().get(pct),
									overlappingContext.getRanges().get(pct) + "", pct.getName(), "RANGES")));
					world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
							Arrays.asList(this.context.getName(), "" + this.context.getRanges().get(pct).getLenght(),
									this.context.getRanges().get(pct)
											.overlapDistance(overlappingContext.getRanges().get(pct)) + "",
									pct.getName(), "LENGTHS")));

					centersAndLengths = this.context.getRanges().get(pct)
							.getCentersAndLengthsOfNonOverlapingZones(overlappingContext.getRanges().get(pct));
					if (centersAndLengths.isEmpty()) {
						newContext = false;
					} else if (centersAndLengths.size() == 1) {
						newContextDimensions.put(pct, new Pair<Double, Double>(centersAndLengths.get(0).getA(),
								centersAndLengths.get(0).getB()));
					} else if (centersAndLengths.size() == 2) {
						newContextDimensions.put(pct, new Pair<Double, Double>(centersAndLengths.get(0).getA(),
								centersAndLengths.get(0).getB()));
						newContextDimensionsBis.put(pct, new Pair<Double, Double>(centersAndLengths.get(1).getA(),
								centersAndLengths.get(1).getB()));
					}
				}
			}
		}

		if (newContext) {

			world.raiseNCS(NCS.CREATE_NEW_CONTEXT);
			for (Percept pct : context.getAmas().getPercepts()) {
				world.trace(TRACE_LEVEL.DEBUG, 
						new ArrayList<String>(Arrays.asList(pct.getName(), "" + newContextDimensions.get(pct).getA(),
								"" + newContextDimensions.get(pct).getB(), "NEW DIM")));
			}
			Context context = new Context(this.context.getAmas(), this.getContext(), newContextDimensions);

			if (centersAndLengths.size() == 2) {
				world.raiseNCS(NCS.CREATE_NEW_CONTEXT);
				for (Percept pct : context.getAmas().getPercepts()) {
					System.out.println("TEST");
					System.out.println(newContextDimensionsBis.get(pct).getA());
					System.out.println(newContextDimensionsBis.get(pct).getB());
					world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
							Arrays.asList(pct.getName(), "" + newContextDimensionsBis.get(pct).getA(),
									"" + newContextDimensionsBis.get(pct).getB(), "NEW DIM")));
				}
				Context contextBis = new Context(this.context.getAmas(), this.getContext(), newContextDimensionsBis);
			}
		}
		// }

	}

	public void setOnConcurentOverlap(Range overlappingContextRanges, double border) {

		world.trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList(this.context.getName(), percept.getName(),
				"*********************************************************************************************************** SET ON OVERLAP")));
		world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
				Arrays.asList(this.context.getName(), overlappingContextRanges.getContext().getName())));

		double increment = Math.min(Math.abs(this.distance(overlappingContextRanges)), getIncrement());

		HashMap<Percept, Pair<Double, Double>> newContextDimensions = new HashMap<Percept, Pair<Double, Double>>();
		HashMap<Percept, Pair<Double, Double>> newContextDimensionsBis = new HashMap<Percept, Pair<Double, Double>>();
		Double center;
		double length;

		if (overlapDistance(overlappingContextRanges) > nonOverlapDistance(overlappingContextRanges)) {

			if (Math.abs(end - border) > Math.abs(border - start)) {
				center = (border + end) / 2;
				length = Math.abs(end - border);
			} else {
				center = (start + border) / 2;
				length = Math.abs(start - border);
			}

		} else {
			if (Math.abs(end - border) < Math.abs(border - start)) {
				center = (border + end) / 2;
				length = Math.abs(end - border);
			} else {
				center = (start + border) / 2;
				length = Math.abs(start - border);

			}
		}

		if (overlapDistance(overlappingContextRanges) > nonOverlapDistance(overlappingContextRanges)) {

			if (Math.abs(end - border) > Math.abs(border - start)) {
				setEnd(border);
			} else {
				setStart(border);
			}

		} else {
			if (Math.abs(end - border) < Math.abs(border - start)) {
				setEnd(border);
			} else {
				setStart(border);
			}
		}

		// if(this.context.getLocalModel().getCoef()[world.getScheduler().getPercepts().size()]
		// != 0.0) { // if model
		newContextDimensions.put(this.percept, new Pair<Double, Double>(center, length));
		newContextDimensionsBis.put(this.percept, new Pair<Double, Double>(center, length));
		ArrayList<Pair<Double, Double>> centersAndLengths = new ArrayList<Pair<Double, Double>>();
		boolean newContext = true;

		for (Percept pct : context.getAmas().getPercepts()) {
			if (pct != percept) {
				Context overlappingContext = overlappingContextRanges.getContext();
				newContext = newContext
						&& !context.getRanges().get(pct).containedBy(overlappingContext.getRanges().get(pct));

				if (newContext) {

					world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
							Arrays.asList(this.context.getName(), "" + context.getRanges().get(pct),
									overlappingContext.getRanges().get(pct) + "", pct.getName(), "RANGES")));
					world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
							Arrays.asList(this.context.getName(), "" + this.context.getRanges().get(pct).getLenght(),
									this.context.getRanges().get(pct)
											.overlapDistance(overlappingContext.getRanges().get(pct)) + "",
									pct.getName(), "LENGTHS")));

					centersAndLengths = this.context.getRanges().get(pct)
							.getCentersAndLengthsOfNonOverlapingZones(overlappingContext.getRanges().get(pct));
					if (centersAndLengths.isEmpty()) {
						newContext = false;
					} else if (centersAndLengths.size() == 1) {
						newContextDimensions.put(pct, new Pair<Double, Double>(centersAndLengths.get(0).getA(),
								centersAndLengths.get(0).getB()));
					} else if (centersAndLengths.size() == 2) {
						newContextDimensions.put(pct, new Pair<Double, Double>(centersAndLengths.get(0).getA(),
								centersAndLengths.get(0).getB()));
						newContextDimensionsBis.put(pct, new Pair<Double, Double>(centersAndLengths.get(1).getA(),
								centersAndLengths.get(1).getB()));
					}
				}
			}
		}

		if (newContext) {

			world.raiseNCS(NCS.CREATE_NEW_CONTEXT);
			for (Percept pct : context.getAmas().getPercepts()) {
				world.trace(TRACE_LEVEL.DEBUG, 
						new ArrayList<String>(Arrays.asList(pct.getName(), "" + newContextDimensions.get(pct).getA(),
								"" + newContextDimensions.get(pct).getB(), "NEW DIM")));
			}
			Context context = new Context(this.context.getAmas(), this.getContext(), newContextDimensions);

			if (centersAndLengths.size() == 2) {

				world.raiseNCS(NCS.CREATE_NEW_CONTEXT);
				for (Percept pct : this.context.getAmas().getPercepts()) {
					System.out.println("TEST");
					System.out.println(newContextDimensionsBis.get(pct).getA());
					System.out.println(newContextDimensionsBis.get(pct).getB());

					world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
							Arrays.asList(pct.getName(), "" + newContextDimensionsBis.get(pct).getA(),
									"" + newContextDimensionsBis.get(pct).getB(), "NEW DIM")));
				}
				Context contextBis = new Context(this.context.getAmas(), this.getContext(), newContextDimensionsBis);
			}
		}
		// }

	}

	public boolean containedBy(Range range) {
		return range.getStart() <= this.getStart() && this.getEnd() <= range.getEnd();
	}

	public ArrayList<Pair<Double, Double>> getCentersAndLengthsOfNonOverlapingZones(Range overlappingRange) {

		ArrayList<Pair<Double, Double>> centersAndLengths = new ArrayList<Pair<Double, Double>>();

		world.trace(TRACE_LEVEL.EVENT, new ArrayList<String>(
				Arrays.asList(this.context.getName(), overlappingRange.getContext().getName(), "SEEK NON OVERLAPING")));

		if (this.getStart() < overlappingRange.getStart() && overlappingRange.getEnd() < this.getEnd()) {

			world.trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("CONTAINED", this.percept.getName())));
			double center1 = (this.getStart() + overlappingRange.getStart()) / 2;
			double center2 = (overlappingRange.getEnd() + this.getEnd()) / 2;
			double length1 = overlappingRange.getStart() - this.getStart();
			double length2 = this.getEnd() - overlappingRange.getEnd();
			centersAndLengths.add(new Pair<Double, Double>(center1, length1));
			centersAndLengths.add(new Pair<Double, Double>(center2, length2));
		} else if (this.getStart() < overlappingRange.getStart() && overlappingRange.getStart() < this.getEnd()) {

			world.trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("START", this.percept.getName())));
			double center = (this.getStart() + overlappingRange.getStart()) / 2;
			double length = overlappingRange.getStart() - this.getStart();
			centersAndLengths.add(new Pair<Double, Double>(center, length));
		} else if (this.getStart() < overlappingRange.getEnd() && overlappingRange.getEnd() < this.getEnd()) {

			world.trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("END", this.percept.getName())));
			double center = (overlappingRange.getEnd() + this.getEnd()) / 2;
			double length = this.getEnd() - overlappingRange.getEnd();
			centersAndLengths.add(new Pair<Double, Double>(center, length));
		}

		return centersAndLengths;

	}

	private void matchBorderOnOverlap(Range bestContextRanges, double border) {

		if (overlapDistance(bestContextRanges) > nonOverlapDistance(bestContextRanges)) {

			if (Math.abs(end - border) > Math.abs(border - start)) {
				setEnd(border);
			} else {
				setStart(border);
			}

		} else {
			if (Math.abs(end - border) < Math.abs(border - start)) {
				setEnd(border);
			} else {
				setStart(border);
			}
		}

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
	public boolean isPerceptEnum() {
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

		if (context != null && percept != null) {
			////// System.out.println(context.getName() + " " + percept.getName() + " START
			////// " + (Math.abs(newStartValue-this.start)));
		}

//		if ((Double) newStartValue != null) {
//			if (newStartValue < percept.getMin()) {
//				this.start = percept.getMin();
//
//			} else {
//				this.start = newStartValue;
//			}
//		} else {
//			this.start = newStartValue;
//		}
		
		this.start = newStartValue;
		
		

		if (this.context != null) {
			
			world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
					Arrays.asList(this.context.getName(), this.percept.getName(), "SET START", "" + newStartValue)));
			
			lastStartTickModification = this.context.getAmas().getCycle();
			this.percept.updateContextProjectionStart(this.context);
			this.percept.updateSortedRanges(this.context, "start");
			context.getAmas().addLastmodifiedContext(context);

			if (!this.inNeighborhood()) {
				// if(!this.contains(percept.getValue(), percept.getRadiusContextForCreation()))
				// {
				if (!this.context.getNonValidNeighborPercepts().contains(this.percept)) {
					this.context.addNonValidNeighborPercept(this.percept);
				}
			} else {
				if (this.context.getNonValidNeighborPercepts().contains(this.percept)) {
					this.context.removeNonValidNeighborPercept(this.percept);
				}
			}

			this.context.updateRequestNeighborState();

			if (!this.contains2(percept.getValue())) {
				if (!this.context.getNonValidPercepts().contains(this.percept)) {
					this.context.addNonValidPercept(this.percept);
				}
			} else {
				if (this.context.getNonValidPercepts().contains(this.percept)) {
					this.context.removeNonValidPercept(this.percept);
				}
			}

			this.context.updateActivatedContextsCopyForUpdate();

			

			if (!this.context.isDying() && !context.getAmas().getSpatiallyAlteredContext().contains(this.context)) {
				context.getAmas().addSpatiallyAlteredContext(this.context);
			}
		}

		// NCSDetection_Uselessness();

	}

	public void setEnd(double newEndValue) {

		if (context != null && percept != null) {
			////// System.out.println(context.getName() + " " + percept.getName() + " END "
			////// + (Math.abs(newEndValue-this.end)));
		}
//		if ((Double) newEndValue != null) {
//			if (newEndValue > percept.getMax()) {
//				this.end = percept.getMax();
//			} else {
//				this.end = newEndValue;
//			}
//		} else {
//			this.end = newEndValue;
//		}
		
		this.end = newEndValue;
		

		if (this.context != null) {
			world.trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
					Arrays.asList(this.context.getName(), this.percept.getName(), "SET END", "" + newEndValue)));
			
			lastEndTickModification = context.getAmas().getCycle();
			this.percept.updateContextProjectionEnd(this.context);
			this.percept.updateSortedRanges(this.context, "end");
			context.getAmas().addLastmodifiedContext(context);

			if (!this.inNeighborhood()) {
				// if(!this.contains(percept.getValue(), percept.getRadiusContextForCreation()))
				// {
				if (!this.context.getNonValidNeighborPercepts().contains(this.percept)) {
					this.context.addNonValidNeighborPercept(this.percept);
				}
			} else {
				if (this.context.getNonValidNeighborPercepts().contains(this.percept)) {
					this.context.removeNonValidNeighborPercept(this.percept);
				}
			}

			this.context.updateRequestNeighborState();

			if (!this.contains2(percept.getValue())) {
				if (!this.context.getNonValidPercepts().contains(this.percept)) {
					this.context.addNonValidPercept(this.percept);
				}
			} else {
				if (this.context.getNonValidPercepts().contains(this.percept)) {
					this.context.removeNonValidPercept(this.percept);
				}
			}

			this.context.updateActivatedContextsCopyForUpdate();

			

			if (!this.context.isDying() && !context.getAmas().getSpatiallyAlteredContext().contains(this.context)) {
				context.getAmas().addSpatiallyAlteredContext(this.context);
			}
		}

		// NCSDetection_Uselessness();
	}

	public void NCSDetection_Uselessness() {
		if (context != null) {
			if (this.isTooSmall()) {
				context.solveNCS_Uselessness();
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
		return Math.abs(value - getCenter()) <= getRadius();
	}

	public Range clone() throws CloneNotSupportedException {
		return (Range) super.clone();
	}

	public double getCenter() {
		return (end + start) / 2;
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
		return Math.abs(this.getCenter() - otherRange.getCenter()) - this.getRadius() - otherRange.getRadius();
	}

	public double distanceForVolume(Range otherRange) {
		double centerDistance = Math.abs(this.getCenter() - otherRange.getCenter());

		if (centerDistance + otherRange.getRadius() < this.getRadius()) {
			return otherRange.getRadius() * 2;
		} else if (centerDistance + this.getRadius() < otherRange.getRadius()) {
			return this.getRadius() * 2;
		} else {
			return centerDistance - this.getRadius() - otherRange.getRadius();
		}

	}

	public double distanceForMaxOrMin(Range otherRange) {
		double centerDistance = Math.abs(this.getCenter() - otherRange.getCenter());

		if ((centerDistance + otherRange.getRadius() < this.getRadius())
				|| (centerDistance + this.getRadius() < otherRange.getRadius())) {
			return 0.0;
		} else {
			return centerDistance - this.getRadius() - otherRange.getRadius();
		}

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
		return this.contains(percept.getValue(), context.getEnvironment().getContextCreationNeighborhood(context, percept))
				|| this.contains(percept.getValue(), context.getEnvironment().getContextCreationNeighborhood(context, percept));
	}

}
