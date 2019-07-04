package experiments;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

public class Test {

	public static void main(String[] args) {
		double[] coefs = {0.5, 10.0, -4.0};
		double constant = 5.0;
		LinearObjectiveFunction fct = new LinearObjectiveFunction(coefs, constant);
		double[] cf = {1.0, 0.0, 0.0};
		LinearConstraint cst1 = new LinearConstraint(cf, Relationship.GEQ, -5.0);
		LinearConstraint cst2 = new LinearConstraint(cf, Relationship.LEQ, 5.0);
		double[] cf2 = {0.0, 1.0, 0.0};
		LinearConstraint cst3 = new LinearConstraint(cf2, Relationship.GEQ, -8.0);
		LinearConstraint cst4 = new LinearConstraint(cf2, Relationship.LEQ, 8.0);
		double[] cf3 = {0.0, 0.0, 1.0};
		LinearConstraint cst5 = new LinearConstraint(cf3, Relationship.GEQ, -10.0);
		LinearConstraint cst6 = new LinearConstraint(cf3, Relationship.LEQ, 10.0);
		LinearConstraintSet set = new LinearConstraintSet(cst1, cst2, cst3, cst4, cst5, cst6);
		GoalType gt = GoalType.MAXIMIZE;
		SimplexSolver solver = new SimplexSolver();
		
		PointValuePair res = solver.optimize(fct, set, gt);
		System.out.println(res);
	}

}
