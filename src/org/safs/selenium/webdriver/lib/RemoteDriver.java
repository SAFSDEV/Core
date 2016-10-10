package org.safs.selenium.webdriver.lib;

/**
* History:<br>
* 
*  <br>   DEC 19, 2013    (DHARMESH4) Initial release.
*  <br>	  JAN 16, 2014    (DHARMESH4) Updated reconnection browser support. 
*  <br>	  APR 09, 2014 	  (DHARMESH4) Fixed javascript support for webdriver reconnection.
*  <br>	  SEP 04, 2014    (LeiWang) Store Capabilities's firefoxProfile info to session file.
*  <br>   OCT 30, 2014    (CANAGL) Initial fix for IE JSON problems after new page load/redirection.
*  <br>	  MAR 17, 2015    (LeiWang) Store Capabilities's chrome custom data info to session file.
*  <br>   JUN 29, 2015	  (LeiWang) Get the RMI Server from the selenium-grid-node machine.
*                                   Add main(): as a entry point for starting Remote Server (standalone or grid).
*  <br>   DEC 24, 2015	  (LeiWang) Add methods to get browser's name, version, and selenium-server's version etc.
*  <br>   FEB 29, 2016	  (LeiWang) Remove the import of org.seleniumhq.jetty7.util.ajax.JSON
*  <br>   MAR 07, 2016	  (LeiWang) Handle firefox preference.
*  <br>   SEP 27, 2016	  (LeiWang) Modified main(): Added parameter "-project"/"-Dselenium.project.location", and 
*                                   adjusted the java doc.
*                                   Wrote debug message to a file on disk c.
*/
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.internal.WebElementToJsonConverter;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.net.NetUtilities;
import org.safs.selenium.rmi.agent.SeleniumAgent;
import org.safs.selenium.util.GridInfoExtractor;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.sockets.DebugListener;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverConstant.SeleniumConfigConstant;
import org.safs.tools.stringutils.StringUtilities;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Handle the Session information (serverhost, browsername, sessionid, firefoxProfile, chrome-user-data-dir, chrome-profile-dir, chrome-preference, chromeExcludedOptions, firefoxPreference).<br>
 * Handle a SeleniumRMIAgent, if enabled, to communicate with a remote SAFS Selenium RMI Server.<br>
 * 
 */
public class RemoteDriver extends RemoteWebDriver implements TakesScreenshot {
	
	private static boolean newSession = true;
	private boolean _quit = false;
	
	/** The URL provided at construction time. */
	public URL remote_URL; 
	/** 
	 * parsed hostname of the remote selenium server (standalone or grid-hub) 
	 * might be needed for additional Selenium RMI Server communication. */
	public String remote_hostname = null;
	/** 
	 * parsed port number of the remote selenium server (standalone or grid-hub) 
	 * might be needed for get grid-node information. */
	public int remote_port = SeleniumConfigConstant.DEFAULT_SELENIUM_PORT_INT;

	/** 
	 * hostname of the remote grid-node. 
	 * might be needed for additional Selenium RMI Server communication. */
	public String rmi_hostname = null;
	
	public int rmi_registry_port = 1099;//default registry port to look for RMI
	
	/**
	 * A SeleniumRMIAgent, if enabled, to communicate with a remote SAFS Selenium RMI Server.<br>
	 * This may be null if we could not initialize an agent or find a server.<br>
	 * This may also be non-null, but not connected to an RMI server if the server is not running.
	 * <p>
	 * @see org.safs.selenium.rmi.agent.SeleniumAgent
	 * @see org.safs.selenium.rmi.agent.SeleniumAgent#server
	 * @see org.safs.selenium.rmi.agent.SeleniumRMIAgent
	 * @see org.safs.selenium.rmi.server.SeleniumRMIServer
	 */
	public SeleniumAgent rmiAgent = null;
	
	/**
	 * It is a string containing session information for reconnecting a session,<br> 
	 * it contains a few fields separated by {@link #SPLITTER}<br>
	 * currently, it contains 
	 * serverHostname +SPLITTER+ browserName +SPLITTER+ sessionid + SPLITTER+ firefoxProfile
	 * + SPLITTER+ chromeUserDataDir + SPLITTER+ chromeProfileDir + SPLITTER+ chromePreference
	 * + SPLITTER+ chromeExcludedOptions + SPLITTER+ firefoxPreference
	 * <br>
	 */
	private static String sessionContent = null;
	private final static String SPLITTER = "_SP_";
	/** "LASTSESSION" */
	public static final String LAST_SESSION_KEY = "LASTSESSION";
	/** "java.io.tmpdir" */
	public static final String JAVA_TMPDIR = "java.io.tmpdir";
	/** "selenium.session.tmp" */
	public static final String SESSION_FILE = "selenium.session.tmp";	
	/** "ID" */
	public static final String CAPABILITY_ID = "ID";
	/** "RECONNECT" */
	public static final String CAPABILITY_RECONNECT = "RECONNECT";
	/** "REMOTESERVER" */
	public static final String CAPABILITY_REMOTESERVER = "REMOTESERVER";

	/**if this is a grid-hub server*/
	protected boolean isGrid = false;
	
	/**
	 * Not used by SAFS Selenium.<br>
	 * Should not be used if retaining SAFS Selenium RMI Server capabilities.
	 * @param capabilities
	 * @see #RemoteDriver(URL, DesiredCapabilities)
	 */
	public RemoteDriver(DesiredCapabilities capabilities){				
		super(capabilities);		
	}

	/**
	 * @return String, the name of the browser where test is running. Or null if something wrong happens.
	 */
	public String getBrowserName(){
		String debugmsg = StringUtils.debugmsg(false);
		try{
			String name = getCapabilities().getBrowserName();
			IndependantLog.debug(debugmsg+name);
			return name;
		}catch(Exception e){
			IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e));
			try{
				JavascriptExecutor js = (JavascriptExecutor) this;
				String useragent = (String)js.executeScript("return navigator.userAgent;");
				IndependantLog.debug("useragent is '"+useragent+"'");
			}catch(Exception e1){
				IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e1));
			}
			return null;
		}
	}
	
	/**
	 * @return String, the version of the browser where test is running. Or null if something wrong happens.
	 */
	public String getBrowserVersion(){
		String debugmsg = StringUtils.debugmsg(false);
		try{			
			String version = getCapabilities().getVersion().toString();
			IndependantLog.debug(debugmsg+version);
			return version;
		}catch(Exception e){
			IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e));
			try{
				JavascriptExecutor js = (JavascriptExecutor) this;
				String useragent = (String)js.executeScript("return navigator.userAgent;");
				IndependantLog.debug("useragent is '"+useragent+"'");
			}catch(Exception e1){
				IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e1));
			}
			return null;
		}
	}
	
	/**
	 * @return String, the name of the platform where the browser is running. Or null if something wrong happens.
	 */
	public String getPlatform(){
		String debugmsg = StringUtils.debugmsg(false);
		try{			
			String platform = getCapabilities().getPlatform().toString();
			IndependantLog.debug(debugmsg+platform);
			return platform;
		}catch(Exception e){
			IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e));
			try{
				JavascriptExecutor js = (JavascriptExecutor) this;
				String useragent = (String)js.executeScript("return navigator.userAgent;");
				IndependantLog.debug("useragent is '"+useragent+"'");
			}catch(Exception e1){
				IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e1));
			}
			return null;
		}
	}
	
	/**
	 * @return String, the version of 'selenium server' with which the test is running. Or null if something wrong happens.
	 */
	public String getDriverVersion(){
		String debugmsg = StringUtils.debugmsg(false);
		String hostname = null;
		String port = null;
		String version = null;
		
		if(isGrid ){
			//according to the session id, get the node hostname and port
			SessionId id = getSessionId(); 
			String[] node  = GridInfoExtractor.getHostNameAndPort(remote_hostname, remote_port, id);
			IndependantLog.debug(debugmsg+"connected to grid NODE, host="+node[0]+"; port="+node[1]);
			hostname = node[0];
			port = node[1];
		}else{
			hostname = remote_hostname;
			port = String.valueOf(remote_port);
		}
		
		String result = WebDriverGUIUtilities.readHubStaticURL(hostname, port);
		//result from error stream, it is something like following:
//		{"sessionId":null,
//			"status":13,
//			"state":"unhandled error",
//			"value":{
//			   "message":"GET /static/resource\nBuild info: version: '2.48.2', revision: '41bccdd', time: '2015-10-09 19:59:12'\nSystem info: host: 'xxx', ip: '172.27.17.89', os.name: 'Windows 7', os.arch: 'x86', os.version: '6.1', java.version: '1.7.0_45'\nDriver info: driver.version: unknown",
//			   "suppressed":[],
//			   "localizedMessage":"GET /static/resource\nBuild info: version: '2.48.2', revision: '41bccdd', time: '2015-10-09 19:59:12'\nSystem info: host: 'xxx', ip: '172.27.17.89', os.name: 'Windows 7', os.arch: 'x86', os.version: '6.1', java.version: '1.7.0_45'\nDriver info: driver.version: unknown","buildInformation":null,"cause":null,"systemInformation":"System info: host: 'xxx', ip: '172.27.17.89', os.name: 'Windows 7', os.arch: 'x86', os.version: '6.1', java.version: '1.7.0_45'","supportUrl":null,"class":"...(line truncated)...
//			   "additionalInformation":"\nDriver info: driver.version: unknown",
//			   "hCode":19885966,
//		       "stackTrace":[null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null]
//		     },
//			 "class":"org.openqa.selenium.remote.Response",
//			 "hCode":17632485}
		IndependantLog.debug(debugmsg+"Selenium Driver Information:\n"+result);
		try {
			JSONObject hubInformaiton = new JSONObject(result);
			String token = "version:";
//			"message":"GET /static/resource\nBuild info: version: '2.48.2', revision: '41bccdd', time: '2015-10-09 19:59:12'\nSystem info: host: 'xxx', ip: '172.27.17.89', os.name: 'Windows 7', os.arch: 'x86', os.version: '6.1', java.version: '1.7.0_45'\nDriver info: driver.version: unknown",
			version = hubInformaiton.getJSONObject("value").getString("message");
			int index = version.indexOf(token);
			if(index>-1){
				version = version.substring(index+token.length());
				index = version.indexOf(",");
				if(index>-1){
					version = version.substring(0, index);
				}
				version = StringUtilities.removeSingleQuotes(version.trim());
			}
			
			IndependantLog.debug(debugmsg+version);
		} catch (Exception e) {
			IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e));
		}
		
		return version;
	}
		
	/**
	 * Connect to a remote selenium standalone server.<br>
	 * We will also parse the URL to attempt to connect to a SAFS Selenium RMI Server on RMI-SERVER-HOST.<br>
	 * The RMI-SERVER-HOST can be:<br>
	 * 1. The same host as this Selenium Server (Standalone).<br>
	 * 2. The node host assigned by this Selenium Server (grid-hub).<br>
	 * @param selenium_server_url -- URL of the remote Selenium Server.
	 * <p>
	 * @param capabilities -- to enable the possibility of SAFS Selenium RMI Server/Agent communication 
	 * you must set a "capability" of REMOTESERVER to be the hostname of the remote selenium server.
	 * <p>
	 * Ex: capabilities.setCapability(RemoteDriver.CAPABILITY_REMOTESERVER, hostname);
	 * <p>
	 * This is done automatically within the SAFS Selenium code where needed.  Only advanced users doing 
	 * custom initialization and instantiation need set this capability.
	 */
	public RemoteDriver(URL selenium_server_url, DesiredCapabilities capabilities){
		super(selenium_server_url,capabilities);
		remote_URL = selenium_server_url;
		parseSeleniumServerURL(selenium_server_url);
		
		//if it is grid, we need to set RMIAgent with the RMIServer on the grid-node (not on the grid-hub)
		Object gridnodes = capabilities.getCapability(SelectBrowser.KEY_GRID_NODES_SETTING);
		if(gridnodes!=null && gridnodes instanceof String) isGrid = StringUtils.isValid((String)gridnodes);
		if(!isGrid) isGrid = WebDriverGUIUtilities.isGridRunning(remote_hostname, String.valueOf(remote_port));

		if(isGrid ){
			SessionId id = getSessionId(); 
			String[] node  = GridInfoExtractor.getHostNameAndPort(remote_hostname, remote_port, id);
			IndependantLog.debug(StringUtils.debugmsg(false)+"connected to grid NODE, host="+node[0]+"; port="+node[1]);
			rmi_hostname = node[0];
		}
		
		startRMIAgent(rmi_hostname, rmi_registry_port);
	}

	/**
	 * try to start a SAFS Selenium RMI Agent if our "selenium server" is NOT on localhost or it is a "grid hub server".
	 * @param rmihost String, the host name of the RMI server
	 * @param rmiport int, the port number of the RMI server, default is 1099 
	 */
	protected void startRMIAgent(String rmihost, int rmiport){
		if(isLocalServer()) return;
		try{
			rmiAgent = new SeleniumAgent();		
			rmiAgent.setServerHost(rmihost);
			rmiAgent.initialize();
		}catch(RemoteException rx){
			rx.printStackTrace();
		}
	}
	
	/**
	 * @return true if our standalone Selenium Server is local (localhost), otherwise false.
	 */
	public boolean isLocalServer(){
		if(isGrid) return false;//If server is grid-hub, then we need RMI server. But if the node is local machine?
		return remote_hostname == null || NetUtilities.isLocalHost(remote_hostname);
	}
	
	/**
	 * Extract the needed remote_hostname and remote_port from the selenium server URL.
	 * @param URL - URL of the Selenium Server used.
	 */
	protected void parseSeleniumServerURL(URL url){
		try{
			remote_hostname = url.getHost();
			if(remote_hostname.startsWith("[")) remote_hostname = remote_hostname.substring(1);
			if(remote_hostname.endsWith("]")) remote_hostname = remote_hostname.substring(0, remote_hostname.length()-1);
			//Set the "RMI host" the same as "remote host"
			rmi_hostname = remote_hostname;
		}catch(NullPointerException np){
			// ignore
		}
		try{
			if(url.getPort()!=-1) remote_port = url.getPort();
		}catch(Exception e){
			remote_port = SeleniumConfigConstant.DEFAULT_SELENIUM_PORT_INT;
		}
	}
	
	@Override
	public void quit(){		
		super.quit();
		try{ deleteSessionIdFromFile((String)this.getCapabilities().getCapability(CAPABILITY_ID));}
		catch(Throwable t){}
		try{ rmiAgent.disconnect();}
		catch(Throwable t){}
		_quit = true;
	}
	
	public boolean hasQuit(){ return _quit; }
	
	/**
	 * Called internally by OpenQA RemoteWebDriver during Constructor initialization.
	 */
	@Override
	public void startSession(Capabilities desiredCapabilities, 
		      Capabilities requiredCapabilities){
		
		String ID = (String) desiredCapabilities.getCapability(CAPABILITY_ID);
		Boolean reconnect = (Boolean) desiredCapabilities.getCapability(CAPABILITY_RECONNECT);
		String browserName = desiredCapabilities.getBrowserName();
		String remoteserver = (String) desiredCapabilities.getCapability(CAPABILITY_REMOTESERVER);
		if(remote_hostname == null) remote_hostname = remoteserver; //might still be null
		
		// clean up obsolete session 
		try {
			List<SessionInfo> list = getSessionsFromFile();			
			for (SessionInfo info : list) {	
				try {					
					setSessionId(info.session);					
					getCurrentUrl();
				} catch (WebDriverException check){	
					IndependantLog.debug("RemoteDriver deleting sessionid "+ info.id +" from session file due to: "+ check.getClass().getSimpleName());
					deleteSessionIdFromFile(info.id);
					quit();	
				}						
			}						
		} catch (Exception e1) {}		
			
		if (reconnect.booleanValue()){
		    SessionInfo sid = null;
			try {						
				sid = retrieveSessionInfoFromFile(ID);				
				if (sid != null){
					setSessionId(sid.session);
					getCurrentUrl();
					newSession = false;
				}
					
			} catch (WebDriverException we) {
				IndependantLog.debug("RemoteDriver deleting sessionid "+ sid +" from session file due to: "+ we.getClass().getSimpleName());
				try {
					deleteSessionIdFromFile(ID);					
				} catch (Exception e) {
					IndependantLog.debug("RemoteDriver error deleting sessionid "+ sid +" from session file due to: "+ e.getClass().getSimpleName());
				}
			} catch (Exception e) {
				IndependantLog.debug("RemoteDriver error retrieving sessionid "+ sid +" from session file due to: "+ e.getClass().getSimpleName());
			}
			
		} else {					
			super.startSession(desiredCapabilities,requiredCapabilities);	
			try {
				storeSessionIdToFile(remote_hostname, ID,browserName,getSessionId().toString(), desiredCapabilities);
			} catch (Exception e) {
				IndependantLog.debug("RemoteDriver error storing sessionid to file due to: "+ e.getClass().getSimpleName());				
			}
			 		
		}
	}
	
	/*
	 * Trying to fix a problem with IE JSON values containing "-1.IND"
	 * (non-Javadoc)
	 * @see org.openqa.selenium.remote.RemoteWebDriver#execute(java.lang.String, java.util.Map)
	 */
	@Override
    protected Response execute(String driverCommand, Map<String, ?> parameters) {
    	Response response = super.execute(driverCommand, parameters);
    	final String debugmsg = "RemoteDriver.execute ";
    	if((response != null) && (response.getValue() instanceof java.lang.String)){
    		String v = (String) response.getValue();
    		v = v.trim();
    		if(v.startsWith("{") && v.endsWith("}")){
        		IndependantLog.info(debugmsg + driverCommand +" attempting to convert uncertain JSON Response to Map Response: "+ v);
	    		v = v.replace("-1.#IND", "0");
	    		v = v.replace("{'", "{\"");
	    		v = v.replace("':", "\":");
	    		v = v.replace(":'", ":\"");
	    		v = v.replace("',", "\",");
	    		v = v.replace(",'", ",\"");	    		
        		try{ 
        			Object nv = Json.convert(Map.class, v);
        			if(nv instanceof Map){
    	        		IndependantLog.info(debugmsg + driverCommand +" JSON Map conversion SUCCESSFUL: "+ nv.toString());
    	        		Map<?, ?> m = (Map<?, ?>) nv;
    	        		final String SESSIONID = "sessionid";
    	        		final String STATUS = "status";
    	        		final String VALUE = "value";
    	        		for(Object key: m.keySet()){
    	        			if(key.toString().equalsIgnoreCase(SESSIONID)) response.setSessionId(m.get(key).toString());
    	        			try{if(key.toString().equalsIgnoreCase(STATUS)) response.setStatus(((Number)m.get(key)).intValue());}
    	        			catch(Exception x){
    	    	        		IndependantLog.info(debugmsg + driverCommand +" STATUS conversion "+ x.getClass().getName()+", "+x.getMessage());
    	    	        	}
            				if(key.toString().equalsIgnoreCase(VALUE)) response.setValue(m.get(key));
    	        		}
        			}else{
    	        		IndependantLog.info(debugmsg + driverCommand +" JSON conversion WAS NOT successful.");
        			}
        		}catch(Throwable t){
        			IndependantLog.debug(debugmsg + driverCommand +" ignoring JSON problem\n", t);
	    		}
    		}
    	}else{
			if(response == null) {
				IndependantLog.debug(debugmsg + driverCommand +" failed to get any type of response.");
			}else{
				// DEBUG IndependantLog.debug(debugmsg + driverCommand +" received a "+ response.getClass().getSimpleName()+" response.");				
			}
    	}
    	return response;
    }
	
	public static boolean isNewSession(){
		return newSession;
	}

	private static String getString(Capabilities capabilities, String key){
		try{
			Object value = capabilities.getCapability(key);
			return (value==null? "":(String)value);			
		}catch(Exception e){
			IndependantLog.error(StringUtils.debugmsg(false)+" Met "+StringUtils.debugmsg(e));
			return "";
		}
	}
	
	public static synchronized void storeSessionIdToFile(String serverHostname, String Id,  String browserName, String sessionid, Capabilities desiredCapabilities) throws Exception{
		
		try {
			Properties prop = new Properties();
			String dirname = System.getProperty(JAVA_TMPDIR);
			String file = dirname + SESSION_FILE;
			
			IndependantLog.debug("RemoteDriver.storeSessionIDToFile attempting to store session id: "+ Id);

			File afile = new File(file);
			if (afile.exists()){
				IndependantLog.debug("RemoteDriver.storeSessionIDToFile file DOES exist: "+ afile.getCanonicalPath());
				prop.load(new FileInputStream(file));
			}else{
				IndependantLog.debug("RemoteDriver.storeSessionIDToFile file does NOT already exist: "+ afile.getCanonicalPath());
			}

			//get firefox-profile (name or filename) from capabilities
			String firefoxProfile = getString(desiredCapabilities, SelectBrowser.KEY_FIREFOX_PROFILE);
			//get chrome user-data-directory from capabilities
			String chromeUserDataDir = getString(desiredCapabilities, SelectBrowser.KEY_CHROME_USER_DATA_DIR);
			//get chrome profile-directory from capabilities
			String chromeProfileDir = getString(desiredCapabilities, SelectBrowser.KEY_CHROME_PROFILE_DIR);
			//get chrome preference from capabilities
			String chromePreference = getString(desiredCapabilities, SelectBrowser.KEY_CHROME_PREFERENCE);
			//get chrome excluded options from capabilities, it is comma-separated string
			String chromeExcludeOptions = getString(desiredCapabilities, SelectBrowser.KEY_CHROME_EXCLUDE_OPTIONS);
			//get firefox preference from capabilities
			String firefoxPreference = getString(desiredCapabilities, SelectBrowser.KEY_FIREFOX_PROFILE_PREFERENCE);

			//create the session-content used for reconnection
			sessionContent = serverHostname +SPLITTER+ browserName +SPLITTER+ sessionid + SPLITTER+ firefoxProfile
					+ SPLITTER+ chromeUserDataDir+ SPLITTER+ chromeProfileDir+ SPLITTER+ chromePreference+ SPLITTER+ chromeExcludeOptions + SPLITTER + firefoxPreference; 
			prop.put(Id, sessionContent); 
			prop.put(LAST_SESSION_KEY, Id); 

			//if (!afile.exists()) afile.createNewFile();
			OutputStream out = new FileOutputStream(afile);	
			prop.store(out, file);
			
		} catch (Exception e) {
			IndependantLog.debug("RemoteDriver error writing session file due to: "+ e.getClass().getSimpleName());
			throw new Exception("Session store issue " + e.getMessage());
		} 

	}
	
	
	/**
	 * Delete the session from the RemoteDriver file with the given Id.
	 * If the Id is also set as the LAST_SESSION, then the LAST_SESSION_KEY 
	 * will also be removed showing the last or "current" session is uncertain.
	 * @param Id
	 * @throws Exception if there is a problem reading or deleting the session info.
	 */
	public static synchronized void deleteSessionIdFromFile(String Id) throws Exception {		
		try {
			Properties prop = new Properties();

			String dirname = System.getProperty(JAVA_TMPDIR);
			String file = dirname + SESSION_FILE;

			IndependantLog.debug("RemoteDriver.deleteSessionIDFromFile attempting to delete session ID: "+ Id);

			File afile = new File(file);
			if (! afile.exists()){
				IndependantLog.debug("RemoteDriver.deleteSessionIDFromFile file does NOT already exist: "+ afile.getCanonicalPath());
			}else{
				IndependantLog.debug("RemoteDriver.deleteSessionIDFromFile file '"+ afile.getCanonicalPath() +" DOES exist.");
			}
			prop.load(new FileInputStream(file));
			String lastSessionID = prop.getProperty(LAST_SESSION_KEY);
			try{
				prop.remove(Id);
				if(Id.equals(lastSessionID)){
					prop.remove(LAST_SESSION_KEY);
					IndependantLog.debug("RemoteDriver.deleteSessionIDFromFile deleting LASTSESSION from session file.");
				}
			}catch(NullPointerException x){
				IndependantLog.debug("RemoteDriver.deleteSessionIDFromFile ignoring NullPointerException: Id="+ Id +", lastSessionID="+ lastSessionID);
			}
			OutputStream out = new FileOutputStream(afile);
			prop.store(out, file);
		} catch (Exception e) {
			IndependantLog.debug("RemoteDriver.deleteSessionIDFromFile file error due to: "+ e.getClass().getSimpleName(), e);
			throw new Exception("Session delete issue");
		}
	}

	/**
	 * Retrieve the remote server's session information for the session with the given id.
	 * @param Id
	 * @return SessionInfo or null if not found.
	 * @throws Exception
	 */
	public static synchronized SessionInfo retrieveSessionInfoFromFile(String Id) throws Exception{
				
		try {
			if(Id == null || Id.length()==0) throw new IllegalArgumentException("Id cannot be null or zero-length.");
			
			Properties prop = new Properties();
					
			String dirname = System.getProperty(JAVA_TMPDIR);
			String file = dirname + SESSION_FILE;
			
			File afile = new File(file);
			if (! afile.exists()){
				IndependantLog.debug("RemoteDriver.retrieveSessionIDFromFile file does NOT already exist: "+ afile.getCanonicalPath());
			}
			prop.load(new FileInputStream(file));
			String session = prop.getProperty(Id,null);
			
			if (session == null)return null;
			
			String last = prop.getProperty(LAST_SESSION_KEY);
			SessionInfo info = new SessionInfo(Id, Id.equalsIgnoreCase(last), session);
			return info;
		} 
		catch (Exception e) {
			IndependantLog.debug("RemoteDriver.retrieveSessionIDFromFile file error due to: "+ e.getClass().getSimpleName()+ ", "+ e.getMessage());
			throw new Exception("Session retrieve issue.  Session info may not be valid or complete.");
		}                
	}
	/**
	 * Retrieve the remote server's session string for the session with the given id.
	 * @param Id
	 * @return
	 * @throws Exception
	 */
	public static synchronized String retriveSessionIdFromFile(String Id) throws Exception{

		return retrieveSessionInfoFromFile(Id).session;        
	}
	
	public static synchronized void setLastSessionId(String Id) throws Exception{
		
		try {
			if( Id==null || Id.length()==0) throw new IllegalArgumentException("Id cannot be null or zero-length.");
			Properties prop = new Properties();
					
			String dirname = System.getProperty(JAVA_TMPDIR);
			String file = dirname + SESSION_FILE;
			
			File afile = new File(file);
			if (afile.exists()){
				prop.load(new FileInputStream(afile));			
				prop.put(LAST_SESSION_KEY, Id);
			}else{
				IndependantLog.debug("RemoteDriver.setLastSessionID file does NOT already exist: "+ afile.getCanonicalPath());				
			}
		    
			//if (!afile.exists()) afile.createNewFile();
			OutputStream out = new FileOutputStream(afile);	
			prop.store(out, file);			
			
		} catch (Exception e) {
			IndependantLog.debug("RemoteDriver.setLastSessionID error due to: "+ e.getClass().getSimpleName()+", "+e.getMessage());
			throw new Exception("Session retrive issue");
		}        
	}
	
	/**
	 * Retrieve SessionInfo representing the "current" or "last" session
	 * @return SessionInfo -- can be null if there is no session running or stored.
	 * @throws Exception
	 */
	public static synchronized SessionInfo retriveLastSessionInfoFromFile() throws Exception{		
		
		try {
			Properties prop = new Properties();
					
			String dirname = System.getProperty(JAVA_TMPDIR);
			String file = dirname + SESSION_FILE;
			
			File afile = new File(file);
			if (!afile.exists()){
				IndependantLog.debug("RemoteDriver.retrieveLastSessionIDFromFile file does NOT already exist: "+ afile.getCanonicalPath());				
			}
			prop.load(new FileInputStream(file));
			String browserId = prop.getProperty(LAST_SESSION_KEY,null);
			if(browserId == null) return null;
			sessionContent = prop.getProperty(browserId,null);

			return new SessionInfo(browserId, true, sessionContent);
			
		} catch (Exception e) {
			IndependantLog.debug("RemoteDriver.retrieveLastSessionIDFromFile error due to: "+ e.getClass().getSimpleName());
			throw new Exception("Session Key retrieval issue");
		}
        
	}
	
	/**
	 * Returns a List of SessionInfo objects.
	 * @return List --can be empty if no sessions and no Exception is thrown.
	 * @throws Exception if the sessions are not retrievable (do not exist).
	 */
	public static synchronized List<SessionInfo> getSessionsFromFile() throws Exception{		
		
		try {
			Properties prop = new Properties();
					
			String dirname = System.getProperty(JAVA_TMPDIR);
			String file = dirname + SESSION_FILE;
			
			File afile = new File(file);
			if (!afile.exists()){
				IndependantLog.debug("RemoteDriver.getSessionsFromFile file does NOT already exist: "+ afile.getCanonicalPath());				
			}
			prop.load(new FileInputStream(file));
			List<SessionInfo> list = new ArrayList<SessionInfo> ();
			String last = prop.getProperty(LAST_SESSION_KEY);
			Enumeration<Object> en = prop.keys();
			String key = null;
			while(en.hasMoreElements()){
				key = (String)en.nextElement();
				if(key.equals(LAST_SESSION_KEY)) continue;
				sessionContent = prop.getProperty(key);				

				try{ list.add(new SessionInfo(key, key.equals(last), sessionContent));}
				catch(Exception ignore){}
			}
			return list;
			
		} catch (Exception e) {
			IndependantLog.debug("RemoteDriver.getSessionsFromFile error due to: "+ e.getClass().getSimpleName());				
			throw new Exception("Session Key retrieval issue", e);
		}        
	}
	
	public static synchronized void deleteSessionFile(){
		String dirname = System.getProperty(JAVA_TMPDIR);
		String fileName = dirname + SESSION_FILE;		
		File afile = new File(fileName);
		if (afile.exists()) afile.delete();		
	}
	
	public <X> X getScreenshotAs(OutputType<X> target)
				throws WebDriverException {
		
			return target
					.convertFromBase64Png(execute(DriverCommand.SCREENSHOT)
							.getValue().toString());		
	}
	
	/*
	 * Override Javascript executescript method due to reconnection 
	 * webdriver throw null exception
	 */
	@Override
	public Object executeScript(String script, Object... args) {
	    		 
	    // Escape the quote marks
	    script = script.replaceAll("\"", "\\\"");

	    Iterable<Object> convertedArgs = Iterables.transform(
	        Lists.newArrayList(args), new WebElementToJsonConverter());

	    Map<String, ?> params = ImmutableMap.of(
	        "script", script,
	        "args", Lists.newArrayList(convertedArgs));

	    return execute(DriverCommand.EXECUTE_SCRIPT, params).getValue();
	 }
	
	/**
	 * The current format of each SessionInfo when stored in file:
	 * <p>
	 * ID=SERVERHOST_SP_BROWSER_SP_SESSION_SP_&lt;extraParameters>
	 */
	public static class SessionInfo {
		public String serverHost = null;
		public String id = null;
		public String browser = null;
		public String session = null;
		public boolean isCurrentSession = false;
		public HashMap<String,Object> extraParameters = new HashMap<String,Object>();
		
		private SessionInfo(){super();}
		
		/**
		 * Builds a new SessionInfo object from a real new session.
		 * 
		 * @param serverHost
		 * @param id
		 * @param browser
		 * @param session
		 * @param isCurrentSession
		 */
		public SessionInfo(String serverHost, String id, String browser, String session, boolean isCurrentSession){
			this.serverHost = serverHost;
			this.id = id;
			this.browser = browser;
			this.session = session;
			this.isCurrentSession = isCurrentSession;
		}
		
		/**
		 * Builds a SessionInfo object from (probably) stored sessionContent information.
		 * @param id
		 * @param isCurrentSession
		 * @param sessionContent String, read from a session file
		 */
		public SessionInfo(String id, boolean isCurrentSession, String sessionContent){
			this.id = id;
			this.isCurrentSession = isCurrentSession;
			
			try{
				String[] sessionInfos = StringUtils.getTokenArray(sessionContent, SPLITTER);
				this.serverHost = sessionInfos[0];
				this.browser = sessionInfos[1];
				this.session = sessionInfos[2];
				try{if(!sessionInfos[3].isEmpty()) extraParameters.put(SelectBrowser.KEY_FIREFOX_PROFILE, sessionInfos[3]);}catch(Exception e){}
				try{if(!sessionInfos[4].isEmpty()) extraParameters.put(SelectBrowser.KEY_CHROME_USER_DATA_DIR, sessionInfos[4]);}catch(Exception e){}
				try{if(!sessionInfos[5].isEmpty()) extraParameters.put(SelectBrowser.KEY_CHROME_PROFILE_DIR, sessionInfos[5]);}catch(Exception e){}
				try{if(!sessionInfos[6].isEmpty()) extraParameters.put(SelectBrowser.KEY_CHROME_PREFERENCE, sessionInfos[6]);}catch(Exception e){}
				try{if(!sessionInfos[7].isEmpty()) extraParameters.put(SelectBrowser.KEY_CHROME_EXCLUDE_OPTIONS, sessionInfos[7]);}catch(Exception e){}
				try{if(!sessionInfos[8].isEmpty()) extraParameters.put(SelectBrowser.KEY_FIREFOX_PROFILE_PREFERENCE, sessionInfos[8]);}catch(Exception e){}
				
			}catch(Exception e){
				IndependantLog.error("Fail to initialize SessionInfo due to "+StringUtils.debugmsg(e));
			}			
		}
	}
	
	/** The debug log file containing debug message when calling main() to start "selenium server". */
	public static final String debugLogFile = "C:\\"+RemoteDriver.class.getName().replaceAll("\\.", "_")+"_debug_log.txt";
	private static PrintStream pstream = null;
	
	/**
	 * This main method will start a Selenium Server (standalone, hub, or node). It will automatically detect the Java to use,
	 * which file to log messages for each browser, and what driver (IE CHROME) to be used to start with Selenium Server.<br>
	 * <br>
	 * @param args String[], it accepts following arguments:<br>
	 * <b>"-port N"</b>, optional, the port number for Selenium Server. If not provided, the default port will be used.
	 *                   For "standalone" and "hub" the default port number is 4444; While for "node", it is 5555.
	 *                   <br>
	 * <b>"SELENIUMSERVER_JVM_OPTIONS=JVM OPTIONS"</b>, optional, the JVM Options to start the Selenium Server<br>
	 * <b>"-role TheServerRole"</b>, optional, if not provided, a standalone server will be launched.<br>
	 *                                         TheServerRole could be <b>"hub"</b>, and selenium server will be launched
	 *                                         as a hub for other node to connect.<br>
	 *                                         TheServerRole could be <b>"node"</b>, and selenium server will be launched
	 *                                         as a node to connect a hub. <b>**Note**</b> Hub's information must also 
	 *                                         be provided. Ex: <b>-role node -hub http://hub.machine:port/grid/register</b><br>
	 * <b>"-project ProjectLocationAbsDir"</b>, optional, the absolute directory holding the project to test; If not provided, use
	 *                                        SeleniumPlus or SAFS installation directory as default. This parameter has higher
	 *                                        priority than JVM parameter "-Dsafs.project.root".<br>
	 * <br>
	 * Example:<br>
	 * <ul>
	 * <li>Start "selenium standalone server on port 4567 with JVM option -Xms512m -Xmx2g"<br>
	 * java org.safs.selenium.webdriver.lib.RemoteDriver "-port 4567" "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx2g"<br>
	 * 
	 * <li>Start "selenium hub on port 4567 with JVM option -Xms512m -Xmx2g"<br>
	 * java org.safs.selenium.webdriver.lib.RemoteDriver "-role hub" "-port 4567" "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx2g"<br>
	 * 
	 * <li>Start "selenium node" on port 5555 with JVM option -Xms512m -Xmx2g", and register it to hub.machine:4567<br>
	 * java org.safs.selenium.webdriver.lib.RemoteDriver "-role node -hub http://hub.machine:4567/grid/register" "-port 5678" "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx2g"<br>
	 * 
	 * </ul>
	 * <br>
	 * We can also provide the following <b>JVM parameters</b>.<br>
	 * <ul>
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_OPTIONS}
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xms}
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xmx}
	 * <li> {@link DriverConstant#PROPERTY_SAFS_PROJECT_ROOT}
	 * </ul>
	 * Example:<br>
	 * <ul>
	 * <li>-DSELENIUMSERVER_JVM_OPTIONS="-Xms256m -Xmx1g"
	 * <li>-DSELENIUMSERVER_JVM_Xmx=4g
	 * <li>-DSELENIUMSERVER_JVM_Xms=512m
	 * <li>-Dsafs.project.root=d:\full_path\to\test_project
	 * </ul>
	 */
	public static void main(String[] args){
		String debugmsg = StringUtils.debugmsg(false);
		
		try {
			pstream = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(debugLogFile))));
			IndependantLog.setDebugListener(new DebugListener(){
				public String getListenerName() {
					return null;
				}
				public void onReceiveDebug(String message) {
					if(pstream!=null) pstream.println(message);
					System.out.println(message);
				}
			});
			IndependantLog.debug(debugmsg+ RemoteDriver.class.getName() + " launching, the debug message will be in file '"+RemoteDriver.debugLogFile+"'.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		IndependantLog.debug(debugmsg+" Current System properties: "+System.getProperties());
		
		String projectLocation = System.getProperty(DriverConstant.PROPERTY_SAFS_PROJECT_ROOT);
		
		for(int i=0;i<args.length;i++){
			if(args[i].equalsIgnoreCase("-project")){
				if((i+1)<args.length){
					projectLocation = args[++i];
				}
			}
		}
		
		IndependantLog.debug(debugmsg+"Project location is "+projectLocation);
		
		if(!WebDriverGUIUtilities.startRemoteServer(projectLocation, args)){
			IndependantLog.error(debugmsg+"Fail to start Remote Server with parameter "+Arrays.toString(args));
		}
		
		if(pstream!=null){
			pstream.flush();
			pstream.close();
		}
		
		IndependantLog.setDebugListener(null);
	}
}
