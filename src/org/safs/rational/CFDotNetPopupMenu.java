/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.rational;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.text.FAILStrings;

import com.rational.test.ft.TargetGoneException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;

/**
 * <br><em>Purpose:</em> 	CFDotNetPopupMenu, process a .NET PopupMenu component
 * <em>Note:</em>			As we need this class to treate .NET popup menu and CFPopupMenu can not treate .NET related menu,
 * 							We did not extends CFPopupMenu. We keep the keyword "SelectIndex" the same as in CFPopupMenu.
 * @author  Lei Wang
 * @since   JUL 30, 2008
 *   <br>	JUL 30, 2008	(LeiWang)	Original Release
 **/
public class CFDotNetPopupMenu extends CFDotNetMenuBar {
	public static final String SELECTINDEX                     = "SelectIndex";
	
	public CFDotNetPopupMenu(){
		super();
	}
	
	/** <br><em>Purpose:</em> process: process the testRecordData
	 * The actions handled here are:
	 * <br><ul>
	 * <li>selectIndex
	 * </ul><br>
	 * The following actions are handled in CFMenuBar:
	 * <br><ul>
	 * <li>selectPopupMenuItem
	 * <li>verifyPopupMenuItem
	 * <li>verifyPopupMenuPartialMatch
	 * <li>SelectUnverifiedPopupMenuItem
	 * <li>VerifyPopupMenu
	 * </ul><br>
	 * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
	 * based on the result of the processing
	 * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
	 * <br><em>Assumptions:</em>  none
	 **/	
	
	protected void localProcess(){
		Log.debug(getClass().getName()+".localProcess():  action: "+action+"; win: "+ windowName +"; comp: "+compName);
		
		try{
			//Do specific work related to .NET popup menu
			if(action.equalsIgnoreCase(SELECTINDEX)){
				selectIndex();
			}
		
			//If the record is not executed, let its superclass to treate
			if(testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED){
				Log.debug(getClass().getName()+".localProcess(): can not treate action "+action);		
				super.localProcess();
			}
		}catch(SAFSException ex){
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			String alttext = "Unable to perform "+action+" on "+compName+" in "+windowName+".";
			String message = failedText.convert(FAILStrings.FAILURE_3, alttext, windowName,compName,action);
			log.logMessage(testRecordData.getFac(),message+" : "+ex.getMessage(),FAILED_MESSAGE);
		}
	}
	
	/**
	 * <em>Note:</em>		Analyse the parameters, prepare them for executing.
	 */
	protected void selectIndex() throws SAFSException{
		String debugmsg = getClass().getName()+".selectIndex() ";
		
		if(params.size()<1){
			paramsFailedMsg(windowName,compName);
			return;
		}
		
		int index = 0;
		GuiSubitemTestObject menuObj = null;
		String indexString = params.iterator().next().toString();
		
		try{
			index = Integer.parseInt(indexString);
			menuObj = new GuiSubitemTestObject(obj1.getObjectReference());
			selectIndex(menuObj, index);
		}catch(NumberFormatException ex){
			Log.debug(debugmsg+ex.getMessage());
			String altmsg = "Invalid parameter value for "+indexString;
			String message = failedText.convert(FAILStrings.BAD_PARAM, altmsg, indexString);
			throw new SAFSException(message);
		}
	}
	
	protected void selectIndex(GuiSubitemTestObject menuObj,int index) throws SAFSException{
		try{ menuObj.click(Script.localAtIndex(index));}
		catch(TargetGoneException tg){
			Log.info("...selectIndex IGNORING TargetGoneException likely resulting from intended window closure...");
		}
		String altText = windowName+":"+compName+" "+action+" successful.";
		String message = passedText.convert(TXT_SUCCESS_3, altText,windowName, compName, action);
        log.logMessage(testRecordData.getFac(),message,PASSED_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
	}
}
