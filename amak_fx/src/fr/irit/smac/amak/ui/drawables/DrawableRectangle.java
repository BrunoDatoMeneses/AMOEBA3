package fr.irit.smac.amak.ui.drawables;

import fr.irit.smac.amak.ui.VUI;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DrawableRectangle extends Drawable {
	private Rectangle rectangle;

	public DrawableRectangle(VUI vui, double dx, double dy, double width, double height) {
		super(vui, dx+width/2, dy+height/2, width, height);
		rectangle = new Rectangle();
		vui.getCanvas().getChildren().add(rectangle);
	}

	@Override
	public void _onDraw() {
		rectangle.setX(left() - getWidth() / 2);
		rectangle.setY(top() - getHeight() / 2);
		rectangle.setWidth(getRenderedWidth());
		rectangle.setHeight(getRenderedHeight());
		if (strokeMode)
			rectangle.setFill(Color.TRANSPARENT);
		else
			rectangle.setFill(color);
	}

}
