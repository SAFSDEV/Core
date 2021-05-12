/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.safs.natives;

import java.awt.event.KeyEvent;

import org.safs.Log;

/**
 * 
 * This class contains Microsoft Virtual Key Code, refer to following link<br>
 * http://msdn.microsoft.com/en-us/library/dd375731(v=VS.85).aspx<br>
 * It contains some convenient method to test MS virtual key code, such as
 * {@link #isShiftKey(int)}
 * {@link #isAltKey(int)} <br>
 * There is another method {@link #convertToJavaVK(int)}, which can convert<br>
 * MS virtual key code to Java virtual key code.<br>
 *
 * @author Lei Wang
 *
 * @see org.safs.natives.MSLLKtoRKEventListener
 * 
 */

public class MSKeyEvent {

	public static final int VK_LSHIFT		= 0xA0;// Left SHIFT key
	public static final int VK_RSHIFT		= 0xA1;// Right SHIFT key
	public static final int VK_LCONTROL		= 0xA2;// Left CONTROL key
	public static final int VK_RCONTROL		= 0xA3;// Right CONTROL key
	public static final int VK_LMENU		= 0xA4;// Left MENU key
	public static final int VK_RMENU		= 0xA5;// Right MENU key
	
	public static final int VK_RETURN		= 0x0D;// ENTER key
	
	public static final int VK_CANCEL       = 0x03;// Control-break processing
	public static final int VK_BACK			= 0x08;// BACKSPACE key
	public static final int VK_TAB			= 0x09;// TAB key
	public static final int VK_CLEAR		= 0x0C;// CLEAR key
	
	public static final int VK_SHIFT		= 0x10;// SHIFT key
	public static final int VK_CONTROL		= 0x11;// CTRL key	 
	public static final int VK_MENU			= 0x12;// ALT key
	public static final int VK_PAUSE		= 0x13;// PAUSE key
	public static final int VK_CAPITAL		= 0x14;// CAPS LOCK key
	
	public static final int VK_ESCAPE		= 0x1B;// ESC key	 	
	public static final int VK_SPACE		= 0x20;// SPACEBAR
	public static final int VK_PRIOR		= 0x21;// PAGE UP key
	public static final int VK_NEXT			= 0x22;// PAGE DOWN key
	public static final int VK_END			= 0x23;// END key
	public static final int VK_HOME			= 0x24;// HOME key
	public static final int VK_LEFT			= 0x25;// LEFT ARROW key
	public static final int VK_UP			= 0x26;// UP ARROW key
	public static final int VK_RIGHT		= 0x27;// RIGHT ARROW key
	public static final int VK_DOWN			= 0x28;// DOWN ARROW key
	
	//Do NOT know which key code to match for the following 3 keys!!!!!!
	public static final int VK_SELECT		= 0x29;// SELECT key
	public static final int VK_PRINT		= 0x2A;// PRINT key
	public static final int VK_EXECUTE		= 0x2B;// EXECUTE key
	
	public static final int VK_SNAPSHOT		= 0x2C;// PRINT SCREEN key
	public static final int VK_INSERT		= 0x2D;// INS key
	public static final int VK_DELETE		= 0x2E;// DEL key
	public static final int VK_HELP			= 0x2F;// HELP key
    
	public static final int VK_LWIN			= 0x5B;// Left Windows key (Natural keyboard) 
	public static final int VK_RWIN			= 0x5C;// Right Windows key (Natural keyboard)
	public static final int VK_APPS			= 0x5D;// Applications key (Natural keyboard)
	 

	public static final int VK_OEM_1		= 0xBA;// Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard, the ';:' key 
	 
	public static final int VK_OEM_PLUS		= 0xBB;// For any country/region, the '+' key
	 
	public static final int VK_OEM_COMMA	= 0xBC;// For any country/region, the ',' key
	 
	public static final int VK_OEM_MINUS	= 0xBD;// For any country/region, the '-' key
	 
	public static final int VK_OEM_PERIOD	= 0xBE;// For any country/region, the '.' key
	 
	public static final int VK_OEM_2		= 0xBF;// Used for miscellaneous characters; it can vary by keyboard.For the US standard keyboard, the '/?' key 
	 
	public static final int VK_OEM_3		= 0xC0;// Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard, the '`~' key 
	 	 
	public static final int VK_OEM_4		= 0xDB;// Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard, the '[{' key
	 
	public static final int VK_OEM_5		= 0xDC;// Used for miscellaneous characters; it can vary by keyboard.For the US standard keyboard, the '\|' key
	 
	public static final int VK_OEM_6		= 0xDD;// Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard, the ']}' key
	 
	public static final int VK_OEM_7		= 0xDE;// Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard, the 'single-quote/double-quote' key
	 
	public static final int VK_OEM_8		= 0xDF;// Used for miscellaneous characters; it can vary by keyboard.	 
	
	public static boolean isShiftKey(int vkcode){
		return (VK_LSHIFT==vkcode || VK_RSHIFT==vkcode || VK_SHIFT==vkcode);
	}
	
	public static boolean isControlKey(int vkcode){
		return (VK_LCONTROL==vkcode || VK_RCONTROL==vkcode || VK_CONTROL==vkcode);
	}
	
	public static boolean isAltKey(int vkcode){
		return (VK_LMENU==vkcode || VK_RMENU==vkcode || VK_MENU==vkcode);
	}
	
	public static boolean isWindowsKey(int vkcode){
		return (VK_LWIN==vkcode || VK_RWIN==vkcode);
	}
	
	public static boolean isCapsLockKey(int vkcode){
		return VK_CAPITAL==vkcode;
	}
	
	public static boolean isEnter(int vkcode){
		return VK_RETURN==vkcode;
	}
	
	/**
	 * <b>Purpose:</b>	The windows virtual key code is NOT consistent with that of JAVA<br>
	 * 					This function is used to convert the different key code of windows to that of JAVA.<br>
	 * <b>Note:</b>		For those key codes of the same value between Windows and Java, they are not converted here.<br>
	 * 					This function may be need to updated, if one day they are not the same.<br><br>
	 * 					For those key codes has two java key codes to map (one is with Shift On, one without), we<br>
	 * 					convert to the Java key code without Shift On.<br>
	 *                  For example:<br>
	 *                  Windows key code: VK_OEM_4, it represents the key '[{' of US keyboard<br>
	 *                  Java has one key code VK_OPEN_BRACKET for '[' (without shift on), <br>
	 *                  and one key code VK_BRACELEFT for '{' (with shift on)<br>
	 *                  We will return VK_OPEN_BRACKET as result, the one without shift on<br>
	 * @param MSVkcode	The Virtual Key Code for Windows, see <br>
	 * 					http://msdn.microsoft.com/en-us/library/dd375731(v=VS.85).aspx<br>
	 * @return			The corresponding JAVA VK KEY CODE
	 */
	public static int convertToJavaVK(int MSVkcode){
		int javaVK = MSVkcode;
		
		if(isShiftKey(MSVkcode)){
			javaVK = KeyEvent.VK_SHIFT;
		}else if(isControlKey(MSVkcode)){
			javaVK = KeyEvent.VK_CONTROL;
		}else if(isAltKey(MSVkcode)){
			javaVK = KeyEvent.VK_ALT;
		}else if(isCapsLockKey(MSVkcode)){
			javaVK = KeyEvent.VK_CAPS_LOCK;
		}else if(isEnter(MSVkcode)){
			javaVK = KeyEvent.VK_ENTER;
		}else if(isWindowsKey(MSVkcode)){
			javaVK = KeyEvent.VK_WINDOWS;
		}else if(VK_OEM_1==MSVkcode){
			//Not return KeyEvent.VK_COLON
			javaVK = KeyEvent.VK_SEMICOLON;
		}else if(VK_OEM_PLUS==MSVkcode){
			javaVK = KeyEvent.VK_EQUALS;
		}else if(VK_OEM_COMMA==MSVkcode){
			javaVK = KeyEvent.VK_COMMA;
		}else if(VK_OEM_MINUS==MSVkcode){
			javaVK = KeyEvent.VK_MINUS;
		}else if(VK_OEM_PERIOD==MSVkcode){
			javaVK = KeyEvent.VK_PERIOD;
		}else if(VK_OEM_2==MSVkcode){
			javaVK = KeyEvent.VK_SLASH;
		}else if(VK_OEM_3==MSVkcode){
			javaVK = KeyEvent.VK_BACK_QUOTE;
		}else if(VK_OEM_4==MSVkcode){
			javaVK = KeyEvent.VK_OPEN_BRACKET;
		}else if(VK_OEM_5==MSVkcode){
			javaVK = KeyEvent.VK_BACK_SLASH;
		}else if(VK_OEM_6==MSVkcode){
			javaVK = KeyEvent.VK_CLOSE_BRACKET;
		}else if(VK_OEM_7==MSVkcode){
			javaVK = KeyEvent.VK_QUOTE;
		}else if(VK_OEM_8==MSVkcode){
			//??? undefined
		}else if(VK_SELECT==MSVkcode){
			//??? undefined
		}else if(VK_PRINT==MSVkcode){
			//??? undefined
		}else if(VK_EXECUTE==MSVkcode){
			//??? undefined
		}else if(VK_SNAPSHOT==MSVkcode){
			javaVK = KeyEvent.VK_PRINTSCREEN;
		}else if(VK_INSERT==MSVkcode){
			javaVK = KeyEvent.VK_INSERT;
		}else if(VK_DELETE==MSVkcode){
			javaVK = KeyEvent.VK_DELETE;
		}else if(VK_HELP==MSVkcode){
			//??? undefined
		}else if(VK_APPS==MSVkcode){
			javaVK = KeyEvent.VK_CONTEXT_MENU;
		}

		Log.debug("MSKeyEvent.convertToJavaVK(): converting MS VK: "+MSVkcode+" to JAVA VK: "+javaVK);
		
		return javaVK;
	}

}
