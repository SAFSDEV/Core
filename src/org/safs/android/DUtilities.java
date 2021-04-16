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

import java.util.Properties;

import org.safs.Log;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.android.remotecontrol.SAFSMessage;
import org.safs.text.ResourceMessageInfo;
import org.safs.tools.GenericProcessMonitor;
import org.safs.tools.ProcessMonitor;
import org.safs.tools.drivers.DriverConstant;

import com.jayway.android.robotium.remotecontrol.solo.RemoteResults;

/**
 * Utilities for the Droid testing environment and Android Developer SDK tools.
 * 
 * @author Carl Nagle
 */
public class DUtilities extends org.safs.android.auto.lib.DUtilities {

	static{
		utils = new org.safs.android.DUtilities();
	}

	@Override
	protected void debugI(String message){
		Log.debug(message);		
	}
	
	/** SAFSDIR variable export from STAFEnv.sh script **/
	public static String rootRunDir = System.getenv("SAFSDIR");
	
	/**
	 * Returns a blank instance of ProcessMonitor on which to call the necessary 
	 * static methods.  Subclasses should override to get an appropriate subclass of 
	 * GenericProcessMonitor.
	 */
	protected static  GenericProcessMonitor getProcessMonitor(){
		return mon == null ? new ProcessMonitor(): mon;
	}
	
	/**
	 * Convert SoloRemoteControl status codes to equivalent SAFS StatusCodes.
	 * Currently, we handle:<pre>
	 * {@link SAFSMessage#STATUS_REMOTE_NOT_EXECUTED} = {@link StatusCodes#SCRIPT_NOT_EXECUTED}
	 * {@link SAFSMessage#STATUS_REMOTERESULT_OK} = {@link StatusCodes#NO_SCRIPT_FAILURE}
	 * {@link SAFSMessage#STATUS_REMOTERESULT_WARN} = {@link StatusCodes#SCRIPT_WARNING}
	 * </pre><br>
	 * Everything else is converted to {@link StatusCodes#GENERAL_SCRIPT_FAILURE}
	 */
	public static int convertRemoteControlStatus(int status){
		switch(status){
			case SAFSMessage.STATUS_REMOTE_NOT_EXECUTED:
				return StatusCodes.SCRIPT_NOT_EXECUTED;
				
			case SAFSMessage.STATUS_REMOTERESULT_OK:
				return StatusCodes.NO_SCRIPT_FAILURE;
				
			case SAFSMessage.STATUS_REMOTERESULT_WARN:
				return StatusCodes.SCRIPT_WARNING;
		}
		return StatusCodes.GENERAL_SCRIPT_FAILURE;
	}
	
	/**
	 * Modifies the TestRecordHelper statuscode and statusinfo from the remote results properties.
	 * Only changes the statuscode and statusinfo if KEY_ISREMOTERESULT exists and is true.  After that, 
	 * statuscode will be set to SCRIPT_NOT_EXECUTED unless modified by KEY_REMOTERESULTCODE.  
	 * statusinfo will be set to null unless modified by KEY_REMOTERESULTINFO.
	 * <p>
	 * The routine will also attempt to instantiate and store any message and details sent as 
	 * ResourceMessageInfos stored in the RemoteResults.
	 * @param props
	 * @param data
	 * @return same data object with statuscode, statusinfo, and any message and detail 
	 * ResourceMessageInfo objects set.
	 * @see DTestRecordHelper#getMessage()
	 * @see DTestRecordHelper#getDetailMessage()
	 * @see ResourceMessageInfo
	 * 
	 */
	public static DTestRecordHelper captureRemoteResultsProperties(RemoteResults results, DTestRecordHelper data){
		if(results.hasItem(SAFSMessage.KEY_ISREMOTERESULT)){
			boolean isRemote = false;
			isRemote = results.getBoolean(SAFSMessage.KEY_ISREMOTERESULT, false);
			if(isRemote){
				String info = null;
				// !!! note REMOTE RESULTCODES are NOT the same as SAFS STATUS CODES !!!!
				int status = results.getInt(SAFSMessage.KEY_REMOTERESULTCODE, SAFSMessage.STATUS_REMOTE_NOT_EXECUTED);
				info = results.getString(SAFSMessage.KEY_REMOTERESULTINFO, "");
				data.setStatusCode(convertRemoteControlStatus(status));
				if(SAFSMessage.NULL_VALUE.equals(info)) info = DriverConstant.SAFS_NULL;
				data.setStatusInfo(info);
			}
			
			ResourceMessageInfo message = new ResourceMessageInfo();
			String msgKey = results.getString(SAFSMessage.RESOURCE_BUNDLE_KEY_FOR_MSG, null);
			String detailMsgKey = results.getString(SAFSMessage.RESOURCE_BUNDLE_KEY_FOR_DETAIL_MSG, null);
			if(msgKey!=null){
				String resourceBundle = results.getString(SAFSMessage.RESOURCE_BUNDLE_NAME_FOR_MSG, null);
				String delimitedParams = results.getString(SAFSMessage.RESOURCE_BUNDLE_PARAMS_FOR_MSG, null);
				String altText = results.getString(SAFSMessage.RESOURCE_BUNDLE_ALTTEXT_FOR_MSG, null);
				String delimiter = "";
				String resteParamters = "";
				
				message.setKey(msgKey);
				if(resourceBundle!=null) message.setResourceBundleName(resourceBundle);
				if(delimitedParams!=null && delimitedParams.length()>0){
					delimiter = delimitedParams.substring(0, 1);
					resteParamters = delimitedParams.substring(1);
					message.setParams(StringUtils.getTokenList(resteParamters, delimiter));
				}
				message.setAltText(altText);
				data.setMessage(message);
			}else{
				data.setMessage(null);
			}
			
			if(detailMsgKey!=null){
				String resourceBundle = results.getString(SAFSMessage.RESOURCE_BUNDLE_NAME_FOR_DETAIL_MSG, null);
				String delimitedParams = results.getString(SAFSMessage.RESOURCE_BUNDLE_PARAMS_FOR_DETAIL_MSG, null);
				String altText = results.getString(SAFSMessage.RESOURCE_BUNDLE_ALTTEXT_FOR_DETAIL_MSG, null);
				String delimiter = "";
				String resteParamters = "";
				
				message.reset();
				message.setKey(detailMsgKey);
				if(resourceBundle!=null) message.setResourceBundleName(resourceBundle);
				if(delimitedParams!=null && delimitedParams.length()>0){
					delimiter = delimitedParams.substring(0, 1);
					resteParamters = delimitedParams.substring(1);
					message.setParams(StringUtils.getTokenList(resteParamters, delimiter));
				}
				message.setAltText(altText);
				data.setDetailMessage(message);
			}else{
				data.setDetailMessage(null);
			}
		}
		return data;
	}
	
	/**
	 * Attempts to fill the DTestHelper winGuiID and compGuiID fields as well as the 
	 * Keyword Properties KEY_WINREC and KEY_COMPREC properties 
	 * using the Keyword Properties and STAFHelper in the provided DTestRecordHelper.
	 * <p>
	 * The routine expects the STAFHelper, windowName, compName, and appMapName fields 
	 * have valid values prior to this call.  
	 * <p>
	 * If the named App Map does NOT contain entries 
	 * for the window and/or component then the window and/or component names themselves 
	 * are used as the recognition information.  This helps support recognition like 
	 * "CurrentWindow" and unused window or component values like "Anything", "AtAll". 
	 * @param droiddata
	 * @return droiddata with updated Keyword Properties and GuiID entries.
	 */
	public static DTestRecordHelper getAppMapRecognition(DTestRecordHelper droiddata){
		String dbPrefix = "DUtilities.getAppMapRecognition ";
		String tempstr = null;
		Properties props = droiddata.getKeywordProperties();
		String windowname = "";
		String compname = "";
		try{windowname = droiddata.getWindowName();}catch(Exception ignore){}
		try{compname = droiddata.getCompName();}catch(Exception ignore){}
		try{
			tempstr = droiddata.getSTAFHelper().getAppMapItem(
					  droiddata.getAppMapName(), 
					  windowname, 
					  windowname);
			if(tempstr != null) {
				props.setProperty(SAFSMessage.KEY_WINREC, tempstr);
				droiddata.setWindowGuiId(tempstr);
			}
		}catch(Exception x){
			Log.info(dbPrefix+"WINREC "+ x.getClass().getSimpleName() +" is being ignored at this time...");
		}finally{
			if(tempstr == null){
				Log.info(dbPrefix+"using Window NAME '"+ windowname+"' as WINREC...");
				props.setProperty(SAFSMessage.KEY_WINREC, windowname);
				droiddata.setWindowGuiId(windowname);
			}
		}
		tempstr = null;
		try{
			tempstr = droiddata.getSTAFHelper().getAppMapItem(
					  droiddata.getAppMapName(), 
					  windowname, 
					  compname);
			if(tempstr != null) {
				props.setProperty(SAFSMessage.KEY_COMPREC, tempstr);
				droiddata.setCompGuiId(tempstr);
			}
		}catch(Exception x){
			Log.info(dbPrefix+"COMPREC "+ x.getClass().getSimpleName() +" is being ignored at this time...");
		}finally{
			if(tempstr == null){
				Log.info(dbPrefix+"using Component NAME '"+ compname+"' as COMPREC...");
				props.setProperty(SAFSMessage.KEY_COMPREC, compname);
				droiddata.setCompGuiId(compname);
			}
		}
		droiddata.setKeywordProperties(props);
		return droiddata;
	}
}
