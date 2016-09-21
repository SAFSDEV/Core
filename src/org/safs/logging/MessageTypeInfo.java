package org.safs.logging;

import java.util.*;

/**
 * This helper class encapsulates all constant information related to a
 * particular message type, including the STAF level that the type maps to, the 
 * prefix used in the text log, and the type string used in the xml log.
 * <p>
 * To get all constant information associated with a message type in a
 * <code>MessageTypeInfo</code>, use the static <code>get</code> method:
 * <p>
 * <code>MessageTypeInfo info = MessageTypeInfo.get(msgType);</code>
 */
public class MessageTypeInfo
{
	// XML message prefix per messageType
	public static final String DEBUG_MESSAGE_XML_PREFIX 		= "DEBUG";
	public static final String GENERIC_MESSAGE_XML_PREFIX 		= "GENERIC";
	public static final String SKIPPED_TEST_XML_PREFIX 			= "SKIPPED RECORD";
	public static final String FAILED_MESSAGE_XML_PREFIX 		= "FAILED";
	public static final String FAILED_OK_MESSAGE_XML_PREFIX 	= "FAILURE EXPECTED";
	public static final String PASSED_MESSAGE_XML_PREFIX 		= "PASSED";
	public static final String WARNING_MESSAGE_XML_PREFIX 		= "WARNING";
	public static final String WARNING_OK_MESSAGE_XML_PREFIX 	= "WARNING EXPECTED";
	public static final String START_DATATABLE_XML_PREFIX 		= "START DATATABLE";
	public static final String END_DATATABLE_XML_PREFIX 		= "END DATATABLE";
	public static final String START_PROCEDURE_XML_PREFIX 		= "START PROCEDURE";
	public static final String END_PROCEDURE_XML_PREFIX 		= "STOP PROCEDURE";
	public static final String START_TESTCASE_XML_PREFIX 		= "START TESTCASE";
	public static final String END_TESTCASE_XML_PREFIX 			= "STOP TESTCASE";
	public static final String START_SUITE_XML_PREFIX 			= "START SUITE";
	public static final String END_SUITE_XML_PREFIX 			= "STOP SUITE";
	public static final String START_COUNTER_XML_PREFIX 		= "START COUNTER";
	public static final String END_COUNTER_XML_PREFIX 			= "STOP COUNTER";
	public static final String SUSPEND_COUNTERS_XML_PREFIX 		= "SUSPEND COUNTERS";
	public static final String RESUME_COUNTERS_XML_PREFIX 		= "RESUME COUNTERS";
	public static final String START_CYCLE_XML_PREFIX 			= "START CYCLE";
	public static final String END_CYCLE_XML_PREFIX 			= "STOP CYCLE";
	public static final String START_LOGGING_XML_PREFIX 		= "START LOGGING";
	public static final String STOP_LOGGING_XML_PREFIX 			= "STOP LOGGING";
	public static final String START_REQUIREMENT_XML_PREFIX 	= "START REQUIREMENT";
	public static final String END_REQUIREMENT_XML_PREFIX 		= "STOP REQUIREMENT";
	public static final String STATUS_REPORT_START_XML_PREFIX 	= "BEGIN STATUS REPORT";
	public static final String STATUS_REPORT_RECORDS_XML_PREFIX = "TOTAL RECORDS";
	public static final String STATUS_REPORT_SKIPPED_XML_PREFIX = "SKIPPED RECORDS";
	public static final String STATUS_REPORT_TESTS_XML_PREFIX 				= "TEST RECORDS";
	public static final String STATUS_REPORT_TEST_PASSES_XML_PREFIX 		= "TESTS PASSED";
	public static final String STATUS_REPORT_TEST_WARNINGS_XML_PREFIX 		= "TEST WARNINGS";
	public static final String STATUS_REPORT_TEST_FAILURES_XML_PREFIX 		= "TEST FAILURES";
	public static final String STATUS_REPORT_GENERAL_XML_PREFIX 			= "GENERAL TESTS";
	public static final String STATUS_REPORT_GENERAL_PASSES_XML_PREFIX 		= "GENERAL PASSED";
	public static final String STATUS_REPORT_GENERAL_WARNINGS_XML_PREFIX 	= "GENERAL WARNINGS";
	public static final String STATUS_REPORT_GENERAL_FAILURES_XML_PREFIX 	= "GENERAL FAILURES";
	public static final String STATUS_REPORT_IO_FAILURES_XML_PREFIX 		= "IO FAILURES";
	public static final String STATUS_REPORT_END_XML_PREFIX = "END STATUS REPORT";


	// standard message prefix per messageType 
	public static final String DEBUG_MESSAGE_PREFIX 		= "- DEBUG - ";
	public static final String GENERIC_MESSAGE_PREFIX 		= "          ";
	public static final String SKIPPED_TEST_PREFIX 			= "- SKIPPED ";
	public static final String FAILED_MESSAGE_PREFIX 		= "**FAILED**";
	public static final String FAILED_OK_MESSAGE_PREFIX 	= "FAILED OK ";
	public static final String PASSED_MESSAGE_PREFIX 		= "    OK    ";
	public static final String WARNING_MESSAGE_PREFIX 		= "- WARNING ";
	public static final String WARNING_OK_MESSAGE_PREFIX 	= " WARN OK  ";
	public static final String START_DATATABLE_PREFIX 		= "  ------  START DATATABLE: ";
	public static final String END_DATATABLE_PREFIX 		= "  ------  END DATATABLE: ";
	public static final String START_PROCEDURE_PREFIX 		= "  >>>>>>  START PROCEDURE ";
	public static final String END_PROCEDURE_PREFIX 		= "  <<<<<<  STOP PROCEDURE ";
	public static final String START_TESTCASE_PREFIX 		= "  >>>>>>  START TESTCASE ";
	public static final String END_TESTCASE_PREFIX 			= "  <<<<<<  STOP TESTCASE ";
	public static final String START_SUITE_PREFIX 			= "  >>>>>>  START SUITE ";
	public static final String END_SUITE_PREFIX 			= "  <<<<<<  STOP SUITE  ";
	public static final String START_COUNTER_PREFIX 		= "  >>>>>>  START STATUS COUNTER ";
	public static final String END_COUNTER_PREFIX 			= "  <<<<<<  STOP STATUS COUNTER ";
	public static final String SUSPEND_COUNTERS_PREFIX 		= "  <!<!<!  SUSPEND STATUS COUNTERS ";
	public static final String RESUME_COUNTERS_PREFIX 		= "  !>!>!>  RESUME STATUS COUNTERS ";
	public static final String START_CYCLE_PREFIX 			= "  >>>>>>  START CYCLE ";
	public static final String END_CYCLE_PREFIX 			= "  <<<<<<  STOP CYCLE ";
	public static final String START_LOGGING_PREFIX 		= "  ......  START LOGGING ";
	public static final String STOP_LOGGING_PREFIX 			= "  ......  STOP LOGGING ";
	public static final String START_REQUIREMENT_PREFIX 	= "  ......  START REQUIREMENT ";
	public static final String END_REQUIREMENT_PREFIX 		= "  ......  STOP REQUIREMENT ";

	public static final String STATUS_REPORT_START_PREFIX 				= "REPORT     BEGIN STATUS: ";
	public static final String STATUS_REPORT_RECORDS_PREFIX 			= "REPORT    TOTAL RECORDS: ";
	public static final String STATUS_REPORT_SKIPPED_PREFIX 			= "REPORT  SKIPPED RECORDS: ";
	public static final String STATUS_REPORT_TESTS_PREFIX 				= "REPORT     TEST RECORDS: ";
	public static final String STATUS_REPORT_TEST_PASSES_PREFIX 		= "REPORT     TESTS PASSED: ";
	public static final String STATUS_REPORT_TEST_WARNINGS_PREFIX 		= "REPORT    TEST WARNINGS: ";
	public static final String STATUS_REPORT_TEST_FAILURES_PREFIX 		= "REPORT    TEST FAILURES: ";
	public static final String STATUS_REPORT_GENERAL_PREFIX 			= "REPORT  GENERAL RECORDS: ";
	public static final String STATUS_REPORT_GENERAL_PASSES_PREFIX		= "REPORT   GENERAL PASSES: ";
	public static final String STATUS_REPORT_GENERAL_WARNINGS_PREFIX 	= "REPORT GENERAL WARNINGS: ";
	public static final String STATUS_REPORT_GENERAL_FAILURES_PREFIX 	= "REPORT GENERAL FAILURES: ";
	public static final String STATUS_REPORT_IO_FAILURES_PREFIX 		= "REPORT      IO FAILURES: ";
	public static final String STATUS_REPORT_END_PREFIX = "REPORT       END STATUS: ";

	// contains a collection of contant info for all message types. the mapping
	// is done statically to maximize run-time performance.
	private static Hashtable types = new Hashtable(37);
	static
	{
		put(new MessageTypeInfo(AbstractLogFacility.START_PROCEDURE, "Start", START_PROCEDURE_PREFIX, START_PROCEDURE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.END_PROCEDURE, "Stop", END_PROCEDURE_PREFIX, END_PROCEDURE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.START_DATATABLE, "Start", START_DATATABLE_PREFIX, START_DATATABLE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.START_TESTCASE, "Start", START_TESTCASE_PREFIX, START_TESTCASE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.START_SUITE, "Start", START_SUITE_PREFIX, START_SUITE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.END_SUITE, "Stop", END_SUITE_PREFIX, END_SUITE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.END_TESTCASE, "Stop", END_TESTCASE_PREFIX, END_TESTCASE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.START_CYCLE, "Start", START_CYCLE_PREFIX, START_CYCLE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.END_CYCLE, "Stop", END_CYCLE_PREFIX, END_CYCLE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.START_COUNTER, "Start", START_COUNTER_PREFIX, START_COUNTER_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.END_COUNTER, "Stop", END_COUNTER_PREFIX, END_COUNTER_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.SUSPEND_STATUS_COUNTS, "Status", SUSPEND_COUNTERS_PREFIX, SUSPEND_COUNTERS_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.RESUME_STATUS_COUNTS, "Status", RESUME_COUNTERS_PREFIX, RESUME_COUNTERS_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.START_LOGGING, "Start", START_LOGGING_PREFIX, START_LOGGING_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STOP_LOGGING, "Stop", STOP_LOGGING_PREFIX, STOP_LOGGING_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STATUS_REPORT_START, "Status", STATUS_REPORT_START_PREFIX, STATUS_REPORT_START_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STATUS_REPORT_RECORDS, "Status", STATUS_REPORT_RECORDS_PREFIX, STATUS_REPORT_RECORDS_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STATUS_REPORT_SKIPPED, "Status", STATUS_REPORT_SKIPPED_PREFIX, STATUS_REPORT_SKIPPED_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STATUS_REPORT_TESTS, "Status", STATUS_REPORT_TESTS_PREFIX, STATUS_REPORT_TESTS_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STATUS_REPORT_TEST_PASSES, "Status", STATUS_REPORT_TEST_PASSES_PREFIX, STATUS_REPORT_TEST_PASSES_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STATUS_REPORT_TEST_WARNINGS, "Status", STATUS_REPORT_TEST_WARNINGS_PREFIX, STATUS_REPORT_TEST_WARNINGS_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STATUS_REPORT_TEST_FAILURES, "Status", STATUS_REPORT_TEST_FAILURES_PREFIX, STATUS_REPORT_TEST_FAILURES_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STATUS_REPORT_GENERAL, "Status", STATUS_REPORT_GENERAL_PREFIX, STATUS_REPORT_GENERAL_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STATUS_REPORT_GENERAL_PASSES, "Status", STATUS_REPORT_GENERAL_PASSES_PREFIX, STATUS_REPORT_GENERAL_PASSES_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STATUS_REPORT_GENERAL_WARNINGS, "Status", STATUS_REPORT_GENERAL_WARNINGS_PREFIX, STATUS_REPORT_GENERAL_WARNINGS_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STATUS_REPORT_GENERAL_FAILURES, "Status", STATUS_REPORT_GENERAL_FAILURES_PREFIX, STATUS_REPORT_GENERAL_FAILURES_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STATUS_REPORT_IO_FAILURES, "Status", STATUS_REPORT_IO_FAILURES_PREFIX, STATUS_REPORT_IO_FAILURES_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.STATUS_REPORT_END, "Status", STATUS_REPORT_END_PREFIX, STATUS_REPORT_END_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.START_REQUIREMENT, "Start", START_REQUIREMENT_PREFIX, START_REQUIREMENT_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.END_REQUIREMENT, "Stop", END_REQUIREMENT_PREFIX, END_REQUIREMENT_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.SKIPPED_TEST_MESSAGE, "User1", SKIPPED_TEST_PREFIX, SKIPPED_TEST_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.END_DATATABLE, "Stop", END_DATATABLE_PREFIX, END_DATATABLE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.DEBUG_MESSAGE, "Debug", DEBUG_MESSAGE_PREFIX, DEBUG_MESSAGE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.GENERIC_MESSAGE, "Info", GENERIC_MESSAGE_PREFIX, GENERIC_MESSAGE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.FAILED_MESSAGE, "Fail", FAILED_MESSAGE_PREFIX, FAILED_MESSAGE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.FAILED_OK_MESSAGE, "Pass", FAILED_OK_MESSAGE_PREFIX, FAILED_OK_MESSAGE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.PASSED_MESSAGE, "Pass", PASSED_MESSAGE_PREFIX, PASSED_MESSAGE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.WARNING_MESSAGE, "Warning", WARNING_MESSAGE_PREFIX, WARNING_MESSAGE_XML_PREFIX));
		put(new MessageTypeInfo(AbstractLogFacility.WARNING_OK_MESSAGE, "Pass", WARNING_OK_MESSAGE_PREFIX, WARNING_OK_MESSAGE_XML_PREFIX));
	}

	/**
	 * The message type identifier. Valid values are defined by 
	 * <code>AbstractLogFacility</code>.
	 */
	public int type;
	/**
	 * STAF Log level that this message type maps to.
	 */
	public String stafLevel;
	/**
	 * Prefix string for this message type used in text log.
	 */
	public String textPrefix;
	/**
	 * Prefix string for this message type used in xml log.
	 */
	public String xmlPrefix;

	/**
	 * Creates a <code>MessageTypeInfo</code>.
	 * <p>
	 * This constructor is declared private to prevent object of this class from
	 * being created explicitly. The user should use the static <code>get</code>
	 * method to get a correctly populated instance of this class.
	 * <p>
	 * @param mt	the message type.
	 * @param level	the STAF Log service level this message type maps to.
	 * @param tp	the prefix string used in text log.
	 * @param xp	the prefix string used in xml log.
	 */
	private MessageTypeInfo(int mt, String level, String tp, String xp)
	{
		type = mt;
		stafLevel = level;
		textPrefix = tp;
		xmlPrefix = xp;
	}

	/**
	 * Puts a <code>MessageTypeInfo</code> in the internal hashtable.
	 * <p>
	 * @param info	the <code>MessageTypeInfo</code> to put.
	 * @return		the previous <code>MessageTypeInfo</code> mapped to the same
	 * 				message type identifier of <code>info</code>.
	 */
	private static MessageTypeInfo put(MessageTypeInfo info)
	{
		return (MessageTypeInfo)types.put((new Integer(info.type)), info);
	}

	/**
	 * Returns a <code>MessageTypeInfo</code> for a message type.
	 * <p>
	 * @param type	the message type.
	 * @return		the <code>MessageTypeInfo</code> for the specified type;
	 * 				<code>null</code> if <code>type</code> is not a standard
	 *              SAFS message type.
	 */
	public static MessageTypeInfo get(int type)
	{
		MessageTypeInfo info = (MessageTypeInfo)types.get(new Integer(type));
		if (info == null) return null;
		return new MessageTypeInfo(info.type, info.stafLevel, info.textPrefix, 
			info.xmlPrefix);
	}

	/**
	 * Tests if a message type "belongs to" a log level.
	 * <p>
	 * A message will not be written to a log if its type does not pass the test
	 * against the log level of that log.
	 * <p>
	 * @param type	the message type to test.
	 * @param level	the log level to test against. One of the 
	 * 				<code>LOGLEVEL</code> constants defined by
	 *              <code>AbstractLogFacility</code>.
	 * @return		<code>true</code> if <code>type</code> passes the test of
	 * 				<code>level</code>; <code>false</code> if not.
	 */
	public static boolean typeBelongsToLevel(int type, int level)
	{
		switch (level) 
		{
			case AbstractLogFacility.LOGLEVEL_DEBUG:
				return true;
			case AbstractLogFacility.LOGLEVEL_INFO:
				if (type != AbstractLogFacility.DEBUG_MESSAGE) return true;
				break;
			case AbstractLogFacility.LOGLEVEL_WARN:
				if (type == AbstractLogFacility.FAILED_MESSAGE ||
					type == AbstractLogFacility.WARNING_MESSAGE) return true;
				break;
			case AbstractLogFacility.LOGLEVEL_ERROR:
				if (type == AbstractLogFacility.FAILED_MESSAGE) return true;
				break;
			default:
				return true;
		}
		return false;
	}

}