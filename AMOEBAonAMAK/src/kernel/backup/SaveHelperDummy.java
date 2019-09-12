package kernel.backup;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A save helper that does nothing. Useful when one wish to deactivate an amoeba save system.
 * 
 * @see SaveHelperImpl
 * @author Hugo
 *
 */
public class SaveHelperDummy implements ISaveHelper {

	@Override
	public void load(String path) {
	}

	@Override
	public void load(File file) {
	}

	@Override
	public void save(String path) {
	}

	@Override
	public void save(File file) {
	}

	@Override
	public void newManualSave(String name) {
	}

	@Override
	public void autosave() {
	}

	@Override
	public List<Path> listAutoSaves() {
		return new ArrayList<Path>();
	}

	@Override
	public List<Path> listManualSaves() {
		return new ArrayList<Path>();
	}

	@Override
	public void setAutoSave(boolean value) {
		
	}

	@Override
	public boolean getAutoSave() {
		return false;
	}

}
