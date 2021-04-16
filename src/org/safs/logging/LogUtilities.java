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
package org.safs.logging;

import org.safs.Log;
import org.safs.SAFSSTAFRegistrationException;
import org.safs.STAFHelper;
import org.safs.staf.service.logging.AbstractSAFSLoggingService;
import org.safs.staf.service.logging.SLSLogFacilityStates;

import com.ibm.staf.STAFResult;

/**
 * Provides access to tool-independent logging through the STAF SAFSLOGS service.
 * This class also acts as a superclass for tool-dependent subclasses so that they
 * can also log the same messages to the native tool log and any tool console. If
 * the SAFSLOGS service is not running; or some other error prevents logging to the
 * SAFSLOGS service; then messages will be sent to the static org.safs.Log class.
 * <P>
 * The subclass would minimally overload the primary logMessage function--the one
 * that actually implements the logging code--and intercept logMessage calls.  The
 * subclass function would call its super.logMessage to perform the normal SAFSLOGS
 * logging and then evaluate the returned String to see if it is expected to write
 * to the native tool log and/or console.
 *
 * @version 1.0, 09/12/2003
 * @author Carl Nagle, SAS Institute
 * @author Carl Nagle, SEP 16, 2003 Enabled copying org.safs.Log with messages.
 * @author Carl Nagle, NOV 18, 2003 Added default System.out console logging.
 *
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
public class LogUtilities extends org.safs.STAFRequester{

	/**
	 * Generic error return value from logMessage routines.
	 **/
	public static final String LOG_ERROR = "LOGERROR";

    /** flag for instance initialization state **/
	protected boolean initialized  = false;

    /** flag for sending/copying messages to static org.safs.Log class. **/
	protected boolean copyLogClass = false;

    /** curent log level as retrieved from SAFSLOGS. **/
	protected int logLevel = AbstractLogFacility.LOGLEVEL_INFO;

	/**
	 * Stores the name of the last used LogFacility when logging.<p>
	 * This is useful for subclasses like ApacheLogUtilities that bridge to our logging
	 * mechanism but do not provide a facname parameter in their APIs.*/
	protected String lastNamedLogFacility = null;

	/**
	 * Noop Constructor
	 * User must provide an initialized STAFHelper via superclass
	 * <code>{@link org.safs.STAFRequester}</code> before the instance can be effective.
	 **/
	public LogUtilities() {	}

	/**
	 * Constructor with pre-registered STAF handle
	 * @param helper -- an initialized STAFHelper for talking with STAF.
	 **/
	public LogUtilities(STAFHelper helper) {
		setSTAFHelper(helper);
		//inherited field "staf"
	}

	/**
	 * Constructor with pre-registered STAF handle and flag for using the static Log class.
	 *
	 * @param helper -- an initialized STAFHelper for talking with STAF.
	 * @param copyLogClass -- flag as to whether or not messages should be forwarded to
	 * the static org.safs.Log class   That Log class is primarily used during development
	 * and debugging and is not normally used during production testing.
	 **/
	public LogUtilities(STAFHelper helper, boolean copyLogClass) {
		setSTAFHelper(helper);
		//inherited field "staf"
		setCopyLogClass(copyLogClass);
	}

	/**
	 * returns the name of the last used LogFacility for logging.<p>
	 * This is useful for subclasses like ApacheLogUtilities that bridge to our logging
	 * mechanism but do not provide a facname parameter in their APIs.*/
	public String getLastNamedLogFacility(){ return lastNamedLogFacility;}
	/**
	 * @param lastNamedLogFacility String, the name of the last used LogFacility for logging
	 */
	public void setLastNamedLogFacility(String lastNamedLogFacility){ this.lastNamedLogFacility = lastNamedLogFacility;}

	/**
	 * Set or Reset the flag to enable logging to org.safs.Log
	 * This Log class is primarily used during development and debugging and is not
	 * normally used during production testing.
	 *
	 * @param copyLogClass -- true to forward messages to org.safs.Log
	 **/
	public void setCopyLogClass(boolean copyLogClass){ this.copyLogClass = copyLogClass;}

	/** Check the present state of our copyLogClass flag. **/
	public boolean getCopyLogClass(){ return copyLogClass;}

	/**
	 * convenience routine to throw a LogException when uninitialized.
	 * Checks for: STAFHelper provided, STAFHelper initialized, and SAFSLOGS
	 * service is running.
	 **/
	protected boolean checkInitialized() throws LogException{
		if(initialized) return true;

		if (staf==null)
			throw new LogException(
			"org.safs.logging.LogUtilities STAFHelper not provided.");

		if (!staf.isInitialized())
			throw new LogException(
			"org.safs.logging.LogUtilities STAFHelper not initialized.");

		if (!staf.isSAFSLOGSAvailable())
			throw new LogException(
			"SAFSLOGS Service is not available.");

		initialized = true;
		return true;
	}

	/**
	 * Initialize a new LogFacility via the SAFSLOGS service.
	 * @param facname -- String name for this log facility.
	 * @param facmode -- long OR'd values of enabled logs.  Valid values are the
	 * various LOGMODE_ constants defined in {@link AbstractLogFacility}.
	 * @param alttextlog -- alternative String name for an enabled SAFS TEXT log.
	 * If null the default name for the text log as defined in {@link AbstractLogFacility}
	 * will be used.
	 * @param altxmllog -- alternative String name for an enabled SAFS XML log.
	 * If null the default name for the XML log as defined in {@link AbstractLogFacility}
	 * will be used.
	 * @param linkedfac -- NOT YET IMPLEMENTED<br>
	 * optional String name for another existing LogFacility
	 * that is to be linked to this one.  Messages sent to this LogFacility will also
	 * be sent to the linked LogFacility.
	 **/
	public void initLogFacility(String facname, long facmode,
	                            String alttextlog, String altxmllog,
	                            String linkedfac) throws LogException{

		if(facname == null) throw new LogException("The argument 'facname' for initLogFacility cannot be null!");

		checkInitialized(); //throws the exception if not
		String s = " ";
		String textlog = null;;
		String xmllog = null;

		if(! (alttextlog == null)) textlog = staf.lentagValue(alttextlog) + s;
		if(! (altxmllog  == null)) xmllog  = staf.lentagValue(altxmllog ) + s;

		String message = AbstractSAFSLoggingService.SLS_SERVICE_REQUEST_INIT + s +
		                 staf.lentagValue(new String(facname.toUpperCase())) + s;

		// if MAXLOGMODE
		if(facmode == AbstractLogFacility.LOGMODE_MAX) {
		     message += AbstractSAFSLoggingService.SLS_SERVICE_PARM_ALL + s;

		     //see if alt log names were provided for text and xml logs
		     try{ if (textlog.length() > 0){
		     	  message += AbstractSAFSLoggingService.SLS_SERVICE_PARM_TEXTLOG +s+ textlog;}
		     }catch(NullPointerException e){}
		     try{ if (xmllog.length() > 0){
		     	  message += AbstractSAFSLoggingService.SLS_SERVICE_PARM_XMLLOG +s+ xmllog;}
		     }catch(NullPointerException e){}

		//otherwise
		}else{
			if((facmode & AbstractLogFacility.LOGMODE_SAFS_TEXT) > 0){
		     	 message += AbstractSAFSLoggingService.SLS_SERVICE_PARM_TEXTLOG + s;
			     try{ if (textlog.length() > 0){ message += textlog;}
		         }catch(NullPointerException e){}
			}
			if((facmode & AbstractLogFacility.LOGMODE_SAFS_XML) > 0){
		     	 message += AbstractSAFSLoggingService.SLS_SERVICE_PARM_XMLLOG + s;
			     try{ if (xmllog.length() > 0){ message += xmllog;}
		         }catch(NullPointerException e){}
			}
			if((facmode & AbstractLogFacility.LOGMODE_TOOL) > 0)
		     	 message += AbstractSAFSLoggingService.SLS_SERVICE_PARM_TOOLLOG + s;

			if((facmode & AbstractLogFacility.LOGMODE_CONSOLE) > 0)
		     	 message += AbstractSAFSLoggingService.SLS_SERVICE_PARM_CONSOLELOG + s;
		}

		if(!(linkedfac==null))
			message += AbstractSAFSLoggingService.SLS_SERVICE_PARM_LINKEDFAC + s +
			           staf.lentagValue(linkedfac.toUpperCase());

		// now send the message
		STAFResult result = staf.submit2ForFormatUnchangedService( staf.LOCAL_MACHINE,
		                                  staf.SAFS_LOGGING_SERVICE,
		                                  message);
		if(result.rc != STAFResult.Ok)
			throw new LogException("Log initialization error:"+
			                        String.valueOf(result.rc) + ":" +
			                        result.result);
		lastNamedLogFacility = facname;
	}

	/**
	 * Initialize a new LogFacility via the SAFSLOGS service.
	 * Provides for minimal parameters.  Default values are used for
	 * all parameters missing from this overloaded method.
	 * @param facname -- String name for this log facility.
	 * @param facmode -- long OR'd values of enabled logs.  Valid values are the
	 * various LOGMODE_ constants defined in {@link AbstractLogFacility}.
	 **/
	public void initLogFacility(String facname, long facmode) throws LogException{
		initLogFacility(facname, facmode, null, null, null);
	}

	/**
	 * Initialize a new LogFacility via the SAFSLOGS service.
	 * Provides for a small subset of parameters.  Default values are used for
	 * all parameters missing from this overloaded method.
	 * @param facname -- String name for this log facility.
	 * @param facmode -- long OR'd values of enabled logs.  Valid values are the
	 * various LOGMODE_ constants defined in {@link AbstractLogFacility}.
	 * @param linkedfac -- NOT YET IMPLEMENTED<br>
	 * optional String name for another existing LogFacility
	 * that is to be linked to this one.  Messages sent to this LogFacility will also
	 * be sent to the linked LogFacility.
	 **/
	public void initLogFacility(String facname, long facmode, String linkedfac) throws LogException{
		initLogFacility(facname, facmode, null, null, linkedfac);
	}

	/**
	 * Closes a specified LogFacility or ALL LogFacilities via the SAFSLOGS service.
	 * @param facname -- String name of an open LogFacility in SAFSLOGS.  If this
	 * parameter is null, then we will close ALL open LogFacilities in the service.
	 **/
	public void closeLogFacility(String facname) throws LogException{
		checkInitialized();
		String s = " ";

		String message = AbstractSAFSLoggingService.SLS_SERVICE_REQUEST_CLOSE + s;
		if(facname==null){
			message += AbstractSAFSLoggingService.SLS_SERVICE_PARM_ALL;
		}else{
			message += staf.lentagValue(facname.toUpperCase());
		}
		// now send the message
		STAFResult result = staf.submit2ForFormatUnchangedService( staf.LOCAL_MACHINE,
		                                  staf.SAFS_LOGGING_SERVICE,
		                                  message);
		if(result.rc != STAFResult.Ok)
			throw new LogException("Log closing error:"+
			                        String.valueOf(result.rc) + ":" +
			                        result.result);
	}

	/**
	 * Copy the message to the static org.safs.Log class if enabled.
	 * @param msg -- the message to send
	 * @param msgType the local msgType.  This will be converted to the appropriate
	 * message type for the Log class.
	 **/
	protected void sendLogClassMessage(String msg, String description, int msgType){
		String theMsg;
		if(description == null) { theMsg = msg; }
		else{ theMsg = msg +"\n"+ description; }

		switch(msgType){
			case AbstractLogFacility.FAILED_MESSAGE:
				Log.error(theMsg);
				break;
			case AbstractLogFacility.WARNING_MESSAGE:
				Log.warn(theMsg);
				break;
			case AbstractLogFacility.PASSED_MESSAGE:
				Log.pass(theMsg);
				break;
			case AbstractLogFacility.DEBUG_MESSAGE:
				Log.debug(theMsg);
				break;
			default:
				Log.generic(theMsg);
				break;
		}
	}

	/**
	 * Log a message to the specified LogFacility via the SAFSLOGS service.
	 * <P>
	 * Tool-dependent subclass implementations would minimally overload this
	 * function to intercept the call. Subclasses would then call this function
	 * via super.logMessage and evaluate the returned String to see if the tool-
	 * dependent subclass is expected to log to the native tool log and/or console.
	 *
	 * @param facname -- String name of an open LogFacility in SAFSLOGS.
	 * @param msg -- String text of the message to log.
	 * @param description -- optional String text of additional details for the message.
	 * @param msgType -- int message type identifier constant as defined in
	 * {@link AbstractLogFacility}.
	 *
	 * @return String STAFResult.result from SAFSLOGS service call; or,
	 * LOG_ERROR if we were unable to make the call due to improper initialization
	 * of SAFSLOGS, this LogUtilities instance, or bad function parameters.
	 * <P>
	 * If no error occurred,
	 * the returned String informs the caller if native tool logging or console
	 * logging is enabled and at what log level.  The caller, presumably a commercial
	 * automation tool, can then determine if it should log the message to the
	 * native tool log and/or console.
	 **/
	public String logMessage(String facname, String msg, String description, int msgType){

		String s = " ";
		if(msg == null) msg = s; // at least one char in length

		// default to org.safs.Log if we CANNOT log to SAFSLOGS
		try{
			checkInitialized();
			if (facname == null)
				throw new LogException("LogFacility name cannot be null!");
		}catch(LogException e){
			sendLogClassMessage(msg, description, msgType);
			return LOG_ERROR+":"+e.getMessage();
		}

		String message = AbstractSAFSLoggingService.SLS_SERVICE_REQUEST_LOGMESSAGE + s +
		                 staf.lentagValue(new String(facname.toUpperCase()))             + s +
		                 AbstractSAFSLoggingService.SLS_SERVICE_PARM_MESSAGE       + s +
		                 staf.lentagValue(msg)                             + s;

		try{ if (description.length() > 0 ) {
			 message += AbstractSAFSLoggingService.SLS_SERVICE_PARM_DESCRIPTION    + s +
			            staf.lentagValue(description)                      + s;}
		}catch(NullPointerException e){}

		message += AbstractSAFSLoggingService.SLS_SERVICE_PARM_MSGTYPE             + s +
		           String.valueOf(msgType).trim();

		// now send the message
		STAFResult result = staf.submit2ForFormatUnchangedService( staf.LOCAL_MACHINE,
		                                  staf.SAFS_LOGGING_SERVICE,
		                                  message);


		if (result.rc != STAFResult.Ok) {
                    if(getCopyLogClass()) sendLogClassMessage(msg, description, msgType);
		    return LOG_ERROR+":"+ String.valueOf(result.rc).trim()+":"+ result.result;
                }

		// now parse result to see if we need to log to RobotJ or Console.
		SLSLogFacilityStates lfs = SLSLogFacilityStates.parseStates(result.result);

		// do not log if the msgType is not within our log level bounds
		// however, allow the Log class to process ANY message using its log level.
		if(! MessageTypeInfo.typeBelongsToLevel(msgType, lfs.level)) {
            if(getCopyLogClass()) sendLogClassMessage(msg, description, msgType);
			return result.result;
		}

		MessageTypeInfo info = MessageTypeInfo.get(msgType);
		String formattedMsg = info.textPrefix + msg;

		if(lfs.tool)          toolLog(msgType, formattedMsg, description);
		if(lfs.console)       consoleLog(formattedMsg, description);
		if(getCopyLogClass()) sendLogClassMessage(msg, description, msgType);

		return result.result;
	}

	/**
	 * Log a message to the specified LogFacility via the SAFSLOGS service.
	 * @param facname -- String name of an open LogFacility in SAFSLOGS.
	 * @param msg -- String text of the message to log.
	 * @param msgType -- int message type identifier constant as defined in
	 * {@link AbstractLogFacility}.
	 * @param description -- optional String text of additional details for the message.
	 *
	 * @return String STAFResult.result from SAFSLOGS service call; or,
	 * LOG_ERROR if we were unable to make the call due to improper initialization
	 * of SAFSLOGS, this LogUtilities instance, or bad function parameters.
	 **/
	public String logMessage(String facname, String msg, int msgType,  String description){
		return logMessage(facname, msg, description, msgType);
	}

	/**
	 * Log a message to the specified LogFacility via the SAFSLOGS service.
	 * @param facname -- String name of an open LogFacility in SAFSLOGS.
	 * @param msg -- String text of the message to log.
	 * @param msgType -- int message type identifier constant as defined in
	 * {@link AbstractLogFacility}.
	 *
	 * @return String STAFResult.result from SAFSLOGS service call; or,
	 * LOG_ERROR if we were unable to make the call due to improper initialization
	 * of SAFSLOGS, this LogUtilities instance, or bad function parameters.
	 **/
	public String logMessage(String facname, String msg, int msgType){
		return logMessage(facname, msg, null, msgType);
	}

	/**
	 * Log a message to the specified LogFacility via the SAFSLOGS service.
	 * A default message type of GENERIC_MESSAGE is used.
	 * @param facname -- String name of an open LogFacility in SAFSLOGS.
	 * @param msg -- String text of the message to log.
	 * @param description -- optional String text of additional details for the message.
	 *
	 * @return String STAFResult.result from SAFSLOGS service call; or,
	 * LOG_ERROR if we were unable to make the call due to improper initialization
	 * of SAFSLOGS, this LogUtilities instance, or bad function parameters.
	 **/
	public String logMessage(String facname, String msg, String description){
		return logMessage(facname, msg, description, AbstractLogFacility.GENERIC_MESSAGE );
	}

	/**
	 * Log a message to the specified LogFacility via the SAFSLOGS service.
	 * A default message type of GENERIC_MESSAGE is used.
	 * @param facname -- String name of an open LogFacility in SAFSLOGS.
	 * @param msg -- String text of the message to log.
	 *
	 * @return String STAFResult.result from SAFSLOGS service call; or,
	 * LOG_ERROR if we were unable to make the call due to improper initialization
	 * of SAFSLOGS, this LogUtilities instance, or bad function parameters.
	 **/
	public String logMessage(String facname, String msg){
		return logMessage(facname, msg, null, AbstractLogFacility.GENERIC_MESSAGE);
	}

	/**
	 * Subclasses override to log to their proprietary tool-dependent log.
	 *
	 * @param messageType int type of to be logged
	 * @param formattedMessage String the message to be logged
	 * @param description String optional description about the message
	 *         being logged
	 **/
	public void toolLog(int messageType, String formattedMessage, String description){}

	/**
	 * Subclasses override to log to a proprietary tool-dependent console.
	 *
	 * @param formattedMessage String the message to be logged
	 * @param description String optional description about the message
	 *         being logged
	 **/
	public void consoleLog(String formattedMessage, String description){
		System.out.println(formattedMessage);
		if(description != null) System.out.println(description);
	}


	/**
	 * Some self-test diagnostics...does it really work :?
	 **/
	public static void main(String[] args){
		String lulog = "alutestlog";

		System.out.println("LUTestProcess begins.");

		try{
			STAFHelper testhelper = new STAFHelper("LUTestProcess");
		    org.safs.logging.LogUtilities lu = new org.safs.logging.LogUtilities(testhelper);

		    String result = null;

		try{
			lu.initLogFacility(lulog, 127);
			System.out.println( lu.logMessage(lulog, "My first test message!"));
			lu.closeLogFacility(lulog);
		}
		catch(LogException e){ System.err.println("ERR:"+ e.getMessage());}
		}
		catch(SAFSSTAFRegistrationException r){ System.err.println("ERR:"+ r.getMessage());}
		System.out.println("LUTestProcess complete.");
	}
}
