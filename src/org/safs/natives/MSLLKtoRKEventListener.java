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

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.safs.Log;
import org.safs.natives.win32.User32.KBDLLHOOKSTRUCT;
import org.safs.tools.input.RobotKeyEvent;

import com.sun.jna.NativeLong;

/**
 * 
 * This Low level key listener will convert the MicroSoft Low Level Key events to<br>
 * Java RobotKeyEvent<br>
 * 
 * This list of RobotKeyEvent can be parsed by method {@link org.safs.tools.input.InputKeysParser#antiParse(List)},<br>
 * and a string, something like "AAAB{%}D", will be returned. This string can be used to generate the test script.<br>
 * 
 * @author Lei Wang
 *
 * @see org.safs.tools.input.RobotKeyEvent
 * @see org.safs.tools.input.InputKeysParser
 */
public class MSLLKtoRKEventListener implements LLKeyboardHookListener{
	private List<RobotKeyEvent> keyEvents = null;
	private boolean shiftOn = false;
	private boolean ctrlOn = false;
	private boolean altOn = false;
	private boolean capsPressed = false;
	private Toolkit toolkit = null;
	
	public MSLLKtoRKEventListener(){
		keyEvents = new ArrayList<RobotKeyEvent>(30);
		toolkit = Toolkit.getDefaultToolkit();
	}

	/**
	 * This method will analyze each input virtual key code (MS virtual key code)<br>
	 * according to their key status (PRESS, RELEASE), generate a series of RobotKeyEvent<br>
	 * and put these events in the List keyEvents.<br>
	 * 
	 * <b>Note:</b> RobotKeyEvent contains Java virtual key code as its key code value<br>
	 * 
	 */
	public void onLLKeyboardHook(int code,NativeLong wParam, KBDLLHOOKSTRUCT info){
		//Convert MS key code to Java key code
		int vk = MSKeyEvent.convertToJavaVK(info.vkCode);
		
		String key = KeyEvent.getKeyText(vk);
		RobotKeyEvent tempEvent = null;
		
		switch(wParam.intValue()) {
		case LLKeyboardHook.WM_SYSKEYDOWN:
		case LLKeyboardHook.WM_KEYDOWN:
			Log.debug("vk: "+vk+"; key="+key+" KEY DOWN");
			//If user continue pressing a key, multiple events will be sent to here.
			//For key shift alt ctrl or caps, we just add one Key Press Event (RobotKeyEvent) to the list
			//For other key, such as 'a' 'B' '3' etc. ,we should add all to the list
			if(KeyEvent.VK_SHIFT==vk){
				if(!shiftOn){
					keyEvents.add(new RobotKeyEvent(RobotKeyEvent.KEY_PRESS,vk));
					shiftOn = true;
				}
			}else if(KeyEvent.VK_CONTROL==vk){
				if(!ctrlOn){
					keyEvents.add(new RobotKeyEvent(RobotKeyEvent.KEY_PRESS,vk));
					ctrlOn = true;
				}
			}else if(KeyEvent.VK_ALT==vk){
				if(!altOn){
					keyEvents.add(new RobotKeyEvent(RobotKeyEvent.KEY_PRESS,vk));
					altOn = true;
				}
			}else if(KeyEvent.VK_CAPS_LOCK==vk){
				if(!capsPressed){
					keyEvents.add(new RobotKeyEvent(RobotKeyEvent.KEY_PRESS,vk));
					capsPressed = true;
				}
			}else{
				keyEvents.add(new RobotKeyEvent(RobotKeyEvent.KEY_PRESS,vk));
			}
			
			break;
		case LLKeyboardHook.WM_SYSKEYUP:
		case LLKeyboardHook.WM_KEYUP:
			Log.debug("vk: "+vk+"; key="+key+" KEY UP");
			if(KeyEvent.VK_SHIFT==vk){
				if(shiftOn) shiftOn = false;		
			}else if(KeyEvent.VK_CONTROL==vk){
				if(ctrlOn) ctrlOn = false;				
			}else if(KeyEvent.VK_ALT==vk){
				if(altOn) altOn = false;				
			}else if(KeyEvent.VK_CAPS_LOCK==vk){
				if(capsPressed) capsPressed = false;
			}
			
			//???? Should NOT be empty, at least there should be one Key Press!!!
			if(keyEvents.isEmpty()) return;
			
			// Here we check the previous Event in the list, if it is Press Event and the vkcode equals this vk
			// Then we combine the Press and Release to one Type Event: "Press+Release" --> "Type"
			int lastIndex = keyEvents.size() - 1;
			tempEvent = keyEvents.get(lastIndex);
			if (tempEvent.get_event()==RobotKeyEvent.KEY_PRESS && tempEvent.get_keycode()==vk) {
				keyEvents.set(lastIndex, new RobotKeyEvent(RobotKeyEvent.KEY_TYPE, vk));
			} else {
				keyEvents.add(new RobotKeyEvent(RobotKeyEvent.KEY_RELEASE, vk));
			}
			
			break;
		default:
			break;
		}
	}
	
	/**
	 * @return	true if the CapsLock is pressed
	 */
	public boolean isCapsLockOn(){
		return toolkit.getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
	}
	
	/**
	 * @return	A list containing s series of RobotKeyEvent
	 */
	public List<RobotKeyEvent> getKeyEvents(){
		return keyEvents;
	}

	/**
	 * Remove all RobotKeyEvent from the list keyEvents
	 */
	public void resetKeyEvents(){
		if(keyEvents!=null) keyEvents.clear();
	}
}
