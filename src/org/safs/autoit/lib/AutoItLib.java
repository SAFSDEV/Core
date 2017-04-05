/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年4月1日    (SBJLWA) Initial release.
 */
package org.safs.autoit.lib;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.autoit.IAutoItRs;

/**
 * @author sbjlwa
 */
public abstract class AutoItLib {

	public static boolean activate(AutoItXPlus it, IAutoItRs rs) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		boolean success = true;
		try {
			it.winActivate(rs.getWindowsRS());
			if(it.getError()==1){
				success = false;
				Log.error(debugmsg+"Failed to activate window: "+rs);
			}

			if(success && !rs.isWindow()){
				success = it.controlFocus(rs.getWindowsRS(), rs.getWindowText(), rs.getComponentRS());
				if(!success){
					Log.error(debugmsg+"Failed to activate control: "+rs);
				}
			}
		}catch(SAFSException se){
			throw se;
		}catch (Exception x) {
			throw new SAFSException(x.toString());
		}
		return success;
	}

	public static String winGetTitle(AutoItXPlus it, IAutoItRs rs){
		return it.winGetTitle(rs.getWindowsRS(), rs.getWindowText());
	}
}
