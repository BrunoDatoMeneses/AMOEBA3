package VISUALIZATION.view.multiAMOEBA;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import MAS.kernel.Launcher;

// TODO: Auto-generated Javadoc
/**
 * The Class JMainFrameMulti.
 */
public class JMainFrameMulti extends JFrame {

	/** The menu bar. */
	private JMenuBar menuBar;
	
	/** The menu files. */
	private JMenu menuFiles;
	
	/** The menu item start modelize. */
	private JMenuItem menuItemStartModelize;
	
	/** The menu item quit. */
	private JMenuItem menuItemQuit;
	
	/** The panel graph. */
	private PanelGraphAMOEBA panelGraph;
	
	/**
	 * Instantiates a new j main frame multi.
	 */
	public JMainFrameMulti() {
		super();
		initializeMenuBar();
		
		panelGraph = new PanelGraphAMOEBA();
		this.setContentPane(panelGraph);
		this.pack();
	}
	
	/**
	 * Initialize.
	 */
	public void initialize() {
		panelGraph.initialize();
	}

	/**
	 * Initialize menu bar.
	 */
	private void initializeMenuBar() {

		menuBar = new JMenuBar();
		menuFiles = new JMenu("neOCampus");
		
		
		menuItemStartModelize = new JMenuItem("Quick launch a modelization");
		menuItemStartModelize.addActionListener(e -> {Launcher.launch(true);});
		menuItemQuit = new JMenuItem("Quit");
		
		
		

		menuFiles.add(menuItemStartModelize);
		menuFiles.add(menuItemQuit);
		
		menuBar.add(menuFiles);
		this.setJMenuBar(menuBar);

	}
}