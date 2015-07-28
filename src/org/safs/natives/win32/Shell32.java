package org.safs.natives.win32;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

/**
 * A JNA Library definition for the Windows shell32.dll
 * <p>
 * <a href="http://jna.dev.java.net" target="_blank">JNA Home Page</a>
 * @author Carl Nagle
 * @since 2009.10.20
 * @see org.safs.natives.NativeWrapper
 */
public interface Shell32 extends StdCallLibrary {

	Shell32 INSTANCE = (Shell32) Native.loadLibrary("shell32", Shell32.class);

	/**
	 * Integer ShellExecuteA(Long hwnd, String lpOperation, String lpURL, 
	 *					  String lpParameters, String lpDirectory, long nShowCmd);
	 * <p>
	 * Performs an operation on a specified file.
	 * <p>
	 * hwnd<br>
	 * [in] A handle to the owner window used for displaying a user interface (UI) or error 
	 * messages. This value can be NULL if the operation is not associated with a window.
	 * lpOperation<br>
	 * [in] A pointer to a null-terminated string, referred to in this case as a verb, that 
	 * specifies the action to be performed. The set of available verbs depends on the 
	 * particular file or folder. Generally, the actions available from an object's shortcut 
	 * menu are available verbs. The following verbs are commonly used:<br>
	 * edit<br>
	 * Launches an editor and opens the document for editing. If lpFile is not a document 
	 * file, the function will fail.<br>
	 * explore<br>
	 * Explores a folder specified by lpFile.<br>
	 * find<br>
	 * Initiates a search beginning in the directory specified by lpDirectory.<br>
	 * open<br>
	 * Opens the item specified by the lpFile parameter. The item can be a file or folder.<br>
	 * print<br>
	 * Prints the file specified by lpFile. If lpFile is not a document file, the function 
	 * fails.<br>
	 * NULL<br>
	 * In systems prior to Microsoft Windows 2000, the default verb is used if it is valid 
	 * and available in the registry. If not, the "open" verb is used.
	 * <p>
	 * In Windows 2000 and later, the default verb is used if available. If not, the "open" 
	 * verb is used. If neither verb is available, the system uses the first verb listed 
	 * in the registry.
	 * <p>
	 * lpFile<br>
	 * [in] A pointer to a null-terminated string that specifies the file or object on 
	 * which to execute the specified verb. To specify a Shell namespace object, pass the 
	 * fully qualified parse name. Note that not all verbs are supported on all objects. 
	 * For example, not all document types support the "print" verb. If a relative path is 
	 * used for the lpDirectory parameter do not use a relative path for lpFile.<br>
	 * lpParameters<br>
	 * [in] If lpFile specifies an executable file, this parameter is a pointer to a 
	 * null-terminated string that specifies the parameters to be passed to the application. 
	 * The format of this string is determined by the verb that is to be invoked. If 
	 * lpFile specifies a document file, lpParameters should be NULL.<br>
	 * lpDirectory<br>
	 * [in] A pointer to a null-terminated string that specifies the default (working) 
	 * directory for the action. If this value is NULL, the current working directory 
	 * is used. If a relative path is provided at lpFile, do not use a relative path for 
	 * lpDirectory.<br>
	 * nShowCmd<br>
	 * [in] The flags that specify how an application is to be displayed when it is 
	 * opened. If lpFile specifies a document file, the flag is simply passed to the 
	 * associated application. It is up to the application to decide how to handle it.
	 * <p>
	 * SW_HIDE<br>
	 * Hides the window and activates another window.<br>
	 * SW_MAXIMIZE<br>
	 * Maximizes the specified window.<br>
	 * SW_MINIMIZE<br>
	 * Minimizes the specified window and activates the next top-level window in the 
	 * z-order.<br>
	 * SW_RESTORE<br>
	 * Activates and displays the window. If the window is minimized or maximized, 
	 * Windows restores it to its original size and position. An application should 
	 * specify this flag when restoring a minimized window.<br>
	 * SW_SHOW<br>
	 * Activates the window and displays it in its current size and position.<br>
	 * SW_SHOWDEFAULT<br>
	 * Sets the show state based on the SW_ flag specified in the STARTUPINFO structure 
	 * passed to the CreateProcess function by the program that started the application. 
	 * An application should call ShowWindow with this flag to set the initial show state 
	 * of its main window.<br>
	 * SW_SHOWMAXIMIZED<br>
	 * Activates the window and displays it as a maximized window.<br>
	 * SW_SHOWMINIMIZED<br>
	 * Activates the window and displays it as a minimized window.<br>
	 * SW_SHOWMINNOACTIVE<br>
	 * Displays the window as a minimized window. The active window remains active.<br>
	 * SW_SHOWNA<br>
	 * Displays the window in its current state. The active window remains active.<br>
	 * SW_SHOWNOACTIVATE<br>
	 * Displays a window in its most recent size and position. The active window remains 
	 * active.<br>
	 * SW_SHOWNORMAL<br>
	 * Activates and displays a window. If the window is minimized or maximized, Windows 
	 * restores it to its original size and position. An application should specify this 
	 * flag when displaying the window for the first time.
	 * <p>
	 * @return If the function succeeds, it returns a value greater than 32. If the 
	 * function fails, it returns an error value that indicates the cause of the failure. 
	 * The return value is cast as an HINSTANCE for backward compatibility with 16-bit 
	 * Windows applications. It is not a true HINSTANCE, however. It can be cast only to 
	 * an int and compared to either 32 or the following error codes below.
	 * <pre>
	 * 0 The operating system is out of memory or resources. 
	 * ERROR_FILE_NOT_FOUND The specified file was not found. 
	 * ERROR_PATH_NOT_FOUND The specified path was not found. 
	 * ERROR_BAD_FORMAT The .exe file is invalid (non-Microsoft Win32 .exe or error in 
	 *                  .exe image). 
	 * SE_ERR_ACCESSDENIED The operating system denied access to the specified file. 
	 * SE_ERR_ASSOCINCOMPLETE The file name association is incomplete or invalid. 
	 * SE_ERR_DDEBUSY The Dynamic Data Exchange (DDE) transaction could not be completed 
	 *                because other DDE transactions were being processed. 
	 * SE_ERR_DDEFAIL The DDE transaction failed. 
	 * SE_ERR_DDETIMEOUT The DDE transaction could not be completed because the request 
	 *                   timed out. 
	 * SE_ERR_DLLNOTFOUND The specified DLL was not found. 
	 * SE_ERR_FNF The specified file was not found. 
	 * SE_ERR_NOASSOC There is no application associated with the given file name 
	 *                extension. This error will also be returned if you attempt to print 
	 *                a file that is not printable. 
	 * SE_ERR_OOM There was not enough memory to complete the operation. 
	 * SE_ERR_PNF The specified path was not found. 
	 * SE_ERR_SHARE A sharing violation occurred.
	 * </pre> 
	 * @see org.safs.natives.NativeWrapper
	 */
	Integer ShellExecuteA(NativeLong hwnd, String lpOperation, String lpURL, 
						String lpParameters, String lpDirectory, long nShowCmd);

}
