/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent;

import org.safs.STAFHelper;
import org.safs.SAFSSTAFRegistrationException;
import org.safs.Log;
import org.safs.rmi.engine.Server;

import com.ibm.staf.STAFResult;

/**
 * Monitors the STAF SEM service for a specific event and executes an action 
 * upon receiving the event trigger.  The monitor will only perform this function 
 * if isTriggerEnabled returns true during initialization. 
 * <p>
 * This superclass is a full implementation.  Subclasses will generally override the 
 * setEventCriteria, isTriggerEnabled, and performAction functions to provide 
 * different capabilities.
 * <p>
 * This type of mechanism is needed because one of the intended applications of subclasses 
 * is to be injected in each JVM via the Java Extensions mechanism often used 
 * for assistive technologies.  While the object will be injected into every JVM, we only 
 * want the object to respond and act in specific JVM instances, not all JVM instances.
 * <p>
 * This superclass will list all the key=value pairs in the JVM System.properties when it 
 * detects the 'ShowProperties' event.  The enabling item for our trigger is the existence 
 * of a 'SEMEventMonitor' System.property in the JVM.  JVMs not containing this property 
 * will not respond to the trigger and the monitor will go dormant.
 * <p>
 * Thus, the JVM must be launched with the following option for this monitor to be enabled:
 * <p>
 * <ul>-DSEMEventMonitor=true</ul>
 * <p>
 * An example STAF command that can trigger this event is:
 * <p>
 * <ul>STAF local SEM EVENT ShowProperties POST</ul>
 * 
 * @author canagl
 */
public class SEMEventMonitor implements Runnable{
	
	/** 'ShowProperties' */
	public static final String DEFAULT_EVENT    = "ShowProperties";
	/** 'SEMEventMonitor' */
	public static final String DEFAULT_PROPERTY = "SEMEventMonitor";
	
	/** The process name that will be registered with STAF */
	protected String       _process  = null;

	/** The STAF SEM event trigger that will be monitored. */
	protected String       _event    = DEFAULT_EVENT;
	
	/** The JVM System.property used to enable the trigger (for this implementation). */
	protected String       _system   = DEFAULT_PROPERTY;
	
	/** The STAFHelper we use to talk with STAF. */
	protected STAFHelper   _staf     = new STAFHelper();
	
	/** set true to cause this class to go dormant and die. */
	protected boolean      _shutdown = false; 	
	
	/** The milliseconds to wait between checks for STAF and the SEM event. */
	protected long         _msdelay  = 2500;

	/** The helper STAFMonitor thread that monitors STAF. */
	protected STAFMonitor _stafmon;
	
	/** The Thread created by this Runnable instance when monitoring the SEM event. */
	protected Thread      _thismon;
	
	/**
	 * Construct a monitor for the event that will be designated during a constructor  
	 * call to function setEventName.  initialize() is then called after setEventCriteria().
	 */
	public SEMEventMonitor() {
		super();
		Log.info("Initializing new "+ getClass().getName());
		setEventCriteria();
		initialize();
	}

	/**
	 * Subclasses override to designate a unique event to monitor.  This function will be 
	 * called during object construction.  The default setting to monitor is DEFAULT_EVENT.
	 * <p>
	 * This default implementation also sets the key name in System.properties that enables 
	 * the trigger.  This key name is stored in the local _system field.  
	 * The default setting here is DEFAULT_PROPERTY.
	 */
	protected void setEventCriteria() {
		Log.info(getClass().getName() +" setting event name...");
		_event = DEFAULT_EVENT;
		Log.info(getClass().getName() +" setting system property name...");
		_system = DEFAULT_PROPERTY;
	}
	
	/**
	 * Subclasses override to provide their own enabling algorithm for the performAction function.
	 * This method is called during initialize() AFTER the call to setEventCriteria().  This 
	 * function should evaluate whatever it needs to evaluate to determine that the particular 
	 * class instance should be allowed to perform its function.
	 * <p>
	 * This superclass implementation checks to see if the System.property stored in the _system field  
	 * is defined in this JVM.  The routine will only return true if this JVM has that System.property.  
	 * This _system field can be set in the setEventCriteria() method if subclasses wish to 
	 * use this particular isTriggerEnabled() implementation.
	 * 
	 * @return true if the expected System.property is set in this JVM.  Subclasses may provide 
	 * alternate implementations.
	 */
	protected boolean isTriggerEnabled(){
		String prop = System.getProperty(_system);
		try{ if(prop.length()> 0) return true; }
		catch(Exception x){}
		return false;
	}
	
	/**
	 * Subclasses should not normally ever need to override this.
	 * This function first calls isTriggerEnabled to verify the operating environment 
	 * and\or JVM is the one we want to act upon.  If the result of this call is 'false' 
	 * then STAF registration will not be attempted and the class instance will essentially 
	 * go dormant and die.
	 * <p>
	 * If isTriggerEnabled returns 'true' then will will register a new STAFMonitor to 
	 * make the connection with STAF and we will begin monitoring for our special event in 
	 * a our own run() method as a new Runnable thread.
	 * <p> 
	 * The process name used to register with STAF is:
	 * <p>
	 * <ul>_event +"Monitor"</ul>
	 */
	protected void initialize(){
		_process = _event +"Monitor";
		try{
			// only run if this is the right JVM
			if(isTriggerEnabled()){
				Log.info(getClass().getName() +" initializing STAF connection...");
				STAFMonitor stafmonitor = new STAFMonitor();
				stafmonitor.start();
				_thismon = new Thread(this);
				_thismon.start();
			}
			else{
				Log.info(getClass().getName()+" inappropriate for this JVM instance.");
				_shutdown = true;
			}
		}
		catch(Exception x){
			Log.info(getClass().getName()+" inappropriate for this JVM instance.");
			_shutdown = true;
		}
	}

	/**
	 * RESET the SEM Event trigger we are monitoring.
	 * This is an optional call often performed in the performAction() function.
	 */
	protected void resetTrigger(){
		String reset = "EVENT " + _event + " RESET";
		STAFResult result = _staf.submit2("local", "SEM", reset);
	}

	/**
	 * Default action is to list all System.getProperty values in the org.safs.Log
	 * Subclasses will likely want to override this.
	 */
	protected void performAction(){
		resetTrigger();
		Log.info(getClass().getName()+" received event "+ _event);
		java.util.Enumeration list = System.getProperties().keys();
		String item;
		String value;
		while(list.hasMoreElements()){
		    item = (String) list.nextElement();
		    value = System.getProperty(item);
		    Log.info("System Property: "+ item +"="+ value);
		}
	}
	
	/**
	 * This is the Runnable thread that will monitor the trigger SEM event.
	 * The initialize() function will launch this new Thread if isTriggerEnabled() 
	 * was true and we successfully registered with STAF.
	 * <p>
	 * Subclasses should not have to override this.  The default implementation does NOT
	 * reset the trigger event, however, so subclasses wishing to react to multiple occurrences 
	 * of the trigger will need to reset the event in their performAction function.
	 */
	public void run(){
		STAFResult result;
	    String wait = "EVENT " + _event + " WAIT 1";
		while(!_shutdown){
			synchronized(_staf){
				if( _staf.isInitialized()){					
					result = _staf.submit2("local", "SEM", wait);											
					if(result.rc==STAFResult.Ok) 
						performAction();
					
					// event has not yet occurred
					else if (result.rc != STAFResult.Timeout){
						Log.info(getClass().getName()+" event monitor rc: "+ result.rc);
					}
				}
			}
			// sleep
			if(! _shutdown){
				try{Thread.sleep(_msdelay);}
				catch(InterruptedException ie){;}
			}
	    }
	}	

	/**
	 * Allow ourselves to disconnect from STAF.
	 */
	protected void finalize() throws Exception{
		// sometimes the Log is shutdown first
		try{ Log.info(getClass().getName()+".finalize");}catch(Exception x){}
		_shutdown = true;
		try{
			if (_staf.isInitialized()){
				_staf.unRegister();
				_staf = new STAFHelper(); // uninitialized
			}
		}
		catch(Exception x){}
	}

	/**
	 * Polls for the existence of STAF every few seconds until found.
	 * This is started at JVM bootup and remains running until 
	 * satisfied.
	 */
	protected class STAFMonitor extends Thread {
		public void run(){
			while(!_shutdown){
				synchronized(_staf){
					if (! _staf.isInitialized()){
						try{
							_staf.initialize( _process );
							Log.setHelper(_staf);
							Log.info(getClass().getName()+" registered "+ _process);	
						}
						catch(Exception re){}
					}
					// _staf IS inititialized
					else{
						// make sure STAF is still running
						try{
							_staf.submit("local", "PING", "PING");
						}
						// reset STAF connection and keep monitoring
						catch(Exception x){
							Log.info(getClass().getName()+" UNregistering "+ _process);	
							try{_staf.unRegister();}
							catch(Exception x2){;}
							_staf = new STAFHelper();
						}
					}
				}
				if(! _shutdown){
					try{ sleep(_msdelay);}
					catch(InterruptedException ie){;}
				}
			}
		}
	}
	
	/**
	 * For test\debug purposes
	 * must launch test JVM with -DSEMEventMonitor=true
	 */ 
	public static void main(String[] args){
		SEMEventMonitor test = new SEMEventMonitor();
	}
}