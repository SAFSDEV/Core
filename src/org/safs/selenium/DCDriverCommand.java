package org.safs.selenium;
/**
 * Logs for developers, not published to API DOC.
 * History:<br>
 *   <br>   JUL 05, 2011    (LeiWang) Update method setFocus().
 *   <br>   APR 07, 2016    (Lei Wang) Refactor to handle OnGUIExistsGotoBlockID/OnGUINotExistGotoBlockID in super class DriverCommand
 */
import java.util.Iterator;

import org.openqa.selenium.server.RemoteControlConfiguration;
import org.safs.DDGUIUtilities;
import org.safs.DriverCommand;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.text.FAILStrings;
import org.safs.tools.drivers.DriverConstant;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class DCDriverCommand extends DriverCommand {
	public static final String CLEARAPPMAPCACHE         = "ClearAppMapCache";
	public static final String STARTWEBBROWSER          = "StartWebBrowser";
	public static final String WAITFORGUI               = "WaitForGui";
	public static final String WAITFORWEBPAGE           = "WaitForWebPage";
	public static final String SETCONTEXT               = "SetContext";
	public static final String SETFOCUS                 = "SetFocus";
	public static final String ONGUIEXISTSGOTOBLOCKID   = "OnGUIExistsGotoBlockID";
	public static final String ONGUINOTEXISTGOTOBLOCKID = "OnGUINotExistGotoBlockID";
	
	public static final String DEFAULT_BROWSER = "*piiexplore";
	
	/**A convenient GUIUtilities*/
	protected SeleniumGUIUtilities sgu = null;
	
	//STestRecordHelper testRecordData;
	public DCDriverCommand() {
		super();
	}

	/** 
	 * Convert the general GUIUtilities to a specific one.
	 **/
	protected void init() throws SAFSException{
		super.init();
		try{
			sgu = (SeleniumGUIUtilities) utils;			
		}catch(Exception e){
			String msg = " Met Exception "+StringUtils.debugmsg(e);
			IndependantLog.error(StringUtils.debugmsg(false)+msg);
			throw new SAFSException("Failed to convert GUIUtilities, "+msg);
		}
	}

	protected void commandProcess() {
		String dbg = getClass().getName()+".commandProcess ";
    	Log.info(dbg+"processing: "+ command);
    	
		if(command.equalsIgnoreCase(STARTWEBBROWSER)){
			startWebBrowser();
		} else if(command.equalsIgnoreCase(WAITFORGUI)|| command.equalsIgnoreCase(WAITFORWEBPAGE)){
			waitForGui();
		} else if(command.equalsIgnoreCase(SETCONTEXT) || command.equalsIgnoreCase(SETFOCUS)){
			setFocus();
		} else if(command.equalsIgnoreCase(CLEARAPPMAPCACHE)){
			clearAppMapCache();
		}
	}
	
	private void clearAppMapCache(){
		//localClearAppMapCache(null, null);
		String msg = "";
		try{
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			DDGUIUtilities utils = ((TestRecordHelper)testRecordData).getDDGUtils();
			utils.clearAllAppMapCaches();
			msg = genericText.convert("success2",
			"Selenium "+ testRecordData.getCommand() +" successful.",
			"Selenium", testRecordData.getCommand());
		}
		catch(Exception x){;}
		log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);
	}
	
	private void setFocus() {
		String debugmsg = getClass().getName()+".setFocus() ";
			
		if(params.size() < 2){
			issueParameterCountFailure();
			return;
		}
		
		Iterator iterator = params.iterator();
		String window = (String) iterator.next();
		String component = (String) iterator.next();
		Log.debug(debugmsg+" Set focus to "+window+":"+component);

		boolean focused = false;
		String winCompString = (window.equalsIgnoreCase(component))? window:window+":"+component;
		
		Selenium sel = SApplicationMap.getSelenium(window);
		//SApplicationMap.getSelenium(window).selectWindow(null);
		
		if(window.equalsIgnoreCase(component)){
			//wait for window object
			String winRec = testRecordData.getSTAFHelper().getAppMapItem(testRecordData.getAppMapName(),window,window);
			String winid = sgu.getWindowIdFromTitle(sel, winRec).getWindowId();
			focused = sgu.selectWindow(sel, winid, 1);
		}else{
			//wait for component object
			SGuiObject sto = sgu.getTestObject(testRecordData.getAppMapName(), window, component, true);
			if(sto!=null){
				try{
					sel.focus(sto.getLocator());
					focused = true;
				}catch(Exception e){
					Log.error(debugmsg+" can't focus component object. Exception="+e.getMessage());					
				}
			}else{
				Log.error(debugmsg+" can't get the component object.");
			}
		}
		
		if(focused){
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			String msg = genericText.convert(TXT_SUCCESS_2,
					winCompString+" "+ testRecordData.getCommand() +" successful.",
					winCompString, testRecordData.getCommand());
			log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);
		}else{
	    	issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND,
	    			winCompString +" was not found.", winCompString));
		}
		
	}

	private void startWebBrowser(){
		if(params.size() < 2){
			issueParameterCountFailure();
			return;
		}
		
		Iterator iterator = params.iterator();
		String url = (String) iterator.next();
		String id = (String) iterator.next();
		String browser = ((STestRecordHelper)testRecordData).getConfig().getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM,"BROWSER");
		if ((browser==null)||(browser.length()==0)) browser = DEFAULT_BROWSER;
		int intPort = RemoteControlConfiguration.DEFAULT_PORT;
		String port = ((STestRecordHelper)testRecordData).getConfig().getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM,"SELENIUMPORT"); 
		if ((port!=null)&&(port.length()>0)) {
			try{
				intPort = Integer.parseInt(port);
				Log.info("Selenium.StartWebBrowser using Selenium Proxy on port: "+ intPort);
			}catch(NumberFormatException nfe){
				Log.debug("Selenium.StartWebBrowser '"+ port +"' NumberFormatException reverting to Selenium Proxy on default port: "+ intPort);
			}
		}
		port = String.valueOf(intPort);
		Selenium selenium = new DefaultSelenium("localhost", intPort, browser, url);
		SApplicationMap.addSelenium(id,selenium);
		String msg = "";
		try{
			selenium.start();
			selenium.open(url);
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			msg = genericText.convert("sentmsg3", testRecordData.getCommand()+" "+ url +" sent to "+ "localhost:"+ port,
					testRecordData.getCommand(), url, "localhost:"+ port);
			log.logMessage(testRecordData.getFac(),msg,GENERIC_MESSAGE);
		}
		catch(Throwable th){
			String thmsg = th.getMessage();
			if (thmsg.length()==0) thmsg = th.getClass().getName();
			System.err.println("Selenium session start() error:"+ thmsg);
			Log.error("Selenium session start() error."+ thmsg);			
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			issueUnknownErrorFailure(thmsg);
		}
	}
	
	/** <br><em>Purpose:</em> waitForGui
	 **/
	private void waitForGui () {
		if (params.size() < 2) {
			issueParameterCountFailure();
			return;
		}
		Iterator iterator = params.iterator();
		final String DEFAULT_SECONDS_STR = "15";
		final String DEFAULT_WEBPAGE_STR = "30";
		boolean isWeb = testRecordData.getCommand().equalsIgnoreCase(WAITFORWEBPAGE);
		String DEFAULT_TIMEOUT = isWeb ? DEFAULT_WEBPAGE_STR:DEFAULT_SECONDS_STR;
		
		// get the window, comp
		String windowName = (String) iterator.next();
		String compName = (String) iterator.next();
		String command = testRecordData.getCommand().toLowerCase();
		String seconds = null;
		int secii = 0;
		try { // optional param
			seconds = (String)iterator.next();
			if (seconds.length()==0) seconds = DEFAULT_TIMEOUT;
			secii = Integer.parseInt(seconds);
			log.logMessage(testRecordData.getFac(),
				genericText.convert("default_missing_param",
							command +" optional parameter '"+ "TIMEOUT" +"' set to '"+ seconds +"'.",
							command, "TIMEOUT", seconds),
				GENERIC_MESSAGE);
		} catch (NumberFormatException e) {
			seconds = DEFAULT_TIMEOUT;
			secii = Integer.parseInt(seconds);
			log.logMessage(testRecordData.getFac(),
				genericText.convert("default_bad_param",
						command +" invalid optional parameter '"+ "TIMEOUT" +"' set to '"+ seconds +"'.",
						command, "TIMEOUT", seconds),
				GENERIC_MESSAGE);
		} catch (Exception e) { 
			seconds = DEFAULT_TIMEOUT;
			secii = Integer.parseInt(seconds);
			log.logMessage(testRecordData.getFac(),
				genericText.convert("default_missing_param",
						command +" optional parameter '"+ "TIMEOUT" +"' set to '"+ seconds +"'.",
						command, "TIMEOUT", seconds),
				GENERIC_MESSAGE);
		}
		if (secii < 0) secii = 0;
		Log.info("............................."+command+": window:"+windowName+", component:"+compName+", seconds:"+seconds);
		String msg = "";
		try {
			// wait for the window/component
			int status = ((TestRecordHelper)testRecordData).getDDGUtils().
			waitForObject(testRecordData.getAppMapName(),
					windowName, compName, secii);
			//if it cannot be found within timeout
			if (status != 0) {
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				msg = failedText.convert("not_found_timeout", 
								compName +" was not found within timeout "+ seconds, 
								compName, seconds);
				standardFailureMessage(msg, testRecordData.getInputRecord());
				return;
			}
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);			
			log.logMessage(testRecordData.getFac(), 
						   genericText.convert("found_timeout", compName +" was found within timeout "+ seconds,
						   compName, seconds),
						   GENERIC_MESSAGE); 
		} catch (SAFSException se) {
			se.printStackTrace();
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			msg = failedText.convert("not_found_timeout", 
							compName +" was not found within timeout "+ seconds, 
							compName, seconds);
			String semsg = se.getMessage();
			if (semsg.length()==0) semsg = se.getClass().getName();
			standardFailureMessage(msg, "SAFSException:"+ semsg);
		}
	}

	protected boolean checkGUIExistence(boolean expectedExist, String mapNam, String window, String component, int timeoutInSeconds) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		//search within timeout value
		long msecTimeout = timeoutInSeconds * 1000;
		long ms0 = System.currentTimeMillis();
		long ms1 = ms0 + msecTimeout;
		Log.info("..begin search for GUI: "+ window + ":" + component +" ms0: "+ms0+", timeout: "+ms1);

		try {
			//clear appmapcache to start fresh search
			SGuiObject tobj = (SGuiObject) localClearAppMapCache(window, component);

			int j=0;
			boolean satisfied = false;
			for(; ; j++) { // try several times
				if (tobj != null) {
					//if match, break loop, success!					
					Selenium sel = SApplicationMap.getSelenium(window);
					sgu.selectWindow(sel, tobj.getWindowId(), 1);
					try{
						if (sel.isElementPresent(tobj.getLocator()) == expectedExist) {					
							satisfied= true;
							break;
						}
					}catch(RuntimeException rx){
						Log.debug("Selenium Runtime Exception IGNORED:", rx);
					}
				}
				long msn = System.currentTimeMillis();
				if (msn > ms1) break;
				delay(100);
				Log.info("..search for GUI: "+ window + ":" + component +" looping... : msn: "+msn+", j: "+j);
				//clear appmapcache again to start fresh search
				tobj = (SGuiObject) localClearAppMapCache(window, component);
			}
			
			return satisfied;
		}catch (Exception e) {
			Log.error(debugmsg +" failed : Met Exception", e);
			throw new SAFSException(e.getMessage());
		}
	}
	
	  /** clear the cache of the test objects maintained by the appmap class,
	   ** plus return the new TestObject for the windowName anc compName
	   ** <br> this version is the worker, and does not set status or log a message
	   ** @param windowName, String, if null then uses a dummy name
	   ** @param compName, String, if null then uses a dummy name
	   ** @return, if windowName or compName are null or not fould, then return null, else
	   ** after clearing the cache, returns the new TestObject if found
	   **/
	  private Object localClearAppMapCache(String windowName, String compName) {
		if (windowName==null) windowName = "___any";
		if (compName==null) compName = "___comp";
		String mapname = testRecordData.getAppMapName();
		DDGUIUtilities utils = ((TestRecordHelper)testRecordData).getDDGUtils();
		Object obj = ((SeleniumGUIUtilities)utils).getTestObject(mapname, windowName, compName, true);
		return obj;
	  }
	
}
