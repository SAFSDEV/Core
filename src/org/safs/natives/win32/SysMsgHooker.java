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
package org.safs.natives.win32;


import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

/**
 * An interface to SysMsgHooker.dll that contains callback functions like MouseProc, KeyboardProc, 
 * hooker setting functions and corresponding listener functions.   
 * @author Junwu Ma
 *
 */
public interface SysMsgHooker  extends StdCallLibrary {
	SysMsgHooker INSTANCE = (SysMsgHooker) Native.loadLibrary("SysMsgHooker", SysMsgHooker.class);

	boolean setMouseProcHook();
	void removeMouseProcHook();
	void setMouseListener(StdCallCallback lpfn);
	
	boolean setKeyboardProcHook();
	void removeKeyboardProcHook();
	void setKeyboardListener(StdCallCallback lpfn);
	
	boolean setGetMsgProcHook();
	void removeGetMsgProcHook();
	void setGetMsgListener(StdCallCallback lpfn);
}
