package org.safs.selenium;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openqa.selenium.server.RemoteControlConfiguration;
import org.safs.DDGUIUtilities;
import org.safs.DriverCommand;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.TestRecordHelper;
import org.safs.text.FAILStrings;
import org.safs.tools.drivers.DriverConstant;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
/**
 *   <br>   JUL 05, 2011    (LeiWang) Update method setFocus().
 */
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
	
	//STestRecordHelper testRecordData;
	public DCDriverCommand() {
		super();
	}

	public void process() {
		// first interpret the fields of the test record and put them into the
		// appropriate fields of testRecordData
		try{ setParams(interpretFields());}		
        catch(SAFSException sx){
        	Log.debug("Selenium DCDriverCommand parsing error:"+ sx.getMessage(), sx);
        }
		//called script MUST set StepDriverTestInfo.statuscode accordingly.
		//this is one way we make sure the script executed and a script 
		//command failure was not encountered prematurely.
		testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
      
		String cmd = testRecordData.getCommand();
		if(cmd.equalsIgnoreCase(STARTWEBBROWSER)){
			startWebBrowser();
		} else if(cmd.equalsIgnoreCase(WAITFORGUI)|| cmd.equalsIgnoreCase(WAITFORWEBPAGE)){
			waitForGui();
		} else if(cmd.equalsIgnoreCase(SETCONTEXT) || cmd.equalsIgnoreCase(SETFOCUS)){
			setFocus();
		} else if(cmd.equalsIgnoreCase(ONGUIEXISTSGOTOBLOCKID)){
			onGUIGotoCommands(true);
		} else if(cmd.equalsIgnoreCase(ONGUINOTEXISTGOTOBLOCKID)){
			onGUIGotoCommands(false);
		} else if(cmd.equalsIgnoreCase(CLEARAPPMAPCACHE)){
			clearAppMapCache();
		} else {		
			super.process();
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
		
		SeleniumGUIUtilities sgu = (SeleniumGUIUtilities) testRecordData.getDDGUtils();
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
	
	/** <br><em>Purpose:</em> OnGUI(Not)Exist(s)GotoBlockID
	 * e.g. C, OnGUIExistsGotoBlockID, BlockID, Window, Component[, Timeout]
	 * 
	 * @author phsabo	1.25.2007	Created
	 * 
	 * This method first determines if branching should occur based on whether or not the GUI is found. Then,
	 * it sets the TestRecordData status to BRANCH_TO_BLOCKID so that the driver knows to attempt a branch when
	 * control returns to the driver.  This method ustilizes the TestRecordData field statusinfo to store the 
	 * name of the blockID.
	 **/
	private void onGUIGotoCommands (boolean exists) {
		//validate number of params
		if (params.size() < 3) {
			issueParameterCountFailure();
			return;
		}

		String methodName = "onGUIGotoCommands";
		String command = testRecordData.getCommand();
		String table = testRecordData.getFilename();
		String recordnum = String.valueOf(testRecordData.getLineNumber());

		// get params
		Iterator iterator = params.iterator();
		String blockName  = (String) iterator.next();
		String windowName = (String) iterator.next();
		String compName   = (String) iterator.next();
		int timeout = 15;	// default timeout
		String seconds = String.valueOf(timeout);
		int secii;
		try { // optional timeout param
			seconds = (String)iterator.next();
		}
		catch (NoSuchElementException nse) {
			//exception is okay, timeout is optional parameter
			Log.info (methodName +": Optional timeout value not specified;" +
					" using default " + seconds + " seconds instead.");
			log.logMessage(testRecordData.getFac(),
				genericText.convert("default_missing_param",
							command +" optional parameter '"+ "TIMEOUT" +"' set to '"+ seconds +"'.",
							command, "TIMEOUT", seconds),
				GENERIC_MESSAGE);
		}
		// create timeout value
		try {
			secii = Integer.parseInt(seconds);
		}
		catch (NumberFormatException nfe) {
			//exception is okay, use default timeout value instead
			secii = timeout;
			seconds = String.valueOf(secii);
			Log.info (methodName +": Optional timeout value not a number;" +
					" using default " + seconds + " seconds instead.");
			log.logMessage(testRecordData.getFac(),
				genericText.convert("default_bad_param",
						command +" invalid optional parameter '"+ "TIMEOUT" +"' set to '"+ seconds +"'.",
						command, "TIMEOUT", seconds),
				GENERIC_MESSAGE);
		}
		Log.info(".."+command+": window:"+windowName+", component:"+compName+", seconds:"+seconds);

		try {
			//clear appmapcache to start fresh search
			SGuiObject tobj = (SGuiObject) localClearAppMapCache(windowName, compName);

			//search within timeout value
			long msecTimeout = secii * 1000;
			long ms0 = System.currentTimeMillis();
			long ms1 = ms0 + msecTimeout;
			Log.info("..begin search for GUI: "+ windowName + ":" + compName +" ms0: "+ms0+", timeout: "+ms1);
			int j=0;
			boolean match = false;
			for(; ; j++) { // try several times
				if (tobj != null) {
					//if match, break loop, success!					
					SeleniumGUIUtilities sgu = (SeleniumGUIUtilities) testRecordData.getDDGUtils();
					Selenium sel = SApplicationMap.getSelenium(windowName);
					sgu.selectWindow(sel, tobj.getWindowId(), 1);
					try{
						if (sel.isElementPresent(tobj.getLocator()) == exists) {					
							match= true;
							break;
						}
					}catch(RuntimeException rx){
						Log.debug("Selenium Runtime Exception IGNORED:", rx);
					}
				}
				long msn = System.currentTimeMillis();
				if (msn > ms1) break;
				delay(100);
				Log.info("..search for GUI: "+ windowName + ":" + compName +" looping... " + command + "... : msn: "+msn+", j: "+j);
				//clear appmapcache again to start fresh search
				tobj = (SGuiObject) localClearAppMapCache(windowName, compName);
			}

			String msg;
			//onguiexists...
			if (exists) {
				if (match) {
					msg = genericText.convert("found_timeout", compName +" was found within timeout "+ seconds,
												compName, seconds);
					msg += ". "+ genericText.convert("branching",  
						command +" attempting branch to "+ blockName +".", command, blockName);
					//set statuscode and statusinfo fields so driver will know to branch
					testRecordData.setStatusCode(StatusCodes.BRANCH_TO_BLOCKID);
					testRecordData.setStatusInfo(blockName);
				}
				else {
					//we were searching for gui, since it wasn't found, don't branch
					msg = genericText.convert("not_found_timeout", compName +" was not found within timeout "+ seconds,
												compName, seconds);
					msg += ". "+ genericText.convert("not_branching",  
						command +" did not branch to "+ blockName +".", command, blockName);
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				}
			}
			//onguinotexist...
			else {
				if (tobj == null) {
					//we were searching for no gui, since it wasn't found, branch
					msg = genericText.convert("not_found_timeout", compName +" was not found within timeout "+ seconds,
												compName, seconds);
					msg += ". "+ genericText.convert("branching",  
						command +" attempting branch to "+ blockName +".", command, blockName);
					//set statuscode and statusinfo fields so driver will know to branch
					testRecordData.setStatusCode(StatusCodes.BRANCH_TO_BLOCKID);
					testRecordData.setStatusInfo(blockName);	
				}
				else {
					//we were searching for no gui, since it was found, don't branch
					msg = genericText.convert("found_timeout", compName +" was found within timeout "+ seconds,
												compName, seconds);
					msg += ". "+ genericText.convert("not_branching",  
						command +" did not branch to "+ blockName +".", command, blockName);
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				}
			}
			log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);  
		}
		catch (Exception e) {
			//e.printStackTrace();
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			String msg = e.getMessage();
			if (msg.length()==0) msg = e.getClass().getName();
			standardFailureMessage(failedText.convert("failure1", 
								   "Unable to perform "+ command, command), 
								   "SAFSException:"+ msg);
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
