package fr.irit.smac.amak.ui.drawables;

import fr.irit.smac.amak.tools.RunLaterHelper;
import javafx.scene.text.Text;

public class DrawableString extends Drawable {
	private Text textZone;

	public DrawableString(double dx, double dy, String text) {
		super(dx+0.5, dy+0.5, 1, 1);
		textZone = new Text(text);
	}
	
	@Override
	public void onAddedToVUI() {
		RunLaterHelper.runLater(()-> vui.getCanvas().getChildren().add(textZone));
	}

	@Override
	public void _onDraw() {
		textZone.setFill(color);
		textZone.setX(left());
		textZone.setY(top());
	}

	public void setText(String text) {
		textZone.setText(text);
	}

	@Override
	protected void _hide() {
		textZone.setVisible(false);
	}

	@Override
	public void _show() {
		textZone.setVisible(true);
	}
}
