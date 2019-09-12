package experiments;

import java.io.IOException;

import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.tools.SerializeBase64;
import gui.AmoebaWindow;
import kernel.AMOEBA;
import kernel.StudiedSystem;

public class Main {

	/**
	 * Launch an amoeba from a config file and a serialized studied system. <br/>
	 * A serialized F_XY_System : rO0ABXNyABdleHBlcmltZW50cy5GX1hZX1N5c3RlbZo0im9N3R7/AgAGWgAJZmlyc3RTdGVwRAAGcmVzdWx0RAAJc3BhY2VTaXplRAABeEQAAXlMAAlnZW5lcmF0b3J0ABJMamF2YS91dGlsL1JhbmRvbTt4cAEAAAAAAAAAAEBJAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHA=
	 * @param args comandLineMode configFile serializedBase64StudiedSystem
	 */
	public static void main(String[] args) {
		if(args.length != 3) {
			System.err.println("Usage : comandLineMode configFile serializedBase64StudiedSystem");
		}
		Configuration.commandLineMode = Boolean.valueOf(args[0]);
		String configFile = args[1];
		String b64StudiedSystem = args[2];
		
		if(!Configuration.commandLineMode) {
			AmoebaWindow.instance();
		}
		
		StudiedSystem ss;
		try {
			ss = (StudiedSystem)SerializeBase64.deserialize(b64StudiedSystem);
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("A");
			Log.defaultLog.error("INIT", "Cannot deserialize %s as a studied system.", b64StudiedSystem);
			e.printStackTrace();
			ss = null;
		}
		
		System.out.println("Creating the amoeba");
		AMOEBA amoeba = new AMOEBA(configFile, ss);
		
		synchronized (Thread.currentThread()){
			try {
				Thread.currentThread().wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
