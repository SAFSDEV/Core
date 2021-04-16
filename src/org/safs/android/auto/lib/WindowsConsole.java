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
 * APR 16, 2018 (Lei Wang) Modify method batch(): launch the batch script in a new command line console.
 */
package org.safs.android.auto.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WindowsConsole extends Console{
	public static final String OS_FAMILY_NAME  = "windows";

	@Override
	public Process2 batch(File workingDirectory, List<String> batchAndArgs) throws IOException {
		List<String> parameters = new ArrayList<String>();

		String systemRoot = System.getenv("SystemRoot");
		if (systemRoot == null) {
			throw new IllegalStateException("Please set (or pass through) the SystemRoot environment variable.");
		}

		//Only make quote for cmd.exe and batch.bat, if make too much quote, exec will fail
		//"C:\Windows\system32\cmd.exe" "/c" "D:\ant182\bin\ant.bat" "debug"

		//quote for cmd.exe
		parameters.add(quote(systemRoot + "\\system32\\cmd.exe"));
		parameters.add("/c");
		parameters.add("start");
		//When using 'start', if we quote the batch script parameter, it will fail to launch the script.
		//cmd /c start "c:\SeleniumPlus\extra\RemoteServer.bat" will fail
		//cmd /c start c:\SeleniumPlus\extra\RemoteServer.bat will work
		//The reason is that the first quoted parameter after 'start' will be considered as the title! Please refer to https://superuser.com/questions/239565/can-i-use-the-start-command-with-spaces-in-the-path

		//quote for batch.bat script
		if(batchAndArgs!=null && batchAndArgs.size()>0){
			String batch = batchAndArgs.get(0);
			//Set the command to run by 'start'
			batchAndArgs.set(0, quote(batch));
			//Set the title for 'start'
			batchAndArgs.add(0, quote(batch));
		}

		parameters.addAll(batchAndArgs);

		return super.start(workingDirectory, parameters);
	}

	@Override
	public String getRecursiveDeleteCommand() {
		return "rmdir /S/Q";
	}
}
