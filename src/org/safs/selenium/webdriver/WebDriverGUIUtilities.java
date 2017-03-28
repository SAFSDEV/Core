/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.selenium.webdriver;

/**
 * History:<br>
 *
 *  <br>   NOV 19, 2013    (CANAGL) Initial release.
 *  <br>   DEC 18, 2013    (SBJLWA) Modify method getCompType() to support DOJO object.
 *  <br>   JAN 08, 2014    (DHARMESH) Add EditBox support.
 *  <br>   JAN 15, 2014    (SBJLWA) Update method getCompType(): deduce the type by dojo-classname if object is dojo domain.
 *  <br>   FEB 11, 2014    (CANAGL) Support external Class2Type.properties and CustomClass2Type.properties mappings.
 *  <br>   FEB 19, 2014    (DHARMESH) Modify Start Remote Server call.
 *  <br>   APR 15, 2014    (DHARMESH) Added HighLight static varibale.
 *  <br>   APR 21, 2014    (DHARMESH) Allow default window RS if user doesn't specify RS.
 *  <br>   NOV 26, 2014    (SBJLWA) Modify waitForObject(): distinguish ObjectNotFound from other errors.
 *  <br>   FEB 27, 2015    (DHARMESH) Added -Xms512m -Xmx2g support to start server JVM.
 *  <br>   APR 08, 2015    (SBJLWA) Modify to permit user to set JVM options for starting SELENIUM server.
 *  <br>   JUN 23, 2015    (SCNTAX) Add waitForPropertyStatus(): wait for property value match or gone with expected value.
 *  <br>   JUN 29, 2015	   (LeiWang) Modify startRemoteServer(): handle selenium grid (hub+node). Output standard out/err message to console.
 *                                   Add methods to handle/test standalone server, grid server like isXXXRunning(), canConnectXXX(), waitXXXRunning() etc.
 *  <br>   JUL 14, 2015	   (LeiWang) Modify isTypeMatched(): try getCompType() firstly and make it more reliable.
 *  <br>   JUL 14, 2015	   (CANAGL) Modify class/type mappings to automatically trim the class Type setting of any spaces and tabs.
 *  <br>   OCT 30, 2015	   (LeiWang) Modify waitForPropertyStatus(): highlight component.
 *  <br>   NOV 02, 2015	   (CANAGL) startRemoteServer on SAFS supporting jre/Java64/jre/bin 64-bit JVM.
 *  <br>   NOV 23, 2015	   (LeiWang) Modify waitForObject(): refresh the window object if it becomes stale during the searching of component object.
 *  <br>   DEC 24, 2015	   (LeiWang) Add methods to read content from url like "http://host:port/wd/hub/static"
 *  <br>   AUG 05, 2016	   (LeiWang) Modified waitForObject(): if RS is AutoIT or IBT, then return SCRIPT_NOT_EXECUTED (4).
 *  <br>   AUG 09, 2016	   (LeiWang) Modified setWDTimeoutxxx()/resetWDTimeoutxxx(): return a boolean value to tell if succeed.
 *  <br>   SEP 27, 2016	   (LeiWang) Moved methods launchSeleniumServers() from class DCDriverCommand.
 *                                   Modified method startRemoteServer(): moved some code to class SePlusInstallInfo.
 *  <br>   NOV 07, 2016	   (LeiWang) Modified method startRemoteServer(): Wait longer to get more information from STDOUT/STDERR, if no running server has been detected.
 *  <br>   MAR 07, 2017	   (LeiWang) Modified methods launchSeleniumServers(), startRemoteServer(): start selenium server with 'browser drivers' option.
 *  <br>   MAR 24, 2017	   (LeiWang) Modified startRemoteServer(): even we cannot determine the installed product, we still try to launch from default batch file.
 **/

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openqa.selenium.WebElement;
import org.safs.ApplicationMap;
import org.safs.Constants.SeleniumConstants;
import org.safs.DDGUIUtilities;
import org.safs.GuiClassData;
import org.safs.GuiObjectRecognition;
import org.safs.IndependantLog;
import org.safs.JavaConstant;
import org.safs.Processor;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordData;
import org.safs.Tree;
import org.safs.autoit.AutoItRs;
import org.safs.image.ImageUtils;
import org.safs.jvmagent.AgentClassLoader;
import org.safs.logging.LogUtilities;
import org.safs.natives.NativeWrapper;
import org.safs.net.NetUtilities;
import org.safs.selenium.STestRecordHelper;
import org.safs.selenium.util.SePlusInstallInfo;
import org.safs.selenium.util.SeleniumServerRunner;
import org.safs.selenium.webdriver.lib.RS;
import org.safs.selenium.webdriver.lib.RS.XPATH;
import org.safs.selenium.webdriver.lib.RemoteDriver;
import org.safs.selenium.webdriver.lib.RemoteDriverLauncher;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.SearchObject.DOJO;
import org.safs.selenium.webdriver.lib.SearchObject.SAP;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.staf.service.map.AbstractSAFSAppMapService;
import org.safs.text.CaseInsensitiveHashtable;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.consoles.JavaJVMConsole;
import org.safs.tools.consoles.ProcessCapture;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverConstant.SeleniumConfigConstant;

import com.ibm.staf.STAFResult;

/**
 * SAFS/Selenium-specific subclass of DDGUIUtilities.
 */
public class WebDriverGUIUtilities extends DDGUIUtilities {

	/**
	 * "Class2Type.properties"
	 * Typically embedded in the JAR file for the class seeking the mapping. */
	public static final String DEFAULT_CLASS2TYPE_MAP = "Class2Type.properties";
	/**
	 * "CustomClass2Type.properties"
	 * should exist in the /lib/ or /libs/ directory of the JAR file seeking the mapping. */
	public static final String CUSTOM_CLASS2TYPE_MAP = "CustomClass2Type.properties";

	public static boolean HIGHLIGHT = false;

	public static WebDriverGUIUtilities _LASTINSTANCE = null;

	private long gSecTimeout = 0;// or Processor.getSecsWaitForComponent();

	/*
	 * Mapped non-generic html tag to component function types here.<br>
	 * Non-mapped items should be considered to be generic "Component" types.
	 */
	private static final CaseInsensitiveHashtable _cfmap = new CaseInsensitiveHashtable();

	/*
	 * Type to classname mapping. One type can be mapped to more than one classes. The class
	 * names are separated by comma (,)
	 * CheckBox: sap.ui.commons.CheckBox, checkbox
	 */
	private static final CaseInsensitiveHashtable _fcmap = new CaseInsensitiveHashtable();
	private static final String CLASS_NAME_SEPARATOR = ",";

	/**
	 * No-argument constructor.
	 * Simply calls super()
	 * @see DDGUIUtilities#DDGUIUtilities()
	 */
	public WebDriverGUIUtilities() {
		super();
		_LASTINSTANCE = this;
	}

	/**
	 * Constructor providing the STAFHelper and LogUtilities needed for
	 * proper operation.
	 * @param helper The STAFHelper for performing STAF requests.
	 * @param log the LogUtilities for logging.
	 * @see org.safs.STAFHelper
	 * @see org.safs.logging.LogUtilities
	 **/
	public WebDriverGUIUtilities(STAFHelper helper, LogUtilities log) {
		super();
		setLogUtilities(log); //must be first
		setSTAFHelper(helper);
		_LASTINSTANCE = this;
	}

	/**
	 * Constructor providing the STAFHelper, LogUtilities, and TestRecordData for proper operation.
	 * The SAFS/Selenium engine generally uses an instanceof STestRecordHelper for TestRecordData.
	 *
	 * @param helper The STAFHelper for performing STAF requests.
	 * @param data the TestRecordData generally an instanceof STestRecordHelper.
	 * @param log the LogUtilities for logging.
	 * @see org.safs.STAFHelper
	 * @see STestRecordHelper
	 * @see org.safs.logging.LogUtilities
	 **/
	public WebDriverGUIUtilities(STAFHelper helper,TestRecordData data, LogUtilities log) {
		this(helper, log);
		setTestRecordData(data);
		_LASTINSTANCE = this;
	}

	/**
	 * @return true if we detect we are running from a SeleniumPlus installation (/libs/selenium-plus*.jar)
	 */
	public static boolean isSeleniumPlus(){
		return SePlusInstallInfo.IsSeleniumPlus();
	}

	/**
	 * @return true if we detect we are running from a SAFS installation (/lib/safsselenium.jar)
	 */
	public static boolean isSAFS(){
		return SePlusInstallInfo.IsSAFS();
	}

	/**
	 * Retrieve the Class2Type mapping, if any, for the provided classname.
	 * This will force the loading of class mappings if they are not already loaded.
	 * @param classnames String[] an array of classnames used to get mapped-type, the first one
	 *                            will be tried firstly, then next one, until a mapped-type is got.
	 * @return the mapped type or null if not mapped.
	 * @see #loadClassMappings()
	 */
	public static String getClassTypeMapping(String... classnames){
		IndependantLog.info("WDGU seeking class mapping for '"+Arrays.toString(classnames)+"' ...");
		if(_cfmap.isEmpty()) loadClassMappings();
		try{
			Object type = null;
			for(String classname: classnames){
				type = _cfmap.get(classname);
				if(type!=null && type instanceof String){
					return ((String) type).trim();
				}
			}
		}
		catch(Exception x){
			IndependantLog.debug("WDGU getClassTypeMapping "+StringUtils.debugmsg(x));
		}
		return null;
	}

	/**
	 * Retrieve the Type2Classes mapping, if any, for the provided type name.
	 * This will force the loading of class mappings if they are not already loaded.
	 * @param type
	 * @return String[], an array of the mapped class-names or null if not mapped.
	 * @see #loadClassMappings()
	 */
	public static String[] getTypeClassesMapping(String type){
		IndependantLog.info("WDGU seeking type mapping for '"+type+"'...");
		if(_fcmap.isEmpty()) loadClassMappings();
		try{
			String typeMappedClassNames = (String) _fcmap.get(type.trim());
			if(typeMappedClassNames!=null){
				String[] classNames = typeMappedClassNames.split(CLASS_NAME_SEPARATOR);
				for(int i=0;i<classNames.length;i++){
					classNames[i] = classNames[i].trim();
				}
				return classNames;
			}
		}catch(Exception x){
			IndependantLog.debug("WDGU getTypeClassesMapping "+x.getClass().getSimpleName()+": "+x.getMessage());
		}
		return null;
	}

	/**
	 * Get the mapped-class-names according to the type; then get the webelement's<br>
	 * class-name (and its superclasses' names) if the webelement is DOJO/SAP object,<br>
	 * get the webelement's tag if the webelement is a standard HTML object.
	 * <p>
	 * Finally, compare the mapped-class-names with webelement's class-names, if one<br>
	 * of them match, then we can say the type is matched.<br>
	 *
	 * <b>Note:</b>
	 * For DOJO, we can only get the class-name of webelement itself.<br>
	 * We need a way to get its superclass names.
	 *
	 * @param we	WebElement, the webelement to test
	 * @param type	String, the SAFS named type for a gui component
	 * @return	boolean, true if the webelement can match the provided type.
	 */
	public static boolean isTypeMatched(WebElement we, String type){
		String debugmsg = StringUtils.debugmsg(false);
		String realType = type;
		String[] typeMappedClassNames = null;
		List<String> elementClassNames = new ArrayList<String>();
		if(we==null || type==null) return false;
		boolean isDojo = false;
		boolean isSap = false;
		try {
			type = type.trim();
			if(type.toUpperCase().startsWith(SearchObject.DOMAIN_DOJO)){
				realType = type.substring(SearchObject.DOMAIN_DOJO.length());
				isDojo = true;
			}else if(type.toUpperCase().startsWith(SearchObject.DOMAIN_SAP)){
				realType = type.substring(SearchObject.DOMAIN_SAP.length());
				isSap = true;
			}else{
				realType = type;
			}

			String compType = getCompType(we);
			IndependantLog.debug(debugmsg+realType+"="+compType+"?");
			if(realType.equalsIgnoreCase(compType)) return true;

			if(isDojo || WDLibrary.isDojoDomain(we)){
				IndependantLog.info(debugmsg+"searching for a Dojo "+ realType);
//				elementClassNames = DOJO.getDojoClassNames(we);
				String classname = DOJO.getDojoClassName(we);
				if(classname!=null) {
					IndependantLog.info(debugmsg+"found Dojo class "+ classname);
					elementClassNames.add(classname);
				}
			}else if(isSap || WDLibrary.isSAPDomain(we)){
				IndependantLog.info(debugmsg+"searching for a SAP "+ realType);
				elementClassNames = SAP.getSAPClassNames(we);

				IndependantLog.info(debugmsg+"found "+ elementClassNames.size() +" in SAP class hierarchy");
			}
			//We will always add the HTML classes to the list elementClassNames
			String[] classes = WDLibrary.HTML.html_getClassName(we);
			IndependantLog.info(debugmsg+"found HTML classes "+ Arrays.toString(classes));
			if(classes!=null) {
				for(String classname: classes) elementClassNames.add(classname);
			}

			typeMappedClassNames = getTypeClassesMapping(realType);
			IndependantLog.info(debugmsg+"comparing against "+ typeMappedClassNames.length +" classes mapped to type "+realType);
			for(String elementClassName: elementClassNames){
				for(String mappedClassName: typeMappedClassNames){
					if(mappedClassName.equalsIgnoreCase(elementClassName)) {
						IndependantLog.info(debugmsg+"matched element class '"+ elementClassName +"' to mapped class '"+ mappedClassName +"'.");
						return true;
					}
				}
			}
		} catch (SeleniumPlusException e) {
			IndependantLog.debug(debugmsg+" cannot get class names for webelement.", e);
		}
		IndependantLog.info(debugmsg+"did not match element as type "+ type);
		return false;
	}

	/**
	 * load the Class2Type.properties mappings and any CustomClass2Type.properties mappings.
	 * CustomClass2Type.properties mappings, if present,
	 * would normally exist in the directory containing the JAR file containing this class.
	 */
	protected static void loadClassMappings(){
		try{
			URL jom = getResourceURL(DEFAULT_CLASS2TYPE_MAP);
			URL customjom = AgentClassLoader.findCustomizedJARResource(jom, CUSTOM_CLASS2TYPE_MAP);

			IndependantLog.info("WDGU.loading standard mappings from "+jom.getPath());
			InputStream in = jom.openStream();
			Properties props = new Properties();
			props.load(in);
			in.close();
			in = null;
			_cfmap.putAll(props);
			if(customjom != null){
				in = customjom.openStream();
				props = new Properties();
				IndependantLog.info("WDGU.merging custom mappings from "+customjom.getPath());
				props.load(in);
				_cfmap.putAll(props);
				in.close();
				in = null;
			}
			props.clear();
			props = null;
			// see if there are local customizations defined
			// this may now be obsolete if the AgentClassLoader stuff above works.
			try{
				in = ClassLoader.getSystemResourceAsStream( CUSTOM_CLASS2TYPE_MAP );
				if(in == null){
					try{
						String safsdir = null;
						if(isSAFS()){
							safsdir = System.getenv(DriverConstant.SYSTEM_PROPERTY_SAFS_DIR)+ File.separator+"lib"+File.separator;
						}else{//isSeleniumPlus
							safsdir = System.getenv(DriverConstant.SYSTEM_PROPERTY_SELENIUMPLUS_DIR)+ File.separator +"libs"+File.separator;
						}
						File custurl = new CaseInsensitiveFile(safsdir + CUSTOM_CLASS2TYPE_MAP).toFile();
						if(custurl.isFile()) in = custurl.toURL().openStream();
					}catch(MissingResourceException x){
						IndependantLog.info("WDGU ignoring missing custom mappings for "+ CUSTOM_CLASS2TYPE_MAP);
					}catch(MalformedURLException x){
						IndependantLog.info("WDGU ignoring malformed URL mappings for "+ CUSTOM_CLASS2TYPE_MAP);
					}
				}
				if(in != null){
					props = new Properties();
					IndependantLog.info("WDGU merging custom mappings from "+ CUSTOM_CLASS2TYPE_MAP);
					props.load(in);
					in.close();
					in = null;
					if (props.size() > 0) _cfmap.putAll(props);
					props.clear();
					props = null;
				}
			}
			catch(Exception anye){
				IndependantLog.error("Error loading "+ CUSTOM_CLASS2TYPE_MAP +" resource:"+ anye.getMessage(),
	                      anye);
			}

			Enumeration<?> clazzes = null;
			String className = null;
			String type = null;
			String classNames = null;
			if(!_cfmap.isEmpty()){
				clazzes = _cfmap.keys();
				while(clazzes.hasMoreElements()){
					className = (String) clazzes.nextElement();
					type = ((String) _cfmap.get(className)).trim();
					classNames = (String) _fcmap.get(type);
					if(classNames==null) _fcmap.put(type, className);
					else _fcmap.put(type, classNames+CLASS_NAME_SEPARATOR+className);
				}
			}

		}catch(Exception ex){
			try{
				//If ex.getMessage() return null, for some java sdk, println(null) will throw NullPointerException
				System.err.println(ex.getMessage());
				IndependantLog.error(ex.getMessage(), ex);
			}catch(Exception e){
				System.err.println(e);
			}
		}
	}

	/**
	 * Attempt to locate the URL of a resource.
	 * This can be in the JAR file containing a specified class,
	 * or in the directory containing the JAR file.
	 * Multitple searches attempted including:
	 * <ol>
	 * <li> getUniversalResourceURL
	 * <li> AgentClassLoader.getResource (using SAFSDIR pathTo/safs.jar)
	 * <li> AgentClassLoader.getResource (using SELENIUM_PLUS pathTo/seleniumplus*.jar)
	 * </ol>
	 * @param clazz -- Class associated with the resource -- mapping to the JAR or directory resource might be found.
	 * @param aresource -- generally, the filename of the resource.
	 * @return URL to a loadable resource or MissingResourceException is thrown.
	 * @throws MissingResourceException if not found
	 * @see #getUniversalResourceURL(Class, String)
	 * @see org.safs.jvmagent.AgentClassLoader#AgentClassLoader(String)
	 */
	protected static URL getResourceURL(String aresource){
		URL jom = null;
		try{ jom = GuiClassData.getUniversalResourceURL(WebDriverGUIUtilities.class, aresource);}
		catch(MissingResourceException ignore){}
		if (jom == null){
			URL domain = WebDriverGUIUtilities.class.getProtectionDomain().getCodeSource().getLocation();
			String filepath = domain.getFile(); //  /C:/SAFS/lib/safs.jar  or  /C:/SeleniumPlus/libs/selenium-plus*.jar
			// ex: /C:/SAFS/lib/safs.jar
			if(domain.getProtocol().equals("file")){
				filepath = filepath.substring(1);
				filepath.replace("/", File.separator);
				IndependantLog.info("WDGU: trying AgentClassLoader with JAR path...");
				IndependantLog.info("    "+ filepath);
				AgentClassLoader loader = new AgentClassLoader(filepath);
				jom = loader.getResource(aresource);
			}
		}
		if (jom == null){
			IndependantLog.debug("GCD: throwing MissingResourceException for "+ aresource);
			throw new java.util.MissingResourceException(aresource,aresource,aresource);
		}
		return jom;
	}

	/**
	 * In SeleniumPlus, some GUILess keywords will be handled in CFComponent<br>
	 * So a fake window 'ApplicationConstants' is used to get these keywords executed.<br>
	 * This method is used to detected if the window is fake or not.<br>
	 * @param windowName String, the window name
	 * @param compName String, the component name
	 * @return boolean if this is a fake window
	 * @see #waitForObject(String, String, String, long)
	 */
	protected boolean isFakeWindow(String windowName, String compName){
		boolean isFakeWindow = false;
		if(windowName!=null && compName!=null){
			isFakeWindow = windowName.equals(compName) && AbstractSAFSAppMapService.DEFAULT_SECTION_NAME.equals(windowName);
		}
		return isFakeWindow;
	}

	/**
	 * Uses the Application Map caching mechanism provided by the superclass.
	 * Retrieves cached references or recognition strings from the App Map and attempts to
	 * identify the matching component via the remote AUT JVM proxies.
	 * <p>
	 * @param appMapName  the name/ID of the App Map currently in use.
	 * @param windowName  the name/ID of the window as predefined in the App Map.
	 * @param compName    the name/ID of the child component of the window as predefined
	 * in the App Map.
	 * @param secTimeout the number of seconds allowed to located the object before a
	 * SAFSObjectNotFoundException is thrown.
	 *
	 * @return 0 on success.<br>
	 *         {@link StatusCodes#SCRIPT_NOT_EXECUTED} if the recognition string is in AUTOIT/IBT format.<br>
	 * @throws SAFSObjectNotFoundException if specified parent or child cannot be found.
	 * @throws SAFSException if other problem occurs<br>
	 *                       such as Map is not registered, or windowName/compName cannot be found in map.<br>
	 * @see org.safs.DDGUIUtilities#waitForObject(String,String,String,long)
	 * @see #waitCompObject(WebElement, String, String, String, String, long)
	 * @see #waitWinObject(String, String, long)
	 */
	public int waitForObject (String appMapName, String windowName, String compName, long secTimeout)
	                           throws SAFSObjectNotFoundException, SAFSException{

		ApplicationMap map = null;
		WDTestRecordHelper trdata = (WDTestRecordHelper) this.trdata;

	    try{
	    	IndependantLog.info("WDGU: WFO Looking for "+windowName+"."+compName+" using AppMap:"+appMapName);
	    	map = getAppMap(appMapName);

		    if (map == null) {
		      if (registerAppMap(appMapName, appMapName)) {
		        map = (ApplicationMap) getAppMap(appMapName);
		        if (map == null) {
		          IndependantLog.debug("WDGU: WFO could NOT retrieve registered AppMap "+ appMapName);
		          throw new SAFSException("Could not retrieve App Map "+ appMapName);
		        }
		      }
		      // what if NOT registered?
		      else{
		          IndependantLog.debug("WDGU: WFO could NOT register AppMap "+ appMapName);
		          throw new SAFSException("Could not register App Map "+ appMapName);
		      }
		    }

		    //TODO: Find and inject into window not launched by us.
    		//TODO: currently, we do no caching so we don't care if the recognition is dynamic.
	    	String winRec = map.getParentGUIID(windowName, false);
	    	//CACHE HANDLE
//	    	boolean ignoreCache = ApplicationMap.isGUIIDDynamic(winRec);
//	    	if(ignoreCache) winRec = ApplicationMap.extractTaggedGUIID(winRec);

	    	//If user doesn't specify window RS then use html default window.
	    	if (winRec == null || winRec.length() == 0){
	    		if(isFakeWindow(windowName, compName)){
	    			//throw new SAFSObjectNotFoundException("Give up looking for fake window '"+windowName+"'.");
		    		trdata.setCompGuiId(windowName);
		    		trdata.setWindowGuiId(compName);
		    		trdata.setCompTestObject(null);
		    		trdata.setWindowTestObject(null);
		    		trdata.setCompType("Component");
		    		IndependantLog.info( "WDGU: WFO assuming GUILess command: "+trdata.getCommand());
					return 0;
	    		}else{
	    			IndependantLog.warn("WDGU: WFO could NOT retrieve AppMap entry for Window '"+ windowName +"'. Verify it exists.");
	    			winRec = RS.xpath(XPATH.html());
	    		}
	    	}
	    	trdata.setWindowGuiId(winRec);

            IndependantLog.info("WDGU: WFO winRec retrieved: "+ winRec);
            WebElement winObj = null;
            //CACHE HANDLE
//			if(!ignoreCache){
//				winObj = (WebElement)map.getParentObject(windowName);
//				IndependantLog.info("WDGU: winObj got from cache: "+ winObj);
//			}

            // check for possible Autoit-Based Testing recognition string
            if(AutoItRs.isAutoitBasedRecognition(winRec)){
	    		trdata.setWindowGuiId(winRec);
	    		trdata.setCompTestObject(null);
	    		trdata.setWindowTestObject(null);
	    		trdata.setCompType("Component");
	    		IndependantLog.info( "WDGU: WFO returning. Assuming AutoIt Testing for "+trdata.getCommand());
				return StatusCodes.SCRIPT_NOT_EXECUTED;
            }

            // check for possible Image-Based Testing recognition string
            if(ImageUtils.isImageBasedRecognition(winRec)){
	    		trdata.setWindowGuiId(winRec);
	    		trdata.setCompTestObject(null);
	    		trdata.setWindowTestObject(null);
	    		trdata.setCompType("Component");
	    		IndependantLog.info( "WDGU: WFO returning. Assuming Image-Based Testing for "+trdata.getCommand());
	    		return StatusCodes.SCRIPT_NOT_EXECUTED;
            }

			//Try to get the Window TestObject dynamically
            if(winObj==null){
            	winObj = waitWinObject(windowName, winRec, secTimeout);
            	IndependantLog.debug("WDGU: WFO Store the window object into the map cache with key '"+windowName+"' in section ["+windowName+"]");
            	map.setParentObject(windowName, winObj);
            }

	    	//these may not be needed if seeking parent window only
	    	String compRec = null;
	    	WebElement compObj = null;

	    	boolean isParent = windowName.equalsIgnoreCase(compName);

	    	// get values if a child component is the target and not just the window
	    	if(!isParent){
	    		// currently, we do no caching so we don't care if the recognition is dynamic.
		    	compRec = map.getChildGUIID(windowName, compName, false);
		        if (compRec == null) {
		          IndependantLog.debug("WDGU: WFO could NOT retrieve AppMap entry for Component '"+ windowName +":"+ compName +"'. Verify it exists.");
		          throw new SAFSException("WDGU: WFO could NOT retrieve AppMap entry for Component '"+ windowName +":"+ compName +"'. Verify it exists.");
		        }
	            IndependantLog.info("WDGU: WFO compRec retrieved: "+ compRec);
	            //CACHE HANDLE
//		    	ignoreCache = ApplicationMap.isGUIIDDynamic(compRec);
//		    	if(ignoreCache) compRec = ApplicationMap.extractTaggedGUIID(compRec);

	    	} else {
	    		//throw NullPointerExceptions if not found/valid
	    		trdata.setWindowGuiId(winRec);
	    		trdata.setWindowTestObject(winObj);
	    		trdata.setCompTestObject(winObj);
	    		trdata.setCompType(getCompType(winObj));  // winObj can still go Stale in here!
				IndependantLog.info( "WDGU: WFO Matched: "+ winObj.toString());
				return 0;
	    	}

	    	//CACHE HANDLE
//	    	if(!ignoreCache){
//	    		compObj = (WebElement)map.getChildObject(windowName, compName);
//	    		IndependantLog.info("WDGU: compObj got from cache: "+ compObj);
//	    	}

	    	//Try to get the Component TestObject dynamically
	    	if(compObj==null){
	    		compObj = waitCompObject(winObj, windowName, winRec, compName, compRec, secTimeout);
	    		IndependantLog.debug("WDGU: WFO Store the component object into the map cache with key '"+compName+"' in section ["+windowName+"]");
            	map.setParentObject(windowName, winObj); // could have changed (StaleElementException handling)
	    		map.setChildObject(windowName, compName, compObj);
	    	}
	    	//Set the test-record with window, component information
	    	trdata.setCompGuiId(compRec);
	    	trdata.setWindowGuiId(winRec);
	    	trdata.setCompTestObject(compObj);
	    	trdata.setWindowTestObject(winObj);
	    	trdata.setCompType(getCompType(compObj)); // compObj can still go Stale in here!
			IndependantLog.info( "WDGU: WFO Matched: "+ compObj.toString());

			return 0;

	    }catch(SAFSObjectNotFoundException sonfe){
	    	throw sonfe;
	    }catch(Exception x){
	    	String msg = windowName+"."+compName+" using MapName:"+appMapName +";ApplicationMap:";
	    	msg += (map == null) ? "NULL":map.getMapName();
	       	IndependantLog.debug("WDGU:WFO EXCEPTION:"+ x.getMessage() + " using "+ msg +"\n", x);
			throw new SAFSException( msg );
		} finally{
			try{
				resetWDTimeout();
			}catch(Throwable th){
				IndependantLog.error("WDGU: WFO Fail to reset WebDriver timeout. Met "+StringUtils.debugmsg(th));
			}
		}
	}

	/**
	 * Wait a window object within a timeout. If not found, a SAFSObjectNotFoundException will be thrown out.<br>
	 * This method will be called in {@link #waitForObject(String, String, String, long)}.<br>
	 *
	 * @param windowName 	String, the window's name
	 * @param winRec 		String, the window's recognition string
	 * @param secTimeout	long, the time (in seconds) to wait for a window
	 * @return WebElement the window object.
	 * @throws SAFSObjectNotFoundException if the window object could not  be found.
	 */
	private WebElement waitWinObject(String windowName, String winRec, long secTimeout) throws SAFSObjectNotFoundException{
		String debugmsg = StringUtils.debugmsg(false);
		WebElement winObj = null;

		boolean done = false;
		long endtime = System.currentTimeMillis()+(secTimeout * 1000);
		long delay = 1000;

		IndependantLog.debug(debugmsg+" Waitting '"+windowName+"' ... ");
		while(!done){
			if(!setWDTimeout(secTimeout)){
				IndependantLog.warn(debugmsg+"Failed to set the timeout '"+secTimeout+"' to wait for '"+windowName+"'");
			}
			winObj = SearchObject.getObject(winRec);
			resetWDTimeout();

			if(winObj!=null) {
				done = true;
			}else{
				done = System.currentTimeMillis() > (endtime - delay);
				if(!done) try{Thread.sleep(delay);}catch(Exception x){}
			}
		}

		if(winObj==null){
			throw new SAFSObjectNotFoundException("Could not find matching object for Window "+ windowName);
		}

		return winObj;
	}

	/**
	 * Wait a component object within a timeout. If not found, a SAFSObjectNotFoundException will be thrown out.<br>
	 * If the window object becomes stale during searching component object, we will try to get a fresh window object.<br>
	 * This method will be called in {@link #waitForObject(String, String, String, long)}.<br>
	 *
	 * @param winObj		WebElement, the window object.
	 * @param windowName 	String, the window's name
	 * @param winRec 		String, the window's recognition string
	 * @param compName		String, the component's name
	 * @param compRec		String, the component's recognition string
	 * @param secTimeout	long, the time (in seconds) to wait for a component
	 * @return WebElement the component object.
	 * @throws SAFSObjectNotFoundException if the component object could not be found,
	 *                                     or the window object becomes stale and could not be refreshed.
	 */
	private WebElement waitCompObject(WebElement winObj, String windowName, String winRec, String compName, String compRec, long secTimeout) throws SAFSObjectNotFoundException{
		String debugmsg = StringUtils.debugmsg(false);
		WebElement compObj = null;

		boolean done = false;
		long endtime = System.currentTimeMillis()+(secTimeout * 1000);
		long delay = 1000;
		boolean isStale = false;

		IndependantLog.debug(debugmsg+" Waitting '"+windowName+"."+compName+"' ... ");
		while(!done){
			isStale = false;
			if(!setWDTimeout(secTimeout)){
				IndependantLog.warn(debugmsg+"Failed to set the timeout '"+secTimeout+"' to wait for '"+windowName+"."+compName+"'");
			}
			compObj = SearchObject.getObject(winObj,compRec);
			resetWDTimeout();

			if(compObj!=null) {
				try {
					isStale = WDLibrary.isStale(compObj);
					if(isStale) {
						IndependantLog.debug(debugmsg+" the component "+compName+" appears to be Stale. Trying to get a refreshed one.");
						compObj = null;
					}else{
						done = true;
					}
				} catch (SeleniumPlusException e) {
					compObj = null;
					isStale = true;
					IndependantLog.debug(debugmsg+" the component "+compName+" test for Stale Element returned "+ e.getClass().getSimpleName()+", "+ e.getMessage());
				}
			}else{
				done = System.currentTimeMillis() > (endtime - delay);
			}
			if(!done || isStale){
				try {
					if(winObj==null || WDLibrary.isStale(winObj)){
						IndependantLog.debug(debugmsg+" the window "+windowName+" is stale, trying to get a refreshed one.");
						winObj = waitWinObject(windowName, winRec, secTimeout);
					}
				} catch (SeleniumPlusException e) {
					// could not get refreshed window, should we continue to wait component?
					// Actually, the waitWinObject will throw SAFSObjectNotFoundException if not found.
					IndependantLog.debug(debugmsg+" fail to get refreshed window '"+windowName+"' due to "+StringUtils.debugmsg(e));
				}
				try{ Thread.sleep(delay); }catch(Exception x){}
			}
		}

    	if(compObj==null){
			throw new SAFSObjectNotFoundException("Could not find matching object for Component '"+ windowName +":"+ compName +"'");
		}

		return compObj;
	}

	public static void highlightThenClear(WebElement webelement, int duration){
		if(HIGHLIGHT) WDLibrary.highlightThenClear(webelement, duration);
	}

	/**
	 * Wait for the property matching/gone with the expected value.
	 * <p>
	 * @param windowName String, the window name
	 * @param compName String, the component name
	 * @param propertyName String, the property name
	 * @param expectedValue String, the expected value
	 * @param secTimeout long, the time of waiting for.
	 * @param b_caseInsensitive boolean, if it is true, compare value case sensitively. If it is false, compare the value case insensitively.
	 * @param propertyStatus boolean, if it is true, waiting for matching with the expected value. If it is false, waiting for the value gone with expected value.
	 * @return If success return 0. If fail, return -1.
	 * @throws SAFSObjectNotFoundException
	 * @throws SAFSException
	 * @author SCNTAX
	 */
	public int waitForPropertyStatus(String windowName, String compName, String propertyName, String expectedValue, long secTimeout, boolean b_caseInsensitive, boolean propertyStatus)
			throws SAFSObjectNotFoundException, SAFSException {

		WDTestRecordHelper trdata = (WDTestRecordHelper) this.trdata;
		WebElement element;
		String propertyValue = "";
		boolean compareRes = false;
		long endtime = System.currentTimeMillis() + (secTimeout * 1000);
		long delay = 1000;
		boolean done = false;

		try {
			element = WDLibrary.getObject(trdata.getCompGuiId());
			highlightThenClear(element, 1000);
		} catch (Exception e) {
			IndependantLog.debug("WDGU: property: fail to highlight component.", e);
		}

		while (!done) {
			// get property value
			try {
				element = WDLibrary.getObject(trdata.getCompGuiId());
				propertyValue = WDLibrary.getProperty(element, propertyName);
			} catch (Exception badvalue) {
				throw badvalue;
			}

			// compare with expected value
			if(b_caseInsensitive) {
				compareRes = propertyValue.equals(expectedValue);
			} else {
				compareRes = propertyValue.equalsIgnoreCase(expectedValue);
			}

			if(compareRes == propertyStatus) {
				if(true == propertyStatus) {
					IndependantLog.info("WDGU: property:" + propertyName + " match with expected value:" + expectedValue);
				} else {
					IndependantLog.info("WDGU: property:" + propertyName + " has gone with expected value:" + expectedValue);
				}

				return 0;
			}


			done = System.currentTimeMillis() > (endtime - delay);

			if(!done) {
				try {
					Thread.sleep(delay);
				} catch(Exception x) { }
			}

		}

		if(true == propertyStatus) {
			IndependantLog.info("WDGU: property:" + propertyName + " did NOT match with expected value:" + expectedValue);
		} else {
			IndependantLog.info("WDGU: property:" + propertyName + " did NOT gone with expected value:" + expectedValue);
		}

		return -1;
	}

	/**
	 * Try to deduce the Component Type as stored in our cfmap.  If not found, then the
	 * default type of "Component" is returned.
	 * @param compObj
	 * @return the Mapped component type, or "Component" if not mapped.
	 */
	public static String getCompType(WebElement compObj) {
		String debugmsg = StringUtils.debugmsg(false);
		String clazz = null;
		String type = null;
		try{

			if(WDLibrary.isDojoDomain(compObj)){
				//Try to deduce the type by DOJO native class name
				try{
					//TODO also need to get the class-names of this object's superclasses.
//					List<String> classes = WDLibrary.DOJO.getDojoClassNames(compObj);
//					for(int i=0;i<classes.size();i++){
//						clazz = classes.get(i);
//						if(clazz!=null) type = (String)cfmap.get(clazz);
//						if(type!=null) break;
//					}
					clazz = WDLibrary.DOJO.getDojoClassName(compObj);
					if(clazz!=null) type = getClassTypeMapping(clazz);
				}catch(SeleniumPlusException e){}

				//Then, we try 'css class name' to map a library type
				if(type==null){
					clazz = compObj.getAttribute("class");
					if(clazz!=null) type = getClassTypeMapping(clazz);
				}
			}
			else if(WDLibrary.isSAPDomain(compObj)){
				try{
					String[] classes = WDLibrary.SAP.getSAPClassNames(compObj).toArray(new String[0]);
					clazz = Arrays.toString(classes);
					type = getClassTypeMapping(classes);

				}catch(SeleniumPlusException e){}

			}
//			else if(WDLibrary.isOtherDomain(compObj)){
//				//Try to get the componet type for this sepcical domain
//			}

			//Finally, try the html class name (tag or type) to deduce the type
			if(type==null){
				String[] classes = WDLibrary.HTML.html_getClassName(compObj);
				clazz = Arrays.toString(classes);
				type = getClassTypeMapping(classes);
			}

			IndependantLog.debug(debugmsg+"Mapping '"+clazz+"' to '"+type+"'");
		}catch(Exception x){
			IndependantLog.debug(debugmsg+" IGNORING Exception.", x);
		}
		return (type == null) ? "Component": type;
	}

	/**
	 * Normally, component-type is the same as library-type, but sometimes they are different.<br>
	 * componentToLibraryMap is used to contain the mapping, if they are different.<br>
	 */
	static CaseInsensitiveHashtable componentToLibraryMap = new CaseInsensitiveHashtable();
	static void loadComponentToLibraryMap(){
		//Maybe, it will be loaded from a text file
		componentToLibraryMap.put("MenuBar", "Menu");
	}
	public static String getLibraryType(WebElement compObj){
		String libType = null;
		String compType = getCompType(compObj);
		loadComponentToLibraryMap();
		libType = (String) componentToLibraryMap.get(compType);
		libType = (libType==null? compType: libType);

		return libType;
	}
	public static String getLibraryPackage(){
		return "org.safs.selenium.webdriver.lib";
	}

	/**
	 * Gets the object defined by the windowName and childName
	 * @param mapName  the name/ID of the App Map currently in use.
	 * @param windowName  the name/ID of the window as predefined in the App Map.
	 * @param childName    the name/ID of the child component of the window as predefined in the App Map.
	 * @param ignoreCache if true, try getting the component from the appmap
	 * @return the object defined by the windowName and childName
	 */
	public WebElement getTestObject(String mapname,String windowName,String childName,boolean ignoreCache){
		if(mapname==null||windowName==null) return null;
		ApplicationMap map = getAppMap(mapname);

		if (map == null) {
			if (registerAppMap(mapname, mapname)) {
				map = getAppMap(mapname);
				if (map == null) {
					IndependantLog.info("WDDDG: gto1 could NOT retrieve registered AppMap "+ mapname);
					return null;
				}
			}
			// what if NOT registered?
			else{
				IndependantLog.info("WDDDG: gto1 could NOT register AppMap "+ mapname);
				return null;
			}
		}


		WebElement tobj = null;
		if ((childName == null)||(windowName.equalsIgnoreCase(childName))){

			if (!ignoreCache) {
				tobj = (WebElement)map.getParentObject(windowName);
			}
			if (tobj == null){
				//map.setParentObject(windowName, null);
				try {
					waitForObject(mapname,windowName,windowName,gSecTimeout);
				} catch (SAFSException e) {
					IndependantLog.info("WDDDG: Could not waitForObject "+ windowName);
					return null;
				}
				tobj = (WebElement)map.getParentObject(windowName);
				if (tobj == null) {
					IndependantLog.info("WDDDG: Could not waitForObject "+ windowName);
					return null;
				}
			}else{
				return tobj;
			}

		} else {

			if (!ignoreCache) {
				tobj = (WebElement)map.getChildObject(windowName, childName);
			}
			if (tobj == null) {
				//map.setChildObject(windowName, childName, null);
				try {
					waitForObject(mapname,windowName,childName,gSecTimeout);
				} catch (SAFSException e) {
					IndependantLog.info("WDDDG: Could not waitForObject "+ childName);
					return null;
				}
				tobj = (WebElement)map.getChildObject(windowName,childName);
				if (tobj == null) {
					IndependantLog.info("WDDDG: Could not waitForObject "+ childName);
					return null;
				}
				((WDTestRecordHelper)trdata).setCompTestObject(tobj);
			}else{
				IndependantLog.info("WDDDG: returning cached object: "+tobj.getTagName());
				return tobj;
			}
		}

		try{
			IndependantLog.info("WDDDG: compTestObject : "+ tobj.getTagName());
		}catch(Exception npe2){
			IndependantLog.info("WDDDG: No Mapped TestObject named \""+ childName +"\" found.");
		}

		return tobj;
	}

	private static boolean timeout_lock = false;
	private static String timeout_lock_owner = null;

	/**
	 * Set the webdriver element search timeout.
	 * Will only succeed if the setting is not locked, or the lock is owned by the caller.
	 * @param timeout -- new timeout value in seconds.
	 * @return boolean true if successfully set the timeout.
	 * @see #setWDTimeoutLock()
	 * @see #resetWDTimeout()
	 * @see #resetWDTimeoutLock()
	 */
	public boolean setWDTimeout(long timeout){
		StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
		String caller = trace.getClassName()+"."+trace.getMethodName();
		if(! timeout_lock || caller.equals(timeout_lock_owner)) {
			SeleniumPlus.WebDriver().manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
	        IndependantLog.info("WDGU: Timeout set value  to '"+ timeout +"' by: "+ caller);
	        return true;
		}else{
	        IndependantLog.info("WDGU: *** WDTimeout cannot be changed due to lock owned by: "+ timeout_lock_owner +" ***");
	        return false;
		}
	}

	/**
	 * Suspends any changes to the WDTimeout except by the calling Method.
	 * This is primarily used in broad widespread processing like ProcessContainer that need
	 * to prevent other smaller processes from resetting the timeout back to its normal value.
	 * <p>
	 * The call to SET and RESET the lock MUST occur within the same Method
	 * of the calling class.
	 * @return boolean true if successfully set the timeout lock.
	 * @see #setWDTimeout(long)
	 * @see #resetWDTimeout()
	 * @see #resetWDTimeoutLock()
	 */
	public boolean setWDTimeoutLock(){
		StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
		String caller = trace.getClassName()+"."+trace.getMethodName();
		if(! timeout_lock){
	        timeout_lock = true;
			timeout_lock_owner = caller;
	        IndependantLog.info("WDGU: *** Changes to WDTimeout have been temporarily locked by: "+ timeout_lock_owner +" ***");
	        return true;
		}else{
	        IndependantLog.info("WDGU: *** WDTimeout Lock cannot be changed.  It is already owned by: "+ timeout_lock_owner +" ***");
	        return false;
		}
	}

	/**
	 * reset the the webdriver timout to the "default" setting of the the running Processor.
	 * Will only succeed if the timer is not locked, or the lock is owned by the caller.
	 * @return boolean true if successfully reset the timeout.
	 * @see #setWDTimeoutLock()
	 * @see #setWDTimeout(long)
	 * @see #resetWDTimeoutLock()
	 */
	public boolean resetWDTimeout(){
		StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
		String caller = trace.getClassName()+"."+trace.getMethodName();
		if(! timeout_lock || caller.equals(timeout_lock_owner)) {
			// use window timeout for reset
			long timeout = Processor.getSecsWaitForWindow();
			SeleniumPlus.WebDriver().manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
	        IndependantLog.info("WDGU: Reset timeout back  to '"+ timeout +"' by: "+caller);
	        return true;
		}else{
	        IndependantLog.info("WDGU: *** Timeout changes cannot be changed due to lock by: "+ timeout_lock_owner +" ***");
	        return false;
		}
	}

	/**
	 * Resumes allowing changes to the WDTimeout.  This is primarily used in broad widespread processing
	 * like ProcessContainer that needs to prevent other smaller processes from changing the timeout
	 * value.
	 * @return boolean true if successfully reset the timeout lock.
	 * @see #setWDTimeoutLock()
	 */
	public boolean resetWDTimeoutLock(){
		StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
		String caller = trace.getClassName()+"."+trace.getMethodName();
		if(timeout_lock && caller.equals(timeout_lock_owner)){
	        timeout_lock_owner = null;
	        timeout_lock = false;
	        IndependantLog.info("WDGU: *** Changes to WDTimeout have been unlocked by: "+ caller +" ***");
	        return true;
		}else{
	        IndependantLog.info("WDGU: *** Reset or possibly null WDTimeout Lock disallowed since lock is not owned by:"+ caller +" ***");
	        return false;
		}
	}

	@Override
	public int setActiveWindow(String appMapName, String windowName,
			String compName) throws SAFSObjectNotFoundException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object findPropertyMatchedChild(Object obj, String property,
			String bench, boolean exactMatch)
			throws SAFSObjectNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List extractListItems(Object obj, String countProp, String itemProp)
			throws SAFSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getListItem(Object obj, int i, String itemProp)
			throws SAFSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tree extractMenuBarItems(Object obj) throws SAFSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tree extractMenuItems(Object obj) throws SAFSException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Start the Selenium Remote Server
	 */
	public static boolean startRemoteServer(){
		String root = null;
		String server = null;
		File executable = null;

		if(isSeleniumPlus()){
			root = System.getenv("SELENIUM_PLUS");
			server = root + "/extra/RemoteServer.bat";
			executable = new File(server);
		}
		if(isSAFS()){
			root = System.getenv("SAFSDIR");
			server = root + "/samples/Selenium2.0/extra/RemoteServer.bat";
			executable = new File(server);
		}

		if(root == null || !executable.exists()){
			if(root==null){
				IndependantLog.warn("WDGU: startRemoteServer cannot determine the installed product.");
			}else{
				IndependantLog.warn("WDGU: startRemoteServer executable '"+executable.getAbsolutePath()+"' doesn't not exist.");
			}
			root = System.getenv("SELENIUM_PLUS");
			server = root + "/extra/RemoteServer.bat";
			executable = new File(server);
			if(!executable.exists()){
				root = System.getenv("SAFSDIR");
				server = root + "/samples/Selenium2.0/extra/RemoteServer.bat";
				executable = new File(server);
				if(!executable.exists()){
					IndependantLog.error("WDGU: startRemoteServer failed to determine RemoteServer startup script location.");
					return false;
				}
			}
		}

		try{
			NativeWrapper.runAsynchExec(server);
		}catch(Throwable t){
	        IndependantLog.error("WDGU: startRemoteServer failed with "+t.getClass().getSimpleName()+": "+t.getMessage());
	        return false;
		}

		return true;
	}

	/**'-port' followed by the port number of the Selenium Server is going to use*/
	public static final String OPTION_PORT 	= "-port";
	/**'-role' followed by the role name of the server, it can be hub or node*/
	public static final String OPTION_ROLE 	= "-role";
	/**'-hub' followed by the hubRegisterUrl, something like http://hubhost:hubport/grid/register */
	public static final String OPTION_HUB 	= "-hub";
	/**'-drivers' followed by the browser drivers (separated by colon :), something like explorer:chrome:MicrosoftEdge */
	public static final String OPTION_BROWSER_DRIVERS 	= "-drivers";

	/**'hub' role name for the "grid hub"*/
	public static final String ROLE_HUB 	= "hub";
	/**'node' role name for the "grid node"*/
	public static final String ROLE_NODE 	= "node";

	/**
	 * Launch Selenium-Standalone-Sever or Selenium-Grid-Hub + Selenium-Grid-Nodes according to the configuration information.<br>
	 * If the grid-node information is provided, then the Grid-Hub + Node will be launched; otherwise, standalone server will be launched.<br>
	 * <b>Note:</b><br>
	 * Before calling this method:<br>
	 * <b>1.</b> We should set the JVM property {@link SelectBrowser#SYSTEM_PROPERTY_SELENIUM_HOST} and {@link SelectBrowser#SYSTEM_PROPERTY_SELENIUM_PORT}.<br>
	 * <b>2.</b> If we want to launch "grid+nodes", we should also set the JVM property {@link SelectBrowser#SYSTEM_PROPERTY_SELENIUM_NODE}.<br>
	 *
	 * @throws SeleniumPlusException, if the server has not been started successfully.
	 * @see #startRemoteServer(String, String...)
	 */
	public static void launchSeleniumServers() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);

		//Retrieve seleniumhost, seleniumport, seleniumnode
		//We have set them to system properties in EmbeddedSeleniumHookDriver#start(), We just need to get them from system properties.
		String host = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_HOST, SeleniumConfigConstant.DEFAULT_SELENIUM_HOST);
		String port = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_PORT, SeleniumConfigConstant.DEFAULT_SELENIUM_PORT);
		String nodesInfo = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_NODE);//There is no default value for seleniumnode
		IndependantLog.info(debugmsg+" using selenium host name: "+ host);
		IndependantLog.info(debugmsg+" using selenium port: "+ port);
		IndependantLog.info(debugmsg+" using selenium nodes: "+ nodesInfo);
		//Retrieve the console state
		String state = JavaJVMConsole.PARAM_STATE + " " + System.getProperty(SeleniumConfigConstant.PROPERTY_CONSOLE_STATE, JavaJVMConsole.STATE_DEFAULT);
		//Retrieve the browser drivers
		String webdrivers = OPTION_BROWSER_DRIVERS+" "+System.getProperty(SeleniumConstants.PROPERTY_WEB_DRIVERS, "");

		//if seleniumnode has been provided, we are going to launch grid-hub and grid-node, not standalone server.
		boolean isGrid = StringUtils.isValid(nodesInfo);
		//serverRunning can tell if the server (standalone or grid-hub) is running or not.
		boolean serverRunning = false;

		//we are going to launch nodes, they have to know where to register them (by hubRegisterUrl)
		//but if the hub is stared with hostname as "localhost" or "127.0.0.1", the nodes (remote machines) will not able to register them on http://localhost or http://127.0.0.1
		//we need to find the real IP address for localhost and use it to build the hubRegisterUrl
		String hubRegisterUrl = "http://"+host+":"+port+WebDriverGUIUtilities.URL_PATH_GRID_REGISTER;
		if(isGrid && NetUtilities.isLocalHost(host)){
			hubRegisterUrl = "http://"+NetUtilities.getLocalHostIP()+":"+port+WebDriverGUIUtilities.URL_PATH_GRID_REGISTER;
		}
		//TODO We could simply use the IP address to build the hubRegisterUrl, it is easier.
//		hubRegisterUrl = "http://"+NetUtilities.getHostIP(host)+":"+port+WebDriverGUIUtilities.URL_PATH_GRID_REGISTER;

		//Before starting server, we need to verify that the server/node is NOT running. Otherwise, we just return.
		if(isGrid){
			if(WebDriverGUIUtilities.isGridRunning(host, port)){
				IndependantLog.debug(debugmsg+" the selenium grid-hub server is already running on port '"+port+"' at '"+host+"'");
				serverRunning = true;
				if(WebDriverGUIUtilities.isNodesRunning(nodesInfo, true, host, port)){
					IndependantLog.debug(debugmsg+" the selenium grid-hub nodes are already running.");
					return;
				}else{
					IndependantLog.warn(debugmsg+" some selenium grid-hub nodes may not run at this moment, we need to restart them.");
				}
			}
		}else{
			if(WebDriverGUIUtilities.isStandalongServerRunning(host, port)){
				IndependantLog.debug(debugmsg+" the selenium standalone server is already running on port '"+port+"' at '"+host+"'");
				serverRunning = true;
				return;
			}
		}

		//if it is Grid, the server is a grid-hub. Otherwise the server is standalone.
		String serverName = "Selenium "+ (isGrid?"Grid Hub":"Standalone server");
		//role contains parameter to determinate what server to start, "standalone", "hub"
		String role = isGrid? " "+OPTION_ROLE+" "+ROLE_HUB+" ":" ";//"" for standalone, "-role hub" for grid-hub

		if(!serverRunning){
			//Start Server: standalone or grid-hub
			IndependantLog.debug(debugmsg+" try to start the "+serverName+" at "+host+":"+port+" ... ");
			if(!launchSeleniumServers(host, port, role, state, webdrivers)){
				//If server cannot be launched, throw exception.
				throw new SeleniumPlusException(" Fail to start '"+serverName+"' at "+ host+":"+port);
			}

			IndependantLog.debug(debugmsg+" "+serverName+" seems started, try to connect it.");
			//Wait for "standalone server" or "grid hub server" to be ready
			if(!WebDriverGUIUtilities.waitSeleniumServerRunning(isGrid, false, false)){
				IndependantLog.warn(debugmsg+serverName+" can not be connected! If it is caused by SocketTimeoutException, you can enlarge timeout by WebDriverGUIUtilities.setTimeoutForHttpConnection(int/*default is 1000*/).");
			}
		}

		if(isGrid){//Grid: grid-hub has already been started, we are going to start each node one by one.
			//Need also to start the nodes, if some nodes fail to launch we just log a warning instead of throwing exception.
			//prepare parameter to register node, something like "-role node -hub http://hubhost:hubport/grid/register"
			role = " "+OPTION_ROLE+" "+ROLE_NODE+" "+OPTION_HUB+" "+hubRegisterUrl;
			List<GridNode> nodes = WebDriverGUIUtilities.getGridNodes(nodesInfo);
			String nodehost = null;
			String nodeport = null;

			for(GridNode node:nodes){
				nodehost = node.getHostname();
				nodeport = node.getPort();
				if(!WebDriverGUIUtilities.canConnectHubURL(nodehost, nodeport)){
					IndependantLog.debug(debugmsg+" try to register the selenium node '"+node+"' to hub "+hubRegisterUrl);
					if(launchSeleniumServers(nodehost, nodeport, role, state, webdrivers)){
						IndependantLog.debug(debugmsg+" '"+node+"' has been launched, waiting for its ready... ");
						WebDriverGUIUtilities.waitSeleniumNodeRunning(nodehost, nodeport);
					}else{
						IndependantLog.warn(debugmsg+" Fail to register node '"+node+"'");
					}
				}else{
					//"connect to hub" is not enough to tell that this node has registered to hub
					//We need to get the grid/console information to analyze the registered nodes
					IndependantLog.debug(debugmsg+" selenium node '"+node+"' seems running.");
					if(!WebDriverGUIUtilities.verifyNodesRegistered(host, port, node)){
						IndependantLog.error(debugmsg+" selenium node '"+node+"' has not registered to hub "+hubRegisterUrl);
					}
				}
			}
		}

		//We only check if the server (standalone or grid-hub) is running
		//if some nodes are not running, we will not throw exception; test may run on other running nodes.
		if(!WebDriverGUIUtilities.isSeleniumServerRunning(host, port, isGrid, false, nodesInfo, false)){
			throw new SeleniumPlusException(" unable to connect to '"+serverName+"' at "+ host+":"+port+ (isGrid?", Grid Nodes are '"+nodesInfo+"'":""));
		}
	}

	/**
	 * Used to launch standalone-server, grid-hub or grid-node.<br>
	 * @param host String, the host name of the selenium server/node
	 * @param port String, the port number of the selenium server/node
	 * @param params String[], the extra parameters for starting server/node
	 * <ul>
	 * <li>params[0] role String, the role option, it can be<br>
	 *               "" for standalone server<br>
	 *               "-role hub" for grid hub server<br>
	 *               "-role node -hub HubRegisterUrl" for grid node<br>
	 *
	 * <li>params[1] state String, the state option, it can "-state MIN|MAX|NORMAL|MINIMIZE|MAXIMIZE"<br>
	 * <li>params[2] drivers String, the drivers option, it can be "-drivers explorer:chrome:MicrosoftEdge"<br>
	 * </ul>
	 * @throws SeleniumPlusException
	 * @see {@link #startRemoteServer()}
	 * @see #startRemoteServer(String, String...)
	 */
	private static boolean launchSeleniumServers(String host, String port, String... params) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		boolean serverStarted = false;

		IndependantLog.info(debugmsg+" starting server with Options: host="+ host+" port="+port+", params="+Arrays.toString(params));
		//Prepare the optional parameters for starting Selenium Server.
		//1. selenium server port
		String serverPort = "";
		if(!SeleniumConfigConstant.DEFAULT_SELENIUM_PORT.equals(port)){
			serverPort = " "+OPTION_PORT+" "+port;
		}
		IndependantLog.info(debugmsg+" port Option: "+ serverPort);
		//2. JVM options for starting selenium server
		String serverJVMOptions = WebDriverGUIUtilities.getRemoteServerJVMOptions();
		if(serverJVMOptions!=null && !serverJVMOptions.trim().isEmpty()){
			//SELENIUMSERVER_JVM_OPTIONS=remoteServerJVMOptions
			serverJVMOptions = SeleniumConfigConstant.SELENIUMSERVER_JVM_OPTIONS+GuiObjectRecognition.DEFAULT_ASSIGN_SEPARATOR+serverJVMOptions;
		}else{
			serverJVMOptions = "";
		}
		//RemoteDriverLauncher/RemoteDriver could run in another JVM on a remote machine
		IndependantLog.info(debugmsg+" JVM Option: "+ serverJVMOptions+", which will be passed as a normal parameter to RemoteDriverLauncher/RemoteDriver.");
		//3. start the RMI server.
		//   We need RMI server for the 2 situations:
		//  a. "standalone server" on remote machine
		//  b. "grid node" on remote machine with "grid hub" on local/remote machine
		String rmiServer = "-"+DriverConstant.PROPERTY_RMISERVER;

		List<String> paramsList = new ArrayList<String>();
		for(String param:params) paramsList.add(param);
		paramsList.add(serverPort);
		paramsList.add(serverJVMOptions);

		if(NetUtilities.isLocalHost(host)){
			//Start the selenium server on the local machine
			IndependantLog.info(debugmsg+" attempting to (re)start Server locally.");
			String projectdir = null;
			try{ projectdir = _LASTINSTANCE.getSTAFHelper().getVariable(STAFHelper.SAFS_VAR_PROJECTDIRECTORY);}
			catch(SAFSException x){
				IndependantLog.info(debugmsg+" getProjectDir ignoring "+x.getClass().getName()+", "+x.getMessage());
			}

			IndependantLog.debug(debugmsg+" getProjectDir '"+projectdir+"'.");
			//locally testing, we don't need RMI server.
			serverStarted = WebDriverGUIUtilities.startRemoteServer(projectdir, paramsList.toArray(new String[0]));

			//TODO we need to modify the batch script for starting a selenium server (standalone, grid-hub, grid-node)
			if(!serverStarted) serverStarted = WebDriverGUIUtilities.startRemoteServer();

		}else{
			//TODO pass the current JVM option to RemoteDriverLauncher
			IndependantLog.debug(debugmsg+" Current properties: "+System.getProperties());
			//Start the selenium server on the remote machine
			Class remoteDriverLauncher = RemoteDriverLauncher.class;
			String launchServerCommand = "java " + remoteDriverLauncher.getName();

			//pass parameters such as "port number", "JVM options", "rmiServer option", "server role" to start server on remote machine
			//remotely testing, we need RMI server.
			paramsList.add(rmiServer);

			for(String param:paramsList){
				if(!StringUtils.isQuoted(param)) param = StringUtils.quote(param);
				launchServerCommand += " "+param;
			}
			IndependantLog.info(debugmsg+" attempting to (re)start Server remotely at "+ host+":"+port+", with command \n"+ launchServerCommand);

			String outputFile = "c:"+File.separator+"staf_launch_"+remoteDriverLauncher.getSimpleName()+"_standardoutput.txt";
			String commandWithOutput = launchServerCommand + " STDOUT "+outputFile+" STDERRTOSTDOUT ";//Append STAF parameter 'STDOUT' and 'STDERRTOSTDOUT'
			IndependantLog.debug(debugmsg+ remoteDriverLauncher.getName() + " starting, the debug message will be in file '"+outputFile+"' on machine '"+host+"'");
			IndependantLog.debug(debugmsg+ RemoteDriver.class.getName() + " launching, the debug message will be in file '"+RemoteDriver.debugLogFile+"' on machine '"+host+"'");

			try {
				//Try to start by STAFHandle, which requires STAF enabled in configuration file
				//[STAF]
				//NOSTAF=False
				IndependantLog.debug(debugmsg+" Try to launch RemoteServer remotely by STAFHandle.");
				STAFResult rc = _LASTINSTANCE.getSTAFHelper().startProcess(host, commandWithOutput, null);
				IndependantLog.debug(debugmsg+" STAFHandle launch RemoteServer, returned RC: "+rc.rc+" and result: "+rc.result);
				serverStarted = (rc.rc==STAFResult.Ok);
				if(!serverStarted) IndependantLog.warn(debugmsg+" STAFHandle launch RemoteServer Failed with RC="+rc.rc+" !");
			} catch (Exception e) {
				IndependantLog.warn(debugmsg+" STAFHandle launch RemoteServer Failed. "+StringUtils.debugmsg(e));
			}

			if(!serverStarted){
				Process process = null;
				ProcessCapture console = null;
				try {
					//try to launch the server on remote machine by launching directly a "staf machine process start command ..." command
					IndependantLog.debug(debugmsg+" Try to launch RemoteServer remotely by STAF process service.");
					String stafCommand = "staf "+host+" process start command "+commandWithOutput;
					IndependantLog.debug(debugmsg+" Runtime exec STAF command: "+stafCommand);
					process = Runtime.getRuntime().exec(stafCommand);
					console = new ProcessCapture(process, null, true, false);
					try{ console.thread.join();}catch(InterruptedException x){;}
					@SuppressWarnings("unchecked")
					Vector<String> data = console.getData();
					if(data!=null && data.size()>0){
						for(String message:data){
							if(message.startsWith(ProcessCapture.ERR_PREFIX) /*if get message from stadard err*/||
									message.contains("RC:") /*some staf error message will be printed to standard out, not standard err*/){
								throw new SAFSException(" Fail to execute command: "+stafCommand+" \n due to "+message);
							}
							//These are only STAF Response message, not interesting
							//IndependantLog.debug(message);
						}
					}

					serverStarted = true;
				}catch(Exception e){
					IndependantLog.warn(debugmsg+StringUtils.debugmsg(e));
				}finally{
					if(console!=null) console.shutdown();
				}
			}

//			if(!serverStarted){
//				//TODO maybe try to launch the server on remote machine if there a "rshd" running there.
//				try {
//					Runtime.getRuntime().exec("rsh "+seleniumhost+launchServerCommand);
//				} catch (IOException e) {
//					IndependantLog.warn(debugmsg+StringUtils.debugmsg(e));
//				}
//			}
		}

		return serverStarted;
	}

	/**
	 *
	 * @param projectdir
	 * @param extraParams String[], the optional parameters
	 * <pre>
	 * <b>-port N</b>, optional, the port number for Selenium Server. If not provided, the default port will be used.
	 *                 For "standalone" and "hub" the default port number is 4444; While for "node", it is 5555.
	 * <b>-role TheServerRole</b>, optional, if not provided, a standalone server will be launched.
	 *                             TheServerRole could be <b>"hub"</b>, and selenium server will be launched
	 *                             as a hub (in grid mode) for other node to connect.
	 *                             TheServerRole could be <b>"node"</b>, and selenium server will be launched
	 *                             as a node (in grid mode) to connect a hub. <b>**Note**</b> Hub's information must also
	 *                             be provided. Ex: <b>-role node -hub http://hub.machine:port/grid/register</b>
	 * <b>SELENIUMSERVER_JVM_OPTIONS=jvm options</b> Ex: SELENIUMSERVER_JVM_OPTIONS=-Xms256m -Xmx1g
	 * <b>-drivers browser-drivers</b> Ex: -drivers=explorer:chrome:MicrosoftEdge
	 *
	 * </pre>
	 * @return boolean, true if the server has been successfully started
	 * @author CANAGL 2015.11.02 Add support for new SAFS\jre\Java64\jre\bin
	 */
	public static boolean startRemoteServer(String projectdir, String... extraParams){
		String debugmsg = StringUtils.debugmsg(false);
		boolean success = false;
		IndependantLog.debug(debugmsg+" projectdir="+projectdir+" extraParams="+Arrays.toString(extraParams));
		String consoledir = null;
		SePlusInstallInfo seinfo = null;
		File projectroot = null;

		try {
			seinfo = SePlusInstallInfo.instance();
		} catch (SeleniumPlusException e) {
			IndependantLog.error(debugmsg, e);
			return success;
		}

		projectroot = projectdir==null? null:new CaseInsensitiveFile(projectdir).toFile();
		if(projectroot != null ){
			consoledir = projectroot.getAbsolutePath();
		}else{
			consoledir = seinfo.getRootDir().getAbsolutePath();
		}
		IndependantLog.debug(debugmsg+": Selenium Server runtime consoles expected at "+ consoledir);

		List<String> extraParamsList = StringUtils.arrayToList(extraParams);

		boolean isGrid = false;//indicate the type (grid-hub or standalone) of the server to start
		boolean isNode = false;//indicate that we are starting a node
		String nodePort = SeleniumConfigConstant.DEFAULT_SELENIUM_NODE_PORT;
		String jvmOptions = null;
		String webDrivers = null;

		String param = null;
		for(int i=0; i<extraParamsList.size();i++){
			param = extraParamsList.get(i).trim();
			if(param.toUpperCase().startsWith(SeleniumConfigConstant.SELENIUMSERVER_JVM_OPTIONS)){
				//try to get "JVM Options" from extra parameter
				int prefixLength = SeleniumConfigConstant.SELENIUMSERVER_JVM_OPTIONS.length();
				int sepLength = GuiObjectRecognition.DEFAULT_ASSIGN_SEPARATOR.length();
				int index = param.indexOf(GuiObjectRecognition.DEFAULT_ASSIGN_SEPARATOR /* = */, prefixLength);
				if(index>-1 && (index+sepLength)<param.length()) jvmOptions = param.substring(index+sepLength);
				extraParamsList.remove(i);//Remove it, it will not be passed to SeleniumServerRunner
			}else if(param.toLowerCase().startsWith(OPTION_ROLE)){
				//try to get grid-hub, grid-node information
				//-role hub
				//-role node -hub hubRegistrerUrl
				isGrid = true;
				isNode = param.substring(OPTION_ROLE.length()).trim().startsWith(ROLE_NODE);
			}else if(param.toLowerCase().startsWith(OPTION_PORT)){
				//try to get the port information, -port portNumber
				nodePort = param.substring(OPTION_PORT.length()).trim();
			}else if(param.toLowerCase().startsWith(OPTION_BROWSER_DRIVERS)){
				//try to get the browser drivers, -drivers explorer:chrome:MicrosoftEdge
				webDrivers = param.substring(OPTION_BROWSER_DRIVERS.length()).trim();
				extraParamsList.remove(i);//Remove it, it will not be passed to SeleniumServerRunner
			}
		}
		//try to get "JVM Options" from system properties
		if(jvmOptions==null) jvmOptions = getRemoteServerJVMOptions();
		IndependantLog.debug(debugmsg+" with JVM options : "+jvmOptions);
		ProcessCapture console = null;

		try{
			String cp = " -cp "+ seinfo.getClassPath(false);

			String cmdline = seinfo.getJavaexe() +" "+jvmOptions + cp +" "+SeleniumServerRunner.class.getName()+" "+
					         " -Dwebdriver.log.file=\""+consoledir+File.separator+"webdriver.console\"";

			if(webDrivers!=null && !webDrivers.isEmpty()){
				IndependantLog.debug(debugmsg+" Starting selenium server with browser drivers '"+webDrivers+"'.");
				String[] driverNames = webDrivers.split(":");
				File driverFile = null;
				String driverShortName = null;
				for(String driverName:driverNames){
					driverName = driverName.trim();
					driverFile = seinfo.getDriver(driverName);
					driverShortName = SeleniumConstants.DRIVER_SHORT_NAME_MAP.get(driverName);
					if(driverFile.isFile()){
						cmdline += " -Dwebdriver."+driverShortName+".driver=\""+ driverFile.getAbsolutePath() +"\"";
					}else{
						IndependantLog.warn(debugmsg+" can NOT set driver '"+driverName+"' VM parameters! File '"+driverFile.getAbsolutePath()+"' doesn't exist!");
					}
					//Some driver is included inside the selenium-server, such as Firefox.
					//Whether the driver executable exists or not, we will set the log file parameter.
					cmdline += " -Dwebdriver."+driverShortName+".logfile=\""+consoledir+File.separator+driverShortName+".console\"";
				}
			}else{
				//Keep these settings for back-compatibility
				cmdline += " -Dwebdriver.firefox.logfile=\""+consoledir+File.separator+"firefox.console\""+
						" -Dwebdriver.safari.logfile=\""+consoledir+File.separator+"safari.console\""+
						" -Dwebdriver.ie.logfile=\""+consoledir+File.separator+"ie.console\""+
						" -Dwebdriver.opera.logfile=\""+consoledir+File.separator+"opera.console\""+
						" -Dwebdriver.chrome.logfile=\""+consoledir+File.separator+"chrome.console\"";

				if(seinfo.getChromeDriver().isFile()) cmdline += " -Dwebdriver.chrome.driver=\""+ seinfo.getChromeDriver().getAbsolutePath() +"\"";
				if(seinfo.getIEDriver().isFile()) cmdline += " -Dwebdriver.ie.driver=\""+ seinfo.getIEDriver().getAbsolutePath() +"\"";
			}

			//The other parameter will be passed directly to "org.safs.selenium.util.SeleniumServerRunner"
			for(String parameter: extraParamsList) cmdline += " "+parameter;
			cmdline += " -timeout=20 -browserTimeout=60 "+SeleniumServerRunner.PARAM_OUTPUTCONSOLE;

			final String fcmd = cmdline;
			final File workdir = projectroot == null ? seinfo.getRootDir(): projectroot;
			IndependantLog.debug(debugmsg+" launching Selenium Server with cmdline: "+ fcmd);

			Process process = null;
			//TODO we should not launch a "selenium server", if there is already one running.
			process = Runtime.getRuntime().exec(fcmd,null,workdir);
			console = new ProcessCapture(process, SeleniumServerRunner.TITLE , true, false/*will not write out/err message to debug log, it is already in SeleniumServerRunner*/);

			if(isNode){
				success = waitSeleniumNodeRunning(SeleniumConfigConstant.DEFAULT_SELENIUM_HOST, nodePort);
			}else{
				success = waitSeleniumServerRunning(isGrid, false, false);
			}
			try{
				if(!success){
					//Wait longer to get more information from STDOUT/STDERR, if no running server has been detected.
					console.waitOutput(10*1000 /* milliseconds*/, 20 /* lines on STDOUT */, 20 /* lines on STDERR */);
				}
			}catch(TimeoutException te){
				IndependantLog.debug(debugmsg+" met "+StringUtils.debugmsg(te));
			}

			@SuppressWarnings("unchecked")
			Vector<String> data = console.getData();
			if(data!=null && data.size()>0){
				for(String message:data){
					IndependantLog.debug(message);
					if(!success){
						System.out.println(message);//print the "server starting message to console"
						//SeleniumServerRunner (org.openqa.grid.selenium.GridLauncher) wrongly write all messages to standard err :-(
//						if(message.startsWith(ProcessCapture.ERR_PREFIX)){
//							throw new SAFSException(" Fail to execute command: "+fcmd+" \n due to "+message);
//						}
						//parse the output message to know if the server has been failed.
						if(message.contains("Failed to start: SocketListener")){
							throw new SAFSException("Failed to execute command: "+fcmd+" \n due to "+message);
						}
					}
				}
			}

		}catch(Exception x){
			IndependantLog.debug(debugmsg+" failed to launch Selenium Server due to "+x.getClass().getName()+": "+x.getMessage(), x);
		}

		return success;
	}

	/**
	 * Wait for selenium "grid node" to be ready.<br>
	 * This method will only check if the URL "http://host:port/wd/hub" can be connected. It will<br>
	 * not check if "grid node" has been registered to "grid hub".<br>
	 *
	 * @param host String, the hostname of the "grid node".
	 * @param port String, the port number where the "grid node" is running on.
	 * @param params int[], optional parameters<br>
	 * <ul>
	 * <li>params[0] repeatTimes int, how many times to try to connect with Server. Default is 5 times.
	 * <li>params[1] pause int, the pause time (milliseconds) between each connection try. Default is 500 milliseconds.
	 * <ul>
	 *
	 * @return boolean, true if the server is running.
	 */
	public static boolean waitSeleniumNodeRunning(String host, String port, int... params){
		int tries = 0;
		int repeat = 5;
		int pause = 500;

		if(params!=null){
			if(params.length>0) repeat=params[0];
			if(params.length>1) pause=params[1];
		}

		while(!canConnectHubURL(host, port) && tries++<repeat){
			try{ Thread.sleep(pause);}catch(Exception e){}
		}

		return canConnectHubURL(host, port);
	}

	/**
	 * Wait for selenium "standalone"/"grid hub, grid nodes" to be ready.<br>
	 * <b>Note:</b> Before calling this method:<br>
	 * We should set the JVM property {@link SelectBrowser#SYSTEM_PROPERTY_SELENIUM_HOST} and {@link SelectBrowser#SYSTEM_PROPERTY_PROXY_PORT}.<br>
	 * If we are waiting for "grid hub, grid nodes", we should also set the JVM property {@link SelectBrowser#SYSTEM_PROPERTY_SELENIUM_NODE}.<br>
	 *
	 * @param isGrid boolean, if false the server is "standalone"; if true the server is "grid hub" and "grid nodes".
	 * @param waitForNodes boolean, if true then should wait for "grid nodes" to be ready. This parameter take effect only if isGrid is true.
	 * @param verifyRegistered boolean, if true then verify "grid nodes" have been registered to "grid hub". This parameter take effect only if isGrid and waitForNodes are true.
	 * @param params int[], optional parameters<br>
	 * <ul>
	 * <li>params[0] repeatTimes int, how many times to try to connect with Server. Default is 5 times.
	 * <li>params[1] pause int, the pause time (milliseconds) between each connection try. Default is 500 milliseconds.
	 * <ul>
	 *
	 * @return boolean, true if the server is running.
	 */
	public static boolean waitSeleniumServerRunning(boolean isGrid, boolean waitForNodes, boolean verifyRegistered, int... params){
		int tries = 0;
		int repeat = 5;
		int pause = 500;
		boolean running = false;

		if(params!=null){
			if(params.length>0) repeat=params[0];
			if(params.length>1) pause=params[1];
		}

		//prepare the host, port, nodesInfo parameter
		String host = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_HOST);
		if (host == null || host.isEmpty()) host = SelectBrowser.DEFAULT_SELENIUM_HOST_IP;//127.0.0.1
		String port = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_PORT);
		port = normalizePort(port, SelectBrowser.DEFAULT_SELENIUM_PORT/*4444 for server (standalone or grid hub)*/);
		String nodesInfo = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_NODE);

		if(isGrid){
			while(!isGridRunning(host, port) && tries++ < repeat){
				try{ Thread.sleep(pause);}catch(Exception x){}
			}
			running = isGridRunning(host, port);

			if(running && waitForNodes){
				running = false;//set running to false for waiting nodes
				while(!running && tries++ < repeat){
					running = isNodesRunning(nodesInfo, verifyRegistered, host, port);
					try{ Thread.sleep(pause);}catch(Exception x){}
				}
			}
		}else{
			while(!running && tries++ < repeat){
				running = isStandalongServerRunning(host, port);
				try{ Thread.sleep(pause);}catch(Exception x){}
			}
		}

		return running;
	}

	/**
	 * Test if selenium "standalone"/"grid hub, grid nodes" is ready.<br>
	 *
	 * @param host String, the hostname of server (standalone or grid-hub).
	 * @param port String, the port number where the server is running.
	 * @param isGrid boolean, if false the server is "standalone"; if true the server is "grid hub".
	 * @param checkNodes boolean, if true then check if "grid nodes" are running. This parameter takes effect only if isGrid is true.
	 * @param nodesInfo String, the information of the "grid nodes" to check. This parameter takes effect only if isGrid and checkNodes are true.
	 * @param verifyRegistered boolean, if true then check if "grid nodes" have been registered to "grid hub". This parameter takes effect only if isGrid and checkNodes are true.
	 *
	 * @return boolean, true if the selenium "standalone"/"grid hub, grid nodes" is running.
	 */
	public static boolean isSeleniumServerRunning(String host, String port, boolean isGrid,
			                                      boolean checkNodes, String nodesInfo, boolean verifyRegistered){
		boolean running = false;

		if(isGrid){
			running = isGridRunning(host, port);
			if(running && checkNodes) running = isNodesRunning(nodesInfo, verifyRegistered, host, port);
		}else{
			running = isStandalongServerRunning(host, port);
		}

		return running;
	}

	/**
	 * Checks to see if a "Selenium Standalone Server" is running.<br>
	 *
	 * @param host String, the hostname of the "standalone server".
	 * @param port String, the port number where the server is running on.
	 * @return true if the server responds, false otherwise.
	 */
	public static boolean isStandalongServerRunning(String host, String port/*4444 for standalone server*/){
		return canConnectHubURL(host, port);
	}

	/**
	 * Checks to see if a "Selenium Grid Hub server" is running.<br>
	 *
	 * @param host String, the hostname of the "grid hub server".
	 * @param port String, the port number where the server is running on.
	 * @return true if the server responds, false otherwise.
	 * @see #isNodesRunning()
	 */
	public static boolean isGridRunning(String host, String port/*4444 for grid hub*/){
		return canConnectGridURL(host, port);
	}

	/**
	 * Checks to see if all "Selenium Grid Nodes" are running and registered to a grid-hub:<br>
	 *
	 * @param nodesInfo String, the "grid nodes" to check. It is like "node1:port:nodeconfig;node2:port:nodeconfig;".
	 * @param verifyRegistered boolean, if we need to verify that all nodes are registered to "grid hub"
	 * @param hubhost String, the name of the "gird hub". It is valid only when verifyRegistered is true.
	 * @param hubport String, the port number where the "grid hub" is running on. It is valid only when verifyRegistered is true.
	 * @return boolean, true if all nodes are running and have been registered to grid hub.
	 * @see #isGridRunning()
	 */
	public static boolean isNodesRunning(String nodesInfo /*node1:port:nodeconfig;node2:port:nodeconfig;*/,
			                             boolean verifyRegistered, String hubhost, String hubport){
		String debugmsg = StringUtils.debugmsg(false);

		List<GridNode> nodes = getGridNodes(nodesInfo);

		for(GridNode node: nodes){
			if(!canConnectHubURL(node.getHostname(), node.getPort())){
				IndependantLog.warn(debugmsg+" node '"+node+"' is not running.");
				return false;
			}
		}

		//"connect to hub" is not enough to tell that this node has registered to hub
		//We need to get the grid/console information to analyze the registered nodes
		IndependantLog.debug(debugmsg+" all selenium nodes '"+nodes+"' seems running.");
		if(verifyRegistered){
			if(!verifyNodesRegistered(hubhost, hubport, nodes.toArray(new GridNode[0]))){
				IndependantLog.error(debugmsg+" not all selenium nodes '"+nodes+"' have been registered to grid hub.");
				return false;
			}
		}

		return true;
	}

	/**
	 * Convert a node information string to a list of GridNode.
	 * @param nodesInfo String, the node information of format "node1:port:nodeconfig;node2:port:nodeconfig"
	 * @return List<GridNode>
	 */
	public static List<GridNode> getGridNodes(String nodesInfo){
		List<GridNode> nodesList = new ArrayList<GridNode>();

		//nodesInfo is like "node1:port:nodeconfig;node2:port:nodeconfig"
		if(nodesInfo!=null){
			List<String> nodes = StringUtils.getTrimmedTokenList(nodesInfo, StringUtils.SEMI_COLON);
			//nodes is a list of "node1:port:nodeconfig"
			for(String node:nodes) nodesList.add(new GridNode(node));
		}

		return nodesList;
	}

	/**
	 * The class GridNode contains the information about a selenium node, including<br>
	 * <ul>
	 * <li>hostname
	 * <li>hostip
	 * <li>port
	 * <li>configuration
	 * </ul>
	 */
	public static class GridNode{
		private String hostname = SelectBrowser.DEFAULT_SELENIUM_HOST;
		private String hostip = SelectBrowser.DEFAULT_SELENIUM_HOST_IP;
		private String port = SeleniumConfigConstant.DEFAULT_SELENIUM_NODE_PORT;//Grid-node's default port is 5555
		private Object config = null;

		public GridNode(String nodeInfo /* nodeInfo is something like "node1:port:nodeconfig"*/) {
			List<String> info = StringUtils.getTrimmedTokenList(nodeInfo, StringUtils.COLON);
			int i = 0;
			for(String value:info) if(value!=null) info.set(i++, value.trim());
			int size = info.size();
			if(size>0) setHostname(info.get(0));
			if(size>1) port = normalizePort(info.get(1), SeleniumConfigConstant.DEFAULT_SELENIUM_NODE_PORT);
			if(size>2) config = info.get(2);
		}
		public GridNode(String hostname, String port) {
			this.port = port;
			setHostname(hostname);
		}
		public GridNode(String hostname, String port, Object config) {
			setHostname(hostname);
			this.port = port;
			this.config = config;
		}
		public String getHostip() {
			return hostip;
		}
		public String getHostname() {
			return hostname;
		}
		public String getPort() {
			return port;
		}
		public Object getConfig() {
			return config;
		}
		public void setHostname(String hostname) {
			this.hostname = hostname;
			this.hostip = NetUtilities.getHostIP(hostname);
		}
		public void setPort(String port) {
			this.port = port;
		}
		public void setConfig(Object config) {
			this.config = config;
		}

		public boolean isSameHostPort(GridNode node){
			if(node==null) return false;
			if(!hostip.trim().equals(node.getHostip().trim())) return false;
			if(!port.trim().equals(node.getPort().trim())) return false;
			return true;
		}

		public String toString(){
			return hostname+StringUtils.COLON+port;
		}
	}

	/** "/grid/register" the path for registering grid-node */
	public static final String URL_PATH_GRID_REGISTER 	= "/grid/register";
	/** "/grid/console" the path to visit HTTP grid-hub server */
	public static final String URL_PATH_GRID_CONSOLE 	= "/grid/console";
	/** "/wd/hub" the path to visit HTTP standalone-server or grid-node */
	public static final String URL_PATH_HUB 			= "/wd/hub";

	/**
	 * "/wd/hub/static" the path to get information about the running standalone-server or grid-node
	 * This is a NON-valid url, and the information is supposed to get from the error-stream.
	 */
	public static final String URL_PATH_HUB_STATIC		= "/wd/hub/static";
	/**
	 * @param host String, the name of host where "grid hub" server runs
	 * @param port String, the port number on which "grid hub" server runs
	 * @return boolean, true if the "grid hub" is available
	 */
	public static boolean canConnectGridURL(String host, String port){
		return canConnectHttpURL(host, port, URL_PATH_GRID_CONSOLE);
	}
	/**
	 * This method will read from the connection to url {@link #URL_PATH_GRID_CONSOLE}.<br>
	 * @param host String, the host name
	 * @param port String, the port number
	 * @return String, the content read from the connection.
	 */
	public static String readGridURL(String host, String port){
		return readHttpURL(host, port, URL_PATH_GRID_CONSOLE);
	}
	/**
	 * @param host String, the name of host where "standalone server"/"grid node" runs
	 * @param port String, the port number on which "standalone server"/"grid node" runs
	 * @return boolean, true if the "standalone server"/"grid node" is available
	 */
	public static boolean canConnectHubURL(String host, String port){
		return canConnectHttpURL(host, port, URL_PATH_HUB);
	}
	/**
	 * This method is supposed to read from the connection to a NON-valid url {@link #URL_PATH_HUB_STATIC}. It will read from error stream.<br>
	 * @param host String, the host name
	 * @param port String, the port number
	 * @return String, the information about the running standalone server or grid-node.
	 */
	public static String readHubStaticURL(String host, String port){
		return readHttpURL(host, port, URL_PATH_HUB_STATIC);
	}

	/**
	 * Parse the port to make sure that it is an integer and bigger than 1000.
	 * If there is something wrong, use the default port number provided by parameter.
	 * @param port String, the port number to parse
	 * @param defaultPort String, the default port number. 4444 for "standalone server"/"grid hub"; 5555 for "grid node".
	 * @return String, the port number
	 */
	private static String normalizePort(String port, String defaultPort){
		String resutlPort = port;
		try{
			if(Integer.parseInt(port) < 1000 ) resutlPort = defaultPort;
		}catch(Exception ignore){
			IndependantLog.debug(" ignoring invalid port setting '"+port+"'. Using default: "+ defaultPort);
			resutlPort = defaultPort;
		}
		return resutlPort;
	}

	/**
	 * Try to test if an HTTP URL can be connected or not.
	 * @param host String, the host name
	 * @param port String, the port number where "HTTP server" runs
	 * @param path String, the path on "HTTP server" to access, like "/wd/hub", "/grid/console" etc.
	 * @return boolean true if the URL can be connected; false otherwise
	 */
	private static boolean canConnectHttpURL(String host, String port, String path){
		String debugmsg = StringUtils.debugmsg(false);

		String url = "http://"+host+":"+ port +path;
		try {
			return canConnectHttpURL(new URL(url));
		} catch (MalformedURLException e) {
			IndependantLog.error(debugmsg+" URL '"+url+"' is not correct."+StringUtils.debugmsg(e));
			return false;
		}
	}
	/**
	 * @param host String, the host name
	 * @param port String, the port number where "HTTP server" runs
	 * @param path String, the path on "HTTP server" to access, like "/wd/hub/static", "/grid/console" etc.
	 * @return String, the content read from connection "http://host:port+path"
	 */
	private static String readHttpURL(String host, String port, String path){
		String debugmsg = StringUtils.debugmsg(false);

		String url = "http://"+host+":"+ port +path;
		try {
			return NetUtilities.readHttpURL(new URL(url), "UTF-8", timeoutForHttpConnection);
		} catch (MalformedURLException e) {
			IndependantLog.error(debugmsg+" URL '"+url+"' is not correct."+StringUtils.debugmsg(e));
			return null;
		}
	}

	/**
	 * timeout for HTTP connection, the default value is 1000 milliseconds.<br>
	 * For example:<br>
	 * connect to selenium-standalone-server {@link #URL_PATH_HUB} <br>
	 * connect to selenium-grid-hub {@link #URL_PATH_GRID_CONSOLE} <br>
	 * If we always fail to connect even the site is running, we may consider to enlarge this timeout by {@link #setTimeoutForHttpConnection(int)}.<br>
	 */
	private static int timeoutForHttpConnection = 1000;//milliseconds

	/** get {@link #timeoutForHttpConnection}*/
	public static int getTimeoutForHttpConnection() {
		return timeoutForHttpConnection;
	}
	/** set {@link #timeoutForHttpConnection}*/
	public static void setTimeoutForHttpConnection(int timeoutForHttpConnection) {
		WebDriverGUIUtilities.timeoutForHttpConnection = timeoutForHttpConnection;
	}

	/**
	 * Try to test if an HTTP URL can be connected or not.
	 * @param serverURL URL, the URL to test.
	 * @return boolean true if the URL can be connected; false otherwise
	 */
	private static boolean canConnectHttpURL(URL serverURL){
		String debugmsg = StringUtils.debugmsg(false);
		boolean debug = false;

		HttpURLConnection con = null;
		try{
			con = (HttpURLConnection) serverURL.openConnection();
			con.setConnectTimeout(timeoutForHttpConnection);
			if(debug){
				IndependantLog.debug(debugmsg+"request properties: "+con.getRequestProperties());
				IndependantLog.debug(debugmsg+"respond code: "+con.getResponseCode());
				IndependantLog.debug(debugmsg+"respond message: "+con.getResponseMessage());
				IndependantLog.debug(debugmsg+"content length: "+con.getContentLengthLong());
			}

			if(con.getResponseCode()==HttpURLConnection.HTTP_OK && con.getContentLengthLong()>50){
				return true;
			}else{
				IndependantLog.warn(debugmsg+"Fail to connect to URL '"+serverURL+"'");
			}

		}catch(Exception e){
			IndependantLog.error(debugmsg+StringUtils.debugmsg(e));
		}finally{
			try{if(con!=null) con.disconnect();}catch(Exception x){}
		}
		return false;
	}

	//The following GRID_CONSOLE_RESPONSE_XXX constants are used to parse the response from grid-console.
	//<p>host:10.121.19.24</p>
	private static final String GRID_CONSOLE_RESPONSE_HOST = "host:";
	//<p>port:5678</p>
	private static final String GRID_CONSOLE_RESPONSE_PORT = "port:";
	//<p>remoteHost:http://10.121.19.24:5678</p>
	private static final String GRID_CONSOLE_RESPONSE_REMOTEHOST = "remoteHost:";
	//The leading char of a tag
	private static final String GRID_CONSOLE_RESPONSE_TAG_BEGIN = "<";
	//The leading string for a registered node
	private static final String GRID_CONSOLE_RESPONSE_CONFIG = "<div type='config' class='content_detail'>";

	/**
	 * Verify if an array of GridNode have been registered to grid hub.<br>
	 * @param hubhost String, the name of "grid hub".
	 * @param hubport String, the port number where "grid hub" is running on.
	 * @param nodes GridNode[], an array of GridNode to verify
	 * @return boolean, true if all nodes have been registered.
	 */
	public static boolean verifyNodesRegistered(String hubhost, String hubport, GridNode... nodes){
		String debugmsg = StringUtils.debugmsg(false);
		boolean debug = false;

		HttpURLConnection con = null;
		InputStream ins = null;
		BufferedReader br = null;

		try{
			String gridConsoleURL = "http://"+hubhost+":"+ hubport +URL_PATH_GRID_CONSOLE;
			con = (HttpURLConnection) (new URL(gridConsoleURL)).openConnection();
			con.setConnectTimeout(1000);
			if(debug){
				IndependantLog.debug(debugmsg+"request properties: "+con.getRequestProperties());
				IndependantLog.debug(debugmsg+"respond code: "+con.getResponseCode());
				IndependantLog.debug(debugmsg+"respond message: "+con.getResponseMessage());
				IndependantLog.debug(debugmsg+"content length: "+con.getContentLengthLong());
			}

			if(con.getResponseCode()==HttpURLConnection.HTTP_OK && con.getContentLengthLong()>50){
				ins = con.getInputStream();
				//verify the content of "http://hub:port/grid/console" to see if the node has registered
				br = new BufferedReader(new InputStreamReader(ins, Charset.forName("UTF-8")), 1024*10);
				String nodeInfo = null;
				GridNode tempNode = null;
				List<GridNode> registeredNodes = new ArrayList<GridNode>();
				while((nodeInfo=br.readLine())!=null){
					System.out.println(nodeInfo);
					if(nodeInfo.contains(GRID_CONSOLE_RESPONSE_CONFIG)){
						tempNode = parseNodeInfo(nodeInfo);
						if(tempNode!=null) registeredNodes.add(tempNode);
					}
				}
				if(registeredNodes.size()<nodes.length) return false;
				//check if all nodes have registered.
				boolean matched = false;
				for(GridNode node: nodes){
					matched = false;
					for(GridNode registeredNode: registeredNodes){
						matched = node.isSameHostPort(registeredNode);
						if(matched) break;
					}
					if(!matched) break;
				}
				if(matched) return true;
			}else{
				IndependantLog.warn(debugmsg+"Fail to connect to grid hub server at URL '"+gridConsoleURL+"'");
			}

		}catch(Exception e){
			IndependantLog.error(debugmsg+StringUtils.debugmsg(e));
		}finally{
			try{if(ins!=null) ins.close();}catch(Exception x){}
			try{if(br!=null) br.close();}catch(Exception x){}
			try{if(con!=null) con.disconnect();}catch(Exception x){}
		}
		return false;
	}

	/**
	 * Parse the response got from URL "http://hub:port/grid/console", and get the GridNode.
	 *
	 * @param nodeInfo String, the string containing information of a registered node.
	 * @return GridNode, the registered node.
	 */
	private static GridNode parseNodeInfo(String nodeInfo){
		String debugmsg = StringUtils.debugmsg(false);
		GridNode node = null;
		int beginIndex = -1;
		int endIndex = -1;
		String host = null;
		String port = null;

		IndependantLog.debug(debugmsg+" parse nodeInfo: "+nodeInfo);
		if(StringUtils.isValid(nodeInfo)){
			if(nodeInfo.contains(GRID_CONSOLE_RESPONSE_HOST)){
				beginIndex = nodeInfo.indexOf(GRID_CONSOLE_RESPONSE_HOST);
				endIndex = nodeInfo.indexOf(GRID_CONSOLE_RESPONSE_TAG_BEGIN);
				if(beginIndex>-1 && endIndex>-1 && beginIndex<endIndex){
					host = nodeInfo.substring(beginIndex+GRID_CONSOLE_RESPONSE_HOST.length(), endIndex);
				}
			}
			if(nodeInfo.contains(GRID_CONSOLE_RESPONSE_PORT)){
				beginIndex = nodeInfo.indexOf(GRID_CONSOLE_RESPONSE_PORT);
				endIndex = nodeInfo.indexOf(GRID_CONSOLE_RESPONSE_TAG_BEGIN);
				if(beginIndex>-1 && endIndex>-1 && beginIndex<endIndex){
					port = nodeInfo.substring(beginIndex+GRID_CONSOLE_RESPONSE_PORT.length(), endIndex);
				}
			}
			if(host==null || port==null){
				if(nodeInfo.contains(GRID_CONSOLE_RESPONSE_REMOTEHOST)){
					beginIndex = nodeInfo.indexOf(GRID_CONSOLE_RESPONSE_REMOTEHOST);
					endIndex = nodeInfo.indexOf(GRID_CONSOLE_RESPONSE_TAG_BEGIN);
					if(beginIndex>-1 && endIndex>-1 && beginIndex<endIndex){
						//http://10.121.19.24:5678
						try {
							URL url = new URL(nodeInfo.substring(beginIndex+GRID_CONSOLE_RESPONSE_REMOTEHOST.length(), endIndex));
							if(host==null) host = url.getHost();
							if(port==null) port = String.valueOf(url.getPort());
						} catch (MalformedURLException e) {IndependantLog.debug(debugmsg+StringUtils.debugmsg(e));}
					}
				}
			}
			IndependantLog.debug(debugmsg+" Create GridNode with host:"+host+" port:"+port);
			node = new GridNode(host, port);
		}

		return node;
	}

	/**
	 * Retrieve the JVM options from system properties.<br>
	 * Before calling this method, we may need to set some java system property.<br>
	 * <ul>
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_OPTIONS}
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xms}
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xmx}
	 * </ul>
	 * If none of them exist in system properties, the minimum JVM option "-Xms512m -Xmx1g" will be provided.<br>
	 * If {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_OPTIONS} exist, but {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xms}
	 * and {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xmx} do NOT exist, then we will add "-Xms512m -Xmx1g" to the JVM options if its
	 * value does not contain "-Xms" or "-Xmx".<br>
	 * If {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_OPTIONS} exist, and {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xms}
	 * and/or {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xmx} exist, then we will add "-Xms512m -Xmx1g" to the JVM options if its
	 * value does not contain "-Xms" or "-Xmx", otherwise if its value contains ""-Xms" or "-Xmx", we will replace them by the value of
	 * {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xms} and/or {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xmx}.<br>
	 *
	 * @return String, the JVM options for starting SELENIUM Remote Server.
	 * @see #startRemoteServer(String)
	 */
	public static String getRemoteServerJVMOptions(){
		String debugmsg = StringUtils.debugmsg(false);
		//Get the JVM Options from system properties
		//We have set them to system properties in EmbeddedSeleniumHookDriver#start()
		String Xms = System.getProperty(SeleniumConfigConstant.SELENIUMSERVER_JVM_Xms);
		String Xmx = System.getProperty(SeleniumConfigConstant.SELENIUMSERVER_JVM_Xmx);
		IndependantLog.debug(debugmsg+" get JVM parameter : Xms="+ Xms+" Xmx="+Xmx);
		String jvmOptions = System.getProperty(SeleniumConfigConstant.SELENIUMSERVER_JVM_OPTIONS);
		IndependantLog.debug(debugmsg+" get JVM Options : "+jvmOptions);
		if(jvmOptions==null){
			if(Xms==null) Xms = SeleniumConfigConstant.DEFAULT_JVM_MEMORY_MINIMUM;
			if(Xmx==null) Xmx = SeleniumConfigConstant.DEFAULT_JVM_MEMORY_MAXIMUM;
			jvmOptions = " "+JavaConstant.JVM_Xms+Xms+" "+JavaConstant.JVM_Xmx+Xmx+" ";
		}else{
			//replace -Xmx and -Xms in JVM options, if user has provided them
			if(Xms!=null){//user has provided Xms
				jvmOptions = StringUtils.replaceJVMOptionValue(jvmOptions, JavaConstant.JVM_Xms, Xms);
			}else{
				Xms = SeleniumConfigConstant.DEFAULT_JVM_MEMORY_MINIMUM;
			}
			if(Xmx!=null){//user has provided Xmx
				jvmOptions = StringUtils.replaceJVMOptionValue(jvmOptions, JavaConstant.JVM_Xmx, Xmx);
			}else{
				Xmx = SeleniumConfigConstant.DEFAULT_JVM_MEMORY_MAXIMUM;
			}

			//if jvmOptions does not contain -Xmx or -Xms, we will add them to jvmOptions
			if(!jvmOptions.contains(JavaConstant.JVM_Xms)) jvmOptions += " "+JavaConstant.JVM_Xms+Xms;
			if(!jvmOptions.contains(JavaConstant.JVM_Xmx)) jvmOptions += " "+JavaConstant.JVM_Xmx+Xmx;
		}
		IndependantLog.debug(debugmsg+" return JVM options : "+jvmOptions);

		return jvmOptions;
	}
}
