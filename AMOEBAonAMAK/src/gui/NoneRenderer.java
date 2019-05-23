package gui;

/**
 * A render strategy that does nothing.
 * @author Hugo
 *
 */
public class NoneRenderer extends RenderStrategy {

	public NoneRenderer(Object o) {
		super(o);
	}

	@Override
	public void initialize() {
	}

	@Override
	public void render() {
	}
	
	@Override
	public void delete() {
	}

}
