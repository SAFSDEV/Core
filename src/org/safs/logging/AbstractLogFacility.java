package org.safs.logging;

/**
 * This class is the abstract representation of log facility -- a named set of
 * multiple logs of different type. The intention is to provide a way for 
 * writing to all of them with a single call.
 * <p>
 * Each log in a log facility is of different type and is enabled by setting its
 * corresponding bit of the log mode identifier passed to the constructor of log
 * facility class. Use the bitwise-OR operator and the <code>LOGMODE</code> 
 * constants to enable multiple logs:
 * <p>
 * <code>long mode = AbstractLogFacility.LOGMODE_TOOL |
 * AbstractLogFacility.LOGMODE_SAFS_TEXT;</code>
 * <p>
 * Log message consists of the main message and an optional descriptive message,
 * and it is of a specific message type (available message types are defined by 
 * constants of this class). Each message type is mapped to a log level. The log
 * level of the log facility determines what messages are actually written to 
 * the logs based on the level that thier type maps to. Available levels are 
 * defined by the <code>LOGLEVEL</code> constants.
 * <p>
 * The <code>{@link #logMessage logMessage}</code> and 
 * <code>{@link #close close}</code> methods of this class are abstract. Both
 * SAFS and SAFS-enabled tools are expected to extend this class to provide
 * the concrete implmentation of their logging functionalities. For example,
 * <code>{@link org.safs.staf.service.logging.SLSLogFacility}</code> extends
 * this class and implements standard SAFS logging using STAF. A tool library 
 * should implement these methods to provide tool-specific logging, in addition 
 * to making calls to standard SAFS logging.
 */
public abstract class AbstractLogFacility
{
	// message type identifiers
	public static final int START_PROCEDURE = 1;
	public static final int END_PROCEDURE = 2;
	public static final int START_DATATABLE = 3;
	public static final int START_TESTCASE = 4;
	public static final int START_SUITE = 5;
	public static final int END_SUITE = 6;

	public static final int END_TESTCASE = 8;
	public static final int START_CYCLE = 9;
	public static final int END_CYCLE = 10;
	public static final int START_COUNTER = 11;
	public static final int END_COUNTER = 12;

	public static final int SUSPEND_STATUS_COUNTS = 13;
	public static final int RESUME_STATUS_COUNTS = 14;

	public static final int START_LOGGING = 16;
	public static final int STOP_LOGGING = 32;

	public static final int STATUS_REPORT_START = 17;
	public static final int STATUS_REPORT_RECORDS = 18;
	public static final int STATUS_REPORT_SKIPPED = 19;
	public static final int STATUS_REPORT_TESTS = 20;
	public static final int STATUS_REPORT_TEST_PASSES = 21;
	public static final int STATUS_REPORT_TEST_WARNINGS = 22;
	public static final int STATUS_REPORT_TEST_FAILURES = 23;
	public static final int STATUS_REPORT_GENERAL_WARNINGS = 24;
	public static final int STATUS_REPORT_GENERAL_FAILURES = 25;
	public static final int STATUS_REPORT_IO_FAILURES = 26;
	public static final int STATUS_REPORT_GENERAL = 27;
	public static final int STATUS_REPORT_GENERAL_PASSES = 28;
	//public static  final int STATUS_REPORT_RESERVED = 29;
	//public static  final int STATUS_REPORT_RESERVED = 30;
	public static final int STATUS_REPORT_END = 31;

	public static final int START_REQUIREMENT = 64;
	public static final int END_REQUIREMENT = 128;
	public static final int SKIPPED_TEST_MESSAGE = 256;
	public static final int END_DATATABLE = 512;

	public static final int DEBUG_MESSAGE = 7;
	public static final int GENERIC_MESSAGE = 0;
	public static final int FAILED_MESSAGE = 1024;
	public static final int FAILED_OK_MESSAGE = 1025;
	public static final int PASSED_MESSAGE = 2048;
	public static final int WARNING_MESSAGE = 4096;
	public static final int WARNING_OK_MESSAGE = 4097;

	public static final int CUSTOM_MESSAGE = 10000;

	/**
	 * "0", Only failed messages will be logged.
	 */
	public static final int LOGLEVEL_ERROR = 0;
	/**
	 * "1", Only failed or warning messages will logged.
	 */
	public static final int LOGLEVEL_WARN = 1;
	/**
	 * "2", All except debug messages will be logged.
	 */
	public static final int LOGLEVEL_INFO = 2;
	/**
	 * "3", All messages will be logged.
	 */
	public static final int LOGLEVEL_DEBUG = 3;

	/**
	 * "0", Disable all logs.
	 * Not to be used as <code>LogItem</code> type.
	 */
	public static final long LOGMODE_DISABLED = 0;
	/**
	 * "1", Bit flag constant for enabling tool specific log.
	 * Also identifies a <code>LogItem</code> as a tool-specific log.
	 */
	public static final long LOGMODE_TOOL = 1;
	/**
	 * "8", Bit flag constant for enabling tool's console log.
	 * Also identifies a <code>LogItem</code> as a tool-specific console log.
	 */
	public static final long LOGMODE_CONSOLE = 8;
	/**
	 * "32", Bit flag constant for enabling standard SAFS text file log.
	 * Also identifies a <code>LogItem</code> as standard SAFS text file log.
	 */
	public static final long LOGMODE_SAFS_TEXT = 32;
	/**
	 * "64", Bit flag constant for enabling standard SAFS xml file log.
	 * Also identifies a <code>LogItem</code> as standard SAFS xml file log.
	 */
	public static final long LOGMODE_SAFS_XML = 64;
	/**
	 * "127", Enable all logs.
	 * Not to be used as <code>LogItem</code> type.
	 */
	public static final long LOGMODE_MAX = 127;

	/**
	 * Default log facility name
	 */
	protected static final String DEFAULT_FAC_NAME = "SAFSLOG";
	/**
	 * Default name for standard STAF text log
	 */
	protected static final String DEFAULT_SAFS_TEXT_NAME = "SAFSLOG.TXT";
	/**
	 * Default file name for standard STAF text log
	 */
	protected static final String DEFAULT_SAFS_TEXT_FILE = "SAFSLOG.TXT";
	/**
	 * Default name for standard STAF xml log
	 */
	protected static final String DEFAULT_SAFS_XML_NAME = "SAFSLOG.XML";
	/**
	 * Default file name for standard STAF xml log
	 */
	protected static final String DEFAULT_SAFS_XML_FILE = "SAFSLOG.XML";

	protected static final String DEFAULT_XML_LOG_HEADER = "SAFS_LOG_HEADER.XML";
	protected static final String DEFAULT_XML_LOG_FOOTER = "SAFS_LOG_FOOTER.XML";

	protected String facName = "";
	protected long logMode = 0;
	protected int logLevel = 0;
	protected String linkedFac = "";
	protected boolean suspended = false;

	/**
	 * Creates a new <code>AbstractLogFacility</code>.
	 * <p>
	 * @param name		the name of this log facility.
	 * @param mode		the log mode.
	 * @param level		the log level.
	 * @param linked	the name of another log facility linked to this one.
	 **/
	public AbstractLogFacility(String name, long mode, int level, String linked)
	{
		facName = (name == null || name.length() <= 0)? DEFAULT_FAC_NAME : name;
		logMode = mode;
		logLevel = level;
		linkedFac = (linked == null)? "" : linked;
		suspended = false;
	}

	/**
	 * Returns the name of this log facility.
	 * <p>
	 * @return the name of this log facility.
	 */
	public String getFacName()
	{
		return facName;
	}

	/**
	 * Returns the log mode of this log facility.
	 * <p>
	 * @return the bitwise-OR of one of more <code>LOGMODE</code> constants.
	 */
	public long getLogMode()
	{
		return logMode;
	}

	/**
	 * Sets the log mode of this log facility.
	 * <p>
	 * @param mode	the new log mode. Bitwise-OR of one or more 
	 * 				<code>LOGMODE</code> constants.
	 */
	public void setLogMode(long mode)
	{
		logMode = mode;
	}

	/**
	 * Returns the log level of this log facility.
	 * <p>
	 * @return one of the <code>LOGLEVEL</code> constants.
	 */
	public int getLogLevel()
	{
		return logLevel;
	}

	/**
	 * Sets the log level of this log facility.
	 * <p>
	 * @param level	the new log level. Must be one of the <code>LOGLEVEL</code>
	 * 				constants.
	 */
	public void setLogLevel(int level)
	{
		logLevel = level;
	}

	/**
	 * Returns the name of the log facility linked to this one.
	 * <p>
	 * @return the name of the linked log facility.
	 */
	public String getLinkedFacName()
	{
		return linkedFac;
	}

	/**
	 * Tests if the specified log mode is enabled.
	 * <p>
	 * @param mode	the mode to test. Must be one of the <code>LOGMODE</code> 
	 * 				constants.
	 * @return		<code>true</code> if <code>mode</code> is enabled;
	 * 				<code>false</code> if not.
	 */
	public boolean isModeEnabled(long mode)
	{
		return ((mode & logMode) > 0);
	}

	/**
	 * Suspends all logging this log facility.
	 * <p>
	 * Incoming log messages are discarded until resumed.
	 */
	 public void suspend()
	 {
		 suspended = true;
	 }

	/**
	 * Resumes logging to this log facility.
	 */
	 public void resume()
	 {
		 suspended = false;
	 }

	/**
	 * Tests if this log facility is currently suspended.
	 * <p>
	 * @return	<code>true</code> if suspended; <code>false</code> if not.
	 */
	public boolean isSuspended()
	{
		return suspended;
	}

	/**
	 * Logs a message to all the enabled logs.
	 * <p>
	 * @param msg		the message to log.
	 * @param desc		additional description to log.
	 * @param msgType	the int identifier of the type of message being logged.
	 */
	public abstract void logMessage(String msg,	String desc, int msgType);

	/**
	 * Closes all logs of this log facility.
	 * <p>
	 * @throws	LogException
	 * 			if this log facility failed to close for any reason.
	 */
	public abstract void close() throws LogException;

	/**
	 * @return the name of this log facility.
	 */
	public String toString()
	{
		return facName;
	}
}