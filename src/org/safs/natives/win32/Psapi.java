package org.safs.natives.win32;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

/**
 * A JNA Library definition for the Windows Psapi.dll
 * <p>
 * <a href="http://jna.dev.java.net" target="_blank">JNA Home Page</a>
 * @author canagl
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
