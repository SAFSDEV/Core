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
 * MAY 16, 2017	Lei Wang	Combined creating selenium-plus shortcuts to one script CreateSeleniumPlusProgramGroup.wsf
 *                      Set on shortcuts' property "Run As Administrator".
 * JUN 29, 2018	Lei Wang Modified install(), uninstall(): install/uninstall ghostscript
 * JUL 30, 2018	Lei Wang Modified install(): do not add "start" to run ServiceMonitor.vbs, we have added "start" command in WindowsConsole.batch().
 * DEC 30, 2019	Lei Wang Modified code to install STAF (32 or 64 bits) correctly.
 * MAR 31, 2020	Lei Wang Added 3 options 'silent', 'verbose' and 'debug', apply them on GhostScriptInstaller.
 * APR 01, 2020	Lei Wang Modified _getEmbeddedJavaExe(): Currently, the embedded JREs are in the same path on Windows and Linux.
 */
package org.safs.install;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.safs.Constants.LogConstants;
import org.safs.IndependantLog;
import org.safs.android.auto.lib.Console;
import org.safs.natives.NativeWrapper;
import org.safs.sockets.DebugListener;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;

import com.sun.jna.Platform;

public class SeleniumPlusInstaller extends InstallerImpl implements DebugListener{

	private static final String s = File.separator;
	public static final String SELENIUMDIREnv  = "SELENIUM_PLUS";
	public static final String SELENIUMDIRAutomation = s+"extra"+s+"automation";
	public static final String SELENIUMBINPath = SELENIUMDIRAutomation+s+"bin";
	public static final String SELENIUMOCRPath = SELENIUMDIRAutomation+s+"ocr";
//	public static final String SELENIUMHARNESSPath = "extra"+s+"harness";
	public static final String SELENIUMLIBPath = s +"libs";
//	public static final String SELENIUMEmbeddedJavaExe = s+"Java"+s+"bin"+s+"java.exe";
	public static final String SAFSJARPath = SELENIUMLIBPath+s+"seleniumplus.jar";
	public static final String STAFEmbeddedJARPath = SELENIUMLIBPath+s+"JSTAFEmbedded.jar";
	public static final String HKLM_RUN_KEY = "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run";
	public static final String HKLM_RUN_MONITOR = "DesignerMon";
	public static final String HKLM_RUN_SERVER = "SeleniumServer";

	/** "/usr/local/seleniumplus" **/
	public static final String DEFAULT_UNX_SELENIUMPLUS_DIR = "/usr/local/seleniumplus";

	static boolean installsafs = true;
	static boolean removestaf = false;
	static boolean removesafs = false;
	static boolean installstaf = false; //Carl Nagle changed 2014.07.29
	static String  installedstafdir = SilentInstaller.DEFAULT_WIN_STAF_DIR;
	static String  installstafexe = SilentInstaller.DEFAULT_WIN_STAF_3_EXE;
	static String  seleniumdir = null;
	static String  stafdir = SilentInstaller.DEFAULT_WIN_STAF_DIR;
	static boolean uninstall = false;
	static boolean propogate = true;
	static boolean debug = false;
	static boolean silent = true;
	static boolean verbose = false;

	static Console console = null;
	/** progressbar is a swing panel to show the progress of installation.*/
	static ProgressIndicator progresser = new ProgressIndicator();

	static int pctProgress = 0;
	static int pctIncrement = 14;
	static int pctSTAFUninstall = pctIncrement;                    // 14
	static int pctSTAFInstall = 100;

	public SeleniumPlusInstaller() {
		super();
	}

	static void printArgs(String[] args){
		System.out.println( args.length + " arguments provided...");
		for(int i = 0; i < args.length; i++){
			System.out.println("");
			System.out.print(" '"+args[i]+"'");
			System.out.println("");
		}
		System.out.println ("");
		System.out.println ("Installing TestDesigner: "+ installsafs);
		System.out.println ("TestDesigner InstallDir: "+ seleniumdir);
		System.out.println ("");
		System.out.println ("Installing Harness: "+ installstaf);
		if(installstaf){
			System.out.println ("Harness  Installer: "+ installstafexe);
			System.out.println ("Harness InstallDir: "+ stafdir);
		}
	}

	static void parseArgs(String[] args){
		int argc = args.length;
		for(int i = 0; i < argc; i++){
			String arg = args[i].toLowerCase();

			if (arg.equals(SilentInstaller.OPTION_NOSAFS)) { installsafs   = false; continue; }
			if (arg.equals(SilentInstaller.OPTION_NOSTAF)) { installstaf   = false; continue; }

			if (arg.equals(SilentInstaller.OPTION_REMOVE_STAF)) {
				if (i < argc -1){
					installedstafdir = args[++i];
					installedstafdir = installedstafdir.trim();
				}
				removestaf = true;
				installstaf = false;
				installsafs = false;
				continue;
			}
			if (arg.equals(SilentInstaller.OPTION_ALTSTAF)) {
				if (i < argc -1) {
					installstaf = true;
					stafdir = args[++i];
				}
				continue;
			}

			if (arg.equals(SilentInstaller.OPTION_STAF_INSTALL_EXE)) {
				if (i < argc -1) {
					installstaf = true;
					installstafexe = args[++i];
					installstafexe = installstafexe.trim();
				}
				continue;
			}
			if (arg.equals(ARG_SILENT)) {
				if (i < argc -1) {
					silent = Boolean.parseBoolean(args[++i]);
				}
				continue;
			}
			if (arg.equals(ARG_DEBUG)) {
				if (i < argc -1) {
					debug = Boolean.parseBoolean(args[++i]);
				}
				continue;
			}
			if (arg.equals(ARG_VERBOSE)) {
				if (i < argc -1) {
					verbose = Boolean.parseBoolean(args[++i]);
				}
				continue;
			}
			if (arg.equals(ARG_UNINSTALL)) {
				uninstall = true;
				installstaf = false;
				installsafs = false;
				removesafs = true;
				progresser.setTitle("TestDesigner Uninstall");
			}else if(arg.equals("-np")) {
				propogate = false;
			}else if(seleniumdir==null) {
				seleniumdir = arg;
			}
		}
	}

	static void initialize(){
		System.out.println ("Installing on '"+ Console.OS_NAME+"' System");

		if(Console.isWindowsOS()){
			progresser.setTitle("TestDesigner Windows Installation");
		}else if(Console.isUnixOS()){

			installedstafdir = SilentInstaller.DEFAULT_UNX_STAF_DIR;
			stafdir = SilentInstaller.DEFAULT_UNX_STAF_DIR;
			seleniumdir = DEFAULT_UNX_SELENIUMPLUS_DIR;
			if(Console.is64BitOS()){
				installstafexe = SilentInstaller.DEFAULT_LINUX_64_STAF_3_BIN;
			}else{
				installstafexe = SilentInstaller.DEFAULT_LINUX_STAF_3_BIN;
			}

			progresser.setTitle("TestDesigner Unix Installation");
		}else if(Console.isMacOS()){
			progresser.setTitle("TestDesigner Mac Installation");
		}else{
			System.err.println("Operating System '"+Console.OS_NAME+"' not yet supported!");
		}

		try{
			console = Console.get();
		}catch(UnsupportedOperationException e){
			System.err.println("Can't get Console instance for Operating System '"+Console.OS_NAME+"' Exception="+e.getMessage());
		}
	}

	private static String _findInstallSTAFExe(String installer)throws IOException{
		CaseInsensitiveFile ifile = new CaseInsensitiveFile(installer);
		if(! ifile.exists()) ifile = new CaseInsensitiveFile(seleniumdir, installer);
		if(! ifile.exists()) ifile = new CaseInsensitiveFile(seleniumdir+File.separator+"install", installer);
		if(! ifile.exists()) throw new FileNotFoundException(installer);
		return ifile.getPath();
	}

	private static String _getEmbeddedJavaExe() throws FileNotFoundException{
		String java = null;
		if(Console.is64BitOS()){
			java = File.separator+"Java64"+File.separator+"jre"+File.separator+"bin"+File.separator+"java";
		}else{
			java = File.separator+"Java"+File.separator+"jre"+File.separator+"bin"+File.separator+"java";
		}
		if(Console.isWindowsOS()){
			IndependantLog.debug(java);
			java += ".exe";
		}

		CaseInsensitiveFile ifile = new CaseInsensitiveFile(seleniumdir + java);
		IndependantLog.debug("Testing if embedded JVM '"+ifile.getAbsolutePath()+"' exist. ");
		if(! ifile.exists()){ throw new FileNotFoundException(java); }
		return ifile.getPath();
	}

	private static String _try_STAF_LAX_VM_Option(String installer){
		String fullpath = installer;
		//to install STAF with SilentInstaller.DEFAULT_WIN_STAF_3_EXE (NoJVM), JVM IS needed!
		if(installer.indexOf("NoJVM")>0){
			try{
				String java = _getEmbeddedJavaExe();
				fullpath += " "+SilentInstaller.STAF_LAX_VM+" "+"\""+ java +"\"";
			}catch(FileNotFoundException ignore){
				System.out.println("No embedded JRE detected.  Hoping for an installed JVM...");
				progresser.setProgressMessage("This STAF install requires JAVA.  No embedded JVM detected.");
			}
		}
		return fullpath;
	}

	/**
	 * Perform STAF install.
	 * If -silent parameter was specified then a silent install will be done.<br>
	 * Currently all installs are silent.<br>
	 * If -staf parameter was specified then the install will attempt to use any
	 * provided user-specified directory for the install.
	 **/
	static int doSTAFInstall(){

		int status = -1;
		String cmd;
		try {
			cmd = _findInstallSTAFExe(installstafexe);
		} catch (IOException e) {
			IndependantLog.error("Installation failed, due to "+e.toString());
			return status;
		}

		cmd = _try_STAF_LAX_VM_Option(cmd);

		if(Console.isWindowsOS()){
			cmd += " "+ SilentInstaller.staf_3_silent + SilentInstaller.staf_3_set_install_dir+"\""+ stafdir +"\"";
			cmd += " "+ SilentInstaller.staf_3_register_0 + SilentInstaller.staf_3_loginstart_0;
		}else if(Console.isUnixOS()){
			cmd += " "+ SilentInstaller.staf_3_silent + SilentInstaller.staf_3_set_install_dir+ stafdir ;
		}else if(Console.isMacOS()){
			cmd += " "+ SilentInstaller.staf_3_silent + SilentInstaller.staf_3_set_install_dir+ stafdir ;
		}else{
			System.err.println(Console.OS_NAME+" has not been supported!!!");
			return status;
		}

		progresser.setProgressMessage("Harness Install: "+ cmd);
		progresser.setProgressMessage("Please wait. This may take a few moments...");

		// If the directory where staf will be installed exists, delete it firstly.
		progresser.setProgress(pctProgress);
		progresser.setProgressMessage("Delete any old files from '"+stafdir+"' ...");
		FileUtilities.deleteDirectoryRecursively(stafdir, false);
		pctProgress += pctIncrement;
		progresser.setProgress(pctProgress);
		// Execute the installation command by shell
		ConsumOutStreamProcess p = new ConsumOutStreamProcess(cmd, false, false);
		status = p.start();

		if(Console.isUnixOS()){
			//On Linux, it seems that running following command in Java process does NOT really install STAF
			// ./STAF341-setup-linux-amd64-NoJVM.bin -i silent -DACCEPT_LICENSE=1 -DUSER_INSTALL_DIR="/usr/local/staf" -DREGISTER=0 -DSTART_ON_LOGIN=0
			//If we run it in the Linux shell, it does install the STAF.

			//We need to check if the installer really does the work!
			File stafDir = new File(stafdir);
			File stafProcFile = new File(stafDir, "bin"+File.separator+"STAFProc");
			if(!stafDir.exists() || !stafProcFile.exists()){
				progresser.setProgressMessage("STAF installer seems not work!", LogConstants.WARN);
				try{
					File stafInstallScript = File.createTempFile("stafinstall", ".sh");
					progresser.setProgressMessage("Creat script "+stafInstallScript+" to install STAF.");
					//Make the script executable
					Runtime.getRuntime().exec("chmod u+x "+stafInstallScript);
					FileUtilities.writeStringToSystemFile(stafInstallScript.getAbsolutePath(), cmd);
					p = new ConsumOutStreamProcess(stafInstallScript.getAbsolutePath(), true, false);
					status = p.start();
					progresser.setProgressMessage("STAF second install exit code: "+ status);

					//Verify that the STAF has been installed correctly
					if(!stafDir.exists() || !stafProcFile.exists()){
						progresser.setProgressMessage("STAF installer (invoked by script) seems not work!", LogConstants.WARN);
					}

				}catch(Exception e){
					progresser.setProgressMessage("Failed to install STAF, met "+e.getMessage(), LogConstants.ERROR);
				}
			}
		}

		progresser.setProgressMessage("Deleting STAF IBM Registration file...");
		File regFile = new CaseInsensitiveFile(stafdir +s+ SilentInstaller.STAF_REG_FILE).toFile();
		if (regFile.exists()) { regFile.delete(); }
		pctProgress += pctIncrement;
		progresser.setProgress(pctProgress);

		progresser.setProgressMessage("Performing STAF post-install configuration...");
		STAFInstaller si = new STAFInstaller();
		si.install(stafdir);
		pctProgress += pctIncrement;
		progresser.setProgress(pctProgress);
		progresser.setProgressMessage("Finished Harness Installation.");

		return status;
	}

	/**
	 * Perform uninstall STAF.
	 * Uninstall STAF silently.<br>
	 **/
	static int doSTAFUnInstall(){

		int status = -1;
		String cmd = "";
		//If installedstafdir contains a \ at the end, remove it.
		if(installedstafdir.endsWith(s)){
			installedstafdir = installedstafdir.substring(0, installedstafdir.length()-1);
		}

		if(Console.isWindowsOS()){
			cmd = installedstafdir+ SilentInstaller.staf_uninstall_3;
		}else if(Console.isUnixOS()){
			cmd = installedstafdir+ SilentInstaller.staf_uninstall_3_linux;
		}else if(Console.isMacOS()){
				cmd = installedstafdir+ SilentInstaller.staf_uninstall_3_mac;
		}else{
			System.err.println(Console.OS_NAME+" has not been supported!!!");
			return status;
		}

		progresser.setProgressMessage("TestDesigner Harness Uninstall: "+ cmd);
		progresser.setProgressMessage("Please wait while the operation completes.");
		progresser.setProgressMessage("This may take a few moments...");
		ConsumOutStreamProcess p = new ConsumOutStreamProcess(cmd,false,false);
		status = p.start();

		if(status!=ConsumOutStreamProcess.PROCESS_NORMAL_END){
			progresser.setProgressMessage("TestDesigner Harness Uninstall Failed.");
			return status;
		}
		progresser.setProgressMessage("Performing TestDesigner Harness Cleanup...");
		pctProgress += pctIncrement;
		//If uninstaller worked correctly, then try to delete all remains files.
		status = FileUtilities.deleteDirectoryRecursively(installedstafdir, false);
		if(Console.isUnixOS() || Console.isMacOS() ){
			//There are some symbolic links in directory staf, so try to use 'rm -rf dir' to delete it
			status = SilentInstaller.deleteAllDirectory(installedstafdir);
		}

		STAFInstaller si = new STAFInstaller();
		si.uninstall(installedstafdir);

		pctProgress += pctIncrement;
		progresser.setProgress(pctProgress);

		if(Console.isWindowsOS()){
	    	try{
	    		Hashtable results = NativeWrapper.runShortProcessAndWait("cscript.exe", seleniumdir+"\\uninstall\\UninstallSTAFProgramGroup.vbs");
	    		Integer rc = (Integer)results.get("Result");
	    		Vector out;
	    		status = rc.intValue();
	    		if(status != 0){
	    			progresser.setProgressMessage("Uninstall STAF Program Group failed with status "+ rc.intValue());
		    		out = (Vector)results.get("Vector");
		    		for(int i=0;i<out.size();i++){
		    			progresser.setProgressMessage(out.get(i).toString());
		    		}
		    		return status;
	    		}
				progresser.setProgressMessage("Uninstall STAF Program Group successful:");
	    		out = (Vector)results.get("Vector");
	    		for(int i=0;i<out.size();i++){
	    			progresser.setProgressMessage(out.get(i).toString());
	    		}
	    	}
	    	catch(Exception x){
	    		progresser.setProgressMessage("Uninstall STAF Program Group "+ x.getClass().getSimpleName()+": "+x.getMessage());
	    		return -1;
	    	}
		}

		progresser.setProgressMessage("TestDesigner Harness uninstalled.");
		return 0;
	}

	private void fixSubdirectoryFiles(String findstr, String replacestr, String rootdir){
	    try{
	    	FileUtilities.replaceAllSubdirectoryFilesSubstrings(
	    	 		  rootdir,
	    			  new String[]{".bat",".ini",".dat",".project",".classpath",".xml",".json",
	    	 				       ".prefs",".launch",".xmi",".txt",".index",".history",
	    	 				       ".location",".markers",".snap"},
	    			  findstr,
	    			  replacestr,
	    			  false);
	    }catch(Exception x){
	    	progresser.setProgressMessage(x.getClass().getSimpleName() +": "+x.getMessage() +" while processing "+ rootdir + " for "+ findstr);
	    }
	}

	static final String[] findStrings = new String[]{
			"C:\\SeleniumPlus",
			"C:\\\\SeleniumPlus",
			"C\\:/SeleniumPlus",
			"C\\:\\\\SeleniumPlus",
			"C:/SeleniumPlus",
			"C\\:_SeleniumPlus",
			"C:\\/SeleniumPlus"
	};

	String convertWinReplaceString(String findStr, String replaceStr){
		StringBuffer rc = new StringBuffer();
		int cindex = replaceStr.indexOf(":");
		if(cindex < 0) return replaceStr;
		cindex = findStr.indexOf(":");
		if(cindex < 0) return replaceStr;
		String[] findarr = findStr.split(":");
		String[] replarr = replaceStr.split(":");
		String temp;

		// handle left of colon

		// start with drive letter--always first
		rc.append(replarr[0]);
		// add \\ backslashes if found before ":"
		if(findarr[0].contains("\\")) rc.append("\\");

		// add the colon
		rc.append(":");

		// handle right of colon

		if(findarr[1].contains("\\\\")) replarr[1] = replarr[1].replace("\\", "\\\\");
		if(findarr[1].contains("/")) replarr[1] = replarr[1].replace("\\", "/");
		if(findarr[1].startsWith("_")) replarr[1] = replarr[1].replace("\\", "_");
		rc.append(replarr[1]);

		return rc.toString();
	}

	/**
	 * Create SeleniumPlus Program Group and shortcuts.
	 * @return boolean true if successful
	 */
	static boolean createSeleniumPlusProgramGroup(){
		if(Console.isWindowsOS()){
	    	try{
	    		Hashtable results = NativeWrapper.runShortProcessAndWait("cscript.exe", seleniumdir +"\\install\\CreateSeleniumPlusProgramGroup.wsf");
	    		Integer rc = (Integer)results.get("Result");
	    		Vector out;
	    		if(rc.intValue()!= 0){
	    			progresser.setProgressMessage("Create Selenium Plus Program Group failed with status "+ rc.intValue());
		    		out = (Vector)results.get("Vector");
		    		for(int i=0;i<out.size();i++){
		    			progresser.setProgressMessage(out.get(i).toString());
		    		}
		    		return false;
	    		}
				progresser.setProgressMessage("Create Selenium Plus Program Group successful:");
	    		out = (Vector)results.get("Vector");
	    		for(int i=0;i<out.size();i++){
	    			progresser.setProgressMessage(out.get(i).toString());
	    		}
	    	}
	    	catch(Exception x){
	    		progresser.setProgressMessage("Create Selenium Plus Program Group "+ x.getClass().getSimpleName()+": "+x.getMessage());
	    		return false;
	    	}
		}
    	return true;
	}
	/**
	 * Currently assumes SeleniumPlus has been installed (unzipped) and SELENIUM_PLUS is already set.
	 * Can set SELENIUM_PLUS if args[0] is given a valid value to use.
	 * If args[0] is provided, the path will be validated before it is used.
	 * Adds SeleniumPlus paths to CLASSPATH and PATH Environment Variables as long as SeleniumPlus JAR
	 * is found to exist.
	 */
	@Override
	public boolean install(String... args) {
		String seleniumdir = null;
		File file = null;
		if(args != null && args.length > 0) seleniumdir = args[0];

		if(Platform.isWindows()){
			if(seleniumdir != null && seleniumdir.length()>0){
				file = new CaseInsensitiveFile(seleniumdir).toFile();
				if(!file.isDirectory()) {
					progresser.setProgressMessage("Specified install directory '"+ seleniumdir +"' is not a valid directory.");
					seleniumdir = null;
					file = null;
				}else{
					if(!setEnvValue(SELENIUMDIREnv, seleniumdir)) {
						progresser.setProgressMessage("Failed to set environment "+SELENIUMDIREnv+" to '"+ seleniumdir +"'.");
						return false;
					}
				}
			}
			if (seleniumdir == null) seleniumdir = getEnvValue(SELENIUMDIREnv);
			if (seleniumdir == null || seleniumdir.length() == 0) {
				progresser.setProgressMessage("Failed to get "+SELENIUMDIREnv+" value to use for the install.");
				return false;
			}
			cleanSystemEnvironment(seleniumdir);
			String safsjar = seleniumdir + SAFSJARPath;
			file = new CaseInsensitiveFile(safsjar).toFile();
			if(! file.isFile()) {
				progresser.setProgressMessage("Failed to locate required "+ safsjar +" for the install.");
				return false;
			}
			appendSystemEnvironment("CLASSPATH", safsjar, null);
			appendSystemEnvironment("PATH", seleniumdir + SELENIUMBINPath, null);

			String stafembed = seleniumdir + STAFEmbeddedJARPath;
			file = new CaseInsensitiveFile(stafembed).toFile();
			if(file.isFile()) {
				appendSystemEnvironment("CLASSPATH", stafembed, null);
			}

			if(propogate){
				InstallerImpl installer = null;

				//installer = new DLLInstaller();
				//installer.install();
				setProgressMessage("Evaluating Windows OCR support requirements.");
				try{
					installer = new OCRInstaller(seleniumdir + SELENIUMDIRAutomation );
					installer.setProgressIndicator(progresser);
					installer.install();
				}catch(Throwable t){
					setProgressMessage("Windows OCR support installer "+ t.getClass().getSimpleName()+": "+ t.getMessage());
				}

				//install ghostscript
				try{
					installer = new GhostScriptInstaller(progresser, seleniumdir, null, silent, verbose, debug);
					installer.install();
				}catch(Throwable t){
					setProgressMessage("Failed to install '"+installer.getProductName()+"', due to "+ t.getClass().getSimpleName()+": "+ t.getMessage());
				}
			}else if(Platform.isLinux()){
				if (seleniumdir == null || seleniumdir.length() == 0) {
					progresser.setProgressMessage("Failed to get "+SELENIUMDIREnv+" value to use for the install.");
					return false;
				}
			}

	    	progresser.setProgressMessage("Creating SeleniumPlus Program Group and ShortCuts ... ");
	    	if(!createSeleniumPlusProgramGroup()){
	    		return false;
	    	}
	    	try {
	    		progresser.setProgressMessage("Installing Windows DesignerMon support.");
	    		NativeWrapper.SetRegistryKeyValue(HKLM_RUN_KEY, HKLM_RUN_MONITOR, "REG_SZ", "wscript.exe "+ seleniumdir +"\\extra\\ServiceMonitor.vbs -noprompt");
	    	}catch(Throwable ignore){
				progresser.setProgressMessage("WARNING: Installing Windows DesignerMon support was not successful at this time.");
	    	}
	    	try{
	    		progresser.setProgressMessage("Starting Windows DesignerMon.");
//	    		NativeWrapper.runAsynchBatchProcess(safsdir +"\\extra", "start "+ safsdir +"\\extra\\ServiceMonitor.vbs -noprompt");
	    		NativeWrapper.runAsynchBatchProcess(seleniumdir +"\\extra", seleniumdir +"\\extra\\ServiceMonitor.vbs", "-noprompt");
	    	}catch(Throwable ignore){
				progresser.setProgressMessage("WARNING: Starting Windows DesignerMon support was not successful at this time.");
	    	}
		}

		String findstr;
		String finddir = seleniumdir +s+ ".metadata" +s+ ".plugins";
		for(int i=0;i<findStrings.length;i++){
			findstr = findStrings[i];
			progresser.setProgressMessage("processing "+ finddir+" for "+findstr);
			fixSubdirectoryFiles(findstr, convertWinReplaceString(findstr, seleniumdir), finddir);
		}
		finddir = seleniumdir +s+ "eclipse" +s+ "configuration";
		for(int i=0;i<findStrings.length;i++){
			findstr = findStrings[i];
			progresser.setProgressMessage("processing "+ finddir+" for "+findstr);
			fixSubdirectoryFiles(findstr, convertWinReplaceString(findstr, seleniumdir), finddir);
		}
		finddir = seleniumdir +s+ "eclipse" +s+ "p2" +s+ "org.eclipse.equinox.p2.engine";
		for(int i=0;i<findStrings.length;i++){
			findstr = findStrings[i];
			progresser.setProgressMessage("processing "+ finddir+" for "+findstr);
			fixSubdirectoryFiles(findstr, convertWinReplaceString(findstr, seleniumdir), finddir);
		}

		return true;
	}

	private boolean cleanSystemEnvironment(String safsdir){
		String libpath = SELENIUMLIBPath + s;
		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "selenium-plus-", null);
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

		removeSystemEnvironmentSubstringContaining("CLASSPATH", libpath + "JSTAFEmbedded.jar", null);
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
	 * Unset the System Environment Variables used by SeleniumPlus.
	 * Remove SELENIUM_PLUS and paths from CLASSPATH and PATH Environment Variables.
	 * Can unset SELENIUM_PLUS if args[0] is given a value to use.
	 * @param args
	 * @return
	 */
	@Override
	public boolean uninstall(String... args){
		String safsdir = null;
		if(args != null && args.length > 0) safsdir = args[0];
		if(safsdir == null) safsdir = getEnvValue(SELENIUMDIREnv);
		cleanSystemEnvironment(safsdir);
		if(safsdir == null || safsdir.length() == 0) return false;
		removeSystemEnvironmentSubstring("PATH", safsdir + SELENIUMBINPath, null);
		removeSystemEnvironmentSubstring("CLASSPATH", safsdir + SELENIUMLIBPath, null);
		if(propogate){
			InstallerImpl installer = null;

			if(Platform.isWindows()){
				//installer = new DLLInstaller();
				//installer.uninstall();
			}

			//install ghostscript
			try{
				installer = new GhostScriptInstaller(progresser, safsdir, null, silent, verbose, debug);
				installer.uninstall();
			}catch(Throwable t){
				setProgressMessage("Failed to uninstall '"+installer.getProductName()+"', due to "+ t.getClass().getSimpleName()+": "+ t.getMessage());
			}
		}
		if(Platform.isWindows()){
			setProgressMessage("Attempting shutdown of any running DesignerMon process.");
    		try{ NativeWrapper.runShortProcessAndWait("cscript.exe", safsdir +"\\extra\\ProcessKiller.vbs -noprompt -process wscript.exe -command \\extra\\ServiceMonitor.vbs");}
    		catch(Throwable ignore){}
			setProgressMessage("Attempting shutdown of any running SeleniumServer process.");
    		try{ NativeWrapper.runShortProcessAndWait("cscript.exe", safsdir +"\\extra\\ProcessKiller.vbs -noprompt -process java.exe -command \\libs\\selenium-server-standalone");}
    		catch(Throwable ignore){}
    		try{ NativeWrapper.runShortProcessAndWait("cscript.exe", safsdir +"\\extra\\ProcessKiller.vbs -noprompt -process wscript.exe -command \\install\\RemoteServerInstall.vbs");}
    		catch(Throwable ignore){}
    		try{ NativeWrapper.runShortProcessAndWait("cscript.exe", safsdir +"\\extra\\ProcessKiller.vbs -noprompt -process cmd.exe -command \\extra\\RemoteServer.bat");}
    		catch(Throwable ignore){}

	    	try{
	    		Hashtable results = NativeWrapper.runShortProcessAndWait("cscript.exe", safsdir +"\\uninstall\\UninstallSeleniumPlusProgramGroup.vbs");
	    		Integer rc = (Integer)results.get("Result");
	    		Vector out;
	    		if(rc.intValue()!= 0){
	    			progresser.setProgressMessage("UninstallSeleniumPlusProgramGroup failed with status "+ rc.intValue());
		    		out = (Vector)results.get("Vector");
		    		for(int i=0;i<out.size();i++){
		    			progresser.setProgressMessage(out.get(i).toString());
		    		}
		    		return false;
	    		}
				progresser.setProgressMessage("UninstallSeleniumPlusProgramGroup successful:");
	    		out = (Vector)results.get("Vector");
	    		for(int i=0;i<out.size();i++){
	    			progresser.setProgressMessage(out.get(i).toString());
	    		}
	    	}
	    	catch(Exception x){
	    		progresser.setProgressMessage("UninstallSeleniumPlusProgramGroup "+ x.getClass().getSimpleName()+": "+x.getMessage());
	    		return false;
	    	}
	    	NativeWrapper.RemoveRegistryKeyValue(HKLM_RUN_KEY, HKLM_RUN_MONITOR);
	    	NativeWrapper.RemoveRegistryKeyValue(HKLM_RUN_KEY, HKLM_RUN_SERVER);
		}
		// remove AFTER all the other uninstallers have used it.
		return true;
	}

	/**
    * This Installer provides no GUI, but will accept some configuration parameters.
    * Any user-specified directories must exist; or, we must not be denied the ability to
    * create them and write/copy files to them.
    * <p>
    *
    * @param args String[]<br>
    * The following parameters or arguments can be specified:
    * <ul>
    *   <li>&lt;SeleniumPlusPath><br>
    *     the root directory where SeleniumPlus resides.<br>
    *     This is usually the directory where the Setup script resides.
    *     <p>
    *   <li>-u<br>
    *     perform an uninstall instead of install.
    *     <p>
    *   <li>-np<br>
	*     If the -np option is not provided, then the installer will propogate and execute
	*     other necessary Installers.
    *     <p>
    *   <li>-nostaf<br>
    *     Skip the installation of STAF.
    *     <p>
    *   <li>-staf "alternate STAF directory"<br>
    *     Install STAF files and services to the user specified directory
    *     instead of the default install directory.<br>
    *     The -staf argument does not have to be specified to install to the
    *     default directory (root\STAF).
    *     <p>
    *   <li>-removestaf installedstafdir<br>
    *     Remove the installed STAF.
    *     installedstafdir indicates the directory where staf is installed, like "C:\STAF\" or "/usr/local/staf"
    *     <p>
    *   <li>-installstafexe alternateSTAFexecutable<br>
    *     Indicate an alternate (newer) STAF installation executable to use.<br>
    *     This will override the STAF 3 or later default executable.
    *     <p>
    * </ul>
    *
	 * Main Java executable.  Primarily to run standalone outside of a larger process.
	 * <p>
	 * %SELENIUMPLUSDIR%\Java\jre\bin\java org.safs.install.SeleniumPlusInstaller [C:\SeleniumPlus]
	 * <p>
	 * %SELENIUMPLUSDIR%\Java\jre\bin\java org.safs.install.SeleniumPlusInstaller -u [C:\SeleniumPlus]
	 * <p>
	 * If the -np option is not provided, then the installer will propogate and execute
	 * other necessary SAFS Framework Installers.
	 * <p>
	 * System.exit(0) on perceived success.<br>
	 * System.exit(-1) on perceived failure.
	 * <p><ul>
	 * "-u"  -- to perform an uninstall instead of install.<br>
	 * "-np" -- do not propogate to other installers.<br>
	 * Any first arg other than (-u or -np) will be considered the path for SELENIUMPLUSDIR.<br>
	 * The path for SELENIUMPLUSDIR does NOT need to be provided if the System Environment Variable
	 * already exists with a valid setting.<br>
	 * </ul>
	 * @see org.safs.install.SilentInstaller
	 * @see #propogate
	 */
	public static void main(String[] args) {
		SeleniumPlusInstaller installer = new SeleniumPlusInstaller();
		IndependantLog.setDebugListener(installer);

		initialize();
		parseArgs(args);
		printArgs(args);

		int status = -1;
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
            	progresser.createAndShowGUI();
            }
        });

		boolean success = true;

		if(uninstall){
			progresser.setProgressMessage("Uninstalling TestDesigner.");
			if(removestaf){
				progresser.setProgressMessage("Removing TestDesigner STAF Harness...");
				success = doSTAFUnInstall() == 0;
				pctProgress += pctIncrement;
				progresser.setProgress(pctProgress);
				progresser.setProgressMessage("Finished Uninstalling TestDesigner STAF Harness.");
			}
			success = installer.uninstall(seleniumdir);
			pctProgress += pctIncrement;
			progresser.setProgress(pctProgress);
			progresser.setProgressMessage("Deleting assets at "+ seleniumdir);

			File aFile = new CaseInsensitiveFile(seleniumdir).toFile();
			File[] files = aFile.listFiles();
			for (int i=0; i<files.length; i++){
				if(!isSelPlusProject(files[i]))
					FileUtilities.deleteDirectoryRecursively(files[i].getAbsolutePath(), false);
			}
			pctProgress += pctIncrement;
			progresser.setProgress(pctProgress);
			progresser.setProgressMessage("Removing Environment "+ SELENIUMDIREnv);
			setEnvValue(SELENIUMDIREnv, null);
			pctProgress = 100;
			progresser.setProgress(pctProgress);
			progresser.setProgressMessage("Finished Uninstalling TestDesigner.");
		}else{
			success = installer.install(seleniumdir);
			if(success && installstaf){
				progresser.setProgressMessage("Installing TestDesigner STAF Harness...");
				pctProgress += pctIncrement;
				progresser.setProgress(pctProgress);
				success = doSTAFInstall()==0;
				if(success){
					pctProgress += pctIncrement;
					progresser.setProgress(pctProgress);
					progresser.setProgressMessage("TestDesigner STAF Harness Ready.");
				}else{
					progresser.setProgressMessage("TestDesigner Failed install STAF Harness.");
				}
			}

			if(!success){
				progresser.setProgressMessage("Aborting the install.");
			}

			pctProgress = 100;
			progresser.setProgress(pctProgress);
		}
		System.exit(success ? 0:1);
	}

	@Override
	public String getListenerName() {
		return getClass().getSimpleName();
	}

	@Override
	public void onReceiveDebug(String message) {
		System.out.println(message);
	}

	/**
	 * Check if SeleniumPlus project or not
	 * @param directory
	 * @return -- true or false
	 */
	public static boolean isSelPlusProject(File directory) {
		String[] files = directory.list();
		if (files != null) {
			for(int i=0; i<files.length; i++){
				if(files[i].equalsIgnoreCase(".project")){
					return true;
				}
			}
		}
		return false;
	}
}
