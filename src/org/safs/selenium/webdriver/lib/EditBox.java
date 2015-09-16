/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib;
/**
 * 
 * History:<br>
 * 
 *  <br>   FEB 05, 2015    (Lei Wang) Refresh the element when meet StaleElementReferenceException.
 *  <br>   Jul 02, 2015    (Tao Xie) Add inputEditBoxChars(): set the text as the content of EditBox without special key dealing.
 *  <br>                            Add verifyEditBox(): verify the contents of EditBox to the original keys.
 *  <br>                            Change inputEditBox() into inputEditBoxKeys(): set the text as the content of EditBox with special key dealing.
 *  <br>   SEP 16, 2015    (Lei Wang) Add waitReactOnBrowser(): wait reaction of browser for input keys.
 */
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.tools.stringutils.StringUtilities;

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
		//element.sendKeys(Keys.HOME,Keys.chord(Keys.SHIFT,Keys.END),"55");		
		try {
			try{
				// chrome and ie are failing element.clear
				webelement.clear();
			}catch (StaleElementReferenceException sere){
				IndependantLog.warn(debugmsg+"StaleElementReferenceException --Object reference is stale.");			
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
			// chrome is failing element.clear
			try{
				String t = webelement.getText();
				if(t==null || t.length()==0) {
					IndependantLog.debug(debugmsg+"ignoring "+e.getClass().getName()+": element may already be cleared.");
					return;				
				}else{
					webelement.sendKeys(Keys.HOME,Keys.chord(Keys.SHIFT,Keys.END),"55");		
				}
			}catch(Exception x){
				IndependantLog.debug(debugmsg+x.getClass().getName()+", "+x.getMessage(),x);
				throw new SeleniumPlusException("EditBox clear action failed");
			}
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
		}
	}
	
	/**
	 * After Robot input keys into a browser, the browser needs time to react. 
	 * Before that reaction, the EditBox may contain incomplete text. And the
	 * verification will fail.
	 */
	protected void waitReactOnBrowser(String text){
		String debugmsg = StringUtils.debugmsg(false);
		try {
			//We may still need to adjust the time to wait for different situation (string, browser etc.)
			int waitReactOnBrowser = (text.length()/100+1)*100 + 200;
			Thread.sleep(waitReactOnBrowser);
			IndependantLog.debug(debugmsg+" pause '"+waitReactOnBrowser+"' milli seconds to wait browser react to input keys.");
		} catch (Exception sere) {
			IndependantLog.warn(debugmsg+" fail to wait browser react to input keys.");
		}
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
		String contents = "";
		
		try {
			waitReactOnBrowser(keys);
			contents = WDLibrary.getProperty(webelement, ATTRIBUTE_VALUE);
		} catch (SeleniumPlusException sere) {
			String msg = "EditBox get property action failed" + "(input value = " + keys + "): caused by " + StringUtils.debugmsg(sere);
			IndependantLog.debug(debugmsg + msg);
			throw new SeleniumPlusException(msg);
		}
		
		if(contents.equals(keys)) {
			passVerification = true;
		} else {
			passVerification = false;
			String msg = "EditBox verify errors: property:'" + contents + "'" + " NOT equals " + " value:'" + keys + "'";
			IndependantLog.debug(debugmsg + msg);
			throw new SeleniumPlusException(msg);
		}
		
		return passVerification;
	}
}
