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
