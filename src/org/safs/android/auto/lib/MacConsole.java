/**
 * Copyright (C) SAS Institute
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/ 
package org.safs.android.auto.lib;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MacConsole extends Console{
	public static final String OS_FAMILY_NAME 	  = "mac";
	
	public Process2 batch(File workingDirectory, List<String> batchAndArgs)
			throws IOException {
		return super.batch(workingDirectory, batchAndArgs);
	}

	public String getRecursiveDeleteCommand() {
		return "rm -rf";
	}
}
