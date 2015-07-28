/**
 * 
 */
package org.safs.staf.service.sem;

import java.util.Enumeration;
import java.util.Vector;

import org.safs.Log;
import org.safs.staf.embedded.EmbeddedHandle;
import org.safs.staf.embedded.EmbeddedServiceHandle;
import org.safs.staf.embedded.ServiceInterface;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.InfoInterface.RequestInfo;
import org.safs.staf.service.input.SAFSFile;
import org.safs.text.CaseInsensitiveHashtable;

import com.gargoylesoftware.htmlunit.util.StringUtils;
import com.ibm.staf.STAFException;
import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFCommandParseResult;
import com.ibm.staf.service.STAFCommandParser;

/**
 * This class is intended to partially mimic and mirror the functionality used by SAFS from the STAF SEM service 
 * when STAF is not installed or otherwise being used.  This means not all features and scenarios of the STAF SEM 
 * service are supported.  Only those features and scenarios already used inside SAFS are supported.
 * <p>
 * Handles SEM commands:
 * <ul>
 *     DELETE EVENT &lt;eventname><br>
 *     LIST EVENT<br>
 *     POST EVENT &lt;eventname><br>
 *     PULSE EVENT &lt;eventname><br>
 *     RESET EVENT &lt;eventname><br>
 *     WAIT EVENT &lt;eventname> TIMEOUT &lt;millis><br>
 *     <br>
 *     RELEASE MUTEX &lt<mutexname><br>
 *     REQUEST MUTEX &lt<mutexname><br>
 * </ul>
 * @author canagl JUL 14, 2014
 */
public class EmbeddedSemService implements ServiceInterface {
	
	private boolean debug_enabled = true;  // set to false for production
	
	public static final String handleId    = "STAF/Service/SEM";
	public static final String servicename = "SEM";
	
	public static final String SVS_SERVICE_REQUEST_POST    = "POST";
	public static final String SVS_SERVICE_REQUEST_PULSE   = "PULSE";
	public static final String SVS_SERVICE_REQUEST_RESET   = "RESET";
	public static final String SVS_SERVICE_REQUEST_DELETE  = "DELETE";
	public static final String SVS_SERVICE_REQUEST_LIST    = "LIST";
	public static final String SVS_SERVICE_REQUEST_WAIT    = "WAIT";
	public static final String SVS_SERVICE_REQUEST_REQUEST = "REQUEST";
	public static final String SVS_SERVICE_REQUEST_RELEASE = "RELEASE";

	public static final String SVS_SERVICE_OPTION_EVENT   = "EVENT";
	public static final String SVS_SERVICE_OPTION_MUTEX   = "MUTEX";
	public static final String SVS_SERVICE_OPTION_TIMEOUT = "TIMEOUT";
	
	/**
	 * a single space
	 **/
	protected static String s = " ";  // space
	
	protected EmbeddedServiceHandle fHandle;
	
	/** Assume CaseInsensitiveHashtable&lt;String eventnames, Vector eventwaiters&lt;SEMState>> */
	private CaseInsensitiveHashtable events = new CaseInsensitiveHashtable();
	/** Assume CaseInsensitiveHashtable&lt;String mutexs, Vector mutexwaiters&lt;MutexState>> */
	private CaseInsensitiveHashtable mutexs = new CaseInsensitiveHashtable();
	
 	protected STAFCommandParser parser = new STAFCommandParser(5);

	/**
	 * 
	 */
	public EmbeddedSemService() { 
		debug("new EmbeddedSemService() Constructor.");
	}

	private void debug(String message){
		if(!debug_enabled) return;
		Log.debug(message);
	}
	/**
	 * Intercepts initializing the instance of the service to get servicename information.
	 * However, "intercepts" isn't really the right terminology since STAF will NOT be running 
	 * and will not initiate a call to the service initialization. 
	 * <p>
	 * Handles SEM commands:
	 * <ul>
	 *     DELETE EVENT &lt;eventname><br>
	 *     LIST EVENT<br>
	 *     POST EVENT &lt;eventname><br>
	 *     PULSE EVENT &lt;eventname><br>
	 *     RESET EVENT &lt;eventname><br>
	 *     WAIT EVENT &lt;eventname> TIMEOUT &lt;millis><br>
	 *     <br>
	 *     REQUEST MUTEX &lt<mutexname><br>
	 *     RELEASE MUTEX &lt<mutexname><br>
	 * </ul>
	 * <p>
	 */
	public STAFResult init(InfoInterface.InitInfo initInfo)
	{
		debug("EmbeddedSemService.init() for handle "+ initInfo.name +", parms "+ initInfo.parms);
		parser.addOption( SVS_SERVICE_REQUEST_REQUEST  , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SVS_SERVICE_REQUEST_RELEASE  , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SVS_SERVICE_REQUEST_DELETE   , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SVS_SERVICE_REQUEST_LIST     , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SVS_SERVICE_REQUEST_WAIT     , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SVS_SERVICE_REQUEST_POST     , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SVS_SERVICE_REQUEST_PULSE    , 1, STAFCommandParser.VALUENOTALLOWED );
		parser.addOption( SVS_SERVICE_REQUEST_RESET    , 1, STAFCommandParser.VALUENOTALLOWED );
		
		parser.addOption( SVS_SERVICE_OPTION_MUTEX     , 1, STAFCommandParser.VALUEREQUIRED );
		parser.addOption( SVS_SERVICE_OPTION_EVENT     , 1, STAFCommandParser.VALUEREQUIRED );
		parser.addOption( SVS_SERVICE_OPTION_TIMEOUT   , 1, STAFCommandParser.VALUEREQUIRED );
		
		parser.addOptionGroup(  SVS_SERVICE_REQUEST_REQUEST +s+
				                     SVS_SERVICE_REQUEST_RELEASE +s+				 
				                     SVS_SERVICE_REQUEST_DELETE  +s+				 
				                     SVS_SERVICE_REQUEST_LIST    +s+				 
				                     SVS_SERVICE_REQUEST_WAIT    +s+				 
				                     SVS_SERVICE_REQUEST_POST    +s+				 
				                     SVS_SERVICE_REQUEST_PULSE   +s+				 
				                     SVS_SERVICE_REQUEST_RESET,  1, 1);
		
		parser.addOptionNeed(SVS_SERVICE_OPTION_TIMEOUT, SVS_SERVICE_REQUEST_WAIT);
		
		parser.addOptionNeed(SVS_SERVICE_REQUEST_REQUEST, SVS_SERVICE_OPTION_MUTEX);
		parser.addOptionNeed(SVS_SERVICE_REQUEST_RELEASE, SVS_SERVICE_OPTION_MUTEX);
		
		parser.addOptionNeed(SVS_SERVICE_REQUEST_DELETE, SVS_SERVICE_OPTION_EVENT);
		parser.addOptionNeed(SVS_SERVICE_REQUEST_POST  , SVS_SERVICE_OPTION_EVENT);
		parser.addOptionNeed(SVS_SERVICE_REQUEST_PULSE , SVS_SERVICE_OPTION_EVENT);
		parser.addOptionNeed(SVS_SERVICE_REQUEST_RESET , SVS_SERVICE_OPTION_EVENT);
		parser.addOptionNeed(SVS_SERVICE_REQUEST_LIST  , SVS_SERVICE_OPTION_EVENT);
		parser.addOptionNeed(SVS_SERVICE_REQUEST_WAIT  , SVS_SERVICE_OPTION_EVENT);

		try{
			debug("EmbeddedSemService.init() registering handle '"+ handleId +"' for service '"+ servicename +"'");
			fHandle = new EmbeddedServiceHandle(handleId, servicename, this);
			fHandle.register();
		}catch(STAFException ignore){ 
			/*  ? STAF should not be present ? */
			debug("EmbeddedSemService.init() "+ignore.getClass().getName()+", "+ignore.getMessage());
			System.out.println("EmbeddedSemService.init() "+ignore.getClass().getName()+", "+ignore.getMessage());
		}
		return new STAFResult(STAFResult.Ok);
	}

	/* (non-Javadoc)
	 * @see org.safs.staf.embedded.ServiceInterface#acceptRequest(org.safs.staf.service.InfoInterface.RequestInfo)
	 */
	@Override
	public STAFResult acceptRequest(RequestInfo info) {
		debug("EmbeddedSemService.acceptRequest() from "+ info.handleName +", "+ info.request);
		STAFCommandParseResult parsedData = parser.parse(info.request);
		if (parsedData.rc != STAFResult.Ok) {
			return new STAFResult(STAFResult.InvalidRequestString,
					parsedData.errorBuffer);
		}
		
		if (parsedData.optionTimes(SVS_SERVICE_REQUEST_LIST) > 0) {
			return eventList();
		}else if (parsedData.optionTimes(SVS_SERVICE_REQUEST_DELETE) > 0) {
			return eventDelete(parsedData.optionValue(SVS_SERVICE_OPTION_EVENT));
		}else if (parsedData.optionTimes(SVS_SERVICE_REQUEST_POST) > 0) {
			return eventPost(parsedData.optionValue(SVS_SERVICE_OPTION_EVENT));
		}else if (parsedData.optionTimes(SVS_SERVICE_REQUEST_PULSE) > 0) {
			return eventPulse(parsedData.optionValue(SVS_SERVICE_OPTION_EVENT));
		}else if (parsedData.optionTimes(SVS_SERVICE_REQUEST_RESET) > 0) {
			return eventReset(parsedData.optionValue(SVS_SERVICE_OPTION_EVENT));
		}else if (parsedData.optionTimes(SVS_SERVICE_REQUEST_WAIT) > 0) {
			long timeout = 0;
			if(parsedData.optionTimes(SVS_SERVICE_OPTION_TIMEOUT) > 0){
			    try{
			    	timeout = Long.parseLong(parsedData.optionValue(SVS_SERVICE_OPTION_TIMEOUT));
			    	if (timeout < 0){
			    		debug("EmbeddedSemService().acceptRequest WAIT TIMEOUT cannot be < 0.");
						return new STAFResult(STAFResult.InvalidRequestString, "WAIT TIMEOUT cannot be < 0.");
			    	}
			    }catch(NumberFormatException nf){
		    		debug("EmbeddedSemService().acceptRequest WAIT TIMEOUT must be a number.");
					return new STAFResult(STAFResult.InvalidRequestString, "WAIT TIMEOUT must be a number.");
			    }
			}
			return eventWait(info.handleName, parsedData.optionValue(SVS_SERVICE_OPTION_EVENT), timeout);
		}
		else if (parsedData.optionTimes(SVS_SERVICE_REQUEST_REQUEST) > 0) {
			return mutexRequest(info.handleName, parsedData.optionValue(SVS_SERVICE_OPTION_MUTEX));
		}else if (parsedData.optionTimes(SVS_SERVICE_REQUEST_RELEASE) > 0) {
			return mutexRelease(info.handleName, parsedData.optionValue(SVS_SERVICE_OPTION_MUTEX));
		}
		else{
    		debug("EmbeddedSemService().acceptRequest unsupported request: "+ info.request);
			return new STAFResult(STAFResult.InvalidRequestString, info.request);
		}
	}

	// p/o Interface STAFServiceInterfaceLevel1
	public STAFResult terminate(){

		debug("EmbeddedSemService().terminate() unregistering service handle.");
		// TODO evaluate -- release (Interrupt) all waiting event and mutex threads ??!!
		fHandle.unRegister();
		return new STAFResult(STAFResult.Ok);				
	}

	/**
	 * DELETE an event.  Only works if there are no waiters for the event.  
	 * @returns STAFResult.OK, STAFResult.InvalidParam, STAFResult.SemaphoreDoesNotExist,
	 * STAFResult.SemaphoreHasPendingRequests
	 */
	private STAFResult eventDelete(String eventname) {
		debug("EmbeddedSemService().eventDelete '"+eventname +"'");
		if(eventname == null || eventname.length()== 0) {
			debug("EmbeddedSemService().eventDelete invalid event param '"+eventname +"'");
			return new STAFResult(STAFResult.InvalidParm, "EmbeddedSemService.eventDelete() event name invalid: "+ eventname);		
		}
		if(!events.containsKey(eventname)) {
			debug("EmbeddedSemService().eventDelete '"+eventname +"' does not exits.");
			return new STAFResult(STAFResult.SemaphoreDoesNotExist, eventname);
		}
		SEMState state = (SEMState) events.get(eventname);
		if(state.hasWaiters()) {
			debug("EmbeddedSemService().eventDelete '"+eventname +"' cannot be deleted with waiters.");
			return new STAFResult(STAFResult.SemaphoreHasPendingRequests, eventname);
		}
		events.remove(eventname);
		return new STAFResult(STAFResult.Ok);
	}

	/**
	 * LIST all events.  Their Names, State, and number of Waiters.
	 * @returns STAFResult.OK
	 */
	private STAFResult eventList() {
		debug("EmbeddedSemService().eventList "+ events.size()+" events.");
		String result = "[\n\r";
		for(Object key: events.keySet()){
			result += "  {\n\r";
			result += "    Name   : "+ key.toString() +"\n\r";
			result += "    State  : "+ ((SEMState)events.get(key)).getStateDesc() +"\n\r";
			result += "    Waiters: "+ ((SEMState)events.get(key)).getWaiterIds().size() +"\n\r";
			result += "  }\n\r";
		}
		result += "]\n\r";
		return new STAFResult(STAFResult.Ok, result);
	}

	/**
	 * POST the event.  Creates the event in the POSTED state if necessary.
	 * Releases all waiters that might have existed for the pre-existing event.
	 * @returns STAFResult.OK, STAFResult.InvalidParam
	 */
	private STAFResult eventPost(String eventname) {
		debug("EmbeddedSemService().eventPost '"+eventname +"'");
		if(eventname == null || eventname.length()== 0){ 
			debug("EmbeddedSemService().eventPost '"+ eventname +"' is invalid.");
			return new STAFResult(STAFResult.InvalidParm, "EmbeddedSemService.eventPost() event name invalid: "+ eventname);
		}
		if(!events.containsKey(eventname)) {
			// there can be no waiters if the event does not exist
			events.put(eventname, new SEMState(eventname, SEMState.STATE_POSTED));
			return new STAFResult(STAFResult.Ok);
		}
		SEMState state = (SEMState) events.get(eventname);
		state.setItemState(SEMState.STATE_POSTED);
		state.releaseWaiters();
		return new STAFResult(STAFResult.Ok);
	}
	
	/**
	 * PULSE and RESET the event.  Creates the event in the RESET state if necessary.
	 * Releases all waiters that might have existed for the pre-existing event.
	 * @returns STAFResult.OK, STAFResult.InvalidParam
	 */
	private STAFResult eventPulse(String eventname) {
		debug("EmbeddedSemService().eventPulse '"+eventname +"'");
		if(eventname == null || eventname.length()== 0){ 
			debug("EmbeddedSemService().eventPulse '"+eventname +"' is invalid.");
			return new STAFResult(STAFResult.InvalidParm, "EmbeddedSemService.eventPulse() event name invalid: "+ eventname);
		}
		if(!events.containsKey(eventname)) {
			// there can be no waiters if the event does not exist
			events.put(eventname, new SEMState(eventname, SEMState.STATE_RESET));
			return new STAFResult(STAFResult.Ok);
		}
		SEMState state = (SEMState) events.get(eventname);
		state.releaseWaiters();
		state.setItemState(SEMState.STATE_RESET);
		return new STAFResult(STAFResult.Ok);
	}
	
	/**
	 * RESET the event.  Creates the event in the RESET state if necessary.
	 * There should be no waiters so no call to release waiters is made.
	 * @returns STAFResult.OK, STAFResult.InvalidParam
	 */
	private STAFResult eventReset(String eventname) {
		debug("EmbeddedSemService().eventReset '"+eventname +"'");
		if(eventname == null || eventname.length()== 0){ 
			debug("EmbeddedSemService().eventReset '"+eventname +"' is invalid.");
			return new STAFResult(STAFResult.InvalidParm, "EmbeddedSemService.eventReset() event name invalid: "+ eventname);
		}
		if(!events.containsKey(eventname)) {
			events.put(eventname, new SEMState(eventname, SEMState.STATE_RESET));
		}else{
			((SEMState) events.get(eventname)).setItemState(SEMState.STATE_RESET);
		}
		return new STAFResult(STAFResult.Ok);
	}

	/**
	 * @returns STAFResult.OK, STAFResult.InvalidParam, 
	 * STAFResult.RequestNotComplete if the waiting thread gets Interrupted while waiting.
	 */
	private STAFResult eventWait(String handleName, String eventname, long msTimeout) {
		debug("EmbeddedSemService().eventWait '"+eventname +"', timeout: "+ msTimeout +", for '"+ handleName +"'");
		if(eventname == null || eventname.length()== 0){ 
			debug("EmbeddedSemService().eventWait '"+eventname +"' is invalid.");
			return new STAFResult(STAFResult.InvalidParm, "EmbeddedSemService.eventWait() event name invalid: "+ eventname);
		}
		if(!events.containsKey(eventname)) {
			events.put(eventname, new SEMState(eventname, SEMState.STATE_WAITING));
		}
		SEMState state = (SEMState) events.get(eventname);
		if(state.getItemState()!= SEMState.STATE_POSTED){
			// blocks until event is pulsed or posted
			try{
				debug("EmbeddedSemService().eventWait '"+eventname +"' adding waiter "+ handleName);
				state.addWaiterId(handleName);
				STAFResult rc = state.addWaiter(msTimeout);
				debug("EmbeddedSemService().eventWait '"+eventname +"' removing waiter "+ handleName);
				state.removeWaiterId(handleName);
				return rc;
			}catch(InterruptedException x){
				debug("EmbeddedSemService().eventWait '"+eventname +"' waiter "+ handleName +" thread Interrupted!");
				debug("EmbeddedSemService().eventWait '"+eventname +"' removing waiter "+ handleName);
				state.removeWaiterId(handleName);
				return new STAFResult(STAFResult.RequestNotComplete, "InterruptedException during wait for event "+ eventname);
			}
		}
		return new STAFResult(STAFResult.Ok);
	}

	/**
	 * Request an exclusive lock on a Mutex.
	 * <p>
	 * Blocks the thread until the Mutex lock is obtained.
	 * <p>
	 * Only the owner of the Mutex can successfully release it.
	 * <p>
	 * @returns STAFResult.OK, STAFResult.InvalidParam, 
	 * STAFResult.RequestNotComplete if the waiting thread gets Interrupted while waiting.
	 * @see #mutexRelease(RequestInfo, String)
	 * @see MutexState#requestMutex(String) 
	 */
	private STAFResult mutexRequest(String handleName, String mutexname) {
		debug("EmbeddedSemService().mutexRequest() '"+mutexname +"' for handle "+ handleName);
		if(handleName == null || handleName.length()==0 ){
			debug("EmbeddedSemService().mutexRequest() handle '"+handleName +"' is invalid.");
			return new STAFResult(STAFResult.InvalidParm,
					              "EmbeddedSemService.mutexRequest() handleName is invalid!");		
		}
		if(mutexname == null || mutexname.length()== 0) {
			debug("EmbeddedSemService().mutexRequest() '"+mutexname +"' is invalid.");
			return new STAFResult(STAFResult.InvalidParm, 
		                          "EmbeddedSemService.mutexRequest() mutexname is invalid!");		
		}
		if(!mutexs.containsKey(mutexname)){
			mutexs.put(mutexname, new MutexState(mutexname, MutexState.STATE_REQUESTED));
		}
		MutexState state = (MutexState) mutexs.get(mutexname);
		try{
			debug("EmbeddedSemService().mutexRequest() '"+ mutexname +"' adding waiter "+ handleName);
			state.addWaiterId(handleName);
			STAFResult rc = state.requestMutex(handleName); 
			debug("EmbeddedSemService().mutexRequest() '"+ mutexname +"' removing waiter "+ handleName);
			state.removeWaiterId(handleName);
			return rc;
		}catch(InterruptedException x){
			debug("EmbeddedSemService().mutexRequest() '"+ mutexname +"' waiter '"+ handleName +"' thread Interrupted!");
			debug("EmbeddedSemService().mutexRequest() '"+ mutexname +"' removing waiter "+ handleName);
			state.removeWaiterId(handleName);
			return new STAFResult(STAFResult.RequestNotComplete, "InterruptedException during Request for Mutex "+ mutexname);
		}
	}
	
	/**
	 * Release the Mutex when finished with it.
	 * <p>
	 * Only the owner of the Mutex can successfully release it.
	 * @return STAFResult.OK, STAFResult.InvalidParam, STAFResult.SemaphoreDoesNotExist 
	 * @see #mutexRequest(RequestInfo, String)
	 * @see MutexState#releaseMutex(String) 
	 */
	private STAFResult mutexRelease(String handleName, String mutexname) {
		debug("EmbeddedSemService().mutexRelease() '"+mutexname +"' for handle "+ handleName);
		if(handleName == null || handleName.length()==0 ){
			debug("EmbeddedSemService().mutexRequest() handle '"+handleName +"' is invalid.");
			return new STAFResult(STAFResult.InvalidParm,
					              "EmbeddedSemService.mutexRelease() handleName is invalid!");		
		}
		if(mutexname == null || mutexname.length()== 0) {
			debug("EmbeddedSemService().mutexRequest() '"+mutexname +"' is invalid.");
			return new STAFResult(STAFResult.InvalidParm, 
		                          "EmbeddedSemService.mutexRelease() mutexname is invalid!");		
		}
		if(!mutexs.containsKey(mutexname)){
			debug("EmbeddedSemService().mutexRequest() '"+mutexname +"' does not exist.");
			return new STAFResult(STAFResult.SemaphoreDoesNotExist, mutexname);		
		}
		MutexState state = (MutexState) mutexs.get(mutexname);
		return state.releaseMutex(handleName); 
	}

	@Override
	public STAFResult terminateService() {
		return terminate();
	}
}
