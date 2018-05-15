package VISUALIZATION.log;

// TODO: Auto-generated Javadoc
/**
 * The Class LoggerFile.
 */
public class LoggerFile extends LoggerMessage{

	/**
	 * Instantiates a new logger file.
	 *
	 * @param logFile the log file
	 */
	public LoggerFile(LogFile logFile) {
		this.logFile = logFile;
		this.logFile.attach(this);
	}
	
	/* (non-Javadoc)
	 * @see log.LoggerMessage#writeMessage(java.lang.String)
	 */
	@Override
	public void writeMessage(String messages) {
		// TODO Auto-generated method stub
		if (logFile.isFile()) {
			logFile.getLogger().debug(messages);
		}
	}

}
