/** Copyright (C) SAS Institute, Inc. All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.install;

import java.io.*;

import com.sun.jna.Platform;
import org.safs.natives.NativeWrapper;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;

/**
 * Handle Windows registration and Environment settings for Android SDK support.  
 * Intended to replace WSH install scripts.
 * @author Carl Nagle
 */
public class AndroidInstaller extends InstallerImpl{
	
	private static final String ROBOTIUMRC_HOME = "ROBOTIUMRC_HOME";
	private static final String ANDROID_HOME    = "ANDROID_HOME";
	private static final String ANT_HOME        = "ANT_HOME";
	// appends to safsdir
	private static final String SamplesDroidDir   = s+"Samples"+s+"Droid"; 
	private static final String SAFSTestRunnerDir = SamplesDroidDir + s + "SAFSTestRunner";
	private static final String SAFSMessengerDir  = SamplesDroidDir + s + "SAFSTCPMessenger";
	private static final String SpinnerSampleDir  = SamplesDroidDir + s + "SpinnerSample";
		
	public AndroidInstaller() {super();}
	
	/**
	 * Main processing routine.
	 * Sets System Environment variables used by SAFS Android Support:
	 * <p>
	 * ROBOTIUMRC_HOME<br>
	 * <p>
	 * boolean success = AndroidInstaller.intall();
	 * <p>
	 * @param args -- none used at this time
	 * @return true if successful, false otherwise.
	 */
	public boolean install(String... args){
		String safsdir = this.getEnvValue(SAFSInstaller.SAFSDIREnv);
		if(safsdir == null || safsdir.length() == 0) return false;
		File file = new CaseInsensitiveFile(safsdir).toFile();
		if(!file.isDirectory()) return false;

		boolean setHome = true;
		File rchome = null;
		String val = getEnvValue(ROBOTIUMRC_HOME);
		if(val != null){
			rchome = new CaseInsensitiveFile(getEnvValue(ROBOTIUMRC_HOME)).toFile();
			if (rchome.isDirectory()){
				setHome = false;
				if(val.endsWith(s)) val = val.substring(0, val.length() - s.length());
			}
		}
		if(setHome){		
			val = safsdir + SamplesDroidDir;
			rchome = new CaseInsensitiveFile(val).toFile();
			if(rchome.isDirectory()){
				if(val.endsWith(s)) val = val.substring(0, val.length() - s.length());
				if(! setEnvValue(ROBOTIUMRC_HOME, val)) return false;
			}else return false;				
		}
		String sdkhome = getEnvValue(ANDROID_HOME);
		if(sdkhome == null) return false;
		rchome = new CaseInsensitiveFile(sdkhome).toFile();
		if(! rchome.isDirectory()) return false;
		String sdkjavapath = Platform.isWindows()? sdkhome.replace(s, s+s): sdkhome;
	    String arg = " \n# location of the SDK. This is only used by Ant\n";
	    arg += "sdk.dir="+ sdkjavapath +"\n";
	    try{
	    	FileUtilities.writeStringToUTF8File(safsdir + SAFSTestRunnerDir +s+"local.properties", arg);
		    FileUtilities.writeStringToUTF8File(safsdir + SpinnerSampleDir +s+"local.properties", arg);
	   	    String targ = safsdir;
	   	    String runner = SAFSTestRunnerDir + s;
	   	    if(Platform.isWindows()){
	   	    	targ = targ.replace(s, s+s);
	   	    	runner = runner.replace(s, s+s);
	   	    }
    	    arg += "safs.droid.automation.libs="+ targ + runner +"libs\n";
		    FileUtilities.writeStringToUTF8File(safsdir + SAFSMessengerDir +s+ "local.properties", arg);
		    String oldline = "C:\\Program Files\\Android\\android-sdk";
		    FileUtilities.replaceDirectoryFilesSubstrings(
		    	 		  safsdir + SamplesDroidDir, 
		    			  new String[]{".bat",".ini"}, 
		    			  oldline, 
		    			  sdkhome, 
		    			  false);
	    }catch(Exception x){return false;}
	    
	    // TODO: Perform automated first-time debug build 
	    
	    return true;
	}
	
	/**
	 * Unset the System Environment Variables used by SAFS.
	 * <p>
	 * ROBOTIUMRC_HOME<br>
	 * <p>
	 * @param args
	 * @return
	 */
	public boolean uninstall(String... args){
		return setEnvValue(ROBOTIUMRC_HOME, null);
	}
	
	/**
	 * Main Java executable.  Primarily to run standalone outside of a larger process.
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.AndroidInstaller
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.AndroidInstaller -u
	 * <p>
	 * System.exit(0) on perceived success.<br>
	 * System.exit(-1) on perceived failure.
	 * @param args --  -u to perform an uninstall instead of install. 
	 * @see org.safs.install.SilentInstaller
	 */
	public static void main(String[] args) {
		boolean uninstall = false;
		AndroidInstaller installer = new AndroidInstaller();
		for(String arg:args) if (arg.equals("-u")) uninstall = true;
		if(uninstall){
			if( installer.uninstall() ) System.exit(0);
		}else{
			if( installer.install() ) System.exit(0);
		}
		System.exit(-1);
	}
}
