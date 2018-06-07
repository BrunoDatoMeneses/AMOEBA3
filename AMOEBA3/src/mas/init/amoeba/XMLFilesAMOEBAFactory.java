package mas.init.amoeba;

import java.io.File;

import mas.blackbox.BlackBox;
import mas.init.Initialization;
import mas.kernel.AMOEBA;
import mas.kernel.Scheduler;
import mas.kernel.World;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating XMLFilesAMOEBA objects.
 */
public class XMLFilesAMOEBAFactory {
	
	/**
	 * Creates a new XMLFilesAMOEBA object.
	 *
	 * @return the amoeba
	 */
	public AMOEBA createAMOEBA() {
		
		boolean viewer = true;
		String[] filePaths = Initialization.initializationFiles();
		File sourceXMLFile = new File(filePaths[0]);
		File agentsXMLFile = new File(filePaths[1]);
		
		Scheduler scheduler = new Scheduler();
		//BlackBox blackBox = new BlackBox(scheduler, sourceXMLFile);	
		World world = new World(scheduler, agentsXMLFile);
		return new AMOEBA(viewer, scheduler, world);
		
	}

}
