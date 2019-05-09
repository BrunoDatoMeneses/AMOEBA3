package fr.irit.smac.amak.ui.drawables;

import fr.irit.smac.amak.ui.VUI;
import javafx.application.Platform;
import javafx.scene.shape.Line;

public class DrawableLine extends Drawable {
	Line line;

	public DrawableLine(VUI vui, double dx, double dy, double tx, double ty) {
		super(vui, 0, 0, 0, 0);
		line = new Line(dx, dy, tx, ty);
		Platform.runLater(() -> vui.getCanvas().getChildren().add(line));
	}

	@Override
	public void _onDraw() {
		line.setFill(color);
		if (!isFixed()) {
			line.setStartX(vui.worldToScreenX(line.getStartX()));
			line.setStartY(vui.worldToScreenY(line.getStartY()));
			line.setEndX(vui.worldToScreenX(line.getEndX()));
			line.setEndY(vui.worldToScreenY(line.getEndY()));
		}
	}

	public void move(double dx, double dy, double tx, double ty) {
		line.setStartX(dx);
		line.setStartY(dy);
		line.setEndX(tx);
		line.setEndY(ty);
		update();
	}

	@Override
	protected void _hide() {
		line.setVisible(false);
	}

	@Override
	public void _show() {
		line.setVisible(true);
	}
}
