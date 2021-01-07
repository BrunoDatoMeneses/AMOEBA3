package gui;

import agents.context.Context;
import agents.percept.Percept;
import fr.irit.smac.amak.ui.VUIMulti;
import fr.irit.smac.amak.ui.drawables.DrawableRectangle;
import gui.utils.ContextColor;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 * A render strategy for contexts using AMAKFX tools.<br/>
 * A Context is represented by a {@link DrawableRectangle} drawn onto {@link EllsaWindow#mainVUI}.
 * 
 * @author Hugo
 *
 */
public class ContextRendererFX extends RenderStrategy {

	private Context context;

	private DrawableContext drawable;
	private DrawableNDimContext drawableNDimensions;

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

		drawableNDimensions.setName(context.toString());
		drawableNDimensions.setInfo(context.toStringFull());

		if(context.isInNeighborhood){
			drawableNDimensions.setVisible(true);
		}else{
			drawableNDimensions.setVisible(false);
		}
	}

	private void updateColor() {
		if(context.getAmas().multiUIWindow.guiData.contextColorByCoef){
			setColorWithCoefs();
		}else{
			setColorWithPrediction(context.getAmas().multiUIWindow.guiData.minPrediction,context.getAmas().multiUIWindow.guiData.maxPrediction);
			//setColorWithPredictionReinforcement();
		}

	}
	
	private void setColorWithCoefs() {
		Double[] c = ContextColor.colorFromCoefs(context.getFunction().getCoef());
		/*if(context.isBest) {
			drawable.setColor(new Color(0.0, 1.0, 0.0, 225d / 255d));
			drawableNDimensions.setColor(new Color(0.0, 1.0, 0.0, 225d / 255d));
		}else if(context.isActivated) {
			drawable.setColor(new Color(0.0, 0.0, 0.0, 225d / 255d));
			drawableNDimensions.setColor(new Color(0.0, 0.0, 0.0, 225d / 255d));
		}else if(context.isInNeighborhood) {
			drawable.setColor(new Color(1.0, 0.0, 0.0, 225d / 255d));
			drawableNDimensions.setColor(new Color(1.0, 0.0, 0.0, 225d / 255d));
		}else if(context.isInSubNeighborhood) {
			drawable.setColor(new Color(0.0, 0.0, 1.0, 225d / 255d));
			drawableNDimensions.setColor(new Color(0.0, 0.0, 1.0, 225d / 255d));
		}else {
			drawable.setColor(new Color(c[0], c[1], c[2], 50d / 255d));
			drawableNDimensions.setColor(new Color(c[0], c[1], c[2], 10d / 255d));
		}*/

		drawable.setColor(new Color(c[0], c[1], c[2], 50d / 255d));
		drawableNDimensions.setColor(new Color(c[0], c[1], c[2], 10d / 255d));
		
	}
	
	private void setColorWithPrediction(double min, double max) {
		
		double r = 0.0;
		double g = 0.0;
		double b = 0.0;
		
		if(context.lastPrediction!=null) {

			double range = max-min;
			double middle = (max+min)/2;
			double prediction = context.lastPrediction;
			if(prediction < middle){
				r = (prediction-min)/(range/2);
				b = (middle-prediction)/(range/2);
			}else{
				g = (prediction-middle)/(range/2);
				r = 1.0;
			}


		}else {
			g = 1.0;
		}
		if(context.lastPrediction == -1.0) {
			r = 1.0;
			g = 1.0;
			b = 0.0;
		}


		r = r > 1.0 ? 1.0 : r;
		g = g > 1.0 ? 1.0 : g;
		b = b > 1.0 ? 1.0 : b;
		r = r < 0.0 ? 0.0 : r;
		g = g < 0.0 ? 0.0 : g;
		b = b < 0.0 ? 0.0 : b;
		if(context.isBest) {
			drawable.setColor(new Color(0.0, 1.0, 0.0, 225d / 255d));
			drawableNDimensions.setColor(new Color(0.0, 1.0, 0.0, 225d / 255d));
		}else if(context.isActivated) {
			drawable.setColor(new Color(0.0, 0.0, 0.0, 225d / 255d));
			drawableNDimensions.setColor(new Color(0.0, 0.0, 0.0, 225d / 255d));
		}else if(context.isInNeighborhood) {
			drawable.setColor(new Color(r, g, b, 225d / 255d));
			drawableNDimensions.setColor(new Color(r, g, b, 225d / 255d));
		}else {
			drawable.setColor(new Color(r, g, b, 50d / 255d));
			drawableNDimensions.setColor(new Color(r, g, b, 10d / 255d));
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
			drawableNDimensions.setColor(new Color(r, g, b, 200d / 255d));
		}else {
			drawable.setColor(new Color(r, g, b, 90d / 255d));
			drawableNDimensions.setColor(new Color(r, g, b, 90d / 255d));
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

		/*drawableNDimensions.setWidth(context.getRanges().get(p1).getLenght());
		drawableNDimensions.setHeight(context.getRanges().get(p2).getLenght());
		drawableNDimensions.move(x, y);*/
		//drawableNDimensions.move(0, 0);

		ArrayList<Pair<Double,Double>> ranges = new ArrayList<>();
		ArrayList<Pair<Double,Double>> maxima = new ArrayList<>();
		for(int i=0; i<context.getAmas().getPercepts().size();i++){
			double startRange = context.getRanges().get(context.getAmas().getPercepts().get(i)).getStart();
			double endRange = context.getRanges().get(context.getAmas().getPercepts().get(i)).getEnd();
			double minRange = context.getAmas().getPercepts().get(i).getMin();
			double maxRange = context.getAmas().getPercepts().get(i).getMax();
			ranges.add(new Pair<>(startRange,endRange));
			maxima.add(new Pair<>(minRange,maxRange));
		}
		drawableNDimensions.setRangesAndMaxima(ranges, maxima);

	}

	/**
	 * Initialize the drawable.
	 * window.
	 */
	@Override
	public void initialize(VUIMulti vui) {
		getDrawable(vui).setName(context.toString()); // create the drawable if it does not exist
		//getDrawable(context.getAmas().multiUIWindow.VUInDimensions).setName(context.toString()); // create the drawable if it does not exist
		getNDDrawable(context.getAmas().multiUIWindow.VUInDimensions).setName(context.toString()); // create the drawable if it does not exist
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

	public DrawableNDimContext getNDDrawable(VUIMulti vui) {
		if (!context.isDying() && drawableNDimensions == null) {
			drawableNDimensions = new DrawableNDimContext(context.getAmas().getPercepts().size(), context);

			//drawableNDimensions.getNode();
			vui.addSeveralDrawables(drawableNDimensions);
		}
		return drawableNDimensions;
	}


}
