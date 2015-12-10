/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * History:
 * 
 *  FEB 05, 2015    (SBJLWA) Refresh the element when meet StaleElementReferenceException.
 *  JUL 02, 2015    (SCNTAX) Add inputEditBoxChars(): set the text as the content of EditBox without special key dealing.
 *                           Add verifyEditBox(): verify the contents of EditBox to the original keys.
 *                           Change inputEditBox() into inputEditBoxKeys(): set the text as the content of EditBox with special key dealing.
 *  SEP 18, 2015    (SBJLWA) Move the functionality of waitReactOnBrowser() to Robot.
 *                           Modify inputEditBoxChars/Keys(): turn on the 'waitReaction' for inputkeys and inputchars.
 *  OCT 13, 2015    (SBJLWA) Modify clearEditBox(): make it robust, call Robot to clear finally.
 *  OCT 16, 2015    (sbjlwa) Refactor to create IOperable object properly.
 *  DEC 09, 2015    (sbjlwa) Modify verifyEditBox(): do not throw exception when verification fails.
 */
package org.safs.selenium.webdriver.lib;

import java.awt.datatransfer.DataFlavor;

import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.robot.Robot;

/** 
 * A library class to handle different specific EditBox.
 */
public class EditBox extends Component {

	public EditBox(WebElement editbox) throws SeleniumPlusException{
		super(editbox);
	}

	/**
	 * <em>Purpose:</em> Remove the content from EditBox.<br>
	 * @param _comp
	 */
	public void clearEditBox() throws SeleniumPlusException{
		String debugmsg = getClass().getName()+".clearEditBox(): ";
		try {
			try{
				// chrome and ie are failing element.clear
				webelement.clear();
			}catch (StaleElementReferenceException sere){
				IndependantLog.warn(debugmsg+"Met "+StringUtils.debugmsg(sere));			
				//fresh the element and clear again.
				refresh(false);
				webelement.clear();
			}
			//Selenium API clear() will sometimes redraw the Web Element on the page,
			//which will cause StaleElementReferenceException, we need to refresh it if stale
			refresh(true);
		} catch (NoSuchElementException msee) {
			IndependantLog.debug(debugmsg+"NoSuchElementException --Object not found.");
			throw new SeleniumPlusException("EditBox object not found");

		} catch (Exception e){
			IndependantLog.debug(debugmsg+"Met "+StringUtils.debugmsg(e));
			try{
				refresh(true);
				webelement.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
			}catch(Exception x){
				IndependantLog.debug(debugmsg+"Met "+StringUtils.debugmsg(x));
				try{
					refresh(true);
					Actions delete = new Actions(WDLibrary.getWebDriver());
					delete.sendKeys(webelement, Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
					delete.perform();
				}catch(Exception ex){
					IndependantLog.warn(debugmsg+"EditBox clear action failed, Met "+StringUtils.debugmsg(ex));
				}
			}
		}finally{
			IndependantLog.debug(debugmsg+" Finally use SAFS Robot to clear again.");
			WDLibrary.inputKeys(webelement, "^a{Delete}");
		}
	}
	
	/**
	 * <em>Purpose:</em> Set the text as the content of EditBox. <br>
	 * This method will not deal with special keys as  + --> ShiftKey  ^ --> CtrlKey. <br>
	 * All text will be treated as literal characters. <br>
	 * For example: the special key "^(v)" will just be treated as literal "^(v)" without any interpretations. <br>
	 * @param text String, the text to be set to EditBox
	 */
	public void inputEditBoxChars(String text) throws SeleniumPlusException {
		String debugmsg = getClass().getName() + ".inputEditBoxChars(): ";
				
		try {
			WDLibrary.setWaitReaction(true);
			try {
				WDLibrary.inputChars(webelement, text);
			} catch (SeleniumPlusException sere) {
				String msg = "EditBox enter action failed" + "(input value = " + text + "): caused by " + StringUtils.debugmsg(sere);
				IndependantLog.debug(debugmsg + msg);
				
				//fresh the element and input keys again
				refresh(false);
				WDLibrary.inputChars(webelement, text);
			}
		} catch (Exception e) {
			String msg = "EditBox enter action failed" + "(input value = " + text + "): caused by " + StringUtils.debugmsg(e);
			IndependantLog.debug(debugmsg + msg);
			throw new SeleniumPlusException(msg);
		}finally{
			WDLibrary.setWaitReaction(Robot.DEFAULT_WAIT_REACTION);
		}
	}
	
	/**
	 * <em>Purpose:</em> Set the text as the content of EditBox. <br>
	 * This method will deal with special keys. <br>
	 * For example: if the text is "^(v)", the content will be interpreted as "Ctrl + v", <br>
	 * which means PASTE the contents of clipboard. <br>
	 * @param text String, the text to be set to EditBox <br>
	 */
	public void inputEditBoxKeys(String text) throws SeleniumPlusException {
		String debugmsg = getClass().getName() + ".inputEditBoxKeys(): ";
				
		try {
			WDLibrary.setWaitReaction(true);
			try {
				WDLibrary.inputKeys(webelement, text);
			} catch (SeleniumPlusException sere) {
				String msg = "EditBox enter action failed" + "(input value = " + text + "): caused by " + StringUtils.debugmsg(sere);
				IndependantLog.debug(debugmsg + msg);
				//fresh the element and input keys again
				refresh(false);
				WDLibrary.inputKeys(webelement, text);
			}
		} catch (Exception e) {
			String msg = "EditBox enter action failed" + "(input value = " + text + "): caused by " + StringUtils.debugmsg(e);
			IndependantLog.debug(debugmsg + msg);
			throw new SeleniumPlusException(msg);
		}finally{
			WDLibrary.setWaitReaction(Robot.DEFAULT_WAIT_REACTION);
		}
	}
	
	/**
	 * @return String, the content of EditBox
	 */
	protected String getValue(){
		String debugmsg = StringUtils.debugmsg(false);
		String value = null;
		try {
			value = WDLibrary.getProperty(webelement, ATTRIBUTE_VALUE);
		} catch (SeleniumPlusException e) {
			try{
				// some EditBox's have a 'text' property and don't necessarily have a 'value' property.				
				value = WDLibrary.getProperty(webelement, ATTRIBUTE_TEXT);
			} catch (SeleniumPlusException e2) {
				IndependantLog.warn(debugmsg + "failure caused by "+ StringUtils.debugmsg(e) +"; and "+ StringUtils.debugmsg(e2));
			}
		}
		return value;
	}
	
	/**
	 * <em>Purpose:</em> Compare the contents of EditBox to the original keys,<br>
	 * If they are same, return true; Otherwise, return false. <br>
	 * @param expectedText String, the string to be compared to during verification.
	 * @return		If verification is passed, return true; otherwise, return false.
	 */
	public boolean verifyEditBox(String expectedText){
		String debugmsg = getClass().getName() + ".verifyEditBox(): ";
		boolean pass = false;		
		String contents = getValue();
		
		pass = expectedText.equals(contents);
		
		if(!pass){
			String msg = "EditBox verify errors: property:\n'" + contents + "'" + " does NOT equal to " + " expected value:\n'" + expectedText + "'.";
			IndependantLog.debug(debugmsg + msg);
			pass = doubleCheckVerification(expectedText);
		}
		
		return pass;
	}
	
	/**
	 * Copy the edit-box's value to clipboard and compare the clipboard's value with the text we try to input.<br>
	 * @param expectedText String, the text to verify with.
	 * @return boolean, true if the edit-box's value equals the text to input.
	 */
	protected boolean doubleCheckVerification(String expectedText){
		String debugmsg = StringUtils.debugmsg(false);
		
		try {
			IndependantLog.debug(debugmsg+" copy editbox's value to clipboard, and compare clipboard's content with the text we want to input.");
			//Copy the editbox's value so that it will be saved to the clipboard
			WDLibrary.clearClipboard();
			try{ Thread.sleep(100);} catch(Exception ignore){}
			inputKeys("^a^c{END}");//Ctrl+A, Ctrl+C, {End}
			//We MUST wait a while before the clip-board is set correctly.
			try{ Thread.sleep(1000);} catch(Exception ignore){}
			//Get the content from the clip-board
			String result = (String) WDLibrary.getClipboard(DataFlavor.stringFlavor);
			IndependantLog.debug(debugmsg+" From RMI server, got clipboard's content \n'"+result+"' =? (expected value) \n'"+expectedText+"'");

			return expectedText.equals(result);

		} catch (Exception e) {
			IndependantLog.debug(debugmsg+"Fail. due to "+StringUtils.debugmsg(e));
		}
		
		return false;
	}
}
