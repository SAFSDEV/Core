/**
 * Copyright (C) SAS Institute
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/ 
package org.safs.android.auto.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WindowsConsole extends Console{
	public static final String OS_FAMILY_NAME  = "windows";
	
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
		
		//quote for batch.bat
		if(batchAndArgs!=null && batchAndArgs.size()>0){
			String batch = batchAndArgs.get(0);
			batchAndArgs.set(0, quote(batch));			
		}
		
		parameters.addAll(batchAndArgs);

		return super.start(workingDirectory, parameters);
	}
	
	private String quote(String unquoted) {
		return "\"" + unquoted + "\"";
	}
	
	public String getRecursiveDeleteCommand() {
		return "rmdir /S/Q";
	}
}
