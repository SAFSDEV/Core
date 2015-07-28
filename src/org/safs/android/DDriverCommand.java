/** Copyright (C) SAS Institute All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.android;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.safs.DriverCommand;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.SAFSObjectRecognitionException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.TestRecordHelper;
import org.safs.android.remotecontrol.SAFSMessage;
import org.safs.android.remotecontrol.SAFSRemoteControl;
import org.safs.android.remotecontrol.SAFSWorker;
import org.safs.image.ImageUtils;
import org.safs.logging.AbstractLogFacility;
import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;
import org.safs.text.CaseInsensitiveHashtable;
import org.safs.text.FAILKEYS;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.text.GENStrings;
import org.safs.text.ResourceMessageInfo;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.stringutils.StringUtilities;

import com.android.ddmlib.IDevice;
import com.jayway.android.robotium.remotecontrol.solo.RemoteResults;

/**
 * SAFS/DROID supported Driver Commands.
 * 
 * @author Carl Nagle
 */
public class DDriverCommand extends DriverCommand {

	public static final String CALLSCRIPT                    = "CallScript";
	public static final String CLOSEAPPLICATION              = "CloseApplication";
	public static final String LAUNCHAPPLICATION             = "LaunchApplication";
	
	protected static final String TEMP_PROP_BLOCKID    = "BLOCKID"; 
	protected static final String TEMP_PROP_WINDOWNAME = "WINDOWNAME"; 
	protected static final String TEMP_PROP_COMPNAME   = "COMPNAME"; 
	protected static final String TEMP_PROP_TIMEOUT    = "TIMEOUT"; 
	protected static final String TEMP_PROP_VARIABLENAME   = "VARIABLENAME"; 
	protected static final String TEMP_PROP_FILENAME   = "FILENAME"; 
	protected static final String TEMP_PROP_ENCODING   = "ENCODING"; 
	protected static final String TEMP_PROP_APP_ROTATABLE   = "APP_ROTATABLE";
	protected static final String TEMP_PROP_SUBAREA   	= "SUBAREA";
	
	protected String command = null;
	protected Hashtable appids = new CaseInsensitiveHashtable();
	protected DTestRecordHelper droiddata = null;	 
	protected Properties props = new Properties();
    protected SAFSRemoteControl control = null;
    protected STAFHelper staf = null;
    
	/** used to pass values locally between process methods, and processResults */
	protected Properties temp_props = new Properties();
    
	public DDriverCommand() {
		super();
	}
	
	@Override
	public void setTestRecordData(TestRecordHelper data){
		super.setTestRecordData(data);
		droiddata = (DTestRecordHelper) data;
		staf = droiddata.getSTAFHelper();
		control = droiddata.controller;
	}

	/**
	 * Calls the default processing of interpretFields and then prepares the 
	 * droiddata (testRecordData) with the initial KeywordProperties of:
	 * <p><ul>
	 * KEY_COMMAND=non-null<br>
	 * KEY_PARAM_1-N  (param1 - paramN, even if > 9)<br>
	 * </ul>
	 * <p>
	 * Keyword implementations will need to add to these properties:
	 * <p><ul>
	 * KEY_TARGET<br>
	 * KEY_PARAM_TIMEOUT (may be different for each keyword)<br>
	 * </ul>
	 */
	@Override
	protected Collection interpretFields() throws SAFSException{
		String dbPrefix = "DDriverCommand.interpretFields ";
		Log.info(dbPrefix+"processing...");
		params = super.interpretFields();
		props.clear();
		props.setProperty(SAFSMessage.KEY_COMMAND, droiddata.getCommand());
		command = droiddata.getCommand();
		String tempstr = null;
		Iterator it = params.iterator();
		// "param"
		String key = SAFSMessage.PARAM_1.substring(0, SAFSMessage.PARAM_1.length()-1);
		String tmpkey = null;
		String val = null;
		int i = 1;
		Log.info(dbPrefix+"processing "+ params.size()+ " '"+ key +"' values...");
		while(it.hasNext()){
			try{
				val = it.next().toString();
				tmpkey = key + String.valueOf(i);
				props.setProperty(tmpkey, val);
			}catch(Exception x){
				Log.debug(dbPrefix+ x.getClass().getSimpleName()+", "+ x.getMessage());
			}
			i++;
		}		
		droiddata.setKeywordProperties(props);
		droiddata.setProcessRemotely(false); // we do it custom here, instead of in the calling JavaHook
		return params;
	}
	
	@Override
	public void process(){
		droiddata.setStatusCode(DriverConstant.STATUS_SCRIPT_NOT_EXECUTED);
		droiddata.setProcessRemotely(false);  // must be purposefully set to true
		try {
			params = interpretFields();
			Log.debug("DDC attempting to process "+ command);
			if(command.equalsIgnoreCase(LAUNCHAPPLICATION)) {
				doLaunchApplication();
			}
			else if(command.equalsIgnoreCase(CLOSEAPPLICATION)){
				doCloseApplication();				
			}
 			else if(command.equalsIgnoreCase(SAFSMessage.driver_onguiexistsgotoblockid)||
 					command.equalsIgnoreCase(SAFSMessage.driver_onguinotexistgotoblockid)){
				doGUIBranching();				
			}
 			else if(command.equalsIgnoreCase(SAFSMessage.driver_waitforgui)||
 					command.equalsIgnoreCase(SAFSMessage.driver_waitforguigone)){
				doWaitForGui();				
			}
 			else if(SAFSMessage.driver_clearclipboard.equalsIgnoreCase(command)||
 					SAFSMessage.driver_saveclipboardtofile.equalsIgnoreCase(command) ||
 					SAFSMessage.driver_verifyclipboardtofile.equalsIgnoreCase(command) ||
 					SAFSMessage.driver_assignclipboardvariable.equalsIgnoreCase(command) ||
 					SAFSMessage.driver_setclipboard.equalsIgnoreCase(command)){
				doClipboardCommand();
			}
 			else if(SAFSMessage.driver_takescreenshot.equalsIgnoreCase(command)){
				doTakeScreenShotCommand();
			}
 			else if(SAFSMessage.driver_hidesoftkeyboard.equalsIgnoreCase(command) ||
 					SAFSMessage.driver_showsoftkeyboard.equalsIgnoreCase(command) ||
 					SAFSMessage.driver_clearappmapcache.equalsIgnoreCase(command)){
 				doCommandWithoutPrameter(15);
 			}
/*			else if(command.equalsIgnoreCase(CALLSCRIPT)){
				doCallScript();				
			}
*/			
		} 
		catch (IllegalThreadStateException e) {
			Log.debug("DDC IllegalThreadStateException: "+ e.getMessage());
			issueActionFailure(e.getMessage());
		} 
		catch (RemoteException e) {
			Log.debug("DDC RemoteException: "+ e.getMessage());
			issueActionFailure(e.getMessage());
		} 
		catch (TimeoutException e) {
			Log.debug("DDC TimeoutException: "+ e.getMessage());
			issueActionFailure(e.getMessage());
		} 
		catch (ShutdownInvocationException e) {
			Log.debug("DDC ShutdownInvocationException: "+ e.getMessage());
			issueActionFailure(e.getMessage());
		} 
		catch (SAFSObjectRecognitionException e) {
			Log.debug("DDC recognition string missing or invalid for: "+ e.getMessage());
			issueParameterValueFailure(e.getMessage());
		} 
		catch (SAFSObjectNotFoundException e){
			Log.debug("DDC "+ e.getClass().getSimpleName() +": "+ e.getMessage());
			issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
									   e.getMessage() +" was not found.", e.getMessage()));
		} 
		catch (Throwable e) {
			Log.debug("DDC.process error: "+ e.getClass().getSimpleName()+": "+ e.getMessage());
		}
		if(! (testRecordData.getStatusCode()==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)){
			setRecordProcessed(true);
		}
	}

	/**
	 * Sets Property PARAM_TIMEOUT and then processes the completed remote control action properties.
	 * This method is called internally by individual keyword implementations. 
	 * After returning from remote processing the routine updates the {@link #droiddata} 
	 * via {@link DUtilities#captureRemoteResultsProperties(RemoteResults, DTestRecordHelper)}. 
	 * This routine then calls {@link #processResults(RemoteResults)}.
	 * @throws TimeoutException if the Ready, Running, or Results signals timeout.
	 * @throws IllegalThreadStateException if sockets communications failed.
	 * @throws ShutdownInvocationException if sockets communication signals a shutdown has commenced.
	 * @throws RemoteException if the remote client has issued an Exception.
	 * @see DUtilities#captureRemoteResultsProperties(RemoteResults, DTestRecordHelper)
	 */
	protected void processProperties(int param_timeout)throws IllegalThreadStateException,
	                                         RemoteException,
	                                         TimeoutException, 
	                                         ShutdownInvocationException{
		props.setProperty(SAFSMessage.PARAM_TIMEOUT, String.valueOf(param_timeout));
		droiddata.setKeywordProperties(props);
		props = control.performRemotePropsCommand(props, 
                droiddata.getReadyTimeout(), 
                droiddata.getRunningTimeout(), 
                param_timeout +5);
		RemoteResults results = new RemoteResults(props);
		droiddata = DUtilities.captureRemoteResultsProperties(results, droiddata);
		Log.info(getClass().getSimpleName()+ " " + command +" returned rc: "+ droiddata.getStatusCode()+", info: "+ droiddata.getStatusInfo());
		processResults(results);
	}
	
	/**
	 * called internally by {@link #processProperties(int)} AFTER the remote execution has completed.
	 */
	protected void processResults(RemoteResults results){
		
		if(SAFSMessage.driver_onguiexistsgotoblockid.equalsIgnoreCase(command)||
		   SAFSMessage.driver_onguinotexistgotoblockid.equalsIgnoreCase(command))
		{
			doGuiBranchingResults(results);
		}
		else
		if(SAFSMessage.driver_waitforgui.equalsIgnoreCase(command)||
		   SAFSMessage.driver_waitforguigone.equalsIgnoreCase(command))
		{
			doWaitForGuiResults(results);
		}
		else if(SAFSMessage.driver_clearclipboard.equalsIgnoreCase(command) ||
				SAFSMessage.driver_setclipboard.equalsIgnoreCase(command) ||
				SAFSMessage.driver_verifyclipboardtofile.equalsIgnoreCase(command) ||
				SAFSMessage.driver_saveclipboardtofile.equalsIgnoreCase(command) ||
				SAFSMessage.driver_assignclipboardvariable.equalsIgnoreCase(command)){
			doClipboardResults(results);
		}else if(SAFSMessage.driver_takescreenshot.equalsIgnoreCase(command)){
			doTakeScreenShotResults(results);
		}else if(SAFSMessage.driver_hidesoftkeyboard.equalsIgnoreCase(command) ||
				 SAFSMessage.driver_showsoftkeyboard.equalsIgnoreCase(command) ||
				 SAFSMessage.driver_clearappmapcache.equalsIgnoreCase(command)){
			doCommandWithoutPrameterResults(results);
		}
	}	
	
	/**
	 * Logs any message or message with detail that might be captured in the current {@link #droiddata}.  
	 * The routine does not set or change any statusCode or statusInfo information.
	 * <p>
	 * If a message was found and logged, the routine returns TRUE.  
	 * This means the caller does NOT have to log any success or failure message.  
	 * <p>
	 * If no message was found, then the routine will return FALSE and the caller 
	 * will need to log an appropriate message.
	 * 
	 * @param msgType -- int message type identifier constant as defined in 
	 * {@link AbstractLogFacility}.  
	 * @return true if we did find and handle a ResoureMessageInfo result. false otherwise.
	 */
	protected boolean processResourceMessageInfoResults(int msgType){
		ResourceMessageInfo msg = droiddata.getMessage();
		if(msg == null) return false;
		String message = msg.getMessage();
		if(message == null) return false;
		ResourceMessageInfo det = droiddata.getDetailMessage();
		String detail = (det == null) ? null: det.getMessage();
		if(detail == null){
			log.logMessage(droiddata.getFac(), message, msgType);
		}else{
			log.logMessage(droiddata.getFac(), message, msgType, detail);
		}
		return true;
	}
	
	/**
	 * Log a FAILED_MESSAGE using whatever ResourceMessageInfo data is returned in {@link #droiddata}.  
	 * The command does not change any statusCode or statusInfo information.
	 * Calls {@link #processResourceMessageInfoResults(int)} to attempt the logging.
	 * If there was no ResourceMessageInfo, we still log a generic failed message via {@link #issueActionFailure(String)}.
	 */
	protected void logResourceMessageFailure(){
		if(processResourceMessageInfoResults(FAILED_MESSAGE)) 
			return;		
		//log a generic failure if no ResourceMessageInfo was provided
		int status = droiddata.getStatusCode();
		try{issueActionFailure("SAFS StatusCode: "+ String.valueOf(status) +
				               "SAFS StatusInfo: "+ droiddata.getStatusInfo());}
		catch(Exception ignore){
			Log.debug("DDriverCommands.logResourceMessageFailure: "+ ignore.getMessage());
		}
		droiddata.setStatusCode(status);
	}
	
	/**
	 * OnGuiExistsGotoBlockID and OnGuiNotExistGotoBlockID commands setup.
	 * <p>
	 * Validates the presence of:<br>
	 * BLOCKID (PARAM_1)<br>
	 * WINDOWID (PARAM_2 and KEY_WINNAME)<br>
	 * COMPID (PARAM_3 and KEY_COMPNAME)<br>
	 * and the optional TIMEOUT parameter (PARAM_4 && PARAM_TIMEOUT)
	 * <p>
	 * Through {@link DUtilities#getAppMapRecognition(DTestRecordHelper)} adds:<br>
	 * KEY_WINREC<br>
	 * KEY_COMPREC<br>
	 * <p>
	 * The current implementation does expect recognition strings to exist in the App Map.
	 * If none are found, the command will issue a failure without making a remote call.
	 * <p>
	 * Upon successful execution of the command, the remote process should return:
	 * <p><ul>
	 * StatusCode = NO_SCRIPT_FAILURE (condition met) allow the branching to occur.<br>
	 * StatusCode = SCRIPT_WARNING (condition not met) but no failures occurred -- do not branch.<br>
	 * StatusCode = anything else will expect RemoteMessageInfo(s) to report the failure.
	 */
	protected void doGUIBranching() throws IllegalThreadStateException,
													RemoteException,
													TimeoutException,
													ShutdownInvocationException{
		
		String dbPrefix = "DDriverCommand.doGuiBranching ";
	    props.setProperty(SAFSMessage.KEY_TARGET, SAFSMessage.target_safs_driver);
	    if(params.size() < 3){
	    	Log.debug(dbPrefix+"insufficient number of parameters provided.");
	    	this.issueParameterCountFailure();
	    	return;
	    }
	    temp_props.clear();
	    Iterator it = params.iterator();
	    //BLOCKID
	    String param = (String)it.next();
	    if(param.length()==0){
	    	Log.debug(dbPrefix+"invalid or missing value for BLOCKID.");
	    	issueParameterValueFailure(TEMP_PROP_BLOCKID);
	    	return;
	    }
	    temp_props.put(TEMP_PROP_BLOCKID, param);
	    
	    //WINDOWNAME
	    param = (String)it.next();
	    if(param.length()==0){
	    	Log.debug(dbPrefix+"invalid or missing value for WINDOWID.");
	    	issueParameterValueFailure(TEMP_PROP_WINDOWNAME);
	    	return;
	    }
	    String winname = param;
	    droiddata.setWindowName(param);
	    props.setProperty(SAFSMessage.KEY_WINNAME, param);
	    
	    //COMPNAME
	    param = (String)it.next();
	    if(param.length()==0){
	    	Log.debug(dbPrefix+"invalid or missing value for COMPID.");
	    	issueParameterValueFailure(TEMP_PROP_COMPNAME);
	    	return;
	    }
	    String compname = param;
	    droiddata.setCompName(param);
	    props.setProperty(SAFSMessage.KEY_COMPNAME, param);
	    
	    droiddata = DUtilities.getAppMapRecognition(droiddata);

	    String winrec = null;
	    String cmprec = null;
	    try{ winrec = droiddata.getWindowGuiId(); }catch(Exception ignore){}
	    try{ cmprec = droiddata.getCompGuiId(); }catch(Exception ignore){}
	    if(winrec==null||cmprec==null||winrec.length()==0||cmprec.length()==0){
	    	Log.debug(dbPrefix+"invalid or missing Window and/or Component recognition.");
	    	issueParameterValueFailure(winname+":"+compname);
	    	return;
	    }

	    int timeout = 15;//seconds
	    if(it.hasNext()){
	    	param = (String) it.next();
	    	try{ 
	    		timeout = Integer.parseInt(param);
	    		if(timeout < 0) {
			    	Log.debug(dbPrefix+"ignoring invalid value for TIMEOUT: "+ param);
	    			timeout = 15;
	    		}
	    	}catch(Exception ignore){
		    	Log.debug(dbPrefix+"ignoring non-numeric value for TIMEOUT: "+ param);
	    	}
	    }
	    
    	Log.info(dbPrefix+"using TIMEOUT: "+ String.valueOf(timeout));
    	temp_props.setProperty(TEMP_PROP_TIMEOUT, String.valueOf(timeout));
	    processProperties(timeout);		
	}

	/**
	 * OnGuiExistsGotoBlockID and OnGuiNotExistGotoBlockID commands remote results.
	 * <p> 
	 * Upon successful execution of the command, the remote process should return:
	 * <p>
	 * <ul>
	 * StatusCode = NO_SCRIPT_FAILURE (condition met) allow the branching to occur.<br>
	 * StatusCode = SCRIPT_WARNING (condition not met) but no failures occurred -- do not branch.<br>
	 * StatusCode = anything else will expect RemoteMessageInfo(s) to report the failure.
	 * <p>
	 * @param results -- returned from the processProperties call.
	 */
	protected void doGuiBranchingResults(RemoteResults results) {
		setRecordProcessed(true);
		String blockName = temp_props.getProperty(TEMP_PROP_BLOCKID, null);
		String timeout = temp_props.getProperty(TEMP_PROP_TIMEOUT, "15");
		String msg = null;
		if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
			droiddata.setStatusCode(StatusCodes.BRANCH_TO_BLOCKID);
			droiddata.setStatusInfo(blockName);
			if(processResourceMessageInfoResults(GENERIC_MESSAGE)) return; //handled
		    //onguiexists -- branch
		    if (SAFSMessage.driver_onguiexistsgotoblockid.equalsIgnoreCase(command)) {
	      		//we were searching for gui, since it was found, attempt branch
	      		msg = GENStrings.convert(GENStrings.BRANCHING, 
	      				command +" attempting branch to "+ blockName +".", 
	      				command, blockName);
	      		msg += "  "+ GENStrings.convert(GENStrings.FOUND_TIMEOUT, 
	      				droiddata.getCompName() +" found within timeout "+ timeout, 
	      				droiddata.getCompName(), timeout);
		    }
		    //onguinotexist -- branch
		    else {
	      		//we were searching for no gui, since it wasn't found, branch
	      		msg = GENStrings.convert(GENStrings.BRANCHING, 
	      				command +" attempting branch to "+ blockName +".", 
	      				command, blockName);
	      		msg += "  "+ GENStrings.convert(GENStrings.NOT_EXIST, 
	      				droiddata.getCompName() +" does not exist", 
	      				droiddata.getCompName());
		    }
		    log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);  
		}
		else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_WARNING){
			if(processResourceMessageInfoResults(GENERIC_MESSAGE)) return; //handled
		    //onguiexists -- no branch
		    if (SAFSMessage.driver_onguiexistsgotoblockid.equalsIgnoreCase(command)) {
	      		//we were searching for gui, since it wasn't found, don't branch
	      		msg = GENStrings.convert(GENStrings.NOT_BRANCHING, 
	      				command +" not branching to "+ blockName +".", 
	      				command, blockName);
	      		msg += "  "+ FAILStrings.convert(FAILStrings.NOT_FOUND_TIMEOUT, 
	      				droiddata.getCompName() +" not found within timeout "+ timeout, 
	      				droiddata.getCompName(), timeout);
		    }
		    // onGuiNotExist -- no branch
		    else{
	      		//we were searching for no gui, since it was found, don't branch
	      		msg = GENStrings.convert(GENStrings.NOT_BRANCHING, 
	      				command +" not branching to "+ blockName +".", 
	      				command, blockName);
	      		msg += "  "+ GENStrings.convert(GENStrings.EXISTS, 
	      				droiddata.getCompName() +" exists", 
	      				droiddata.getCompName());
		    }
		    log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);  
		}
		else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			// driver will log warning...
			setRecordProcessed(false);
		}
		else{ //object not found? what?		
			logResourceMessageFailure();
		}
	}

	/**
	 * WaitForGui and WaitForGuiGone commands setup.
	 * <p>
	 * Validates the presence of:<br>
	 * WINDOWID (PARAM_1 && KEY_WINNAME)<br>
	 * COMPID (PARAM_2 && KEY_COMPNAME)<br>
	 * and the optional TIMEOUT parameter (PARAM_3 && PARAM_TIMEOUT)
	 * <p>
	 * Through {@link DUtilities#getAppMapRecognition(DTestRecordHelper)} adds:<br>
	 * KEY_WINREC<br>
	 * KEY_COMPREC<br>
	 * <p>
	 * The current implementation does expect recognition strings to exist in the App Map.
	 * If none are found, the command will issue a failure without making a remote call.
	 * <p>
	 * Upon successful execution of the command, the remote process should return:
	 * <p><ul>
	 * StatusCode = NO_SCRIPT_FAILURE -- condition met in timeout period.<br>
	 * StatusCode = SCRIPT_WARNING -- condition not met in timeout period.<br>
	 * StatusCode = anything else will expect RemoteMessageInfo(s) to report the failure.
	 */
	protected void doWaitForGui() throws IllegalThreadStateException,
													RemoteException,
													TimeoutException,
													ShutdownInvocationException{
		
		String dbPrefix = "DDriverCommand.doWaitForGui ";
	    props.setProperty(SAFSMessage.KEY_TARGET, SAFSMessage.target_safs_driver);
	    if(params.size() < 2){
	    	Log.debug(dbPrefix+"insufficient number of parameters provided.");
	    	this.issueParameterCountFailure();
	    	return;
	    }
	    temp_props.clear();
	    Iterator it = params.iterator();
	    //WINDOWNAME
	    String param = (String)it.next();
	    if(param.length()==0){
	    	Log.debug(dbPrefix+"invalid or missing value for WINDOWID.");
	    	issueParameterValueFailure(TEMP_PROP_WINDOWNAME);
	    	return;
	    }
	    String winname = param;
	    droiddata.setWindowName(param);
	    props.setProperty(SAFSMessage.KEY_WINNAME, param);
	    
	    //COMPNAME
	    param = (String)it.next();
	    if(param.length()==0){
	    	Log.debug(dbPrefix+"invalid or missing value for COMPID.");
	    	issueParameterValueFailure(TEMP_PROP_COMPNAME);
	    	return;
	    }
	    String compname = param;
	    droiddata.setCompName(param);
	    props.setProperty(SAFSMessage.KEY_COMPNAME, param);
	    
	    droiddata = DUtilities.getAppMapRecognition(droiddata);

	    String winrec = null;
	    String cmprec = null;
	    try{ winrec = droiddata.getWindowGuiId(); }catch(Exception ignore){}
	    try{ cmprec = droiddata.getCompGuiId(); }catch(Exception ignore){}
	    if(winrec==null||cmprec==null||winrec.length()==0||cmprec.length()==0){
	    	Log.debug(dbPrefix+"invalid or missing Window and/or Component recognition.");
	    	issueParameterValueFailure(winname+":"+compname);
	    	return;
	    }
	    int timeout = 15;//seconds
	    if(it.hasNext()){
	    	param = (String) it.next();
	    	try{ 
	    		timeout = Integer.parseInt(param);
	    		if(timeout < 0) {
			    	Log.debug(dbPrefix+"ignoring invalid value for TIMEOUT: "+ param);
	    			timeout = 15;
	    		}
	    	}catch(Exception ignore){
		    	Log.debug(dbPrefix+"ignoring non-numeric value for TIMEOUT: "+ param);
	    	}
	    }
    	Log.info(dbPrefix+"using TIMEOUT: "+ String.valueOf(timeout));
    	temp_props.setProperty(TEMP_PROP_TIMEOUT, String.valueOf(timeout));
	    processProperties(timeout);		
	}
	
	/**
	 * WaitForGui and WaitForGuiGone commands remote results.
	 * <p> 
	 * Upon successful execution of the command, the remote process should return:
	 * <p>
	 * <ul>
	 * StatusCode = NO_SCRIPT_FAILURE -- condition met in timeout period.<br>
	 * StatusCode = SCRIPT_WARNING -- condition not met in timeout period.<br>
	 * StatusCode = anything else will expect RemoteMessageInfo(s) to report the failure.
	 * <p>
	 * @param results -- returned from the processProperties call.
	 */
	protected void doWaitForGuiResults(RemoteResults results) {
		setRecordProcessed(true);
		String timeout = temp_props.getProperty(TEMP_PROP_TIMEOUT, "15");
		if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
			if(processResourceMessageInfoResults(GENERIC_MESSAGE)) return; //handled
		    if (SAFSMessage.driver_waitforgui.equalsIgnoreCase(command)) {
		        issueGenericSuccess(GENStrings.convert(GENStrings.FOUND_TIMEOUT, 
		      		droiddata.getCompName() +" was found in timeout "+ timeout,
		      		droiddata.getCompName(), timeout));
		    }
		    else { // waitforguigone
		        issueGenericSuccess(GENStrings.convert(GENStrings.GONE_TIMEOUT, 
		        	droiddata.getCompName() +" was gone in timeout "+ timeout,
		        	droiddata.getCompName(), timeout));
		    }
		}
		else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_WARNING){
			if(processResourceMessageInfoResults(WARNING_MESSAGE)) return; //handled
		    if (SAFSMessage.driver_waitforgui.equalsIgnoreCase(command)) {
		    	issueActionWarning(FAILStrings.convert(FAILStrings.NOT_FOUND_TIMEOUT, 
		        	droiddata.getCompName() + " could not be found in timeout "+ timeout,
		        	droiddata.getCompName(), timeout));
		    }
		    else{ // waitforguigone 
		    	issueActionWarning(FAILStrings.convert(FAILStrings.NOT_GONE_TIMEOUT, 
		    		droiddata.getCompName() +" not gone in timeout "+ timeout, 
		    		droiddata.getCompName(), timeout));
		    }
		}
		else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			// driver will log warning...
			setRecordProcessed(false);
		}
		else{ //object not found? what?		
			logResourceMessageFailure();
		}
	}

	/**
	 * Process clipboard related keywords:<br>
	 * AssignClipboardVariable<br>
	 * ClearClipboard<br>
	 * SaveClipboardToFile<br>
	 * SetClipboard<br>
	 * VerifyClipboardToFile<br>
	 * 
	 * <p>
	 * Validates the presence of:<br>
	 * FileName (PARAM_1) for keyword SaveClipboardToFile and VerifyClipboardToFile<br>
	 * ContentToSet (PARAM_1) for keyword SetClipboard<br>
	 * VariableName (PARAM_1) for keyword AssignClipboardVariable<br>
	 * <p>
	 * Then pass the command to the remote side to be processed.
	 * <p>
	 * Upon successful execution of the command, the remote process should return:
	 * <p><ul>
	 * StatusCode = NO_SCRIPT_FAILURE -- condition met in timeout period.<br>
	 * StatusCode = anything else will expect RemoteMessageInfo(s) to report the failure.
	 */	
	protected void doClipboardCommand() throws IllegalThreadStateException,
												RemoteException,
												TimeoutException,
												ShutdownInvocationException{
		String dbPrefix = "DDriverCommand.doClipboardCommand(): ";
	    props.setProperty(SAFSMessage.KEY_TARGET, SAFSMessage.target_safs_driver);
	    
	    //filename or
	    //content to set to clipboard or
	    //variable to be set with clipboard's content
	    String param = null;
	    temp_props.clear();

	    //verify the parameter
	    if(SAFSMessage.driver_saveclipboardtofile.equalsIgnoreCase(command) ||
	       SAFSMessage.driver_verifyclipboardtofile.equalsIgnoreCase(command) ||
	       SAFSMessage.driver_assignclipboardvariable.equalsIgnoreCase(command) ||
	       SAFSMessage.driver_setclipboard.equalsIgnoreCase(command)){
	    	
	    	if(params.size() < 1){
	    		Log.debug(dbPrefix+"insufficient number of parameters provided.");
	    		issueParameterCountFailure();
	    		return;
	    	}
	    	
	    	Iterator iter = params.iterator();
	    	param = (String) iter.next();
		    if(SAFSMessage.driver_saveclipboardtofile.equalsIgnoreCase(command) ||
		    		SAFSMessage.driver_verifyclipboardtofile.equalsIgnoreCase(command)){
		    	if (param.length() == 0) {
					Log.debug(dbPrefix+ "invalid or missing value for filename.");
					issueParameterValueFailure(TEMP_PROP_FILENAME);
					return;
				}
		    	temp_props.setProperty(TEMP_PROP_FILENAME, param);
		    	String encoding = "UTF-8";
		    	if(iter.hasNext()){
		    		encoding = (String) iter.next();
		    	}
		    	temp_props.setProperty(TEMP_PROP_ENCODING, encoding);
		    	
		    }else if(SAFSMessage.driver_assignclipboardvariable.equalsIgnoreCase(command)){
			   	if (param.length() == 0) {
					Log.debug(dbPrefix+ "invalid or missing value for variablename.");
					issueParameterValueFailure(TEMP_PROP_VARIABLENAME);
						return;
					}
			   	temp_props.setProperty(TEMP_PROP_VARIABLENAME, param);
		    }else if(SAFSMessage.driver_setclipboard.equalsIgnoreCase(command)){
		    	props.setProperty(SAFSMessage.PARAM_1, param);
		    }
	    }
					
	    int timeout = 15;//seconds
    	Log.info(dbPrefix+"using TIMEOUT: "+ String.valueOf(timeout));
    	
	    processProperties(timeout);			
	}

	/**
	 * This method is used to check the parameter of keyword TakeScreenShot.<br>
	 * Filename is the required parameter.<br>
	 * Rotatable is the optional parameter.<br>
	 * 
	 * @throws IllegalThreadStateException
	 * @throws RemoteException
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	protected void doTakeScreenShotCommand() throws IllegalThreadStateException,
			RemoteException, TimeoutException, ShutdownInvocationException {
		String dbPrefix = "DDriverCommand.doTakeScreenShotCommand(): ";
		props.setProperty(SAFSMessage.KEY_TARGET, SAFSMessage.target_safs_driver);

		String param = null;
		temp_props.clear();

		// verify the parameter, filename
		if (SAFSMessage.driver_takescreenshot.equalsIgnoreCase(command)) {

			if (params.size() < 1) {
				Log.debug(dbPrefix + "insufficient number of parameters provided.");
				issueParameterCountFailure();
				return;
			}

			Iterator iter = params.iterator();
			param = (String) iter.next();
			if (SAFSMessage.driver_takescreenshot.equalsIgnoreCase(command)) {
				if (param.length() == 0) {
					Log.debug(dbPrefix + "invalid or missing value for filename.");
					issueParameterValueFailure(TEMP_PROP_FILENAME);
					return;
				}
				temp_props.setProperty(TEMP_PROP_FILENAME, param);
				
				if(iter.hasNext()){
					temp_props.setProperty(TEMP_PROP_APP_ROTATABLE, (String)iter.next());
				}
				if(iter.hasNext()){
					temp_props.setProperty(TEMP_PROP_SUBAREA, (String)iter.next());
				}
			}
		}
		
	    int timeout = 15;//seconds
    	Log.info(dbPrefix+"using TIMEOUT: "+ String.valueOf(timeout));
    	
	    processProperties(timeout);	
	}
	
	/**
	 * Process result of keyword TakeScreenShot.<br>
	 * The remote device side will return the rotate degree of the device.<br>
	 * The ScreenShot will be got through API of the AndroidDebugBridger at computer side.<br>
	 * The returned image is always that you put your device at the default direction.<br>
	 * We should rotate that returned image back if the tested android application is rotatable.<br>
	 * 
	 * @param results RemoteResults, returned from the processProperties call.
	 */
	protected void doTakeScreenShotResults(RemoteResults results){
		String debugmsg = "DDriverCommand.doTakeScreenShotResults(): ";
	    //currently we offer support for JPG, BMP, TIF, GIF, PNG and PNM, default to bmp
		int rotation = 0;
		String filename = temp_props.getProperty(TEMP_PROP_FILENAME);
	    
	    //build File
	    File fn=null;
		try {
			filename = ImageUtils.normalizeFileNameSuffix(filename);
			fn = deduceTestFile(filename);
		} catch (SAFSException e) {
	    	Log.debug(debugmsg+e.getMessage());
	    	issueParameterValueFailure("OutputFile "+e.getMessage());
	    	return;
		}
	    
	    //Get the device rotation status from the device side
	    if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
	    	rotation = results.getInt(SAFSMessage.KEY_REMOTERESULTINFO);
	    	Log.debug(debugmsg+"The mobile device's rotation is "+rotation);
	    }
	    
		try {
			IDevice device = null;
			BufferedImage screenImage = null;
			//Be default, we consider the mobile application is rotatable
			boolean rotatable = true;
			String subarea = temp_props.getProperty(TEMP_PROP_SUBAREA);

			device = DUtilities.getIDevice();
			if(device!=null){
				if(temp_props.containsKey(TEMP_PROP_APP_ROTATABLE)){
					rotatable = StringUtilities.convertBool(temp_props.getProperty(TEMP_PROP_APP_ROTATABLE)); 
				}
				screenImage = DUtilities.getDeviceScreenImage(device,rotation, rotatable);
			}else{
				Log.debug(debugmsg+"Can't get mobile device!");
			}

			if(screenImage!=null){
				if(subarea!=null && !subarea.isEmpty()){
					Rectangle screenrec = new Rectangle();
					screenrec.setRect(0, 0, screenImage.getWidth(), screenImage.getHeight());
					Rectangle subrec = ImageUtils.getSubAreaRectangle(screenrec, subarea);
					screenImage = screenImage.getSubimage(subrec.x, subrec.y, subrec.width, subrec.height);
				}
				ImageUtils.saveImageToFile(screenImage, fn, 1.0F);
				issueGenericSuccess("Screenshot was saved to file " + fn.getAbsolutePath());
			}else{
				issueActionFailure("Can't retrieve screen image from device.");
			}

		} catch (Exception x) {
			Log.debug(x.getMessage(), x);
			issueErrorPerformingActionOnX(fn.getAbsolutePath(), x.getMessage());
		}
	}
	
	/**
	 * Process result of clipboard related keywords:<br>
	 * AssignClipboardVariable<br>
	 * ClearClipboard<br>
	 * SaveClipboardToFile<br>
	 * SetClipboard<br>
	 * VerifyClipboardToFile<br>
	 * <p> 
	 * Upon successful execution of the command, the remote process should return:
	 * <p>
	 * <ul>
	 * StatusCode = NO_SCRIPT_FAILURE -- remote execution succeed. We need continue process at local side.<br>
	 * StatusCode = anything else will expect RemoteMessageInfo(s) to report the failure.
	 * <p>
	 * @param results -- returned from the processProperties call.
	 */	
	protected void doClipboardResults(RemoteResults results) {
		setRecordProcessed(true);
		
		if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
			if(processResourceMessageInfoResults(GENERIC_MESSAGE)) return; //handled
			
		    if (SAFSMessage.driver_clearclipboard.equalsIgnoreCase(command)) {
		        issueGenericSuccess("Android's clipboard has been successfully reset.");
		    }
		    else if (SAFSMessage.driver_setclipboard.equalsIgnoreCase(command)) {
		    	issueGenericSuccess("Android's clipboard has been set to "+results.getString(SAFSMessage.PARAM_1));
		    }
		    else if (SAFSMessage.driver_saveclipboardtofile.equalsIgnoreCase(command)) {
		    	String clipboardContent = results.getString(SAFSMessage.KEY_REMOTERESULTINFO);
		    	String filename = temp_props.getProperty(TEMP_PROP_FILENAME);
		    	String encoding = temp_props.getProperty(TEMP_PROP_ENCODING);
		    	Log.debug("Clipboard's content: "+clipboardContent);
		    	Log.debug("Filename: "+filename+"; Encoding: "+encoding);
		    	
		        File fn = new CaseInsensitiveFile(filename).toFile();
		        if (!fn.isAbsolute()) {
		            String pdir = null;
					try {
						pdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
					} catch (SAFSException e) {
						Log.warn("Met "+e.getClass().getSimpleName()+":"+e.getMessage());
					}
		            if (pdir == null) pdir="";
		           	fn = new CaseInsensitiveFile(pdir, filename).toFile();
		            filename = fn.getAbsolutePath();
		    	} 
		    	
		        try {
					FileUtilities.writeStringToFile(filename, encoding, clipboardContent);
			    	issueGenericSuccess(GENStrings.convert(GENStrings.BE_SAVED_TO, 
			    			"'"+clipboardContent+"' has been saved to '"+filename+"'",
			    			clipboardContent, filename));
				} catch (Exception e) {
					Log.error("Met "+e.getClass().getSimpleName()+":"+e.getMessage());
					String error = GENStrings.convert(FAILKEYS.FILE_WRITE_ERROR,"Error writing to file '"+filename+"'",filename);
					issueErrorPerformingAction(error);
				}
		        
		    }
		    else if (SAFSMessage.driver_verifyclipboardtofile.equalsIgnoreCase(command)) {
		    	String clipboardContent = results.getString(SAFSMessage.KEY_REMOTERESULTINFO);
		    	String filename = temp_props.getProperty(TEMP_PROP_FILENAME);
		    	String encoding = temp_props.getProperty(TEMP_PROP_ENCODING);
		    	Log.debug("Clipboard's content: "+clipboardContent);
		    	Log.debug("Filename: "+filename+"; Encoding: "+encoding);
		    	
		        File fn = new CaseInsensitiveFile(filename).toFile();
		        if (!fn.isAbsolute()) {
		            String pdir = null;
					try {
						pdir = getVariable(STAFHelper.SAFS_VAR_BENCHDIRECTORY);
					} catch (SAFSException e) {
						Log.warn("Met "+e.getClass().getSimpleName()+":"+e.getMessage());
					}
		            if (pdir == null) pdir="";
		           	fn = new CaseInsensitiveFile(pdir, filename).toFile();
		            filename = fn.getAbsolutePath();
		    	} 
		    	
		        try {
					String fileContent = FileUtilities.readStringFromEncodingFile(filename, encoding);
					if(fileContent.equals(clipboardContent)){
				    	issueGenericSuccess("Clipboard's content equals to the provided value.");
					}else{
						String error = GENStrings.convert(FAILKEYS.SOMETHING_NOT_MATCH,
								"Clipboard value '"+clipboardContent+"' does not match expected value '"+fileContent+"'",
								"Clipboard", clipboardContent, fileContent);
						issueErrorPerformingAction(error);
					}
				} catch (Exception e) {
					Log.error("Met "+e.getClass().getSimpleName()+":"+e.getMessage());
					String error = GENStrings.convert(FAILKEYS.FILE_READ_ERROR,"Error reading from file '"+filename+"'",filename);
					issueErrorPerformingAction(error);
				}

		    }
		    else if (SAFSMessage.driver_assignclipboardvariable.equalsIgnoreCase(command)) {
		    	String clipboardContent = results.getString(SAFSMessage.KEY_REMOTERESULTINFO);
		    	String variablename = temp_props.getProperty(TEMP_PROP_VARIABLENAME);
		    	Log.debug("Clipboard's content: "+clipboardContent);
		    	Log.debug("Variablename: "+variablename);
		    	
		    	try {
		    		if(staf.setVariable(variablename, clipboardContent)){
				    	issueGenericSuccess(GENStrings.convert(GENStrings.SOMETHING_SET, 
				    			"'"+clipboardContent+"' set to '"+variablename+"'",
				    			clipboardContent, variablename));
		    		}else{
						String error = GENStrings.convert(FAILKEYS.COULD_NOT_SET,
								"Could not set '"+clipboardContent+"' to '"+variablename+"'",
								clipboardContent, variablename);
						issueErrorPerformingAction(error);
		    		}
		    	} catch (Exception e) {
		    		Log.error("Met "+e.getClass().getSimpleName()+":"+e.getMessage());
		    		String error = GENStrings.text(FAILKEYS.COULD_NOT_SET_VARS,"Could not set one or more variable values.");
		    		issueErrorPerformingAction(error);
		    	}
		    	
		    }
		}
		else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
			setRecordProcessed(false);
		}
		else if(droiddata.getStatusCode()==StatusCodes.GENERAL_SCRIPT_FAILURE){	
			logResourceMessageFailure();
		}
	}
	
	/**
	 * This method is used to execute a keyword without parameter:<br>
	 * <ul>
	 * 	<li>HideSoftKeyboard
	 * 	<li>ShowSoftKeyboard
	 * </ul>
	 * 
	 * @throws IllegalThreadStateException
	 * @throws RemoteException
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	protected void doCommandWithoutPrameter(int timeout/** second*/) throws IllegalThreadStateException,
			RemoteException, TimeoutException, ShutdownInvocationException {
		String dbPrefix = "DDriverCommand.doCommandWithoutPrameter(): ";
		props.setProperty(SAFSMessage.KEY_TARGET, SAFSMessage.target_safs_driver);

		temp_props.clear();
		
    	Log.info(dbPrefix+"using TIMEOUT: "+ String.valueOf(timeout));
    	
	    processProperties(timeout);	
	}
	
	/**
	 * This method is used to handle result of a keyword without parameter:<br>
	 * <ul>
	 * 	<li>HideSoftKeyboard
	 * 	<li>ShowSoftKeyboard
	 * </ul>
	 * 
	 * @param results RemoteResults, returned from the processProperties call.
	 */
	protected void doCommandWithoutPrameterResults(RemoteResults results){
	    if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
	    	issueGenericSuccess(command+" has be executed successfully.");
	    }else{
	    	issueActionFailure("Fail to execute "+command);
	    }
	}
	
	/**
	 * LaunchApplication implementation.<br>
	 * Requires no parameters and any provided parameters will be ignored.
	 */
	private void doLaunchApplication() throws IllegalThreadStateException,
	                                            RemoteException,
	                                            TimeoutException,
	                                            ShutdownInvocationException{
		SAFSWorker worker = droiddata.getSAFSWorker();
		boolean success = worker.startMainLauncher();
		RemoteResults results = new RemoteResults(worker._last_remote_result);
		if(success && results.isRemoteResult()){
			if(results.getStatusCode()==SAFSMessage.STATUS_REMOTERESULT_OK){
				String activity = results.getString(SAFSMessage.PARAM_NAME, "Main Activity");
				issueGenericSuccess(activity);
				return;
			}
		}
		String error = failedText.convert(TXT_FAILURE_1, "Unable to perform "+command, command);
		if(results.isRemoteResult()) error += ". "+ results.getString(SAFSMessage.PARAM_ERRORMSG, "startMainLauncher unsuccessful.");
		issueActionFailure(error);
	}
	
	
	/**
	 * CloseApplication implementation.
	 * Currently does not use the ApplicationID.  It simply "finishes" all open activities 
	 * related to the test app.
	 */
	private void doCloseApplication()throws IllegalThreadStateException,
                                              RemoteException,
                                              TimeoutException,
                                              ShutdownInvocationException{
		SAFSWorker worker = droiddata.getSAFSWorker();
		boolean success = worker.finishOpenedActivities();
		RemoteResults results = new RemoteResults(worker._last_remote_result);
		if(success && results.isRemoteResult()){
			if(results.getStatusCode()==SAFSMessage.STATUS_REMOTERESULT_OK){
				issueGenericSuccess(null);
				return;
			}
		}
		String error = failedText.convert(TXT_FAILURE_1, "Unable to perform "+command, command);
		if(results.isRemoteResult()) error += ". "+ results.getString(SAFSMessage.PARAM_ERRORMSG, "finishOpenActivities unsuccessful.");
		issueActionFailure(error);
	}
	
	/**
	 * CallScript implementation.
	 * Currently does nothing but issue success.
	 */
	private void doCallScript() {
	    issueGenericSuccess("Issuing success but CallScript is not implemented yet.");		
	}
}
