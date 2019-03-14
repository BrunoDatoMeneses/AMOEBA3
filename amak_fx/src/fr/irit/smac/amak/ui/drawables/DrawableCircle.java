package fr.irit.smac.amak.ui.drawables;

import fr.irit.smac.amak.ui.VUI;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DrawableCircle extends Drawable {
	private Circle circle;
	
	public DrawableCircle(VUI vui, double dx, double dy, double size) {
		super(vui, dx, dy, size, size);
		circle = new Circle();
		vui.getGroup().getChildren().add(circle);
	}

	@Override
	public void _onDraw() {
		double renderedWidth = getRenderedWidth();
		double renderedHeigth = getRenderedHeight();
		circle.setCenterX(left()+renderedWidth/2);
		circle.setCenterY(right()+renderedHeigth/2);
		circle.setRadius(renderedWidth*2);
		if (strokeMode)
			circle.setFill(Color.TRANSPARENT);
		else
			circle.setFill(color);
	}


}
