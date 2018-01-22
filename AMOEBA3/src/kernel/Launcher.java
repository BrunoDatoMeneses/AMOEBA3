package kernel;

import java.io.File;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import view.JMainFrame;
import view.system.MainPanel;
import agents.localModel.TypeLocalModel;
import blackbox.BlackBox;


// TODO: Auto-generated Javadoc
/**
 * The Class Launcher.
 */
public class Launcher implements Serializable {

	/** The main panel. */
	public static MainPanel mainPanel;
	
	/** The Constant viewer. */
	/*GUI or not*/
	public static final boolean viewer = true;
	


	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		launch(viewer);
	}
	
	/**
	 * Launch.
	 *
	 * @param viewer the viewer
	 */
	public static void launch(boolean viewer) {

		printStartInfo();
		
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		setSystemLookAndFeel();

		JFrame frame = null;
		if (viewer) {
			frame = new JMainFrame();
			mainPanel = new MainPanel(false);
			frame.setContentPane(mainPanel);			
			frame.setVisible(true);
		}
		
		Scheduler scheduler = new Scheduler();

		BlackBox blackBox = new BlackBox(scheduler, new File(System
				.getProperty("user.dir")
				+ "/bin/ressources/A+B+C.xml"));
		World world = new World(scheduler,
				new File(System.getProperty("user.dir")
						+ "/bin/ressources/A+B+C_solver.xml"),
						blackBox);
		world.setBlackBox(blackBox);

		scheduler.getHeadAgent().setDataForErrorMargin(3, 1.2, 0.8, 0.0, 10, 100);
		scheduler.getHeadAgent().setDataForInexactMargin(0.5, 1.2, 0.8, 0.0, 10, 100);
		world.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		if (viewer) {
			mainPanel.setWorld(world);
			mainPanel.setBlackBox(blackBox);
			frame.pack();
		}
		
		AMOEBA amoeba = new AMOEBA(null);
		scheduler.setView(mainPanel);	
		scheduler.setWorld(world);
		amoeba.setScheduler(scheduler);
		
		if (!viewer) {
			amoeba.start(true);
		} else {
			amoeba.start(false);
		}

		if (viewer) {
			mainPanel.setAMOEBA(amoeba);
		}
	}
	
	/**
	 * Prints the start info.
	 */
	private static void printStartInfo() {
		System.out.println(printWithDash("",30));
		System.out.println(printWithDash(Config.getProjectname(),30));
		System.out.println(printWithDash(Config.getVersionname(),30));
		System.out.println(printWithDash(Config.getVersionnumber(),30));
		System.out.println(printWithDash("",30));

	}

	/**
	 * Prints the with dash.
	 *
	 * @param str the str
	 * @param l the l
	 * @return the string
	 */
	private static String printWithDash(String str, int l) {
		String newStr = str;
		int nDash = (l - str.length())/2;
		for (int i = 0 ; i < nDash; i++) {
			newStr = "-" + newStr + "-";
		}
		return newStr;	
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


}
