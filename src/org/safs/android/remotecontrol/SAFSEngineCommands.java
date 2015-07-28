/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.android.remotecontrol;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;

import com.jayway.android.robotium.remotecontrol.solo.RemoteResults;
import com.jayway.android.robotium.remotecontrol.solo.Solo;

/**
 * Provide an Android RemoteControl API interface to SAFS Engine Commands for non-SAFS users using a SAFSTestRunner.
 * <p>
 * Default usage:
 * <p><pre>
 * Instantiate *and* initialize Solo API object according to the Solo class, then:
 * 
 * SAFSEngineCommands engine = new SAFSEngineCommands(solo);
 * (use the API)
 * </pre>
 * Note: SAFSEngineCommands can use component UID references that have been acquired from either 
 * the remote control Solo class APIs or the SAFSEngineCommands APIs.
 * <p>
 * @author Carl Nagle, SAS Institute, Inc.
 * @see org.safs.android.remotecontrol.SoloTest
 */
public class SAFSEngineCommands extends SAFSWorker {

	private Solo solo = null;
	private static final String TAG = "SAFSEngineCommands.";
	
	/**
	 * Default (required) public constructor.
	 * @param solo Solo instance already created and initialized.
	 */
	public SAFSEngineCommands(Solo solo){
		super();		
		setRemoteControl(solo.getRemoteControl(), true);
	}
	
	/* hidden for non-use */
	private SAFSEngineCommands() {
		super();
	}

	/**
	 * Used internally. 
	 * Prepare a dispatchProps object targeting a remote "safs_engine" command instead of a remote "instrument" 
	 * or "solo" command.
	 * @param command
	 * @return Properties ready to be populated with command-specific parameters.
	 */
	private Properties prepEngineDispatch(String command){
		_props = super.prepSoloDispatch(command);
		_props.setProperty(SAFSMessage.KEY_TARGET, SAFSMessage.target_safs_engine);
		return _props;
	}
	
	/**
	 * Converts a delimited string representing an array of Strings as returned by several engine commands.
	 * It treats the first character as the delimiter used to separate the remaining Strings.
	 * @param svalue -- String value usually returned from a remote engine command.  A null value will result 
	 * in a returned String[0].
	 * @return String[] -- array of 0 or more Strings. 
	 */
	private String[] convertDelimitedStrings(String svalue){
		if(svalue == null || svalue.length() < 2) return new String[0];
		String sep = svalue.substring(0, 1);
		svalue = svalue.substring(1);
		return svalue.split(sep);
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsClearHighlightedDialog.html">ClearHighlightedDialog</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public void clearHighlightedDialog()throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{		
		Properties props = prepEngineDispatch(SAFSMessage.engine_clearhighlighteddialog);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()||results.getStatusCode()!=SAFSMessage.STATUS_REMOTERESULT_OK) throw new RemoteException("clearHighlightedDialog did not execute properly on remote device.");
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsClearReferenceCache.html">ClearReferenceCache</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public void clearReferenceCache()throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_clearreferencecache);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()||results.getStatusCode()!=SAFSMessage.STATUS_REMOTERESULT_OK) throw new RemoteException("clearreferencecache did not execute properly on remote device.");
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetAccessibleName.html">GetAccessibleName</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String getAccessibleName(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getaccessiblename);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getAccessibleName did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 
			throw new RemoteException("getAccessibleName did not return with success.");
		return results.getStatusInfo();
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetCaption.html">GetCaption</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String getCaption(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getcaption);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getCaption did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 
			throw new RemoteException("getCaption did not return with success.");
		return results.getStatusInfo();
	}
		
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetCurrentWindow.html">GetCurrentWindow</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String getCurrentWindow()throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getcurrentwindow);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getCurrentWindow did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("getCurrentWindow did not return with success.");
		return results.getStatusInfo();
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetChildCount.html">GetChildCount</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public int getChildCount(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getchildcount);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getChildCount did not execute properly on remote device.");
		try{
			if(results.getStatusCode()==SAFSMessage.STATUS_REMOTERESULT_OK){ 
				return Integer.parseInt(results.getStatusInfo());
			}else{
				throw new RemoteException("getChildCount did not return with success.");
			}
		}catch(NumberFormatException x){
			throw new RemoteException("getChildCount returned something other than an integer: "+ results.getStatusInfo());
		}
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetChildren.html">GetChildren</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String[] getChildren(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getchildren);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getChildren did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 
			throw new RemoteException("getChildren did not return with success.");
		return convertDelimitedStrings(results.getStatusInfo());
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetClassIndex.html">GetClassIndex</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public int getClassIndex(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getclassindex);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) throw new RemoteException("getClassIndex did not execute properly on remote device.");
		try{
			if(results.getStatusCode()==SAFSMessage.STATUS_REMOTERESULT_OK){ 
				return Integer.parseInt(results.getStatusInfo());
			}else{
				throw new RemoteException("getClassIndex did not return with success.");
			}
		}catch(NumberFormatException x){
			throw new RemoteException("getClassIndex returned something other than an integer: "+ results.getStatusInfo());
		}
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetClassname.html">GetClassname</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String getClassname(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getclassname);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getClassname did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("getClassname did not return with success.");
		return results.getStatusInfo();
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetId.html">GetId</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String getId(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getid);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getId did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("getId did not return with success.");
		return results.getStatusInfo();
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetMatchingChildObjects.html">GetMatchingChildObjects</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String getMatchingChildObjects(String parentUID, String recognition)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{		
		Properties props = prepEngineDispatch(SAFSMessage.engine_getmatchingchildobjects);
		props.setProperty(SAFSMessage.PARAM_1, parentUID);
		props.setProperty(SAFSMessage.PARAM_2, recognition);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getMatchingChildObjects did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("getMatchingChildObjects did not return with success.");
		return results.getStatusInfo();
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetMatchingParentObject.html">GetMatchingParentObject</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String getMatchingParentObject(String recognition)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getmatchingparentobject);
		props.setProperty(SAFSMessage.PARAM_1, recognition);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getMatchingParentObject did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("getMatchingParentObject did not return with success.");
		return results.getStatusInfo();
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetName.html">GetName</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String getName(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getname);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getName did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("getName did not return with success.");
		return results.getStatusInfo();
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetNonAccessibleName.html">GetNonAccessibleName</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String getNonAccessibleName(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getnonaccessiblename);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getNonAccessibleName did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("getNonAccessibleName did not return with success.");
		return results.getStatusInfo();
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetProperty.html">GetProperty</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String getProperty(String uID, String propertyName)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getproperty);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		props.setProperty(SAFSMessage.PARAM_2, propertyName);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getProperty did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("getProperty did not return with success.");
		return results.getStatusInfo();
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetPropertyNames.html">GetPropertyNames</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String[] getPropertyNames(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getpropertynames);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getPropertyNames did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("getPropertyNames did not return with success.");
		return convertDelimitedStrings(results.getStatusInfo());
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetSuperClassnames.html">GetSuperClassnames</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String[] getSuperclassNames(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_getsuperclassnames);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getSuperclassNames did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("getSuperclassNames did not return with success.");
		return convertDelimitedStrings(results.getStatusInfo());
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetText.html">GetText</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String getText(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_gettext);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getText did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("getText did not return with success.");
		return results.getStatusInfo();
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetTopLevelCount.html">GetTopLevelCount</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public int getTopLevelCount()throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_gettoplevelcount);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getTopLevelCount did not execute properly on remote device.");
		if(results.getStatusCode()==SAFSMessage.STATUS_REMOTERESULT_OK)		
			throw new RemoteException("getTopLevelCount did not return with success.");
		try{ return Integer.parseInt(results.getStatusInfo());}catch(NumberFormatException x){
			throw new RemoteException("getTopLevelCount returned something other than an integer: "+ results.getStatusInfo());
		}		
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsGetTopLevelWindows.html">GetTopLevelWindows</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public String[] getTopLevelWindows()throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_gettoplevelwindows);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("getTopLevelWindows did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("getTopLevelWindows did not return with success.");
		return convertDelimitedStrings(results.getStatusInfo());
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsHighlightMatchingChildObjectByKey.html">HighlightMatchingChildObjectByKey</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public void highlightMatchingChildObjectByKey(String parentUID, String childUID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_highlightmatchingchildobjectbykey);
		props.setProperty(SAFSMessage.PARAM_1, parentUID);
		props.setProperty(SAFSMessage.PARAM_2, childUID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()||results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 
			throw new RemoteException("highlightMatchingChildObjectByKey did not execute properly on remote device.");
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsIsEnabled.html">IsEnabled</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public boolean isEnabled(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_isenabled);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("isEnabled did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("isEnabled did not return with success.");
		return Boolean.parseBoolean(results.getStatusInfo());
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsIsShowing.html">IsShowing</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public boolean isShowing(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_isshowing);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("isShowing did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("isShowing did not return with success.");
		return Boolean.parseBoolean(results.getStatusInfo());
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsIsValid.html">IsValid</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public boolean isValid(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_isvalid);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("isValid did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("isValid did not return with success.");
		return Boolean.parseBoolean(results.getStatusInfo());
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsIsTopLevelPopupContainer.html">IsTopLevelPopupContainer</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public boolean isTopLevelPopupContainer(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_istoplevelpopupcontainer);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()) 
			throw new RemoteException("isTopLevelPopupContainer did not execute properly on remote device.");
		if(results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 		
			throw new RemoteException("isTopLevelPopupContainer did not return with success.");
		return Boolean.parseBoolean(results.getStatusInfo());
	}
	
	/**
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/AndroidEngineComponentCommandsSetActiveWindow.html">SetActiveWindow</a>
	 * @throws IllegalThreadStateException
	 * @throws RemoteException -- if a remote exception was thrown, or if the status indicates the command 
	 * did not execute properly on the remote device or emulator.
	 * @throws TimeoutException
	 * @throws ShutdownInvocationException
	 */
	public void setActiveWindow(String uID)throws IllegalThreadStateException, RemoteException, TimeoutException, ShutdownInvocationException{
		Properties props = prepEngineDispatch(SAFSMessage.engine_setactivewindow);
		props.setProperty(SAFSMessage.PARAM_1, uID);
		_last_remote_result = control.performRemotePropsCommand(props, default_ready_stimeout, default_running_stimeout, default_result_stimeout);
		RemoteResults results = new RemoteResults(_last_remote_result);
		if(!results.isRemoteResult()||results.getStatusCode()!= SAFSMessage.STATUS_REMOTERESULT_OK) 
			throw new RemoteException("setActiveWindow did not execute properly on remote device.");
	}
}
