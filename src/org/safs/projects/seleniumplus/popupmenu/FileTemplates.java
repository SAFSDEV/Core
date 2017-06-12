package org.safs.projects.seleniumplus.popupmenu;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class FileTemplates {
	/**
	 * We will initialize the INI file with this working template.
	 */
	public static InputStream testINI(String seleniumloc,String projectName) {

		String contents =	"\n[STAF]\n" +
  				            "# Comment out the line below (using ; or #) to turn OFF the Debug Log to improve performance slightly.\n" +
				            "EmbedDebug=\"DebugLog.txt\"\n" +
							"\n" +

				            "[SAFS_DRIVER]\n" +
							"DriverRoot=\"%SELENIUM_PLUS%\\extra\\automation\"\n" +
							"# Uncomment showMonitor below to use the SAFS Monitor during testing.\n" +
							"# showMonitor=True\n" +
							"\n" +

							"[SAFS_DIRECTORIES]\n" +
							"DATADIR=Maps\n" +
							"BENCHDIR=Benchmarks\n" +
							"DIFFDIR=Diffs\n" +
							"LOGDIR=Logs\n" +
							"TESTDIR=Actuals\n" +
							"\n" +

							"[SAFS_SELENIUM]\n" +
							"# Grid or Remote Selenium Server\n" +
							"#SELENIUMHOST=host.domain.com\n" +
							"#SELENIUMPORT=4444\n" +
							"\n" +

							"[SAFS_TEST]\n" +
							"TestName=\""+ projectName + "\"\n" +
							"TestLevel=\"Cycle\"\n" +
							"CycleSeparator=\"\t\"\n" +
							"# CycleLogName=\""+ projectName + "\"\n" +
							"\n" +

							"# 3 logmodes all enabled below.\n" +
							"# Delete those you will not use to improve performance.\n" +
							"CycleLogMode=\"TEXTLOG CONSOLELOG XMLLOG\"\n" +
							"\n" +

							"# secsWaitForWindow=30\n" +
							"# secsWaitForComponent=30\n" +
							"\n" +

							"[SAFS_SELENIUM]\n" +
							"# BROWSER defines the browser on which to run test.\n" +
							"# BROWSER=explorer | firefox | chrome\n" +
							"BROWSER=chrome\n";

		return new ByteArrayInputStream(contents.getBytes());
	}

}
