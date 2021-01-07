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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import kernel.ELLSA;
import kernel.backup.SaveHelperImpl;

/**
 * The main window for AMOEBA GUI.
 * @author Hugo
 *
 */
public class EllsaWindow extends MainWindow {

	protected HashMap<String, AmakPlot> plots = new HashMap<>();
	
	/**
	 * The main {@link VUI} for AMOEBA, by default it's the 2D representation of the contexts.
	 */
	public VUI mainVUI;
	
	public Drawable point;
	public Drawable rectangle;
	public ToggleButton toggleRender;
	public SchedulerToolbar schedulerToolbar;
	public DimensionSelector dimensionSelector;
	public Menu windowMenu;
	
	public EllsaWindow() throws InstanceAlreadyExistsException {
		super();
	}
	
	public void initialize(ELLSA ellsa) {
		
		mainVUI = VUI.get("2D");
		mainVUI.setDefaultView(200, 0, 0);
		EllsaWindow.addTabbedPanel("2D VUI", mainVUI.getPanel());
		
		// scheduler toolbar
		schedulerToolbar = new SchedulerToolbar("AMOEBA", ellsa.getScheduler());
		EllsaWindow.addToolbar(schedulerToolbar);
		
		// plots
		point = mainVUI.createAndAddPoint(0, 0);
		point.setName("Cursor");
		rectangle = mainVUI.createAndAddRectangle(10, 10, 10, 10);
		rectangle.setName("Neighborhood");
		rectangle.setColor(new Color(1, 1, 1, 0));
		
		plots.put("This loop NCS", new AmakPlot("This loop NCS", ChartType.LINE, "Cycle", "Number of NCS"));
		plots.put("All time NCS", new AmakPlot("All time NCS", ChartType.LINE, "Cycle", "Number of NCS"));
		plots.put("Number of agents", new AmakPlot("Number of agents", ChartType.LINE, "Cycle", "Number of agents"));
		plots.put("Errors", new AmakPlot("Errors", ChartType.LINE, "Cycle", "Coefficients"));
		plots.put("Distances to models", new AmakPlot("Distances to models", ChartType.LINE, "Cycle", "Distances"));
		plots.put("Global Mapping Criticality", new AmakPlot("Global Mapping Criticality", ChartType.LINE, "Cycle", "Criticalities"));
		plots.put("Time Execution", new AmakPlot("Time Execution", ChartType.LINE, "Cycle", "Times"));
		plots.put("Criticalities", new AmakPlot("Criticalities", ChartType.LINE, "Cycle", "Criticalities"));
		
		// update render button
		toggleRender = new ToggleButton("Allow Rendering");
		toggleRender.setOnAction(evt -> {
			ellsa.setRenderUpdate(toggleRender.isSelected());
			if(ellsa.isRenderUpdate()) {
				ellsa.updateAgentsVisualisation();
				ellsa.nextCycleRunAllAgents();
			}
		});
		toggleRender.setSelected(ellsa.isRenderUpdate());
		EllsaWindow.addToolbar(toggleRender);
		
		// dimension selector
		dimensionSelector = new DimensionSelector(ellsa.getPercepts(), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ellsa.updateAgentsVisualisation();
			}
		});
		RunLaterHelper.runLater(()->mainVUI.toolbar.getItems().add(dimensionSelector));
		
		// contextMenu "Request Here" on VUI
		new ContextMenuVUI(ellsa, mainVUI); //the ContextMenu add itself to the VUI
		
		// manual save button
		EllsaWindow.addToolbar(newManualSaveButton(ellsa));
		
		Slider slider = new Slider(0, 0.1, 0.1);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				ellsa.getEnvironment().setMappingErrorAllowed(newValue.doubleValue());
			}
		});
		EllsaWindow.addToolbar(slider);
	}
	
	/**
	 * Return the unique instance of MainWindow, may create it.
	 * 
	 * @return instance
	 */
	public static EllsaWindow instance() {
		if(!isInstance()) {
			instanceLock.lock();
			if(!isInstance()) {
				Thread ui = new Thread(new Runnable() {
					@Override
					public void run() {
						Application.launch(EllsaWindow.class);
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
		return (EllsaWindow) instance;
	}
	
	/**
	 * Get an existing {@link AmakPlot}. 
	 * @param name name of the plot to get
	 * @return an existing plot.
	 * @see EllsaWindow#addPlot(String, AmakPlot)
	 */
	public AmakPlot getPlot(String name) {
		return plots.get(name);
	}
	
	/**
	 * Add an {@link AmakPlot} to le map of plots. Allowing for easy access with {@code AmoebaWindow.instance().getPlot(name)}
	 * @param name name of the plot to add
	 * @param plot the plot to add
	 * @see EllsaWindow#getPlot(String)
	 */
	public void addPlot(String name, AmakPlot plot) {
		plots.put(name, plot);
	}
	
	/**
	 * Create a button 'Quick Save' button, when clicked create a manual save point using an amoeba's saver.
	 * @param ellsa
	 * @return
	 * @see ELLSA#saver
	 * @see SaveHelperImpl#newManualSave(String)
	 */
	public Button newManualSaveButton(ELLSA ellsa) {
		Button button = new Button("Quick save");
		button.setTooltip(new Tooltip("Create a new save point. You will be able to find it in 'Save Explorer' -> 'Manual Saves'"));
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(ellsa.saver != null) {
					ellsa.saver.newManualSave("manualSaveButton");
				} else {
					Log.defaultLog.error("Main Window", "Cannot make a save point of an amoeba without saver");
				}
			}
		});
		return button;
	}
}
