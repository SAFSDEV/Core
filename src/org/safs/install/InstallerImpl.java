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
 * History:
 * OCT 26, 2016	(Lei Wang) Added some general code used to detect the installed versions of one product.
 * JUN 29, 2018	(Lei Wang) Added getSafsHome(): the SAFS/SeleniumPlus home directory
 *                             getHome(): the special tool's home directory
 *                             getDefaultHome(): the default home directory of special tool
 *                             findInstaller(): find the installer executable for special tool
 *                       Added BIT_OPTION: to distinguish 32 bits and 64 bits product.
 */
package org.safs.install;

import java.io.File;
import java.io.FileNotFoundException;

import org.safs.Constants.LogConstants;
import org.safs.IndependantLog;
import org.safs.natives.NativeWrapper;
import org.safs.tools.CaseInsensitiveFile;

import com.sun.jna.Platform;

/**
 * @author Carl Nagle
 */
public abstract class InstallerImpl implements InstallerInterface{

	static ProgressIndicator progresser = null;

	public static final String s = File.separator;

	public static final String SYSTEM32   = s+"System32";
	public static final String SYSWOW64   = s+"SysWOW64";

	public static final String REGSVR32   = s+"regsvr32.exe";

	protected static final String PARAM_USE_LATEST_VERSION 	= "-"+VERSION_OPTION.USE_LATEST.name;
	protected static final String PARAM_SWITCH 				= "-"+VERSION_OPTION.SWITCH.name;

	/**<b>install</b> the sub directory where to find the install stuffs, such as installer executable file */
	public static final String DIR_INSTALL					= "install";

	/**<b>bin</b> the sub directory holds executable */
	public static final String DIR_BIN 					= "bin";

	/**<b>c:\safs</b> the directory holds the SAFS product */
	public static final String DIR_SAFS_HOME_DEFAULT	= "c:\\safs";

	/** if installation is in verbose more, default is false. */
	protected boolean verbose = false;

	/** if installation is in silent more, default is true. */
	protected boolean silent = true;

	/** if installation is in debug more, default is false. */
	protected boolean debug = false;

	/**
	 * The full path to the SAFS or SeleniumPlus, which provides the installer for other tools.<br>
	 * We don't use this field as the installation directory to install SAFS or SeleniumPlus, we use {@link #home}.<br>
	 * It is only used to find install asserts (for other tools, products needed by SAFS/SeleniumPlus) provided by SAFS or SeleniumPlus.<br>
	 */
	protected String safsHome = DIR_SAFS_HOME_DEFAULT;

	/**
	 * The home directory where this product will be installed.<br>
	 * It may be the same as {@link #safsHome} if we are installing SAFS or SeleniumPlus.<br>
	 */
	protected String home = null;

	/**
	 * The ProductDetector. Sub-class may need to implement:
	 * <ul>
	 * <li> {@link #getDefaultProductDetector()}
	 * <li> {@link #getUnixProductDetector()}
	 * <li> {@link #getWindowsProductDetector()}
	 * </ul>
	 * @see #initilizeProductDetector()
	 */
	protected IProductDetector productDetector = null;

	protected static enum VERSION_OPTION{
		CHECK_ENVIRONMENT("default", "Use the current version."),
		USE_LATEST("latest", "Check the latest version"),
		SWITCH("switch", "Switch between multiple versions.");

		public final String name;
		public final String message;
		VERSION_OPTION(String name, String message){
			this.name = name;
			this.message = message;
		}

		@Override
		public String toString(){
			return name;
		}
	}

	protected static enum BIT_OPTION{
		BIT_32("32 bits", "Install 32 bits version."),
		BIT_64("64 bits", "Install 64 bits version");

		public final String name;
		public final String message;
		BIT_OPTION(String name, String message){
			this.name = name;
			this.message = message;
		}

		@Override
		public String toString(){
			return name;
		}
	}

	protected String getProductName(){
		return "";
	}

	/**
	 * @return String, The full path to the SAFS or SeleniumPlus, which provides the installer for other tools
	 */
	public String getSafsHome() {
		return safsHome;
	}
	/**
	 * @param safsHome String, The full path to the SAFS or SeleniumPlus, which provides the installer for other tools
	 */
	public void setSafsHome(String safsHome) {
		this.safsHome = safsHome;
	}

	/** The home directory where this product will be installed. */
	public String getHome() {
		return home;
	}

	/**
	 * @param home String, The home directory where this product will be installed.
	 */
	public void setHome(String home) {
		this.home = home;
	}

	/**
	 * @return String, the default home directory to install the product
	 */
	public String getDefaultHome(){
		try{
			return productDetector.getDefaultHome();
		}catch(Exception e){
			return null;
		}
	}

	/**
	 * @return String, the full path to get the installer executable file.<br>
	 *                 such as C:/safs/install or C:/SeleniumPlus/install<br>
	 */
	protected String getInstallerDir(){
		return getSafsHome()+File.separator+DIR_INSTALL;
	}

	/**
	 * @param executable String, the installer executable file name
	 * @return String, the full path of the installer executable file
	 * @throws FileNotFoundException
	 */
	protected String findInstaller(String executable)throws FileNotFoundException{
		setProgressMessage("Searching installer ...");
		if(executable==null){
			throw new FileNotFoundException("The installer executable file is not provided!");
		}
		CaseInsensitiveFile installer = new CaseInsensitiveFile(executable);
		if(! installer.exists()) installer = new CaseInsensitiveFile(getInstallerDir(), executable);
		if(! installer.exists()) throw new FileNotFoundException(executable);
		return installer.getPath();
	}

	/**
	 * -installdir <br>
	 * optional command-line argument to explicitly set the installation directory for an installer.<br>
	 * Ex: -installdir "C:\SAFS"  (for SAFS proper)<br>
	 * Ex: -installdir "C:\SeleniumPlus"  (for SeleniumPlus proper)<br>
	 * Ex: -installdir "C:\SeleniumPlus\extra\automation"  (for SeleniumPlus OCR, etc.)<br>
	 */
	public static final String ARG_INSTALLDIR = "-installdir";

	/**
	 * -u <br>
	 * command-line argument to signal an uninstall invocation.
	 */
	public static final String ARG_UNINSTALL = "-u";

	/** <b>-safs</b>
	 * Ex: -safs "C:\SAFS"  (for SAFS product)<br>
	 * Ex: -safs "C:\SeleniumPlus"  (for SeleniumPlus product)<br>
	 */
	public static final String ARG_SAFS_DIR = "-safs";

	/** <b>-debug</b> if in debug mode */
	public static final String ARG_DEBUG 	= "-debug";

	/** <b>-silent</b> if in silent mode */
	public static final String ARG_SILENT 	= "-silent";

	/** <b>-v</b> if in verbose mode, more details will be output  */
	public static final String ARG_VERBOSE 	= "-v";

	protected static String rootdir = null;

	/** Default no-arg constructor. */
	public InstallerImpl()  {
		super();
		try{
			initilizeProductDetector();
		}catch(UnsupportedOperationException e){
			setProgressMessage("Ignore "+e.getMessage());
		}
	}

	/** Preset the installation directory so it does not need to be deduced. */
	public InstallerImpl(String _installDir) {
		this();
		rootdir = _installDir;
	}

	/**
	 * Subclasses use this to determine what is the root directory of the current
	 * install/uninstall that is in-progress.
	 * <p>
	 * Needs to be enhanced to temporarily flag what is in-progress, versus
	 * what might already exist as deduced from the normal SAFSDIREnv or
	 * SELENIUMDIREnv.
	 * <p>
	 * @return
	 */
	protected String getInstallationRoot(){
		if(rootdir == null || rootdir.length()==0) rootdir = getEnvValue(SAFSInstaller.SAFSDIREnv);
		if(rootdir == null || rootdir.length() == 0){
			rootdir = getEnvValue(SeleniumPlusInstaller.SELENIUMDIREnv);
			if(rootdir != null && rootdir.length() >0)
				rootdir += s+"extra"+s+"automation";
		}
		return rootdir;
	}

	/**
	 * Initialize the IProdcutDetector object according to the Operation System.<br>
	 * A ProductDetectorDefault will be returned if the OS is not Windows or UNIX/LINUX.<br>
	 * @throws UnsupportedOperationException
	 */
	protected void initilizeProductDetector() throws UnsupportedOperationException{
		if(Platform.isWindows()){
			if(productDetector==null)
				productDetector = getWindowsProductDetector();
		}else if(Platform.isOpenBSD() ||
				 Platform.isFreeBSD() ||
				 Platform.isLinux() ||
				 Platform.isSolaris()){
			if(productDetector==null)
				productDetector = getUnixProductDetector();
		}else{
			IndependantLog.debug("Use default ProductDetector for OS:  com.sun.jna.Platform.getOSType()="+Platform.getOSType());
			if(productDetector==null)
				productDetector = getDefaultProductDetector();
		}
	}

	protected IProductDetector getWindowsProductDetector() throws UnsupportedOperationException{
		throw new UnsupportedOperationException("The method getWindowsProductDetector() is not supported yet.");
	}
	protected IProductDetector getUnixProductDetector() throws UnsupportedOperationException{
		throw new UnsupportedOperationException("The method getUnixProductDetector() is not supported yet.");
	}
	/**
	 * @return ProductDetectorDefault
	 * @see org.safs.install.ProductDetectorDefault
	 */
	protected IProductDetector getDefaultProductDetector(){
		return new ProductDetectorDefault();
	}

	public static void setProgressIndicator(ProgressIndicator _progresser){
	   progresser = _progresser;
	}

	protected static void setProgress(int percent){
		try{ progresser.setProgress(percent);}catch(Throwable ignore){}
	}
	protected static void increaseProgress(int increase){
		try{ progresser.increaseProgress(increase);}catch(Throwable ignore){}
	}

	protected static void setProgressMessage(String message){
		try{ progresser.setProgressMessage(message);}catch(Throwable ignore){System.out.println(message);}
	}

	protected static void setProgressMessage(String message, int logLevel){
		try{ progresser.setProgressMessage(message, logLevel);}catch(Throwable ignore){System.out.println(message);}
	}

	/**
	 * Return a System Environment String value.
	 * <p>
	 * Note: Environment variables set or changed after JVM start are NOT available to the currently
	 * running JVM through System.getEnv().  This is because the JVM does not refresh its
	 * Environment variable space after launch.  Use this routine to get
	 * the latest "refreshed" value of any System Environment Variable.
	 * <p>
	 * @param key Environment variable value to retrieve.
	 * @return String value, or null if not found.
	 */
	public static String getEnvValue(String key){
		return NativeWrapper.GetSystemEnvironmentVariable(key);
	}

	/**
	 * Set a System Environment value.
	 * <p>
	 * Note: Environment variables set or changed after JVM start are NOT available to the currently
	 * running JVM through System.getEnv().  This is because the JVM does not refresh its
	 * Environment variable space after launch.  Use getEnvValue to get
	 * the latest "refreshed" value of any System Environment Variable.
	 * <p>
	 * @param key name of variable to set
	 * @param value to be set.  Null will remove the variable.
	 * @return true on succes, false otherwise.
	 * @see NativeWrapper#SetSystemEnvironmentVariable(Object, Object)
	 * @see #getEnvValue(String)
	 */
	public static boolean setEnvValue(String key, String value){
		return value == null ?
				NativeWrapper.RemoveSystemEnvironmentVariable(key):
				NativeWrapper.SetSystemEnvironmentVariable(key, value);
	}

	/**
	 * A convenient method to set/clear an environment.<br>
	 * It will also write success/failure message to {@link #progresser}.<br>
	 *
	 * @param key name of variable to set
	 * @param value to be set. Null will remove the variable.
	 *
	 * @see #setEnvValue(String, String)
	 */
	public void setEnvironment(String key, String value){
		if(setEnvValue(key, value)){
			if(value==null){
				setProgressMessage("Cleared environment '"+key+"'");
			}else{
				setProgressMessage("Set environment '"+key+"' with value '"+value+"'.");
			}
		}else{
			if(value==null){
				setProgressMessage("Failed to clear '"+key+"'", LogConstants.WARN);
			}else{
				setProgressMessage("Failed to set environment '"+key+"' with value '"+value+"'.", LogConstants.WARN);
			}
		}
	}

	/**
	 * Append a value to an existing Environment variable (CLASSPATH, PATH, etc..)<br>
	 * This adds the value at the end of any existing value.
	 * @param varname -- name of Environment variable to modify. cannot be null.
	 * @param append -- the value to add if it is not already present. cannot be null.
	 * @param sep - optional separator to use between appends. if null, File.pathSeparator is used.
	 * Can also be an empty string to indicate no separator should be used.
	 * @return the new value of the variable, or null if a problem occurred.
	 */
	public static String appendSystemEnvironment(String varname, String append, String sep){
		String s = sep == null ? File.pathSeparator : sep;
		if(varname == null || varname.length()==0 || append == null || append.length()==0)
			return null;
		String varvalue = getEnvValue(varname);
		if(varvalue == null) {
			setEnvValue(varname, append);
		}else
		if(! varvalue.toLowerCase().contains(append.toLowerCase())){
			varvalue += s + append;
			setEnvValue(varname, varvalue);
		}
		return getEnvValue(varname);
	}

	/**
	 * Prepend a value to an existing Environment variable (CLASSPATH, PATH, etc..)<br>
	 * This puts the value at the beginning of any existing value.
	 * @param varname -- name of Environment variable to modify. cannot be null.
	 * @param prepend -- the value to prepend if it is not already present. cannot be null.
	 * @param sep - optional separator to use between values. if null, File.pathSeparator is used.
	 * Can also be an empty string to indicate no separator should be used.
	 * @return the new value of the variable, or null if a problem occurred.
	 */
	public static String prependSystemEnvironment(String varname, String prepend, String sep){
		String s = sep == null ? File.pathSeparator : sep;
		if(varname == null || varname.length()==0 || prepend == null || prepend.length()==0)
			return null;
		String varvalue = getEnvValue(varname);
		if(varvalue == null) {
			setEnvValue(varname, prepend);
		}else
		if(! varvalue.toLowerCase().contains(prepend.toLowerCase())){
			varvalue = prepend + s + varvalue;
			setEnvValue(varname, varvalue);
		}
		return getEnvValue(varname);
	}

	/**
	 * Remove a substring from an existing Environment variable (CLASSPATH, PATH, etc..)<br>
	 * @param varname -- name of Environment variable to modify. cannot be null.
	 * @param substring -- the value to remove if it is present. cannot be null.
	 * @param sep - optional separator found between values. if null, File.pathSeparator is assumed.
	 * Can also be an empty string to indicate no separator should be considered.
	 * @return the new value of the variable, or null if a problem occurred or the variable
	 * no longer exists.
	 */
	public static String removeSystemEnvironmentSubstring(String varname, String substring, String sep){
		String s = sep == null ? File.pathSeparator : sep;
		if(varname == null || varname.length()==0 || substring == null || substring.length()==0)
			return null;
		String varvalue = getEnvValue(varname);
		if(varvalue == null) return null;
		String begin = "";
		String end = "";
		if(varvalue.toLowerCase().contains(substring.toLowerCase())){
			int index = varvalue.toLowerCase().indexOf(substring.toLowerCase());
			boolean sepstripped = false;
			if(index > 0){
				begin = varvalue.substring(0, index);
				if (begin.endsWith(s)){
					begin = begin.substring(0, begin.length() - s.length());
					sepstripped = true;
				}
			}
			// done if substring was at the end of varvalue
			index += substring.length();
			if(index < varvalue.length()){
				end = varvalue.substring(index);
				if(end.startsWith(s) && !sepstripped){
					end = end.substring(s.length());
				}
			}
			//clear the variable if there is nothing left
			s = begin + end;
			if (s.length()==0) s = null;
			setEnvValue(varname, s);
		}
		return getEnvValue(varname);
	}

	/**
	 * Remove a string entry from an existing Environment variable (CLASSPATH, PATH, etc..)<br>
	 * In this method, we attempt to do a partial match on an entry between separators to locate
	 * the item to remove from the variable.  After making a partial match, we remove the full
	 * substring between the separators.
	 * <p>
	 * Ex: removeSystemEnvironmentSubstringContaining("CLASSPATH", "\\safs.jar", null);<br>
	 * This will remove the safs.jar reference in CLASSPATH no matter what the path to it might be.
	 * <p>
	 * @param varname -- name of Environment variable to modify. cannot be null.
	 * @param substring -- the partial match value to remove, if it is present. cannot be null.
	 * @param sep - optional separator found between values. if null or zero-length then File.pathSeparator is used.
	 * @return the new value of the variable, or null if a problem occurred.
	 */
	public static String removeSystemEnvironmentSubstringContaining(String varname, String substring, String sep){
		String s = (sep == null ) ? File.pathSeparator : sep;

		//MUST have a valid separator
		if(s.length()==0) return null;

		if(varname == null   || varname.length()==0   ||
		   substring == null || substring.length()==0 )
		   return null;

		String varvalue = getEnvValue(varname);
		if(varvalue == null || varvalue.length() < substring.length()) return null;

		String begin = "";
		String end = "";
		int startsep = -1;
		int endsep = -1;
		int startsubstring = varvalue.toLowerCase().indexOf(substring.toLowerCase());
		if(startsubstring >= 0){
			boolean sepstripped = false;
			if(startsubstring > 0){
				begin = varvalue.substring(0, startsubstring);
				startsep = begin.lastIndexOf(s);
				if(startsep > 0) {
					begin = begin.substring(0, startsep);
					sepstripped = true;
				}else{
					begin = "";
				}
			}
			// done if substring was at the end of varvalue
			endsep = startsubstring + substring.length();
			if(endsep < varvalue.length()){
				end = varvalue.substring(endsep);
				endsep = end.indexOf(s);
				if(endsep >= 0) {
					end = end.substring(endsep);
				}else{ //there was no other item after ours
					end = "";
				}
				if(end.startsWith(s) && !sepstripped){
					end = end.substring(s.length());
				}
			}
			//clear the variable if there is nothing left
			s = begin + end;
			if (s.length()==0) s = null;
			setEnvValue(varname, s);
		}
		return getEnvValue(varname);
	}

	/**
	 * True if the requested "registry key" already exists, false otherwise.<br>
	 * This is currently only supported on Windows.
	 * <p>
	 * The Windows version uses Reg.EXE which is supplied with WindowsXP.  If this EXE is
	 * not present on the Windows system then this function will return false.
	 * <p>
	 * @param key For Windows this is a String. Ex:"HKCU\\Environment"
	 * @param valuename For Windows this is a String. Ex:"Path".  Can be null.
	 * @return true if the key and value (if provided) exists, otherwise false.
	 */
	public static boolean hasRegistryEntry(String key, String valuename){
		return NativeWrapper.DoesRegistryKeyExist(key, valuename);
	}

	/**
	 * Gets the (Windows) registry value, or null if it doesn't exist.
	 * @param key -- Ex: "HKCU\\Environment"
	 * @param valuename -- Ex: "Path". can be null
	 * @return the value found in the registry, or null if not found.
	 * @see NativeWrapper#GetRegistryKeyValue(Object, Object)
	 */
	public static String getRegistryValue(String key, String valuename){
		return (String)NativeWrapper.GetRegistryKeyValue(key, valuename);	}

	/**
	 * @param key
	 * @param valuename
	 * @param value
	 * @return
	 * @see NativeWrapper#GetRegistryKeyValue(Object, Object)
	 */
	public static boolean setRegistryValue(String key, String valuename, String value){
		return NativeWrapper.SetRegistryKeyValue(key, valuename, null, value);	}
}
