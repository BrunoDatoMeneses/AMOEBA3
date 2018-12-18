package visualization.view.system;

import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.jfree.data.xy.XYSeries;

import mas.kernel.World;
import visualization.log.ConsolePanel;
import visualization.observation.Observation;
import visualization.view.global.PanelChart;
import visualization.view.global.PanelChart2;
import visualization.view.global.PanelOneChart;
import visualization.view.global.ScatterPlotExample;
import visualization.view.global.PanelExoVSEndo;
import visualization.view.system.nDim.PanelParallelCoordinates;
import visualization.view.system.twoDim.GrapheTwoDimPanelStandard;
import visualization.graphView.TemporalGraph;

// TODO: Auto-generated Javadoc
/**
 * The Class MainTabbedPanel.
 */
public class MainTabbedPanel extends JTabbedPane{

	/** The world. */
	private World world;
	
	/** The black box panel. */
	//private BlackBoxPanel blackBoxPanel;
	
	/** The system panel. */
	private SystemPanel systemPanel;
	
	/** The panel chart. */
	private PanelChart panelChart;
	
	/** The panel two dim standard. */
	private GrapheTwoDimPanelStandard panelTwoDimStandard;
	
	/** The panel parallel coordinates. */
	private PanelParallelCoordinates panelParallelCoordinates;
	
	/** The console panel. */
	private ConsolePanel consolePanel;


	/**
	 * Instantiates a new main tabbed panel.
	 */
	public MainTabbedPanel() {
		super();

	}

	/**
	 * Gets the world.
	 *
	 * @return the world
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Sets the world.
	 *
	 * @param world the new world
	 */
	public void setWorld(World world) {
		this.world = world;
		

		
		
		//blackBoxPanel = new BlackBoxPanel(world);
		//systemPanel = new SystemPanel(world);
		panelChart = new PanelChart(world);
		//panelTwoDim = new GrapheTwoDimPanel(world);
		panelTwoDimStandard = new GrapheTwoDimPanelStandard(world);
		panelParallelCoordinates = new PanelParallelCoordinates(world);
		consolePanel = new ConsolePanel();
		

		
		
		
		
		
		world.getScheduler().addScheduledItem(panelChart);
		
		//this.addTab("BlackBox", new JScrollPane(blackBoxPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		//this.addTab("System", systemPanel);
		this.addTab("Charts", new JScrollPane(panelChart,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		this.addTab("2-Dimensions", new JScrollPane(panelTwoDimStandard,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		this.addTab("N-Dimensions", new JScrollPane(panelParallelCoordinates,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
	//	this.addTab("TwoDim", new JScrollPane(panelTwoDim,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
	//	this.addTab("TwoDim", panelTwoDim);
		this.addTab("Console", new JScrollPane(consolePanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		
		
		((Frame) this.getTopLevelAncestor()).pack();
	}
	
	/**
	 * Update.
	 */
	public void update() {
		//blackBoxPanel.update();
	//	systemPanel.update();
	//	panelTwoDim.update();
		panelTwoDimStandard.update();
		world.getScheduler().setWaitForGUIUpdate(false);
	}
	


	/**
	 * Update N dimension.
	 */
	public void updateNDimension() {
		panelParallelCoordinates.update();		
	}
	
	/**
	 * Sets the remember state.
	 *
	 * @param rememberState the new remember state
	 */
	// Set the boolean whether to remember previous states or not
	public void setRememberState(boolean rememberState) {
		panelTwoDimStandard.setRememberState(rememberState);
	}

	/**
	 * Sets the visualization.
	 *
	 * @param obsList the new visualization
	 */
	// Set the visualization after serialization
	public void setVisualization(ArrayList<Observation> obsList) {
		panelTwoDimStandard.setVisualization(obsList);
	}
	
	/**
	 * Sets the temporal graph.
	 *
	 * @param temporalGraph the new temporal graph
	 */
	// Set the TemporalGraph for the visualization of 2DimGraph and NDimGraph
	public void setTemporalGraph(TemporalGraph temporalGraph) {
		panelTwoDimStandard.setTemporalGraph(temporalGraph);
	}

	/**
	 * Gets the console panel.
	 *
	 * @return the console panel
	 */
	// Get Console Panel
	public ConsolePanel getConsolePanel() {
		return consolePanel;
	}
	
	/**
	 * Gets the observation list.
	 *
	 * @return the observation list
	 */
	// Get Observation List
	public ArrayList<Observation> getObservationList() {
		return panelTwoDimStandard.getObservationList();
	}
	
	public GrapheTwoDimPanelStandard getPanelTwoDimStandard() {
		return panelTwoDimStandard;
	}
	
}