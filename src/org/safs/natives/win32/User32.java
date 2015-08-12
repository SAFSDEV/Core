package org.safs.natives.win32;

import java.util.Vector;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
/**
 * A JNA Library definition for the Windows user32.dll
 * <p>
 * <a href="http://jna.dev.java.net" target="_blank">JNA Home Page</a>
 * @author Carl Nagle
 * @author Carl Nagle  Jun 04, 2009  Added GetGuiResources API interface.  
 * @author JunwuMa Oct 21, 2010  Adding API interfaces for using low-level Mouse/Keyboard callback hooks. 
 * @since 2009.02.03
 * @see org.safs.natives.NativeWrapper
 */
public interface User32 extends W32APIOptions, StdCallLibrary {
	
	User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class, DEFAULT_OPTIONS);
	
	int SW_SHOW = 1;
	
	/**
	 * HWND GetForegroundWindow(VOID)
	 * The GetForegroundWindow function returns a handle to the foreground window 
	 * (the window with which the user is currently working).
	 * <p>
	 * The user should not normally call this method directly, but should try to call the NativeWrapper 
	 * library instead. 
	 * @return HWND wrapped in a JNA NativeLong
	 * @see org.safs.natives.NativeWrapper#GetForegroundWindow()
	 */
	NativeLong GetForegroundWindow();

	/**
	 * Show window on desktop 	
	 * @param hWnd
	 * @param nCmdShow
	 * @return boolean 
	 * @see org.safs.natives.NativeWrapper#SetForegroundWindow(String)
	 */
	boolean ShowWindow(NativeLong hWnd, int nCmdShow);
		
	/**
	 * HWND GetDesktopWindow(VOID)
	 * The GetDesktopWindow function returns a handle to the main Desktop window 
	 * (the window on which all other windows are painted).
	 * <p>
	 * The user should not normally call this method directly, but should try to call the NativeWrapper 
	 * library instead. 
	 * @return HWND wrapped in a JNA NativeLong
	 * @see org.safs.natives.NativeWrapper#GetDesktopWindow()
	 */
	NativeLong GetDesktopWindow();

	/**
	 * The EnumWindows function enumerates all top-level windows on the screen 
	 * by passing the handle to each window, in turn, to an application-defined 
	 * callback function. EnumWindows continues until the last top-level window 
	 * is enumerated or the callback function returns FALSE. 
	 * @param lpEnumFunc instance of callback function WNDENUMPROC
	 * @param arg
	 * @return 
	 */			
	boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer arg);
    
	/**
	 * The EnumChildWindows function enumerates all child windows of the parent window 
	 * by passing the handle to each child window, in turn, to an application-defined 
	 * callback function. EnumChildWindows continues until the last child window 
	 * is enumerated or the callback function returns FALSE. 
	 * @param Handle to parent window
	 * @param lpEnumFunc instance of callback function WNDENUMPROC
	 * @param arg
	 * @return 
	 */			
	boolean EnumChildWindows(NativeLong parent, WNDENUMPROC lpEnumFunc, Pointer arg);
    
	/**
	 * Callback Function used for User32 EnumWindows 
	 * @author Carl Nagle
	 */
	interface WNDENUMPROC extends StdCallCallback {
        /** Return whether to continue enumeration. */
        boolean callback(NativeLong hWnd, Pointer arg);
        public Vector handles = new Vector();
    }
	
	/**
	 * The GetWindowThreadProcessId function retrieves the identifier of the thread that 
	 * created the specified window and, optionally, the identifier of the process that 
	 * created the window. 
	 * @param hWnd -- [in] Handle to the window. 
	 * @param pidOut -- [out] Pointer to a variable that receives the process identifier. 
	 * If this parameter is not NULL, GetWindowThreadProcessId copies the identifier of 
	 * the process to the variable; otherwise, it does not.
	 * @return -- The return value is the identifier of the thread that created the window.
	 */
	int GetWindowThreadProcessId(NativeLong hWnd, Pointer pidOut);

	/**
	 * DWORD WINAPI GetGuiResources(_in  HANDLE hProcess, _in  DWORD uiFlags);
	 * <p>
	 * Retrieves the count of handles of GUI objects used by the specified process.
	 * <p>
	 *  We usually want to get USER objects.  These are:
	 *        Accelerator table -- Keyboard Accelerators, 
	 *        Caret -- Carets, 
	 *        Cursor -- Cursors, 
	 *        DDE conversation -- Dynamic Data Exchange Management Library, 
	 *        Hook -- Hooks, 
	 *        Icon -- Icons, 
	 *        Menu -- Menus, 
	 *        Window -- Windows, 
	 *        Window position -- Windows.
	 * <p>        
	 * GDI objects:
	 *        Bitmap -- Bitmaps, 
	 *        Brush -- Brushes, 
	 *        DC -- Device Contexts, 
	 *        Enhanced metafile -- Metafiles, 
	 *        Enhanced-metafile DC -- Metafiles, 
	 *        Font -- Fonts and Text, 
	 *        Memory DC -- Device Contexts, 
	 *        Metafile -- Metafiles, 
	 *        Metafile DC -- Metafiles, 
	 *        Palette -- Colors, 
	 *        Pen and extended pen -- Pens, 
	 *        Region -- Regions. 
	 * <p>        
	 * @param hProcess - HANDLE received from OpenProcess
	 * @param uiFlags -- 0= get count of GDI objects. 1=count of USER objects.
	 * @return
	 */
	int GetGuiResources(Pointer hProcess, int uiFlags);	
	
	
	/**
	 * typedef struct tagPOINT {
  	 *		LONG x;
  	 *		LONG y;
	 *		} POINT, *PPOINT;
	 * <p>
	 * The POINT structure defines the x- and y- coordinates of a point. Used for MSG.
	 * <p>
	 */
	public class POINT extends Structure {
		public NativeLong x;
        public NativeLong y;  //should be NativeLong for matching Long
	}
	
	/**
	 * typedef struct tagMSG {
  	 *	HWND   hwnd;
  	 *	UINT   message;
  	 *	WPARAM wParam;
  	 *  LPARAM lParam;
  	 *	DWORD  time;
  	 *	POINT  pt;
	 *	} MSG
	 * <p>Contains message information from a thread's message queue.<p> 
	 *
	 */
	public class MSG extends Structure {
		public NativeLong  hwnd;   
		public int   message; 
		public NativeLong wParam;  
		public NativeLong lParam;
		public int  time;
		public POINT  pt;  	
	}
	
	/**
	 * Refer to MSDN:
	 * Contains information about a mouse event passed to a WH_MOUSE hook procedure
	 * typedef struct tagMOUSEHOOKSTRUCT {
  	 * 		POINT     pt;
  	 *		HWND      hwnd;
  	 *		UINT      wHitTestCode;
  	 *		ULONG_PTR dwExtraInfo;
	 *	} MOUSEHOOKSTRUCT, *PMOUSEHOOKSTRUCT, *LPMOUSEHOOKSTRUCT;
	 */
    public class MOUSEHOOKSTRUCT extends Structure {
        public POINT pt;
        public NativeLong hwnd; //HWND public NativeLong hwnd; //HWND
        public int wHitTestCode;
        public  Pointer dwExtraInfo;  // Pointer dwExtraInfo
    }
    /**
     * Contains information about a low-level mouse input event.
     * typedef struct tagMSLLHOOKSTRUCT {
  	 * 		POINT     pt;
  	 *		DWORD     mouseData;
  	 *		DWORD     flags;
  	 *		DWORD     time;
     *		ULONG_PTR dwExtraInfo;
	 *	} MSLLHOOKSTRUCT, *PMSLLHOOKSTRUCT, *LPMSLLHOOKSTRUCT;
	 *
     */
    public class MSLLHOOKSTRUCT extends Structure {
        public POINT pt;
        public int mouseData; //HWND public NativeLong hwnd; //HWND
        public int flags;
        public int time;
        public  Pointer dwExtraInfo;  
    }
	/**
	 * define Hook structure about a low-level keyboard input event.   
	 * refer to KBDLLHOOKSTRUCT in MSDN
	 * typedef struct tagKBDLLHOOKSTRUCT {
  	 * DWORD     vkCode;
  	 * DWORD     scanCode;
  	 * DWORD     flags;
  	 * DWORD     time;
  	 * ULONG_PTR dwExtraInfo;
	 * } KBDLLHOOKSTRUCT, *PKBDLLHOOKSTRUCT, *LPKBDLLHOOKSTRUCT;
	 *
	 */
	public class KBDLLHOOKSTRUCT extends Structure {
        public int vkCode;
        public int scanCode;
        public int flags;
        public int time;
        public Pointer dwExtraInfo;
    }

    public class CWPSTRUCT extends Structure {
		public NativeLong lParam;
		public NativeLong wParam;  
		public int   message; 
		public NativeLong  hwnd;   
    }	
	/**
	 * Refer to MSDN:
	 * HHOOK WINAPI SetWindowsHookEx(__in  int idHook,__ *in  HOOKPROC lpfn,__in  HINSTANCE hMod,__in  DWORD dwThreadId);
	 * <p>
	 * Installs an application-defined hook procedure into a hook chain. You would install a hook procedure to 
	 * monitor the system for certain types of events. These events are associated either with a specific thread or
	 *  with all threads in the same desktop as the calling thread.
	 * <p>
	 * @param idHook -- the type of hook procedure to be installed.
	 * @param lpfn -- a pointer to the hook procedure.
	 * @param hMod -- a handle to the DLL containing the hook procedure pointed to by the lpfn parameter. The hMod parameter 
	 *             must be set to NULL if the dwThreadId parameter specifies a thread created by the current process and 
	 *             if the hook procedure is within the code associated with the current process.
	 * @param dwThreadId -- the identifier of the thread with which the hook procedure is to be associated. If this
	 *             parameter is zero, the hook procedure is associated with all existing threads running in the 
	 *             same desktop as the calling thread.
	 * @return the return value is the handle to the hook procedure if the function succeeds.
	 */
	Pointer SetWindowsHookExA(int idHook, StdCallCallback lpfn, Pointer hMod, int dwThreadId);
	Pointer SetWindowsHookExW(int idHook, StdCallCallback lpfn, Pointer hMod, int dwThreadId);
	
	/**
	 * Refer to MSDN: BOOL WINAPI UnhookWindowsHookEx(__in  HHOOK hhk)
	 * <p>Removes a hook procedure installed in a hook chain by the SetWindowsHookEx function. <p>
	 * 
	 * @param hProcess -- A handle to the hook to be removed. This parameter is a hook handle obtained by a 
	 *                    previous call to SetWindowsHookEx. 
	 * @return 0 if fails; non  zero if succeeds.
	 */
	boolean UnhookWindowsHookEx(Pointer hProcess);
	/**
	 * Refer to MSDN:
	 * LRESULT WINAPI CallNextHookEx(_in_opt HHOOK hhk, _in int nCode, _in WPARAM wParam, _in LPARAM lParam);
	 * <p>
	 * Passes the hook information to the next hook procedure in the current hook chain. 
	 * A hook procedure can call this function either before or after processing the hook information
	 * <p>
	 * @param hhk -- optional, this parameter is ignored. 
	 * @param nCode -- hook code,next hook procedure uses this code to determine how to process the hook information.
	 * @param wParam -- the wParam value, its meaning depends on the type of hook associated with the current hook chain.
	 * @param lParam -- the lParam value, its meaning depends on the type of hook associated with the current hook chain.
	 * @return a internal value returned by the next hook procedure in the chain;current hook procedure must also return this value.
	 */
	Pointer CallNextHookEx(Pointer hhk, int nCode, NativeLong wParam, Pointer lParam);
	
    /**
     * Callback function interface to LowLevelKeyboardProc for hook type WH_KEYBOARD_LL
     * @author Junwu Ma
     */
	interface LLKeyBoardCallBack extends StdCallCallback  {
    	Pointer callback(int nCode, NativeLong wParam, KBDLLHOOKSTRUCT lParam);
    }


	/**
	 * Callback function interface to LowLevelMouseProc for hook type WH_MOUSE_LL
	 * @author Junwu Ma
	 */
    interface LLMouseCallBack extends StdCallCallback {
        Pointer callback(int nCode, NativeLong wParam, MSLLHOOKSTRUCT lParam);
    }

	/**
	 * Callback function interface to MouseProc for hook type WH_MOUSE
	 * 
	 */
    interface MouseCallBack extends StdCallCallback {
        Pointer callback(int nCode, NativeLong wParam, MOUSEHOOKSTRUCT lParam);
    }
	/**
	 * Callback function interface to KeyboardProc for hook type WH_KEYBOARD
	 * 
	 */    
    interface KeyBoardCallBack extends StdCallCallback {
        Pointer callback(int nCode, NativeLong wParam, NativeLong lParam);
    }
	/**
	 * Callback function interface to GetMsgProc for hook type WH_GETMESSAGE
	 * 
	 */    
    interface GetMsgProcCallBack extends StdCallCallback {
        Pointer callback(int nCode, NativeLong wParam, MSG lParam);
    }
	/**
	 * Callback function interface to CallWndProc for hook type WH_CALLWNDPROC
	 * 
	 */
    interface CallWndProcCallBack extends StdCallCallback {
        Pointer callback(int nCode, NativeLong wParam, CWPSTRUCT lParam);
    }
	/**
	 * Refer to MSDN: 
     * BOOL WINAPI GetMessage(__out LPMSG lpMsg,__in_opt  HWND hWnd, __in UINT wMsgFilterMin, __in UINT wMsgFilterMax);
	 * <p>
	 * Retrieves a message from the calling thread's message queue. The function dispatches incoming sent messages
	 * until a posted message is available for retrieval.
	 * <p>
	 * @param pMsg -- A pointer to an MSG structure that receives message information from the thread's message queue.
	 * @param hwnd -- A handle to the window whose messages are to be retrieved. The window must belong to the current thread.
	 *              If this value is NULL, this method obtains messages for any window that belongs to the calling thread.
	 * @param wMsgFilterMin -- the lowest message value obtained.
	 * @param wMsgFilterMax -- the lowest message value obtained.
	 *                         If wMsgFilterMin and wMsgFilterMax are both zero, this method returns all available messages; 
	 *                         that is, no range filtering is performed.
	 * @return 0 if retrieves WM_QUIT; nonzero if other than WM_QUIT. 
	 */
	int GetMessageA(MSG pMsg, NativeLong hwnd, int wMsgFilterMin, int wMsgFilterMax);
	
	/**
	 * Translates virtual-key messages into character messages.
	 * @param msg -- A pointer to an MSG structure that contains message information retrieved from the calling 
	 *            thread's message queue by using the GetMessage or PeekMessage function.
	 * @return nonzero if the message is translated (that is, a character message is posted to the thread's message queue).
	 *         0 if the message is not translated. 
	 */
	boolean TranslateMessage(MSG msg);
	
	/**
	 * Dispatches a message to a window procedure. It is typically used to dispatch a message 
	 * retrieved by the GetMessage function. 
	 * @param msg -- A pointer to a structure that contains the message.
	 * @return The return value specifies the value returned by the window procedure. Although its meaning depends
	 *         on the message being dispatched, the return value generally is ignored.
	 */
	Pointer DispatchMessageA(MSG msg);
	
	/**
	 * SHORT WINAPI GetKeyState(__in  int nVirtKey)
	 * <p>
	 * Retrieves the status of the specified virtual key. The status specifies whether the key is up, down, or 
	 * toggled (on, off-alternating each time the key is pressed.
	 * <p>
	 * @param vkcode -- A virtual key.
	 * @return The return value specifies the status of the specified virtual key. (see details in MSDN)
	 */
	short GetKeyState(int vkcode);
	
	/**
	 *	int WINAPI GetWindowText(
  	 *	__in   HWND hWnd,
  	 *	__out  LPTSTR lpString,
  	 *	__in   int nMaxCount
	 *	);
	 *
	 */
	int GetWindowTextA(NativeLong hWnd, Pointer lpString, int nMaxCount);
	
	/**
	 *	int WINAPI GetWindowText(
  	 *	__in   HWND hWnd,
  	 *	__out  LPTSTR lpString,
  	 *	__in   int nMaxCount
	 *	);
	 *
	 */
	int GetWindowTextA(NativeLong hWnd, byte[] lpString, int nMaxCount);
	
	/**
	 * 
	 * @param hWnd	A handle to the window whose menu handle is to be retrieved.
	 * @return		The return value is a handle to the menu. If the specified window has no menu, the return value is NULL.
	 * 				If the window is a child window, the return value is undefined. 
	 */
	//HMENU WINAPI GetMenu(__in  HWND hWnd);
	NativeLong GetMenu(NativeLong hWnd);
	
	/**
	 * 
	 * @param hMenu		A handle to be tested.
	 * @return			If the handle is a menu handle, the return value is nonzero
	 * 					If the handle is not a menu handle, the return value is zero
	 */
	//BOOL WINAPI IsMenu(__in  HMENU hMenu);
	boolean  IsMenu(NativeLong hMenu);
	
	/**
	 * 
	 * @param hMenu		A handle to the menu to be examined
	 * @return			If the function succeeds, the return value specifies the number of items in the menu
	 * 					If the function fails, the return value is -1
	 */
	//int WINAPI GetMenuItemCount(__in_opt  HMENU hMenu);
	int GetMenuItemCount(NativeLong hMenu);

	/**
	 * 
	 * @param hMenu		A handle to the menu
	 * @param uIDItem	The menu item to be changed, as determined by the uFlag parameter
	 * @param lpString	The buffer that receives the null-terminated string. If the string is as long or longer than lpString, 
	 * 					the string is truncated and the terminating null character is added. 
	 * 					If lpString is NULL, the function returns the length of the menu string
	 * @param nMaxCount	The maximum length, in characters, of the string to be copied. If the string is longer than the maximum 
	 * 					specified in the nMaxCount parameter, the extra characters are truncated. 
	 * 					If nMaxCount is 0, the function returns the length of the menu string
	 * @param uFlag		Indicates how the uId parameter is interpreted. This parameter can be one of the following values
	 * 					MF_BYCOMMAND (0x00000000L) ; MF_BYPOSITION (0x00000400L)
	 * @return			If the function succeeds, the return value specifies the number of characters copied to the buffer,
	 * 					not including the terminating null character.
	 * 					If the function fails, the return value is zero. 
	 * 					If the specified item is not of type MIIM_STRING or MFT_STRING, then the return value is zero.
	 */
	//int WINAPI GetMenuString(__in HMENU hMenu,__in UINT uIDItem,__out_opt LPTSTR lpString,__in int nMaxCount,__in UINT uFlag);
	int GetMenuStringW(NativeLong hMenu, int uIDItem,Pointer lpString,int  nMaxCount,int uFlag);

	/**
	 * 
	 * @param hMenu		A handle to the menu that contains the item whose identifier is to be retrieved
	 * @param nPos		The zero-based relative position of the menu item whose identifier is to be retrieved
	 * @return			The return value is the identifier of the specified menu item. 
	 * 					If the menu item identifier is NULL or if the specified item opens a submenu, the return value is -1
	 */
	//UINT WINAPI GetMenuItemID(__in  HMENU hMenu,__in  int nPos);
	int GetMenuItemID(NativeLong hMenu,int nPos);

	/**
	 * 
	 * @param hMenu		A handle to the menu that contains the menu item whose flags are to be retrieved
	 * @param uId		The menu item for which the menu flags are to be retrieved, as determined by the uFlags parameter
	 * @param uFlags	Indicates how the uId parameter is interpreted. This parameter can be one of the following values
	 * 					MF_BYCOMMAND (0x00000000L) ; MF_BYPOSITION (0x00000400L)
	 * @return			If the specified item does not exist, the return value is -1.
	 * 					If the menu item opens a submenu, the low-order byte of the return value contains 
	 * 					the menu flags associated with the item, and the high-order byte contains the number 
	 * 					of items in the submenu opened by the item. 
	 * 					Otherwise, the return value is a mask (Bitwise OR) of the menu flags.
	 */
	//UINT WINAPI GetMenuState(__in  HMENU hMenu,__in  UINT uId,__in  UINT uFlags);
	int GetMenuState(NativeLong hMenu,int uId,int uFlags);

	/**
	 * 
	 * @param hMenu		A handle to the menu
	 * @param nPos		The zero-based relative position in the specified menu of an item that activates a drop-down menu or submenu
	 * @return			If the function succeeds, the return value is a handle to the drop-down menu or submenu activated by the menu item.
	 * 					If the menu item does not activate a drop-down menu or submenu, the return value is NULL
	 */
	//HMENU WINAPI GetSubMenu(__in  HMENU hMenu,__in  int nPos);
	NativeLong GetSubMenu(NativeLong hMenu, int nPos);

	/**
	 * 
	 * @param hWnd		A handle to the window to be tested
	 * @return			If the window handle identifies an existing window, return true.
	 */
	//BOOL WINAPI IsWindow(__in_opt  HWND hWnd);
	boolean IsWindow(NativeLong hWnd);
}
