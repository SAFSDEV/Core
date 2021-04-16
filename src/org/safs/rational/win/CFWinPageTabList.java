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
package org.safs.rational.win;

import java.util.ArrayList;
import java.util.List;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.rational.CFPageTabList;
import org.safs.rational.Script;
import org.safs.text.FAILStrings;

import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;


/**
 * <br><em>Purpose:</em> CFWinPageTabList, process a WinPageTabList component (.Pagetablist)
 * @author  JunwuMa
 * @since   DEC 9, 2008
 * 
 **/
public class CFWinPageTabList extends CFPageTabList {
	public static final String PROPERTY_NAME = ".name";
	public static final String PROPERTY_TEXT = ".text";
	
	/** <br><em>Purpose:</em> constructor, calls super
	 **/
	public CFWinPageTabList () {
		super();
	}

	/** <br><em>Purpose:</em> process: process the testRecordData
	 ** <br>This is our specific version for WinPagetablist component.
	 ** The actions handled here are:
	 * <br><ul>
	 * <li>commandWithOneParam()
	 * </ul><br>
	 * The following actions are handled in CFMenuBar:
	 * <br><ul>
	 * <li>CLICKTAB
	 * <li>UNVERIFIEDCLICKTAB
	 * <li>SELECTTAB
	 * <li>MAKESELECTION
	 * </ul><br>
	 ** The types of objects handled here are '{@link GuiSubitemTestObject}'.
	 * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
	 * based on the result of the processing
	 * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
	 * <br><em>Assumptions:</em>  none
	 **/
	protected void localProcess() {
        Log.info(getClass().getName()+".localProcess():  action: " + action + "; win: " + windowName + "; comp: " + compName);
		try {
	        // process following commands on specific Win Pagetablist, making the commands have same behaviour on Win Pagetablist. 
	        if (action.equalsIgnoreCase(CLICKTAB)||
	            action.equalsIgnoreCase(UNVERIFIEDCLICKTAB)||
	            action.equalsIgnoreCase(SELECTTAB) ||
	            action.equalsIgnoreCase(MAKESELECTION))	{        
	        	commandWithOneParam();
	        } else 
	        	super.localProcess();
	    } catch (SAFSException ex) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			String alttext = "Unable to perform " + action + " on " + compName + " in " + windowName + ".";
			String message = failedText.convert(FAILStrings.FAILURE_3, alttext, windowName,compName,action);
			log.logMessage(testRecordData.getFac(),message+" : "+ex.getMessage(),FAILED_MESSAGE);
	    }
	}
  
	/** <br><em>Purpose:</em> process commands like: CLICKTAB, UNVERIFIEDCLICKTAB, SELECTTAB and MAKESELECTION
	 */
	protected void commandWithOneParam() throws SAFSException {
		if (params.size() < 1) {
			paramsFailedMsg(windowName, compName);
		} else {
			String param1 = (String) params.iterator().next();
			Log.info("... "+ action + " param: " + param1);

			if (action.equalsIgnoreCase(CLICKTAB)||
				action.equalsIgnoreCase(UNVERIFIEDCLICKTAB)||
				action.equalsIgnoreCase(SELECTTAB) ||
				action.equalsIgnoreCase(MAKESELECTION)) {
				try {
					// not verified
					GuiSubitemTestObject guiObj = new GuiSubitemTestObject(obj1.getObjectReference());
					guiObj.click(Script.atName(param1));
				} catch (Exception ex) { 
					Log.debug(getClass().getName()+".commandWithOneParam() " + ex.getMessage());
					throw new SAFSException(ex.toString());
				}
				// set status to ok
				testRecordData.setStatusCode(StatusCodes.OK);
				String altText = windowName+":"+compName+" "+action +" successful using "+ param1;
				log.logMessage(testRecordData.getFac(),
								passedText.convert("success3a", altText, windowName, compName, action, param1),
								PASSED_MESSAGE);
			}else{
				super.commandWithOneParam();
			}

		}
	}
	  /**
	   * Overrides its super for DotNet TabControl. Forward to super for all other cases.  
	   */
	  protected List captureObjectData(TestObject guiObj)throws IllegalArgumentException, SAFSException{
		  String debugMsg =  getClass().getName() + ".captureObjectData(): ";  
		  Log.info(debugMsg + "attempting to get title for its every table page...");

		  if (isWinDomain(guiObj)) {
			  List result = new ArrayList();
			  TestObject[] children = guiObj.getChildren();
			  String tabItemName = "";
			  Log.debug(debugMsg+" Tab has "+children.length+" items.");
			  for(int i=0;i<children.length;i++){
				  try{
					  tabItemName = children[i].getProperty(PROPERTY_NAME).toString();
				  }catch(PropertyNotFoundException e){
					  try{
						  tabItemName = children[i].getProperty(PROPERTY_TEXT).toString();
					  }catch(PropertyNotFoundException pne){}
				  }
				  result.add(tabItemName);
			  }

		      return result;   
		  }
		  else
			  return super.captureObjectData(guiObj);
	  }	
}
