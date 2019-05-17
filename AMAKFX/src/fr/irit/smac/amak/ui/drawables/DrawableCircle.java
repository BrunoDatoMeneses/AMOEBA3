package fr.irit.smac.amak.ui.drawables;

import fr.irit.smac.amak.tools.RunLaterHelper;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DrawableCircle extends Drawable {
	private Circle circle;
	
	public DrawableCircle(double dx, double dy, double size) {
		super(dx, dy, size, size);
		circle = new Circle();
	}
	
	@Override
	public void onAddedToVUI() {
		RunLaterHelper.runLater(()-> vui.getCanvas().getChildren().add(circle));
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
