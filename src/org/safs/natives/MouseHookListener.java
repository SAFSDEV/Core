package org.safs.natives;

import org.safs.natives.win32.User32.MOUSEHOOKSTRUCT;
import com.sun.jna.NativeLong;

/**
 * a Listener to receive Mouse messages for hook WH_MOUSE 
 * @author JunwuMa
 * @since OCT 21 2010
 */
public interface MouseHookListener extends CallbackHookListener {
	public void onMouseHook(int nCode, NativeLong wParam, MOUSEHOOKSTRUCT info);
}