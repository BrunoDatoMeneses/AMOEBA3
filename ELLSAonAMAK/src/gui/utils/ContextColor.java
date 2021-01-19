package gui.utils;

import java.util.ArrayList;
import java.util.List;

import agents.context.Context;
import agents.context.localModel.LocalModel;

/**
 * Tools for determining the color of a {@link Context} based on the coefficients of its {@link LocalModel}
 * @author Hugo
 *
 */
public class ContextColor {
	
	/**
	 * Compute the color of a {@link Context} based on the coefficients of its {@link LocalModel}
	 * @param coefs
	 * @return
	 */
	public static Double[] colorFromCoefs(Double[] coefs) {
		ArrayList<Double> c = new ArrayList<Double>();

		for(double v : coefs)
			c.add(v);
		return colorFromCoefs(c);
	}
	
	/**
	 * Compute the color of a {@link Context} based on the coefficients of its {@link LocalModel}
	 * @param coefs
	 * @return
	 */
	public static Double[] colorFromCoefs(List<Double> coefs) {
		double upperBound = 255;
		double dispersion = 100;
		
		
		Double r = 0.0;
		Double g = 0.0;
		Double b = 0.0;

		
		if(coefs.size()>=3) {
			r =  0.0;//normalizePositiveValues(upperBound, dispersion,  Math.abs(coefs.get(0)));
			g =  normalizePositiveValues(upperBound, dispersion,  Math.abs(coefs.get(1)));
			b =  normalizePositiveValues(upperBound, dispersion,  Math.abs(coefs.get(2)));

			
			if(r.isNaN() || g.isNaN() || b.isNaN()) {
				r = 255.0;
				g = 0.0;
				b = 0.0;
			}
		}else if(coefs.size()>=2) {
			r =  normalizePositiveValues(upperBound, dispersion,  Math.abs(coefs.get(0)));
			g =  normalizePositiveValues(upperBound, dispersion,  Math.abs(coefs.get(1)));
			
			if(r.isNaN() || g.isNaN() || b.isNaN()) {
				r = 255.0;
				g = 0.0;
			}
		}else if(coefs.size()==1) {
			r =  normalizePositiveValues(upperBound, dispersion,  Math.abs(coefs.get(0)));
			
			if(r.isNaN() || g.isNaN() || b.isNaN()) {
				r = 255.0;
			}
		}else {
			r = 0.0;
			g = 255.0;
			b = 0.0;
		}
		

		Double[] ret = new Double[3];
		ret[0] = r / 255.0d;
		ret[1] = g / 255.0d;
		ret[2] = b / 255.0d;
		return ret;
	}
	
	
	
	public static double normalizePositiveValues(double upperBound, double dispersion, double value) {
		return upperBound * 2 * (-0.5 + 1 / (1 + Math.exp(-value / dispersion)));
	}
	

}
