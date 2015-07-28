package org.safs.natives;

import org.safs.natives.win32.SysMsgHooker;
import org.safs.natives.win32.User32;
import org.safs.natives.win32.User32.MOUSEHOOKSTRUCT;
import org.safs.natives.win32.User32.MSG;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

/**
 * Hook class for intercepting mouse events.
 * Hook function MouseProc is put in SysMsgHooker.dll for using globally. 
 * <p>Usage:
 * <br>1) define a class that implements interface MouseHookListener
 * <br>2) to intercept mouse events in the class, implement: public void onMouseHook(int nCode, NativeLong wParam, MOUSEHOOKSTRUCT info)
 * <br>3) use MouseHook in the class
 *        <li>MouseHook mousehook = new MouseHook();  
 *        <li>mousehook.addListener(this);
 * 	      <li>mousehook.run();
 * <p> 	
 * 
 * @see  org.safs.natives.test.hookTest  
 * @author JunwuMa
 * <br>NOV 10, 2010 JunwuMa Initial Release
 */
public class MouseHook extends AbstractHook {
	/**
	 * the type of hook for a hook procedure that monitors mouse events
	 */
	public static final int WH_MOUSE = 7;       

	public MouseHook(){
		super("MouseHook");
		//setHook(WH_MOUSE, callbackProc());  done in SysMsgHooker.dll
	}
	
    public boolean addListener(CallbackHookListener listener) {
    	if (listener == null || !(listener instanceof MouseHookListener)) 
    		return false;
    	else 
    		return super.addListener((MouseHookListener)listener); // ensure listener must be a MouseHookListener
    }
	
    public void stop() {
    	if (isHooked) {
    		SysMsgHooker.INSTANCE.removeMouseProcHook();
    		System.out.println(hookname + " unhooked!");
    		isHooked = false;
    	} else 
    		System.out.println(hookname + " not running!");	
    }
    public void run() {
        Thread hook_thread = new Thread(new Runnable() {
            public void run(){
                try {
                	isHooked = SysMsgHooker.INSTANCE.setMouseProcHook();
                	if (isHooked) {
                		System.out.println("mousehook installed!");
                		SysMsgHooker.INSTANCE.setMouseListener(callbackMouseProc());
                	} else {
                		System.err.println("failed to set hook for " + hookname);
                		return;
                	}
                	
                    // message dispatch loop (message pump)
                    MSG msg = new MSG();
                    int bret;
                    while ((bret = USER32INST.GetMessageA(msg, null, 0, 0)) != 0) {
                        if (bret == -1) {
                            // handle the error and possibly exit
                        	break;
                        }else {
                        	USER32INST.TranslateMessage(msg);
                        	USER32INST.DispatchMessageA(msg);
                        }
                        if (!isHooked)
                            break;
                    }
                } catch (Exception e) { 
                	System.err.println("Exception in Hook!" + e.toString());
                }
            }
        });
        // start the thread
        hook_thread.start();
    }
    
    private StdCallCallback callbackMouseProc() {
        return new User32.MouseCallBack() {
	            public Pointer callback(int nCode, NativeLong wParam, MOUSEHOOKSTRUCT lParam) { 
	            	// call listeners
	            	for (int i=0; i<listeners.size(); i++){
	            		CallbackHookListener tempListener = listeners.get(i);
	            		if (tempListener instanceof MouseHookListener)
	            			((MouseHookListener)tempListener).onMouseHook(nCode, wParam, lParam);
	            	}
    				return null;
	            }
        };    
	}
     
}