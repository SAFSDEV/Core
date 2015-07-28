/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.android;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.TestRecordHelper;
import org.safs.TestStepProcessor;
import org.safs.android.remotecontrol.SAFSMessage;
import org.safs.logging.LogUtilities;
import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;

/**
 * Primary handler for all CF Functions processors for Android.
 * <br>(CANAGL) May 10, 2012  Added support for "CurrentWindow" and other special recognition strings. 
 */
public class DTestStepProcessor extends TestStepProcessor {

	protected DTestRecordHelper droiddata = null;
	protected Properties props = new Properties();
	protected ArrayList processors = new ArrayList();
	
	public DTestStepProcessor() {
		super();
		processors.add(new CFComponentFunctions()); //routing as needed
	}
	
	@Override
	public void setLogUtilities(LogUtilities _log){
		super.setLogUtilities(_log);
		for(int i=0;i<processors.size();i++) ((CFComponentFunctions)processors.get(i)).setLogUtilities(_log);
	}

	@Override
	public void setTestRecordData(TestRecordHelper data){
		super.setTestRecordData(data);
		droiddata = (DTestRecordHelper) data;
	}
	
	/**
	 * Calls the default processing of interpretFields and then prepares the 
	 * droiddata (testRecordData) with the initial KeywordProperties of:
	 * <p><ul>
	 * KEY_WINNAME=non-null<br>
	 * KEY_COMPNAME=non-null<br>
	 * KEY_WINREC=non-null<br>
	 * KEY_COMPREC=non-null<br>
	 * KEY_COMMAND=non-null<br>
	 * KEY_PARAM_1-N  (param1 - paramN, even if > 9)<br>
	 * </ul>
	 * <p>
	 * Keyword implementations will need to add to these properties:
	 * <p><ul>
	 * KEY_TARGET<br>
	 * KEY_PARAM_TIMEOUT (may be different for each keyword)<br>
	 * </ul>
	 */
	@Override
	protected Collection interpretFields() throws SAFSException{
		Log.info("DTestStepProcessor.interpretFields processing...");
		params = super.interpretFields();
		droiddata = (DTestRecordHelper) testRecordData; //convenience
		props.clear();
		props.setProperty(SAFSMessage.KEY_WINNAME, droiddata.getWindowName());
		props.setProperty(SAFSMessage.KEY_COMPNAME, droiddata.getCompName());
		props.setProperty(SAFSMessage.KEY_COMMAND, droiddata.getCommand());
		Log.info("DTestStepProcessor getting recognition for Window '"+ droiddata.getWindowName()+"' and Component '"+ droiddata.getCompName()+"'");
		droiddata.setKeywordProperties(props);//probably unnecessary
		droiddata = DUtilities.getAppMapRecognition(droiddata);
		Iterator it = params.iterator();
		// "param"
		String key = SAFSMessage.PARAM_1.substring(0, SAFSMessage.PARAM_1.length()-1);
		String tmpkey = null;
		String val = null;
		int i = 1;
		Log.info("DTestStepProcessor processing "+ params.size()+ " '"+ key +"' values...");
		while(it.hasNext()){
			try{
				val = it.next().toString();
				tmpkey = key + String.valueOf(i);
				props.setProperty(tmpkey, val);
			}catch(Exception x){
				Log.debug("DTestStepProcessor "+ x.getClass().getSimpleName()+", "+ x.getMessage());
			}
			i++;
		}		
		droiddata.setKeywordProperties(props);
		droiddata.setProcessRemotely(false); // we do it custom here, instead of in the calling JavaHook
		return params;
	}
	

	/** <br><em>Purpose:</em> process: process the testRecordData
	 * <p>
	 *      At this point the Driver has determined we are dealing with a Test Record.
	 *      A Test Record is one acting on a window or a component within a window.
	 * <p><code>
	 *      Field #1:   The TEST record type (T).
	 * </code><p>
	 *      Subsequent fields would be as follows (with a separator between each field):
	 * <code>
	 * <br> Field:  #2            #3          #4          #5 - N
	 * <br> ==============  ==============  ========  ===============
	 * <br> WINDOWNAME,  COMPONENTNAME,   ACTION,  [PARAMETER(S),]
	 * </code>
	 * <p>
	 *      <em>WINDOWNAME</em> is the name given the window in the appmap that you intend to
	 *      have focus for this test step.
	 * <p>
	 *      <em>COMPONENTNAME</em> is the name of the component within that window you intend
	 *      to perform some function or test on.  If it is the window itself then
	 *      the COMPONENTNAME should be the same as the WINDOWNAME.
	 * <p>
	 *      <em>ACTION</em> is the command or test you wish to perform.  Different types of 
	 *      components support different types of actions.  Almost all support some
	 *      versions of VERIFY actions.  Pushbuttons can be CLICKed etc... Consult 
	 *      each Component's TYPE or CLASS documentation for the actions available for
	 *      the component.
	 * <p>
	 *      <em>PARAMETER(s)</em> are the additional fields needed based upon the action to 
	 *      be completed.  Each action can have its own unique set of parameters.
	 *      Some actions may take no parameters at all.  Consult the component's
	 *      TYPE or CLASS documentation for the parameters needed for a given action.
	 * <p>
	 *      Although the separator used in the example above is a comma, any separator 
	 *      can be used as long as it is specified at the time the file is provided or 
	 *      in subsequent command lines which might change the separator in use.
	 * <p>
	 *      <em>NOTE:</em>
	 *      A user or developer would not normally call this routine.  This
	 *      routine is intended to be called from the StepDriver routine as 
	 *      deemed necessary by the input records of the data table provided to
	 *      the StepDriver routine.  The internals of this routine and the declaration 
	 *      and parameters are all subject to change as necessary.
	 *
	 * <br><em>Side Effects:</em> {@link #testRecordData} statusCode and statusInfo are set
	 * based on the result of the processing
	 * <br><em>State Read:</em>   {@link #testRecordData}
	 **/
	public void process() {
		String debugmsg = getClass().getName()+".process(): ";
	    
        // first interpret the fields of the test record and put them into the
	    // appropriate fields of params and droiddata
	    try{ params = interpretFields();}catch(SAFSException x){
	        Log.debug(debugmsg+"ignoring interpretFields "+ x.getClass().getSimpleName());
	    }
	    //called script MUST set droiddata.statuscode accordingly.
	    //this is one way we make sure the script executed and a script 
	    //command failure was not encountered prematurely.
	    droiddata.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);

	    // check for my breakpoints, but maybe this should go ahead of the 'waitfor...' above??
	    Log.info("checking for active breakpoints...");
	    checkMyBreakpoints(getClass().getName() +" "+genericText.translate("Breakpoint"));
	    
	    CFComponentFunctions cfprocessor = null;
	    
	    Log.info("Attempting "+ droiddata.getCommand() +" via "+ processors.size()+" chained processors...");
	    try{
	    	for(int i=0;
	        droiddata.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED && 
	        							i < processors.size();
	        							i++){
		    	cfprocessor = (CFComponentFunctions)processors.get(i);
		    	cfprocessor.setTestRecordData(droiddata);
		    	cfprocessor.setParams(params);
		    	cfprocessor.processCommand();
		    }
		} catch (IllegalThreadStateException e) {
			Log.debug(debugmsg +" IllegalThreadStateException: "+ e.getMessage(), e);
		} catch (RemoteException e) {
			Log.debug(debugmsg +" RemoteException: "+ e.getMessage(), e);
		} catch (TimeoutException e) {
			Log.debug(debugmsg +" TIMEOUT reached. "+ e.getMessage());
		} catch (ShutdownInvocationException e) {
			Log.debug(debugmsg +" ShutdownInvocationException: "+ e.getMessage(), e);
		} catch (Exception e) {
			Log.debug(debugmsg +" "+ e.getClass().getSimpleName()+", "+ e.getMessage(), e);
		}
	    
		postProcess();
	}
}
