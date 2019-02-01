package mas.agents.context;

import java.io.Serializable;

import org.hamcrest.core.IsNull;

import mas.agents.percept.Percept;
import mas.kernel.World;
import mas.agents.Agent;
import mas.agents.messages.MessageType;

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
 /** The Constant startLenghtRatio. */
 /*
								 * The weight in an interpolation : the impact
								 * on action for a +1 change in this range value
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
	//Only used if useAVT == false
	static public double percent_up = 0.2;
	static public double percent_down = 0.1;
	/*---------------------------------*/
	
	public double increment_up = 0.05;
	public double increment_down = 0.05;

	
	/**
	 * Instantiates a new range.
	 *
	 * @param context the context
	 * @param start the start
	 * @param end the end
	 * @param extendedrangeatcreation the extendedrangeatcreation
	 * @param start_inclu the start inclu
	 * @param end_inclu the end inclu
	 * @param p the p
	 */
	public Range(Context context, double start, double end,
			double extendedrangeatcreation, boolean start_inclu,
			boolean end_inclu, Percept p, World wrld) {
		super();
		
		world= wrld;
		
		AVT_deceleration = context.getWorld().getAVT_deceleration();
		AVT_acceleration = context.getWorld().getAVT_acceleration();
		AVT_minRatio = context.getWorld().getAVT_percentAtStart();

		this.percept = p;
		if (isPerceptEnum()) {
			this.setStart_inclu(start_inclu);
			this.setEnd_inclu(end_inclu);
			this.setStart( Math.round(p.getValue()));
			this.setEnd( Math.round(p.getValue()));
		} else {
			this.setStart_inclu(start_inclu);
			this.setEnd_inclu(end_inclu);
			this.setStart(start - Math.abs(extendedrangeatcreation * start));
			this.setEnd( end + Math.abs(extendedrangeatcreation * end));
		}
		this.context = context;
		id = maxid;
		maxid++;
		
		/*Initialization of AVT : a better way to do that should be developped*/
//		this.AVT_deltaStart = (end - start) * AVT_minRatio + 0.0001;
//		this.AVT_deltaEnd = (end - start) * AVT_minRatio + 0.0001;
		this.AVT_deltaStart = getLenght() * 0.2 + 0.0001;
		this.AVT_deltaEnd = getLenght() * 0.2 + 0.0001;
		System.out.println(world.getScheduler().getTick() + "\t" + context.getName() + "\t" + percept.getName()+ "\t" + "Creation" + "\t" + "START" + "\t" + AVT_deltaStart);
		System.out.println(world.getScheduler().getTick() + "\t" + context.getName() + "\t" + percept.getName()+ "\t" + "Creation" + "\t" + "END" + "\t" + AVT_deltaEnd);
		

	}
	
	/**
	 * Instantiates a new range.
	 *
	 * @param r the r
	 */
	public Range(Range r) {
		super();
		this.setStart( r.start);
		this.setEnd( r.end);
		this.start_inclu = r.start_inclu;
		this.end_inclu = r.end_inclu;
		this.value = r.value;
		this.alphaFactor = r.alphaFactor;
		this.oldValue = r.oldValue;
		this.context = r.context;
		this.percept = new Percept(r.percept);
		this.id = r.id;
		this.AVT_acceleration = r.AVT_acceleration;
		this.AVT_deceleration = r.AVT_deceleration;
		this.AVT_deltaEnd = r.AVT_deltaEnd;
		this.AVT_deltaStart = r.AVT_deltaStart;
		this.AVT_lastFeedbackEnd = r.AVT_lastFeedbackEnd;
		this.AVT_lastFeedbackStart = r.AVT_lastFeedbackStart;
		
	}


	/**
	 * Extends the range to the specified target value.
	 *
	 * @param target : value to be included
	 * @param p the p
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

		if(Math.abs(getStart() - target) < Math.abs(getEnd() - target)) {
			this.setStart(target + this.getRadius()*0.02);
		}
		else {
			this.setEnd(target - this.getRadius()*0.02);
		}
	}

	/**
	 * Method called by the context agent. Allow the range to adapt itself.
	 *
	 * @param c : the associated Context.
	 * @param oracleValue the oracle value
	 * @param p the p
	 */
	public void adapt(Double oracleValue) {
		if (!isPerceptEnum()) {
	
			
			staticAdapt(oracleValue);
			
			//adaptUsingAVT(c, oracleValue);
			//adaptWithoutAVT(c, oracleValue);
			
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
	 * @param c the c
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
	 * @param c the c
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
	 * @param c the c
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
	 * @param c the c
	 * @param oracleValue the oracle value
	 */
	private void adaptUsingAVT(Context c, Double oracleValue) {
		
		if (Math.abs(end - oracleValue) < Math.abs(oracleValue - start)) {
			adaptEndUsingAVT(c, oracleValue);
		} else {
			adaptStartUsingAVT(c, oracleValue);
		}
		
		
	}
	
	
	
	private void staticAdapt(double oracleValue) {
		if (Math.abs(end - oracleValue) < Math.abs(oracleValue - start)) {
			adaptEnd(oracleValue);
		} else {
			adaptStart(oracleValue);
		}
	}
	
	/**
	 * Adapt End using AVT.
	 *
	 * @param c the c
	 * @param oracleValue the oracle value
	 */
	private void adaptEndUsingAVT(Context c, double oracleValue) {

		System.out.print(world.getScheduler().getTick() + "\t" + context.getName() + "\t" + percept.getName()+ "\t" + " AdaptEndUsingAVT");
		
		if (contains(oracleValue) == 0.0) {  //If value is contained, it's a negative feedback for AVT (ie : we must exclude the value)

			System.out.print( "\tContained : True" );

			if (AVT_lastFeedbackEnd == 1) {
				AVT_deltaEnd *= AVT_deceleration;
				System.out.print(" AVT_deceleration AVT_deltaEnd : " + "\t" +  AVT_deltaEnd);
			} else {
				AVT_deltaEnd *= AVT_acceleration;
				System.out.print(" AVT_acceleration AVT_deltaEnd : " + "\t" +  AVT_deltaEnd);
			}
			this.setEnd(end - AVT_deltaEnd);

			
			System.out.print("\tAVT_lastFeedbackEn\t" + AVT_lastFeedbackEnd);
			AVT_lastFeedbackEnd = -1;
			System.out.print("\t" + AVT_lastFeedbackEnd + "\n");

		} else {
			System.out.print( "\tContained : False" );
			if (AVT_lastFeedbackEnd == 1) {
				AVT_deltaEnd *= AVT_acceleration;
				System.out.print(" AVT_acceleration AVT_deltaEnd : " + "\t" +  AVT_deltaEnd);
			} else {
				AVT_deltaEnd *= AVT_deceleration;
				System.out.print(" AVT_deceleration AVT_deltaEnd : " + "\t" +  AVT_deltaEnd);
			}
			this.setEnd(end + AVT_deltaEnd);

			System.out.print("\tAVT_lastFeedbackEn\t" + AVT_lastFeedbackEnd);
			AVT_lastFeedbackEnd = 1;
			System.out.print("\t" + AVT_lastFeedbackEnd + "\n");
		}		

		
	}
	
	public void endogenousAdaptEndUsingAVT() {

		System.out.print(world.getScheduler().getTick() + "\t" + context.getName() + "\t" + percept.getName()+ "\t" + " AdaptEndUsingAVT");
		
		AVT_deltaEnd *= AVT_deceleration;
		System.out.print(" AVT_deceleration AVT_deltaEnd : " + "\t" +  AVT_deltaEnd);
		
		System.out.print("\tAVT_lastFeedbackEn\t" + AVT_lastFeedbackEnd);
		AVT_lastFeedbackEnd = 0;
		System.out.print("\t" + AVT_lastFeedbackEnd + "\n");

		
	}
	
	public void endogenousAdaptStartUsingAVT() {

		System.out.print(world.getScheduler().getTick() + "\t" + context.getName() + "\t" + percept.getName()+ "\t" + " AdaptEndUsingAVT");
		
		AVT_deltaStart *= AVT_deceleration;
		System.out.print(" AVT_deceleration AVT_deltaStart : " + "\t" +  AVT_deltaStart);
		
		System.out.print("\tAVT_lastFeedbackStart\t" + AVT_lastFeedbackStart);
		AVT_lastFeedbackStart = 0;
		System.out.print("\t" + AVT_lastFeedbackStart + "\n");

		
	}
	
	private void adaptEnd(double oracleValue) {

		if (!(contains(oracleValue) == 0.0)) {  //If value is contained, it's a negative feedback for AVT (ie : we must exclude the value)

			//this.setEnd(end + (world.getContextGrowingPercent()*2*this.getRadius()));
			
			if(world.getScheduler().getTick()>world.tickThreshol) {
				this.setEnd(end + world.getIncrements()*percept.getMinMaxDistance()*getLenght()); 
			}
			else {
				this.setEnd(end + world.getIncrements()*percept.getMinMaxDistance()); 
			}
			
		} 
		else {
			if(world.getScheduler().getTick()>world.tickThreshol) {
				this.setEnd(end - world.getIncrements()*percept.getMinMaxDistance()*getLenght());
			}
			else {
				this.setEnd(end - world.getIncrements()*percept.getMinMaxDistance());
			}
			
		}


	}

	/**
	 * Adapt start using AVT.
	 *
	 * @param c the c
	 * @param oracleValue the oracle value
	 */
	private void adaptStartUsingAVT(Context c, double oracleValue) {

		System.out.print(world.getScheduler().getTick() + "\t" + context.getName() + "\t" + percept.getName()+ "\t" + " AdaptStartUsingAVT");
		
		
		if (contains(oracleValue) == 0.0) {  //If value is contained, it's a negative feedback for AVT (ie : we must exclude the value)

			System.out.print( "\tContained : True" );
			if (AVT_lastFeedbackStart == 1) {
				AVT_deltaStart *= AVT_deceleration;
				System.out.print(" AVT_deceleration AVT_deltaStart : " + "\t" +  AVT_deltaStart);
			} else {
				AVT_deltaStart *= AVT_acceleration;
				System.out.print(" AVT_acceleration AVT_deltaStart : " + "\t" +  AVT_deltaStart);
			}
			this.setStart(start + AVT_deltaStart);

			System.out.print("\tAVT_lastFeedbackStart\t" + AVT_lastFeedbackStart);
			AVT_lastFeedbackStart = -1;
			System.out.print("\t" + AVT_lastFeedbackStart + "\n");

		} else {

			System.out.print( "\tContained : False" );
			if (AVT_lastFeedbackStart == 1) {
				AVT_deltaStart *= AVT_acceleration;
				System.out.print(" AVT_acceleration AVT_deltaStart : " + "\t" +  AVT_deltaStart);
			} else {
				AVT_deltaStart *= AVT_deceleration;
				System.out.print(" AVT_deceleration AVT_deltaStart : " + "\t" +  AVT_deltaStart);
			}
			this.setStart(start - AVT_deltaStart);

			System.out.print("\tAVT_lastFeedbackStart\t" + AVT_lastFeedbackStart);
			AVT_lastFeedbackStart = 1;
			System.out.print("\t" + AVT_lastFeedbackStart + "\n");
		}		

	}
	
	private void adaptStart(double oracleValue) {

		if (!(contains(oracleValue) == 0.0)) {  //If value is contained, it's a negative feedback for AVT (ie : we must exclude the value)

			//this.setStart(start - (world.getContextGrowingPercent()*2*this.getRadius()));
			if(world.getScheduler().getTick()>world.tickThreshol) {
				this.setStart(start - world.getIncrements()*percept.getMinMaxDistance()*getLenght());
			}
			else {
				this.setStart(start - world.getIncrements()*percept.getMinMaxDistance());
			}
			

		}else {
			if(world.getScheduler().getTick()>world.tickThreshol) {
				this.setStart(start + world.getIncrements()*percept.getMinMaxDistance()*getLenght());
			}
			else {
				this.setStart(start + world.getIncrements()*percept.getMinMaxDistance());
			}
			
		}
			

	}
	
	/**
	 * Simulate negative AVT feedback start.
	 *
	 * @param oracleValue the oracle value
	 * @return the double
	 */
	public double simulateNegativeAVTFeedbackStart(double oracleValue) {
		
		System.out.print(world.getScheduler().getTick() + "\t" + context.getName() + "\t" + percept.getName()+ "\t" );
		if (AVT_lastFeedbackStart == 1) {
			System.out.println("simulateNegativeAVTFeedbackStart :" + Math.abs(AVT_deltaStart * AVT_deceleration) + "\t" + "AVT_deltaStart : " + "\t" +  AVT_deltaStart);
			return start + (AVT_deltaStart * AVT_deceleration);
		} else {
			System.out.println("simulateNegativeAVTFeedbackStart :" + Math.abs(AVT_deltaStart * AVT_acceleration) + "\t" + "AVT_deltaStart : " + "\t" +  AVT_deltaStart);
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
		
		System.out.print(world.getScheduler().getTick() + "\t" + context.getName() + "\t" + percept.getName()+ "\t" );
		if (AVT_lastFeedbackEnd == 1) {
			System.out.println("simulateNegativeAVTFeedbackEnd :" + Math.abs(AVT_deltaEnd * AVT_deceleration) + "\t" + "AVT_deltaEnd : " + "\t" +  AVT_deltaEnd);
			return end - (AVT_deltaEnd * AVT_deceleration);
		} else {
			System.out.println("simulateNegativeAVTFeedbackEnd :" + Math.abs(AVT_deltaEnd * AVT_acceleration) + "\t" + "AVT_deltaEnd : " + "\t" +  AVT_deltaEnd);
			return end - (AVT_deltaEnd * AVT_acceleration);
		}

	}
	
	
	

	/**
	 * Check if the ranges is too small according to strategy.
	 * 
	 * @return boolean representing if the range is too small.
	 */
	public boolean isTooSmall() {
		if((end - start) < mininimalRange && (end - start)>0) {
			System.out.println("£££££££££££££££££££££££££££££ mininimalRange :" + mininimalRange + " ~~~ " + (end - start));
		}
		
		return (end - start) < mininimalRange && !this.isPerceptEnum();
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return ((start_inclu ? "[" : "]") + start + "," + end
				+ (!end_inclu ? "[" : "]") + "  Current value : " + percept.getValue()
				+ "  AVT_Start : " + AVT_deltaStart
				+ "  AVT_End : " + AVT_deltaEnd );
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
			this.context.die();
		} else {
			if (Math.abs(r.getStart() - this.getEnd()) > Math.abs(r.getEnd() - this.getStart())) {
				//Change start
//				AVT_deltaStart = Math.abs(r.getEnd()-getStart());
//				AVT_lastFeedbackStart = 1;
				this.setStart(r.getEnd());
				this.setStart_inclu(!r.isEnd_inclu());
				
			} else {
				//Change End
//				AVT_deltaEnd = Math.abs(r.getStart()-getEnd());
//				AVT_lastFeedbackEnd = 1;
				this.setEnd( r.getStart());
				this.setEnd_inclu(!r.isStart_inclu());
			}
		}

	}
	
	public void adaptTowardsBorder(Context bestContext) {
		
		System.out.println("Adapt towards border " + percept.getName());
		
		Range bestContextRanges = bestContext.getRanges().get(percept);
		
		if (bestContextRanges.getStart() <= this.start &&  this.end <= bestContextRanges.getEnd() ) {
			
			System.out.println(context.getName() + " DIES");
			this.context.die();
			
		} else {
			
			if (Math.abs(bestContextRanges.getStart() - this.getEnd()) >= Math.abs(bestContextRanges.getEnd() - this.getStart())) {
		
				adaptOnOverlap(bestContextRanges, bestContextRanges.getEnd());
			} else {
				adaptOnOverlap(bestContextRanges, bestContextRanges.getStart());
			}
		}

	}
	
	
	private void adaptOnOverlap(Range bestContextRanges, double border) {
		
		if(overlapDistance(bestContextRanges) > nonOverlapDistance(bestContextRanges)) {
			
			if (Math.abs(end - border) > Math.abs(border - start)) {
				adaptEnd(border);
			} else {
				adaptStart(border);
			}
			
		}
		else {
			if (Math.abs(end - border) < Math.abs(border - start)) {
				adaptEnd(border);
			} else {
				adaptStart(border);
			}
		}
		
	}

	/**
	 * Contains.
	 *
	 * @param d            : the value to test
	 * @return -1 if lower, +1 if higher, 0 if contained
	 */
	public int contains(Double d) {
		if (  (d > start || (d >= start && start_inclu)) && (d < end || (d <= end && end_inclu))  ) {
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
			double distanceToAdd = (v.getMinMaxDistance() * Range.minLenghtRatio)
					- getLenght();
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
		if(rangeType.equals("start")) {
			return start;
		}
		else if(rangeType.equals("end")) {
			return end;
		}
		else {
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

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		return this.compareTo(o);
	}
	
	private void setStart(double newStartValue) {
		if(context !=null && percept !=null) {
			System.out.println(context.getName() + " " + percept.getName() + " START " + (Math.abs(newStartValue-this.start)));
		}
		this.start = newStartValue;
		if(world != null) {
			lastStartTickModification = world.getScheduler().getTick();
		}
		

		if(this.context != null) {
			this.percept.updateContextProjectionStart(this.context);
			this.percept.updateSortedRanges(this.context, "start");
			context.getWorld().getScheduler().addLastmodifiedContext(context);
		}
		
	}
	
	private void setEnd(double newEndValue) {
		if(context !=null && percept !=null ) {
			System.out.println(context.getName() + " " + percept.getName() + " END " + (Math.abs(newEndValue-this.end)));
		}
		
		this.end = newEndValue;

		if(world != null) {
			lastEndTickModification = world.getScheduler().getTick();
		}
		

		if(this.context != null) {
			this.percept.updateContextProjectionEnd(this.context);
			this.percept.updateSortedRanges(this.context, "end");
			context.getWorld().getScheduler().addLastmodifiedContext(context);
			
		}
	}

	public Range clone() throws CloneNotSupportedException{
		return (Range)super.clone();
	}
	
	public double getCenter() {
		return (end + start)/2;
	}
	
	public double getRadius() {
		return (end - start)/2;
	}
	
	public int getLastStartTickModification() {
		return lastStartTickModification;
	}
	
	public int getLastEndTickModification() {
		return lastEndTickModification;
	}
	
	private double distance(Range otherRange) {
		return Math.abs(this.getCenter() - otherRange.getCenter()) - this.getRadius() - otherRange.getRadius();
	}
	
	private double overlapDistance(Range otherRange) {
		
		double distanceBetweenRanges = distance(otherRange);
		if (distanceBetweenRanges<0) {
			return Math.abs(distanceBetweenRanges);
		}
		else {
			return 0.0;
		}
	}
	
	private double nonOverlapDistance(Range otherRange) {
		return this.getLenght() - overlapDistance(otherRange);
	}
	
	private double voidDistance(Range otherRange) {
		
		double distanceBetweenRanges = distance(otherRange);
		if (distanceBetweenRanges>0) {
			return distanceBetweenRanges;
		}
		else {
			return 0.0;
		}
	}
}
