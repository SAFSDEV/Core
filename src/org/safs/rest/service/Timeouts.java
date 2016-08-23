/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

package org.safs.rest.service;

/**
 * Maintains default timeouts used during REST Requests.<br>
 * Currently, there is only 1 type of timeout specified.<br>
 * Future updates may provide for different timeout settings for different parts of the request and response.
 * @author Carl Nagle
 */
public class Timeouts {

	static Timeouts _defaultTimeouts = new Timeouts();
	
	long _millisToTimeout = 180000;  // 3 minutes

	public static Timeouts getDefaultTimeouts(){
		return _defaultTimeouts;
	}
	public static void setDefaultTimeouts(Timeouts timeouts)throws IllegalArgumentException{
		if(timeouts == null) throw new IllegalArgumentException("Default Timeouts parameter cannot be null.");
		_defaultTimeouts = timeouts;
	}
	
	public Timeouts(){}
	
	public Timeouts(long millisToTimeout) throws IllegalArgumentException{
		setMillisToTimeout(millisToTimeout);
	}
	
	public void setMillisToTimeout(long millisToTimeout) throws IllegalArgumentException{
		if(millisToTimeout < 0) throw new IllegalArgumentException("Timeouts cannot be set less than 0.");
		_millisToTimeout = millisToTimeout;
	}
	
	public long getMillisToTimeout(){
		return _millisToTimeout;
	}
}
