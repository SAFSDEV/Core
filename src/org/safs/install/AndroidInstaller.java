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
 * @author canagl
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
		if(safsdir == null || safsdir.length() == 0) {
			setProgressMessage("AndroidInstaller did not find expected '"+ SAFSInstaller.SAFSDIREnv +"' Environment Variable");
			return false;
		}
		File file = new CaseInsensitiveFile(safsdir).toFile();
		if(!file.isDirectory()) {
			setProgressMessage("AndroidInstaller '"+ SAFSInstaller.SAFSDIREnv +"' Environment Variable is NOT a directory!");
			return false;
		}

		boolean setHome = true;
		File rchome = null;
		String val = getEnvValue(ROBOTIUMRC_HOME);
		if(val != null){
			rchome = new CaseInsensitiveFile(getEnvValue(ROBOTIUMRC_HOME)).toFile();
			if (rchome.isDirectory()){
				setHome = false;
				if(val.endsWith(s)) val = val.substring(0, val.length() - s.length());
				setProgressMessage("AndroidInstaller found '"+ ROBOTIUMRC_HOME +"' Environment Variable set to: "+ val);
			}else{
				setProgressMessage("AndroidInstaller found '"+ ROBOTIUMRC_HOME +"' Environment Variable is NOT a valid directory.");
			}
		}
		if(setHome){		
			val = safsdir + SamplesDroidDir;
			rchome = new CaseInsensitiveFile(val).toFile();
			if(rchome.isDirectory()){
				if(val.endsWith(s)) val = val.substring(0, val.length() - s.length());
				if(! setEnvValue(ROBOTIUMRC_HOME, val)) {
					setProgressMessage("AndroidInstaller could not SET '"+ ROBOTIUMRC_HOME +"' Environment Variable to: "+ val);
					return false;
				}else{
					setProgressMessage("AndroidInstaller SET '"+ ROBOTIUMRC_HOME +"' Environment Variable to: "+ val);
				}
			}else {
				setProgressMessage("AndroidInstaller expected location '"+ val +"' is NOT a valid directory!");
				return false;				
			}
		}
		String sdkhome = getEnvValue(ANDROID_HOME);
		if(sdkhome == null) {
			setProgressMessage("AndroidInstaller '"+ ANDROID_HOME +"' Environment Variable is NOT set.");
			return false;
		}
		rchome = new CaseInsensitiveFile(sdkhome).toFile();
		if(! rchome.isDirectory()) {
			setProgressMessage("AndroidInstaller '"+ ANDROID_HOME +"' setting '"+ rchome.getAbsolutePath()+"' is NOT a valid directory!");
			return false;
		}
		String sdkjavapath = Platform.isWindows()? sdkhome.replace(s, s+s): sdkhome;
	    String arg = " \n# location of the SDK. This is only used by Ant\n";
	    arg += "sdk.dir="+ sdkjavapath +"\n";
	    try{
	    	try{FileUtilities.writeStringToUTF8File(safsdir + SAFSTestRunnerDir +s+"local.properties", arg);}
	    	catch(Exception x){
				setProgressMessage("Android SAFSTestRunner installer "+ x.getClass().getSimpleName()+": "+ x.getMessage());
	    	}
		    try{FileUtilities.writeStringToUTF8File(safsdir + SpinnerSampleDir +s+"local.properties", arg);}
	    	catch(Exception x){
				setProgressMessage("Android SampleSpinner installer "+ x.getClass().getSimpleName()+": "+ x.getMessage());
	    	}
	   	    String targ = safsdir;
	   	    String runner = SAFSTestRunnerDir + s;
	   	    if(Platform.isWindows()){
	   	    	targ = targ.replace(s, s+s);
	   	    	runner = runner.replace(s, s+s);
	   	    }
    	    arg += "safs.droid.automation.libs="+ targ + runner +"libs\n";
		    try{FileUtilities.writeStringToUTF8File(safsdir + SAFSMessengerDir +s+ "local.properties", arg);}
	    	catch(Exception x){
				setProgressMessage("Android SAFSTestMessenger installer "+ x.getClass().getSimpleName()+": "+ x.getMessage());
	    	}
		    String oldline = "C:\\Program Files\\Android\\android-sdk";
		    try{ FileUtilities.replaceDirectoryFilesSubstrings(
		    	 		  safsdir + SamplesDroidDir, 
		    			  new String[]{".bat",".ini"}, 
		    			  oldline, 
		    			  sdkhome, 
		    			  false);
		    }catch(Exception x){
				setProgressMessage("Android BAT/INI installer "+ x.getClass().getSimpleName()+": "+ x.getMessage());
	    	}
		    if(Platform.isWindows()){
		    	arg = "\nSET CLASSPATH=\"\"\nstart \"AnT Debug\" /B /WAIT \"cmd.exe\" /C \"ant debug\"\n";
			    try{FileUtilities.writeStringToUTF8File(safsdir + SAFSMessengerDir +s+ "build.bat", arg);}
		    	catch(Exception x){
					setProgressMessage("Android SAFSTestMessenger BUILD.BAT installer "+ x.getClass().getSimpleName()+": "+ x.getMessage());
		    	}
			    try{FileUtilities.writeStringToUTF8File(safsdir + SAFSTestRunnerDir +s+ "build.bat", arg);}
		    	catch(Exception x){
					setProgressMessage("Android SAFSTestRunner BUILD.BAT installer "+ x.getClass().getSimpleName()+": "+ x.getMessage());
		    	}
		    }
	    }catch(Exception x){
			setProgressMessage("Android support installer "+ x.getClass().getSimpleName()+": "+ x.getMessage());
	    	return false;
	    }
	    
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
