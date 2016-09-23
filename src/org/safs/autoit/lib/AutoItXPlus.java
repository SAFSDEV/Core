/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

/**
*
* History:<br>
*
* <br>   SEP 22, 2016  (Tao Xie) Initial release: create SAFS version AutoItXPlus to bridge Java and AutoIt APIs.
* 								Add convertCOMParams(): convert String parameters array into com.jacob.com.Variant array.
* 								Add controlClick(): Workhorse of AutoIt click action.
* 								Move 'isValidMouseButton()' from AutoItComponent to here.
* 
*/
package org.safs.autoit.lib;

import java.awt.Point;

import org.safs.Log;
import org.safs.StringUtils;

import com.jacob.com.Variant;
import autoitx4java.AutoItX;

/**
 * <a href="https://www.autoitscript.com/site/autoit/downloads/"> AutoIt/AutoItX </a> is the DLL/COM control, which can be used by different programming 
 * languages to access AutoIt through COM objects. Currently, 3rd part Java JAR <a href="https://github.com/accessrichard/autoitx4java"> autoitx4java </a> 
 * has provided one java version AutoItX object by using JACOB to access this AutoIt/AutoItX COM object. It gives many common Java APIs to use AutoIt functionalities, 
 * but still lacks of APIs like "Ctrl + Click", "Alt + Click" to support SAFS keywords. Thus it requires creating SAFS version 'AutoItXPlus', which extends from 
 * 'autoitx4java/AutoItX', to access AutoIt functionalities through AutoIt/AutoItX COM object.
 * 
 * @author Tao Xie
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
	
	public final static String AUTOIT_ALT_DOWN 			= "{ALTDOWN}";
	public final static String AUTOIT_ALT_UP 				= "{ALTUP}";
	public final static String AUTOIT_SHIFT_DOWN 			= "{SHIFTDOWN}";
	public final static String AUTOIT_SHIFT_UP 			= "{SHIFTUP}";
	public final static String AUTOIT_CTRL_DOWN 			= "{CTRLDOWN}";
	public final static String AUTOIT_CTRL_UP 				= "{CTRLUP}";
	public final static String AUTOIT_LEFT_WINDOWS_DOWN 	= "{LWINDOWN}";
	public final static String AUTOIT_LEFT_WINDOWS_UP		= "{LWINUP}";
	public final static String AUTOIT_RIGHT_WINDOWS_DOWN 	= "{RWINDOWN}";
	public final static String AUTOIT_RIGHT_WINDOWS_UP 	= "{RWINUP}";
	
	/** AutoIt's click button constants **/
	public final static String AUTOIT_CLICKBUTTON_LEFT   = "left";
	public final static String AUTOIT_CLICKBUTTON_MIDDLE = "middle";
	public final static String AUTOIT_CLICKBUTTON_RIGHT  = "right";
	
	public AutoItXPlus(){
		super();
	}
	
	/**
	 * Convert parameters String array into com.jacob.com.Variant array.
	 * 
	 * @param 	paramMsgs String[], string array parameters
	 * @return 			  Variant[]
	 * 
	 * @author Tao Xie
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
	 * @author Tao Xie
	 * 
	 */
	public boolean isValidMouseButton(String mouseButton){
		if (mouseButton == null 
				|| mouseButton.equals("")
				|| mouseButton == AUTOIT_CLICKBUTTON_LEFT 
				|| mouseButton == AUTOIT_CLICKBUTTON_MIDDLE
				|| mouseButton == AUTOIT_CLICKBUTTON_RIGHT) {
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
	 * @author Tao Xie
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
	 * Workhorse of AutoIt click action.
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
	 * @author Tao Xie
	 */
	public boolean controlClick(String title, String text, String controlID, 
            String button, int nClicks, Point offset, String specialKey) {		
		String dbgmsg = StringUtils.getMethodName(0, false);
		String[] pressKeyParam = null;
		String[] upKeyParam = null;
		String[] clickParam = null;
		
		if (!isValidMouseButton(button) || nClicks < 1) {
			Log.error(dbgmsg + "(): " + "invalid parameters button:'" + button + "', clicks:'" + nClicks + "' provided.");
			return false;
		}
		
		if (button == null || button.equals("")) { 
			Log.debug(dbgmsg + "(): use 'left' value as no mouse button value is assigned.");
			button = AUTOIT_CLICKBUTTON_LEFT;
		} else{
			Log.debug(dbgmsg + "(): " + "use mouse button '" + button + "' to click.");
		}
		
		if (offset != null && !offset.equals("")) {
			Log.debug(dbgmsg + "(): " + "click at position offset '" + offset + "'.");
			clickParam = new String[]{ title, text, controlID, button, String.valueOf(nClicks), String.valueOf(offset.x), String.valueOf(offset.y) };
		} else{
			Log.debug(dbgmsg + "(): " + "click at default center position.");
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
		
		if (pressKeyParam != null) { autoItX.invoke(AUTOIT_KEYWORD_SEND, convertCOMParams(pressKeyParam)); } 
		boolean result = oneToTrue((autoItX.invoke(AUTOIT_KEYWORD_CONTROLCLICK, convertCOMParams(clickParam))).getInt());
		if (upKeyParam != null) { autoItX.invoke(AUTOIT_KEYWORD_SEND, convertCOMParams(upKeyParam)); }
		
		return result;
	}
	
}
