package gui;

import agents.context.Context;
import agents.percept.Percept;
import fr.irit.smac.amak.ui.drawables.DrawableRectangle;
import gui.utils.ContextColor;
import javafx.scene.paint.Color;

/**
 * A render strategy for contexts using AMAKFX tools.<br/>
 * A Context is represented by a {@link DrawableRectangle} drawn onto {@link AmoebaWindow#mainVUI}.
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
	 * Initialize the drawable.
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
	 * Return the visualization for the VUI, may create and add it to the VUI.
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
