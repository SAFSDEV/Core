/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs;

import java.util.Properties;


/**
 * @author Carl Nagle
 */
public class SocketTestRecordHelper extends TestRecordHelper {

	/** Keyword-specific properties/values passed via a SocketServer to a remote engine.*/
	Properties props = null;
	boolean processRemotely = false;
	private int commandTimeout;
	private int readyTimeout;
	private int runningTimeout;
	
	/**
	 * 
	 */
	public SocketTestRecordHelper() {
		super();
	}

	/** 
	 * Can be null if no additional properties/values are needed for remote execution. 
	 */
	public Properties getKeywordProperties(){ return props; }
	
	/** 
	 * Can set to null if no additional properties/values are needed for remote execution. 
	 */
	public void setKeywordProperties(Properties _props) { props = _props;}
	
	/**
	 * Add a keyword property to the Properties list.
	 * If the Properties list does not yet exist, it will be created prior to 
	 * adding the new keyword property.
	 * @param propname case-insensitive name of the property.  It will be stored 
	 * and transmitted in lowerCase.
	 * @param propvalue case-sensitive value for the property.
	 */
	public void addKeywordProperty(String propname, String propvalue){
		if(props == null) setKeywordProperties(new Properties());
		props.setProperty(propname.toLowerCase(), propvalue);
	}
	
	/**
	 * Get a keyword property from the Properties list.
	 * @param propname case-insensitive name of the property.  It will be converted 
	 * to lowerCase before use.
	 * @return String value of the property, or null.
	 */
	public String getKeywordProperty(String propname){
		if(props == null) return null;
		return props.getProperty(propname.toLowerCase());
	}
	
	/**
	 * @return true if the underlying engine is expected to dispatch the command to 
	 * a remote engine.  return false if the keyword was handled locally in its entirety.
	 */
	public boolean processRemotely() {
		return processRemotely;
	}

	/**
	 * Keyword implementors will set this to true if the keyword is expecting a remote 
	 * engine to complete the processing of the inputrecord. 
	 * @param processRemotely set true if the underlying engine is expected to dispatch the command to 
	 * a remote engine.  set false if the keyword was handled locally in its entirety.
	 */
	public void setProcessRemotely(boolean processRemotely) {
		this.processRemotely = processRemotely;
	}

	/**
	 * @return the commandTimeout
	 */
	public int getCommandTimeout() {
		return commandTimeout;
	}

	/**
	 * Every command might want to change its default timeout.
	 * @param commandTimeout -- number of seconds to wait for "Results" after Running before a 
	 * timeout is suspected.
	 */
	public void setCommandTimeout(int commandTimeout) {
		this.commandTimeout = commandTimeout;
	}
	
	/**
	 * @return number of seconds to wait for "Ready" before Dispatch before a 
	 * timeout is suspected.
	 */
	public int getReadyTimeout() {
		return readyTimeout;
	}

	/**
	 * Every command might want to change its default timeouts.
	 * @param readyTimeout - number of seconds to wait for "Ready" before Dispatch before a 
	 * timeout is suspected.
	 */
	public void setReadyTimeout(int readyTimeout) {
		this.readyTimeout = readyTimeout;
	}
	
	/**
	 * @return number of seconds to wait for "Running" after Dispatch before a 
	 * timeout is suspected.
	 */
	public int getRunningTimeout() {
		return runningTimeout;
	}

	/**
	 * Every command might want to change its default timeouts.
	 * @param runningTimeout - number of seconds to wait for Running after Dispatch before a 
	 * timeout is suspected.
	 */
	public void setRunningTimeout(int runningTimeout) {
		this.runningTimeout = runningTimeout;
	}
	
	/**
	 * Set our added fields to their default values during each command initialization:<br>
	 * setKeywordProperties(false);<br>
	 * setProcessRemotely(false);<br>
	 * @see org.safs.TestRecordData#reinit()
	 */
	@Override
	public void reinit() {
		super.reinit();
		setKeywordProperties(null);
		setProcessRemotely(false); 
	}

}
