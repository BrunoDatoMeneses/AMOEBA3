package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class XmlConfigGenerator {
	
	public static String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	public static String systemStart = "<System>\n";
	public static String configurationStart = "<Configuration>\n";
	public static String configuration = "    <Learning allowed = \"%s\" creationOfNewContext = \"%s\" loadPresetContext = \"%s\" />\n";
	public static String configurationEnd = "</Configuration>\n";
	public static String agentsStart = "<StartingAgents>\n";
	public static String sensor = "    <Sensor Name=\"%s\" Enum=\"%s\" />\n";
	public static String controller = "    <Controller Name=\"Controller\">\n" + 
			"        <ErrorMargin ErrorAllowed=\"%f\" AugmentationFactorError=\"%f\" DiminutionFactorError=\"%f\" MinErrorAllowed=\"%f\" NConflictBeforeAugmentation=\"%d\" NSuccessBeforeDiminution=\"%d\" />\n" + 
			"    </Controller>\n";
	public static String agentsEnd = "</StartingAgents>\n";
	public static String systemEnd = "</System>\n";
	
	private static String makeSensor(String sensorName, boolean isEnum) {
		return String.format(sensor, sensorName, ""+isEnum);
	}
	
	private static String makeConfiguration(boolean isLearningAllowed, boolean isCreationOfNewContext, boolean isLoadContext) {
		return String.format(configuration, ""+isLearningAllowed, ""+isCreationOfNewContext, ""+isLoadContext);
	}
	
	private static String makeController(double errorAllowed, double augmentationFactorError, double diminutionFactorError, double minErrorAllowed, int nConflictBeforeAugmentation, int nSuccessBeforeDiminution) {
		return String.format(Locale.ENGLISH, controller, errorAllowed, augmentationFactorError, diminutionFactorError, minErrorAllowed, nConflictBeforeAugmentation, nSuccessBeforeDiminution);
	}

	public static void makeXML(File file, List<Pair<String, Boolean>> sensors) throws IOException {
	    try(FileWriter fw = new FileWriter(file)){
	    	fw.write(header);
	    	fw.write(systemStart);
	    	fw.write(configurationStart);
	    	fw.write(makeConfiguration(true, true, false));
	    	fw.write(configurationEnd);
	    	fw.write(agentsStart);
	    	for(Pair<String, Boolean> s : sensors) {
	    		fw.write(makeSensor(s.getA(), s.getB()));
	    	}
	    	fw.write(makeController(1.0, 0.5, 0.5, 0.01, 50, 50));
	    	fw.write(agentsEnd);
	    	fw.write(systemEnd);
	    }
	}
	
	public static void makeXML(File file, int dimension) throws IOException {
		ArrayList<Pair<String, Boolean>> sensors = new ArrayList<>(dimension);
		for(int i = 1; i <= dimension; i++) {
			sensors.add(new Pair<String, Boolean>("p"+i, false));
		}
		makeXML(file, sensors);
	}
}
