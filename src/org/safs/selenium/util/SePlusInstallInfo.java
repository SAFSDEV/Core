/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * SEP 20, 2016    (SBJLWA) Initial release: Moved existing code from WebDriverGUIUtilities to here.
 */
package org.safs.selenium.util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.natives.NativeWrapper;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverConstant.SeleniumConfigConstant;

/**
 * This class holds the information related to the Selenium, Embedded Java, Library etc.<br>
 * We provide support of Selenium Engine in both SAFS and SeleniumPlus.<br>
 * But the the file structure is different between SAFS and SeleniumPlus,<br> 
 * this class intends to detect automatically which one has been installed<br>
 * and deduce the related appropriate resources to use.<br>
 *  
 */
public class SePlusInstallInfo{
	private String extra = null;
	private String library = null;
	private String javabin = null;
	private String product = null;
	
	/** The installation directory of SAFS or SeleniumPlus */
	private File rootDir = null;
	/** The extra directory holding extra resources related to Selenium */
	private File extraDir = null;
	/** The library directory holding the library jar files etc. */
	private File libraryDir = null;
	/** The full path of java executable provided by SAFS or SeleniumPlus*/
	private String javaexe = null;
	/** The latest selenium-standalone jar file provided by SAFS or SeleniumPlus*/
	private File seleniumStandaloneJar = null;
	/** The chrome-driver executable file */
	private File chromedriver = null;
	/** The ie-driver executable file */
	private File iedriver = null;
	
	/** The instance of this class. */
	private static SePlusInstallInfo instance = null;
	
	/**
	 * @param extra String, 'extra' directory relative to root installation directory
	 * @param library String, 'library' directory relative to root installation directory
	 * @param javabin String, 'java bin' directory relative to root installation directory
	 * @param product String, product name, {@link #PRODUCT_SAFS} or {@link #PRODUCT_SELENIUM_PLUS}
	 */
	private SePlusInstallInfo(String extra, String library, String javabin, String product) {
		super();
		this.extra = extra;
		this.library = library;
		this.javabin = javabin;
		this.product = product;
	}
	
	public boolean isSeleniumPlus(){
		return PRODUCT_SELENIUM_PLUS.equals(product);
	}
	public boolean isSAFS(){
		return PRODUCT_SAFS.equals(product);
	}
	
	public static synchronized SePlusInstallInfo instance() throws SeleniumPlusException{
		if(instance==null){
			//TODO if it is not SELENIUM, assume it is SAFS. Future test precise condition to create instance.
			instance = IsSeleniumPlus()? instanceSEPLUS():instanceSAFS();
		}
		return instance.validate();
	}
	
	private static SePlusInstallInfo instanceSAFS(){
		return  new SePlusInstallInfo(
				RELATIVE_DIR_EXTRA_SAFS,
				RELATIVE_DIR_LIB_SAFS,
				RELATIVE_DIR_JAVA64_BIN_SAFS,
				PRODUCT_SAFS);
	}
	private static SePlusInstallInfo instanceSEPLUS(){
		return new SePlusInstallInfo(
				RELATIVE_DIR_EXTRA_SEPLUS,
				RELATIVE_DIR_LIB_SEPLUS,
				RELATIVE_DIR_JAVA_BIN_SEPLUS,
				PRODUCT_SELENIUM_PLUS);
	}

	public File getRootDir() {
		return rootDir;
	}
	public void setRootDir(File rootDir) {
		this.rootDir = rootDir;
	}
	public File getExtraDir() {
		return extraDir;
	}
	public void setExtraDir(File extraDir) {
		this.extraDir = extraDir;
	}
	public File getLibraryDir() {
		return libraryDir;
	}
	public void setLibraryDir(File libraryDir) {
		this.libraryDir = libraryDir;
	}
	public String getJavaexe() {
		return javaexe;
	}
	public void setJavaexe(String javaexe) {
		this.javaexe = javaexe;
	}
	public File getSeleniumStandaloneJar() {
		return seleniumStandaloneJar;
	}
	public void setSeleniumStandaloneJar(File seleniumStandaloneJar) {
		this.seleniumStandaloneJar = seleniumStandaloneJar;
	}
	
	public String getClassPath(boolean appendSystemClassPath){
		String cp = seleniumStandaloneJar.getAbsolutePath();

		if(isSeleniumPlus()){
			cp += File.pathSeparatorChar + libraryDir.getAbsolutePath()+File.separator+JAR_SELENIUM_SEPLUS;
		}else{
			cp += File.pathSeparatorChar +libraryDir.getAbsolutePath()+File.separator+JAR_SELENIUM_SAFS;
		}

		cp += File.pathSeparatorChar +libraryDir.getAbsolutePath()+File.separator+JAR_JSTAFEMBEDDED;

		if(appendSystemClassPath) cp += File.pathSeparatorChar+NativeWrapper.GetSystemEnvironmentVariable("CLASSPATH");
		if(cp.contains(" ")) cp ="\""+ cp + "\"";

		return cp;
	}
	
	public File getChromeDriver(){
		if(chromedriver==null){
			chromedriver = new CaseInsensitiveFile(extraDir, CHOROMEDRIVER_WINDOWS).toFile();
			if(!chromedriver.isFile()) chromedriver = new CaseInsensitiveFile(extraDir, CHOROMEDRIVER_UNIX).toFile();
			if(!chromedriver.isFile()){
				IndependantLog.warn(StringUtils.debugmsg(false)+" chromedriver '"+chromedriver+"' is NOT a file.");
			}
		}
		return chromedriver;
	}
	
	public File getIEDriver(){
		if(iedriver==null){
			iedriver = new CaseInsensitiveFile(extraDir, IEDRIVER_WINDOWS).toFile();
			if(!iedriver.isFile()){
				IndependantLog.warn(StringUtils.debugmsg(false)+" iedriver '"+iedriver+"' is NOT a file.");
			}
		}
		return iedriver;
	}
	
	/**
	 * Validate the path of Selenium, Embedded Java, Library etc.<br>
	 * @return SePlusInstallInfo, a valid SePlusInstallInfo holding correct information about Selenium, Embedded Java, Library etc.
	 * @throws SeleniumPlusException if the validation fails.
	 */
	protected SePlusInstallInfo validate() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		String homeEnv = null;
		String installationDir = null;//SEPLUS or SAFS installation directory
		String errmsg = null;
			
		homeEnv = isSeleniumPlus()? ENV_SELENIUM_PLUS: ENV_SAFSDIR;
		installationDir = System.getenv(homeEnv);
		if(!StringUtils.isValid(installationDir)){
			errmsg = "cannot deduce "+ product +" Installation Directory by Environment Variable '"+homeEnv+"'.";
			IndependantLog.debug(debugmsg+errmsg);
			throw new SeleniumPlusException(errmsg);
		}
		
		//Old SAFS installation is NOT shipped with 64 bit java, we have to use 32 bit java
		if(isSAFS()){
			File java64Dir = new CaseInsensitiveFile(installationDir, javabin).toFile();
			if(!java64Dir.isDirectory()){
				IndependantLog.debug(debugmsg+" cannot deduce expected Java64 Installation Directory: "+ javabin);
				javabin = RELATIVE_DIR_JAVA32_BIN_SAFS;
				IndependantLog.debug(debugmsg+" trying older 32-bit Java Installation Directory: "+ javabin);
			}
		}
				
		rootDir = new CaseInsensitiveFile(installationDir).toFile();
		if(!rootDir.isDirectory()){
			errmsg = "cannot confirm "+ product +" install directory at: "+rootDir.getAbsolutePath();
			IndependantLog.debug(debugmsg+errmsg);
			throw new SeleniumPlusException(errmsg);
		}

		extraDir = new File(rootDir, extra);
		if(!extraDir.isDirectory()){
			errmsg = "cannot deduce Selenium 'extra' directory at: "+extraDir.getAbsolutePath();
			IndependantLog.debug(debugmsg+errmsg);
			throw new SeleniumPlusException(errmsg);
		}
		javaexe = System.getProperty(SeleniumConfigConstant.SELENIUMSERVER_JVM);
		if(javaexe==null){
			File javabindir = new CaseInsensitiveFile(rootDir, javabin).toFile();		
			if(javabindir.isDirectory()) javaexe = javabindir.getAbsolutePath()+File.separator+"java";
			else{
				IndependantLog.debug(debugmsg+"can not deduce java bin directory, "+javabindir.getAbsolutePath()+" is not a directory. Simply use 'java' as executable. ");
				javaexe = "java"; 
			}
		}
		if(!StringUtils.isQuoted(javaexe)) javaexe=StringUtils.quote(javaexe);
		
		libraryDir = new CaseInsensitiveFile(rootDir, library).toFile();
		if(!libraryDir.isDirectory()){
			errmsg = "cannot deduce valid "+ product +" library directory at: "+libraryDir.getAbsolutePath();
			IndependantLog.debug(debugmsg+errmsg);
			throw new SeleniumPlusException(errmsg);
		}
		
		//Find the latest selenium-server-standalone jar
		File[] files = libraryDir.listFiles(new FilenameFilter(){ public boolean accept(File dir, String name){
			try{ return name.toLowerCase().startsWith(NAME_PARTIAL_SELENIUM_SERVER_STDALONE);}catch(Exception x){ return false;}
		}});
		if(files.length ==0){
			errmsg = "cannot deduce "+ product +" "+NAME_PARTIAL_SELENIUM_SERVER_STDALONE+"* JAR file in library directory '"+libraryDir.getAbsolutePath()+"'.";
			IndependantLog.debug(debugmsg+errmsg);
			throw new SeleniumPlusException(errmsg);
		}
		long diftime = 0;
		for(File afile: files){
			if(afile.lastModified() > diftime){
				diftime = afile.lastModified();
				seleniumStandaloneJar = afile;
			}
		}
		
		getChromeDriver();
		getIEDriver();
		
		IndependantLog.debug(debugmsg+this);
		
		return this;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(" \n");
		sb.append(" the "+product+" insallation root dir is '"+ rootDir+"'\n");
		sb.append(" the embedded java executable is '"+ javaexe+"'\n");
		sb.append(" the library path is '"+ libraryDir+"'\n");
		sb.append(" the extra path is '"+ extraDir+"'\n");
		sb.append(" the chromedriver path is '"+ chromedriver+"'\n");
		sb.append(" the iedriver path is '"+ iedriver+"'\n");
		sb.append(" the selenium server jar is '"+ seleniumStandaloneJar+"'\n");			

		return sb.toString();
	}
	
	public static final String PRODUCT_SELENIUM_PLUS 	= "SeleniumPlus";
	public static final String PRODUCT_SAFS 			= "SAFS";
	
	public static final String ENV_SELENIUM_PLUS 		= "SELENIUM_PLUS";
	public static final String ENV_SAFSDIR 				= "SAFSDIR";
	
	public static final String RELATIVE_DIR_EXTRA_SAFS 			= "samples/Selenium2.0/extra";
	public static final String RELATIVE_DIR_LIB_SAFS 			= "lib";
	public static final String RELATIVE_DIR_JAVA64_BIN_SAFS 	= "jre/Java64/jre/bin";
	public static final String RELATIVE_DIR_JAVA32_BIN_SAFS 	= "jre/bin";
	
	public static final String RELATIVE_DIR_EXTRA_SEPLUS 		= "extra";
	public static final String RELATIVE_DIR_LIB_SEPLUS 			= "libs";
	public static final String RELATIVE_DIR_JAVA_BIN_SEPLUS 	= "Java64/jre/bin";
	
	public static final String JAR_SELENIUM_SAFS 				= "safsselenium.jar";
	public static final String JAR_SELENIUM_SEPLUS 				= "seleniumplus.jar";	
	public static final String JAR_JSTAFEMBEDDED		 		= "JSTAFEmbedded.jar";

	// file:/c:/pathTo/libs/selenium-plus*.jar
	public static final String INDICATOR_SEPLUS		 		= "/libs/selenium";
	// file:/c:/pathTo/lib/safsselenium*.jar
	public static final String INDICATOR_SAFS		 		= "/lib/safsselenium";
	
	public static final String CHOROMEDRIVER_WINDOWS 		= "chromedriver.exe";
	public static final String CHOROMEDRIVER_UNIX	 		= "chromedriver";
	
	public static final String IEDRIVER_WINDOWS	 			= "IEDriverServer.exe";
	
	public static final String NAME_PARTIAL_SELENIUM_SERVER_STDALONE	= "selenium-server-standalone";
	
	private static String _sourceLocation = null;
	/**
	 * @return String, the source location of this class. ( xxx/libs/selenium-plus*.jar or xxx/lib/safsselenium.jar)
	 */
	public static String getSourceLocation(){
		if(_sourceLocation==null){
			URL domain = SePlusInstallInfo.class.getProtectionDomain().getCodeSource().getLocation();
			_sourceLocation = domain.getFile();			
		}
		IndependantLog.info(StringUtils.debugmsg(false)+" class Location:"+ _sourceLocation); 
		return _sourceLocation;
	}
	
	/**
	 * @return true if we detect we are running from a SeleniumPlus installation (/libs/selenium-plus*.jar)
	 */
	public static boolean IsSeleniumPlus(){
		String filepath = getSourceLocation();
		return filepath.toLowerCase().contains(INDICATOR_SEPLUS);
	}
	
	/**
	 * @return true if we detect we are running from a SAFS installation (/lib/safsselenium.jar)
	 */
	public static boolean IsSAFS(){
		String filepath = getSourceLocation();
		return filepath.toLowerCase().contains(INDICATOR_SAFS);
	}
}
