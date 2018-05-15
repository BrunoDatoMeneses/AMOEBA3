package VISUALIZATION.log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import VISUALIZATION.graphView.TemporalGraph;
import MAS.kernel.World;

// TODO: Auto-generated Javadoc
/**
 * The Class LogFile.
 */
public class LogFile {
	
	 /** The file date. */
 	private static String fileDate = new SimpleDateFormat("yyMMdd-hhmmss").format(new Date());
	 static {System.setProperty("log.out", "tmp/myLog"+fileDate+".out");}
	 
 	/** The Constant logger. */
 	private static final Logger logger = LoggerFactory.getLogger(LogFile.class);
	 
	 /** The criticity. */
 	private int criticity;
	 
 	/** The types. */
 	private LogMessageType[] types;
	 
 	/** The is console. */
 	private boolean isConsole;
	 
 	/** The is file. */
 	private boolean isFile;
	 
 	/** The previous state. */
 	private boolean previousState = false;
	 
 	/** The console panel. */
 	private ConsolePanel consolePanel;
	 
 	/** The world. */
 	private World world;
	 
 	/** The temporal graph. */
 	private TemporalGraph temporalGraph;
	 
	 /** The logger messages. */
 	private List<LoggerMessage> loggerMessages = new ArrayList<LoggerMessage>();
	 
	 /**
 	 * Generate log file.
 	 */
 	public void generateLogFile() {
		try {
			Properties props = new Properties();
			props.load(this.getClass().getClassLoader().getResourceAsStream("log/log4j.properties"));
			PropertyConfigurator.configure(props);
			new LoggerConsole(world.getAmoeba().getLogFile());
			new LoggerFile(world.getAmoeba().getLogFile());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File not found!");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("IO Exception!");
			e.printStackTrace();
		}
	 }
	 
	 /**
 	 * Attach.
 	 *
 	 * @param loggerMessage the logger message
 	 */
 	public void attach(LoggerMessage loggerMessage) {
		 loggerMessages.add(loggerMessage);
	 }
	 
	 /**
 	 * Sets the conditions for debug.
 	 *
 	 * @param criticity the criticity
 	 * @param types the types
 	 * @param isConsole the is console
 	 * @param isFile the is file
 	 */
 	public void setConditionsForDebug(int criticity, LogMessageType[] types, boolean isConsole, boolean isFile) {
		 this.criticity = criticity;
		 this.types = types;
		 this.isConsole = isConsole;
		 this.isFile = isFile;
	 }
	 
	 /**
 	 * Message to debug.
 	 *
 	 * @param messages the messages
 	 * @param criticity the criticity
 	 * @param types the types
 	 */
 	public void messageToDebug(String messages, int criticity, LogMessageType[] types) {
		 // Create Marker in graph
		 String className = new Exception().getStackTrace()[1].getClassName();
		 boolean agentMessage = isContextMessage(className);
		 if (agentMessage) {
			 temporalGraph.createMarker(world.getScheduler().getTick(), messages);
		 }
		 // Log message
		 if (previousState) {
			boolean hasTag = containTag(this.getLogMessageTypes(), types);
			 if (hasTag && criticity == this.criticity) {
				 for (LoggerMessage loggerMessage : loggerMessages) {	
					loggerMessage.writeMessage(messages);
				 }
			 }
		 }
	 }
	 
	 /**
 	 * Contain tag.
 	 *
 	 * @param conditionTypes the condition types
 	 * @param givenTypes the given types
 	 * @return true, if successful
 	 */
 	public boolean containTag(LogMessageType[] conditionTypes, LogMessageType[] givenTypes) {
		 for (LogMessageType typeCondition : conditionTypes) {
			 for (LogMessageType typeGiven : givenTypes) {
				 if (typeCondition.equals(typeGiven)) {
					 return true;
				 }
			 }	
		 }
		 return false;
	 }
	 
	/**
	 * Checks if is context message.
	 *
	 * @param className the class name
	 * @return true, if is context message
	 */
	private boolean isContextMessage(String className) {
		try {
			String simpleClassName = Class.forName(className).getSimpleName();
			// to test whether the message is from class "context"
			if (simpleClassName.equals("context")) {
				return true;
			}
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}
	 
	 /**
 	 * Gets the crititity.
 	 *
 	 * @return the crititity
 	 */
 	public int getCrititity() {
		 return criticity;
	 }
	 
	 /**
 	 * Gets the log message types.
 	 *
 	 * @return the log message types
 	 */
 	public LogMessageType[] getLogMessageTypes() {
		 return types;
	 }
	 
	 /**
 	 * Checks if is console.
 	 *
 	 * @return true, if is console
 	 */
 	public boolean isConsole() {
		 return isConsole;
	 }
	 
	 /**
 	 * Checks if is file.
 	 *
 	 * @return true, if is file
 	 */
 	public boolean isFile() {
		 return isFile;
	 }
	 
	 /**
 	 * Sets the previous state.
 	 *
 	 * @param previousState the new previous state
 	 */
 	public void setPreviousState(boolean previousState) {
		 this.previousState = previousState;
	 }
	 
	 /**
 	 * Gets the previous state.
 	 *
 	 * @return the previous state
 	 */
 	public boolean getPreviousState() {
		 return previousState;
	 }
	 
	 /**
 	 * Sets the console panel.
 	 *
 	 * @param consolePanel the new console panel
 	 */
 	public void setConsolePanel(ConsolePanel consolePanel) {
		 this.consolePanel = consolePanel;
	 }
	 
	 /**
 	 * Gets the console panel.
 	 *
 	 * @return the console panel
 	 */
 	public ConsolePanel getConsolePanel() {
		 return consolePanel;
	 }
	 
	 /**
 	 * Gets the logger.
 	 *
 	 * @return the logger
 	 */
 	public Logger getLogger() {
		 return logger;
	 }
	 
	 /**
 	 * Gets the logger file name.
 	 *
 	 * @return the logger file name
 	 */
 	public String getLoggerFileName() {
		 return "resources/myLog"+fileDate+".out";
	 }
	 
	 /**
 	 * Sets the world.
 	 *
 	 * @param world the new world
 	 */
 	public void setWorld(World world) {
		 this.world = world;
	 }
	 
	 /**
 	 * Sets the temporal graph.
 	 *
 	 * @param temporalGraph the new temporal graph
 	 */
 	public void setTemporalGraph(TemporalGraph temporalGraph) {
		 this.temporalGraph = temporalGraph;
	 }
}
