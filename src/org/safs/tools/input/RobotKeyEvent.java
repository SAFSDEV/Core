/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.input;

import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Vector;

import org.safs.Log;

/**
 * A simplified KeyEvent class used with the AWT Robot to generate low-level keystrokes.
 * @author Carl Nagle
 * @see InputKeysParser
 * @see java.awt.Robot
 */
public class RobotKeyEvent {

	private int _keycode;
	private int _event;
	
	public static final int KEY_PRESS = 1;
	public static final int KEY_RELEASE = 2;
	public static final int KEY_TYPE = KEY_PRESS | KEY_RELEASE;
	
	public RobotKeyEvent(int event, int keycode){
		this._event = event;
		this._keycode = keycode;		
	}

	public void doEvent(java.awt.Robot robot, int ms_delay){
		Log.debug("RobotKeyEvent: "+ toString()); 		
		if (ms_delay >= 0) robot.setAutoDelay(ms_delay);
		if((_event & KEY_PRESS)== KEY_PRESS){
			Log.debug("PRESSING keycode "+_keycode); 		
			robot.keyPress(_keycode);
		}
		if((_event & KEY_RELEASE)== KEY_RELEASE){
			Log.debug("RELEASING keycode "+_keycode); 		
			robot.keyRelease(_keycode);
		}
	}
	
	public int get_keycode() {
		return _keycode;
	}

	public int get_event() {
		return _event;
	}

	public String toString(){
		String rc = null;
		String vkString = "0X"+Integer.toString(_keycode, 16).toUpperCase();
	    switch(_event){
	    	case KEY_PRESS:
	    		rc= "KEY_PRESS: ";
	    		break;
	    	case KEY_RELEASE:
				rc= "KEY_RELEASE: ";
				break;	    	
	    	case KEY_TYPE:
				rc= "KEY_PRESS_N_RELEASE: ";	    	
	    }
	    rc += "key="+vkString+"("+_keycode+");  keyString="+KeyEvent.getKeyText(_keycode);
	    return rc;
	}
	
	public boolean equals(Object event){
		if(event==null) return this==null;
		if(! (event instanceof RobotKeyEvent) ) return false;
		RobotKeyEvent other = (RobotKeyEvent) event;
		
		return (other._event==this._event) && (other._keycode==this._keycode);
	}
	
	/**
	 * Execute multiple RobotKeyEvent keystrokes sequentially.
	 * The keystrokes to execute are stored in a Vector as RobotKeyEvent objects.
	 * @param keystrokes Vector of RobotKeyEvent objects to be typed in sequence
	 * @param robot java.awt.Robot to use for typing.
	 * @param ms_delay delay tp use between keystrokes
	 */
	public static void doKeystrokes(Vector keystrokes, java.awt.Robot robot, int ms_delay){
		Iterator events = keystrokes.iterator();
		RobotKeyEvent event;				
		while(events.hasNext()){
			try{
				event = (RobotKeyEvent) events.next();
				event.doEvent(robot, ms_delay);
			}catch(Exception x){
				Log.debug("RobotKeyEvent exception:", x);
			}
		}	   	
		
	}
}
