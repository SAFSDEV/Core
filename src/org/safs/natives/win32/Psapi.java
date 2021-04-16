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
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

/**
 * A JNA Library definition for the Windows Psapi.dll
 * <p>
 * <a href="http://jna.dev.java.net" target="_blank">JNA Home Page</a>
 * @author Carl Nagle
 * @since 2009.08.06
 * @see org.safs.natives.NativeWrapper
 */
public interface Psapi extends StdCallLibrary {

	Psapi INSTANCE = (Psapi) Native.loadLibrary("psapi", Psapi.class);

	/**
	 * DWORD WINAPI GetProcessImageFileName(_in HANDLE hProcess, _out LPTSTR lpImageFileName, _in DWORD nSize);
	 * <p>
	 * Retrieves the name of the executable file for the specified process.<br>
	 * For Windows Server 2008, Windows Vista, Windows Server 2003, & Windows XP/2000<br>
	 * Use the Kernel32 version for later versions of Windows.
	 * <p>
	 * @param hProcess [in] A handle to the process. The handle must have the 
	 * PROCESS_QUERY_INFORMATION or PROCESS_QUERY_LIMITED_INFORMATION access right. 
	 * For more information, see MSDN Process Security and Access Rights.
	 * <p>
	 * Windows Server 2003 and Windows XP:  The handle must have the PROCESS_QUERY_INFORMATION 
	 * access right.
	 * @param pImageFileName [out] A pointer to a buffer that receives the full path to 
	 * the executable file.
	 * @param nSize [in] The size of the pImageFileName buffer, in characters.
	 * @return If the function succeeds, the return value specifies the length of the 
	 * string copied to the buffer. If the function fails, the return value is zero.
	 * @see Kernel32#OpenProcess(int, boolean, int)
	 * @see Kernel32#CloseHandle(Pointer)
	 * @see Kernel32#GetProcessImageFileNameA(Pointer, Pointer, int)
	 * @see org.safs.natives.NativeWrapper#GetProcessFileName(Object)
	 */
	int GetProcessImageFileNameA(Pointer hProcess, Pointer pImageFilename, int nSize);

}
