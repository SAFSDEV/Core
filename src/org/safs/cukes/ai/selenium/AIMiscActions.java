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
 *
 *  JUN 27, 2019    (Lei Wang) Added 2 step definitions: wait_seconds_if_a_component_is_not_ready and do_not_wait_if_a_component_is_not_ready.
 *  JUN 28, 2019    (Lei Wang) Added assign_variable() and wait_seconds_until_a_component_is_ready().
 *                            Modified wait_seconds_if_a_component_is_not_ready(): set timeout to Processor's 'secsWaitForWindow' and 'secsWaitForComponent' before setting to selenium's implicit-wait.
 *  JUL 31, 2019    (Lei Wang) Accept "SAFS Variable" as parameter in cucumber step.
 *  SEP 18, 2019    (Lei Wang) Renamed focus_on_embedded_iframe() to focus_on_first_embedded_iframe(): call initializeFrames() to get first iframe.
 *                            Added focus_on_embedded_iframe(): to set focus on a certain frame indicated by user.
 *                            Added release_embedded_iframe(): reset the frame context to the topmost document.
 *  SEP 19, 2019    (Lei Wang) Modified focus_on_embedded_iframe(): handle not only the 'recognition string' but also the 'frame's name, ID'.
 */
package org.safs.cukes.ai.selenium;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.safs.Arbre;
import org.safs.Constants.HTMLConst;
import org.safs.IndependantLog;
import org.safs.Processor;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.SeleniumPlus;
import org.safs.selenium.webdriver.WDTestStepProcessor;
import org.safs.selenium.webdriver.lib.RemoteDriver;
import org.safs.selenium.webdriver.lib.SearchObject.FrameElement;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

/**
 * Used to hold miscellaneous test step definitions for gherkin feature files.
 */
public class AIMiscActions extends AIComponent {

	/**
	 * disables abort on find failure and allows testing to continue even when certain failures occur.
	 * <p>
	 * <b>Cucumber Expression: "we continue testing if an item is not found"</b><br>
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * Given we continue testing if an item is not found<br>
	 * Or<br>
	 * And we continue testing if an item is not found<br>
	 * Or<br>
	 * But we continue testing if an item is not found<br>
	 * </code></ul>
	 * @see #abort_testing_on_item_not_found()
	 */
	@Given("we continue testing if an item is not found")
	public void continue_testing_on_item_not_found(){
		String dbgmsg = StringUtils.debugmsg(false);
		_abort_on_find_failure = false;
		String msg = "Test will not abort if items are not found.";
		IndependantLog.info(dbgmsg+" "+ msg);
		Logging.LogMessage(msg);
	}

	/**
	 * enables abort on find failure which aborts testing if certain actions fail.
	 * <p>
	 * <b>Cucumber Expression: "we abort testing if an item is not found"</b><br>
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * Given we abort testing if an item is not found<br>
	 * Or<br>
	 * And we abort testing if an item is not found<br>
	 * Or<br>
	 * But we abort testing if an item is not found<br>
	 * </code></ul>
	 * @see #continue_testing_on_item_not_found()
	 */
	@Given("we abort testing if an item is not found")
	public void abort_testing_on_item_not_found(){
		String dbgmsg = StringUtils.debugmsg(false);
		_abort_on_find_failure = true;
		String msg = "Test will abort if item is not found.";
		IndependantLog.info(dbgmsg+" "+ msg);
		Logging.LogMessage(msg);
	}

	/**
	 * enables text substring matching when comparing text values.
	 * Traditionally, substring matching also ignores case when comparing text values.
	 * <p>
	 * <b>Cucumber Expression: "we can accept partial text matches"</b><br>
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * Given we can accept partial text matches<br>
	 * Or<br>
	 * And we can accept partial text matches<br>
	 * Or<br>
	 * But we can accept partial text matches<br>
	 * </code></ul>
	 * @see #deny_partial_text_matches()
	 */
	@Given("we can accept partial text matches")
	public void accept_partial_text_matches(){
		String dbgmsg = StringUtils.debugmsg(false);
		_substring_matches_allowed = true;
		String msg = "Test will allow partial text matches.";
		IndependantLog.info(dbgmsg+" "+ msg);
		Logging.LogMessage(msg);
	}

	/**
	 * disables text substring matching when comparing text values.
	 * Text strings must match in full.
	 * <p>
	 * <b>Cucumber Expression: "we cannot accept partial text matches"</b><br>
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * Given we cannot accept partial text matches<br>
	 * Or<br>
	 * And we cannot accept partial text matches<br>
	 * Or<br>
	 * But we cannot accept partial text matches<br>
	 * </code></ul>
	 * @see #accept_partial_text_matches()
	 */
	@Given("we cannot accept partial text matches")
	public void deny_partial_text_matches(){
		String dbgmsg = StringUtils.debugmsg(false);
		_substring_matches_allowed = false;
		String msg = "Test will not allow partial text matches.";
		IndependantLog.info(dbgmsg+" "+ msg);
		Logging.LogMessage(msg);
	}

	/**
	 * Find and switchTo an embedded iframe, if present in the current RemoteDriver session.
	 * <p>
	 * <b>Cucumber Expression: "testing should focus on the embedded iframe"</b><br>
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * Given testing should focus on the embedded iframe<br>
	 * Or<br>
	 * And testing should focus on the embedded iframe<br>
	 * Or<br>
	 * But testing should focus on the embedded iframe<br>
	 * </code></ul>
	 * @see #initializeFrames(WebDriver)
	 * @see #release_embedded_iframe()
	 */
	@Given("testing should focus on the embedded iframe")
	public void focus_on_first_embedded_iframe(){
		String dbgmsg = StringUtils.debugmsg(false);
		String msg = null;
		RemoteDriver selenium = (RemoteDriver) WDLibrary.getWebDriver();
		boolean switched = false;
		Arbre<FrameElement> frameNode = null;
		try{
			initializeFrames(selenium);
			if(frames.size()>1){
				frameNode = frames.get(0);
				WDLibrary.switchToFrame(frameNode, selenium);
				//We need to set 'bypass frame reset' to true so that the frame context will not change any more.
				WDLibrary.setBypassFramesReset(true);
				switched = true;
			}
		}catch(Exception e){
			IndependantLog.error(dbgmsg+" Failed to focus on first iframe, due to "+e.toString());
		}

		if(switched){
			msg = "Search found the embedded iframe for testing.";
			Logging.LogTestSuccess(msg);
		}else{
			msg = "Search did not find a new embedded iframe for testing.";
			Logging.LogTestWarning(msg);
		}
	}

	/**
	 * Find and switchTo an embedded iframe, if present in the current RemoteDriver session.
	 * <p>
	 * <b>Cucumber Expression: "testing should focus on the embedded iframe {var_mapitem_or_string}"</b><br>
	 * {var_mapitem_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_MAPITEM_OR_STRING},
	 *                     represents a variable, or a map item, such as mapID:section.item or a double-quoted-string or a single-quoted-string.
	 *                     It will be parsed by {@link TypeRegistryConfiguration}<br>
	 * <p>
	 * Examples invocations:
	 * <p>
	 * <ul>
	 * <br>//Use the recognition string to specify the frame
	 * <li>focus on the frame indicated by recognition string<br>
	 * <b>Given testing should focus on the embedded iframe "FRAMENAME=main"</b>
	 * <li>focus on the frame indicated by recognition string<br>
	 * <b>And testing should focus on the embedded iframe "FRAMENAME=bottom"</b>
	 * <li>focus on the frame indicated by recognition string (in parent-child format)<br>
	 * <b>But testing should focus on the embedded iframe "FRAMENAME=bottom;\;FRAMENAME=bottom_content"</b>
	 * <br>//Use the simple name, ID to specify the frame
	 * <li>focus on the frame indicated by frame name<br>
	 * <b>Given testing should focus on the embedded iframe "main"</b>
	 * <li>focus on the frame indicated by frame name<br>
	 * <b>Given testing should focus on the embedded iframe "bottom"</b>
	 * <li>focus on the frame indicated by frame name<br>
	 * <b>Given testing should focus on the embedded iframe "bottom_content"</b>
	 * <br>//Use the map-item to specify the frame, the value of the map-item can be 'recognition string' or simple name, id<br>
	 * <li>focus on the frame indicated by map item InternalFrameBottomContent.InternalFrameBottomContent<br>
	 *     == on map chain, a map containing =========<br>
	 *     [InternalFrameBottomContent]<br>
	 *     InternalFrameBottomContent="FRAMENAME=bottom;\;FRAMENAME=bottom_content"<br>
	 *     ================================<br>
	 * <b>Given testing should focus on the embedded iframe InternalFrameBottomContent.InternalFrameBottomContent</b>
	 * <li>focus on the frame indicated by map item InternalFrameBottomContent.FrameName<br>
	 *     == on map chain, a map containing =========<br>
	 *     [InternalFrameBottomContent]<br>
	 *     FrameName="bottom_content"<br>
	 *     ================================<br>
	 * <b>Given testing should focus on the embedded iframe InternalFrameBottomContent.FrameName</b>
	 * <br>
	 * </ul>
	 *
	 * @param frameRS String, the string to find frame to focus. It can be
	 *                        <ul>
	 *                        <li>"SAFS recognition string" such as below:<br>
	 *                            Ex: id=..., name=..., frameid=..., iframeid=..., framename=..., frameindex=..., framexpath=....<br>
	 *                            frameid=...;\\;frameid=...<br>
	 *                            frameid=...;\\;framename=...<br>
	 *                         <li>A simple string as frame's name, frame's ID.
	 *                        </ul>
	 *
	 * @see #initializeFrames(WebDriver)
	 * @see #release_embedded_iframe()
	 */
	@Given("testing should focus on the embedded iframe {var_mapitem_or_string}")
	public void focus_on_embedded_iframe(String frameRS){
		String dbgmsg = StringUtils.debugmsg(false);
		boolean switched = false;

		IndependantLog.debug(dbgmsg+" receieved frame recognition string '"+frameRS+"'.");

		try {
			if(frameRS.contains(TypeRegistryConfiguration.SEPARATOR_WIN_COMP)){
				//We got the map item, we need to break the parent-child RS into Criteria object
				Criteria criteria = generateCriteria(frameRS);
				if(criteria!=null) frameRS = criteria.getComponentRS();
			}
			IndependantLog.debug(dbgmsg+" trying to set frame context according to '"+frameRS+"'.");

			//We switch frame context according to the "recognition string"
			switched = WDLibrary.switchFrames(frameRS);

			//User probably indicates the frame by a simple name, ID
			if(!switched){
				WebDriver selenium = WDLibrary.getWebDriver();
				initializeFrames(selenium);
				FrameElement frameElement = null;
				for(Arbre<FrameElement> frameNode: frames){
					frameElement = frameNode.getUserObject();
					if(frameElement==null) continue;
					if(frameRS.equals(frameElement.getProperty(HTMLConst.ATTRIBUTE_ID)) ||
					   frameRS.equals(frameElement.getProperty(HTMLConst.ATTRIBUTE_NAME))){
						switched = WDLibrary.switchToFrame(frameNode, selenium);
						break;
					}
				}
			}

			//We need to set 'bypass frame reset' to true so that the frame context will not change any more.
			if(switched) WDLibrary.setBypassFramesReset(true);

		} catch (Exception e) {
			IndependantLog.error(dbgmsg+"Met "+e.toString());
		}

		if(switched){
			Logging.LogTestSuccess("Focus on the embedded iframe '"+frameRS+"'.");
		}else{
			Logging.LogTestWarning("Failed to focus on embedded iframe '"+frameRS+"'.");
		}
	}

	/**
	 * Switch the frame context back to the topmost document and set the 'bypassFrameReset'
	 * to false so that the 'frame context' can be modified again.<br>
	 *
	 * <p>
	 * <b>Cucumber Expression: "release the focused embedded iframe"</b><br>
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * Given release the focused embedded iframe<br>
	 * Or<br>
	 * And release the focused embedded iframe<br>
	 * Or<br>
	 * But release the focused embedded iframe<br>
	 * </code></ul>
	 * @see #focus_on_embedded_iframe(String)
	 */
	@Given("release the focused embedded iframe")
	public void release_embedded_iframe(){
		String dbgmsg = StringUtils.debugmsg(false);
		String msg = null;
		RemoteDriver selenium = (RemoteDriver) WDLibrary.getWebDriver();

		Object previousFrame = null;
		try {
			previousFrame = WDLibrary._getCurrentFrame(selenium);
			IndependantLog.debug(dbgmsg+" previous frame is '"+previousFrame+"'.");
		} catch (Exception e1) {
			IndependantLog.warn(dbgmsg+" Failed to get the previous frame, Met "+e1.toString());
		}

		try{
			//We need to set 'bypass frame reset' to false so that the frame context can be changed again.
			WDLibrary.setBypassFramesReset(false);
			//We switch back to the top most document
			WDLibrary.switchToDefaultContent(selenium);

			msg = "Release the previous embedded iframe "+(previousFrame==null?"":"'"+previousFrame+"'")+", we should be on the topmost frame context.";
			Logging.LogTestSuccess(msg);
		} catch (Exception e) {
			IndependantLog.error(dbgmsg+"Met "+e.toString());
			Logging.LogTestWarning("Failed to release the previous embedded iframe '"+previousFrame==null?"":("'"+previousFrame+"'")+".");
		}
	}

	/**
	 * Generate a new uniquely named web session for the browser type and URL.
	 * Since a unique ID is always generated for the session, a new session is always created.
	 * This will not attempt to retrieve and use existing web sessions.
	 * <p>
	 * <b>Cucumber Expression: "a {var_or_string} session is started for URL {var_or_string}"</b><br>
	 * <b>{var_or_string}</b> Matches {@link TypeRegistryConfiguration#REGEX_VAR_OR_STRING}, represents a variable name (with an optional leading symbol ^) or a double-quoted-string or a single-quoted-string, such as browserName, _browserName, ^browserName, "chrome", 'firefox' etc.
	 *                        The variable will be resolved as "SAFS variable", if not found then it is returned as itself.
	 * <br>
	 * <p>
	 * Examples invocations:
	 * <p><ul>
	 * <li><b>Given a chrome session is started for URL "http://google.com"</b>
	 * <li><b>And a chrome session is started for URL "http://google.com"</b>
	 * <li>Given assign "chrome" to ^browserName<br>
	 *     <b>Given a ^browserName session is started for URL "http://google.com"</b>
	 * <li>Given assign "http://google.com" to ^googleURL<br>
	 *     <b>Given a ^browserName session is started for URL ^googleURL</b>
	 * <li><br>
	 *     Map items defined under section [ApplicationConstants] will also be considered as variable<br>
	 *     == A map file on the map chain =========<br>
	 *     [ApplicationConstants]<br>
	 *     browserName="chrome"<br>
	 *     googleURL="http://google.com"<br>
	 *     ================================<br>
	 *     <b>Given a ^browserName session is started for URL ^googleURL</b>
	 * </ul>
	 * @param browser String, the browser's name, such as "chrome", "firefox" etc.
	 * @param url String, the URL to open
	 * @see #a_named_web_session_is_started_for_URL(String, String, String)
	 * @throws SAFSObjectNotFoundException if abort on find failure is enabled and the session is not started.
	 * @see #close_the_last_browser_session()
	 * @see AIMiscActions#abort_testing_on_item_not_found()
	 * @see AIMiscActions#continue_testing_on_item_not_found()
	 */
	@Given("a {var_or_string} session is started for URL {var_or_string}")
	public void an_unnamed_web_session_is_started_for_URL(String browser, String url) throws SAFSException{
		a_named_web_session_is_started_for_URL(StringUtils.generateUniqueName(browser), browser, url);
	}

	/**
	 * Given a specific web session id, attempt to reconnect to that web session.
	 * If the web session id does not already exist, then a new web session will be started using
	 * the browser type and URL provided.
	 * <p>
	 * <b>Cucumber Expression: "a {var_or_string} {var} session is started for URL {var_or_string}"</b><br>
	 * <b>{var_or_string}</b> Matches {@link TypeRegistryConfiguration#REGEX_VAR_OR_STRING}, represents a variable name (with an optional leading symbol ^) or a double-quoted-string or a single-quoted-string.<br>
	 * <b>{var}</b> Matches {@link TypeRegistryConfiguration#REGEX_VAR}, represents a variable name (with an optional leading symbol ^), such as browserName, _browserName, ^browserName etc.
	 *              It will be resolved as "SAFS variable", if not found then it is returned as itself.<br>
	 * <br>
	 * <p>
	 * Examples invocations:
	 * <p><ul>
	 * <li><b>Given a "MyApp" chrome session is started for URL "http://google.com"</b>
	 * <li><b>And a "MyApp" chrome session is started for URL "http://google.com"</b>
	 * <li>Given assign "chrome" to ^browserName<br>
	 *     <b>Given a "MyApp" ^browserName session is started for URL "http://google.com"</b>
	 * <li>Given assign "MyApp" to ^sessionID<br>
	 *     <b>Given a ^sessionID ^browserName session is started for URL "http://google.com"</b>
	 * <li>Given assign "http://google.com" to ^googleURL<br>
	 *     <b>Given a ^sessionID ^browserName session is started for URL ^googleURL</b>
	 * <li><br>
	 *     Map items defined under section [ApplicationConstants] will also be considered as variable<br>
	 *     == A map file on the map chain =========<br>
	 *     [ApplicationConstants]<br>
	 *     sessionID="MyApp"<br>
	 *     browserName="chrome"<br>
	 *     googleURL="http://google.com"<br>
	 *     ================================<br>
	 *     <b>Given a ^sessionID ^browserName session is started for URL ^googleURL</b>
	 * </ul><p>
	 * If the SeleniumServer is not already running, this routine will attempt to start it using the
	 * normal StartWebBrowser invocation.
	 * @param id String, Unique application/browser ID.
	 * @param browser String, the browser's name, such as "chrome", "firefox" etc.
	 * @param url String, the URL to open
	 * @see SeleniumPlus#StartWebBrowser(String, String, String...)
	 * @throws SAFSObjectNotFoundException if abort on find failure is enabled and the session is not found or started.
	 * @see #an_unnamed_web_session_is_started_for_URL(String, String)
	 * @see #close_the_browser_session(String)
	 * @see AIMiscActions#abort_testing_on_item_not_found()
	 * @see AIMiscActions#continue_testing_on_item_not_found()
	 */
	@Given("a {var_or_string} {var} session is started for URL {var_or_string}")
	public void a_named_web_session_is_started_for_URL(String id, String browser, String url) throws SAFSException{
		WebDriver session = null;
		String failmsg = null;
		try{
			// will reconnect any sessions still alive on the running server
			session = WDLibrary.getWebDriver();
			if((session instanceof WebDriver) && !id.equalsIgnoreCase(WDLibrary.getIDForWebDriver(session))){
				session = WDLibrary.getBrowserWithID(id);
			}
			if(session instanceof WebDriver) {
				failmsg = "Using previous browser session with id '"+ id +"'";
				IndependantLog.info(failmsg);
				Logging.LogTestSuccess(failmsg);
				return;
			}
		}catch(SeleniumPlusException spx){
			IndependantLog.debug("getBrowserWithID("+id+") ignoring "+ spx.getClass().getSimpleName() +" stating '"+ spx.getMessage()+"'");
		}
		IndependantLog.info("Attempting to start a new browser session with id '"+ id +"' using '"+ browser +"'");
		failmsg = "Failed to start a new browser session with id '"+ id +"'";
		if(SeleniumPlus.StartWebBrowser(url, id, browser, String.valueOf(WDTestStepProcessor.getSecsWaitForWindow()))){
			try{
				session = WDLibrary.getBrowserWithID(id);
				resetFrames();
				IndependantLog.info("Successfully started a new browser session with id '"+ id +"' using '"+ browser +"'");
				return;
			}catch(SeleniumPlusException spx){
				IndependantLog.info(failmsg);
				Assert.fail(failmsg);
				if(_abort_on_find_failure) throw new org.safs.SAFSObjectNotFoundException(failmsg);
				return;
			}
		}else{
			IndependantLog.warn(failmsg);
			Assert.fail(failmsg);
			if(_abort_on_find_failure) throw new org.safs.SAFSObjectNotFoundException(failmsg);
			return;
		}
	}

	/**
	 * Verifies a browser session with the requested title is present and, if so,
	 * makes it the currently active session.  The routine will match on text
	 * substrings if substring matching is currently enabled.
	 * <p>
	 * <b>Cucumber Expression: "the {var_or_string} window is displayed"</b><br>
	 * <b>{var_or_string}</b> Matches {@link TypeRegistryConfiguration#REGEX_VAR_OR_STRING}, represents a variable name (with an optional leading symbol ^) or a double-quoted-string or a single-quoted-string.<br>
	 * <p>
	 * Examples invocations:
	 * <p><ul>
	 * <li><b>Given the "My Apps Title" window is displayed</b>
	 * <li><b>And the "My Apps Title" window is displayed</b>
	 * <li>Given assign "My Apps Title" to ^appTitle<br>
	 *     <b>Given the ^appTitle window is displayed</b>
	 * <li><br>
	 *     Map items defined under section [ApplicationConstants] will also be considered as variable<br>
	 *     == A map file on the map chain =========<br>
	 *     [ApplicationConstants]<br>
	 *     appTitle="My Apps Title"<br>
	 *     ================================<br>
	 *     <b>Given the ^appTitle window is displayed</b>
	 * </ul><p>
	 * The routine will seek out or wait for the titled session up to the current
	 * waitForWindow timeout setting.
	 * @param title
	 * @see #accept_partial_text_matches()
	 * @see #deny_partial_text_matches()
	 * @see WDTestStepProcessor#getSecsWaitForWindow()
	 * @see WDTestStepProcessor#setSecsWaitForWindow(int)
	 * @throws SAFSObjectNotFoundException if abort on find failure is enabled and the matching browser is not found.
	 * @see AIMiscActions#abort_testing_on_item_not_found()
	 * @see AIMiscActions#continue_testing_on_item_not_found()
	 */
	@Given("the {var_or_string} window is displayed")
	public void the_titled_window_is_displayed(String title) throws SAFSException {
		long nowtime = System.currentTimeMillis();
		long endtime = nowtime + (1000 * WDTestStepProcessor.getSecsWaitForWindow());
		String [] titles = null;
		String msg = null;
		while(nowtime < endtime){
			// first try exact matching
			try {
				msg = "Found browser session with title '"+ title +"'";
				WDLibrary.getBrowserWithTitle(title);
				IndependantLog.info(msg);
				Logging.LogTestSuccess(msg);
				return;
			} catch (SeleniumPlusException spx) {
				// IndependantLog.debug("getBrowserWithTitle("+title+") "+ spx.getClass().getSimpleName() +" stating '"+ spx.getMessage()+"'");
			}
			// if unsuccessful, try a partial match
			if(_substring_matches_allowed){
				String lctitle = title.toLowerCase().trim();
				try{
					titles = WDLibrary.getAllWindowTitles();
					for(String t:titles){
						if (t.toLowerCase().trim().contains(lctitle)){
							WDLibrary.getBrowserWithTitle(t);
							msg = "Found browser session with title '"+ t +"' matching requested '"+ title +"'";
							IndependantLog.info(msg);
							Logging.LogTestSuccess(msg);
							return;
						}
					}
				} catch (NullPointerException npx) {
					// IndependantLog.debug("getBrowserWithTitle("+title+") "+ npx.getClass().getSimpleName() +" stating '"+ npx.getMessage()+"'");
				}
			}
			nowtime = System.currentTimeMillis();
			try{Thread.sleep(100);}catch(Exception ignore){}
		}
		msg = "Failed to locate browser session with title '"+ title +"'.";
		IndependantLog.warn(msg);
		Assert.fail(msg);
		if(_abort_on_find_failure) throw new org.safs.SAFSObjectNotFoundException(msg);
	}

	/**
	 * Close/Quit the browser session with the given id.
	 * <p>
	 * <b>Cucumber Expression: "close the {var_or_string} browser session"</b><br>
	 * <b>{var_or_string}</b> Matches {@link TypeRegistryConfiguration#REGEX_VAR_OR_STRING}, represents a variable name (with an optional leading symbol ^) or a double-quoted-string or a single-quoted-string.
	 * <p>
	 * Examples invocations:
	 * <p><ul>
	 * <li><b>Then close the "MyApp" browser session</b>
	 * <br>Or<br>
	 * <li><b>And close the "MyApp" browser session</b>
	 * <li>close the browser identified by variable browserID<br>
	 *     Given assign "MyApp" to ^browserID<br>
	 *     <b>Then close the ^browserID browser session</b>
	 * </ul><p>
	 * @throws SAFSObjectNotFoundException if abort on find failure is enabled and the session is not found and closed.
	 * @see #a_named_web_session_is_started_for_URL(String, String, String)
	 * @see AIMiscActions#abort_testing_on_item_not_found()
	 * @see AIMiscActions#continue_testing_on_item_not_found()
	 */
	@Then("close the {var_or_string} browser session")
	public void close_the_browser_session(String id) throws SAFSException{
		String dbgmsg = StringUtils.debugmsg(false);
		WebDriver session = null;
		String msg = null;
		try{
			session = WDLibrary.getBrowserWithID(id);
			session.quit();
			resetFrames();
			msg = "Closed browser session with id '"+ id +"'.";
			IndependantLog.info(dbgmsg +" "+msg);
			Logging.LogTestSuccess(msg);
		}catch(SeleniumPlusException spx){
			msg = "Did not find a browser session with id '"+ id +"'.";
			IndependantLog.error(dbgmsg + " "+ msg);
			Assert.fail(msg);
			if(_abort_on_find_failure) throw new org.safs.SAFSObjectNotFoundException(msg);
		}
	}

	/**
	 * Close/Quit the currently active WebDriver session.
	 * <p>
	 * <b>Cucumber Expression: "close the browser session"</b>
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * Then close the browser session<br>
	 * Or<br>
	 * And close the browser session<br>
	 * </code></ul><p>
	 * @throws SAFSObjectNotFoundException if abort on find failure is enabled and no session was found to close.
	 * @see #an_unnamed_web_session_is_started_for_URL(String, String)
	 * @see AIMiscActions#abort_testing_on_item_not_found()
	 * @see AIMiscActions#continue_testing_on_item_not_found()
	 */
	@Then("close the browser session")
	public void close_the_last_browser_session() throws SAFSException{
		String dbgmsg = StringUtils.debugmsg(false);
		WebDriver session = null;
		String msg = null;
		try{
			session = WDLibrary.getWebDriver();
			String browser = WDLibrary.getIDForWebDriver(session);
			session.quit();
			resetFrames();
			msg = "Closed last browser session having id '"+ browser +"'.";
			IndependantLog.info(dbgmsg +" "+msg);
			Logging.LogTestSuccess(msg);
		}catch(Exception spx){
			msg = "Did not find any running browser session to close.";
			IndependantLog.error(dbgmsg + " "+ msg);
			Assert.fail(msg);
			if(_abort_on_find_failure) throw new org.safs.SAFSObjectNotFoundException(msg);
		}
	}

	@Given("wait {int} seconds until a component is ready")
	public void wait_seconds_until_a_component_is_ready(Integer timeout) {
		wait_seconds_if_a_component_is_not_ready(timeout);
	}

	/**
	 * Given a timeout (in seconds), set it as the timeout to wait for a component's readiness.<br>
	 * This method will firstly set the fields 'secsWaitForWindow' and 'secsWaitForComponent' of {@link Processor}.<br>
	 * Then it will set the 'implicit wait' for selenium if a selenium session has been started.<br>
	 * <p>
	 * <b>Cucumber Expression: "wait <a href="https://cucumber.io/docs/cucumber/cucumber-expressions/#parameter-types">{int}</a> seconds if a component is not ready"</b><br>
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * Given a "SAPDemoID" chrome session is started for URL "http://www.google.com"<br>
	 * And wait 0 seconds if a component is not ready<br>
	 * <br>
	 * Given wait 10 seconds if a component is not ready<br>
	 * Then click the "Allow Multiple Select" checkbox<br>
	 * Then verify "allow multiple selection" is displayed<br>
	 * </code></ul><p>
	 * @param timeout
	 * @see WDTimeOut#implicitlyWait(long, TimeUnit)
	 * @see #do_not_wait_if_a_component_is_not_ready()
	 * @see Processor#setSecsWaitForComponent(int)
	 * @see Processor#setSecsWaitForWindow(int)
	 */
	@Given("wait {int} seconds if a component is not ready")
	public void wait_seconds_if_a_component_is_not_ready(Integer timeout) {
		String dbgmsg = StringUtils.debugmsg(false);
		String msg = null;

		Processor.setSecsWaitForWindow(timeout);
		Processor.setSecsWaitForComponent(timeout);
		IndependantLog.debug(dbgmsg +" set the global 'secsWaitForWindow' and 'secsWaitForComponent' to "+timeout);

		if(WDLibrary.getWebDriver()!=null){
			if(WDTimeOut.implicitlyWait(timeout, TimeUnit.SECONDS)!=null){
				IndependantLog.warn(dbgmsg +" set the selenium implicit timeout to "+timeout);
				msg = "set timeout "+timeout+" to wait for component to be ready.";
				Logging.LogTestSuccess(msg);
				return;
			}else{
				msg = "Failed to set timeout "+timeout+" to wait for component to be ready.";
				IndependantLog.warn(dbgmsg +" "+ msg);
				Assert.fail(msg);
			}
		}else{
			IndependantLog.warn(dbgmsg +" the browser may not start yet!");
			msg = "set timeout "+timeout+" to wait for component to be ready.";
			Logging.LogTestSuccess(msg);
			return;
		}
	}

	/**
	 * Set 0 seconds as the timeout to wait for a component's readiness, which means we don't
	 * wait if a component is not ready. This will accelerate the test speed.
	 * <p>
	 * <b>Cucumber Expression: "do not wait if a component is not ready"</b>
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * Given a "SAPDemoID" chrome session is started for URL "http://www.google.com"<br>
	 * And do not wait if a component is not ready<br>
	 * <br>
	 * Given do not wait if a component is not ready<br>
	 * Then click the "Allow Multiple Select" checkbox<br>
	 * Then verify "allow multiple selection" is displayed<br>
	 * </code></ul><p>
	 * @see WDTimeOut#implicitlyWait(long, TimeUnit)
	 * @see #wait_seconds_if_a_component_is_not_ready(Integer)
	 */
	@Given("do not wait if a component is not ready")
	public void do_not_wait_if_a_component_is_not_ready() {
		wait_seconds_if_a_component_is_not_ready(0);
	}

	/**
	 * Store a variable by SAFS Variable Service.
	 * <p>
	 * <b>Cucumber Expression: "assign <a href="https://cucumber.io/docs/cucumber/cucumber-expressions/#parameter-types">{string}</a> to <a href="https://cucumber.io/docs/cucumber/cucumber-expressions/#parameter-types">{word}</a>"</b>
	 * <p>
	 * Example Scenario Step:
	 * <ul>
	 * 	<li>Given assign "chrome" to browserName
	 *  <li>Given a "GoogleID" browserName session is started for URL "http://www.google.com"
	 * </ul>
	 *
	 * @param value String, the value to be assigned
	 * @param variable String, the name of the variable
	 * @return
	 */
	@Given("assign {string} to {word}")
	public void assign_variable(String value, String variable) {
		String localVar = variable;
		//Strip the possible leading ^
		if(localVar.startsWith(StringUtils.CARET)){
			localVar = localVar.substring(1);
		}
	    SetVariableValue(localVar, value);
	}

	/**
	 * Using cached components to improve the test performance.
	 * <p>
	 * <b>Cucumber Expression: "using cached components"</b><br>
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * Given using cached components<br>
	 * </code></ul>
	 * @see #stop_using_cached_components()
	 */
	@Given("using cached components")
	public void using_cached_components(){
		String dbgmsg = StringUtils.debugmsg(false);
		_using_cached_component = true;
		String msg = "Using cached component.";
		IndependantLog.info(dbgmsg+" "+ msg);
		Logging.LogMessage(msg);
	}

	/**
	 * Stop using cached components to improve the accuracy.
	 * <p>
	 * <b>Cucumber Expression: "stop using cached components"</b><br>
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * Given stop using cached components<br>
	 * </code></ul>
	 * @see #using_cached_components()
	 */
	@Given("stop using cached components")
	public void stop_using_cached_components(){
		String dbgmsg = StringUtils.debugmsg(false);
		_using_cached_component = false;
		String msg = "Stop using cached component.";
		IndependantLog.info(dbgmsg+" "+ msg);
		Logging.LogMessage(msg);
	}

	/**
	 * Turn on/off "log details" to improve the performance.
	 * <p>
	 * <b>Cucumber Expression: "log details {boolean}"</b><br>
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * Given log details on<br>
	 * Given log details off<br>
	 * </code></ul>
	 */
	@Given("log details {boolean}")
	public void logDetails(boolean on){
		String dbgmsg = StringUtils.debugmsg(false);
		_log_details = on;
		String msg = "Turn "+(on?"on":"off")+" 'log details'.";
		IndependantLog.info(dbgmsg+" "+ msg);
		Logging.LogMessage(msg);
	}

}
