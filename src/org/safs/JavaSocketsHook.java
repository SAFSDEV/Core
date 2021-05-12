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
package org.safs;

import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.safs.android.remotecontrol.SAFSRemoteControl;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogUtilities;
import org.safs.sockets.DebugListener;
import org.safs.sockets.Message;
import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;

import com.jayway.android.robotium.remotecontrol.solo.SoloRemoteControl;


/**
 * A class to extend the standard JavaHook allowing the integration/control of a "remote"
 * engine controlled via TCP Sockets where STAF is NOT available.  This class implements
 * an interface between a "local" STAF-controlled engine agent and the actual "remote" engine
 * communicating via TCP Sockets.
 * @author Carl Nagle
 */
public abstract class JavaSocketsHook extends JavaHook implements DebugListener  {

	/**
	 * Not instantiated until the call to {@link #createProtocolRunner()}.
	 */
	protected SAFSRemoteControl tcpServer = null;

	/**
	 * Default remote connection timeout is 30 seconds.
	 * This timeout is used AFTER the tcpServer has been started and, in theory,
	 * the remote engine was already remotely launched.  Thus, the time to wait for
	 * the remote Connection being made should be relatively short.
	 * @see SoloRemoteControl#waitForRemoteConnected(int)
	 */
	protected int remoteConnectionTimeout = 30;

	/**
	 * Default remote Ready timeout is 10 seconds.
	 * This timeout is used AFTER the remote connection has already been established, and
	 * AFTER the Results of a previous start loop execution have been received and processed.
	 * Thus, the time to wait for the remote Ready signal should be relatively short.
	 * @see SoloRemoteControl#waitForRemoteReady(int)
	 */
	protected int remoteReadyTimeout = 10;

	/**
	 * Default remote Running timeout is 10 seconds.
	 * This timeout is used AFTER the remote Ready has already been established, and
	 * AFTER we have issued the remote Dispatch.
	 * Thus, the time to wait for the remote Running signal should be relatively short.
	 * @see SoloRemoteControl#waitForRemoteRunning(int)
	 */
	protected int remoteRunningTimeout = 10;

	/**
	 * The standard data TestRecordHelper simply cast to a SocketTestRecordHelper type for convenience.
	 */
	protected SocketTestRecordHelper socketdata = null;

	boolean remoteShutdown = false;
	int     remoteLogtype;

	/**
	 */
	public JavaSocketsHook() {;}

	/**
	 * @param process_name
	 */
	public JavaSocketsHook(String process_name) {
		super(process_name);
	}

	/**
	 * @param process_name
	 * @param logs
	 */
	public JavaSocketsHook(String process_name, LogUtilities logs) {
		super(process_name, logs);
	}

	/**
	 * @param process_name
	 * @param trd_name
	 */
	public JavaSocketsHook(String process_name, String trd_name) {
		super(process_name, trd_name);
	}

	/**
	 * @param process_name
	 * @param trd_name
	 * @param logs
	 */
	public JavaSocketsHook(String process_name, String trd_name, LogUtilities logs) {
		super(process_name, trd_name, logs);
	}

	/**
	 * @param process_name
	 * @param trd_name
	 * @param logs
	 * @param trd_data
	 * @param gui_utils
	 * @param aprocessor
	 */
	public JavaSocketsHook(String process_name, String trd_name, LogUtilities logs,
			               SocketTestRecordHelper trd_data, DDGUIUtilities gui_utils,
			               ProcessRequest aprocessor) {
		super(process_name, trd_name, logs, trd_data, gui_utils, aprocessor);
	}

	/**
	 * Invoked during start loop initialization.
	 * Allows subclasses to perform any necessarily initialization prior to the
	 * call to {@link #launchRemoteEngine()}.  The default implementation does
	 * nothing and returns true to allow execution to proceed.
	 *
	 * @return true to proceed with engine execution.
	 * false will cause an abort of the engine startup procedure and any attempt
	 * to launch the remote engine.
	 */
	protected boolean beforeLaunchRemoteEngine(){ return true; }

	/**
	 * Invoked during start loop initialization.
	 * Allows subclasses to initiate the launching of a remote engine prior
	 * to starting the local TCP Server and entering the actual working loop.
	 * @return true to proceed with engine execution.
	 * Returning false will cause an abort of the engine startup procedure.
	 */
	protected abstract boolean launchRemoteEngine();

	/**
	 * Invoked during start loop initialization.
	 * Allows subclasses to perform any work after a successful call to {@link #launchRemoteEngine()}
	 * but prior to the entry to the main start loop.
	 * The default implementation does nothing and returns true to allow execution to proceed.
	 *
	 * @return true to proceed with engine execution.
	 * Returning false will cause an abort of the engine startup procedure.
	 */
	protected boolean afterLaunchRemoteEngine(){ return true; }

	/**
	 * Invoked during start loop initialization.
	 * Creates the default instance of our abstractProtocolRunner with the hook process_name and provides
	 * this hook as the default NamedListener for the server.
	 * @return true to allow normal execution to proceed.
	 * Returning false will cause an abort of the engine startup procedure.
	 */
	protected abstract boolean createProtocolRunner();

	/**
	 * Invoked during start loop initialization.
	 * Creates the default instance of the tcpServerThread providing the current tcpServer Runnable
	 * as its argument.  The tcpServerThread is then immediately started.
	 * @return true to allow normal execution to proceed.
	 * Returning false will cause an abort of the engine startup procedure.
	 */
	protected boolean startProtocolRunner() {
	    try{
	    	tcpServer.start();
		    return true;
	    }catch(Exception x){ return false; }
	}

	/**
	 * Retrieve the SocketTestRecordHelper used by the subclass.
	 * If no SocketTestRecordHelper has been set (data == null), then the implementation is expected
	 * to instantiate and return a SocketTestRecordHelper instance appropriate for the hook.
	 * @throws SAFSRuntimeException if the instance is NOT a SocketTestRecordHelper subclass.
	 */
	@Override
	public TestRecordHelper getTRDData(){
		if (data==null) {
			data=new SocketTestRecordHelper();
			data.setSTAFHelper(getHelper());
			data.setDDGUtils(getGUIUtilities());
		}
		if(!(data instanceof SocketTestRecordHelper))
			throw new SAFSRuntimeException("Java Sockets Hooks require a SocketTestRecordHelper for proper operation.");
		socketdata = (SocketTestRecordHelper) data;
		return data;
	}

	/**
	 * Force the use of a SocketTestRecordHelper as required by the underlying remote engine hook.
	 * @throws SAFSRuntimeException if the instance is NOT a SocketTestRecordHelper subclass.
	 */
	@Override
	public void setTRDData(TestRecordHelper trd_data){
	  	if(trd_data instanceof SocketTestRecordHelper) {
	  		data = trd_data;
	  		if(data.getSTAFHelper()==null) data.setSTAFHelper(getHelper());
	  		if(data.getDDGUtils()==null) data.setDDGUtils(getGUIUtilities());
	  		socketdata = (SocketTestRecordHelper) data;
	  	}else{
	  		throw new SAFSRuntimeException("Java Sockets Hooks require a SocketTestRecordHelper for proper operation.");
	  	}
	}

	/**
	 * Called by the running loop to extract whatever remoteResultProperties were received.
	 * Subclasses will extend this to extract additional properties.
	 */
	protected void interpretResultProperties(Properties remoteResultProperties){
	  // get statuscode and statusinfo
	  String temp = remoteResultProperties.getProperty(Message.KEY_REMOTERESULTCODE);
	  if(temp != null) data.setStatusCode(Integer.parseInt(temp));
	  temp = remoteResultProperties.getProperty(Message.KEY_REMOTERESULTINFO);
	  if(temp != null) data.setStatusInfo(new String(temp));
	  switch(data.getStatusCode()){
	  	  case StatusCodes.GENERAL_SCRIPT_FAILURE:
	  	  case StatusCodes.INVALID_FILE_IO:
	  	  case StatusCodes.NO_RECORD_TYPE_FIELD:
	  	  case StatusCodes.SCRIPT_NOT_EXECUTED:
	  	  case StatusCodes.UNRECOGNIZED_RECORD_TYPE:
	  	  case StatusCodes.WRONG_NUM_FIELDS:
	  		  remoteLogtype = AbstractLogFacility.FAILED_MESSAGE;
	  		  break;
	  	  case StatusCodes.SCRIPT_WARNING:
	  		  remoteLogtype = AbstractLogFacility.WARNING_MESSAGE;
	  		  break;
	  	  default:
	  		  remoteLogtype = AbstractLogFacility.GENERIC_MESSAGE;
	  		  if(processor.getTestStepProcessor().isComponentFunctionRecord(data.getRecordType()))
	  			  remoteLogtype = AbstractLogFacility.PASSED_MESSAGE;
	  }
	}

	/**
	 * Called by the running loop when it is time to log the results of the executed command.
	 * Implementations should
	 * #see {@link LogUtilities#logMessage(String, String, int, String)}
	 */
	protected abstract void logResultsMessage();

	/**
	 * Normal JavaHook start loop initialization and looping is augmented with additional requirements to
	 * handle the communications and synchronization with a remote socket-controlled engine as follows:
	 * <ol>
	 * <li>normal initialization for STAFHelper, GUIUtilities, TestRecordHelper, and RequestProcessor.
	 * <li>{@link #createProtocolRunner()}
	 * <li>{@link #beforeLaunchRemoteEngine()}
	 * <li>{@link #launchRemoteEngine()}
	 * <li>{@link #afterLaunchRemoteEngine()}
	 * <li>{@link #startProtocolRunner()}
	 * <li>{@link SoloRemoteControl#waitForRemoteConnected(int)}
	 * </ol>
	 * @see org.safs.JavaHook#start()
	 */
	@Override
	public void start() {
	    if (getHelper() == null)
	        throw new SAFSProcessorInitializationException
	        (getClass().getName()+":STAFHelper not initialized for this process!");

	    boolean shutdown = false;
	    String  testrecord = null;
	    int rc = 99;  // 99 is just some bogus RC
	    String COLON_SEP = ":";
	    try {
	      Log.info("JavaSocketsHook resetting hook events for default behavior initialization...");
	      helper.resetHookEvents(semaphore_name);

	      if (utils == null) utils = getGUIUtilities();
	      if (data == null) data = getTRDData();
	      socketdata = (SocketTestRecordHelper) data; // just in-case
	      socketdata.setSTAFHelper(helper);
	      socketdata.setDDGUtils(utils);
	      utils.setTestRecordData(socketdata);
	      log.setSTAFHelper(helper);

	      Log.info("JavaSocketsHook insuring processors have been initialized...");
	      if (processor == null) processor = getRequestProcessor();

	      Log.info("JavaSocketsHook attempting to create SocketProtocol runner...");
	      boolean proceed = false;
	      try{ proceed = createProtocolRunner(); }
	      catch(Throwable e){
	    	  Log.debug("createProtocolRunner Throwable: "+ e.getClass().getName()+", "+ e.getMessage());
	      }
	      if(proceed) {
	    	  proceed = beforeLaunchRemoteEngine();
		      if(proceed) {
		    	  proceed = launchRemoteEngine();
			      if(proceed) {
			    	  proceed = afterLaunchRemoteEngine();
				      if(proceed) {
				    	  proceed = startProtocolRunner();
					      if(!proceed){
						      Log.debug("JavaSocketsHook failed in startProtocolRunner() initialization.");
					      }
				      }else{
					      Log.debug("JavaSocketsHook failed in afterLaunchRemoteEngine() initialization.");
				      }
			      }else{
				      Log.debug("JavaSocketsHook failed in launchRemoteEngine() initialization.");
			      }
		      }else{
			      Log.debug("JavaSocketsHook failed in beforeLaunchRemoteEngine() initialization.");
		      }
	      }else{
		      Log.debug("JavaSocketsHook failed to create SocketProtocol runner!");
	      }
	      if(proceed){
		      try{
		    	  tcpServer.waitForRemoteConnected(remoteConnectionTimeout);
		      }catch(RemoteException x){
		    	  // TODO:
		    	  shutdown = true;
		      }catch(TimeoutException x){
		    	  // TODO:
		    	  shutdown = true;
		      }catch(ShutdownInvocationException x){
		    	  // TODO:
		    	  shutdown = true;
		      }
	      }else{
		      Log.debug("JavaSocketsHook failed initialization and will be shutting down!");
	    	  shutdown = true;
	      }

	      while(!shutdown){

	    	  try{ tcpServer.waitForRemoteReady(remoteReadyTimeout); }
	    	  catch(RemoteException x){
				  Log.debug("waitForRemoteReady RemoteException SHUTDOWN due to "+ x.getClass().getSimpleName()+":"+ x.getMessage(), x);
	    		  shutdown = true; }
	      	  catch(ShutdownInvocationException x){
				  Log.debug("waitForRemoteReady ShutdownInvocationException SHUTDOWN due to "+ x.getClass().getSimpleName()+":"+ x.getMessage(), x);
	      		  remoteShutdown = shutdown = true; }
	    	  catch(TimeoutException x){
				  Log.debug("waitForRemoteReady TimeoutException SHUTDOWN due to "+ x.getClass().getSimpleName()+":"+ x.getMessage(), x);
	    		  shutdown = true;
	    	  }

	          helper.postEvent(semaphore_name + "Ready");
	          if(!shutdown){
	        	  try {
		              testrecord = helper.getNextHookTestEvent(semaphore_name, trd_name);
		              if (testrecord.equals(SHUTDOWN_RECORD)) {
		                   shutdown = true;
		              }
		          } catch (SAFSException safsex) {
		              log.logMessage(null, safsex.toString(), WARNING_MESSAGE);
		              shutdown = true;
		          }
	          }
	          if (!shutdown) {
	              socketdata.reinit(); // reset the data to starting point
		          try{
		              socketdata.setInstanceName(trd_name);
		              socketdata.populateDataFromVar();
		              socketdata.setReadyTimeout(remoteReadyTimeout);
		              socketdata.setRunningTimeout(remoteRunningTimeout);
		              try{
		            	  // doRequest locally which sets ALL socketdata.Properties
		            	  // which can then be dispatched to the remote engine, if needed.
		            	  processor.doRequest();

		            	  // most processor subclasses likely will do remote processing internally,
		            	  // so this below section will NOT normally be executed.
		            	  if(socketdata.processRemotely()){
	            			  socketdata.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
	            			  socketdata.setStatusInfo(null);
	            			  try{
		            			  Properties results = tcpServer.performRemotePropsCommand(socketdata.getKeywordProperties(), remoteReadyTimeout, remoteRunningTimeout, socketdata.getCommandTimeout());
	            				  remoteLogtype = AbstractLogFacility.GENERIC_MESSAGE;
	            				  interpretResultProperties(results);
	            				  // TODO: Process anyything else in results ?
		            			  logResultsMessage();
					          }
	            			  catch(RemoteException x){
	            				  Log.debug("performRemoteProps RemoteException SHUTDOWN due to "+ x.getClass().getSimpleName()+":"+ x.getMessage(), x);
	            				  shutdown = true; }
	            			  catch(TimeoutException x){
	            				  Log.debug("performRemoteProps TimeoutException SHUTDOWN due to "+ x.getClass().getSimpleName()+":"+ x.getMessage(), x);
	            				  shutdown = true; }
	            			  catch(ShutdownInvocationException x){
	            				  Log.debug("performRemoteProps ShutdownInvocationException SHUTDOWN due to "+ x.getClass().getSimpleName()+":"+ x.getMessage(), x);
	            				  remoteShutdown = shutdown = true; }
		            	  }
		              }
		              catch (SAFSRuntimeException ex) {
		                  long response = evaluateRuntimeException (ex);
		                  if (response == REQUEST_USER_STOPPED_SCRIPT_REQUEST) shutdown = true;
		              }
  		              socketdata.sendbackResponse();
		          }
		          catch (SAFSException ex) {
		            System.err.println( errorText.convert(GENERIC_ERROR,
		                                                 "ERROR :"+ ex.getMessage(),
		                                                  ex.getMessage()));
		          }
	          }
	          helper.setHookTestResults(semaphore_name);
	          helper.resetEvent(semaphore_name + "Running");
	      };//end while loop

	      helper.resetHookEvents(semaphore_name);
	      helper.postEvent(semaphore_name +"Shutdown");

	    } catch(SAFSException e){
	    	String txt = errorText.convert(STAF_ERROR, "STAF ERROR :"+ e.getClass().getSimpleName()+", "+ e.getMessage(),
                    	                   e.getClass().getSimpleName()+", "+e.getMessage());
	    	Log.debug(txt);
	        System.err.println(txt);
	    } finally {
	        if (hook_shutdown() && allowSystemExit()) System.exit(0);
	    }
	}

	/**
	 * Sends a shutdown command to the tcpServer if the tcpServer
	 * is NOT the one that initiated the shutdown.
	 * Also removes ourselves as the SocketServerListener for the
	 * tcpServer and nulls out our to the tcpServer.
	 * Then continues by invoking the superclass hook_shutdown.
	 * @see org.safs.JavaHook#hook_shutdown()
	 */
	@Override
	protected boolean hook_shutdown() {
		try{
			disconnectSocketServer();
		}catch(Exception x){
			Log.debug("JavaSocketsHook.hook_shutdown activities ignoring "+ x.getClass().getSimpleName());
		}
		return super.hook_shutdown();
	}

	protected void disconnectSocketServer(){
		try{
			if(!remoteShutdown ){
				tcpServer.shutdown();
			}
			//tcpServer.removeListener(this);
			tcpServer = null;
		}catch(Exception x){}
	}

	/* SocketServerListener implementation */

	/**
	 * Default implementation returns our process_name.
	 */
	@Override
	public String getListenerName() {
		return this.process_name;
	}
	/**
	 * Default implementation logs the message to the SAFS Debug Log.
	 */
	@Override
	public void onReceiveDebug(String message) {
		Log.debug(message);
	}
}
