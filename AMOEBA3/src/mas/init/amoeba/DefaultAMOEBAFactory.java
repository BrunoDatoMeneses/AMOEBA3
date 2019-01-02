package mas.init.amoeba;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//import mas.blackbox.BlackBox;
import mas.init.Serialization;
import mas.kernel.AMOEBA;
import mas.kernel.Scheduler;
import mas.kernel.World;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating DefaultAMOEBA objects.
 */
public class DefaultAMOEBAFactory {
	
	/**
	 * Creates a new DefaultAMOEBA object.
	 *
	 * @param viewer the viewer
	 * @param pathToAgentsXML the path to agent XML
	 * @return the amoeba
	 */
	public AMOEBA createAMOEBA(boolean viewer, String pathToAgentsXML) {
		try {
			InputStream agentsXMLInput = getClass().getClassLoader().getResourceAsStream(pathToAgentsXML);
			
			if ( agentsXMLInput.available() > 0) {
								
				return amoebaCreationWithSchedulerBlackBoxAndWorld(viewer, agentsXMLInput);
				
			} else {
				XMLFilesAMOEBAFactory xmlFilesAMOEBA = new XMLFilesAMOEBAFactory();
				return xmlFilesAMOEBA.createAMOEBA();
			}
					
		} catch (Exception e) {
			e.printStackTrace();
			File file = Serialization.openSerializationFile();
			if ( file != null) {
				SerializedAMOEBAFactory serializedAMOEBA = new SerializedAMOEBAFactory();
				return serializedAMOEBA.createAMOBA(viewer, file.getAbsolutePath());
			} else {
				XMLFilesAMOEBAFactory xmlFilesAMOEBA = new XMLFilesAMOEBAFactory();
				return xmlFilesAMOEBA.createAMOEBA();
			}	
		}
		
	}
	
	
	private AMOEBA amoebaCreationWithSchedulerBlackBoxAndWorld(boolean viewer, InputStream agentsXMLInput) {
		
		Scheduler scheduler = new Scheduler();
		
		//File sourceFile = new File("tmp/sourceXML.xml");
		File agentsFile = new File("tmp/agentsXML.xml");
		
		//sourceFile.getParentFile().mkdirs();
		try {
			//sourceFile.createNewFile();
			agentsFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//inputStreamToFile(sourceXMLInput, sourceFile);
		inputStreamToFile(agentsXMLInput, agentsFile);
		
		//BlackBox blackBox = new BlackBox(scheduler, sourceFile);	
		World world = new World(scheduler, agentsFile);
		
		AMOEBA amoeba = new AMOEBA(viewer, scheduler, world);
		//sourceFile.delete();
		agentsFile.delete();
		
		return amoeba;
	}
	
	
	/**
	 * Input stream to file.
	 *
	 * @param input the input
	 * @param file the file
	 */
	private void inputStreamToFile(InputStream input, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte buf[] = new byte[1024];
			int len;
			while((len = input.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

}
