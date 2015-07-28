/** Copyright (C) SAS Institute, Inc. All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.install;

import java.io.File;

import org.safs.tools.CaseInsensitiveFile;

public class RFTInstaller extends InstallerImpl {

	public static final String RATIONAL_TEST_8_KEY = "Rational Software\\Rational Test\\8";
	public static final String RFT_INSTALL_DIRECTORY = "Rational FT Install Directory";

	public static final String normalkey  = "HKLM\\Software\\";
	public static final String wow6432key = "HKLM\\Software\\Wow6432Node\\";
	
	public static final String IBM_RFT_INSTALL_BIN_ENV = "IBM_RATIONAL_RFT_INSTALL_DIR";

	public static String rational_ft_jar  = "\\rational_ft.jar";                            // Appends to rationalftdir FT
	public static String safsrational_ft_jar = "\\lib\\safsrational_ft.jar";                // Appends to SAFSDIR for FT
	public static String safsrational_ft_enabler_jar = "\\lib\\safsrational_ft_enabler.jar";// Appends to SAFSDIR for FT
	
	public RFTInstaller() { super(); }

	/**
	 * Set the System Environment Variables used by SAFS.
	 * <p>
	 * CLASSPATH<br>
	 * <p>
	 * 
	 * @param args
	 * @return
	 */
	public boolean install(String... args) {

		String rationalftdir;
		cleanSystemEnvironment();
		rationalftdir = getRegistryValue(wow6432key + RATIONAL_TEST_8_KEY, RFT_INSTALL_DIRECTORY);
		if(rationalftdir == null ||
		   rationalftdir.length()==0) 
		   rationalftdir = getRegistryValue(normalkey + RATIONAL_TEST_8_KEY, RFT_INSTALL_DIRECTORY);
		
		if(rationalftdir == null || 
		   rationalftdir.length()==0)
		   rationalftdir = getEnvValue(IBM_RFT_INSTALL_BIN_ENV);

		// cannot deduce that RFT is installed
		if(rationalftdir == null || rationalftdir.length()==0) return false;
		
		File dir = new CaseInsensitiveFile(rationalftdir).toFile();
		if(! dir.isDirectory()) {
			return false;
		}
		
		String rationalftjar = rationalftdir + rational_ft_jar;
		File jar = new CaseInsensitiveFile(rationalftjar).toFile();
		if(! jar.isFile()) return false;
		
		String safsdir = getEnvValue(SAFSInstaller.SAFSDIREnv);
		if(safsdir == null || safsdir.length()==0) return false;
		
		String safsrational_jar = safsdir + safsrational_ft_jar;
		String safsrational_enabler = safsdir + safsrational_ft_enabler_jar;
		
		jar = new CaseInsensitiveFile(safsrational_jar).toFile();
		if(!jar.isFile()) return false;
		jar = new CaseInsensitiveFile(safsrational_ft_jar).toFile();
		if(!jar.isFile()) return false;
		
		appendSystemEnvironment("CLASSPATH", rationalftjar, null);
		appendSystemEnvironment("CLASSPATH", safsrational_jar, null);
		appendSystemEnvironment("CLASSPATH", safsrational_enabler, null);
		
		return true;
	}

	private boolean cleanSystemEnvironment(){
		removeSystemEnvironmentSubstringContaining("CLASSPATH", File.separator +"safsrational.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", File.separator +"safsrational_ft.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", File.separator +"safsrational_ft_custom.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", File.separator +"safsrational_ft_enabler.jar", null);
		removeSystemEnvironmentSubstringContaining("CLASSPATH", File.separator +"safsrational_xde.jar", null);
		return true;
	}
	
	/**
	 * Unset the System Environment Variables used by SAFS.
	 * <p>
	 * CLASSPATH<br>
	 * <p>
	 * @param args
	 * @return
	 */
	public boolean uninstall(String... args) {
		return cleanSystemEnvironment();
	}

	/**
	 * Main Java executable.  Primarily to run standalone outside of a larger process.
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.RFTInstaller
	 * <p>
	 * %SAFSDIR%\jre\bin\java org.safs.install.RFTInstaller -u
	 * <p>
	 * System.exit(0) on perceived success.<br>
	 * System.exit(-1) on perceived failure.
	 * @param args --  -u to perform an uninstall instead of install. 
	 * @see org.safs.install.SilentInstaller
	 */
	public static void main(String[] args) {
		boolean uninstall = false;
		RFTInstaller installer = new RFTInstaller();
		for(String arg:args) if (arg.equals("-u")) uninstall = true;
		if(uninstall){
			if( installer.uninstall() ) System.exit(0);
		}else{
			if( installer.install() ) System.exit(0);
		}
		System.exit(-1);
	}
}
