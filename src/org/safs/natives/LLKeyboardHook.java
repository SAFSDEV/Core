package org.safs.natives;

import org.safs.natives.win32.User32;
import org.safs.natives.LLKeyboardHookListener;
import org.safs.natives.win32.User32.KBDLLHOOKSTRUCT;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

/**
 * Hook class for intercepting low-level keyboard events.
 * <p>Usage:
 * <br>1) define a class that implements interface LLKeyboardHookListener
 * <br>2) to intercept keyboard events in the class, implement: public void onLLKeyboardHook(int nCode, NativeLong wParam, KBDLLHOOKSTRUCT info)
 * <br>3) use LLKeyboardHook in the class
 *        <li>LLKeyboardHook llkeyhook = new LLKeyboardHook();  
 *        <li>llkeyhook.addListener(this);
 * 	      <li>llkeyhook.run();
 * <p> 	
 * @see  org.safs.natives.test.hookTest 
 * @author JunwuMa
 * 
 * <br>OCT 21, 2010 JunwuMa Initial Release
 */
public class LLKeyboardHook extends AbstractHook {
	/**
	 * the type of hook for a hook procedure that monitors low-level keyboard events
	 */
	static public final int  WH_KEYBOARD_LL = 13;
  
	public LLKeyboardHook(){
		super("low-level Keyboard");
		setHook(WH_KEYBOARD_LL, callbackProc());
	}
	
    public boolean addListener(CallbackHookListener listener) {
    	if (listener == null || !(listener instanceof LLKeyboardHookListener)) 
    		return false;
    	else {
    		return super.addListener((LLKeyboardHookListener)listener); // ensure listener must be a KeyboardHookListener
    	}	
    }
    
    /**
     * define the callback procedure for hook type WH_KEYBOARD_LL
     * @return the procedure 
     */
    private StdCallCallback callbackProc() {
        return new User32.LLKeyBoardCallBack() {
	            public Pointer callback(int nCode, NativeLong wParam, KBDLLHOOKSTRUCT lParam) { 
	            	// call listeners
	            	for (int i=0; i<listeners.size(); i++){
	            		CallbackHookListener tempListener = listeners.get(i);
	            		if (tempListener instanceof LLKeyboardHookListener)
	            			((LLKeyboardHookListener) tempListener).onLLKeyboardHook(nCode, wParam, lParam);
	            	}
    				return USER32INST.CallNextHookEx(null, nCode, wParam, lParam.getPointer());
	            }
        };    
	}
}	