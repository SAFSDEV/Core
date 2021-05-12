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
package org.safs.android.auto.lib;

import org.safs.IndependantLog;
import org.safs.StringUtils;

/**
 * 
 * <br>
 * History:<br>
 * 
 *  <br>   Nov 12, 2014    (Lei Wang) Initial release.
 */
public class DefaultConsoleTool extends ConsoleTool {

	public static DefaultConsoleTool instance(){
		return new DefaultConsoleTool();
	}
	
	public String getToolHome() { return null; }
	
	protected void modifyBinDirectories() {
		try{
			String[] pathes = Console.getPath().split(Console.PATH_SEP);
			for(String path: pathes){
				binDirectories.add(path);
			}
		}catch(Exception e){
			IndependantLog.error(StringUtils.debugmsg(false)+" met "+StringUtils.debugmsg(e));
		}
	}
}
