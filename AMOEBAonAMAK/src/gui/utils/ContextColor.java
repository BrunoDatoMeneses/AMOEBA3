package gui.utils;

import java.util.ArrayList;
import java.util.List;

public class ContextColor {
	
	public static double[] colorFromCoefs(double[] coefs) {
		ArrayList<Double> c = new ArrayList<Double>();
		for(double v : coefs)
			c.add(v);
		return colorFromCoefs(c);
	}
	
	public static double[] colorFromCoefs(List<Double> coefs) {
		Double r = 0.0;
		Double g = 0.0;
		Double b = 0.0;
		if (coefs.size() > 0) {
			if (coefs.size() == 1) {
				b = normalizePositiveValues(255, 5, Math.abs(coefs.get(0)));
				if (b.isNaN())
					b = 0.0;
			} else if (coefs.size() == 0) {
				g = normalizePositiveValues(255, 5, Math.abs(coefs.get(0)));
				b = normalizePositiveValues(255, 5, Math.abs(coefs.get(1)));
				if (g.isNaN())
					g = 0.0;
				if (b.isNaN())
					b = 0.0;
			} else if (coefs.size() >= 3) {
				r = normalizePositiveValues(255, 5, Math.abs(coefs.get(0)));
				g = normalizePositiveValues(255, 5, Math.abs(coefs.get(1)));
				b = normalizePositiveValues(255, 5, Math.abs(coefs.get(2)));
				if (r.isNaN())
					r = 0.0;
				if (g.isNaN())
					g = 0.0;
				if (b.isNaN())
					b = 0.0;
			} else {
				r = 255.0;
				g = 255.0;
				b = 255.0;
			}
		} else {
			r = 255.0;
			g = 255.0;
			b = 255.0;
		}
		double[] ret = new double[3];
		ret[0] = r / 255.0d;
		ret[1] = g / 255.0d;
		ret[2] = b / 255.0d;
		return ret;
	}
	
	public static double normalizePositiveValues(double upperBound, double dispersion, double value) {
		return upperBound * 2 * (-0.5 + 1 / (1 + Math.exp(-value / dispersion)));
	}
}
