/**
 * Original work provided by defunct 'autoandroid-1.0-rc5': http://code.google.com/p/autoandroid/
 * New Derivative work required to repackage for wider distribution and continued development.
 * Copyright (C) SAS Institute
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/ 
package org.safs.android.auto.tools;
import java.io.IOException;

import org.safs.android.auto.lib.AndroidTools;

public class adb {
	public static void main(String [] args) throws InterruptedException, IOException {
		AndroidTools.get().adb(args).forwardOutput().waitForSuccess();
	}
}
