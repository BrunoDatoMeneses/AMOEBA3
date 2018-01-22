package log;

// TODO: Auto-generated Javadoc
/**
 * The Class LoggerConsole.
 */
public class LoggerConsole extends LoggerMessage {

	/**
	 * Instantiates a new logger console.
	 *
	 * @param logFile the log file
	 */
	public LoggerConsole(LogFile logFile) {
		this.logFile = logFile;
		this.logFile.attach(this);
	}
	
	/* (non-Javadoc)
	 * @see log.LoggerMessage#writeMessage(java.lang.String)
	 */
	@Override
	public void writeMessage(String messages) {
		// TODO Auto-generated method stub
		if (logFile.isConsole()) {
			 System.out.println("Log: " + messages);
			 logFile.getConsolePanel().writeInConsole(messages);
		}
		
	}

}
