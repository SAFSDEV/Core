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
 * Logs for developers, not published to API DOC.
 *
 * History:
 * SEP 23, 2016 (Lei Wang) Initial release.
 * SEP 21, 2017	(Lei Wang) Quote the parameter then pass it to RemoteDriver.
 * MAR 03, 2020	(Lei Wang) Don't call NativeWrapper.runAsynchBatchProcess() to start a "RemoteServer" process.
 *                        In Linux, it will use "xterm -e" to start a terminal and then run the java command to start a "RemoteServer",
 *                        the problem is that the "RemoteServer" will disappear automatically, very bizzar.
 *                        I will call Runtime.getRuntime().exec() directly to run the java command for launching "RemoteServer".
 *
 *
 */
package org.safs.selenium.webdriver.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.util.SePlusInstallInfo;
import org.safs.sockets.DebugListener;
import org.safs.tools.consoles.ProcessCapture;

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
			@Override
			public String getListenerName() {
				return null;
			}
			@Override
			public void onReceiveDebug(String message) {
				System.out.println(message);
			}
		});

		SePlusInstallInfo seinfo = null;

		try{
			List<String> cmdList = new ArrayList<String>();
			IndependantLog.debug(debugmsg+" Current System properties: "+System.getProperties() );

			seinfo = SePlusInstallInfo.instance();

			cmdList.add(seinfo.getJavaexe());
			cmdList.add("-cp");
			cmdList.add(seinfo.getClassPath(false));
			cmdList.add("org.safs.selenium.webdriver.lib.RemoteDriver");

			for(String parameter: args){
				cmdList.add(parameter);
			}

	        String[] cmdarray = cmdList.toArray(new String[0]);
			IndependantLog.debug(debugmsg+" launching RemoteDriver with cmdline: "+ Arrays.toString(cmdarray));

			File workdir = new File(seinfo.getRootDir().getAbsolutePath());
	        Process process = null;
	        //TODO we should not launch a "selenium server", if there is already one running.
			process = Runtime.getRuntime().exec(cmdarray, null, workdir);
			ProcessCapture console = new ProcessCapture(process, "RemoteDriverLauncher" , true, false);

			@SuppressWarnings("unchecked")
			Vector<String> data = console.getData();
			if(data!=null && data.size()>0){
				for(String message:data){
					IndependantLog.debug(message);
				}
			}

		}catch(SeleniumPlusException | IOException x){
			IndependantLog.debug(debugmsg+" failed to launch Selenium Server due to "+x.getClass().getName()+": "+x.getMessage(), x);
		}

		IndependantLog.setDebugListener(null);
	}
}
