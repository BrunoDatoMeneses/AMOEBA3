package fr.irit.smac.amak.ui.drawables;

import fr.irit.smac.amak.ui.VUI;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DrawableRectangle extends Drawable {
	private Rectangle rectangle;

	public DrawableRectangle(VUI vui, double dx, double dy, double width, double height) {
		super(vui, dx+width/2, dy+height/2, width, height);
		rectangle = new Rectangle();
		Platform.runLater(() -> vui.getCanvas().getChildren().add(rectangle));
	}

	@Override
	public void _onDraw() {
		rectangle.setX(left());
		rectangle.setY(top());
		rectangle.setWidth(getRenderedWidth());
		rectangle.setHeight(getRenderedHeight());
		if (strokeMode)
			rectangle.setFill(Color.TRANSPARENT);
		else
			rectangle.setFill(color);
	}

	@Override
	protected void _hide() {
		Platform.runLater(() -> vui.getCanvas().getChildren().remove(rectangle));
	}

	@Override
	public void _show() {
		Platform.runLater(() -> vui.getCanvas().getChildren().add(rectangle));
	}
}
