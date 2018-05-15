package VISUALIZATION.view.system.twoDim;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.dnd.Autoscroll;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
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

import MAS.kernel.Config;
import MAS.kernel.Scheduler;
import MAS.kernel.World;
import VISUALIZATION.log.LogMessageType;
import VISUALIZATION.observation.Observation;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import VISUALIZATION.view.system.PanelController;
import VISUALIZATION.view.system.paving.Panel1DPaving;
import VISUALIZATION.view.system.projection.PanelProjection;
import MAS.agents.Agent;
import MAS.agents.Percept;
import MAS.agents.SystemAgent;
import MAS.agents.context.Context;
import MAS.agents.context.Range;
import MAS.agents.head.Head;
import VISUALIZATION.graphView.GraphicVisualization2Dim;
import VISUALIZATION.graphView.GraphicVisualizationNDim;
import VISUALIZATION.graphView.TemporalGraph;

// TODO: Auto-generated Javadoc
/**
 * The Class GrapheTwoDimPanelStandard.
 */
public class GrapheTwoDimPanelStandard extends JPanel implements ViewerListener, MouseInputListener{
	
	/** The graph. */
	Graph graph;
	
	/** The viewer. */
	Viewer viewer;
	
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

	/** The view mode. */
	private int viewMode = 0;
	
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

		buttonShowDefault = new JButton(Config.getIcon("tag--plus.png"));
		buttonShowDefault.addActionListener(e -> {showDefault();});
		toolBar.add(buttonShowDefault);

		buttonShowValue = new JButton(Config.getIcon("tag--exclamation.png"));
		buttonShowValue.addActionListener(e -> {showValue();});
		toolBar.add(buttonShowValue);

		buttonShowName = new JButton(Config.getIcon("tag.png"));
		buttonShowName.addActionListener(e -> {showName();});
		toolBar.add(buttonShowName);
		
		toolBar.addSeparator();
		
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
		toolBar.add(comboDimX);
		toolBar.add(comboDimY);
		
		xValue = new JLabel();
		yValue = new JLabel();
		
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
			setTextAreaInfo(currentId);
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
		graph.addAttribute("ui.stylesheet", "url('" + this.getClass().getClassLoader().getResource("src/VISUALIZATION/styles/styleSystemScalable.css") + "')");
	}
	
	/**
	 * Sets the dark style.
	 */
	public void setDarkStyle() {
		graph.removeAttribute("ui.stylesheet");
		//graph.addAttribute("ui.stylesheet", "url('file:"+System.getProperty("user.dir")+"/bin/styles/styleSystemDark.css')");
		graph.addAttribute("ui.stylesheet", "url('" + this.getClass().getClassLoader().getResource("src/VISUALIZATION/styles/styleSystemDark.css") + "')");
	}
	
	/**
	 * Sets the soft style.
	 */
	public void setSoftStyle() {
		graph.removeAttribute("ui.stylesheet");
		//graph.addAttribute("ui.stylesheet", "url('file:"+System.getProperty("user.dir")+"/bin/styles/styleSystemSoft.css')");
		graph.addAttribute("ui.stylesheet", "url('" + this.getClass().getClassLoader().getResource("src/VISUALIZATION/styles/styleSystemSoft.css") + "')");
	}

	
	/**
	 * Destroy context.
	 */
	public void destroyContext() {
		world.destroy(Context.class);  //TODO
	}
	
	/**
	 * Show value.
	 */
	public void showValue() {
		viewMode = 1;
		if (world != null) {
//			for (String name : blackBox.getBlackBoxAgents().keySet()) {
//				BlackBoxAgent bba = blackBox.getBlackBoxAgents().get(name);	
//				graph.getNode(bba.getName()).setAttribute("ui.label", bba.getValue());		
//			}
		}
	}
	
	/**
	 * Show default.
	 */
	public void showDefault() {
		viewMode = 0;
		if (world != null) {
//			for (String name : blackBox.getBlackBoxAgents().keySet()) {
//				BlackBoxAgent bba = blackBox.getBlackBoxAgents().get(name);	
//				graph.getNode(bba.getName()).setAttribute("ui.label", bba.getName() + " " + bba.getValue());		
//			}
		}
	}
	
	/**
	 * Show name.
	 */
	public void showName() {
		viewMode = 2;
		if (world != null) {
//			for (String name : blackBox.getBlackBoxAgents().keySet()) {
//				BlackBoxAgent bba = blackBox.getBlackBoxAgents().get(name);	
//				graph.getNode(bba.getName()).setAttribute("ui.label", bba.getName());		
//			}
		}
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
		ArrayList<Percept> perceptList = world.getAllPercept();
		
		for (int i=0; i<temporalGraph.get2DimGraphList().size(); i++) {
			String contextID = temporalGraph.get2DimGraphList().get(i).getContextID();
			if (world.getAgents().get(contextID) != null) {
				Context c = (Context) world.getAgents().get(contextID);
				for (int j=0; j<perceptList.size(); j++) {
					double min = c.getRanges().get(perceptList.get(j)).getEnd();
					double max = c.getRanges().get(perceptList.get(j)).getStart();
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
			info = info.concat(world.getAgents().get(currentId).toString());
			info = info.replace("Current", "\nCurrent");
			info = info.replace("AVT", "\nAVT");
			
		} else {
			info = "No context";
		}
		
		textarea.setText(info);
		/* End of update slider */
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
		
		switch(viewMode) {
		
		case 0 : 
			showDefault();
			break;
			
		case 1 : 
			showValue();
			break;
			
		case 2 :
			showName();
			break;
		}
		
		//TODO
		xValue.setText(   String.valueOf( ( (((Percept) world.getAgents().get(comboDimX.getSelectedItem())).getValue()  )  )));
		yValue.setText(   String.valueOf( ( (((Percept) world.getAgents().get(comboDimY.getSelectedItem())).getValue()  )  )));

		/* Create and store the element tick of observation */
		Observation obsEle = new Observation();
		// Set tick for the element of observation
		if (rememberState) {
			obsEle.setTick(world.getScheduler().getTick());
		}
		
		// Store values into array list of percept of observation
		ArrayList<Percept> perceptList = world.getAllPercept();
		
		for(int i=0; i< perceptList.size(); i++) {
			if (rememberState) {
				Percept p = new Percept(perceptList.get(i));
				obsEle.addPerceptList(p);
			}	
		}
		
		for (String name : world.getAgents().keySet()) {
			SystemAgent a = world.getAgents().get(name);
			if (a instanceof Context) {
				Context n = (Context)a;
				// Store values into array list of context of observation
				if (rememberState) {
					obsEle.addContextList(new Context(n));
				}	
				
				Node node;
				if (graph.getNode(name) != null) {
					node = graph.getNode(name);
				} else {
					graph.addNode(name);
					node = graph.getNode(name);
					node.addAttribute("ui.class",
							a.getClass().getSimpleName());
					node.addAttribute("ui.label", a.getName());
				}

				node.addAttribute("EXIST", true);
				if (n.getRanges().size() > 0){
				//	System.out.println(n.getRanges().get(world.getAgents().get("Sensor")).getStart() + " " + n.getRanges().get(world.getAgents().get("SensorPerturbation")).getStart());
					
					double lengthX = n.getRanges().get(world.getAgents().get(comboDimX.getSelectedItem())).getEnd() 
							- n.getRanges().get(world.getAgents().get(comboDimX.getSelectedItem())).getStart();
					double lengthY = n.getRanges().get(world.getAgents().get(comboDimY.getSelectedItem())).getEnd() 
							- n.getRanges().get(world.getAgents().get(comboDimY.getSelectedItem())).getStart();
					node.setAttribute("xyz", n.getRanges().get(world.getAgents().get(comboDimX.getSelectedItem())).getStart() + (0.5*lengthX),
							n.getRanges().get(world.getAgents().get(comboDimY.getSelectedItem())).getStart() + (0.5*lengthY), 0);
					
				//	node.setAttribute("xyz", n.getRanges().get(world.getAgents().get("Sensor")).getValue(), n.getRanges().get(world.getAgents().get("SensorPerturbation")).getValue(), 0);
				//	node.addAttribute("ui.size", "8px");
					node.addAttribute("ui.style", "size: " + doubleFormat.format(lengthX) + "gu, " + doubleFormat.format(lengthY) +"gu;");
				}
				if (n.isBestContext()) {
					node.addAttribute("ui.class","BestContextSelected");				
				} else if (n.getNSelection() > 0) {
					node.addAttribute("ui.class","ContextAwaked");
					if (n.getNSelection() == 3) {
						node.addAttribute("ui.class","ContextSelected");				
					}
				}
	
				else {
					node.addAttribute("ui.class","Context");
				}


	/*			else if (n.getImpact() < 0) {
					node.addAttribute("ui.class",a.getClass().getSimpleName());				
				}
				else if (n.getImpact() == 0) {
					node.addAttribute("ui.class","stableNodexte");				
				} 
				else 
				{
					node.addAttribute("ui.class","NodexteBadImpact");				
				}*/

				//TODO
				n.setnSelection(0);
			}
			
			if (a instanceof Head) {
				Head n = (Head)a;
				
				controller = n; //TODO dirty
				
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
				
			}
			
		}
		
		if (rememberState) {
			obsList.add(obsEle);
		}

		//Draw edge
/*		for (String name : world.getAgents().keySet()) {
			SystemAgent a = world.getAgents().get(name);

			for (Agent target : a.getTargets()) {
				String fullname = name + " " + target.getName();
				if (graph.getEdge(fullname) == null) {
					graph.addEdge(fullname, a.getName(), target.getName(), true);				
				}
				graph.getEdge(fullname).addAttribute("EXIST", true);
				

				if (a instanceof Nodexte) {
					if ( ((Nodexte) a).getProposalTrust() > 0) {
						graph.getEdge(fullname).addAttribute("ui.class", "validContext");
						graph.getEdge(fullname).addAttribute("layout.weight", 0.5);
					}
					else {
						graph.getEdge(fullname).removeAttribute("ui.class");
						graph.getEdge(fullname).addAttribute("layout.weight", 1);
					}
				}
			}

		}*/
		
		for (Node node : graph) {
			if (node.hasAttribute("EXIST")) {
				node.removeAttribute("EXIST");
			} else {
				graph.removeNode(node);
			}
		}
		/*
		for (Edge edge : graph.getEachEdge()) {
			if (edge.hasAttribute("EXIST")) {
				edge.removeAttribute("EXIST");
			} else {
				graph.removeEdge(edge);
			}
		}*/
		if (colorIsDynamic) {
			
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
				}

			}
			
			

		}
		
		//---------------------------------------------------Add scale----------------------------------------------------
	//	if (graph.getNode("scale") == null ) {
	//		graph.addNode("scale");
	//		Node sc = graph.getNode("scale");
	//		sc.addAttribute("ui.class", "scale");
	//		sc.setAttribute("xyz", 0	,0, 100);
	//	}
		
//		int tick = world.getScheduler().getTick();
//		if (tick % 500 == 0 && tick > 0) {
//			graph.addAttribute("ui.screenshot", "'/home/nigon/Téléchargements/screen" + tick +".png");
//		}

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
		this.add(viewer.getDefaultView(),BorderLayout.CENTER);
		setStandardStyle();
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
		currentId = id;
		System.out.println("node pushed : " + id);
		//String info = world.getAgents().get(id).toString();
		//System.out.println(info);
		
		//JOptionPane.showMessageDialog(this, info, "Context : " + id, JOptionPane.PLAIN_MESSAGE);
		if (currentId != null) {
			setTextAreaInfo(currentId);
			
			if (sliderValue == currentTick || (!rememberState)) {
				if (rightClick) {
					popupMenuForVisualization(currentId);
					rightClick = false;
				}
			}
			
		}


/*		Node node = graph.getNode(id);
		System.out.println(node.getAttribute("xyz").toString());
		Context n = (Context) world.getAgents().get(id);*/
	
	}
	
	/**
	 * Sets the text area info.
	 *
	 * @param id the new text area info
	 */
	private void setTextAreaInfo(String id) {
		System.out.println("node pushed : " + id);
		
		String info = "";
		if (rememberState) {
			Observation o = getObservationByTick(sliderValue);
			if (o != null) {
				Context c = o.getContextById(id);
				if ( c != null ) {
					info = "State :" + sliderValue + "\n";
					info = info.concat(o.getContextById(id).toString());
					info = info.replace("Current", "\nCurrent");
					info = info.replace("AVT", "\nAVT");
				} else {
					info = "No context";
				}
				
			} 
			
		} else {
			info = "State :" + currentTick + "\n";
			info = info.concat(world.getAgents().get(id).toString());
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
		JMenuItem itemShow2Dim = new JMenuItem("History of grapgh in 2 Dim");
		itemShow2Dim.addActionListener(e -> {popupVisualization2Dim(id);});
	    JMenuItem itemShowNDim = new JMenuItem("Graph Visualization in N Dim");
	    itemShowNDim.addActionListener(e -> {popupVisualizationNDim(id);});
	    JMenuItem itemShowAll = new JMenuItem("Both");
	    itemShowAll.addActionListener( e -> {popupVisualization2Dim(id); popupVisualizationNDim(id);});
	    popup.add(itemShow2Dim);
	    popup.add(itemShowNDim);
	    popup.add(itemShowAll);
	    popup.show(this, this.getX() + mouseEvent.getX(), this.getY() + mouseEvent.getY());
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
		if(SwingUtilities.isRightMouseButton(e)){
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
}