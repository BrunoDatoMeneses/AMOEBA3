package kernel;

import java.io.File;

public interface IBackupSystem {

	/**
	 * Load from an XML file a model for AMOEBA.
	 * 
	 * @param file The file you want to read. It is supposed to be a text file with
	 *             XML format.
	 */
	public void loadXML(File file);
	
	/**
	 * Save into a file the current model of AMOEBA (agents and some variables in
	 * AMOEBA class).
	 * 
	 * @note Remember AMAK add agents only at the end of a cycle, trying to save
	 *       just after load will result in a XML file without any agents.
	 * @param file The file where you want to insert the model.
	 */
	public void saveXML(File file);
	
	/**
	 * Allow to load preset context. Default at true.
	 * @param loadPresetContext
	 */
	public void setLoadPresetContext(boolean loadPresetContext);
	
	/**
	 * If the backup system is allowed to lead preset context.
	 * @return
	 */
	public boolean isLoadPresetContext();
	
}
