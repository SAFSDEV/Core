/** Copyright (C) SAS Institute, Inc. All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * History:
 * OCT 25, 2016	(SBJLWA) Modified to make it easier to support higher version.
 *                       Added parameter "-latest", "-switch".
 */
package org.safs.install;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.safs.IndependantLog;
import org.safs.tools.CaseInsensitiveFile;

/**
 * Handle Windows registration and Environment settings for SmartBear TestComplete and 
 * TestExecute.  Intended to replace WSH install scripts.
 * @author canagl
 */
public class TCAFSInstaller extends InstallerImpl{
	/** 'TESTCOMPLETE_HOME' environment variable to save the product's home */
	private static final String TCHomeEnv = "TESTCOMPLETE_HOME";
	/** 'TESTCOMPLETE_EXE' environment variable to save the "name" of product's executable. */
	private static final String TCExeEnv  = "TESTCOMPLETE_EXE";
	
	/**The supported version should be given from latest to oldest, we prefer to use the latest version. 
	 * This array contains version number <= 8.0, "Automated QA" */
	private static final float[] SUPPORTED_VERSION_AUTOMATED_QA = {8.0F};
	/**The supported version should be given from latest to oldest, we prefer to use the latest version.
	 * This array contains version number >= 9.0, "Smart Bear" */
	private static final float[] SUPPORTED_VERSION_SMART_BEAR = {12.0F, /*11.0F, not supported*/ 10.0F, 9.0F};

	/** '-u' parameter indicator for un-installation. */
	private static final String PARAM_UNINSTALL 			= "-u";
	
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
	 * @param args String[], optional<br> 
	 * <b>-latest</b>, check the latest installed TestExecute/TestComplete and reset the environment variable {@link #TCHomeEnv} and {@value #TCExeEnv}<br>
	 * <b>-switch</b>, check all the installed TestExecute/TestComplete for switching and reset the environment variable {@link #TCHomeEnv} and {@value #TCExeEnv}<br>
	 * @return true if successful, false otherwise.
	 */
	public boolean install(String... args){
		VERSION_OPTION versionOption = VERSION_OPTION.CHECK_ENVIRONMENT;
		File tchome = null;
		String productHome = null;
		
		for(String arg:args){
			if (PARAM_SWITCH.equals(arg)) versionOption = VERSION_OPTION.SWITCH;
			else if (PARAM_USE_LATEST_VERSION.equals(arg)) versionOption = VERSION_OPTION.USE_LATEST;
		}
		
		if(VERSION_OPTION.CHECK_ENVIRONMENT.equals(versionOption)){
			String temp_tchome = getEnvValue(TCHomeEnv);
			setProgressMessage("Evaluating Environment "+ TCHomeEnv+": "+temp_tchome);
			if(temp_tchome!= null){
				tchome = new CaseInsensitiveFile(temp_tchome).toFile();
				if(tchome.isDirectory()){
					String temp_tcexe = getEnvValue(TCExeEnv);
			        try{
			        	setProgressMessage("Evaluating Environment "+ TCExeEnv+": "+temp_tcexe);
			        	if(!checkTCExecutableAndSetEnv(tchome, temp_tcexe)){
			        		return complete("Unable to set required Path for Windows SmartBear TestComplete support.", false);
			        	}else{
			        		String successInfo = getSuccessInfo();
			        		VERSION_OPTION[] options = {VERSION_OPTION.CHECK_ENVIRONMENT, VERSION_OPTION.USE_LATEST, VERSION_OPTION.SWITCH, };
							String message = successInfo + "\n"+
									"Please choose your option\n"+
									options[0].name+": "+options[0].message+"\n"+
									options[1].name+": "+options[2].message+"\n"+
									options[2].name+": "+options[2].message;
							int response = TopMostOptionPane.showOptionDialog(null, message, "Confirm", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options , options[0]);
							if (response == JOptionPane.YES_OPTION) {
								return complete(successInfo, true);
							}else if (response == JOptionPane.NO_OPTION) {
								setProgressMessage("You choose to check the latest version of Test Complete.");
								versionOption = VERSION_OPTION.USE_LATEST;
							}else if(response==JOptionPane.CANCEL_OPTION){
								setProgressMessage("You are going to switch TestComplete version.");
								versionOption = VERSION_OPTION.SWITCH;
							}
			        	}
			        }catch(FileNotFoundException e){
						setProgressMessage(e.getMessage());
						setProgressMessage("Cannot find any executable '"+temp_tcexe+"' under TC HOME "+temp_tchome+"!");
					}
				}else{
					setProgressMessage("The producton home '"+tchome.getAbsolutePath()+"' is NOT a directory!");
					versionOption = VERSION_OPTION.SWITCH;
				}
			}else{
				setProgressMessage("The value of Environment Varialbe '"+TCHomeEnv+"' is null!");
				versionOption = VERSION_OPTION.SWITCH;
			}
		}
		
		if(VERSION_OPTION.USE_LATEST.equals(versionOption)){
			productHome = productDetector.gerPreferredHome();
		}else if(VERSION_OPTION.SWITCH.equals(versionOption)){
			Map<String, String> homes = null;
			homes = productDetector.getHomes();
			try{
				String[] options = homes.keySet().toArray(new String[0]);
				if(options.length>0){
					int response = TopMostOptionPane.showOptionDialog(null, "Please choose your preferred version.", "Switch Version.", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options , null);
					productHome = homes.get(options[response]);
				}				
			}catch(NullPointerException e){
				IndependantLog.warn("TCAFSInstaller.instll(): cannot detect product homes to switch, due to "+e.getMessage()); 
			}
		}

		if(productHome != null){
			if(productHome.endsWith(File.separator)) productHome = productHome.substring(0, productHome.length() - File.separator.length());
			setProgressMessage("Verifying Windows SmartBear support at "+ productHome);
			tchome = new CaseInsensitiveFile(productHome).toFile();
			if(tchome.isDirectory()){
				if(!setEnvValue(TCHomeEnv, productHome)) {
					return complete("Unable to set required Directory for Windows SmartBear TestComplete support." ,false);
				}
			}else {
				return complete("Directory for Windows SmartBear TestComplete support seems to be invalid." ,false);
			}
		}
		
		// home is set or null
		if(tchome == null){
			return complete("Did not detect Windows SmartBear TestComplete installation." ,false);
		}
        try{
        	if(!checkTCExecutableAndSetEnv(tchome, null)){
        		return complete("Unable to set required Path for Windows SmartBear TestComplete support." ,false);
        	}
        }catch(FileNotFoundException e){
			return complete(e.getMessage() ,false);
		}
        
		return complete(getSuccessInfo(), true);
	}
	
	/**
	 * Check if executable "TestExecute" or "TestComplete" exists, and set it<br>
	 * to environment {@link #TCExeEnv} if it exists.<br>
	 * @param tchome	File, the file represents "TestComplete" installation home.
	 * @param defaultExecutable String, the default executable. It could be "name" or "relative path"(to product home). Ex. "TestComplete.exe", "bin\TestComplete.exe"
	 * @return boolean, true if "TC executable" exists and it has been correctly set to environment {@link #TCExeEnv}.
	 *                  false if "TC executable" exists BUT it failed to be set to environment {@link #TCExeEnv}.
	 * @throws FileNotFoundException, if "TC executable" could not be detected.
	 */
	private boolean checkTCExecutableAndSetEnv(File tchome, String defaultExecutable) throws FileNotFoundException{
		File tcexe = productDetector.getValidExecutable(tchome, defaultExecutable);
		
		if(!tcexe.isFile()){
			throw new FileNotFoundException("Executable Path '"+tcexe.getAbsolutePath()+"' seems to be invalid.");
		}
		
		return setEnvValue(TCExeEnv, tcexe.getName());
	}
	
	private String getSuccessInfo(){
		String message = "SmartBear TestComplete has been successfully set.\n"+
						TCHomeEnv+"="+getEnvValue(TCHomeEnv)+"\n"+
						TCExeEnv+"="+getEnvValue(TCExeEnv)+"\n";
		
		return message;
	}
	
	private boolean complete(String message, boolean success){
		setProgressMessage(message);
		setProgressMessage("SmartBear TestComplete Installlation Complete with "+(success?"Success.":"Failure."));
		return success;
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
	
	protected IProductDetector getWindowsProductDetector() throws UnsupportedOperationException{
		return new ProductDetectorTCAFSWindows();
	}
	
	protected IProductDetector getUnixProductDetector() throws UnsupportedOperationException{
		//Just return a default detector, 
		//TODO For the real UNIX Product Detector, to be implemented later.
		return new ProductDetectorDefault();
	}
	
	private static class ProductDetectorTCAFSWindows extends ProductDetectorDefault{
		/** executable TestComplete.exe */
		private static final String TCExePath = "TestComplete.exe";
		/** executable TestExecute.exe */
		private static final String TEExePath = "TestExecute.exe";

		//for version 8.0 and earlier
		private static final String REG_PREFIX_TC_AQA = "Automated QA\\Test Complete\\";
		private static final String REG_PREFIX_TE_AQA = "Automated QA\\Test Execute\\";
		
		//for version 9.0 and higher
		private static final String REG_PREFIX_TC_SB = "SmartBear\\TestComplete\\";
		private static final String REG_PREFIX_TE_SB = "SmartBear\\TestExecute\\";
		
		private static final String REG_SUFFIX_SETUP = "Setup";
		private static final String TCProductPath = "Product Path";
		
		public Map<String, String> getHomes(){
			Map<String, String> homes = new LinkedHashMap<String, String>();
			String productPath = null;
			String key = null;
			
			setProgressMessage("Checking Windows Registry ...");
			//1. Favor "post win7" over "pre win7"
			//2. Favor "TestExecute" over "TestComplete"			
			setProgressMessage("Evaluating for Windows 7 or later.");
			setProgressMessage("Evaluating Windows SmartBear TestExecute support.");
			for(float version: SUPPORTED_VERSION_SMART_BEAR){
				productPath = getRegistryValue(REG_HKLM_ST_WOW6432+REG_PREFIX_TE_SB+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
				key = REG_PREFIX_TE_SB+version;
				if(productPath!=null) store(homes, key, productPath);
			}
			for(float version: SUPPORTED_VERSION_AUTOMATED_QA){
				productPath = getRegistryValue(REG_HKLM_ST_WOW6432+REG_PREFIX_TE_AQA+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
				key = REG_PREFIX_TE_AQA+version;
				if(productPath!=null) store(homes, key, productPath);
			}
			setProgressMessage("Evaluating Windows SmartBear TestComplete support.");
			for(float version: SUPPORTED_VERSION_SMART_BEAR){
				productPath = getRegistryValue(REG_HKLM_ST_WOW6432+REG_PREFIX_TC_SB+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
				key = REG_PREFIX_TC_SB+version;
				if(productPath!=null) store(homes, key, productPath);
			}
			for(float version: SUPPORTED_VERSION_AUTOMATED_QA){
				productPath = getRegistryValue(REG_HKLM_ST_WOW6432+REG_PREFIX_TC_AQA+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
				key = REG_PREFIX_TC_AQA+version;
				if(productPath!=null) store(homes, key, productPath);
			}

			setProgressMessage("Evaluating for Windows earlier than Win7.");
			setProgressMessage("Evaluating Windows SmartBear TestExecute support.");
			for(float version: SUPPORTED_VERSION_SMART_BEAR){
				productPath = getRegistryValue(REG_HKLM_ST+REG_PREFIX_TE_SB+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
				key = REG_PREFIX_TE_SB+version;
				if(productPath!=null) store(homes, key, productPath);
			}
			for(float version: SUPPORTED_VERSION_AUTOMATED_QA){
				productPath = getRegistryValue(REG_HKLM_ST+REG_PREFIX_TE_AQA+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
				key = REG_PREFIX_TE_AQA+version;
				if(productPath!=null) store(homes, key, productPath);
			}
			setProgressMessage("Evaluating Windows SmartBear TestComplete support.");
			for(float version: SUPPORTED_VERSION_SMART_BEAR){
				productPath = getRegistryValue(REG_HKLM_ST+REG_PREFIX_TC_SB+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
				key = REG_PREFIX_TC_SB+version;
				if(productPath!=null) store(homes, key, productPath);
			}
			for(float version: SUPPORTED_VERSION_AUTOMATED_QA){
				productPath = getRegistryValue(REG_HKLM_ST+REG_PREFIX_TC_AQA+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
				key = REG_PREFIX_TC_AQA+version;
				if(productPath!=null) store(homes, key, productPath);
			}

			return homes;
		}
		
		private void store(Map<String, String> homes, String key, String productPath){
			setProgressMessage("... FOUND Product '"+key+"', installed at '"+productPath+"'.");
			if(!homes.containsKey(key)){
				homes.put(key, productPath);
			}else{
				setProgressMessage("... Ignore Product '"+key+"', it is already in the Map with value as '"+homes.get(key)+"'.");
			}
		}

		public String gerPreferredHome(){
			String productPath = null;

searchRegistry:
			{
				setProgressMessage("Checking Windows Registry ...");
				//1. Favor "post win7" over "pre win7"
				//2. Favor "TestExecute" over "TestComplete"			
				setProgressMessage("Evaluating for Windows 7 or later.");
				setProgressMessage("Evaluating Windows SmartBear TestExecute support.");
				for(float version: SUPPORTED_VERSION_SMART_BEAR){
					productPath = getRegistryValue(REG_HKLM_ST_WOW6432+REG_PREFIX_TE_SB+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
					if(productPath!=null) break searchRegistry;
				}
				for(float version: SUPPORTED_VERSION_AUTOMATED_QA){
					productPath = getRegistryValue(REG_HKLM_ST_WOW6432+REG_PREFIX_TE_AQA+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
					if(productPath!=null) break searchRegistry;
				}
				
				setProgressMessage("Evaluating Windows SmartBear TestComplete support.");
				for(float version: SUPPORTED_VERSION_SMART_BEAR){
					productPath = getRegistryValue(REG_HKLM_ST_WOW6432+REG_PREFIX_TC_SB+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
					if(productPath!=null) break searchRegistry;
				}
				for(float version: SUPPORTED_VERSION_AUTOMATED_QA){
					productPath = getRegistryValue(REG_HKLM_ST_WOW6432+REG_PREFIX_TC_AQA+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
					if(productPath!=null) break searchRegistry;
				}
				
				setProgressMessage("Evaluating for Windows earlier than Win7.");
				setProgressMessage("Evaluating Windows SmartBear TestExecute support.");
				for(float version: SUPPORTED_VERSION_SMART_BEAR){
					productPath = getRegistryValue(REG_HKLM_ST+REG_PREFIX_TE_SB+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
					if(productPath!=null) break searchRegistry;
				}
				for(float version: SUPPORTED_VERSION_AUTOMATED_QA){
					productPath = getRegistryValue(REG_HKLM_ST+REG_PREFIX_TE_AQA+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
					if(productPath!=null) break searchRegistry;
				}
				setProgressMessage("Evaluating Windows SmartBear TestComplete support.");
				for(float version: SUPPORTED_VERSION_SMART_BEAR){
					productPath = getRegistryValue(REG_HKLM_ST+REG_PREFIX_TC_SB+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
					if(productPath!=null) break searchRegistry;
				}
				for(float version: SUPPORTED_VERSION_AUTOMATED_QA){
					productPath = getRegistryValue(REG_HKLM_ST+REG_PREFIX_TC_AQA+version+"\\"+REG_SUFFIX_SETUP, TCProductPath);
					if(productPath!=null) break searchRegistry;
				}
			}
			
			if(productPath==null){
				setProgressMessage("... NO product was detected!");
			}else{
				setProgressMessage("... FOUND Product, installed at '"+productPath+"'.");
			}
			
			return productPath;
		}

		public String[] getPossibleExecutables() {
			return new String[]{
					BIN+File.separator+TEExePath,
					BIN+File.separator+TCExePath
					};
		}
		
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
	 * @param args String[]<br>
	 * <b>-u</b> to perform an uninstall instead of install.<br>
	 * <b>-latest</b>, check the latest installed TestExecute/TestComplete and reset the environment variable {@link #TCHomeEnv} and {@value #TCExeEnv}<br>
	 * <b>-switch</b>, check all the installed TestExecute/TestComplete for switching and reset the environment variable {@link #TCHomeEnv} and {@value #TCExeEnv}<br>
	 * @see org.safs.install.SilentInstaller
	 */
	public static void main(String[] args) {
		boolean uninstall = false;
		TCAFSInstaller installer = new TCAFSInstaller();
		for(String arg:args){
			if (PARAM_UNINSTALL.equals(arg)){
				uninstall = true;
				break;
			}
		}
		if(uninstall){
			if( installer.uninstall(args) ) System.exit(0);
		}else{
			if( installer.install(args) ) System.exit(0);
		}
		System.exit(-1);
	}
}
