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
