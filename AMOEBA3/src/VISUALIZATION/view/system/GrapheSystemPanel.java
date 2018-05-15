package VISUALIZATION.view.system;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import MAS.kernel.Config;
import MAS.kernel.World;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import VISUALIZATION.view.system.paving.Panel1DPaving;
import MAS.agents.Agent;
import MAS.agents.Percept;
import MAS.agents.SystemAgent;
import MAS.agents.context.Context;

// TODO: Auto-generated Javadoc
/**
 * The Class GrapheSystemPanel.
 */
public class GrapheSystemPanel extends JPanel implements ViewerListener, MouseInputListener{
	
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

	/** The view mode. */
	private int viewMode = 0;
	
	/** The mouse event. */
	private MouseEvent mouseEvent;


	/**
	 * Instantiates a new graphe system panel.
	 */
	public GrapheSystemPanel() {
		setLayout(new BorderLayout());

		this.setMinimumSize(new Dimension(400,400));

		toolBar = new JToolBar(null, JToolBar.VERTICAL);

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
		
		this.add(toolBar,BorderLayout.WEST);
		
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
	 * Sets the standard style.
	 */
	public void setStandardStyle() {
		graph.removeAttribute("ui.stylesheet");
		//graph.addAttribute("ui.stylesheet", "url('file:"+System.getProperty("user.dir")+"/bin/styles/styleSystem.css')");
		graph.addAttribute("ui.stylesheet", "url('" + this.getClass().getClassLoader().getResource("src/styles/styleSystem.css") + "')");
		//"url('file:"+System.getProperty("user.dir")+"/bin/styles/styleSystemSoft.css')"
	}
	
	/**
	 * Sets the dark style.
	 */
	public void setDarkStyle() {
		graph.removeAttribute("ui.stylesheet");
		//graph.addAttribute("ui.stylesheet", "url('file:"+System.getProperty("user.dir")+"/bin/styles/styleSystemDark.css')");
		graph.addAttribute("ui.stylesheet", "url('" + this.getClass().getClassLoader().getResource("src/styles/styleSystemDark.css") + "')");
	}
	
	/**
	 * Sets the soft style.
	 */
	public void setSoftStyle() {
		graph.removeAttribute("ui.stylesheet");
		//graph.addAttribute("ui.stylesheet", "url('file:"+System.getProperty("user.dir")+"/bin/styles/styleSystemSoft.css')");
		graph.addAttribute("ui.stylesheet", "url('" + this.getClass().getClassLoader().getResource("src/styles/styleSystemSoft.css") + "')");
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
	 * Update.
	 */
	public void update () {
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
		
		//graph.clear();
		for (String name : world.getAgents().keySet()) {
			SystemAgent a = world.getAgents().get(name);
			
			if (graph.getNode(name) != null) {
				//do nothing
			} else {
				graph.addNode(name);
				graph.getNode(name).addAttribute("ui.class", a.getClass().getSimpleName());
				graph.getNode(name).addAttribute("ui.label", a.getName());
			}
			

			graph.getNode(name).addAttribute("EXIST", true);

		}

		//Draw edge
		for (String name : world.getAgents().keySet()) {
			SystemAgent a = world.getAgents().get(name);

			for (Agent target : a.getTargets()) {
				String fullname = name + " " + target.getName();
				if (graph.getEdge(fullname) == null) {
					graph.addEdge(fullname, a.getName(), target.getName(), true);				
				}
				graph.getEdge(fullname).addAttribute("EXIST", true);
				

			}

		}
		
		for (Node node : graph) {
			if (node.hasAttribute("EXIST")) {
				node.removeAttribute("EXIST");
			} else {
				graph.removeNode(node);
			}
		}
		
		for (Edge edge : graph.getEachEdge()) {
			if (edge.hasAttribute("EXIST")) {
				edge.removeAttribute("EXIST");
			} else {
				graph.removeEdge(edge);
			}
		}
		
	}
	
	
	/**
	 * Creates the graph.
	 */
	private void createGraph() {
		System.out.println("Create graph system");
		graph = new SingleGraph("SYSTEM");
		for (String name : world.getAgents().keySet()) {
			SystemAgent a = world.getAgents().get(name);
			//graph.addAttribute("ui.stylesheet", "url('file:"+System.getProperty("user.dir")+"/bin/styles/styleSystem.css')");
			graph.addAttribute("ui.stylesheet", "url('" + this.getClass().getClassLoader().getResource("styles/styleSystem.css") + "')");
			
		/*	graph.addAttribute("ui.stylesheet", "node { stroke-mode: plain;"
					+ "fill-color: red;"
					+ "shape: box;"
					+ "stroke-color: yellow;"
					+ " }");//text-mode
			graph.addAttribute("ui.style", "shape: box;");*/

			//graph.addAttribute("ui.stylesheet", "node { text-mode: normal; }");//text-mode

			//graph.
			graph.addNode(a.getName());
			graph.getNode(a.getName()).addAttribute("ui.class", a.getClass().getSimpleName());
			graph.getNode(a.getName()).addAttribute("ui.label", a.getName());
			//graph.getNode(bba.getName()).;
			//System.out.println(graph.getNode(bba.getName()).getLabe)());
			
		}

		//Draw edge
		for (String name : world.getAgents().keySet()) {
			SystemAgent a = world.getAgents().get(name);

			for (Agent target : a.getTargets()) {
				graph.addEdge(a.getName() + " " + target.getName(), a.getName(), target.getName(), true);				
			}
			
		}

		/*If we want to improve perf on display...*/
		//graph.addAttribute("layout.stabilization-limit", 0.85);
		
		viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.addDefaultView(false);
		viewer.enableAutoLayout();
		viewer.getDefaultView().addMouseListener(this);
		

		pipe = viewer.newViewerPipe();
        pipe.addViewerListener(this);
        pipe.addSink(graph);

		viewer.getDefaultView().setMinimumSize(new Dimension(400,400));
		this.add(viewer.getDefaultView(),BorderLayout.CENTER);
		
		setDarkStyle();
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
		System.out.println("node pushed : " + id);
		
		System.out.println(world.getAgents().get(id).toString());
		
		if (rightClick) {
			if (world.getAgents().get(id) instanceof Percept) {
				popupPercept(id);
			}
			
			rightClick = false;
		}


		
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
