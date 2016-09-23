/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

/**
 * History:
 * 		SEP 23, 2016, SCNTAX, Replace 'AutoItX' as SAFS version 'AutoItXPlus'.  
 */
package org.safs.autoit;

import java.io.File;
import java.util.Vector;

import org.safs.IndependantLog;
import org.safs.SAFSProcessorInitializationException;
import org.safs.StringUtils;
import org.safs.autoit.lib.AutoItXPlus;
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

		/** COM class instantiation method */
		String methodName = StringUtils.getMethodName(0, false) + "() ";
		
		if (it == null) {
			
			String jacobDllVersionToUse;
			String libdir = null;
			
			if (jvmBitVersion().contains("32")){
				jacobDllVersionToUse = JACOB_DLL_32;
			} else {
				jacobDllVersionToUse = JACOB_DLL_64;
			}
			
			// ****************************************
			// this test for SAFS or SeleniumPlus should become more centally located for all of SAFS.
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
								
				String cmd =  System.getenv("SYSTEMDRIVE") + "\\Windows\\SysWOW64\\regsvr32 /s "+libdir+"AutoItX3.dll";
				executeCommand(cmd);
				cmd = System.getenv("SYSTEMDRIVE") + "\\Windows\\System32\\regsvr32 /s "+libdir+"AutoItX3_x64.dll";
				executeCommand(cmd);
				
				IndependantLog.debug(methodName + "AutoItX DLLs should now be registered.");
				
				try{ 
					it = new AutoItXPlus();
					IndependantLog.debug(methodName + "AutoItX object was finally created");
				}
				catch(ComFailException cf){
					IndependantLog.debug(methodName + "still cannot instantiate AutoIt Object due to "+
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
	 * @return Vector containing any data made available by the ProcessCapture console.
	 * @see org.safs.tools.consoles.ProcessCapture
	 */
	private static Vector executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		ProcessCapture console;
		Vector data = new Vector();
		
		try {
			p = Runtime.getRuntime().exec(command);
			console = new ProcessCapture(p, null, true, true);
			try{ console.thread.join(); }catch(InterruptedException x){;}
			console.shutdown();
			data = console.getData();
		} catch (Exception e) {
			IndependantLog.debug("AutoIt.executeCommand "+ e.getClass().getSimpleName()+", "+ e.getMessage(), e);
		}
		return data;
	}

	/**
	 * Determine 32-bit vs 64-bit architecture.
	 * @return System Property value for "sun.arch.data.model"
	 */
	private static String jvmBitVersion(){
		return System.getProperty(DriverConstant.PROPERTY_JVM_BIT_VERSION);
	}	
}
