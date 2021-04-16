/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.safs.staf.service.logging;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.safs.Log;
import org.safs.STAFHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogItemDictionary;
import org.safs.staf.STAFHandleInterface;
import org.safs.staf.embedded.EmbeddedHandles;
import org.safs.staf.embedded.HandleInterface;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.ServiceDebugLog;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFCommandParseResult;
import com.ibm.staf.service.STAFCommandParser;

/**
 * <code>AbstractSAFSLoggingService</code> is an external STAF service that handles
 * standard SAFS logging.
 * This class is defined as an abstract class, for different version of STAF,
 * you may extends this class and implements the staf-version-related interface
 * (STAFServiceInterfaceLevel30 or STAFServiceInterfaceLevel3).
 * We have implemented the class for version 2 and 3: SAFSLoggingService and SAFSLoggingService3
 *
 * <p>
 * This service supports multiple processes (SAFS-aware testing tools) running
 * in support of the same test at the same time to write to the same log
 * facility (see <code>{@link org.safs.logging.AbstractLogFacility}</code>). It
 * provides all standard SAFS logging functions, including creating new log
 * facilities, interrogating and manipulating settings of existing log
 * facilities, logging messages etc. This service also keeps track of all log
 * facilities that it created. All tools should perform standard SAFS logging
 * via this service so that their logging actions can be coordinated.
 * <p>
 * Internally this service uses STAF LOG service to store messages for running
 * logs until they are closed, when the content of the STAF log is exported to
 * the destination. Upon initializing, this service will load a unique instance
 * of STAF LOG service to use for the temporary STAF logs (The name of this STAF
 * LOG service instance is "&lt;ServiceName>Log", where &lt;ServiceName> is the
 * registered name of this service).
 * <p>
 * This service can operate in two modes: local or remote, which is specified
 * when this service is registered with STAF (statically via STAF.cfg file or
 * dynamically using the SERVICE service). In local mode, logs are generated on
 * the local machine. In remote mode, logs are generated on a remote machine.
 * In this case, the remote machine must be running this service in local mode.
 * Settings such as the default log directory are all with respect to the remote
 * machine. The local service simply serves as a proxy to the remote service.
 * From a user's perspective, however, there is absolutely no difference at all
 * in how to submit requests to this service.
 * <p>
 * This service can also invoke custom logging functions, which must be
 * implemented as a STAF service. A custom logging service is required to expose
 * certain commands that this service will call. Custom logging can only be
 * enabled in local mode.
 * <p>
 * Registration of this service takes the following format:
 * <p>
 * <code>
 * SERVICE &lt;ServiceName> LIBRARY JSTAF EXECUTE &lt;ServiceJarFile> PARMS
 * [DIR &lt;LogDir>] [REMOTE &lt;RemoteMachine> [NAME &lt;RemoteServiceName>]]
 * [CUSTOMLOGGING &lt;CustomLoggingService>]
 * </code>
 * <p>
 * Example:
 * <p>
 * <code>
 * SERVICE SAFSLogs LIBRARY JSTAF EXECUTE c:\staf\services\SAFSLogs.jar PARMS
 * DIR c:\safslogs
 * </code><br>
 * <code>
 * SERVICE SAFSLogs LIBRARY JSTAF EXECUTE c:\staf\services\SAFSLogs.jar PARMS
 * DIR c:\safslogs CUSTOMLOGGING SAFSCustomLogs
 * </code><br>
 * <code>
 * SERVICE SAFSLogs LIBRARY JSTAF EXECUTE c:\staf\services\SAFSLogs.jar PARMS
 * REMOTE LogServer
 * </code>
 * <p>
 * This service provides the following commands:
 * <p>
 * <pre>
 * HANDLEID
 * INIT        &lt;facname> [TEXTLOG [&lt;altname>]] [XMLLOG [&lt;altname>]] [TOOLLOG] [CONSOLELOG] [ALL]
 *             [LINKEDFAC &lt;name>] [OVERWRITE] [CAPXML]
 *             [TRUNCATE [&lt;numchars | ON | OFF ]]
 * QUERY       &lt;facname> [TEXTLOG | XMLLOG | TOOLLOG | CONSOLELOG | ALL]
 * LIST        [SETTINGS]
 * SUSPENDLOG  &lt;facname> | ALL
 * RESUMELOG   &lt;facname> | ALL
 * LOGLEVEL    &lt;facname> [DEBUG | INFO | WARN | ERROR]
 * LOGMESSAGE  &lt;facname> MESSAGE &lt;msg> [DESCRIPTION &lt;desc>] [MSGTYPE &lt;msgType>]
 * TRUNCATE    [&lt;numchars | ON | OFF ]
 * CLOSE       &lt;facname> | ALL  [CAPXML]
 * HELP        [MSGTYPE]
 * VERSION
 * </pre>
 *
 * @see org.safs.logging.AbstractLogFacility
 * @see SLSLogFacility
 * @see org.safs.staf.service.logging.v2.SAFSCustomLoggingService
 * @see org.safs.staf.service.logging.v3.SAFSCustomLoggingService3
 * @see "SAFSLogs Service User's Guide"
 * @see org.safs.staf.service.logging.v2.SAFSLoggingService
 * @see org.safs.staf.service.logging.v3.SAFSLoggingService3
 *
 * @since   MAY 19, 2009	(LW)	Add two abstract methods getSTAFTextLogItem() and getSTAFXmlLogItem()
 * 									Modify method handleInit() so that the right STAF-version LogItem will be instantiated.
 */

public abstract class AbstractSAFSLoggingService {
	/**
	 * The name of the separate instance of STAF LOG service loaded and used
	 * solely by this service in local mode. This is built based on the name of
	 * the service to guarantee it is unique.
	 */
	public static String SLS_STAF_LOG_SERVICE_NAME = "";

	public static final String SLS_SERVICE_MODE_LOCAL = "LOCAL";
	public static final String SLS_SERVICE_MODE_REMOTE = "REMOTE";

	public static final String SLS_SERVICE_OPTION_DIR = "DIR";
	public static final String SLS_SERVICE_OPTION_CUSTOMLOGGING = "CUSTOMLOGGING";
	public static final String SLS_SERVICE_OPTION_REMOTE = "REMOTE";
	public static final String SLS_SERVICE_OPTION_NAME = "NAME";

	public static final String SLS_SERVICE_REQUEST_HANDLEID = "HANDLEID";
	public static final String SLS_SERVICE_REQUEST_INIT = "INIT";
	public static final String SLS_SERVICE_REQUEST_QUERY = "QUERY";
	public static final String SLS_SERVICE_REQUEST_LIST = "LIST";
	public static final String SLS_SERVICE_REQUEST_SUSPENDLOG = "SUSPENDLOG";
	public static final String SLS_SERVICE_REQUEST_RESUMELOG = "RESUMELOG";
	public static final String SLS_SERVICE_REQUEST_LOGLEVEL = "LOGLEVEL";
	public static final String SLS_SERVICE_REQUEST_LOGMESSAGE = "LOGMESSAGE";
	public static final String SLS_SERVICE_REQUEST_CLOSE = "CLOSE";
	public static final String SLS_SERVICE_REQUEST_HELP = "HELP";
	public static final String SLS_SERVICE_REQUEST_VERSION = "VERSION";
	public static final String SLS_SERVICE_REQUEST_TRUNCATE = "TRUNCATE";
	public static final String SLS_SERVICE_PARM_ON = "ON";
	public static final String SLS_SERVICE_PARM_OFF = "OFF";

	public static final String SLS_SERVICE_PARM_ALL = "ALL";
	public static final String SLS_SERVICE_PARM_TEXTLOG = "TEXTLOG";
	public static final String SLS_SERVICE_PARM_XMLLOG = "XMLLOG";
	public static final String SLS_SERVICE_PARM_TOOLLOG = "TOOLLOG";
	public static final String SLS_SERVICE_PARM_CONSOLELOG = "CONSOLELOG";
	public static final String SLS_SERVICE_PARM_LINKEDFAC = "LINKEDFAC";
	public static final String SLS_SERVICE_PARM_OVERWRITE = "OVERWRITE";
	public static final String SLS_SERVICE_PARM_CAPXML = "CAPXML";

	public static final String SLS_SERVICE_PARM_SETTINGS = "SETTINGS";

	public static final String SLS_SERVICE_PARM_DEBUG = "DEBUG";
	public static final String SLS_SERVICE_PARM_INFO = "INFO";
	public static final String SLS_SERVICE_PARM_WARN = "WARN";
	public static final String SLS_SERVICE_PARM_ERROR = "ERROR";

	public static final String SLS_SERVICE_PARM_MESSAGE = "MESSAGE";
	public static final String SLS_SERVICE_PARM_DESCRIPTION = "DESCRIPTION";
	public static final String SLS_SERVICE_PARM_MSGTYPE = "MSGTYPE";

	/**
	 * Prefix to the mode setting of this service in response to the LIST
	 * SETTINGS command.
	 */
	public static final String SLS_SETTINGS_MODE_PREFIX =
		"Mode           : ";
	/**
	 * Prefix to the remote machine setting of this service in response to the
	 * LIST SETTINGS command.
	 */
	public static final String SLS_SETTINGS_REMOTE_MACHINE_PREFIX =
		"Remote Machine : ";
	/**
	 * Prefix to the remote service setting of this service in response to the
	 * LIST SETTINGS command.
	 */
	public static final String SLS_SETTINGS_REMOTE_SERVICE_PREFIX =
		"Remote Service : ";
	/**
	 * Prefix to the default directory setting of this service in response to
	 * the LIST SETTINGS command.
	 */
	public static final String SLS_SETTINGS_DEFAULT_DIR_PREFIX =
		"Default Dir    : ";

	/**
	 * Prefix to the custom logging service setting of this service in response
	 * to the LIST SETTINGS command.
	 */
	public static final String SLS_SETTINGS_CUSTOM_LOGGING_PREFIX =
		"Custom Logging : ";

	/**
	 * Prefix to the toollog state of this service in the result buffer of
	 * every LOGMESSAGE command.
	 */
	public static final String SLS_STATES_TOOLLOG_PREFIX = "TOOLLOG=";
	/**
	 * Prefix to the consolelog state of this service in the result buffer of
	 * every LOGMESSAGE command.
	 */
	public static final String SLS_STATES_CONSOLELOG_PREFIX = "CONSOLELOG=";
	/**
	 * Prefix to the log level state of this service in the result buffer of
	 * every LOGMESSAGE command.
	 */
	public static final String SLS_STATES_LOGLEVEL_PREFIX = "LOGLEVEL=";

	public static final int SLS_TRUNCATELENGTH_DEFAULT = 128;

	public static final String VERSION_STR = "1.1";
	public static final String HELP_STR =
		"SAFSLoggingService HELP\n\n" +
		"HANDLEID\n\n" +
		"INIT        <facname> [TEXTLOG [<altname>]] [XMLLOG [<altname>]] [TOOLLOG]\n" +
		"            [CONSOLELOG] [ALL] [LINKEDFAC <name>] [OVERWRITE] [CAPXML]\n"+
		"            [TRUNCATE [<numchars> | ON | OFF ]]\n\n" +
		"QUERY       <facname> [TEXTLOG | XMLLOG | TOOLLOG | CONSOLELOG | ALL]\n\n" +
		"LIST        [SETTINGS]\n\n" +
		"SUSPENDLOG  <facname> | ALL\n\n" +
		"RESUMELOG   <facname> | ALL\n\n" +
		"LOGLEVEL    <facname> [DEBUG | INFO | WARN | ERROR]\n\n" +
		"LOGMESSAGE  <facname> MESSAGE <msg> [DESCRIPTION <desc>] [MSGTYPE <msgType>]\n\n" +
		"TRUNCATE    [numchars]" +
		"CLOSE       <facname> | ALL\n\n" +
		"HELP        [MSGTYPE]\n\n" +
		"VERSION";

	public static final String HELP_MSGTYPE_STR =
		"SAFSLoggingService LogMessage MSGTYPE Values:\n\n" +
		"PURPOSE                      VALUE\n" +
		"==================================\n" +
		"DEBUG_MESSAGE                    7\n" +
		"GENERIC_MESSAGE                  0\n" +
		"FAILED_MESSAGE                1024\n" +
		"PASSED_MESSAGE                2048\n" +
		"WARNING_MESSAGE               4096\n" +
		"FAILED_OK_MESSAGE             1025\n" +
		"WARNING_OK_MESSAGE            4097\n" +
		"START_LOGGING                   16\n" +
		"STOP_LOGGING                    32\n" +
		"START_DATATABLE                  3\n" +
		"END_DATATABLE                  512\n" +
		"START_PROCEDURE                  1\n" +
		"END_PROCEDURE                    2\n" +
		"START_TESTCASE                   4\n" +
		"END_TESTCASE                     8\n" +
		"START_SUITE                      5\n" +
		"END_SUITE                        6\n" +
		"START_CYCLE                      9\n" +
		"END_CYCLE                       10\n" +
		"START_COUNTER                   11\n" +
		"END_COUNTER                     12\n" +
		"SUSPEND_STATUS_COUNTS           13\n" +
		"RESUME_STATUS_COUNTS            14\n" +
		"STATUS_REPORT_START             17\n" +
		"STATUS_REPORT_RECORDS           18\n" +
		"STATUS_REPORT_SKIPPED           19\n" +
		"STATUS_REPORT_TESTS             20\n" +
		"STATUS_REPORT_TEST_PASSES       21\n" +
		"STATUS_REPORT_TEST_WARNINGS     22\n" +
		"STATUS_REPORT_TEST_FAILURES     23\n" +
		"STATUS_REPORT_GENERAL_WARNINGS  24\n" +
		"STATUS_REPORT_GENERAL_FAILURES  25\n" +
		"STATUS_REPORT_IO_FAILURES       26\n" +
		"STATUS_REPORT_FUTURE_RESERVED   27\n" +
		"STATUS_REPORT_FUTURE_RESERVED   28\n" +
		"STATUS_REPORT_FUTURE_RESERVED   29\n" +
		"STATUS_REPORT_FUTURE_RESERVED   30\n" +
		"STATUS_REPORT_END               31\n" +
		"START_REQUIREMENT               64\n" +
		"END_REQUIREMENT                128\n" +
		"SKIPPED_TEST_MESSAGE           256\n" +
		"CUSTOM_MESSAGE               10000\n";

	/**
	 * Stores all open log facilities created by this service.
	 */
	protected Hashtable logfacs = new Hashtable();

	protected static HandleInterface handle;

	protected STAFCommandParser initParser;
	protected STAFCommandParser queryParser;
	protected STAFCommandParser listParser;
	protected STAFCommandParser suspendLogParser;
	protected STAFCommandParser resumeLogParser;
	protected STAFCommandParser logLevelParser;
	protected STAFCommandParser logMessageParser;
	protected STAFCommandParser closeParser;
	protected STAFCommandParser helpParser;
	protected STAFCommandParser truncateParser;

	// The STAF registered process name of this service.
	protected String procName = "";
	protected String servicename = "";

	protected String mode;
	protected String defaultDir;
	protected String remoteMachine = null;
	protected String remoteService = null;
	protected String customService = null;

	protected boolean truncateLines = false;
	protected int truncateLength = 128; // default when truncation enabled.

	//After debugging the service, we should set the 2th parameter to false.
	protected ServiceDebugLog debugLog = new ServiceDebugLog(ServiceDebugLog.DEBUG_LOG_LOGGING, false);

	/**
	 * Loads the service initialization parameters.
	 * <p>
	 * @param info	the <code>InitInfo</code> from STAF.
	 * @return		a STAF RC.
	 * <p>
	 */
	protected int loadServiceInitParameters(InfoInterface.InitInfo info)
	{
		// build the parameter parser
		STAFCommandParser registrar = new STAFCommandParser(0, false);
		registrar.addOption(SLS_SERVICE_OPTION_DIR, 1,
			STAFCommandParser.VALUEREQUIRED);
		registrar.addOption(SLS_SERVICE_OPTION_REMOTE, 1,
			STAFCommandParser.VALUEREQUIRED);
		registrar.addOption(SLS_SERVICE_OPTION_NAME, 1,
			STAFCommandParser.VALUEREQUIRED);
		registrar.addOption(SLS_SERVICE_OPTION_CUSTOMLOGGING, 1,
			STAFCommandParser.VALUEREQUIRED);
		// if NAME is sepecified, REMOTE must be specified
		registrar.addOptionNeed(SLS_SERVICE_OPTION_NAME,
			SLS_SERVICE_OPTION_REMOTE);

		String parms = info.parms;
		STAFCommandParseResult parsedData = registrar.parse(parms);
		if (parsedData.rc != STAFResult.Ok)	return parsedData.rc;

		if (parsedData.optionTimes(SLS_SERVICE_OPTION_REMOTE) >0)
		{
			mode = SLS_SERVICE_MODE_REMOTE;
			remoteMachine = parsedData.optionValue(SLS_SERVICE_OPTION_REMOTE);
			if (parsedData.optionTimes(SLS_SERVICE_OPTION_NAME) > 0)
				remoteService = parsedData.optionValue(SLS_SERVICE_OPTION_NAME);
			else
				remoteService = info.name;
			// query the remote service for its default dir
			STAFResult result = handle.submit2(remoteMachine, remoteService,
				"list settings");
			if (result.rc != STAFResult.Ok) return result.rc;
			defaultDir = SLSSettings.parseSettings(result.result).defaultDir;
		}
		else
		{
			mode = SLS_SERVICE_MODE_LOCAL;
			if (parsedData.optionTimes(SLS_SERVICE_OPTION_DIR) > 0)
				defaultDir = parsedData.optionValue(SLS_SERVICE_OPTION_DIR);

			if (defaultDir.length() == 0)
			{
				// use {STAF/Config/STAFRoot}\data\log as the default directory
				STAFResult result = handle.submit2("local", "var",
					"resolve {STAF/Config/STAFRoot}\\data\\log");
				if (result.rc != result.Ok) return result.rc;
				defaultDir = result.result;
			}

			if (parsedData.optionTimes(SLS_SERVICE_OPTION_CUSTOMLOGGING) > 0)
			{
				customService = parsedData.optionValue(
					SLS_SERVICE_OPTION_CUSTOMLOGGING);
			}
		}

		return STAFResult.Ok;
	}

	protected void createInitParser()
	{
		initParser = new STAFCommandParser(0, false);
		initParser.addOption(SLS_SERVICE_REQUEST_INIT, 1,
			STAFCommandParser.VALUEREQUIRED);
		initParser.addOption(SLS_SERVICE_PARM_TEXTLOG, 1,
			STAFCommandParser.VALUEALLOWED);
		initParser.addOption(SLS_SERVICE_PARM_XMLLOG, 1,
			STAFCommandParser.VALUEALLOWED);
		initParser.addOption(SLS_SERVICE_PARM_TOOLLOG, 1,
			STAFCommandParser.VALUENOTALLOWED);
		initParser.addOption(SLS_SERVICE_PARM_CONSOLELOG, 1,
			STAFCommandParser.VALUENOTALLOWED);
		initParser.addOption(SLS_SERVICE_PARM_ALL, 1,
			STAFCommandParser.VALUENOTALLOWED);
		initParser.addOption(SLS_SERVICE_PARM_LINKEDFAC, 1,
			STAFCommandParser.VALUEREQUIRED);
		initParser.addOption(SLS_SERVICE_REQUEST_TRUNCATE, 1,
				STAFCommandParser.VALUEALLOWED);
		initParser.addOption(SLS_SERVICE_PARM_OVERWRITE, 1,
			STAFCommandParser.VALUENOTALLOWED);
		initParser.addOption(SLS_SERVICE_PARM_CAPXML, 1,
				STAFCommandParser.VALUENOTALLOWED);
	}

	protected void createQueryParser()
	{
		queryParser = new STAFCommandParser(0, false);
		queryParser.addOption(SLS_SERVICE_REQUEST_QUERY, 1,
			STAFCommandParser.VALUEREQUIRED);
		queryParser.addOption(SLS_SERVICE_PARM_TEXTLOG, 1,
			STAFCommandParser.VALUENOTALLOWED);
		queryParser.addOption(SLS_SERVICE_PARM_XMLLOG, 1,
			STAFCommandParser.VALUENOTALLOWED);
		queryParser.addOption(SLS_SERVICE_PARM_TOOLLOG, 1,
			STAFCommandParser.VALUENOTALLOWED);
		queryParser.addOption(SLS_SERVICE_PARM_CONSOLELOG, 1,
			STAFCommandParser.VALUENOTALLOWED);
		queryParser.addOption(SLS_SERVICE_PARM_ALL, 1,
			STAFCommandParser.VALUENOTALLOWED);
		// log mode. none or only one allowed
		queryParser.addOptionGroup(SLS_SERVICE_PARM_ALL + " "	+
			SLS_SERVICE_PARM_TEXTLOG + " " + SLS_SERVICE_PARM_XMLLOG + " " +
			SLS_SERVICE_PARM_TOOLLOG + " " + SLS_SERVICE_PARM_CONSOLELOG, 0, 1);
	}

	protected void createListParser()
	{
		listParser = new STAFCommandParser(0, false);
		listParser.addOption(SLS_SERVICE_REQUEST_LIST, 1,
			STAFCommandParser.VALUENOTALLOWED);
		listParser.addOption(SLS_SERVICE_PARM_SETTINGS, 1,
			STAFCommandParser.VALUENOTALLOWED);
	}

	protected void createSuspendLogParser()
	{
		suspendLogParser = new STAFCommandParser(0, false);
		suspendLogParser.addOption(SLS_SERVICE_REQUEST_SUSPENDLOG, 1,
			STAFCommandParser.VALUEALLOWED);
		suspendLogParser.addOption(SLS_SERVICE_PARM_ALL, 1,
			STAFCommandParser.VALUENOTALLOWED);
	}

	protected void createResumeParser()
	{
		resumeLogParser = new STAFCommandParser(0, false);
		resumeLogParser.addOption(SLS_SERVICE_REQUEST_RESUMELOG, 1,
			STAFCommandParser.VALUEALLOWED);
		resumeLogParser.addOption(SLS_SERVICE_PARM_ALL, 1,
			STAFCommandParser.VALUENOTALLOWED);
	}

	protected void createLogLevelParser()
	{
		logLevelParser = new STAFCommandParser(0, false);
		logLevelParser.addOption(SLS_SERVICE_REQUEST_LOGLEVEL, 1,
			STAFCommandParser.VALUEREQUIRED);
		logLevelParser.addOption(SLS_SERVICE_PARM_DEBUG, 1,
			STAFCommandParser.VALUENOTALLOWED);
		logLevelParser.addOption(SLS_SERVICE_PARM_INFO, 1,
			STAFCommandParser.VALUENOTALLOWED);
		logLevelParser.addOption(SLS_SERVICE_PARM_WARN, 1,
			STAFCommandParser.VALUENOTALLOWED);
		logLevelParser.addOption(SLS_SERVICE_PARM_ERROR, 1,
			STAFCommandParser.VALUENOTALLOWED);
		// none or only on of these levels allowed
		logLevelParser.addOptionGroup(SLS_SERVICE_PARM_DEBUG + " " +
			SLS_SERVICE_PARM_INFO + " " + SLS_SERVICE_PARM_WARN + " " +
			SLS_SERVICE_PARM_ERROR, 0, 1);
	}

	protected void createLogMessageParser()
	{
		logMessageParser = new STAFCommandParser(0, false);
		logMessageParser.addOption(SLS_SERVICE_REQUEST_LOGMESSAGE, 1,
			STAFCommandParser.VALUEREQUIRED);
		logMessageParser.addOption(SLS_SERVICE_PARM_MESSAGE, 1,
			STAFCommandParser.VALUEREQUIRED);
		logMessageParser.addOption(SLS_SERVICE_PARM_DESCRIPTION, 1,
			STAFCommandParser.VALUEREQUIRED);
		logMessageParser.addOption(SLS_SERVICE_PARM_MSGTYPE, 1,
			STAFCommandParser.VALUEREQUIRED);
		// MESSAGE option is required
		logMessageParser.addOptionNeed(SLS_SERVICE_REQUEST_LOGMESSAGE,
			SLS_SERVICE_PARM_MESSAGE);
	}

	protected void createCloseParser()
	{
		closeParser = new STAFCommandParser(0, false);
		closeParser.addOption(SLS_SERVICE_REQUEST_CLOSE, 1,
			STAFCommandParser.VALUEALLOWED);
		closeParser.addOption(SLS_SERVICE_PARM_ALL, 1,
			STAFCommandParser.VALUENOTALLOWED);
		closeParser.addOption(SLS_SERVICE_PARM_CAPXML, 1,
				STAFCommandParser.VALUENOTALLOWED);
	}

	protected void createTruncateParser()
	{
		truncateParser = new STAFCommandParser(0, false);
		truncateParser.addOption(SLS_SERVICE_REQUEST_TRUNCATE, 1,
			STAFCommandParser.VALUEALLOWED);
	}

	protected void createHelpParser()
	{
		helpParser = new STAFCommandParser(0, false);
		helpParser.addOption(SLS_SERVICE_REQUEST_HELP, 1,
			STAFCommandParser.VALUENOTALLOWED);
		helpParser.addOption(SLS_SERVICE_PARM_MSGTYPE, 1,
			STAFCommandParser.VALUENOTALLOWED);
	}

	protected STAFResult handleHandleId(
		InfoInterface.RequestInfo info)
	{
		return new STAFResult(STAFResult.Ok,
			String.valueOf(handle.getHandle()));
	}

	abstract protected AbstractSTAFTextLogItem getSTAFTextLogItem(String name,String directory,String filename);
	abstract protected AbstractSTAFXmlLogItem getSTAFXmlLogItem(String name,String directory,String filename);

	protected STAFResult handleInit(InfoInterface.RequestInfo info)
	{
		debugLog.debugPrintln("handleInit()");

		// validate the request
		STAFCommandParseResult parsedRequest = initParser.parse(info.request);
		if( parsedRequest.rc != STAFResult.Ok )
		{
			return new STAFResult( STAFResult.InvalidRequestString,
				parsedRequest.errorBuffer );
		}

		String facname = parsedRequest.optionValue(
			SLS_SERVICE_REQUEST_INIT).toUpperCase();

		if (logfacs.containsKey(facname))
			return new STAFResult(STAFResult.AlreadyExists, facname);

		// process log mode and file name
		long logmode = 0;
		AbstractSTAFTextLogItem txtLog = null;
		AbstractSTAFXmlLogItem xmlLog = null;
		boolean overwrite = (parsedRequest.optionTimes(SLS_SERVICE_PARM_OVERWRITE) > 0);
		boolean capXML = (parsedRequest.optionTimes(SLS_SERVICE_PARM_CAPXML) > 0);

		if( parsedRequest.optionTimes(SLS_SERVICE_REQUEST_TRUNCATE) > 0 ){
			String truncateval = parsedRequest.optionValue(SLS_SERVICE_REQUEST_TRUNCATE);
			if(truncateval == null){
				truncateLines = true;
			}else{
				if(SLS_SERVICE_PARM_ON.equalsIgnoreCase(truncateval))
					truncateLines = true;
				else if(SLS_SERVICE_PARM_OFF.equalsIgnoreCase(truncateval))
					truncateLines = false;
				else{
					try{
						truncateLength = Integer.parseInt(truncateval);
						if(truncateLength < 1){
							truncateLength = SLS_TRUNCATELENGTH_DEFAULT;
							return new STAFResult( STAFResult.InvalidRequestString,
									"TRUNCATE value cannot be < 1." );
						}
						truncateLines = true;
					}catch(Exception x){
						return new STAFResult( STAFResult.InvalidRequestString,
								"Optional TRUNCATE value must be > 0 | ON | OFF " );
					}
				}
			}
		}

		if (parsedRequest.optionTimes(SLS_SERVICE_PARM_ALL) > 0)
		{
			// ALL specified. ignore other log mode options
			logmode = SLSLogFacility.LOGMODE_MAX;
			txtLog = getSTAFTextLogItem(facname + ".txt", defaultDir, facname + ".txt");
			xmlLog = getSTAFXmlLogItem(facname + ".xml", defaultDir,  facname + ".xml");
			xmlLog.setCapXML(capXML);
		}
		else
		{
			// check individual log mode one by one
			if( parsedRequest.optionTimes(SLS_SERVICE_PARM_TEXTLOG) > 0 )
			{
				logmode = logmode | SLSLogFacility.LOGMODE_SAFS_TEXT;
				String altname = normalizeAltname(
					parsedRequest.optionValue(SLS_SERVICE_PARM_TEXTLOG),
					facname + ".txt");
				txtLog = getSTAFTextLogItem(facname + ".txt", defaultDir, altname);
			}
			if( parsedRequest.optionTimes(SLS_SERVICE_PARM_XMLLOG) > 0 )
			{
				logmode = logmode | SLSLogFacility.LOGMODE_SAFS_XML;
				String altname = normalizeAltname(
					parsedRequest.optionValue(SLS_SERVICE_PARM_XMLLOG),
					facname + ".xml");
				xmlLog = getSTAFXmlLogItem(facname + ".xml", defaultDir, altname);
				xmlLog.setCapXML(capXML);
			}
			// this service do not know how to handle tool-specific logs, so
			// just set the mode and do nothing else.
			if( parsedRequest.optionTimes(SLS_SERVICE_PARM_TOOLLOG) > 0 )
				logmode = logmode | SLSLogFacility.LOGMODE_TOOL;
			if( parsedRequest.optionTimes(SLS_SERVICE_PARM_CONSOLELOG) > 0 )
				logmode = logmode | SLSLogFacility.LOGMODE_CONSOLE;
		}
		LogItemDictionary logfiles = new LogItemDictionary();
		if( txtLog != null )
		{
			debugLog.debugPrintln(txtLog.getAbsolutePath());
			txtLog.setDebugLog(debugLog);
			logfiles.put(txtLog);
		}
		if( xmlLog != null )
		{
			debugLog.debugPrintln(xmlLog.getAbsolutePath());
			xmlLog.setDebugLog(debugLog);
			logfiles.put(xmlLog);
		}

		// get linked fac
		String linkedfac = null;
		if (parsedRequest.optionTimes(SLS_SERVICE_PARM_LINKEDFAC) > 0)
		{
			linkedfac = parsedRequest.optionValue(SLS_SERVICE_PARM_LINKEDFAC);
		}

		// create the log facility and set enabled logs on it.
		try
		{
			SLSLogFacility fac;
			if (mode == SLS_SERVICE_MODE_LOCAL)
			{
				if (!overwrite){
				    fac = new LocalSLSLogFacility(facname, logmode,
					SLSLogFacility.LOGLEVEL_INFO, linkedfac, handle, defaultDir,
					logfiles, customService,debugLog);
				}else{
				    fac = new LocalSLSLogFacility(facname, logmode,
					SLSLogFacility.LOGLEVEL_INFO, linkedfac, handle, defaultDir,
					logfiles, customService, overwrite,debugLog);
				}
			}
			else
			{
				if (!overwrite){
   	     		    fac = new RemoteSLSLogFacility(facname, logmode,
					SLSLogFacility.LOGLEVEL_INFO, linkedfac, handle, defaultDir,
					logfiles, remoteMachine, remoteService,debugLog);
				}else{
   	     		    fac = new RemoteSLSLogFacility(facname, logmode,
					SLSLogFacility.LOGLEVEL_INFO, linkedfac, handle, defaultDir,
					logfiles, remoteMachine, remoteService, overwrite,debugLog);
				}
			}
			if(capXML)fac.setSAFSXmlLogCapXML(capXML);
			logfacs.put(fac.getFacName(), fac);
		}
		catch (STAFLogException e)
		{
			return e.result;
		}

		return new STAFResult(STAFResult.Ok);
	}

	/**
	 * Makes sure the <code>altname</code> option value is a file.
	 * <p>
	 * The <code>altname</code> option in the INIT request can specify either a
	 * file or a directory. Directory spec ends with a name separator, e.g. "\"
	 * for Win32. In this case, <code>altname + filename</code> is returned as
	 * the file spec for the log file.
	 * <p>
	 * @param altname	the <code>altname</code> option value. Directory spec
	 * 				 	ends with <code>File.separator</code>.
	 * @param filename	the default file name to use to build the file spec in
	 * 					case <code>altname</code> is a directory. Ignored if
	 * 					<code>altname</code> is a file.
	 * @return			the normalized log file spec.
	 */
	protected String normalizeAltname(String altname, String filename)
	{
		if( altname == null || altname.length() == 0 )
			// no altname specified. use default filename.
			return filename;
		else if( altname.endsWith(File.separator) )
			// altname is a directory. append filename
			return altname + filename;
		else
			// altname is a file. just use it
			return altname;
	}

	protected STAFResult handleQuery(InfoInterface.RequestInfo info)
	{
		// validate the request
		STAFCommandParseResult parsedRequest = queryParser.parse(info.request);
		if( parsedRequest.rc != STAFResult.Ok )
		{
			return new STAFResult( STAFResult.InvalidRequestString,
				parsedRequest.errorBuffer );
		}

		String facname = parsedRequest.optionValue(
			SLS_SERVICE_REQUEST_QUERY).toUpperCase();

		if( !logfacs.containsKey(facname) )
			return new STAFResult(STAFResult.DoesNotExist, facname);

		SLSLogFacility logfac = (SLSLogFacility)logfacs.get(facname);

		String result = "FACNAME:              " + logfac.getFacName() + "\n" +
						"LINKED FAC:           " + logfac.getLinkedFacName() +
						"\n\n";
		if( parsedRequest.optionTimes(SLS_SERVICE_PARM_TOOLLOG) > 0 )
		{
			result += logfac.getToolLogStateString();
		}
		else if( parsedRequest.optionTimes(SLS_SERVICE_PARM_CONSOLELOG) > 0 )
		{
			result += logfac.getConsoleLogStateString();
		}
		else if( parsedRequest.optionTimes(SLS_SERVICE_PARM_TEXTLOG) > 0 )
		{
			result += logfac.getSAFSTextLogStateString();
		}
		else if( parsedRequest.optionTimes(SLS_SERVICE_PARM_XMLLOG) > 0 )
		{
			result += logfac.getSAFSXmlLogStateString();
		}
		else
		{
			result += logfac.getSAFSTextLogStateString() + "\n";
			result += logfac.getSAFSXmlLogStateString() + "\n";
			result += logfac.getToolLogStateString() + "\n";
			result += logfac.getConsoleLogStateString();
		}

		return new STAFResult(STAFResult.Ok, result);
	}

	protected STAFResult handleList(InfoInterface.RequestInfo info)
	{
		// validate the request
		STAFCommandParseResult parsedRequest = listParser.parse(info.request);
		if( parsedRequest.rc != STAFResult.Ok )
		{
			return new STAFResult( STAFResult.InvalidRequestString,
				parsedRequest.errorBuffer );
		}

		if (parsedRequest.optionTimes(SLS_SERVICE_PARM_SETTINGS) > 0)
		{
			return new STAFResult(STAFResult.Ok,
				SLS_SETTINGS_MODE_PREFIX + mode + "\n" +
				SLS_SETTINGS_REMOTE_MACHINE_PREFIX + remoteMachine + "\n" +
				SLS_SETTINGS_REMOTE_SERVICE_PREFIX + remoteService + "\n" +
				SLS_SETTINGS_DEFAULT_DIR_PREFIX + defaultDir + "\n" +
				SLS_SETTINGS_CUSTOM_LOGGING_PREFIX + customService + "\n");
		}

		String result;
		int count = logfacs.size();
		result = count + ";";
		for( Enumeration keys = logfacs.keys(); keys.hasMoreElements(); )
		{
			result += ((String) keys.nextElement()) + ";";
		}

		return new STAFResult(STAFResult.Ok, result);
	}

	protected STAFResult handleSuspendLog(
		InfoInterface.RequestInfo info)
	{
		// validate the request
		STAFCommandParseResult parsedRequest = suspendLogParser.parse(
			info.request);
		if( parsedRequest.rc != STAFResult.Ok )
		{
			return new STAFResult( STAFResult.InvalidRequestString,
				parsedRequest.errorBuffer );
		}

		if (parsedRequest.optionTimes(SLS_SERVICE_PARM_ALL) > 0)
		{
			for (Enumeration e = logfacs.elements(); e.hasMoreElements();)
			{
				((SLSLogFacility)e.nextElement()).suspend();
			}
		}
		else
		{
			String facname = parsedRequest.optionValue(
				SLS_SERVICE_REQUEST_SUSPENDLOG).toUpperCase();

			if (facname == null || facname.length() <=0)
				return new STAFResult(STAFResult.InvalidRequestString,
					"You must specifiy a <facname> or the ALL option.");

			if( !logfacs.containsKey(facname) )
				return new STAFResult(STAFResult.DoesNotExist, facname);

			((SLSLogFacility)logfacs.get(facname)).suspend();
		}

		return new STAFResult(STAFResult.Ok);
	}

	protected STAFResult handleResumeLog(
		InfoInterface.RequestInfo info)
	{
		// validate the request
		STAFCommandParseResult parsedRequest = resumeLogParser.parse(
			info.request);
		if( parsedRequest.rc != STAFResult.Ok )
		{
			return new STAFResult( STAFResult.InvalidRequestString,
				parsedRequest.errorBuffer );
		}

		if (parsedRequest.optionTimes(SLS_SERVICE_PARM_ALL) > 0)
		{
			for (Enumeration e = logfacs.elements(); e.hasMoreElements();)
			{
				((SLSLogFacility)e.nextElement()).resume();
			}
		}
		else
		{
			String facname = parsedRequest.optionValue(
				SLS_SERVICE_REQUEST_RESUMELOG).toUpperCase();

			if (facname == null || facname.length() <=0)
				return new STAFResult(STAFResult.InvalidRequestString,
					"You must specifiy a <facname> or the ALL option.");

			if( !logfacs.containsKey(facname) )
				return new STAFResult(STAFResult.DoesNotExist, facname);

			((SLSLogFacility)logfacs.get(facname)).resume();
		}

		return new STAFResult(STAFResult.Ok);
	}

	protected STAFResult handleLogLevel(
		InfoInterface.RequestInfo info)
	{
		// validate the request
		STAFCommandParseResult parsedRequest = logLevelParser.parse(
			info.request);
		if( parsedRequest.rc != STAFResult.Ok )
		{
			return new STAFResult( STAFResult.InvalidRequestString,
				parsedRequest.errorBuffer );
		}

		String facname = parsedRequest.optionValue(
			SLS_SERVICE_REQUEST_LOGLEVEL).toUpperCase();

		if( !logfacs.containsKey(facname) )
			return new STAFResult(STAFResult.DoesNotExist, facname);

		SLSLogFacility logfac = (SLSLogFacility)logfacs.get(facname);
		String result = "";
		if (parsedRequest.optionTimes(SLS_SERVICE_PARM_DEBUG) > 0)
			logfac.setLogLevel(AbstractLogFacility.LOGLEVEL_DEBUG);
		else if (parsedRequest.optionTimes(SLS_SERVICE_PARM_INFO) > 0)
			logfac.setLogLevel(AbstractLogFacility.LOGLEVEL_INFO);
		else if (parsedRequest.optionTimes(SLS_SERVICE_PARM_WARN) > 0)
			logfac.setLogLevel(AbstractLogFacility.LOGLEVEL_WARN);
		else if (parsedRequest.optionTimes(SLS_SERVICE_PARM_ERROR) > 0)
			logfac.setLogLevel(AbstractLogFacility.LOGLEVEL_ERROR);
		else
			switch (logfac.getLogLevel())
			{
				case AbstractLogFacility.LOGLEVEL_DEBUG:
					result = SLS_SERVICE_PARM_DEBUG;
					break;
				case AbstractLogFacility.LOGLEVEL_INFO:
					result = SLS_SERVICE_PARM_INFO;
					break;
				case AbstractLogFacility.LOGLEVEL_WARN:
					result = SLS_SERVICE_PARM_WARN;
					break;
				case AbstractLogFacility.LOGLEVEL_ERROR:
					result = SLS_SERVICE_PARM_ERROR;
					break;
				default:
					result = "UNKNOWN";
					break;
			}

		return new STAFResult(STAFResult.Ok, result);
	}

	protected STAFResult handleLogMessage(
		InfoInterface.RequestInfo info)
	{
		// validate the request
		STAFCommandParseResult parsedRequest = logMessageParser.parse(
			info.request);
		if( parsedRequest.rc != STAFResult.Ok )
		{
			return new STAFResult( STAFResult.InvalidRequestString,
				parsedRequest.errorBuffer );
		}

		String facname = parsedRequest.optionValue(
			SLS_SERVICE_REQUEST_LOGMESSAGE).toUpperCase();

		if( !logfacs.containsKey(facname) )
			return new STAFResult(STAFResult.DoesNotExist, facname);

		SLSLogFacility logfac = (SLSLogFacility)logfacs.get(facname);
		String msg = parsedRequest.optionValue(SLS_SERVICE_PARM_MESSAGE);
		String desc = null;
		int msgType = SLSLogFacility.GENERIC_MESSAGE;
		if( parsedRequest.optionTimes(SLS_SERVICE_PARM_DESCRIPTION) > 0 )
		{
			desc = parsedRequest.optionValue(SLS_SERVICE_PARM_DESCRIPTION);
		}
		if( parsedRequest.optionTimes(SLS_SERVICE_PARM_MSGTYPE) > 0 )
		{
			String mts = parsedRequest.optionValue(SLS_SERVICE_PARM_MSGTYPE);
			try
			{
				msgType = Integer.parseInt(mts);
			}
			catch( Exception e )
			{
				return new STAFResult(STAFResult.InvalidValue, mts);
			}
		}
		if(truncateLines){
			try{ if (msg  != null && msg.length() > truncateLength) msg = msg.substring(0, truncateLength);}catch(Exception ignore){}
			try{ if (desc != null && desc.length()> truncateLength) desc=desc.substring(0, truncateLength);}catch(Exception ignore){}
		}
		logfac.logMessage(msg, desc, msgType);
		//Call SAFSVARS Service to store the log message in a global variable
		_saveMessageToGlobalVariable(msg, desc, msgType);

		return new STAFResult(STAFResult.Ok, logfac.getStatesString());
	}

	/**
	 * Store the log message to global variables.<br>
	 * @param message		String, the log message
	 * @param description	String, the description for the log
	 * @param msgType		int,	the type of the log message
	 *
	 * <br> SEP 10, 2013	(Lei Wang)	Warp message/description in format :len:var, before passing it to SAFSVARS.
	 */
	private void _saveMessageToGlobalVariable(String message, String description, int msgType){
		//We save the last log message to global variables ONLY when the message type is 'failed' or 'error'
		//For other types, do we need to save them???
		try{
			if(AbstractLogFacility.FAILED_MESSAGE==msgType ||
			    AbstractLogFacility.FAILED_OK_MESSAGE==msgType ||
			    AbstractLogFacility.WARNING_MESSAGE==msgType ||
			    AbstractLogFacility.WARNING_OK_MESSAGE==msgType){

				//Before storing the message/description to variable, replace the separator by spaces.
				String separators = _getVariableValue(STAFHelper.SAFS_HOOK_TRD+STAFHelper.SAFS_VAR_SEPARATOR);
				Log.debug("Current record separator is '"+separators+"'");

				_setVariableValue(STAFHelper.SAFS_VAR_GLOBAL_LAST_LOG_MSG, _replaceSeparator(message, separators));
				_setVariableValue(STAFHelper.SAFS_VAR_GLOBAL_LAST_LOG_DESC, _replaceSeparator(description, separators));
				_setVariableValue(STAFHelper.SAFS_VAR_GLOBAL_LAST_LOG_TYPE, String.valueOf(msgType));
			}
		}catch(Exception x){
			debugLog.debugPrintln("AbstractSAFSLoggingService._saveMessageToGlobalVariable() "+x.getClass().getSimpleName()+", "+ x.getMessage());
		}
	}
	/**
	 * Get the variable's value from service {@link STAFHelper#SAFS_VARIABLE_SERVICE}
	 * @param var, String, the variable name
	 * @return String, the variable value, can be null if some error occurs.
	 */
	private String _getVariableValue(String var) {
		String value = null;
		if(var!=null){
		   String     request = "GET "+STAFHelper.lentagValue(var);
		   STAFResult result  = handle.submit2("local", STAFHelper.SAFS_VARIABLE_SERVICE, request);
		   if(result.rc==STAFResult.Ok) value=result.result;
		   else{
			   Log.debug("Fail to get variable '"+var+"'");
			   result  = handle.submit2("local", STAFHelper.SAFS_VARIABLE_SERVICE, "LIST");
			   Log.debug("current var: \n"+result.result);
		   }
		}else{
			Log.debug("Variable should not be null!");
		}
		return value;
	}

	/**
	 * Get a value to a variable in service {@link STAFHelper#SAFS_VARIABLE_SERVICE}
	 * @param var, String, the variable name, must not be null.
	 * @param value, String, the value, must not be null.
	 */
	private void _setVariableValue(String var, String value) {
		if(var!=null && value!=null){
			STAFResult result = null;
			String command = "SET "+STAFHelper.lentagValue(var)+" VALUE "+STAFHelper.lentagValue(value);
			result = handle.submit2("local", STAFHelper.SAFS_VARIABLE_SERVICE, command);
			if(result.rc!=STAFResult.Ok)
				debugLog.debugPrintln("Fail to store '"+value+"' to global variable '"+var+"'!", result);
		}else{
			debugLog.debugPrintln("Failed. The variable or value is null! variable="+var+" ;value="+value);
		}
	}

	/**
	 * Replace the separator by spaces for string value.<br>
	 * The separators may contain more than one character, each one represents a separator, see SAFSStringTokenizer#tokenize().<br>
	 * Each separator needs to be replaced. If the separator is TAB, replace it by 4 spaces;<br>
	 * For other separator, replace it by one space.<br>
	 *
	 *
	 * @param value	String, the string value
	 * @param separators String, the separator to be replaced
	 * @return String the string value after the replacement.
	 */
	private static String _replaceSeparator(String value, String separators){
		String result = value;
		String replacement = null;
		String separator = null;

		if(value!=null && separators!=null){
			separators.trim();
			int len = separators.length();

			for(int i=0;i<len;i++){
				separator = separators.substring(i, i+1);
				if("\t".equals(separators)) replacement = "    ";
				else replacement = " ";
				result = value.replace(separator, replacement);
			}
		}

		return result;
	}

	protected STAFResult handleTruncate(InfoInterface.RequestInfo info)
	{
		// validate the request
		STAFCommandParseResult parsedRequest = truncateParser.parse(info.request);
		if( parsedRequest.rc != STAFResult.Ok )
		{
			return new STAFResult( STAFResult.InvalidRequestString,
				parsedRequest.errorBuffer );
		}
		String truncateval = parsedRequest.optionValue(SLS_SERVICE_REQUEST_TRUNCATE);
		if(truncateval == null){
			truncateLines = true;
		}else{
			if(SLS_SERVICE_PARM_ON.equalsIgnoreCase(truncateval))
				truncateLines = true;
			else if(SLS_SERVICE_PARM_OFF.equalsIgnoreCase(truncateval))
				truncateLines = false;
			else{
				try{
					truncateLength = Integer.parseInt(truncateval);
					if(truncateLength < 1){
						truncateLength = SLS_TRUNCATELENGTH_DEFAULT;
						return new STAFResult( STAFResult.InvalidRequestString,
								"TRUNCATE value cannot be < 1." );
					}
					truncateLines = true;
				}catch(Exception x){
					return new STAFResult( STAFResult.InvalidRequestString,
							"Optional TRUNCATE value must be > 0 | ON | OFF " );
				}
			}
		}
		String desc = truncateLines ? "TRUNCATE "+ truncateLength : "OFF";
		return new STAFResult(STAFResult.Ok, desc);
	}

	protected STAFResult handleClose(InfoInterface.RequestInfo info)
	{
		// validate the request
		STAFCommandParseResult parsedRequest = closeParser.parse(info.request);
		if( parsedRequest.rc != STAFResult.Ok )
		{
			return new STAFResult( STAFResult.InvalidRequestString,
				parsedRequest.errorBuffer );
		}

		boolean capXML = (parsedRequest.optionTimes(SLS_SERVICE_PARM_CAPXML) > 0);

		if (parsedRequest.optionTimes(SLS_SERVICE_PARM_ALL) > 0)
		{
			String result = "";
			for (Enumeration e = logfacs.elements(); e.hasMoreElements();)
			{
				SLSLogFacility logfac = (SLSLogFacility)e.nextElement();
				try
				{
					if(capXML){ logfac.setSAFSXmlLogCapXML(capXML);	}
					// only remove a log fac if closed successfully
					logfac.close();
					logfacs.remove(logfac.getFacName());
				}
				catch (STAFLogException ex)
				{
					result += logfac.getFacName() + ";" + ex.result.rc + ";" +
						ex.result.result + "\n";
				}
			}
			if( logfacs.size() > 0 )
				// if we have left-overs, something went wrong
				return new STAFResult(STAFResult.UserDefined, result);
		}
		else
		{
			String facname = parsedRequest.optionValue(
				SLS_SERVICE_REQUEST_CLOSE).toUpperCase();

			if (facname == null || facname.length() <=0)
				return new STAFResult(STAFResult.InvalidRequestString,
					"You must specifiy <facname> or the ALL option.");

			if( !logfacs.containsKey(facname) )
				return new STAFResult(STAFResult.DoesNotExist, facname);

			SLSLogFacility logfac = (SLSLogFacility)logfacs.get(facname);
			try
			{
				if(capXML){ logfac.setSAFSXmlLogCapXML(capXML);	}
				// only remove the log fac if closed successfully
				logfac.close();
				logfacs.remove(facname);
			}
			catch (STAFLogException e)
			{
				return e.result;
			}
		}

		return new STAFResult(STAFResult.Ok);
	}

	protected STAFResult handleHelp(InfoInterface.RequestInfo info)
	{
		// validate the request
		STAFCommandParseResult parsedRequest = helpParser.parse(info.request);
		if( parsedRequest.rc != STAFResult.Ok )
		{
			return new STAFResult( STAFResult.InvalidRequestString,
				parsedRequest.errorBuffer );
		}

		if (parsedRequest.optionTimes(SLS_SERVICE_PARM_MSGTYPE) > 0)
			return new STAFResult(STAFResult.Ok, HELP_MSGTYPE_STR);
		else
			return new STAFResult(STAFResult.Ok, HELP_STR);
	}

	protected STAFResult handleVersion(
		InfoInterface.RequestInfo info)
	{
		return new STAFResult(STAFResult.Ok, VERSION_STR);
	}

	protected void registerHandle(String handleId)throws STAFException{
		String debugmsg = getClass().getName() + ".registerHandle():";
    	debugLog.debugPrintln(debugmsg+" registering STAFHandle handleId: "+ handleId);
		handle = new STAFHandleInterface(handleId);
	}

	public STAFResult init(InfoInterface.InitInfo info) {
		debugLog.debugInit();

		try{
			procName = "STAF/Service/" + info.name;
			SLS_STAF_LOG_SERVICE_NAME = info.name + "Log";
			registerHandle(procName);
		}catch (STAFException e){
			debugLog.debugPrintln(e.rc +":"+ e.getMessage());
			debugLog.debugTerm();
			return new STAFResult(STAFResult.STAFRegistrationError);
		}

		// handle initialization parameters: mode, defaultDir, remoteMachine,
		// remoteService, and customService
		int rc = loadServiceInitParameters(info);
		if( rc != STAFResult.Ok ){
			debugLog.debugTerm();
			try { handle.unRegister(); }
			catch (STAFException e) {}
			return new STAFResult(rc);
		}

		if (mode == SLS_SERVICE_MODE_LOCAL)
		{
			STAFResult result = initSTAFLogService();
			if( result.rc != STAFResult.Ok ) return result;
		}

		// setup request parsers
		createInitParser();
		createQueryParser();
		createListParser();
		createSuspendLogParser();
		createResumeParser();
		createLogLevelParser();
		createLogMessageParser();
		createCloseParser();
		createTruncateParser();
		createHelpParser();

		debugLog.debugPrintln("SAFSLoggingService.init(): mode=" + mode + ";dir=" +
			defaultDir + ";rmachine=" + remoteMachine + ";rservcie=" +
			remoteService + ";custom=" + customService);

		return new STAFResult(STAFResult.Ok);
	}

	/**
	 * Subclasses can override to change the STAFLog service initialization, or even
	 * bypass it entirely if not used.
	 * @return STAFResult
	 */
	protected STAFResult initSTAFLogService(){
		// if running in local mode, load a separate instance of STAF LOG
		// service. we do not want to use the default instance because we
		// have special requirements for its settings.
		STAFResult result = handle.submit2("local", "service",
			"add service " + SLS_STAF_LOG_SERVICE_NAME +
			" library STAFLog parms MAXRECORDSIZE 1048576");
		if( result.rc != STAFResult.Ok )
		{
			debugLog.debugTerm();
			try { handle.unRegister(); }
			catch (STAFException e) {}
			return new STAFResult(result.rc);
		}
		return result;
	}

	/**
	 * Handles service request from STAF.
	 * <p>
	 */
	public STAFResult acceptRequest(InfoInterface.RequestInfo info)
	{
		String upperRequest = info.request.toUpperCase();
		StringTokenizer requestTokenizer = new StringTokenizer(upperRequest);
		String request = requestTokenizer.nextToken();

		// call the appropriate method to handle the command
		if (request.equals(SLS_SERVICE_REQUEST_HANDLEID))
		{
			return handleHandleId(info);

		}
		else if (request.equals(SLS_SERVICE_REQUEST_INIT))
		{
			return handleInit(info);
		}
		else if (request.equals(SLS_SERVICE_REQUEST_QUERY))
		{
			return handleQuery(info);
		}
		else if (request.equals(SLS_SERVICE_REQUEST_LIST))
		{
			return handleList(info);
		}
		else if (request.equals(SLS_SERVICE_REQUEST_SUSPENDLOG))
		{
			return handleSuspendLog(info);
		}
		else if (request.equals(SLS_SERVICE_REQUEST_RESUMELOG))
		{
			return handleResumeLog(info);
		}
		else if (request.equals(SLS_SERVICE_REQUEST_LOGLEVEL))
		{
			return handleLogLevel(info);
		}
		else if (request.equals(SLS_SERVICE_REQUEST_LOGMESSAGE))
		{
			return handleLogMessage(info);
		}
		else if (request.equals(SLS_SERVICE_REQUEST_CLOSE))
		{
			return handleClose(info);
		}
		else if (request.equals(SLS_SERVICE_REQUEST_TRUNCATE))
		{
			return handleTruncate(info);
		}
		else if (request.equals(SLS_SERVICE_REQUEST_HELP))
		{
			return handleHelp(info);
		}
		else if (request.equals(SLS_SERVICE_REQUEST_VERSION))
		{
			return handleVersion(info);
		}
		else
		{
			return new STAFResult(STAFResult.InvalidRequestString,
				"Unknown Request: " + info.request);
		}
	}

	/**
	 * Handles removing this service from STAF.
	 * <p>
	 * All log facilities are forced to close.
	 * <p>
	 */
	public STAFResult terminate()
	{
		// close all log facilities
		if( !logfacs.isEmpty() )
		{
			for (Enumeration e = logfacs.elements(); e.hasMoreElements();)
			{
				SLSLogFacility fac = (SLSLogFacility)e.nextElement();
				try{ fac.close(); }
				catch (Exception ex) {}
			}
			logfacs.clear();
		}

		// unload the STAF LOG service instance loaded in init
		if(!EmbeddedHandles.isServiceRunning(STAFHelper.SAFS_LOGGING_SERVICE))
			handle.submit2("local", "service", "remove service " +SLS_STAF_LOG_SERVICE_NAME);

		debugLog.debugTerm();

		try
		{
			handle.unRegister();
		}
		catch( STAFException ex )
		{
			return new STAFResult(STAFResult.STAFRegistrationError);
		}

		return new STAFResult(STAFResult.Ok);
	}
}
