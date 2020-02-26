package gui;

import agents.context.Context;
import agents.percept.Percept;
import experiments.nDimensionsLaunchers.PARAMS;
import fr.irit.smac.amak.ui.VUIMulti;
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

	private DrawableContext drawable;

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
		if(PARAMS.contextColorByCoef){
			setColorWithCoefs();
		}else{
			setColorWithPrediction(PARAMS.minPrediction,PARAMS.maxPrediction);
			//setColorWithPredictionReinforcement();
		}

	}
	
	private void setColorWithCoefs() {
		Double[] c = ContextColor.colorFromCoefs(context.getFunction().getCoef());
		if(context.isInNeighborhood) {
			drawable.setColor(new Color(c[0], c[1], c[2], 200d / 255d));
		}else {
			drawable.setColor(new Color(c[0], c[1], c[2], 90d / 255d));
		}
		
	}
	
	private void setColorWithPrediction(double min, double max) {
		
		double r = 0.0;
		double g = 0.0;
		double b = 0.0;
		
		if(context.lastPrediction!=null) {

			double range = max-min;
			r = (context.lastPrediction-min)/range;
			b = (max-context.lastPrediction)/range;

			r = r > 1.0 ? 1.0 : r;
			g = g > 1.0 ? 1.0 : g;
		}else {
			g = 1.0;
		}
		if(context.lastPrediction == -1.0) {
			r = 1.0;
			g = 1.0;
			b = 0.0;
		}
		if(Math.abs(context.lastPrediction) > max) {
			
			r = 1.0;
			g = 0.0;
			b = 1.0;
		}


		if(context.isInNeighborhood) {
			drawable.setColor(new Color(r, g, b, 200d / 255d));
		}else {
			drawable.setColor(new Color(r, g, b, 140d / 255d));
		}
	}

	private void setColorWithPredictionReinforcement() {

		double r = 0.0;
		double g = 0.0;
		double b = 0.0;

		if(context.lastPrediction!=null) {
			r = context.lastPrediction < 0 ? Math.abs(context.lastPrediction)/2 : 0.0;
			g = context.lastPrediction > 0 ? context.lastPrediction/2	 : 0.0;
			r = r > 1.0 ? 1.0 : r;
			g = g > 1.0 ? 1.0 : g;
		}else {
			b = 1.0;
		}
		if(context.lastPrediction == -1.0) {
			r = 1.0;
			g = 0.0;
			b = 1.0;
		}
		if(Math.abs(context.lastPrediction) > 10000) {

			r = 1.0;
			g = 1.0;
			b = 0.0;
		}


		if(context.isInNeighborhood) {
			drawable.setColor(new Color(r, g, b, 200d / 255d));
		}else {
			drawable.setColor(new Color(r, g, b, 90d / 255d));
		}
	}
	
	
	
	public String getColorForUnity() {
		Double[] c = ContextColor.colorFromCoefs(context.getFunction().getCoef());
		 return c[0].intValue() + "," + c[1].intValue() + "," + c[2].intValue() + ",100";
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
	public void initialize(VUIMulti vui) {
		getDrawable(vui).setName(context.toString()); // create the drawable if it does not exist

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
	public DrawableRectangle getDrawable(VUIMulti vui) {
		if (!context.isDying() && drawable == null) {
			drawable = new DrawableContext(0, 0, 0, 0, context);
			vui.add(drawable);
		}
		return drawable;
	}
}
