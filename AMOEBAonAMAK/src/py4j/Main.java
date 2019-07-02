package py4j;

import fr.irit.smac.amak.Configuration;

public class Main {

	/*
	 * Setting static field in py4j is difficult, this class add methods to help with that
	 */
	public static class Control{
		public static void setComandLine(boolean value) {
			Configuration.commandLineMode = value;
		}
	}
	
	public static void main(String[] args) {
		GatewayServer server = new GatewayServer();
		server.start();
	}

}
