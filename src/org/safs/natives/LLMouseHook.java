package org.safs.natives;

import org.safs.natives.win32.User32;
import org.safs.natives.win32.User32.MSLLHOOKSTRUCT;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

/**
 * Hook class for intercepting low-level mouse events.
 * <p>Usage:
 * <br>1) define a class that implements interface LLMouseHookListener
 * <br>2) to intercept mouse events in the class, implement: public void onLLMouseHook(int nCode, NativeLong wParam, MSLLHOOKSTRUCT info)
 * <br>3) use MouseHook in the class
 *        <li>LLMouseHook llmousehook = new LLMouseHook();  
 *        <li>llmousehook.addListener(this);
 * 	      <li>llmousehook.run();
 * <p> 	
 * 
 * @see  org.safs.natives.test.hookTest  
 * @author JunwuMa
 * <br>OCT 21, 2010 JunwuMa Initial Release
 */
public class LLMouseHook extends AbstractHook {
	/**
	 * the type of hook for a hook procedure that monitors low-level mouse events
	 */
	public static final int WH_MOUSE_LL = 14;       
   
	public LLMouseHook(){
		super("low-level MouseHook");
		setHook(WH_MOUSE_LL, callbackProc());
	}
	
    public boolean addListener(CallbackHookListener listener) {
    	if (listener == null || !(listener instanceof LLMouseHookListener)) 
    		return false;
    	else 
    		return super.addListener((LLMouseHookListener)listener); // ensure listener must be a MouseHookListener
    	
    }
	
    private StdCallCallback callbackProc() {
        return new User32.LLMouseCallBack() {
	            public Pointer callback(int nCode, NativeLong wParam, MSLLHOOKSTRUCT lParam) { 
	            	// call listeners
	            	for (int i=0; i<listeners.size(); i++){
	            		CallbackHookListener tempListener = listeners.get(i);
	            		if (tempListener instanceof LLMouseHookListener)
	            			((LLMouseHookListener)tempListener).onLLMouseHook(nCode, wParam, lParam);
	            	}
    				return USER32INST.CallNextHookEx(null, nCode, wParam, lParam.getPointer());
	            }
        };    
	}
}