package kernel;

import java.io.File;

public interface ISaveState {
	public void load(File file);
	public void save(File file);
}
