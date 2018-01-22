package view.blackbox;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import kernel.Config;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import agents.Agent;
import blackbox.BBFunction;
import blackbox.BlackBox;
import blackbox.BlackBoxAgent;
import blackbox.Input;
import blackbox.MathFunction;

// TODO: Auto-generated Javadoc
/**
 * The Class GrapheBlackBoxPanel.
 */
public class GrapheBlackBoxPanel extends JPanel implements MouseInputListener, ViewerListener{
	
	/** The graph. */
	Graph graph;
	
	/** The viewer. */
	Viewer viewer;
	
	/** The black box. */
	BlackBox blackBox;
	
	/** The tool bar. */
	/* ----ToolBar Components----*/
	private JToolBar toolBar;
	
	/** The button show value. */
	private JButton buttonShowValue;
	
	/** The button show default. */
	private JButton buttonShowDefault;
	
	/** The button show name. */
	private JButton buttonShowName;
	
	
	/** The view mode. */
	private int viewMode = 0;
	
	/** The pipe. */
	/*Interaction with simulator*/
	private ViewerPipe pipe;
	
	/** The mouse event. */
	private MouseEvent mouseEvent;
	
	/** The right click. */
	Boolean rightClick = false;

	/**
	 * Instantiates a new graphe black box panel.
	 */
	public GrapheBlackBoxPanel() {
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
		


		
		//update();
		
	}
	
	/**
	 * Show value.
	 */
	public void showValue() {
		viewMode = 1;
		if (blackBox != null) {
			for (String name : blackBox.getBlackBoxAgents().keySet()) {
				BlackBoxAgent bba = blackBox.getBlackBoxAgents().get(name);	
				graph.getNode(bba.getName()).setAttribute("ui.label", bba.getValue());		
			}
		}
	}
	
	/**
	 * Show default.
	 */
	public void showDefault() {
		viewMode = 0;
		if (blackBox != null) {
			for (String name : blackBox.getBlackBoxAgents().keySet()) {
				BlackBoxAgent bba = blackBox.getBlackBoxAgents().get(name);	
				graph.getNode(bba.getName()).setAttribute("ui.label", bba.getName() + " " + bba.getValue());		
			}
		}
	}
	
	/**
	 * Show name.
	 */
	public void showName() {
		viewMode = 2;
		if (blackBox != null) {
			for (String name : blackBox.getBlackBoxAgents().keySet()) {
				BlackBoxAgent bba = blackBox.getBlackBoxAgents().get(name);	
				graph.getNode(bba.getName()).setAttribute("ui.label", bba.getName());		
			}
		}
	}
	
	/**
	 * Sets the black box.
	 *
	 * @param blackBox the new black box
	 */
	public void setBlackBox(BlackBox blackBox) {
		this.blackBox = blackBox;
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
	}
	
	
	/**
	 * Creates the graph.
	 */
	private void createGraph() {
		graph = new SingleGraph("BLACK BOX");
		for (String name : blackBox.getBlackBoxAgents().keySet()) {
			BlackBoxAgent bba = blackBox.getBlackBoxAgents().get(name);
			//graph.addAttribute("ui.stylesheet", "url('file:"+System.getProperty("user.dir")+"/bin/styles/styleBlackBox.css')");
			graph.addAttribute("ui.stylesheet", "url('" + this.getClass().getClassLoader().getResource("src/styles/styleBlackBox.css") + "')");
			
		/*	graph.addAttribute("ui.stylesheet", "node { stroke-mode: plain;"
					+ "fill-color: red;"
					+ "shape: box;"
					+ "stroke-color: yellow;"
					+ " }");//text-mode
			graph.addAttribute("ui.style", "shape: box;");*/

			//graph.addAttribute("ui.stylesheet", "node { text-mode: normal; }");//text-mode

			//graph.
			graph.addNode(bba.getName());
			graph.getNode(bba.getName()).addAttribute("ui.class", bba.getClass().getSimpleName());
			graph.getNode(bba.getName()).addAttribute("ui.label", bba.getName());
		//	graph.getNode(bba.getName()).addAttribute("ui.fill-color", "red");
			//graph.getNode(bba.getName()).;
			//System.out.println(graph.getNode(bba.getName()).getLabe)());
			
		}

		//Draw edge
		for (String name : blackBox.getBlackBoxAgents().keySet()) {
			BlackBoxAgent bba = blackBox.getBlackBoxAgents().get(name);

			for (Agent target : bba.getTargets()) {
				graph.addEdge(bba.getName() + " " + target.getName(), bba.getName(), target.getName(), true);				
			}
			
		}

		viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.addDefaultView(false);
		viewer.enableAutoLayout();
		viewer.getDefaultView().addMouseListener(this);
		

		pipe = viewer.newViewerPipe();
        pipe.addViewerListener(this);
        pipe.addSink(graph);

		viewer.getDefaultView().setMinimumSize(new Dimension(400,400));
		this.add(viewer.getDefaultView(),BorderLayout.CENTER);
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

	/* (non-Javadoc)
	 * @see org.graphstream.ui.view.ViewerListener#buttonPushed(java.lang.String)
	 */
	@Override
	public void buttonPushed(String id) {
		System.out.println("node pushed : " + id);
		
		
		if (rightClick) {
			if (blackBox.getBlackBoxAgents().get(id) instanceof Input) {
				popupInput(id);
			} else if (blackBox.getBlackBoxAgents().get(id) instanceof BBFunction) {
				popupFunction(id);
			}
				
			
			rightClick = false;
		}		
	}

	/* (non-Javadoc)
	 * @see org.graphstream.ui.view.ViewerListener#buttonReleased(java.lang.String)
	 */
	@Override
	public void buttonReleased(String arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.graphstream.ui.view.ViewerListener#viewClosed(java.lang.String)
	 */
	@Override
	public void viewClosed(String arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Show a popup with options for function agent.
	 *
	 * @param id : the id of the agent
	 */
	public void popupFunction(String id){
		
		JPopupMenu popup = new JPopupMenu("Function");
		
		JMenu subMathFunc = new JMenu("Mathematical function");
		popup.add(subMathFunc);
		
		for (MathFunction mf : MathFunction.values()) {
			JMenuItem item = new JMenuItem(mf.toString());
			item.addActionListener(e -> {changeMathFunc(mf,id);});
			item.setIcon(Config.getIcon("pencil.png"));
			subMathFunc.add(item);
		}
		
		popup.show(this, this.getX() + mouseEvent.getX(), this.getY() + mouseEvent.getY());
	}
	
	/**
	 * Show a popup with options for input agent.
	 *
	 * @param id : the id of the agent
	 */
	public void popupInput(String id){
		
		JPopupMenu popup = new JPopupMenu("Input");
		
		JMenuItem itemX2 = new JMenuItem("x2");
		itemX2.addActionListener(e -> {factorInput(2,id);});
		itemX2.setIcon(Config.getIcon("pencil.png"));
		popup.add(itemX2);
		
		JMenuItem itemDiv2 = new JMenuItem("/2");
		itemDiv2.addActionListener(e -> {factorInput(0.5,id);});
		itemDiv2.setIcon(Config.getIcon("pencil.png"));
		popup.add(itemDiv2);
		
		popup.show(this, this.getX() + mouseEvent.getX(), this.getY() + mouseEvent.getY());
	}
	
	/**
	 * Factor input.
	 *
	 * @param factor the factor
	 * @param id the id
	 */
	private void factorInput(double factor, String id) {
		((Input)blackBox.getBlackBoxAgents().get(id)).multValue(factor);
	}
	
	/**
	 * Change math func.
	 *
	 * @param mf the mf
	 * @param id the id
	 */
	private void changeMathFunc(MathFunction mf, String id) {
		((BBFunction)blackBox.getBlackBoxAgents().get(id)).setFunc(mf);
	}
}
