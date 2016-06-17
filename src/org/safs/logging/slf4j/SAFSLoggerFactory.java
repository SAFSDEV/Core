/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

package org.safs.logging.slf4j;

import org.safs.SAFSException;
import org.safs.tools.drivers.AbstractDriver;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * SLF4J Logger Factory capable of returning Adapters for the SAFSLOG and the SAFS Debug Log.
 * 
 * @author canagl
 */
public class SAFSLoggerFactory implements ILoggerFactory {

	/** "DEBUG" Name of the Adapter to facilitate SAFS Debug Logging. */
	public static final String DEBUG_LOGNAME = "DEBUG";
	
	/** "TEST" Name of the Adapter to facilitate SAFS Test Logging. */
	public static final String TEST_LOGNAME = "TEST";

	protected final TestLoggerAdapter safslog = new TestLoggerAdapter();
	protected final DebugLoggerAdapter debuglog = new DebugLoggerAdapter();
	protected final CombinedLoggerAdapter duallog = new CombinedLoggerAdapter();
	
	/**
	 * 
	 */
	public SAFSLoggerFactory() {
		super();
	}

	/**
	 * Specify which Logger Adapter is desired.<br>
	 * DEBUG_LOGNAME returns the SAFS Debug Log Adapter.<br>
	 * SAFSLOG_LOGNAME returns the SAFS Test Log Adapter.<br>
	 * An unrecognized or unknown logname will return the SAFS Test Log Adapter.
	 * 
	 * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
	 */
	@Override
	public Logger getLogger(String logname) {
		if(DEBUG_LOGNAME.equalsIgnoreCase(logname)){
			return debuglog;
		}else if(TEST_LOGNAME.equalsIgnoreCase(logname)){
			return safslog;
		}
		return duallog;		
	}
	
	public static void initializeTestLoggerAdapter(Logger logger, AbstractDriver driver){
		if(! (logger instanceof TestLoggerAdapter)) throw new IllegalArgumentException("SLF4J Logger must be a SAFS TestLoggerAdapter");
		if(driver == null) throw new IllegalArgumentException("SAFS Driver used for SLF4J Logger initialization cannot be null!");
		if(driver.getCoreInterface()==null)throw new IllegalArgumentException("SAFS Driver used for SLF4J Logger initialization must be initialized!");
		String logname = null;
		try{ 
			logname = driver.getCoreInterface().getLogName(); 
			logname = logname.split(";")[1]; // format: 1;logname;???
		}catch(SAFSException x){logname = null;}
		if(logname==null || logname.length()==0)throw new IllegalArgumentException("SAFS Logging must already be running with a Log name for SLF4J use.");
		((TestLoggerAdapter)logger).setLogFacName(logname);
	}
}
