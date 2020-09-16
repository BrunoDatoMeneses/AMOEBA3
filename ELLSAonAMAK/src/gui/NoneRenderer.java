package gui;

import fr.irit.smac.amak.ui.VUIMulti;

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
	public void initialize(VUIMulti vui) {
	}

	@Override
	public void render() {
	}
	
	@Override
	public void delete() {
	}

}