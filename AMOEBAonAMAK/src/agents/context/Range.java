package agents.context;

import agents.percept.Percept;

/**
 * The Class Range.
 */
public class Range implements Comparable<Object>, Cloneable {

	/** The start. */
	private double start;
	
	/** The end. */
	private double end;
	
	/** The start inclu. */
	private boolean start_inclu;
	
	/** The end inclu. */
	private boolean end_inclu;

	/** The context. */
	private Context context;
	
	/** The percept. */
	private Percept percept;
	
	/** The maxid. */
	public static int maxid = 0; // TODO for debug purposes
	
	/** The id. */
	public int id;

	/** The Constant mininimalRange. */
	public static final double mininimalRange = 10;
	
	/** The Constant useAVT. */
	private static final boolean useAVT = true;
	
	/** The AV T delta min. */
	/*---------------AVT---------------*/
	private double AVT_deltaMin = 0.5;
	
	/** The AV T delta max. */
	private double AVT_deltaMax = 0.5;
	
	/** The AV T last feedback min. */
	private int AVT_lastFeedbackMin = 1;
	
	/** The AV T last feedback max. */
	private int AVT_lastFeedbackMax = 1;
	
	/** The AV T acceleration. */
	private double AVT_acceleration;
	
	/** The AV T deceleration. */
	private double AVT_deceleration;
	
	/** The AV T start ratio. */
	private double AVT_startRatio;
	/*---------------------------------*/
	
	/*------------Percent--------------*/
	/** The percent up. */
	//Only used if useAVT == false
	static public double percent_up = 0.2;
	
	/** The percent down. */
	static public double percent_down = 0.1;
	/*---------------------------------*/

	
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
			boolean end_inclu, Percept p) {
		super();
		
		AVT_deceleration = context.getAmas().getEnvironment().getAVT_deceleration();
		AVT_acceleration = context.getAmas().getEnvironment().getAVT_acceleration();
		AVT_startRatio = context.getAmas().getEnvironment().getAVT_percentAtStart();

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
		this.AVT_deltaMin = (end - start) * AVT_startRatio + 0.0001;
		this.AVT_deltaMax = (end - start) * AVT_startRatio + 0.0001;
	}
	
	//TODO copy constructor

	/**
	 * Method called by the context agent. Allow the range to adapt itself.
	 *
	 * @param c : the associated Context.
	 * @param oracleValue the oracle value
	 * @param p the p
	 */
	public void adapt(Context c, double oracleValue, Percept p) {
		if (!isPerceptEnum()) {
			if (Range.useAVT) {
				adaptUsingAVT(c, oracleValue);
			} else {
				adaptWithoutAVT(c, oracleValue);
			}
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
			adaptMaxWithoutAVT(c, oracleValue);
		} else {
			adaptMinWithoutAVT(c, oracleValue);
		}		
	}

	/**
	 * Adapt max without AVT.
	 *
	 * @param c the c
	 * @param oracleValue the oracle value
	 */
	private void adaptMaxWithoutAVT(Context c, double oracleValue) {
		if (contains(oracleValue) == 0.0) {
			this.setEnd(end - ((end - start) * percent_down));
		} else {
			this.setEnd(end + ((end - start) * percent_up));
		}
	}

	/**
	 * Adapt min without AVT.
	 *
	 * @param c the c
	 * @param oracleValue the oracle value
	 */
	private void adaptMinWithoutAVT(Context c, double oracleValue) {
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
	private void adaptUsingAVT(Context c, double oracleValue) {
		if (Math.abs(end - oracleValue) < Math.abs(oracleValue - start)) {
			adaptMaxUsingAVT(c, oracleValue);
		} else {
			adaptMinUsingAVT(c, oracleValue);
		}
	}
	
	/**
	 * Adapt max using AVT.
	 *
	 * @param c the c
	 * @param oracleValue the oracle value
	 */
	private void adaptMaxUsingAVT(Context c, double oracleValue) {

		if (contains(oracleValue) == 0.0) {  //If value is contained, it's a negative feedback for AVT (ie : we must exclude the value)

			if (AVT_lastFeedbackMax == 1) {
				AVT_deltaMax *= AVT_deceleration;
			} else {
				AVT_deltaMax *= AVT_acceleration;
			}
			this.setEnd(end - AVT_deltaMax);

			AVT_lastFeedbackMax = -1;

		} else {

			if (AVT_lastFeedbackMax == 1) {
				AVT_deltaMax *= AVT_acceleration;
			} else {
				AVT_deltaMax *= AVT_deceleration;
			}
			this.setEnd(end + AVT_deltaMax);

			AVT_lastFeedbackMax = 1;
		}		


	}

	/**
	 * Adapt min using AVT.
	 *
	 * @param c the c
	 * @param oracleValue the oracle value
	 */
	private void adaptMinUsingAVT(Context c, double oracleValue) {

		if (contains(oracleValue) == 0.0) {  //If value is contained, it's a negative feedback for AVT (ie : we must exclude the value)

			if (AVT_lastFeedbackMin == 1) {
				AVT_deltaMin *= AVT_deceleration;
			} else {
				AVT_deltaMin *= AVT_acceleration;
			}
			this.setStart(start + AVT_deltaMin);

			AVT_lastFeedbackMin = -1;

		} else {

			if (AVT_lastFeedbackMin == 1) {
				AVT_deltaMin *= AVT_acceleration;
			} else {
				AVT_deltaMin *= AVT_deceleration;
			}
			this.setStart(start - AVT_deltaMin);

			AVT_lastFeedbackMin = 1;
		}		

	}
	
	/**
	 * Simulate negative AVT feedback min.
	 *
	 * @param oracleValue the oracle value
	 * @return the double
	 */
	public double simulateNegativeAVTFeedbackMin(double oracleValue) {
		
		if (AVT_lastFeedbackMin == 1) {
			return start + (AVT_deltaMin * AVT_deceleration);
		} else {
			return start + (AVT_deltaMin * AVT_acceleration);
		}

	}
	
	/**
	 * Simulate negative AVT feedback max.
	 *
	 * @param oracleValue the oracle value
	 * @return the double
	 */
	public double simulateNegativeAVTFeedbackMax(double oracleValue) {
		
		if (AVT_lastFeedbackMax == 1) {
			return end - (AVT_deltaMax * AVT_deceleration);
		} else {
			return end - (AVT_deltaMax * AVT_acceleration);
		}

	}
	
	
	

	/**
	 * Check if the ranges is too small according to strategy.
	 * 
	 * @return boolean representing if the range is too small.
	 */
	public boolean isTooSmall() {
		
		return (end - start) < mininimalRange && !this.isPerceptEnum();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return ((start_inclu ? "[" : "]") + start + "," + end
				+ (!end_inclu ? "[" : "]") + "  Current value : " + percept.getValue()
				+ "  AVT_MIN : " + AVT_deltaMin
				+ "  AVT_MAX : " + AVT_deltaMax );
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
	 * Gets the AV twill to reduce.
	 *
	 * @param max the max
	 * @return the AV twill to reduce
	 */
	public double getAVTwillToReduce(boolean max) {
		if (max) {
			return this.AVT_lastFeedbackMax * this.AVT_deltaMax;
		} else {
			return this.AVT_lastFeedbackMin * this.AVT_deltaMin;
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
				//Change min
				this.setStart(r.getEnd());
				this.setStart_inclu(!r.isEnd_inclu());
				
			} else {
				//Change max
				this.setEnd( r.getStart());
				this.setEnd_inclu(!r.isStart_inclu());
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
	 * Gets the nearest limit.
	 *
	 * @param d the d
	 * @return max is true, min is false
	 */
	public boolean getNearestLimit(double d) {
		return (d - start < end - d) ? false : true;
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
		this.start = newStartValue;
		if(this.context != null) {
			this.percept.updateContextProjectionStart(this.context);
		}
		
	}
	
	private void setEnd(double newEndValue) {
		this.end = newEndValue;
		if(this.context != null) {
			this.percept.updateContextProjectionEnd(this.context);			
		}
	}

	public Range clone() throws CloneNotSupportedException{
		return (Range)super.clone();
	}
}
