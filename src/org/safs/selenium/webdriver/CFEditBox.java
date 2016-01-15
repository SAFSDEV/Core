/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * 
 * History:<br>
 * 
 *  <br>   JAN 07, 2014    (DHARMESH4) Initial release.
 *  <br>   JUL 02, 2015    (Tao Xie) Add doSetTextCharacters(): set edit box text without special key dealing.
 *  <br>                            Change doSetTextValue(): set edit box text with special key dealing.
 *  <br>   DEC 09, 2015    (Lei Wang) Refactor to reduce redundancy.
 *                                  Add a loop to reenter the text if the verification fails. Double check the verification.
 */
package org.safs.selenium.webdriver;

import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.model.commands.EditBoxFunctions;
import org.safs.selenium.webdriver.lib.EditBox;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;

/**
 * Handle the keywords related to 'EditBox', such as SetTextValue, SetTextCharacters etc.
 *
 */
public class CFEditBox extends CFComponent {
	
	/** "EditBox" */
	public static final String LIBRARY_NAME = CFEditBox.class.getSimpleName().substring("CF".length());
    
    private EditBox editbox;
    	
	public CFEditBox() {
		super();		
	}

	protected EditBox newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new EditBox(webelement);
	}
	
	protected void localProcess(){
		String debugmsg = StringUtils.debugmsg(getClass(), "localProcess");
		if (action != null) {
			IndependantLog.debug(debugmsg+" processing command '"+action+"'...");
			
			try{
				super.localProcess();
				editbox = (EditBox) libComponent;
				
				if(EditBoxFunctions.SETTEXTCHARACTERS_KEYWORD.equalsIgnoreCase(action)) {
					doSetText(LIBRARY_NAME, true, true);					
				} else if(EditBoxFunctions.SETUNVERIFIEDTEXTCHARACTERS_KEYWORD.equalsIgnoreCase(action)) {
					doSetText(LIBRARY_NAME, true, false);
				} else if(EditBoxFunctions.SETTEXTVALUE_KEYWORD.equalsIgnoreCase(action)){
					doSetText(LIBRARY_NAME, false, true);
				} else if(EditBoxFunctions.SETUNVERIFIEDTEXTVALUE_KEYWORD.equalsIgnoreCase(action)) {
					doSetText(LIBRARY_NAME, false, false);
				}				
			}catch(Exception e){
				IndependantLog.error(debugmsg+"Error performing "+ action, e);
				issueErrorPerformingActionOnX(compName, e.getMessage());
			}			
		}else{
			
		}
	}
}
