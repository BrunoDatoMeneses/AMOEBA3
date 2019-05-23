package gui.context;

import java.util.ArrayList;
import java.util.List;

import agents.context.Context;
import gui.AmoebaWindow;
import gui.Visualization;
import javafx.event.Event;

/**
 * A class that contain all {@link Visualization} for a {@link Context}.
 * @author Hugo
 *
 */
public class ContextVisualizations {
	private List<Visualization> visualizations;
	private Context context;
	
	private ContextRectangle drawable;
	private MiniContextRectangle mini;
	
	public ContextVisualizations(Context context) {
		this.context = context;
		visualizations = new ArrayList<>();
	}
	
	/**
	 * Dispatch an event to all visualization.
	 * @param event
	 */
	public void dispatchEvent(Event event) {
		if("MOUSE_CLICKED".equals(event.getEventType().getName())) {
			if(context.getRenderStrategy() instanceof ContextRendererFX) {
				ContextRendererFX.contextExplorer.update();
			}
		}
		synchronized (visualizations) {
			for(Visualization v : visualizations) {
				v.onEvent(event);
			}
		}
	}
	
	/**
	 * Return the visualization for the VUI, may create it.
	 * @return
	 */
	public ContextRectangle getDrawable() {
		if(!context.isDying() && drawable == null) {
			drawable = new ContextRectangle(0, 0, 10, 10, context);
			visualizations.add(drawable);
			AmoebaWindow.instance().mainVUI.add(drawable);
		}
		return drawable;
	}
	
	/**
	 * Return the visualization quick access, may create it.
	 * Used in the {@link ContextExplorer}.
	 * @return
	 */
	public MiniContextRectangle getMini() {
		if(!context.isDying() && mini == null) {
			mini = new MiniContextRectangle(getDrawable());
			visualizations.add(mini);
		}
		return mini;
	}
}
