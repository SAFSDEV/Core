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
 *  OCT 16, 2015    (Lei Wang) Refactor to create IOperable object properly.
 *  DEC 09, 2015    (Lei Wang) Modify verifyEditBox(): do not throw exception when verification fails.
 *  JAN 13, 2016	(Tao Xie) Move the implementation of 'clearEditBox, inputEditBoxChars, inputEditBoxKeys, verifyEditBox, doubleCheckVerification' 
 *  						 methods to 'Component.java'.
 *  
 */
package org.safs.selenium.webdriver.lib;

import org.openqa.selenium.WebElement;
import org.safs.selenium.webdriver.CFEditBox;

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
		clearComponentBox(CFEditBox.LIBRARY_NAME);
	}
	
	/**
	 * <em>Purpose:</em> Set the text as the content of EditBox. <br>
	 * This method will not deal with special keys as  + --> ShiftKey  ^ --> CtrlKey. <br>
	 * All text will be treated as literal characters. <br>
	 * For example: the special key "^(v)" will just be treated as literal "^(v)" without any interpretations. <br>
	 * @param text String, the text to be set to EditBox
	 */
	public void inputEditBoxChars(String text) throws SeleniumPlusException {
		inputComponentBoxChars(CFEditBox.LIBRARY_NAME, text);
	}
	
	/**
	 * <em>Purpose:</em> Set the text as the content of EditBox. <br>
	 * This method will deal with special keys. <br>
	 * For example: if the text is "^(v)", the content will be interpreted as "Ctrl + v", <br>
	 * which means PASTE the contents of clipboard. <br>
	 * @param text String, the text to be set to EditBox <br>
	 */
	public void inputEditBoxKeys(String text) throws SeleniumPlusException {
		inputComponentBoxKeys(CFEditBox.LIBRARY_NAME, text);
	}
	
	/**
	 * <em>Purpose:</em> Compare the contents of EditBox to the original keys,<br>
	 * If they are same, return true; Otherwise, return false. <br>
	 * @param expectedText String, the string to be compared to during verification.
	 * @return		If verification is passed, return true; otherwise, return false.
	 */
	public boolean verifyEditBox(String expectedText){
		return verifyComponentBox(CFEditBox.LIBRARY_NAME, expectedText);
	}
	
	/**
	 * Copy the edit-box's value to clipboard and compare the clipboard's value with the text we try to input.<br>
	 * @param expectedText String, the text to verify with.
	 * @return boolean, true if the edit-box's value equals the text to input.
	 */
	protected boolean doubleCheckVerification(String expectedText){		
		return doubleCheckVerification(CFEditBox.LIBRARY_NAME, expectedText);
	}
}
