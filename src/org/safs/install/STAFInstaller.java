/** Copyright (C) SAS Institute, Inc. All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.install;

import java.io.File;

import org.safs.natives.NativeWrapper;
import org.safs.tools.CaseInsensitiveFile;

import com.sun.jna.Platform;

public class STAFInstaller extends InstallerImpl{
	
	public static final String STAFDIREnv = "STAFDIR";
	public static final String STAFBINPath = File.separator +"bin";
	public static final String JSTAFPath = STAFBINPath + File.separator +"JSTAF.jar";
	public static final String JSTAFZIPPath = STAFBINPath + File.separator +"JSTAF.zip";

	public STAFInstaller(){super();}
	
	/**
	 * Currently assumes STAF has been installed and STAFDIR is already set.
	 * Can set STAFDIR if args[0] is given a valid value to use.  
	 * If args[0] is provided, the path will be validated before it is used.
	 * Adds STAF paths to CLASSPATH and PATH Environment Variables as long as JSTAF.JAR 
	 * is found to exist.
	 */
	public boolean install(String... args) {
		String stafdir = null;
		File file = null;
		if(args != null && args.length > 0) stafdir = args[0];
		if(stafdir != null && stafdir.length()>0){
			file = new CaseInsensitiveFile(stafdir).toFile();
			if(!file.isDirectory()) {
				stafdir = null;
				file = null;
			}else{
				if(!setEnvValue(STAFDIREnv, stafdir)) return false;
			}
		}
		if (stafdir == null) stafdir = getEnvValue(STAFDIREnv);		
		if (stafdir == null || stafdir.length() == 0) return false;
		String stafjar = stafdir + JSTAFPath;
		file = new CaseInsensitiveFile(stafjar).toFile();
		if(! file.isFile()){
			stafjar = stafdir + JSTAFZIPPath;
			file = new CaseInsensitiveFile(stafjar).toFile();
		}
		if(! file.isFile()) return false;
		appendSystemEnvironment("CLASSPATH", stafjar, null);
		appendSystemEnvironment("PATH", stafdir + STAFBINPath, null);
		return true;
	}

	private boolean cleanSystemEnvironment(){
		removeSystemEnvironmentSubstringContaining("CLASSPATH", JSTAFPath, null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", JSTAFZIPPath, null);
		return true;
	}
	
	/**
	 * Unset the System Environment Variables used by SAFS for STAF.
	 * Remove STAFDIR and paths from CLASSPATH and PATH Environment Variables.
	 * Can unset STAFDIR if args[0] is given a value to use.  
	 * @param args
	 * @return
	 */
	public boolean uninstall(String... args){
		String stafdir = null;
		if(args != null && args.length > 0) stafdir = args[0];
		if(stafdir == null) stafdir = getEnvValue(STAFDIREnv);
		cleanSystemEnvironment();
		setEnvValue(STAFDIREnv, null);
		if(stafdir == null || stafdir.length() == 0) return false;
		removeSystemEnvironmentSubstring("PATH", stafdir + STAFBINPath, null);
		return true;
	}
	

	/**
	 * Main Java executable.  Primarily to run standalone outside of a larger process.
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.STAFInstaller C:\STAF
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.STAFInstaller -u C:\STAF
	 * <p>
	 * System.exit(0) on perceived success.<br>
	 * System.exit(-1) on perceived failure.
	 * @param args --  -u to perform an uninstall instead of install.<br> 
	 * Any first arg other than (-u) will be considered the path for STAFDIR.<br>
	 * The path for STAFDIR does NOT need to be provided if the System Environment Variable 
	 * already exists with a valid setting.<br>
	 * @see org.safs.install.SilentInstaller
	 */
	public static void main(String[] args) {
		boolean uninstall = false;
		String stafdir = null;
		STAFInstaller installer = new STAFInstaller();
		for(String arg:args) {
			if (arg.equals("-u")) {
				uninstall = true;
			}else{
				if(stafdir == null) stafdir = arg;
			}
		}
		if(uninstall){
			if( installer.uninstall(stafdir) ) System.exit(0);
		}else{
			if( installer.install(stafdir) ) System.exit(0);
		}
		System.exit(-1);
	}
}
