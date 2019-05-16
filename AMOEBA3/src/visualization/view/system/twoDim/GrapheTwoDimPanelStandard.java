package visualization.view.system.twoDim;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.dnd.Autoscroll;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;

import mas.kernel.Config;
import mas.kernel.NCSMemory;
import mas.kernel.Scheduler;
import mas.kernel.World;
import visualization.log.LogMessageType;
import visualization.observation.Observation;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.j2dviewer.Camera;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.jfree.chart.plot.XYPlot;

import visualization.view.system.PanelController;
import visualization.view.system.paving.Panel1DPaving;
import visualization.view.system.projection.PanelProjection;
import mas.agents.Agent;
import mas.agents.percept.Percept;
import mas.agents.SystemAgent;
import mas.agents.context.Context;
import mas.agents.context.ContextOverlap;
import mas.agents.context.ContextVoid;
import mas.agents.context.Experiment;
import mas.agents.context.Range;
import mas.agents.head.Head;
import visualization.graphView.GraphicVisualization2Dim;
import visualization.graphView.GraphicVisualizationNDim;
import visualization.graphView.TemporalGraph;

import org.graphstream.algorithm.Toolkit;


// TODO: Auto-generated Javadoc
/**
 * The Class GrapheTwoDimPanelStandard.
 */
public class GrapheTwoDimPanelStandard extends JPanel implements ViewerListener, MouseInputListener{
	
	/** The graph. */
	Graph graph;
	
	/** The viewer. */
	Viewer viewer;
	private double zoomLevel = 1.0;
	
	/** The world. */
	World world;
	
	/** The pipe. */
	/* ----Interaction with system----*/
	ViewerPipe pipe;
	
	/** The right click. */
	Boolean rightClick = false;

	/** The tool bar. */
	/* ----ToolBar Components----*/
	private JToolBar toolBar;
	
	/** The tool bar slider. */
	private JToolBar toolBarSlider;
	
	/** The tool bar info. */
	private JToolBar toolBarInfo;
	
	/** The button show value. */
	private JButton buttonShowValue;
	
	/** The button show default. */
	private JButton buttonShowDefault;
	
	/** The button show name. */
	private JButton buttonShowName;
	
	/** The button destroy context. */
	private JButton buttonDestroyContext;
	
	/** The button soft style. */
	private JButton buttonSoftStyle;
	
	/** The button standard style. */
	private JButton buttonStandardStyle;
	
	/** The button dark style. */
	private JButton buttonDarkStyle;
	
	/** The button enable auto layout. */
	private JButton buttonEnableAutoLayout;
	
	/** The button disable auto layout. */
	private JButton buttonDisableAutoLayout;
	
	/** The button show ctrl. */
	private JButton buttonShowCtrl;
	
	/** The button show percept tree. */
	private JButton buttonShowPerceptTree;
	
	/** The button create image map. */
	private JButton buttonCreateImageMap;
	
	/** The button oracle. */
	private JButton buttonOracle;
	
	/** The button show projection. */
	private JButton buttonShowProjection;
	
	/** The button show selectable context. */
	private JButton buttonShowSelectableContext;
	
	private JButton buttonSearchContext;
	
	/** The button color according to prediction. */
	private JButton buttonColorAccordingToPrediction;
	
	/** The combo dim X. */
	private JComboBox comboDimX;
	
	/** The combo dim Y. */
	private JComboBox comboDimY;
	
	/** The x value. */
	private JLabel xValue;
	
	/** The y value. */
	private JLabel yValue;

	private JLabel labelSearchContext = new JLabel("Search Context :");
	private JTextField contextID = new JTextField("?");
	
	private JComboBox<Context> Contexts;

	
	/** The mouse event. */
	private MouseEvent mouseEvent;
	
	/** The double format. */
	private NumberFormat doubleFormat;
	
	/** The controller. */
	private Head controller; //TODO
	
	/** The color is dynamic. */
	private boolean colorIsDynamic = true;
	
	/** The slider. */
	/* ----Variables for slider and graph ---- */
	private JSlider slider;
	
	/** The current step. */
	private JTextField currentStep;
	
	/** The textarea. */
	private JTextArea textarea;
	
	/** The position. */
	private Hashtable<Integer, JLabel> position;
	
	/** The obs list. */
	private ArrayList<Observation> obsList = new ArrayList<Observation>();
	
	/** The temporal graph. */
	private TemporalGraph temporalGraph;
	
	/** The remember state. */
	private boolean rememberState = false;
	
	/** The current tick. */
	private int currentTick = 0;
	
	/** The slider value. */
	private int sliderValue = 0;
	
	/** The max slider. */
	private int maxSlider = 100;
	
	/** The current id. */
	private String currentId;
	
	/** The percept name. */
	private List<String> perceptName = new ArrayList<>();	
	
	private Point3 requestPosition;
	
	private ArrayList<Point3> request = new ArrayList<Point3>();	
	
	/**
	 * Instantiates a new graphe two dim panel standard.
	 *
	 * @param world the world
	 */
	public GrapheTwoDimPanelStandard(World world) {
		setLayout(new BorderLayout());
		
		/*Format the double*/
		DecimalFormatSymbols symbol = new DecimalFormatSymbols();
		symbol.setDecimalSeparator('.');
		doubleFormat = new DecimalFormat("#0.00",symbol);     

		this.world  = world;
		this.setMinimumSize(new Dimension(400,400));

		setPerceptName();
		
		toolBar = new JToolBar(null, JToolBar.HORIZONTAL);

//		buttonShowDefault = new JButton(Config.getIcon("tag--plus.png"));
//
//		toolBar.add(buttonShowDefault);
//
//		buttonShowValue = new JButton(Config.getIcon("tag--exclamation.png"));
//
//		toolBar.add(buttonShowValue);
//
//		buttonShowName = new JButton(Config.getIcon("tag.png"));
//
//		toolBar.add(buttonShowName);
//		
//		toolBar.addSeparator();
		
		buttonSoftStyle = new JButton(Config.getIcon("flag-white.png"));
		buttonSoftStyle.addActionListener(e -> {setSoftStyle();});
		buttonSoftStyle.setToolTipText("Switch to soft style.");
		toolBar.add(buttonSoftStyle);
		
		buttonStandardStyle = new JButton(Config.getIcon("flag-green.png"));
		buttonStandardStyle.addActionListener(e -> {setStandardStyle();});
		buttonStandardStyle.setToolTipText("Switch to standard style.");
		toolBar.add(buttonStandardStyle);
		
		buttonDarkStyle = new JButton(Config.getIcon("flag-black.png"));
		buttonDarkStyle.addActionListener(e -> {setDarkStyle();});
		buttonDarkStyle.setToolTipText("Switch to dark style.");
		toolBar.add(buttonDarkStyle);
		
		buttonColorAccordingToPrediction = new JButton(Config.getIcon("color.png"));
		buttonColorAccordingToPrediction.addActionListener(e -> {colorIsDynamic = !colorIsDynamic;});
		buttonColorAccordingToPrediction.setToolTipText("Set color according to prediction from context agent (doesn't work with regression)");
		toolBar.add(buttonColorAccordingToPrediction);
		
		toolBar.addSeparator();
		buttonDestroyContext = new JButton(Config.getIcon("eraser.png"));
		buttonDestroyContext.addActionListener(e -> {destroyContext();});
		toolBar.add(buttonDestroyContext);
		
		buttonEnableAutoLayout = new JButton(Config.getIcon("node-select-all.png"));
		buttonEnableAutoLayout.addActionListener(e -> {enableAutoLayout();});
		buttonEnableAutoLayout.setToolTipText("Enable auto layout.");
		toolBar.add(buttonEnableAutoLayout);
		
		buttonDisableAutoLayout = new JButton(Config.getIcon("node.png"));
		buttonDisableAutoLayout.addActionListener(e -> {disableAutoLayout();});
		buttonDisableAutoLayout.setToolTipText("Disable auto layout.");
		toolBar.add(buttonDisableAutoLayout);
		
		buttonShowCtrl = new JButton(Config.getIcon("bug.png"));
		buttonShowCtrl.addActionListener(e -> {startPanelController();});
		buttonShowCtrl.setToolTipText("Show controller informations");
		toolBar.add(buttonShowCtrl);
		
	/*	buttonShowPerceptTree = new JButton(Config.getIcon("tree.png"));
		buttonShowPerceptTree.addActionListener(e -> {showPerceptTree();});
		buttonShowPerceptTree.setToolTipText("Show first percept tree");
		toolBar.add(buttonShowPerceptTree);*/
		
		buttonOracle = new JButton(Config.getIcon("compass.png"));
		buttonOracle.addActionListener(e -> {changeOracleConnection();});
		buttonOracle.setToolTipText("Disconnect or connect the oracle");
		toolBar.add(buttonOracle);

		toolBar.addSeparator();

		buttonCreateImageMap = new JButton(Config.getIcon("picture.png"));
		buttonCreateImageMap.addActionListener(e -> {world.exportAsPicture(100,-1,1,(((Percept) world.getAgents().get(comboDimX.getSelectedItem()))  )  
			,100,-1,1,(((Percept) world.getAgents().get(comboDimY.getSelectedItem()))  ), 0, 2  );});
		buttonCreateImageMap.setToolTipText("Export an image of the prediction of the AMAS");
		toolBar.add(buttonCreateImageMap);
		
		buttonShowProjection = new JButton(Config.getIcon("target.png"));
		buttonShowProjection.addActionListener(e -> {showProjection();});
		buttonShowProjection.setToolTipText("Show a window of the 2D projection");
		toolBar.add(buttonShowProjection);
		
		buttonShowSelectableContext = new JButton(Config.getIcon("fruit.png"));
		buttonShowSelectableContext.addActionListener(e -> {printSelectableContext();});
		buttonShowSelectableContext.setToolTipText("Print in console the selectable contexts");
		toolBar.add(buttonShowSelectableContext);
		
		comboDimX = new JComboBox();
		comboDimY = new JComboBox();
		ArrayList<Percept> var = (ArrayList<Percept>) world
				.getAllAgentInstanceOf(Percept.class);
		for (Percept v : var) {
			comboDimX.addItem(v.getName());
			comboDimY.addItem(v.getName());
		}
		
		if(var.size()>1) {
			comboDimY.setSelectedIndex(1);
		}
		toolBar.add(comboDimX);
		toolBar.add(comboDimY);
		
		xValue = new JLabel();
		yValue = new JLabel();
		
		Contexts = new JComboBox<Context>();
		Contexts.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				
				
				
				recolorContexts();
				
				
				if(Contexts.getSelectedItem()!=null) {
					
					Node node = graph.getNode(((Context) Contexts.getSelectedItem()).getName());
					node.addAttribute("ui.style", "fill-color: rgba(0,255,0,150);");
				}

				
				
				
			}
		});
		
		
		
		toolBar.add(Contexts);
		
		
		toolBar.add(labelSearchContext);
		contextID.setSize(new Dimension(50,30));
		toolBar.add(contextID);
		buttonSearchContext = new JButton(Config.getIcon("magnifier.png"));
		buttonSearchContext.addActionListener(e -> {highlightContexts(world.getScheduler().getContextByName(contextID.getText()));;});
		buttonSearchContext.setToolTipText("Highlight a context");
		toolBar.add(buttonSearchContext);
		
		toolBar.add(xValue);
		toolBar.addSeparator();
		toolBar.add(yValue);
		
		
		
		
		this.add(toolBar,BorderLayout.NORTH);
		
		/* Add JSlider to show the graph of previous states */
		toolBarSlider = new JToolBar(null, JToolBar.HORIZONTAL);
		slider = new JSlider(JSlider.HORIZONTAL);
		slider.setMinorTickSpacing(1);
		slider.setMajorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setMaximum(maxSlider);
		
		// Add positions label in the slider
		position = new Hashtable<Integer, JLabel>();
		position.put(sliderValue, new JLabel(String.valueOf(sliderValue)));
		slider.setValue(sliderValue);
		slider.setLabelTable(position);
		
		// Add change listener to the slider
		slider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				int valueTick = ((JSlider)e.getSource()).getValue();
				
				if (valueTick != sliderValue) {
					if (valueTick < currentTick) {
						sliderValue = valueTick;
						updateSliderValue();
						updateGrapgh(sliderValue);
					} else {
						if (valueTick > currentTick && sliderValue == currentTick) {
							updateSliderValue();
						} else {
							sliderValue = currentTick;
							updateSliderValue();
							updateGrapgh(sliderValue);
						}
					}
					
				}
				
			}
		});
		
		JButton btnPrev = new JButton(Config.getIcon("control-180.png"));
		btnPrev.addActionListener(e -> { previousObservation(); });
		btnPrev.setToolTipText("Previous");
		
		JButton btnNext = new JButton(Config.getIcon("control.png"));
		btnNext.addActionListener(e -> { nextObservation(); });
		btnNext.setToolTipText("Next");
		
		/* Add text field to show the current step */
		currentStep = new JTextField(4);
		Dimension d = currentStep.getPreferredSize();
		d.width = 50;
		currentStep.setMinimumSize(d);
		currentStep.setMaximumSize(d);
		currentStep.setHorizontalAlignment(JTextField.CENTER);
		currentStep.setText(String.valueOf(sliderValue));
		
		currentStep.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                currentStepKeyPressed(evt);
            }
        });

		
		JButton btnGo = new JButton("Go");
		btnGo.addActionListener(e -> { getTextValue(); }); 
		
		toolBarSlider.add(slider);
		toolBarSlider.add(btnPrev);
		toolBarSlider.add(btnNext);
		toolBarSlider.add(currentStep);
		toolBarSlider.add(btnGo);
		this.add(toolBarSlider, BorderLayout.SOUTH);
		
		/* End of Slider */
		
		/* Add text area to display the info of context selected */
		
		toolBarInfo = new JToolBar(null, JToolBar.VERTICAL);
		textarea = new JTextArea();
		textarea.setEditable(false);
		JScrollPane pane = new JScrollPane(textarea);
		Dimension dInfo = pane.getPreferredSize();
		dInfo.width = 250;
		dInfo.height = 400;
		pane.setPreferredSize(dInfo);
		toolBarInfo.add(pane);
		this.add(toolBarInfo, BorderLayout.EAST);
		textarea.setText("No context");
		toolBarInfo.setVisible(false);
		
		
		
		
		/* End of text area */
		
		createGraph();
		

		
		
		
		
	}
	
	/**
	 * Sets the percept name.
	 */
	/* Informations for Header of cs file - name of each percept */
	private void setPerceptName() {
		for (int i = 0; i < world.getAllPercept().size(); i++) {
			perceptName.add(world.getAllPercept().get(i).getName());
		}
	}

	/**
	 * Sets the observation list.
	 *
	 * @param oList the new observation list
	 */
	/* Set list of observation as global variable */
	public void setObservationList(ArrayList<Observation> oList){
		for(Observation observation: oList) {
			obsList.add(observation);
		}
	}

	/**
	 * Gets the observation list.
	 *
	 * @return the observation list
	 */
	/* Return list of observation as global variable */
	public ArrayList<Observation> getObservationList() {
		return obsList;
	}	
		
	/**
	 * Sets the remember state.
	 *
	 * @param rememberState the new remember state
	 */
	/* Set the value of boolean rememberState */
	public void setRememberState(boolean rememberState) {
		this.rememberState = rememberState;
		if (!rememberState) {
			this.remove(toolBarSlider);
		}
	}
	
	/**
	 * Sets the temporal graph.
	 *
	 * @param temporalGraph the new temporal graph
	 */
	/* Initialize the variable temporalGraph */
	public void setTemporalGraph(TemporalGraph temporalGraph) {
		this.temporalGraph = temporalGraph;
	}
	
	/**
	 * Update slider value.
	 */
	/* Update slider and button value */
	private void updateSliderValue() {
		slider.setValue(sliderValue);
		position.clear();
		position.put(sliderValue, new JLabel(String.valueOf(sliderValue)));
		slider.setLabelTable(position);
		// Update value in text field
		currentStep.setText(String.valueOf(sliderValue));
	}
	
	/**
	 * Gets the observation by tick.
	 *
	 * @param tick the tick
	 * @return the observation by tick
	 */
	/* To get the observation by the value of tick */
	private Observation getObservationByTick(int tick) {
		for(Observation observation: obsList) {
			if(observation.getTick() == tick) {
				return observation;
			}
		}
		return null;
	}
	
	/**
	 * Update grapgh.
	 *
	 * @param tick the tick
	 */
	/* Update graph */
	private void updateGrapgh (int tick) {
		// Update Information in the text area
		if (currentId != null) {
			setContextTextAreaInfo(currentId);
		}
		
		// Get the information from observation to update graph
		Observation selectedObs = getObservationByTick(tick);
		if (selectedObs != null) {
			createGraphPreviousState(selectedObs);
		}
	}
	
	/**
	 * Next observation.
	 */
	/* Action for the next button of observation */
	private void nextObservation() {
		if (sliderValue < currentTick) {
			sliderValue++;
			updateSliderValue();
			updateGrapgh(sliderValue);
		}	
	}
	
	/**
	 * Previous observation.
	 */
	/* Action for the previous button of observation */
	private void previousObservation() {
		if (sliderValue != 0) {
			sliderValue--;
			updateSliderValue();
			updateGrapgh(sliderValue);
		}	
	}
	
	/**
	 * Test max slider.
	 *
	 * @param value the value
	 */
	/* Test and increase Maximum number of slider if necessary */
	private void testMaxSlider(int value) {
		// To change the max number of slider (by increasing 100 units)
		if (value >= maxSlider) {
			maxSlider = (value/100 + 1) * 100;
			slider.setMaximum(maxSlider);
			sliderValue++;
			updateSliderValue();
		}
	}
	
	/**
	 * Current step key pressed.
	 *
	 * @param evt the evt
	 */
	/* Get text value of the slider input box when pressing Enter */	
	private void currentStepKeyPressed(java.awt.event.KeyEvent evt) { 
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			getTextValue();
		}	
	}
	
	/**
	 * Gets the text value.
	 *
	 * @return the text value
	 */
	/* Get text value from the text field */
	private void getTextValue() {
		String text = currentStep.getText();
		try {
			int num = Integer.parseInt(text);
			if (num <= currentTick) {
				if (num != sliderValue) {
					sliderValue = num;
					updateSliderValue();
					updateGrapgh(sliderValue);
				}
				
			} else {
				if (sliderValue == currentTick) {
					currentStep.setText(String.valueOf(sliderValue));
				} else {
					sliderValue = currentTick;
					updateSliderValue();
					updateGrapgh(sliderValue);
				}
				
			}
				
		} catch (NumberFormatException e) {
			System.out.println("Please input a positive number.");
			currentStep.setText("NaN");
		}
	}
	
	/**
	 * Prints the selectable context.
	 */
	private void printSelectableContext() {
		System.out.println(world.getSelectableContext());
		
	}

	/**
	 * Change oracle connection.
	 */
	private void changeOracleConnection() {
		world.changeOracleConnection();
		
	}

	/**
	 * Show projection.
	 */
	private void showProjection() {
		PanelProjection pan = new PanelProjection(world,500,-1,1,(((Percept) world.getAgents().get(comboDimX.getSelectedItem()))  )  
				,500,-1,1,(((Percept) world.getAgents().get(comboDimY.getSelectedItem()))  ), 0, 2  );
		JFrame frame = new JFrame(">>Projection<<");
		world.getScheduler().addScheduledItem(pan);
        frame.setAlwaysOnTop(true);
		frame.setContentPane(pan);
		frame.setVisible(true);
		frame.pack();
		frame.setSize(600, 600);
	}
	
/*	private void showPerceptTree() {
		Percept p = (((Percept) world.getAgents().get(comboDimX.getSelectedItem()))  );
		ArrayList<Range> l = new ArrayList<Range>();
		p.getTree().search(p.getTree().top, ( (((Percept) world.getAgents().get(comboDimX.getSelectedItem())).getValue()  )  ), l);;
		System.out.println(l);
 		JFrame frame = new JFrame();
		frame.setContentPane(new TreeViewer(p.getTree()));
		frame.pack();
		frame.setVisible(true);
	}*/
	
	/**
 * Start panel controller.
 */
private void startPanelController() {
		
		PanelController pan = new PanelController(controller, world);
		JFrame frame = new JFrame(">>Controller<<");
		JScrollPane scrollPane = new JScrollPane(pan,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		world.getScheduler().addScheduledItem(pan);
        frame.setAlwaysOnTop(true);
		frame.setContentPane(scrollPane);
		frame.setVisible(true);
		frame.pack();
	}


	/**
	 * Enable auto layout.
	 */
	public void enableAutoLayout() {
		viewer.enableAutoLayout();
	}
	
	/**
	 * Disable auto layout.
	 */
	public void disableAutoLayout() {
		viewer.disableAutoLayout();
	}
	
	/**
	 * Show controller.
	 */
	public void showController() {
		viewer.disableAutoLayout();
	}
	
	/**
	 * Sets the standard style.
	 */
	public void setStandardStyle() {
		graph.removeAttribute("ui.stylesheet");
		//graph.addAttribute("ui.stylesheet", "url('file:"+System.getProperty("user.dir")+"/bin/styles/styleSystemScalable.css')");
		graph.addAttribute("ui.stylesheet", "url('" + this.getClass().getClassLoader().getResource("VISUALIZATION/styles/styleSystemScalable.css") + "')");
	}
	
	/**
	 * Sets the dark style.
	 */
	public void setDarkStyle() {
		graph.removeAttribute("ui.stylesheet");
		//graph.addAttribute("ui.stylesheet", "url('file:"+System.getProperty("user.dir")+"/bin/styles/styleSystemDark.css')");
		graph.addAttribute("ui.stylesheet", "url('" + this.getClass().getClassLoader().getResource("VISUALIZATION/styles/styleSystemDark.css") + "')");
	}
	
	/**
	 * Sets the soft style.
	 */
	public void setSoftStyle() {
		graph.removeAttribute("ui.stylesheet");
		//graph.addAttribute("ui.stylesheet", "url('file:"+System.getProperty("user.dir")+"/bin/styles/styleSystemSoft.css')");
		graph.addAttribute("ui.stylesheet", "url('" + this.getClass().getClassLoader().getResource("VISUALIZATION/styles/styleSystemSoft.css") + "')");
	}

	
	/**
	 * Destroy context.
	 */
	public void destroyContext() {
		world.destroy(Context.class);  //TODO
	}
	

	

	

	
	/**
	 * Sets the world.
	 *
	 * @param world the new world
	 */
	public void setWorld(World world) {
		this.world = world;
		createGraph();
	}
	
	/**
	 * Creates the graph previous state.
	 *
	 * @param myObs the my obs
	 */
	public void createGraphPreviousState(Observation myObs) {
		
		/* Set new graph from observation */
		for(Percept p: myObs.getPerceptList()) {
			if( p.getName().equals( world.getAgents().get(comboDimX.getSelectedItem()).getName() ) ) {
				xValue.setText(String.valueOf(p.getValue()));
			}
			if( p.getName().equals( world.getAgents().get(comboDimY.getSelectedItem()).getName() ) ) {
				yValue.setText(String.valueOf(p.getValue()));
			}
		}
		graph.clear();
		setStandardStyle();
		

		
		
		
		for(Context obsContext: myObs.getContextList()) {
			Node node;
			String name = obsContext.getName();
			
			if (graph.getNode(name) != null) {
				node = graph.getNode(name);
			} else {
				graph.addNode(name);
				node = graph.getNode(name);
				node.addAttribute("ui.class",
						obsContext.getClass().getSimpleName());
				node.addAttribute("ui.label", obsContext.getName());
			}
			node = graph.getNode(name);
			node.addAttribute("ui.class", obsContext.getClass().getSimpleName());
			node.addAttribute("ui.label", name);
			node.addAttribute("EXIST", true);
			if(obsContext.getRanges().size() > 0) {
				String key1 = world.getAgents().get(comboDimX.getSelectedItem()).getName();
				String key2 = world.getAgents().get(comboDimY.getSelectedItem()).getName();
				double key1End = 0.0, key1Start = 0.0, key2End = 0.0, key2Start = 0.0;
				for(Entry<Percept, Range> entry : obsContext.getRanges().entrySet()) {
					if(entry.getKey().getName().equals(key1)) {
						key1End = entry.getValue().getEnd();
						key1Start = entry.getValue().getStart();
					}
					if(entry.getKey().getName().equals(key2)) {
						key2End = entry.getValue().getEnd();
						key2Start = entry.getValue().getStart();
					}
				}
				//System.out.println("key" + key1End + "," + key1Start + "," + key2End + "," + key2Start);
				
				double lengthX = key1End - key1Start;
				double lengthY = key2End - key2Start;
				node.setAttribute("xyz", key1Start + (0.5*lengthX), key2Start + (0.5*lengthY), 0);
				node.addAttribute("ui.style", "size: " + doubleFormat.format(lengthX) + "gu, " + doubleFormat.format(lengthY) +"gu;");

			}
			if (obsContext.isBestContext()) {
				node.addAttribute("ui.class","BestContextSelected");				
			} 
			else if (obsContext.getNSelection() > 0) {
				node.addAttribute("ui.class","ContextAwaked");
				if (obsContext.getNSelection() == 3) {
					node.addAttribute("ui.class","ContextSelected");				
				}
			}
			else {
				node.addAttribute("ui.class","Context");
			}
			obsContext.setnSelection(0);
		}
		
		for (Node node : graph) {
			if (node.hasAttribute("EXIST")) {
				node.removeAttribute("EXIST");
			} else {
				graph.removeNode(node);
			}
		}
		
		if (colorIsDynamic) {
			
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			
			for(Context obsContext: myObs.getContextList()) {
				//double val = obsContext.getActionProposal();
				double val = obsContext.getValueActionProposition();
				if (val < min) {
					min = val;
				} 
				if (val > max) {
					max = val;
				}
			}
			
			for(Context obsContext: myObs.getContextList()) {
				
				Node node = graph.getNode(obsContext.getName());
				node.addAttribute("ui.class","ContextColorDynamic");
				node.setAttribute("ui.color", (obsContext.getValueActionProposition() - min) / (max - min) ); 
				
			}

		}
		
	}
	
	/**
	 * Sets the visualization.
	 *
	 * @param obsList the new visualization
	 */
	// Set the visualization after serialization
	public void setVisualization(ArrayList<Observation> obsList) {
		
		setObservationList(obsList);
		
		if (getObservationList().size() > 0) {
			setRememberState(true);
			currentTick = world.getScheduler().getTick() - 1;
			maxSlider = (currentTick/100 + 1) * 100;
			slider.setMaximum(maxSlider);
			position = new Hashtable<Integer, JLabel>();
			position.put(0, new JLabel("0"));
			slider.setLabelTable(position);
			sliderValue = currentTick;
		} else {
			setRememberState(false);
		}
		
	}
	
	/**
	 * Gets the percept min max values 2 dim.
	 *
	 * @return the percept min max values 2 dim
	 */
	/* Get the min-max value of each percept and update graph visualization 2-Dim */
	private void getPerceptMinMaxValues2Dim() {
		ArrayList<Percept> perceps = world.getAllPercept();

		for (int i=0; i<temporalGraph.get2DimGraphList().size(); i++) {
			String contextID = temporalGraph.get2DimGraphList().get(i).getContextID();
			if (world.getAgents().get(contextID) != null) {
				Context c = (Context) world.getAgents().get(contextID);
				for (int j=0; j<perceps.size(); j++) {
					double min = c.getRanges().get(perceps.get(j)).getEnd();
					double max = c.getRanges().get(perceps.get(j)).getStart();
					temporalGraph.get2DimGraphList().get(i).updateData(j*2, currentTick, min);
					temporalGraph.get2DimGraphList().get(i).updateData((j*2)+1, currentTick, max);
				}
			} 
		}	
		
	}
	
	/**
	 * Gets the percept min max values N dim.
	 *
	 * @return the percept min max values N dim
	 */
	/* Get the min-max value of each percept and update graph visualization N-Dim */
	private void getPerceptMinMaxValuesNDim() {
		ArrayList<Percept> perceptList = world.getAllPercept();
		
		for (int i=0; i<temporalGraph.getNDimGraphList().size(); i++) {
			String contextID = temporalGraph.getNDimGraphList().get(i).getContextID();
			if (world.getAgents().get(contextID) != null) {
				Context c = (Context) world.getAgents().get(contextID);
				for (int j=0; j<perceptList.size(); j++) {
					double min = c.getRanges().get(perceptList.get(j)).getEnd();
					double max = c.getRanges().get(perceptList.get(j)).getStart();
					
					temporalGraph.getNDimGraphList().get(i).updateDataset("Min", min, j);
					temporalGraph.getNDimGraphList().get(i).updateDataset("Max", max, j);
				}
				temporalGraph.getNDimGraphList().get(i).repaint();
			} 
		}	
		
	}
	
	/**
	 * Update slider info.
	 */
	private void updateSliderInfo() {
		/* Update value of slider */
		currentTick = world.getScheduler().getTick();
		if (rememberState) {
			sliderValue = currentTick;
			testMaxSlider(sliderValue);
			updateSliderValue();
		}
		
		// Update info in the text area
		String info = "";
		if (world.getAgents().get(currentId) != null) {
			info = "State: " + currentTick + "\n";
			info = info.concat(world.getAgents().get(currentId).toStringFull());
			info = info.replace("Current", "\nCurrent");
			info = info.replace("AVT", "\nAVT");
			
		} else {
			info = "No context";
		}
		
		textarea.setText(info);
		/* End of update slider */
	}

	private void setOrigin() {

		
		Node originNode1;
		
		graph.addNode("origin1");
		originNode1 = graph.getNode("origin1");
		originNode1.addAttribute("EXIST", true);
		originNode1.setAttribute("xyz", 0, 0, 0);
		originNode1.addAttribute("ui.style", "size: " + doubleFormat.format(0.2) + "gu, " + doubleFormat.format(4) +"gu;");
		originNode1.addAttribute("ui.class","RGBAColor");
		
		originNode1.addAttribute("ui.style", "fill-color: rgba(0,0,0,255);");
		
		Node originNode2;
		
		graph.addNode("origin2");
		originNode2 = graph.getNode("origin2");
		originNode2.addAttribute("EXIST", true);
		originNode2.setAttribute("xyz", 0, 0, 0);
		originNode2.addAttribute("ui.style", "size: " + doubleFormat.format(4) + "gu, " + doubleFormat.format(0.2) +"gu;");
		originNode2.addAttribute("ui.class","RGBAColor");
		
		originNode2.addAttribute("ui.style", "fill-color: rgba(0,0,0,255);");
	}
	
	private void setMinMax() {

		
		Node minMax;
		
		graph.addNode("minMax");
		
		
		
		minMax = graph.getNode("minMax");
		
		minMax.addAttribute("EXIST", true);
		
		Percept pAffichageX = (Percept)world.getAgents().get(comboDimX.getSelectedItem());
		Percept pAffichageY = (Percept)world.getAgents().get(comboDimY.getSelectedItem());

		double lengthX = (world.getScheduler().getTick()>0 ) ? pAffichageX.getMax() - pAffichageX.getMin() : 1;
		double lengthY = (world.getScheduler().getTick()>0 ) ? pAffichageY.getMax() - pAffichageY.getMin() : 1;
		
		double xPos = (world.getScheduler().getTick()>0 ) ? (pAffichageX.getMax() + pAffichageX.getMin())/2 : 0;
		double yPos = (world.getScheduler().getTick()>0 ) ? (pAffichageY.getMax() + pAffichageY.getMin())/2 : 0;
		
		
		
		minMax.setAttribute("xyz", xPos, yPos, 0);
		
		minMax.addAttribute("ui.style", "size: " + doubleFormat.format(lengthX) + "gu, " + doubleFormat.format(lengthY) +"gu;");
		
		minMax.addAttribute("ui.class","RGBAColor");
		minMax.addAttribute("ui.style", "fill-color: rgba(255,255,255,0);");
		
		
		
		
	}
	
	private void setHiddenModel() {
		Node hiddenModel;
		
		graph.addNode("hiddenModel");
		
		hiddenModel = graph.getNode("hiddenModel");
		
		hiddenModel.addAttribute("EXIST", true);
		

		double lengthX = (world.getScheduler().getTick()>0 ) ? world.getAmoeba().getManager().getSpaceSize()*2 : 1;
		double lengthY = (world.getScheduler().getTick()>0 ) ? world.getAmoeba().getManager().getSpaceSize()*2 : 1;
		
		double xPos =  0;
		double yPos =  0;
		
		
		
		hiddenModel.setAttribute("xyz", xPos, yPos, 0);
		
		hiddenModel.addAttribute("ui.style", "size: " + doubleFormat.format(lengthX) + "gu, " + doubleFormat.format(lengthY) +"gu;");
		
		hiddenModel.addAttribute("ui.class","RGBAColor");
		hiddenModel.addAttribute("ui.style", "fill-color: rgba(255,255,255,0);");
	}
	
	
	private void updateOrigin() {
		
		Node originNode1;
		if(graph.getNode("origin1") == null) {
			graph.addNode("origin1");
		}
		originNode1 = graph.getNode("origin1");
		originNode1.addAttribute("EXIST", true);
		originNode1.setAttribute("xyz", 0, 0, 0);
		originNode1.addAttribute("ui.style", "size: " + doubleFormat.format(0.5) + "gu, " + doubleFormat.format(2) +"gu;");
		originNode1.addAttribute("ui.class","RGBAColor");
		
		originNode1.addAttribute("ui.style", "fill-color: rgba(0,0,0,255);");
		
		Node originNode2;
		if(graph.getNode("origin2") == null) {
			graph.addNode("origin2");
		}
		originNode2 = graph.getNode("origin2");
		originNode2.addAttribute("EXIST", true);
		originNode2.setAttribute("xyz", 0, 0, 0);
		originNode2.addAttribute("ui.style", "size: " + doubleFormat.format(2) + "gu, " + doubleFormat.format(0.5) +"gu;");
		originNode2.addAttribute("ui.class","RGBAColor");
		
		originNode2.addAttribute("ui.style", "fill-color: rgba(0,0,0,255);");
	}
	
	private void updateMinMax() {
		Node minMax;
		
		if(graph.getNode("minMax") == null) {
			graph.addNode("minMax");
		}
		minMax = graph.getNode("minMax");
		
		minMax.addAttribute("EXIST", true);
		
		Percept pAffichageX = (Percept)world.getAgents().get(comboDimX.getSelectedItem());
		Percept pAffichageY = (Percept)world.getAgents().get(comboDimY.getSelectedItem());

		double lengthX = (world.getScheduler().getTick()>0 ) ? pAffichageX.getMax() - pAffichageX.getMin() : 1;
		double lengthY = (world.getScheduler().getTick()>0 ) ? pAffichageY.getMax() - pAffichageY.getMin() : 1;
		
		double xPos = (world.getScheduler().getTick()>0 ) ? (pAffichageX.getMax() + pAffichageX.getMin())/2 : 0;
		double yPos = (world.getScheduler().getTick()>0 ) ? (pAffichageY.getMax() + pAffichageY.getMin())/2 : 0;
		
		
		
		minMax.setAttribute("xyz", xPos, yPos, 0);
		
		minMax.addAttribute("ui.style", "size: " + doubleFormat.format(lengthX) + "gu, " + doubleFormat.format(lengthY) +"gu;");
		
		minMax.addAttribute("ui.class","RGBAColor");
		minMax.addAttribute("ui.style", "fill-color: rgba(255,255,255,0);");
	}
	
	private void updateHiddenModel() {
		
		Node hiddenModel;
		
		if(graph.getNode("hiddenModel") == null) {
			graph.addNode("hiddenModel");
		}
		
		hiddenModel = graph.getNode("hiddenModel");
		
		hiddenModel.addAttribute("EXIST", true);
		

		double lengthX2 = (world.getScheduler().getTick()>0 ) ? world.getAmoeba().getManager().getSpaceSize()*2 : 1;
		double lengthY2 = (world.getScheduler().getTick()>0 ) ? world.getAmoeba().getManager().getSpaceSize()*2 : 1;
		
		double xPos2 =  0;
		double yPos2 =  0;
		
		
		
		hiddenModel.setAttribute("xyz", xPos2, yPos2, 0);
		
		hiddenModel.addAttribute("ui.style", "size: " + doubleFormat.format(lengthX2) + "gu, " + doubleFormat.format(lengthY2) +"gu;");
		
		hiddenModel.addAttribute("ui.class","RGBAColor");
		hiddenModel.addAttribute("ui.style", "fill-color: rgba(0,255,0,25);");
		
	}
	
	private void updateHead() {
		
		Head head = world.getScheduler().getHeadAgent();
		String name = head.getName();
		
		controller = head; //TODO dirty
		
		Node node;
		if (graph.getNode(name) != null) {
			node = graph.getNode(name);
			node.addAttribute("ui.label", ((Percept)(world.getAgents().get(comboDimX.getSelectedItem()))).getValue() + " , " + ((Percept)(world.getAgents().get(comboDimY.getSelectedItem()))).getValue());

		} else {
			graph.addNode(name);
			node = graph.getNode(name);
			node.addAttribute("ui.class", "Center");
		}

		node.addAttribute("EXIST", true);
		node.setAttribute("xyz", ((Percept)(world.getAgents().get(comboDimX.getSelectedItem()))).getValue(), ((Percept)(world.getAgents().get(comboDimY.getSelectedItem()))).getValue(), 0);
		node.addAttribute("ui.style", "size: " + doubleFormat.format(1) + "gu, " + doubleFormat.format(10) +"gu;");
		node.addAttribute("ui.class","RGBAColor");
		
		node.addAttribute("ui.style", "fill-color: rgba(0,255,0,255);");
		
		Node node2;
		if (graph.getNode(name+"2") != null) {
			node2 = graph.getNode(name+"2");
			node2.addAttribute("ui.label", ((Percept)(world.getAgents().get(comboDimX.getSelectedItem()))).getValue() + " , " + ((Percept)(world.getAgents().get(comboDimY.getSelectedItem()))).getValue());

		} else {
			graph.addNode(name+"2");
			node2 = graph.getNode(name+"2");
			node2.addAttribute("ui.class", "Center");
		}
		
		node2.addAttribute("EXIST", true);
		node2.setAttribute("xyz", ((Percept)(world.getAgents().get(comboDimX.getSelectedItem()))).getValue(), ((Percept)(world.getAgents().get(comboDimY.getSelectedItem()))).getValue(), 0);
		node2.addAttribute("ui.style", "size: " + doubleFormat.format(10) + "gu, " + doubleFormat.format(1) +"gu;");
		node2.addAttribute("ui.class","RGBAColor");
		
		node2.addAttribute("ui.style", "fill-color: rgba(0,255,0,255);");
		
		Node node3;
		if (graph.getNode(name+"3") != null) {
			node3 = graph.getNode(name+"3");
			node3.addAttribute("ui.label", ((Percept)(world.getAgents().get(comboDimX.getSelectedItem()))).getValue() + " , " + ((Percept)(world.getAgents().get(comboDimY.getSelectedItem()))).getValue());

		} else {
			graph.addNode(name+"3");
			node3 = graph.getNode(name+"3");
			node3.addAttribute("ui.class", "Center");
		}
		
		node3.addAttribute("EXIST", true);
		node3.setAttribute("xyz", ((Percept)(world.getAgents().get(comboDimX.getSelectedItem()))).getValue(), ((Percept)(world.getAgents().get(comboDimY.getSelectedItem()))).getValue(), 0);
		double XLength = 2*world.getContextCreationNeighborhood(null, (Percept)(world.getAgents().get(comboDimX.getSelectedItem())));
		double YLength = 2*world.getContextCreationNeighborhood(null, (Percept)(world.getAgents().get(comboDimY.getSelectedItem())));
		node3.addAttribute("ui.style", "size: " + doubleFormat.format(XLength) + "gu, " + doubleFormat.format(YLength) +"gu;");
		node3.addAttribute("ui.class","RGBAColor");
		
		node3.addAttribute("ui.style", "fill-color: rgba(255,255,255,0);");
		
	}
	
	private void updateContexts() {
		
		
		for(Context context : world.getScheduler().getContextsAsContext()) {
			
			String name = context.getName();
			
			// Store values into array list of context of observation
//			if (rememberState) {
//				obsEle.addContextList(new Context(context));
//			}	
			
			Node node;
			if (graph.getNode(name) != null) {
				node = graph.getNode(name);
			} else {
				graph.addNode(name);
				node = graph.getNode(name);
				
//				node.addAttribute("ui.class", agent.getClass().getSimpleName());
//				node.addAttribute("ui.label", agent.getName());
			}

			node.addAttribute("EXIST", true);
			if (context.getRanges().size() > 0){
				drawRectangle(node, context);
			}
			node.addAttribute("ui.class","Context");
			
//			if (context.isBestContext()) {
//				node.addAttribute("ui.class","BestContextSelected");				
//			} else if (context.getNSelection() > 0) {
//				node.addAttribute("ui.class","ContextAwaked");
//				if (context.getNSelection() == 3) {
//					node.addAttribute("ui.class","ContextSelected");				
//				}
//			}
//
//			else {
//				node.addAttribute("ui.class","Context");
//			}

			context.setnSelection(0);
			
		}
		
	}
	
	
	/**
	 * Update.
	 */
	public void update () {
		
		// Update information of slider
		updateSliderInfo();
		
		// Update values of graphic visualization
		getPerceptMinMaxValues2Dim();
		getPerceptMinMaxValuesNDim();
		
		
		
		//TODO
		xValue.setText(   String.valueOf( ( (((Percept) world.getAgents().get(comboDimX.getSelectedItem())).getValue()  )  )));
		yValue.setText(   String.valueOf( ( (((Percept) world.getAgents().get(comboDimY.getSelectedItem())).getValue()  )  )));

		/* Create and store the element tick of observation */
//		Observation obsEle = new Observation();
//		// Set tick for the element of observation
//		if (rememberState) {
//			obsEle.setTick(world.getScheduler().getTick());
//		}
//		
//		// Store values into array list of percept of observation
//		ArrayList<Percept> perceptList = world.getAllPercept();
//		
//		for(int i=0; i< perceptList.size(); i++) {
//			if (rememberState) {
//				Percept p = new Percept(perceptList.get(i));
//				obsEle.addPerceptList(p);
//			}	
//		}
		
		for(Node n : graph.getNodeSet()) {
			
			String name = n.getId();
			String delimsTag = "_";
			String[] tokens = name.split(delimsTag);
			if(tokens[0].equals("Exp")) {
				graph.removeNode(name);
			}
			
		}
		
		
		updateOrigin();
		//updateMinMax();
		//updateHiddenModel();
		updateHead();
		updateContexts();
		recolorContexts();
		
		
		
		
		
		
		
	
		

		
		
//		for (String name : world.getAgents().keySet()) {
//			SystemAgent agent = world.getAgents().get(name);
//			
//			
//			if (agent instanceof Head) {
//				
//			}
//			
//		}
		
//		if (rememberState) {
//			obsList.add(obsEle);
//		}


		
//		for (Node node : graph) {
//			if (node.hasAttribute("EXIST")) {
//				node.removeAttribute("EXIST");
//			} else {
//				graph.removeNode(node);
//			}
//		}

//		if (colorIsDynamic) {
//			
//			double min = Double.POSITIVE_INFINITY;
//			double max = Double.NEGATIVE_INFINITY;
//			
//			for (String name : world.getAgents().keySet()) {
//				SystemAgent a = world.getAgents().get(name);
//				if (a instanceof Context) {
//					double val = ((Context) a).getActionProposal();
//					//double val = ((Context) a).getFunction().getFormula((Context) a);
//					if (val < min) {
//						min = val;
//					}
//					if (val > max) {
//						max = val;
//					}
//				}
//
//			}		
//
//
//			recolorContexts();
//			
//			
//			
//			for(Context context : world.getScheduler().getContextsAsContext()) {
//				for(ContextOverlap contextOverlap : context.contextOverlaps) {
//					world.getScheduler().getView().getTabbedPanel().getPanelTwoDimStandard().drawOverlap(contextOverlap);
//				}
//				
//				for(ContextVoid contextVoid : context.contextVoids) {
//					world.getScheduler().getView().getTabbedPanel().getPanelTwoDimStandard().drawVoid(contextVoid);
//				}
//				
//			}
//
//		}
		
		
		
		
		
		Contexts.removeAllItems();
		Contexts.addItem(null);
		for(Context ctxt : world.getScheduler().getContextsAsContext()) {
			Contexts.addItem(ctxt);
		}
		
		for(Context ctxt : world.getScheduler().getToKillContext()) {
			if (graph.getNode(ctxt.getName()) != null) {
				graph.removeNode(ctxt.getName());
				
				
			}
		}

	}
	
	
	public void highlightExperiments(Context ctxt) {
		
		
		for(Experiment exp : ctxt.getExperiments()) {
			
			Node expNode1;
			String expNode1String = "" + ctxt.getName()+"Exp"+ctxt.getExperiments().indexOf(exp)+"_1";
			Node expNode2;
			String expNode2String = "" + ctxt.getName()+"Exp"+ctxt.getExperiments().indexOf(exp)+"_2";
			
			if(graph.getNode(expNode1String) == null) {
				graph.addNode(expNode1String);
			}
			if(graph.getNode(expNode2String) == null) {
				graph.addNode(expNode2String);
			}
			expNode1 = graph.getNode(expNode1String);
			expNode2 = graph.getNode(expNode2String);
			
			expNode1.addAttribute("EXIST", true);
			expNode2.addAttribute("EXIST", true);
			
//			world.trace(new ArrayList<String>(Arrays.asList(
//					"DRAW EXP", 
//					expNode1String,
//					" 1 "+comboDimX.getSelectedItem(),
//					" 2 "+world.getAgents().get(comboDimX.getSelectedItem()),
//					" 3 "+exp.getValuesAsHashMap().get(world.getAgents().get(comboDimX.getSelectedItem())),
//					" 4 "+exp.getValuesAsHashMap()	
//					)));
			double expNode1x = exp.getValuesAsHashMap().get(world.getAgents().get(comboDimX.getSelectedItem()));
			double expNode2x = exp.getValuesAsHashMap().get(world.getAgents().get(comboDimX.getSelectedItem()));;
			
			double expNode1y = exp.getValuesAsHashMap().get(world.getAgents().get(comboDimY.getSelectedItem()));;
			double expNode2y = exp.getValuesAsHashMap().get(world.getAgents().get(comboDimY.getSelectedItem()));;

									
					
			
			expNode1.setAttribute("xyz", expNode1x, expNode1y, 0);
			expNode2.setAttribute("xyz", expNode2x, expNode2y, 0);
			
			expNode1.addAttribute("ui.style", "size: " + doubleFormat.format(0.5) + "gu, " + doubleFormat.format(6) +"gu;");
			expNode2.addAttribute("ui.style", "size: " + doubleFormat.format(6) + "gu, " + doubleFormat.format(0.5) +"gu;");
			
			expNode1.addAttribute("ui.class","RGBAColor");
			expNode2.addAttribute("ui.class","RGBAColor");
			
			expNode1.addAttribute("ui.style", "fill-color: rgba(255,255,255,255);");
			expNode2.addAttribute("ui.style", "fill-color: rgba(255,255,255,255);");
			

		}
		
	}
	
	public void drawExperiments(Context ctxt) {
		
		
		
		for(Experiment exp : ctxt.getExperiments()) {
			
			Node expNode1;
			String expNode1String = "" +"Exp_"+ctxt.getExperiments().indexOf(exp)+"_1"+"_"+ ctxt.getName();
			Node expNode2;
			String expNode2String = "" +"Exp_"+ctxt.getExperiments().indexOf(exp)+"_2"+"_"+ ctxt.getName();
			
			if(graph.getNode(expNode1String) == null) {
				graph.addNode(expNode1String);
			}
			if(graph.getNode(expNode2String) == null) {
				graph.addNode(expNode2String);
			}
			expNode1 = graph.getNode(expNode1String);
			expNode2 = graph.getNode(expNode2String);
			
			expNode1.addAttribute("EXIST", true);
			expNode2.addAttribute("EXIST", true);
			
//			world.trace(new ArrayList<String>(Arrays.asList(
//					"DRAW EXP", 
//					expNode1String,
//					" 1 "+comboDimX.getSelectedItem(),
//					" 2 "+world.getAgents().get(comboDimX.getSelectedItem()),
//					" 3 "+exp.getValuesAsHashMap().get(world.getAgents().get(comboDimX.getSelectedItem())),
//					" 4 "+exp.getValuesAsHashMap()	
//					)));
			double expNode1x = exp.getValuesAsHashMap().get(world.getAgents().get(comboDimX.getSelectedItem()));
			double expNode2x = exp.getValuesAsHashMap().get(world.getAgents().get(comboDimX.getSelectedItem()));;
			
			double expNode1y = exp.getValuesAsHashMap().get(world.getAgents().get(comboDimY.getSelectedItem()));;
			double expNode2y = exp.getValuesAsHashMap().get(world.getAgents().get(comboDimY.getSelectedItem()));;

									
					
			
			expNode1.setAttribute("xyz", expNode1x, expNode1y, 0);
			expNode2.setAttribute("xyz", expNode2x, expNode2y, 0);
			
			expNode1.addAttribute("ui.style", "size: " + doubleFormat.format(0.5) + "gu, " + doubleFormat.format(2) +"gu;");
			expNode2.addAttribute("ui.style", "size: " + doubleFormat.format(2) + "gu, " + doubleFormat.format(0.5) +"gu;");
			
			expNode1.addAttribute("ui.class","RGBAColor");
			expNode2.addAttribute("ui.class","RGBAColor");
			
			expNode1.addAttribute("ui.style", "fill-color: rgba(255,255,255,255);");
			expNode2.addAttribute("ui.style", "fill-color: rgba(255,255,255,255);");
			

		}
		
	}
	
	public void recolorContexts() {
		for (Context n : world.getScheduler().getContextsAsContext()) {
			String name = n.getName();

				Node node = graph.getNode(name);

				//node.addAttribute("ui.class","ContextColorDynamic");
				//node.setAttribute("ui.color", (n.getActionProposal() - min) / (max - min) ); 
//				node.setAttribute("ui.color", 0.0 ); 
				
				Double r = 0.0;
				Double g = 0.0;
				Double b = 0.0;
				double[] coefs = n.getLocalModel().getCoef();
				//System.out.println("COEFS : " + coefs.length);
				if(coefs.length>0) {
					if(coefs.length==1) {
						//System.out.println(coefs[0]);	
						b = normalizePositiveValues(255, 5, Math.abs(coefs[0]));
						if(b.isNaN()) {
							b = 0.0;
						}
					}
					else if(coefs.length==2) {
						//System.out.println(coefs[0] + " " + coefs[1]);
						g =  normalizePositiveValues(255, 5, Math.abs(coefs[0]));
						b =  normalizePositiveValues(255, 5, Math.abs(coefs[1]));
						if(g.isNaN()) {
							g = 0.0;
						}
						if(b.isNaN()) {
							b = 0.0;
						}
					}
					else if(coefs.length>=3) {
						//System.out.println(coefs[0] + " " + coefs[1] + " " + coefs[2]);
						r =  normalizePositiveValues(255, 5,  Math.abs(coefs[0]));
						g =  normalizePositiveValues(255, 5,  Math.abs(coefs[1]));
						b =  normalizePositiveValues(255, 5,  Math.abs(coefs[2]));
						if(r.isNaN()) {
							r = 0.0;
						}
						if(g.isNaN()) {
							g = 0.0;
						}
						if(b.isNaN()) {
							b = 0.0;
						}
					}
					else {
						r = 255.0;
						g = 255.0;
						b = 255.0;
					}
				}
				else {
					r = 255.0;
					g = 255.0;
					b = 255.0;
				}
				
				node.addAttribute("ui.class","RGBAColor");
				
				node.addAttribute("ui.style", "fill-color: rgba(" + r.intValue() + "," + g.intValue() + "," + b.intValue() + ",100);");

				
				
				
				
				
//				for(Percept pct : world.getScheduler().getPercepts()) {
//					if(world.getScheduler().getHeadAgent().getPartiallyActivatedNeighborContexts(pct).contains(n)) {
//						node.addAttribute("ui.style", "fill-color: rgba(0,255,255,150);");
//					}
//				}
			
//				for(Percept pct : world.getScheduler().getPercepts()) {
//					if(world.getScheduler().getHeadAgent().getPartiallyActivatedContexts(pct).contains(n)) {
//						node.addAttribute("ui.style", "fill-color: rgba(0,0,255,150);");
//					}
//				}
//				
//				System.out.println(world.getScheduler().getTick() + " Neighbor size "+ world.getScheduler().getHeadAgent().getActivatedNeighborsContexts().size());
//				if(world.getScheduler().getHeadAgent().getActivatedNeighborsContexts().contains(n)) {
//					node.addAttribute("ui.style", "fill-color: rgba(255,0,255,200);");
//				}
//				
//				if(world.getScheduler().getHeadAgent().getActivatedContexts().contains(n)) {
//					node.addAttribute("ui.style", "fill-color: rgba(255,0,127,200);");
//				}
				
//				if(world.getScheduler().getHeadAgent().getBestContext() == n) {
//				node.addAttribute("ui.style", "fill-color: rgba(0,255,0,200);");
//			}

			
				
//				drawExperiments(n);
				

			
			

		}
	}
	
	
	public void drawOverlap(ContextOverlap contextOverlap){
		Node node;
		if (graph.getNode(contextOverlap.getName()) != null) {
			node = graph.getNode(contextOverlap.getName());
		} else {
			graph.addNode(contextOverlap.getName());
			node = graph.getNode(contextOverlap.getName());
			node.addAttribute("ui.class", contextOverlap.getClass().getSimpleName());
			node.addAttribute("ui.label", contextOverlap.getName());
		}

		node.addAttribute("EXIST", true);
		
		double lengthX = contextOverlap.getRanges(comboDimX.getSelectedItem()).get("end") 
				- contextOverlap.getRanges(comboDimX.getSelectedItem()).get("start");
		double lengthY = contextOverlap.getRanges(comboDimY.getSelectedItem()).get("end")
				- contextOverlap.getRanges(comboDimY.getSelectedItem()).get("start");
		
		node.setAttribute("xyz", contextOverlap.getRanges(comboDimX.getSelectedItem()).get("start") + (0.5*lengthX),
		contextOverlap.getRanges(comboDimY.getSelectedItem()).get("start") + (0.5*lengthY), 0);
		
		node.addAttribute("ui.style", "size: " + doubleFormat.format(lengthX) + "gu, " + doubleFormat.format(lengthY) +"gu;");
		node.addAttribute("ui.class","Rectangle");
		
		//node.addAttribute("ui.color", Color.RED);
		node.addAttribute("ui.class","OverlapColor");

	}
	
	
	public void drawVoid(ContextVoid contextVoid){
		Node node;
		if (graph.getNode(contextVoid.getName()) != null) {
			node = graph.getNode(contextVoid.getName());
		} else {
			graph.addNode(contextVoid.getName());
			node = graph.getNode(contextVoid.getName());
			node.addAttribute("ui.class", contextVoid.getClass().getSimpleName());
			node.addAttribute("ui.label", contextVoid.getName());
		}

		node.addAttribute("EXIST", true);
		
		
		node.setAttribute("xyz", contextVoid.getPositionByString(comboDimX.getSelectedItem()) ,
				contextVoid.getPositionByString(comboDimY.getSelectedItem()), 0);
		
		double XWidth = contextVoid.getWidthByString(comboDimX.getSelectedItem());
		double YWidth = contextVoid.getWidthByString(comboDimY.getSelectedItem());
		
		node.addAttribute("ui.style", "size: " + doubleFormat.format( XWidth ) + "gu, " + doubleFormat.format( YWidth ) +"gu;");
		node.addAttribute("ui.class","Rectangle");
		
		//node.addAttribute("ui.color", Color.RED);
		node.addAttribute("ui.class","VoidColorDynamic");
		node.setAttribute("ui.color", 0.1 ); 
		//node.addAttribute("ui.class","VoidColor");

	}
	
	public void drawRectangle (Node node, Context context) {
		
//		System.out.println(n.getRanges().get(world.getAgents().get("Sensor")).getStart() + " " + n.getRanges().get(world.getAgents().get("SensorPerturbation")).getStart());
		
		double lengthX = context.getRanges().get(world.getAgents().get(comboDimX.getSelectedItem())).getEnd() 
				- context.getRanges().get(world.getAgents().get(comboDimX.getSelectedItem())).getStart();
		double lengthY = context.getRanges().get(world.getAgents().get(comboDimY.getSelectedItem())).getEnd() 
				- context.getRanges().get(world.getAgents().get(comboDimY.getSelectedItem())).getStart();
		
		node.setAttribute("xyz", context.getRanges().get(world.getAgents().get(comboDimX.getSelectedItem())).getStart() + (0.5*lengthX),
				context.getRanges().get(world.getAgents().get(comboDimY.getSelectedItem())).getStart() + (0.5*lengthY), 0);
		
	//	node.setAttribute("xyz", n.getRanges().get(world.getAgents().get("Sensor")).getValue(), n.getRanges().get(world.getAgents().get("SensorPerturbation")).getValue(), 0);
	//	node.addAttribute("ui.size", "8px");
		
		node.addAttribute("ui.style", "size: " + doubleFormat.format(lengthX) + "gu, " + doubleFormat.format(lengthY) +"gu;");

	}
	
	public void drawXMinMax (Node nodeMin, Node nodeMax, Percept pct, Percept otherPercept) {
		
		
		double lengthX = 1.0;
		double lengthY = (world.getScheduler().getTick()>0 ) ? otherPercept.getMax() - otherPercept.getMin() : 1;
		
		double xMinPosition = (world.getScheduler().getTick()>0 ) ? pct.getMin() : 0;
		double xMaxPosition = (world.getScheduler().getTick()>0 ) ? pct.getMax() : 0;
		
		double yMinPosition = (world.getScheduler().getTick()>0 ) ? (otherPercept.getMax() + otherPercept.getMin())/2 : 1;
		double yMaxPosition = (world.getScheduler().getTick()>0 ) ? (otherPercept.getMax() + otherPercept.getMin())/2 : 1;
		
		
		nodeMin.setAttribute("xyz", xMinPosition, yMinPosition, 0);
		nodeMax.setAttribute("xyz", xMaxPosition, yMaxPosition, 0);
		
		
		nodeMin.addAttribute("ui.style", "size: " + doubleFormat.format(lengthX) + "gu, " + doubleFormat.format(lengthY) +"gu;");
		nodeMax.addAttribute("ui.style", "size: " + doubleFormat.format(lengthX) + "gu, " + doubleFormat.format(lengthY) +"gu;");
		
		nodeMin.addAttribute("ui.class","RGBAColor");
		nodeMin.addAttribute("ui.style", "fill-color: rgba(0,0,0,100);");
		
		nodeMax.addAttribute("ui.class","RGBAColor");
		nodeMax.addAttribute("ui.style", "fill-color: rgba(0,0,0,100);");

	}
	
	public void drawYMinMax (Node nodeMin, Node nodeMax, Percept pct, Percept otherPercept) {
		
		
		double lengthY = 1.0;
		double lengthX = (world.getScheduler().getTick()>0 ) ? otherPercept.getMax() - otherPercept.getMin() : 1;
		
		double yMinPosition = (world.getScheduler().getTick()>0 ) ? pct.getMin() : 0;
		double yMaxPosition = (world.getScheduler().getTick()>0 ) ? pct.getMax() : 0;
		
		double xMinPosition = (world.getScheduler().getTick()>0 ) ? (otherPercept.getMax() + otherPercept.getMin())/2 : 1;
		double xMaxPosition = (world.getScheduler().getTick()>0 ) ? (otherPercept.getMax() + otherPercept.getMin())/2 : 1;
		
		
		nodeMin.setAttribute("xyz", xMinPosition, yMinPosition, 0);
		nodeMax.setAttribute("xyz", xMaxPosition, yMaxPosition, 0);
		
		
		nodeMin.addAttribute("ui.style", "size: " + doubleFormat.format(lengthX) + "gu, " + doubleFormat.format(lengthY) +"gu;");
		nodeMax.addAttribute("ui.style", "size: " + doubleFormat.format(lengthX) + "gu, " + doubleFormat.format(lengthY) +"gu;");
		
		nodeMin.addAttribute("ui.class","RGBAColor");
		nodeMin.addAttribute("ui.style", "fill-color: rgba(0,0,0,100);");
		
		nodeMax.addAttribute("ui.class","RGBAColor");
		nodeMax.addAttribute("ui.style", "fill-color: rgba(0,0,0,100);");

	}
	
	public HashMap<String, Double> request(Point3 position){
		Node requestNode;
		if (graph.getNode("request") != null) {
			requestNode = graph.getNode("request");
		} else {
			graph.addNode("request");
			requestNode = graph.getNode("request");
		}

		requestNode.addAttribute("EXIST", true);
		
		requestNode.setAttribute("xyz", position.x, position.y, position.z);
		
		requestNode.addAttribute("ui.style", "size: " + doubleFormat.format(2) + "gu, " + doubleFormat.format(2) +"gu;");
		
		
		HashMap<String, Double> requestHashMap = new HashMap<String, Double>();
		
		requestHashMap.put("px",position.x);
		requestHashMap.put("py",position.y);
		requestHashMap.put("oracle",0.0);
		
		return requestHashMap;
	}
	
	public void highlightContextNeighbours(Context context) {
		
		
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		
		for (String name : world.getAgents().keySet()) {
			SystemAgent a = world.getAgents().get(name);
			if (a instanceof Context) {
				double val = ((Context) a).getActionProposal();
				if (val < min) {
					min = val;
				}
				if (val > max) {
					max = val;
				}
			}

		}	
		
		for (String name : world.getAgents().keySet()) {
			SystemAgent a = world.getAgents().get(name);
			if (a instanceof Context) {
				Context n = (Context)a;
				Node node = graph.getNode(name);

				node.addAttribute("ui.class","ContextColorDynamic");
				
				
				node.setAttribute("ui.color", 0.1 ); 
				
				if(context.possibleNeighbours.contains(n)) {
					node.setAttribute("ui.color", 0.5 ); 
				}
				if(context.neighbours.contains(n)){
					node.setAttribute("ui.color", 1 );  
				}
				if(context.equals(n)) {
					node.setAttribute("ui.color", 0.0 ); 
				}
				
				
 
			}

		}
		
		
		for(ContextVoid contextVoid : world.getScheduler().contextVoids) {
			Node node = graph.getNode(contextVoid.getName());
			node.addAttribute("ui.class","VoidColorDynamic");
			
			if(context.contextVoids.contains(contextVoid)) {
				node.setAttribute("ui.color", 1 ); 
			}
			else {
				node.setAttribute("ui.color", 0.1 ); 
			}
			
			
		}
		
		
	}
	
	public void highlightVoidContexts(ContextVoid contextVoid) {
		
		
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		
		for (String name : world.getAgents().keySet()) {
			SystemAgent a = world.getAgents().get(name);
			if (a instanceof Context) {
				double val = ((Context) a).getActionProposal();
				if (val < min) {
					min = val;
				}
				if (val > max) {
					max = val;
				}
			}

		}	
		
		for (Context ctxt :  contextVoid.getSurroundingContexts()) {

				Node node = graph.getNode(ctxt.getName());
				node.addAttribute("ui.class","ContextColorDynamic");
				node.setAttribute("ui.color", 0.5 ); 
		}
	}
	
	public void highlightContexts(Context context) {
		
		
		for (String name : world.getAgents().keySet()) {
			SystemAgent a = world.getAgents().get(name);
			if (a instanceof Context) {
				Context n = (Context)a;
				Node node = graph.getNode(name);

				Double r = 0.0;
				Double g = 0.0;
				Double b = 0.0;
				double[] coefs = n.getLocalModel().getCoef();
				//System.out.println("COEFS : " + coefs.length);
				if(coefs.length>0) {
					if(coefs.length==1) {
						//System.out.println(coefs[0]);	
						b = normalizePositiveValues(255, 5, Math.abs(coefs[0]));
						if(b.isNaN()) {
							b = 0.0;
						}
					}
					else if(coefs.length==2) {
						//System.out.println(coefs[0] + " " + coefs[1]);
						g =  normalizePositiveValues(255, 5, Math.abs(coefs[0]));
						b =  normalizePositiveValues(255, 5, Math.abs(coefs[1]));
						if(g.isNaN()) {
							g = 0.0;
						}
						if(b.isNaN()) {
							b = 0.0;
						}
					}
					else if(coefs.length>=3) {
						//System.out.println(coefs[0] + " " + coefs[1] + " " + coefs[2]);
						r =  normalizePositiveValues(255, 5,  Math.abs(coefs[0]));
						g =  normalizePositiveValues(255, 5,  Math.abs(coefs[1]));
						b =  normalizePositiveValues(255, 5,  Math.abs(coefs[2]));
						if(r.isNaN()) {
							r = 0.0;
						}
						if(g.isNaN()) {
							g = 0.0;
						}
						if(b.isNaN()) {
							b = 0.0;
						}
					}
					else {
						r = 255.0;
						g = 255.0;
						b = 255.0;
					}
				}
				else {
					r = 255.0;
					g = 255.0;
					b = 255.0;
				}
				
				node.addAttribute("ui.class","RGBAColor");
				
				node.addAttribute("ui.style", "fill-color: rgba(" + r.intValue() + "," + g.intValue() + "," + b.intValue() + ",50);");


				if(context.equals(n)) {
					node.addAttribute("ui.style", "fill-color: rgba(" + r.intValue() + "," + g.intValue() + "," + b.intValue() + ",200);");
				}
			}
			
			

		}
	}
	
	public void recolorAllContexts() {
		
		
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		
		for (String name : world.getAgents().keySet()) {
			SystemAgent a = world.getAgents().get(name);
			if (a instanceof Context) {
				double val = ((Context) a).getActionProposal();
				if (val < min) {
					min = val;
				}
				if (val > max) {
					max = val;
				}
			}
	
		}		
	
		for (String name : world.getAgents().keySet()) {
			SystemAgent a = world.getAgents().get(name);
			if (a instanceof Context) {
				Context n = (Context)a;
				Node node = graph.getNode(name);
	
				node.addAttribute("ui.class","ContextColorDynamic");
				node.setAttribute("ui.color", (n.getActionProposal() - min) / (max - min) ); 
				//node.setAttribute("ui.color", 0.5 );  
				for(ContextVoid contextVoid : n.contextVoids) {
					Node node2 = graph.getNode(contextVoid.getName());
					node2.addAttribute("ui.class","VoidColorDynamic");
					node2.setAttribute("ui.color", 1 ); 
				}
			}
	
		}
	
	}
		
	
	/**
	 * Creates the graph.
	 */
	private void createGraph() {
		
		
		
		
		System.out.println("Create graph system");
		graph = new SingleGraph("SYSTEM");

		viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.addDefaultView(false);
		viewer.disableAutoLayout();
		viewer.getDefaultView().addMouseListener(this);
		

		pipe = viewer.newViewerPipe();
        pipe.addViewerListener(this);
        pipe.addSink(graph);

        viewer.getDefaultView().setMinimumSize(new Dimension(400,400));
        
        viewer.getDefaultView().addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() == -1) {
					zoomLevel = zoomLevel - 0.1;
					if (zoomLevel < 0.1) {
						zoomLevel = 0.1;
					}
					viewer.getDefaultView().getCamera().setViewPercent(zoomLevel);
				}
				if (e.getWheelRotation() == 1) {
					zoomLevel = zoomLevel + 0.1;
					viewer.getDefaultView().getCamera().setViewPercent(zoomLevel);
				}
			}
		});
        
		this.add(viewer.getDefaultView(),BorderLayout.CENTER);
		setStandardStyle();
		
		setOrigin();
		setMinMax();
		setHiddenModel();
		
		
	}
	
	/**
	 * Removes the graph.
	 */
	private void removeGraph() {
		//this.remove(((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER));
		pipe.removeSink(graph);
	}

	/**
	 * New agent.
	 *
	 * @param a the a
	 */
	public void newAgent(Agent a) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.graphstream.ui.view.ViewerListener#buttonPushed(java.lang.String)
	 */
	@Override
	public void buttonPushed(String id) {
		world.getAmoeba().getLogFile().messageToDebug("Push button", 5, new LogMessageType[] {LogMessageType.INFORMATION});
		toolBarInfo.setVisible(true);
		System.out.println("node pushed : " + id);
		requestPosition = viewer.getDefaultView().getCamera().transformPxToGu(mouseEvent.getX(), mouseEvent.getY());
		System.out.println("POSITION :" + requestPosition.x + " ; " + requestPosition.y + " ; " + requestPosition.z);

		if(world.getScheduler().getContextByName(id)!=null) {
			Context pushedContext = world.getScheduler().getContextByName(id);
			setContextTextAreaInfo(id);
			
			if (sliderValue == currentTick || (!rememberState)) {
				if (rightClick) {
					popupMenuForContextVisualization(id);
					rightClick = false;
				}
			}

		}
		else if(world.getScheduler().getContextOverlapByName(id)!=null) {
			ContextOverlap pushedContextOverlap = world.getScheduler().getContextOverlapByName(id);
			setContextOverlapTextAreaInfo(id);
			
			if (sliderValue == currentTick || (!rememberState)) {
				if (rightClick) {
					popupMenuForOverlapVisualization(id);
					rightClick = false;
				}
			}
			
		}
		else if(world.getScheduler().getContextVoidByName(id)!=null) {
			ContextVoid pushedContextVoid = world.getScheduler().getContextVoidByName(id);
			setContextVoidTextAreaInfo(id);
			
			if (sliderValue == currentTick || (!rememberState)) {
				if (rightClick) {
					popupMenuForVoidVisualization(id);
					rightClick = false;
				}
			}
			
		}
		else {
			if (rightClick) {
				popupMenuForVoidVisualization(id);
				rightClick = false;
			}
		}
		
		
		//String info = world.getAgents().get(id).toString();
		//System.out.println(info);
		
		//JOptionPane.showMessageDialog(this, info, "Context : " + id, JOptionPane.PLAIN_MESSAGE);



/*		Node node = graph.getNode(id);
		System.out.println(node.getAttribute("xyz").toString());
		Context n = (Context) world.getAgents().get(id);*/
	
	}
	
	/**
	 * Sets the text area info.
	 *
	 * @param id the new text area info
	 */
	private void setContextTextAreaInfo(String id) {
		System.out.println("Context pushed : " + id);
		
		String info = "";
		if (rememberState) {
			Observation o = getObservationByTick(sliderValue);
			if (o != null) {
				Context c = o.getContextById(id);
				if ( c != null ) {
					info = "State :" + sliderValue + "\n";
					info = info.concat(o.getContextById(id).toStringFull());
					info = info.replace("Current", "\nCurrent");
					info = info.replace("AVT", "\nAVT");
				} else {
					info = "No context";
				}
				
			} 
			
		} else {
			info = "State :" + currentTick + "\n";
			info = info.concat(world.getAgents().get(id).toStringFull());
			info = info.replace("Current", "\nCurrent");
			info = info.replace("AVT", "\nAVT");
		}
		
		textarea.setText(info);
		
	}
	
	private void setContextOverlapTextAreaInfo(String id) {
		System.out.println("Context Overlap pushed : " + id);
		
		String info = "";
		if (rememberState) {
			Observation o = getObservationByTick(sliderValue);
			if (o != null) {
				ContextOverlap contextOverlap = world.getScheduler().getContextOverlapByName(id);
				if ( contextOverlap != null ) {
					info = "State :" + sliderValue + "\n";
					info = info.concat(contextOverlap.toString());
					info = info.replace("Current", "\nCurrent");
					info = info.replace("AVT", "\nAVT");
				} else {
					info = "No context";
				}
				
			} 
			
		} else {
			info = "State :" + currentTick + "\n";
			info = info.concat(world.getScheduler().getContextOverlapByName(id).toString());
			info = info.replace("Current", "\nCurrent");
			info = info.replace("AVT", "\nAVT");
		}
		
		textarea.setText(info);
		
	}
	
	private void setContextVoidTextAreaInfo(String id) {
		System.out.println("Context Void pushed : " + id);
		
		String info = "";
		if (rememberState) {
			Observation o = getObservationByTick(sliderValue);
			if (o != null) {
				ContextVoid contextVoid = world.getScheduler().getContextVoidByName(id);
				if ( contextVoid != null ) {
					info = "State :" + sliderValue + "\n";
					info = info.concat(contextVoid.toString());
					info = info.replace("Current", "\nCurrent");
					info = info.replace("AVT", "\nAVT");
				} else {
					info = "No context";
				}
				
			} 
			
		} else {
			info = "State :" + currentTick + "\n";
			info = info.concat(world.getScheduler().getContextVoidByName(id).toString());
			info = info.replace("Current", "\nCurrent");
			info = info.replace("AVT", "\nAVT");
		}
		
		textarea.setText(info);
		
	}
	
	/**
	 * Start panel criterion.
	 *
	 * @param id the id
	 */
	private void startPanelCriterion(String id) {
	}
	
	/**
	 * Start panel 1 D paving.
	 *
	 * @param id the id
	 */
	private void startPanel1DPaving(String id) {
		Panel1DPaving pan = new Panel1DPaving((Percept) world.getAgents().get(id), world);
		JFrame frame = new JFrame(id);
		world.getScheduler().addScheduledItem(pan);
        frame.setAlwaysOnTop(true);
		frame.setContentPane(new JScrollPane(pan));
		frame.setVisible(true);
		frame.pack();
	}
	
	/**
	 * Popup criterion.
	 *
	 * @param id the id
	 */
	public void popupCriterion(String id){
		
		JPopupMenu popup = new JPopupMenu("Criterion");
		JMenuItem itemChartCriterion = new JMenuItem("Show charts");
		itemChartCriterion.addActionListener(e -> {startPanelCriterion(id);});
		itemChartCriterion.setIcon(Config.getIcon("pencil.png"));
		popup.add(itemChartCriterion);
		
		popup.show(this, this.getX() + mouseEvent.getX(), this.getY() + mouseEvent.getY());
	}
	
	/**
	 * Popup percept.
	 *
	 * @param id the id
	 */
	public void popupPercept(String id){
					
		JPopupMenu popup = new JPopupMenu("Percept");
		JMenuItem itemShow1DPaving = new JMenuItem("Show 1D paving");
		itemShow1DPaving.addActionListener(e -> {startPanel1DPaving(id);});
		itemShow1DPaving.setIcon(Config.getIcon("pencil.png"));
		popup.add(itemShow1DPaving);
		
		popup.show(this, this.getX() + mouseEvent.getX(), this.getY() + mouseEvent.getY());
	}
	
	/**
	 * Popup menu for visualization.
	 *
	 * @param id the id
	 */
	
	
	public void popupMenuForVisualization(String id) {
		JPopupMenu popup = new JPopupMenu("Visualization");
		JMenuItem itemRecolorAll = new JMenuItem("ReColorAll");
		itemRecolorAll.addActionListener(e -> {recolorAllContexts();});
		JMenuItem itemShowAll = new JMenuItem("Both");
	    
	    itemShowAll.addActionListener( e -> {recolorAllContexts();});
	    popup.add(itemRecolorAll);
	    popup.add(itemShowAll);
	    popup.show(this, this.getX() + mouseEvent.getX(), this.getY() + mouseEvent.getY());
	}
	
	
	public void popupMenuForContextVisualization(String id) {
		JPopupMenu popup = new JPopupMenu("Visualization");
		JMenuItem itemShowContextNeighbours = new JMenuItem("Neighbours");
		itemShowContextNeighbours.addActionListener(e -> {highlightNeighbours(id);});
		JMenuItem itemShow2Dim = new JMenuItem("History of grapgh in 2 Dim");
		itemShow2Dim.addActionListener(e -> {popupVisualization2Dim(id);});
	    JMenuItem itemShowNDim = new JMenuItem("Graph Visualization in N Dim");
	    itemShowNDim.addActionListener(e -> {popupVisualizationNDim(id);});
	    JMenuItem itemShowAll = new JMenuItem("Both");
	    JMenuItem itemRecolorAll = new JMenuItem("Recolor All Contexts");
		itemRecolorAll.addActionListener(e -> {recolorAllContexts();});
		JMenuItem itemUdateView = new JMenuItem("Update View");
		itemUdateView.addActionListener(e -> {update();});
		JMenuItem itemUdateOverlapsAndNeighbours = new JMenuItem("Update Overlaps and Neighbours");
		itemUdateOverlapsAndNeighbours.addActionListener(e -> {updateOverlapsAndNeighbours();});
		JMenuItem itemShowExperiments = new JMenuItem("Show Experiments");
		itemShowExperiments.addActionListener(e -> {showExperiments(id);});
		JMenuItem itemKill = new JMenuItem("Kill");
		itemKill.addActionListener(e -> {killContext(id);});
		JMenuItem itemManualRequest = new JMenuItem("Manual request");
		itemManualRequest.addActionListener(e -> {world.getScheduler().setManualRequest(requestPosition);});
	    
	    itemShowAll.addActionListener( e -> {highlightNeighbours(id); popupVisualization2Dim(id); popupVisualizationNDim(id); recolorAllContexts(); update(); showExperiments(id);killContext(id);world.getScheduler().setManualRequest(requestPosition);});
	    popup.add(itemShowContextNeighbours);
	    popup.add(itemShow2Dim);
	    popup.add(itemShowNDim);
	    popup.add(itemShowAll);
	    popup.add(itemRecolorAll);
	    popup.add(itemUdateView);
	    popup.add(itemUdateOverlapsAndNeighbours);
	    popup.add(itemShowExperiments);
	    popup.add(itemKill);
	    popup.add(itemManualRequest);
	    
	    popup.show(this, this.getX() + mouseEvent.getX(), this.getY() + mouseEvent.getY());
	}
	
	public void killContext(String id) {
		world.getScheduler().getContextByName(id).die();
		update();
		
	}
	
	public void showExperiments(String id) {
		
		highlightExperiments(world.getScheduler().getContextByName(id));
		
		
	}
	
	public void popupMenuForRightClickOnVoid() {
		JPopupMenu popup = new JPopupMenu("Right Click On Void");
		JMenuItem itemShowAll = new JMenuItem("Show All");
		JMenuItem itemManualRequest = new JMenuItem("Manual request");
		itemManualRequest.addActionListener(e -> {world.getScheduler().setManualRequest(requestPosition);});
	    
	    itemShowAll.addActionListener( e -> {world.getScheduler().setManualRequest(requestPosition);});
	 
	    popup.add(itemManualRequest);
	    
	    popup.show(this, this.getX() + mouseEvent.getX(), this.getY() + mouseEvent.getY());
	}
	
	public void popupMenuForVoidVisualization(String id) {
		JPopupMenu popup = new JPopupMenu("Visualization");
		JMenuItem itemShowContextNeighbours = new JMenuItem("Contexts");
		itemShowContextNeighbours.addActionListener(e -> {hightlightContextVoid(id);});
		JMenuItem itemShowAll = new JMenuItem("Both");
		
	    itemShowAll.addActionListener( e -> {hightlightContextVoid(id);});
	    popup.add(itemShowContextNeighbours);
	    popup.add(itemShowAll);
	    popup.show(this, this.getX() + mouseEvent.getX(), this.getY() + mouseEvent.getY());
	}
	
	public void popupMenuForOverlapVisualization(String id) {
		
		JPopupMenu popup = new JPopupMenu("Visualization");
		JMenuItem itemSolvsNCS = new JMenuItem("Solve Overlap NCS");
		itemSolvsNCS.addActionListener(e -> {solveNCSByClick(id);});
		JMenuItem itemSolveAllNCS = new JMenuItem("Solve All Overlaps NCS");
		itemSolveAllNCS.addActionListener(e -> {solveAllNCSByClick(id);});
		JMenuItem itemUdateView = new JMenuItem("Update View");
		itemUdateView.addActionListener(e -> {update();});
		JMenuItem itemUdateOverlapsAndNeighbours = new JMenuItem("Update Overlaps and Neighbours");
		itemUdateOverlapsAndNeighbours.addActionListener(e -> {updateOverlapsAndNeighbours();});
 
	    popup.add(itemSolvsNCS);
	    popup.add(itemSolveAllNCS);
	    popup.add(itemUdateView);
	    popup.add(itemUdateOverlapsAndNeighbours);
	    popup.show(this, this.getX() + mouseEvent.getX(), this.getY() + mouseEvent.getY());
		
	}
	

//	private void displayMousePositionInChart() {
//		Point2D p = graph.
//				translateScreenToJava2D(mouseEvent.getPoint());
//		Rectangle2D plotArea = chartPanel.getScreenDataArea();
//		XYPlot plot = (XYPlot) chart.getPlot(); // your plot
//		double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
//		double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());
//	}
	
	private void highlightNeighbours(String id) {
		highlightContextNeighbours(world.getScheduler().getContextByName(id));
		
	}
	
	private void hightlightContextVoid(String id) {
		highlightVoidContexts(world.getScheduler().getContextVoidByName(id));
	}
	
	
	private void updateOverlapsAndNeighbours() {
		
		for(Percept percept : world.getScheduler().getPercepts()) {
			percept.overlapsDetection();
			percept.overlapNotification();
		}
		
		for(Context context : world.getScheduler().getContextsAsContext()) {
			
			context.computeOverlapsByPercepts();
			context.getNearestNeighbours();
		}
		
		update();
	}
	
	public void solveAllNCSByClick(String id) {
		//TODO ERREUR ICI car je supprime des éléments de la liste et je la lis je pense...
		for(ContextOverlap contextOverlap : world.getScheduler().getContextOverlaps()) {
			contextOverlap.solveNCS_Overlap(0.1d);
			System.out.println(contextOverlap.getName());
	
			
			update();
		}
		
		world.getScheduler().clearContextOverlaps();
	}
	
	public void solveNCSByClick(String id) {
		world.getScheduler().getContextOverlapByName(id).solveNCS_Overlap(0.1d);
	}

	/**
	 * Popup visualization 2 dim.
	 *
	 * @param id the id
	 */
	public void popupVisualization2Dim(String id) {
		GraphicVisualization2Dim gv = new GraphicVisualization2Dim();
		gv.setXYSeries(perceptName);
		gv.init();
		gv.setContextID(id);
		temporalGraph.get2DimGraphList().add(gv);
		getPerceptMinMaxValues2Dim();
		gv.setVisible(true);
	}
	
	/**
	 * Popup visualization N dim.
	 *
	 * @param id the id
	 */
	public void popupVisualizationNDim(String id) {
		GraphicVisualizationNDim ngv = new GraphicVisualizationNDim();
		ngv.initializeColumn(perceptName);
		ngv.init();
		ngv.setContextID(id);
		temporalGraph.getNDimGraphList().add(ngv);

		getPerceptMinMaxValuesNDim();
		ngv.setVisible(true);	
	}

	/* (non-Javadoc)
	 * @see org.graphstream.ui.view.ViewerListener#buttonReleased(java.lang.String)
	 */
	@Override
	public void buttonReleased(String id) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.graphstream.ui.view.ViewerListener#viewClosed(java.lang.String)
	 */
	@Override
	public void viewClosed(String arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	
	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		mouseEvent = e;
		//System.out.println("getPoint " + mouseEvent.getPoint() + " " + this.getSize());
		//graph.getNode("origin").
		requestPosition = viewer.getDefaultView().getCamera().transformPxToGu(mouseEvent.getX(), mouseEvent.getY());
		System.out.println("POSITION :" + requestPosition.x + " ; " + requestPosition.y + " ; " + requestPosition.z);
		
		
		if(SwingUtilities.isRightMouseButton(e)){
			popupMenuForRightClickOnVoid();
			rightClick = true;
			Robot bot;
			try {
				bot = new Robot();
				int mask = InputEvent.BUTTON1_DOWN_MASK;
				bot.mousePress(mask);  
				bot.mouseRelease(mask);  
			} catch (AWTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
   
		}
		pipe.pump();
		
		
		
		//this.world.getAmoeba().request(request(requestPosition));
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
	public double normalizePositiveValues(double upperBound, double dispersion, double value) {
		return upperBound*2*(- 0.5 + 1/(1+Math.exp(-value/dispersion)));
	}
	

}