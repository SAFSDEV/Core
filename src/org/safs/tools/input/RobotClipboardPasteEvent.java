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
package org.safs.tools.input;

import java.util.Iterator;
import java.util.Vector;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.safs.Log;
import org.safs.StringUtils;

/**
 * Derived from RobotKeyEvent for inputting NLS characters. It holds an 'non-standard' string and will copy it to 
 * system Clipboard when doEvent called; a group of RobotKeyEvent(Ctrl+v) followed shall paste the content 
 * in system Clipboard to focused control.  
 * 
 * @author JunwuMa Sept 22, 2008
 * @see InputKeysParser
 * @see org.safs.robot.Robot
 * @see org.safs.tools.input.RobotKeyEvent
 */
public class RobotClipboardPasteEvent extends RobotKeyEvent{
	private StringSelection nlstring = null;
	private Vector<RobotKeyEvent> pasteKeys = null; 
	
	/** The constructor of class RobotClipboardPasteEvent. 
	 * @param content: a 'non-standard' string to be copied to Clipboard
	 * @param pasteKeys: key strokes for doing paste-operation. (Ctrl+v) 
	 */
	public RobotClipboardPasteEvent(String content, Vector<RobotKeyEvent> pasteKeys) {
		super(0, 0);
		this.nlstring = new StringSelection(content);
		this.pasteKeys = pasteKeys;
	}
	/** Overides RobotKeyEvent.doEvent(Robot, int). 
	* Suggest ms_delay>=50, for paste operation needs to wait until copying Clipboard finished.
	*/
	public void doEvent(java.awt.Robot robot, int ms_delay){
	    String debugmsg = StringUtils.getClassName(0, false) + ": ";
	    
	    try {
	    	String copiedMsg = this.nlstring.getTransferData(DataFlavor.stringFlavor).toString();
			Log.debug(debugmsg + "copying '" + copiedMsg + "' to system Clipboard");
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents( this.nlstring, null );
			
			String clipboardMsg = clipboard.getData(DataFlavor.stringFlavor).toString();
			if (!clipboardMsg.equals(copiedMsg)) {
				Log.debug(debugmsg + "failed to send '" + copiedMsg + "' to system Clipboard, try to send it again ...");
				
				clipboard.setContents( this.nlstring, null );
				clipboardMsg = clipboard.getData(DataFlavor.stringFlavor).toString();
				if (!clipboardMsg.equals(copiedMsg)) {
					Log.debug(debugmsg + "the copied contents '" + copiedMsg + "' is NOT equal with current system Clipboard contents '" + clipboardMsg + "'.");
					throw new RuntimeException(debugmsg + "failed to send contents '" + copiedMsg + "' to clipboard .");
				}
			}
		} catch (UnsupportedFlavorException | IOException e) {
			Log.debug(debugmsg + e.getMessage());
		}
	    
	    if(this.pasteKeys == null){
		    Log.debug(" .....Null pasteKeys in RobotClipboardPasteEvent. No paste operation.");
	    	return;
	    }
	    Log.debug(" .....delaying "+ms_delay+ "ms to paste(Ctrl+v) from Clipboard.");
	    Iterator pastestrokes = pasteKeys.iterator();
		while( pastestrokes.hasNext()){
	    	RobotKeyEvent event = (RobotKeyEvent)pastestrokes.next();
	    	event.doEvent(robot, ms_delay);
	    }
	}
}
