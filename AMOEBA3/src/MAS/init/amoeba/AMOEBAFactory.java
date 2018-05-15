package MAS.init.amoeba;

import MAS.kernel.AMOEBA;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating AMOEBA objects.
 */
public class AMOEBAFactory {
	
	/* Create an AMOEBA by selecting the XML Files */
	public static AMOEBA createAMOEBA() {
		return new XMLFilesAMOEBAFactory().createAMOEBA();
	}
	
	
	/* Create an AMOEBA by default - providing relative path of xml files */
	public static AMOEBA createAMOEBA(boolean viewer, String pathToSourceXML, String pathToAgentsXML) {
		return new DefaultAMOEBAFactory().createAMOEBA(viewer, pathToSourceXML, pathToAgentsXML);
	}
	
	
	/* Create an AMOEBA by selecting serialized file */
	public static AMOEBA createAMOEBA(boolean viewer, String path) {
		return new SerializedAMOEBAFactory().createAMOBA(viewer, path);
	}
}
