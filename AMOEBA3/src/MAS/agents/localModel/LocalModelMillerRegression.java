package MAS.agents.localModel;

import java.io.Serializable;
import java.util.ArrayList;

import MAS.kernel.World;
import MAS.agents.Agent;
import MAS.agents.Percept;
import MAS.agents.context.Context;
import MAS.agents.context.Experiment;
import MAS.agents.messages.Message;

// TODO: Auto-generated Javadoc
/**
 * The Class LocalModelMillerRegression.
 */
public class LocalModelMillerRegression extends LocalModelAgent implements Serializable{
	
	/** The n parameters. */
	private int nParameters;
	
	/** The regression. */
	transient private Regression regression;

	
	/** The coef. */
	private double[] coef;

	/**
	 * Instantiates a new local model miller regression.
	 *
	 * @param world the world
	 */
	public LocalModelMillerRegression(World world) {
		super(world);
		ArrayList<Percept> var = world.getAllPercept();
		this.nParameters = var.size();
		regression = new Regression(nParameters,true);
	}
	
	/**
	 * Sets the coef.
	 *
	 * @param coef the new coef
	 */
	public void setCoef(double[] coef) {
		this.coef = coef.clone();
	}
	
	/**
	 * Gets the coef.
	 *
	 * @return the coef
	 */
	public double[] getCoef() {
		return coef;
	}

	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#getTargets()
	 */
	@Override
	public ArrayList<? extends Agent> getTargets() {
		return null;
	}

	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#computeAMessage(agents.messages.Message)
	 */
	@Override
	public void computeAMessage(Message m) {
	}

	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#getProposition(agents.context.Context)
	 */
	public double getProposition(Context context) {
		
	//	LinkedHashMap<Percept,Double> values = new LinkedHashMap<Percept,Double>();
		
		ArrayList<Percept> var = world.getAllPercept();
		
		if (context.getExperiments().size() == 1) {
			return context.getExperiments().get(0).getProposition();
		}
		

		//
	/*	for (int i = 0 ; i < coef.length ; i++ ) {
			System.out.print(coef[i] + "   " );
		}
		System.out.println();*/
		
		
		double result = coef[0];
	//	System.out.println("Result 0" + " : " + result);
		if (coef[0] == Double.NaN) System.exit(0);
		for (int i = 1 ; i < coef.length ; i++) {
			if (Double.isNaN(coef[i])) coef[i] = 0;
			result += coef[i]*var.get(i-1).getValue();
	//		System.out.println("Result " + i + " : " + result);
	//		System.out.print(var.get(i-1).getName() + " coef : " + coef[i] + "   " );
		}
//		System.out.println("Result final" + " : " + result);


		
		return result;
	}

	
	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#getProposition(agents.context.Context, agents.Percept, agents.Percept, double, double)
	 */
	public double getProposition(Context context, Percept p1, Percept p2, double v1, double v2) {
		
	//	System.out.println("get proposition");
	//	LinkedHashMap<Percept,Double> values = new LinkedHashMap<Percept,Double>();
		
		ArrayList<Percept> var = world.getAllPercept();
		
		if (context.getExperiments().size() == 1) {
			return context.getExperiments().get(0).getProposition();
		}
		
		regression = new Regression(nParameters,true);
		for (Experiment exp : context.getExperiments()) {
			regression.addObservation(exp.getValuesAsArray(), exp.getProposition());

			while (regression.getN() < context.getExperiments().get(0).getValues().size() + 2) { //TODO : to improve
			regression.addObservation(context.getExperiments().get(0).getValuesAsArray(), context.getExperiments().get(0).getProposition());
		}
		}

		
		double[] coef = regression.regress().getParameterEstimates();
		
		//
	//	for (int i = 0 ; i < coef.length ; i++ ) {
	//		System.out.print(var.get(i).getName() + " coef : " + coef[i] + "   " );
	//	}
	//	System.out.println();
		
		double[] tabv = {v1,v2};
		
		double result = coef[0];
	//	System.out.println("Result 0" + " : " + result);
		if (coef[0] == Double.NaN) System.exit(0);
		for (int i = 1 ; i < coef.length ; i++) {
			if (Double.isNaN(coef[i])) coef[i] = 0;
			result += coef[i]*tabv[i-1];
		}
//		System.out.println("Result final" + " : " + result);
		return result;

	

	}
	
	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#getFormula(agents.context.Context)
	 */
	public String getFormula(Context context) {
		String s = "";
		if (context.getExperiments().size() == 1) {
			return ""+context.getExperiments().get(0).getProposition();
		}
		else {
			if (regression == null) updateModel(context);
			double[] coef = regression.regress().getParameterEstimates();
			
			ArrayList<Percept> var = world.getAllPercept();
			
			if (coef[0] == Double.NaN) System.exit(0);
			for (int i = 1 ; i < coef.length ; i++) {
				if (Double.isNaN(coef[i])) coef[i] = 0;
				s += coef[i] + "*" + var.get(i-1).getName();
				
				if (i < coef.length - 1) s += " + ";
			}
			
			s += "\n with " ;
			
			for (int i = 1 ; i < coef.length ; i++) {
				if (Double.isNaN(coef[i])) coef[i] = 0;
				s += var.get(i-1).getName() + " = " + var.get(i-1).getValue();
				s += ", ";
			}
			
			s += "\n with " ;
			s += context.getExperiments().size() + " experimentations";
			
			s += "\n with " ;
			s += getProposition(context) + " as result";
			
			return s;
		}

	}


	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#updateModel(agents.context.Context)
	 */
	@Override
	public void updateModel(Context context) {
		
		regression = new Regression(nParameters,true);
		for (Experiment exp : context.getExperiments()) {
			regression.addObservation(exp.getValuesAsArray(), exp.getProposition());
		//	System.out.println(exp.getValues().toString());
			for (int i = 0 ; i < exp.getValuesAsArray().length ; i++ ) {
	//			System.out.print(exp.getValuesAsArray()[i] + "   " );
			}
//			System.out.println(exp.getProposition() + "   " );
		}
		
	//	System.out.println("Number of experiments : " + context.getExperiments().size());
		while (regression.getN() < context.getExperiments().get(0).getValues().size() + 2) { //TODO : to improve
			regression.addObservation(context.getExperiments().get(0).getValuesAsArray(), context.getExperiments().get(0).getProposition());
		}
		

		
		coef = regression.regress().getParameterEstimates();
		
	}
	

}
