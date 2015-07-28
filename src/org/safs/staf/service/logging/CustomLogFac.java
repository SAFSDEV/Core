package org.safs.staf.service.logging;

import org.safs.staf.embedded.HandleInterface;

import com.ibm.staf.*;

/**
 * This class and <code>AbstractSAFSCustomLoggingService</code> provide a sample custom
 * logging service implementation that writes directly to the STAF logs.
 * 
 * @see AbstractSAFSCustomLoggingService
 * @see org.safs.staf.service.logging.v2.SAFSCustomLoggingService
 * @see org.safs.staf.service.logging.v3.SAFSCustomLoggingService3
 */
public class CustomLogFac
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
	//public static  final int STATUS_REPORT_RESERVED = 27;
	//public static  final int STATUS_REPORT_RESERVED = 28;
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
	 * Disable all logs.
	 * Not to be used as <code>LogItem</code> type.
	 */
	public static final long LOGMODE_DISABLED = 0;
	/**
	 * Bit flag constant for enabling tool specific log.
	 * Also identifies a <code>LogItem</code> as a tool-specific log.
	 */
	public static final long LOGMODE_TOOL = 1;
	/**
	 * Bit flag constant for enabling tool's console log.
	 * Also identifies a <code>LogItem</code> as a tool-specific console log.
	 */
	public static final long LOGMODE_CONSOLE = 8;
	/**
	 * Bit flag constant for enabling standard SAFS text file log.
	 * Also identifies a <code>LogItem</code> as standard SAFS text file log.
	 */
	public static final long LOGMODE_SAFS_TEXT = 32;
	/**
	 * Bit flag constant for enabling standard SAFS xml file log.
	 * Also identifies a <code>LogItem</code> as standard SAFS xml file log.
	 */
	public static final long LOGMODE_SAFS_XML = 64;
	/**
	 * Enable all logs.
	 * Not to be used as <code>LogItem</code> type.
	 */
	public static final long LOGMODE_MAX = 127;

	private String facName;
	private long logMode;
	private String linkedFac;
	private HandleInterface handle;
	private String stafLogService;

	private String txtLog;
	private String xmlLog;

	/**
	 * Creates a <code>CustomLogFac</code>.
	 * <p>
	 * @param name		the name of this custom log facility.
	 * @param mode		the log mode of this custom log facility.
	 * @param linked	the name of the log facility linked to this one.
	 * @param h			the STAF handle for interacting with STAF.
	 * @param stafLog	the name of the STAF Log service loaded by standard
	 * 					logging service.
	 */
	public CustomLogFac(String name, long mode, String linked, HandleInterface h, 
		String stafLog)
	{
		facName = name;
		logMode = mode;
		linkedFac = (linked == null)? "" : linked;
		handle = h;
		stafLogService = stafLog;

		// these two are STAF log names for the text and xml logs respectively
		txtLog = facName + ".txt";
		xmlLog = facName + ".xml";

		// TODO: implement custom log facility initialization here.
		// This is executed after the standard initialization procedure.

/********************* SAMPLE IMPLEMENTATION **********************************/

		stafTextLog("Custom init.");
		stafXmlLog("<CUSTOM_INIT/>");

/********************* SAMPLE IMPLEMENTATION **********************************/
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
	 * Logs a message to all the enabled logs.
	 * <p>
	 * @param msg		the message to log.
	 * @param desc		additional description to log.
	 * @param msgType	the int identifier of the type of message being logged.
	 * @return			a STAFResult indicating the result of the operation and 
	 * 					whether normal logging is to be bypassed. Normal logging
	 *                  will be bypassed if rc of the return value is 
	 *                  <code>STAFResult.Ok</code> and the result buffer is
	 *                  "BYPASS".
	 */
	public STAFResult logMessage(String msg, String desc, int msgType)
	{
		// TODO: implement custom logging here.
		// This is executed before standard logging procedure.

/********************* SAMPLE IMPLEMENTATION **********************************/

		if (isModeEnabled(LOGMODE_SAFS_TEXT))
			logTextMessage(msg, desc, msgType);

		if (isModeEnabled(LOGMODE_SAFS_XML))
			logXmlMessage(msg, desc, msgType);

		if (msgType >= CUSTOM_MESSAGE)
			return new STAFResult(STAFResult.Ok, "BYPASS");

		return new STAFResult(STAFResult.Ok);

/********************* SAMPLE IMPLEMENTATION **********************************/

	}

	/**
	 * Logs custom message to the text STAF log.
	 * <p>
	 * @param msg		the message to log.
	 * @param desc		additional description to log.
	 * @param msgType	the int identifier of the type of message being logged.
	 */
	private void logTextMessage(String msg, String desc, int msgType)
	{
		if (msgType >= CUSTOM_MESSAGE)
			stafTextLog("Custom text message: " +
				"msg=" + msg + ";desc=" + desc + ";type=" + msgType);
	}

	/**
	 * Logs custom message to the xml STAF log.
	 * <p>
	 * @param msg		the message to log.
	 * @param desc		additional description to log.
	 * @param msgType	the int identifier of the type of message being logged.
	 */
	private void logXmlMessage(String msg, String desc, int msgType)
	{
		if (msgType >= CUSTOM_MESSAGE)
			stafXmlLog("<CUSTOM_XML_MESSAGE>" +
				"msg=" + msg + ";desc=" + desc + ";type=" + msgType + 
				"</CUSTOM_XML_MESSAGE>");
	}

	/**
	 * Writes to the text STAF log.
	 * <p>
	 * @param msg	the message to write
	 * @return		the <code>STAFResult</code> of the operation.
	 */
	private STAFResult stafTextLog(String msg)
	{
		return handle.submit2("local", stafLogService, "log machine logname " + 
			txtLog + " level info message " + STAFUtil.wrapData(msg));
	}

	/**
	 * Writes to the xml STAF log.
	 * <p>
	 * @param msg	the message to write
	 * @return		the <code>STAFResult</code> of the operation.
	 */
	private STAFResult stafXmlLog(String msg)
	{
		return handle.submit2("local", stafLogService, "log machine logname " + 
			xmlLog + " level info message " + STAFUtil.wrapData(msg));
	}

	/**
	 * Closes all logs of this log facility.
	 * <p>
	 */
	public void close()
	{
		// TODO: implement custom log facility termination here.
		// This is executed before the standard termination procedure.

/********************* SAMPLE IMPLEMENTATION **********************************/

		stafTextLog("Custom close.");
		stafXmlLog("<CUSTOM_CLOSE/>");

/********************* SAMPLE IMPLEMENTATION **********************************/
	}
}