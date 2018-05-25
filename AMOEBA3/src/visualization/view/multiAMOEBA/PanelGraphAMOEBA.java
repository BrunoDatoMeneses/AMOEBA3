package visualization.view.multiAMOEBA;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

// TODO: Auto-generated Javadoc
/**
 * The Class PanelGraphAMOEBA.
 */
public class PanelGraphAMOEBA extends JPanel implements ViewerListener, MouseInputListener {
	
	/** The graph. */
	SingleGraph graph;
	
	/** The viewer. */
	Viewer viewer;
	
	/** The pipe. */
	ViewerPipe pipe;
	
	/**
	 * Instantiates a new panel graph AMOEBA.
	 */
	public PanelGraphAMOEBA() {
		setLayout(new BorderLayout());
		this.setMinimumSize(new Dimension(400,400));

	}
	
	/**
	 * Initialize.
	 */
	public void initialize() {
		createGraph();
	}

	/**
	 * Creates the graph.
	 */
	private void createGraph() {
		System.out.println("Create graph AMOEBAs");
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
		this.add(new JLabel("Nord"),BorderLayout.NORTH);
		
		graph.addNode("jn");
		graph.addNode("fgjhgh");
		graph.getNode("fgjhgh").addAttribute("ui.label", "fgjhgh");

		graph.addNode("jkuyg");
		graph.addNode("rdrdufg");
		//graph.addAttribute("ui.stylesheet", "url('file:"+System.getProperty("user.dir")+"/bin/styles/styleAMOEBA.css')");
		graph.addAttribute("ui.stylesheet", "url('" + this.getClass().getClassLoader().getResource("/styles/styleAMOEBA.css") + "')");
		
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		System.out.println("click");
		graph.addNode("aaaa");
		
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
	public void buttonPushed(String arg0) {
		// TODO Auto-generated method stub
		
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
	
}
