package fr.irit.smac.amak.ui.drawables;

import fr.irit.smac.amak.ui.VUI;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DrawableCircle extends Drawable {
	private Circle circle;
	
	public DrawableCircle(VUI vui, double dx, double dy, double size) {
		super(vui, dx, dy, size, size);
		circle = new Circle();
		Platform.runLater(() -> vui.getCanvas().getChildren().add(circle));
	}

	@Override
	public void _onDraw() {
		double renderedWidth = getRenderedWidth();
		double renderedHeigth = getRenderedHeight();
		circle.setCenterX(left()+renderedWidth/2);
		circle.setCenterY(top()+renderedHeigth/2);
		circle.setRadius(renderedWidth*2);
		if (strokeMode)
			circle.setFill(Color.TRANSPARENT);
		else
			circle.setFill(color);
	}

	@Override
	protected void _hide() {
		circle.setVisible(false);
	}

	@Override
	public void _show() {
		circle.setVisible(true);
	}


}
