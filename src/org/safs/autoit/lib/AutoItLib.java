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
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年4月1日    (Lei Wang) Initial release.
 */
package org.safs.autoit.lib;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.autoit.IAutoItRs;

/**
 * @author Lei Wang
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
