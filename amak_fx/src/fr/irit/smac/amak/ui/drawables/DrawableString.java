package fr.irit.smac.amak.ui.drawables;

import fr.irit.smac.amak.ui.VUI;
import javafx.scene.text.Text;

public class DrawableString extends Drawable {
	private Text textZone;

	public DrawableString(VUI vui, double dx, double dy, String text) {
		super(vui, dx+0.5, dy+0.5, 1, 1);
		textZone = new Text(text);
		vui.getCanvas().getChildren().add(textZone);
	}

	@Override
	public void _onDraw() {
		textZone.setFill(color);
		textZone.setX(left() - getWidth()/2);
		textZone.setY(top() - getHeight()/2);
	}

	public void setText(String text) {
		textZone.setText(text);
	}
}
