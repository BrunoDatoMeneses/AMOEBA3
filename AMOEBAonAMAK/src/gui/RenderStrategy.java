package gui;

import fr.irit.smac.amak.ui.VUIMulti;

/**
 * Strategy on how to render an object.
 * See {@link ContextRendererFX} for example on how to extends this class.
 * @author Hugo
 */
public abstract class RenderStrategy {
	
	/**
	 * @param o the object to be rendered
	 */
	public RenderStrategy(Object o) {
	}
	
	/**
	 * Called when the rendered object need to be initialized
	 */
	//abstract public void initialize();
	abstract public void initialize(VUIMulti vui);
	
	/**
	 * Called to render the object.
	 */
	abstract public void render();
	
	/**
	 * Called when the render of the object is no longer needed.
	 */
	abstract public void delete();

}
