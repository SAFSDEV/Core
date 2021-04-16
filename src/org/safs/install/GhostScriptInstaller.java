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
 * Logs for developers, not published to API DOC.
 *
 * History:
 * JUL 02, 2018    (Lei Wang) Used NativeWrapper.is64BitOS() to detect OS architecture.
 * JUL 03, 2018    (Lei Wang) Made 64 bit ghostscript as default to install.
 *                           Moved getting registry code to NativeWrapper.getRegistry32Prodcut/getRegistry64Prodcut.
 *                           Fixed the problem if user click the 'close' button when choosing 'product bit' to install.
 *                           Clear the registry if the un-installer cannot be found.
 *                           Added debug log file c:\GhostScriptInstaller_debug_log.txt
 * JUL 09, 2018    (Lei Wang) Install both 32-bit and 64-bit ghostscript. Don't set GS_HOME anymore.
 *                           Uninstall all installed ghostscript.
 * MAR 31, 2020    (Lei Wang) Modified install(), uninstall(): don't show the 'confirm dialog' if in silent mode.
 *
 */
package org.safs.install;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.safs.Constants;
import org.safs.Constants.LogConstants;
import org.safs.Constants.RegistryConstants;
import org.safs.IndependantLog;
import org.safs.natives.NativeWrapper;
import org.safs.sockets.DebugListener;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;

/**
 * Install/Uninstall GhostScript.<br>
 *
 * @author Lei Wang
 */
public class GhostScriptInstaller extends InstallerImpl implements DebugListener{

	/** <b>GHOSTSCRIPT</b> */
	public static final String PRODUCT_NAME 							= "GHOSTSCRIPT";
	/** <b>GS_HOME</b> the environment to keep the product installation path */
	public static final String ENV_HOME 								= "GS_HOME";

	/** <b>GS_DLL</b> the environment to keep the path of ghostscript dll */
	public static final String ENV_GS_DLL 								= "GS_DLL";

	/** <b>GS_LIB</b> the environment to keep the path of ghostscript library files */
	public static final String ENV_GS_LIB 								= "GS_LIB";

	/** <b>GHOSTSCRIPT INSTALLATION</b> */
	public static final String TITLE	 								= "GHOSTSCRIPT INSTALLATION";

	/** <b>9.23</b> current install version */
	public static final String CURRENT_VERSION 							= "9.23";
	/** [<b>{@link #CURRENT_VERSION}</b>, ... ] an array of supported version, {@link #CURRENT_VERSION} should always be put at the first position */
	public static final String[] SUPPORTED_VERSION 						= {CURRENT_VERSION};

	/** <b>c:\GhostScriptInstaller_debug_log.txt</b> the default debug log */
	private static final String debugLogFile = "c:\\GhostScriptInstaller_debug_log.txt";

	/**
	 * A convenient product detector of ghostscript. It is cast from super class field {@link #productDetector}.
	 *
	 * @see #initilizeProductDetector()
	 */
	protected ProductDetectorGhostScript myProductDetector = null;

	private BufferedWriter debuglog = null;

	public GhostScriptInstaller(ProgressIndicator progresser, String safsHome){
		super();
		initilizeProductDetector();
		setProgressIndicator(progresser);
		this.safsHome = safsHome;
	}

	public GhostScriptInstaller(ProgressIndicator progresser, String safsHome, String home){
		this(progresser, safsHome);
		this.home = home;
	}

	public GhostScriptInstaller(ProgressIndicator progresser, String safsHome, String home, boolean silent, boolean verbose, boolean debug){
		this(progresser, safsHome, home);
		this.silent = silent;
		this.verbose = verbose;
		this.debug = debug;
		if(debug) initDebugLog();
	}

	@Override
	public String getListenerName() {
		return PRODUCT_NAME+" Debug";
	}

	@Override
	public void onReceiveDebug(String message) {
		try { if(debuglog!=null) debuglog.write(message+"\n"); } catch (IOException e) {}
	}

	private void initDebugLog(){
		if(IndependantLog.getDebugListener()==null){
			try {
				debuglog = FileUtilities.getSystemBufferedFileWriter(debugLogFile);
				IndependantLog.setDebugListener(this);
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						if(debuglog!=null) try {debuglog.close(); } catch (IOException e) {}
					}
				});
			} catch (FileNotFoundException e1) {
			}
		}
	}

	@Override
	protected String getProductName(){
		return PRODUCT_NAME;
	}

	/**
	 * @return String, the environment name to store the home directory of this product
	 */
	protected String envHome(){
		return ENV_HOME;
	}

	/**
	 * Install both 32-bit and 64-bit ghostscript.<br>
	 * Add bin directory to the PATH environment.<br>
	 *
	 * See <a href="https://www.ghostscript.com/doc/9.23/Install.htm">Install ghostscript</a>.
	 */
	@Override
	public boolean install(String... optionals) {
		String product = getProductName();
//		String preferredHome = productDetector.getPreferredHome();
//		if(preferredHome!=null){
//			setProgressMessage("The product '"+product+"' is already installed.");
//			return true;
//		}

		//Check if the 32 bit and 64 bit ghostscript have already been installed
		boolean bit32Installed = false;
		boolean bit64Installed = false;

		Map<String, String> installedProducts = productDetector.getHomes();
		if(!installedProducts.isEmpty()){
			for(String key:installedProducts.keySet()){
				if(key.contains(BIT_OPTION.BIT_32.name)) bit32Installed=true;
				if(key.contains(BIT_OPTION.BIT_64.name)) bit64Installed=true;
			}
		}
		if(bit32Installed && bit64Installed){
			setProgressMessage("The product "+product+"'s 32bit version and 64bit version are both already installed.");
			return true;
		}

		//Choose the executable 32bits or 64bits to install the ghostscript
		List<BIT_OPTION> bitOptionToInstall = new ArrayList<BIT_OPTION>();
		JCheckBox bit64 = new JCheckBox(BIT_OPTION.BIT_64.name);
		JCheckBox bit32 = new JCheckBox(BIT_OPTION.BIT_32.name);
		JLabel indication = new JLabel();
		JPanel panel = new JPanel();
		JPanel options = new JPanel();
		panel.setLayout(new GridLayout(2, 1));
		options.add(bit64);
		options.add(bit32);
		panel.add(options);
		panel.add(indication);

		if(bit32Installed){
			bit32.setEnabled(false);
			indication.setText("32 bit ghost script has already been insalled.");
		}else{
			bit32.setSelected(true);
		}
		if(bit64Installed){
			bit64.setEnabled(false);
			indication.setText("64 bit ghost script has already been insalled.");
		}else{
			bit64.setSelected(true);
		}

		if(silent){
			setProgressMessage("Silently chose "+ (!bit32Installed?"32":"")+" "+(!bit64Installed?"64":"")+" bit version to install ...");
		}else{
			setProgressMessage("Choosing 32 or 64 bit version to install ...");
			int response = TopMostOptionPane.showConfirmDialog(progresser, panel, "Install "+product+", choose version.", JOptionPane.OK_CANCEL_OPTION);

			if (response==JOptionPane.CANCEL_OPTION || response==JOptionPane.CLOSED_OPTION) {
				//If users click the cancel button or close button
				setProgressMessage("User canceled the installation.", LogConstants.WARN);
				return true;
			}
		}

		if(bit64.isSelected()) bitOptionToInstall.add(BIT_OPTION.BIT_64);
		if(bit32.isSelected()) bitOptionToInstall.add(BIT_OPTION.BIT_32);

		BIT_OPTION bitOption = null;
		for(int i=0;i<bitOptionToInstall.size();i++){
			bitOption = bitOptionToInstall.get(i);
			productDetector.setProductBit(bitOption);
			install(productDetector, null, Boolean.toString(silent));
		}

		return true;
	}

	/**
	 * @param productDetector IProductDetector, the detector to get product's information
	 * @param optionals String[] optional parameters
	 * <ul>
	 *   <li>optionals[0] home String, the home directory where the product is installed
	 *   <li>optionals[1] silent boolean (default is true), if the installation is in silent mode
	 * </ul>
	 * @return boolean, if the installation succeeds.
	 */
	private boolean install(IProductDetector productDetector, String... optionals){
		String product = getProductName();

		//home is the installation directory, can be provided by optionals parameter
		String home = null;
		boolean silent = true;
		if(optionals!=null){
			if(optionals.length>0) home=optionals[0];
			if(optionals.length>1) silent=Boolean.parseBoolean(optionals[1]);
		}

		//Prepare the installation directory
		if (home == null){
			home = productDetector.getDefaultHome();
			setProgressMessage("Cannot deduce the home directory for product '"+product+"', will install on default path '"+home+"'");
		}

		if (home == null){
			setProgressMessage("Cannot deduce the home directory for product '"+product+"', abort installation.", LogConstants.ERROR);
			return false;
		}

		//If the directory where the product will be installed exists, delete it firstly.
		if(new CaseInsensitiveFile(home).toFile().exists()){
			FileUtilities.deleteDirectoryRecursively(home, verbose);
			setProgressMessage("Deleted old '"+product+"' files from '"+home+"' ...");
		}

		setProgressMessage("installing '"+product+"' to directory '"+home+"' ...");
		increaseProgress(1);

		//gs923w64.exe /D C:\gs
		String cmd = "";
		try {
			cmd = findInstaller(productDetector.getInstaller());
		} catch (FileNotFoundException e) {
			setProgressMessage("Cannot find "+product+"'s installer!", LogConstants.ERROR);
			return false;
		}

		if(silent) cmd = productDetector.appendSientOption(cmd);
		//TODO the option "/D" doesn't work, comment it
//		cmd = productDetector.appendInstallDirOption(cmd, home);

		setProgressMessage("Executing "+cmd);
		setProgressMessage("This may take a few moments......");

		// Execute the installation command by shell
		ConsumOutStreamProcess p = new ConsumOutStreamProcess(cmd, verbose, false);
		int status = p.start();
		increaseProgress(2);

		if(status!=ConsumOutStreamProcess.PROCESS_NORMAL_END){
			setProgressMessage("Installation failed with exit code: "+status, LogConstants.ERROR);
			return false;
		}else{
			setProgressMessage("It seems that "+product+" has been installed successfully, exit code: "+ status);
		}

		//Verify to confirm the installation path
		setProgressMessage("Verify the installation path ... ");
		String homeFromRegistry = productDetector.getPreferredHome();
		if(!home.equals(homeFromRegistry)){
			setProgressMessage("Installation path verification failed: expected path '"+home+"' does NOT equal actual path '"+homeFromRegistry+"'", LogConstants.ERROR);
			return false;
		}

		//Set the environments
//		setEnvironment(envHome(), home);
//		setEnvironment(ENV_GS_DLL, myProductDetector.getDllPath());
//		setEnvironment(ENV_GS_LIB, myProductDetector.getLibraryPath());

		//Append the PATH environment
		String binDirString = home.endsWith(File.separator)? home+DIR_BIN : home+File.separator+DIR_BIN;
		appendSystemEnvironment(Constants.ENV_PATH, binDirString, null);
		setProgressMessage("Append environment '"+Constants.ENV_PATH+"' with value '"+binDirString+"'.");

		setProgressMessage("The product '"+product+"' has been successfully instlled.");

		return true;
	}

	/**
	 * UnInstall the all installed ghostscript.<br>
	 * Clear the HOME environment.<br>
	 * Clear the PATH environment.<br>
	 * Clear the Registry.<br>
	 *
	 * See <a href="https://www.ghostscript.com/doc/9.23/Install.htm">Install ghostscript</a>.
	 */
	@Override
	public boolean uninstall(String... args){

		String product = getProductName();
		setProgressMessage("Uninstall product '"+product+"'.");

		//Detect all the installed versions, let user to choose to delete
		Map<String, String> installedProducts = productDetector.getHomes();

		JLabel indication = new JLabel("All checked product will be uninstalled.");
		JPanel panel = new JPanel();
		JPanel options = new JPanel();
		panel.setLayout(new GridLayout(2, 1));
		panel.add(options);
		panel.add(indication);

		JCheckBox aProduct = null;
		if(!installedProducts.isEmpty()){
			for(String key:installedProducts.keySet()){
				aProduct = new JCheckBox(key);
				aProduct.setSelected(true);
				options.add(aProduct);
			}
		}else{
			setProgressMessage("Cannot detect any product to un-install.");
			return true;
		}

		if(silent){
			setProgressMessage("Uninstall silently ... ");
		}else{
			int response = TopMostOptionPane.showConfirmDialog(progresser, panel, "UnInstall "+product+".", JOptionPane.OK_CANCEL_OPTION);
			if (response==JOptionPane.CANCEL_OPTION || response==JOptionPane.CLOSED_OPTION) {
				//If users click the cancel button or close button
				setProgressMessage("User canceled the un-installation.", LogConstants.WARN);
				return true;
			}
		}

		BIT_OPTION bitOption = null;
		for(Component component: options.getComponents()){
			try{
				aProduct = (JCheckBox) component;
				if(aProduct.isSelected()){
					if(aProduct.getText().contains(BIT_OPTION.BIT_32.name)){
						bitOption = BIT_OPTION.BIT_32;
					}else{
						bitOption = BIT_OPTION.BIT_64;
					}
					productDetector.setProductBit(bitOption);
					uninstall(productDetector, installedProducts.get(aProduct.getText()), Boolean.toString(silent));
				}
			}catch(Exception e){
				IndependantLog.error("Failed to uninstall product "+product+", met "+e.getClass().getSimpleName()+":"+e.getMessage());
			}
		}

		return true;
	}

	/**
	 * @param productDetector IProductDetector, the detector to get product's information
	 * @param optionals String[] optional parameters
	 * <ul>
	 *   <li>optionals[0] home String, the home directory where the product is installed
	 *   <li>optionals[1] silent boolean (default is true), if the installation is in silent mode
	 * </ul>
	 * @return boolean, if the un-installation succeeds.
	 */
	private boolean uninstall(IProductDetector productDetector, String... optionals){
		String product = getProductName();
		//home is the installation directory, can be provided by optionals parameter
		String home = null;
		boolean silent = true;
		if(optionals!=null){
			if(optionals.length>0) home=optionals[0];
			if(optionals.length>1) silent=Boolean.parseBoolean(optionals[1]);
		}

		if(home==null) home = productDetector.getPreferredHome();

		if(home==null){
			setProgressMessage("Cannot find "+product+"'s home directory!", LogConstants.ERROR);
			return false;
		}

		setProgressMessage("Uninstalling product '"+product+"' from directory '"+home+"' ... ");

		try {
			String cmd = productDetector.findUnInstaller(home);
			if(silent) cmd = productDetector.appendSientOption(cmd);

			//TODO the option "/D" doesn't work, comment it
//			cmd = productDetector.appendInstallDirOption(cmd, homeDir);

			setProgressMessage("Executing "+cmd);
			setProgressMessage("This may take a few moments......");

			// Execute the installation command by shell
			ConsumOutStreamProcess p = new ConsumOutStreamProcess(cmd, verbose, false);
			int status = p.start();
			//The process executing ghostscript's un-installer "uninstgs.exe" will return terminate normally even
			//the user doesn't click 'Uninstall' button on pop-up window,
			//To fix that bug, we wait 5 seconds here, hope user will click the 'Uninstall' button within that time.
			//StringUtils.sleep(5000);
			increaseProgress(2);

			if(status!=ConsumOutStreamProcess.PROCESS_NORMAL_END){
				setProgressMessage("UnInstallation failed with exit code: "+status, LogConstants.ERROR);
				return false;
			}else{
				setProgressMessage("It seems that "+product+" has been uninstalled successfully, exit code: "+ status);
			}

		} catch (FileNotFoundException e) {
			setProgressMessage("Cannot find "+product+"'s uninstaller!", LogConstants.WARN);
			//Clear the registry
			if(productDetector instanceof ProductDetectorWindows){
				setProgressMessage("Clear registry by ourselves ");
				((ProductDetectorWindows) productDetector).deleteRegistry(home);
			}
		}

		//Clear the environments
//		setEnvironment(envHome(), null);
//		setEnvironment(ENV_GS_DLL, null);
//		setEnvironment(ENV_GS_LIB, null);

		//Remove ghostscript's bin folder from the PATH environment
		String binDirString = home.endsWith(File.separator)? home+DIR_BIN : home+File.separator+DIR_BIN;
		removeSystemEnvironmentSubstring(Constants.ENV_PATH, binDirString, null);
		setProgressMessage("Removed from environment '"+Constants.ENV_PATH+"' the value '"+binDirString+"'.");

		//Finally delete the whole directory
		if(new CaseInsensitiveFile(home).toFile().exists()){
			setProgressMessage("Delete '"+product+"' files from '"+home+"' ...");
			FileUtilities.deleteDirectoryRecursively(home, verbose);
		}

		increaseProgress(1);
		setProgressMessage("The product '"+product+"' has been successfully un-installed.");
		return true;
	}

	@Override
	protected void initilizeProductDetector() throws UnsupportedOperationException{
		super.initilizeProductDetector();
		myProductDetector = (ProductDetectorGhostScript) productDetector;
	}

	@Override
	protected IProductDetector getDefaultProductDetector(){
		return new ProductDetectorGhostScript();
	}

	@Override
	protected IProductDetector getWindowsProductDetector() throws UnsupportedOperationException{
		return new ProductDetectorWindows();
	}

	@Override
	protected IProductDetector getUnixProductDetector() throws UnsupportedOperationException{
		//Just return a default detector,
		//TODO For the real UNIX Product Detector, to be implemented later.
		return new ProductDetectorGhostScript();
	}

	private static class ProductDetectorGhostScript extends ProductDetectorDefault{
		public ProductDetectorGhostScript(){}
		public ProductDetectorGhostScript(BIT_OPTION productBit){
			super(productBit);
		}

		public String getDllPath(){
			throw new UnsupportedOperationException();
		}
		public String getLibraryPath(){
			throw new UnsupportedOperationException();
		}
	}

	private static class ProductDetectorWindows extends ProductDetectorGhostScript{
		/** executable gswin32.exe */
		private static final String gswin32 							= "gswin32.exe";
		/** executable gswin32c.exe */
		private static final String gswin32c 							= "gswin64c.exe";
		/** executable gswin64.exe */
		private static final String gswin64 							= "gswin64.exe";
		/** executable gswin64c.exe */
		private static final String gswin64c 							= "gswin64c.exe";

		/** <b>"Artifex\GPL Ghostscript\"</b> part of registry path 'HKEY_LOCAL_MACHINE\SOFTWARE\Artifex\GPL Ghostscript\version' */
		public static final String REG_SUFFIX_HOME 						= "Artifex\\GPL Ghostscript\\";//+version, "HKEY_LOCAL_MACHINE\\SOFTWARE\\Artifex\\GPL Ghostscript\\9.23"

		/** <b>"GPL Ghostscript\"</b> part of registry path "HKEY_LOCAL_MACHINE\SOFTWARE\GPL Ghostscript\version" */
		public static final String REG_SUFFIX_LIBRARY 					= "GPL Ghostscript\\";//+version, "HKEY_LOCAL_MACHINE\\SOFTWARE\\GPL Ghostscript\\9.23"

		//C:\Program Files (x86)\gs\gs9.23\bin\gsdll32.dll
		/** <b>"GPL GS_DLL"</b> registry value under path "HKEY_LOCAL_MACHINE\SOFTWARE\GPL Ghostscript\version" */
		public static final String REGISTRY_VALUE_DLL					= "GS_DLL";//under "HKEY_LOCAL_MACHINE\\SOFTWARE\\GPL Ghostscript\\9.23"
		//C:\Program Files (x86)\gs\gs9.23\bin;C:\Program Files (x86)\gs\gs9.23\lib;C:\Program Files (x86)\gs\gs9.23\fonts
		/** <b>"GPL GS_LIB"</b> registry value under path "HKEY_LOCAL_MACHINE\SOFTWARE\GPL Ghostscript\version" */
		public static final String REGISTRY_VALUE_LIB					= "GS_LIB";//under "HKEY_LOCAL_MACHINE\\SOFTWARE\\GPL Ghostscript\\9.23"

		public static final String INSTALLER_32BIT 							= "gs923w32.exe";
		public static final String INSTALLER_64BIT 							= "gs923w64.exe";

		public static final String UNINSTALLER	 							= "uninstgs.exe";

		public static final String INSTALL_PATH_DEFAULT_32BIT 				= "C:\\Program Files (x86)\\gs\\gs"+CURRENT_VERSION;
		public static final String INSTALL_PATH_DEFAULT_64BIT 				= "C:\\Program Files\\gs\\gs"+CURRENT_VERSION;

		/** <b>/S</b> to install/uninstall silently */
		public static final String OPTION_SILENT				= "/S";

		/** <b>/D</b> to indicate the directory for installation */
		public static final String OPTION_DIRECTORY				= "/D";

		public ProductDetectorWindows(){}
		public ProductDetectorWindows(BIT_OPTION productBit){
			super(productBit);
		}

		@Override
		public String getDefaultHome(){
			return BIT_OPTION.BIT_32.equals(productBit)? INSTALL_PATH_DEFAULT_32BIT : INSTALL_PATH_DEFAULT_64BIT;
		}

		//https://www.ghostscript.com/doc/9.23/Install.htm
		//The installer is NSIS-based (see also Release.htm) and supports a few standard NSIS
		//options: /NCRC disables the CRC check,
		// /S runs the installer or uninstaller silently,
		@Override
		public String appendSientOption(String command){
			return command + " " + OPTION_SILENT;
		}
		// /D sets the default installation directory (It must be the last parameter used in the command line and must not contain any quotes, even if the path contains spaces. Only absolute paths are supported)
		@Override
		public String appendInstallDirOption(String command, String installDirectory){
			return command + " " + OPTION_DIRECTORY+ " "+installDirectory;
		}

		@Override
		public String getUnInstaller(){
			return UNINSTALLER;
		}

		@Override
		public String getInstaller(){
			return BIT_OPTION.BIT_32.equals(productBit)? INSTALLER_32BIT: INSTALLER_64BIT;
		}

		@Override
		public Map<String, String> getHomes(){
			Map<String, String> homes = new LinkedHashMap<String, String>();
			String productPath = null;
			String key = null;

			setProgressMessage("Checking Windows Registry to get all installed products ...");

			setProgressMessage("Evaluating 32 bit products ...");
			for(String version: SUPPORTED_VERSION){
				productPath = NativeWrapper.getRegistry32Prodcut(RegistryConstants.HKLM_ST, REG_SUFFIX_HOME+version, RegistryConstants.VALUE_DEFAULT);
				key = PRODUCT_NAME+version+" "+BIT_OPTION.BIT_32;
				if(productPath!=null){
					store(homes, key, productPath);
				}
			}
			setProgressMessage("Evaluating 64 bit products ...");
			for(String version: SUPPORTED_VERSION){
				productPath = NativeWrapper.getRegistry64Prodcut(RegistryConstants.HKLM_ST, REG_SUFFIX_HOME+version, RegistryConstants.VALUE_DEFAULT);
				key = PRODUCT_NAME+version+" "+BIT_OPTION.BIT_64;
				if(productPath!=null){
					store(homes, key, productPath);
				}
			}

			return homes;
		}

		/**
		 * @param home String, the product home directory. It is used to get the matched product.
		 */
		public void deleteRegistry(String home){
			Map<String, String> homes = getHomes();
			String productToUninstall = null;
			for(String key : homes.keySet()){
				if(home.equals(homes.get(key))){
					productToUninstall = key;
					break;
				}
			}
			if(productToUninstall!=null){
				//GHOSTSCRIPT9.23 32 bits
				//GHOSTSCRIPT9.23 64 bits
				setProgressMessage("Clear Windows Registry for product '"+productToUninstall+"' installed at '"+home+"' ...");
				productBit = productToUninstall.indexOf(BIT_OPTION.BIT_32.toString())>-1? BIT_OPTION.BIT_32:BIT_OPTION.BIT_64;
				String productVersion = productToUninstall.substring(PRODUCT_NAME.length(), productToUninstall.length()-productBit.toString().length()).trim();

				deleteRegistry(RegistryConstants.HKLM_ST, REG_SUFFIX_HOME+productVersion, RegistryConstants.VALUE_DEFAULT);
				deleteRegistry(RegistryConstants.HKLM_ST, REG_SUFFIX_LIBRARY+productVersion, REGISTRY_VALUE_DLL);
				deleteRegistry(RegistryConstants.HKLM_ST, REG_SUFFIX_LIBRARY+productVersion, REGISTRY_VALUE_LIB);
			}
		}

		private void store(Map<String, String> homes, String key, String productPath){
			setProgressMessage("... FOUND Product '"+key+"', installed at '"+productPath+"'.");
			if(!homes.containsKey(key)){
				homes.put(key, productPath);
			}else{
				setProgressMessage("... Ignore Product '"+key+"', it is already in the Map with value as '"+homes.get(key)+"'.");
			}
		}

//		* First, try to get the environment value of {@link GhostScriptInstaller#ENV_HOME}, return if not null.<br>
		/**
		 * Check the versions of {@link GhostScriptInstaller#SUPPORTED_VERSION} in the registry, return the first non-null value.<br>
		 */
		@Override
		public String getPreferredHome(){
			String productPath = null;

			search:
			{
//				setProgressMessage("Evaluating environment '"+ENV_HOME+"' ...");
//				productPath = getEnvValue(ENV_HOME);
//				if(productPath!=null) break search;

				for(String version: SUPPORTED_VERSION){
					productPath = getRegistryValue(RegistryConstants.HKLM_ST, REG_SUFFIX_HOME+version, RegistryConstants.VALUE_DEFAULT);
					if(productPath!=null) break search;
				}
			}

			if(productPath==null){
				setProgressMessage("... NO product was detected!");
			}else{
				setProgressMessage("... FOUND Product, installed at '"+productPath+"'.");
			}

			return productPath;
		}

		@Override
		public String getDllPath(){
			String result = null;
			for(String version: SUPPORTED_VERSION){
				result = getRegistryValue(RegistryConstants.HKLM_ST, REG_SUFFIX_LIBRARY+version, REGISTRY_VALUE_DLL);
				if(result!=null) break;
			}
			return result;
		}
		@Override
		public String getLibraryPath(){
			String result = null;
			for(String version: SUPPORTED_VERSION){
				result = getRegistryValue(RegistryConstants.HKLM_ST, REG_SUFFIX_LIBRARY+version, REGISTRY_VALUE_LIB);
				if(result!=null) break;
			}
			return result;
		}

		@Override
		public String[] getPossibleExecutables() {
			if(BIT_OPTION.BIT_32.equals(productBit)){
				return new String[]{
						BIN+File.separator+gswin32,
						BIN+File.separator+gswin32c
						};
			}else if(BIT_OPTION.BIT_64.equals(productBit)){
				return new String[]{
						BIN+File.separator+gswin64,
						BIN+File.separator+gswin64c
						};
			}else{
				return new String[]{
						BIN+File.separator+gswin32,
						BIN+File.separator+gswin32c,
						BIN+File.separator+gswin64,
						BIN+File.separator+gswin64c
						};
			}
		}

	}

	private static String usage(){
		return "Usage:\njava org.safs.install.GhostScriptInstaller "+ARG_SAFS_DIR+" safsHome ["+ARG_UNINSTALL+"] ["+ARG_INSTALLDIR+" home] ["+ARG_SILENT+"] ["+ARG_VERBOSE+"] ["+ARG_DEBUG+"]";
	}

	/**
	 * Main Java executable.  Primarily to run standalone outside of a larger process.
	 * <p>
	 * %SELENIUM_PLUS%\jre\bin\java org.safs.install.GhostScriptInstaller <b>-safs safsHome</b> <b>[-u]</b> <b>[-installdir home]</b> <b>[-silent]</b> <b>[-v]</b> <b>[-debug]</b><br>
	 * <br>
	 * @param args
	 * <br>
	 * <b>-safs safsHome</b> required, is the SAFS/SeleniumPlus directory, its sub-folder 'install' holds the
	 *                       installer (executable) to install the product.<br>
	 * <b>-installdir home</b> <font color='red'>NOT SUPPORTED YET</font> optional, is the directory where the product will be installed to or un-installed from.<br>
	 *                		   if not provided, then the {@link #getDefaultHome()} will be used to get the directory from system environment.<br>
	 * <b>-u</b> optional, means to un-install the product; if not provided, then install the product.<br>
	 * <b>-debug</b> optional, means in debug mode, default is false<br>
	 * <b>-silent</b> optional, means in silent mode, default is true<br>
	 * <b>-v</b> optional, means in verbose mode, default is false<br>
	 *
	 * <p>
	 * <b>example:</b><br>
	 * %SELENIUM_PLUS%\jre\bin\java org.safs.install.GhostScriptInstaller -safs c:\SeleniumPlus<br>
	 * %SELENIUM_PLUS%\jre\bin\java org.safs.install.GhostScriptInstaller -safs c:\SeleniumPlus -u<br>
	 * <p>
	 * System.exit(0) on perceived success.<br>
	 * System.exit(-1) on perceived failure.<br>
	 * System.exit(-2) on any un-expected exception.<br>
	 *
	 * @see org.safs.install.SilentInstaller
	 */
	public static void main(String[] args) {
		boolean uninstall = false;
		boolean debug = false;
		boolean silent = false;
		boolean verbose = false;

		String safsHome = null;
		String home = null;


		try{
			for(int i=0;i<args.length;i++){
				if(args[i].equalsIgnoreCase(ARG_SAFS_DIR)){
					if(++i<args.length){
						safsHome = args[i];
					}else{
						System.err.println("Missing parameter for "+ARG_SAFS_DIR);
					}
				}else if(args[i].equalsIgnoreCase(ARG_INSTALLDIR)){
					if(++i<args.length){
						home = args[i];
					}else{
						System.err.println("Missing parameter for "+ARG_INSTALLDIR);
					}
				}else if(args[i].equalsIgnoreCase(ARG_UNINSTALL)){
					uninstall = true;
				}else if(args[i].equalsIgnoreCase(ARG_DEBUG)){
					debug = true;
				}else if(args[i].equalsIgnoreCase(ARG_SILENT)){
					silent = true;
				}else if(args[i].equalsIgnoreCase(ARG_VERBOSE)){
					verbose = true;
				}
			}

			if(safsHome==null){
				System.err.println(usage());
				System.exit(-1);
				return;
			}

			ProgressIndicator progresser = new ProgressIndicator();
			progresser.setTitle(TITLE);
			progresser.createAndShowGUI();

			GhostScriptInstaller installer = new GhostScriptInstaller(progresser, safsHome, home, silent, verbose, debug);
			if(uninstall){
				if( installer.uninstall() ) System.exit(0);
			}else{
				if( installer.install() ) System.exit(0);
			}
			System.exit(-1);

		}catch(Throwable th){
			IndependantLog.error("Met "+th.getClass().getSimpleName()+":"+th.getMessage());
			System.exit(-2);
		}
	}
}
