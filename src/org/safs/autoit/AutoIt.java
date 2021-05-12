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
 * History:
 * 		SEP 23, 2016 Tao Xie Replace 'AutoItX' as SAFS version 'AutoItXPlus'.
 * 		MAY 16, 2017 Lei Wang Modified executeCommand(): return boolean to tell if command succeed or not.
 *                           Modified AutoItObject(): only register the appropriate AutoItX3 DLL according to system bits.
 * 		JUL 19, 2018 Lei Wang Modified AutoItObject(): Close the AutoIt DLL registration confirmation popup window.
 */
package org.safs.autoit;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;

import org.safs.IndependantLog;
import org.safs.SAFSProcessorInitializationException;
import org.safs.StringUtils;
import org.safs.android.auto.lib.Console;
import org.safs.autoit.lib.AutoItXPlus;
import org.safs.robot.Robot;
import org.safs.tools.consoles.ProcessCapture;
import org.safs.tools.drivers.DriverConstant;

import com.jacob.com.ComFailException;
import com.jacob.com.LibraryLoader;

/**
 * Class used to instantiate AutoIt support.
 * Will automatically register the DLLs on the system at runtime if that has never occurred.
 * @author dharmesh
 */
public class AutoIt {

	private static AutoItXPlus it = null;
	private static final String JACOB_DLL_32 = "jacob-1.18-x86.dll";
	private static final String JACOB_DLL_64 = "jacob-1.18-x64.dll";

	public static AutoItXPlus AutoItObject() throws SAFSProcessorInitializationException{

		if(Console.isUnixOS() || Console.isMacOS()){
			throw new SAFSProcessorInitializationException("The System is Linux/Unix or Mac, AutoIT is not supported on them.");
		}

		/** COM class instantiation method */
		String methodName = StringUtils.getMethodName(0, false) + "() ";
		boolean is32Bits = jvmBitVersion().contains("32");

		if (it == null) {

			String jacobDllVersionToUse;
			String libdir = null;

			if (is32Bits){
				jacobDllVersionToUse = JACOB_DLL_32;
			} else {
				jacobDllVersionToUse = JACOB_DLL_64;
			}

			// ****************************************
			// this test for SAFS or SeleniumPlus should become more centrally located for all of SAFS.
			// See org.safs.install.InstallerImpl, or associated classes.
			String root = null;
			boolean found = false;
			File file = null;
			root = System.getenv(DriverConstant.SYSTEM_PROPERTY_SAFS_DIR);
			if (root != null) {
				libdir = root + File.separator+"lib"+File.separator;
				file = new File(libdir, jacobDllVersionToUse);
				if(file.isFile()){
					found = true;
				}
			}

			if (!found) { // check SEL+ environment or other...
				root = System.getenv(DriverConstant.SYSTEM_PROPERTY_SELENIUMPLUS_DIR);
				if (root != null){
				    libdir = root + File.separator +"libs"+File.separator;
				    file = new File(libdir, jacobDllVersionToUse);
					if(!file.isFile()){
						IndependantLog.debug(methodName +"cannot locate required AutoIt binary DLLs!");
						throw new SAFSProcessorInitializationException(methodName +"cannot locate required AutoIt binary DLL!");
					}
				}
			}

			if(root == null){
				IndependantLog.debug(methodName +"cannot deduce a valid SAFS installation directory!");
				throw new SAFSProcessorInitializationException(methodName +"cannot deduce a valid SAFS installation directory.");
			}
			// end test for SAFS install directories.
			// **************************************

			System.setProperty(LibraryLoader.JACOB_DLL_PATH, file.getAbsolutePath());
			IndependantLog.debug(methodName + "attempting to create AutoItX object.");

			try {
				it = new AutoItXPlus();
				IndependantLog.debug(methodName + "AutoItX object was created");
			} catch (ComFailException cfe) {
				// register dll and re initiate object
				IndependantLog.debug(methodName + "Registering AutoIt DLLs on the System.");
				String cmd = System.getenv("SYSTEMDRIVE") + "\\Windows\\SysWOW64\\regsvr32  "+libdir+"AutoItX3.dll";
				if(!is32Bits){
					cmd = System.getenv("SYSTEMDRIVE") + "\\Windows\\System32\\regsvr32  "+libdir+"AutoItX3_x64.dll";
				}

				int millisecondToWaitDllRegistration = 2000;
				Thread closeRegisterPopupThread = new Thread(new Runnable(){
					@Override
					public void run() {
						StringUtils.sleep(millisecondToWaitDllRegistration);
						try {
							Robot.inputKeys("{Enter}");
						} catch (AWTException e) {
							Robot.keyPress(KeyEvent.VK_ENTER);
							Robot.keyRelease(KeyEvent.VK_ENTER);
						}
					}
				});
				closeRegisterPopupThread.start();

				if(executeCommand(cmd)){//Registration will popup a confirmation window, we need to close it.
					IndependantLog.debug(methodName + "AutoItX DLLs should now be registered.");
				}else{
					IndependantLog.warn(methodName + "Failed to register AutoItX DLLs.");
				}

				try{
					it = new AutoItXPlus();
					IndependantLog.debug(methodName + "AutoItX object was finally created");
				}catch(ComFailException cf){
					IndependantLog.error(methodName + "still cannot instantiate AutoIt Object due to "+
				                         cf.getClass().getSimpleName()+", "+ cf.getMessage());
				}
			}

			if(it == null) IndependantLog.debug(methodName + "AutoItX object was NOT created!");

		} else { // use existing object
			IndependantLog.debug(methodName + "Recycle AutoIt object");
		}

		return it;
	}

	/**
	 * Used internally to register the DLLs if that has never been done.
	 * @param command
	 * @return boolean true if the command has been successfully handled.
	 * @see org.safs.tools.consoles.ProcessCapture
	 */
	private static boolean executeCommand(String command) {
		Process p;
		ProcessCapture console;

		try {
			p = Runtime.getRuntime().exec(command);
			console = new ProcessCapture(p, null, true, true);
			try{ console.thread.join(); }catch(InterruptedException x){;}
			console.shutdown();

			int exitcode = console.getExitValue();
			if(exitcode!=0){
				IndependantLog.debug("Failed to execute command '"+command+"'\n"+"exitcode="+exitcode);
			}
			return exitcode==0;
		} catch (Exception e) {
			IndependantLog.debug("AutoIt.executeCommand "+ e.getClass().getSimpleName()+", "+ e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Determine 32-bit vs 64-bit architecture.
	 * @return System Property value for "sun.arch.data.model"
	 */
	private static String jvmBitVersion(){
		return System.getProperty(DriverConstant.PROPERTY_JVM_BIT_VERSION);
	}
}
