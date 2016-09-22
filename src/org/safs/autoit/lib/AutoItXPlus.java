/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

/**
*
* History:<br>
*
* <br>   SEP 22, 2016  (Tao Xie) Initial release: create SAFS version AutoItXPlus to bridge Java and AutoIt APIs.
* 								Add generateCOMParams(): convert String parameters array into com.jacob.com.Variant array.
* 								Add controlClick(): provide holding special key when click action happen.
* 
*/
package org.safs.autoit.lib;

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
	public final boolean DEFAULT_SUPPORT_SPECIALKEY = true; 
	public boolean supportSpecialKey = DEFAULT_SUPPORT_SPECIALKEY; 
	
	/**
	 * AutoIt's supporting KeyPress/KeyDown keywords
	 */
	public final String ALT 		= "ALT";
	public final String SHIFT 		= "SHIFT";
	public final String CTRL 		= "CTRL";
	public final String LWINDOWS 	= "LWIN";
	public final String RWINDOWS 	= "RWIN";
	
	public final String AUTOIT_ALT_DOWN 			= "{ALTDOWN}";
	public final String AUTOIT_ALT_UP 				= "{ALTUP}";
	public final String AUTOIT_SHIFT_DOWN 			= "{SHIFTDOWN}";
	public final String AUTOIT_SHIFT_UP 			= "{SHIFTUP}";
	public final String AUTOIT_CTRL_DOWN 			= "{CTRLDOWN}";
	public final String AUTOIT_CTRL_UP 				= "{CTRLUP}";
	public final String AUTOIT_LEFT_WINDOWS_DOWN 	= "{LWINDOWN}";
	public final String AUTOIT_LEFT_WINDOWS_UP		= "{LWINUP}";
	public final String AUTOIT_RIGHT_WINDOWS_DOWN 	= "{RWINDOWN}";
	public final String AUTOIT_RIGHT_WINDOWS_UP 	= "{RWINUP}";
	
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
	public Variant[] generateCOMParams(final String[] paramMsgs) {
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
		if(keyStr != null && (keyStr.equals(ALT)
				|| keyStr.equals(CTRL)
				|| keyStr.equals(LWINDOWS)
				|| keyStr.equals(RWINDOWS)
				|| keyStr.equals(SHIFT))) {
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
	 * Provide functionality that holding special key when click action happen.
	 * 
	 * @param title 		String, title of the window to access.
	 * @param text 			String, text of the window to access.
	 * @param controlID		String, control to interact with.
	 * @param button		String, button to click, "left", "right" or "middle".
	 * @param clicks		int, 	number of times to click the mouse. Default is center.
	 * @param x				int,	x position to click within the control.
	 * @param y				int,	y position to click within the control.
	 * @param specialKey	String, keyboard key that be hold when click action happening.
	 * @return				boolean
	 * 
	 * @author Tao Xie
	 */
	public boolean controlClick(String title, String text, String controlID, 
            String button, int clicks, int x, int y, String specialKey) {
		
		String dbgmsg = StringUtils.getMethodName(0, false);
		
		if (!supportHoldingKeyPress(specialKey)) {
			Log.debug(dbgmsg + "(): " + "the key '" + specialKey + "' is NOT supported by AutoIt.");
			return false;
		}
		
		Variant[] pressKeyParams = generateCOMParams(new String[] 
									{wrapKeyPress(specialKey), String.valueOf((supportSpecialKey? 0 : 1))}
								);		
		Variant[] clickParams = generateCOMParams(new String[]
									{title, text, controlID, button, String.valueOf(clicks), String.valueOf(x), String.valueOf(y)}
								);
		Variant[] upKeyParams = generateCOMParams(new String[] 
									{wrapKeyUp(specialKey), String.valueOf((supportSpecialKey? 0 : 1))}
								);
		
		autoItX.invoke("Send", pressKeyParams);
		Variant result = autoItX.invoke("ControlClick", clickParams);
		autoItX.invoke("Send", upKeyParams);
		
		return oneToTrue(result.getInt());
	}
	
	
}
