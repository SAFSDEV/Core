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

import java.awt.datatransfer.DataFlavor;

import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.model.commands.EditBoxFunctions;
import org.safs.robot.Robot;
import org.safs.selenium.webdriver.lib.EditBox;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.text.FAILKEYS;
import org.safs.text.GENKEYS;
import org.safs.tools.stringutils.StringUtilities;

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
					//Double check the text in EditBox
					if(!verified) verified = doubleCheckVerification(text); 
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

	/**
	 * Copy the edit-box's value to clipboard and compare the clipboard's value with the text we try to input.<br>
	 * Note: This only works on local machine.<br>
	 * @param text String, the text to verify with.
	 * @return boolean, true if the edit-box's value equals the text to input.
	 */
	protected boolean doubleCheckVerification(String text){
		String debugmsg = StringUtils.debugmsg(false);
		
		try {
			IndependantLog.debug(debugmsg+" copy content to clipboard, and compare clipboard's content with the text we want to input.");
			//TODO Cut the content to set to the clip-board, we need to get this work on remote machine thru RMI
			Robot.clearClipboard();
			StringUtilities.sleep(100);
			editbox.inputKeys("^a");
			editbox.inputKeys("^c");
			//We MUST wait a while before the clip-board is set correctly.
			Thread.sleep(1000);
			//TODO Get the content from the clip-board, we need to get this work on remote machine thru RMI
			String result = (String) Robot.getClipboard(DataFlavor.stringFlavor);
			
			if(text.equals(result)){
				IndependantLog.debug(debugmsg+"======================================================== THEY ARE EQUAL!!!");
				return true;
			}
		} catch (Exception e) {
			IndependantLog.debug(debugmsg+"Fail. due to "+StringUtils.debugmsg(e));
		}
		
		return false;
	}
}
