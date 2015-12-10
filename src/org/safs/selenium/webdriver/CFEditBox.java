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
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.model.commands.EditBoxFunctions;
import org.safs.selenium.webdriver.lib.EditBox;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.text.FAILKEYS;
import org.safs.text.GENKEYS;

/**
 * Handle the keywords related to 'EditBox', such as SetTextValue, SetTextCharacters etc.
 *
 */
public class CFEditBox extends CFComponent {
	
	/** "EditBox" */
	public static final String LIBRARY_NAME = CFEditBox.class.getSimpleName().substring("CF".length());
    
    private EditBox editbox;
    /** 5, the default maximum time to retry enter text if verification fails.*/
    public static int DEFAULT_MAX_RETRY_ENTER = 5;
    /** The max times to retry enter the text into EditBox if the verification fails.*/
	private int maxRetry = DEFAULT_MAX_RETRY_ENTER;
	
	public CFEditBox() {
		super();		
	}

	/**
	 * @param maxRetry int, The max times to retry enter the text into EditBox if the verification fails.
	 */
	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
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
					doSetText(true, true);					
				} else if(EditBoxFunctions.SETUNVERIFIEDTEXTCHARACTERS_KEYWORD.equalsIgnoreCase(action)) {
					doSetText(true, false);
				} else if(EditBoxFunctions.SETTEXTVALUE_KEYWORD.equalsIgnoreCase(action)){
					doSetText(false, true);
				} else if(EditBoxFunctions.SETUNVERIFIEDTEXTVALUE_KEYWORD.equalsIgnoreCase(action)) {
					doSetText(false, false);
				}				
			}catch(Exception e){
				IndependantLog.error(debugmsg+"Error performing "+ action, e);
				issueErrorPerformingActionOnX(compName, e.getMessage());
			}			
		}else{
			
		}
	}

	/**
	 * Set the text of edit box.<br> 
	 * 
	 * @param isCharacter boolean, if true then the text will be treated as plain text, without special key dealing;<br>
	 *                             if false then the text will be treated as special keys.
	 * @param needVerify boolean, if true, then verify that text has been correctly entered. 
	 *                                     But if the isCharacter is false and text contains special keys, no verification.<br>
	 *                            if false, no verification.
	 */
	protected void doSetText(boolean isCharacter, boolean needVerify) {
		String dbg = StringUtils.debugmsg(false);
		String msg = "";
		
		if(params.size() < 1) {
			issueParameterCountFailure();
			return;
		}
		
		iterator = params.iterator();
		String text = iterator.next();
		IndependantLog.debug(dbg + "isCharacter="+isCharacter+", needVerify=" + needVerify + " proceeding with TEXT parameter '" + text + "'");
		if(needVerify){
			//For SetTextValue, If there're special keys, no verification.
			if(!isCharacter && StringUtils.containsSepcialKeys(text)){
				IndependantLog.debug(dbg+"Input text contains special keys, ignoring verification.");
				needVerify = false;
			}
		}
		
		try {
			setText(isCharacter, text);

			if (needVerify) {
				IndependantLog.info("Verifying the EditBox ...");
				boolean verified = editbox.verifyEditBox(text);
				int count = 0;
				
				//If verification fails, then try to reenter text
				while(!verified && (count++<maxRetry)){
					IndependantLog.debug(dbg+" retry to enter '"+text+"'");
					setText(isCharacter, text);
					//we MAY need to slow down, so that we can get all text from edit-box after setting.
					verified = editbox.verifyEditBox(text);
				}
				
				if (verified) {
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					msg = genericText.convert(GENKEYS.SUCCESS_2,
							action + " '"+ "verifying" + "' successful",
							action,
							"verifying");
					log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
				} else {
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					msg = failedText.convert(FAILKEYS.ERROR_PERFORMING_2,
										"Error performing '" + "verification" + "' on " + action,
										"verification",
										action);
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
			IndependantLog.error(dbg + " failed due to: " + spe.getMessage());
			issueActionOnXFailure(compName, spe.getMessage());
		}
	}
	
	/**
	 * Clear the EditBox and then set text to it.<br>
	 * @param isCharacter boolean, if true then the text will be treated as plain text, without special key dealing;<br>
	 *                             if false then the text will be treated as special keys.
	 * @param text String, the text to enter into EditBox.
	 * @throws SeleniumPlusException
	 */
	protected void setText(boolean isCharacter, String text) throws SeleniumPlusException{
		editbox.clearEditBox();
		if(isCharacter){
			editbox.inputEditBoxChars(text);
		}else{
			editbox.inputEditBoxKeys(text);
		}
	}

}
