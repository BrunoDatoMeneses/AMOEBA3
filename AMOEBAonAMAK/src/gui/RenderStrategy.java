package gui;

/**
 * Strategy on how to render on object.
 * @author Hugo
 *
 * @param <E> class of the object to be rendered.
 */
public interface RenderStrategy {
	
	/**
	 * Called when the render of an object need to be initialized.
	 * @param object object to be rendered.
	 */
	public void initialize(Object object);
	
	/**
	 * Called to render an object.
	 * @param object object to be rendered.
	 */
	public void render(Object object);
	
	/**
	 * Called when the render of an object is no longer needed.
	 * @param object object to be rendered.
	 */
	public void delete(Object object);

}
