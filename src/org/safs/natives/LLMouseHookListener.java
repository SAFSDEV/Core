package org.safs.natives;

import org.safs.natives.win32.User32.MSLLHOOKSTRUCT;
import com.sun.jna.NativeLong;

/**
 * a Listener to receive low level Mouse messages for hook WH_MOUSE_LL, LowLevelMouseProc 
 * @author JunwuMa
 * @since OCT 21 2010
 */
public interface LLMouseHookListener extends CallbackHookListener {
	public void onLLMouseHook(int nCode, NativeLong wParam, MSLLHOOKSTRUCT info);
}