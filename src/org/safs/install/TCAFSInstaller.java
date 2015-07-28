/** Copyright (C) SAS Institute, Inc. All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.install;

import java.io.*;

import org.safs.natives.NativeWrapper;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;

/**
 * Handle Windows registration and Environment settings for SmartBear TestComplete and 
 * TestExecute.  Intended to replace WSH install scripts.
 * @author Carl Nagle
 */
public class TCAFSInstaller extends InstallerImpl{
	
	private static final String TCHomeEnv = "TESTCOMPLETE_HOME";
	private static final String TCExeEnv  = "TESTCOMPLETE_EXE";
	
	private static final String TCExePath = "TestComplete.exe";
	private static final String TEExePath = "TestExecute.exe";	
	
	//pre  W7
	private static final String HKLMRoot  = "HKLM\\Software\\";
	
	//post W7
	private static final String HKCURoot  = "HKCU\\Software\\";
	
	//post W7
	private static final String WOW6432   = "HKLM\\Software\\Wow6432Node\\";
	
	//on 64-bit this is in WOW6432
	private static final String TCPath_V8 = "Automated QA\\TestComplete\\8.0\\Setup";
	private static final String TEPath_V8 = "Automated QA\\TestExecute\\8.0\\Setup";

	//on 64-bit this is in WOW6432
	private static final String TCPath_V9 = "SmartBear\\TestComplete\\9.0\\Setup";
	private static final String TEPath_V9 = "SmartBear\\TestExecute\\9.0\\Setup";

	//on 64-bit this is in WOW6432
	private static final String TCPath_V10 = "SmartBear\\TestComplete\\10.0\\Setup";
	private static final String TEPath_V10 = "SmartBear\\TestExecute\\10.0\\Setup";

	//under Setup
	private static final String TCProductPath = "Product Path";
	
	public TCAFSInstaller() {super();}
	
	/**
	 * Main processing routine.
	 * Sets System Environment variables used by SAFS and TestComplete:
	 * <p>
	 * TESTCOMPLETE_HOME<br>
	 * TESTCOMPLETE_EXE<br>
	 * <p>
	 * boolean success = TCAFSInstaller.intall();
	 * <p>
	 * @param args -- none used at this time
	 * @return true if successful, false otherwise.
	 */
	public boolean install(String... args){
		boolean setHome = true;
		File tchome = null;
		File tcexe = null;
		String val;
		setProgressMessage("Evaluation "+ TCHomeEnv);
		if(getEnvValue(TCHomeEnv)!= null){
			tchome = new CaseInsensitiveFile(getEnvValue(TCHomeEnv)).toFile();
			if(tchome.isDirectory()){
				setHome = false;
				setProgressMessage("Evaluation "+ TCExeEnv);
				if(getEnvValue(TCExeEnv)!= null){
					tcexe = new CaseInsensitiveFile(tchome, "bin\\"+ getEnvValue(TCExeEnv)).toFile();
					if(tcexe.isFile()){
						setProgressMessage("Windows SmartBear TestComplete support complete.");
						// we should be done.  Things are set.
						return true;
					}
				}
				
			}
		}
		if(setHome){
			// first try execution environment of TestExecute			
			setProgressMessage("Evaluating Windows SmartBear TestExecute support.");
			val = getRegistryValue(WOW6432+TEPath_V10, TCProductPath);
			if(val == null)
				val = getRegistryValue(WOW6432+TEPath_V9, TCProductPath);
			if(val == null)
				val = getRegistryValue(WOW6432+TEPath_V8, TCProductPath);
			if(val == null) 
				val = getRegistryValue(HKLMRoot+TEPath_V9, TCProductPath);
			if(val == null) 
				val = getRegistryValue(HKLMRoot+TEPath_V8, TCProductPath);
			
			// if TestExecute not found then look for TestComplete
			if(val == null)
				setProgressMessage("Evaluating Windows SmartBear TestComplete support.");
				val = getRegistryValue(WOW6432+TCPath_V10, TCProductPath);
 			if(val == null)	
 				val = getRegistryValue(WOW6432+TCPath_V9, TCProductPath);
			if(val == null)
				val = getRegistryValue(WOW6432+TCPath_V8, TCProductPath);
			if(val == null) 
				val = getRegistryValue(HKLMRoot+TCPath_V9, TCProductPath);
			if(val == null) 
				val = getRegistryValue(HKLMRoot+TCPath_V8, TCProductPath);
			if(val != null){
				if(val.endsWith(File.separator)) val = val.substring(0, val.length() - File.separator.length());
				setProgressMessage("Detecting Windows SmartBear support at "+ val);
				tchome = new CaseInsensitiveFile(val).toFile();
				if(tchome.isDirectory()){
					if(! setEnvValue(TCHomeEnv, val)) {
						setProgressMessage("Unable to set required Directory for Windows SmartBear TestComplete support.");
						return false;
					}
				}else {
					setProgressMessage("Directory for Windows SmartBear TestComplete support seems to be invalid.");
					return false;
				}
				
			}
		}
		// home is set or null
		if(tchome == null){
			setProgressMessage("Did not detect Windows SmartBear TestComplete installation.");
			return false;
		}
		tcexe = new CaseInsensitiveFile(tchome, "bin\\"+ TEExePath).toFile();
		if(! tcexe.isFile()) tcexe = new CaseInsensitiveFile(tchome, "bin\\"+ TCExePath).toFile();
        boolean result = false;
		if( tcexe.isFile() ){
			result = setEnvValue(TCExeEnv, tcexe.getName());
			if(!result){
				setProgressMessage("Unable to set required Path for Windows SmartBear TestComplete support.");
			}
		}else{
			setProgressMessage("Path for Windows SmartBear TestComplete support seems to be invalid.");
		}
		return result;
	}
	
	/**
	 * Unset the System Environment Variables used by SAFS.
	 * <p>
	 * TESTCOMPLETE_HOME<br>
	 * TESTCOMPLETE_EXE<br>
	 * <p>
	 * @param args
	 * @return
	 */
	public boolean uninstall(String... args){
		boolean success1 = setEnvValue(TCHomeEnv, null);
		boolean success2 = setEnvValue(TCExeEnv, null);
		return success1 && success2;
	}
	
	/**
	 * Main Java executable.  Primarily to run standalone outside of a larger process.
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.TCAFSInstaller
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.TCAFSInstaller -u
	 * <p>
	 * System.exit(0) on perceived success.<br>
	 * System.exit(-1) on perceived failure.
	 * @param args --  -u to perform an uninstall instead of install. 
	 * @see org.safs.install.SilentInstaller
	 */
	public static void main(String[] args) {
		boolean uninstall = false;
		TCAFSInstaller installer = new TCAFSInstaller();
		for(String arg:args) if (arg.equals("-u")) uninstall = true;
		if(uninstall){
			if( installer.uninstall() ) System.exit(0);
		}else{
			if( installer.install() ) System.exit(0);
		}
		System.exit(-1);
	}
}
