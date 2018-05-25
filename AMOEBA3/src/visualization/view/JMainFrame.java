package visualization.view;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

// TODO: Auto-generated Javadoc
/**
 * The Class JMainFrame.
 */
public class JMainFrame extends JFrame {

	/** The menu bar. */
	private JMenuBar menuBar;
	
	/** The menu files. */
	private JMenu menuFiles;
	
	/** The menu item start modelize. */
	private JMenuItem menuItemStartModelize;
	
	/** The menu item quit. */
	private JMenuItem menuItemQuit;

	/**
	 * Instantiates a new j main frame.
	 */
	public JMainFrame() {
		super();
		initializeMenuBar();
	}

	/**
	 * Initialize menu bar.
	 */
	private void initializeMenuBar() {

		menuBar = new JMenuBar();
		menuFiles = new JMenu("neOCampus");
		
		
		//menuItemStartModelize = new JMenuItem("Quick launch a modelization");
		//menuItemStartModelize.addActionListener(e -> {Launcher.launch(true);});
		//menuItemQuit = new JMenuItem("Quit");
		
		
		

		//menuFiles.add(menuItemStartModelize);
		//menuFiles.add(menuItemQuit);
		
		//menuBar.add(menuFiles);
		//this.setJMenuBar(menuBar);

	}
}