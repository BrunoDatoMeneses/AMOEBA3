package log;

// TODO: Auto-generated Javadoc
/**
 * The Class LoggerMessage.
 */
public abstract class LoggerMessage {
	
	/** The log file. */
	protected LogFile logFile;
	
	/**
	 * Write message.
	 *
	 * @param messages the messages
	 */
	public abstract void writeMessage(String messages);

}
