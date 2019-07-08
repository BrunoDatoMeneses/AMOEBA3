package experiments;

import kernel.AMOEBA;
import kernel.StudiedSystem;

/**
 * The most minimal main possible producing a functioning amoeba.
 * @author Hugo
 *
 */
public class MinimalMain {

	public static void main(String[] args) {
		// create a system to be studied
		StudiedSystem studiedSystem = new NDimCube(50.0, 100);
		// create the amoeba
		// Make sure the path to the config file is correct.
		AMOEBA amoeba = new AMOEBA("resources/100DimensionsLauncher.xml", studiedSystem);
		// a window should have appeared, allowing you to control and visualize the amoeba.
	}

}
