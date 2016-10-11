/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

/**
*
* History:<br>
*
* <br>   SEP 22, 2016  (SCNTAX) Initial release: create SAFS version AutoItXPlus to bridge Java and AutoIt APIs.
* 								Add convertCOMParams(): convert String parameters array into com.jacob.com.Variant array.
* 								Add controlClick(): Workhorse of AutoIt click action.
* 								Move 'isValidMouseButton()' from AutoItComponent to here.
* <br>   OCT 10, 2016  (SCNTAX) Refactor 'controlClick()' into 'click()', which will use Java AWT Robot click call first,
*                               and then use AutoIt API to click if previous click action failed.
*                               Add 'robotClick()': use Java AWT Robot to click.
*                               Add assistant methods: 'autoit2JavaButtonmask()' and 'autoit2JavaSpecialKey()'.
* <br>   OCT 11, 2016  (SCNTAX) Add assistant methods: 'getBorderWidth()' and 'getTitleBarHeight()'.
* 
*/
package org.safs.autoit.lib;

import java.awt.Point;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.robot.Robot;
import org.safs.selenium.webdriver.lib.WDLibrary;

import com.jacob.com.Variant;

import autoitx4java.AutoItX;

/**
 * <a href="https://www.autoitscript.com/site/autoit/downloads/"> AutoIt/AutoItX </a> is the DLL/COM control, which can be used by different programming 
 * languages to access AutoIt through COM objects. Currently, 3rd part Java JAR <a href="https://github.com/accessrichard/autoitx4java"> autoitx4java </a> 
 * has provided one java version AutoItX object by using JACOB to access this AutoIt/AutoItX COM object. It gives many common Java APIs to use AutoIt functionalities, 
 * but still lacks of APIs like "Ctrl + Click", "Alt + Click" to support SAFS keywords. Thus it requires creating SAFS version 'AutoItXPlus', which extends from 
 * 'autoitx4java/AutoItX', to access AutoIt functionalities through AutoIt/AutoItX COM object.
 * 
 * @author scntax
 *
 */
public class AutoItXPlus extends AutoItX {
	
	/**
	 * According to https://www.autoitscript.com/autoit3/docs/functions/Send.htm, AutoIt's 'Send()'
	 * method contains the optional parameter 'flag' to indicate if text contains special characters like + and !
	 * to indicate SHIFT and ALT key-presses. If 'flag == 0', it will parse special key, if 'flag == 1', no parsing.
	 */
	public final static boolean DEFAULT_SUPPORT_SPECIALKEY = true; 
	private boolean supportSpecialKey = DEFAULT_SUPPORT_SPECIALKEY;
	
	/** AutoIt 'Send' keyword, sends simulated keystrokes to the active window. **/
	public final static String AUTOIT_KEYWORD_SEND = "Send";
	
	/** AutoIt 'ControlClick' keyword, sends a mouse click command to a given control. **/
	public final static String AUTOIT_KEYWORD_CONTROLCLICK = "ControlClick";
	
	/**
	 * AutoIt's supporting KeyPress/KeyDown keywords
	 */
	public final static String AUTOIT_SUPPORT_PRESS_ALT 		= "ALT";
	public final static String AUTOIT_SUPPORT_PRESS_SHIFT 		= "SHIFT";
	public final static String AUTOIT_SUPPORT_PRESS_CTRL 		= "CTRL";
	public final static String AUTOIT_SUPPORT_PRESS_LWINDOWS 	= "LWIN";
	public final static String AUTOIT_SUPPORT_PRESS_RWINDOWS 	= "RWIN";
	
	public final static String AUTOIT_ALT_DOWN 				= "{ALTDOWN}";
	public final static String AUTOIT_ALT_UP 				= "{ALTUP}";
	public final static String AUTOIT_SHIFT_DOWN 			= "{SHIFTDOWN}";
	public final static String AUTOIT_SHIFT_UP 				= "{SHIFTUP}";
	public final static String AUTOIT_CTRL_DOWN 			= "{CTRLDOWN}";
	public final static String AUTOIT_CTRL_UP 				= "{CTRLUP}";
	public final static String AUTOIT_LEFT_WINDOWS_DOWN 	= "{LWINDOWN}";
	public final static String AUTOIT_LEFT_WINDOWS_UP		= "{LWINUP}";
	public final static String AUTOIT_RIGHT_WINDOWS_DOWN 	= "{RWINDOWN}";
	public final static String AUTOIT_RIGHT_WINDOWS_UP 		= "{RWINUP}";
	
	/** AutoIt's click button constants **/
	public final static String AUTOIT_MOUSE_BUTTON_LEFT   = "left";
	public final static String AUTOIT_MOUSE_BUTTON_MIDDLE = "middle";
	public final static String AUTOIT_MOUSE_BUTTON_RIGHT  = "right";
	
	public AutoItXPlus(){
		super();
	}
	
	/**
	 * Convert parameters String array into com.jacob.com.Variant array.
	 * 
	 * @param 	paramMsgs String[], string array parameters
	 * @return 			  Variant[]
	 * 
	 * @author scntax
	 */
	public Variant[] convertCOMParams(final String[] paramMsgs) {
		String dbgmsg = StringUtils.getMethodName(0, false);
		Variant[] params = null;
		
		if(paramMsgs == null || paramMsgs.equals("")) {
			Log.debug(dbgmsg + "(): no parameters provided.");
			return null;
		}
		
		params = new Variant[paramMsgs.length];
		
		for (int indx = 0; indx < paramMsgs.length; indx++) {
			params[indx] = new Variant(paramMsgs[indx]);
		}
		
		return params;
	}
	
	/**
	 * Check if the mouse button string is acceptable for AutoIt engine.
	 * 
	 * 
	 * @param mouseButton String,	the button to click, null, empty, "left", "right", or "middle" are acceptable.
	 * @return			  boolean,	return true if mouseButton is valid, else return false.
	 * 
	 * @author scntax
	 * 
	 */
	public boolean isValidMouseButton(String mouseButton){
		if (mouseButton == null 
				|| mouseButton.equals("")
				|| mouseButton == AUTOIT_MOUSE_BUTTON_LEFT 
				|| mouseButton == AUTOIT_MOUSE_BUTTON_MIDDLE
				|| mouseButton == AUTOIT_MOUSE_BUTTON_RIGHT) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * According to https://www.autoitscript.com/autoit3/docs/functions/Send.htm, AutoIt supports
	 * 5 keys pairs: 
	 * 		1. Alt: 				{ALTDOWN}/{ALTUP},
	 * 		2. Shift: 				{SHIFTDOWN}/{SHIFTUP}, 
	 * 		3. Ctrl: 				{CTRLDOWN}/{CTRLUP}, 
	 * 		4. Left Windows key:	{LWINDOWN}/{LWINUP}, 
	 * 		5. Right Windows key: 	{RWINDOWN}/{RWINUP}
	 * Check if the key string belongs to one of these supported pressing keyword.
	 * 
	 * @param keyStr String,
	 * @return		 boolean
	 * 
	 * @author scntax
	 */
	public boolean supportHoldingKeyPress(String keyStr) {
		if(keyStr != null && (keyStr.equals(AUTOIT_SUPPORT_PRESS_ALT)
				|| keyStr.equals(AUTOIT_SUPPORT_PRESS_CTRL)
				|| keyStr.equals(AUTOIT_SUPPORT_PRESS_LWINDOWS)
				|| keyStr.equals(AUTOIT_SUPPORT_PRESS_RWINDOWS)
				|| keyStr.equals(AUTOIT_SUPPORT_PRESS_SHIFT))) {
			return true;
		}
		
		return false;
	}
	
	public String wrapKeyPress(String keyStr){
		return "{" + keyStr + "DOWN}";
	}
	
	public String wrapKeyUp(String keyStr){
		return "{" + keyStr + "UP}";
	}
	
	/** 
	 * Transform AutoIt button RS into Java button mask. 
	 *
	 * @author scntax
	 */
	public int autoit2JavaButtonmask(String autoitButton) throws SAFSException{
		if (autoitButton.equals(AUTOIT_MOUSE_BUTTON_LEFT)) {
			return WDLibrary.MOUSE_BUTTON_LEFT;
		} else if (autoitButton.equals(AUTOIT_MOUSE_BUTTON_MIDDLE)) {
			return WDLibrary.MOUSE_BUTTON_MIDDLE;
		} else if (autoitButton.equals(AUTOIT_MOUSE_BUTTON_RIGHT)) {
			return WDLibrary.MOUSE_BUTTON_RIGHT;
		}
		
		throw new SAFSException("Invalid AutoIt button RS '" + autoitButton + "' to be transformed into Java button mask");
	}
	
	/**
	 * Transform AutoIt Special Key RS into Java key code.
	 * 
	 * Currently, it supports only 3 special keys: Alt, Shift, Ctrl.
	 * The 'Left Windows' and 'Right Windows' keys are treated as invalid.
	 * 
	 * @param autoitSpecialKey	String, AutoIt supported special key.
	 * @return
	 * @throws SAFSException	throw exception when meeting unsupported special key.
	 * 
	 * @author scntax
	 */
	public int autoit2JavaSpecialKey(String autoitSpecialKey) throws SAFSException{
		if (autoitSpecialKey.equals(AUTOIT_SUPPORT_PRESS_ALT)) {
			return java.awt.event.InputEvent.ALT_DOWN_MASK;
		} else if(autoitSpecialKey.equals(AUTOIT_SUPPORT_PRESS_SHIFT)) {
			return java.awt.event.InputEvent.SHIFT_DOWN_MASK;
		} else if (autoitSpecialKey.equals(AUTOIT_SUPPORT_PRESS_CTRL)) {
			return java.awt.event.InputEvent.CTRL_DOWN_MASK;
		}
		
		throw new SAFSException("Invalid AutoIt key RS '" + autoitSpecialKey + "' to be transformed into Java key code.");
	}
	
	/**
	 * Workhorse of Java AWT Robot click action.
	 * 
	 * Click by using Robot click.
	 * 
	 * @param x				int, 	absolute screen x coordinate of component.
	 * @param y				int, 	absolute screen y coordinate of component.
	 * @param button		String, button to click, "left", "right" or "middle".
	 * @param nClicks		int, 	number of times to click the mouse. It's supposed to be greater than or equal to 1.
	 * @param offset		Point,  position to click within the control. It can be null or empty String. Default value is center.
	 * @param specialKey	String, keyboard key that be hold when click action happening. It can be null or empty String.
	 * @return
	 * @throws Exception
	 */
	public boolean robotClick(int x, int y, String button, int nClicks, Point offset, String specialKey) throws Exception {
		int buttonMask = autoit2JavaButtonmask(button);
		
		if (specialKey != null && !specialKey.equals("")) {
			return ((Boolean)Robot.clickWithKeyPress(x + offset.x, y + offset.y, buttonMask, autoit2JavaSpecialKey(specialKey), nClicks)).booleanValue();
		}
		
		return ((Boolean)Robot.click(x + offset.x, y + offset.y, buttonMask, nClicks)).booleanValue();
	}
	
	/**
	 * Get the border width of a window.
	 * 
	 * @param title 		String, title of the window to access.
	 * @param text 			String, text of the window to access.
	 * @return				int, 	border width of accessed window.  
	 * 
	 * @author scntax
	 */
	public int getBorderWidth(String title, String text){
		return (winGetPosWidth(title, text) - winGetClientSizeWidth(title, text)) / 2 ;
	}
	
	/**
	 * Get the title height of a window.
	 * 
	 * @param title 		String, title of the window to access.
	 * @param text 			String, text of the window to access.
	 * @return				int, 	title height of accessed window.
	 * 
	 * @author scntax
	 */
	public int getTitleBarHeight(String title, String text){
		/**
		 * Notes:
		 *     Traditionally, the computation of title bar height will reduce 2 times border width, i.e.
		 *         winGetPosHeight(title, text) - winGetClientSizeHeight(title, text) - 2 * getBorderWidth(title, text);
		 *     But I find in current Windows 10 system, only one side contains the border width, which may not be always consistent.
		 */
		return winGetPosHeight(title, text) - winGetClientSizeHeight(title, text) - getBorderWidth(title, text);
	}
	
	/**
	 * Workhorse of click action.
	 * 
	 * It'll first use Java AWT Robot click call first, and then use AutoIt API to click if previous click action failed. 
	 * 
	 * Provide click action through command sending with. It can be assigned mouse button like 'left, middle, right', number of click times, click offset
	 * position and the special key holding when clicking.
	 * 
	 * @param title 		String, title of the window to access.
	 * @param text 			String, text of the window to access.
	 * @param controlID		String, control to interact with.
	 * @param button		String, button to click, "left", "right" or "middle".
	 * @param nClicks		int, 	number of times to click the mouse. It's supposed to be greater than or equal to 1.
	 * @param offset		Point,  position to click within the control. It can be null or empty String. Default value is center.
	 * @param specialKey	String, keyboard key that be hold when click action happening. It can be null or empty String.
	 * @return				boolean
	 * 
	 * @author scntax
	 */
	public boolean click(String title, String text, String controlID, 
            String button, int nClicks, Point offset, String specialKey) {		
		String dbgmsg = StringUtils.getMethodName(0, false);
		String[] pressKeyParam = null;
		String[] upKeyParam = null;
		String[] clickParam = null;
		
		// 1. Check and validate parameters.
		if (!isValidMouseButton(button) || nClicks < 1) {
			Log.error(dbgmsg + "(): " + "invalid parameters button:'" + button + "', clicks:'" + nClicks + "' provided.");
			return false;
		}
		
		if (button == null || button.equals("")) { 
			Log.debug(dbgmsg + "(): use 'left' value as no mouse button value is assigned.");
			button = AUTOIT_MOUSE_BUTTON_LEFT;
		} else{
			Log.debug(dbgmsg + "(): " + "use mouse button '" + button + "' to click.");
		}
		
		if (offset != null && !offset.equals("")) {
			Log.debug(dbgmsg + "(): " + "click at position offset '" + offset + "'.");
			clickParam = new String[]{ title, text, controlID, button, String.valueOf(nClicks), String.valueOf(offset.x), String.valueOf(offset.y) };
		} else{
			Log.debug(dbgmsg + "(): " + "click at default center position.");
			offset = new Point(controlGetPosWidth(title, text, controlID) / 2, controlGetPosHeight(title, text, controlID) / 2);
			Log.debug(dbgmsg + "(): " + "set click position offset '" + offset + "'.");
			clickParam = new String[]{ title, text, controlID, button, String.valueOf(nClicks) };
		}
		
		if (specialKey != null && !specialKey.equals("")) {
			if(!supportHoldingKeyPress(specialKey)) {
				Log.error(dbgmsg + "(): " + "the key '" + specialKey + "' is NOT supported by AutoIt.");
				return false;
			}
			
			Log.debug(dbgmsg + "(): " + "holding special key '" + specialKey + "' while clicking.");
			
			pressKeyParam 	= new String[]{ wrapKeyPress(specialKey), String.valueOf((supportSpecialKey? 0 : 1)) };
			upKeyParam 	= new String[]{ wrapKeyUp(specialKey), String.valueOf((supportSpecialKey? 0 : 1)) };
		}
		
		boolean result = false;
		try {
			// 2. Use Java AWT Robot click call first
			Log.info(dbgmsg + "(): " + "use Java AWT Robot to click.");
			
			result = robotClick(winGetPosX(title, text) + controlGetPosX(title, text, controlID) + getBorderWidth(title, text), 
					            winGetPosY(title, text) + controlGetPosY(title, text, controlID) + getTitleBarHeight(title, text), 
					            button, nClicks, offset, specialKey);
		} catch (Exception e) {
			Log.info(dbgmsg + "(): " + "failed to use Java AWT Robot click with exception: " + e.toString());
			Log.info(dbgmsg + "(): " + "try to use AutoIt click API.");
			
			// 3. If Windows Native click call failed, use AutoIt API to execute click action. 
			if (pressKeyParam != null) { autoItX.invoke(AUTOIT_KEYWORD_SEND, convertCOMParams(pressKeyParam)); } 
			result = oneToTrue((autoItX.invoke(AUTOIT_KEYWORD_CONTROLCLICK, convertCOMParams(clickParam))).getInt());
			if (upKeyParam != null) { autoItX.invoke(AUTOIT_KEYWORD_SEND, convertCOMParams(upKeyParam)); }
		}
		
		return result;
	}
	
}
