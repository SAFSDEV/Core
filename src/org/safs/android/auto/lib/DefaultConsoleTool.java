/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
