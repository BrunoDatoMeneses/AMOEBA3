package kernel;

import java.io.File;

public interface ISaveState {
	public void loadXML(File file);
	public void saveXML(File file);
}
