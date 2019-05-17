package fr.irit.smac.amak.ui.drawables;

import fr.irit.smac.amak.tools.RunLaterHelper;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DrawableRectangle extends Drawable {
	public Rectangle rectangle;

	public DrawableRectangle(double dx, double dy, double width, double height) {
		super(dx+width/2, dy+height/2, width, height);
		rectangle = new Rectangle();
	}
	
	@Override
	public void onAddedToVUI() {
		RunLaterHelper.runLater(()-> vui.getCanvas().getChildren().add(rectangle));
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
		RunLaterHelper.runLater(() -> vui.getCanvas().getChildren().remove(rectangle));
	}

	@Override
	public void _show() {
		RunLaterHelper.runLater(() -> vui.getCanvas().getChildren().add(rectangle));
	}
}
