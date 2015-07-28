/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver;

import org.openqa.selenium.WebElement;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.model.commands.TabControlFunctions;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.TabControl;
import org.safs.tools.stringutils.StringUtilities;

/**
 * 
 * History:<br>
 * 
 *  <br>   APR 21, 2014    (Lei Wang) Initial release.
 */
public class CFTabControl extends CFComponent {

	TabControl tabcontrol;

	public CFTabControl() {
		super();
	}
	
	protected TabControl newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new TabControl(webelement);
	}
	
	protected void localProcess(){
		String debugmsg = StringUtils.debugmsg(getClass(), "localProcess");

		if (action != null) {
			String msg = null;
			String detail = null;

			try{
				super.localProcess();
				tabcontrol = (TabControl) libComponent;

				Log.debug(debugmsg+" processing command '"+action+"' with parameters "+params);
				iterator = params.iterator();

				if(action.equalsIgnoreCase(TabControlFunctions.CLICKTAB_KEYWORD)
				   ||action.equalsIgnoreCase(TabControlFunctions.CLICKTABCONTAINS_KEYWORD)
				   ||action.equalsIgnoreCase(TabControlFunctions.MAKESELECTION_KEYWORD)
				   ||action.equalsIgnoreCase(TabControlFunctions.SELECTTAB_KEYWORD)
				   ||action.equalsIgnoreCase(TabControlFunctions.SELECTTABINDEX_KEYWORD)
				   ||action.equalsIgnoreCase(TabControlFunctions.UNVERIFIEDCLICKTAB_KEYWORD)){
					
					if(params.size() < 1){
						issueParameterCountFailure();
						return;
					}
					
					if(processWithOneParameter()){
						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
						msg = genericText.convert("success3", windowName +":"+ compName + " "+ action +" successful.",
								windowName, compName, action);
						log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);	
					}
					
				}else{
					testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
					Log.warn(debugmsg+action+" could not be handled here.");
				}
				
			}catch(Exception e){
				Log.error(debugmsg+"Selenium TabControl Error processing '"+action+"'.", e);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				msg = getStandardErrorMessage(windowName +":"+ compName +" "+ action);
				detail = "Met Exception "+e.getMessage();
				log.logMessage(testRecordData.getFac(),msg, detail, FAILED_MESSAGE);
			}
		}
	}
	
	/**
	 * @return boolean true if the keyword has been handled successfully;<br>
	 *                 false if the keyword should not be handled in this method.<br>
	 * @throws SeleniumPlusException
	 */
	private boolean processWithOneParameter() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "processWithOneParameter");
		String parameter = (String) iterator.next();
		int matchIndex = 1;
		if(iterator.hasNext()){
			matchIndex = StringUtilities.parseIndex((String)iterator.next());
		}
		matchIndex--;//convert 1-based index to 0-based index
		
		if(action.equalsIgnoreCase(TabControlFunctions.CLICKTAB_KEYWORD)
		  ||action.equalsIgnoreCase(TabControlFunctions.MAKESELECTION_KEYWORD)
		  ||action.equalsIgnoreCase(TabControlFunctions.SELECTTAB_KEYWORD)){
			tabcontrol.selectTab(parameter, false, matchIndex, true);
			
		}else if(action.equalsIgnoreCase(TabControlFunctions.CLICKTABCONTAINS_KEYWORD)){
			tabcontrol.selectTab(parameter, true, matchIndex, true);
			
		}else if(action.equalsIgnoreCase(TabControlFunctions.SELECTTABINDEX_KEYWORD)){
			int index = StringUtilities.getIndex(parameter);
			tabcontrol.selectTab(index-1, true);
			
		}else if(action.equalsIgnoreCase(TabControlFunctions.UNVERIFIEDCLICKTAB_KEYWORD)){
			tabcontrol.selectTab(parameter, false, matchIndex, false);
			
		}else{
			testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			Log.warn(debugmsg+action+" could not be handled here.");
			return false;
		}
		
		return true;
	}

}
