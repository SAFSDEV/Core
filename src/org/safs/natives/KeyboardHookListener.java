package org.safs.natives;

import org.safs.natives.win32.User32.KBDLLHOOKSTRUCT;
import com.sun.jna.NativeLong;
/**
 * a Listener to receive Keyboard message for hook WH_KEYBOARD 
 * @author JunwuMa
 * @since OCT 21 2010
 */
public interface KeyboardHookListener extends CallbackHookListener {
	public void onKeyboardHook(int nCode, NativeLong wParam, NativeLong lParam);
}