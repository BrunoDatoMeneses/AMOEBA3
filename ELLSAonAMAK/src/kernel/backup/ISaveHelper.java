package kernel.backup;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * An helper class that handle save, autosave, and load needs of an AMOEBA.
 * @see SaveHelperImpl
 * @see SaveHelperDummy
 * @author Hugo
 *
 */
public interface ISaveHelper {
	
	/**
	 * Load a save pointed by path.
	 * @param path path to the save.
	 */
	public void load(String path);
	
	/**
	 * Load a save from file.
	 * @param path path to the save.
	 */
	public void load(File file);

	/**
	 * Create a save at path.
	 * @param path path of the new save
	 */
	public void save(String path);
	
	/**
	 * Create a save in file.
	 * @param path path of the new save
	 */
	public void save(File file);

	/**
	 * Add a new save in {@link SaveHelperImpl#dirManual}.
	 * @param name
	 */
	public void newManualSave(String name);

	public void newManualSave(String name, String path);

	/**
	 * Add a new save in {@link SaveHelperImpl#dirAuto}.
	 */
	public void autosave();

	/**
	 * List saves in {@link SaveHelperImpl#dirAuto}.
	 */
	public List<Path> listAutoSaves();

	/**
	 * List saves in {@link SaveHelperImpl#dirManual}.
	 */
	public List<Path> listManualSaves();
	
	/**
	 * Activate or deactivate the automatic save system.
	 * @param value
	 */
	public void setAutoSave(boolean value);
	
	/**
	 * Tell if the automatic save system is activated.
	 * @return
	 */
	public boolean getAutoSave();
}
