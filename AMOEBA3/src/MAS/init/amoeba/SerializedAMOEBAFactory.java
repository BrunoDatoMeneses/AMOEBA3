package MAS.init.amoeba;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import MAS.blackbox.BlackBox;
import MAS.kernel.AMOEBA;
import MAS.kernel.Scheduler;
import MAS.kernel.World;
import VISUALIZATION.observation.Observation;
import VISUALIZATION.view.system.MainPanel;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating SerializedAMOEBA objects.
 */
public class SerializedAMOEBAFactory {
	
	/**
	 * Creates a new SerializedAMOEBA object.
	 *
	 * @param viewer the viewer
	 * @param path the path
	 * @return the amoeba
	 */
	public AMOEBA createAMOBA(boolean viewer, String path) {
		Object obj[] = new Object[3];
		World world;
		boolean rememberState;
		ArrayList<Observation> obsList;
		
		try {
        	FileInputStream fis;
    		
        	fis = new FileInputStream(new File(path));
			ObjectInputStream ois = new ObjectInputStream(fis);
		    //obj = ois.readObject();
		    obj = (Object[]) ois.readObject();
			ois.close();	
		    fis.close();
		    
		    world = (World) obj[0];
            rememberState = (boolean) obj[1];
            obsList = (ArrayList<Observation>) obj[2];
            
            Scheduler scheduler = new Scheduler();
    		scheduler = world.getScheduler();
    		BlackBox blackBox = scheduler.getWorld().getBlackbox();
    		
    		AMOEBA amoeba = new AMOEBA(viewer, scheduler, world, blackBox);
    		MainPanel mainPanel = amoeba.getMainPanel();
    		mainPanel.disableCheckBoxRememberState(rememberState);
    		mainPanel.setVisualization(obsList);
    		return amoeba;
    		
		} catch (ClassNotFoundException | IOException | NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

}
