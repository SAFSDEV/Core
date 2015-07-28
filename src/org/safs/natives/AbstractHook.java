package org.safs.natives;

import java.util.ArrayList;

import org.safs.natives.win32.Kernel32;
import org.safs.natives.win32.User32;
import org.safs.natives.win32.User32.MSG;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

/**
 * AbstractHook is an abstract implementation of setting a callback(hook) procedure for Hook mechanism, by which 
 * an application can intercept events in OS, such as messages, mouse actions, and keystrokes.
 * <p>Concrete implementation of specific hook types will extend this class.
 * 
 * @author JunwuMa
 * <br>OCT 21, 2010 JunwuMa Initial Release
 */
public abstract class AbstractHook {

    // messages for Mouse events on Windows OS
	public static final int WM_LBUTTONDOWN   	= 0x0201;
    public static final int WM_LBUTTONUP 	 	= 0x0202;
    public static final int WM_LBUTTONDBLCLK 	= 0x0203;
    public static final int WM_RBUTTONDOWN 		= 0x0204;
    public static final int WM_RBUTTONUP 		= 0x0205;
    public static final int WM_MOUSEMOVE 		= 0x0200;
    public static final int WM_MOUSEHOVER 		= 0x02A1;
    
    // messages for Keyboard events on Windows OS
    public static final int  WM_KEYDOWN 		= 0x0100;
    public static final int  WM_CHAR          	= 0x0102;
    public static final int  WM_SETFOCUS      	= 0x0007;
    public static final int  WM_KILLFOCUS       = 0x0008;

    public static final int  WM_KEYUP 			= 0x0101;
    public static final int  WM_SYSKEYDOWN 		= 0x0104;
    public static final int  WM_SYSKEYUP		= 0x0105;
    public static final int	 WM_UNICHAR   		= 0x0109;



	protected String hookname;
    //handle to the hook procedure, returned by SetWindowsHookEx. 
	protected Pointer hhk = null;       
    //statue of whether the current hook is installed and available
	protected boolean isHooked = false;
    
    //store the type of the hook to be installed 
	protected int	hookId = -1;
    //the pointer to the callback procedure for the hook defined by hookId. The specific proc will be defined in derived classes.
	protected StdCallCallback hookProc = null;
    //JNA library, user32.dll,  various native APIs for Hook mechanism can be called
    protected static User32 USER32INST;
    //containing listeners who wish to listen to the messages sent out from the type of the hook  
    protected ArrayList<CallbackHookListener> listeners = new ArrayList<CallbackHookListener>(1);

    /**
     * add a listener for current Hook, it should be overridden in its derived classes for setting a specific listener.
     * @param listener -- a CallbackHookListener
     * @return true if succeeds; otherwise false
     */
    public boolean addListener(CallbackHookListener listener) {
   		return listeners.add(listener);
    }
    
    /**
     * remove a listener from the listener list
     * @param listener -- a CallbackHookListener
     * @return true if succeeds; otherwise false
     */
    public boolean removeListener(CallbackHookListener listener) {
    	return listeners.remove(listener);
    }
    
    /**
     * set necessary information for a specific hook to be installed 
     * @param id -- the type of hook
     * @param proc -- the callback procedure that should matched the first parameter 
     */
    protected void setHook(int id, StdCallCallback proc) {
    	hookId = id;
    	hookProc = proc;
    }
    
    public boolean isHooked() { return isHooked;  }
    
    public Pointer getHook() {  return hhk; }
    
    public AbstractHook(String name) {
    	hookname = name;
        if (!Platform.isWindows()) { 
            throw new UnsupportedOperationException("Not supported on platforms other than Windows.");
    	}
        USER32INST = User32.INSTANCE;
        Native.setProtected(true);
        listeners.clear();
    }	
    
    /**
     * start the hook
     */
    public void run() {
        Thread hook_thread = new Thread(new Runnable() {
            public void run(){
                try {
                	/* Note: the third parameter CANNOT be set to the address of a virtual procedure in the root class.
                	 * It will take much time to find the real procedure that overrides its super. 
                	 * The searching process in Virtual table sometimes will cause the callback failure. 
                	 * So, this callback procedure should be defined in a derived class, and its address should be
                	 * set to hookProc before execute run().
                	 */
                    if(!isHooked) {
                        hhk = USER32INST.SetWindowsHookExA(
                        		hookId,
                        		hookProc, 
                                Kernel32.INSTANCE.GetModuleHandleA(null),
                                0);
 
                        if (hhk != null)
                        	isHooked = true;
                        else {
                        	System.err.println("failed to set hookProc for " + hookname);
                        	return;
                        }
 
                        System.out.println("hook started for " + hookname);
                      
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
                        
                    } else
                        System.out.println(hookname + " hook is already installed.");
                    
                } catch (Exception e) { 
                	System.err.println("Exception in Hook!" + e.toString());
                }
            }
        });
        // start the thread
        hook_thread.start();
    }
    
    /**
     * stop the hook
     */
    public void stop() {
    	if (hhk != null) {
    		USER32INST.UnhookWindowsHookEx(hhk);
        	isHooked = false;
        	System.out.println(hookname + " unhooked!");
    	} else {
    		System.out.println("no running hook to stop!");
    	}
    }
}