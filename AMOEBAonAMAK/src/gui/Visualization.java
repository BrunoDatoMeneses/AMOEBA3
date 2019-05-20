package gui;

import javafx.event.Event;
import javafx.scene.Node;

/**
 * A basic interface for elements that need to be displayed.
 * @author Hugo
 *
 */
public interface Visualization {

	/**
	 * Return the graphical node of this visualization
	 * @return
	 */
	public Node getNode();
	
	/**
	 * Called when a visualization need to respond to an event
	 * @param event
	 */
	public void onEvent(Event event);
	
	/**
	 * Force the visualization to update itself
	 */
	public void update();
}
