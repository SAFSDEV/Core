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
