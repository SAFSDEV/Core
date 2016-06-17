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
 * <p>
 * The Factory supports 3 kinds of Loggers that can be retrieved by Name:
 * <p><ol>
 *    <li>"TEST"  -- Logger writes only to the active SAFS Test Log.
 *    <li>"DEBUG" -- Logger writes only to the active SAFS Debug Log--if running.
 *    <li>&lt;other> -- Anything else gets a single Logger that writes to both:
 *    <ul>
 *       <li>info, warn, error messages to SAFS Test Log.
 *       <li>debug, trace to SAFS Debug Log--if running.
 *    </ul>
 * </ol>
 * <p>
 * At this time, SAFS Loggers that write to the SAFS Test Log *MUST* be further initialized AFTER they 
 * are returned by the SAFSLogFactory. &nbsp;Since these Adapters are primarily intended for integrating  
 * non-SAFS tools using SLF4J logging into the SAFS framework or test, this is usually not an issue.
 * <p>
 * The typical mechanism for using the SAFS Logger Adapters would be:
 * <p><ul><code>
		Logger logger = LoggerFactory.getLogger(MySAFSAwareClass.class);
		SAFSLoggerFactory.initializeTestLoggerAdapter(logger, driver);
 * </code></ul>
 * 
 * @author Carl Nagle
 * @see #initializeTestLoggerAdapter(Logger, AbstractDriver)
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
	 * An unrecognized or unknown logname/classname will return the SAFS Combined Logger Adapter.
	 * 
	 * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
	 * @see DebugLoggerAdapter, TestLoggerAdapter, CombinedLoggerAdapter
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

	/**
	 * At this time, SAFS Loggers that write to the SAFS Test Log *MUST* be further initialized AFTER they 
	 * are returned by the SAFSLogFactory. &nbsp;Since these Adapters are primarily intended for integrating  
	 * non-SAFS tools using SLF4J logging into the SAFS framework or test, this is usually not an issue.
	 * <p>
	 * The typical mechanism for using the SAFS Logger Adapters would be:
	 * <p><ul><code>
			Logger logger = LoggerFactory.getLogger(MySAFSAwareClass.class);
			SAFSLoggerFactory.initializeTestLoggerAdapter(logger, driver);
	 * </code></ul>
	 * @param logger
	 * @param driver --AbstractDrivers like JSAFSDriver, SAFSDRIVER, and STAFProcessContainer providing access 
	 * to a SAFS CoreInterface (STAFHelper subclass).
	 */
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
