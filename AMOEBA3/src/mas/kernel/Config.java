package mas.kernel;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;


// TODO: Auto-generated Javadoc
/**
 * The Class Config.
 */
public class Config implements Serializable {
	
	/** The verbosity. */
	private static int verbosity = 0;
	
	/** The Constant projectName. */
	private final static String projectName = "AMOEBA"; 

	/** The Constant versionName. */
	private final static String versionName = "---";
	
	/** The Constant versionNumber. */
	private final static String versionNumber = "0.11";
	
	
	
	/** The icons. */
	static public HashMap<String,ImageIcon> icons = new HashMap<String,ImageIcon>();

	
	/**
	 * Gets the icon.
	 *
	 * @param name the name
	 * @return the icon
	 */
	public static ImageIcon getIcon (String name) {
		if (!icons.containsKey(name)) {
			try {	
				icons.put(name, new ImageIcon(ImageIO.read(Config.class.getResourceAsStream("/VISUALIZATION/icons/" + name))));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	

	return icons.get(name);
	
	}

	/**
	 * Gets the verbosity.
	 *
	 * @return the verbosity
	 */
	public static int getVerbosity() {
		return verbosity;
	}

	/**
	 * Sets the verbosity.
	 *
	 * @param verbosity the new verbosity
	 */
	public static void setVerbosity(int verbosity) {
		Config.verbosity = verbosity;
	}
	
	/**
	 * Change verbosity.
	 *
	 * @param dx the dx
	 */
	public static void changeVerbosity(int dx) {
		verbosity += dx;
		System.out.println("New verbosity level : "+ verbosity);
	}
	
	/**
	 * Prints the.
	 *
	 * @param s the s
	 * @param priority the priority
	 */
	public static void print(String s, int priority) {
		if (priority <= verbosity) {
			System.out.println(s);
		}
	}

	/**
	 * Gets the projectname.
	 *
	 * @return the projectname
	 */
	public static String getProjectname() {
		return projectName;
	}

	/**
	 * Gets the versionnumber.
	 *
	 * @return the versionnumber
	 */
	public static String getVersionnumber() {
		return versionNumber;
	}

	/**
	 * Gets the versionname.
	 *
	 * @return the versionname
	 */
	public static String getVersionname() {
		return versionName;
	}
	
}
