package fr.irit.smac.amak.ui.drawables;

import org.kordamp.ikonli.dashicons.Dashicons;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Drawable to point things on the VUI, use a '+' icon as graphical representation.
 * @author Hugo
 *
 */
public class DrawablePoint extends Drawable {

	private FontIcon icon;
	
	public DrawablePoint(double dx, double dy) {
		super(dx, dy, 10, 10);
		icon = FontIcon.of(Dashicons.PLUS_LIGHT);
		getNode().addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				dispatchEvent(event);
			}
		});
	}

	@Override
	public void _onDraw() {
		icon.setFill(color);
		icon.setIconSize((int)Math.ceil(getRenderedWidth()));
		icon.setX(left());
		icon.setY(top());
	}

	@Override
	protected void _hide() {
		icon.setVisible(false);
	}

	@Override
	public void _show() {
		icon.setVisible(true);
	}

	@Override
	public Node getNode() {
		return icon;
	}
	
	@Override
	protected void onMouseExited(MouseEvent event) {
		icon.setIconSize((int)Math.ceil(getRenderedWidth()));
		icon.setX(left());
		icon.setY(top());
	}

	@Override
	protected void onMouseEntered(MouseEvent event) {
		icon.setIconSize((int)Math.ceil(getRenderedWidth()*1.5));
		icon.setX(left() - getRenderedWidth()*0.25 );
		icon.setY(top() + getRenderedWidth()*0.25);
	}

}
