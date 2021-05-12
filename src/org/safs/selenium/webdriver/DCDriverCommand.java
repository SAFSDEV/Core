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
package org.safs.selenium.webdriver;
/**
 * Logs for developers, not published to API DOC.
 * History:<br>
 *   <br>   JUL 05, 2011    (Lei Wang) Update method setFocus().
 *   <br>   JAN 16, 2014    (DHARMESH) Update Start/Stop Browser call.
 *   <br>   FEB 02, 2014	(DHARMESH) Add Resize and Maximize WebBrowser window KW.
 *   <br>   APR 15, 2014    (DHARMESH) Added HighLight keyword
 *   <br>   MAY 21, 2014    (Carl Nagle) Refactored for better (custom) subclassing.
 *   <br>   AUG 29, 2014    (DHARMESH) Add selenium grid host and port support.
 *   <br>   SEP 10, 2014    (Carl Nagle) Fixed StartWebBrowser documentation and config() and System Properties support..
 *   <br>   NOV 26, 2014    (Lei Wang) Support setFocus(), waitForGuiGone().
 *   <br>   APR 08, 2015    (Lei Wang) Modify method startWebBrowser(): get property value of JVM Options for Remote Selenium Server.
 *   <br>   APR 16, 2015    (Carl Nagle) Modify method startWebBrowser to ignore the isRemote parameter, if provided.
 *   <br>   JUN 23, 2015	(Tao Xie) Add waitForPropertyValueStatus(): wait for property value match or gone with expected value.
 *   <br>   JUN 29, 2015	(Lei Wang) Modify startWebBrowser(): launch selenium server remotely.
 *                                    Add launchSeleniumServers(): start selenium standalone or grid automatically.
 *   <br>   JUL 24, 2015	(Lei Wang) Add sendHttpGetRequest(): handle keyword like GetURL, SaveURLToFile, VerifyURLContent, VerifyURLToFile.
 *   <br>   NOV 20, 2015	(Lei Wang) Use java AtomicBoolean to replace my AtomicReady class.
 *                                    Modify method sendHttpGetRequest(): set the thread (executing AJAX request) as daemon.
 *   <br>   DEC 24, 2015	(Lei Wang) Modify method sendHttpGetRequest(): check known issue 'ajax execution stuck with firefox'.
 *   <br>   MAR 31, 2016	(Lei Wang) Add onGUIGotoCommands(): implement OnGUIExistsGotoBlockID/OnGUINotExistGotoBlockID,
 *                                    I did nothing but set the BLOCKID to test-record's status-info.
 *   <br>   APR 07, 2016    (Lei Wang) Refactor to handle OnGUIExistsGotoBlockID/OnGUINotExistGotoBlockID in super class DriverCommand
 *   <br>   AUT 05, 2016    (Lei Wang) Modified waitForGui/waitForGuiGone: if the RC is SCRIPT_NOT_EXECUTED (4) then stop handling here.
 *   <br>   SEP 27, 2016    (Lei Wang) Moved methods launchSeleniumServers() to class WebDriverGUIUtilities.
 *   <br>   JUN 07, 2017    (Lei Wang) Handled keyword 'SwitchWindow'.
 *   <br>   JUN 27, 2017    (Lei Wang) Modified callScript(): Write each step execution information into SAFS Log.
 *   <br>   DEC 29, 2017    (Lei Wang) Modified callScript(): Put the information of each step (in sebuilder script) into variable store so that SAFS Monitor can get it.
 *   <br>   JAN 16, 2018    (Lei Wang) Modified callScript(): Get the "step's test record" from the variable store and retry it.
 *   <br>   JAN 18, 2018    (Lei Wang) Modified callScript(): When retry step: 1. set locator object (instead of string) to step.
 *                                                                            2. set stepSuccess to false if meet exception.
 *   <br>   JUN 17, 2019    (Lei Wang) Added SetNetworkConditions/DeleteNetworkConditions/GetNetworkConditions.
 *   <br>   OCT 17, 2019    (Lei Wang) Modified sendHttpGetRequest(): if AJAX request failed, try REST request and pure java HTTP request.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.safs.Constants.BrowserConstants;
import org.safs.DriverCommand;
import org.safs.IndependantLog;
import org.safs.JavaHook;
import org.safs.Log;
import org.safs.Processor;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordData;
import org.safs.Utils;
import org.safs.model.commands.DDDriverCommands;
import org.safs.model.commands.DDDriverFlowCommands;
import org.safs.net.HttpRequest;
import org.safs.net.IHttpRequest.HttpCommand;
import org.safs.net.IHttpRequest.Key;
import org.safs.rest.REST;
import org.safs.rest.service.Headers;
import org.safs.rest.service.Response;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.IgnoredStepException;
import org.safs.selenium.webdriver.lib.interpreter.WDStep;
import org.safs.selenium.webdriver.lib.interpreter.WDTestRun;
import org.safs.selenium.webdriver.lib.interpreter.WDTestRunFactory;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.text.FAILKEYS;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.text.GENKEYS;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.stringutils.StringUtilities;

import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;

public class DCDriverCommand extends DriverCommand {

	public static final String DEFAULT_BROWSER = SelectBrowser.BROWSER_NAME_FIREFOX;

	public static final int DEFAULT_BROWSER_TIMEOUT = 15;//in seconds

	public static final int DEFAULT_GET_URL_TIMEOUT = 120;//in seconds

	/**A convenient GUIUtilities*/
	protected WebDriverGUIUtilities wdgu = null;

	//STestRecordHelper testRecordData;
	public DCDriverCommand() {
		super();
	}

	/**
	 * Convert the general GUIUtilities to a specific one.
	 **/
	@Override
	protected void init() throws SAFSException{
		super.init();

		try{
			wdgu = (WebDriverGUIUtilities) utils;
		}catch(Exception e){
			String msg = " Met Exception "+StringUtils.debugmsg(e);
			IndependantLog.error(StringUtils.debugmsg(false)+msg);
			throw new SAFSException("Failed to convert GUIUtilities, "+msg);
		}
	}

	@Override
	protected void commandProcess() {
		String dbg = getClass().getName()+".commandProcess ";
    	Log.info(dbg+"processing: '"+ command+ "' with parameters: "+this.params);

		if(command.equalsIgnoreCase(DDDriverCommands.STARTWEBBROWSER_KEYWORD)){
			startWebBrowser();
		}else if(command.equalsIgnoreCase(DDDriverCommands.STOPWEBBROWSER_KEYWORD)){
			stopWebBrowser();
		}else if(command.equalsIgnoreCase(DDDriverCommands.USEWEBBROWSER_KEYWORD)){
			useWebBrowser();
		} else if(command.equalsIgnoreCase(DDDriverCommands.WAITFORGUI_KEYWORD)||
				  command.equalsIgnoreCase(DDDriverCommands.WAITFORWEBPAGE_KEYWORD)){
			waitForGui();
		}else if(command.equalsIgnoreCase(DDDriverCommands.WAITFORGUIGONE_KEYWORD)){
			waitForGuiGone();
		}else if(command.equalsIgnoreCase(DDDriverCommands.SETCONTEXT_KEYWORD) ||
				  command.equalsIgnoreCase(DDDriverCommands.SETFOCUS_KEYWORD)){
			setFocus();
		} else if(command.equalsIgnoreCase(DDDriverCommands.CLEARAPPMAPCACHE_KEYWORD)){
			clearAppMapCache();
		} else if(command.equalsIgnoreCase(DDDriverCommands.HIGHLIGHT_KEYWORD)){
			 highlight();
		} else if(command.equalsIgnoreCase(DDDriverFlowCommands.CALLSCRIPT_KEYWORD)){
			 callScript();
		} else if (command.equalsIgnoreCase(DDDriverCommands.WAITFORPROPERTYVALUE_KEYWORD)) {
			waitForPropertyValueStatus(true);
		} else if (command.equalsIgnoreCase(DDDriverCommands.WAITFORPROPERTYVALUEGONE_KEYWORD)) {
			waitForPropertyValueStatus(false);
		} else if (command.equalsIgnoreCase(DDDriverCommands.GETURL_KEYWORD) ||
				   command.equalsIgnoreCase(DDDriverCommands.SAVEURLTOFILE_KEYWORD ) ||
				   command.equalsIgnoreCase(DDDriverCommands.VERIFYURLCONTENT_KEYWORD) ||
				   command.equalsIgnoreCase(DDDriverCommands.VERIFYURLTOFILE_KEYWORD)) {
			sendHttpGetRequest();
		}else if(DDDriverCommands.SWITCHWINDOW_KEYWORD.equalsIgnoreCase(command)){
			switchWindow();
		}else if (DDDriverCommands.SETNETWORKCONDITIONS_KEYWORD.equalsIgnoreCase(command)) {
			setNetworkConditions();
		}else if (DDDriverCommands.GETNETWORKCONDITIONS_KEYWORD.equalsIgnoreCase(command)) {
			getNetworkConditions();
		}else if (DDDriverCommands.DELETENETWORKCONDITIONS_KEYWORD.equalsIgnoreCase(command)) {
			deleteNetworkConditions();
		}
	}

	/**
	 * params[0] the network conditions, in JSON format.<br>
	 */
	private void setNetworkConditions(){
		String debugmsg = StringUtils.debugmsg(false);

		Iterator<?> iterator = params.iterator();
		String networkConditions = "";
		try{
			networkConditions = (String) iterator.next();
			Log.info(debugmsg+" received network conditions: "+ networkConditions);
		}catch(Exception badvalue){
			Log.info(debugmsg+" Failed to receive network conditions, met "+ badvalue);
		}

		try {
			if(!WDLibrary.setNetworkConditions(networkConditions)){
				Log.error(debugmsg+" WDLibrary.setNetworkConditions returned false.");
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				issueUnknownErrorFailure("Failed to setNetworkConditions.");
				return;
			}
		}catch(Throwable th){
			Log.error(debugmsg, th);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			issueUnknownErrorFailure("Failed to setNetworkConditions.");
			return;
		}

		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(),
				genericText.convert(GENKEYS.SUCCESS_2,
						testRecordData.getCommand()+ " "+networkConditions +" successful.",
						testRecordData.getCommand(), networkConditions),
						GENERIC_MESSAGE);
	}

	/**
	 * params[0] the name of the variable to store network conditions, in JSON format.<br>
	 */
	private void getNetworkConditions(){
		String debugmsg = StringUtils.debugmsg(false);

		Iterator<?> iterator = params.iterator();
		String networkConditionsVar = null;
		try{
			networkConditionsVar = (String) iterator.next();
			Log.info(debugmsg+" received network conditions variable: "+ networkConditionsVar);
		}catch(Exception badvalue){
			issueParameterValueFailure("NetworkConditionsVar");
		}

		String networkConditions = null;

		try {
			networkConditions = String.valueOf(WDLibrary.getNetworkConditions());
			if (!setVariable(networkConditionsVar, String.valueOf(networkConditions))) {
				issueErrorPerformingAction(FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS, "Could not set one or more variable values."));
			}
		}catch(Throwable th){
			Log.error(debugmsg, th);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			issueUnknownErrorFailure("Failed to getNetworkConditions.");
			return;
		}

		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(),
				genericText.convert(GENKEYS.SUCCESS_3B,
						testRecordData.getCommand()+ " "+networkConditions +" successful using '"+networkConditionsVar+"'",
						testRecordData.getCommand(), networkConditions, networkConditionsVar),
				GENERIC_MESSAGE);
	}

	private void deleteNetworkConditions(){
		String debugmsg = StringUtils.debugmsg(false);

		try {
			if(!WDLibrary.deleteNetworkConditions()){
				Log.error(debugmsg+" WDLibrary.deleteNetworkConditions() returned false.");
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				issueUnknownErrorFailure("Failed to deleteNetworkConditions.");
				return;
			}
		}catch(Throwable th){
			Log.error(debugmsg, th);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			issueUnknownErrorFailure("Failed to deleteNetworkConditions.");
			return;
		}

		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(),
				genericText.convert(GENKEYS.SUCCESS_1,
						testRecordData.getCommand() +" successful.",
						testRecordData.getCommand()),
				GENERIC_MESSAGE);
	}

	protected void switchWindow(){
		testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
		if ( params.size() < 1 ) {
			this.issueParameterCountFailure();
			return;
		}
		final String debugmsg = StringUtils.debugmsg(false);

		String title = null;
		String browserID = null;
		int expectedMatchIndex = WDLibrary.MATCHED_ONE_TIME;
		boolean partialMatch = WDLibrary.DEFAULT_PARTIAL_MATCH;
		boolean ignoreCase = WDLibrary.DEFAULT_IGNORE_CASE;

		title = iterator.next();

		if(iterator.hasNext()){
			browserID = iterator.next();
		}
		if(iterator.hasNext()){
			try{
				expectedMatchIndex = Integer.parseInt(iterator.next());
			}catch(NumberFormatException e){
				IndependantLog.warn(debugmsg+"ignore failure for parameter 'expectedMatchIndex': "+e);
				expectedMatchIndex = WDLibrary.MATCHED_ONE_TIME;
			}
		}
		if(iterator.hasNext()){
			partialMatch = StringUtils.convertBool(iterator.next());
		}
		if(iterator.hasNext()){
			ignoreCase = StringUtils.convertBool(iterator.next());
		}

		try {
			boolean matched = false;

			if(StringUtils.isValid(browserID)){
				int matchedTime = WDLibrary.switchWindow(browserID, title, expectedMatchIndex, partialMatch, ignoreCase);
				matched = (matchedTime==expectedMatchIndex);
			}else{
				IndependantLog.debug(debugmsg+"browserID '"+browserID+"' is not valid, try to search window from the last used browser firstly, "
						+ "then from all opened browsers.");
				matched = WDLibrary.switchWindow(title, expectedMatchIndex, partialMatch, ignoreCase);
			}

			if(matched){
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				log.logMessage(testRecordData.getFac(),
						genericText.convert(GENKEYS.SUCCESS_2A,
								testRecordData.getCommand()+" successful using '"+title+"'",
								testRecordData.getCommand(), title),
								GENERIC_MESSAGE);
			}else{
				issueActionFailure("Failed to get window matching title '"+title+"'");
			}
		} catch (SeleniumPlusException e) {
			issueActionFailure(e.toString());
		}

	}

	public static final String SUFFIX_VARIABLE_READY_STATE 	= ".readyState";
	public static final String SUFFIX_VARIABLE_HEADERS 		= ".headers";
	public static final String SUFFIX_VARIABLE_STATUS 		= ".status";
	public static final String SUFFIX_VARIABLE_STATUS_TEXT 	= ".statusText";
	public static final String SUFFIX_VARIABLE_XML 			= ".xml";

	protected void sendHttpGetRequest(){
		testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
		if ( params.size() < 2 ) {
			this.issueParameterCountFailure();
			return;
		}
		final String debugmsg = StringUtils.debugmsg(false);

		//Check the known issue with selenium-standalone2.47.1 and Firefox 42.0
		//It seems that stuck happen with previous firefox too :-(
		try {
			WDLibrary.checkKnownIssue(command);
		} catch (SeleniumPlusException e) {
			testRecordData.setStatusCode( StatusCodes.SCRIPT_NOT_EXECUTED );
			log.logMessage(testRecordData.getFac(), command+" NOT executed." , e.getMessage(), WARNING_MESSAGE);
			return;
		}

		final String url = iterator.next();//The first is URL
		IndependantLog.debug(debugmsg+" parameters: url="+url);
		//Second parameter:
		//'variable name' for GetURL, 'string content' for VerifyURLContent, 'test file' for SaveURLToFile, and 'bench file' for VerifyURLToFile
		String secondParam = iterator.next();
		IndependantLog.debug(debugmsg+" parameters: variable/content/test file/bench file="+secondParam);
		File fn = null;//for SavaURLToFile and VerifyURLToFile
		try{
			if(DDDriverCommands.SAVEURLTOFILE_KEYWORD.equalsIgnoreCase(command)) fn = deduceTestFile(secondParam);
			else if(DDDriverCommands.VERIFYURLTOFILE_KEYWORD.equalsIgnoreCase(command)) fn = deduceBenchFile(secondParam);
			if(fn!=null) IndependantLog.debug(debugmsg+" deduced file="+fn.getAbsolutePath());
		}catch(SAFSException e){
			issueParameterValueFailure("Test File or Bench File "+e.getMessage());
			return;
		}

		//get optional parameters, first is timeout
		int timeout = DEFAULT_GET_URL_TIMEOUT;
		try{
			if(iterator.hasNext()){
				timeout = Integer.parseInt(iterator.next());
			}
		}catch(NumberFormatException e){
			IndependantLog.warn(debugmsg+"Parameter timeout is not valid. "+e.getMessage());
			timeout = DEFAULT_GET_URL_TIMEOUT;
		}
		//get other optional parameters, pairs of (headerName, headerValue)
		final Map<String, String> headers = new HashMap<String, String>();
		String headerName = null;
		String headerValue = null;
		while(iterator.hasNext()){
			headerName = iterator.next();
			if(iterator.hasNext()){
				headerValue = iterator.next();
				headers.put(headerName, headerValue);
			}else{
				IndependantLog.warn(debugmsg+" parameter pairs (headerName, headerValue) are not pair for header '"+headerName+"'!");
			}
		}

		final Map<String, Object> resultMap = new HashMap<String, Object>();
		final AtomicBoolean resultReady = new AtomicBoolean(false);
		try {
			Thread threadGetUrl = new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						Map<String, Object> results = WDLibrary.AJAX.getURL(url, headers);
						for(String key:results.keySet()){
							resultMap.put(key, results.get(key));
						}
						resultReady.set(true);
					} catch (Throwable e) {
						IndependantLog.warn(debugmsg+"Thread AJAX.getURL() failed, Met "+StringUtils.debugmsg(e));
						try {
							String serviceId = "service"+System.currentTimeMillis();
							REST.StartServiceSession(serviceId, url);
							Response resp = REST.request(serviceId, HttpCommand.GET.name(), "", Headers.convertHeadersMapToMultiLineString(headers), "");

							resultMap.put(Key.RESPONSE_HEADERS.value(), resp.get_headers());
							resultMap.put(Key.RESPONSE_STATUS.value(), resp.get_status_code());
							resultMap.put(Key.RESPONSE_STATUS_TEXT.value(), resp.get_status_line());
							resultMap.put(Key.RESPONSE_TEXT.value(), resp.get_entity_body());

							resultReady.set(true);
							REST.EndServiceSession(serviceId);

						} catch (Exception e1) {
							IndependantLog.warn(debugmsg+"Thread REST.execute GET failed, Met "+StringUtils.debugmsg(e));

							try {
								HttpRequest request = new HttpRequest();
								request.setIncludeResponseHeaders(true);
								Map<String, Object> results = request.execute(HttpCommand.GET, url, true, headers, "");
								for(String key:results.keySet()){
									resultMap.put(key, results.get(key));
								}
								resultReady.set(true);
							} catch (SAFSException e2) {
								IndependantLog.debug(debugmsg+"Thread http.proxyHost="+System.getProperty("http.proxyHost"));
								IndependantLog.debug(debugmsg+"Thread http.proxyPort="+System.getProperty("http.proxyPort"));
								IndependantLog.error(debugmsg+"Thread HR.execute GET Thread: Met "+StringUtils.debugmsg(e));
							}

						}
					}
				}
			});
			//Set as daemon, we don't want this thread to block the termination of the main thread.
			threadGetUrl.setDaemon(true);
			threadGetUrl.start();
			//Wait for the sub thread to terminate
			threadGetUrl.join(timeout*1000);
			if(!resultReady.get()){
				throw new SeleniumPlusException("Cannot get result ready from url '"+url+"'");
			}

			String content = String.valueOf(resultMap.get(Key.RESPONSE_TEXT.value()));
			IndependantLog.debug(debugmsg+" http response\n"+content);

			if(DDDriverCommands.GETURL_KEYWORD.equalsIgnoreCase(command)){
				//second parameter is the variable to store the url's content
				String var = secondParam;
				String varState = secondParam+SUFFIX_VARIABLE_READY_STATE;
				String varStatus = secondParam+SUFFIX_VARIABLE_STATUS;
				String varStatusTxt = secondParam+SUFFIX_VARIABLE_STATUS_TEXT;
				String varHeaders = secondParam+SUFFIX_VARIABLE_HEADERS;
				String varXml = secondParam+SUFFIX_VARIABLE_XML;
				setVariable(var, content);
				setVariable(varState, String.valueOf(resultMap.get(Key.READY_STATE.value())));
				setVariable(varStatus, String.valueOf(resultMap.get(Key.RESPONSE_STATUS.value())));
				setVariable(varStatusTxt, String.valueOf(resultMap.get(Key.RESPONSE_STATUS_TEXT.value())));
				setVariable(varHeaders, String.valueOf(resultMap.get(Key.RESPONSE_HEADERS.value())));
				setVariable(varXml, String.valueOf(resultMap.get(Key.RESPONSE_XML.value())));

				String temp = "Requesting URL '"+url+"', content/readyState/Status/StatusText/ResponseHeaders/contentXML";
				String value = " variable '"+var+"'/'"+varState+"'/'"+varStatus+"'/'"+varStatusTxt+"'/'"+varHeaders+"'/'"+varXml+"'";
				issuePassedSuccess(GENStrings.convert(GENStrings.BE_SAVED_TO,
						temp+" has been saved to "+value+"",
						temp, value));

			}else if(DDDriverCommands.SAVEURLTOFILE_KEYWORD.equalsIgnoreCase(command)){
				FileUtilities.writeStringToUTF8File(fn.getAbsolutePath(), content);
				issuePassedSuccess(GENStrings.convert(GENStrings.BE_SAVED_TO,
						"The contet of URL '"+url+"' has been saved to '"+fn.getAbsolutePath()+"'",
						"The contet of URL '"+url+"'", fn.getAbsolutePath()));

			}else if(DDDriverCommands.VERIFYURLCONTENT_KEYWORD.equalsIgnoreCase(command)){
				//second parameter is the url's content to compare with for verification.
				if(content.equals(secondParam)){
					issuePassedSuccess(GENStrings.convert(GENStrings.CONTENT_MATCHES_KEY,
							"the content of '"+url+"' matches the content of '"+secondParam+"'",
							url, secondParam));
				}else{
					String detail = GENStrings.convert(GENStrings.CONTENT_NOT_MATCHES_KEY,
							"the content of '"+ url +"' does not match the content of '"+secondParam+"'",
							url, "'"+secondParam+"'");
					issueActionUsingNegativeMessage(command, detail);
				}

			}else if(DDDriverCommands.VERIFYURLTOFILE_KEYWORD.equalsIgnoreCase(command)){
				//verify with bench file
				String benchContent = FileUtilities.readStringFromUTF8File(fn.getAbsolutePath());
				if(content.equals(benchContent)){
					issuePassedSuccess(GENStrings.convert(GENStrings.CONTENT_MATCHES_KEY,
							"the content of '"+url+"' matches the content of '"+fn.getCanonicalPath()+"'",
							url, fn.getCanonicalPath()));
				}else{
					String detail = GENStrings.convert(GENStrings.CONTENT_NOT_MATCHES_KEY,
							"the content of '"+ url +"' does not match the content of '"+fn.getCanonicalPath()+"'",
							url, fn.getCanonicalPath());
					issueActionUsingNegativeMessage(command, detail);
				}

			}else{
				IndependantLog.warn(debugmsg+"action '"+command+"' should not be executed here.");
				testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
				return;
			}

			//success!  set status to ok
			testRecordData.setStatusCode(StatusCodes.OK);
		}
		catch (Exception e) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			this.issueErrorPerformingAction(StringUtils.debugmsg(e));
		}
	}

	private void putStepInVariableStore(String recordInstanceName, Step step, String scriptFile){
		//Save the step record into variable 'inputrecord' so that 'SAFS Monitor' will see it.
		try {
			setVariable(recordInstanceName + STAFHelper.SAFS_VAR_INPUTRECORD, step.toJSON().toString());
			setVariable(recordInstanceName + STAFHelper.SAFS_VAR_FILENAME, scriptFile);
		} catch (SAFSException e) {
			IndependantLog.error("Met "+e.toString());
		}
	}

	/**
	 * Hold the original test-record information of command 'CallScript' when executing each step in sebuilder script.
	 */
	private Map<String, String> testRecordVariables = new HashMap<String, String>();

	@Override
	public void pushTestRecord(TestRecordData trd) {
		super.pushTestRecord(trd);

		String debugmsg = StringUtils.debugmsg(false);
		String recordInstanceName = trd.getInstanceName();
		String record = recordInstanceName + STAFHelper.SAFS_VAR_INPUTRECORD;
		String filename = recordInstanceName + STAFHelper.SAFS_VAR_FILENAME;

		try {
			testRecordVariables.put(record, getVariable(record));
			testRecordVariables.put(filename, getVariable(filename));
		} catch (SAFSException e) {
			IndependantLog.warn(debugmsg+"Met "+e.toString());
		}
	}

	@Override
	public TestRecordData popTestRecord() {
		String debugmsg = StringUtils.debugmsg(false);

		for(String key:testRecordVariables.keySet()){
			try {
				setVariable(key, testRecordVariables.get(key));
			} catch (SAFSException e) {
				IndependantLog.warn(debugmsg+"Met "+e.toString());
			}
		}

		return super.popTestRecord();
	}

	private void callScript(){
		String debugmsg = "DCDriverCommand.callScript ";
		if (params.size() < 1) {
			issueParameterCountFailure();
			return;
		}
		Iterator iterator = params.iterator();
		String path = (String) iterator.next();
		CaseInsensitiveFile file = new CaseInsensitiveFile(path);
		if (file.isAbsolute()&& (!file.exists()||!file.canRead())){
			issueFileErrorFailure(path);
			return;
		}
		File actual = file.toFile();
		if(!file.isAbsolute()){
			try{
				actual = deduceProjectFile(path);
				if (!actual.isAbsolute() || !actual.exists()||!actual.canRead()){
					issueFileErrorFailure(path);
					return;
				}
			}catch(Exception x){
				issueFileErrorFailure(path);
				return;
			}
		}
		// actual should now be an absolute file path
		String scriptFile = actual.getName();
		String record = null;
		try{
			//Push the test-record-data into a stack, before executing the scripts
			pushTestRecord(testRecordData);

			// load the Script and prepare to run it Step by Step
			Script script = WDLibrary.getSeleniumBuilderScript(actual.getAbsolutePath());
			// How does a script explicitly say it does or does NOT want to close the driver?
			// Scripts seem to be set to closeDriver=true by default.
			script.closeDriver = false;
			script.testRunFactory = new WDTestRunFactory();
			WebDriverFactory factory = WDLibrary.getWebDriverAsWebDriverFactory();
			WDTestRun test = (WDTestRun)script.start((org.apache.commons.logging.Log) log,
					                    factory,
					                    null,
					                    new HashMap<String, String>());
			boolean finished = false;
			boolean success = true;
			//boolean shutdownHook = false;
			boolean retryStep = false;
			Step step = null;
			String driverStatus = null;
			boolean stepping = false;
			boolean stepSuccess = false;
			List<String> errorlist = new ArrayList<String>();
			int stepnumber = 0;

			String beginMessage = testRecordData.getCommand()+" "+actual.getAbsolutePath()+" is processing ... ";
			log.logMessage(testRecordData.getFac(), beginMessage, GENERIC_MESSAGE);

			String recordInstanceName = testRecordData.getInstanceName();

mainloop:	while (!finished ){
				Log.suspend();
				try{ driverStatus = getVariable(DriverInterface.DRIVER_CONTROL_VAR);}
				catch(Exception any){ driverStatus = JavaHook.RUNNING_EXECUTION; }
				stepping = driverStatus.equalsIgnoreCase(JavaHook.RUNNING_EXECUTION);

holdloop:		while(! driverStatus.equalsIgnoreCase(JavaHook.RUNNING_EXECUTION)){
					// PAUSE
					if (driverStatus.equalsIgnoreCase(JavaHook.PAUSE_EXECUTION)){
						//check every 100 millis
						try{ Thread.sleep(100);}catch(Exception x){;}
					// STEPPING
					}else if (driverStatus.equalsIgnoreCase(JavaHook.STEPPING_EXECUTION)){
						setVariable(DriverInterface.DRIVER_CONTROL_VAR, JavaHook.PAUSE_EXECUTION);
					// STEP
					}else if (driverStatus.equalsIgnoreCase(JavaHook.STEP_EXECUTION)){
						setVariable(DriverInterface.DRIVER_CONTROL_VAR, JavaHook.STEPPING_EXECUTION);
						break holdloop;
					// SHUTDOWN
					}else if (driverStatus.equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)){
						Log.resume();
						Log.info(debugmsg+" processing USER SHUTDOWN REQUEST...");
						success = false;
						stepping = false;
						break mainloop;
					// STEP_RETRY_EXECUTION
					}else if (driverStatus.equalsIgnoreCase(JavaHook.STEP_RETRY_EXECUTION)){
						setVariable(DriverInterface.DRIVER_CONTROL_VAR, JavaHook.STEPPING_RETRY_EXECUTION);
						retryStep = true;
						break holdloop;
					// STEPPING_RETRY_EXECUTION
					}else if (driverStatus.equalsIgnoreCase(JavaHook.STEPPING_RETRY_EXECUTION)){
						retryStep = false;
						setVariable(DriverInterface.DRIVER_CONTROL_VAR, JavaHook.PAUSE_EXECUTION);
					}else{
						Log.resume();
						Log.info(debugmsg+" unknown or invalid SAFS_DRIVER_CONTROL status. ReSet to RUNNING!");
						Log.suspend();
						setVariable(DriverInterface.DRIVER_CONTROL_VAR, JavaHook.RUNNING_EXECUTION);
						stepping = false;
						break holdloop;
					}
					try{ driverStatus = getVariable(DriverInterface.DRIVER_CONTROL_VAR);}
					catch(Exception any){ driverStatus = JavaHook.RUNNING_EXECUTION; }
					stepping = driverStatus.equalsIgnoreCase(JavaHook.RUNNING_EXECUTION);
				}// end of holdloop:
				Log.resume();
				// continue in mainloop:
				stepSuccess = true;
				if(!retryStep || step == null){
					step = test.popNext();

					if(step == null){
						finished = true;
						test.cleanup();
						break;
					}else{
						stepnumber++;
						record = "Step #"+ stepnumber +": "+ step.toPrettyString()+".";
						//Save the step record into variable 'inputrecord' so that 'SAFS Monitor' will see it.
						putStepInVariableStore(recordInstanceName, step, scriptFile);

						try{
							stepSuccess = test.runStep(step);
							if(!stepSuccess) success = false;// set and keep the false setting
						}catch(IgnoredStepException e){
							IndependantLog.debug(debugmsg+e.toString());
						}
					}
				}else{ // EXPERIMENTAL: retry last Step
					try{
						String sebuilderStep = getVariable(recordInstanceName + STAFHelper.SAFS_VAR_INPUTRECORD);
						Map<String, Object> sebuilderStepParams = Utils.fromJsonString(sebuilderStep, Map.class);
						Set<String> keys = sebuilderStepParams.keySet();
						Object value = null;
						for(String key:keys){
							if(!WDStep.isUnModifiableParameter(key)){
								value = sebuilderStepParams.get(key);

								if(step.stringParams.containsKey(key)){
									if(!value.equals(step.stringParams.get(key))){
										step.stringParams.put(key, value.toString());
									}
								}
								else if(step.locatorParams.containsKey(key)){
									if(!value.equals(step.locatorParams.get(key))){
										SRUtilities.setLocatorParam(step, value, key);
									}
								}
								else{
									IndependantLog.debug(debugmsg+" missing change parameter '"+key+"' to value '"+value+"'.");
								}
							}
						}

						record = "RETRY Step #"+ stepnumber +": "+ step.toPrettyString()+".";
						stepSuccess = test.runStep(step);
						if(!stepSuccess) success = false;
					}catch(Exception x){
						// what do we want to do when a retry blows up?
						// currently, we are going to DebugLog it and let the execution/stepping continue.
						Log.debug(debugmsg+" ignoring Step retry "+ x.getClass().getSimpleName()+", "+ x.getMessage(), x);
						//we should set the stepSuccess to false
						stepSuccess = false;
					}
				}
				retryStep = false; // insure reset

				if(!stepSuccess){
					// do we want to set finished = true?
					// or does the Script/TestRun automatically do that for us?
					errorlist.add(record+" was not successful!");
					log.logMessage(testRecordData.getFac(), record , FAILED_MESSAGE);
				}else{
					log.logMessage(testRecordData.getFac(), record , PASSED_MESSAGE);
				}

			}// end of mainloop:
			Log.resume();

			//Pop the 'test-record-data' from the stack after 'scripts execution', and restore the 'test-record-data' if the
			//'script execution' changed the shared class field 'test-record-data'.
			popTestRecord();

			if(success) {
				issueGenericSuccessUsing(scriptFile, null);
				return;
			}else{
				StringBuilder detail = new StringBuilder("-------------------  failed steps ------------------------\n");
				for(String line:errorlist) detail.append(line);
				detail.append("\n--------------------------------------------------------");
				issueActionUsingNegativeMessage(scriptFile, detail.toString());
				return;
			}
		}catch(Throwable t){
			Log.resume();
			String p2 = "Test record "+(record==null?"":record)+" failed, due to "+t.getClass().getSimpleName()+": "+ t.getMessage();
			Log.debug(p2, t);
			issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SCRIPT_ERROR, "Script '"+ scriptFile+"' error: "+p2, scriptFile, p2));
		}
	}

	private void clearAppMapCache(){
		//localClearAppMapCache(null, null);
		String msg = "";
		try{
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			wdgu.clearAllAppMapCaches();
			msg = genericText.convert("success2",
			"Selenium "+ testRecordData.getCommand() +" successful.",
			"Selenium", testRecordData.getCommand());
		}
		catch(Exception x){;}
		log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);
	}


	/**
	 * params[0] url<br>
	 * params[1] browser id (default {@link #DEFAULT_BROWSER})<br>
	 * params[2] browser name (default {@link #DEFAULT_BROWSER})<br>
	 * params[3] timeout (default 30) in seconds.<br>
	 * params[4] true/false isRemoteBrowser (ignored -- treated as always true)<br>
	 * <br>
	 * Following parameters indicate the extra parameters, they should be given by pair(key, value)<br>
	 * The key can be one of:<br>
	 * {@link BrowserConstants#getExtraParameterKeys()}<br>
	 * <br>
	 * params[5] extra parameter key1<br>
	 * params[6] extra parameter value for key1<br>
	 * <br>
	 * params[7] extra parameter key2<br>
	 * params[8] extra parameter value for key2<br>
	 * <br>
	 * params[9] extra parameter key3<br>
	 * params[10] extra parameter value for key3<br>
	 * ...
	 */
	private void startWebBrowser(){
		if(params.size() < 1){
			issueParameterCountFailure();
			return;
		}
		Iterator iterator = params.iterator();
		String url = (String) iterator.next();                  // params[0]
		String debugmsg = StringUtils.debugmsg(false);
		Log.info(debugmsg+" received URL: "+ url);

		String id = DEFAULT_BROWSER;
		try{
			id = (String) iterator.next();                      // params[1]
		}catch(Exception badvalue){
			Log.info(debugmsg+" browser id may not be passed via parameters.  Using Default.");
		}
		Log.info(debugmsg+" using browser id: "+ id);

		String browser = DEFAULT_BROWSER;
		try{
			//First, try to get the browser name from the system properties
			String temp = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_BROWSER_NAME);
			if(temp!=null && !temp.isEmpty()) browser = temp;
			//Second, try to get the browser name from the parameter

			temp = (String) iterator.next();                     // params[2]

			if(temp!=null && !temp.trim().isEmpty()) browser = temp;
		}catch(Exception badvalue){	}
		Log.info(debugmsg+" using browser name: "+ browser);

		int timeout = Processor.getSecsWaitForComponent(); //default timeout in seconds
		try{
			timeout = Integer.parseInt((String)iterator.next()); // params[3]
		}catch(Exception badvalue){	}
		Log.info(debugmsg+" using timeout: "+ String.valueOf(timeout));

		boolean isRemoteBrowser = true;

		// we will support a SYSTEM property that overrides the ALWAYS true concept (just in case)
		try{
			//First, try to get the browser-remote from the system properties.
			//This can be set on the command-line or in the [SAFS_SELENIUM] INI config file.
			String temp = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_BROWSER_REMOTE);
			if(temp!=null && !temp.isEmpty()) {
				Log.info(debugmsg+" SYSTEM PROPERTY for isRemoteBrowser: "+ isRemoteBrowser);
				isRemoteBrowser = StringUtilities.convertBool(temp);
			}

			// Get the ignored browser-remote from the parameter, if present.
			// must READ the value to get it out of the iterator, but 'false' will be ignored
			temp = (String) iterator.next();
			if(temp!=null && !temp.isEmpty() && StringUtilities.convertBool(temp)) {
				Log.info(debugmsg+" parameter for isRemoteBrowser set 'true'.");
				isRemoteBrowser = true;
			}else{
				Log.info(debugmsg+" parameter for isRemoteBrowser missing or 'false' and will be ignored.");
			}

		}catch(Exception badvalue){	}
		Log.info(debugmsg+" using browser isRemoteBrowser: "+ isRemoteBrowser);

		//Handle the extra parameters, appear as pair(key, value)
		String key = null;
		Object value = null;
		Map<String,Object> extraParameters = new HashMap<String,Object>();
		while(iterator.hasNext()){                               // params[5] +
			try{
				key = (String) iterator.next();
				Log.info(debugmsg+" received extra parameter's key: "+ key);
				value = iterator.next();
				Log.info(debugmsg+" received extra parameter's value: "+ value);
				extraParameters.put(key, value);
			}catch(Exception badvalue){
				Log.info(debugmsg+" received extra parameter: ", badvalue);
			}
		}

		//if seleniumnode has been provided, we are going to launch grid-hub and grid-node, not standalone server.
		String nodesInfo = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_NODE);
		Log.info(debugmsg+" using selenium nodes: "+ nodesInfo);
		boolean isGrid = StringUtils.isValid(nodesInfo);
		if(isGrid) extraParameters.put(SelectBrowser.KEY_GRID_NODES_SETTING, nodesInfo);

		try {
			WDLibrary.startBrowser(browser, url, id, timeout, isRemoteBrowser, extraParameters);
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			log.logMessage(testRecordData.getFac(),
					genericText.convert(GENKEYS.SUCCESS_3A,
							testRecordData.getCommand()+":"+ id +" "+ url +" successful using "+ browser,
							testRecordData.getCommand(), id, url, browser),
					GENERIC_MESSAGE);
			return;
		}
		catch(Throwable th){
			String errorMsg = th.getMessage();
			IndependantLog.warn(debugmsg+" Fail due to: "+errorMsg);
			try{
				if(isRemoteBrowser){
					WebDriverGUIUtilities.launchSeleniumServers();
					WDLibrary.startBrowser(browser, url, id, timeout, isRemoteBrowser, extraParameters);
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					log.logMessage(testRecordData.getFac(),
							genericText.convert(GENKEYS.SUCCESS_3A,
									testRecordData.getCommand()+":"+ id +" "+ url +" successful using "+ browser,
									testRecordData.getCommand(), id, url, browser),
									GENERIC_MESSAGE);
					return;
				}
			}catch(SeleniumPlusException se){
				errorMsg = StringUtils.debugmsg(se);
			}

			Log.error(debugmsg+" Fail due to "+errorMsg);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			issueUnknownErrorFailure(errorMsg);
		}
	}

	/**
	 * params[0] browser id (default {@link #DEFAULT_BROWSER_ID})<br>
	 */
	private void useWebBrowser(){
		String debugmsg = StringUtils.debugmsg(this.getClass(), "useWebBrowser");
		Iterator iterator = params.iterator();
		String id = "";
		try{
			id = (String) iterator.next();
			Log.info(debugmsg+"received browser id: "+ id);
		}catch(Exception badvalue){
			Log.info(debugmsg+"received browser id: "+ id);
		}

		try {
			WDLibrary.useBrowser(id);
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			log.logMessage(testRecordData.getFac(),
					genericText.convert(GENKEYS.SUCCESS_2,
							testRecordData.getCommand()+ id +" successful.",
							testRecordData.getCommand(), id),
							GENERIC_MESSAGE);
			return;
		}
		catch(Throwable th){
			String thmsg = "WebDriver swithcing error.";
			Log.error(debugmsg+thmsg, th);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			issueUnknownErrorFailure(thmsg);
		}
	}

	/**
	 * params[0] browser id (default {@link #DEFAULT_BROWSER_ID})<br>
	 */
	private void stopWebBrowser(){
		String debugmsg = StringUtils.debugmsg(this.getClass(), "stopWebBrowser");

		Iterator iterator = params.iterator();
		String id = "";
		try{
			id = (String) iterator.next();
			Log.info(debugmsg+"received browser id: "+ id);
		}catch(Exception badvalue){
			Log.info(debugmsg+"received browser id: "+ id);
		}

		try {
			WDLibrary.stopBrowser(id);
		}
		catch(Throwable th){
			String thmsg = "WebDriver stopping error: "+ th.getMessage();
			Log.error(debugmsg+thmsg, th);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			issueUnknownErrorFailure(thmsg);
			return;
		}

		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(),
				genericText.convert(GENKEYS.SUCCESS_2,
						testRecordData.getCommand()+ id +" successful.",
						testRecordData.getCommand(), id),
						GENERIC_MESSAGE);
	}

	/**
	 * Turn the highlight-switch on/off.
	 * If the highlight-switch is turned on, the test component will be highlighted during runtime.
	 */
	private void highlight(){
		if (params.size() < 1) {
			issueParameterCountFailure();
			return;
		}

		WebDriverGUIUtilities.HIGHLIGHT = StringUtilities.convertBool(params.iterator().next());

		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(),
				genericText.convert(GENKEYS.SUCCESS_2,
						testRecordData.getCommand()+ " successful.",
						testRecordData.getCommand()),
						GENERIC_MESSAGE);
	}

	/** <br><em>Purpose:</em> waitForGui
	 **/
	private void waitForGui () {
		if (params.size() < 2) {
			issueParameterCountFailure();
			return;
		}
		Iterator<?> iterator = params.iterator();
		final String DEFAULT_SECONDS_STR = "15";
		final String DEFAULT_WEBPAGE_STR = "30";
		boolean isWeb = testRecordData.getCommand().equalsIgnoreCase(DDDriverCommands.WAITFORWEBPAGE_KEYWORD);
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
			Log.debug(command +" optional parameter '"+ "TIMEOUT" +"' set to '"+ seconds +"'.");
		}catch (Exception e) {
			Log.warn(command+" optional parameter timeout '"+seconds+"' is not valid: "+e.getMessage()+". Use the default timeout "+DEFAULT_TIMEOUT);
			seconds = DEFAULT_TIMEOUT;
			secii = Integer.parseInt(seconds);
		}
		if (secii < 0) secii = 0;
		Log.info("............................."+command+": window:"+windowName+", component:"+compName+", seconds:"+seconds);
		String msg = "";
		try {
			// wait for the window/component
			int status = wdgu.waitForObject(testRecordData.getAppMapName(),windowName, compName, secii);
			//if it cannot be found within timeout
			if (status == 0) {
				WebElement winObject = ((WDTestRecordHelper) testRecordData).getWindowTestObject();
				WebElement compObject = ((WDTestRecordHelper) testRecordData).getCompTestObject();
				WebDriverGUIUtilities.highlightThenClear((compObject==null? winObject:compObject), 1000);
			}else if(status==StatusCodes.SCRIPT_NOT_EXECUTED){
				testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
				Log.debug(command+" was not handled by Selenium WebDriver. It will be handled by other engine later.");
				return;
			}else{
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
			//se.printStackTrace();
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			msg = failedText.convert("not_found_timeout",
							compName +" was not found within timeout "+ seconds,
							compName, seconds);
			/**
			String semsg = se.getMessage();
			if (semsg.length()==0) semsg = se.getClass().getName();
			standardFailureMessage(msg, "SAFSException:"+ semsg);
			*/
			standardFailureMessage(msg, "");
		}
	}

	@Override
	protected boolean checkGUIExistence(boolean expectedExist, String mapNam, String window, String component, int timeoutInSeconds) throws SAFSException{
		  boolean exist;

		  if(expectedExist){//Expect the component to be present
			  exist = false;
			  try{ exist = (wdgu.waitForObject(mapNam, window, component, timeoutInSeconds)==0);}
			  catch(SAFSObjectNotFoundException sonf){ /*ignore*/}
		  }
		  else{//Expect the component to be not present
			  exist = true;
			  long endTime = System.currentTimeMillis()+timeoutInSeconds*1000;
			  while(exist && (System.currentTimeMillis()<endTime)){
				  try{ exist = (wdgu.waitForObject(mapNam, window, component, 0)==0);}
				  catch(SAFSObjectNotFoundException sonf){ exist=false; }
			  }
		  }

		  //if it exists, then highlight it and clear the highlight
		  if (exist){
			  WebElement winObject = ((WDTestRecordHelper) testRecordData).getWindowTestObject();
			  WebElement compObject = ((WDTestRecordHelper) testRecordData).getCompTestObject();
			  WebDriverGUIUtilities.highlightThenClear((compObject==null? winObject:compObject), 1000);
		  }
		  return (expectedExist==exist);
	}

	private void waitForGuiGone(){
		if (params.size() < 2) {
			issueParameterCountFailure();
			return;
		}
		Log.info(".............................params= "+params);
		Iterator<?> iterator = params.iterator();
		String DEFAULT_TIMEOUT = "15";

		// get the window, comp
		String windowName = (String) iterator.next();
		String compName = (String) iterator.next();
		String seconds = iterator.hasNext()? (String)iterator.next():DEFAULT_TIMEOUT;
		int timeoutInMillis = 0;
		try { // optional param
			timeoutInMillis = Integer.parseInt(seconds)*1000;
		}catch (Exception e) {
			seconds = DEFAULT_TIMEOUT;
			timeoutInMillis = Integer.parseInt(seconds)*1000;
		}
		if (timeoutInMillis < 0) timeoutInMillis = 0;

		Log.info("............................. timeoutInMillis="+timeoutInMillis);

		String winCompName = windowName+":"+ compName;
		int status = 0;
		boolean exist = false;
		boolean didExist = false;
		boolean isDisplayed = false;
		long endTime = System.currentTimeMillis()+timeoutInMillis;
		try {
			//wait for the GUI to show up: didExist = true
			while(!didExist){
				try{
					status = wdgu.waitForObject(testRecordData.getAppMapName(),windowName, compName, 0);

					if(status==StatusCodes.SCRIPT_NOT_EXECUTED){
						testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
						Log.debug(command+" was not handled by Selenium WebDriver. It will be handled by other engine later.");
						return;
					}
					exist = (status==0);

					if(exist){
						try{
							WebElement e = ((WDTestRecordHelper) wdgu.getTestRecordData()).getCompTestObject();
							try{
								WebDriverGUIUtilities.highlightThenClear(e, 1000);
								isDisplayed = WDLibrary.isDisplayed(e);
								Log.info("............................. "+winCompName +" is Displayed = "+ isDisplayed);
							}
							catch(SeleniumPlusException stale){
								exist = false;
								didExist = true;
							}
						}catch(Exception fallback){
							Log.debug("..........."+ fallback.getClass().getName()+", "+ fallback.getMessage());
							Log.info("............................. skipping initial visibility check.");
						}
					}
				}
				catch(SAFSObjectNotFoundException ignore){}
				if(exist)
					didExist = true; // set once to show we did see it at least once.
				if(!didExist){
					if( System.currentTimeMillis() > endTime) break;
					// give the app some computing time to play with
				    try{Thread.sleep(20);}catch(InterruptedException x){}
				}
			}

			// now wait for it to go away.
			while(exist && System.currentTimeMillis() < endTime){
				// give the app some computing time to play with
				try{Thread.sleep(20);}catch(InterruptedException x){}
				try{
					exist = (wdgu.waitForObject(testRecordData.getAppMapName(),windowName, compName, 0)==0);
					if(exist){
						try{
							WebElement e = ((WDTestRecordHelper) wdgu.getTestRecordData()).getCompTestObject();
							try{
								exist = WDLibrary.isDisplayed(e);
								Log.info("............................. "+winCompName +" is still Displayed = "+ exist);
							}
							catch(SeleniumPlusException stale){
								exist = false;
							}
						}catch(Exception fallback){
							Log.debug("..........."+ fallback.getClass().getName()+", "+ fallback.getMessage());
							Log.info("............................. skipping final visibility check.");
						}
					}
				}
				catch(SAFSObjectNotFoundException nf){
					exist = false;
				}
			}

			//if it is not gone within timeout
			if (exist) {
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				String msg = failedText.convert(FAILKEYS.NOT_GONE_TIMEOUT,
						winCompName +" was not gone within timeout "+ seconds,
						winCompName, seconds);
				standardFailureMessage(msg, testRecordData.getInputRecord());
				return;
			}
			if(didExist){
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				log.logMessage(testRecordData.getFac(),
						genericText.convert(GENKEYS.GONE_TIMEOUT,
								winCompName +" was gone within timeout "+ seconds,
								winCompName, seconds),
								GENERIC_MESSAGE);
			}else{
				// log a warning that we never saw it.  Recognition could be bad.
				testRecordData.setStatusCode(StatusCodes.SCRIPT_WARNING);
				log.logMessage(testRecordData.getFac(),
						failedText.convert(FAILKEYS.NOT_FOUND_TIMEOUT,
								winCompName +" was not found in timeout "+ seconds,
								winCompName, seconds),
								WARNING_MESSAGE);
			}
		}catch(SAFSObjectNotFoundException se) {
			if(didExist){
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				log.logMessage(testRecordData.getFac(),
						genericText.convert(GENKEYS.GONE_TIMEOUT,
								winCompName +" was gone within timeout "+ seconds,
								winCompName, seconds),
								GENERIC_MESSAGE);
			}else{
				testRecordData.setStatusCode(StatusCodes.SCRIPT_WARNING);
				log.logMessage(testRecordData.getFac(),
						failedText.convert(FAILKEYS.NOT_FOUND_TIMEOUT,
								winCompName +" was not found in timeout "+ seconds,
								winCompName, seconds),
								WARNING_MESSAGE);
			}
		}catch(Exception e){
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			standardFailureMessage(command+" Fail.", "Met "+StringUtils.debugmsg(e));
		}
	}

	private void setFocus(){
		String debugmsg = StringUtils.debugmsg(false);

		if(params.size() < 2){
			issueParameterCountFailure();
			return;
		}

		Iterator<?> iterator = params.iterator();
		String window = (String) iterator.next();
		String component = (String) iterator.next();
		IndependantLog.debug(debugmsg+" Setting focus to "+window+":"+component);
		testRecordData.setWindowName(window);
		testRecordData.setCompName(component);

		boolean focused = false;
		boolean isWindow = window.equalsIgnoreCase(component);
		String winCompString = isWindow? window:window+":"+component;

		//wait for window and component object
		try {
			WebDriver webdriver = WDLibrary.getWebDriver();
			//wait for window and component object
			long timeout = isWindow? getSecsWaitForWindow():getSecsWaitForComponent();
			int status = wdgu.waitForObject(testRecordData.getAppMapName(), window, component, timeout);
			if(status==0){
				//TODO focus the window, How to???
				focusWindow(webdriver);
				if(!isWindow){
					//focus the component
					WebElement element = WDLibrary.getObject(testRecordData.getCompGuiId());
					Actions focusAction = new Actions(webdriver).moveToElement(element);
					if("EditBox".equalsIgnoreCase(WebDriverGUIUtilities.getCompType(element)))
						focusAction = focusAction.click();
					focusAction.perform();
				}
				focused = true;
			}else{
				IndependantLog.error(debugmsg+" cannot find "+winCompString);
			}
		} catch(Exception e) {
			IndependantLog.error(debugmsg+" cannot set focus to "+winCompString+" due to "+StringUtils.debugmsg(e));
		}

		if(focused){
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			String msg = genericText.convert(TXT_SUCCESS_2,
					winCompString+" "+ testRecordData.getCommand() +" successful.",
					winCompString, testRecordData.getCommand());
			log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);
		}else{
			issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND,
					winCompString +" was not focused.", winCompString));
		}
	}

	/**
	 * TODO Need to move to class WDLibrary, this doesn't work yet!!!
	 * @param webdriver
	 * @throws SeleniumPlusException
	 */
	static void focusWindow(WebDriver webdriver) throws SeleniumPlusException{
		try{
			org.openqa.selenium.Point position = webdriver.manage().window().getPosition();
			org.openqa.selenium.Dimension dim = webdriver.manage().window().getSize();
			webdriver.manage().window().maximize();
			webdriver.manage().window().setPosition(position);
			webdriver.manage().window().setSize(dim);
			webdriver.switchTo().window(webdriver.getWindowHandle());
			WDLibrary.executeScript("window.focus();");
		} catch (Exception e){
			throw new SeleniumPlusException("Failed to minimize current browser window"+ e.getMessage());
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
		Object obj = wdgu.getTestObject(mapname, windowName, compName, true);
		return obj;
	}

	/**
	 * <br><em>Purpose:</em> Helper function for waitForPropertyValue and waitForPropertyValueGone <br>
	 * @param propertyStatus boolean, if it is true, it means wait for property matching expected values; <br>
	 *                                if it is false, it means wait for property gone, i.e. NOT matching expected values.
	 * @author Tao Xie
	 */
	private void waitForPropertyValueStatus(boolean propertyStatus) {
		String dbgmsg = StringUtils.debugmsg(DCDriverCommand.class, "waitForPropertyValueStatus");

		if (params.size() < 4) {
			issueParameterCountFailure();
			return;
		}

		final String DEFAULT_TIMEOUT = "15";
		final String DEFAULT_CASE_INSENSITIVE = "False";
		String seconds = "";
		String caseInsensitive = "";
		iterator = params.iterator();

		// get the window, component, property, expected value.
		String windowName = iterator.next();
		String compName = iterator.next();
		String propertyName = iterator.next();
		String expectedValue = iterator.next();

		testRecordData.setWindowName(windowName);
		testRecordData.setCompName(compName);

		// get timeout, case sensitive parameters
		if(params.size() > 4) {
			seconds = iterator.next();
			if(params.size() > 5)
				caseInsensitive = iterator.next();
		}

		// timeout parsing
		int secii = 0;
		if(0 == seconds.length())
			seconds = DEFAULT_TIMEOUT;
		try {
			secii = Integer.parseInt(seconds);
			log.logMessage(testRecordData.getFac(),
					genericText.convert(GENKEYS.DEFAULT_MISSING_PARAM,
							command + " optional parameter '" + "TIMEOUT" + "' set to '" + seconds + "'.",
							command,
							"TIMEOUT",
							seconds),
					GENERIC_MESSAGE);
		} catch (NumberFormatException e) {
			seconds = DEFAULT_TIMEOUT;
			secii = Integer.parseInt(seconds);
			log.logMessage(testRecordData.getFac(),
					genericText.convert(GENKEYS.DEFAULT_BAD_PARAM,
							command + " invalid optional parameter '" + "TIMEOUT" + "' set to '" + seconds + "'.",
						command,
						"TIMEOUT",
						seconds),
					GENERIC_MESSAGE);
		} catch (Exception e) {
			seconds = DEFAULT_TIMEOUT;
			secii = Integer.parseInt(seconds);
			log.logMessage(testRecordData.getFac(),
					genericText.convert(GENKEYS.DEFAULT_MISSING_PARAM,
						command + " optional parameter '" + "TIMEOUT" + "' set to '" + seconds + "'.",
						command,
						"TIMEOUT",
						seconds),
					GENERIC_MESSAGE);
		}
		if (secii < 0)
			secii = 0;

		// case sensitive parsing
		if(0 == caseInsensitive.length())
			caseInsensitive = DEFAULT_CASE_INSENSITIVE;
		boolean booleanCaseInsensitive = Boolean.parseBoolean(caseInsensitive);
		log.logMessage(testRecordData.getFac(),
				genericText.convert(GENKEYS.DEFAULT_MISSING_PARAM,
					command + " optional parameter '" + "CASEINSENSITIVE" + "' set to '" + caseInsensitive + "'.",
					command,
					"CASEINSENSITIVE",
					caseInsensitive),
				GENERIC_MESSAGE);

		Log.debug(dbgmsg + "......" + command + ": window:" + windowName + ", component:" + compName
				+ ", property name:" + propertyName + ", expected value:" + expectedValue + ", seconds:" + seconds + ", caseInsensitive:" + caseInsensitive);

		// compare with expected value
		String msg = "";
		try {
			// wait for the window/component
			int status = wdgu.waitForPropertyStatus(windowName, compName, propertyName, expectedValue, secii, booleanCaseInsensitive, propertyStatus);

			//if it is not match with expected value
			if (status != 0) {
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				msg = failedText.convert(FAILKEYS.SELECTION_NOT_MATCH,
								"Selection '" + propertyName + "' does not match expected value '" + expectedValue + "'",
								propertyName,
								expectedValue);

				standardFailureMessage(msg, testRecordData.getInputRecord());
				return;
			}

			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			log.logMessage(testRecordData.getFac(),
						   genericText.convert(GENKEYS.FOUND_TIMEOUT,
								   compName + " was found within timeout " + seconds,
								   compName,
								   seconds),
						   GENERIC_MESSAGE);
		} catch (SAFSException se) {
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			msg = failedText.convert(FAILKEYS.NOT_FOUND_TIMEOUT,
							compName + " was not found within timeout " + seconds,
							compName,
							seconds);
			standardFailureMessage(msg, testRecordData.getInputRecord());
		}
	}

}
