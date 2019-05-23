package gui.context;

import java.util.ArrayList;
import java.util.List;

import agents.context.Context;
import agents.percept.Percept;
import fr.irit.smac.amak.ui.VUI;
import gui.AmoebaWindow;
import gui.RenderStrategy;
import gui.Visualization;
import javafx.event.Event;
import javafx.scene.paint.Color;

/**
 * A render strategy for contexts using JavaFX.
 * <p>
 * Contexts have 2 visualizations that need to be rendered : 
 * <ul>
 * <li>a drawable {@link ContextRendererFX#getDrawable()} for the {@link VUI} </li>
 * <li>and a mini visualization {@link ContextRendererFX#getMini()} for the {@link ContextExplorer} </li>
 * </ul>
 * This class make sure that the 2 visualizations stay coherent.
 * <p>
 * If there's no {@link ContextExplorer} in the main window when
 * {@link ContextRendererFX#initialize()} is called, a new one is created and
 * added.
 * 
 * @author Hugo
 *
 */
public class ContextRendererFX extends RenderStrategy {

	public static ContextExplorer contextExplorer;

	private List<Visualization> visualizations;
	private Context context;

	private ContextRectangle drawable;
	private MiniContextRectangle mini;

	public ContextRendererFX(Object o) {
		this((Context) o);
	}

	/**
	 * @param context the context to be rendered.
	 */
	public ContextRendererFX(Context context) {
		super(context);
		this.context = context;
		visualizations = new ArrayList<>();
	}

	@Override
	public void render() {

		updatePosition();

		updateColor();
	}

	private void updateColor() {
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
		getDrawable().setColor(new Color((double) r.intValue() / 255d, (double) g.intValue() / 255d,
				(double) b.intValue() / 255d, 90d / 255d));
	}

	private void updatePosition() {
		Percept p1 = context.getAmas().getDimensionSelector().d1();
		Percept p2 = context.getAmas().getDimensionSelector().d2();
		double x = context.getRanges().get(p1).getStart() + (context.getRanges().get(p1).getLenght() / 2);
		double y = context.getRanges().get(p2).getStart() + (context.getRanges().get(p2).getLenght() / 2);
		getDrawable().move(x, y);
		getDrawable().setWidth(context.getRanges().get(p1).getLenght());
		getDrawable().setHeight(context.getRanges().get(p2).getLenght());
	}

	private double normalizePositiveValues(double upperBound, double dispersion, double value) {
		return upperBound * 2 * (-0.5 + 1 / (1 + Math.exp(-value / dispersion)));
	}

	/**
	 * Initialize the drawable, and may add a {@link ContextExplorer} to the main
	 * window.
	 */
	@Override
	public void initialize() {
		getDrawable(); // create the drawable if it does not exist

		if (contextExplorer == null) {
			contextExplorer = new ContextExplorer(context.getAmas());
		}
	}

	@Override
	public void delete() {
		if (drawable != null)
			drawable.delete();
	}

	/**
	 * Dispatch an event to all visualization.
	 * 
	 * @param event
	 */
	public void dispatchEvent(Event event) {
		if ("MOUSE_CLICKED".equals(event.getEventType().getName())) {
			if (context.getRenderStrategy() instanceof ContextRendererFX) {
				ContextRendererFX.contextExplorer.update();
			}
		}
		synchronized (visualizations) {
			for (Visualization v : visualizations) {
				v.onEvent(event);
			}
		}
	}

	/**
	 * Return the visualization for the VUI, may create it.
	 * 
	 * @return
	 */
	public ContextRectangle getDrawable() {
		if (!context.isDying() && drawable == null) {
			drawable = new ContextRectangle(0, 0, 10, 10, context);
			visualizations.add(drawable);
			AmoebaWindow.instance().mainVUI.add(drawable);
		}
		return drawable;
	}

	/**
	 * Return the visualization quick access, may create it. Used in the
	 * {@link ContextExplorer}.
	 * 
	 * @return
	 */
	public MiniContextRectangle getMini() {
		if (!context.isDying() && mini == null) {
			mini = new MiniContextRectangle(getDrawable());
			visualizations.add(mini);
		}
		return mini;
	}

}
