package experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class XmlConfigGenerator {

	public XmlConfigGenerator() {
		
	}
	
	public static String makeLineXML(String sensorName, String source) {
		return "\t\t<Sensor Name=\""+sensorName+"\" Source=\""+source+"\"></Sensor>\n"; 
	}

	public static void makeXML(String XMLFile, int dimension) {
		String path = "Ressources";
	
	    try {
	    	File file = new File(path+"/"+XMLFile);
	        FileWriter fw = new FileWriter(file);
	        
	        //Make content
	        String str = "";
	        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
	        		"<System>\n" + 
	        		"\n" + 
	        		"\t<!-- General config options -->\n" + 
	        		"\t<Configuration>\n" + 
	        		"\t\t<Learning allowed = \"true\" creationOfNewContext = \"true\" loadPresetContext = \"false\"></Learning>	\n" + 
	        		"\t</Configuration>\n" + 
	        		"\n" + 
	        		"\t<StartingAgents>\n";
	  
	        for(int j = 0; j < dimension; ++j) {
	        	str += makeLineXML("px"+j,"x"+j);
    		}
	        
	        str += "\t\t<Controller Name=\"Controller\" Oracle=\"test\"></Controller>\n\n";

	        
	        		
	        str += "\t</StartingAgents>\n" + 
	        		"\n" + 
	        		"</System>";
	        
	        //Write and close file
	        fw.write(str);
	        fw.close();
	      } catch (FileNotFoundException e) {
	        e.printStackTrace();
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String XMLConfigFile = "nDimensionLauncher.xml";
		
		XmlConfigGenerator.makeXML(XMLConfigFile, 4);
		
		
	}
}
