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
/*************************************************************************************************************************
 *
 *
 * @author Carl Nagle  Original Release
 * @author Lei Wang JUN 28, 2009  Added arguments "-removestaf" and "-installstafversion" for installing/removing STAF 2.X/3.X.
 * @author JunwuMa Jun 29, 2009  Update not to overwrite the existing SAFS files unless these SAFS files have new version.
 * @author Carl Nagle  OCt 06, 2009  Update to use the STAF333 NoJVM EXE, if possible.
 * @author Lei Wang OCT 27, 2010  Do some modifications for installation on Linux. (Red Hat Enterprise Linux Server release 5.3 (Tikanga))
 * @author Dharmesh4 AUG 17, 2011  Do some modifications for installation on MAC.(Version: Darwin, 10.6.0)
 * @author Carl Nagle AUG 02, 2012  Support -installstafexe alternate executable for STAF 3 and later.
 * @author Lei Wang JUL 24, 2013  Add ProgressIndicator, use Console to detect OS, remove redundant code.
 *                              Move some codes to org.safs.text.FileUtilities
 * @author Lei Wang JAN 17, 2019 Modified constant DEFAULT_LINUX_STAF_3_BIN to use STAF341-setup-linux-NoJVM.bin,
 *                              the embedded JRE seems cannot work on certain Linux (Red Hat Enterprise Linux Server 7.1 Maipo).
 * @author Lei Wang JAN 22, 2019 Modified _getEmbeddedJavaExe(): Use 64 bit Java if we are on 64 bit Linux/Mac OS.
 * @author Lei Wang JAN 23, 2019 Modified doSTAFInstall(): do not specify the JVM when installing STAF for Linux system.
 * @author Lei Wang JAN 24, 2019 Use appropriate STAF installer (32 bit or 64 bit according to Linux OS architecture)
 * @author Lei Wang JAN 25, 2019 Modified doSTAFInstall(): On Unix system, if STAF fails to install in Java process, create a shell script to install it.
 * @author Lei Wang APR 10, 2019 Add chooseSTAFBitness(): provide user a chance to choose the bit-ness of STAF to install.
 * @author Lei Wang MAY 08, 2019 Modified doSTAFInstall(): choose STAF's bit-ness (32 or/and 64 bits) and the preferred STAF to install.
 * @author Lei Wang MAY 10, 2019 Modified doSTAFInstall(): If user provides its own STAF installer, we only use it to install.
 *
 * @see org.safs.install.ConsumOutStreamProcess
 */
package org.safs.install;

import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.safs.Constants.LogConstants;
import org.safs.android.auto.lib.Console;
import org.safs.install.InstallerImpl.BIT_OPTION;
import org.safs.natives.NativeWrapper;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;

/*************************************************************************************************************************
 *
 *
 * @author Carl Nagle  Original Release
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
	/** "C:\STAF" for 32-bit STAF installation **/
	public static final String DEFAULT_WIN_STAF_DIR = "c:\\staf";
	/** "_64" appending to 'stafdir' for 64-bit STAF installation **/
	public static final String SUFFIX_STAF_64_DIR = "_64";
	/** "/usr/local/safs" **/
	public static final String DEFAULT_UNX_SAFS_DIR = "/usr/local/safs";
	/** "/usr/local/staf" **/
	public static final String DEFAULT_UNX_STAF_DIR = "/usr/local/staf";
	/** "/Library/safs" **/
	public static final String DEFAULT_MAC_SAFS_DIR = "/Library/safs";
	/** "/Library/staf" **/
	public static final String DEFAULT_MAC_STAF_DIR = "/Library/staf";

	//static final String DEFAULT_WIN_STAF_JAR = "STAF250-setup-win32.exe";
	//static final String DEFAULT_WIN_STAF_JAR = "java -jar STAF251-setup-win32.jar";
	//static final String DEFAULT_WIN_STAF_2_JAR = " java -jar STAF2611-setup-win32.jar ";
	static final String DEFAULT_WIN_STAF_3_EXE = "STAF3426-setup-win32-NoJVM.exe";
	static final String DEFAULT_WIN_64_STAF_3_EXE = "STAF3426-setup-winamd64-NoJVM.exe";
	static final String DEFAULT_LINUX_STAF_3_BIN = "./STAF341-setup-linux-NoJVM.bin";
	static final String DEFAULT_LINUX_64_STAF_3_BIN = "./STAF341-setup-linux-amd64-NoJVM.bin";
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
	/** If user provides its own staf installer by parameter <b>-installstafexe stafInstaller</b>, this field will be set to true. */
	static boolean userProvidedSTAFInstaller = false;

	/** Keep a list of STAF-bitness (32, 64) to install, the last one is preferred and
	 *  will be installed at the last moment to override the environments set by the previous installation. */
	static List<BIT_OPTION>  installStafPreferredList = new ArrayList<BIT_OPTION>();

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
	static int pctSTAFUninstall = 10;
	static int pctSAFSUninstall = 20;
	static int pctSAFSInstall   = 30;
	static int pctSAFSOverlay   = 40;
	static int pctCopyEmbedded  = 50;
	static int pctPostInstall   = 60;
	static int pctPreSTAFInstall = 70;
	static int pctPostSTAFInstall = 95;

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
			if(Console.is64BitOS()){
				installstafexe = DEFAULT_WIN_64_STAF_3_EXE;
			}else{
				installstafexe = DEFAULT_WIN_STAF_3_EXE;
			}
			progresser.setTitle("SAFS Windows Installation");
		}else if(Console.isUnixOS()){
			installedstafdir = DEFAULT_UNX_STAF_DIR;
			safsdir = DEFAULT_UNX_SAFS_DIR;
			stafdir = DEFAULT_UNX_STAF_DIR;
			overlayfile = ZIP_OVERLAY_FILE;
			if(Console.is64BitOS()){
				installstafexe = DEFAULT_LINUX_64_STAF_3_BIN;
			}else{
				installstafexe = DEFAULT_LINUX_STAF_3_BIN;
			}
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
					userProvidedSTAFInstaller = true;
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
		catch(FileNotFoundException fnf){
			progresser.setProgressMessage("Met Exception "+fnf.getMessage(), LogConstants.WARN);
			FileUtilities.unzipFile("."+File.separator+"install"+File.separator+ZIP_INSTALL_FILE, root, verbose);
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
		CaseInsensitiveFile ifile = new CaseInsensitiveFile(installer);
		if(! ifile.exists()) ifile = new CaseInsensitiveFile(safsdir+File.separator+"install", installer);
		if(! ifile.exists()) throw new FileNotFoundException(installer);
		return ifile.getPath();
	}

	private static String _getEmbeddedJavaExe() throws FileNotFoundException{
		String java = File.separator+"jre"+File.separator+"bin"+File.separator+"java.exe";
		if(Console.isUnixOS() || Console.isMacOS()){
			if(Console.is64BitOS()){
				java = File.separator+"jre"+File.separator+"Java64"+File.separator+File.separator+"jre"+File.separator+"bin"+File.separator+"java";
			}else{
				java = File.separator+"jre"+File.separator+"bin"+File.separator+"java";
			}
		}

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
	 * This method will help user to choose which STAF (32 or 64 bit version, or both) to install.<br>
	 * If user choose both 32 and 64 bit STAF, this method will help user to choose which one is preferred.<br>
	 *
	 * After execution, the list {@link #installStafPreferredList} will contain {@link BIT_OPTION}s.<br>
	 */
	private static void chooseSTAFBitness(){
		//Choose the executable 32bits or/and 64bits to install
		JLabel dialogMessage = new JLabel("Install STAF, choose 32 bits or/and 64 bits.");
		JCheckBox bit64 = new JCheckBox(BIT_OPTION.BIT_64.name);
		JCheckBox bit32 = new JCheckBox(BIT_OPTION.BIT_32.name);
		JLabel indication = new JLabel();
		JPanel panel = new JPanel();
		JPanel options = new JPanel();
		panel.setLayout(new GridLayout(3, 1));
		options.add(bit32);
		options.add(bit64);
		panel.add(dialogMessage);
		panel.add(options);
		panel.add(indication);

		progresser.setProgressMessage("The default STAF's executable is "+installstafexe);

		if(Console.is64BitOS()){
			progresser.setProgressMessage("Choose 32 or/and 64 bits STAF to install on 64 bits OS.");
			bit64.setSelected(true);
			int response = TopMostOptionPane.showConfirmDialog(progresser, panel, "Choose STAF bit-ness.", JOptionPane.OK_CANCEL_OPTION);

			if (response==JOptionPane.CANCEL_OPTION || response==JOptionPane.CLOSED_OPTION) {
				//If users click the cancel button or close button
				progresser.setProgressMessage("User canceled the selection, will install 64 bits STAF.", LogConstants.WARN);
				installStafPreferredList.add(BIT_OPTION.BIT_64);
				return;
			}

			//If user wants to install both 32 band 64 bit STAF, we need to know which one is preferred (concerning STAFDIR environments).
			if(bit32.isSelected() && bit64.isSelected()){
				dialogMessage = new JLabel("You want to install both 32 bits and 64 bits STAF, which one is preferred?");
				JRadioButton bit64Preferred = new JRadioButton(BIT_OPTION.BIT_64.name);
				JRadioButton bit32Preferred = new JRadioButton(BIT_OPTION.BIT_32.name);
				ButtonGroup bitBtnGroup = new ButtonGroup();
				indication = new JLabel();
				panel = new JPanel();
				options = new JPanel();
				panel.setLayout(new GridLayout(3, 1));
				bitBtnGroup.add(bit32Preferred);
				bitBtnGroup.add(bit64Preferred);
				options.add(bit32Preferred);
				options.add(bit64Preferred);
				panel.add(dialogMessage);
				panel.add(options);
				panel.add(indication);

				bit64Preferred.setSelected(true);

				response = TopMostOptionPane.showConfirmDialog(progresser, panel, "Choose preferred STAF.", JOptionPane.OK_CANCEL_OPTION);

				if (response==JOptionPane.CANCEL_OPTION || response==JOptionPane.CLOSED_OPTION) {
					//If users click the cancel button or close button
					installStafPreferredList.add(BIT_OPTION.BIT_32);
					installStafPreferredList.add(BIT_OPTION.BIT_64);
					progresser.setProgressMessage("User canceled the selection, 64 bits STAF is preferred.", LogConstants.WARN);
					return;
				}

				if(bit64Preferred.isSelected()){
					installStafPreferredList.add(BIT_OPTION.BIT_32);
					installStafPreferredList.add(BIT_OPTION.BIT_64);
				}else{
					installStafPreferredList.add(BIT_OPTION.BIT_64);
					installStafPreferredList.add(BIT_OPTION.BIT_32);
				}
			}else if(bit64.isSelected()){
				installStafPreferredList.add(BIT_OPTION.BIT_64);
			}else if(bit32.isSelected()){
				installStafPreferredList.add(BIT_OPTION.BIT_32);
			}else{
				progresser.setProgressMessage("User doesn't select any STAF to install, 64 bits STAF will be installed.", LogConstants.WARN);
				installStafPreferredList.add(BIT_OPTION.BIT_64);
				return;
			}
		}else{
			//We cannot install 64 bits application on 32 bits OS.
			bit32.setSelected(true);
			bit64.setEnabled(false);
			installStafPreferredList.add(BIT_OPTION.BIT_32);
			progresser.setProgressMessage("We don't need to choose STAF bitness (32 or 64) on 32 bits OS, only 32 bit STAF can be installed.");
		}

		progresser.setProgressMessage("User will install "+Arrays.toString(installStafPreferredList.toArray())+" bits STAF.");
	}

	/**
	 * Perform STAF install.
	 * If user provides its own STAF-installer, then we use it to install STAF.<br>
	 * Otherwise, we ask user to choose the bit-ness (32 or/and 64 bits) of STAF to install.<br>
	 * If both 32 and 64 bits STAF are selected to install, user needs to choose which one is preferred.<br>
	 * The preferred STAF will be installed later so that the system environments will be updated, it is important.<br>
	 *
	 **/
	static int doSTAFInstall(String version){
		int status = 0;

		if(userProvidedSTAFInstaller){
			status = _doSTAFInstall(version);
		}else{

			chooseSTAFBitness();

			for(BIT_OPTION bitness: installStafPreferredList){
				//Switch 'installstafexe' according to STAF bitness
				if(Console.isWindowsOS()){
					if(bitness.equals(BIT_OPTION.BIT_32)){
						installstafexe = DEFAULT_WIN_STAF_3_EXE;
						progresser.setProgressMessage("Installing 32 bits STAF for Windows.");
					}else{
						installstafexe = DEFAULT_WIN_64_STAF_3_EXE;
						progresser.setProgressMessage("Installing 64 bits STAF for Windows.");
					}
				}else if(Console.isUnixOS()){
					if(bitness.equals(BIT_OPTION.BIT_32)){
						installstafexe = DEFAULT_LINUX_STAF_3_BIN;
						progresser.setProgressMessage("Installing 64 bits STAF for Linux.");
					}else{
						installstafexe = DEFAULT_LINUX_64_STAF_3_BIN;
						progresser.setProgressMessage("Installing 64 bits STAF for Linux.");
					}
				}else if(Console.isMacOS()){
					if(bitness.equals(BIT_OPTION.BIT_32)){
						installstafexe = DEFAULT_MAC_STAF_3_BIN;
						progresser.setProgressMessage("Installing 32 bits STAF for Mac.");
					}else{
						progresser.setProgressMessage("Installing 64 bits STAF for "+Console.getOsFamilyName()+" has not been supported, skipped.", LogConstants.WARN);
						status--;
						continue;
					}
				}else{
					progresser.setProgressMessage("Installing STAF for "+Console.getOsFamilyName()+" has not been supported, skipped.", LogConstants.WARN);
					status--;
					continue;
				}

				//Switch 'stafdir' according to STAF bitness
				if(bitness.equals(BIT_OPTION.BIT_32)){
					if(stafdir.endsWith(SUFFIX_STAF_64_DIR)){
						stafdir = stafdir.substring(0, stafdir.indexOf(SUFFIX_STAF_64_DIR));
					}
					progresser.setProgressMessage("Installing 32 bits STAF to directory "+stafdir+" by executable "+installstafexe);
				}else{
					if(!stafdir.endsWith(SUFFIX_STAF_64_DIR)){
						stafdir = stafdir+SUFFIX_STAF_64_DIR;
					}
					progresser.setProgressMessage("Installing 64 bits STAF to directory "+stafdir+" by executable "+installstafexe);
				}

				status += _doSTAFInstall(version);
			}
		}

		return status;
	}

	/**
	 * Perform STAF install.
	 * If -silent parameter was specified then a silent install will be done.<br>
	 * Currently all installs are silent.<br>
	 * If -staf parameter was specified then the install will attempt to use any
	 * provided user-specified directory for the install.
	 **/
	private static int _doSTAFInstall(String version){

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

					//Lei Wang: S1477584: STAF installer cannot access the "embedded JRE"!
					//./STAF341-setup-linux-NoJVM.bin: line 2466: /tmp/install.dir.10152/"/usr/local/safs/jre/Java64/jre/bin/java": No such file or directory
					//STAF install exit code: 127
					//comment out '_try_STAF_LAX_VM_Option'.
					//cmd = _try_STAF_LAX_VM_Option(cmd);

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
		progresser.setProgressMessage("STAF installing to '"+stafdir+"' ...");

		progresser.setProgress(pctPreSTAFInstall);

		// Execute the installation command by shell
		ConsumOutStreamProcess p = new ConsumOutStreamProcess(cmd, verbose, false);
		status = p.start();
		progresser.setProgressMessage("STAF install exit code: "+ status);

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
					p = new ConsumOutStreamProcess(stafInstallScript.getAbsolutePath(), verbose, false);
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

		progresser.setProgress(pctPreSTAFInstall + 10);

		progresser.setProgressMessage("Deleting STAF IBM Registration file...");
		File regFile = new CaseInsensitiveFile(stafdir + File.separator+ STAF_REG_FILE).toFile();
		if (regFile.exists()) { regFile.delete(); }
		progresser.setProgressMessage("Performing STAF post-install configuration...");
		STAFInstaller si = new STAFInstaller();
		si.install(stafdir);
		progresser.setProgressMessage("Finished Installation of STAF.");
		progresser.setProgress(pctPostSTAFInstall);

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
    		if(Console.isUnixOS() || Console.isMacOS()){
    			progresser.setProgressMessage("'SAFS Program Group' has not yet been supported in Linux/Mac.");
    			return true;
    		}
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
            @Override
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
			progresser.setProgress(pctPostSTAFInstall);

			if(installsafs) createSAFSProgramGroup(safsdir);
			progresser.setProgress(100);

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

