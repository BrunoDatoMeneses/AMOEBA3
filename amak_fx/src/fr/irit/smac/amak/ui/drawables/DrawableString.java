package fr.irit.smac.amak.ui.drawables;

import fr.irit.smac.amak.ui.VUI;
import javafx.scene.text.Text;

public class DrawableString extends Drawable {
	private Text textZone;

	public DrawableString(VUI vui, double dx, double dy, String text) {
		super(vui, dx, dy, 1, 1);
		textZone = new Text(text);
		vui.getCanvas().getChildren().add(textZone);
	}

	@Override
	public void _onDraw() {
		textZone.setFill(color);
		textZone.setX(left());
		textZone.setY(right());
	}

	public void setText(String text) {
		textZone.setText(text);
	}
}
