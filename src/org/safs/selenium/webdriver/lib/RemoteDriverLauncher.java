/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2016年9月23日    (Lei Wang) Initial release.
 */
package org.safs.selenium.webdriver.lib;

import java.io.IOException;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.natives.NativeWrapper;
import org.safs.selenium.util.SePlusInstallInfo;
import org.safs.sockets.DebugListener;

/**
 * <pre>
 * We don't put the selenium-server-standalone-xxx.jar on the CLASSPATH during the installation.
 * While RemoteDriver needs that jar to run, so we create this class to detect necessary information
 * such as path to selenium-server-standalone-xxx.jar and launch RemoteDriver.
 * </pre>
 * This class accepts the same parameters as {@link RemoteDriver#main(String[])}.<br>
 * 
 * @see RemoteDriver#main(String[])
 * 
 * @author Lei Wang Created on SEP 22, 2016
 */
public class RemoteDriverLauncher {
		
	public static void main(String[] args){
		
		String debugmsg = StringUtils.debugmsg(false);
		
		//Debug message will be output to the standard out
		IndependantLog.setDebugListener(new DebugListener(){
			public String getListenerName() {
				return null;
			}
			public void onReceiveDebug(String message) {
				System.out.println(message);
			}
		});
		
		SePlusInstallInfo seinfo = null;
		
		try{
			seinfo = SePlusInstallInfo.instance();
			
			IndependantLog.debug(debugmsg+" Current System properties: "+System.getProperties() );
			
			//TODO pass the current JVM option to RemoteDriver
			String cmdline = seinfo.getJavaexe() +" -cp " + seinfo.getClassPath(false) +" org.safs.selenium.webdriver.lib.RemoteDriver ";
			for(String parameter: args) cmdline += " "+parameter;
			
			IndependantLog.debug(debugmsg+" launching RemoteDriver with cmdline: "+ cmdline);
						
			NativeWrapper.runAsynchBatchProcess(seinfo.getRootDir().getAbsolutePath(), cmdline);
			
		}catch(SeleniumPlusException | IOException x){
			IndependantLog.debug(debugmsg+" failed to launch Selenium Server due to "+x.getClass().getName()+": "+x.getMessage(), x);			
		}
		
		IndependantLog.setDebugListener(null);
	}
}
