package gui;

import agents.context.Context;
import fr.irit.smac.amak.ui.drawables.DrawableRectangle;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * The visualization of a Context in 2D
 * @author Hugo
 *
 */
public class ContextRectangle extends DrawableRectangle implements Visualization{
	protected Context context;
	protected static String defaultStyle = "-fx-stroke: black; -fx-stroke-width: 1;";
	
	public ContextRectangle(double dx, double dy, double width, double height, Context context) {
		super(dx, dy, width, height);
		this.context = context;
		
		rectangle.setStyle(defaultStyle);
		
		rectangle.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				AmoebaWindow.instance().getContextVisualizations(context).dispatchEvent(event);
			}
		});
	}
	
	@Override
	public void onEvent(Event event) {
		switch (event.getEventType().getName()) {
		case "MOUSE_CLICKED":
			onMouseClick((MouseEvent)event);
			break;
		case "MOUSE_ENTERED":
			onMouseEntered((MouseEvent)event);
			break;
		case "MOUSE_EXITED":
			onMouseExited((MouseEvent)event);
			break;
		default:
			break;
		}
	}
	
	protected void onMouseClick(MouseEvent event) {
	}
	
	protected void onMouseEntered(MouseEvent event) {
		rectangle.setStyle("-fx-stroke: black; -fx-stroke-width: 3;");
	}
	
	protected void onMouseExited(MouseEvent event) {
		rectangle.setStyle(defaultStyle);
	}
	
	@Override
	public Node getNode() {
		return rectangle;
	}

}
