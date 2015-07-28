/**
 * Original work provided by defunct 'autoandroid-1.0-rc5': http://code.google.com/p/autoandroid/
 * New Derivative work required to repackage for wider distribution and continued development.
 * Copyright (C) SAS Institute
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/ 
package org.safs.android.auto.tools;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.safs.android.auto.lib.AndroidTools;

public class dx {
	public static void main(String [] args) throws InterruptedException, IOException {
		List<String> parameters = new ArrayList<String>();
		parameters.addAll(asList(args));

		String additionalArguments = System.getProperty("additional-arguments");
		if (additionalArguments != null) {
			parameters.addAll(asList(additionalArguments.split(File.pathSeparator)));
		}
		
		AndroidTools.get().dx(parameters).forwardOutput().waitForSuccess();
	}
}
