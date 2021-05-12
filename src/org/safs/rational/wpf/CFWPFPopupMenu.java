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
package org.safs.rational.wpf;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.rational.Script;
import org.safs.text.FAILStrings;

import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;

/**
 * <br><em>Purpose:</em> 	process a popup menu component of domain .NET_WPF
 * <br><em>Lifetime:</em> 	instantiated by TestStepProcessor
 * <br><em>Note:</em>		As we need this class to treate .NET_WPF popup menu and CFPopupMenu can not treate .NET_WPF related menu,
 * 							We did not extends CFPopupMenu. We keep the keyword "SelectIndex" the same as in CFPopupMenu.
 * <p>
 * @author  Lei	Wang
 * @since   Sep 29, 2009
 * 
 **/
public class CFWPFPopupMenu extends CFWPFMenuBar {
	public static final String SELECTINDEX                     = "SelectIndex";
	
	public CFWPFPopupMenu(){
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
		menuObj.click(Script.localAtIndex(index));
		String altText = windowName+":"+compName+" "+action+" successful.";
		String message = passedText.convert(TXT_SUCCESS_3, altText,windowName, compName, action);
        log.logMessage(testRecordData.getFac(),message,PASSED_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
	}
}
