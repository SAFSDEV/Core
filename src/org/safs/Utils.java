/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * APR 22, 2016    (SBJLWA) Initial release.
 */
package org.safs;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

public class Utils {
	/**
	 * @param onOff boolean, Set keyboard's 'NumLock' on or off.
	 */
	public static void setNumLock(boolean onOff){
		Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, onOff);
	}
	/**
	 * @return boolean, the current keyboard's 'NumLock' status.
	 */
	public static boolean getNumLock(){
		return Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
	}
}
