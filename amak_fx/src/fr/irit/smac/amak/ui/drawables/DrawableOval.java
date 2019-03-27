package fr.irit.smac.amak.ui.drawables;

import fr.irit.smac.amak.ui.VUI;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

public class DrawableOval extends Drawable {
	Ellipse ellipse;
	
	public DrawableOval(VUI vui, double dx, double dy, double width, double height) {
		super(vui, dx, dy, width, height);
		ellipse = new Ellipse();
		vui.getCanvas().getChildren().add(ellipse);
	}

	@Override
	public void _onDraw() {
		double renderedWidth = getRenderedWidth();
		double renderedHeigth = getRenderedHeight();
		ellipse.setCenterX(left()+renderedWidth/2);
		ellipse.setCenterY(top()+renderedHeigth/2);
		ellipse.setRadiusX(renderedWidth*2);
		ellipse.setRadiusY(renderedHeigth*2);
		if (strokeMode)
			ellipse.setFill(Color.TRANSPARENT);
		else
			ellipse.setFill(color);
	}


}
