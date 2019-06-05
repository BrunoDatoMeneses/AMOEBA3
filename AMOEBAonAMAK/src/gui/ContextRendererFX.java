package gui;

import agents.context.Context;
import agents.percept.Percept;
import fr.irit.smac.amak.ui.VUI;
import fr.irit.smac.amak.ui.drawables.DrawableRectangle;
import gui.utils.ContextColor;
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

	private Context context;

	private DrawableRectangle drawable;

	public ContextRendererFX(Object o) {
		this((Context) o);
	}

	/**
	 * @param context the context to be rendered.
	 */
	public ContextRendererFX(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public void render() {
		updatePosition();
		updateColor();
		drawable.setName(context.toString());
		drawable.setInfo(context.toStringFull());
	}

	private void updateColor() {
		double[] c = ContextColor.colorFromCoefs(context.getFunction().getCoefs());
		drawable.setColor(new Color(c[0], c[1], c[2], 90d / 255d));
	}

	private void updatePosition() {
		Percept p1 = context.getAmas().getDimensionSelector().d1();
		Percept p2 = context.getAmas().getDimensionSelector().d2();
		double x = context.getRanges().get(p1).getStart();
		double y = context.getRanges().get(p2).getStart();
		drawable.setWidth(context.getRanges().get(p1).getLenght());
		drawable.setHeight(context.getRanges().get(p2).getLenght());
		drawable.move(x, y);
	}

	/**
	 * Initialize the drawable, and may add a {@link ContextExplorer} to the main
	 * window.
	 */
	@Override
	public void initialize() {
		getDrawable().setName(context.toString()); // create the drawable if it does not exist

	}

	@Override
	public void delete() {
		if (drawable != null)
			drawable.delete();
	}


	/**
	 * Return the visualization for the VUI, may create it.
	 * 
	 * @return
	 */
	public DrawableRectangle getDrawable() {
		if (!context.isDying() && drawable == null) {
			drawable = new DrawableRectangle(0, 0, 10, 10);
			AmoebaWindow.instance().mainVUI.add(drawable);
		}
		return drawable;
	}
}
