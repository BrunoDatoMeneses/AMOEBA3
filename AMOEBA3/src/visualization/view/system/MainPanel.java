package visualization.view.system;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import mas.kernel.AMOEBA;
import mas.kernel.Config;
import mas.kernel.World;
import visualization.log.ConsolePanel;
import visualization.log.LogForm;
import visualization.observation.Observation;
import mas.agents.percept.Percept;
import mas.agents.context.Context;
import mas.agents.head.Head;
import mas.blackbox.BlackBox;
import visualization.graphView.TemporalGraph;

// TODO: Auto-generated Javadoc
/**
 * The Class MainPanel.
 */
public class MainPanel extends JPanel{
	
	
	/** The tool bar. */
	/* ----ToolBar Components----*/
	private JToolBar toolBar;
	
	/** The button pause start. */
	private JButton buttonPauseStart;
	
	/** The button play one step. */
	private JButton buttonPlayOneStep;
	
	/** The button exit. */
	private JButton buttonExit;
	
	/** The button close GUI. */
	private JButton buttonCloseGUI;
	
	/** The button plus verbosity. */
	private JButton buttonPlusVerbosity;
	
	/** The button minus verbosity. */
	private JButton buttonMinusVerbosity;
	
	/** The button log. */
	private JButton buttonLog;
	
	/** The button contexts to CSV. */
	private JButton buttonContextsToCSV;
	
	/** The button serialize. */
	private JButton buttonSerialize;
	
	/** The button control. */
	private JButton buttonControl;
	
	/** The icon ne O campus. */
	private JLabel iconNeOCampus;
	
	/** The button show ctrl. */
	private JButton buttonShowCtrl;
	
	/** The button oracle. */
	private JButton buttonOracle;
	
	/** The csv check box. */
	private JCheckBox csvCheckBox;
	
	/** The remember state check box. */
	private JCheckBox rememberStateCheckBox;
	
	/** The tabbed panel. */
	private MainTabbedPanel tabbedPanel;
	
	/** The world. */
	private World world;
	
	/** The temporal graph. */
	private TemporalGraph temporalGraph;

	/** The amoeba. */
	private AMOEBA amoeba;
	
	/** The minimal display. */
	private boolean minimalDisplay = false;
	
	/** The log form. */
	private LogForm logForm;
	
	/** The remember state. */
	boolean rememberState = false;
	
	/** The generate csv file. */
	boolean generateCsvFile = false;
	
	/** The first get value check box. */
	private boolean firstGetValueCheckBox = false;
	
	/**
	 * Instantiates a new main panel.
	 *
	 * @param minimalDisplay the minimal display
	 */
	public MainPanel (boolean minimalDisplay) {
		
		//Use the advanced graph viewer from GraphStream
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		setSystemLookAndFeel();
		this.minimalDisplay = minimalDisplay;
		this.setLayout(new BorderLayout());
		this.addToolBar();
		this.add(toolBar,BorderLayout.NORTH);
		
		if (!minimalDisplay){
			addTabbedPanel();
		}
		
	}
	
	/**
	 * Instantiates a new main panel.
	 */
	public MainPanel() {
		
		//Use the advanced graph viewer from GraphStream
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		setSystemLookAndFeel();
		this.setLayout(new BorderLayout());

		this.addToolBar();
		this.add(toolBar,BorderLayout.NORTH);
		addTabbedPanel();
		
		
	}
	
	/**
	 * Sets the system look and feel.
	 */
	private static void setSystemLookAndFeel() {

		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException e) {
			// handle exception
		}
		catch (ClassNotFoundException e) {
			// handle exception
		}
		catch (InstantiationException e) {
			// handle exception
		}
		catch (IllegalAccessException e) {
			// handle exception
		}

	}
	
	/**
	 * Adds the tabbed panel.
	 */
	private void addTabbedPanel() {
		this.tabbedPanel = new MainTabbedPanel();
		this.add(tabbedPanel,BorderLayout.CENTER);
	}
	
	/**
	 * Adds the tool bar.
	 */
	private void addToolBar() {
		toolBar = new JToolBar();
		
		iconNeOCampus = new JLabel(Config.getIcon("neOCampus.png"));
		toolBar.add(iconNeOCampus);

		toolBar.addSeparator();

		buttonExit = new JButton(Config.getIcon("cross-circle.png"));
		buttonExit.addActionListener(e -> {System.exit(0);});
		buttonExit.setToolTipText("Exit");
		toolBar.add(buttonExit);
		
		buttonCloseGUI = new JButton(Config.getIcon("cross.png"));
		buttonCloseGUI.addActionListener(e -> {closeGUI();});
		buttonCloseGUI.setToolTipText("Close GUI");
		toolBar.add(buttonCloseGUI);
		
		toolBar.addSeparator();
		
		buttonPauseStart = new JButton(Config.getIcon("control.png"));
		buttonPauseStart.addActionListener(e -> {getValueCheckBox(); togglePause(!world.getScheduler().isRunning());});
		buttonPauseStart.setToolTipText("Pause/Start");
		toolBar.add(buttonPauseStart);

		buttonPlayOneStep = new JButton(Config.getIcon("control-stop.png"));
		buttonPlayOneStep.addActionListener(e -> {getValueCheckBox(); oneStep();});
		buttonPlayOneStep.setToolTipText("Play one step");
		toolBar.add(buttonPlayOneStep);
		
		toolBar.addSeparator();

		buttonMinusVerbosity = new JButton(Config.getIcon("terminal--minus.png"));
		buttonMinusVerbosity.addActionListener(e -> {Config.changeVerbosity(-1);});
		buttonMinusVerbosity.setToolTipText("Reduce verbosity");
		toolBar.add(buttonMinusVerbosity);
		
		buttonPlusVerbosity = new JButton(Config.getIcon("terminal--plus.png"));
		buttonPlusVerbosity.addActionListener(e -> {Config.changeVerbosity(+1);});
		buttonPlusVerbosity.setToolTipText("Augment verbosity");
		toolBar.add(buttonPlusVerbosity);
		
		buttonLog = new JButton(Config.getIcon("terminal.png"));
		buttonLog.addActionListener(e -> {generateLog();});
		buttonLog.setToolTipText("Generate .log file");
		toolBar.add(buttonLog);

		buttonContextsToCSV = new JButton(Config.getIcon("document.png"));
		buttonContextsToCSV.addActionListener(e -> {generateCSV();});
		buttonContextsToCSV.setToolTipText("Generate .csv file with context ranges");
		toolBar.add(buttonContextsToCSV);
		
		toolBar.addSeparator();

		buttonSerialize = new JButton(Config.getIcon("document.png"));
		buttonSerialize.addActionListener(e -> {serialize();});
		buttonSerialize.setToolTipText("Serialize");
		toolBar.add(buttonSerialize);
		
		toolBar.addSeparator();

		buttonControl = new JButton(Config.getIcon("hammer.png"));
		buttonControl.addActionListener(e -> {changeControl();});
		buttonControl.setToolTipText("Allow control if possible");
		toolBar.add(buttonControl);
		
		buttonShowCtrl = new JButton(Config.getIcon("bug.png"));
		buttonShowCtrl.addActionListener(e -> {startPanelController(world.getScheduler().getHeadAgent());});
		buttonShowCtrl.setToolTipText("Show controller informations");
		toolBar.add(buttonShowCtrl);
		
		buttonOracle = new JButton(Config.getIcon("compass.png"));
		buttonOracle.addActionListener(e -> {changeOracleConnection();});
		buttonOracle.setToolTipText("Disconnect or connect the oracle");
		toolBar.add(buttonOracle);
		
		toolBar.addSeparator();
		
		csvCheckBox = new JCheckBox("CSV File");
		csvCheckBox.setSelected(true);
		toolBar.add(csvCheckBox);
		
		rememberStateCheckBox = new JCheckBox("Remember State");
		rememberStateCheckBox.setSelected(false);
		toolBar.add(rememberStateCheckBox);
		
	}

	
	/**
	 * Change oracle connection.
	 */
	private void changeOracleConnection() {
		world.changeOracleConnection();
		
	}
	 
	/**
	 * Change control.
	 */
	private void changeControl() {
		System.out.println("changeControl...");
		amoeba.changeControl();
	}
	
	/**
	 * Generate log.
	 */
	private void generateLog() {	
		System.out.println("Generate log file...");
		logForm = new LogForm();
		System.out.println(world.getAmoeba());
		logForm.setLogFile(world.getAmoeba().getLogFile());
		logForm.createLogForm();
		world.getAmoeba().getLogFile().setConsolePanel(getConsolePanel());
	}

	/**
	 * Gets the value check box.
	 *
	 * @return the value check box
	 */
	private void getValueCheckBox() {
		if(!firstGetValueCheckBox) {
			// CSV CheckBox
			if(csvCheckBox.isSelected()) {
				setGenerateCsv(true);
			} else {
				setGenerateCsv(false);
			}
			csvCheckBox.setEnabled(false);
			// Remember State CheckBox
			if(rememberStateCheckBox.isSelected()) {
				setRememberState(true);
			} else {
				setRememberState(false);
			}
			rememberStateCheckBox.setEnabled(false);
			firstGetValueCheckBox = true;
		}
	}

	/**
	 * Serialize.
	 */
	private void serialize() {
		System.out.println("serialize...");
		final JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Serilaization file (.amo)", "amo");
		fc.setFileFilter(filter);
		fc.setSelectedFile(new File("serialisation.amo"));
		int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            System.out.println(file);
           
            world.setStartSerialization(true, file);
            if (world.isStartSerialization()) {
				world.getScheduler().serialize();
				world.setStartSerialization(false, null);
				Config.print("End of the serialization", -100);
			}
        }  
	}


	/**
	 * Generate CSV.
	 */
	private void generateCSV() {
		System.out.println("Generate new CSV file" + System.getProperty("user.dir"));
		
		String name = "contexts.csv";
		
		PrintWriter out;
		try {
			//out = new PrintWriter(new FileWriter(System.getProperty("user.dir")+"/bin/view/system/nDim/"+name));
			out = new PrintWriter(new FileWriter("tmp/"+name));
			
			out.print("Name");				

			ArrayList<Percept> var = world.getAllPercept();
			for (Percept v : var) {
				out.print(",");
				out.print(v.getName());				
			}
			out.println();
			
			ArrayList<Context> contexts = (ArrayList<Context>) world
					.getAllAgentInstanceOf(Context.class);
			for (Context v : contexts) {
				out.print("\"");				
				out.print(v.getName());				
				out.print("\"");				

				for (Percept p : var) {
					out.print(",");
					out.print((float)v.getRanges().get(p).getStart());
				}
				
				out.println();
				
				out.print("\"");				
				out.print(v.getName());				
				out.print("\"");				

				for (Percept p : var) {
					out.print(",");
					out.print((float)v.getRanges().get(p).getEnd());
				}
				
				out.println();
			}
			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		tabbedPanel.updateNDimension();

	}

	/**
	 * Close the GUI.
	 */
	public void closeGUI() {
		((JFrame) (this.getTopLevelAncestor())).dispose();
	}
	
	/**
	 * Play one step of simulation.
	 * Toggle pause off if running after one step.
	 */
	public void oneStep() {
		togglePause(false);
		amoeba.playOneStep();
	}
	
	/**
	 * Pause/Unpause the simulation and adapt the button accordingly.
	 *
	 * @param newState the new state
	 */
	public void togglePause(boolean newState) {
		amoeba.setRunning(newState);
		boolean running = amoeba.isRunning();
		if (!running) {
			buttonPauseStart.setIcon(Config.getIcon("control.png"));
		} else {
			buttonPauseStart.setIcon(Config.getIcon("control-pause.png"));
		}
	}
	
	/**
	 * Start panel controller.
	 *
	 * @param h the h
	 */
	private void startPanelController(Head h) {
		
		PanelController pan = new PanelController(h, world);
		JFrame frame = new JFrame(">>Controller<<");
		JScrollPane scrollPane = new JScrollPane(pan,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		world.getScheduler().addScheduledItem(pan);
        frame.setAlwaysOnTop(true);
		frame.setContentPane(scrollPane);
		frame.setVisible(true);
		frame.pack();
	}

	
	/**
	 * Sets the world.
	 *
	 * @param world the new world
	 */
	public void setWorld(World world) {
		this.world = world;
		world.setAmoeba(amoeba);
		world.getAmoeba().getLogFile().setWorld(world);
		if (!minimalDisplay) tabbedPanel.setWorld(world);

	}
	
	/**
	 * Sets the temporal graph.
	 *
	 * @param temporalGraph the new temporal graph
	 */
	public void setTemporalGraph(TemporalGraph temporalGraph) {
		world.getAmoeba().getLogFile().setTemporalGraph(temporalGraph);
		this.temporalGraph = temporalGraph;
		if (!minimalDisplay) tabbedPanel.setTemporalGraph(temporalGraph);
	}

	
	/**
	 * Sets the black box.
	 *
	 * @param blackBox the new black box
	 */
	public void setBlackBox(BlackBox blackBox) {
		if (!minimalDisplay) tabbedPanel.setBlackBox(blackBox);
	}
	
	/**
	 * Update.
	 */
	public void update() {
		if (!minimalDisplay) tabbedPanel.update();
	}

	/**
	 * Sets the amoeba.
	 *
	 * @param amoeba the new amoeba
	 */
	public void setAMOEBA(AMOEBA amoeba) {
		this.amoeba = amoeba;
		
	}

	/**
	 * Sets the visualization.
	 *
	 * @param obsList the new visualization
	 */
	// Set the visualization after serialization
	public void setVisualization(ArrayList<Observation> obsList) {
		if (!minimalDisplay) tabbedPanel.setVisualization(obsList);
	}
	
	/**
	 * Gets the console panel.
	 *
	 * @return the console panel
	 */
	// Get console panel
	public ConsolePanel getConsolePanel() {
		return tabbedPanel.getConsolePanel();
	}
	
	/**
	 * Disable check box remember state.
	 *
	 * @param rememberState the remember state
	 */
	// Disable CheckBox Remember State  
	public void disableCheckBoxRememberState(boolean rememberState) {
		setRememberState(rememberState);
		rememberStateCheckBox.setSelected(rememberState);
		rememberStateCheckBox.setEnabled(false);
	}
		
	/**
	 * Sets the remember state.
	 *
	 * @param rememberState the new remember state
	 */
	// Set boolean whether to remember previous states or not
	public void setRememberState(boolean rememberState) {
		this.rememberState = rememberState;
		if (!minimalDisplay) tabbedPanel.setRememberState(rememberState);
	}
	
	/**
	 * Gets the remember state.
	 *
	 * @return the remember state
	 */
	// Get the value of remember state
	public boolean getRememberState() {
		return rememberState;
	}
	
	/**
	 * Sets the generate csv.
	 *
	 * @param generateCsvFile the new generate csv
	 */
	// Set boolean whether to generate .csv file 
	public void setGenerateCsv(boolean generateCsvFile) {
		this.generateCsvFile = generateCsvFile;
		amoeba.setGenerateCSV(generateCsvFile);
	}
	
	/**
	 * Disable check box generate csv.
	 *
	 * @param generateCsvFile the generate csv file
	 */
	public void disableCheckBoxGenerateCsv(boolean generateCsvFile) {
		//setGenerateCsv(generateCsvFile);
		this.generateCsvFile = generateCsvFile;
		csvCheckBox.setSelected(generateCsvFile);
		csvCheckBox.setEnabled(false);
		
	}
	
	/**
	 * Gets the observation list.
	 *
	 * @return the observation list
	 */
	// Get Observation List
	public ArrayList<Observation> getObservationList() {
		return tabbedPanel.getObservationList();
	}
	
	public MainTabbedPanel getTabbedPanel() {
		return tabbedPanel;
	}

	

}