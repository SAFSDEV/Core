/**
 * History for developer:
 * JunwuMa  Oct 21, 2010  Add GetModuleHandleA API interface.
 * Lei Wang 	Nov 18, 2011  Add API interface: CreateFile and GetFileTime
 *                        Add structure FileTime and SecurityAttribute
 * Lei Wang 	MAY 03, 2016  Upgrade the dependency JNA to 4.2.2.
 */
package org.safs.natives.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

/**
 * A JNA Library definition for the Windows Kernel32.dll
 * <p>
 * <a href="http://jna.dev.java.net" target="_blank">JNA Home Page</a>
 * @author Carl Nagle
 * @since 2009.06.03
 * @see org.safs.natives.NativeWrapper
 */
public interface Kernel32 extends StdCallLibrary {

	/**
	 * <pre>
	 * For method {@link #CreateFileA(String, int, int, SecurityAttribute, int, int, Pointer)}
	 * and {@link #CreateFileW(String, int, int, SecurityAttribute, int, int, Pointer)}
	 * The second parameter dwDesiredAccess can take one of the following constants,
	 * or any combination of them
	 * </pre>
	 */
	public static final int GENERIC_READ = 0x80000000;
	public static final int GENERIC_WRITE = 0x40000000;
	public static final int GENERIC_EXECUTE = 0x20000000;
	public static final int GENERIC_ALL = 0x10000000;
	public static final int GENERIC_NO_ACCESS = 0x00000000;
	
	/**
	 * <pre>
	 * For method {@link #CreateFileA(String, int, int, SecurityAttribute, int, int, Pointer)}
	 * and {@link #CreateFileW(String, int, int, SecurityAttribute, int, int, Pointer)}
	 * The third parameter dwShareMode can take one of the following constants,
	 * or any combination of them
	 * 
	 * FILE_SHARE_EXCLUSIVE: Prevents other processes from opening a file or device 
	 *                       if they request delete, read, or write access.
	 * FILE_SHARE_READ: Enables subsequent open operations on a file or device to request read access.
	 *                  Otherwise, other processes cannot open the file or device if they request read access.
	 *                  If this flag is not specified, but the file or device has been opened for read access, the function fails.                
	 * FILE_SHARE_WRITE: Enables subsequent open operations on a file or device to request write access.
	 *                   Otherwise, other processes cannot open the file or device if they request write access.
	 *                   If this flag is not specified, but the file or device has been opened for write access 
	 *                   or has a file mapping with write access, the function fails.
	 * FILE_SHARE_DELETE: Enables subsequent open operations on a file or device to request delete access.
	 *                    Otherwise, other processes cannot open the file or device if they request delete access.
	 *                    If this flag is not specified, but the file or device has been opened for delete access, the function fails.
	 *                    Note:  Delete access allows both delete and rename operations.
	 * </pre>
	 */
	public static final int FILE_SHARE_EXCLUSIVE = 0x00000000;
	public static final int FILE_SHARE_READ = 0x00000001;
	public static final int FILE_SHARE_WRITE = 0x00000002;
	public static final int FILE_SHARE_DELETE = 0x00000004;
	//FILE_SHARE_R_W_D is a combination of FILE_SHARE_READ, FILE_SHARE_WRITE and FILE_SHARE_DELETE
	public static final int FILE_SHARE_R_W_D = FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE;
	
	/**
	 * <pre>
	 * For method {@link #CreateFileA(String, int, int, SecurityAttribute, int, int, Pointer)}
	 * and {@link #CreateFileW(String, int, int, SecurityAttribute, int, int, Pointer)}
	 * The fifth parameter dwCreationDisposition can take one of the following constants,
	 * or any combination of them
	 * 
	 * OPEN_EXISTING: Opens a file or device, only if it exists.
	 *                If the specified file or device does not exist, 
	 *                the function fails and the last-error code is set to ERROR_FILE_NOT_FOUND (2).
	 * </pre>
	 */
	public static final int OPEN_EXISTING = 3;
	
	/**
	 * <pre>
	 * For method {@link #CreateFileA(String, int, int, SecurityAttribute, int, int, Pointer)}
	 * and {@link #CreateFileW(String, int, int, SecurityAttribute, int, int, Pointer)}
	 * the sixth parameter dwFlagsAndAttributes can take one of the following constants,
	 * or any combination of them
	 * 
	 * FILE_FLAG_BACKUP_SEMANTICS: The file is being opened or created for a backup or restore operation. 
	 *                             The system ensures that the calling process overrides file security checks
	 *                             when the process has SE_BACKUP_NAME and SE_RESTORE_NAME privileges.
	 *                             You must set this flag to obtain a handle to a directory.
	 * </pre>
	 */
	public static final int FILE_FLAG_BACKUP_SEMANTICS = 0x02000000;
	
	/**
	 * Define some constants to represent the last error code.
	 * The last error code can be retrieved by method {@link #GetLastError()}
	 */
	public static final int ERROR_FILE_NOT_FOUND = 2;
	public static final int SUCCESS_EXECUTE = 0;
	
	Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);

	/**
	 * <pre>
	 * Returns the calling thread's last-error code. The Return Value section of the WINAPI 
	 * documentation for each function that sets the last-error code notes the conditions 
	 * under which the function sets the last-error code. Most functions that set the thread's 
	 * last-error code set it when they fail. However, some functions also set the last-error 
	 * code when they succeed. If the function is not documented to set the last-error code, 
	 * the value returned by this function is simply the most recent last-error code to have 
	 * been set; some functions set the last-error code to 0 on success and others do not.
	 * 
	 * Remarks
	 * Functions executed by the calling thread set this value by calling the SetLastError 
	 * function. You should call the GetLastError function immediately when a function's return
	 * value indicates that such a call will return useful data. That is because some functions 
	 * call SetLastError with a zero when they succeed, wiping out the error code set by the 
	 * most recently failed function.
	 * 
	 * To obtain an error string for system error codes, use the FormatMessage function. For 
	 * a complete list of error codes provided by the operating system, see System Error Codes.
	 * 
	 * The error codes returned by a function are not part of the Windows API specification and 
	 * can vary by operating system or device driver. For this reason, we cannot provide the 
	 * complete list of error codes that can be returned by each function. There are also many 
	 * functions whose documentation does not include even a partial list of error codes that 
	 * can be returned.
	 * 
	 * Error codes are 32-bit values (bit 31 is the most significant bit). Bit 29 is reserved 
	 * for application-defined error codes; no system error code has this bit set. If you are 
	 * defining an error code for your application, set this bit to one. That indicates that 
	 * the error code has been defined by an application, and ensures that your error code does 
	 * not conflict with any error codes defined by the system.
	 * 
	 * To convert a system error into an HRESULT value, use the HRESULT_FROM_WIN32 macro.
	 * </pre>
	 * @return int 
	 * @see http://msdn.microsoft.com/en-us/library/ms681381(VS.85).aspx  (msdn:system error codes)
	 */
	int GetLastError();
	
	
	/**
	 * Formats a message string. The function requires a message definition as input. 
	 * The message definition can come from a buffer passed into the function. It can come from 
	 * a message table resource in an already-loaded module. Or the caller can ask the function 
	 * to search the system's message table resource(s) for the message definition. The function 
	 * finds the message definition in a message table resource based on a message identifier 
	 * and a language identifier. The function copies the formatted message text to an output 
	 * buffer, processing any embedded insert sequences if requested.<br>
	 * See the complete MSDN documentation for more information.
	 * <p>
	 * @param dwFlags -- [in] The formatting options, and how to interpret the lpSource parameter. 
	 * The low-order byte of dwFlags specifies how the function handles line breaks in the output 
	 * buffer. The low-order byte can also specify the maximum width of a formatted output line. 
	 * This parameter can be one or more of the following values.<br>
	 * FORMAT_MESSAGE_ALLOCATE_BUFFER - 0x00000100 <br>
	 *   The function allocates a buffer large enough to hold the formatted message, and places a 
	 *   pointer to the allocated buffer at the address specified by lpBuffer. The lpBuffer 
	 *   parameter is a pointer to an LPTSTR; you must cast the pointer to an LPTSTR (for example, 
	 *   (LPTSTR)&lpBuffer). The nSize parameter specifies the minimum number of TCHARs to allocate 
	 *   for an output message buffer. The caller should use the LocalFree function to free the 
	 *   buffer when it is no longer needed.<br>
	 * FORMAT_MESSAGE_ARGUMENT_ARRAY -- 0x00002000 <br>
	 *   The Arguments parameter is not a va_list structure, but is a pointer to an array of values 
	 *   that represent the arguments.<br>
	 *   This flag cannot be used with 64-bit integer values. If you are using a 64-bit integer, 
	 *   you must use the va_list structure.<br>
	 * FORMAT_MESSAGE_FROM_HMODULE -- 0x00000800 <br>
	 *   The lpSource parameter is a module handle containing the message-table resource(s) to 
	 *   search. If this lpSource handle is NULL, the current process's application image file 
	 *   will be searched. This flag cannot be used with FORMAT_MESSAGE_FROM_STRING.<br>
	 *   If the module has no message table resource, the function fails with 
	 *   ERROR_RESOURCE_TYPE_NOT_FOUND.<br>
	 * FORMAT_MESSAGE_FROM_STRING -- 0x00000400 <br>
	 *   The lpSource parameter is a pointer to a null-terminated string that contains a message 
	 *   definition. The message definition may contain insert sequences, just as the message text 
	 *   in a message table resource may. This flag cannot be used with FORMAT_MESSAGE_FROM_HMODULE 
	 *   or FORMAT_MESSAGE_FROM_SYSTEM.<br>
	 * FORMAT_MESSAGE_FROM_SYSTEM -- 0x00001000 (MOST COMMON USE)<br>
	 *   The function should search the system message-table resource(s) for the requested 
	 *   message. If this flag is specified with FORMAT_MESSAGE_FROM_HMODULE, the function 
	 *   searches the system message table if the message is not found in the module specified 
	 *   by lpSource. This flag cannot be used with FORMAT_MESSAGE_FROM_STRING. <br>
	 *   If this flag is specified, an application can pass the result of the GetLastError 
	 *   function to retrieve the message text for a system-defined error.<br>
	 * FORMAT_MESSAGE_IGNORE_INSERTS -- 0x00000200 <br>
	 *   Insert sequences in the message definition are to be ignored and passed through to the 
	 *   output buffer unchanged. This flag is useful for fetching a message for later formatting. 
	 *   If this flag is set, the Arguments parameter is ignored.<br>
	 *   <p>
	 * The low-order byte of dwFlags can specify the maximum width of a formatted output line. 
	 * The following are possible values of the low-order byte:<br>
	 * 0 -- There are no output line width restrictions. The function stores line breaks that 
	 *   are in the message definition text into the output buffer.<br>
	 * FORMAT_MESSAGE_MAX_WIDTH_MASK -- 0x000000FF <br>
	 * The function ignores regular line breaks in the message definition text. The function 
	 * stores hard-coded line breaks in the message definition text into the output buffer. The 
	 * function generates no new line breaks.<br>
	 * <p>
	 * @param lpSource -- [in_opt] -- The location of the message definition. The type of this 
	 * parameter depends upon the settings in the dwFlags parameter:<br>
	 * FORMAT_MESSAGE_FROM_HMODULE -- 0x00000800 <br>
	 *   A handle to the module that contains the message table to search.<br>
	 * FORMAT_MESSAGE_FROM_STRING -- 0x00000400 <br>
	 *   Pointer to a string that consists of unformatted message text. It will be scanned for 
	 *   inserts and formatted accordingly.<br>
	 * If neither of these flags is set in dwFlags, then lpSource is ignored.
	 * <p>
	 * @param dwMessageId - [in] - The message identifier for the requested message. 
	 * This parameter is ignored if dwFlags includes FORMAT_MESSAGE_FROM_STRING.
	 * <p>
	 * @param dwLanguageId - [in] - The language identifier for the requested message. 
	 * This parameter is ignored if dwFlags includes FORMAT_MESSAGE_FROM_STRING. <br>
	 * If you pass a specific LANGID in this parameter, FormatMessage will return a message for 
	 * that LANGID only. If the function cannot find a message for that LANGID, it returns 
	 * ERROR_RESOURCE_LANG_NOT_FOUND. If you pass in zero, FormatMessage looks for a message 
	 * for LANGIDs in the following order:<br>
	 *   1. Language neutral<br>
	 *   2. Thread LANGID, based on the thread's locale value<br>
	 *   3. User default LANGID, based on the user's default locale value<br>
	 *   4. System default LANGID, based on the system default locale value<br>
	 *   5. US English <br>
	 * If FormatMessage does not locate a message for any of the preceding LANGIDs, it returns 
	 * any language message string that is present. If that fails, it returns 
	 * ERROR_RESOURCE_LANG_NOT_FOUND.
	 * <p>
	 * @param lpBuffer - [out] - A pointer to a buffer that receives the null-terminated string 
	 * that specifies the formatted message. If dwFlags includes FORMAT_MESSAGE_ALLOCATE_BUFFER, 
	 * the function allocates a buffer using the LocalAlloc function, and places the pointer to 
	 * the buffer at the address specified in lpBuffer.<br>
	 * This buffer cannot be larger than 64K bytes.
	 * <p>
	 * @param nSize - [in] - If the FORMAT_MESSAGE_ALLOCATE_BUFFER flag is not set, this 
	 * parameter specifies the size of the output buffer, in TCHARs. 
	 * If FORMAT_MESSAGE_ALLOCATE_BUFFER is set, this parameter specifies the minimum number of 
	 * TCHARs to allocate for an output buffer.<br>
	 * The output buffer cannot be larger than 64K bytes.
	 * <p>
	 * @param arrStrings - [in_opt] - An array of values that are used as insert values in the 
	 * formatted message. A %1 in the format string indicates the first value in the Arguments 
	 * array; a %2 indicates the second argument; and so on. <br>
	 * The interpretation of each value depends on the formatting information associated with 
	 * the insert in the message definition. The default is to treat each value as a pointer to 
	 * a null-terminated string.<br>
	 * By default, the Arguments parameter is of type va_list*, which is a language- and 
	 * implementation-specific data type for describing a variable number of arguments. The 
	 * state of the va_list argument is undefined upon return from the function. To use the 
	 * va_list again, destroy the variable argument list pointer using va_end and reinitialize 
	 * it with va_start.<br>
	 * If you do not have a pointer of type va_list*, then specify the 
	 * FORMAT_MESSAGE_ARGUMENT_ARRAY flag and pass a pointer to an array of 
	 * DWORD_PTR values (STRING pointers);<br> 
	 * those values are input to the message formatted as the insert values. Each insert must 
	 * have a corresponding element in the array.
	 * <p>
	 * @return If the function succeeds, the return value is the number of TCHARs stored in the 
	 * output buffer, excluding the terminating null character.<br>
	 * If the function fails, the return value is zero. To get extended error information, 
	 * call GetLastError.
	 * <p>
	 */
	int FormatMessageA(int dwFlags, Pointer lpSource, int dwMessageId, int dwLanguageId, Pointer lpBuffer, int nSize, Pointer arrStrings);
	
	/**
	 * HANDLE WINAPI OpenProcess(_in  DWORD dwDesiredAccess, _in  BOOL bInheritHandle, _in  DWORD dwProcessId);
	 * <p>
	 * Process Access Rights values:
	 * <p>
	 * Standard:<br><pre>
	 * DELETE (0x00010000L) Required to delete the object. 
	 * READ_CONTROL (0x00020000L) Required to read information in the security descriptor for the 
	 *              object, not including the information in the SACL. To read or write the SACL, 
	 *              you must request the ACCESS_SYSTEM_SECURITY access right. For more 
	 *              information, see MSDN SACL Access Right. 
	 * SYNCHRONIZE (0x00100000L) The right to use the object for synchronization. This enables a 
	 *              thread to wait until the object is in the signaled state. 
	 * WRITE_DAC (0x00040000L) Required to modify the DACL in the security descriptor for the object. 
	 * WRITE_OWNER (0x00080000L) Required to change the owner in the security descriptor for the object.
	 *  
	 * Process-specific Rights:<br>
	 * PROCESS_ALL_ACCESS All possible access rights for a process object.
	 *            Windows Server 2003 and Windows XP/2000:  The size of the PROCESS_ALL_ACCESS flag 
	 *            increased on Windows Server 2008 and Windows Vista. If an application compiled for 
	 *            Windows Server 2008 and Windows Vista is run on Windows Server 2003 or 
	 *            Windows XP/2000, the PROCESS_ALL_ACCESS flag is too large and the function 
	 *            specifying this flag fails with ERROR_ACCESS_DENIED. To avoid this problem, 
	 *            specify the minimum set of access rights required for the operation. 
	 *            If PROCESS_ALL_ACCESS must be used, set _WIN32_WINNT to the minimum operating 
	 *            system targeted by your application 
	 *            (for example, #define _WIN32_WINNT _WIN32_WINNT_WINXP). 
	 *            For more information, see Using the Windows Headers.  
	 * PROCESS_CREATE_PROCESS (0x0080) Required to create a process. 
	 * PROCESS_CREATE_THREAD (0x0002) Required to create a thread. 
	 * PROCESS_DUP_HANDLE (0x0040) Required to duplicate a handle using DuplicateHandle. 
	 * PROCESS_QUERY_INFORMATION (0x0400) Required to retrieve certain information about a process, 
	 *            such as its token, exit code, and priority class (see OpenProcessToken, 
	 *            GetExitCodeProcess, GetPriorityClass, and IsProcessInJob). 
	 * PROCESS_QUERY_LIMITED_INFORMATION (0x1000) Required to retrieve certain information about a 
	 *            process (see QueryFullProcessImageName). A handle that has the 
	 *            PROCESS_QUERY_INFORMATION access right is automatically granted 
	 *            PROCESS_QUERY_LIMITED_INFORMATION.
	 *            Windows Server 2003 and Windows XP/2000:  This access right is not supported. 
	 * PROCESS_SET_INFORMATION (0x0200) Required to set certain information about a process, such as 
	 *            its priority class (see SetPriorityClass). 
	 * PROCESS_SET_QUOTA (0x0100) Required to set memory limits using SetProcessWorkingSetSize. 
	 * PROCESS_SUSPEND_RESUME (0x0800) Required to suspend or resume a process. 
	 * PROCESS_TERMINATE (0x0001) Required to terminate a process using TerminateProcess. 
	 * PROCESS_VM_OPERATION (0x0008) Required to perform an operation on the address space of a 
	 *            process (see VirtualProtectEx and WriteProcessMemory). 
	 * PROCESS_VM_READ (0x0010) Required to read memory in a process using ReadProcessMemory. 
	 * PROCESS_VM_WRITE (0x0020) Required to write to memory in a process using WriteProcessMemory. 
	 * SYNCHRONIZE (0x00100000L) Required to wait for the process to terminate using the wait 
	 *            functions. 
	 * <p>
	 * <b>NOTE: Protected Processes:</b><br>
	 * Windows Vista introduces protected processes to enhance support for Digital Rights Management. 
	 * The system restricts access to protected processes and the threads of protected processes.
	 * The following standard access rights are not allowed from a process to a protected process:
	 * <p>
	 * DELETE, 
	 * READ_CONTROL, 
	 * WRITE_DAC, 
	 * WRITE_OWNER, 
	 * <p>
	 * The following specific access rights are not allowed from a process to a protected process:
	 * <p>
	 * PROCESS_ALL_ACCESS, 
	 * PROCESS_CREATE_PROCESS, 
	 * PROCESS_CREATE_THREAD, 
	 * PROCESS_DUP_HANDLE, 
	 * PROCESS_QUERY_INFORMATION, 
	 * PROCESS_SET_INFORMATION, 
	 * PROCESS_SET_QUOTA, 
	 * PROCESS_VM_OPERATION, 
	 * PROCESS_VM_READ, 
	 * PROCESS_VM_WRITE. 
	 * <p>
	 * The PROCESS_QUERY_LIMITED_INFORMATION right was introduced to provide access to a subset of 
	 *        the information available through PROCESS_QUERY_INFORMATION.
	 * <p></pre>
	 * @param dwDesiredAccess -- The access to the process object. This access right is 
	 *        checked against the security descriptor for the process. This parameter 
	 *        can be one or more of the process access rights.   
     *        If the caller has enabled the SeDebugPrivilege privilege, the requested 
     *        access is granted regardless of the contents of the security descriptor.
	 * @param bInheritHandle --  If this value is TRUE, processes created by this 
	 *        process will inherit the handle. Otherwise, the processes do not inherit 
	 *        this handle.  Should almost always be FALSE?.
	 * @param dwProcessId  -- The identifier of the local process to be opened. 
	 *        If the specified process is the System Process (0x00000000), the function 
	 *        fails and the last error code is ERROR_INVALID_PARAMETER. If the specified 
	 *        process is the Idle process or one of the CSRSS processes, this function 
	 *        fails and the last error code is ERROR_ACCESS_DENIED because their access 
	 *        restrictions prevent user-level code from opening them.
	 * @return Handle to specified process or NULL
	 */
	Pointer OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId);

	/**
	 * BOOL WINAPI CloseHandle(_in  HANDLE hObject);
	 * <p>
	 * Releases the HANDLE references and resources from any HANDLE previously provided by the 
	 * system of these types of objects:
	 * <p>
	 * Access token, 
	 * Communications device, 
	 * Console input, 
	 * Console screen buffer, 
	 * Event, 
	 * File, 
	 * File mapping, 
	 * I/O completion port, 
	 * Job, 
	 * Mailslot, 
	 * Memory resource notification, 
	 * Mutex, 
	 * Named pipe, 
	 * Pipe, 
	 * Process, 
	 * Semaphore, 
	 * Thread, 
	 * Transaction, 
	 * Waitable timer.
	 * <p>
	 * @param handle -- a HANDLE retrieved via a supported function requiring CloseHandle when done.
	 * @return -- If the function succeeds, the return value is nonzero.
	 *            If the function fails, the return value is zero. To get extended error information, 
	 *            call GetLastError.
	 *            If the application is running under a debugger, the function will throw an exception if 
	 *            it receives either a handle value that is not valid or a pseudo-handle value. This can 
	 *            happen if you close a handle twice, or if you call CloseHandle on a handle returned by 
	 *            the FindFirstFile function instead of calling the FindClose function.
	 */
	boolean CloseHandle(Pointer handle);	

	/**
	 * DWORD WINAPI GetProcessImageFileName(_in HANDLE hProcess, _out LPTSTR lpImageFileName, _in DWORD nSize);
	 * <p>
	 * Retrieves the name of the executable file for the specified process.<br>
	 * For Windows 7, Windows Server 2008 R2<br>
	 * Use the Psapi version for earlier versions of Windows.
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
	 * @see #OpenProcess(int, boolean, int)
	 * @see #CloseHandle(Pointer)
	 * @see Psapi#GetProcessImageFileNameA(Pointer, Pointer, int)
	 * @see org.safs.natives.NativeWrapper#GetProcessFileName(Object)
	 */
	int GetProcessImageFileNameA(Pointer hProcess, Pointer pImageFilename, int nSize);
	
	
	
	/**
	 * HMODULE WINAPI GetModuleHandle(  __in_opt  LPCTSTR lpModuleName );
	 * <p>
	 * Retrieves a module handle for the specified module. The module must have been loaded by the calling process.
	 * <p>
	 * @param lpmoduleName -- The name of the loaded module (either a .dll or .exe file). If this parameter is NULL,
	 *                  GetModuleHandle returns a handle to the file used to create the calling process (.exe file).
	 * @return a handle to the specified module
	 */
	Pointer GetModuleHandleA(Pointer lpmoduleName);
	
	/**
	 * VOID WINAPI Sleep(__in  DWORD dwMilliseconds)
	 * <p>Suspends the execution of the current thread until the time-out interval elapses.<p>
	 * @param ms -- The time interval for which execution is to be suspended, in milliseconds.
	 */
	void Sleep(int ms);

	/**
	 * <pre>
	 * Creates or opens a file or I/O device. The most commonly used I/O devices are as follows:
	 * file, file stream, directory, physical disk, volume, console buffer, tape drive, communications
	 * resource, mailslot, and pipe. The function returns a handle that can be used to access the 
	 * file or device for various types of I/O depending on the file or device and the flags and attributes specified.
	 * 
	 * HANDLE WINAPI CreateFile(
	 * __in      LPCTSTR lpFileName,
	 * __in      DWORD dwDesiredAccess,
	 * __in      DWORD dwShareMode,
	 * __in_opt  LPSECURITY_ATTRIBUTES lpSecurityAttributes,
	 * __in      DWORD dwCreationDisposition,
	 * __in      DWORD dwFlagsAndAttributes,
	 * __in_opt  HANDLE hTemplateFile
	 * );
	 * 
	 * @param lpFileName [in] 
	 * The name of the file or device to be created or opened.
	 * @param dwDesiredAccess [in]
	 * The requested access to the file or device, which can be summarized as read, write, both or neither zero).
	 * If this parameter is zero, the application can query certain metadata such as file, directory,
	 * or device attributes without accessing that file or device, even if GENERIC_READ access would have been denied.
	 * @param dwShareMode [in]
	 * The requested sharing mode of the file or device, which can be read, write, both, delete, all of these, or none
	 * @param lpSecurityAttributes [in, optional]
	 * This parameter can be NULL.
	 * @param dwCreationDisposition [in]
	 * An action to take on a file or device that exists or does not exist.
	 * @param dwFlagsAndAttributes [in]
	 * The file or device attributes and flags, FILE_ATTRIBUTE_NORMAL being the most common default value for files.
	 * @param hTemplateFile [in, optional]
	 * This parameter can be NULL.
	 * 
	 * For parameters: dwDesiredAccess, dwShareMode, dwCreationDisposition and dwFlagsAndAttributes, Please see
	 * the constants definition at the beginning of this class.
	 * </pre>
	 */
	public Pointer CreateFileA(
			String lpFileName, int dwDesiredAccess, int dwShareMode,
			SecurityAttribute lpSecurityAttributes, int dwCreationDisposition,
			int dwFlagsAndAttributes, Pointer hTemplateFile);
	
	public Pointer CreateFileW(
			String lpFileName, int dwDesiredAccess, int dwShareMode,
			SecurityAttribute lpSecurityAttributes, int dwCreationDisposition,
			int dwFlagsAndAttributes, Pointer hTemplateFile);
	
	public boolean Wow64EnableWow64FsRedirection(boolean enable);
	
	/**
	 * <pre>
	 * The SECURITY_ATTRIBUTES structure contains the security descriptor for an object 
	 * and specifies whether the handle retrieved by specifying this structure is inheritable.
	 * This structure provides security settings for objects created by various functions, 
	 * such as CreateFile, CreatePipe, CreateProcess, RegCreateKeyEx, or RegSaveKeyEx.
	 * 
	 * typedef struct _SECURITY_ATTRIBUTES {
	 *   DWORD  nLength;
	 *   LPVOID lpSecurityDescriptor;
	 *   BOOL   bInheritHandle;
	 * } SECURITY_ATTRIBUTES, *PSECURITY_ATTRIBUTES, *LPSECURITY_ATTRIBUTES;
	 */
	public class SecurityAttribute extends DefaultStructure{
		public int nLength;
		public Pointer lpSecurityDescriptor;
		public boolean bInheritHandle;
	}
	
	/**
	 *  <pre>
	 *  {@literal
	 *   Retrieves the date and time that a file or directory was created, last accessed, and last modified.
	 *   BOOL WINAPI GetFileTime(
	 *   __in       HANDLE hFile,
	 *   __out_opt  LPFILETIME lpCreationTime,
	 *   __out_opt  LPFILETIME lpLastAccessTime,
	 *   __out_opt  LPFILETIME lpLastWriteTime
	 *   ); 
	 *  }
	 *   
	 *@param  hFile [in] 
	 *  A handle to the file or directory for which dates and times are to be retrieved.
	 *  The handle must have been created using the CreateFile function with the GENERIC_READ access 
	 *  right. For more information, see File Security and Access Rights.
	 *
	 *@param  lpCreationTime [out, optional]
	 *  A pointer to a FILETIME structure to receive the date and time the file or directory was created.
	 *  This parameter can be NULL if the application does not require this information.
	 *
	 *@param  lpLastAccessTime [out, optional]
	 *  A pointer to a FILETIME structure to receive the date and time the file or directory was last accessed.
	 *  The last access time includes the last time the file or directory was written to, read from, or, 
	 *  in the case of executable files, run. This parameter can be NULL if the application does not require this information.
	 *
	 *@param  lpLastWriteTime [out, optional]
	 *  A pointer to a FILETIME structure to receive the date and time the file or directory was last 
	 *  written to, truncated, or overwritten (for example, with WriteFile or SetEndOfFile). 
	 *  This date and time is not updated when file attributes or security descriptors are changed. 
	 *  This parameter can be NULL if the application does not require this information
	 *
 	 *@return A boolean value to indicate if this function succeeds
	 *  To get extended error information, call GetLastError. 
	 *
	 *</pre>
	 */

	public boolean GetFileTime(Pointer hFile, FileTime lpCreationTime,FileTime lpLastAccessTime,FileTime lpLastWriteTime );
	
	/**
	 * <pre>
	 * Contains a 64-bit value representing the number of 100-nanosecond intervals since January 1, 1601 (UTC).
	 * typedef struct _FILETIME {
	 *   DWORD dwLowDateTime;
	 *   DWORD dwHighDateTime;
	 * } FILETIME, *PFILETIME;
	 * 
	 * dwLowDateTime 	The low-order part of the file time.
	 * dwHighDateTime 	The high-order part of the file time.
	 * 1, 000, 000 nanoseconds = 1 millisecond
	 *</pre>
	 */
	public class FileTime extends DefaultStructure{
		public int dwLowDateTime;
		public int dwHighDateTime;
	}
}