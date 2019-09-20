package gui;

import java.util.HashMap;

import javax.management.InstanceAlreadyExistsException;

import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.tools.RunLaterHelper;
import fr.irit.smac.amak.ui.AmakPlot;
import fr.irit.smac.amak.ui.AmakPlot.ChartType;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.MainWindow;
import fr.irit.smac.amak.ui.SchedulerToolbar;
import fr.irit.smac.amak.ui.VUI;
import fr.irit.smac.amak.ui.VUIMulti;
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
import kernel.AMOEBA;
import kernel.backup.SaveHelperImpl;

/**
 * The multi window for AMOEBA GUI.
 * @author Bruno
 *
 */
public class AmoebaMultiUIWindow extends AmasMultiUIWindow{

	protected HashMap<String, AmakPlot> plots = new HashMap<>();
	
	/**
	 * The main {@link VUI} for AMOEBA, by default it's the 2D representation of the contexts.
	 */
	public VUIMulti mainVUI;
	
	public Drawable point;
	public Drawable rectangle;
	public ToggleButton toggleRender;
	public SchedulerToolbar schedulerToolbar;
	public DimensionSelector dimensionSelector;
	public Menu windowMenu;
	
	public AmoebaMultiUIWindow(String title, VUIMulti vui) {
		super(title);
		mainVUI = vui;
	}
	
	public void initialize(AMOEBA amoeba) {
		

		mainVUI.setDefaultView(200, 0, 0);
		//addTabbedPanel("2D VUI", mainVUI.getPanel());
		
		// scheduler toolbar
		schedulerToolbar = new SchedulerToolbar("AMOEBA", amoeba.getScheduler());
		addToolbar(schedulerToolbar);	
		
		// plots
		point = mainVUI.createAndAddPoint(0, 0);
		point.setName("Cursor");
		rectangle = mainVUI.createAndAddRectangle(10, 10, 10, 10);
		rectangle.setName("Neighborhood");
		rectangle.setColor(new Color(1, 1, 1, 0));
		
		plots.put("This loop NCS", new AmakPlot(this, "This loop NCS", ChartType.LINE, "Cycle", "Number of NCS"));
		plots.put("All time NCS", new AmakPlot(this, "All time NCS", ChartType.LINE, "Cycle", "Number of NCS"));
		plots.put("Number of agents", new AmakPlot(this, "Number of agents", ChartType.LINE, "Cycle", "Number of agents"));
		plots.put("Errors", new AmakPlot(this, "Errors", ChartType.LINE, "Cycle", "Coefficients"));
		plots.put("Distances to models", new AmakPlot(this, "Distances to models", ChartType.LINE, "Cycle", "Distances"));
		plots.put("Global Mapping Criticality", new AmakPlot(this, "Global Mapping Criticality", ChartType.LINE, "Cycle", "Criticalities"));
		plots.put("Time Execution", new AmakPlot(this, "Time Execution", ChartType.LINE, "Cycle", "Times"));
		plots.put("Criticalities", new AmakPlot(this, "Criticalities", ChartType.LINE, "Cycle", "Criticalities"));
		
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
		addToolbar(toggleRender);
		
		// dimension selector
		dimensionSelector = new DimensionSelector(amoeba.getPercepts(), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				amoeba.updateAgentsVisualisation();
			}
		});
		RunLaterHelper.runLater(()->mainVUI.toolbar.getItems().add(dimensionSelector));
		
		// contextMenu "Request Here" on VUI
		new ContextMenuVUIMulti(amoeba, mainVUI); //the ContextMenu add itself to the VUI
		
		// manual save button
		addToolbar(newManualSaveButton(amoeba));
		
		Slider slider = new Slider(0, 0.1, 0.1);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				amoeba.getEnvironment().mappingErrorAllowed = newValue.doubleValue();
			}
		});
		addToolbar(slider);
	}
	
	
	
	/**
	 * Get an existing {@link AmakPlot}. 
	 * @param name name of the plot to get
	 * @return an existing plot.
	 * @see AmoebaMultiUIWindow#addPlot(String, AmakPlot)
	 */
	public AmakPlot getPlot(String name) {
		return plots.get(name);
	}
	
	/**
	 * Add an {@link AmakPlot} to le map of plots. Allowing for easy access with {@code AmoebaWindow.instance().getPlot(name)}
	 * @param name name of the plot to add
	 * @param plot the plot to add
	 * @see AmoebaMultiUIWindow#getPlot(String)
	 */
	public void addPlot(String name, AmakPlot plot) {
		plots.put(name, plot);
	}
	
	/**
	 * Create a button 'Quick Save' button, when clicked create a manual save point using an amoeba's saver.
	 * @param amoeba
	 * @return
	 * @see AMOEBA#saver
	 * @see SaveHelperImpl#newManualSave(String)
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
