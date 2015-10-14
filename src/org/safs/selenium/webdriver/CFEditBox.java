/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver;

import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.model.commands.EditBoxFunctions;
import org.safs.selenium.webdriver.lib.EditBox;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.text.FAILKEYS;
import org.safs.text.GENKEYS;

/**
 * 
 * History:<br>
 * 
 *  <br>   JAN 07, 2014    (DHARMESH4) Initial release.
 *  <br>   Jul 02, 2015    (Tao Xie) Add doSetTextCharacters(): set edit box text without special key dealing.
 *  <br>                            Change doSetTextValue(): set edit box text with special key dealing.
 */
public class CFEditBox extends CFComponent {
	
	/** "EditBox" */
	public static final String LIBRARY_NAME = CFEditBox.class.getSimpleName().substring("CF".length());
	/** "SetTextCharacters" */
    static public final String SETTEXTCHARACTERS_KEYWORD = EditBoxFunctions.SETTEXTCHARACTERS_KEYWORD;
    /** "SetTextValue" */
    static public final String SETTEXTVALUE_KEYWORD = EditBoxFunctions.SETTEXTVALUE_KEYWORD;
    /** "SetUnverifiedTextCharacters" */
    static public final String SETUNVERIFIEDTEXTCHARACTERS_KEYWORD = EditBoxFunctions.SETUNVERIFIEDTEXTCHARACTERS_KEYWORD;
    /** "SetUnverifiedTextValue" */
    static public final String SETUNVERIFIEDTEXTVALUE_KEYWORD = EditBoxFunctions.SETUNVERIFIEDTEXTVALUE_KEYWORD;
    
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
			Log.debug(debugmsg+" processing command '"+action+"'...");
			
			try{
				super.localProcess();
				editbox = (EditBox) libComponent;
				
				if(action.equalsIgnoreCase(SETTEXTCHARACTERS_KEYWORD)) {
					doSetTextCharacters(true);					
				} else if(action.equalsIgnoreCase(SETUNVERIFIEDTEXTCHARACTERS_KEYWORD)) {
					doSetTextCharacters(false);
				} else if(action.equalsIgnoreCase(SETTEXTVALUE_KEYWORD)){
					doSetTextValue(true);
				} else if(action.equalsIgnoreCase(SETUNVERIFIEDTEXTVALUE_KEYWORD)) {
					doSetTextValue(false);
				}				
			}catch(Exception e){
				Log.error(debugmsg+"Error performing "+ action, e);
				issueErrorPerformingActionOnX(compName, e.getMessage());
			}			
		}
	}

	/**
	 * Set the text of edit box. The text only be treated as plain text, without special keywords dealing.
	 * @param needVerify boolean, if it is true, the setting process will be verified.
	 * <br> If it is false, the setting process will not be verified. 
	 */
	protected void doSetTextCharacters(boolean needVerify) {
		String dbg = getClass().getName() + ".doSetTextCharacters";
		String msg = "";
		
		if(params.size() < 1) {
			issueParameterCountFailure();
			return;
		}
		
		iterator = params.iterator();
		String option = iterator.next();
		Log.debug(dbg + " needVerify = " + needVerify + " proceeding with TEXT parameter '" + option + "'");
		
		try {
			editbox.clearEditBox();
			editbox.inputEditBoxChars(option);
			
			if (needVerify) {
				Log.info("Verifying the EditBox ...");
				boolean verified = editbox.verifyEditBox(option);

				if (verified) {
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					msg = genericText.convert(GENKEYS.SUCCESS_2,
							SETTEXTCHARACTERS_KEYWORD + " '"+ "verifying" + "' successful",
							SETTEXTCHARACTERS_KEYWORD,
							"verifying");
					log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
				} else {
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					msg = failedText.convert(FAILKEYS.ERROR_PERFORMING_2,
										"Error performing '" + "verification" + "' on " + SETTEXTCHARACTERS_KEYWORD,
										"verification",
										SETTEXTCHARACTERS_KEYWORD);
					standardFailureMessage(msg, testRecordData.getInputRecord());
				}
			}else{
				//If we don't need to verify, we just set status to OK.
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				msg = genericText.convert(GENKEYS.SUCCESS_3, 
						                  windowName +":"+ compName + " "+ action +" successful.",
						                  windowName, compName, action);
				log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);	
			}
		} catch(SeleniumPlusException spe) {
			Log.error(dbg + " failed due to: " + spe.getMessage());
			issueActionOnXFailure(compName, spe.getMessage());
		}
	}
	
	/**
	 * Set the text of edit box. The text only be treated with special keywords dealing.
	 * @param needVerify boolean, if it is true, the setting process will be verified.
	 * <br> If it is false, the setting process will not be verified. 
	 */
	protected void doSetTextValue(boolean needVerify){
		String dbg = getClass().getName() + ".doSetTextValue";
		String msg = "";
		
		if (params.size() < 1) {
			issueParameterCountFailure();
			return;
		}
		
		iterator = params.iterator();
		String option = iterator.next();
		Log.debug(dbg + " needVerify = " + needVerify + " proceeding with TEXT parameter '" + option + "'");
		
		try {
			editbox.clearEditBox();
			editbox.inputEditBoxKeys(option);
			
			if(needVerify){
				// Check if there's special keyword, if there're special keywords, no verification.
				if(StringUtils.containsSepcialKeys(option)){
					IndependantLog.debug(dbg+"Input text contains special key words, ignoring verification.");
					needVerify = false;
				}
			}
			
			// Verify if needed
			if (needVerify) {
				Log.info("Verifying the EditBox ...");
				boolean verified = editbox.verifyEditBox(option);

				if (verified) {
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					msg = genericText.convert(GENKEYS.SUCCESS_2,
							SETTEXTVALUE_KEYWORD + " '" + "verifying" + "' successful",
							SETTEXTVALUE_KEYWORD,
							"verifying");
					log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
				} else {
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					msg = failedText.convert(FAILKEYS.ERROR_PERFORMING_2,
							"Error performing '" + "verification" + "' on " + SETTEXTVALUE_KEYWORD,
							"verification",
							SETTEXTVALUE_KEYWORD);
					standardFailureMessage(msg, testRecordData.getInputRecord());
				}
			}else{
				//If we don't need to verify, we just set status to OK.
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				msg = genericText.convert(GENKEYS.SUCCESS_3, 
						                  windowName +":"+ compName + " "+ action +" successful.",
						                  windowName, compName, action);
				log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);	
			}
		} catch(SeleniumPlusException spe) {
			Log.error(dbg + " failed due to: " + spe.getMessage());
			issueActionOnXFailure(compName, spe.getMessage());
		}
	}
}
