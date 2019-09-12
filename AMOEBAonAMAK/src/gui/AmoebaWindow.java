package gui;

import java.util.HashMap;

import javax.management.InstanceAlreadyExistsException;

import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.tools.RunLaterHelper;
import fr.irit.smac.amak.ui.AmakPlot;
import fr.irit.smac.amak.ui.AmakPlot.ChartType;
import fr.irit.smac.amak.ui.MainWindow;
import fr.irit.smac.amak.ui.SchedulerToolbar;
import fr.irit.smac.amak.ui.VUI;
import fr.irit.smac.amak.ui.drawables.Drawable;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import kernel.AMOEBA;
import kernel.SaveHelper;

/**
 * The main window for AMOEBA GUI.
 * @author Hugo
 *
 */
public class AmoebaWindow extends MainWindow {

	protected HashMap<String, AmakPlot> plots = new HashMap<>();
	
	/**
	 * The main {@link VUI} for AMOEBA, by default it's the 2D representation of the contexts.
	 */
	public VUI mainVUI;
	
	public Drawable point;
	public ToggleButton toggleRender;
	public SchedulerToolbar schedulerToolbar;
	public DimensionSelector dimensionSelector;
	public Menu windowMenu;
	
	public AmoebaWindow() throws InstanceAlreadyExistsException {
		super();
	}
	
	public void initialize(AMOEBA amoeba) {
		
		mainVUI = VUI.get("2D");
		mainVUI.setDefaultView(200, 0, 0);
		AmoebaWindow.addTabbedPanel("2D VUI", mainVUI.getPanel());
		
		// scheduler toolbar
		schedulerToolbar = new SchedulerToolbar("AMOEBA", amoeba.getScheduler());
		AmoebaWindow.addToolbar(schedulerToolbar);	
		
		// plots
		point = mainVUI.createAndAddPoint(0, 0);
		point.setName("Cursor");
		plots.put("This loop NCS", new AmakPlot("This loop NCS", ChartType.LINE, "Cycle", "Number of NCS"));
		plots.put("All time NCS", new AmakPlot("All time NCS", ChartType.LINE, "Cycle", "Number of NCS"));
		plots.put("Number of agents", new AmakPlot("Number of agents", ChartType.LINE, "Cycle", "Number of agents"));
		plots.put("Errors", new AmakPlot("Errors", ChartType.LINE, "Cycle", "Coefficients"));

		// update render button
		toggleRender = new ToggleButton("Allow Rendering");
		toggleRender.setOnAction(evt -> {
			amoeba.setRenderUpdate(toggleRender.isSelected()); 
			if(amoeba.isRenderUpdate()) {
				amoeba.updateAgentsVisualisation();
				amoeba.nextCycleRunAllAgents();
			}
		});
		toggleRender.setSelected(amoeba.isRenderUpdate());
		AmoebaWindow.addToolbar(toggleRender);
		
		// dimension selector
		dimensionSelector = new DimensionSelector(amoeba.getPercepts(), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				amoeba.updateAgentsVisualisation();
			}
		});
		RunLaterHelper.runLater(()->mainVUI.toolbar.getItems().add(dimensionSelector));
		
		// contextMenu "Request Here" on VUI
		new ContextMenuVUI(amoeba, mainVUI); //the ContextMenu add itself to the VUI
		
		// manual save button
		AmoebaWindow.addToolbar(newManualSaveButton(amoeba));
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
	
	/**
	 * Get an existing {@link AmakPlot}. 
	 * @param name name of the plot to get
	 * @return an existing plot.
	 * @see AmoebaWindow#addPlot(String, AmakPlot)
	 */
	public AmakPlot getPlot(String name) {
		return plots.get(name);
	}
	
	/**
	 * Add an {@link AmakPlot} to le map of plots. Allowing for easy access with {@code AmoebaWindow.instance().getPlot(name)}
	 * @param name name of the plot to add
	 * @param plot the plot to add
	 * @see AmoebaWindow#getPlot(String)
	 */
	public void addPlot(String name, AmakPlot plot) {
		plots.put(name, plot);
	}
	
	/**
	 * Create a button 'Quick Save' button, when clicked create a manual save point using an amoeba's saver.
	 * @param amoeba
	 * @return
	 * @see AMOEBA#saver
	 * @see SaveHelper#newManualSave(String)
	 */
	public Button newManualSaveButton(AMOEBA amoeba) {
		Button button = new Button("Quick save");
		button.setTooltip(new Tooltip("Create a new save point. You will be able to find it in 'Save Explorer' -> 'Manual Saves'"));
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(amoeba.saver != null) {
					amoeba.saver.newManualSave("manualSaveButton");
				} else {
					Log.defaultLog.error("Main Window", "Cannot make a save point of an amoeba without saver");
				}
			}
		});
		return button;
	}
}
