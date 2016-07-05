/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2016年6月14日    (Lei Wang) Initial release.
 */
package com.sas.spock.safs.runner.tests;

import org.junit.Test;
import org.safs.model.tools.Runner;

/**
 * @author Lei Wang
 *
 */
public class SimpleTest {

	@Test
	public void test() {
//		System.out.println("The current system date time: "+Misc.GetSystemDateTime(true));
		String result = "GetSystemDate_VAR";
		try {
			Runner.command("GetSystemDateTime", result, "true");
			System.out.println("The current system date time: "+ Runner.GetVariableValue(result));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
