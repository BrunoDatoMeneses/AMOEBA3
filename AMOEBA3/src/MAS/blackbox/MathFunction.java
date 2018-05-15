package MAS.blackbox;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The different functions allowed for the agent "Function" to use.
 * The enum name is the name which must be write in the XML config file.
 * @author nigon
 *
 */
public enum MathFunction implements Serializable {

	/** The plus. */
	PLUS("+"),
	
	/** The mult. */
	MULT("*"),
	
	/** The minus. */
	MINUS("-"),
	
	/** The divide. */
	DIVIDE("/"),
	
	/** The between. */
	BETWEEN("<...<"),
	
	/** The int between. */
	INT_BETWEEN("int<...<"),
	
	/** The or. */
	OR("||"),
	
	/** The and. */
	AND("&&"),
	
	/** The xor. */
	XOR("xor"),
	
	/** The is part of unit circle. */
	IS_PART_OF_UNIT_CIRCLE("unit_circle"),
	
	/** The euclide. */
	EUCLIDE("euclide"),
	
	/** The mountain car. */
	MOUNTAIN_CAR("mountain"),
	
	/** The cos plus. */
	COS_PLUS("cos+");
	
	/** The symbol. */
	private final String symbol;
	
	/**
	 * Instantiates a new math function.
	 *
	 * @param symbol the symbol
	 */
	private MathFunction(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * Compute.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the double
	 */
	public double compute(double a, double b) {

		switch (this) {

		case PLUS:
			return a + b;

		case MINUS:
			return a - b;

		case DIVIDE:
			return a / b;

		case MULT:
			return a * b;
			
		case INT_BETWEEN:
			return ((int)(Math.random() * ((Math.max(a,b) - Math.min(a,b)+2))+ Math.min(a, b) - 1));

		case BETWEEN:
			return Math.random() * (Math.max(a,b) - Math.min(a,b))+ Math.min(a, b);
			
		case OR:
			return (a >= 0 || b >= 0) ? 1.0 : -1.0;
			
		case XOR:
			return ((a >= 0 && b < 0) || (a < 0 && b >= 0)) ? 1.0 : -1.0;
			
		case AND:
			return (a >= 0 && b >= 0) ? 1.0 : -1.0;
		
		case IS_PART_OF_UNIT_CIRCLE:
			return (Math.sqrt((a*a) + (b*b)) <= 1 ? 1.0 : -1.0);
			
		case EUCLIDE:
			return (Math.sqrt((a*a) + (b*b)));
			
		case MOUNTAIN_CAR:
			return ((a*0.001) + (Math.cos(3*b) * -0.0025));
			
		case COS_PLUS:
			return (Math.cos(a) + b);
			
		default:
			return 0.0;
		}
	}

	/**
	 * Gets the symbol.
	 *
	 * @return the symbol
	 */
	public String getSymbol() {
		return symbol;
	}
}
