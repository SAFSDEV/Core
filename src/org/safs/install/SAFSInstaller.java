/** Copyright (C) SAS Institute, Inc. All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.install;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.safs.natives.NativeWrapper;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;

import com.sun.jna.Platform;

public class SAFSInstaller extends InstallerImpl {
	
	public static final String SAFSDIREnv  = "SAFSDIR";
	public static final String SAFSBINPath = s +"bin";
	public static final String SAFSLIBPath = s +"lib";
	public static final String SAFSProjectPath = s +"Project";
	public static final String SAFSSamplesPath = s +"Samples";
	public static final String SAFSJARPath = SAFSLIBPath + s +"SAFS.jar";
	public static final String HKLM_RUN_KEY = "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run";
	public static final String HKLM_RUN_VALUE = "SAFSMon";
	
	/**
	 * Allows the installer to propopate and execute all known installers (RFT, TCAFS, etc..)
	 * Default = true.  Set to false to prevent propogating execution to all installers.
	 * <p><ul>
	 *    TCAFSInstaller<br>
	 *    RFTInstaller<br>
	 *    OCRInstaller<br>
	 *    COMInstaller<br>
	 *    AndroidInstaller<br>
	 *    SeleniumInstaller<br>
	 *    RobotiumInstaller<br>
	 *  </ul>
	 */
	public boolean propogate = true;
	
	public SAFSInstaller() {
		super();
	}
	
	/**
	 * Currently assumes SAFS has been installed (unzipped) and SAFSDIR is already set.
	 * Can set SAFSDIR if args[0] is given a valid value to use.  
	 * If args[0] is provided, the path will be validated before it is used.
	 * Adds SAFS paths to CLASSPATH and PATH Environment Variables as long as SAFS.JAR 
	 * is found to exist.
	 */
	public boolean install(String... args) {
		String safsdir = null;
		File file = null;
		setProgressMessage("Evaluating installation directory integrity.");
		if(args != null && args.length > 0) safsdir = args[0];
		if(safsdir != null && safsdir.length()>0){
			file = new CaseInsensitiveFile(safsdir).toFile();
			if(!file.isDirectory()) {
				safsdir = null;
				file = null;
			}else{
				if(!setEnvValue(SAFSDIREnv, safsdir)) {
					setProgressMessage("Unable to create required Environment Variable.");
					return false;
				}
			}
		}
		if (safsdir == null) safsdir = getEnvValue(SAFSDIREnv);		
		if (safsdir == null || safsdir.length() == 0) {
			setProgressMessage("Unable to retrieve required Environment Variable.");
			return false;
		}
		setProgressMessage("Evaluating System Environment for outdated installation remnants.");
		cleanSystemEnvironment(safsdir);
		String safsjar = safsdir + SAFSJARPath;
		file = new CaseInsensitiveFile(safsjar).toFile();
		if(! file.isFile()) return false;
		setProgressMessage("Appending CLASSPATH and PATH values as needed.");
		appendSystemEnvironment("CLASSPATH", safsjar, null);
		appendSystemEnvironment("PATH", safsdir + SAFSBINPath, null);
		
		if(propogate){
			setProgressMessage("Evaluating associated installation requirements...");
			InstallerImpl installer = null;
			if(Platform.isWindows()){
				setProgressMessage("Evaluating Windows SmartBear TestComplete support requirements.");
				try{
					installer = new TCAFSInstaller();
					installer.setProgressIndicator(progresser);
					if(installer.install()){
						setProgressMessage("Windows SmartBear TestComplete support successfully installed.");
					}else{
						setProgressMessage("Windows SmartBear TestComplete support installation did NOT complete successfully.");
					}
				}catch(Throwable t){
					setProgressMessage("SmartBear TestComplete support installer "+ t.getClass().getSimpleName()+": "+ t.getMessage());
					setProgressMessage("Windows SmartBear TestComplete support installation did NOT complete successfully.");
				}
				setProgressMessage("Evaluating Windows IBM Rational Functional Tester support requirements.");
				try{
					installer = new RFTInstaller();
					installer.setProgressIndicator(progresser);
					if(installer.install()){
						setProgressMessage("Windows IBM Rational Functional Tester support successfully installed.");
					}else{
						setProgressMessage("Windows IBM Rational Functional Tester support installation did NOT complete successfully.");
					}
				}catch(Throwable t){
					setProgressMessage("IBM Rational support installer "+ t.getClass().getSimpleName()+": "+ t.getMessage());
					setProgressMessage("Windows IBM Rational Functional Tester support installation did NOT complete successfully.");
				}
				setProgressMessage("Evaluating Windows OCR support requirements.");
				try{
					installer = new OCRInstaller();
					installer.setProgressIndicator(progresser);
					if(installer.install()){
						setProgressMessage("Windows OCR support successfully installed.");
					}else{
						setProgressMessage("Windows OCR support installation did NOT complete successfully.");
					}
				}catch(Throwable t){
					setProgressMessage("Windows OCR support installer "+ t.getClass().getSimpleName()+": "+ t.getMessage());
					setProgressMessage("Windows OCR support installation did NOT complete successfully.");
				}
				setProgressMessage("Evaluating Windows DLL support requirements.");
				try{
					installer = new DLLInstaller();
					installer.setProgressIndicator(progresser);
					if(installer.install()){
						setProgressMessage("Windows DLL support successfully installed.");
					}else{
						setProgressMessage("Windows DLL support installation did NOT complete successfully.");
					}
				}catch(Throwable t){
					setProgressMessage("Windows DLL support installer "+ t.getClass().getSimpleName()+": "+ t.getMessage());
					setProgressMessage("Windows DLL support installation did NOT complete successfully.");
				}
			}
			setProgressMessage("Evaluating Android support requirements.");
			try{
				installer = new AndroidInstaller();
				installer.setProgressIndicator(progresser);
				if(installer.install()){
					setProgressMessage("Windows Android support successfully installed.");
				}else{
					setProgressMessage("Windows Android support installation did NOT complete successfully.");
				}
			}catch(Throwable t){
				setProgressMessage("Android support installer "+ t.getClass().getSimpleName()+": "+ t.getMessage());
				setProgressMessage("Windows Android support installation did NOT complete successfully.");
			}
		}
		if(Platform.isWindows()){
    		try{
				setProgressMessage("Attempting to start Windows SAFSMon support.");
    			NativeWrapper.runAsynchBatchProcess(safsdir+"\\bin", "start "+ safsdir +"\\bin\\ServiceMonitor.vbs -noprompt");
    		}catch(Throwable ignore){
				setProgressMessage("WARNING: Windows SAFSMon support startup not successful at this time.");
    		}
    		try{
				setProgressMessage("Installing Windows SAFSMon support.");
    			NativeWrapper.SetRegistryKeyValue(HKLM_RUN_KEY, HKLM_RUN_VALUE, "REG_SZ", "wscript.exe "+ safsdir +"\\bin\\ServiceMonitor.vbs -noprompt");
			}catch(Throwable ignore){
				setProgressMessage("WARNING: Installing Windows SAFSMon support not successful at this time.");				
			}    	
		}
	    String oldline = "C:\\SAFS";
	    try{
			setProgressMessage("Modifying Project template installation path information.");
	    	FileUtilities.replaceDirectoryFilesSubstrings(
	    	 		  safsdir + SAFSProjectPath, 
	    			  new String[]{".bat",".ini"}, 
	    			  oldline, 
	    			  safsdir, 
	    			  false);		    	
			setProgressMessage("Modifying Samples template installation path information.");
	    	FileUtilities.replaceAllSubdirectoryFilesSubstrings(
	    	 		  safsdir + SAFSSamplesPath, 
	    			  new String[]{".bat",".ini"}, 
	    			  oldline, 
	    			  safsdir, 
	    			  false);		    	
	    }catch(IOException x){
	    	return false;
	    }		
		return true;
	}

	private boolean cleanSystemEnvironment(String safsdir){
		String libpath = SAFSLIBPath + File.separator;
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safs.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsmodel.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsjvmagent.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safscust.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsjrex.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsdebug.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsselenium.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsdroid.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsios.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safs-remotecontrol.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safssockets.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsabbot.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsupdate.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsinstall.jar", null);

		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsrational.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsrational_ft.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsrational_ft_custom.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsrational_ft_enabler.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "safsrational_xde.jar", null);
		
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "robotium-remotecontrol.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "robotium-serializable.jar", null);
		
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "jaccess.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "jakarta-regexp-1.3.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "JRex.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "jna.zip", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "win32-x86.zip", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "jai_core.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "jai_codec.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "jai_imageio.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "clibwrapper_jiio.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "dom4j-2.0.0-ALPHA-2.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "jaxen-1.1.1.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "nekohtml.jar", null);
		String path = removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "juniversalchardet-1.0.3.jar", null);
		if((safsdir != null) && (safsdir.length() > 0) && (path != null) ){
			int oldlength = path.length();
			int newlength = 0;
			while(newlength < oldlength){
				oldlength = path.length();
				path = removeSystemEnvironmentSubstringContaining("CLASSPATH", safsdir + libpath, null);
				newlength = (path == null) ? Integer.MAX_VALUE : path.length();
			}
		}
		setEnvValue("CLASSPATH_SAFSBAK", null);
		setEnvValue("PATH_SAFSBAK", null);
		return true;
	}
	
	/**
	 * Unset the System Environment Variables used by SAFS.
	 * Remove SAFSDIR and paths from CLASSPATH and PATH Environment Variables.
	 * Can unset SAFSDIR if args[0] is given a value to use.  
	 * @param args
	 * @return
	 */
	public boolean uninstall(String... args){
		String safsdir = null;
		setProgressMessage("Evaluating installation directory integrity.");
		if(args != null && args.length > 0) safsdir = args[0];
		if(safsdir == null) safsdir = getEnvValue(SAFSDIREnv);
		setProgressMessage("Removing System environment variable associations.");
		cleanSystemEnvironment(safsdir);
		if(safsdir == null || safsdir.length() == 0) return false;
		setProgressMessage("Removing CLASSPATH and PATH variable associations.");
		removeSystemEnvironmentSubstring("PATH", safsdir + SAFSBINPath, null);
		removeSystemEnvironmentSubstring("CLASSPATH", safsdir + SAFSLIBPath, null);
		InstallerImpl installer = null;
		if(propogate){
			if(Platform.isWindows()){
				setProgressMessage("Removing any Windows SmartBear TestComplete associations.");
				installer = new TCAFSInstaller();
				installer.setProgressIndicator(progresser);
				installer.uninstall();
				setProgressMessage("Removing any Windows IBM Rational Functional Tester associations.");
				installer = new RFTInstaller();
				installer.setProgressIndicator(progresser);
				installer.uninstall();
				setProgressMessage("Removing any Windows OCR associations.");
				installer = new OCRInstaller();
				installer.setProgressIndicator(progresser);
				installer.uninstall();
				setProgressMessage("Removing any Windows DLL associations.");
				installer = new DLLInstaller();
				installer.setProgressIndicator(progresser);
				installer.uninstall();
			}
			setProgressMessage("Removing any Android testing associations.");
			installer = new AndroidInstaller();
			installer.setProgressIndicator(progresser);
			installer.uninstall();
		}
		if(Platform.isWindows()){
			setProgressMessage("Attempting shutdown of any running SAFSMon process.");
    		try{ NativeWrapper.runShortProcessAndWait("cscript.exe", safsdir +"\\bin\\ProcessKiller.vbs -noprompt -process wscript.exe -command \\bin\\ServiceMonitor.vbs");}
    		catch(Throwable ignore){}
			setProgressMessage("Removing Windows SAFSMon assocations.");
	    	try{ NativeWrapper.RemoveRegistryKeyValue(HKLM_RUN_KEY, HKLM_RUN_VALUE);}
	    	catch(Throwable ignore){
	    		progresser.setProgressMessage("WARNING: Removing Windows SAFSMon runtime was not successful.");
	    	}
    		try{ NativeWrapper.runShortProcessAndWait("cscript.exe", safsdir +"\\uninstall\\UninstallSAFSProgramGroup.vbs");}
	    	catch(Throwable ignore){
	    		progresser.setProgressMessage("WARNING: Uninstalling the SAFS Program Group was NOT successful.");
	    	}
		}
		// remove AFTER all the other uninstallers have used it.
		setProgressMessage("Removing System Environment installation directory associations.");
		setEnvValue(SAFSDIREnv, null);
		setProgressMessage("Removing installation directory.");
		FileUtilities.deleteDirectoryRecursively(safsdir, true);
		return true;
	}
	
	/**
	 * Main Java executable.  Primarily to run standalone outside of a larger process.
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.SAFSInstaller [C:\SAFS]
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.SAFSInstaller -u [C:\SAFS]
	 * <p>
	 * If the -np option is not provided, then the installer will propogate and execute 
	 * all known SAFS Framework Installers.
	 * <p>
	 * System.exit(0) on perceived success.<br>
	 * System.exit(-1) on perceived failure.
	 * @param args:
	 * <p><ul>
	 * "-u"  -- to perform an uninstall instead of install.<br>
	 * "-np" -- do not propogate to other installers for TCAFS, RFT, etc..<br> 
	 * Any first arg other than (-u or -np) will be considered the path for SAFSDIR.<br>
	 * The path for SAFSDIR does NOT need to be provided if the System Environment Variable 
	 * already exists with a valid setting.<br>
	 * </ul>
	 * @see org.safs.install.SilentInstaller
	 * @see #propogate
	 */
	public static void main(String[] args) {
		boolean uninstall = false;
		boolean dopropogate = true;
		String safsdir = null;
		SAFSInstaller installer = new SAFSInstaller();
		for(String arg:args) {
			if (arg.equals("-u")) {
				uninstall = true;
			}else if(arg.equals("-np")) {
				dopropogate = false;
			}else if(safsdir==null) {
				safsdir = arg;
			}
		}
		installer.propogate = dopropogate;
		boolean success = true;
		if(uninstall){
			success = installer.uninstall(safsdir);
		}else{
			success = installer.install(safsdir);
		}
		System.exit(success ? 0:1);
	}
}
