package gui;

import java.util.HashMap;

import javax.management.InstanceAlreadyExistsException;

import agents.context.Context;
import fr.irit.smac.amak.ui.AmakPlot;
import fr.irit.smac.amak.ui.AmakPlot.ChartType;
import fr.irit.smac.amak.ui.MainWindow;
import fr.irit.smac.amak.ui.SchedulerToolbar;
import fr.irit.smac.amak.ui.VUI;
import fr.irit.smac.amak.ui.drawables.Drawable;
import javafx.application.Application;
import javafx.scene.control.Menu;
import javafx.scene.control.ToggleButton;
import kernel.AMOEBA;

/**
 * The main window for AMOEBA GUI.
 * @author Hugo
 *
 */
public class AmoebaWindow extends MainWindow {

	private HashMap<String, AmakPlot> plots = new HashMap<>();
	
	private HashMap<Context, ContextVisualizations> contextVisualizationMap = new HashMap<>();
	
	public Drawable point;
	public ToggleButton toggleRender;
	public SchedulerToolbar schedulerToolbar;
	public DimensionSelector dimensionSelector;
	public ContextExplorer contextExplorer;
	public Menu windowMenu;
	
	public AmoebaWindow() throws InstanceAlreadyExistsException {
		super();
	}
	
	public void initialize(AMOEBA amoeba) {
		
		// scheduler toolbar
		schedulerToolbar = new SchedulerToolbar("AMOEBA", amoeba.getScheduler());
		AmoebaWindow.addToolbar(schedulerToolbar);	
		
		// amoeba and agent
		VUI.get().setDefaultView(200, 0, 0);
		point = VUI.get().createPoint(0, 0);
		plots.put("This loop NCS", new AmakPlot("This loop NCS", ChartType.LINE, "Cycle", "Number of NCS"));
		plots.put("All time NCS", new AmakPlot("All time NCS", ChartType.LINE, "Cycle", "Number of NCS"));
		plots.put("Number of agents", new AmakPlot("Number of agents", ChartType.LINE, "Cycle", "Number of agents"));
		plots.put("Errors", new AmakPlot("Errors", ChartType.LINE, "Cycle", "Coefficients"));

		// update render button
		toggleRender = new ToggleButton("Allow Rendering");
		toggleRender.setOnAction(evt -> {
			amoeba.renderUpdate = toggleRender.isSelected(); 
			if(amoeba.renderUpdate) {
				amoeba.updateAgentsVisualisation();
				amoeba.nextCycleRunAllAgents();
			}
		});
		toggleRender.setSelected(amoeba.renderUpdate);
		AmoebaWindow.addToolbar(toggleRender);
		
		// dimension selector
		dimensionSelector = new DimensionSelector(amoeba);
		AmoebaWindow.addToolbar(dimensionSelector);
		
		// contextMenu "Request Here" on VUI
		new ContextMenuVUI(amoeba); //the ContextMenu add itself to the VUI
	}
	
	/**
	 * Return the unique instance of MainWindow, may create it.
	 * 
	 * @return instance
	 */
	public static AmoebaWindow instance() {
		if(!isInstance()) {
			instanceLock.lock();
			if(!isInstance()) {
				Thread ui = new Thread(new Runnable() {
					@Override
					public void run() {
						Application.launch(AmoebaWindow.class);
					}
				});
				ui.start();
				try {
					synchronized (startEnded) {
						startEnded.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.err.println("Failure at start : Cannot be sure that the MainWindow is correctly launched. Exit.");
					System.exit(1);
				}
			}
			instanceLock.unlock();
		}
		return (AmoebaWindow) instance;
	}
	
	public AmakPlot getPlot(String name) {
		return plots.get(name);
	}
	
	public ContextVisualizations getContextVisualizations(Context context) {
		if(!contextVisualizationMap.containsKey(context)) {
			contextVisualizationMap.put(context, new ContextVisualizations(context));
		}
		return contextVisualizationMap.get(context);
	}
	
	public void removeContextVisualization(Context context) {
		contextVisualizationMap.remove(context);
	}

}
