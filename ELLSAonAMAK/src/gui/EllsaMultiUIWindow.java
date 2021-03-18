package gui;

import java.util.ArrayList;
import java.util.HashMap;

import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.tools.RunLaterHelper;
import fr.irit.smac.amak.ui.AmakPlot;
import fr.irit.smac.amak.ui.AmakPlot.ChartType;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.SchedulerToolbar;
import fr.irit.smac.amak.ui.VUI;
import fr.irit.smac.amak.ui.VUIMulti;
import fr.irit.smac.amak.ui.drawables.Drawable;
import fr.irit.smac.amak.ui.drawables.DrawableString;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.backup.SaveHelperImpl;

/**
 * The multi window for AMOEBA GUI.
 * @author Bruno
 *
 */
public class EllsaMultiUIWindow extends AmasMultiUIWindow{

	protected HashMap<String, AmakPlot> plots = new HashMap<>();
	
	/**
	 * The main {@link VUI} for AMOEBA, by default it's the 2D representation of the contexts.
	 */
	public VUIMulti mainVUI;
	public VUIMulti VUInDimensions;
	public View3D view3D;
	public View3DContexts view3DContexts;
	
	public Drawable point;
	public Drawable pointVerticalLine;
	public Drawable pointHorizontalLine;
	public Drawable rectangle;
	public ToggleButton toggleRender;
	public SchedulerToolbar schedulerToolbar;
	public DimensionSelector dimensionSelector;
	public DimensionSelector3D dimensionSelector3D;
	public Menu windowMenu;
	public ToggleButton togglecontextColorByCoef;
	public ToggleButton togglecontextColorByPrediction;

	public StudiedSystem studiedSystem = null;
	public GuiData guiData ;
	
	public EllsaMultiUIWindow(String title, VUIMulti vui, StudiedSystem ss) {
		super(title);
		mainVUI = vui;
		VUInDimensions =  new VUIMulti("ND");

		studiedSystem = ss;
		guiData = new GuiData();
	}
	
	public void initialize(ELLSA ellsa) {
		

		mainVUI.setDefaultView(200, 0, 0);
		VUInDimensions.setDefaultView(200, 0, 0);
		//addTabbedPanel("2D VUI", mainVUI.getPanel());
		
		// scheduler toolbar
		schedulerToolbar = new SchedulerToolbar("ELLSA", ellsa.getScheduler());
		addToolbar(schedulerToolbar);	
		
		// plots
		point = mainVUI.createAndAddPoint(0, 0);
		point.setName("Cursor");
		rectangle = mainVUI.createAndAddRectangle(10, 10, 10, 10);
		//VUInDimensions.createAndAddRectangle(10, 10, 10, 10);
		rectangle.setName("Neighborhood");
		pointHorizontalLine = mainVUI.createAndAddRectangle(0,0,20000,0.1);
		pointVerticalLine = mainVUI.createAndAddRectangle(0,0,0.1,20000);
		pointHorizontalLine.setName("pointHorizontalLine");
		pointVerticalLine.setName("pointVerticalLine");

		rectangle.setColor(new Color(1, 1, 1, 0));



		this.addTabbedPanel("ND", VUInDimensions.getPanel());


		if(studiedSystem != null){
			view3D = new View3D(studiedSystem, ellsa);
			this.addTabbedPanel("3D Models", view3D.getPane());
		}
		view3DContexts = new View3DContexts(ellsa);
		this.addTabbedPanel("3D Contexts", view3DContexts.getPane());


		
		plots.put("This loop NCS", new AmakPlot(this, "This loop NCS", ChartType.LINE, "Cycle", "Number of NCS"));
		plots.put("All time NCS", new AmakPlot(this, "All time NCS", ChartType.LINE, "Cycle", "Number of NCS"));
		plots.put("Number of agents", new AmakPlot(this, "Number of agents", ChartType.LINE, "Cycle", "Number of agents"));
		plots.put("Errors", new AmakPlot(this, "Errors", ChartType.LINE, "Cycle", "Coefficients"));
		plots.put("Distances to models", new AmakPlot(this, "Distances to models", ChartType.LINE, "Cycle", "Distances"));
		plots.put("Global Mapping Criticality", new AmakPlot(this, "Global Mapping Criticality", ChartType.LINE, "Cycle", "Criticalities"));
		plots.put("Time Execution", new AmakPlot(this, "Time Execution", ChartType.LINE, "Cycle", "Times"));
		plots.put("Time Execution All", new AmakPlot(this, "Time Execution All", ChartType.LINE, "Cycle", "Times"));
		plots.put("Criticalities", new AmakPlot(this, "Criticalities", ChartType.LINE, "Cycle", "Criticalities"));
		plots.put("Number of neighbors", new AmakPlot(this, "Number of neighbors", ChartType.LINE, "Cycle", "Number of neighbors"));



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
		addToolbar(toggleRender);


		

		
		// contextMenu "Request Here" on VUI
		new ContextMenuVUIMulti(ellsa, mainVUI); //the ContextMenu add itself to the VUI
		
		// manual save button
		addToolbar(newManualSaveButton(ellsa));

		Label text2D = new Label("2D");
		RunLaterHelper.runLater(()->toolbarPanel.getItems().add(text2D));

		// dimension selector
		dimensionSelector = new DimensionSelector(ellsa.getPercepts(), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ellsa.updateAgentsVisualisation();
			}
		});
		RunLaterHelper.runLater(()->toolbarPanel.getItems().add(dimensionSelector));

		Label text3D = new Label("3D");
		RunLaterHelper.runLater(()->toolbarPanel.getItems().add(text3D));

		// dimension selector 3D
		dimensionSelector3D = new DimensionSelector3D(ellsa.getPercepts(), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ellsa.updateAgentsVisualisation();
			}
		});
		RunLaterHelper.runLater(()->toolbarPanel.getItems().add(dimensionSelector3D));




		togglecontextColorByCoef = new ToggleButton("Color by\ncoef");
		togglecontextColorByCoef.setOnAction(evt -> {
			guiData.contextColorByCoef = togglecontextColorByCoef.isSelected();
			togglecontextColorByPrediction.setSelected(false);
		});
		togglecontextColorByCoef.setSelected(guiData.contextColorByCoef);

		addToolbar(togglecontextColorByCoef);

		togglecontextColorByPrediction = new ToggleButton("Color by\nprediction");
		togglecontextColorByPrediction.setOnAction(evt -> {
			guiData.contextColorByCoef = !togglecontextColorByPrediction.isSelected();
			togglecontextColorByCoef.setSelected(false);
		});
		togglecontextColorByPrediction.setSelected(guiData.contextColorByCoef);
		addToolbar(togglecontextColorByPrediction);




		Label mappingError = new Label("Mapping\nError");
		RunLaterHelper.runLater(()->toolbarPanel.getItems().add(mappingError));

		Slider slider = new Slider(0, 0.1, ellsa.getEnvironment().getMappingErrorAllowed());
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				ellsa.getEnvironment().setMappingErrorAllowed(newValue.doubleValue())  ;
			}
		});
		addToolbar(slider);

		Label refreshCycle = new Label("Refresh\nCycle");
		RunLaterHelper.runLater(()->toolbarPanel.getItems().add(refreshCycle));
		Slider refreshCycleSlider = new Slider(1, 250, 50);
		refreshCycleSlider.setShowTickLabels(true);
		refreshCycleSlider.setShowTickMarks(true);
		refreshCycleSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				ellsa.multiUIWindow.guiData.nbCycleRefresh3DView = (int)newValue.doubleValue();
			}
		});
		addToolbar(refreshCycleSlider);

		Label defaultPercetValue = new Label("Default Percept Value");
		RunLaterHelper.runLater(()->toolbarPanel.getItems().add(defaultPercetValue));
		TextField defaultPercetValueTextField = new TextField("");
		defaultPercetValueTextField.setOnKeyPressed(event -> {if(event.getCode()== KeyCode.ENTER){
			ellsa.multiUIWindow.guiData.defaultValue3DViewNonSeletedPercept= Double.parseDouble(defaultPercetValueTextField.getText());
			view3D.updateContextChart();}
		});
		addToolbar(defaultPercetValueTextField);


	}
	
	
	
	/**
	 * Get an existing {@link AmakPlot}. 
	 * @param name name of the plot to get
	 * @return an existing plot.
	 * @see EllsaMultiUIWindow#addPlot(String, AmakPlot)
	 */
	public AmakPlot getPlot(String name) {
		return plots.get(name);
	}
	
	/**
	 * Add an {@link AmakPlot} to le map of plots. Allowing for easy access with {@code AmoebaWindow.instance().getPlot(name)}
	 * @param name name of the plot to add
	 * @param plot the plot to add
	 * @see EllsaMultiUIWindow#getPlot(String)
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
