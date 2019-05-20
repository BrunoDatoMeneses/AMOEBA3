package gui;

import agents.context.Context;
import agents.percept.Percept;
import fr.irit.smac.amak.ui.VUI;
import javafx.scene.paint.Color;

/**
 * A render strategy for contexts using JavaFX. 
 * @author Hugo
 *
 */
public class ContextRendererFX implements RenderStrategy {
	
	private static ContextExplorer contextExplorer;

	@Override
	public void render(Object object) {
		Context context = (Context) object;
		ContextVisualizations vizu = AmoebaWindow.instance().getContextVisualizations(context);
		// update position
		Percept p1 = context.getAmas().getDimensionSelector().d1();
		Percept p2 = context.getAmas().getDimensionSelector().d2();
		double x = context.getRanges().get(p1).getStart() + (context.getRanges().get(p1).getLenght() / 2);
		double y = context.getRanges().get(p2).getStart() + (context.getRanges().get(p2).getLenght() / 2);
		vizu.getDrawable().move(x, y);
		vizu.getDrawable().setWidth(context.getRanges().get(p1).getLenght());
		vizu.getDrawable().setHeight(context.getRanges().get(p2).getLenght());

		// Update colors
		Double r = 0.0;
		Double g = 0.0;
		Double b = 0.0;
		double[] coefs = context.getLocalModel().getCoefs();
		if (coefs.length > 0) {
			if (coefs.length == 1) {
				b = normalizePositiveValues(255, 5, Math.abs(coefs[0]));
				if (b.isNaN())
					b = 0.0;
			} else if (coefs.length == 0) {
				g = normalizePositiveValues(255, 5, Math.abs(coefs[0]));
				b = normalizePositiveValues(255, 5, Math.abs(coefs[1]));
				if (g.isNaN())
					g = 0.0;
				if (b.isNaN())
					b = 0.0;
			} else if (coefs.length >= 3) {
				r = normalizePositiveValues(255, 5, Math.abs(coefs[0]));
				g = normalizePositiveValues(255, 5, Math.abs(coefs[1]));
				b = normalizePositiveValues(255, 5, Math.abs(coefs[2]));
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
		vizu.getDrawable().setColor(new Color((double) r.intValue()/255d, (double) g.intValue()/255d, (double) b.intValue()/255d, 90d/255d));
	
	}
	
	private double normalizePositiveValues(double upperBound, double dispersion, double value) {
		return upperBound * 2 * (-0.5 + 1 / (1 + Math.exp(-value / dispersion)));
	}

	@Override
	public void initialize(Object object) {
		Context context = (Context) object;
		ContextVisualizations vizu = AmoebaWindow.instance().getContextVisualizations(context);
		VUI.get().add(vizu.getDrawable());
		vizu.getDrawable().setLayer(1);
		vizu.getDrawable().setColor(new Color(173d/255d, 79d/255d, 9d/255d, 90d/255d));
		
		if(contextExplorer == null) {
			contextExplorer = new ContextExplorer(context.getAmas());
		}
	}
	
	@Override
	public void delete(Object object) {
		Context context = (Context) object;
		AmoebaWindow.instance().getContextVisualizations(context).getDrawable().hide();
		AmoebaWindow.instance().removeContextVisualization(context);
	}

}
