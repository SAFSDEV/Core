package org.safs.natives;

import org.safs.natives.win32.User32.KBDLLHOOKSTRUCT;
import com.sun.jna.NativeLong;
/**
 * a Listener to receive low level Keyboard messages for hook WH_KEYBOARD_LL, LowLevelKeyboardProc 
 * @author JunwuMa
 * @since OCT 21 2010
 */
public interface LLKeyboardHookListener extends CallbackHookListener {
	public void onLLKeyboardHook(int nCode, NativeLong wParam, KBDLLHOOKSTRUCT info);
}