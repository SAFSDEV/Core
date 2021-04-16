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
 * APR 01, 2019 (Lei Wang) Add code to detect the 'architecture' (32 or 64 bits) of the installed STAF.
 * MAY 09, 2019 (Lei Wang) Modified install(): Modify the STAFEnv script to set the environment "STAFDIR".
 *                        Modified getInstallProperties(): use System.getenv() to get the 'STAFDIR' firstly, then by NativeWrapper.
 *                        Modified install(): combine codes for Windows/Unix OS.
 */
package org.safs.install;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.safs.IndependantLog;
import org.safs.android.auto.lib.Console;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;

public class STAFInstaller extends InstallerImpl{

	public static final String STAFDIREnv = "STAFDIR";
	public static final String STAFBINPath = File.separator +"bin";
	public static final String JSTAFPath = STAFBINPath + File.separator +"JSTAF.jar";
	public static final String JSTAFZIPPath = STAFBINPath + File.separator +"JSTAF.zip";

	/** %STAFDIR%\install.properties holding the installation information */
	public static final String InstallPropertiesPath = File.separator +"install.properties";

	/** %STAFDIR%\STAFEnv.bat */
	public static final String STAFEnvWinPath = File.separator +"STAFEnv.bat";
	/** %STAFDIR%\STAFEnv.sh */
	public static final String STAFEnvUnixPath = File.separator +"STAFEnv.sh";

	public STAFInstaller(){super();}

	/**
	 * Currently assumes STAF has been installed and STAFDIR is already set.
	 * Can set STAFDIR if args[0] is given a valid value to use.
	 * If args[0] is provided, the path will be validated before it is used.
	 * Adds STAF paths to CLASSPATH and PATH Environment Variables as long as JSTAF.JAR
	 * is found to exist.
	 * Modify the STAFEnv script to set the environment "STAFDIR".
	 */
	@Override
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

		//Modify the STAFEnv script to set the environment "STAFDIR"
		String stafenvScript = stafdir + STAFEnvWinPath;
		String setEnvironmentSTAFDIR = "set "+STAFDIREnv+"="+stafdir;

		if(Console.isWindowsOS() || Console.isUnixOS() || Console.isMacOS()){

			if(Console.isUnixOS() || Console.isMacOS()){
				stafenvScript = stafdir + STAFEnvWinPath;
				setEnvironmentSTAFDIR = STAFDIREnv+"="+stafdir;
			}

			file = new CaseInsensitiveFile(stafenvScript).toFile();
			if(file.isFile()){
				IndependantLog.debug("Modify the STAFEnv script '"+stafenvScript+"' to set the environment 'STAFDIR'.");
				try {
					String[] lines = FileUtilities.readLinesFromFile(file.getAbsolutePath());
					List<String> updateContents = new ArrayList<String>();
					for(String line:lines){
						if(line.startsWith("export")){
							//This is for Unix/Linux/Mac OS
							//export PATH LD_LIBRARY_PATH CLASSPATH STAFCONVDIR STAF_INSTANCE_NAME
							//export also "STAFDIR"
							updateContents.add(line+" "+STAFDIREnv);
						}else{
							updateContents.add(line);
						}
						//REM STAF environment variables
						//# STAF environment variables
						if(line.contains("STAF environment variables")){
							updateContents.add(setEnvironmentSTAFDIR);
						}
					}
					FileUtilities.writeCollectionToUTF8File(file.getAbsolutePath(), updateContents);
				} catch (IOException e) {
					IndependantLog.error("Met "+e.toString());
				}
			}else{
				IndependantLog.warn("skipped updating the STAFEnv script: the file '"+file.getAbsolutePath()+"' is not found.");
			}
		}else{
			IndependantLog.warn(Console.getOsFamilyName()+" has not been supported, skipped updating the STAFEnv script. ");
		}

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
	@Override
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

	private static Properties insallProperties = null;
	/**
	 * Read the file c:\staf\install.properties and load it into the Properties.
	 *
	 * @return Properties of c:\staf\install.properties
	 */
	public static Properties getInstallProperties(){
		//http://staf.sourceforge.net/current/STAFFAQ.htm#d0e361, read the file c:\staf\install.properties

		if(insallProperties!=null) return insallProperties;

		insallProperties = new Properties();
		InputStream input = null;
		String installPropertiesFile = null;

		try{
			//First, we try to get by Java System.getenv, see defect S1504632
			String stafDir = System.getenv(STAFDIREnv);
			if(stafDir==null || stafDir.isEmpty()){
				//If not found, then we try to get by NativeWrapper to look from the registry.
				getEnvValue(STAFDIREnv);
			}
			installPropertiesFile = stafDir + InstallPropertiesPath;
			input = new FileInputStream(new CaseInsensitiveFile(installPropertiesFile).toFile());
			insallProperties.load(input);

		}catch(Exception e){
			IndependantLog.error("getInstallProperties(): failed to load "+installPropertiesFile+", due to "+e.toString());
		}finally{
			if(input!=null){
				try{ input.close(); }catch(Exception e){}
			}
		}

		return insallProperties;
	}

	/**
	 * @return boolean true if the installed STAF is 32-bits.
	 */
	public static boolean is32BitsSTAF(){
		Properties insallProperties = getInstallProperties();
		//http://staf.sourceforge.net/current/STAFFAQ.htm#d0e361
		String architecture = insallProperties.getProperty("architecture");

		IndependantLog.debug("The installed STAF's architecture is "+architecture);//32-bit or 64-bit

		return architecture.contains("32");
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
