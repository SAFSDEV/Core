/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.safs.natives;

import org.safs.natives.win32.SysMsgHooker;
import org.safs.natives.win32.User32;
import org.safs.natives.KeyboardHookListener;
import org.safs.natives.win32.User32.KBDLLHOOKSTRUCT;
import org.safs.natives.win32.User32.MSG;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

/**
 * Hook class for intercepting keyboard events.
 * Hook function KeyboardProc is put in SysMsgHooker.dll for using globally. 
 * <p>Usage:
 * <br>1) define a class that implements interface KeyboardHookListener
 * <br>2) to intercept keyboard events in the class, implement: public void onKeyboardHook(int nCode, NativeLong wParam, NativeLong lParam)
 * <br>3) use KeyboardHook in the class
 *        <li>KeyboardHook keyhook = new KeyboardHook();  
 *        <li>keyhook.addListener(this);
 * 	      <li>keyhook.run();
 * <p> 	
 * @see  org.safs.natives.test.hookTest 
 * @author JunwuMa
 * 
 * <br>NOV 10, 2010 JunwuMa Initial Release
 */
public class KeyboardHook extends AbstractHook {
	/**
	 * the type of hook for a hook procedure that monitors keyboard events
	 */
	static public final int  WH_KEYBOARD = 2;
  
	public KeyboardHook(){
		super("Keyboard");
		//setHook(WH_KEYBOARD, callbackProc());  done in SysMsgHooker.dll
	}
	
    public boolean addListener(CallbackHookListener listener) {
    	if (listener == null || !(listener instanceof KeyboardHookListener)) 
    		return false;
    	else {
    		return super.addListener((KeyboardHookListener)listener); // ensure listener must be a KeyboardHookListener
    	}	
    }
    
    public void stop() {
    	if (isHooked) {
    		SysMsgHooker.INSTANCE.removeKeyboardProcHook();
    		isHooked = false;
    		System.out.println(hookname + " unhooked!");
    	} else 
    		System.out.println(hookname + " not running!");	
    }
    
    public void run() {
        Thread hook_thread = new Thread(new Runnable() {
            public void run(){
                try {
                	isHooked = SysMsgHooker.INSTANCE.setKeyboardProcHook();
                	if (isHooked) {
                		System.out.println("keyboardhook installed!");
                		SysMsgHooker.INSTANCE.setKeyboardListener(callbackKeyboardProc());
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
    
    /**
     * define the callback procedure for hook type WH_KEYBOARD
     * @return the procedure 
     */
    private StdCallCallback callbackKeyboardProc() {
        return new User32.KeyBoardCallBack() {
	            public Pointer callback(int nCode, NativeLong wParam, NativeLong lParam) { 
	            	// call listeners
	            	for (int i=0; i<listeners.size(); i++){
	            		CallbackHookListener tempListener = listeners.get(i);
	            		if (tempListener instanceof KeyboardHookListener)
	            			((KeyboardHookListener) tempListener).onKeyboardHook(nCode, wParam, lParam);
	            	}
    				return null; 
	            }
        };    
	}
}	
