/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent;

import org.safs.Log;

import com.ibm.staf.STAFResult;

/**
 * When injected into a JVM will monitor for a SEM JVMShutdown POST.
 * Thus, the JVM must be launched with the following option for this monitor to be enabled:
 * <p>
 * <ul>-DJVMShutdownMonitor=true</ul>
 * <p>
 * An example STAF command that can trigger this event is:
 * <p>
 * <ul>STAF local SEM EVENT JVMShutdown POST</ul>
 * 
 * @author CANAGL
 */
public class ShutdownEventMonitor extends SEMEventMonitor {

	/** 'JVMShutdown' */
	public static final String SHUTDOWN_EVENT    = "JVMShutdown";
	/** 'JVMShutdownMonitor' */
	public static final String SHUTDOWN_PROPERTY = "JVMShutdownMonitor";
	
	/**
	 * Default constructor that simply forwards to the superclass.
	 */
	public ShutdownEventMonitor() {
		super();
	}

	protected void setEventCriteria() {
		_event = SHUTDOWN_EVENT;
		_system = SHUTDOWN_PROPERTY;
	}
	
	/**
	 * Perform a System.exit(0) shutdown.
	 */
	protected void performAction(){
		//resetTrigger();
		Log.info(getClass().getName()+" received event "+ _event);
		// we are in the _thismon thread
		_shutdown = true;		
		try{ if(_stafmon.isAlive()) _stafmon.interrupt(); }
		catch(Exception x){}		
		// let things settle down that may need to
		try{Thread.sleep(100);}
		catch(InterruptedException ie){;}		
		System.exit(0);
	}	
}
