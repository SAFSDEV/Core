/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * SEP 20, 2016    (Lei Wang) Initial release: Moved existing code from WebDriverGUIUtilities to here.
 * MAR 02, 2016    (Lei Wang) Refactored code to get driver file according to the browser name.
 *                          Changed the RELATIVE_DIR_EXTRA_SAFS: use 'extra' instead of 'samples/Selenium2.0/extra', all drivers have been put into the folder 'extra'.
 * APR 11, 2017    (Lei Wang) Added methods to detect embedded Eclipse, get Eclipse configuration info. Added IsProduct().
 */
package org.safs.selenium.util;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.safs.Constants;
import org.safs.Constants.BrowserConstants;
import org.safs.IndependantLog;
import org.safs.StringUtils;
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
	private String eclipse = null;
	private String library = null;
	private String javabin = null;
	private String product = null;

	/** The installation directory of SAFS or SeleniumPlus */
	private File rootDir = null;
	/** The extra directory holding extra resources related to Selenium */
	private File extraDir = null;
	/** The eclipse directory holding Eclipse resources embedded in SeleniumPlus */
	private File eclipseDir = null;
	/** The library directory holding the library jar files etc. */
	private File libraryDir = null;
	/** The full path of java executable provided by SAFS or SeleniumPlus*/
	private String javaexe = null;
	/** The latest selenium-standalone jar file provided by SAFS or SeleniumPlus*/
	private File seleniumStandaloneJar = null;

	/**
	 * Holding the pair(<b>key</b>, driver-executable-file).<br/>
	 * The <b>key</b> is the browser name, such as
	 * <ul>
	 * <li>{@link BrowserConstants#BROWSER_NAME_CHROME}
	 * <li>{@link BrowserConstants#BROWSER_NAME_IE}
	 * <li>{@link BrowserConstants#BROWSER_NAME_EDGE}
	 * </ul>
	 * The <b>value</b> is the File object of that driver executable.
	 * <b>Note:</b>It supposed that only one executable exists for a certain browser
	 * under an Operating System. If multiple executables exist, we may use a List as value.
	 * */
	private Map<String, File> browserToDriverFile = new HashMap<String, File>();

	/**
	 * Holding the pair(<b>key</b>, a <b>list</b> of driver-file-name).<br/>
	 * The <b>key</b> is the browser name, such as
	 * <ul>
	 * <li>{@link BrowserConstants#BROWSER_NAME_CHROME}
	 * <li>{@link BrowserConstants#BROWSER_NAME_IE}
	 * <li>{@link BrowserConstants#BROWSER_NAME_EDGE}
	 * </ul>
	 * The <b>list</b> of driver-file-name contains the driver file name under different OS.
	 */
	private static Map<String, List<String>> browserToDriverFiles = new HashMap<String, List<String>>();

	/** The instance of this class. */
	private static SePlusInstallInfo instance = null;

	/**
	 * @param extra String, 'extra' directory relative to root installation directory
	 * @param eclipse String, 'eclipse' directory relative to root installation directory
	 * @param library String, 'library' directory relative to root installation directory
	 * @param javabin String, 'java bin' directory relative to root installation directory
	 * @param product String, product name, {@link #PRODUCT_SAFS} or {@link #PRODUCT_SELENIUM_PLUS}
	 */
	private SePlusInstallInfo(String extra, String eclipse, String library, String javabin, String product) {
		super();
		this.extra = extra;
		this.eclipse = eclipse;
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
		IndependantLog.debug("Intialize SePlusInstallInfo with SAFS Product.");
		return  new SePlusInstallInfo(
				RELATIVE_DIR_EXTRA_SAFS,
				RELATIVE_DIR_ECLIPSE,
				RELATIVE_DIR_LIB_SAFS,
				RELATIVE_DIR_JAVA64_BIN_SAFS,
				PRODUCT_SAFS);
	}
	private static SePlusInstallInfo instanceSEPLUS(){
		IndependantLog.debug("Intialize SePlusInstallInfo with SeleniumPlus Product.");
		return new SePlusInstallInfo(
				RELATIVE_DIR_EXTRA_SEPLUS,
				RELATIVE_DIR_ECLIPSE,
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
	/**
	 * @return File, the Eclipse directory {@link #eclipseDir}.
	 */
	public File getEclipseDir() {
		return eclipseDir;
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

		if(appendSystemClassPath) cp += File.pathSeparatorChar+GetSystemEnvironmentVariable("CLASSPATH");
		if(cp.contains(" ")) cp ="\""+ cp + "\"";

		return cp;
	}

	public static boolean isOverridePropertySet(String env){
		/*
		 * If the override property is set, then use of the installed product is not
		 * desired.  This would especially be true during testing, but it
		 * also applies when the SeleniumPlusClient is being used.
		 */
		return Boolean.getBoolean(env + "_OVERRIDE");
	}

	public static String GetSystemPropertyOrEnvironmentVariable(String env){
		String result = null;

		boolean override = isOverridePropertySet(env);
		result = override ? System.getProperty(env) : null;

		if (result==null){
			result = System.getenv(env);
		}
		return result;
	}

	protected static String GetSystemEnvironmentVariable(String env){
		Object result = null;

		result = GetSystemPropertyOrEnvironmentVariable(env);

		String nativeWrapperClassName = "org.safs.natives.NativeWrapper";
		Class<?> nativeWrapperClass = null;

		if(result==null){
			try {
				nativeWrapperClass = Class.forName(nativeWrapperClassName);
//				Method method = nativeWrapperClass.getMethod(StringUtils.getCurrentMethodName(false), env.getClass());
				Method method = nativeWrapperClass.getMethod("GetSystemEnvironmentVariable", Object.class);
				result = method.invoke(null, env);
			} catch (Exception e) {
				IndependantLog.warn(StringUtils.debugmsg(false)+" cannot find "+nativeWrapperClassName);
			}
		}

		return result==null?null:result.toString();
	}

	/**
	 * Get the value of a property from the Eclipse configuration file (under Eclipse directory {@link #getEclipseDir()})
	 * <ul>
	 * <li>".eclipseproduct"
	 * <li>"configuration\config.ini"
	 * </ul>
	 * @param property String, the property name in Eclipse configuration file.
	 * @return String, the value of the property
	 * @see #getEclipseDir()
	 */
	public String getEclipseConfig(String property){
		String value = null;
		Properties p = new Properties();
		try {
			p.load(new FileReader(getEclipseDir().getAbsolutePath()+File.separator+".eclipseproduct"));
			if(p.containsKey(property)){
				value = p.getProperty(property);
			}
		} catch (Exception e) {
			IndependantLog.debug(StringUtils.debugmsg(false)+"Failed to get property '"+property+"' from file '.eclipseproduct', due to "+e.toString());
		}

		if(value==null){
			try {
				p.load(new FileReader(getEclipseDir().getAbsolutePath()+File.separator+"configuration"+File.separator+"config.ini"));
				if(p.containsKey(property)){
					value = p.getProperty(property);
				}
			} catch (Exception e) {
				IndependantLog.debug(StringUtils.debugmsg(false)+"Failed to get property '"+property+"' from file 'config.ini', due to "+e.toString());
			}
		}

		return value;
	}

	public File getChromeDriver(){
		return getDriver(BrowserConstants.BROWSER_NAME_CHROME);
	}

	public File getIEDriver(){
		return getDriver(BrowserConstants.BROWSER_NAME_IE);
	}

	public File getEdgeDriver(){
		return getDriver(BrowserConstants.BROWSER_NAME_EDGE);
	}

	/**
	 *
	 * @param browserName String, the key representing the browser, such as
	 * <ul>
	 * <li>{@link BrowserConstants#BROWSER_NAME_CHROME}
	 * <li>{@link BrowserConstants#BROWSER_NAME_IE}
	 * <li>{@link BrowserConstants#BROWSER_NAME_EDGE}
	 * </ul>
	 * @return File, the driver executable file.
	 *               The return value is never null, we need to call {@link File#isFile()} to verify its validity.
	 */
	public File getDriver(String browserName){
		String debugmsg = StringUtils.debugmsg(false);
		File driver = browserToDriverFile.get(browserName);
		if(driver==null || !driver.isFile()){
			List<String> names = browserToDriverFiles.get(browserName);
			if(names!=null){
				//iterate through driver names, we use the first found.
				for(String name:names){
					driver = new CaseInsensitiveFile(extraDir, name).toFile();
					browserToDriverFile.put(browserName, driver);
					if(driver.isFile()) break;
				}
			}else{
				IndependantLog.warn(debugmsg+browserName+" driver has not been supported!");
			}

			driver = browserToDriverFile.get(browserName);
			if(driver==null){
				//We should not return a null as result to keep the backward compatibility.
				IndependantLog.debug(debugmsg+" cannot get '"+browserName+"' driver! Put a fake File in the Map.");
				driver = new CaseInsensitiveFile(extraDir, FAKE_DRIVER_NAME).toFile();
				browserToDriverFile.put(browserName, driver);
			}else if(driver.isFile()){
				IndependantLog.debug(debugmsg+browserName+" driver '"+driver+"' has been found.");
			}
		}

		return driver;
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
		installationDir = GetSystemEnvironmentVariable(homeEnv);
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
		eclipseDir = new File(rootDir, eclipse);
		if(!eclipseDir.isDirectory()){
			errmsg = "cannot deduce 'eclipse' directory at: "+eclipseDir.getAbsolutePath();
			IndependantLog.warn(debugmsg+errmsg);
			eclipseDir = null;
			//SAFS doesn't have the embedded Eclipse, so we will not
			//throw new SeleniumPlusException(errmsg);
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

		for(String browser:browserToDriverFiles.keySet()){
			getDriver(browser);
		}

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
		sb.append(" the embedded eclipse path is '"+ eclipseDir+"'\n");
		for(String browser:browserToDriverFiles.keySet()){
			sb.append(" the '"+browser+"' driver path is '"+ getDriver(browser) +"'\n");
		}
		sb.append(" the selenium server jar is '"+ seleniumStandaloneJar+"'\n");

		return sb.toString();
	}

	public static final String PRODUCT_SELENIUM_PLUS 	= "SeleniumPlus";
	public static final String PRODUCT_SAFS 			= "SAFS";

	public static final String ENV_SELENIUM_PLUS 		= Constants.ENV_SELENIUM_PLUS;
	public static final String ENV_SAFSDIR 				= Constants.ENV_SAFSDIR;

	public static final String RELATIVE_DIR_ECLIPSE 			= "eclipse";

	public static final String RELATIVE_DIR_EXTRA_SAFS 			= "extra";
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

	public static final String EDGEDRIVER_WINDOWS	 		= "MicrosoftWebDriver.exe";

	public static final String OPERADRIVER_WINDOWS	 		= "operadriver.exe";

	public static final String FAKE_DRIVER_NAME	 			= "aFakeBrowserDriver.exe";

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
	 * To test if we are using product "SeleniumPlus". Needs satisfy:
	 * <ul>
	 * <li>The environment {@link Constants#ENV_SELENIUM_PLUS} must exist.
	 * <li>We are running from a SeleniumPlus installation (/libs/selenium-plus*.jar or {@link Constants#ENV_SELENIUM_PLUS})
	 * </ul>
	 * @return true if we are using product "SeleniumPlus"
	 */
	public static boolean IsSeleniumPlus(){
		return IsProduct(ENV_SELENIUM_PLUS, INDICATOR_SEPLUS);
	}
	/**
	 * To test if we are using product "SAFS". Needs satisfy:
	 * <ul>
	 * <li>The environment {@link Constants#ENV_SAFSDIR} must exist.
	 * <li>We are running from a SAFS installation (/lib/safsselenium.jar or {@link Constants#ENV_SAFSDIR})
	 * </ul>
	 * @return boolean if we detect we are using product "SAFS"
	 */
	public static boolean IsSAFS(){
		return IsProduct(ENV_SAFSDIR, INDICATOR_SAFS);
	}

	private static boolean IsProduct(String environmentHome, String indicator){
		String sourceLocation = getSourceLocation().toLowerCase();

		String home = GetSystemPropertyOrEnvironmentVariable(environmentHome);

		if(home!=null){
			//replace backslash "\" by slash "/"
			if(File.separator.equals("\\")){
				home = home.replaceAll("\\\\", "/");
			}
			IndependantLog.debug(environmentHome+"="+home);
			IndependantLog.debug("sourceLocation="+sourceLocation);
			return sourceLocation.contains(indicator) || sourceLocation.contains(home.toLowerCase());
		}

		return false;
	}

	static{
		browserToDriverFiles.put(BrowserConstants.BROWSER_NAME_CHROME, Arrays.asList(CHOROMEDRIVER_WINDOWS, CHOROMEDRIVER_UNIX));
		browserToDriverFiles.put(BrowserConstants.BROWSER_NAME_IE, Arrays.asList(IEDRIVER_WINDOWS));
		browserToDriverFiles.put(BrowserConstants.BROWSER_NAME_EDGE, Arrays.asList(EDGEDRIVER_WINDOWS));
		browserToDriverFiles.put(BrowserConstants.BROWSER_NAME_OPERA, Arrays.asList(OPERADRIVER_WINDOWS));
		//Add more drivers for other browser
	}
}
