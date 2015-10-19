/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * History:
 * 
 *  FEB 05, 2015    (Lei Wang) Refresh the element when meet StaleElementReferenceException.
 *  JUL 02, 2015    (Tao Xie) Add inputEditBoxChars(): set the text as the content of EditBox without special key dealing.
 *                           Add verifyEditBox(): verify the contents of EditBox to the original keys.
 *                           Change inputEditBox() into inputEditBoxKeys(): set the text as the content of EditBox with special key dealing.
 *  SEP 18, 2015    (Lei Wang) Move the functionality of waitReactOnBrowser() to Robot.
 *                           Modify inputEditBoxChars/Keys(): turn on the 'waitReaction' for inputkeys and inputchars.
 *  OCT 13, 2015    (Lei Wang) Modify clearEditBox(): make it robust, call Robot to clear finally.
 *  OCT 16, 2015    (Lei Wang) Refector to create IOperable object properly.
 */
package org.safs.selenium.webdriver.lib;

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
			String msg = "Failed, caused by " + StringUtils.debugmsg(e);
			IndependantLog.warn(debugmsg + msg);
		}

		return value;
	}
	
	/**
	 * <em>Purpose:</em> Compare the contents of EditBox to the original keys,<br>
	 * If they are same, return true; Otherwise, return false. <br>
	 * @param keys String, the string to be compared to during verification.
	 * @return		If verification is passed, return true; otherwise, return false.
	 */
	public boolean verifyEditBox(String keys) throws SeleniumPlusException {
		String debugmsg = getClass().getName() + ".verifyEditBox(): ";		
		boolean passVerification = false;		
		String contents = getValue();
		
		if(keys.equals(contents)) {
			passVerification = true;
		} else {
			passVerification = false;
			String msg = "EditBox verify errors: property:'" + contents + "'" + " does NOT equal to " + " value:'" + keys + "'";
			IndependantLog.debug(debugmsg + msg);
			throw new SeleniumPlusException(msg);
		}
		
		return passVerification;
	}
}
