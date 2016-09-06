/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
			Log.debug(debugmsg + "copying '" + this.nlstring.getTransferData(DataFlavor.stringFlavor) + "' to system Clipboard");
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents( this.nlstring, null );
			Log.debug(debugmsg + "the content of system Clipboard is set as: '" + clipboard.getData(DataFlavor.stringFlavor) + "'.");
		} catch (UnsupportedFlavorException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
