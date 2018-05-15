package MAS.init.amoeba;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import MAS.blackbox.BlackBox;
import MAS.init.Serialization;
import MAS.kernel.AMOEBA;
import MAS.kernel.Scheduler;
import MAS.kernel.World;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating DefaultAMOEBA objects.
 */
public class DefaultAMOEBAFactory {
	
	/**
	 * Creates a new DefaultAMOEBA object.
	 *
	 * @param viewer the viewer
	 * @param pathToSourceXML the path to source XML
	 * @param pathToAgentsXML the path to agents XML
	 * @return the amoeba
	 */
	public AMOEBA createAMOEBA(boolean viewer, String pathToSourceXML, String pathToAgentsXML) {
		try {
			InputStream sourceXMLInput = getClass().getClassLoader().getResourceAsStream(pathToSourceXML);
			InputStream agentsXMLInput = getClass().getClassLoader().getResourceAsStream(pathToAgentsXML);
			
			if (sourceXMLInput.available() > 0 && agentsXMLInput.available() > 0) {
								
				return amoebaCreationWithSchedulerBlackBoxAndWorld(viewer, sourceXMLInput, agentsXMLInput);
				
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
	
	
	private AMOEBA amoebaCreationWithSchedulerBlackBoxAndWorld(boolean viewer, InputStream sourceXMLInput, InputStream agentsXMLInput) {
		
		Scheduler scheduler = new Scheduler();
		
		File f1 = new File("tmp/sourceXML.xml");
		File f2 = new File("tmp/agentsXML.xml");
		
		f1.getParentFile().mkdirs();
		try {
			f1.createNewFile();
			f2.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		inputStreamToFile(sourceXMLInput, f1);
		inputStreamToFile(agentsXMLInput, f2);
		
		BlackBox blackBox = new BlackBox(scheduler, f1);	
		World world = new World(scheduler, f2, blackBox);
		
		AMOEBA amoeba = new AMOEBA(viewer, scheduler, world, blackBox);
		f1.delete();
		f2.delete();
		
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
