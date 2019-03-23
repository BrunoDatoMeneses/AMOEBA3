package fr.irit.smac.amak.ui.drawables;

import fr.irit.smac.amak.ui.VUI;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DrawableRectangle extends Drawable {
	private Rectangle rectangle;

	public DrawableRectangle(VUI vui, double dx, double dy, double width, double height) {
		super(vui, dx, dy, width, height);
		rectangle = new Rectangle();
		vui.getCanvas().getChildren().add(rectangle);
	}

	@Override
	public void _onDraw() {
		rectangle.setX(left());
		rectangle.setY(right());
		rectangle.setWidth(getRenderedWidth());
		rectangle.setHeight(getRenderedHeight());
		if (strokeMode)
			rectangle.setFill(Color.TRANSPARENT);
		else
			rectangle.setFill(color);
	}

}
