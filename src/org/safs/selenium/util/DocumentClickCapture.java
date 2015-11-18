/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.util;
/**
 * History:
 * OCT 15, 2014    (LeiWang) Add field 'target' as the event-receiver.
 * MAR 23, 2015    (LeiWang) Modify waitForClick(): wait the click event ready if we detect the event-fired.
 * MAR 31, 2015    (LeiWang) Modify getEventListener(): once received 'mousedown', set the global variable immediately
 *                                  Modify onEventFired(): Add debug information to show where the click happened.
 * JUN 04, 2015    (LeiWang) Modify run(): if the target webelement is stale, we consider click as success.
 * JUN 25, 2015    (LeiWang) Modify onEventFired(): ignore event's information to save time.
 * NOV 18, 2015    (LeiWang) Provide a way to disable the DocumentClickCapture.
 */
import java.util.Date;

import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.tools.stringutils.StringUtilities;

/**
 * Used to intercept mouseup, mousedown, and click events at the DOM document level in order to allow 
 * clicks on the document that will NOT invoke the application's event handlers.  For example, when 
 * a ProcessContainer tool wants to investigate element properties by selecting an element with a click. 
 * <p>
 * Usage:
 * <p><pre><code>
 * DocumentClickCapture listener = new DocumentClickCapture();
 * try{
 *     MouseEvent event = listener.waitForClick(timeout_in_seconds);
 *     // do stuff with event info
 *     // use again if desired
 *     event = listener.waitForClick(timeout_in_seconds);
 *     // do stuff with event info
 * }catch(Exception x){
 *     // handle them here
 * }
 * </code></pre>
 * <p>
 * The use CAN allow the application's event handlers to see the events if the alternate constructor is used 
 * setting the allowPropogation boolean to true:
 * <p>
 * Usage:
 * <p><pre><code>
 * DocumentClickCapture listener = new DocumentClickCapture(true);
 * try{
 *     MouseEvent event = listener.waitForClick(timeout_in_seconds);
 *     // do stuff with event info
 *     // use again if desired
 *     event = listener.waitForClick(timeout_in_seconds);
 *     // do stuff with event info
 * }catch(Exception x){
 *     // handle them here
 * }
 * </code></pre>
 * @author canagl
 */
public class DocumentClickCapture implements Runnable{
	
	/** "safs_clickcapture" */
	public static final String LISTENER_ID_ROOT = "safs_clickcapture";
	
	/** in a loop, time (in milliseconds) to sleep before trying to detect the click event-fired on page another time.*/
	public static int LISTENER_LOOP_DELAY = 200;//milliseconds
	
	/**
	 * After event-fired, default timeout (5000 milliseconds) before click event-ready (event information has be retrieved from page by javascript).
	 * @see #timeoutBtwEvtFireAndReady
	 */
	public static final int DEFAULT_TIMEOUT_BETWEEN_EVENT_FIRE_AND_READY = 5000;//milliseconds
	/** in a loop, default delay time (in milliseconds) to sleep before trying to detect the 
	 * click event-ready (event information has be retrieved from page by javascript) another time.*/
	public static final int DEFAULT_DELAY_WAIT_READY_LOOP = 100;//milliseconds
	
	/**
	 * Only {@link #eventFired} is set to true, this field will take effect.<br>
	 * after event-fired, in a loop,
	 * timeout (in milliseconds) before click event-ready (event information has be retrieved from page by javascript).
	 * @see #eventFired
	 * @see #waitForClick(long)
	 */
	public static int timeoutBtwEvtFireAndReady = DEFAULT_TIMEOUT_BETWEEN_EVENT_FIRE_AND_READY;//milliseconds
	/**
	 * in a loop, time (in milliseconds) to sleep before trying to detect the 
	 * click event-ready (event information has be retrieved from page by javascript) another time.
	 * @see #waitForClick(long)
	 */
	public static int delayWaitReady = DEFAULT_DELAY_WAIT_READY_LOOP;//milliseconds
	
	/** true, will not retrieve event's information.*/
	public static final boolean DEFAULT_IGNORE_EVENT_INFORMATION = true;
	
	/**
	 * This field could be used to turn on/off the DocumentClickCapture.<br>
	 * This field will ONLY affect the new instance of DocumentClickCapture. For example:<br>
	 * <pre>
	 * DocumentClickCapture.ENABLE_CLICK_CAPTURE = true;
	 * DocumentClickCapture captureA = new DocumentClickCapture(true, target);
	 * 
	 * DocumentClickCapture.ENABLE_CLICK_CAPTURE = false;
	 * DocumentClickCapture captureB = new DocumentClickCapture(true, target);
	 * DocumentClickCapture captureC = new DocumentClickCapture(true, target);
	 * 
	 * captureA is enabled; while captureB and captureC will be disabled.
	 * </pre>
	 */
	public static boolean ENABLE_CLICK_CAPTURE = true;
	/**
	 * Represent if this 'click capture' is enabled.<br>
	 * This filed will be set in constructor {@link #DocumentClickCapture()} according to static field {@link #ENABLE_CLICK_CAPTURE}.<br>
	 * <br>
	 * If disabled, then we will not capture click event.<br>
	 * 
	 * @see #startListening()
	 * @see #waitForClick(long)
	 */
	private boolean enabled = true;
	
	/**
	 * After event happened, we can retrieve event's information  and assign them to {@link #mouseEvent}.<br>
	 * If this field is false, the program will retrieve the information, which will spend some time <br>
	 * and cause the timeout {@link WDLibrary#timeoutWaitClick} reached and click action fails. If we don't<br>
	 * really need these event's information, we should set this field to true to save time.<br>
	 * @see #onEventFired()
	 */
	private static boolean ignoreEventInformation = DEFAULT_IGNORE_EVENT_INFORMATION;

	/** get {@link #ignoreEventInformation}. */
	public static boolean isIgnoreEventInformation() {
		return ignoreEventInformation;
	}

	/** set {@link #ignoreEventInformation}. */
	public static void setIgnoreEventInformation(boolean ignoreEventInformation) {
		DocumentClickCapture.ignoreEventInformation = ignoreEventInformation;
	}

	private String listenerID = null;
	private String listenerFunction = null;
	private String listenerError = null;
	private String EVENT_PHASE = null;
	private String EVENT_TYPE = null;
	private String EVENT_TARGET = null;
	private String EVENT_CURRENTTARGET = null;
	private String EVENT_TIMESTAMP = null;
	@SuppressWarnings("unused")
	private String EVENT_VIEW = null;
	private String EVENT_DETAIL = null;
	private String EVENT_SCREENX = null;
	private String EVENT_SCREENY = null;
	private String EVENT_CLIENTX = null;
	private String EVENT_CLIENTY = null;
	private String EVENT_CTRLKEY = null;
	private String EVENT_SHIFTKEY = null;
	private String EVENT_ALTKEY = null;
	private String EVENT_METAKEY = null;
	private String EVENT_BUTTON = null;
	private String EVENT_RELATEDTARGET = null;
	
	private boolean keepRunning = false;
	/** will be set to true, if detect the javascript's event fire (a global variable has been set)
	 * @see #onEventFired()
	 */
	private boolean eventFired = false;
	/** will be set to true, after all values of the javascript's event have been gotten.
	 * @see #onEventFired()
	 */
	private boolean eventReady = false;
	private MouseEvent mouseEvent = null;
	private boolean propogate = false;
	private boolean notListening = true;
	
	/** 
	 * The element is expected to receive the click event.<br>
	 * If this field is null, the DOM's document is the receiver.<br>
	 */
	private WebElement target = null;
	
	/**
	 * Default constructor.
	 * Capture mousedown, mouseup, and click event and DO NOT allow default operation or propagation of 
	 * the event to the application.
	 */
	public DocumentClickCapture() {
		listenerID          = LISTENER_ID_ROOT;
		listenerFunction    = listenerID+"Listener";
		listenerError       = listenerID+"Error";
		EVENT_TYPE          = listenerID+"_type";
		EVENT_PHASE         = listenerID+"_eventPhase";
		EVENT_TARGET        = listenerID+"_target";
		EVENT_CURRENTTARGET = listenerID+"_currentTarget";
		EVENT_TIMESTAMP     = listenerID+"_timestamp";
		EVENT_VIEW          = listenerID+"_view";
		EVENT_DETAIL        = listenerID+"_detail";
		EVENT_SCREENX       = listenerID+"_screenX";
		EVENT_SCREENY       = listenerID+"_screenY";
		EVENT_CLIENTX       = listenerID+"_clientX";
		EVENT_CLIENTY       = listenerID+"_clientY";
		EVENT_CTRLKEY       = listenerID+"_ctrlKey";
		EVENT_SHIFTKEY      = listenerID+"_shiftKey";
		EVENT_ALTKEY        = listenerID+"_altKey";
		EVENT_METAKEY       = listenerID+"_metaKey";
		EVENT_BUTTON        = listenerID+"_button";
		EVENT_RELATEDTARGET = listenerID+"_relatedTarget";
		
		//According the the static filed 'ENABLE_CLICK_CAPTURE' to enable or disable this click capture.
		enabled = ENABLE_CLICK_CAPTURE;
	}

	/**
	 * Alternate constructor.
	 * Capture mousedown, mouseup, and click event trio. 
	 * Set allowPropogation = true if we want the final "click" to be handled by the application normally.
	 * @see #DocumentClickCapture()
	 */
	public DocumentClickCapture(boolean allowPropogation) {
		this();
		propogate = allowPropogation;
	}
	
	/**
	 * Alternate constructor.
	 * Capture mousedown, mouseup, and click event trio. 
	 * Set allowPropogation = true if we want the final "click" to be handled by the application normally.<br>
	 * The target is the receiver of the event. Only the event happens on the target, the attached listener<br>
	 * will be trrigered; Set target to null, if user wants the window.document to be the event receiver.<br>
	 * @see #DocumentClickCapture(boolean)
	 */
	public DocumentClickCapture(boolean allowPropogation, WebElement target) {
		this(allowPropogation);
		this.target = target;
	}
	
	/**
	 * History:
	 * <pre>
	 * Jun 25, 2015 (LeiWang) Retrieve event's information will spend some time, if the event happens on a remote machine, it will take much longer.
	 *                        Even if the click really happened and we spend a lot of time on getting the event's information, 
	 *                        the timeout (default 2 seconds) will be reached and the click will be considered not happened, which is not what we want.
	 *                        To save time, we will not retrieve event's information unless user set {@link #ignoreEventInformation} to false.
	 * <pre>
	 */
	private void onEventFired(){
		String debugmsg = StringUtils.debugmsg(false);
		setEventFired(true);
		mouseEvent = new MouseEvent(listenerID);
		
		if(!ignoreEventInformation){
			String eventHappenTime = "";
			
			try{ mouseEvent.EVENT_TYPE = (String)(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_TYPE)));}catch(Exception ignore){}
			try{ mouseEvent.EVENT_PHASE = ((Long)(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_PHASE)))).intValue();}catch(Exception ignore){}
			try{ mouseEvent.EVENT_TARGET = (WebElement) SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_TARGET));}catch(Exception ignore){}
			try{ 
				mouseEvent.EVENT_TIMESTAMP = ((Long)(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_TIMESTAMP)))).longValue();
				eventHappenTime = StringUtilities.getTimeString(new Date(mouseEvent.EVENT_TIMESTAMP), true);
			}catch(Exception ignore){}

			//PROBLEM IN FIREFOX 28 WITH SELENIUM 2.41
			//try{ mouseEvent.EVENT_VIEW = String.valueOf(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_VIEW)));}catch(Exception ignore){}

			try{ mouseEvent.EVENT_CURRENTTARGET = (WebElement) SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_CURRENTTARGET));}catch(Exception ignore){}
			try{ mouseEvent.EVENT_RELATEDTARGET = (WebElement) SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_RELATEDTARGET));}catch(Exception ignore){}
			try{ mouseEvent.EVENT_DETAIL = ((Long)(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_DETAIL)))).longValue();}catch(Exception ignore){}
			try{ mouseEvent.EVENT_SCREENX = ((Long)(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_SCREENX)))).longValue();}catch(Exception ignore){}
			try{ mouseEvent.EVENT_SCREENY = ((Long)(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_SCREENY)))).longValue();}catch(Exception ignore){}

			try{
				WebElement target = mouseEvent.EVENT_TARGET;
				if(target==null) target = mouseEvent.EVENT_CURRENTTARGET;
				IndependantLog.debug(debugmsg+" Event '"+mouseEvent.EVENT_TYPE+
						"' happended at "+eventHappenTime+" on element '"+target.getTagName()+"'"+
						" at screen location ("+mouseEvent.EVENT_SCREENX+","+mouseEvent.EVENT_SCREENY+").");
			}catch(Exception ignore){}

			try{ mouseEvent.EVENT_CLIENTX = ((Long)(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_CLIENTX)))).longValue();}catch(Exception ignore){}
			try{ mouseEvent.EVENT_CLIENTY = ((Long)(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_CLIENTY)))).longValue();}catch(Exception ignore){}

			try{ mouseEvent.EVENT_CTRLKEY = ((Boolean)(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_CTRLKEY)))).booleanValue();}catch(Exception ignore){}
			try{ mouseEvent.EVENT_SHIFTKEY = ((Boolean)(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_SHIFTKEY)))).booleanValue();}catch(Exception ignore){}
			try{ mouseEvent.EVENT_ALTKEY = ((Boolean)(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_ALTKEY)))).booleanValue();}catch(Exception ignore){}
			try{ mouseEvent.EVENT_METAKEY = ((Boolean)(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_METAKEY)))).booleanValue();}catch(Exception ignore){}
			try{ mouseEvent.EVENT_BUTTON = ((Long)(SearchObject.executeScript(JavaScriptFunctions.getGlobalVariable(EVENT_BUTTON)))).intValue();}catch(Exception ignore){}
		}

		IndependantLog.debug(debugmsg+" setting ready to true.");
		setReady(true);
	}
	
	/**
	 * Inject the Document level event listeners if they are not already injected.
	 * Does nothing if the listeners are already injected.
	 * @param secondsTimeout
	 * @throws SeleniumPlusException
	 * @see #addEventListeners()
	 */
	public void startListening()throws SeleniumPlusException{
		String debugmsg = "DocumentClickCapture.startListening ";
		
		if(!enabled){
			IndependantLog.debug(debugmsg+" DocumentClickCapture has been disabled.");
			return;
		}
		
	    if(notListening){
	    	IndependantLog.info(debugmsg+" not Listening: injecting Listeners.");
			mouseEvent = null;
			setReady(false);
			setEventFired(false);
			addEventListeners(); // can throw SeleniumPlusException
			try{Thread.sleep(100);}catch(Exception ignore){}
	    }else{
	    	IndependantLog.info(debugmsg+" already listening: NOT injecting Listeners.");
	    }
	}
	
	/**
	 * Stop the pollingThread from listening and remove the listeners.
	 * Normally only called if waitForClick is going to be bypassed for some reason.
	 */
	public void stopListening(){
    	IndependantLog.info("DocumentClickCapture.stopListening attempting to stop Listeners.");
		setRunning(false);
	}
	
	/**
	 * 
	 * @param secondsTimeout -- timeout in seconds before throwing InterruptedException
	 * @return MouseEvent object with some or all of its fields filled in.  Fields can be null.
	 * @throws InterruptedException if timeout has been reached.
	 * @throws IllegalArgumentException if secondsTimeout is < 1.
	 * @throws SeleniumPlusException if we could not add or remove eventListeners in the browser.
	 * @see #startListening(long) 
	 */
	public MouseEvent waitForClick(long secondsTimeout)throws IllegalArgumentException, 
	                                                          InterruptedException,
	                                                          SeleniumPlusException{
		String debugmsg = "DocumentClickCapture.waitForClick ";
		
		if(!enabled){
			IndependantLog.debug(debugmsg+" DocumentClickCapture has been disabled! Return a void MouseEvent.");
			return new MouseEvent(null);
		}
		
		if(secondsTimeout < 1) throw new IllegalArgumentException(debugmsg +"secondsTimeout must be greater than 0.");
		startListening();
		long curTime = System.currentTimeMillis();
		long endTime = curTime + (1000*secondsTimeout);
		IndependantLog.info(debugmsg+" waiting for event fired signal.");
		while((curTime < endTime)&& isRunning() && !isEventFired()){
			try{Thread.sleep(delayWaitReady);}catch(InterruptedException ignore){}
			curTime = System.currentTimeMillis();
		}
		if(isEventFired()){
			IndependantLog.info(debugmsg+" detected event has been fired. Checking READY.");
		}else{
			IndependantLog.info(debugmsg+" detected event has NOT been fired.");
		}
		setRunning(false);
		notListening = true;
		
		//If the event has been fired but not ready, then the event should be ready (event got from page by js) in a few while.
		if(!isReady() && isEventFired()){
			int tic = 0;
			int totalTics = timeoutBtwEvtFireAndReady/delayWaitReady;
			while(!isReady() && tic++<totalTics){//Wait ready for more times
				try{Thread.sleep(delayWaitReady);}catch(InterruptedException ignore){}
			}
			// TODO CANAGL resetting eventFired may be wrong here?!
			// only if !isReady() ?
			setEventFired(false);
		}
		
		if(isReady()){
			setReady(false);
		}else{
			IndependantLog.info(debugmsg+" isReady() timeout has been reached.");
			throw new InterruptedException(debugmsg +" isReady() timeout has been reached.");
		}
		IndependantLog.info(debugmsg+" returning mouseEvent: "+ mouseEvent);
		return mouseEvent;
	}
	
	private synchronized boolean isRunning() {
		return keepRunning;
	}
	private synchronized void setRunning(boolean keepRunning) {
		this.keepRunning = keepRunning;
	}		
	private synchronized boolean isReady() {
		return eventReady;
	}
	private synchronized void setReady(boolean ready) {
		eventReady = ready;
	}		
	private synchronized boolean isEventFired() {
		return eventFired;
	}
	private synchronized void setEventFired(boolean eventFired) {
		this.eventFired = eventFired;
	}		
	/**
	 * Internal Runnable use only.
	 * <p>
	 * This thread monitors the javascript event flag to be set indicating the event was fired.
	 * monitor loop can be stopped internally with setRunning(false).
	 * If the event is detected the onEventFired() method is invoked by this thread.
	 * This is a single-fire event.  Once fired or stopped the monitor will exit and the 
	 * remote EventListeners will be removed from the EventTarget.
	 */
	public void run() {
		mouseEvent = null;
		setReady(false);
		setEventFired(false);
		setRunning(true);
		String debugmsg = "DocumentClickCapture.run ";
		IndependantLog.debug(debugmsg+"started pollingThread for "+listenerID);
		boolean eventfired = false;
		boolean jsGone = false;
		boolean targetGone = false;
		while(isRunning()){
			try {
				IndependantLog.suspendLogging();
				try{ 
					eventfired = SearchObject.js_getGlobalBoolVariable(listenerID);
				}
				catch(IllegalStateException is){
					// 99% likely the page has been refreshed because of the click!
					// our global variables and such are GONE!
					IndependantLog.resumeLogging();
					IndependantLog.debug(debugmsg+ listenerID+ " global data appears to be GONE! DOM refresh?");
					eventfired = true;
					jsGone = true;
				}catch(Throwable thr){
					IndependantLog.resumeLogging();
					IndependantLog.debug(debugmsg+ listenerID+ " getGlobalBoolVariable Throws "+ thr.getClass().getSimpleName()+", "+ thr.getMessage());
					eventfired = true;
					jsGone = true;
				}
				IndependantLog.resumeLogging();
				
				//if the target is gone, we don't need to wait for the "listener detection"
				try{
					if(!eventfired && target!=null && WDLibrary.isStale(target)){
						IndependantLog.debug(debugmsg+ listenerID+ " target appears to be GONE!");
						eventfired = true;
						targetGone = true;						
					}
				}catch(SeleniumPlusException se){
					IndependantLog.warn(StringUtils.debugmsg(se));
				}
				
				if(eventfired){
					if(jsGone||targetGone){
						mouseEvent = new MouseEvent(listenerID);
						setReady(true);
					}else{						
						IndependantLog.debug(debugmsg+ listenerID+ " event has been fired.");
						onEventFired();
					}
					setRunning(false);
					//reset the global variable
					if(!jsGone) {
						IndependantLog.debug(debugmsg+ listenerID+ " resetting global javascript variable.");
						SearchObject.js_setGlobalBoolVariable(listenerID, false);
					}else{
						IndependantLog.debug(debugmsg+ listenerID+ " javascript variable GONE and will not be reset.");
					}
				}else{
					try { Thread.sleep(LISTENER_LOOP_DELAY); } catch (InterruptedException ignore) { }
				}
			} catch (Throwable e) {
				IndependantLog.resumeLogging();
				IndependantLog.error(debugmsg+" pollingThread "+e.getClass().getSimpleName()+", "+e.getMessage());
				break;
			}
		}		
		IndependantLog.resumeLogging();
		setRunning(false);
		try{
			removeEventListeners(); 
		}catch(Throwable ignore){
			IndependantLog.warn(debugmsg+" ignoring failure to removeEventListeners: "+ ignore.getClass().getSimpleName()+", "+ ignore.getMessage());
		}
		IndependantLog.debug(debugmsg+"ended pollingThread for "+ listenerID);
	}
		
	public String getListenerID(){ return listenerID;}
	
	/**
	 * Define a javascript EventListener that performs a Capture and only allow the 
	 * mousedown, mouseup, or click events to "trickle down" to the application component(s) if allowPropogation 
	 * was true during initialization.
	 * <p>
	 * The EventListener will set the root global variable to true when a Mouse "click" event 
	 * has occurred and will fill-in associated global MouseEvent variables with the info 
	 * from the event.
	 * <p>
	 * @param variable String, the root global variable name.
	 * @return String, the javascript callback function.
	 */
	private String getEventListener(){
		String captureOption = propogate ? "" : 
			                               "        evt.preventDefault();\n"+
	                                       "        evt.stopPropagation();\n";

		return "\n" +
			   "window."+ listenerID +"=false;\n"+
	           "window."+ listenerError +"='';\n"+
  	           "window."+ listenerFunction +"={\n"+
		       "    handleEvent: function (evt) {\n"+
			   "        switch(evt.type) {\n"+
			   "            case 'mouseup':\n"+
			   "                break;// ignore \n"+
			   "            case 'mousedown':\n"+
			   //When receive the event, set the global variable immediately so that 
			   //even the time used for gathering event information is bigger than the timeout of waiting for 'mousedown'
			   //we still can receive the 'event fired' at java side.
			   "                window."+ listenerID +"=true;\n"+
		       "                try{window."+ EVENT_TYPE +"=evt.type;}catch(err){}\n"+
			   "                try{window."+ EVENT_PHASE +"=evt.eventPhase;}catch(err){}\n"+
			   "                try{window."+ EVENT_TARGET +"=evt.target;}catch(err){}\n"+
				  
			   // PROBLEM IN FIREFOX 28 WITH SELENIUM 2.41
//			   "                try{window."+ EVENT_VIEW +"=evt.view;}catch(err){}\n"+
				  
			   "                try{window."+ EVENT_RELATEDTARGET +"=evt.relatedTarget;}catch(err){}\n"+
			   "                try{window."+ EVENT_CURRENTTARGET +"=evt.currentTarget;}catch(err){}\n"+
			   "                try{window."+ EVENT_TIMESTAMP +"=evt.timeStamp;}catch(err){}\n"+
			   "                try{window."+ EVENT_DETAIL +"=evt.detail;}catch(err){}\n"+
			   "                try{window."+ EVENT_SCREENX +"=evt.screenX;}catch(err){}\n"+
			   "                try{window."+ EVENT_SCREENY +"=evt.screenY;}catch(err){}\n"+
			   "                try{window."+ EVENT_CLIENTX +"=evt.clientX;}catch(err){}\n"+
			   "                try{window."+ EVENT_CLIENTY +"=evt.clientY;}catch(err){}\n"+
			   "                try{window."+ EVENT_CTRLKEY +"=evt.ctrlKey;}catch(err){}\n"+
			   "                try{window."+ EVENT_SHIFTKEY +"=evt.shiftKey;}catch(err){}\n"+
			   "                try{window."+ EVENT_ALTKEY +"=evt.altKey;}catch(err){}\n"+
			   "                try{window."+ EVENT_METAKEY +"=evt.metaKey;}catch(err){}\n"+
			   "                try{window."+ EVENT_BUTTON +"=evt.button;}catch(err){}\n"+
			   "                break;// ignore \n"+
			   "            case 'click':\n"+
			   "                break;// ignore \n"+
               "        }\n"+
			   captureOption+
			   "    }\n"+
		       "};\n";
	}

	/**
	 * @return true if the browser already has the listener object injected into the window object.
	 */
	private boolean isSet(){
		boolean set = false;
		String debugmsg ="DocumentClickCapture.isSet executeScript ";
		String script = "if(typeof window."+listenerFunction+" != 'undefined'){ return true;}else{return false;}\n";
		try{ set = ((Boolean)SearchObject.executeScript(script)).booleanValue(); }
		catch(ClassCastException cc){
			IndependantLog.debug(debugmsg+"failed to receive a Boolean result.");
		}
		catch(SeleniumPlusException sp){
			IndependantLog.debug(debugmsg+"SeleniumPlusException "+sp.getMessage());
		}
		return set;
	}
	
	/**
	 * Attach event-listeners to a target.<br>
	 * @param domelement (<b>Javascript</b>) Object, the dom-element to attach the event-listeners.
	 * @see #addEventListeners()
	 */	
	String _addEventListeners(){
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function _addEventListeners(domelement){\n");
		scriptCommand.append("  var target=null;\n");
		scriptCommand.append("  if(domelement!=undefined){\n");
		scriptCommand.append("    target = domelement;\n");
		scriptCommand.append("  }else{\n");
		scriptCommand.append("    target = window.document;\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("  try{target.addEventListener('mouseup', window."+listenerFunction+", true);}catch(err){}\n");
		scriptCommand.append("  try{target.addEventListener('mousedown', window."+listenerFunction+", true);}catch(err){}\n");
		scriptCommand.append("  try{target.addEventListener('click', window."+listenerFunction+", true);}catch(err){}\n");
		scriptCommand.append("};\n");
		return scriptCommand.toString();
	}
	
	/**
	 * Remove the attached event-listeners from a target.<br>
	 * @param domelement (<b>Javascript</b>) Object, the dom-element to detach the event-listeners.
	 * @see #removeEventListeners()
	 */	
	String _removeEventListeners(){
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function _removeEventListeners(domelement){\n");
		scriptCommand.append("  var target=null;\n");
		scriptCommand.append("  if(domelement!=undefined){\n");
		scriptCommand.append("    target = domelement;\n");
		scriptCommand.append("  }else{\n");
		scriptCommand.append("    target = window.document;\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("  window.listenerError='';\n");
		scriptCommand.append("  try{target.removeEventListener('mouseup', window."+listenerFunction+", true);}catch(err){window."+ listenerError +" += err;}\n");
		scriptCommand.append("  try{target.removeEventListener('mousedown', window."+listenerFunction+", true);}catch(err){window."+ listenerError +" += err;}\n");
		scriptCommand.append("  try{target.removeEventListener('click', window."+listenerFunction+", true);}catch(err){window."+ listenerError +" += err;}\n");
		scriptCommand.append("};\n");
		return scriptCommand.toString();
	}
	
	/**
	 * Adds/Renews the listener to mousedown, mouseup, and click events on the {@link #target}.
	 * @throws SeleniumPlusException if we fail the addEventListener calls in the browser.
	 */
	private void addEventListeners() throws SeleniumPlusException{
		String debugmsg = "DocumentClickCapture.addEventListeners ";
		setReady(false);
		try {
			// only add the EventListener object the first time
			// except for FireFox.  It does not retain them?
			String listener = isSet() ? "" : getEventListener();
			String script = listener + _addEventListeners()+" _addEventListeners(arguments[0]);";
			SearchObject.executeScript(script, target);
			notListening = false;
			Thread pollingThread = new Thread(this);			
			pollingThread.setDaemon(true);//so that it will not block the whole program
			pollingThread.start();
			
		} catch (SeleniumPlusException e1) {
			IndependantLog.error(debugmsg+" failed to add EventListeners.", e1);
			throw new SeleniumPlusException("failed to add EventListeners: "+e1.getMessage());
		}
	}
	
	/**
	 * Removes this listener from {@link #target} for mousedown, mouseup, and click events.
	 * @throws SeleniumPlusException
	 */
	private void removeEventListeners(){
		String debugmsg = "DocumentClickCapture.removeEventListeners ";		
		setRunning(false);	
		String errmsg = "";
		try{ 
			// listenerSent will be true when/if addEventListeners sets it
			// if it was not set there, then we need to send it each time (Firefox?).
			String listener = isSet() ? "" : getEventListener();
			notListening = true;
			String script = listener + _removeEventListeners()+" _removeEventListeners(arguments[0]);";;
			SearchObject.executeScript(script, target);
			try{errmsg = SearchObject.executeScript("return window."+listenerError+";\n").toString();}
			catch(Throwable t){}
			IndependantLog.info(debugmsg+" problems, if any: "+ errmsg);			
		}
		catch(Throwable x){
			try{errmsg = SearchObject.executeScript("return window."+listenerError+";\n").toString();}
			catch(Throwable t){}
			IndependantLog.debug(debugmsg+" failed to remove EventListeners for "+listenerID+": "+errmsg);
		}
	}	
}
