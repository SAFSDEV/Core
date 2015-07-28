/** Copyright (C) SAS Institute, Inc. All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.install;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

import org.safs.natives.NativeWrapper;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.android.auto.lib.Console;
import org.safs.text.FileUtilities;

/*************************************************************************************************************************
 * 
 * 
 * @author Carl Nagle  Original Release
 * @author LeiWang JUN 28, 2009  Added arguments "-removestaf" and "-installstafversion" for installing/removing STAF 2.X/3.X.
 * @author JunwuMa Jun 29, 2009  Update not to overwrite the existing SAFS files unless these SAFS files have new version.
 * @author Carl Nagle  OCt 06, 2009  Update to use the STAF333 NoJVM EXE, if possible.
 * @author LeiWang OCT 27, 2010  Do some modifications for installation on Linux. (Red Hat Enterprise Linux Server release 5.3 (Tikanga))
 * @author Dharmesh4 AUG 17, 2011  Do some modifications for installation on MAC.(Version: Darwin, 10.6.0)
 * @author Carl Nagle AUG 02, 2012  Support -installstafexe alternate executable for STAF 3 and later.
 * @author Lei Wang JUL 24, 2013  Add ProgressIndicator, use Console to detect OS, remove redundant code.
 *                              Move some codes to org.safs.text.FileUtilities
 *                               
 * @see org.safs.install.ConsumOutStreamProcess                              
 */

public class SilentInstaller {

	/** "SAFSInstall.zip" */
	static final String ZIP_INSTALL_FILE    = "SAFSInstall.ZIP";
	/** "SAFSSnapshot.zip" */
	static final String ZIP_OVERLAY_FILE    = "SAFSSnapshot.ZIP";
		
	/** "C:\SAFS" **/
	public static final String DEFAULT_WIN_SAFS_DIR = "c:\\safs";
	/** "C:\STAF" **/
	public static final String DEFAULT_WIN_STAF_DIR = "c:\\staf";
	/** "/usr/local" **/
	public static final String DEFAULT_UNX_SAFS_DIR = "/usr/local/safs";
	public static final String DEFAULT_UNX_STAF_DIR = "/usr/local/staf";
	/** "/Library" **/
	public static final String DEFAULT_MAC_SAFS_DIR = "/Library/safs";
	public static final String DEFAULT_MAC_STAF_DIR = "/Library/staf";

	//static final String DEFAULT_WIN_STAF_JAR = "STAF250-setup-win32.exe";
	//static final String DEFAULT_WIN_STAF_JAR = "java -jar STAF251-setup-win32.jar";
	//static final String DEFAULT_WIN_STAF_2_JAR = " java -jar STAF2611-setup-win32.jar ";
	static final String DEFAULT_WIN_STAF_3_EXE = "STAF3411-setup-win32-NoJVM.exe";
	static final String DEFAULT_LINUX_STAF_3_BIN = "./STAF341-setup-linux.bin";
	static final String DEFAULT_MAC_STAF_3_BIN = "./STAF346-setup-macosx-i386.bin";

	static final String STAF_REG_FILE        = "STAFReg.inf";	

	/** "-nosafs" **/
	public static final String OPTION_NOSAFS  = "-nosafs";
	/** "-nostaf" **/
	public static final String OPTION_NOSTAF  = "-nostaf";
	/** "-removestaf" **/
	public static final String OPTION_REMOVE_STAF  = "-removestaf";
	/** "-removesafs" **/
	public static final String OPTION_REMOVE_SAFS  = "-removesafs";
	/** "-installstafversion" 
	 * Ex: -installstafversion 2  (or 3) **/
	public static final String OPTION_STAF_TO_INSTALL  = "-installstafversion";
	/** "-installstafexe" 
	 * Ex: -installstafexe STAF3410-setup-win32-NoJVM.exe **/
	public static final String OPTION_STAF_INSTALL_EXE  = "-installstafexe";
	/** "-silent" **/
	public static final String OPTION_SILENT  = "-silent";	
	/** "-safs" **/
	public static final String OPTION_ALTSAFS = "-safs";
	/** "-overlay" **/
	public static final String OPTION_OVERLAY = "-overlay";
	/** "-staf" **/
	public static final String OPTION_ALTSTAF = "-staf";	
	/** "-v" **/
	public static final String OPTION_VERBOSE = "-v";
	
	static boolean overlaysafs = false;
	static boolean installsafs = true;
	static boolean removestaf = false;
	static boolean removesafs = false;
	static boolean installstaf = true;
	static boolean installtcafs = true;
	static boolean verbose = false;
	static String  installstafversion = "3";
	static String  installedstafdir = DEFAULT_WIN_STAF_DIR;
	static String  installedsafsdir = DEFAULT_WIN_SAFS_DIR;
	static String  installstafexe = "";
	static String  safsdir = "";
	static String  stafdir = "";
	static String  overlayfile = "";
	//static String  staf_2_silent  = " -silent -W license.selection=\"Accept\" ";
	//static String  staf_2_set_install_dir  = " -W stafinstalldirectory.defaultInstallLocation=";
	public static final String STAF_LAX_VM = "LAX_VM";//Specify the JVM to install STAF, LAX_VM "javaPathBin\java.exe"
	public static final String SAFSEmbeddedJavaExe = File.separator+"jre"+File.separator+"bin"+File.separator+"java.exe";
	static String  staf_3_silent  = " -i silent -DACCEPT_LICENSE=1 ";
	static String  staf_3_set_install_dir  = " -DUSER_INSTALL_DIR=";
	static String  staf_3_register_0  = " -DREGISTER=0 ";
	static String  staf_3_loginstart_0  = " -DSTART_ON_LOGIN=0 ";
	
	static String  safs_silent  = "-silent";
	static String  staf_uninstall_2 = "\\_uninst\\uninstaller.exe -silent ";
	static String  staf_uninstall_3 = "\\Uninstall_STAF\\Uninstall_STAF.exe -i silent";
	static String  staf_uninstall_3_linux = "/Uninstall_STAF/Uninstall_STAF -i silent";
	static String  staf_uninstall_3_mac = "/Uninstall_STAF/Uninstall_STAF.command -i silent";
	static String  stafMajorVersionTobeRemoved = "0";
	static boolean installsilent = true;
	
	static int pctProgress = 0;
	static int pctIncrement = 14;
	static int pctSTAFUninstall = pctIncrement;                    // 14
	static int pctSAFSUninstall = pctSTAFUninstall + pctIncrement; // 28
	static int pctSAFSInstall = pctSAFSUninstall + pctIncrement;   // 42
	static int pctSAFSOverlay = pctSAFSInstall + pctIncrement;     // 56
	static int pctCopyEmbedded = pctSAFSOverlay + pctIncrement;    // 70
	static int pctPostInstall = pctCopyEmbedded + pctIncrement;    // 85
	static int pctSTAFInstall = 100;

	static Console console = null;
	/** progressbar is a swing panel to show the progress of installation.*/
	static ProgressIndicator progresser = new ProgressIndicator();
	
	static{
		System.out.println ("Installing on '"+ Console.OS_NAME+"' System");

		if(Console.isWindowsOS()){
			installedstafdir = DEFAULT_WIN_STAF_DIR;
			safsdir = DEFAULT_WIN_SAFS_DIR;
			stafdir = DEFAULT_WIN_STAF_DIR;
			overlayfile = ZIP_OVERLAY_FILE;
			installstafexe = DEFAULT_WIN_STAF_3_EXE;
			progresser.setTitle("SAFS Windows Installation");
		}else if(Console.isUnixOS()){
			installedstafdir = DEFAULT_UNX_STAF_DIR;
			safsdir = DEFAULT_UNX_SAFS_DIR;
			stafdir = DEFAULT_UNX_STAF_DIR;
			overlayfile = ZIP_OVERLAY_FILE;
			installstafexe = DEFAULT_LINUX_STAF_3_BIN;
			installtcafs = false;
			progresser.setTitle("SAFS Unix Installation");
		}else if(Console.isMacOS()){
			installedstafdir = DEFAULT_MAC_STAF_DIR;
			safsdir = DEFAULT_MAC_SAFS_DIR;
			stafdir = DEFAULT_MAC_STAF_DIR;
			overlayfile = ZIP_OVERLAY_FILE;
			installstafexe = DEFAULT_MAC_STAF_3_BIN;
			installtcafs = false;
			progresser.setTitle("SAFS Mac Installation");
		}else{
			System.err.println("Operating System '"+Console.OS_NAME+"' not yet supported!");
		}
		
		try{
			console = Console.get();
		}catch(UnsupportedOperationException e){
			System.err.println("Can't get Console instance for Operating System '"+Console.OS_NAME+"' Exception="+e.getMessage());
		}
	}
	
	static void printArgs(String[] args){
		System.out.println( args.length + " arguments provided...");
		for(int i = 0; i < args.length; i++){
			System.out.println(args[i]);
		}
		System.out.println ("");
		System.out.println ("Overlaying SAFS: "+ overlaysafs);
		System.out.println ("Installing SAFS: "+ installsafs);
		System.out.println ("SAFS InstallDir: "+ safsdir);
		System.out.println ("");
		System.out.println ("Installing STAF: "+ installstaf);
		System.out.println ("STAF  Installer: "+ installstafexe);		
		System.out.println ("STAF Versioning: "+ installstafversion);		
		System.out.println ("STAF InstallDir: "+ stafdir);		
	}


	static void parseArgs(String[] args){
		int argc = args.length;
		int ip = 0;
		String tversion = null;
		for(int i = 0; i < argc; i++){
			String arg = args[i].toLowerCase();

			if (arg.equals(OPTION_NOSAFS)) { installsafs   = false; continue; }
			if (arg.equals(OPTION_NOSTAF)) { installstaf   = false; continue; }
			if (arg.equals(OPTION_SILENT)) { installsilent = true;  continue; }
			
			if (arg.equals(OPTION_REMOVE_STAF)) {
				if (i < argc -1){
					installedstafdir = args[++i];
					installedstafdir = installedstafdir.trim();
				}
				if (i < argc -1){
					ip = i + 1;
					tversion = args[ip];
					try{
						stafMajorVersionTobeRemoved = String.valueOf(Integer.parseInt(tversion.trim()));
						stafMajorVersionTobeRemoved = stafMajorVersionTobeRemoved.trim();
						i = ip;
					}catch(NumberFormatException nan){
						stafMajorVersionTobeRemoved = "3";
					}
				}
				if(stafMajorVersionTobeRemoved.equals("0")) stafMajorVersionTobeRemoved = "3";
				removestaf = true;
				installstaf = false;
				installsafs = false;
				continue;
			}
			if (arg.equals(OPTION_REMOVE_SAFS)) {
				if (i < argc -1){
					installedsafsdir = args[++i];
					installedsafsdir = installedsafsdir.trim();
				}
				removesafs = true;
				installsafs = false;
				installstaf = false;
				continue;
			}
			if (arg.equals(OPTION_OVERLAY)) {
				if (i < argc -1) {
					overlayfile = args[++i];
					overlaysafs = true;
					installsafs = false;
					installstaf = false;
				}
				continue; 
			}
			
			if (arg.equals(OPTION_ALTSAFS)) { 
				if (i < argc -1) safsdir = args[++i];
				continue;
			}
			
			if (arg.equals(OPTION_ALTSTAF)) { 
				if (i < argc -1) stafdir = args[++i];
				continue;
			}

			if (arg.equals(OPTION_STAF_TO_INSTALL)) { 
				if (i < argc -1){
					installstafversion = args[++i];
					installstafversion = installstafversion.trim();
				}
				continue;
			}
			
			if (arg.equals(OPTION_STAF_INSTALL_EXE)) { 
				if (i < argc -1) {
					installstafexe = args[++i];
					installstafexe = installstafexe.trim();
				}
				continue;
			}
			
			if(arg.equals(OPTION_VERBOSE)){
				verbose = true;
				continue;
			}
		}
	}

	/**
	 * Perform SAFS install.
	 * The SAFS install is currently always silent (GUI-less).<br>
	 * If -safs parameter was specified then the install will attempt to use any 
	 * provided user-specified directory for the install.
	 * <p>
     * Any user-specified directories must exist; or, we must not be denied the ability to 
     * create them and write/copy files to them.
	 **/
	static int doSAFSInstall() throws IOException, FileNotFoundException{
		
		int     status        = -1;
		
		progresser.setProgressMessage("SAFS InstallDir: "+ safsdir);
		progresser.setProgressMessage("Start SAFS Extraction......");
		
		// create/verify safsdir
		File root = new CaseInsensitiveFile(safsdir).toFile();
		
		if (root.exists()){
			if (!root.isDirectory()) {
			    throw new FileNotFoundException(
			    "Specified install path must be a DIRECTORY.");
			}			
		}
		// root does NOT exist
		else{
			if(! root.mkdirs()){
			    throw new FileNotFoundException(
			    "Specified install path could not be created.");
			}			
		}
		
		progresser.setProgressMessage("Unzip "+ZIP_INSTALL_FILE+" ......");
		try{ 
			FileUtilities.unzipFile(ZIP_INSTALL_FILE, root, verbose);}
		catch(FileNotFoundException nf){
			FileUtilities.unzipFile(".\\install\\"+ZIP_INSTALL_FILE, root, verbose);
		}
		status = 0;
	
		progresser.setProgressMessage("Finished SAFS Extraction.");
		return status;
	}	
	
	/**
	 * Perform SAFS Overlay.
	 * The SAFS overlay is currently always silent (GUI-less).<br>
	 * If -overlay parameter was specified then the install will attempt to use the 
	 * provided Snapshot ZIP file for the overlay.
	 * <p>
     * The overlay ZIP file must exist.  An overlay can be applied immediately 
     * following a SAFSInstall during the same invocation of this installer.
	 **/
	static int doSAFSOverlay() throws IOException, FileNotFoundException{
		int     status        = -1;
		
		progresser.setProgressMessage("SAFS Install Dir : "+ safsdir);
		progresser.setProgressMessage("SAFS Overlay File: "+ overlayfile);
		// create/verify safsdir
		File root = new CaseInsensitiveFile(safsdir).toFile();
		
		if (root.exists()){
			if (!root.isDirectory()) {
			    throw new FileNotFoundException(
			    "Specified install path must be a DIRECTORY.");
			}			
		}
		// root does NOT exist
		else{
		    throw new FileNotFoundException(
		    "SAFS Installation must already exist for an OVERLAY to be applied!");
		}
		
		progresser.setProgressMessage("Unzip "+overlayfile+" ......");
		FileUtilities.unzipFile(overlayfile, root, verbose);
		status = 0;
		
		progresser.setProgressMessage("Finished SAFS Overlay.");		
		return status;
	}	

	private static String _findInstallSTAFExe(String installer)throws FileNotFoundException{
		String fullpath = installer;
		CaseInsensitiveFile ifile = new CaseInsensitiveFile(installer);
		if(! ifile.exists()) ifile = new CaseInsensitiveFile(safsdir+File.separator+"install", installer);
		if(! ifile.exists()) throw new FileNotFoundException(installer);
		return ifile.getPath();
	}
	
	private static String _getEmbeddedJavaExe() throws FileNotFoundException{
		String java = File.separator+"jre"+File.separator+"bin"+File.separator+"java.exe";
		CaseInsensitiveFile ifile = new CaseInsensitiveFile(safsdir + java);
		if(! ifile.exists()) throw new FileNotFoundException(java);
		return ifile.getPath();
	}
	
	private static String _try_STAF_LAX_VM_Option(String installer){
		String fullpath = installer;
		if(installer.indexOf("NoJVM")>0){
			try{
				String java = _getEmbeddedJavaExe();
				fullpath += " "+STAF_LAX_VM+" "+"\""+ java +"\"";
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
	static int doSTAFInstall(String version){

		int status = -1;
		String cmd = "";
		
		try{
			if(Console.isWindowsOS()){
				//if(version.equals("2")){
				//	cmd += DEFAULT_WIN_STAF_2_JAR + staf_2_silent + staf_2_set_install_dir+"\""+ stafdir +"\"";
				//}else 
				if(version.equals("3")){
					cmd = _findInstallSTAFExe(installstafexe);
					cmd = _try_STAF_LAX_VM_Option(cmd);
					cmd += " "+ staf_3_silent + staf_3_set_install_dir+"\""+ stafdir +"\"";
					cmd += " "+ staf_3_register_0 + staf_3_loginstart_0;
				}else{
					System.err.println("Can not install STAF for version="+version);
					return status;
				}
			}else if(Console.isUnixOS()){
				//For linux system, only STAF 3 is supported for now, no STAF 2
				if(version.equals("3")){
					//cmd += DEFAULT_LINUX_STAF_3_BIN + staf_3_silent + staf_3_set_install_dir+"\""+ stafdir +"\"";
					//If we put a string command including quotes in the Linux Shell directly, the shell will remove 
					//the quotes then execute the command, so there is no problem. But if we put the same command to 
					//Java Runtime, the runtime has no ability to remove the quotes, so it fails to execute the command
					//We should not use quote to surround stafdir, if so, "stafdir" including quote will be considered  
					//as the install directory, which will cause error!!!
					cmd = _findInstallSTAFExe(installstafexe);
					cmd = _try_STAF_LAX_VM_Option(cmd);
					cmd += " "+ staf_3_silent + staf_3_set_install_dir+"\""+ stafdir +"\"";
					cmd += " "+ staf_3_register_0 + staf_3_loginstart_0;
				}else{
					System.err.println("Can not install STAF for version="+version);
					return status;
				}
			}else if(Console.isMacOS()){
				//For mac system, only STAF 3 is supported for now, no STAF 2
				if(version.equals("3")){
					cmd = _findInstallSTAFExe(installstafexe);
					cmd = _try_STAF_LAX_VM_Option(cmd);
					cmd += " "+ staf_3_silent + staf_3_set_install_dir+"\""+ stafdir +"\"";
					cmd += " "+ staf_3_register_0 + staf_3_loginstart_0;
				}else{
					System.err.println("Can not install STAF for version="+version);
					return status;
				}
			}else{
				System.err.println(Console.OS_NAME+" has not been supported!!!");
				return status;
			}
		}catch(FileNotFoundException nf){
			progresser.setProgressMessage("STAF Install FAILED. Installer was NOT found. "+ nf.getMessage());
			return status;
		}

		progresser.setProgressMessage("STAF Install: "+ cmd);
		progresser.setProgressMessage("Please wait while STAF installs.");
		progresser.setProgressMessage("This may take a few moments......");

		// If the directory where staf will be installed exists, delete it firstly.
		progresser.setProgressMessage("Delete old STAF files from '"+stafdir+"' ...");
		FileUtilities.deleteDirectoryRecursively(stafdir, verbose);
		progresser.setProgress(pctSTAFInstall + (pctIncrement /2));
		
		// Execute the installation command by shell
		ConsumOutStreamProcess p = new ConsumOutStreamProcess(cmd, verbose, false);
		status = p.start();
		progresser.setProgressMessage("STAF install exit code: "+ status);
		
		File regFile = new CaseInsensitiveFile(stafdir + File.separator+ STAF_REG_FILE).toFile();
		if (regFile.exists()) { regFile.delete(); }
		STAFInstaller si = new STAFInstaller();
		si.install(stafdir);
		progresser.setProgressMessage("Finished Installation of STAF.");

		return status;
	}
	
	/**
	 * Perform uninstall STAF.
	 * Uninstall STAF silently.<br>
	 **/
	static int doSTAFUnInstall(String version){

		int status = -1;
		String cmd = "";
		if(version == null) version = "3";
		//If installedstafdir contains a \ at the end, remove it.
		if(installedstafdir.endsWith(File.separator)){
			installedstafdir = installedstafdir.substring(0, installedstafdir.length()-1);
		}
		
		if(Console.isWindowsOS()){
			if(version.equals("2")){
				cmd = installedstafdir+staf_uninstall_2;
			}else if(version.equals("3")){
				cmd = installedstafdir+staf_uninstall_3;
			}else{
				System.err.println("Can not unInstall STAF for version="+version);
				return status;
			}
		}else if(Console.isUnixOS()){
			if(version.equals("3")){
				cmd = installedstafdir+staf_uninstall_3_linux;
			}else{
				System.err.println("Can not unInstall STAF for version="+version);
				return status;
			}
		}else if(Console.isMacOS()){
			if(version.equals("3")){
				cmd = installedstafdir+staf_uninstall_3_mac;
			}else{
				System.err.println("Can not unInstall STAF for version="+version);
				return status;
			}
		}else{
			System.err.println(Console.OS_NAME+" has not been supported!!!");
			return status;
		}

		progresser.setProgressMessage("STAF UnInstall: "+ cmd);
		progresser.setProgressMessage("Please wait while STAF UnInstalls.");
		progresser.setProgressMessage("This may take a few moments...");
		ConsumOutStreamProcess p = new ConsumOutStreamProcess(cmd,verbose,false);
		status = p.start();

		if(status!=ConsumOutStreamProcess.PROCESS_NORMAL_END){
			progresser.setProgressMessage("STAF UnInstalls Failed.");
			return status;
		}
		//If uninstaller worked correctly, then try to delete all remains files.
		status = FileUtilities.deleteDirectoryRecursively(installedstafdir, verbose);
		if(Console.isUnixOS() || Console.isMacOS() ){
			//There are some symbolic links in directory staf, so try to use 'rm -rf dir' to delete it
			status = deleteAllDirectory(installedstafdir);
		}else{
    		try{ 
    			NativeWrapper.runShortProcessAndWait("cscript.exe", safsdir +"\\bin\\UninstallSTAFProgramGroup.vbs");
    			NativeWrapper.runShortProcessAndWait("cscript.exe", safsdir +"\\uninstall\\UninstallSTAFProgramGroup.vbs");
    		}
	    	catch(Throwable ignore){
	    		progresser.setProgressMessage("WARNING: Uninstalling the STAF Program Group may not have been successful.");
	    	}
		}
		NativeWrapper.RemoveSystemEnvironmentVariable("STAFDIR");
		progresser.setProgressMessage("STAF UnInstalls Finished.");
		return status;
	}
	
	static int doSAFSUnInstall(){
		progresser.setProgressMessage("SAFS UnInstall: "+ installedsafsdir);
		progresser.setProgressMessage("This may take a few moments...");

		int status = FileUtilities.deleteDirectoryRecursively(installedsafsdir, verbose);
		progresser.setProgressMessage("Completed SAFS UnInstallation.");
		return status;
	}

	static boolean createSAFSProgramGroup(String safsdir){
    	try{
    		Hashtable results = NativeWrapper.runShortProcessAndWait("cscript.exe", safsdir +"\\install\\CreateSAFSProgramGroup.vbs");
    		Integer rc = (Integer)results.get("Result");
    		Vector out;
    		if(rc.intValue()!= 0){
    			progresser.setProgressMessage("Create SAFS Program Group failed with status "+ rc.intValue());
	    		out = (Vector)results.get("Vector");
	    		for(int i=0;i<out.size();i++){
	    			progresser.setProgressMessage(out.get(i).toString());
	    		}
	    		return false;
    		}
			progresser.setProgressMessage("Create SAFS Program Group successful:");
    		out = (Vector)results.get("Vector");
    		for(int i=0;i<out.size();i++){
    			progresser.setProgressMessage(out.get(i).toString());
    		}
    	}
    	catch(Exception x){
    		progresser.setProgressMessage("Create SAFS Program Group "+ x.getClass().getSimpleName()+": "+x.getMessage());
    		return false;
    	}	    
    	return true;
	}
	
	
	/**
	 * If the uninstaller can not delete directory completely, we need to clean the remain files.
	 * Use DOS COMMAND "rmdir /S/Q dir" to delete the whole directory.
	 * @param directory
	 * @return
	 */
	static int deleteAllDirectory(String directory){
		//If directory does not exist, just return 0.
		File aFile = new CaseInsensitiveFile(directory).toFile();
		if( !aFile.exists()){
			System.err.println("Warning: File '"+aFile.getAbsolutePath()+"' doesn't exist! Can't delete.");
			return 0;
		}

		if(console!=null && console.deleteDirectory(directory)) return 0;
		else return 1;
	}
	
	/**
	 * Must be called AFTER a SAFS Install or Overlay.
	 * @param jrepath -- path to JRE root dir to be copied as an embedded JRE.
	 * Can be null. If null, we seek it in the user's current working directory.  
	 * This is presumably where the install was launched from.
	 * @return 0 on success or "not needed". Thrown Exception if a problem occurred.
	 * @throws FileNotFoundException
	 */
	static int copyEmbeddedJRE(String jrepath) throws IOException{
		int     status        = -1;
		
		progresser.setProgressMessage("Evaluating Embedded JRE...");
		// create/verify safsdir
		File root = new CaseInsensitiveFile(safsdir).toFile();		
		if (root.exists()){
			if (!root.isDirectory()) {
			    throw new FileNotFoundException(
			    "Specified SAFSDIR path must be a DIRECTORY.");
			}			
		}
		// root does NOT exist
		else{
		    throw new FileNotFoundException(
		    "SAFS Installation must already exist for the Embedded JRE to be copied.");
		}
		String workdir = System.getProperty("user.dir");
		// if the install ZIP was exploded at SAFSDIR then the JRE is already there.
		if(workdir.equalsIgnoreCase(safsdir)){
			progresser.setProgressMessage("Do not need to copy embedded JRE.");
			return 0;
		}
		File jreSource = new CaseInsensitiveFile(workdir, "jre").toFile();
		if(!jreSource.exists()){
			progresser.setProgressMessage("No Embedded JRE to copy.");
			return 0;
		}
		// copy the JRE subdir to the SAFSDIR\JRE subdir
		File jreTarget = new CaseInsensitiveFile(root, "jre").toFile();
		if(! jreTarget.mkdirs()){
			if(!jreTarget.exists())
		    throw new FileNotFoundException(
		    "Specified Embedded JRE path could not be created.");
		}					
		progresser.setProgressMessage("Copying Embedded JRE to "+ jreTarget.getPath());
		try{ FileUtilities.copyDirectoryRecursively(jreSource, jreTarget); }
		catch(Exception x){ throw new IOException(x.getMessage()); }
			
		status = 0;		
		progresser.setProgressMessage("Finished Copying JRE.");		
		return status;
	}
	
	/**
	 * Must be called AFTER a SAFS Install or Overlay.
	 * @param installpath -- path to install root dir to be copied to new install dir.
	 * Can be null. If null, we seek it in the user's current working directory.  
	 * This is presumably where the install was launched from.
	 * @return 0 on success or "not needed". Thrown Exception if a problem occurred.
	 * @throws FileNotFoundException
	 */
	static int copyInstallDir(String installpath) throws IOException{
		int     status        = -1;
		
		progresser.setProgressMessage("Evaluating /install/ directory");
		// create/verify safsdir
		File root = new CaseInsensitiveFile(safsdir).toFile();		
		if (root.exists()){
			if (!root.isDirectory()) {
			    throw new FileNotFoundException(
			    "Specified SAFSDIR path must be a DIRECTORY.");
			}			
		}
		// root does NOT exist
		else{
		    throw new FileNotFoundException(
		    "SAFS Installation must already exist for the install directory to be copied.");
		}
		String workdir = System.getProperty("user.dir");
		// if the install ZIP was exploded at SAFSDIR then the install directory is already there.
		if(workdir.equalsIgnoreCase(safsdir)){
			progresser.setProgressMessage("Do not need to copy install directory.");
			return 0;
		}
		File installSource = new CaseInsensitiveFile(workdir, "install").toFile();
		if(!installSource.exists()){
			progresser.setProgressMessage("No install directory to copy.");
			return 0;
		}
		// copy the install subdir to the SAFSDIR\install subdir
		File installTarget = new CaseInsensitiveFile(root, "install").toFile();
		if(! installTarget.mkdirs()){
			if(!installTarget.exists())
		    throw new FileNotFoundException(
		    "Specified install path could not be created.");
		}					
		progresser.setProgressMessage("Copying Install directory to "+ installTarget.getPath());
		try{ FileUtilities.copyDirectoryRecursively(installSource, installTarget); }
		catch(Exception x){ throw new IOException(x.getMessage()); }
			
		status = 0;		
		progresser.setProgressMessage("Finished Copying Install Directory.");		
		return status;
	}
	
	/**
	 * Must be called AFTER a SAFS Install or Overlay.
	 * @param uninstallpath -- path to uninstall root dir to be copied to new uninstall dir.
	 * Can be null. If null, we seek it in the user's current working directory.  
	 * This is presumably where the install was launched from.
	 * @return 0 on success or "not needed". Thrown Exception if a problem occurred.
	 * @throws FileNotFoundException
	 */
	static int copyUninstallDir(String uninstallpath) throws IOException{
		int     status        = -1;
		
		progresser.setProgressMessage("Evaluating /uninstall/ directory");
		// create/verify safsdir
		File root = new CaseInsensitiveFile(safsdir).toFile();		
		if (root.exists()){
			if (!root.isDirectory()) {
			    throw new FileNotFoundException(
			    "Specified SAFSDIR path must be a DIRECTORY.");
			}			
		}
		// root does NOT exist
		else{
		    throw new FileNotFoundException(
		    "SAFS Installation must already exist for the uninstall directory to be copied.");
		}
		String workdir = System.getProperty("user.dir");
		// if the install ZIP was exploded at SAFSDIR then the uninstall directory is already there.
		if(workdir.equalsIgnoreCase(safsdir)){
			progresser.setProgressMessage("Do not need to copy uninstall directory.");
			return 0;
		}
		File uninstallSource = new CaseInsensitiveFile(workdir, "uninstall").toFile();
		if(!uninstallSource.exists()){
			progresser.setProgressMessage("No uninstall directory to copy.");
			return 0;
		}
		// copy the uninstall subdir to the SAFSDIR/uninstall subdir
		File uninstallTarget = new CaseInsensitiveFile(root, "uninstall").toFile();
		if(! uninstallTarget.mkdirs()){
			if(!uninstallTarget.exists())
		    throw new FileNotFoundException(
		    "Specified uninstall path could not be created.");
		}					
		progresser.setProgressMessage("Copying Uninstall directory to "+ uninstallTarget.getPath());
		try{ FileUtilities.copyDirectoryRecursively(uninstallSource, uninstallTarget); }
		catch(Exception x){ throw new IOException(x.getMessage()); }
			
		status = 0;		
		progresser.setProgressMessage("Finished Copying Uninstall Directory.");		
		return status;
	}
	
	/**
	 * Runs AFTER SAFS has been laid down, or uninstalled.
	 * @param install
	 * @return
	 */
	static int doSAFSInstallers(){
		
		SAFSInstaller si = new SAFSInstaller();
		si.setProgressIndicator(progresser);
		if(installsafs || overlaysafs){
			si.install(safsdir);
		}else if (removesafs){
			si.uninstall(safsdir);
		}		
		return 0;
	}
	
    /**
    * This SilentInstaller provides no GUI, but will accept some configuration parameters.
    * Any user-specified directories must exist; or, we must not be denied the ability to 
    * create them and write/copy files to them.
    * <p>
    * 
    * @param args[] The following parameters or arguments can be specified:<br>
    * <ul>
    *   <li>-silent<br>
    *     A silent STAF install with provided parameters.  At this time, 
    *     a STAF silent install is performed with or without this parameter.<br>
    *     Currently, the install for SAFS is always silent (GUI-less).
    *     <p>
    *   <li>-nosafs<br>
    *     Skip the installation of SAFS files and associated services.
    *     <p>
    *   <li>-nostaf<br>
    *     Skip the installation of STAF.
    *     <p>
    *   <li>-notcafs<br>
    *     Skip the installation of TCAFS support registration.
    *     <p>
    *   <li>-safs "alternate SAFS directory"<br>
    *     Install SAFS files and services to the user specified directory 
    *     instead of the default install directory.<br>
    *     The -safs argument does not have to be specified to install to the 
    *     default directory (root\SAFS).
    *     <p>
    *   <li>-staf "alternate STAF directory"<br>
    *     Install STAF files and services to the user specified directory 
    *     instead of the default install directory.<br>
    *     The -staf argument does not have to be specified to install to the 
    *     default directory (root\STAF).
    *     <p>
    *   <li>-overlay SAFSSnapshot.ZIP<br>
    *     Overlay SAFS files over an existing SAFS installation.<br>
    *     The -safs argument does not have to be specified to overlay the 
    *     default directory (root\SAFS).<br>
    *     The overlay can occur immediately following a new installation 
    *     during the same class invocation.
    *     <p>
    *   <li>-removestaf installedstafdir stafMajorVersionTobeRemoved<br>
    *     Remove the installed STAF.
    *     installedstafdir indicates the directory where staf is installed, like "C:\STAF\" or "/usr/local/staf"
    *     stafMajorVersionTobeRemoved indicates the major version of STAF, like 2 or 3
    *     <p>
    *   <li>-installstafversion installstafversion<br>
    *     Indicate the major version of STAF you want to install. The default is 3.
    *     You can specify it to 2 for STAF 2.6.11 etc.
    *     You can specify it to 3 for STAF 3.3.3  etc.
    *     This option take effect only option "-nostaf" does NOT present
    *     <p>
    *   <li>-installstafexe alternateSTAFexecutable<br>
    *     Indicate an alternate (newer) STAF installation executable to use.<br>
    *     This will override the STAF 3 or later default executable.
    *     <p>
    *   <li>-removesafs installedsafs<br>
    *     Remove the installed SAFS.
    *     installedsafs indicates the directory where SAFS is installed, like "C:\SAFS\" or "/usr/local/safs"
    *     <p>
    *   <li>-v<br>
    *     Verbose to see the detail installation or un-installation information
    *     <p>
    * </ul>
    * 
    **/
	public static void main(String[] args) {

		parseArgs(args);
//		printArgs(args);
		
		int status = -1;		
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	progresser.createAndShowGUI();
            }
        });
        
		try{
			if (removestaf){status = doSTAFUnInstall(stafMajorVersionTobeRemoved);}
			progresser.setProgress(pctSTAFUninstall);
			if (removesafs && installsafs){ status = doSAFSUnInstall(); }
			progresser.setProgress(pctSAFSUninstall);
   	        if (installsafs){ status = doSAFSInstall(); }
			progresser.setProgress(pctSAFSInstall);
   	        if (overlaysafs){ status = doSAFSOverlay(); }
			progresser.setProgress(pctSAFSOverlay);
   	        if (installsafs || overlaysafs) { 
   	        	copyEmbeddedJRE(null); 
   	        	copyInstallDir(null); 
   	        	copyUninstallDir(null); 
   	        }
			progresser.setProgress(pctCopyEmbedded);
			
        	doSAFSInstallers();        	
			progresser.setProgress(pctPostInstall);
	        
			if (installstaf){ status = doSTAFInstall(installstafversion);}
			progresser.setProgress(pctSTAFInstall);
			
			if(installsafs) createSAFSProgramGroup(safsdir);
			
		}catch(FileNotFoundException fnf){
			System.out.println(" ");
			System.out.println("Installation Error:");
			System.out.println(fnf.getMessage());
			fnf.printStackTrace();
			status = -1;
		}catch(IOException io){
			System.out.println(" ");
			System.out.println("File Extraction Error:");
			System.out.println(io.getMessage());
			io.printStackTrace();
			status = -2;
		}
				
	    System.exit(status);
	}
}

