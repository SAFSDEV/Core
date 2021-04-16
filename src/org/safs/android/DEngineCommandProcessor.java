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
package org.safs.android;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.safs.EngineCommandProcessor;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.TestRecordHelper;
import org.safs.android.remotecontrol.SAFSMessage;
import org.safs.android.remotecontrol.SAFSRemoteControl;
import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;
import org.safs.text.GENStrings;
import org.safs.text.ResourceMessageInfo;
import org.safs.tools.drivers.DriverConstant;

import com.jayway.android.robotium.remotecontrol.solo.RemoteResults;

/**
 * Used to route <a href="/sqabasic2000/SAFSReference.php?rt=E&lib=EngineComponentCommands"
 * target="_blank" title="SAFS Engine Commands Reference" alt="SAFS Engine Commands Reference">EngineCommands</a>
 * to the remote SAFSTestRunner.
 * <p>
 * The SAFSTestRunner should expect to execute from Properties with the following settings:
 * <p>
 * <ul>
 * SAFSMessage.KEY_TARGET=SAFSMessage.target_safs_engine<br>
 * SAFSMessage.KEY_COMMAND=&lt;enginecommand><br>
 * SAFSMessage.PARAM_TIMEOUT=&lt;seconds><br>
 * </ul>
 * <p>
 * Upon a valid and complete execution the SAFSTestRunner is expected to return:
 * <p>
 * <ul>
 * SAFSMessage.KEY_ISREMOTERESULT=true<br>
 * SAFSMessage.KEY_REMOTERESULTCODE=int<br>
 * SAFSMessage.KEY_REMOTERESULTINFO=String<br>
 * </ul>
 * <p>
 * Any command returning a REMOTERESULTINFO should format it in the exact manner documented in the
 * SAFS Keyword Reference for that command.  This processor will simply forward that info into the
 * testRecordData used by the engine using this processor.
 * <p>
 * As specific commands require additional parameters they are sent as:
 * <p>
 * <ul>
 * SAFSMessage.PARAM_1=val<br>
 * SAFSMessage.PARAM_2=val<br>
 * etc...<br>
 * SAFSMessage.PARAM_9=val<br>
 * </ul>
 * <p>
 * For these Engine Commands, this processor sets global SAFSVARS variables that allow a running test
 * to reference the statuscode and statusinfo resulting from the last engine command executed.  See {@link #setStatusVars()}.
 * @author Carl Nagle, SAS Institute, Inc.
 */
public class DEngineCommandProcessor extends EngineCommandProcessor {

	public static final String TAG = "DECP: ";
	public static final String STATUSINFO = "StatusInfo"; // Var DroidEngine.StatusInfo
	public static final String STATUSCODE = "StatusCode"; // Var DroidEngine.StatusCode
	public static final String VAR_PREFIX = "DroidEngine."; // Prefix
	public static final String COMMAND = "Command";       // Var DroidEngine.Command

	/** 'getCurrentWindow' */
	public static final String COMMAND_GET_CURRENT_WINDOW = "getCurrentWindow";
	/** 'isEnabled' */
	public static final String COMMAND_IS_ENABLED = "isEnabled";

	/** simple class cast of existing testRecordData */
	protected DTestRecordHelper droiddata = null; //cast of testRecordData
	protected Properties props = new Properties();
	protected SAFSRemoteControl control = null;
	/**
	 *
	 * @param controller
	 */
	public DEngineCommandProcessor() {
		super();
	}

	@Override
	public void setTestRecordData(TestRecordHelper data){
		super.setTestRecordData(data);
		droiddata = (DTestRecordHelper) data;
	}


	/**
	 * Calls the default processing of interpretFields and then prepares the
	 * droiddata (testRecordData) with the initial KeywordProperties of:
	 * <p><ul>
	 * KEY_TARGET=safs_engine<br>
	 * KEY_COMMAND=command<br>
	 * </ul>
	 * <p>
	 * Also insures our control object is set to the current SAFSRemoteControl instance
	 * stored in droiddata.
	 */
	@Override
	protected Collection interpretFields() throws SAFSException{
		Collection c = super.interpretFields();
		droiddata = (DTestRecordHelper) testRecordData; //convenience
		props.clear();
		props.setProperty(SAFSMessage.KEY_TARGET, SAFSMessage.target_safs_engine);
		props.setProperty(SAFSMessage.KEY_COMMAND, command);
		droiddata.setKeywordProperties(props);
		control = droiddata.getRemoteController();
		droiddata.setProcessRemotely(false); // we do it custom here, instead of in the calling JavaHook
		return c;
	}

	protected void logGenericSuccess(){
		String msg = genericText.convert(GENStrings.SUCCESS_1, command +" successful.", command);
		String detail = genericText.convert(GENStrings.SOMETHING_SET, VAR_PREFIX+STATUSINFO +" set to "+droiddata.getStatusInfo(),
				                            VAR_PREFIX+STATUSINFO, droiddata.getStatusInfo());
		log.logMessage(droiddata.getFac(), msg, detail);
	}

	protected void logGenericWarning(){
		String msg = "";
		String description = "";
		ResourceMessageInfo message = droiddata.getMessage();
		ResourceMessageInfo detailMessage = droiddata.getDetailMessage();
		if(message!=null){
			//TODO how to set the alttext???
			msg = failedText.convert(message.getKey(), "altext", message.getParams());
		}else{
			msg = genericText.convert(GENStrings.SOMETHING_SET, VAR_PREFIX+STATUSINFO +" set to "+droiddata.getStatusInfo(),
                    VAR_PREFIX+STATUSINFO, droiddata.getStatusInfo());
		}
		if(detailMessage!=null){
			description = failedText.convert(detailMessage.getKey(), "alttext", detailMessage.getParams());
		}
		log.logMessage(droiddata.getFac(), msg, description, WARNING_MESSAGE);
	}

	/**
	 * Reset the global SAFSVARS variables that store the most recent command results:
	 * <pre><ul>
	 * DroidEngine.Command = ""
	 * DroidEngine.StatusCode = "IGNORE_RETURN_CODE"  (StatusCodes.STR_IGNORE_RETURN_CODE)
	 * DroidEngine.StatusInfo = ""
	 * </ul></pre>
	 * @see StatusCodes#getStatusString(int)
	 */
	protected void resetStatusVars(){
		try{ setVariable(VAR_PREFIX+COMMAND, "");}catch(Exception x){}
		try{ setVariable(VAR_PREFIX+STATUSCODE, StatusCodes.STR_IGNORE_RETURN_CODE);}catch(Exception x){}
		try{ setVariable(VAR_PREFIX+STATUSINFO, "");}catch(Exception x){}
	}

	/**
	 * Sets the global SAFSVARS variables to the values of the most recent (last) engine command
	 * executed:
	 * <pre><ul>
	 * DroidEngine.Command = [command]
	 * DroidEngine.StatusCode = [statuscode string]
	 * DroidEngine.StatusInfo = [statusinfo]
	 * </ul></pre>
	 * <p>
	 * Which can be referenced in SAFS tests as normal SAFS variables:
	 * <pre><ul>
	 * ^DroidEngine.Command
	 * ^DroidEngine.StatusCode
	 * ^DroidEngine.StatusInfo
	 * </ul></pre>
	 * <p>
	 * These variables will retain these values until the execution of another engine command by
	 * this processor. So, they can be parsed and manipulated by Driver Commands and other processes
	 * until they are reset by another engine command.
	 * <p>
	 * @see StatusCodes#getStatusString(int)
	 */
	protected void setStatusVars(){
		try{ setVariable(VAR_PREFIX+COMMAND, command);}catch(Exception x){}
		try{ setVariable(VAR_PREFIX+STATUSINFO, droiddata.getStatusInfo());}catch(Exception x){}
		try{ setVariable(VAR_PREFIX+STATUSCODE, StatusCodes.getStatusString(droiddata.getStatusCode()));}catch(Exception x){}
	}

	@Override
	public void process(){

		setRecordProcessed(false);
		droiddata.setProcessRemotely(false); // we are handling the remote processing here, by default.
		try{
			//params to contain inputrecord fields 2-N -- which is params 1 - N)
			params = interpretFields();
			Log.info("DEngineCommandProcessor attempting "+ command);
			RemoteResults results = null;
			// 0 params, 15 seonds timeout commands
			if( command.equalsIgnoreCase(COMMAND_GET_CURRENT_WINDOW)  ||
			    command.equalsIgnoreCase(COMMAND_GET_TOPLEVEL_COUNT)  ||
			    command.equalsIgnoreCase(COMMAND_GET_TOPLEVEL_WINDOWS) ||
			    SAFSMessage.engine_clearhighlighteddialog.equalsIgnoreCase(command)){
				props.setProperty(SAFSMessage.PARAM_TIMEOUT, String.valueOf(15));
				resetStatusVars();
				props = control.performRemotePropsCommand(props,
						                                  droiddata.getReadyTimeout(),
						                                  droiddata.getRunningTimeout(),
						                                  15);
				results = new RemoteResults(props);
				droiddata = DUtilities.captureRemoteResultsProperties(results, droiddata);
				Log.info(TAG + command +" returned rc: "+ droiddata.getStatusCode()+", info: "+ droiddata.getStatusInfo());
				setRecordProcessed(true);
				if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
					setStatusVars();
					logGenericSuccess();
				}else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
					// driver will log warning...
					setRecordProcessed(false);
				}else{
					setStatusVars();
					logGenericWarning();
				}
			}else
			// 1 param, 15 seconds timeout commands
			if( command.equalsIgnoreCase(COMMAND_GET_ACCESSIBLENAME) ||
				command.equalsIgnoreCase(COMMAND_GET_CAPTION)        ||
				command.equalsIgnoreCase(COMMAND_GET_CHILD_COUNT)    ||
				command.equalsIgnoreCase(COMMAND_GET_CHILDREN)       ||
				command.equalsIgnoreCase(COMMAND_GET_CLASSINDEX)     ||
				command.equalsIgnoreCase(COMMAND_GET_CLASSNAME)      ||
				command.equalsIgnoreCase(COMMAND_GET_ID)             ||
				command.equalsIgnoreCase(COMMAND_GET_NAME)           ||
				command.equalsIgnoreCase(COMMAND_GET_NONACCESSIBLENAME) ||
				command.equalsIgnoreCase(COMMAND_GET_PROPERTY_NAMES) ||
				command.equalsIgnoreCase(COMMAND_GET_STRING_DATA)    ||
				command.equalsIgnoreCase(COMMAND_GET_SUPER_CLASSNAMES)  ||
				command.equalsIgnoreCase(COMMAND_GET_TEXT)           ||
				command.equalsIgnoreCase(COMMAND_IS_ENABLED)         ||
				command.equalsIgnoreCase(COMMAND_IS_SHOWING)         ||
				command.equalsIgnoreCase(COMMAND_IS_VALID)           ||
				command.equalsIgnoreCase(COMMAND_IS_TOPLEVEL_POPUP_CONTAINER)||
				command.equalsIgnoreCase(COMMAND_SET_ACTIVE_WINDOW)){
				if(validateParamSize(1)){
					Iterator it = params.iterator();
					String p = (String) it.next();
					props.setProperty(SAFSMessage.PARAM_TIMEOUT, String.valueOf(15));
					props.setProperty(SAFSMessage.PARAM_1, p);
					resetStatusVars();
					props = control.performRemotePropsCommand(props,
							                                  droiddata.getReadyTimeout(),
							                                  droiddata.getRunningTimeout(),
							                                  15);
					results = new RemoteResults(props);
					droiddata = DUtilities.captureRemoteResultsProperties(results, droiddata);
					Log.info(TAG + command +" returned rc: "+ droiddata.getStatusCode()+", info: "+ droiddata.getStatusInfo());
					setRecordProcessed(true);
					if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
						setStatusVars();
						logGenericSuccess();
					}else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
						// driver will log warning...
						setRecordProcessed(false);
					}else{
						setStatusVars();
						logGenericWarning();
					}
				}
			}else
			// 2 params, 15 seconds timeout commands
			if( command.equalsIgnoreCase(COMMAND_GET_PROPERTY) ||
				SAFSMessage.engine_highlightmatchingchildobjectbykey.equalsIgnoreCase(command)){
				if(validateParamSize(2)){
					Iterator it = params.iterator();
					String p1 = (String) it.next(); //validate it?
					String p2 = (String) it.next(); //validate it?
					props.setProperty(SAFSMessage.PARAM_TIMEOUT, String.valueOf(15));
					props.setProperty(SAFSMessage.PARAM_1, p1);
					props.setProperty(SAFSMessage.PARAM_2, p2);
					resetStatusVars();
					props = control.performRemotePropsCommand(props,
							                                  droiddata.getReadyTimeout(),
							                                  droiddata.getRunningTimeout(),
							                                  15);
					results = new RemoteResults(props);
					droiddata = DUtilities.captureRemoteResultsProperties(results, droiddata);
					Log.info(TAG + command +" returned rc: "+ droiddata.getStatusCode()+", info: "+ droiddata.getStatusInfo());
					setRecordProcessed(true);
					if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
						setStatusVars();
						String msg = genericText.convert(GENStrings.SUCCESS_2, command +" "+ p2 +" successful.", command, p2);
						String detail = genericText.convert(GENStrings.SOMETHING_SET, VAR_PREFIX+STATUSINFO +" set to "+droiddata.getStatusInfo(),
			                    VAR_PREFIX+STATUSINFO, droiddata.getStatusInfo());
						log.logMessage(droiddata.getFac(), msg, detail);
					}else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
						// driver will log warning...
						setRecordProcessed(false);
					}else{
						setStatusVars();
						logGenericWarning();
					}
				}
			}else
			// 1 param, 120 seconds timeout commands
			if(command.equalsIgnoreCase(COMMAND_GET_MATCHING_PARENT_OBJECT)){
				if(validateParamSize(1)){
					Iterator it = params.iterator();
					String p = (String) it.next(); //validate it?
					props.setProperty(SAFSMessage.PARAM_TIMEOUT, String.valueOf(120));
					props.setProperty(SAFSMessage.PARAM_1, p);
					resetStatusVars();
					props = control.performRemotePropsCommand(props,
							                                  droiddata.getReadyTimeout(),
							                                  droiddata.getRunningTimeout(),
							                                  120);
					results = new RemoteResults(props);
					droiddata = DUtilities.captureRemoteResultsProperties(results, droiddata);
					Log.info(TAG + command +" returned rc: "+ droiddata.getStatusCode()+", info: "+ droiddata.getStatusInfo());
					setRecordProcessed(true);
					if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
						setStatusVars();
						logGenericSuccess();
					}else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
						// driver will log warning...
						setRecordProcessed(false);
					}else{
						setStatusVars();
						logGenericWarning();
					}
				}
			}else
			// 2 params, 120 seconds timeout commands
			if( command.equalsIgnoreCase(COMMAND_IS_MATCHING_PATH)           ||
				command.equalsIgnoreCase(COMMAND_GET_MATCHING_CHILD_OBJECTS) ||
				command.equalsIgnoreCase(COMMAND_GET_MATCHING_PATH_OBJECT)){
				if(validateParamSize(2)){
					Iterator it = params.iterator();
					String p1 = (String) it.next(); //validate it?
					String p2 = (String) it.next(); //validate it?
					props.setProperty(SAFSMessage.PARAM_TIMEOUT, String.valueOf(60));
					props.setProperty(SAFSMessage.PARAM_1, p1);
					props.setProperty(SAFSMessage.PARAM_2, p2);
					resetStatusVars();
					props = control.performRemotePropsCommand(props,
							                                  droiddata.getReadyTimeout(),
							                                  droiddata.getRunningTimeout(),
							                                  60);
					results = new RemoteResults(props);
					droiddata = DUtilities.captureRemoteResultsProperties(results, droiddata);
					Log.info(TAG + command +" returned rc: "+ droiddata.getStatusCode()+", info: "+ droiddata.getStatusInfo());
					setRecordProcessed(true);
					if(droiddata.getStatusCode()==StatusCodes.NO_SCRIPT_FAILURE){
						setStatusVars();
						logGenericSuccess();
					}else if(droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
						// driver will log warning...
						setRecordProcessed(false);
					}else{
						setStatusVars();
						logGenericWarning();
					}
				}
			}
			else if(COMMAND_GET_DOMAINNAME.equalsIgnoreCase(command)){
				//The domain is always "Android"
				droiddata.setStatusInfo(DriverConstant.ANDROID_CLIENT_TEXT);
				droiddata.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				logGenericSuccess();
			}
		}
		catch(SAFSException x){
			Log.debug(TAG+ command +" SAFSException: "+ x.getMessage(), x);
		} catch (IllegalThreadStateException e) {
			Log.debug(TAG+ command +" IllegalThreadStateException: "+ e.getMessage(), e);
		} catch (RemoteException e) {
			Log.debug(TAG+ command +" RemoteException: "+ e.getMessage(), e);
		} catch (TimeoutException e) {
			Log.debug(TAG+ command +" TIMEOUT reached. "+ e.getMessage());
		} catch (ShutdownInvocationException e) {
			Log.debug(TAG+ command +" ShutdownInvocationException: "+ e.getMessage(), e);
		}
	}
}
