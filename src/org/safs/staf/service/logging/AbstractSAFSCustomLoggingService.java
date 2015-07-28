package org.safs.staf.service.logging;

import java.util.Hashtable;
import java.util.StringTokenizer;

import org.safs.staf.STAFHandleInterface;
import org.safs.staf.embedded.HandleInterface;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.ServiceDebugLog;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFCommandParseResult;
import com.ibm.staf.service.STAFCommandParser;


/**
 * <code>AbstractSAFSCustomLoggingService</code> is an external STAF service that 
 * handles custom SAFS logging. 
 * This class is defined as an abstract class, for  different version of STAF, 
 * you may extends this class and implements the  staf-version-related interface 
 * (STAFServiceInterfaceLevel or STAFServiceInterfaceLevel30).
 * We have implemented the class for version 2 and 3: SAFSCustomLoggingService and SAFSCustomLoggingService3
 * 
 * <p>
 * This class and {@link CustomLogFac} provide a sample implementation of custom
 * logging service that writes directly to the same STAF logs that standard 
 * logging service writes to.
 * <p>
 * Registration of this service takes the following format:
 * <p>
 * <code>
 * SERVICE &lt;ServiceName> LIBRARY JSTAF EXECUTE &lt;ServiceJarFile> PARMS 
 * SAFSLOGGING &lt;SAFSLoggingService>
 * </code>
 * <p>
 * <code>SAFSLOGGING</code> option is required because this service uses the
 * unique STAF LOG service instance loaded by SAFS logging service to access
 * the running STAF logs.
 * <p>
 * Example: 
 * <p>
 * <code>
 * SERVICE SAFSCustomLogs LIBRARY JSTAF EXECUTE
 * c:\staf\services\SAFSCustomLogs.jar PARMS SAFSLOGGING SAFSLogs
 * </code>
 * <p>
 * This service provides the following commands:
 * <p>
 * <pre>
 * INIT        &lt;facname> MODE &lt;mode> [LINKEDFAC &lt;name>]
 * LOGMESSAGE  &lt;facname> MESSAGE &lt;msg> [DESCRIPTION &lt;desc>] [MSGTYPE &lt;msgType>]
 * CLOSE       &lt;facname>
 * HELP
 * VERSION
 * </pre>
 * 
 * @see CustomLogFac
 * @see org.safs.staf.service.logging.v2.SAFSLoggingService
 * @see org.safs.staf.service.logging.v3.SAFSLoggingService3
 * @see "SAFSLogs Service User's Guide"
 * @see org.safs.staf.service.logging.v2.SAFSCustomLoggingService
 * @see org.safs.staf.service.logging.v3.SAFSCustomLoggingService3
 */

public abstract class AbstractSAFSCustomLoggingService {
	public static final String SCLS_SERVICE_OPTION_SAFSLOGGING = "SAFSLOGGING";
	
	public static final String SCLS_SERVICE_REQUEST_INIT = "INIT";
	public static final String SCLS_SERVICE_REQUEST_LOGMESSAGE = "LOGMESSAGE";
	public static final String SCLS_SERVICE_REQUEST_CLOSE = "CLOSE";
	public static final String SCLS_SERVICE_REQUEST_HELP = "HELP";
	public static final String SCLS_SERVICE_REQUEST_VERSION = "VERSION";
	
	public static final String SCLS_SERVICE_PARM_MODE = "MODE";
	public static final String SCLS_SERVICE_PARM_LINKEDFAC = "LINKEDFAC";

	public static final String SCLS_SERVICE_PARM_MESSAGE = "MESSAGE";
	public static final String SCLS_SERVICE_PARM_DESCRIPTION = "DESCRIPTION";
	public static final String SCLS_SERVICE_PARM_MSGTYPE = "MSGTYPE";

	public static final String SCLS_SERVICE_RESPONSE_BYPASS = "BYPASS";

	private static final String VERSION_STR = "1.0";
	private static final String HELP_STR = 
		"SAFSCustomLoggingService HELP\n\n" + 
		"INIT        <facname> MODE <mode> [LINKEDFAC <name>]\n\n" +
		"LOGMESSAGE  <facname> MESSAGE <msg> [DESCRIPTION <desc>] [MSGTYPE <msgType>]\n\n" +
		"CLOSE       <facname>\n\n" +
		"HELP\n\n" +
		"VERSION";

	/**
	 * Stores all open log facilities.
	 */
	private Hashtable logfacs = new Hashtable();

	private static HandleInterface handle;

	private STAFCommandParser initParser;
	private STAFCommandParser logMessageParser;
	private STAFCommandParser closeParser;

	// name of the standard SAFS logging service
	private String safsService;

	// for debugging
	protected ServiceDebugLog debugLog = new ServiceDebugLog(ServiceDebugLog.DEBUG_LOG_CUSTOM_LOGGING,false);

	public AbstractSAFSCustomLoggingService() {}

	/**
	 * Handles initializing this instance of the service for STAF.
	 * <p>
	 * This service is registered under process name 
	 * "STAF/Service/&lt;ServiceName>".
	 * <p>
	 */
	public STAFResult init(InfoInterface.InitInfo info)
	{
		debugLog.debugInit();

		try
		{
			handle = new STAFHandleInterface("STAF/Service/" + info.name);
		}
		catch (STAFException e)
		{
			return new STAFResult(STAFResult.STAFRegistrationError);
		}

		// handle initialization parameters
		int rc = loadServiceInitParameters(info);
		if( rc != STAFResult.Ok )
		{
			debugLog.debugTerm();
			try { handle.unRegister(); }
			catch (STAFException e) {}
			return new STAFResult(rc);
		}

		// setup request parsers
		createInitParser();
		createLogMessageParser();
		createCloseParser();

		return new STAFResult(STAFResult.Ok);
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
		if (request.equals(SCLS_SERVICE_REQUEST_INIT))
		{
			return handleInit(info);
		}
		else if (request.equals(SCLS_SERVICE_REQUEST_LOGMESSAGE))
		{
			return handleLogMessage(info);
		}
		else if (request.equals(SCLS_SERVICE_REQUEST_CLOSE))
		{
			return handleClose(info);
		}
		else if (request.equals(SCLS_SERVICE_REQUEST_HELP))
		{
			return handleHelp(info);
		}
		else if (request.equals(SCLS_SERVICE_REQUEST_VERSION))
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
	 */
	public STAFResult terminate()
	{
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

	/**
	 * Loads the service initialization parameters.
	 * <p>
	 * @param info	the <code>InitInfo</code> from STAF.
	 * @return		a STAF RC.
	 * <p>
	 */
	private int loadServiceInitParameters(InfoInterface.InitInfo info)
	{
		// build the parameter parser
		STAFCommandParser registrar = new STAFCommandParser(0, false);
		registrar.addOption(SCLS_SERVICE_OPTION_SAFSLOGGING, 1,
			STAFCommandParser.VALUEREQUIRED);

		String parms = info.parms;
		STAFCommandParseResult parsedData = registrar.parse(parms);
		if (parsedData.rc != STAFResult.Ok)	return parsedData.rc;

		safsService = parsedData.optionValue(SCLS_SERVICE_OPTION_SAFSLOGGING);

		return STAFResult.Ok;
	}

	private void createInitParser()
	{
		initParser = new STAFCommandParser(0, false);
		initParser.addOption(SCLS_SERVICE_REQUEST_INIT, 1, 
			STAFCommandParser.VALUEREQUIRED);
		initParser.addOption(SCLS_SERVICE_PARM_MODE, 1, 
			STAFCommandParser.VALUEREQUIRED);
		initParser.addOption(SCLS_SERVICE_PARM_LINKEDFAC, 1, 
			STAFCommandParser.VALUEREQUIRED);
		initParser.addOptionNeed(SCLS_SERVICE_REQUEST_INIT,
			SCLS_SERVICE_PARM_MODE);
	}

	private void createLogMessageParser()
	{
		logMessageParser = new STAFCommandParser(0, false);
		logMessageParser.addOption(SCLS_SERVICE_REQUEST_LOGMESSAGE, 1,
			STAFCommandParser.VALUEREQUIRED);
		logMessageParser.addOption(SCLS_SERVICE_PARM_MESSAGE, 1, 
			STAFCommandParser.VALUEREQUIRED);
		logMessageParser.addOption(SCLS_SERVICE_PARM_DESCRIPTION, 1,
			STAFCommandParser.VALUEREQUIRED);
		logMessageParser.addOption(SCLS_SERVICE_PARM_MSGTYPE, 1, 
			STAFCommandParser.VALUEREQUIRED);
		// MESSAGE option is required
		logMessageParser.addOptionNeed(SCLS_SERVICE_REQUEST_LOGMESSAGE,
			SCLS_SERVICE_PARM_MESSAGE);
	}

	private void createCloseParser()
	{
		closeParser = new STAFCommandParser(0, false);
		closeParser.addOption(SCLS_SERVICE_REQUEST_CLOSE, 1, 
			STAFCommandParser.VALUEREQUIRED);
	}

	private STAFResult handleInit(InfoInterface.RequestInfo info)
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
			SCLS_SERVICE_REQUEST_INIT).toUpperCase();

		if (logfacs.containsKey(facname))
			return new STAFResult(STAFResult.AlreadyExists, facname);

		long logmode = Long.parseLong(
			parsedRequest.optionValue(SCLS_SERVICE_PARM_MODE));

		// get linked fac
		String linkedfac = null;
		if (parsedRequest.optionTimes(SCLS_SERVICE_PARM_LINKEDFAC) > 0)
		{
			linkedfac = parsedRequest.optionValue(SCLS_SERVICE_PARM_LINKEDFAC);
		}

		// create the log facility.
		CustomLogFac fac = new CustomLogFac(facname, logmode, linkedfac, handle,
			safsService + "Log");
		logfacs.put(fac.getFacName(), fac);
		return new STAFResult(STAFResult.Ok);
	}

	private STAFResult handleLogMessage(
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
			SCLS_SERVICE_REQUEST_LOGMESSAGE).toUpperCase();

		if( !logfacs.containsKey(facname) )
			return new STAFResult(STAFResult.DoesNotExist, facname);

		CustomLogFac logfac = (CustomLogFac)logfacs.get(facname);
		String msg = parsedRequest.optionValue(SCLS_SERVICE_PARM_MESSAGE);
		String desc = null;
		int msgType = CustomLogFac.GENERIC_MESSAGE;
		if( parsedRequest.optionTimes(SCLS_SERVICE_PARM_DESCRIPTION) > 0 )
		{
			desc = parsedRequest.optionValue(SCLS_SERVICE_PARM_DESCRIPTION);
		}
		if( parsedRequest.optionTimes(SCLS_SERVICE_PARM_MSGTYPE) > 0 )
		{
			String mts = parsedRequest.optionValue(SCLS_SERVICE_PARM_MSGTYPE);
			try
			{
				msgType = Integer.parseInt(mts);
			}
			catch( Exception e )
			{
				return new STAFResult(STAFResult.InvalidValue, mts);
			}
		}
		return logfac.logMessage(msg, desc, msgType);
	}

	private STAFResult handleClose(InfoInterface.RequestInfo info)
	{
		// validate the request
		STAFCommandParseResult parsedRequest = closeParser.parse(info.request);
		if( parsedRequest.rc != STAFResult.Ok )
		{
			return new STAFResult( STAFResult.InvalidRequestString, 
				parsedRequest.errorBuffer );
		}

		String facname = parsedRequest.optionValue(
			SCLS_SERVICE_REQUEST_CLOSE).toUpperCase();

		if( !logfacs.containsKey(facname) )
			return new STAFResult(STAFResult.DoesNotExist, facname);

		((CustomLogFac)logfacs.get(facname)).close();
		logfacs.remove(facname);
		return new STAFResult(STAFResult.Ok);

	}

	private STAFResult handleHelp(InfoInterface.RequestInfo info)
	{
		return new STAFResult(STAFResult.Ok, HELP_STR);
	}

	private STAFResult handleVersion(
		InfoInterface.RequestInfo info)
	{
		return new STAFResult(STAFResult.Ok, VERSION_STR);
	}

}
