package mas.init.amoeba;

import mas.kernel.AMOEBA;

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
	public static AMOEBA createAMOEBA(boolean viewer, String pathToAgentsXML) {
		return new DefaultAMOEBAFactory().createAMOEBA(viewer, pathToAgentsXML);
	}
	

}
