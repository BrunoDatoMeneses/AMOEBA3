package py4j;

import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.tools.Log;

public class Main {

	/*
	 * Setting static field in py4j is difficult, this class add methods to help with that
	 */
	public static class Control{
		public static void setComandLine(boolean value) {
			Configuration.commandLineMode = value;
		}
		
		public static void setLogLevel(String level) {
			Log.defaultMinLevel = Log.Level.valueOf(level);
		}
	}
	
	public static void main(String[] args) {
		GatewayServer server = new GatewayServer();
		server.start();
	}

}
